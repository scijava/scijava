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

package org.scijava.ops.image.filter.tv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link DefaultTVDenoise}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DefaultTVDenoiseTest extends AbstractOpTest {

	/**
	 * For a 1D signal of two plateaus of length L and height difference h, the
	 * ROF minimizer moves each plateau by lambda / L towards the other (as long
	 * as this is less than h / 2).
	 */
	@Test
	public void testTwoPlateausAnalytic() {
		final var in = ArrayImgs.doubles(20);
		final var c = in.localizingCursor();
		while (c.hasNext())
			c.next().set(c.getLongPosition(0) < 10 ? 0 : 100);
		final var out = ArrayImgs.doubles(20);
		ops.op("filter.tvDenoise").input(in, 200.0, 5000).output(out).compute();
		final var oc = out.localizingCursor();
		while (oc.hasNext()) {
			final var v = oc.next().get();
			assertEquals(oc.getLongPosition(0) < 10 ? 20 : 80, v, 1e-3);
		}
	}

	@Test
	public void testConstantImageUnchanged() {
		final var in = ArrayImgs.floats(6, 5, 4);
		in.forEach(t -> t.set(42));
		final var out = ArrayImgs.floats(6, 5, 4);
		ops.op("filter.tvDenoise").input(in, 10.0).output(out).compute();
		for (final var t : out)
			assertEquals(42, t.get(), 1e-4);
	}

	@Test
	public void testNoisySquare() {
		final var rnd = new Random(5);
		final int w = 32, h = 32;
		final var clean = ArrayImgs.doubles(w, h);
		final var noisy = ArrayImgs.unsignedBytes(w, h);
		final var cc = clean.localizingCursor();
		final var nc = noisy.cursor();
		while (cc.hasNext()) {
			final var v = cc.next();
			final long x = cc.getLongPosition(0), y = cc.getLongPosition(1);
			v.set(x >= 8 && x < 24 && y >= 8 && y < 24 ? 180 : 60);
			nc.next().set((int) Math.round(v.get() + 15 * rnd.nextGaussian()));
		}
		final var out = ArrayImgs.doubles(w, h);
		ops.op("filter.tvDenoise").input(noisy, 25.0, 100, 0.2).output(out)
			.compute();

		assertEquals(mean(noisy), mean(out), 1e-9);
		assertTrue(tv(out) < tv(noisy) / 3, "TV: " + tv(noisy) + " -> " + tv(
			out));
		final var before = rmse(noisy, clean);
		final var after = rmse(out, clean);
		assertTrue(after < before / 2, "RMSE: " + before + " -> " + after);
		// The edge stays sharp: the step across it is at least 70% of its height (TV denoising also reduces contrast).
		final var ra = out.randomAccess();
		for (var y = 10; y < 22; y++) {
			final var step = ra.setPositionAndGet(8, y).get() - ra.setPositionAndGet(
				7, y).get();
			assertTrue(step > 0.7 * 120, "step at y=" + y + ": " + step);
		}
	}

	private static double mean(
		final RandomAccessibleInterval<? extends RealType<?>> img)
	{
		double sum = 0;
		for (final var t : img)
			sum += t.getRealDouble();
		return sum / img.size();
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

	/** Computes the anisotropic total variation of a 2D image. */
	private static double tv(
		final RandomAccessibleInterval<? extends RealType<?>> img)
	{
		final var ra = img.randomAccess();
		double sum = 0;
		for (var y = 0; y < img.dimension(1); y++) {
			for (var x = 0; x < img.dimension(0); x++) {
				final var v = ra.setPositionAndGet(x, y).getRealDouble();
				if (x > 0) sum += Math.abs(v - ra.setPositionAndGet(x - 1, y)
					.getRealDouble());
				if (y > 0) sum += Math.abs(v - ra.setPositionAndGet(x, y - 1)
					.getRealDouble());
			}
		}
		return sum;
	}
}
