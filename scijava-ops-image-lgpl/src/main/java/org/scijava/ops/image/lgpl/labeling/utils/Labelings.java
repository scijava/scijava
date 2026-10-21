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

package org.scijava.ops.image.lgpl.labeling.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

/**
 * Internal helpers shared by the label-map utility ops.
 * <p>
 * Most of the ops only rewrite the label sets of an {@link ImgLabeling}: they
 * compute the new label set of each index of the labeling's mapping once and
 * then remap the index image, which is linear in the number of pixels.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
final class Labelings {

	private Labelings() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Interns label sets, assigning each distinct set a consecutive index, with
	 * the empty set at index 0.
	 */
	static final class SetTable<M> {

		private final List<Set<M>> sets = new ArrayList<>();
		private final Map<Set<M>, Integer> indices = new HashMap<>();

		SetTable() {
			indexOf(Collections.emptySet());
		}

		int indexOf(final Set<M> set) {
			final var existing = indices.get(set);
			if (existing != null) return existing;
			final Set<M> copy = Collections.unmodifiableSet(new HashSet<>(set));
			sets.add(copy);
			indices.put(copy, sets.size() - 1);
			return sets.size() - 1;
		}

		List<Set<M>> sets() {
			return sets;
		}
	}

	/** Creates a zeroed image with the type and interval of the given one. */
	static <I extends IntegerType<I>> RandomAccessibleInterval<I> createIndexImg(
		final RandomAccessibleInterval<I> like)
	{
		final var type = like.getType().createVariable();
		final Img<I> img = Util.getSuitableImgFactory(like, type).create(like);
		return Views.isZeroMin(like) ? img : Views.translate(img, like
			.minAsLongArray());
	}

	/**
	 * Creates a new labeling by applying the given function to each label set
	 * of the given labeling.
	 */
	static <L, M, I extends IntegerType<I>> ImgLabeling<M, I> mapLabelSets(
		final ImgLabeling<L, I> labeling, final Function<Set<L>, Set<M>> f)
	{
		final var mapping = labeling.getMapping();
		final var table = new SetTable<M>();
		final var lut = new int[mapping.numSets()];
		for (var i = 0; i < lut.length; i++)
			lut[i] = table.indexOf(f.apply(mapping.labelsAtIndex(i)));
		final var indexImg = createIndexImg(labeling.getIndexImg());
		LoopBuilder.setImages(labeling.getIndexImg(), indexImg).forEachPixel((in,
			out) -> out.setInteger(lut[in.getInteger()]));
		return ImgLabeling.fromImageAndLabelSets(indexImg, table.sets());
	}

	/**
	 * Creates a new labeling containing only the labels accepted by the given
	 * predicate, which is evaluated once per label.
	 */
	static <L, I extends IntegerType<I>> ImgLabeling<L, I> retain(
		final ImgLabeling<L, I> labeling, final Predicate<L> keep)
	{
		final var decisions = new HashMap<L, Boolean>();
		return mapLabelSets(labeling, set -> {
			final var result = new HashSet<L>();
			for (final var label : set) {
				if (decisions.computeIfAbsent(label, keep::test)) result.add(label);
			}
			return result;
		});
	}

	/** Counts the pixels of each index of the labeling's index image. */
	static long[] indexCounts(final ImgLabeling<?, ?> labeling) {
		final var counts = new long[labeling.getMapping().numSets()];
		for (final var index : labeling.getIndexImg())
			counts[index.getInteger()]++;
		return counts;
	}

	/**
	 * Computes the pixel count of each label present in the labeling, in
	 * order of the labeling's mapping. A pixel with several labels counts
	 * towards each of them.
	 */
	static <L> Map<L, Long> sizes(final ImgLabeling<L, ?> labeling) {
		return sumPerLabel(labeling, indexCounts(labeling));
	}

	/** Sums per-index values into per-label values, skipping zero entries. */
	static <L> Map<L, Long> sumPerLabel(final ImgLabeling<L, ?> labeling,
		final long[] perIndex)
	{
		final var mapping = labeling.getMapping();
		final var result = new LinkedHashMap<L, Long>();
		for (var i = 0; i < perIndex.length; i++) {
			if (perIndex[i] == 0) continue;
			for (final var label : mapping.labelsAtIndex(i))
				result.merge(label, perIndex[i], Long::sum);
		}
		return result;
	}

	/**
	 * Orders labels by their natural ordering if they are mutually comparable,
	 * otherwise keeps the given order.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static <L> List<L> order(final Collection<L> labels) {
		final var list = new ArrayList<L>(new LinkedHashSet<>(labels));
		try {
			Collections.sort((List) list);
		}
		catch (final ClassCastException exc) {
			// Note: Labels without a natural ordering keep the given order.
			return new ArrayList<>(new LinkedHashSet<>(labels));
		}
		return list;
	}

	/**
	 * Gets the neighbor offsets of a structuring element in {@code n}
	 * dimensions: the {@code 2n} face neighbors for
	 * {@link StructuringElement#FOUR_CONNECTED}, or the {@code 3^n - 1} face,
	 * edge and corner neighbors for {@link StructuringElement#EIGHT_CONNECTED}.
	 * If {@code forwardOnly} is set, only the offsets whose first nonzero
	 * coordinate is positive are returned, so that each pair of neighboring
	 * pixels is visited once.
	 */
	static List<long[]> offsets(final int n, final StructuringElement se,
		final boolean forwardOnly)
	{
		final var offsets = new ArrayList<long[]>();
		if (se == StructuringElement.FOUR_CONNECTED) {
			for (var d = 0; d < n; d++) {
				for (final var step : new long[] { 1, -1 }) {
					if (forwardOnly && step < 0) continue;
					final var offset = new long[n];
					offset[d] = step;
					offsets.add(offset);
				}
			}
			return offsets;
		}
		var total = 1;
		for (var d = 0; d < n; d++)
			total *= 3;
		for (var k = 0; k < total; k++) {
			final var offset = new long[n];
			var rest = k;
			for (var d = 0; d < n; d++) {
				offset[d] = rest % 3 - 1;
				rest /= 3;
			}
			var first = 0L;
			for (var d = 0; d < n && first == 0; d++)
				first = offset[d];
			if (first == 0 || forwardOnly && first < 0) continue;
			offsets.add(offset);
		}
		return offsets;
	}

	/**
	 * Moves {@code target} to {@code position + offset}, returning false if the
	 * result lies outside the interval.
	 */
	static boolean shift(final long[] position, final long[] offset,
		final Interval interval, final long[] target)
	{
		for (var d = 0; d < position.length; d++) {
			target[d] = position[d] + offset[d];
			if (target[d] < interval.min(d) || target[d] > interval.max(d))
				return false;
		}
		return true;
	}

	/** Whether the position lies on the border of the interval. */
	static boolean onBorder(final long[] position, final Interval interval) {
		for (var d = 0; d < position.length; d++) {
			if (position[d] == interval.min(d) || position[d] == interval.max(d))
				return true;
		}
		return false;
	}
}
