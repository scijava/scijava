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

package org.scijava.ops.image.segment.graphCut;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Predicate;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link DefaultGraphCut} and {@link GraphCutCosts}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class GraphCutTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<BitType>> MASK =
		new Nil<>()
		{};

	/** A noisy bright square on a dark background is recovered. */
	@Test
	public void testNoisyTwoRegions() {
		final var truth = new boolean[64 * 64];
		final var image = noisy(new long[] { 64, 64 }, truth, 0.5, 1, x -> x[0] >=
			16 && x[0] < 48 && x[1] >= 20 && x[1] < 52);

		final var pixelwise = errorRate(segment(image, 0.0, null, null, null,
			null), truth);
		assertTrue(pixelwise > 0.1, "pixelwise error " + pixelwise);
		final var smooth = errorRate(segment(image, null, null, null, null, null),
			truth);
		assertTrue(smooth < 0.01, "graph cut error " + smooth);
		final var full = errorRate(segment(image, 1.0, null, null, false, true),
			truth);
		assertTrue(full < 0.01, "full connectivity error " + full);
		// Note: the contrast term weakens the smoothness term across noise too,
		// which a larger lambda compensates for.
		final var contrast = errorRate(segment(image, 3.0, null, null, true,
			false), truth);
		assertTrue(contrast < 0.03, "contrast error " + contrast);
	}

	/** A noisy ball in 3D is recovered, with full connectivity. */
	@Test
	public void testNoisyBall3D() {
		final var truth = new boolean[24 * 24 * 24];
		final var image = noisy(new long[] { 24, 24, 24 }, truth, 0.6, 2, x -> {
			var r2 = 0.0;
			for (final var c : x)
				r2 += (c - 11.5) * (c - 11.5);
			return r2 < 64;
		});
		final var pixelwise = errorRate(segment(image, 0.0, 1.0, 0.0, null,
			true), truth);
		// Note: with full connectivity in 3D, a boundary cuts about six times as
		// much weight per unit area as with face connectivity.
		final var smooth = errorRate(segment(image, 0.25, 1.0, 0.0, null, true),
			truth);
		assertTrue(pixelwise > 0.1, "pixelwise error " + pixelwise);
		assertTrue(smooth < 0.02, "graph cut error " + smooth);
	}

	/** With lambda = 0, the segmentation thresholds the data term. */
	@Test
	public void testZeroLambdaThresholds() {
		final var rnd = new Random(3);
		final var image = ArrayImgs.doubles(17, 13);
		for (final var v : image)
			v.set(100 * rnd.nextDouble());
		final double fg = 70, bg = 20;
		final var mask = segment(image, 0.0, fg, bg, null, null);
		final var m = Views.flatIterable(mask).cursor();
		for (final var v : Views.flatIterable(image)) {
			final var expected = Math.abs(v.get() - fg) < Math.abs(v.get() - bg);
			assertEquals(expected, m.next().get(), "value " + v.get());
		}

		// The same holds with cost images, also with negative costs.
		final var fgCost = ArrayImgs.doubles(17, 13);
		final var bgCost = ArrayImgs.doubles(17, 13);
		for (final var v : fgCost)
			v.set(rnd.nextGaussian());
		for (final var v : bgCost)
			v.set(rnd.nextGaussian());
		final var costMask = segmentCosts(fgCost, bgCost, 0.0, null, null);
		final var f = fgCost.cursor();
		final var b = bgCost.cursor();
		final var c = Views.flatIterable(costMask).cursor();
		while (f.hasNext())
			assertEquals(f.next().get() < b.next().get(), c.next().get());
	}

	/**
	 * On a small grid, the segmentation of the cost op has the minimal energy
	 * among all labelings, found by brute force.
	 */
	@Test
	public void testMinimalEnergy() {
		final var rnd = new Random(11);
		final long[] dims = { 4, 3 };
		for (var trial = 0; trial < 20; trial++) {
			final var fgCost = ArrayImgs.doubles(dims);
			final var bgCost = ArrayImgs.doubles(dims);
			final var contrast = ArrayImgs.doubles(dims);
			for (final var v : fgCost)
				v.set(rnd.nextDouble());
			for (final var v : bgCost)
				v.set(rnd.nextDouble());
			for (final var v : contrast)
				v.set(rnd.nextDouble());
			final var lambda = 0.5 * rnd.nextDouble();
			final var full = trial % 2 == 1;
			final var useContrast = trial % 4 >= 2;
			final var mask = segmentCosts(fgCost, bgCost, lambda, useContrast
				? contrast : null, full);
			final var labels = new boolean[12];
			var i = 0;
			for (final var v : Views.flatIterable(mask))
				labels[i++] = v.get();

			final var f = fgCost.update(null).getCurrentStorageArray();
			final var b = bgCost.update(null).getCurrentStorageArray();
			final var beta = useContrast ? beta(contrast.update(null)
				.getCurrentStorageArray(), full) : 0;
			final var energy = energy(labels, f, b, contrast.update(null)
				.getCurrentStorageArray(), lambda, beta, full);
			var best = Double.POSITIVE_INFINITY;
			for (var mask2 = 0; mask2 < 1 << 12; mask2++) {
				final var l = new boolean[12];
				for (var p = 0; p < 12; p++)
					l[p] = (mask2 >> p & 1) != 0;
				best = Math.min(best, energy(l, f, b, contrast.update(null)
					.getCurrentStorageArray(), lambda, beta, full));
			}
			assertEquals(best, energy, 1e-9, "trial " + trial);
		}
	}

	/**
	 * The estimated data term scales with the intensities, so an affine
	 * intensity transform gives the same segmentation.
	 */
	@Test
	public void testAffineInvariance() {
		final var truth = new boolean[40 * 30];
		final var image = noisy(new long[] { 40, 30 }, truth, 0.7, 5, x -> x[0] +
			x[1] < 35);
		final var transformed = ArrayImgs.doubles(40, 30);
		final var t = transformed.cursor();
		for (final var v : image)
			t.next().set(3 * v.get() + 10);
		final var expected = bits(segment(image, 1.5, null, null, true, true));
		assertArrayEquals(expected, bits(segment(transformed, 1.5, null, null,
			true, true)));
		var foreground = 0;
		for (final var b : expected)
			if (b) foreground++;
		assertTrue(foreground > 0 && foreground < expected.length);
	}

	private RandomAccessibleInterval<BitType> segment(
		final RandomAccessibleInterval<DoubleType> image, final Double lambda,
		final Double fgMean, final Double bgMean, final Boolean contrast,
		final Boolean full)
	{
		return ops.op("segment.graphCut").input(image, lambda, fgMean, bgMean,
			contrast, full).outType(MASK).apply();
	}

	private RandomAccessibleInterval<BitType> segmentCosts(
		final RandomAccessibleInterval<DoubleType> fgCost,
		final RandomAccessibleInterval<DoubleType> bgCost, final Double lambda,
		final RandomAccessibleInterval<DoubleType> contrast, final Boolean full)
	{
		return ops.op("segment.graphCut").input(fgCost, bgCost, lambda, contrast,
			full).outType(MASK).apply();
	}

	private static boolean[] bits(final RandomAccessibleInterval<BitType> mask) {
		final var bits = new boolean[(int) mask.size()];
		var i = 0;
		for (final var v : Views.flatIterable(mask))
			bits[i++] = v.get();
		return bits;
	}

	private static double errorRate(final RandomAccessibleInterval<BitType> mask,
		final boolean[] truth)
	{
		var errors = 0;
		var i = 0;
		for (final var v : Views.flatIterable(mask))
			if (v.get() != truth[i++]) errors++;
		return (double) errors / truth.length;
	}

	/**
	 * Creates an image of 1 inside the region and 0 outside, plus Gaussian
	 * noise.
	 */
	private static RandomAccessibleInterval<DoubleType> noisy(final long[] dims,
		final boolean[] truth, final double noise, final long seed,
		final Predicate<long[]> region)
	{
		final var rnd = new Random(seed);
		final var image = ArrayImgs.doubles(dims);
		final var cursor = Views.flatIterable(image).localizingCursor();
		var i = 0;
		while (cursor.hasNext()) {
			cursor.fwd();
			truth[i] = region.test(cursor.positionAsLongArray());
			cursor.get().set((truth[i] ? 1 : 0) + noise * rnd.nextGaussian());
			i++;
		}
		return image;
	}

	/** The 4 x 3 grid neighbor pairs, with their distances. */
	private static double[][] pairs(final boolean full) {
		final var pairs = new ArrayList<double[]>();
		for (var y = 0; y < 3; y++)
			for (var x = 0; x < 4; x++)
				for (var dy = -1; dy <= 1; dy++)
					for (var dx = -1; dx <= 1; dx++) {
						if (dy < 0 || dy == 0 && dx <= 0) continue;
						if (!full && dx != 0 && dy != 0) continue;
						final var x2 = x + dx;
						final var y2 = y + dy;
						if (x2 < 0 || x2 >= 4 || y2 >= 3) continue;
						pairs.add(new double[] { y * 4 + x, y2 * 4 + x2, Math.sqrt(dx * dx +
							dy * dy) });
					}
		return pairs.toArray(new double[0][]);
	}

	private static double beta(final double[] contrast, final boolean full) {
		var sum = 0.0;
		final var pairs = pairs(full);
		for (final var pair : pairs)
			sum += Math.pow(contrast[(int) pair[0]] - contrast[(int) pair[1]], 2);
		return pairs.length / (2 * sum);
	}

	private static double energy(final boolean[] labels, final double[] fg,
		final double[] bg, final double[] contrast, final double lambda,
		final double beta, final boolean full)
	{
		var e = 0.0;
		for (var p = 0; p < labels.length; p++)
			e += labels[p] ? fg[p] : bg[p];
		for (final var pair : pairs(full)) {
			final int p = (int) pair[0], q = (int) pair[1];
			if (labels[p] != labels[q]) e += lambda * Math.exp(-beta * Math.pow(
				contrast[p] - contrast[q], 2)) / pair[2];
		}
		return e;
	}
}
