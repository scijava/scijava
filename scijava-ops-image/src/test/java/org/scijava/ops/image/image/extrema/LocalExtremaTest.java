/*-
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.image.image.extrema;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link DefaultLocalExtrema} and {@link LocalExtremaMask}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class LocalExtremaTest extends AbstractOpTest {

	// This 6x5 image has an isolated peak (4,1)=9, a corner peak (0,0)=5, a
	// two-pixel plateau (1,3)=(2,3)=7, a plateau of 1s and a minimum (4,3)=0.
	// Some of the 2s are also non-strict local extrema, hence the thresholds.
	private static final byte[] DATA = { //
		5, 1, 1, 2, 2, 2, //
		1, 1, 1, 2, 9, 2, //
		1, 2, 3, 2, 2, 2, //
		2, 7, 7, 2, 0, 2, //
		2, 2, 2, 2, 2, 2 //
	};

	private RandomAccessibleInterval<UnsignedByteType> image() {
		return ArrayImgs.unsignedBytes(DATA.clone(), 6, 5);
	}

	private List<Point> extrema(final RandomAccessibleInterval<?> img,
		final Shape shape, final Double threshold, final Boolean maxima)
	{
		return ops.op("image.localExtrema").input(img, shape, threshold, maxima)
			.outType(new Nil<List<Point>>()
			{}).apply();
	}

	@Test
	public void testMaxima() {
		final List<Point> maxima = ops.op("image.localExtrema").input(image(),
			null, 4.0).outType(new Nil<List<Point>>()
			{}).apply();
		assertPoints(maxima, 0, 0, 4, 1, 1, 3, 2, 3);
	}

	@Test
	public void testThreshold() {
		assertPoints(extrema(image(), null, 6.0, true), 4, 1, 1, 3, 2, 3);
		assertPoints(extrema(image(), null, 8.0, true), 4, 1);
	}

	@Test
	public void testMinima() {
		// The 1s plateau (top left) touches no darker pixel; nor does 0 at (4,3).
		assertPoints(extrema(image(), null, 1.5, false), 1, 0, 2, 0, 1, 1, 2, 1,
			0, 1, 0, 2, 4, 3);
		assertPoints(extrema(image(), null, 0.5, false), 4, 3);
	}

	@Test
	public void testLargerNeighborhood() {
		// With span 3, the corner peak and the plateau see brighter pixels.
		assertPoints(extrema(image(), new RectangleShape(3, true), 4.0, true), 4,
			1);
	}

	@Test
	public void testMask() {
		final var mask = ArrayImgs.bits(6, 5);
		ops.op("image.localExtrema").input(image(), null, 8.0, true).output(mask)
			.compute();
		final var ra = mask.randomAccess();
		var count = 0;
		for (final var c = mask.localizingCursor(); c.hasNext();) {
			if (c.next().get()) {
				count++;
				assertArrayEquals(new long[] { 4, 1 }, c.positionAsLongArray());
			}
		}
		assertEquals(1, count);
		ra.setPosition(new long[] { 4, 1 });
		assertEquals(true, ra.get().get());
	}

	@Test
	public void testOffsetInput() {
		final var img = Views.translate(image(), 10, 20);
		assertPoints(extrema(img, null, 8.0, true), 14, 21);
	}

	/** Compares against a brute-force search on a random 3D image. */
	@Test
	public void testAgainstBruteForce() {
		final long[] dims = { 9, 7, 5 };
		final var img = ArrayImgs.unsignedBytes(dims);
		final var rng = new Random(0xdeadbeefL);
		for (final var t : img)
			t.set(rng.nextInt(8));

		final List<Point> expected = new ArrayList<>();
		final var ra = img.randomAccess();
		for (var z = 0; z < dims[2]; z++)
			for (var y = 0; y < dims[1]; y++)
				for (var x = 0; x < dims[0]; x++) {
					ra.setPosition(new int[] { x, y, z });
					final var v = ra.get().get();
					var isMax = v >= 3;
					for (var dz = -1; dz <= 1 && isMax; dz++)
						for (var dy = -1; dy <= 1 && isMax; dy++)
							for (var dx = -1; dx <= 1 && isMax; dx++) {
								final int xx = x + dx, yy = y + dy, zz = z + dz;
								if (xx < 0 || yy < 0 || zz < 0 || xx >= dims[0] ||
									yy >= dims[1] || zz >= dims[2]) continue;
								ra.setPosition(new int[] { xx, yy, zz });
								if (ra.get().get() > v) isMax = false;
							}
					if (isMax) expected.add(new Point(x, y, z));
				}

		final var actual = extrema(img, null, 3.0, true);
		assertEquals(expected, actual);
	}

	private static void assertPoints(final List<Point> actual,
		final long... xy)
	{
		final List<Point> expected = new ArrayList<>();
		for (var i = 0; i < xy.length; i += 2)
			expected.add(new Point(xy[i], xy[i + 1]));
		expected.sort((p1, p2) -> {
			final var c = Long.compare(p1.getLongPosition(1), p2.getLongPosition(1));
			return c != 0 ? c : Long.compare(p1.getLongPosition(0), p2
				.getLongPosition(0));
		});
		assertEquals(expected, actual);
	}
}
