/*-
 * #%L
 * LGPL-licensed image processing operations for SciJava Ops, translated from LGPL ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.lgpl.segment.watershed;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * Marker-controlled watershed by priority flooding on a flattened
 * n-dimensional pixel grid (first dimension varying fastest), shared by the
 * ops of this package.
 * <p>
 * The flooding follows F. Meyer, "Un algorithme optimal pour la ligne de
 * partage des eaux", 8e congres AFCET, 1991: starting from the markers, the
 * queued pixel of lowest priority is repeatedly given the label of the region
 * which queued it, and queues its unlabeled neighbors in turn. Each queue entry
 * carries its label and the marker pixel its flooding path started from, so
 * that the priority can include the compactness term of P. Neubert and P.
 * Protzel, "Compact watershed and preemptive SLIC: On improving trade-offs of
 * superpixel segmentation algorithms", ICPR 2014: the value of the pixel plus
 * the compactness times its Euclidean distance (in pixels) to that marker
 * pixel. Entries of equal priority are processed in the order they were
 * queued.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
final class Flooding {

	/** Internal label of watershed line pixels. */
	static final int LINE = -1;

	/** Internal label of pixels outside the mask. */
	private static final int OUTSIDE = -2;

	/** Number of dimensions. */
	private final int n;

	/** Size of each dimension. */
	private final int[] dims;

	/** Total number of pixels. */
	final int size;

	/** Neighbor offsets (symmetric, excluding the origin). */
	private final int[][] offsets;

	/** Linear index step of each neighbor offset. */
	private final int[] steps;

	/**
	 * Creates a grid with the dimensions of the given interval.
	 *
	 * @param interval the image whose dimensions to use
	 * @param connectivity the neighborhood defining which pixels are adjacent,
	 *          or null for full connectivity
	 */
	Flooding(final Dimensions interval, final Shape connectivity) {
		n = interval.numDimensions();
		if (Intervals.numElements(interval) > Integer.MAX_VALUE - 8) {
			throw new IllegalArgumentException(
				"Images with more than 2^31 pixels are not supported");
		}
		dims = new int[n];
		final var strides = new int[n];
		var s = 1;
		for (var d = 0; d < n; d++) {
			dims[d] = (int) interval.dimension(d);
			strides[d] = s;
			s *= dims[d];
		}
		size = s;
		offsets = neighborOffsets(connectivity == null ? new RectangleShape(1,
			false) : connectivity, n);
		steps = new int[offsets.length];
		for (var k = 0; k < offsets.length; k++) {
			for (var d = 0; d < n; d++)
				steps[k] += offsets[k][d] * strides[d];
		}
	}

	/**
	 * Floods an image from markers.
	 *
	 * @param values the priority of each pixel (typically its gray level)
	 * @param markers the internal marker label of each pixel, positive on
	 *          markers and 0 elsewhere
	 * @param mask whether each pixel may be labeled, or null for all pixels
	 * @param compactness the weight of the distance to the marker in the
	 *          priority; 0 for the classical watershed
	 * @param lines whether to separate the regions by watershed lines
	 * @return the label of each pixel: the marker label, {@link #LINE} on
	 *         watershed lines, or a non-positive value on pixels which were not
	 *         reached (outside the mask, or cut off by watershed lines)
	 */
	int[] flood(final double[] values, final int[] markers, final boolean[] mask,
		final double compactness, final boolean lines)
	{
		if (!(compactness >= 0)) {
			throw new IllegalArgumentException(
				"The compactness must be non-negative");
		}
		final var labels = new int[size];
		final var queue = new Queue();
		final var pos = new int[n];
		final var src = new int[n];
		for (var i = 0; i < size; i++) {
			if (mask != null && !mask[i]) labels[i] = OUTSIDE;
			else if (markers[i] > 0) labels[i] = markers[i];
		}
		for (var i = 0; i < size; i++) {
			if (labels[i] <= 0) continue;
			position(i, pos);
			for (var k = 0; k < steps.length; k++) {
				if (!contains(pos, k)) continue;
				final var j = i + steps[k];
				if (labels[j] != 0) continue;
				queue.push(priority(values, j, i, src, compactness), j, labels[i], i);
			}
		}
		while (queue.size > 0) {
			queue.pop();
			final var i = queue.index;
			if (labels[i] != 0) continue;
			final var label = queue.label;
			final var source = queue.source;
			position(i, pos);
			if (lines && touchesOtherLabel(labels, i, pos, label)) {
				labels[i] = LINE;
				continue;
			}
			labels[i] = label;
			for (var k = 0; k < steps.length; k++) {
				if (!contains(pos, k)) continue;
				final var j = i + steps[k];
				if (labels[j] != 0) continue;
				queue.push(priority(values, j, source, src, compactness), j, label,
					source);
			}
		}
		return labels;
	}

	/**
	 * Labels the connected components of a binary image.
	 *
	 * @param foreground whether each pixel belongs to the foreground
	 * @return the label of each pixel: 1, 2, ... on the components (numbered in
	 *         raster order of their first pixel), 0 on the background
	 */
	int[] components(final boolean[] foreground) {
		final var labels = new int[size];
		final var pos = new int[n];
		final var stack = new ArrayDeque<Integer>();
		var count = 0;
		for (var start = 0; start < size; start++) {
			if (!foreground[start] || labels[start] != 0) continue;
			labels[start] = ++count;
			stack.push(start);
			while (!stack.isEmpty()) {
				final int i = stack.pop();
				position(i, pos);
				for (var k = 0; k < steps.length; k++) {
					if (!contains(pos, k)) continue;
					final var j = i + steps[k];
					if (!foreground[j] || labels[j] != 0) continue;
					labels[j] = count;
					stack.push(j);
				}
			}
		}
		return labels;
	}

	/**
	 * Copies the values of an image into a new array, in flat iteration order.
	 *
	 * @param image the image to copy
	 * @return the values of the image
	 */
	double[] values(final RandomAccessibleInterval<? extends RealType<?>> image) {
		checkDimensions(image);
		final var values = new double[size];
		var i = 0;
		for (final var t : Views.flatIterable(image))
			values[i++] = t.getRealDouble();
		return values;
	}

	/**
	 * Copies the values of a binary image into a new array, in flat iteration
	 * order.
	 *
	 * @param image the image to copy, or null
	 * @return the flags of the image, or null if {@code image} is null
	 */
	boolean[] flags(
		final RandomAccessibleInterval<? extends BooleanType<?>> image)
	{
		if (image == null) return null;
		checkDimensions(image);
		final var flags = new boolean[size];
		var i = 0;
		for (final var t : Views.flatIterable(image))
			flags[i++] = t.get();
		return flags;
	}

	/**
	 * Copies the labels of a labeling into internal marker labels.
	 *
	 * @param labeling the labeling to copy, with at most one label per pixel
	 * @param ids filled with the label corresponding to each internal marker
	 *          label (the internal label {@code k} standing for
	 *          {@code ids.get(k - 1)})
	 * @return the internal marker label of each pixel, 1, 2, ... on labeled
	 *         pixels and 0 elsewhere
	 */
	int[] markers(final RandomAccessibleInterval<LabelingType<Integer>> labeling,
		final List<Integer> ids)
	{
		checkDimensions(labeling);
		final var markers = new int[size];
		final var internal = new HashMap<Integer, Integer>();
		var i = 0;
		for (final var t : Views.flatIterable(labeling)) {
			if (t.size() > 1) {
				throw new IllegalArgumentException(
					"Markers must have at most one label per pixel");
			}
			if (t.size() == 1) {
				markers[i] = internal.computeIfAbsent(t.iterator().next(), l -> {
					ids.add(l);
					return ids.size();
				});
			}
			i++;
		}
		return markers;
	}

	/**
	 * Writes labels into an empty labeling, in flat iteration order.
	 *
	 * @param labels the internal label of each pixel; only positive labels are
	 *          written
	 * @param ids the label corresponding to each internal label, or null to
	 *          write the internal labels themselves
	 * @param lineLabel the label to write on watershed line pixels, or null to
	 *          leave them unlabeled
	 * @param labeling the labeling to write into
	 * @throws IllegalArgumentException if {@code lineLabel} is also the label
	 *           of a region
	 */
	void write(final int[] labels, final List<Integer> ids,
		final Integer lineLabel, final ImgLabeling<Integer, IntType> labeling)
	{
		checkDimensions(labeling);
		if (lineLabel != null) {
			final boolean clash;
			if (ids != null) clash = ids.contains(lineLabel);
			else clash = lineLabel > 0 && Arrays.stream(labels).anyMatch(
				l -> l == lineLabel);
			if (clash) {
				throw new IllegalArgumentException("The watershed label " +
					lineLabel + " is also the label of a region");
			}
		}
		var i = 0;
		for (final var t : Views.flatIterable(labeling)) {
			final var label = labels[i++];
			if (label > 0) t.add(ids == null ? label : ids.get(label - 1));
			else if (label == LINE && lineLabel != null) t.add(lineLabel);
		}
	}

	private double priority(final double[] values, final int j, final int source,
		final int[] src, final double compactness)
	{
		if (compactness == 0) return values[j];
		position(source, src);
		var sum = 0.0;
		var rem = j;
		for (var d = 0; d < n; d++) {
			final double diff = rem % dims[d] - src[d];
			rem /= dims[d];
			sum += diff * diff;
		}
		return values[j] + compactness * Math.sqrt(sum);
	}

	private boolean touchesOtherLabel(final int[] labels, final int i,
		final int[] pos, final int label)
	{
		for (var k = 0; k < steps.length; k++) {
			if (!contains(pos, k)) continue;
			final var other = labels[i + steps[k]];
			if (other > 0 && other != label) return true;
		}
		return false;
	}

	private boolean contains(final int[] pos, final int k) {
		final var o = offsets[k];
		for (var d = 0; d < n; d++) {
			final var c = pos[d] + o[d];
			if (c < 0 || c >= dims[d]) return false;
		}
		return true;
	}

	private void position(int index, final int[] pos) {
		for (var d = 0; d < n; d++) {
			pos[d] = index % dims[d];
			index /= dims[d];
		}
	}

	private void checkDimensions(final RandomAccessibleInterval<?> image) {
		if (image.numDimensions() != n) {
			throw new IllegalArgumentException("Expected " + n +
				" dimensions but got " + image.numDimensions());
		}
		for (var d = 0; d < n; d++) {
			if (image.dimension(d) != dims[d]) {
				throw new IllegalArgumentException("Image dimensions " + Arrays
					.toString(image.dimensionsAsLongArray()) + " do not match " + Arrays
						.toString(dims));
			}
		}
	}

	/**
	 * Lists the offsets of a shape around the origin, made symmetric and
	 * without the origin itself.
	 */
	private static int[][] neighborOffsets(final Shape shape, final int n) {
		final var source = ConstantUtils.constantRandomAccessible(new DoubleType(),
			n);
		final var ra = shape.neighborhoodsRandomAccessible(source).randomAccess();
		ra.setPosition(new long[n]);
		final var set = new LinkedHashSet<List<Integer>>();
		final var c = ra.get().localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final var o = new ArrayList<Integer>(n);
			final var neg = new ArrayList<Integer>(n);
			var zero = true;
			for (var d = 0; d < n; d++) {
				final var v = c.getIntPosition(d);
				o.add(v);
				neg.add(-v);
				if (v != 0) zero = false;
			}
			if (zero) continue;
			set.add(o);
			set.add(neg);
		}
		final var result = new int[set.size()][];
		var k = 0;
		for (final var o : set) {
			result[k++] = o.stream().mapToInt(Integer::intValue).toArray();
		}
		return result;
	}

	/**
	 * A binary min-heap of flooding entries, ordered by priority and then by
	 * insertion order. {@link #pop()} stores the removed entry in the
	 * {@code index}, {@code label} and {@code source} fields.
	 */
	private static final class Queue {

		private double[] priorities = new double[1024];
		private long[] order = new long[1024];
		private int[] indices = new int[1024];
		private int[] labels = new int[1024];
		private int[] sources = new int[1024];
		private long counter;
		int size;
		int index;
		int label;
		int source;

		void push(final double priority, final int i, final int l, final int s) {
			if (size == priorities.length) {
				final var capacity = (int) Math.min(Integer.MAX_VALUE - 8, 2L * size);
				if (capacity <= size) {
					throw new IllegalStateException("Watershed queue overflow");
				}
				priorities = Arrays.copyOf(priorities, capacity);
				order = Arrays.copyOf(order, capacity);
				indices = Arrays.copyOf(indices, capacity);
				labels = Arrays.copyOf(labels, capacity);
				sources = Arrays.copyOf(sources, capacity);
			}
			final var o = counter++;
			var c = size++;
			while (c > 0) {
				final var p = (c - 1) >>> 1;
				if (!before(priority, o, priorities[p], order[p])) break;
				move(p, c);
				c = p;
			}
			set(c, priority, o, i, l, s);
		}

		void pop() {
			index = indices[0];
			label = labels[0];
			source = sources[0];
			final var last = --size;
			if (last == 0) return;
			final var priority = priorities[last];
			final var o = order[last];
			final var i = indices[last];
			final var l = labels[last];
			final var s = sources[last];
			var c = 0;
			while (true) {
				var child = 2 * c + 1;
				if (child >= last) break;
				if (child + 1 < last && before(priorities[child + 1], order[child + 1],
					priorities[child], order[child])) child++;
				if (!before(priorities[child], order[child], priority, o)) break;
				move(child, c);
				c = child;
			}
			set(c, priority, o, i, l, s);
		}

		private static boolean before(final double p, final long o, final double q,
			final long r)
		{
			return p < q || (p == q && o < r);
		}

		private void set(final int c, final double priority, final long o,
			final int i, final int l, final int s)
		{
			priorities[c] = priority;
			order[c] = o;
			indices[c] = i;
			labels[c] = l;
			sources[c] = s;
		}

		private void move(final int from, final int to) {
			set(to, priorities[from], order[from], indices[from], labels[from],
				sources[from]);
		}
	}
}
