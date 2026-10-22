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

import java.util.Arrays;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.ops.image.util.Calibrations;
import org.scijava.ops.spi.Nullable;

/**
 * Geodesic distance maps: the length of the shortest path from a marker to
 * each pixel that stays inside a mask.
 * <p>
 * Paths run between pixel centers along the steps of a discrete
 * neighborhood, each weighted by its Euclidean length in the units of the
 * pixel spacing, and the map is computed exactly for this weighted grid graph
 * with the algorithm of E. W. Dijkstra ("A note on two problems in
 * connexion with graphs", <i>Numerische Mathematik</i> 1: 269&ndash;271,
 * 1959), in a single pass and in any number of dimensions. With neighborhood
 * radius 1, the steps are those to the {@code 3^n - 1} adjacent pixels (in 2D
 * a chamfer metric with weights 1 and &radic;2); with radius 2, the steps
 * also include the primitive offsets reaching two pixels away, such as the
 * knight moves (1, 2) in 2D (weight &radic;5), which reduces the anisotropy
 * of the metric from about 8 % to about 3 % in 2D. Such a long step is only
 * taken if one of the two pixels nearest to its midpoint is also inside the
 * mask. Geodesic distances were introduced to image analysis by C.
 * Lantu&eacute;joul and S. Beucher ("On the use of the geodesic metric in
 * image analysis", <i>Journal of Microscopy</i> 121(1): 39&ndash;49, 1981);
 * the weighted neighborhoods follow the chamfer distances of G. Borgefors
 * ("Distance transformations in digital images", <i>Computer Vision,
 * Graphics, and Image Processing</i> 34(3): 344&ndash;371, 1986), with exact
 * Euclidean step lengths instead of integer weights.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class GeodesicDistanceMaps {

	private GeodesicDistanceMaps() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes the geodesic distance from the marker to each pixel of the
	 * mask: the length of the shortest path from a marker pixel that stays
	 * inside the mask. Marker pixels have distance 0, mask pixels that no path
	 * from the marker reaches have positive infinity, and pixels outside the
	 * mask are {@code NaN}. Marker pixels outside the mask are ignored.
	 *
	 * @param marker the pixels from which distances are measured, with the
	 *          same dimensions as the mask
	 * @param mask the pixels within which paths must stay
	 * @param spacing the pixel spacing along each dimension (default 1)
	 * @param radius the radius of the neighborhood of path steps, 1 (adjacent
	 *          pixels) or 2 (also knight moves and their n-D analogs; default)
	 * @return the geodesic distance map, with the interval of the mask
	 * @param <B> marker type
	 * @param <M> mask type
	 * @implNote op names='morphology.geodesicDistanceMap', type=Function
	 */
	public static <B extends BooleanType<B>, M extends BooleanType<M>>
		RandomAccessibleInterval<DoubleType> binary(
			final RandomAccessibleInterval<B> marker,
			final RandomAccessibleInterval<M> mask, @Nullable double[] spacing,
			@Nullable Integer radius)
	{
		final var region = new int[checkedSize(mask)];
		var i = 0;
		for (final var m : Views.flatIterable(mask))
			region[i++] = m.get() ? 0 : -1;
		return compute(marker, mask, region, spacing, radius);
	}

	/**
	 * Computes the geodesic distance from the marker to each labeled pixel,
	 * with paths that never leave the region of the pixel. Each distinct label
	 * set of the labeling is one region: a pixel carrying the labels {A, B}
	 * belongs to a region distinct from the pixels carrying only A. Labeled
	 * pixels that no path from the marker reaches (such as all pixels of a
	 * region without marker pixels) have positive infinity, and unlabeled
	 * pixels are {@code NaN}. Marker pixels outside the labels are ignored.
	 *
	 * @param marker the pixels from which distances are measured, with the
	 *          same dimensions as the labeling
	 * @param labeling the regions within which paths must stay
	 * @param spacing the pixel spacing along each dimension (default 1)
	 * @param radius the radius of the neighborhood of path steps, 1 (adjacent
	 *          pixels) or 2 (also knight moves and their n-D analogs; default)
	 * @return the geodesic distance map, with the interval of the labeling
	 * @param <B> marker type
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='morphology.geodesicDistanceMap', type=Function
	 */
	public static <B extends BooleanType<B>, L, I extends IntegerType<I>>
		RandomAccessibleInterval<DoubleType> labeling(
			final RandomAccessibleInterval<B> marker,
			final ImgLabeling<L, I> labeling, @Nullable double[] spacing,
			@Nullable Integer radius)
	{
		final var region = new int[checkedSize(labeling)];
		final var mapping = labeling.getMapping();
		final var empty = new boolean[mapping.numSets()];
		for (var s = 0; s < empty.length; s++)
			empty[s] = mapping.labelsAtIndex(s).isEmpty();
		var i = 0;
		for (final var index : Views.flatIterable(labeling.getIndexImg())) {
			final var s = index.getInteger();
			region[i++] = empty[s] ? -1 : s;
		}
		return compute(marker, labeling, region, spacing, radius);
	}

	private static <B extends BooleanType<B>>
		RandomAccessibleInterval<DoubleType> compute(
			final RandomAccessibleInterval<B> marker,
			final RandomAccessibleInterval<?> domain, final int[] region,
			final double[] spacing, final Integer radius)
	{
		if (!Arrays.equals(marker.dimensionsAsLongArray(), domain
			.dimensionsAsLongArray()))
		{
			throw new IllegalArgumentException(
				"Marker and mask dimensions differ: " + Arrays.toString(marker
					.dimensionsAsLongArray()) + " vs " + Arrays.toString(domain
						.dimensionsAsLongArray()));
		}
		final var dims = domain.dimensionsAsLongArray();
		final var graph = new GeodesicGraph(dims, region, Calibrations
			.spacing(spacing, dims.length, "spacing"), radius == null ? 2 : radius);
		final var seeds = new int[region.length];
		var count = 0;
		var i = 0;
		for (final var m : Views.flatIterable(marker)) {
			if (m.get()) seeds[count++] = i;
			i++;
		}
		graph.propagate(seeds, count);
		return Views.translate(ArrayImgs.doubles(graph.dist, dims), domain
			.minAsLongArray());
	}

	private static int checkedSize(final RandomAccessibleInterval<?> image) {
		final var size = Intervals.numElements(image);
		if (size > Integer.MAX_VALUE - 8) {
			throw new IllegalArgumentException("Image too large: " + size +
				" pixels");
		}
		return (int) size;
	}
}
