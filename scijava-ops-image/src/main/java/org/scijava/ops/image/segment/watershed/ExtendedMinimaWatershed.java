/* #%L
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

package org.scijava.ops.image.segment.watershed;

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
 * @author Claude Opus 5.5
 * @param <T> element type of the input image
 * @param <B> element type of the mask
 * @implNote op names='segment.watershedExtendedMinima'
 */
public class ExtendedMinimaWatershed<T extends RealType<T>, B extends BooleanType<B>>
	implements
	Functions.Arity7<RandomAccessibleInterval<T>, Double, Shape, Boolean, RandomAccessibleInterval<B>, Double, Integer, ImgLabeling<Integer, IntType>>
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
	 * @param drawWatersheds if true, pixels where two regions meet form
	 *          watershed lines, which get the {@code watershedLabel} (or no
	 *          label); otherwise every reachable pixel gets the label of a
	 *          region. Defaults to true.
	 * @param mask a binary image, of the same dimensions as {@code input},
	 *          restricting the segmentation to its true pixels. Defaults to the
	 *          whole image.
	 * @param compactness the (non-negative) weight of the distance to the
	 *          marker in the flooding priority (see
	 *          {@code segment.compactWatershed}). Defaults to 0, the classical
	 *          watershed.
	 * @param watershedLabel the label to give the watershed line pixels when
	 *          {@code drawWatersheds} is true, such as 0 or -1; it must not be
	 *          the label of a region. Ignored when {@code drawWatersheds} is
	 *          false. Defaults to none: watershed lines are left unlabeled.
	 * @return a labeling with one label per region, numbered from 1 in raster
	 *         order of the extended minima; watershed lines have the
	 *         {@code watershedLabel} (or no label), and pixels outside the mask
	 *         have no label
	 */
	@Override
	public ImgLabeling<Integer, IntType> apply( //
		final RandomAccessibleInterval<T> input, //
		final Double dynamic, //
		@Nullable Shape connectivity, //
		@Nullable final Boolean drawWatersheds, //
		@Nullable final RandomAccessibleInterval<B> mask, //
		@Nullable final Double compactness, //
		@Nullable final Integer watershedLabel //
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
		grid.write(labels, null, watershedLabel, output);
		return output;
	}
}
