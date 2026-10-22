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

package org.scijava.ops.image.filter.kuwahara;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link DefaultKuwahara}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DefaultKuwaharaTest extends AbstractOpTest {

	/** A noisy vertical step edge (8-bit). */
	private static final int[][] INPUT = {
		{ 58, 63, 62, 62, 57, 77, 181, 169, 196, 174, 172, 177 },
		{ 78, 76, 44, 69, 69, 47, 186, 196, 194, 163, 168, 199 },
		{ 49, 47, 61, 62, 42, 73, 177, 165, 195, 186, 181, 193 },
		{ 56, 41, 50, 46, 41, 58, 170, 193, 195, 163, 185, 196 },
		{ 41, 62, 76, 51, 40, 47, 200, 188, 181, 189, 182, 196 },
		{ 78, 60, 51, 73, 68, 60, 183, 176, 162, 190, 169, 185 },
		{ 74, 71, 60, 63, 64, 72, 181, 166, 189, 163, 180, 191 },
		{ 58, 50, 41, 75, 70, 66, 184, 199, 196, 162, 179, 171 },
		{ 46, 42, 45, 42, 74, 41, 171, 179, 174, 187, 198, 161 },
		{ 56, 80, 69, 51, 73, 61, 168, 195, 164, 174, 175, 193 } };

	/**
	 * Output of the Fiji Kuwahara_Filter plugin 2.0.1 ({@code filter(ip)} with
	 * its default size of 5) for {@link #INPUT}. Its border handling differs, so
	 * only pixels at least 2 pixels from the border are compared.
	 */
	private static final int[][] PLUGIN = {
		{ 60, 61, 59, 61, 59, 62, 184, 182, 184, 179, 181, 179 },
		{ 56, 55, 54, 55, 54, 56, 186, 183, 186, 182, 181, 182 },
		{ 54, 61, 59, 61, 59, 51, 184, 184, 184, 186, 184, 186 },
		{ 57, 57, 54, 54, 54, 54, 186, 182, 186, 184, 180, 184 },
		{ 54, 63, 54, 63, 61, 51, 181, 178, 184, 186, 184, 186 },
		{ 60, 60, 63, 68, 63, 68, 183, 182, 180, 177, 180, 177 },
		{ 54, 63, 61, 63, 61, 60, 182, 178, 178, 183, 178, 183 },
		{ 60, 60, 63, 68, 63, 68, 182, 181, 182, 177, 179, 177 },
		{ 54, 54, 54, 63, 59, 63, 182, 179, 182, 177, 181, 177 },
		{ 54, 55, 54, 61, 60, 61, 181, 181, 179, 178, 179, 178 } };

	@Test
	public void testMatchesPlugin() {
		final var h = INPUT.length;
		final var w = INPUT[0].length;
		final var in = ArrayImgs.unsignedBytes(w, h);
		final var ra = in.randomAccess();
		for (var y = 0; y < h; y++)
			for (var x = 0; x < w; x++)
				ra.setPositionAndGet(x, y).set(INPUT[y][x]);
		final var out = ArrayImgs.floats(w, h);
		ops.op("filter.kuwahara").input(in).output(out).compute();
		final var oa = out.randomAccess();
		for (var y = 2; y < h - 2; y++) {
			for (var x = 2; x < w - 2; x++) {
				assertEquals(PLUGIN[y][x], Math.floor(oa.setPositionAndGet(x, y).get() +
					0.5), 0, "(" + x + ", " + y + ")");
			}
		}
	}

	@Test
	public void testMatchesBruteForce3D() {
		final var rnd = new Random(3);
		final var in = ArrayImgs.doubles(7, 6, 5);
		in.forEach(t -> t.set(100 * rnd.nextDouble()));
		final var out = ArrayImgs.doubles(7, 6, 5);
		final var oob = new OutOfBoundsBorderFactory<DoubleType, RandomAccessibleInterval<DoubleType>>();
		ops.op("filter.kuwahara").input(in, 3, oob).output(out).compute();

		final var ext = Views.extend(in, oob).randomAccess();
		final var c = out.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			var bestVar = Double.POSITIVE_INFINITY;
			var bestMean = 0.0;
			for (var w = 0; w < 8; w++) {
				double sum = 0, sumSq = 0;
				for (var dz = 0; dz <= 1; dz++)
					for (var dy = 0; dy <= 1; dy++)
						for (var dx = 0; dx <= 1; dx++) {
							final var v = ext.setPositionAndGet( //
								c.getLongPosition(0) + ((w & 1) == 0 ? -dx : dx), //
								c.getLongPosition(1) + ((w & 2) == 0 ? -dy : dy), //
								c.getLongPosition(2) + ((w & 4) == 0 ? -dz : dz)).get();
							sum += v;
							sumSq += v * v;
						}
				final var mean = sum / 8;
				final var variance = sumSq / 8 - mean * mean;
				if (variance < bestVar) {
					bestVar = variance;
					bestMean = mean;
				}
			}
			assertEquals(bestMean, c.get().get(), 1e-9);
		}
	}

	@Test
	public void testStepEdgePreservedAndNoiseReduced() {
		final var rnd = new Random(11);
		final long w = 40, h = 30;
		final var clean = ArrayImgs.floats(w, h);
		final var noisy = ArrayImgs.floats(w, h);
		final var cc = clean.localizingCursor();
		final var nc = noisy.cursor();
		while (cc.hasNext()) {
			final var v = cc.next();
			v.set(cc.getLongPosition(0) < w / 2 ? 50 : 150);
			nc.next().set(v.get() + (float) (5 * rnd.nextGaussian()));
		}

		// A clean step is reproduced exactly: every pixel has a flat sub-window.
		final var out = ArrayImgs.floats(w, h);
		ops.op("filter.kuwahara").input(clean, 7).output(out).compute();
		final var oc = out.cursor();
		for (final var t : clean)
			assertEquals(t.get(), oc.next().get(), 1e-4);

		ops.op("filter.kuwahara").input(noisy, 7).output(out).compute();
		// Noise is reduced within the flat regions...
		final var flat = Intervals.createMinMax(2, 2, w / 2 - 4, h - 3);
		assertTrue(stdDev(Views.interval(out, flat)) < stdDev(Views.interval(
			noisy, flat)) / 2);
		// ...while the edge stays sharp.
		final var ra = out.randomAccess();
		for (var y = 0; y < h; y++) {
			assertEquals(50, ra.setPositionAndGet(w / 2 - 1, y).get(), 10);
			assertEquals(150, ra.setPositionAndGet(w / 2, y).get(), 10);
		}
	}

	@Test
	public void testInvalidSize() {
		final var img = ArrayImgs.floats(5, 5);
		assertThrows(RuntimeException.class, () -> ops.op("filter.kuwahara").input(
			img, 4).output(ArrayImgs.floats(5, 5)).compute());
	}

	private static double stdDev(
		final RandomAccessibleInterval<? extends RealType<?>> img)
	{
		double sum = 0, sumSq = 0;
		long n = 0;
		for (final var t : img) {
			final var v = t.getRealDouble();
			sum += v;
			sumSq += v * v;
			n++;
		}
		final var mean = sum / n;
		return Math.sqrt(sumSq / n - mean * mean);
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
		ops.op("filter.kuwahara").input(in).output(expected).compute();
		final var actual = ArrayImgs.unsignedBytes(16, 16);
		ops.op("filter.kuwahara").input(in).output(actual).compute();
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
