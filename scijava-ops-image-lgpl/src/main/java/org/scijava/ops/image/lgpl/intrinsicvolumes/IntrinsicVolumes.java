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
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.ConstantUtils;

import org.scijava.ops.spi.Nullable;

/**
 * Ops estimating the intrinsic volumes (Minkowski functionals) of 2D and 3D
 * binary images and of each label of 2D and 3D labelings: area, perimeter and
 * Euler number in 2D; volume, surface area, mean breadth and Euler number in
 * 3D; their densities per unit area or volume; and the area of the interface
 * between two labels.
 * <p>
 * All measures are computed from the histogram of the binary configurations of
 * 2&times;2 pixels (or 2&times;2&times;2 voxels) of the image, by weighting each
 * configuration with a look-up table. Perimeter, surface area and mean breadth
 * use discretizations of the Crofton formula over 2 or 4 directions in 2D, or 3
 * or 13 directions in 3D, which (for 4 and 13 directions) are much less biased
 * than counting boundary pixels or measuring the length of a polygonal
 * contour. Euler numbers are computed for a chosen connectivity of the
 * foreground, given as a {@link Shape}: face connectivity (a
 * {@code DiamondShape} of radius 1, i.e. 4-connectivity in 2D and
 * 6-connectivity in 3D) or full connectivity (a {@code RectangleShape} of span
 * 1, i.e. 8-connectivity in 2D and 26-connectivity in 3D), which is the
 * default; other shapes are rejected. (MorphoLibJ defaults to face
 * connectivity for Euler numbers.) Spatial calibration is supported via the
 * pixel spacing.
 * </p>
 * <p>
 * Per-object measures consider every configuration that contains at least one
 * pixel of the image, treating pixels outside the image as background, so
 * objects touching the border are measured as if the image were padded with
 * background. Density measures instead use only the configurations fully
 * contained in the image, normalized by the area or volume of the sampling
 * window {@code (size - 1) * spacing} along each dimension, which gives
 * unbiased estimates for a stationary random set observed through the image
 * window (Ohser and M&uuml;cklich 2000).
 * </p>
 * <p>
 * Relation to other ops: {@code geom.boundarySize} and {@code geom.size}
 * measure polygons, meshes and label regions, and the perimeter of a polygonal
 * contour overestimates that of the digitized shape;
 * {@code topology.eulerCharacteristic26N} computes the 26-connected Euler
 * characteristic of a 3D image from the same look-up table as
 * {@code features.intrinsicVolumes.eulerNumber} with connectivity 26, but only
 * over the configurations fully inside the image (it equals the latter on an
 * image padded with background, and {@code topology.eulerCorrection} accounts
 * for the border). These ops live in the {@code features.intrinsicVolumes}
 * namespace rather than {@code geom} or {@code topology} because they are
 * pixel-configuration estimators of a whole family of related measures with
 * their own parameters (directions, connectivity), operating on binary images
 * and labelings rather than on geometric objects.
 * </p>
 * <p>
 * Translated from the {@code inra.ijpb.measure} package of <a href=
 * "https://github.com/ijpb/MorphoLibJ">MorphoLibJ</a> (LGPL-3, Copyright (C)
 * 2014 - 2026 INRA) by David Legland (classes {@code IntrinsicVolumes2D},
 * {@code IntrinsicVolumes3D}, {@code InterfaceSurfaceArea} and their helpers).
 * </p>
 * <p>
 * References:
 * </p>
 * <ul>
 * <li>D. Legland, K. Ki&ecirc;u and M.-F. Devaux, "Computation of Minkowski
 * measures on 2D and 3D binary images", <i>Image Analysis and Stereology</i>
 * 26(2): 83&ndash;92, 2007.</li>
 * <li>J. Ohser and F. M&uuml;cklich, <i>Statistical Analysis of Microstructures
 * in Materials Science</i>, Wiley, 2000.</li>
 * <li>J. Ohser and K. Schladitz, <i>3D Images of Material Structures:
 * Processing and Analysis</i>, Wiley, 2009.</li>
 * <li>D. Legland, I. Arganda-Carreras and P. Andrey, "MorphoLibJ: integrated
 * library and plugins for mathematical morphology with ImageJ",
 * <i>Bioinformatics</i> 32(22): 3532&ndash;3534, 2016.</li>
 * </ul>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author David Legland
 */
public final class IntrinsicVolumes {

	private IntrinsicVolumes() {
		// Prevent instantiation of static utility class.
	}

	// -- 2D --

	/**
	 * Computes the area of the foreground of a 2D binary image.
	 *
	 * @param <B> pixel type
	 * @param image the 2D binary image
	 * @param spacing the pixel spacing along each dimension (default 1)
	 * @return the area
	 * @implNote op names='features.intrinsicVolumes.area', type=Function
	 */
	public static <B extends BooleanType<B>> Double area(
		final RandomAccessibleInterval<B> image, @Nullable double[] spacing)
	{
		final var s = spacing(image, spacing, 2);
		return measure(image, IntrinsicVolumesLuts.areaLut(s));
	}

	/**
	 * Computes the area of each label of a 2D labeling.
	 *
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @param labeling the 2D labeling
	 * @param spacing the pixel spacing along each dimension (default 1)
	 * @return the area of each label
	 * @implNote op names='features.intrinsicVolumes.area', type=Function
	 */
	public static <L, I extends IntegerType<I>> Map<L, Double> area(
		final ImgLabeling<L, I> labeling, @Nullable double[] spacing)
	{
		final var s = spacing(labeling, spacing, 2);
		return measure(labeling, IntrinsicVolumesLuts.areaLut(s));
	}

	/**
	 * Computes the perimeter of the foreground of a 2D binary image, using the
	 * Crofton formula discretized over 2 (horizontal and vertical) or 4 (plus
	 * diagonal) directions.
	 *
	 * @param <B> pixel type
	 * @param image the 2D binary image
	 * @param spacing the pixel spacing along each dimension (default 1)
	 * @param directions the number of directions, 2 or 4 (default 4)
	 * @return the perimeter
	 * @implNote op names='features.intrinsicVolumes.perimeter', type=Function
	 */
	public static <B extends BooleanType<B>> Double perimeter(
		final RandomAccessibleInterval<B> image, @Nullable double[] spacing,
		@Nullable Integer directions)
	{
		final var s = spacing(image, spacing, 2);
		return measure(image, IntrinsicVolumesLuts.perimeterLut(s, or(directions,
			4)));
	}

	/**
	 * Computes the perimeter of each label of a 2D labeling, using the Crofton
	 * formula discretized over 2 (horizontal and vertical) or 4 (plus diagonal)
	 * directions.
	 *
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @param labeling the 2D labeling
	 * @param spacing the pixel spacing along each dimension (default 1)
	 * @param directions the number of directions, 2 or 4 (default 4)
	 * @return the perimeter of each label
	 * @implNote op names='features.intrinsicVolumes.perimeter', type=Function
	 */
	public static <L, I extends IntegerType<I>> Map<L, Double> perimeter(
		final ImgLabeling<L, I> labeling, @Nullable double[] spacing,
		@Nullable Integer directions)
	{
		final var s = spacing(labeling, spacing, 2);
		return measure(labeling, IntrinsicVolumesLuts.perimeterLut(s, or(
			directions, 4)));
	}

	// -- 3D --

	/**
	 * Computes the volume of the foreground of a 3D binary image.
	 *
	 * @param <B> voxel type
	 * @param image the 3D binary image
	 * @param spacing the voxel spacing along each dimension (default 1)
	 * @return the volume
	 * @implNote op names='features.intrinsicVolumes.volume', type=Function
	 */
	public static <B extends BooleanType<B>> Double volume(
		final RandomAccessibleInterval<B> image, @Nullable double[] spacing)
	{
		final var s = spacing(image, spacing, 3);
		return measure(image, IntrinsicVolumesLuts.volumeLut(s));
	}

	/**
	 * Computes the volume of each label of a 3D labeling.
	 *
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @param labeling the 3D labeling
	 * @param spacing the voxel spacing along each dimension (default 1)
	 * @return the volume of each label
	 * @implNote op names='features.intrinsicVolumes.volume', type=Function
	 */
	public static <L, I extends IntegerType<I>> Map<L, Double> volume(
		final ImgLabeling<L, I> labeling, @Nullable double[] spacing)
	{
		final var s = spacing(labeling, spacing, 3);
		return measure(labeling, IntrinsicVolumesLuts.volumeLut(s));
	}

	/**
	 * Computes the surface area of the foreground of a 3D binary image, using
	 * the Crofton formula discretized over 3 (isothetic) or 13 (plus face and
	 * cube diagonal) directions.
	 *
	 * @param <B> voxel type
	 * @param image the 3D binary image
	 * @param spacing the voxel spacing along each dimension (default 1)
	 * @param directions the number of directions, 3 or 13 (default 13)
	 * @return the surface area
	 * @implNote op names='features.intrinsicVolumes.surfaceArea', type=Function
	 */
	public static <B extends BooleanType<B>> Double surfaceArea(
		final RandomAccessibleInterval<B> image, @Nullable double[] spacing,
		@Nullable Integer directions)
	{
		final var s = spacing(image, spacing, 3);
		return measure(image, IntrinsicVolumesLuts.surfaceAreaLut(s, or(
			directions, 13)));
	}

	/**
	 * Computes the surface area of each label of a 3D labeling, using the
	 * Crofton formula discretized over 3 (isothetic) or 13 (plus face and cube
	 * diagonal) directions.
	 *
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @param labeling the 3D labeling
	 * @param spacing the voxel spacing along each dimension (default 1)
	 * @param directions the number of directions, 3 or 13 (default 13)
	 * @return the surface area of each label
	 * @implNote op names='features.intrinsicVolumes.surfaceArea', type=Function
	 */
	public static <L, I extends IntegerType<I>> Map<L, Double> surfaceArea(
		final ImgLabeling<L, I> labeling, @Nullable double[] spacing,
		@Nullable Integer directions)
	{
		final var s = spacing(labeling, spacing, 3);
		return measure(labeling, IntrinsicVolumesLuts.surfaceAreaLut(s, or(
			directions, 13)));
	}

	/**
	 * Computes the mean breadth of the foreground of a 3D binary image, i.e.
	 * the mean over all directions of its projected diameter (proportional to
	 * the integral of the mean curvature), using the Crofton formula over the
	 * Euler numbers of planar sections in 3 or 13 directions. The mean breadth
	 * of a ball is its diameter.
	 *
	 * @param <B> voxel type
	 * @param image the 3D binary image
	 * @param spacing the voxel spacing along each dimension (default 1)
	 * @param directions the number of directions, 3 or 13 (default 13)
	 * @param connectivity the connectivity used to compute the Euler numbers of
	 *          planar sections: face connectivity (a {@code DiamondShape} of
	 *          radius 1, i.e. 4-connected sections) or full connectivity (a
	 *          {@code RectangleShape} of span 1, i.e. 8-connected sections).
	 *          Defaults to full connectivity.
	 * @return the mean breadth
	 * @implNote op names='features.intrinsicVolumes.meanBreadth', type=Function
	 */
	public static <B extends BooleanType<B>> Double meanBreadth(
		final RandomAccessibleInterval<B> image, @Nullable double[] spacing,
		@Nullable Integer directions, @Nullable Shape connectivity)
	{
		final var s = spacing(image, spacing, 3);
		return measure(image, IntrinsicVolumesLuts.meanBreadthLut(s, or(
			directions, 13), isFace(connectivity, 3) ? 4 : 8));
	}

	/**
	 * Computes the mean breadth of each label of a 3D labeling, i.e. the mean
	 * over all directions of its projected diameter, using the Crofton formula
	 * over the Euler numbers of planar sections in 3 or 13 directions.
	 *
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @param labeling the 3D labeling
	 * @param spacing the voxel spacing along each dimension (default 1)
	 * @param directions the number of directions, 3 or 13 (default 13)
	 * @param connectivity the connectivity used to compute the Euler numbers of
	 *          planar sections: face connectivity (a {@code DiamondShape} of
	 *          radius 1, i.e. 4-connected sections) or full connectivity (a
	 *          {@code RectangleShape} of span 1, i.e. 8-connected sections).
	 *          Defaults to full connectivity.
	 * @return the mean breadth of each label
	 * @implNote op names='features.intrinsicVolumes.meanBreadth', type=Function
	 */
	public static <L, I extends IntegerType<I>> Map<L, Double> meanBreadth(
		final ImgLabeling<L, I> labeling, @Nullable double[] spacing,
		@Nullable Integer directions, @Nullable Shape connectivity)
	{
		final var s = spacing(labeling, spacing, 3);
		return measure(labeling, IntrinsicVolumesLuts.meanBreadthLut(s, or(
			directions, 13), isFace(connectivity, 3) ? 4 : 8));
	}

	/**
	 * Computes the area of the interface between two labels of a 3D labeling,
	 * using the Crofton formula discretized over 3 or 13 directions, by counting
	 * the pairs of voxels of the first and second label within each
	 * 2&times;2&times;2 configuration. For two complementary regions, the
	 * result equals the surface area of either region (away from the image
	 * border). As in MorphoLibJ, only the configurations fully contained in the
	 * image are considered. Unlike MorphoLibJ, the 3-direction estimate is
	 * calibrated and consistent with {@code features.intrinsicVolumes.surfaceArea}
	 * (MorphoLibJ returns twice that value, regardless of the spacing).
	 *
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @param labeling the 3D labeling
	 * @param label1 the first label
	 * @param label2 the second label
	 * @param spacing the voxel spacing along each dimension (default 1)
	 * @param directions the number of directions, 3 or 13 (default 13)
	 * @return the interface area
	 * @implNote op names='features.intrinsicVolumes.interfaceArea', type=Function
	 */
	public static <L, I extends IntegerType<I>> Double interfaceArea(
		final ImgLabeling<L, I> labeling, final L label1, final L label2,
		@Nullable double[] spacing, @Nullable Integer directions)
	{
		final var s = spacing(labeling, spacing, 3);
		final var contribs = IntrinsicVolumesLuts.interfaceContributions(s, or(
			directions, 13));
		return ConfigurationHistograms.interfaceSum(labeling, label1, label2,
			contribs);
	}

	// -- 2D and 3D --

	/**
	 * Computes the Euler number of the foreground of a 2D or 3D binary image:
	 * in 2D, the number of connected components minus the number of holes; in
	 * 3D, the number of connected components minus the number of tunnels
	 * (handles) plus the number of cavities.
	 *
	 * @param <B> pixel type
	 * @param image the 2D or 3D binary image
	 * @param connectivity the connectivity of the foreground: face connectivity
	 *          (a {@code DiamondShape} of radius 1, i.e. 4-connectivity in 2D
	 *          and 6-connectivity in 3D) or full connectivity (a
	 *          {@code RectangleShape} of span 1, i.e. 8-connectivity in 2D and
	 *          26-connectivity in 3D). Defaults to full connectivity.
	 * @return the Euler number
	 * @implNote op names='features.intrinsicVolumes.eulerNumber', type=Function
	 */
	public static <B extends BooleanType<B>> Double eulerNumber(
		final RandomAccessibleInterval<B> image, @Nullable Shape connectivity)
	{
		return measure(image, eulerLut(image, connectivity));
	}

	/**
	 * Computes the Euler number of each label of a 2D or 3D labeling: in 2D, the
	 * number of connected components minus the number of holes; in 3D, the
	 * number of connected components minus the number of tunnels (handles) plus
	 * the number of cavities.
	 *
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @param labeling the 2D or 3D labeling
	 * @param connectivity the connectivity of the labels: face connectivity (a
	 *          {@code DiamondShape} of radius 1, i.e. 4-connectivity in 2D and
	 *          6-connectivity in 3D) or full connectivity (a
	 *          {@code RectangleShape} of span 1, i.e. 8-connectivity in 2D and
	 *          26-connectivity in 3D). Defaults to full connectivity.
	 * @return the Euler number of each label
	 * @implNote op names='features.intrinsicVolumes.eulerNumber', type=Function
	 */
	public static <L, I extends IntegerType<I>> Map<L, Double> eulerNumber(
		final ImgLabeling<L, I> labeling, @Nullable Shape connectivity)
	{
		return measure(labeling, eulerLut(labeling, connectivity));
	}

	/**
	 * Computes the densities of the intrinsic volumes of the foreground of a 2D
	 * or 3D binary image, considered as a sample of a stationary random set
	 * (microstructure analysis), i.e. the measures per unit area or volume of
	 * the sampling window. Only the configurations fully contained in the image
	 * are considered, which avoids edge effects. The image must therefore have
	 * a size of at least 2 along each dimension; an image with a dimension of
	 * size 1 (e.g. a one-slice 3D stack) is rejected with an
	 * {@link IllegalArgumentException}.
	 *
	 * @param <B> pixel type
	 * @param image the 2D or 3D binary image
	 * @param spacing the pixel spacing along each dimension (default 1)
	 * @param directions the number of directions of the Crofton formula: 2 or 4
	 *          in 2D (default 4), 3 or 13 in 3D (default 13)
	 * @param connectivity the connectivity of the foreground for the Euler
	 *          number: face connectivity (a {@code DiamondShape} of radius 1,
	 *          i.e. 4-connectivity in 2D and 6-connectivity in 3D) or full
	 *          connectivity (a {@code RectangleShape} of span 1, i.e.
	 *          8-connectivity in 2D and 26-connectivity in 3D). Defaults to full
	 *          connectivity.
	 * @return in 2D, a map with the entries {@code areaDensity} (the area
	 *         fraction), {@code perimeterDensity} and
	 *         {@code eulerNumberDensity}; in 3D, a map with the entries
	 *         {@code volumeDensity} (the volume fraction),
	 *         {@code surfaceAreaDensity}, {@code meanBreadthDensity} (with
	 *         8-connected planar sections) and {@code eulerNumberDensity}
	 * @implNote op names='features.intrinsicVolumes.densities', type=Function
	 */
	public static <B extends BooleanType<B>> Map<String, Double> densities(
		final RandomAccessibleInterval<B> image, @Nullable double[] spacing,
		@Nullable Integer directions, @Nullable Shape connectivity)
	{
		final var nd = image.numDimensions();
		for (var d = 0; d < nd; d++) {
			if (image.dimension(d) < 2) {
				throw new IllegalArgumentException("Dimension " + d +
					" has size 1, so no configuration lies inside the image; drop it" +
					" (e.g. with Views.dropSingletonDimensions) to measure a " + (nd -
						1) + "D image");
			}
		}
		final var s = spacing(image, spacing, nd);
		final var hist = ConfigurationHistograms.histogram(image, true);
		var window = 1.0;
		for (var d = 0; d < nd; d++)
			window *= (image.dimension(d) - 1) * s[d];
		final var eulerLut = eulerLut(image, connectivity);
		final var result = new LinkedHashMap<String, Double>();
		if (nd == 2) {
			final var nDirs = or(directions, 4);
			result.put("areaDensity", ConfigurationHistograms.apply(hist,
				IntrinsicVolumesLuts.areaLut(s)) / window);
			result.put("perimeterDensity", ConfigurationHistograms.apply(hist,
				IntrinsicVolumesLuts.perimeterLut(s, nDirs)) / window);
		}
		else {
			final var nDirs = or(directions, 13);
			result.put("volumeDensity", ConfigurationHistograms.apply(hist,
				IntrinsicVolumesLuts.volumeLut(s)) / window);
			result.put("surfaceAreaDensity", ConfigurationHistograms.apply(hist,
				IntrinsicVolumesLuts.surfaceAreaLut(s, nDirs)) / window);
			result.put("meanBreadthDensity", ConfigurationHistograms.apply(hist,
				IntrinsicVolumesLuts.meanBreadthLut(s, nDirs, 8)) / window);
		}
		result.put("eulerNumberDensity", ConfigurationHistograms.apply(hist,
			eulerLut) / window);
		return result;
	}

	// -- Helper methods --

	private static <B extends BooleanType<B>> double measure(
		final RandomAccessibleInterval<B> image, final double[] lut)
	{
		return ConfigurationHistograms.apply(ConfigurationHistograms.histogram(
			image, false), lut);
	}

	private static <L, I extends IntegerType<I>> Map<L, Double> measure(
		final ImgLabeling<L, I> labeling, final double[] lut)
	{
		return ConfigurationHistograms.apply(ConfigurationHistograms.histograms(
			labeling), lut);
	}

	private static double[] eulerLut(final Interval interval,
		final Shape connectivity)
	{
		switch (interval.numDimensions()) {
			case 2:
				return IntrinsicVolumesLuts.eulerNumberLut2D(isFace(connectivity, 2)
					? 4 : 8);
			case 3:
				return IntrinsicVolumesLuts.eulerNumberLut3D(isFace(connectivity, 3)
					? 6 : 26);
			default:
				throw new IllegalArgumentException(
					"Only 2D and 3D images are supported, not " + interval
						.numDimensions() + "D");
		}
	}

	private static double[] spacing(final Interval interval,
		final double[] spacing, final int nd)
	{
		if (interval.numDimensions() != nd) {
			throw new IllegalArgumentException("Expected a " + nd +
				"D image, not " + interval.numDimensions() + "D");
		}
		if (spacing == null) {
			final var ones = new double[nd];
			Arrays.fill(ones, 1);
			return ones;
		}
		if (spacing.length != nd) {
			throw new IllegalArgumentException("Expected " + nd +
				" spacing values, not " + spacing.length);
		}
		return spacing;
	}

	/**
	 * Tells whether a connectivity is face connectivity (true) or full
	 * connectivity (false, also for null), by enumerating its neighborhood in
	 * {@code n} dimensions.
	 *
	 * @throws IllegalArgumentException if it is neither
	 */
	private static boolean isFace(final Shape connectivity, final int n) {
		if (connectivity == null) return false;
		final var source = ConstantUtils.constantRandomAccessible(new BitType(),
			n);
		final var ra = connectivity.neighborhoodsRandomAccessible(source)
			.randomAccess();
		ra.setPosition(new long[n]);
		final var c = ra.get().localizingCursor();
		final var offsets = new HashSet<List<Integer>>();
		var face = true;
		var full = true;
		while (c.hasNext()) {
			c.fwd();
			final var o = new ArrayList<Integer>(n);
			var nonzero = 0;
			for (var d = 0; d < n; d++) {
				final var v = c.getIntPosition(d);
				o.add(v);
				if (v != 0) nonzero++;
				if (Math.abs(v) > 1) full = false;
			}
			if (nonzero == 0) continue;
			if (nonzero != 1) face = false;
			offsets.add(o);
		}
		if (face && offsets.size() == 2 * n) return true;
		if (full && offsets.size() == (int) Math.pow(3, n) - 1) return false;
		throw new IllegalArgumentException("Unsupported connectivity: expected " +
			"face connectivity (a DiamondShape of radius 1) or full connectivity " +
			"(a RectangleShape of span 1), not " + connectivity);
	}

	private static int or(final Integer value, final int defaultValue) {
		return value == null ? defaultValue : value;
	}

}
