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

package org.scijava.ops.image.labeling.measure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import org.scijava.ops.image.util.Calibrations;
import org.scijava.ops.spi.Nullable;

/**
 * The maximum Feret diameter (caliper diameter) of each labeled region, in
 * any number of dimensions: the largest distance between two points of the
 * region, each pixel counting as a solid box of the size of the pixel
 * spacing.
 * <p>
 * The farthest pair of points of a set is a pair of vertices of its convex
 * hull, so only pixel corners that can be hull vertices are compared: a
 * corner strictly between two other corners of the region on an axis-aligned
 * line cannot be a vertex, which leaves the corners that are extreme on all
 * axis-aligned lines through them. The remaining candidates are compared
 * exhaustively, which takes time quadratic in their number: fine for typical
 * cells and nuclei, but slow for very large 3D regions (a digital ball of
 * radius 200 leaves on the order of 10<sup>5</sup> candidates). For 2D polygons, see
 * also {@code geom.maximumFeretsDiameter}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class FeretDiameters {

	private FeretDiameters() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes the maximum Feret diameter of each label, in the units of the
	 * pixel spacing. A single pixel has the length of its diagonal (&radic;2 in
	 * 2D with unit calibration).
	 *
	 * @param labeling the labeled objects
	 * @param calibration the pixel spacing along each dimension (default 1)
	 * @return a map from each label to its maximum Feret diameter
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.maximumFeretsDiameter', type=Function
	 */
	public static <L, I extends IntegerType<I>> Map<L, Double> diameters(
		final ImgLabeling<L, I> labeling, @Nullable double[] calibration)
	{
		final var result = new LinkedHashMap<L, Double>();
		for (final var entry : ferets(labeling, calibration).entrySet()) {
			final var p = entry.getValue();
			double d2 = 0;
			for (var d = 0; d < p.getA().numDimensions(); d++) {
				final var delta = p.getA().getDoublePosition(d) - p.getB()
					.getDoublePosition(d);
				d2 += delta * delta;
			}
			result.put(entry.getKey(), Math.sqrt(d2));
		}
		return result;
	}

	/**
	 * Finds the endpoints of the maximum Feret diameter of each label: two
	 * pixel corners at the largest distance from each other, in calibrated
	 * coordinates (the pixel position times the calibration, with pixel centers
	 * at integer positions, so corners lie half a pixel away from them).
	 *
	 * @param labeling the labeled objects
	 * @param calibration the pixel spacing along each dimension (default 1)
	 * @return a map from each label to the two endpoints of its maximum Feret
	 *         diameter
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.maximumFeret', type=Function
	 */
	public static <L, I extends IntegerType<I>>
		Map<L, Pair<RealLocalizable, RealLocalizable>> ferets(
			final ImgLabeling<L, I> labeling, @Nullable double[] calibration)
	{
		final var n = labeling.numDimensions();
		final var s = Calibrations.spacing(calibration, n, "calibration");
		final var result =
			new LinkedHashMap<L, Pair<RealLocalizable, RealLocalizable>>();
		for (final var region : new LabelRegions<>(labeling)) {
			if (region.inside().size() == 0) continue;
			result.put(region.getLabel(), feret(region, s));
		}
		return result;
	}

	private static Pair<RealLocalizable, RealLocalizable> feret(
		final LabelRegion<?> region, final double[] s)
	{
		final var n = region.numDimensions();
		final var min = region.minAsLongArray();
		// The corner grid has one more node than pixels along each dimension.
		final var dims = new int[n];
		final var strides = new int[n];
		long total = 1;
		for (var d = 0; d < n; d++) {
			dims[d] = (int) region.dimension(d) + 1;
			strides[d] = (int) total;
			total *= dims[d];
		}
		if (total > Integer.MAX_VALUE - 8) {
			throw new IllegalArgumentException("Region too large: " + total +
				" corners");
		}
		final var size = (int) total;

		// Mark the corners of all region pixels.
		final var corner = new boolean[size];
		final var cursor = region.inside().localizingCursor();
		final var cornerCount = 1 << n;
		while (cursor.hasNext()) {
			cursor.fwd();
			var base = 0;
			for (var d = 0; d < n; d++)
				base += (int) (cursor.getLongPosition(d) - min[d]) * strides[d];
			for (var c = 0; c < cornerCount; c++) {
				var index = base;
				for (var d = 0; d < n; d++)
					if ((c & (1 << d)) != 0) index += strides[d];
				corner[index] = true;
			}
		}

		// Keep the corners that are extreme on every axis-aligned line.
		final var extremeCount = new byte[size];
		for (var d = 0; d < n; d++) {
			final var len = dims[d];
			final var stride = strides[d];
			for (var start = 0; start < size; start++) {
				if ((start / stride) % len != 0) continue;
				var first = -1;
				var last = -1;
				for (int k = 0, i = start; k < len; k++, i += stride) {
					if (!corner[i]) continue;
					if (first < 0) first = i;
					last = i;
				}
				if (first < 0) continue;
				extremeCount[first]++;
				if (last != first) extremeCount[last]++;
			}
		}
		final var candidates = new ArrayList<double[]>();
		for (var i = 0; i < size; i++) {
			if (!corner[i] || extremeCount[i] < n) continue;
			final var p = new double[n];
			var rem = i;
			for (var d = 0; d < n; d++) {
				p[d] = (min[d] + rem % dims[d] - 0.5) * s[d];
				rem /= dims[d];
			}
			candidates.add(p);
		}

		var best = -1.0;
		double[] a = null, b = null;
		for (var i = 0; i < candidates.size(); i++) {
			final var p = candidates.get(i);
			for (var j = i + 1; j < candidates.size(); j++) {
				final var q = candidates.get(j);
				double d2 = 0;
				for (var d = 0; d < n; d++) {
					final var delta = p[d] - q[d];
					d2 += delta * delta;
				}
				if (d2 > best) {
					best = d2;
					a = p;
					b = q;
				}
			}
		}
		return new ValuePair<>(new RealPoint(a), new RealPoint(b));
	}
}
