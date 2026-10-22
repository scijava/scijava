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

import java.util.Arrays;
import java.util.Random;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Clusters the pixels of an image by k-means, on their intensities or on
 * feature vectors stored along a channel axis.
 * <p>
 * The cluster centers are initialized by k-means++ seeding (Arthur &amp;
 * Vassilvitskii 2007): the first center is a uniformly random pixel, and each
 * further center is a pixel chosen with probability proportional to its
 * squared distance to the nearest center chosen so far. Lloyd iterations
 * (Lloyd 1982) then alternate between assigning each pixel to its nearest
 * center (in Euclidean distance) and moving each center to the mean of its
 * pixels, until no assignment changes or the iteration limit is reached. A
 * cluster that becomes empty is moved to the pixel farthest from its center.
 * The iterations always end with an assignment step, so every pixel belongs
 * to the nearest of the centers by which the clusters are numbered. The
 * result depends only on the input and the random seed.
 * </p>
 * <p>
 * Pixels with a NaN value (in any channel) are not clustered and are left
 * unlabeled.
 * </p>
 * <p>
 * The clusters are numbered {@code 0, ..., k-1} in lexicographic order of
 * their centers, so for intensity clustering label 0 is the darkest cluster.
 * </p>
 * <p>
 * Implemented from the papers.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @see <a href="https://doi.org/10.1109/TIT.1982.1056489">S. P. Lloyd, "Least
 *      squares quantization in PCM", IEEE Trans. Inf. Theory 28(2):129-137,
 *      1982</a>
 * @see <a href="https://dl.acm.org/doi/10.5555/1283383.1283494">D. Arthur and
 *      S. Vassilvitskii, "k-means++: the advantages of careful seeding", Proc.
 *      18th ACM-SIAM Symp. Discrete Algorithms, 1027-1035, 2007</a>
 * @implNote op names='segment.kMeans'
 */
public class KMeans<T extends RealType<T>> implements
	Functions.Arity5<RandomAccessibleInterval<T>, Integer, Integer, Long, Integer, ImgLabeling<Integer, IntType>>
{

	@OpDependency(name = "create.imgLabeling")
	private BiFunction<Dimensions, IntType, ImgLabeling<Integer, IntType>> labelingCreator;

	/**
	 * Clusters the pixels by k-means.
	 *
	 * @param input the image; with a channel axis, the values along that axis
	 *          form the feature vector of each pixel
	 * @param k the number of clusters
	 * @param channelAxis the axis holding the features, or null (the default)
	 *          to cluster scalar intensities
	 * @param seed the seed of the random k-means++ initialization (default: 0)
	 * @param maxIterations the maximum number of Lloyd iterations, i.e. of
	 *          updates of the centers (default: 100); with 0, the pixels are
	 *          assigned to the nearest k-means++ seeds
	 * @return a labeling of the pixels (over all axes except the channel axis,
	 *         with the same interval as the input) with cluster labels
	 *         {@code 0, ..., k-1}
	 */
	@Override
	public ImgLabeling<Integer, IntType> apply(
		final RandomAccessibleInterval<T> input, final Integer k,
		@Nullable final Integer channelAxis, @Nullable final Long seed,
		@Nullable final Integer maxIterations)
	{
		final var n = input.numDimensions();
		if (channelAxis != null && (channelAxis < 0 || channelAxis >= n)) {
			throw new IllegalArgumentException("Invalid channel axis: " +
				channelAxis);
		}
		final RandomAccessibleInterval<T> first = channelAxis == null ? input
			: Views.hyperSlice(input, channelAxis, input.min(channelAxis));
		final var size = Intervals.numElements(first);
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image is too large: " + size +
				" pixels");
		}
		final var total = (int) size;
		final var f = channelAxis == null ? 1 : (int) input.dimension(channelAxis);
		final var all = new double[total * f];
		for (var c = 0; c < f; c++) {
			final RandomAccessibleInterval<T> slice = channelAxis == null ? input
				: Views.hyperSlice(input, channelAxis, input.min(channelAxis) + c);
			var i = 0;
			for (final var t : Views.flatIterable(slice))
				all[i++ * f + c] = t.getRealDouble();
		}
		// Note: pixels with a NaN feature are not clustered.
		final var valid = new boolean[total];
		var count = 0;
		for (var p = 0; p < total; p++) {
			valid[p] = true;
			for (var c = 0; c < f; c++)
				if (Double.isNaN(all[p * f + c])) valid[p] = false;
			if (valid[p]) count++;
		}
		if (k == null || k < 1 || k > count) {
			throw new IllegalArgumentException("k must be in [1, " + count +
				"] (the number of pixels without NaN): " + k);
		}
		final int maxIter = maxIterations == null ? 100 : maxIterations;
		if (maxIter < 0) {
			throw new IllegalArgumentException("Invalid maximum number of " +
				"iterations: " + maxIter);
		}
		final double[] features;
		if (count == total) features = all;
		else {
			features = new double[count * f];
			var q = 0;
			for (var p = 0; p < total; p++)
				if (valid[p]) System.arraycopy(all, p * f, features, q++ * f, f);
		}

		final var centers = seedCenters(features, count, f, k, new Random(
			seed == null ? 0 : seed));
		final var assignment = new int[count];
		Arrays.fill(assignment, -1);
		final var sums = new double[k * f];
		final var sizes = new int[k];
		for (var it = 0;; it++) {
			var changed = false;
			for (var p = 0; p < count; p++) {
				final var nearest = nearest(features, p, centers, k, f);
				if (nearest != assignment[p]) {
					assignment[p] = nearest;
					changed = true;
				}
			}
			// Note: stopping right after an assignment step keeps the labels
			// consistent with the centers by which they are numbered.
			if (!changed || it >= maxIter) break;
			Arrays.fill(sums, 0);
			Arrays.fill(sizes, 0);
			for (var p = 0; p < count; p++) {
				final var a = assignment[p];
				sizes[a]++;
				for (var c = 0; c < f; c++)
					sums[a * f + c] += features[p * f + c];
			}
			for (var j = 0; j < k; j++) {
				if (sizes[j] > 0) {
					for (var c = 0; c < f; c++)
						centers[j * f + c] = sums[j * f + c] / sizes[j];
				}
				else {
					// Move the empty cluster to the worst-fitting pixel.
					var worst = 0;
					var worstDist = -1.0;
					for (var p = 0; p < count; p++) {
						final var d = dist2(features, p, centers, assignment[p], f);
						if (d > worstDist) {
							worstDist = d;
							worst = p;
						}
					}
					System.arraycopy(features, worst * f, centers, j * f, f);
					assignment[worst] = j;
				}
			}
		}

		// Number the clusters in lexicographic order of their centers.
		final var order = new Integer[k];
		for (var j = 0; j < k; j++)
			order[j] = j;
		Arrays.sort(order, (a, b) -> {
			for (var c = 0; c < f; c++) {
				final var cmp = Double.compare(centers[a * f + c], centers[b * f + c]);
				if (cmp != 0) return cmp;
			}
			return Integer.compare(a, b);
		});
		final var rank = new int[k];
		for (var j = 0; j < k; j++)
			rank[order[j]] = j;

		final var output = labelingCreator.apply(first, new IntType());
		var p = 0;
		var q = 0;
		for (final var label : Views.flatIterable(output))
			if (valid[p++]) label.add(rank[assignment[q++]]);
		return output;
	}

	/** Chooses the initial centers by k-means++ seeding. */
	private static double[] seedCenters(final double[] features,
		final int count, final int f, final int k, final Random random)
	{
		final var centers = new double[k * f];
		System.arraycopy(features, random.nextInt(count) * f, centers, 0, f);
		final var minDist = new double[count];
		Arrays.fill(minDist, Double.POSITIVE_INFINITY);
		for (var j = 1; j < k; j++) {
			var total = 0.0;
			for (var p = 0; p < count; p++) {
				minDist[p] = Math.min(minDist[p], dist2(features, p, centers, j - 1,
					f));
				total += minDist[p];
			}
			var chosen = count - 1;
			if (total > 0) {
				var r = random.nextDouble() * total;
				for (var p = 0; p < count; p++) {
					r -= minDist[p];
					if (r < 0) {
						chosen = p;
						break;
					}
				}
			}
			else chosen = random.nextInt(count);
			System.arraycopy(features, chosen * f, centers, j * f, f);
		}
		return centers;
	}

	private static int nearest(final double[] features, final int p,
		final double[] centers, final int k, final int f)
	{
		var best = 0;
		var bestDist = Double.POSITIVE_INFINITY;
		for (var j = 0; j < k; j++) {
			final var d = dist2(features, p, centers, j, f);
			if (d < bestDist) {
				bestDist = d;
				best = j;
			}
		}
		return best;
	}

	private static double dist2(final double[] features, final int p,
		final double[] centers, final int j, final int f)
	{
		var sum = 0.0;
		for (var c = 0; c < f; c++) {
			final var d = features[p * f + c] - centers[j * f + c];
			sum += d * d;
		}
		return sum;
	}
}
