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
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link CoherenceEnhancingDiffusion}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class CoherenceEnhancingDiffusionTest extends AbstractOpTest {

	@Test
	public void testEigen() {
		final var rnd = new Random(7);
		for (var n = 1; n <= 4; n++) {
			final var a = new double[n][n];
			for (var r = 0; r < n; r++)
				for (var s = r; s < n; s++)
					a[r][s] = a[s][r] = rnd.nextGaussian();
			final var copy = new double[n][];
			for (var r = 0; r < n; r++)
				copy[r] = a[r].clone();
			final var vals = new double[n];
			final var vecs = new double[n][n];
			CoherenceEnhancingDiffusion.eigen(copy, vals, vecs);
			for (var k = 0; k < n; k++) {
				for (var r = 0; r < n; r++) {
					var av = 0.0;
					for (var s = 0; s < n; s++)
						av += a[r][s] * vecs[s][k];
					assertEquals(vals[k] * vecs[r][k], av, 1e-10);
				}
			}
		}
	}

	/** With alpha = 1, one iteration is one explicit step of the heat equation. */
	@Test
	public void testIsotropicLimit() {
		final var rnd = new Random(1);
		final var in = ArrayImgs.doubles(7, 6);
		in.forEach(t -> t.set(10 * rnd.nextDouble()));
		final var out = ArrayImgs.doubles(7, 6);
		final var dt = 0.2;
		ops.op("filter.coherenceEnhancingDiffusion").input(in, 1.0, 2.0, 1, dt,
			1.0).output(out).compute();
		final var ra = in.randomAccess();
		final var c = out.localizingCursor();
		final var pos = new long[2];
		while (c.hasNext()) {
			c.fwd();
			c.localize(pos);
			final var v = ra.setPositionAndGet(pos).get();
			var lap = 0.0;
			for (var d = 0; d < 2; d++) {
				for (final var o : new int[] { -1, 1 }) {
					final var q = pos.clone();
					q[d] += o;
					if (q[d] < 0 || q[d] >= in.dimension(d)) continue;
					lap += ra.setPositionAndGet(q).get() - v;
				}
			}
			assertEquals(v + dt * lap, c.get().get(), 1e-9);
		}
	}

	@Test
	public void testConstantImage() {
		final var in = ArrayImgs.floats(8, 7, 5);
		in.forEach(t -> t.set(42));
		final var out = ArrayImgs.floats(8, 7, 5);
		ops.op("filter.coherenceEnhancingDiffusion").input(in, 1.0, 2.0, 5).output(
			out).compute();
		out.forEach(t -> assertEquals(42, t.get(), 1e-4));
	}

	/**
	 * Noisy oblique stripes are restored much better than by isotropic diffusion
	 * of the same duration, since smoothing happens along the stripes.
	 */
	@Test
	public void testStripes() {
		final long w = 48, h = 48;
		final var clean = ArrayImgs.doubles(w, h);
		final var noisy = ArrayImgs.doubles(w, h);
		final var rnd = new Random(3);
		final var cc = clean.localizingCursor();
		final var nc = noisy.cursor();
		while (cc.hasNext()) {
			cc.fwd();
			final var x = cc.getDoublePosition(0);
			final var y = cc.getDoublePosition(1);
			final var v = 100 + 50 * Math.sin(2 * Math.PI * (x + 0.5 * y) / 8);
			cc.get().set(v);
			nc.next().set(v + 30 * rnd.nextGaussian());
		}
		final var ced = ArrayImgs.doubles(w, h);
		ops.op("filter.coherenceEnhancingDiffusion").input(noisy, 1.0, 4.0, 40,
			0.2, 0.001, 1.0).output(ced).compute();
		final var iso = ArrayImgs.doubles(w, h);
		ops.op("filter.coherenceEnhancingDiffusion").input(noisy, 1.0, 4.0, 40,
			0.2, 1.0).output(iso).compute();
		final var before = rmse(noisy, clean);
		final var afterCed = rmse(ced, clean);
		final var afterIso = rmse(iso, clean);
		assertTrue(afterCed < before / 2, afterCed + " vs " + before);
		assertTrue(afterCed < afterIso / 2, afterCed + " vs " + afterIso);
	}

	/** Noise in 3D stays bounded and is smoothed. */
	@Test
	public void testStability3D() {
		final var rnd = new Random(4);
		final var in = ArrayImgs.doubles(12, 11, 10);
		in.forEach(t -> t.set(255 * rnd.nextDouble()));
		final var out = ArrayImgs.doubles(12, 11, 10);
		ops.op("filter.coherenceEnhancingDiffusion").input(in, 1.0, 2.0, 30)
			.output(out).compute();
		out.forEach(t -> assertTrue(t.get() > -10 && t.get() < 265, "" + t));
		assertTrue(stdDev(out) < stdDev(in) / 2);
	}

	@Test
	public void testInvalidArguments() {
		final var in = ArrayImgs.doubles(5, 5);
		final var out = ArrayImgs.doubles(5, 5);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"filter.coherenceEnhancingDiffusion").input(in, 1.0, 2.0, 1, 0.3).output(
				out).compute());
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"filter.coherenceEnhancingDiffusion").input(in, 1.0, 2.0, 1, 0.1, 0.0)
			.output(out).compute());
	}

	private static double rmse(final RandomAccessibleInterval<? extends RealType<?>> a,
		final RandomAccessibleInterval<? extends RealType<?>> b)
	{
		final var ac = Views.flatIterable(a).cursor();
		final var bc = Views.flatIterable(b).cursor();
		var sum = 0.0;
		var count = 0;
		while (ac.hasNext()) {
			final var d = ac.next().getRealDouble() - bc.next().getRealDouble();
			sum += d * d;
			count++;
		}
		return Math.sqrt(sum / count);
	}

	private static double stdDev(
		final RandomAccessibleInterval<? extends RealType<?>> img)
	{
		var sum = 0.0;
		var sumSq = 0.0;
		var count = 0;
		for (final var t : Views.flatIterable(img)) {
			final var v = t.getRealDouble();
			sum += v;
			sumSq += v * v;
			count++;
		}
		final var mean = sum / count;
		return Math.sqrt(sumSq / count - mean * mean);
	}

	/**
	 * An 8-bit output saturates rather than wrapping around: the result must be
	 * the floating-point result clamped to [0, 255].
	 */
	@Test
	public void testUnsignedByteOutputIsClamped() {
		final var in = ArrayImgs.floats(16, 16);
		final var rnd = new Random(3);
		final var c = in.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().setReal((c.getIntPosition(0) < 8 ? 300 : -20) + rnd
				.nextDouble());
		}
		final var expected = ArrayImgs.floats(16, 16);
		ops.op("filter.coherenceEnhancingDiffusion").input(in, 1.0, 2.0, 5).output(expected).compute();
		final var actual = ArrayImgs.unsignedBytes(16, 16);
		ops.op("filter.coherenceEnhancingDiffusion").input(in, 1.0, 2.0, 5).output(actual).compute();
		final var ec = expected.cursor();
		var saturated = 0;
		for (final var t : actual) {
			final var e = Math.max(0, Math.min(255, ec.next().get()));
			assertEquals(e, t.get(), 0.5 + 1e-4);
			if (t.get() == 0 || t.get() == 255) saturated++;
		}
		assertTrue(saturated > 100, "saturated pixels: " + saturated);
	}
}
