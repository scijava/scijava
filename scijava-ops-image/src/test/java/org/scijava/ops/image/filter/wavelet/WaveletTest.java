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

package org.scijava.ops.image.filter.wavelet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link WaveletATrous} and {@link WaveletDenoise}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class WaveletTest extends AbstractOpTest {

	private static final Nil<List<RandomAccessibleInterval<FloatType>>> PLANES =
		new Nil<>()
		{};

	private static final Nil<RandomAccessibleInterval<FloatType>> FLOATS =
		new Nil<>()
		{};

	@Test
	public void testReconstruction() {
		final var rnd = new Random(1);
		final var in = ArrayImgs.unsignedShorts(23, 17, 5);
		in.forEach(t -> t.set(rnd.nextInt(4000)));
		final var planes = ops.op("filter.waveletATrous").input(in, 4).outType(
			PLANES).apply();
		assertEquals(5, planes.size());
		final var sum = ArrayImgs.doubles(23, 17, 5);
		for (final var p : planes) {
			final var c = sum.cursor();
			final var pc = p.cursor();
			while (c.hasNext())
				c.next().set(c.get().get() + pc.next().get());
		}
		final var c = sum.cursor();
		final var ic = in.cursor();
		while (c.hasNext())
			assertEquals(ic.next().get(), c.next().get(), 1e-2);
	}

	/** Checks the first smoothing step against the explicit 1D filter. */
	@Test
	public void testFirstScale1D() {
		final var in = ArrayImgs.floats(new float[] { 0, 0, 0, 16, 0, 0, 0, 0 },
			8);
		final var planes = ops.op("filter.waveletATrous").input(in, 2).outType(
			PLANES).apply();
		final float[] w1 = { 0, -1, -4, 10, -4, -1, 0, 0 };
		var i = 0;
		for (final var t : planes.get(0))
			assertEquals(w1[i++], t.get(), 1e-6);
		// Second scale: c1 convolved with taps 2 apart, mirrored at the left
		// border (c1 = 0, 1, 4, 6, 4, 1, 0, 0).
		final float[] c1 = { 0, 1, 4, 6, 4, 1, 0, 0 };
		i = 0;
		for (final var t : planes.get(1)) {
			final var c2 = (tap(c1, i - 4) + 4 * tap(c1, i - 2) + 6 * c1[i] + 4 *
				tap(c1, i + 2) + tap(c1, i + 4)) / 16;
			assertEquals(c1[i] - c2, t.get(), 1e-6);
			i++;
		}
	}

	@Test
	public void testConstantImage() {
		final var in = ArrayImgs.floats(10, 12);
		in.forEach(t -> t.set(7));
		final var planes = ops.op("filter.waveletATrous").input(in, 3).outType(
			PLANES).apply();
		for (var j = 0; j < 3; j++)
			for (final var t : planes.get(j))
				assertEquals(0, t.get(), 1e-5);
		for (final var t : planes.get(3))
			assertEquals(7, t.get(), 1e-5);
	}

	/**
	 * The noise levels of the wavelet planes of white noise match the
	 * published values for the 2D B3-spline starlet (Starck and Murtagh), which
	 * were estimated by simulation.
	 */
	@Test
	public void testNoiseLevels() {
		final var levels = Starlet.noiseLevels(5, 2);
		final double[] expected = { 0.889, 0.200, 0.086, 0.041, 0.020 };
		for (var j = 0; j < 5; j++)
			assertEquals(expected[j], levels[j], 0.002);
		final var rnd = new Random(7);
		final var noise = ArrayImgs.floats(128, 128);
		noise.forEach(t -> t.set((float) rnd.nextGaussian()));
		final var planes = ops.op("filter.waveletATrous").input(noise, 2).outType(
			PLANES).apply();
		assertEquals(levels[0], std(planes.get(0)), 0.02);
		assertEquals(levels[1], std(planes.get(1)), 0.02);
	}

	@Test
	public void testDenoise() {
		final var rnd = new Random(11);
		final int w = 64, h = 64;
		final var clean = ArrayImgs.doubles(w, h);
		final var noisy = ArrayImgs.floats(w, h);
		final var cc = clean.localizingCursor();
		final var nc = noisy.cursor();
		while (cc.hasNext()) {
			final var v = cc.next();
			final double dx = cc.getDoublePosition(0) - 32, dy = cc
				.getDoublePosition(1) - 32;
			v.set(100 + 200 * Math.exp(-(dx * dx + dy * dy) / (2 * 4 * 4)));
			nc.next().set((float) (v.get() + 20 * rnd.nextGaussian()));
		}
		final var out = ops.op("filter.waveletDenoise").input(noisy).outType(
			FLOATS).apply();
		final var before = rmse(noisy, clean);
		final var after = rmse(out, clean);
		assertTrue(after < before / 3, "RMSE: " + before + " -> " + after);
		final var out2 = ops.op("filter.waveletDenoise").input(noisy, 3, 2.0)
			.outType(FLOATS).apply();
		assertTrue(rmse(out2, clean) < before / 2);
	}

	private static float tap(final float[] a, final int i) {
		final var n = a.length;
		final var m = i < 0 ? -i : i >= n ? 2 * n - 2 - i : i;
		return a[m];
	}

	private static double mean(
		final RandomAccessibleInterval<? extends RealType<?>> img)
	{
		double sum = 0;
		for (final var t : img)
			sum += t.getRealDouble();
		return sum / img.size();
	}

	private static double std(
		final RandomAccessibleInterval<? extends RealType<?>> img)
	{
		final var m = mean(img);
		double sum = 0;
		for (final var t : img)
			sum += (t.getRealDouble() - m) * (t.getRealDouble() - m);
		return Math.sqrt(sum / img.size());
	}

	private static double rmse(
		final RandomAccessibleInterval<? extends RealType<?>> a,
		final RandomAccessibleInterval<? extends RealType<?>> b)
	{
		final var ca = a.cursor();
		final var cb = b.cursor();
		double sum = 0;
		while (ca.hasNext()) {
			final var d = ca.next().getRealDouble() - cb.next().getRealDouble();
			sum += d * d;
		}
		return Math.sqrt(sum / a.size());
	}
}
