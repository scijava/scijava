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

package org.scijava.ops.image.features.texture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * Utility methods shared by the texture matrix Ops: gray level discretization
 * of a region of interest, neighbor offsets, and connected zone labeling.
 * <p>
 * All texture matrices of this package (except the NGTDM) are indexed as
 * {@code matrix[i - 1][j - 1]}, where {@code i} is the (1-based) discretized
 * gray level and {@code j} is the (1-based) run length, zone size, zone
 * distance or dependence count, following the conventions of the Image
 * Biomarker Standardisation Initiative (IBSI).
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class TextureUtils {

	private TextureUtils() {
		// Prevent instantiation of utility class.
	}

	/**
	 * A discretized region of interest, stored as a flat array in which
	 * dimension 0 varies fastest.
	 */
	public static final class GrayLevels {

		/** Discretized gray level of each voxel; 0 for voxels outside the ROI. */
		public final int[] levels;
		/** Dimensions of the flattened interval. */
		public final int[] dims;
		/** Strides of each dimension in {@link #levels}. */
		public final int[] strides;
		/** Number of gray levels (the maximum possible level). */
		public final int nLevels;
		/** Number of voxels inside the ROI. */
		public final long count;

		GrayLevels(final int[] levels, final int[] dims, final int nLevels,
			final long count)
		{
			this.levels = levels;
			this.dims = dims;
			this.nLevels = nLevels;
			this.count = count;
			strides = new int[dims.length];
			var s = 1;
			for (var d = 0; d < dims.length; d++) {
				strides[d] = s;
				s *= dims[d];
			}
		}

		/** @return the number of dimensions. */
		public int numDimensions() {
			return dims.length;
		}

		/**
		 * Gets the flat index of the voxel at {@code index + offset}, or -1 if
		 * that voxel lies outside the interval.
		 *
		 * @param index flat index of the reference voxel
		 * @param offset per-dimension offset
		 * @return the flat index of the offset voxel, or -1
		 */
		public int offset(final int index, final int[] offset) {
			var result = index;
			for (var d = 0; d < dims.length; d++) {
				if (offset[d] == 0) continue;
				final var p = (index / strides[d]) % dims[d] + offset[d];
				if (p < 0 || p >= dims[d]) return -1;
				result += offset[d] * strides[d];
			}
			return result;
		}

		/**
		 * Gets the gray level at the given flat index, or 0 if the index is -1.
		 *
		 * @param index flat index, or -1
		 * @return the gray level (0 when outside the ROI)
		 */
		public int level(final int index) {
			return index < 0 ? 0 : levels[index];
		}
	}

	/**
	 * Discretizes the gray levels of a region of interest.
	 * <p>
	 * If {@code nGrayLevels} is {@code null}, the input values are taken as the
	 * gray levels directly (rounded to the nearest integer); they must then be
	 * at least 1, and the number of gray levels is the maximum value inside the
	 * ROI. Otherwise, the ROI intensities are discretized into
	 * {@code nGrayLevels} bins of equal width spanning the ROI intensity range
	 * (the IBSI fixed bin number method).
	 * </p>
	 *
	 * @param input the input image
	 * @param mask the ROI mask, or {@code null} to use the whole image. Only the
	 *          intersection of the image and mask intervals is considered,
	 *          which allows e.g. a {@code LabelRegion} to be used as the mask.
	 * @param nGrayLevels the number of gray levels, or {@code null}
	 * @return the discretized ROI
	 */
	public static <T extends RealType<T>, B extends BooleanType<B>> GrayLevels
		discretize(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<B> mask, final Integer nGrayLevels)
	{
		if (nGrayLevels != null && nGrayLevels < 1) {
			throw new IllegalArgumentException(
				"Number of gray levels must be positive: " + nGrayLevels);
		}
		final Interval interval = mask == null ? input : Intervals.intersect(
			input, mask);
		if (Intervals.isEmpty(interval)) {
			throw new IllegalArgumentException(
				"Input and mask intervals do not overlap");
		}
		final var dims = new int[interval.numDimensions()];
		long total = 1;
		for (var d = 0; d < dims.length; d++) {
			dims[d] = (int) interval.dimension(d);
			total *= dims[d];
		}
		if (total > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Region of interest is too large");
		}
		final var values = new double[(int) total];
		final var inRoi = new boolean[values.length];
		final var in = Views.flatIterable(Views.interval(input, interval))
			.cursor();
		final var m = mask == null ? null : Views.flatIterable(Views.interval(
			mask, interval)).cursor();
		var min = Double.POSITIVE_INFINITY;
		var max = Double.NEGATIVE_INFINITY;
		long count = 0;
		for (var i = 0; i < values.length; i++) {
			final var v = in.next().getRealDouble();
			final var inside = m == null ? true : m.next().get();
			if (!inside || Double.isNaN(v)) continue;
			values[i] = v;
			inRoi[i] = true;
			count++;
			if (v < min) min = v;
			if (v > max) max = v;
		}
		final var levels = new int[values.length];
		if (count == 0) return new GrayLevels(levels, dims, nGrayLevels == null
			? 1 : nGrayLevels, 0);
		int nLevels;
		if (nGrayLevels == null) {
			nLevels = 0;
			for (var i = 0; i < values.length; i++) {
				if (!inRoi[i]) continue;
				final var level = Math.round(values[i]);
				if (level < 1 || level > Integer.MAX_VALUE) {
					throw new IllegalArgumentException("Gray level " + values[i] +
						" out of range; specify the number of gray levels to discretize");
				}
				levels[i] = (int) level;
				if (levels[i] > nLevels) nLevels = levels[i];
			}
		}
		else {
			nLevels = nGrayLevels;
			final var range = max - min;
			for (var i = 0; i < values.length; i++) {
				if (!inRoi[i]) continue;
				if (range == 0) levels[i] = 1;
				else levels[i] = Math.min(nLevels, (int) Math.floor(nLevels *
					(values[i] - min) / range) + 1);
			}
		}
		return new GrayLevels(levels, dims, nLevels, count);
	}

	/**
	 * Gets the offsets of all {@code numDims}-dimensional neighbors within the
	 * given Chebyshev distance, excluding the zero offset.
	 *
	 * @param numDims the dimensionality
	 * @param distance the Chebyshev distance
	 * @return the list of offsets
	 */
	public static List<int[]> neighborOffsets(final int numDims,
		final int distance)
	{
		final List<int[]> offsets = new ArrayList<>();
		final var o = new int[numDims];
		Arrays.fill(o, -distance);
		while (true) {
			var zero = true;
			for (final var v : o)
				if (v != 0) zero = false;
			if (!zero) offsets.add(o.clone());
			var d = 0;
			while (d < numDims && o[d] == distance) {
				o[d] = -distance;
				d++;
			}
			if (d == numDims) break;
			o[d]++;
		}
		return offsets;
	}

	/**
	 * Gets one of each pair of opposite unit directions of the
	 * {@code numDims}-dimensional grid, i.e. the (3<sup>n</sup>-1)/2 offsets in
	 * {-1, 0, 1}<sup>n</sup> whose last nonzero component is positive. For 2D
	 * these are the 4 directions 0, 45, 90 and 135 degrees; for 3D these are the
	 * 13 standard directions.
	 *
	 * @param numDims the dimensionality
	 * @return the list of directions
	 */
	public static List<int[]> directions(final int numDims) {
		final List<int[]> dirs = new ArrayList<>();
		for (final var o : neighborOffsets(numDims, 1)) {
			var d = numDims - 1;
			while (o[d] == 0)
				d--;
			if (o[d] > 0) dirs.add(o);
		}
		return dirs;
	}

	/**
	 * Labels the connected zones of equal gray level within the ROI, using full
	 * (8-connectivity in 2D, 26-connectivity in 3D) connectivity.
	 *
	 * @param g the discretized ROI
	 * @return the zone label of each voxel (1-based; 0 outside the ROI), with
	 *         the number of zones stored as the last array element
	 */
	public static int[] labelZones(final GrayLevels g) {
		final var labels = new int[g.levels.length + 1];
		final var offsets = neighborOffsets(g.numDimensions(), 1);
		final var stack = new int[g.levels.length];
		var nZones = 0;
		for (var start = 0; start < g.levels.length; start++) {
			if (g.levels[start] == 0 || labels[start] != 0) continue;
			final var level = g.levels[start];
			nZones++;
			labels[start] = nZones;
			var top = 0;
			stack[top++] = start;
			while (top > 0) {
				final var p = stack[--top];
				for (final var o : offsets) {
					final var q = g.offset(p, o);
					if (q < 0 || labels[q] != 0 || g.levels[q] != level) continue;
					labels[q] = nZones;
					stack[top++] = q;
				}
			}
		}
		labels[g.levels.length] = nZones;
		return labels;
	}

	/**
	 * Removes trailing all-zero columns from a matrix, keeping at least one
	 * column.
	 *
	 * @param matrix the matrix to trim
	 * @return the trimmed matrix
	 */
	public static double[][] trimColumns(final double[][] matrix) {
		var width = 1;
		for (final var row : matrix) {
			for (var j = row.length - 1; j >= width; j--) {
				if (row[j] != 0) {
					width = j + 1;
					break;
				}
			}
		}
		final var result = new double[matrix.length][];
		for (var i = 0; i < matrix.length; i++)
			result[i] = Arrays.copyOf(matrix[i], width);
		return result;
	}
}
