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

package org.scijava.ops.image.lgpl.segment.watershed;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Distance transform watershed: splits touching objects of a binary image, in
 * any number of dimensions.
 * <p>
 * The exact Euclidean distance transform of the foreground (see
 * {@code image.distanceTransform}) is negated, so that the centers of the
 * objects become basins, and segmented by an extended-minima watershed (see
 * {@code segment.watershedExtendedMinima}) restricted to the foreground. Two
 * touching objects are separated if the distance at the neck between them is
 * lower than the distance at the center of the smaller object by more than
 * {@code dynamic}.
 * </p>
 * <p>
 * This differs from the binary variant of {@code image.watershed}, which
 * smooths the inverted distance transform with a Gaussian filter and floods
 * from all of its regional minima: there, the amount of splitting is controlled
 * only indirectly by the Gaussian sigma (which also blurs the shape of the
 * relief and thus moves the separating lines), whereas here it is controlled
 * directly by the depth of the necks in distance units, and the distances
 * themselves are not altered. See P. Soille, <em>Morphological
 * Image Analysis</em>, 2nd ed., Springer, 2003, section 9.4.
 * </p>
 *
 * @author Curtis Rueden
 * @param <B> element type of the input image
 * @implNote op names='segment.distanceTransformWatershed'
 */
public class DistanceTransformWatershed<B extends BooleanType<B>> implements
	Functions.Arity5<RandomAccessibleInterval<B>, Double, Shape, Boolean, double[], ImgLabeling<Integer, IntType>>
{

	@OpDependency(name = "image.distanceTransform")
	private Computers.Arity1<RandomAccessibleInterval<B>, RandomAccessibleInterval<FloatType>> distanceTransform;

	@OpDependency(name = "image.distanceTransform")
	private Computers.Arity2<RandomAccessibleInterval<B>, double[], RandomAccessibleInterval<FloatType>> calibratedDistanceTransform;

	@OpDependency(name = "segment.watershedExtendedMinima")
	private Functions.Arity6<RandomAccessibleInterval<FloatType>, Double, Shape, Boolean, RandomAccessibleInterval<B>, Double, ImgLabeling<Integer, IntType>> watershed;

	/**
	 * Computes the distance transform watershed of a binary image.
	 *
	 * @param input the binary image whose foreground objects to split
	 * @param dynamic the (non-negative) minimal depth, in distance units, of the
	 *          distance basin of an object for it to be split from its
	 *          neighbors. Defaults to 1.
	 * @param connectivity the neighborhood defining which pixels are adjacent,
	 *          such as a {@code DiamondShape} of radius 1 for face connectivity
	 *          (4-connectivity in 2D, 6-connectivity in 3D) or a
	 *          {@code RectangleShape} of span 1 for full connectivity
	 *          (8-connectivity in 2D, 26-connectivity in 3D). Defaults to full
	 *          connectivity.
	 * @param drawWatersheds if true, pixels where two objects meet are left
	 *          unlabeled, separating them by watershed lines; otherwise every
	 *          foreground pixel is labeled. Defaults to true.
	 * @param calibration the pixel spacing along each dimension, used by the
	 *          distance transform. Defaults to 1 along every dimension.
	 * @return a labeling with one label per object, numbered from 1; the
	 *         background and watershed lines have no label
	 */
	@Override
	public ImgLabeling<Integer, IntType> apply( //
		final RandomAccessibleInterval<B> input, //
		@Nullable final Double dynamic, //
		@Nullable final Shape connectivity, //
		@Nullable final Boolean drawWatersheds, //
		@Nullable final double[] calibration //
	) {
		final var distances = ArrayImgs.floats(input.dimensionsAsLongArray());
		if (calibration == null) distanceTransform.compute(input, distances);
		else calibratedDistanceTransform.compute(input, calibration, distances);
		for (final var t : distances)
			t.set(-t.get());
		return watershed.apply(distances, dynamic == null ? 1.0 : dynamic,
			connectivity, drawWatersheds, input, 0.0);
	}
}
