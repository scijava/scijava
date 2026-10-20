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

package org.scijava.ops.image.segment.srm;

import java.util.Arrays;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * Core implementation of Statistical Region Merging (SRM), shared by the
 * {@code segment.statisticalRegionMerging} ops.
 * <p>
 * The algorithm is described in R. Nock and F. Nielsen, "Statistical Region
 * Merging", IEEE Transactions on Pattern Analysis and Machine Intelligence
 * 26(11):1452&ndash;1458 (2004),
 * <a href="https://doi.org/10.1109/TPAMI.2004.110">doi:10.1109/TPAMI.2004.110</a>.
 * </p>
 * <p>
 * This is an n-dimensional translation of the {@code SRM_} plugin of the
 * <a href="https://github.com/fiji/Statistical_Region_Merging">Fiji
 * Statistical_Region_Merging</a> project, Copyright (C) 2009 - 2013 Johannes
 * Schindelin, distributed under the Simplified (2-clause) BSD License. The
 * merge order, the relaxed merging predicate and the region numbering are
 * those of the original plugin; see {@link #run} for the generalizations.
 * </p>
 *
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
final class StatisticalRegionMerging {

	/** Default value of the complexity parameter Q. */
	static final double DEFAULT_Q = 25;

	/** Default number of intensity levels g. */
	static final int DEFAULT_G = 256;

	/** Region index (0-based) of each sample, in flat iteration order. */
	final int[] labels;

	/** Mean intensity (in the units of the input) of each region. */
	final double[] means;

	private StatisticalRegionMerging(final int[] labels, final double[] means) {
		this.labels = labels;
		this.means = means;
	}

	/** Number of regions found. */
	int regionCount() {
		return means.length;
	}

	/**
	 * Runs Statistical Region Merging on the given image.
	 * <p>
	 * The original plugin works on 8-bit images, i.e. g = 256 intensity levels.
	 * To support arbitrary {@link RealType}s, the intensities are mapped
	 * linearly from the image's actual value range {@code [min, max]} onto
	 * {@code [0, g - 1]}; all statistics (the merging predicate and the region
	 * means) are computed on these real-valued, unquantized intensities. Only the
	 * ordering of the neighbor pairs is quantized: the pairs are bucket-sorted
	 * into {@code g} buckets by the integer part of their scaled intensity
	 * difference. For an 8-bit image whose values span the full range 0-255 and
	 * g = 256, this reproduces the original plugin.
	 * </p>
	 * <p>
	 * Neighbors are the 2n face-adjacent samples (4-connectivity in 2D,
	 * 6-connectivity in 3D). The pair connecting a sample at flat index
	 * {@code i} to its successor along axis {@code d} has id {@code n * i + d};
	 * within each bucket, pairs are processed in ascending id order, as in the
	 * original.
	 * </p>
	 *
	 * @param input the image to segment
	 * @param q the complexity parameter Q; larger values yield more regions
	 * @param g the number of intensity levels
	 * @return the segmentation result
	 */
	static <T extends RealType<T>> StatisticalRegionMerging run(
		final RandomAccessibleInterval<T> input, final double q, final int g)
	{
		if (!(q > 0)) throw new IllegalArgumentException("Q must be positive: " +
			q);
		if (g < 2) throw new IllegalArgumentException("g must be at least 2: " +
			g);

		final var n = input.numDimensions();
		final var dims = input.dimensionsAsLongArray();
		final var size = Views.iterable(input).size();
		if (n == 0 || size == 0) {
			return new StatisticalRegionMerging(new int[0], new double[0]);
		}
		if ((long) n * size > Integer.MAX_VALUE - 8) {
			throw new IllegalArgumentException("Image too large for SRM: " + size +
				" samples in " + n + " dimensions");
		}
		final var pixelCount = (int) size;

		// Note: these are the strides of flat iteration order, first axis fastest.
		final var strides = new int[n];
		strides[0] = 1;
		for (var d = 1; d < n; d++) {
			strides[d] = strides[d - 1] * (int) dims[d - 1];
		}

		// Read the input values and determine their range.
		final var values = new double[pixelCount];
		var min = Double.POSITIVE_INFINITY;
		var max = Double.NEGATIVE_INFINITY;
		final var c = Views.flatIterable(input).cursor();
		for (var i = 0; i < pixelCount; i++) {
			final var v = c.next().getRealDouble();
			values[i] = v;
			if (v < min) min = v;
			if (v > max) max = v;
		}
		final var scale = max > min ? (g - 1) / (max - min) : 0;

		// Initialize one region per sample, with intensities scaled to [0, g-1].
		final var average = new double[pixelCount];
		final var count = new int[pixelCount];
		// Note: a negative regionIndex value encodes -1 - parent.
		final var regionIndex = new int[pixelCount];
		for (var i = 0; i < pixelCount; i++) {
			average[i] = (values[i] - min) * scale;
			count[i] = 1;
			regionIndex[i] = i;
		}

		// Bucket-sort all neighbor pairs by their intensity difference.
		// Note: iterating backwards and prepending to each bucket's linked list
		// leaves each list in ascending order of pair id.
		final var nextNeighbor = new int[n * pixelCount];
		final var neighborBucket = new int[g];
		Arrays.fill(neighborBucket, -1);
		final var pos = new long[n];
		for (var d = 0; d < n; d++)
			pos[d] = dims[d] - 1;
		for (var index = pixelCount - 1; index >= 0; index--) {
			for (var d = n - 1; d >= 0; d--) {
				if (pos[d] >= dims[d] - 1) continue;
				final var diff = Math.abs(values[index] - values[index +
					strides[d]]) * scale;
				final var bucket = (int) Math.min(g - 1, diff);
				final var pair = n * index + d;
				nextNeighbor[pair] = neighborBucket[bucket];
				neighborBucket[bucket] = pair;
			}
			// Move to the previous position in flat iteration order.
			for (var d = 0; d < n; d++) {
				if (--pos[d] >= 0) break;
				pos[d] = dims[d] - 1;
			}
		}

		// Merge regions, visiting pairs in ascending order of intensity
		// difference. As in the original, delta = 1 / (6 |I|) and the relaxed
		// predicate is used, since the paper's exact bound is more prone to
		// oversegmenting.
		final var factor = (double) g * g / 2 / q;
		final var logDelta = 2 * Math.log(6.0 * pixelCount);
		for (final var head : neighborBucket) {
			var pair = head;
			while (pair >= 0) {
				final var p1 = pair / n;
				final var p2 = p1 + strides[pair % n];
				final var r1 = root(regionIndex, p1);
				final var r2 = root(regionIndex, p2);
				if (r1 != r2 && predicate(average, count, r1, r2, g, factor,
					logDelta))
				{
					merge(average, count, regionIndex, r1, r2);
				}
				pair = nextNeighbor[pair];
			}
		}

		// Consolidate: number the regions in order of their first sample.
		// Note: a negative regionIndex always points to a smaller index, which
		// has therefore already been consolidated.
		final var means = new double[pixelCount];
		var regionCount = 0;
		for (var i = 0; i < pixelCount; i++) {
			if (regionIndex[i] < 0) {
				regionIndex[i] = regionIndex[-1 - regionIndex[i]];
			}
			else {
				means[regionCount] = scale == 0 ? min : min + average[i] / scale;
				regionIndex[i] = regionCount++;
			}
		}
		return new StatisticalRegionMerging(regionIndex, Arrays.copyOf(means,
			regionCount));
	}

	/** Finds the root region of sample {@code i}, compressing the path. */
	private static int root(final int[] regionIndex, final int i) {
		var r = i;
		while (regionIndex[r] < 0)
			r = -1 - regionIndex[r];
		// Note: path compression speeds up lookups and leaves roots unchanged.
		var j = i;
		while (regionIndex[j] < 0) {
			final var next = -1 - regionIndex[j];
			regionIndex[j] = -1 - r;
			j = next;
		}
		return r;
	}

	/**
	 * The relaxed merging predicate of the original plugin: should regions
	 * {@code r1} and {@code r2} be merged?
	 */
	private static boolean predicate(final double[] average, final int[] count,
		final int r1, final int r2, final int g, final double factor,
		final double logDelta)
	{
		final var difference = average[r1] - average[r2];
		final var c1 = count[r1];
		final var c2 = count[r2];
		final var log1 = Math.log(1 + c1) * Math.min(g, c1);
		final var log2 = Math.log(1 + c2) * Math.min(g, c2);
		return difference * difference < .1 * factor * ((log1 + logDelta) / c1 +
			(log2 + logDelta) / c2);
	}

	/** Merges two root regions, keeping the smaller index as the new root. */
	private static void merge(final double[] average, final int[] count,
		final int[] regionIndex, final int r1, final int r2)
	{
		final var mergedCount = count[r1] + count[r2];
		final var mergedAverage = (average[r1] * count[r1] + average[r2] *
			count[r2]) / mergedCount;
		final var lo = Math.min(r1, r2);
		final var hi = Math.max(r1, r2);
		average[lo] = mergedAverage;
		count[lo] = mergedCount;
		regionIndex[hi] = -1 - lo;
	}
}
