/* #%L
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

package org.scijava.ops.image.labeling.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.ops.spi.Nullable;

/**
 * Ops rewriting the labels or label regions of an {@link ImgLabeling}.
 * <p>
 * Each op returns a new labeling (or image); the input is not modified.
 * Labelings may have overlapping labels, i.e. pixels with several labels; how
 * each op treats them is described in its documentation.
 * </p>
 * <p>
 * These are clean-room implementations of common label-map utilities, as
 * offered by the {@code LabelImages} class and label plugins of MorphoLibJ
 * (D. Legland, I. Arganda-Carreras and P. Andrey, "MorphoLibJ: integrated
 * library and plugins for mathematical morphology with ImageJ",
 * <i>Bioinformatics</i> 32(22): 3532&ndash;3534, 2016), generalized to
 * n-dimensional labelings with arbitrary, possibly overlapping labels.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class LabelEditing {

	private LabelEditing() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Replaces the labels by consecutive integers. If the labels are mutually
	 * comparable (such as numbers or strings), they are numbered in their
	 * natural order; otherwise they are numbered in order of their first
	 * occurrence in a raster scan of the labeling. Overlaps are preserved.
	 *
	 * @param labeling the labeled objects
	 * @param start the first label (default 1)
	 * @return a new labeling with the labels {@code start, start + 1, ...}
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.relabel', type=Function
	 */
	public static <L, I extends IntegerType<I>> ImgLabeling<Integer, I> relabel(
		final ImgLabeling<L, I> labeling, @Nullable Integer start)
	{
		final int first = start == null ? 1 : start;
		final var mapping = labeling.getMapping();
		final var seen = new boolean[mapping.numSets()];
		final var present = new LinkedHashSet<L>();
		for (final var index : Views.flatIterable(labeling.getIndexImg())) {
			final var i = index.getInteger();
			if (seen[i]) continue;
			seen[i] = true;
			present.addAll(mapping.labelsAtIndex(i));
		}
		final var newLabels = new HashMap<L, Integer>();
		var next = first;
		for (final var label : Labelings.order(present))
			newLabels.put(label, next++);
		return Labelings.mapLabelSets(labeling, set -> {
			final var result = new HashSet<Integer>();
			for (final var label : set) {
				final var newLabel = newLabels.get(label);
				if (newLabel != null) result.add(newLabel);
			}
			return result;
		});
	}

	/**
	 * Merges several labels into one: every pixel carrying any of the given
	 * labels carries the target label instead. Other labels of those pixels
	 * are kept.
	 *
	 * @param labeling the labeled objects
	 * @param labels the labels to merge
	 * @param target the label of the merged region (default: the smallest of
	 *          the given labels if they are mutually comparable, otherwise the
	 *          first one)
	 * @return a new labeling with the labels merged
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.mergeLabels', type=Function
	 */
	public static <L, I extends IntegerType<I>> ImgLabeling<L, I> mergeLabels(
		final ImgLabeling<L, I> labeling, final Collection<L> labels,
		@Nullable L target)
	{
		if (labels.isEmpty()) return Labelings.mapLabelSets(labeling, s -> s);
		final L merged = target == null ? Labelings.order(labels).get(0) : target;
		final var toMerge = new HashSet<>(labels);
		return Labelings.mapLabelSets(labeling, set -> {
			final var result = new HashSet<L>();
			var hit = false;
			for (final var label : set) {
				if (toMerge.contains(label)) hit = true;
				else result.add(label);
			}
			if (hit) result.add(merged);
			return result;
		});
	}

	/**
	 * Fills the holes of the labeled regions. A hole is a connected component
	 * of unlabeled pixels that does not touch the border of the labeling
	 * interval. It is filled with the labels shared by all labeled pixels
	 * adjacent to it, i.e. the labels enclosing it; a hole between several
	 * regions that do not share a label is left unlabeled.
	 * <p>
	 * The connectivity applies to the background: it defines the connected
	 * components of unlabeled pixels and their adjacency to labeled pixels, as
	 * the connectivity of {@code morphology.fillHoles} does. With the default
	 * full connectivity, a hole leaks out through a diagonal gap in its
	 * enclosing region and is not filled. For regions that are only connected
	 * diagonally (such as a thin outline drawn with diagonal steps), use face
	 * connectivity, the dual of their full connectivity.
	 * </p>
	 *
	 * @param labeling the labeled objects
	 * @param connectivity the connectivity of the unlabeled components and of
	 *          their adjacency to labeled pixels, such as a
	 *          {@code DiamondShape} of radius 1 for face connectivity
	 *          (4-connectivity in 2D, 6-connectivity in 3D) or a
	 *          {@code RectangleShape} of span 1 for full connectivity
	 *          (8-connectivity in 2D, 26-connectivity in 3D). Defaults to full
	 *          connectivity.
	 * @return a new labeling with the holes filled
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.fillHoles', type=Function
	 */
	public static <L, I extends IntegerType<I>> ImgLabeling<L, I> fillHoles(
		final ImgLabeling<L, I> labeling, @Nullable Shape connectivity)
	{
		final var mapping = labeling.getMapping();
		final var indexImg = labeling.getIndexImg();
		final var n = labeling.numDimensions();
		final var empty = new boolean[mapping.numSets()];
		for (var i = 0; i < empty.length; i++)
			empty[i] = mapping.labelsAtIndex(i).isEmpty();

		final var offsets = Labelings.offsets(n, connectivity, false);
		final var dims = indexImg.dimensionsAsLongArray();
		final var size = Intervals.numElements(indexImg);
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Labeling is too large: " + size +
				" pixels");
		}
		final var background = new boolean[(int) size];
		var p = 0;
		for (final var t : Views.flatIterable(indexImg))
			background[p++] = empty[t.getInteger()];
		final var ids = new int[(int) size];
		final var count = labelComponents(background, dims, offsets, ids);
		final RandomAccessibleInterval<IntType> components = Views.translate(
			ArrayImgs.ints(ids, dims), indexImg.minAsLongArray());

		// The labels enclosing each component, or null if none are known yet.
		final var enclosing = new ArrayList<Set<L>>();
		final var touchesBorder = new boolean[count + 1];
		for (var c = 0; c <= count; c++)
			enclosing.add(null);
		final var ra = indexImg.randomAccess();
		final var position = new long[n];
		final var neighbor = new long[n];
		final var cursor = Views.flatIterable(components).localizingCursor();
		while (cursor.hasNext()) {
			final var c = cursor.next().get();
			if (c == 0 || touchesBorder[c]) continue;
			cursor.localize(position);
			if (Labelings.onBorder(position, indexImg)) {
				touchesBorder[c] = true;
				continue;
			}
			for (final var offset : offsets) {
				if (!Labelings.shift(position, offset, indexImg, neighbor)) continue;
				final var labels = mapping.labelsAtIndex(ra.setPositionAndGet(neighbor)
					.getInteger());
				if (labels.isEmpty()) continue;
				final var known = enclosing.get(c);
				if (known == null) enclosing.set(c, new HashSet<>(labels));
				else known.retainAll(labels);
			}
		}

		final var table = new Labelings.SetTable<L>();
		final var lut = new int[mapping.numSets()];
		for (var i = 0; i < lut.length; i++)
			lut[i] = table.indexOf(mapping.labelsAtIndex(i));
		final var fill = new int[count + 1];
		for (var c = 1; c <= count; c++) {
			final var labels = enclosing.get(c);
			if (!touchesBorder[c] && labels != null) fill[c] = table.indexOf(labels);
		}
		final var output = Labelings.createIndexImg(indexImg);
		LoopBuilder.setImages(indexImg, components, output).forEachPixel((in, c,
			out) -> {
			final var f = fill[c.get()];
			out.setInteger(f != 0 ? f : lut[in.getInteger()]);
		});
		return ImgLabeling.fromImageAndLabelSets(output, table.sets());
	}

	/**
	 * Labels the connected components of the true pixels of a flat mask, with
	 * dimension 0 varying fastest, numbering them from 1 in raster order.
	 *
	 * @return the number of components
	 */
	private static int labelComponents(final boolean[] mask, final long[] dims,
		final List<long[]> offsets, final int[] labels)
	{
		final var n = dims.length;
		final var strides = new long[n];
		var stride = 1L;
		for (var d = 0; d < n; d++) {
			strides[d] = stride;
			stride *= dims[d];
		}
		final var position = new long[n];
		final var queue = new int[mask.length];
		var count = 0;
		for (var start = 0; start < mask.length; start++) {
			if (!mask[start] || labels[start] != 0) continue;
			labels[start] = ++count;
			var head = 0;
			var tail = 0;
			queue[tail++] = start;
			while (head < tail) {
				final var p = queue[head++];
				var rest = (long) p;
				for (var d = 0; d < n; d++) {
					position[d] = rest % dims[d];
					rest /= dims[d];
				}
				for (final var offset : offsets) {
					var q = 0L;
					var inside = true;
					for (var d = 0; d < n; d++) {
						final var c = position[d] + offset[d];
						if (c < 0 || c >= dims[d]) {
							inside = false;
							break;
						}
						q += c * strides[d];
					}
					if (!inside || !mask[(int) q] || labels[(int) q] != 0) continue;
					labels[(int) q] = count;
					queue[tail++] = (int) q;
				}
			}
		}
		return count;
	}

	/**
	 * Crops a binary mask of one label to its bounding box, optionally padded
	 * by a border. The mask keeps the coordinates of the labeling, i.e. its
	 * minimum is the minimum of the bounding box minus the border; use
	 * {@code Views.zeroMin} for a zero-based image. Pixels of the padding that
	 * lie outside the labeling are false.
	 *
	 * @param labeling the labeled objects
	 * @param label the label to crop
	 * @param border the number of pixels to pad on each side (default 0)
	 * @return the mask of the label within its (padded) bounding box
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.cropLabel', type=Function
	 */
	public static <L, I extends IntegerType<I>> RandomAccessibleInterval<BitType>
		cropLabel(final ImgLabeling<L, I> labeling, final L label,
			@Nullable Long border)
	{
		final long pad = border == null ? 0 : border;
		if (pad < 0) throw new IllegalArgumentException("Negative border: " +
			pad);
		final var mapping = labeling.getMapping();
		final var hasLabel = new boolean[mapping.numSets()];
		for (var i = 0; i < hasLabel.length; i++)
			hasLabel[i] = mapping.labelsAtIndex(i).contains(label);

		final var n = labeling.numDimensions();
		final var min = new long[n];
		final var max = new long[n];
		Arrays.fill(min, Long.MAX_VALUE);
		Arrays.fill(max, Long.MIN_VALUE);
		final var cursor = labeling.getIndexImg().localizingCursor();
		var found = false;
		while (cursor.hasNext()) {
			if (!hasLabel[cursor.next().getInteger()]) continue;
			found = true;
			for (var d = 0; d < n; d++) {
				final var p = cursor.getLongPosition(d);
				min[d] = Math.min(min[d], p);
				max[d] = Math.max(max[d], p);
			}
		}
		if (!found) throw new IllegalArgumentException("No such label: " + label);
		for (var d = 0; d < n; d++) {
			min[d] -= pad;
			max[d] += pad;
		}
		final var box = new FinalInterval(min, max);
		final RandomAccessibleInterval<BitType> mask = Views.translate(ArrayImgs
			.bits(box.dimensionsAsLongArray()), min);
		final var overlap = Intervals.intersect(box, labeling);
		LoopBuilder.setImages(Views.interval(labeling.getIndexImg(), overlap),
			Views.interval(mask, overlap)).forEachPixel((index, m) -> m.set(
				hasLabel[index.getInteger()]));
		return mask;
	}
}
