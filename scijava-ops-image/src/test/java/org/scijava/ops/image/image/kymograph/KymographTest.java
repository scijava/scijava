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

package org.scijava.ops.image.image.kymograph;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.geom.real.DefaultWritablePolyline;
import net.imglib2.roi.geom.real.Polyline;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link DefaultKymograph}.
 *
 * @author Curtis Rueden
 */
public class KymographTest extends AbstractOpTest {

	private static Polyline line(final double... xy) {
		final var points = new RealPoint[xy.length / 2];
		for (var i = 0; i < points.length; i++)
			points[i] = new RealPoint(xy[2 * i], xy[2 * i + 1]);
		return new DefaultWritablePolyline(Arrays.asList(points));
	}

	/** Creates a 20x9x6 (x, y, t) movie of a dot moving at 3 pixels/frame. */
	private static Img<FloatType> movingDot(final int y) {
		final var img = ArrayImgs.floats(20, 9, 6);
		final var ra = img.randomAccess();
		for (var t = 0; t < 6; t++) {
			ra.setPosition(new int[] { 2 + 3 * t, y, t });
			ra.get().set(100);
		}
		return img;
	}

	private RandomAccessibleInterval<FloatType> kymograph(
		final RandomAccessibleInterval<FloatType> img, final Polyline line,
		final Integer timeDim, final Integer width, final String reduce,
		final String interpolation)
	{
		return ops.op("image.kymograph").input(img, line, timeDim, width, reduce,
			interpolation).outType(new Nil<RandomAccessibleInterval<FloatType>>()
		{}).apply();
	}

	private static float get(final RandomAccessibleInterval<FloatType> img,
		final long... pos)
	{
		return img.getAt(pos).get();
	}

	@Test
	public void testMovingDot() {
		final RandomAccessibleInterval<FloatType> kymo = ops.op("image.kymograph")
			.input(movingDot(4), line(0, 4, 19, 4)).outType(
				new Nil<RandomAccessibleInterval<FloatType>>()
				{}).apply();
		assertArrayEquals(new long[] { 20, 6 }, kymo.dimensionsAsLongArray());
		// The dot traces a straight line of slope 3 in the kymograph.
		for (var t = 0; t < 6; t++)
			for (var x = 0; x < 20; x++)
				assertEquals(x == 2 + 3 * t ? 100 : 0, get(kymo, x, t), 0);
	}

	@Test
	public void testFirstRowAndColumnAreSampled() {
		// KymographBuilder skipped x = 0 and y = 0; this op must not.
		final var img = ArrayImgs.floats(6, 6, 2);
		img.getAt(0, 0, 1).set(42);
		final var kymo = kymograph(img, line(0, 0, 5, 0), null, null, null, null);
		assertEquals(42, get(kymo, 0, 1), 0);
		assertEquals(0, get(kymo, 0, 0), 0);
	}

	@Test
	public void testLineWidth() {
		// The dot is at y = 5, one pixel off the line at y = 4.
		final var img = movingDot(5);
		final var line = line(0, 4, 19, 4);
		final var thin = kymograph(img, line, 2, 1, "max", null);
		assertEquals(0, get(thin, 5, 1), 0);

		final var max = kymograph(img, line, 2, 3, "max", null);
		assertEquals(100, get(max, 5, 1), 0);
		final var mean = kymograph(img, line, 2, 3, "mean", null);
		assertEquals(100 / 3.0, get(mean, 5, 1), 1e-5);

		final var none = kymograph(img, line, 2, 3, "none", null);
		assertArrayEquals(new long[] { 20, 6, 3 }, none.dimensionsAsLongArray());
		// The parallel lines are offset by -1, 0 and +1 along the normal (0, 1).
		assertEquals(0, get(none, 5, 1, 0), 0);
		assertEquals(0, get(none, 5, 1, 1), 0);
		assertEquals(100, get(none, 5, 1, 2), 0);
	}

	@Test
	public void testPolyline() {
		// The pixel value 10x + y identifies each position.
		final var img = ArrayImgs.floats(10, 10, 2);
		final var c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set(10 * c.getIntPosition(0) + c.getIntPosition(1));
		}
		// The path goes down 4 pixels, then right 6 pixels.
		final var kymo = kymograph(img, line(2, 2, 2, 6, 8, 6), null, null, null,
			null);
		assertArrayEquals(new long[] { 11, 2 }, kymo.dimensionsAsLongArray());
		for (var t = 0; t < 2; t++)
			for (var j = 0; j <= 10; j++) {
				final var expected = j <= 4 ? 22 + j : 10 * (j - 2) + 6;
				assertEquals(expected, get(kymo, j, t), 1e-4);
			}
	}

	@Test
	public void testInterpolation() {
		// A horizontal ramp, sampled along a diagonal between pixel centers.
		final var img = ArrayImgs.floats(8, 8, 1);
		final var c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set(c.getIntPosition(0));
		}
		final var s = 0.6;
		final var linear = kymograph(img, line(1.2, 1, 1.2 + 5 * s, 1 + 5 * 0.8),
			null, null, null, "linear");
		final var nearest = kymograph(img, line(1.2, 1, 1.2 + 5 * s, 1 + 5 * 0.8),
			null, null, null, "nearest");
		for (var j = 0; j <= 5; j++) {
			final var x = 1.2 + j * s;
			assertEquals(x, get(linear, j, 0), 1e-5);
			assertEquals(Math.round(x), get(nearest, j, 0), 0);
		}
	}

	@Test
	public void testExtraDimensionsAndIntegerType() {
		// This is an (x, y, c, t) image whose value encodes channel and time.
		final var img = ArrayImgs.unsignedBytes(5, 3, 2, 4);
		final var c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set(10 * c.getIntPosition(2) + c.getIntPosition(3));
		}
		final RandomAccessibleInterval<UnsignedByteType> kymo = ops.op(
			"image.kymograph").input(img, line(0, 1, 4, 1), 3).outType(
				new Nil<RandomAccessibleInterval<UnsignedByteType>>()
				{}).apply();
		assertArrayEquals(new long[] { 5, 4, 2 }, kymo.dimensionsAsLongArray());
		for (var ch = 0; ch < 2; ch++)
			for (var t = 0; t < 4; t++)
				for (var j = 0; j < 5; j++)
					assertEquals(10 * ch + t, kymo.getAt(j, t, ch).get());
	}
}
