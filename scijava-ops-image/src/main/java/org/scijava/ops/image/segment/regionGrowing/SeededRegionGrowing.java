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

package org.scijava.ops.image.segment.regionGrowing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Seeded region growing (Adams &amp; Bischof 1994) in <i>n</i> dimensions.
 * <p>
 * Starting from labeled seed regions, the regions grow one pixel at a time.
 * All unlabeled pixels adjacent to a region are kept in a priority queue,
 * ordered by {@code δ = |g(x) - mean(R)|}, the difference between the pixel
 * intensity and the mean intensity of the adjacent region {@code R} at the
 * time the pixel was queued. The pixel with the smallest {@code δ} is
 * labeled next and the mean of its region is updated, so that the regions
 * grow in order of increasing dissimilarity until every pixel reachable from
 * a seed is labeled. Ties are broken first come, first served.
 * </p>
 * <p>
 * Where a pixel adjoins several regions, it joins the one whose current mean
 * is closest to its intensity (as in the improved algorithm of Mehnert &amp;
 * Jackway 1997), rather than being flagged as a boundary pixel as in the
 * original algorithm, so that the result is a complete partition.
 * </p>
 * <p>
 * Images containing NaN are rejected with an
 * {@link IllegalArgumentException}, since NaN has no order, and so are seeds
 * with several labels at one pixel.
 * </p>
 * <p>
 * Implemented from the papers.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @param <L> label type
 * @see <a href="https://doi.org/10.1109/34.295913">R. Adams and L. Bischof,
 *      "Seeded region growing", IEEE Trans. Pattern Anal. Mach. Intell.
 *      16(6):641-647, 1994</a>
 * @see <a href="https://doi.org/10.1016/S0167-8655(97)00131-1">A. Mehnert and
 *      P. Jackway, "An improved seeded region growing algorithm", Pattern
 *      Recognit. Lett. 18(10):1065-1071, 1997</a>
 * @implNote op names='segment.seededRegionGrowing'
 */
public class SeededRegionGrowing<T extends RealType<T>, L> implements
	Functions.Arity3<RandomAccessibleInterval<T>, RandomAccessibleInterval<LabelingType<L>>, StructuringElement, ImgLabeling<L, IntType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, IntType, RandomAccessibleInterval<IntType>> createOp;

	/**
	 * Grows the seed regions.
	 *
	 * @param input the intensity image, without NaN
	 * @param seeds the seed labeling, with the same dimensions as the image;
	 *          unlabeled pixels are to be assigned, and no pixel may have
	 *          several labels
	 * @param connectivity the pixel adjacency, FOUR_CONNECTED (the default) or
	 *          EIGHT_CONNECTED, i.e. sharing a face or touching in any way
	 * @return the grown labeling
	 */
	@Override
	public ImgLabeling<L, IntType> apply(final RandomAccessibleInterval<T> input,
		final RandomAccessibleInterval<LabelingType<L>> seeds,
		@Nullable final StructuringElement connectivity)
	{
		if (!Intervals.equalDimensions(input, seeds)) {
			throw new IllegalArgumentException(
				"Image and seeds must have the same dimensions");
		}
		final var n = input.numDimensions();
		final var size = Intervals.numElements(input);
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image is too large: " + size +
				" pixels");
		}
		final var count = (int) size;
		final var dims = input.dimensionsAsLongArray();
		final var full = connectivity == StructuringElement.EIGHT_CONNECTED;

		final var values = new double[count];
		var i = 0;
		for (final var t : Views.flatIterable(input)) {
			final var v = t.getRealDouble();
			if (Double.isNaN(v)) {
				throw new IllegalArgumentException("NaN values are not supported");
			}
			values[i++] = v;
		}

		// Map seed labels to region indices.
		final var region = new int[count];
		final var labels = new ArrayList<L>();
		final var indices = new HashMap<L, Integer>();
		i = 0;
		for (final var set : Views.flatIterable(seeds)) {
			if (set.isEmpty()) region[i] = -1;
			else if (set.size() > 1) {
				throw new IllegalArgumentException(
					"Seeds must have at most one label per pixel");
			}
			else {
				final var label = set.iterator().next();
				region[i] = indices.computeIfAbsent(label, l -> {
					labels.add(l);
					return labels.size() - 1;
				});
			}
			i++;
		}
		final var sums = new double[labels.size()];
		final var sizes = new long[labels.size()];
		for (var p = 0; p < count; p++) {
			if (region[p] >= 0) {
				sums[region[p]] += values[p];
				sizes[region[p]]++;
			}
		}

		final var offsets = neighborOffsets(n, full);
		final var coords = new int[n];
		final var queue = new PriorityQueue<Candidate>();
		var order = 0L;
		for (var p = 0; p < count; p++) {
			if (region[p] < 0) continue;
			position(p, dims, coords);
			for (final var o : offsets) {
				final var q = neighbor(p, coords, o, dims);
				if (q >= 0 && region[q] < 0) {
					queue.add(new Candidate(q, Math.abs(values[q] - sums[region[p]] /
						sizes[region[p]]), order++));
				}
			}
		}

		while (!queue.isEmpty()) {
			final var p = queue.poll().index;
			if (region[p] >= 0) continue;
			position(p, dims, coords);
			var best = -1;
			var bestDelta = Double.POSITIVE_INFINITY;
			for (final var o : offsets) {
				final var q = neighbor(p, coords, o, dims);
				if (q < 0 || region[q] < 0) continue;
				final var r = region[q];
				final var delta = Math.abs(values[p] - sums[r] / sizes[r]);
				if (delta < bestDelta) {
					bestDelta = delta;
					best = r;
				}
			}
			region[p] = best;
			sums[best] += values[p];
			sizes[best]++;
			final var mean = sums[best] / sizes[best];
			for (final var o : offsets) {
				final var q = neighbor(p, coords, o, dims);
				if (q >= 0 && region[q] < 0) {
					queue.add(new Candidate(q, Math.abs(values[q] - mean), order++));
				}
			}
		}

		final var output = new ImgLabeling<L, IntType>(createOp.apply(input,
			new IntType()));
		i = 0;
		for (final var set : Views.flatIterable(output)) {
			if (region[i] >= 0) set.add(labels.get(region[i]));
			i++;
		}
		return output;
	}

	/** Lists the neighbor offsets for the given connectivity. */
	private static List<int[]> neighborOffsets(final int n, final boolean full) {
		final var offsets = new ArrayList<int[]>();
		if (!full) {
			for (var d = 0; d < n; d++) {
				for (final var s : new int[] { -1, 1 }) {
					final var o = new int[n];
					o[d] = s;
					offsets.add(o);
				}
			}
			return offsets;
		}
		final var total = (int) Math.pow(3, n);
		for (var c = 0; c < total; c++) {
			final var o = new int[n];
			var rest = c;
			var zero = true;
			for (var d = 0; d < n; d++) {
				o[d] = rest % 3 - 1;
				rest /= 3;
				if (o[d] != 0) zero = false;
			}
			if (!zero) offsets.add(o);
		}
		return offsets;
	}

	private static void position(int p, final long[] dims, final int[] coords) {
		for (var d = 0; d < dims.length; d++) {
			coords[d] = (int) (p % dims[d]);
			p /= (int) dims[d];
		}
	}

	/** Gets the flat index of a neighbor, or -1 if it is outside the image. */
	private static int neighbor(final int p, final int[] coords, final int[] o,
		final long[] dims)
	{
		var q = 0;
		var stride = 1;
		for (var d = 0; d < dims.length; d++) {
			final var c = coords[d] + o[d];
			if (c < 0 || c >= dims[d]) return -1;
			q += c * stride;
			stride *= (int) dims[d];
		}
		return q;
	}

	/** A queued pixel with its priority. */
	private static final class Candidate implements Comparable<Candidate> {

		private final int index;
		private final double delta;
		private final long order;

		private Candidate(final int index, final double delta, final long order) {
			this.index = index;
			this.delta = delta;
			this.order = order;
		}

		@Override
		public int compareTo(final Candidate other) {
			final var cmp = Double.compare(delta, other.delta);
			return cmp != 0 ? cmp : Long.compare(order, other.order);
		}
	}
}
