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


package org.scijava.ops.image.lgpl.intrinsicvolumes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.iterator.LocalizingIntervalIterator;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

/**
 * Histograms of the binary configurations of 2&times;2 pixels or
 * 2&times;2&times;2 voxels of an image, from which intrinsic volumes are
 * obtained by applying the look-up tables of {@link IntrinsicVolumesLuts}.
 * <p>
 * Translated from {@code BinaryConfigurationsHistogram2D} and
 * {@code BinaryConfigurationsHistogram3D} of <a href=
 * "https://github.com/ijpb/MorphoLibJ">MorphoLibJ</a> (LGPL-3, Copyright (C)
 * 2014 - 2026 INRA) by David Legland, generalized to labelings with
 * overlapping labels.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author David Legland
 */
final class ConfigurationHistograms {

	/** Tile voxels (bit indices) in an order where consecutive ones differ by one step. */
	private static final int[] ORDER = { 0, 1, 3, 2, 6, 7, 5, 4 };

	/** Dimension of the step leading to each element of {@link #ORDER}. */
	private static final int[] MOVE_DIM = { -1, 0, 1, 0, 2, 0, 1, 0 };

	/** Direction of the step leading to each element of {@link #ORDER}. */
	private static final boolean[] MOVE_FWD = { true, true, true, false, true,
		true, false, false };

	private ConfigurationHistograms() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes the histogram of binary configurations of a 2D or 3D binary
	 * image.
	 *
	 * @param image the binary image
	 * @param innerFrame if true, consider only the configurations fully
	 *          contained in the image (for densities); otherwise, all the
	 *          configurations containing at least one pixel of the image, with
	 *          pixels outside the image considered as background
	 * @return the number of occurrences of each configuration
	 */
	static <B extends BooleanType<B>> long[] histogram(
		final RandomAccessibleInterval<B> image, final boolean innerFrame)
	{
		final var nd = checkDimensions(image);
		final var hist = new long[1 << (1 << nd)];
		final var origins = origins(image, innerFrame);
		if (origins == null) return hist;
		final var bg = image.getType().createVariable();
		bg.set(false);
		final var ra = Views.extendValue(image, bg).randomAccess();
		final var nVoxels = 1 << nd;
		final var pos = new long[nd];
		while (origins.hasNext()) {
			origins.fwd();
			origins.localize(pos);
			ra.setPosition(pos);
			var index = 0;
			for (var k = 0; k < nVoxels; k++) {
				move(ra, k);
				if (ra.get().get()) index |= 1 << ORDER[k];
			}
			hist[index]++;
		}
		return hist;
	}

	/**
	 * Computes the histogram of binary configurations of each label of a 2D or
	 * 3D labeling, considering all the configurations containing at least one
	 * pixel of the labeling. For each label, pixels without that label are
	 * considered as background.
	 *
	 * @param labeling the labeling
	 * @return the configuration histogram of each label occurring in the
	 *         labeling
	 */
	static <L, I extends IntegerType<I>> Map<L, long[]> histograms(
		final ImgLabeling<L, I> labeling)
	{
		final var nd = checkDimensions(labeling);
		final var mapping = labeling.getMapping();
		final var labels = new ArrayList<>(mapping.getLabels());
		final var ids = new HashMap<L, Integer>();
		for (var i = 0; i < labels.size(); i++)
			ids.put(labels.get(i), i);
		final var setLabels = new int[mapping.numSets()][];
		for (var s = 0; s < setLabels.length; s++) {
			setLabels[s] = mapping.labelsAtIndex(s).stream().mapToInt(ids::get)
				.toArray();
		}

		final var nVoxels = 1 << nd;
		final var hists = new long[labels.size()][1 << nVoxels];
		final var bits = new int[labels.size()];
		final var touched = new int[labels.size()];
		final var origins = origins(labeling, false);
		final var ra = indexAccess(labeling);
		final var pos = new long[nd];
		while (origins.hasNext()) {
			origins.fwd();
			origins.localize(pos);
			ra.setPosition(pos);
			var nTouched = 0;
			for (var k = 0; k < nVoxels; k++) {
				move(ra, k);
				for (final var id : setLabels[ra.get().getInteger()]) {
					if (bits[id] == 0) touched[nTouched++] = id;
					bits[id] |= 1 << ORDER[k];
				}
			}
			for (var t = 0; t < nTouched; t++) {
				final var id = touched[t];
				hists[id][bits[id]]++;
				bits[id] = 0;
			}
		}

		final var result = new LinkedHashMap<L, long[]>();
		for (var i = 0; i < labels.size(); i++) {
			if (isEmpty(hists[i])) continue;
			result.put(labels.get(i), hists[i]);
		}
		return result;
	}

	/**
	 * Computes the sum, over all 2&times;2&times;2 configurations fully contained
	 * in a 3D labeling, of the contributions of the pairs of voxels {@code (p, q)}
	 * of the configuration such that {@code p} has label {@code label1} and
	 * {@code q} has label {@code label2}.
	 *
	 * @param labeling the 3D labeling
	 * @param label1 the first label
	 * @param label2 the second label
	 * @param contribs the contribution of a voxel pair, for each of the 7
	 *          neighbor offsets X, Y, Z, XY, XZ, YZ and XYZ within a
	 *          configuration
	 * @return the sum of contributions
	 */
	static <L, I extends IntegerType<I>> double interfaceSum(
		final ImgLabeling<L, I> labeling, final L label1, final L label2,
		final double[] contribs)
	{
		if (labeling.numDimensions() != 3) {
			throw new IllegalArgumentException(
				"Interface area requires a 3D labeling, not " + labeling
					.numDimensions() + "D");
		}
		final var mapping = labeling.getMapping();
		final var has1 = new boolean[mapping.numSets()];
		final var has2 = new boolean[mapping.numSets()];
		for (var s = 0; s < has1.length; s++) {
			has1[s] = mapping.labelsAtIndex(s).contains(label1);
			has2[s] = mapping.labelsAtIndex(s).contains(label2);
		}
		// Bit masks flipping the coordinates of a voxel within a configuration,
		// in the order X, Y, Z, XY, XZ, YZ and XYZ.
		final int[] flips = { 1, 2, 4, 3, 5, 6, 7 };

		final var origins = origins(labeling, true);
		if (origins == null) return 0;
		final var ra = indexAccess(labeling);
		final var pos = new long[3];
		var sum = 0.0;
		while (origins.hasNext()) {
			origins.fwd();
			origins.localize(pos);
			ra.setPosition(pos);
			var bits1 = 0;
			var bits2 = 0;
			for (var k = 0; k < 8; k++) {
				move(ra, k);
				final var s = ra.get().getInteger();
				if (has1[s]) bits1 |= 1 << ORDER[k];
				if (has2[s]) bits2 |= 1 << ORDER[k];
			}
			if (bits1 == 0 || bits2 == 0) continue;
			for (var p = 0; p < 8; p++) {
				if ((bits1 & 1 << p) == 0) continue;
				for (var n = 0; n < flips.length; n++) {
					if ((bits2 & 1 << (p ^ flips[n])) != 0) sum += contribs[n];
				}
			}
		}
		return sum;
	}

	/** Computes the dot product of a configuration histogram with a LUT. */
	static double apply(final long[] histogram, final double[] lut) {
		var sum = 0.0;
		for (var i = 0; i < histogram.length; i++)
			sum += histogram[i] * lut[i];
		return sum;
	}

	/** Applies a LUT to the configuration histogram of each label. */
	static <L> Map<L, Double> apply(final Map<L, long[]> histograms,
		final double[] lut)
	{
		final var result = new LinkedHashMap<L, Double>();
		for (final var entry : histograms.entrySet())
			result.put(entry.getKey(), apply(entry.getValue(), lut));
		return result;
	}

	// -- Helper methods --

	private static int checkDimensions(final Interval interval) {
		final var nd = interval.numDimensions();
		if (nd != 2 && nd != 3) {
			throw new IllegalArgumentException(
				"Only 2D and 3D images are supported, not " + nd + "D");
		}
		return nd;
	}

	/**
	 * Iterates over the minimal corners of the configurations, or returns null
	 * if there is none.
	 */
	private static LocalizingIntervalIterator origins(final Interval interval,
		final boolean innerFrame)
	{
		final var nd = interval.numDimensions();
		final var min = new long[nd];
		final var max = new long[nd];
		for (var d = 0; d < nd; d++) {
			min[d] = innerFrame ? interval.min(d) : interval.min(d) - 1;
			max[d] = innerFrame ? interval.max(d) - 1 : interval.max(d);
			if (max[d] < min[d]) return null;
		}
		return new LocalizingIntervalIterator(min, max);
	}

	private static <L, I extends IntegerType<I>> RandomAccess<I> indexAccess(
		final ImgLabeling<L, I> labeling)
	{
		// Note: Index 0 of a labeling mapping is always the empty label set.
		final var indexImg = labeling.getIndexImg();
		final var empty = indexImg.getType().createVariable();
		empty.setZero();
		return Views.extendValue(indexImg, empty).randomAccess();
	}

	/** Moves from voxel {@code ORDER[k - 1]} to voxel {@code ORDER[k]}. */
	private static void move(final RandomAccess<?> ra, final int k) {
		if (k == 0) return;
		if (MOVE_FWD[k]) ra.fwd(MOVE_DIM[k]);
		else ra.bck(MOVE_DIM[k]);
	}

	private static boolean isEmpty(final long[] histogram) {
		for (final var count : histogram)
			if (count != 0) return false;
		return true;
	}

}
