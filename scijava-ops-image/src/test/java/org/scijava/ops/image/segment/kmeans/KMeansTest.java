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

package org.scijava.ops.image.segment.kmeans;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link KMeans}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class KMeansTest extends AbstractOpTest {

	private static final Nil<ImgLabeling<Integer, IntType>> LABELS =
		new Nil<>()
		{};

	/** Three well-separated intensity populations are recovered exactly. */
	@Test
	public void testIntensityMixture() {
		final var rnd = new Random(1);
		final var means = new double[] { 200, 20, 100 };
		final var in = ArrayImgs.doubles(30, 20);
		final var truth = new int[30 * 20];
		var i = 0;
		for (final var t : in) {
			final var c = rnd.nextInt(3);
			truth[i++] = c;
			t.set(means[c] + 5 * rnd.nextGaussian());
		}
		final ImgLabeling<Integer, IntType> labels = ops.op("segment.kMeans")
			.input(in, 3)
			.outType(LABELS).apply();
		// Labels are ordered by center: 20 -> 0, 100 -> 1, 200 -> 2.
		final var expected = new int[] { 2, 0, 1 };
		i = 0;
		for (final var set : Views.flatIterable(labels)) {
			assertEquals(1, set.size());
			assertEquals(expected[truth[i++]], set.iterator().next());
		}
	}

	/** Clusters separable only in feature space, along a channel axis. */
	@Test
	public void testFeatureVectors() {
		final var rnd = new Random(2);
		final double[][] centers = { { 60, 0 }, { 0, 50 }, { 70, 50 }, { 10, 0 } };
		final var in = ArrayImgs.floats(16, 12, 2);
		final var truth = new int[16 * 12];
		final var ra = in.randomAccess();
		for (var y = 0; y < 12; y++) {
			for (var x = 0; x < 16; x++) {
				final var c = rnd.nextInt(4);
				truth[y * 16 + x] = c;
				for (var ch = 0; ch < 2; ch++) {
					ra.setPositionAndGet(x, y, ch).set((float) (centers[c][ch] + 3 * rnd
						.nextGaussian()));
				}
			}
		}
		final ImgLabeling<Integer, IntType> labels = ops.op("segment.kMeans")
			.input(in, 4, 2, 7L)
			.outType(LABELS).apply();
		assertArrayEquals(new long[] { 16, 12 }, labels.dimensionsAsLongArray());
		// Lexicographic order: (0,50), (10,0), (60,0), (70,50).
		final var expected = new int[] { 2, 0, 3, 1 };
		var i = 0;
		for (final var set : Views.flatIterable(labels))
			assertEquals(expected[truth[i++]], set.iterator().next());
	}

	@Test
	public void testDeterminism() {
		final var rnd = new Random(3);
		final var in = ArrayImgs.doubles(25, 25);
		in.forEach(t -> t.set(rnd.nextDouble()));
		final ImgLabeling<Integer, IntType> a = ops.op("segment.kMeans").input(in,
			5, null, 42L).outType(LABELS).apply();
		final ImgLabeling<Integer, IntType> b = ops.op("segment.kMeans").input(in,
			5, null, 42L).outType(LABELS).apply();
		final var ac = Views.flatIterable(a.getIndexImg()).cursor();
		final var bc = Views.flatIterable(b.getIndexImg()).cursor();
		final var used = new boolean[5];
		final var labels = Views.flatIterable(a).cursor();
		while (ac.hasNext()) {
			assertEquals(ac.next().get(), bc.next().get());
			used[labels.next().iterator().next()] = true;
		}
		for (final var u : used)
			assertEquals(true, u);

		final ImgLabeling<Integer, IntType> one = ops.op("segment.kMeans").input(
			in, 1).outType(LABELS).apply();
		for (final var set : Views.flatIterable(one))
			assertEquals(0, set.iterator().next());
	}

	@Test
	public void testInvalidArguments() {
		final var in = ArrayImgs.doubles(3, 3);
		assertThrows(IllegalArgumentException.class, () -> ops.op("segment.kMeans")
			.input(in, 10).outType(LABELS).apply());
		assertThrows(IllegalArgumentException.class, () -> ops.op("segment.kMeans")
			.input(in, 2, 2).outType(LABELS).apply());
	}

	/**
	 * With few iterations, the labels still correspond to the final centers:
	 * for intensity clustering, each label is an intensity interval, and the
	 * intervals are ordered by label. Before, a limit of 0 iterations threw
	 * ArrayIndexOutOfBoundsException, and when the limit was hit, the centers
	 * were moved after the last assignment and the labels were ordered by the
	 * moved centers.
	 */
	@Test
	public void testIterationLimit() {
		for (var seed = 0L; seed < 20; seed++) {
			final var rnd = new Random(seed);
			final var in = ArrayImgs.doubles(20, 20);
			in.forEach(t -> t.set(rnd.nextDouble() * rnd.nextDouble()));
			for (var maxIter = 0; maxIter <= 3; maxIter++) {
				final ImgLabeling<Integer, IntType> labels = ops.op("segment.kMeans")
					.input(in, 4, null, seed, maxIter).outType(LABELS).apply();
				final var min = new double[4];
				final var max = new double[4];
				Arrays.fill(min, Double.POSITIVE_INFINITY);
				Arrays.fill(max, Double.NEGATIVE_INFINITY);
				final var ic = Views.flatIterable(in).cursor();
				for (final var set : Views.flatIterable(labels)) {
					final var v = ic.next().get();
					assertEquals(1, set.size());
					final int l = set.iterator().next();
					min[l] = Math.min(min[l], v);
					max[l] = Math.max(max[l], v);
				}
				for (var l = 0; l + 1 < 4; l++) {
					if (max[l] == Double.NEGATIVE_INFINITY) continue;
					for (var m = l + 1; m < 4; m++) {
						assertTrue(max[l] <= min[m], "seed " + seed + ", maxIterations " +
							maxIter + ": label " + l + " overlaps label " + m);
					}
				}
			}
		}
		final var in = ArrayImgs.doubles(3, 3);
		assertThrows(IllegalArgumentException.class, () -> ops.op("segment.kMeans")
			.input(in, 2, null, 0L, -1).outType(LABELS).apply());
	}

	/** The labeling keeps the min of a translated input. */
	@Test
	public void testTranslatedInput() {
		final var in = ArrayImgs.doubles(10, 8, 2);
		final var c = in.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set(c.getIntPosition(0) < 5 ? c.getIntPosition(2) : 50);
		}
		final var translated = Views.translate(in, 4, -2, 3);
		final ImgLabeling<Integer, IntType> labels = ops.op("segment.kMeans")
			.input(translated, 2, 2).outType(LABELS).apply();
		assertArrayEquals(new long[] { 4, -2 }, labels.minAsLongArray());
		assertArrayEquals(new long[] { 13, 5 }, labels.maxAsLongArray());
		final var lc = labels.localizingCursor();
		while (lc.hasNext()) {
			final var set = lc.next();
			assertEquals(lc.getIntPosition(0) < 9 ? 0 : 1, set.iterator().next());
		}
	}

	/** NaN pixels are left unlabeled and do not affect the clusters. */
	@Test
	public void testNaN() {
		final var in = ArrayImgs.doubles(10, 10);
		final var c = in.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final var x = c.getIntPosition(0);
			c.get().set(x == 3 ? Double.NaN : x < 5 ? 10 : 90);
		}
		final ImgLabeling<Integer, IntType> labels = ops.op("segment.kMeans")
			.input(in, 2).outType(LABELS).apply();
		final var lc = labels.localizingCursor();
		while (lc.hasNext()) {
			final var set = lc.next();
			final var x = lc.getIntPosition(0);
			if (x == 3) assertEquals(0, set.size());
			else assertEquals(x < 5 ? 0 : 1, set.iterator().next());
		}
		in.forEach(t -> t.set(Double.NaN));
		assertThrows(IllegalArgumentException.class, () -> ops.op("segment.kMeans")
			.input(in, 1).outType(LABELS).apply());
	}
}
