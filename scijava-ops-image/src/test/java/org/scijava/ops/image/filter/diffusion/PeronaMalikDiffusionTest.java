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

package org.scijava.ops.image.filter.diffusion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link PeronaMalikDiffusion}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class PeronaMalikDiffusionTest extends AbstractOpTest {

	/**
	 * With a huge kappa the conductance is 1, so one iteration is one explicit
	 * step of the linear heat equation with zero-flux borders.
	 */
	@Test
	public void testLinearLimit3D() {
		final var rnd = new Random(1);
		final var in = ArrayImgs.doubles(5, 4, 3);
		in.forEach(t -> t.set(10 * rnd.nextDouble()));
		final var out = ArrayImgs.doubles(5, 4, 3);
		final var dt = 0.1;
		ops.op("filter.anisotropicDiffusion").input(in, 1e9, 1, dt).output(out)
			.compute();

		final var ra = in.randomAccess();
		final var c = out.localizingCursor();
		final var pos = new long[3];
		while (c.hasNext()) {
			c.fwd();
			c.localize(pos);
			final var v = ra.setPositionAndGet(pos).get();
			var lap = 0.0;
			for (var d = 0; d < 3; d++) {
				for (final var o : new int[] { -1, 1 }) {
					final var q = pos.clone();
					q[d] += o;
					if (q[d] < 0 || q[d] >= in.dimension(d)) continue;
					lap += ra.setPositionAndGet(q).get() - v;
				}
			}
			assertEquals(v + dt * lap, c.get().get(), 1e-6);
		}
	}

	@Test
	public void testTotalIntensityPreserved() {
		final var rnd = new Random(2);
		final var in = ArrayImgs.floats(9, 8);
		in.forEach(t -> t.set(100 * rnd.nextFloat()));
		for (final var g : new String[] { "exponential", "quadratic" }) {
			final var out = ArrayImgs.floats(9, 8);
			ops.op("filter.anisotropicDiffusion").input(in, 20.0, 15, 0.25, g)
				.output(out).compute();
			assertEquals(sum(in), sum(out), 1e-2);
		}
	}

	@Test
	public void testNoisyStep() {
		final var rnd = new Random(3);
		final long w = 40, h = 20;
		final var in = ArrayImgs.floats(w, h);
		final var c = in.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set((float) ((c.getLongPosition(0) < w / 2 ? 50 : 150) + 5 * rnd
				.nextGaussian()));
		}
		final var flat = Intervals.createMinMax(0, 0, w / 2 - 4, h - 1);
		final var noise = stdDev(Views.interval(in, flat));
		for (final var g : new String[] { "exponential", "quadratic" }) {
			final var out = ArrayImgs.floats(w, h);
			ops.op("filter.anisotropicDiffusion").input(in, 15.0, 30, null, g).output(
				out).compute();
			assertTrue(stdDev(Views.interval(out, flat)) < noise / 3, g);
			final var ra = out.randomAccess();
			for (var y = 0; y < h; y++) {
				final var step = ra.setPositionAndGet(w / 2, y).get() - ra
					.setPositionAndGet(w / 2 - 1, y).get();
				assertTrue(step > 80, g + ": step at y=" + y + ": " + step);
			}
		}
	}

	@Test
	public void testInvalidParameters() {
		final var img = ArrayImgs.floats(5, 5);
		final var out = ArrayImgs.floats(5, 5);
		assertThrows(RuntimeException.class, () -> ops.op(
			"filter.anisotropicDiffusion").input(img, 5.0, 1, 0.3).output(out)
			.compute());
		assertThrows(RuntimeException.class, () -> ops.op(
			"filter.anisotropicDiffusion").input(img, 5.0, 1, 0.1, "linear").output(
				out).compute());
	}

	private static double sum(
		final RandomAccessibleInterval<? extends RealType<?>> img)
	{
		double s = 0;
		for (final var t : img)
			s += t.getRealDouble();
		return s;
	}

	private static double stdDev(
		final RandomAccessibleInterval<? extends RealType<?>> img)
	{
		double s = 0, sq = 0;
		for (final var t : img) {
			final var v = t.getRealDouble();
			s += v;
			sq += v * v;
		}
		final var mean = s / img.size();
		return Math.sqrt(sq / img.size() - mean * mean);
	}
}
