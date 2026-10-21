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

package org.scijava.ops.image.labeling.distance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.distance.DistanceTransform;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.ops.spi.Nullable;

/**
 * Distance maps and morphological operations of labelings, based on exact
 * Euclidean distances.
 * <p>
 * All ops treat each distinct label set of the labeling as one region: a
 * pixel carrying the labels {A, B} belongs to a region distinct from the
 * pixels carrying only A. For labelings without overlaps, the regions are
 * simply the labels. Distances are measured between pixel centers, in the
 * units of the given pixel spacing (default 1 along each dimension), and the
 * labeling interval is not extended: pixels outside it are neither
 * background nor other labels.
 * </p>
 * <p>
 * These are clean-room implementations of the label distance map and label
 * morphology tools of MorphoLibJ (D. Legland, I. Arganda-Carreras and P.
 * Andrey, "MorphoLibJ: integrated library and plugins for mathematical
 * morphology with ImageJ", <i>Bioinformatics</i> 32(22): 3532&ndash;3534,
 * 2016), generalized to n-dimensional, calibrated labelings and using exact
 * rather than chamfer distances. The distance map uses the separable
 * algorithm of T. Saito and J.-I. Toriwaki ("New algorithms for Euclidean
 * distance transformation of an n-dimensional digitized picture with
 * applications", <i>Pattern Recognition</i> 27(11): 1551&ndash;1565, 1994),
 * with the lower envelope computation of P. F. Felzenszwalb and D. P.
 * Huttenlocher ("Distance transforms of sampled functions", <i>Theory of
 * Computing</i> 8: 415&ndash;428, 2012), restricted to runs of equal regions
 * so that touching regions are separated. Dilation and influence zones use
 * the Voronoi distance transform of ImgLib2.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class LabelDistances {

	private LabelDistances() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes, for each labeled pixel, the Euclidean distance to the nearest
	 * pixel that does not belong to its region, i.e. that is unlabeled or
	 * carries different labels. Pixels at the edge of a region thus have a
	 * distance of one pixel spacing, also where two regions touch. Unlabeled
	 * pixels have distance 0. A region whose distance is not bounded within
	 * the labeling interval (such as a region filling it) has infinite
	 * distance.
	 *
	 * @param labeling the labeled objects
	 * @param spacing the pixel spacing along each dimension (default 1)
	 * @return the distance map, with the interval of the labeling
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.distanceMap', type=Function
	 */
	public static <L, I extends IntegerType<I>> RandomAccessibleInterval<DoubleType>
		distanceMap(final ImgLabeling<L, I> labeling, @Nullable double[] spacing)
	{
		final var regions = Regions.of(labeling);
		final var d = squaredDistances(regions.ids, regions.empty, regions.dims,
			spacing(spacing, regions.dims.length));
		for (var i = 0; i < d.length; i++)
			d[i] = Math.sqrt(d[i]);
		final RandomAccessibleInterval<DoubleType> img = ArrayImgs.doubles(d,
			regions.dims);
		return Views.translate(img, labeling.minAsLongArray());
	}

	/**
	 * Dilates the labels by the given radius. Each unlabeled pixel within the
	 * radius of a labeled pixel takes the labels of the nearest labeled pixel,
	 * so labels never overwrite each other: where two labels grow into each
	 * other, they meet halfway (ties are broken arbitrarily).
	 *
	 * @param labeling the labeled objects
	 * @param radius the dilation radius, in the units of the spacing
	 * @param spacing the pixel spacing along each dimension (default 1)
	 * @return a new labeling with the labels dilated
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.dilate', type=Function
	 */
	public static <L, I extends IntegerType<I>> ImgLabeling<L, I> dilate(
		final ImgLabeling<L, I> labeling, final Double radius,
		@Nullable double[] spacing)
	{
		final var regions = Regions.of(labeling);
		final var r2 = radius * radius;
		final var d = propagate(regions, spacing);
		for (var i = 0; i < d.length; i++)
			if (d[i] > r2) regions.ids[i] = regions.empty;
		return regions.toLabeling(labeling);
	}

	/**
	 * Erodes the labels by the given radius: each labeled pixel within the
	 * radius of a pixel outside its region becomes unlabeled. By default,
	 * regions are eroded from both the unlabeled pixels and the pixels of other
	 * regions, so touching labels separate.
	 *
	 * @param labeling the labeled objects
	 * @param radius the erosion radius, in the units of the spacing
	 * @param spacing the pixel spacing along each dimension (default 1)
	 * @param backgroundOnly if true, erode only from the unlabeled pixels,
	 *          leaving the borders between touching regions in place (default
	 *          false)
	 * @return a new labeling with the labels eroded
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.erode', type=Function
	 */
	public static <L, I extends IntegerType<I>> ImgLabeling<L, I> erode(
		final ImgLabeling<L, I> labeling, final Double radius,
		@Nullable double[] spacing, @Nullable Boolean backgroundOnly)
	{
		final var regions = Regions.of(labeling);
		erode(regions, radius, spacing, Boolean.TRUE.equals(backgroundOnly));
		return regions.toLabeling(labeling);
	}

	/**
	 * Opens the labels by the given radius, i.e. erodes and then dilates them.
	 * Each region is reduced to the union of the balls of the given radius
	 * that fit inside it, removing details (and regions) thinner than the
	 * ball.
	 *
	 * @param labeling the labeled objects
	 * @param radius the radius of the ball, in the units of the spacing
	 * @param spacing the pixel spacing along each dimension (default 1)
	 * @return a new labeling with the labels opened
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.open', type=Function
	 */
	public static <L, I extends IntegerType<I>> ImgLabeling<L, I> open(
		final ImgLabeling<L, I> labeling, final Double radius,
		@Nullable double[] spacing)
	{
		final var regions = Regions.of(labeling);
		erode(regions, radius, spacing, false);
		final var d = propagate(regions, spacing);
		final var r2 = radius * radius;
		for (var i = 0; i < d.length; i++)
			if (d[i] > r2) regions.ids[i] = regions.empty;
		return regions.toLabeling(labeling);
	}

	/**
	 * Closes the labels by the given radius, i.e. dilates them (without
	 * overwriting each other) and then erodes the result from the unlabeled
	 * pixels only. Gaps narrower than the ball are filled, and the pixels of a
	 * filled gap between two regions are split between them by proximity.
	 * Labeled pixels of the input keep their labels.
	 *
	 * @param labeling the labeled objects
	 * @param radius the radius of the ball, in the units of the spacing
	 * @param spacing the pixel spacing along each dimension (default 1)
	 * @return a new labeling with the labels closed
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.close', type=Function
	 */
	public static <L, I extends IntegerType<I>> ImgLabeling<L, I> close(
		final ImgLabeling<L, I> labeling, final Double radius,
		@Nullable double[] spacing)
	{
		final var regions = Regions.of(labeling);
		final var d = propagate(regions, spacing);
		final var r2 = radius * radius;
		for (var i = 0; i < d.length; i++)
			if (d[i] > r2) regions.ids[i] = regions.empty;
		erode(regions, radius, spacing, true);
		return regions.toLabeling(labeling);
	}

	/**
	 * Computes the influence zones of the labels, i.e. their Voronoi
	 * tessellation: each pixel takes the labels of the nearest labeled pixel
	 * (ties are broken arbitrarily). Labeled pixels keep their labels.
	 *
	 * @param labeling the labeled objects
	 * @param spacing the pixel spacing along each dimension (default 1)
	 * @param mask a mask restricting the influence zones: pixels where it is
	 *          false are left unlabeled (default: no restriction). Note that
	 *          distances are still Euclidean, not geodesic within the mask.
	 * @return a new labeling of the influence zones
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @param <B> mask type
	 * @implNote op names='labeling.influenceZones', type=Function
	 */
	public static <L, I extends IntegerType<I>, B extends BooleanType<B>>
		ImgLabeling<L, I> influenceZones(final ImgLabeling<L, I> labeling,
			@Nullable double[] spacing, @Nullable RandomAccessibleInterval<B> mask)
	{
		final var regions = Regions.of(labeling);
		propagate(regions, spacing);
		if (mask != null) {
			if (!Intervals.equalDimensions(mask, labeling)) {
				throw new IllegalArgumentException(
					"Mask and labeling dimensions differ");
			}
			var i = 0;
			for (final var m : Views.flatIterable(mask)) {
				if (!m.get()) regions.ids[i] = regions.empty;
				i++;
			}
		}
		return regions.toLabeling(labeling);
	}

	// -- Helper methods --

	private static void erode(final Regions regions, final double radius,
		final double[] spacing, final boolean backgroundOnly)
	{
		final var ids = regions.ids;
		final int[] areas;
		if (backgroundOnly) {
			areas = new int[ids.length];
			for (var i = 0; i < ids.length; i++)
				areas[i] = ids[i] == regions.empty ? 0 : 1;
		}
		else areas = ids;
		final var empty = backgroundOnly ? 0 : regions.empty;
		final var d = squaredDistances(areas, empty, regions.dims, spacing(
			spacing, regions.dims.length));
		final var r2 = radius * radius;
		for (var i = 0; i < d.length; i++)
			if (d[i] <= r2) ids[i] = regions.empty;
	}

	/**
	 * Propagates the region ids to all unlabeled pixels, by nearest labeled
	 * pixel, and returns the squared distances to it.
	 */
	private static double[] propagate(final Regions regions,
		final double[] spacing)
	{
		final var n = regions.dims.length;
		final var s = spacing(spacing, n);
		final var weights = new double[n];
		for (var d = 0; d < n; d++)
			weights[d] = s[d] * s[d];
		final var ids = ArrayImgs.ints(regions.ids, regions.dims);
		final var distances = DistanceTransform.voronoiDistanceTransform(ids,
			regions.empty, weights);
		final var result = new double[regions.ids.length];
		var i = 0;
		for (final var v : Views.flatIterable(distances))
			result[i++] = v.get();
		return result;
	}

	private static double[] spacing(final double[] spacing, final int n) {
		if (spacing == null) {
			final var ones = new double[n];
			Arrays.fill(ones, 1);
			return ones;
		}
		if (spacing.length != n) {
			throw new IllegalArgumentException("Expected " + n +
				" spacing values but got " + spacing.length);
		}
		return spacing;
	}

	/**
	 * Computes the squared Euclidean distance of each pixel to the nearest
	 * pixel with a different region id. Pixels of the {@code empty} region
	 * get distance 0. One pass per dimension computes the exact distance
	 * restricted to the subspace of the dimensions processed so far: along
	 * each line, a pixel of region k is either closest to the first pixel
	 * outside its run of region k, or to a pixel inside that run plus that
	 * pixel's partial distance from the previous passes.
	 */
	static double[] squaredDistances(final int[] ids, final int empty,
		final long[] dims, final double[] spacing)
	{
		final var total = ids.length;
		final var f = new double[total];
		for (var i = 0; i < total; i++)
			f[i] = ids[i] == empty ? 0 : Double.POSITIVE_INFINITY;
		var stride = 1;
		final var maxLength = (int) Arrays.stream(dims).max().orElse(1);
		final var line = new double[maxLength];
		final var pos = new int[maxLength + 2];
		final var val = new double[maxLength + 2];
		final var v = new int[maxLength + 2];
		final var z = new double[maxLength + 3];
		for (var d = 0; d < dims.length; d++) {
			final var length = (int) dims[d];
			final var w = spacing[d] * spacing[d];
			final var block = stride * length;
			for (var outer = 0; outer < total; outer += block) {
				for (var inner = 0; inner < stride; inner++) {
					final var start = outer + inner;
					processLine(ids, f, empty, start, stride, length, w, line, pos, val,
						v, z);
				}
			}
			stride = block;
		}
		return f;
	}

	private static void processLine(final int[] ids, final double[] f,
		final int empty, final int start, final int stride, final int length,
		final double w, final double[] line, final int[] pos, final double[] val,
		final int[] v, final double[] z)
	{
		var a = 0;
		while (a < length) {
			final var id = ids[start + a * stride];
			var b = a;
			while (b + 1 < length && ids[start + (b + 1) * stride] == id)
				b++;
			if (id != empty) {
				// Collect the sample points: the run and its outside neighbors.
				var count = 0;
				if (a > 0) {
					pos[count] = a - 1;
					val[count++] = 0;
				}
				for (var i = a; i <= b; i++) {
					final var g = f[start + i * stride];
					if (g == Double.POSITIVE_INFINITY) continue;
					pos[count] = i;
					val[count++] = g;
				}
				if (b < length - 1) {
					pos[count] = b + 1;
					val[count++] = 0;
				}
				lowerEnvelope(pos, val, count, a, b, w, line, v, z);
				for (var i = a; i <= b; i++)
					f[start + i * stride] = line[i];
			}
			a = b + 1;
		}
	}

	/**
	 * Evaluates min_q (val[q] + w (i - pos[q])^2) for each i in [a, b], via the
	 * lower envelope of parabolas.
	 */
	private static void lowerEnvelope(final int[] pos, final double[] val,
		final int count, final int a, final int b, final double w,
		final double[] out, final int[] v, final double[] z)
	{
		if (count == 0) {
			for (var i = a; i <= b; i++)
				out[i] = Double.POSITIVE_INFINITY;
			return;
		}
		var k = 0;
		v[0] = 0;
		z[0] = Double.NEGATIVE_INFINITY;
		z[1] = Double.POSITIVE_INFINITY;
		for (var q = 1; q < count; q++) {
			double s;
			while (true) {
				final var p = v[k];
				s = ((val[q] + w * pos[q] * pos[q]) - (val[p] + w * pos[p] *
					pos[p])) / (2 * w * (pos[q] - pos[p]));
				if (k == 0 || s > z[k]) break;
				k--;
			}
			k++;
			v[k] = q;
			z[k] = s;
			z[k + 1] = Double.POSITIVE_INFINITY;
		}
		k = 0;
		for (var i = a; i <= b; i++) {
			while (z[k + 1] < i)
				k++;
			final var p = v[k];
			final double dx = i - pos[p];
			out[i] = val[p] + w * dx * dx;
		}
	}

	/** The label set index of each pixel, as a flat array. */
	private static final class Regions {

		private final int[] ids;
		private final long[] dims;
		private final int empty;
		private final List<Set<?>> sets;

		private Regions(final int[] ids, final long[] dims, final int empty,
			final List<Set<?>> sets)
		{
			this.ids = ids;
			this.dims = dims;
			this.empty = empty;
			this.sets = sets;
		}

		private static Regions of(final ImgLabeling<?, ?> labeling) {
			final var dims = labeling.dimensionsAsLongArray();
			final var size = Intervals.numElements(labeling);
			if (size > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Labeling too large: " + size +
					" pixels");
			}
			final var mapping = labeling.getMapping();
			final List<Set<?>> sets = new ArrayList<>();
			for (var i = 0; i < mapping.numSets(); i++)
				sets.add(mapping.labelsAtIndex(i));
			final var ids = new int[(int) size];
			var i = 0;
			for (final var index : Views.flatIterable(labeling.getIndexImg()))
				ids[i++] = index.getInteger();
			// Note: Index 0 of a labeling mapping is always the empty label set.
			return new Regions(ids, dims, 0, sets);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private <L, I extends IntegerType<I>> ImgLabeling<L, I> toLabeling(
			final ImgLabeling<L, I> like)
		{
			final var indexImg = like.getIndexImg();
			final var type = indexImg.getType().createVariable();
			final Img<I> img = Util.getSuitableImgFactory(indexImg, type).create(
				indexImg);
			var i = 0;
			for (final var index : Views.flatIterable(img))
				index.setInteger(ids[i++]);
			final RandomAccessibleInterval<I> out = Views.isZeroMin(indexImg) ? img
				: Views.translate(img, indexImg.minAsLongArray());
			return ImgLabeling.fromImageAndLabelSets(out, (List) sets);
		}
	}
}
