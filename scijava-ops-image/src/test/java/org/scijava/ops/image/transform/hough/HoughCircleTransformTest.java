/*
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
package org.scijava.ops.image.transform.hough;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link HoughCircleTransform}.
 *
 * @author Curtis Rueden
 */
public class HoughCircleTransformTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<DoubleType>> VOTES =
		new Nil<>()
		{};

	/**
	 * Draws the discrete sphere (offsets whose norm rounds to the radius) into
	 * the image, and returns its number of pixels.
	 */
	static int drawSphere(final RandomAccessibleInterval<BitType> img,
		final long[] center, final double radius)
	{
		var count = 0;
		final var c = Views.iterable(img).localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			var r2 = 0.0;
			for (var d = 0; d < center.length; d++) {
				final var dx = c.getDoublePosition(d) - center[d];
				r2 += dx * dx;
			}
			if (Math.round(Math.sqrt(r2)) == Math.round(radius)) {
				c.get().set(true);
				count++;
			}
		}
		return count;
	}

	private RandomAccessibleInterval<DoubleType> hough(
		final RandomAccessibleInterval<BitType> img, final int minRadius,
		final int maxRadius, final Integer stepRadius)
	{
		return ops.op("transform.houghCircle") //
			.input(img, minRadius, maxRadius, stepRadius) //
			.outType(VOTES).apply();
	}

	private static double at(final RandomAccessibleInterval<DoubleType> votes,
		final long... pos)
	{
		return votes.getAt(pos).get();
	}

	/** A single pixel votes once at every point of each sphere around it. */
	@Test
	public void testSinglePixel() {
		final var img = ArrayImgs.bits(41, 41);
		img.getAt(20, 20).set(true);
		final var votes = hough(img, 3, 11, 4);
		assertArrayEquals(new long[] { 41, 41, 3 }, votes.dimensionsAsLongArray());
		for (var i = 0; i < 3; i++) {
			final var r = 3 + 4 * i;
			final var slice = Views.hyperSlice(votes, 2, i);
			final var expected = drawSphere(ArrayImgs.bits(41, 41), new long[] { 20,
				20 }, r);
			var sum = 0.0;
			for (final var v : Views.iterable(slice))
				sum += v.get();
			assertEquals(expected, sum, 0);
			assertEquals(1, at(votes, 20 + r, 20, i), 0);
			assertEquals(1, at(votes, 20, 20 - r, i), 0);
			assertEquals(0, at(votes, 20, 20, i), 0);
		}
	}

	/** A ring gets all its votes at its center and true radius. */
	@Test
	public void testRing() {
		final var img = ArrayImgs.bits(51, 45);
		final var n = drawSphere(img, new long[] { 25, 21 }, 10);
		final var votes = hough(img, 6, 14, null);
		assertEquals(9, votes.dimension(2));
		assertEquals(n, at(votes, 25, 21, 4), 0);
		for (final var v : Views.iterable(votes))
			assertEquals(true, v.get() <= n);
		// The circumference is close to 2 pi r.
		assertEquals(2 * Math.PI * 10, n, 0.15 * 2 * Math.PI * 10);
	}

	/** The spatial dimensions of the vote image match the input interval. */
	@Test
	public void testOffsetInput() {
		final var img = ArrayImgs.bits(31, 31);
		drawSphere(img, new long[] { 15, 15 }, 7);
		final var shifted = Views.translate(img, -5, 12);
		final var votes = hough(shifted, 5, 9, null);
		assertArrayEquals(new long[] { -5, 12, 0 }, Intervals.minAsLongArray(
			votes));
		assertEquals(at(hough(img, 5, 9, null), 15, 15, 2), at(votes, 10, 27, 2),
			0);
	}

	/** Spheres are detected in 3D too. */
	@Test
	public void testSphere3D() {
		final var img = ArrayImgs.bits(21, 21, 21);
		final var n = drawSphere(img, new long[] { 10, 9, 11 }, 6);
		final var votes = hough(img, 4, 8, null);
		assertEquals(n, at(votes, 10, 9, 11, 2), 0);
		for (final var v : Views.iterable(votes))
			assertEquals(true, v.get() <= n);
	}

	@Test
	public void testInvalidRadii() {
		final var img = ArrayImgs.bits(10, 10);
		assertThrows(IllegalArgumentException.class, () -> hough(img, 0, 5,
			null));
		assertThrows(IllegalArgumentException.class, () -> hough(img, 5, 4,
			null));
		assertThrows(IllegalArgumentException.class, () -> hough(img, 1, 4, 0));
	}
}
