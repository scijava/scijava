/*-
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

package org.scijava.ops.image.segment.graphCut;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Binary segmentation of an n-dimensional image by a minimum graph cut, with a
 * data term given as cost images.
 * <p>
 * Minimizes {@code sum_p D_p(L_p) + sum_{p,q} w_pq [L_p != L_q]}, where
 * {@code D_p(foreground)} and {@code D_p(background)} are read from the cost
 * images, typically negative log-likelihoods of the labels (e.g.
 * {@code -ln P(foreground)} and {@code -ln(1 - P(foreground))} from a
 * probability map), and {@code w_pq} is the smoothness weight {@code lambda},
 * optionally reduced across the edges of a contrast image. The labeling of
 * minimal energy is found exactly with the min-cut/max-flow algorithm of
 * Boykov &amp; Kolmogorov (2004); see {@link GridGraphCut} for the energy and
 * {@link BKMaxFlow} for the algorithm. Only the difference of the two costs
 * of a pixel matters, so the costs may be negative. With {@code lambda = 0},
 * a pixel is foreground if and only if its foreground cost is lower than its
 * background cost.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> the type of the foreground costs
 * @param <U> the type of the background costs
 * @param <V> the type of the contrast image
 * @implNote op names='segment.graphCut'
 */
public class GraphCutCosts<T extends RealType<T>, U extends RealType<U>, V extends RealType<V>>
	implements
	Functions.Arity5<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, Double, RandomAccessibleInterval<V>, Shape, RandomAccessibleInterval<BitType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, BitType, RandomAccessibleInterval<BitType>> createOp;

	/**
	 * Segments an image given its label costs.
	 *
	 * @param foregroundCost the cost of labeling each pixel foreground
	 * @param backgroundCost the cost of labeling each pixel background, of the
	 *          same size
	 * @param lambda the weight of the smoothness term (default 1); 0 gives a
	 *          pixelwise segmentation
	 * @param contrastImage if given, the smoothness term is weakened across its
	 *          edges, by the factor exp(-beta (I_p - I_q)^2) with beta the
	 *          inverse of twice the mean squared neighbor difference (default:
	 *          no contrast term)
	 * @param connectivity the neighborhood defining which pixels are adjacent,
	 *          such as a {@code DiamondShape} of radius 1 for face connectivity
	 *          (4-connectivity in 2D, 6-connectivity in 3D) or a
	 *          {@code RectangleShape} of span 1 for full connectivity
	 *          (8-connectivity in 2D, 26-connectivity in 3D), with the weight
	 *          of each pair divided by its distance. Defaults to full
	 *          connectivity. Full connectivity makes boundaries costlier than
	 *          face connectivity, by a factor of about 2.4 in 2D and 6 in 3D, so
	 *          it calls for a smaller lambda.
	 * @return the foreground mask
	 */
	@Override
	public RandomAccessibleInterval<BitType> apply(
		final RandomAccessibleInterval<T> foregroundCost,
		final RandomAccessibleInterval<U> backgroundCost, @Nullable Double lambda,
		@Nullable RandomAccessibleInterval<V> contrastImage,
		@Nullable Shape connectivity)
	{
		if (lambda == null) lambda = 1.0;
		GridGraphCut.checkDimensions(foregroundCost, backgroundCost);
		if (contrastImage != null) GridGraphCut.checkDimensions(foregroundCost,
			contrastImage);

		final var foreground = GridGraphCut.segment(foregroundCost
			.dimensionsAsLongArray(), GridGraphCut.flatten(foregroundCost),
			GridGraphCut.flatten(backgroundCost), contrastImage == null ? null
				: GridGraphCut.flatten(contrastImage), lambda, connectivity);
		return GridGraphCut.mask(createOp, foregroundCost, foreground);
	}
}
