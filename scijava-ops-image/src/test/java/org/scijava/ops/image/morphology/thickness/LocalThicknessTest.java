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

package org.scijava.ops.image.morphology.thickness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * Tests {@link DefaultLocalThickness}.
 *
 * @author Curtis Rueden
 */
public class LocalThicknessTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<FloatType>> OUT =
		new Nil<>()
		{};

	/** A slab of even width W has local thickness exactly W. */
	@Test
	public void testSlab2D() {
		final int width = 10;
		final Img<BitType> in = image(new long[] { 30, 12 }, p -> p[0] >= 8 &&
			p[0] < 8 + width);
		final RandomAccessibleInterval<FloatType> lt = ops.op(
			"morphology.localThickness").input(in).outType(OUT).apply();
		assertForeground(in, lt, (v, p) -> assertEquals(width, v, 1e-6));
	}

	/** Calibration scales thickness along each axis independently. */
	@Test
	public void testSlabCalibrated() {
		final double[] cal = { 0.5, 2.0 };
		final Img<BitType> slabX = image(new long[] { 30, 12 }, p -> p[0] >= 8 &&
			p[0] < 18);
		final RandomAccessibleInterval<FloatType> ltX = ops.op(
			"morphology.localThickness").input(slabX, cal).outType(OUT).apply();
		assertForeground(slabX, ltX, (v, p) -> assertEquals(5.0, v, 1e-6));

		final Img<BitType> slabY = image(new long[] { 12, 30 }, p -> p[1] >= 8 &&
			p[1] < 18);
		final RandomAccessibleInterval<FloatType> ltY = ops.op(
			"morphology.localThickness").input(slabY, cal).outType(OUT).apply();
		assertForeground(slabY, ltY, (v, p) -> assertEquals(20.0, v, 1e-6));
	}

	/** A 3D slab of even width W has local thickness exactly W. */
	@Test
	public void testSlab3D() {
		final int width = 6;
		final Img<BitType> in = image(new long[] { 10, 10, 16 }, p -> p[2] >= 5 &&
			p[2] < 5 + width);
		final RandomAccessibleInterval<FloatType> lt = ops.op(
			"morphology.localThickness").input(in).outType(OUT).apply();
		assertForeground(in, lt, (v, p) -> assertEquals(width, v, 1e-6));
	}

	/**
	 * A disk of radius R has a thickness close to its diameter. The distance
	 * transform measures distances to background pixel centres, so the
	 * thickness is between 2R and 2R + 2.
	 */
	@Test
	public void testDisk() {
		final int r = 10;
		final Img<BitType> in = image(new long[] { 31, 31 }, p -> sq(p[0] - 15) +
			sq(p[1] - 15) <= r * r);
		final RandomAccessibleInterval<FloatType> lt = ops.op(
			"morphology.localThickness").input(in).outType(OUT).apply();
		final double[] sum = { 0, 0 };
		assertForeground(in, lt, (v, p) -> {
			assertTrue(v >= 2 * r && v <= 2 * r + 2, "Thickness " + v + " at " +
				p[0] + "," + p[1]);
			sum[0] += v;
			sum[1]++;
		});
		assertEquals(2 * r + 1, sum[0] / sum[1], 1.0);
	}

	/** A ball of radius R has a thickness close to its diameter. */
	@Test
	public void testBall() {
		final int r = 6;
		final Img<BitType> in = image(new long[] { 17, 17, 17 }, p -> sq(p[0] -
			8) + sq(p[1] - 8) + sq(p[2] - 8) <= r * r);
		final RandomAccessibleInterval<FloatType> lt = ops.op(
			"morphology.localThickness").input(in).outType(OUT).apply();
		assertForeground(in, lt, (v, p) -> assertTrue(v >= 2 * r &&
			v <= 2 * r + 2, "Thickness " + v));
	}

	/** Background is 0 by default, and the result keeps the input interval. */
	@Test
	public void testMaskAndInterval() {
		final Img<BitType> img = image(new long[] { 20, 20 }, p -> p[0] >= 5 &&
			p[0] < 9);
		final RandomAccessibleInterval<BitType> in = Views.translate(img, 3, -7);
		final RandomAccessibleInterval<FloatType> lt = ops.op(
			"morphology.localThickness").input(in).outType(OUT).apply();
		assertEquals(3, lt.min(0));
		assertEquals(-7, lt.min(1));
		final RandomAccess<FloatType> ra = lt.randomAccess();
		ra.setPosition(new long[] { 3 + 4, -7 + 10 });
		assertEquals(0, ra.get().get());
		ra.setPosition(new long[] { 3 + 6, -7 + 10 });
		assertEquals(4, ra.get().get(), 1e-6);
		// Unmasked, the thickness map spills into the adjacent background.
		final RandomAccessibleInterval<FloatType> unmasked = ops.op(
			"morphology.localThickness").input(in, null, false).outType(OUT)
			.apply();
		final RandomAccess<FloatType> ua = unmasked.randomAccess();
		ua.setPosition(new long[] { 3 + 4, -7 + 10 });
		assertEquals(4, ua.get().get(), 1e-6);
	}

	@Test
	public void testBadCalibration() {
		final Img<BitType> in = image(new long[] { 5, 5 }, p -> p[0] == 2);
		assertThrows(RuntimeException.class, () -> ops.op(
			"morphology.localThickness").input(in, new double[] { 1 }).outType(OUT)
			.apply());
	}

	/**
	 * Compares against the original Fiji LocalThickness plugins (EDT_S1D,
	 * Distance_Ridge, Local_Thickness_Parallel, Clean_Up_Local_Thickness, from
	 * sc.fiji:LocalThickness_:5.0.0) run on the same synthetic image without
	 * masking. The expected sums were produced by a throwaway harness calling
	 * those plugins headlessly on an 8-bit stack with foreground = 255.
	 */
	@Test
	public void testRegression3D() {
		final Img<BitType> in = image(new long[] { 24, 20, 16 }, p -> shape(p[0],
			p[1], p[2]));
		assertSums(in, 27308.722930908203, 18969.93926382065);
	}

	/** As {@link #testRegression3D()}, for a single 2D plane (z = 8). */
	@Test
	public void testRegression2D() {
		final Img<BitType> in = image(new long[] { 24, 20 }, p -> shape(p[0], p[1],
			8));
		assertSums(in, 2698.6377420425415, 2157.8645343780518);
	}

	private void assertSums(final Img<BitType> in, final double expectedSum,
		final double expectedSumFg)
	{
		final RandomAccessibleInterval<FloatType> unmasked = ops.op(
			"morphology.localThickness").input(in, null, false).outType(OUT)
			.apply();
		final RandomAccessibleInterval<FloatType> masked = ops.op(
			"morphology.localThickness").input(in).outType(OUT).apply();
		double sum = 0, sumFg = 0, sumMasked = 0;
		final Cursor<BitType> c = in.localizingCursor();
		final RandomAccess<FloatType> ua = unmasked.randomAccess();
		final RandomAccess<FloatType> ma = masked.randomAccess();
		while (c.hasNext()) {
			c.fwd();
			ua.setPosition(c);
			ma.setPosition(c);
			sum += ua.get().get();
			if (c.get().get()) sumFg += ua.get().get();
			sumMasked += ma.get().get();
		}
		assertEquals(expectedSum, sum, 1e-2);
		assertEquals(expectedSumFg, sumFg, 1e-2);
		assertEquals(expectedSumFg, sumMasked, 1e-2);
	}

	private static boolean shape(final long x, final long y, final long z) {
		if (sq(x - 10) + sq(y - 9) + sq(z - 8) <= 36) return true;
		if (x >= 15 && x < 21 && y >= 2 && y < 18 && z >= 3 && z < 13) return true;
		return y == 4 && z == 4 && x >= 2 && x <= 20;
	}

	private static long sq(final long v) {
		return v * v;
	}

	private static Img<BitType> image(final long[] dims,
		final Predicate<long[]> fg)
	{
		final Img<BitType> img = ArrayImgs.bits(dims);
		final Cursor<BitType> c = img.localizingCursor();
		final long[] pos = new long[dims.length];
		while (c.hasNext()) {
			c.fwd();
			c.localize(pos);
			c.get().set(fg.test(pos));
		}
		return img;
	}

	private interface Check {

		void check(float value, long[] pos);
	}

	private static void assertForeground(final RandomAccessibleInterval<BitType> in,
		final RandomAccessibleInterval<FloatType> lt, final Check check)
	{
		final Cursor<BitType> c = Views.iterable(in).localizingCursor();
		final RandomAccess<FloatType> ra = lt.randomAccess();
		final long[] pos = new long[in.numDimensions()];
		while (c.hasNext()) {
			c.fwd();
			c.localize(pos);
			ra.setPosition(pos);
			if (c.get().get()) check.check(ra.get().get(), pos);
			else assertEquals(0, ra.get().get());
		}
	}
}
