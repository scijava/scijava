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

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Morphological segmentation: a marker-controlled watershed seeded with the
 * extended minima of the image, in any number of dimensions.
 * <p>
 * The extended minima of the relief (the minima whose dynamic exceeds
 * {@code dynamic}, see {@code morphology.extendedMinima}) are labeled as
 * connected components, imposed as the only minima of the relief (see
 * {@code morphology.imposeMinima}), and the result is flooded from them (see
 * {@code segment.compactWatershed}). The dynamic thus controls which basins are
 * deep enough to become regions of their own, which is a much more robust
 * criterion than the depth of single minima. This is the classical
 * marker-controlled watershed pipeline of F. Meyer and S. Beucher,
 * "Morphological segmentation", Journal of Visual Communication and Image
 * Representation 1(1), 21-46, 1990; see also P. Soille, <em>Morphological
 * Image Analysis</em>, 2nd ed., Springer, 2003, chapter 9. It is typically
 * applied to a gradient image (see {@code morphology.gradient}) to segment
 * regions of homogeneous intensity, or to an image whose objects are dark.
 * </p>
 *
 * @author Curtis Rueden
 * @param <T> element type of the input image
 * @param <B> element type of the mask
 * @implNote op names='segment.watershedExtendedMinima'
 */
public class ExtendedMinimaWatershed<T extends RealType<T>, B extends BooleanType<B>>
	implements
	Functions.Arity6<RandomAccessibleInterval<T>, Double, Shape, Boolean, RandomAccessibleInterval<B>, Double, ImgLabeling<Integer, IntType>>
{

	@OpDependency(name = "morphology.extendedMinima")
	private Computers.Arity3<RandomAccessibleInterval<DoubleType>, Double, Shape, RandomAccessibleInterval<BitType>> extendedMinima;

	@OpDependency(name = "morphology.imposeMinima")
	private Computers.Arity3<RandomAccessibleInterval<DoubleType>, RandomAccessibleInterval<BitType>, Shape, RandomAccessibleInterval<DoubleType>> imposeMinima;

	@OpDependency(name = "create.imgLabeling")
	private BiFunction<Dimensions, IntType, ImgLabeling<Integer, IntType>> labelingCreator;

	/**
	 * Computes the extended-minima watershed of an image.
	 *
	 * @param input the relief to segment, typically a gradient image
	 * @param dynamic the (non-negative) minimal dynamic of the minima seeding a
	 *          region: the depth of a basin below the lowest saddle leading to a
	 *          deeper basin, in intensity units
	 * @param connectivity the neighborhood defining which pixels are adjacent,
	 *          such as a {@code DiamondShape} of radius 1 for face connectivity
	 *          (4-connectivity in 2D, 6-connectivity in 3D) or a
	 *          {@code RectangleShape} of span 1 for full connectivity
	 *          (8-connectivity in 2D, 26-connectivity in 3D). Defaults to full
	 *          connectivity.
	 * @param drawWatersheds if true, pixels where two regions meet are left
	 *          unlabeled, forming watershed lines; otherwise every reachable
	 *          pixel is labeled. Defaults to true.
	 * @param mask a binary image, of the same dimensions as {@code input},
	 *          restricting the segmentation to its true pixels. Defaults to the
	 *          whole image.
	 * @param compactness the (non-negative) weight of the distance to the
	 *          marker in the flooding priority (see
	 *          {@code segment.compactWatershed}). Defaults to 0, the classical
	 *          watershed.
	 * @return a labeling with one label per region, numbered from 1 in raster
	 *         order of the extended minima; watershed lines and pixels outside
	 *         the mask have no label
	 */
	@Override
	public ImgLabeling<Integer, IntType> apply( //
		final RandomAccessibleInterval<T> input, //
		final Double dynamic, //
		@Nullable Shape connectivity, //
		@Nullable final Boolean drawWatersheds, //
		@Nullable final RandomAccessibleInterval<B> mask, //
		@Nullable final Double compactness //
	) {
		if (connectivity == null) connectivity = new RectangleShape(1, false);
		final var grid = new Flooding(input, connectivity);
		final var inside = grid.flags(mask);
		final var values = grid.values(input);

		// Note: Raising the pixels outside the mask to the highest value inside
		// it keeps them from creating minima or connecting basins.
		if (inside != null) {
			var max = Double.NEGATIVE_INFINITY;
			for (var i = 0; i < grid.size; i++)
				if (inside[i]) max = Math.max(max, values[i]);
			for (var i = 0; i < grid.size; i++)
				if (!inside[i]) values[i] = max;
		}
		final var relief = ArrayImgs.doubles(values, input
			.dimensionsAsLongArray());

		final var minima = ArrayImgs.bits(input.dimensionsAsLongArray());
		extendedMinima.compute(relief, dynamic, connectivity, minima);
		final var imposed = ArrayImgs.doubles(input.dimensionsAsLongArray());
		imposeMinima.compute(relief, minima, connectivity, imposed);

		final var markerFlags = grid.flags(minima);
		if (inside != null) {
			for (var i = 0; i < grid.size; i++)
				markerFlags[i] &= inside[i];
		}
		final var labels = grid.flood(grid.values(imposed), grid.components(
			markerFlags), inside, compactness == null ? 0 : compactness,
			drawWatersheds == null || drawWatersheds);
		final var output = labelingCreator.apply(input, new IntType());
		grid.write(labels, null, output);
		return output;
	}
}
