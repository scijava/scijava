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

package org.scijava.ops.image.filter.retinex;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@code filter.retinex}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class RetinexTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<FloatType>> OUT =
		new Nil<>()
		{};

	private RandomAccessibleInterval<FloatType> retinex(
		final RandomAccessibleInterval<?> img, final double[] sigmas,
		final Double dynamic)
	{
		return ops.op("filter.retinex").input(img, sigmas, dynamic).outType(OUT)
			.apply();
	}

	@Test
	public void testConstantImage() {
		final var img = ArrayImgs.doubles(16, 12);
		for (final var t : img)
			t.set(3);
		// log I - log(G * I) vanishes everywhere.
		for (final var t : Views.iterable(retinex(img, new double[] { 2, 5 },
			0.0)))
			assertEquals(0, t.get(), 1e-6);
		// With no contrast, the stretch maps to mid-grey.
		for (final var t : Views.iterable(ops.op("filter.retinex").input(img)
			.outType(OUT).apply()))
			assertEquals(0.5, t.get(), 1e-6);
	}

	@Test
	public void testScalesApplyToOriginal() {
		// The multi-scale result is the mean of the single-scale results, i.e.
		// each surround is applied to the original image, not to the previous
		// blur.
		final var rnd = new Random(1);
		final var img = ArrayImgs.unsignedBytes(20, 18);
		for (final var t : img)
			t.set(1 + rnd.nextInt(255));
		final var a = retinex(img, new double[] { 2 }, 0.0);
		final var b = retinex(img, new double[] { 6 }, 0.0);
		final var ab = retinex(img, new double[] { 2, 6 }, 0.0);
		final var ca = Views.flatIterable(a).cursor();
		final var cb = Views.flatIterable(b).cursor();
		for (final var t : Views.flatIterable(ab))
			assertEquals((ca.next().get() + cb.next().get()) / 2, t.get(), 1e-5);

		// Single scale, by hand at one pixel: log I - log(G * I).
		final RandomAccessibleInterval<FloatType> blurred = ops.op("filter.gauss")
			.input(img, 2.0).outType(OUT).apply();
		final var x = 7;
		final var y = 9;
		final var i = img.randomAccess().setPositionAndGet(x, y).get();
		final var g = blurred.randomAccess().setPositionAndGet(x, y).get();
		assertEquals(Math.log(i) - Math.log(g), a.randomAccess().setPositionAndGet(
			x, y).get(), 1e-4);
	}

	@Test
	public void testIlluminationRemoved() {
		// A reflectance checkerboard under an exponential illumination ramp.
		final long w = 64, h = 32;
		final var lit = ArrayImgs.doubles(w, h);
		final var c = lit.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final long x = c.getLongPosition(0), y = c.getLongPosition(1);
			final var reflectance = (x / 4 + y / 4) % 2 == 0 ? 0.2 : 0.8;
			c.get().set(reflectance * Math.exp(x / 30.0));
		}
		final var sigmas = new double[] { 3, 6, 12 };
		final var out = retinex(lit, sigmas, null);

		final var left = Intervals.createMinMax(12, 4, 27, h - 5);
		final var right = Intervals.createMinMax(36, 4, 51, h - 5);
		final var inRatio = mean(Views.interval(lit, right)) / mean(Views.interval(
			lit, left));
		assertTrue(inRatio > 2, "input ratio " + inRatio);
		final var outDiff = Math.abs(mean(Views.interval(out, right)) - mean(Views
			.interval(out, left)));
		assertTrue(outDiff < 0.05, "output difference " + outDiff);
		for (final var t : Views.iterable(out))
			assertTrue(t.get() >= 0 && t.get() <= 1);
	}

	@Test
	public void testColorRestoration() {
		// A grey image keeps equal channels.
		final var rnd = new Random(2);
		final long w = 12, h = 10;
		final var grey = ArrayImgs.unsignedBytes(w, h, 3);
		final var ra = grey.randomAccess();
		for (var y = 0; y < h; y++) {
			for (var x = 0; x < w; x++) {
				final var v = 1 + rnd.nextInt(255);
				for (var ch = 0; ch < 3; ch++)
					ra.setPositionAndGet(x, y, ch).set(v);
			}
		}
		final RandomAccessibleInterval<FloatType> out = ops.op("filter.retinex")
			.input(grey, 2, new double[] { 2, 4 }).outType(OUT).apply();
		final var oa = out.randomAccess();
		for (var y = 0; y < h; y++) {
			for (var x = 0; x < w; x++) {
				final var r = oa.setPositionAndGet(x, y, 0).get();
				assertEquals(r, oa.setPositionAndGet(x, y, 1).get(), 1e-6);
				assertEquals(r, oa.setPositionAndGet(x, y, 2).get(), 1e-6);
			}
		}

		// The ARGB variant agrees with the channel variant.
		final var argb = ArrayImgs.argbs(w, h);
		final var aa = argb.randomAccess();
		final var col = ArrayImgs.unsignedBytes(w, h, 3);
		final var ca = col.randomAccess();
		for (var y = 0; y < h; y++) {
			for (var x = 0; x < w; x++) {
				final int r = rnd.nextInt(256), g = rnd.nextInt(256), b = rnd.nextInt(
					256);
				aa.setPositionAndGet(x, y).set(ARGBType.rgba(r, g, b, 255));
				ca.setPositionAndGet(x, y, 0).set(r);
				ca.setPositionAndGet(x, y, 1).set(g);
				ca.setPositionAndGet(x, y, 2).set(b);
			}
		}
		final var sigmas = new double[] { 3 };
		final RandomAccessibleInterval<FloatType> fromARGB = ops.op(
			"filter.retinex").input(argb, sigmas, 1.0).outType(OUT).apply();
		final RandomAccessibleInterval<FloatType> fromChannels = ops.op(
			"filter.retinex").input(col, 2, sigmas, 1.0).outType(OUT).apply();
		assertArrayEquals(col.dimensionsAsLongArray(), fromARGB
			.dimensionsAsLongArray());
		final var fa = Views.flatIterable(fromARGB).cursor();
		for (final var t : Views.flatIterable(fromChannels))
			assertEquals(t.get(), fa.next().get(), 1e-6);
	}

	private static double mean(
		final RandomAccessibleInterval<? extends net.imglib2.type.numeric.RealType<?>> img)
	{
		double sum = 0;
		long n = 0;
		for (final var t : Views.iterable(img)) {
			sum += t.getRealDouble();
			n++;
		}
		return sum / n;
	}
}
