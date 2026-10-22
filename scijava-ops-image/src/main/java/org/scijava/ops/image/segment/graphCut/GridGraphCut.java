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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * Binary segmentation of an n-dimensional grid by a minimum graph cut, shared
 * by the {@code segment.graphCut} ops.
 * <p>
 * It minimizes the energy
 * {@code E(L) = sum_p D_p(L_p) + sum_{p,q} w_pq [L_p != L_q]} over the binary
 * labelings {@code L}, where {@code D_p} are the costs of labeling pixel
 * {@code p} foreground or background, and the sum of the smoothness term runs
 * over the pairs of neighboring pixels, with
 * {@code w_pq = lambda exp(-beta (I_p - I_q)^2) / |p - q|} (Boykov &amp;
 * Jolly (2001), "Interactive graph cuts for optimal boundary &amp; region
 * segmentation of objects in N-D images", Proc. ICCV 1:105-112). Without a
 * contrast image, {@code beta = 0}, i.e. the Potts model. With one,
 * {@code beta = 1 / (2 <(I_p - I_q)^2>)}, averaged over all neighbor pairs
 * (Rother, Kolmogorov &amp; Blake (2004), "GrabCut: interactive foreground
 * extraction using iterated graph cuts", ACM Trans. Graph. 23(3):309-314).
 * The minimum is found exactly as the minimum cut of a graph with one node
 * per pixel (Greig, Porteous &amp; Seheult (1989), J. R. Stat. Soc. B
 * 51(2):271-279), computed with {@link BKMaxFlow}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class GridGraphCut {

	private GridGraphCut() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Segments a grid.
	 *
	 * @param dims the grid dimensions
	 * @param foregroundCost the cost of labeling each pixel foreground,
	 *          flattened with dimension 0 varying fastest
	 * @param backgroundCost the cost of labeling each pixel background
	 * @param contrast the contrast image, or {@code null}
	 * @param lambda the weight of the smoothness term
	 * @param fullConnectivity whether pixels neighbor diagonally too, or only
	 *          across faces
	 * @return whether each pixel is foreground
	 */
	public static boolean[] segment(final long[] dims,
		final double[] foregroundCost, final double[] backgroundCost,
		final double[] contrast, final double lambda,
		final boolean fullConnectivity)
	{
		if (!(lambda >= 0) || Double.isInfinite(lambda))
			throw new IllegalArgumentException(
				"The smoothness weight must be finite and nonnegative.");
		final var n = dims.length;
		final var size = foregroundCost.length;
		final var offsets = offsets(n, fullConnectivity);
		final var strides = new long[n];
		var stride = 1L;
		for (var d = 0; d < n; d++) {
			strides[d] = stride;
			stride *= dims[d];
		}

		final var hint = lambda > 0 ? (long) size * offsets.size() : 0;
		if (hint > Integer.MAX_VALUE / 2) throw new IllegalArgumentException(
			"The image is too large.");
		final var graph = new BKMaxFlow(size, (int) hint);
		for (var p = 0; p < size; p++) {
			final var f = foregroundCost[p];
			final var b = backgroundCost[p];
			if (Double.isNaN(f) || Double.isNaN(b) || Double.isInfinite(f - b))
				throw new IllegalArgumentException(
					"The costs must be finite numbers.");
			final var min = Math.min(f, b);
			// Note: the source side of the cut is the foreground.
			graph.addTerminalWeights(p, b - min, f - min);
		}

		if (lambda > 0) {
			final var beta = contrast == null ? 0 : beta(dims, strides, offsets,
				contrast);
			forEachPair(dims, strides, offsets, (p, q, offset) -> {
				var w = lambda / offset.distance;
				if (beta > 0) {
					final var diff = contrast[p] - contrast[q];
					w *= Math.exp(-beta * diff * diff);
				}
				graph.addEdge(p, q, w, w);
			});
		}

		graph.computeMaximumFlow();
		final var foreground = new boolean[size];
		for (var p = 0; p < size; p++)
			foreground[p] = graph.isSource(p);
		return foreground;
	}

	/** Copies an image into an array, with dimension 0 varying fastest. */
	static <T extends RealType<T>> double[] flatten(
		final RandomAccessibleInterval<T> image)
	{
		if (image.size() > Integer.MAX_VALUE) throw new IllegalArgumentException(
			"The image is too large.");
		final var data = new double[(int) image.size()];
		var i = 0;
		for (final var v : Views.flatIterable(image))
			data[i++] = v.getRealDouble();
		return data;
	}

	/** Creates the mask image of a segmentation. */
	static RandomAccessibleInterval<BitType> mask(
		final BiFunction<Dimensions, BitType, RandomAccessibleInterval<BitType>> createOp,
		final Dimensions dims, final boolean[] foreground)
	{
		final var mask = createOp.apply(dims, new BitType());
		var i = 0;
		for (final var v : Views.flatIterable(mask))
			v.set(foreground[i++]);
		return mask;
	}

	/** Checks that the images have the same dimensions. */
	static void checkDimensions(final RandomAccessibleInterval<?> a,
		final RandomAccessibleInterval<?> b)
	{
		if (a.numDimensions() != b.numDimensions()) throw new IllegalArgumentException(
			"The images must have the same dimensions.");
		for (var d = 0; d < a.numDimensions(); d++)
			if (a.dimension(d) != b.dimension(d))
				throw new IllegalArgumentException(
					"The images must have the same dimensions.");
	}

	/**
	 * Computes {@code beta = 1 / (2 <(I_p - I_q)^2>)}, or 0 if the contrast
	 * image is constant.
	 */
	private static double beta(final long[] dims, final long[] strides,
		final List<Offset> offsets, final double[] contrast)
	{
		final var sum = new double[2];
		forEachPair(dims, strides, offsets, (p, q, offset) -> {
			final var diff = contrast[p] - contrast[q];
			sum[0] += diff * diff;
			sum[1]++;
		});
		return sum[0] > 0 ? sum[1] / (2 * sum[0]) : 0;
	}

	/**
	 * Gets the offsets to half of the neighbors (those whose last nonzero
	 * coordinate is positive), so that each pair is visited once.
	 */
	private static List<Offset> offsets(final int n,
		final boolean fullConnectivity)
	{
		final List<Offset> offsets = new ArrayList<>();
		final var offset = new int[n];
		final var total = (int) Math.pow(3, n);
		for (var k = 0; k < total; k++) {
			var rest = k;
			var nonzero = 0;
			var last = 0;
			for (var d = 0; d < n; d++) {
				offset[d] = rest % 3 - 1;
				rest /= 3;
				if (offset[d] != 0) {
					nonzero++;
					last = offset[d];
				}
			}
			if (last > 0 && (fullConnectivity || nonzero == 1)) offsets.add(
				new Offset(offset.clone(), Math.sqrt(nonzero)));
		}
		return offsets;
	}

	private static void forEachPair(final long[] dims, final long[] strides,
		final List<Offset> offsets, final PairConsumer action)
	{
		final var n = dims.length;
		var size = 1L;
		for (final var d : dims)
			size *= d;
		final var position = new long[n];
		for (var p = 0; p < size; p++) {
			for (final var offset : offsets) {
				var q = p;
				var inside = true;
				for (var d = 0; d < n && inside; d++) {
					final var x = position[d] + offset.offset[d];
					if (x < 0 || x >= dims[d]) inside = false;
					q += offset.offset[d] * strides[d];
				}
				if (inside) action.accept(p, q, offset);
			}
			for (var d = 0; d < n; d++) {
				if (++position[d] < dims[d]) break;
				position[d] = 0;
			}
		}
	}

	private interface PairConsumer {

		void accept(int p, int q, Offset offset);
	}

	private static class Offset {

		private final int[] offset;
		private final double distance;

		private Offset(final int[] offset, final double distance) {
			this.offset = offset;
			this.distance = distance;
		}
	}
}
