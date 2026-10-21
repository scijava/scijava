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

package org.scijava.ops.image.labeling.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Predicate;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

import org.scijava.ops.spi.Nullable;

/**
 * Ops selecting a subset of the labels of an {@link ImgLabeling}.
 * <p>
 * Each op returns a new labeling over a new index image of the same type and
 * interval, containing only the selected labels; the input is not modified.
 * Labelings may have overlapping labels: each label is selected or rejected
 * on its own, and a pixel keeps exactly those of its labels that are
 * selected. Sizes are pixel counts, and a pixel with several labels counts
 * towards each of them.
 * </p>
 * <p>
 * These are clean-room implementations of common label-map utilities, as
 * offered by the {@code LabelImages} class of MorphoLibJ (D. Legland, I.
 * Arganda-Carreras and P. Andrey, "MorphoLibJ: integrated library and plugins
 * for mathematical morphology with ImageJ", <i>Bioinformatics</i> 32(22):
 * 3532&ndash;3534, 2016), the edge exclusion and size filter of the 3D
 * Objects Counter (S. Bolte and F. P. Cordeli&egrave;res, "A guided tour into
 * subcellular colocalization analysis in light microscopy", <i>Journal of
 * Microscopy</i> 224(3): 213&ndash;232, 2006), and the Binary Feature
 * Extractor of the BioVoxxel Toolbox (J. Brocher), generalized to
 * n-dimensional labelings with arbitrary, possibly overlapping labels.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class LabelFiltering {

	private LabelFiltering() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Keeps the labels accepted by a predicate.
	 *
	 * @param labeling the labeled objects
	 * @param predicate the test deciding which labels to keep
	 * @return a new labeling with only the accepted labels
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.filter', type=Function
	 */
	public static <L, I extends IntegerType<I>> ImgLabeling<L, I> filter(
		final ImgLabeling<L, I> labeling, final Predicate<L> predicate)
	{
		return Labelings.retain(labeling, predicate);
	}

	/**
	 * Keeps the labels whose value (such as a shape descriptor, size or mean
	 * intensity) lies within a range. Labels without a value, or with a NaN
	 * value, are removed.
	 *
	 * @param labeling the labeled objects
	 * @param values the value of each label
	 * @param min the minimum value (inclusive) to keep (default: no minimum)
	 * @param max the maximum value (inclusive) to keep (default: no maximum)
	 * @return a new labeling with only the labels in range
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @param <N> value type
	 * @implNote op names='labeling.filter', type=Function
	 */
	public static <L, I extends IntegerType<I>, N extends Number>
		ImgLabeling<L, I> filterByValue(final ImgLabeling<L, I> labeling,
			final Map<L, N> values, @Nullable Double min, @Nullable Double max)
	{
		final double lo = min == null ? Double.NEGATIVE_INFINITY : min;
		final double hi = max == null ? Double.POSITIVE_INFINITY : max;
		return Labelings.retain(labeling, label -> {
			final var value = values.get(label);
			if (value == null) return false;
			final var v = value.doubleValue();
			return v >= lo && v <= hi;
		});
	}

	/**
	 * Keeps the labels whose pixel count lies within a range. Use a minimum
	 * only for a size (area or volume) opening.
	 *
	 * @param labeling the labeled objects
	 * @param minSize the minimum pixel count (inclusive) to keep (default: no
	 *          minimum)
	 * @param maxSize the maximum pixel count (inclusive) to keep (default: no
	 *          maximum)
	 * @return a new labeling with only the labels in the size range
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.filterBySize', type=Function
	 */
	public static <L, I extends IntegerType<I>> ImgLabeling<L, I> filterBySize(
		final ImgLabeling<L, I> labeling, @Nullable Long minSize,
		@Nullable Long maxSize)
	{
		final long lo = minSize == null ? 0 : minSize;
		final long hi = maxSize == null ? Long.MAX_VALUE : maxSize;
		final var sizes = Labelings.sizes(labeling);
		return Labelings.retain(labeling, label -> {
			final var size = sizes.get(label);
			return size != null && size >= lo && size <= hi;
		});
	}

	/**
	 * Keeps only the label with the largest pixel count. If several labels
	 * share the largest pixel count, all of them are kept.
	 *
	 * @param labeling the labeled objects
	 * @return a new labeling with only the largest label
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.keepLargest', type=Function
	 */
	public static <L, I extends IntegerType<I>> ImgLabeling<L, I> keepLargest(
		final ImgLabeling<L, I> labeling)
	{
		final var sizes = Labelings.sizes(labeling);
		final var largest = largestSize(sizes);
		return Labelings.retain(labeling, label -> sizes.getOrDefault(label,
			-1L) == largest);
	}

	/**
	 * Removes the label with the largest pixel count, such as a background
	 * region. If several labels share the largest pixel count, all of them are
	 * removed.
	 *
	 * @param labeling the labeled objects
	 * @return a new labeling without the largest label
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.removeLargest', type=Function
	 */
	public static <L, I extends IntegerType<I>> ImgLabeling<L, I> removeLargest(
		final ImgLabeling<L, I> labeling)
	{
		final var sizes = Labelings.sizes(labeling);
		final var largest = largestSize(sizes);
		return Labelings.retain(labeling, label -> sizes.getOrDefault(label,
			-1L) != largest);
	}

	/**
	 * Keeps the given labels and removes all others.
	 *
	 * @param labeling the labeled objects
	 * @param labels the labels to keep
	 * @return a new labeling with only the given labels
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.keepLabels', type=Function
	 */
	public static <L, I extends IntegerType<I>> ImgLabeling<L, I> keepLabels(
		final ImgLabeling<L, I> labeling, final Collection<L> labels)
	{
		final var set = new HashSet<>(labels);
		return Labelings.retain(labeling, set::contains);
	}

	/**
	 * Removes the given labels and keeps all others.
	 *
	 * @param labeling the labeled objects
	 * @param labels the labels to remove
	 * @return a new labeling without the given labels
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.removeLabels', type=Function
	 */
	public static <L, I extends IntegerType<I>> ImgLabeling<L, I> removeLabels(
		final ImgLabeling<L, I> labeling, final Collection<L> labels)
	{
		final var set = new HashSet<>(labels);
		return Labelings.retain(labeling, label -> !set.contains(label));
	}

	/**
	 * Removes the labels touching the border of the labeling interval, such as
	 * objects only partially inside the field of view.
	 *
	 * @param labeling the labeled objects
	 * @param borders which borders to consider: either one flag per dimension
	 *          (both the minimum and maximum border of that dimension), or two
	 *          flags per dimension in the order {@code min0, max0, min1, max1,
	 *          ...} (default: all borders)
	 * @return a new labeling without the labels touching the chosen borders
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.removeBorderLabels', type=Function
	 */
	public static <L, I extends IntegerType<I>> ImgLabeling<L, I>
		removeBorderLabels(final ImgLabeling<L, I> labeling,
			@Nullable boolean[] borders)
	{
		final var n = labeling.numDimensions();
		final var sides = new boolean[2 * n];
		if (borders == null) Arrays.fill(sides, true);
		else if (borders.length == n) {
			for (var d = 0; d < n; d++)
				sides[2 * d] = sides[2 * d + 1] = borders[d];
		}
		else if (borders.length == 2 * n) System.arraycopy(borders, 0, sides, 0,
			2 * n);
		else throw new IllegalArgumentException("Expected " + n + " or " + 2 * n +
			" border flags, but got " + borders.length);

		final var indexImg = labeling.getIndexImg();
		final var touching = new long[labeling.getMapping().numSets()];
		for (var d = 0; d < n; d++) {
			for (var side = 0; side < 2; side++) {
				if (!sides[2 * d + side]) continue;
				final var pos = side == 0 ? indexImg.min(d) : indexImg.max(d);
				final var face = n == 1 ? Views.interval(indexImg, new long[] { pos },
					new long[] { pos }) : Views.hyperSlice(indexImg, d, pos);
				for (final var index : Views.iterable(face))
					touching[index.getInteger()] = 1;
			}
		}
		final var border = Labelings.sumPerLabel(labeling, touching).keySet();
		return Labelings.retain(labeling, label -> !border.contains(label));
	}

	/**
	 * Keeps the labels that overlap a mask, such as objects containing a
	 * marker. The overlap of a label is the fraction of its pixels inside the
	 * mask.
	 *
	 * @param labeling the labeled objects
	 * @param mask the selecting mask, with the same interval as the labeling
	 * @param minOverlap the minimum overlap fraction (between 0 and 1,
	 *          inclusive) to keep a label (default: keep each label with at
	 *          least one pixel inside the mask)
	 * @return a new labeling with only the overlapping labels
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @param <B> mask type
	 * @implNote op names='labeling.selectByOverlap', type=Function
	 */
	public static <L, I extends IntegerType<I>, B extends BooleanType<B>>
		ImgLabeling<L, I> selectByOverlap(final ImgLabeling<L, I> labeling,
			final RandomAccessibleInterval<B> mask, @Nullable Double minOverlap)
	{
		final var numSets = labeling.getMapping().numSets();
		final var inside = new long[numSets];
		LoopBuilder.setImages(labeling.getIndexImg(), mask).forEachPixel((index,
			m) -> {
			if (m.get()) inside[index.getInteger()]++;
		});
		final var insideSizes = Labelings.sumPerLabel(labeling, inside);
		final var sizes = Labelings.sizes(labeling);
		return Labelings.retain(labeling, label -> {
			final long in = insideSizes.getOrDefault(label, 0L);
			if (in == 0) return false;
			return minOverlap == null || (double) in / sizes.get(
				label) >= minOverlap;
		});
	}

	private static long largestSize(final Map<?, Long> sizes) {
		var largest = -1L;
		for (final var size : sizes.values())
			largest = Math.max(largest, size);
		return largest;
	}
}
