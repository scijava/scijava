/*-
 * #%L
 * GPL-licensed image processing operations for SciJava Ops, translated from copyleft ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.gpl.biovoxxel;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.Intervals;

import org.scijava.ops.image.labeling.utils.LabelFiltering;
import org.scijava.ops.spi.Nullable;

/**
 * Ops counting the secondary objects (such as speckles, spots or nuclei)
 * contained in each primary object (such as cells), and selecting the primary
 * objects by that count.
 * <p>
 * The secondary objects of a primary object are the connected components of
 * the secondary mask restricted to the primary object, with full connectivity
 * (8-connected in 2D, 26-connected in 3D). A secondary object crossing the
 * border of a primary object is thus clipped to it, and counted for each
 * primary object it overlaps, with its clipped size. Each primary label is
 * handled on its own, so labelings with overlapping labels are supported.
 * </p>
 * <p>
 * This is a translation of the {@code Speckle_Inspector} plugin of the
 * <a href="https://github.com/biovoxxel/BioVoxxel-Toolbox">BioVoxxel
 * Toolbox</a> by Jan Brocher (GPL v3+), generalized to n-D. The plugin
 * filters the primary objects by size and circularity and excludes edge
 * objects; with these ops, build and filter the primary labeling beforehand
 * (e.g. with {@code labeling.cca}, {@code labeling.filterBySize},
 * {@code labeling.filter} and {@code labeling.removeBorderLabels}). The plugin
 * delimits each primary object by its outer outline, so secondary objects in
 * its holes are counted too; to do the same, fill the holes of the primary
 * labeling first ({@code labeling.fillHoles}). The circularity limits of the
 * secondary objects are not supported.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Jan Brocher
 */
public final class ContainedCount {

	private ContainedCount() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Counts the secondary objects contained in each primary object.
	 *
	 * @param primary the primary objects
	 * @param secondary the secondary objects, as a mask with the same interval
	 *          as the primary labeling
	 * @param minSize the minimum pixel count (inclusive) of a counted secondary
	 *          object, after clipping to the primary object (default: no
	 *          minimum)
	 * @param maxSize the maximum pixel count (inclusive) of a counted secondary
	 *          object, after clipping to the primary object (default: no
	 *          maximum)
	 * @return the number of secondary objects in each primary label that has
	 *         pixels
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @param <B> type of the secondary mask
	 * @implNote op names='labeling.countContained', type=Function
	 */
	public static <L, I extends IntegerType<I>, B extends BooleanType<B>>
		Map<L, Integer> countContained(final ImgLabeling<L, I> primary,
			final RandomAccessibleInterval<B> secondary, @Nullable final Long minSize,
			@Nullable final Long maxSize)
	{
		if (!Intervals.equals(primary, secondary)) {
			throw new IllegalArgumentException(
				"The primary labeling and the secondary mask must have the same interval");
		}
		final long lo = minSize == null ? 0 : minSize;
		final long hi = maxSize == null ? Long.MAX_VALUE : maxSize;
		final int n = primary.numDimensions();
		final LabelRegions<L> regions = new LabelRegions<>(primary);
		final RandomAccess<B> sa = secondary.randomAccess();
		final Map<L, Integer> counts = new LinkedHashMap<>();
		for (final LabelRegion<L> region : regions) {
			// Mark the secondary pixels of the region in a local grid over its
			// bounding box.
			final long[] min = region.minAsLongArray();
			final int[] dims = new int[n];
			final int[] strides = new int[n];
			long total = 1;
			for (int d = 0; d < n; d++) {
				dims[d] = (int) region.dimension(d);
				strides[d] = (int) total;
				total *= dims[d];
			}
			if (total > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Label region too large: " +
					region.getLabel());
			}
			final boolean[] mask = new boolean[(int) total];
			final Cursor<Void> c = region.inside().localizingCursor();
			while (c.hasNext()) {
				c.fwd();
				sa.setPosition(c);
				if (!sa.get().get()) continue;
				int index = 0;
				for (int d = 0; d < n; d++)
					index += (int) (c.getLongPosition(d) - min[d]) * strides[d];
				mask[index] = true;
			}
			counts.put(region.getLabel(), countComponents(mask, dims, strides, lo,
				hi));
		}
		return counts;
	}

	/**
	 * Keeps the primary objects whose number of contained secondary objects
	 * lies within a range, like the objects marked in magenta by the plugin.
	 *
	 * @param primary the primary objects
	 * @param secondary the secondary objects, as a mask with the same interval
	 *          as the primary labeling
	 * @param minCount the minimum number (inclusive) of secondary objects of a
	 *          kept primary object (default 0)
	 * @param maxCount the maximum number (inclusive) of secondary objects of a
	 *          kept primary object (default: no maximum)
	 * @param minSize the minimum pixel count (inclusive) of a counted secondary
	 *          object, after clipping to the primary object (default: no
	 *          minimum)
	 * @param maxSize the maximum pixel count (inclusive) of a counted secondary
	 *          object, after clipping to the primary object (default: no
	 *          maximum)
	 * @return a new labeling with only the primary labels in the count range
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @param <B> type of the secondary mask
	 * @implNote op names='labeling.filterByContainedCount', type=Function
	 */
	public static <L, I extends IntegerType<I>, B extends BooleanType<B>>
		ImgLabeling<L, I> filterByContainedCount(final ImgLabeling<L, I> primary,
			final RandomAccessibleInterval<B> secondary,
			@Nullable final Integer minCount, @Nullable final Integer maxCount,
			@Nullable final Long minSize, @Nullable final Long maxSize)
	{
		final Map<L, Integer> counts = countContained(primary, secondary, minSize,
			maxSize);
		return LabelFiltering.filterByValue(primary, counts, minCount == null
			? null : minCount.doubleValue(), maxCount == null ? null : maxCount
				.doubleValue());
	}

	/**
	 * Counts the fully connected components of a mask whose size is in range.
	 * The mask is cleared.
	 */
	private static int countComponents(final boolean[] mask, final int[] dims,
		final int[] strides, final long minSize, final long maxSize)
	{
		final int n = dims.length;
		final int[] pos = new int[n];
		final int[] q = new int[n];
		final int nOffsets = (int) Math.pow(3, n);
		final ArrayDeque<Integer> queue = new ArrayDeque<>();
		int count = 0;
		for (int seed = 0; seed < mask.length; seed++) {
			if (!mask[seed]) continue;
			mask[seed] = false;
			queue.add(seed);
			long size = 0;
			while (!queue.isEmpty()) {
				final int index = queue.poll();
				size++;
				int rest = index;
				for (int d = n - 1; d >= 0; d--) {
					pos[d] = rest / strides[d];
					rest %= strides[d];
				}
				for (int o = 0; o < nOffsets; o++) {
					int code = o;
					boolean inside = true;
					int neighbor = 0;
					for (int d = 0; d < n; d++) {
						q[d] = pos[d] + code % 3 - 1;
						code /= 3;
						if (q[d] < 0 || q[d] >= dims[d]) {
							inside = false;
							break;
						}
						neighbor += q[d] * strides[d];
					}
					if (inside && mask[neighbor]) {
						mask[neighbor] = false;
						queue.add(neighbor);
					}
				}
			}
			if (size >= minSize && size <= maxSize) count++;
		}
		return count;
	}
}
