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

package org.scijava.ops.image.segment.watershed;

import java.util.ArrayList;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Compact watershed: a marker-controlled watershed whose regions are pushed
 * towards compact shapes, in any number of dimensions.
 * <p>
 * The image is flooded from the markers as in the classical marker-controlled
 * watershed (F. Meyer, "Un algorithme optimal pour la ligne de partage des
 * eaux", 8e congres AFCET, 1991), except that the flooding priority of a pixel
 * is its value plus {@code compactness} times its Euclidean distance (in
 * pixels) to the marker pixel its flooding path started from, as proposed by P.
 * Neubert and P. Protzel, "Compact watershed and preemptive SLIC: On improving
 * trade-offs of superpixel segmentation algorithms", ICPR 2014. A compactness
 * of 0 gives the classical watershed; as it grows, the result tends to the
 * Voronoi partition of the markers. Pixels of equal priority are flooded in the
 * order in which they were reached.
 * </p>
 * <p>
 * Unlike {@code image.watershed}, which decides the label of a pixel only when
 * it is flooded, each queued pixel here carries the label (and marker pixel) of
 * the region which reached it, which is what makes a distance-dependent
 * priority possible.
 * </p>
 *
 * @author Curtis Rueden
 * @param <T> element type of the input image
 * @param <B> element type of the mask
 * @implNote op names='segment.compactWatershed'
 */
public class CompactWatershed<T extends RealType<T>, B extends BooleanType<B>>
	implements
	Functions.Arity6<RandomAccessibleInterval<T>, ImgLabeling<Integer, IntType>, Double, Shape, Boolean, RandomAccessibleInterval<B>, ImgLabeling<Integer, IntType>>
{

	@OpDependency(name = "create.imgLabeling")
	private BiFunction<Dimensions, IntType, ImgLabeling<Integer, IntType>> labelingCreator;

	/**
	 * Computes the compact watershed of an image.
	 *
	 * @param input the relief to flood, typically a gradient image
	 * @param markers the markers, of the same dimensions as {@code input}, with
	 *          at most one label per pixel; each label defines one region
	 * @param compactness the (non-negative) weight of the distance to the marker
	 *          in the flooding priority, in intensity units per pixel; 0 gives
	 *          the classical marker-controlled watershed
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
	 *          restricting the flooding to its true pixels; markers outside it
	 *          are ignored. Defaults to the whole image.
	 * @return a labeling with the label of the marker of each region; watershed
	 *         lines and pixels outside the mask have no label
	 */
	@Override
	public ImgLabeling<Integer, IntType> apply( //
		final RandomAccessibleInterval<T> input, //
		final ImgLabeling<Integer, IntType> markers, //
		final Double compactness, //
		@Nullable final Shape connectivity, //
		@Nullable final Boolean drawWatersheds, //
		@Nullable final RandomAccessibleInterval<B> mask //
	) {
		final var grid = new Flooding(input, connectivity);
		final var ids = new ArrayList<Integer>();
		final var labels = grid.flood(grid.values(input), grid.markers(markers,
			ids), grid.flags(mask), compactness, drawWatersheds == null ||
				drawWatersheds);
		final var output = labelingCreator.apply(input, new IntType());
		grid.write(labels, ids, output);
		return output;
	}
}
