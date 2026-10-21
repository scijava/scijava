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

package org.scijava.ops.image.threshold.rats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Computes the local threshold surface of Robust Automatic Threshold
 * Selection (RATS), following Kittler, Illingworth and F&ouml;glein (1985)
 * and its hierarchical, noise-robust extension by Wilkinson (1998).
 * <p>
 * The idea of RATS is that pixels on object boundaries, where the gradient is
 * strong, have intensities halfway between object and background, so the
 * gradient-weighted mean intensity {@code sum(e I) / sum(e)} is a good
 * threshold. To adapt to uneven illumination, this threshold is computed in
 * regions of a 2^n-tree (a quadtree in 2D) and interpolated. Precisely, this
 * op implements the following:
 * </p>
 * <ol>
 * <li>The edge strength {@code e} of each pixel is the magnitude of the
 * gradient {@code g} computed by central differences
 * {@code g_d = (I(x + 1) - I(x - 1)) / 2} along every dimension {@code d},
 * with border pixels replicated outside the image. The intensity paired with
 * {@code e} is not {@code I(x)} itself but the mean
 * {@code m_d = (I(x + 1) + I(x - 1)) / 2} of the two pixels of each
 * difference, averaged over the dimensions with weights {@code g_d^2}. For
 * an ideal step edge, this is exactly the mid-level on both sides of the
 * step, so a region containing only one side of a boundary still gets an
 * unbiased threshold.</li>
 * <li>Noise gate: {@code e} is set to zero where {@code e < lambda * sigma},
 * with {@code sigma} the standard deviation of the image noise.</li>
 * <li>The image is divided into leaf regions of {@code minLeafSize} pixels in
 * every dimension (smaller at the far borders). Each level of the tree above
 * merges up to 2^n neighboring regions (2 along each dimension) until a
 * single root region covers the image. Each region accumulates
 * {@code sum(e)} and {@code sum(e m)}.</li>
 * <li>Minimum edge strength: a region is reliable if
 * {@code sum(e) > lambda * sigma * minLeafSize}, which is roughly the edge
 * strength of a single boundary just above the noise gate crossing a leaf
 * region. A reliable region gets the threshold {@code sum(e m) / sum(e)}; an
 * unreliable region inherits the threshold of its parent. If the root is not
 * reliable either (which means that the image has no edges at all), it gets
 * the maximum intensity of the image, so that no pixel is foreground.</li>
 * <li>The leaf thresholds are assigned to the centers of the leaf regions and
 * interpolated n-linearly in between; beyond the outermost centers, the
 * nearest value is used.</li>
 * </ol>
 * <p>
 * If {@code sigma} is not given, it is estimated robustly from the
 * differences {@code d} of horizontally neighboring pixels (along dimension
 * 0) as {@code 1.4826 * MAD(d) / sqrt(2)}, which is insensitive to edges
 * and to smooth illumination gradients.
 * </p>
 * <p>
 * This implementation was written from the general description of the
 * method, without reference to any existing RATS code. The gradient
 * operator, the use of {@code m}, the reliability criterion, the fallback for
 * the root and the noise estimate are choices of this implementation, so
 * results differ in detail from the papers and from other RATS
 * implementations (such as the RATS plugin of Fiji).
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @see "J. Kittler, J. Illingworth and J. F&ouml;glein, Threshold selection
 *      based on a simple image statistic, Comput. Vis. Graph. Image Process.
 *      30:125-147, 1985"
 * @see "M. H. F. Wilkinson, Optimizing edge detectors for robust automatic
 *      threshold selection: coping with edge curvature and noise, Graph.
 *      Models Image Process. 60(5):385-401, 1998"
 * @implNote op names='threshold.ratsSurface'
 */
public class RatsSurface<T extends RealType<T>> implements
	Functions.Arity4<RandomAccessibleInterval<T>, Double, Double, Integer, RandomAccessibleInterval<FloatType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> createOp;

	/**
	 * Computes the RATS threshold surface.
	 *
	 * @param input the image to threshold
	 * @param sigma the standard deviation of the image noise (default: estimated
	 *          from the image)
	 * @param lambda the noise gate, in multiples of sigma (default: 3)
	 * @param minLeafSize the side length, in pixels, of the smallest regions of
	 *          the tree (default: 32)
	 * @return the threshold of every pixel
	 */
	@Override
	public RandomAccessibleInterval<FloatType> apply(
		final RandomAccessibleInterval<T> input, @Nullable final Double sigma,
		@Nullable final Double lambda, @Nullable final Integer minLeafSize)
	{
		final var surface = surface(input, sigma, lambda, minLeafSize);
		final var output = createOp.apply(input, new FloatType());
		var i = 0;
		for (final var t : Views.flatIterable(output))
			t.setReal(surface[i++]);
		return output;
	}

	/** Computes the threshold surface, in flat iteration order. */
	static <T extends RealType<T>> double[] surface(
		final RandomAccessibleInterval<T> input, final Double sigma,
		final Double lambda, final Integer minLeafSize)
	{
		final var n = input.numDimensions();
		final var size = Intervals.numElements(input);
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image is too large: " + size +
				" pixels");
		}
		final var count = (int) size;
		final double lam = lambda == null ? 3 : lambda;
		final int leaf = minLeafSize == null ? 32 : minLeafSize;
		if (leaf < 1) {
			throw new IllegalArgumentException("Invalid leaf size: " + leaf);
		}
		final var dims = new int[n];
		for (var d = 0; d < n; d++)
			dims[d] = (int) input.dimension(d);
		final var strides = new int[n];
		for (var d = 0; d < n; d++)
			strides[d] = d == 0 ? 1 : strides[d - 1] * dims[d - 1];

		final var img = new double[count];
		var i = 0;
		for (final var t : Views.flatIterable(input))
			img[i++] = t.getRealDouble();
		final var noise = sigma == null ? estimateNoise(img, dims) : sigma;
		final var gate = lam * noise;

		// Leaf grid and tree levels.
		final var levels = new ArrayList<int[]>();
		var cells = new int[n];
		for (var d = 0; d < n; d++)
			cells[d] = (dims[d] + leaf - 1) / leaf;
		levels.add(cells);
		while (Arrays.stream(cells).anyMatch(c -> c > 1)) {
			final var parent = new int[n];
			for (var d = 0; d < n; d++)
				parent[d] = (cells[d] + 1) / 2;
			levels.add(parent);
			cells = parent;
		}
		final var nLevels = levels.size();
		final var sumE = new double[nLevels][];
		final var sumEI = new double[nLevels][];
		for (var l = 0; l < nLevels; l++) {
			sumE[l] = new double[product(levels.get(l))];
			sumEI[l] = new double[sumE[l].length];
		}

		// Edge strengths, accumulated into the leaves.
		final var leafCells = levels.get(0);
		final var pos = new int[n];
		var max = Double.NEGATIVE_INFINITY;
		for (i = 0; i < count; i++) {
			var g2 = 0.0;
			var gm = 0.0;
			var cell = 0;
			var cellStride = 1;
			for (var d = 0; d < n; d++) {
				final var lo = pos[d] > 0 ? i - strides[d] : i;
				final var hi = pos[d] < dims[d] - 1 ? i + strides[d] : i;
				final var g = (img[hi] - img[lo]) / 2;
				g2 += g * g;
				gm += g * g * (img[hi] + img[lo]) / 2;
				cell += pos[d] / leaf * cellStride;
				cellStride *= leafCells[d];
			}
			final var e = Math.sqrt(g2);
			if (e >= gate && e > 0) {
				sumE[0][cell] += e;
				sumEI[0][cell] += e * gm / g2;
			}
			max = Math.max(max, img[i]);
			increment(pos, dims);
		}

		// Sums of the upper levels.
		for (var l = 1; l < nLevels; l++) {
			final var child = levels.get(l - 1);
			final var parent = levels.get(l);
			final var cpos = new int[n];
			for (var c = 0; c < sumE[l - 1].length; c++) {
				var p = 0;
				var stride = 1;
				for (var d = 0; d < n; d++) {
					p += cpos[d] / 2 * stride;
					stride *= parent[d];
				}
				sumE[l][p] += sumE[l - 1][c];
				sumEI[l][p] += sumEI[l - 1][c];
				increment(cpos, child);
			}
		}

		// Thresholds, from the root down to the leaves.
		final var minEdge = gate * leaf;
		var thresholds = new double[] { sumE[nLevels - 1][0] > minEdge
			? sumEI[nLevels - 1][0] / sumE[nLevels - 1][0] : max };
		for (var l = nLevels - 2; l >= 0; l--) {
			final var child = levels.get(l);
			final var parent = levels.get(l + 1);
			final var next = new double[sumE[l].length];
			final var cpos = new int[n];
			for (var c = 0; c < next.length; c++) {
				var p = 0;
				var stride = 1;
				for (var d = 0; d < n; d++) {
					p += cpos[d] / 2 * stride;
					stride *= parent[d];
				}
				next[c] = sumE[l][c] > minEdge ? sumEI[l][c] / sumE[l][c]
					: thresholds[p];
				increment(cpos, child);
			}
			thresholds = next;
		}

		return interpolate(thresholds, leafCells, dims, leaf);
	}

	/** Interpolates leaf thresholds n-linearly between leaf centers. */
	private static double[] interpolate(final double[] leafValues,
		final int[] cells, final int[] dims, final int leaf)
	{
		final var n = dims.length;
		// Per dimension and coordinate: lower cell index and weight of the upper.
		final var lower = new int[n][];
		final var weight = new double[n][];
		for (var d = 0; d < n; d++) {
			final var centers = new double[cells[d]];
			for (var c = 0; c < cells[d]; c++) {
				final var end = Math.min((c + 1) * leaf, dims[d]) - 1;
				centers[c] = (c * leaf + end) / 2.0;
			}
			lower[d] = new int[dims[d]];
			weight[d] = new double[dims[d]];
			var c = 0;
			for (var x = 0; x < dims[d]; x++) {
				while (c < cells[d] - 2 && x >= centers[c + 1])
					c++;
				lower[d][x] = c;
				if (cells[d] == 1) weight[d][x] = 0;
				else {
					final var w = (x - centers[c]) / (centers[c + 1] - centers[c]);
					weight[d][x] = Math.max(0, Math.min(1, w));
				}
			}
		}
		final var cellStrides = new int[n];
		for (var d = 0; d < n; d++)
			cellStrides[d] = d == 0 ? 1 : cellStrides[d - 1] * cells[d - 1];
		final var result = new double[product(dims)];
		final var pos = new int[n];
		final var corners = 1 << n;
		for (var i = 0; i < result.length; i++) {
			// Note: values are interpolated relative to the first corner, so that
			// equal corner values are reproduced exactly.
			var baseCell = 0;
			for (var d = 0; d < n; d++)
				baseCell += lower[d][pos[d]] * cellStrides[d];
			final var base = leafValues[baseCell];
			var value = 0.0;
			for (var k = 1; k < corners; k++) {
				var w = 1.0;
				var cell = 0;
				for (var d = 0; d < n && w > 0; d++) {
					final var up = (k >> d & 1) == 1;
					final var wd = weight[d][pos[d]];
					w *= up ? wd : 1 - wd;
					cell += (lower[d][pos[d]] + (up ? 1 : 0)) * cellStrides[d];
				}
				if (w > 0) value += w * (leafValues[cell] - base);
			}
			result[i] = base + value;
			increment(pos, dims);
		}
		return result;
	}

	/**
	 * Estimates the noise standard deviation from the differences of
	 * neighbors along dimension 0.
	 */
	private static double estimateNoise(final double[] img, final int[] dims) {
		if (dims[0] < 2) return 0;
		final var diffs = new double[img.length / dims[0] * (dims[0] - 1)];
		var k = 0;
		for (var i = 0; i < img.length; i++)
			if (i % dims[0] < dims[0] - 1) diffs[k++] = img[i + 1] - img[i];
		final var median = median(diffs);
		for (var j = 0; j < diffs.length; j++)
			diffs[j] = Math.abs(diffs[j] - median);
		return 1.4826 * median(diffs) / Math.sqrt(2);
	}

	private static double median(final double[] values) {
		final var sorted = values.clone();
		Arrays.sort(sorted);
		final var m = sorted.length / 2;
		return sorted.length % 2 == 1 ? sorted[m] : (sorted[m - 1] + sorted[m]) /
			2;
	}

	private static int product(final int[] dims) {
		var p = 1;
		for (final var d : dims)
			p *= d;
		return p;
	}

	/** Advances a position in flat iteration order. */
	private static void increment(final int[] pos, final int[] dims) {
		for (var d = 0; d < pos.length; d++) {
			if (++pos[d] < dims[d]) return;
			pos[d] = 0;
		}
	}
}
