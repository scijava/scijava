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

package org.scijava.ops.image.morphology.geodesic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.Point;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.IntegerType;

import org.scijava.ops.image.util.Calibrations;
import org.scijava.ops.spi.Nullable;

/**
 * Geodesic diameters of labeled regions: the length of the longest shortest
 * path between two pixels of a region that stays inside the region.
 * <p>
 * The diameter is found with the two-pass method of C. Lantu&eacute;joul and
 * S. Beucher ("On the use of the geodesic metric in image analysis",
 * <i>Journal of Microscopy</i> 121(1): 39&ndash;49, 1981) as used by D.
 * Legland and F. Beaugrand ("Automated clustering of lignocellulosic fibres
 * based on morphometric features and using clustering of variables",
 * <i>Industrial Crops and Products</i> 45: 253&ndash;261, 2013): starting
 * from a central pixel of the region (one farthest from its boundary), the
 * geodesically farthest pixel is the first extremity; the pixel farthest from
 * the first extremity is the second one, and their geodesic distance is the
 * diameter. This is exact for regions without holes in the continuous
 * setting, and a close lower bound otherwise. Geodesic distances are
 * computed as by {@code morphology.geodesicDistanceMap}, along the steps of a
 * neighborhood of radius 1 or 2, in any number of dimensions.
 * </p>
 * <p>
 * All ops measure each label on its own (a pixel carrying the labels {A, B}
 * belongs to both A and B), and report results in the units of the pixel
 * spacing.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class GeodesicDiameters {

	private GeodesicDiameters() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes the geodesic diameter of each label. The diameter is the
	 * geodesic distance between the centers of the two extremity pixels plus
	 * the length of the diagonal of one pixel, which accounts for the extent
	 * of the extremity pixels themselves: a single pixel has diameter
	 * &radic;2 in 2D, and a straight bar of {@code N} pixels has diameter
	 * {@code N - 1 + √2}. A label whose region is not connected (with respect
	 * to the steps of the neighborhood) has an infinite diameter.
	 *
	 * @param labeling the labeled objects
	 * @param calibration the pixel spacing along each dimension (default 1)
	 * @param radius the radius of the neighborhood of path steps, 1 (adjacent
	 *          pixels) or 2 (also knight moves and their n-D analogs; default)
	 * @return a map from each label to its geodesic diameter
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.geodesicDiameter', type=Function
	 */
	public static <L, I extends IntegerType<I>> Map<L, Double> diameters(
		final ImgLabeling<L, I> labeling, @Nullable double[] calibration,
		@Nullable Integer radius)
	{
		final var s = Calibrations.spacing(calibration, labeling
			.numDimensions(), "calibration");
		double diagonal = 0;
		for (final var si : s)
			diagonal += si * si;
		diagonal = Math.sqrt(diagonal);
		final var result = new LinkedHashMap<L, Double>();
		for (final var region : new LabelRegions<>(labeling)) {
			if (region.inside().size() == 0) continue;
			final var diameter = Diameter.of(region, s, radius);
			result.put(region.getLabel(), diameter.connected ? diameter.length +
				diagonal : Double.POSITIVE_INFINITY);
		}
		return result;
	}

	/**
	 * Computes a geodesic diameter path of each label: a shortest path inside
	 * the region between the two extremities of its geodesic diameter (see
	 * {@code labeling.geodesicDiameter}). The first point of each path is the
	 * first extremity and the last point is the second one; consecutive
	 * points are steps of the neighborhood. For a region that is not
	 * connected, the path lies in the part containing its most central pixel.
	 *
	 * @param labeling the labeled objects
	 * @param calibration the pixel spacing along each dimension (default 1)
	 * @param radius the radius of the neighborhood of path steps, 1 (adjacent
	 *          pixels) or 2 (also knight moves and their n-D analogs; default)
	 * @return a map from each label to its path, as pixel positions
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.geodesicDiameterPath', type=Function
	 */
	public static <L, I extends IntegerType<I>> Map<L, List<Point>> paths(
		final ImgLabeling<L, I> labeling, @Nullable double[] calibration,
		@Nullable Integer radius)
	{
		final var s = Calibrations.spacing(calibration, labeling
			.numDimensions(), "calibration");
		final var result = new LinkedHashMap<L, List<Point>>();
		for (final var region : new LabelRegions<>(labeling)) {
			if (region.inside().size() == 0) continue;
			final var diameter = Diameter.of(region, s, radius);
			final var graph = diameter.graph;
			final var min = region.minAsLongArray();
			final var path = new ArrayList<Point>();
			for (var p = diameter.end; p >= 0; p = graph.pred[p]) {
				final var pos = graph.position(p);
				for (var d = 0; d < pos.length; d++)
					pos[d] += min[d];
				path.add(new Point(pos));
			}
			// Note: The predecessors lead from the second extremity to the first.
			Collections.reverse(path);
			result.put(region.getLabel(), path);
		}
		return result;
	}

	/** The geodesic diameter of one region, in its bounding box. */
	private static final class Diameter {

		GeodesicGraph graph;
		/** Index of the second extremity; predecessors lead to the first. */
		int end;
		double length;
		boolean connected;

		static Diameter of(final LabelRegion<?> region,
			final double[] calibration, final Integer radius)
		{
			final var dims = region.dimensionsAsLongArray();
			final var min = region.minAsLongArray();
			long total = 1;
			for (final var d : dims)
				total *= d;
			if (total > Integer.MAX_VALUE - 8) {
				throw new IllegalArgumentException("Region too large: " + total +
					" pixels");
			}
			final var ids = new int[(int) total];
			Arrays.fill(ids, -1);
			final var cursor = region.inside().localizingCursor();
			var count = 0;
			while (cursor.hasNext()) {
				cursor.fwd();
				var index = 0L;
				var stride = 1L;
				for (var d = 0; d < dims.length; d++) {
					index += (cursor.getLongPosition(d) - min[d]) * stride;
					stride *= dims[d];
				}
				ids[(int) index] = 0;
				count++;
			}
			final var graph = new GeodesicGraph(dims, ids, calibration,
				radius == null ? 2 : radius);

			// Find a central pixel: one farthest from the region boundary.
			final var seeds = new int[count];
			var seedCount = 0;
			for (var i = 0; i < ids.length; i++)
				if (ids[i] == 0 && graph.isBorder(i)) seeds[seedCount++] = i;
			graph.propagate(seeds, seedCount);
			final var center = graph.farthest();

			final var single = new int[1];
			single[0] = center;
			graph.propagate(single, 1);
			single[0] = graph.farthest();
			graph.propagate(single, 1);

			final var diameter = new Diameter();
			diameter.graph = graph;
			diameter.end = graph.farthest();
			diameter.length = graph.dist[diameter.end];
			diameter.connected = true;
			for (var i = 0; i < ids.length; i++) {
				if (graph.dist[i] == Double.POSITIVE_INFINITY) {
					diameter.connected = false;
					break;
				}
			}
			return diameter;
		}
	}
}
