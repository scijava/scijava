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

package org.scijava.ops.image.threshold.localPhansalkar;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleNeighborhood;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.composite.Composite;

import org.scijava.function.Computers;
import org.scijava.ops.image.threshold.Thresholds;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * <p>
 * Local thresholding algorithm as proposed by Phansalkar et al., for an image
 * normalized to [0, 1], with the population standard deviation (see
 * {@link ComputeLocalPhansalkarThreshold}).
 * </p>
 * <p>
 * This implementation improves execution speed by using integral images for the
 * computations of mean and standard deviation in the local windows. A
 * significant improvement can be observed for increased window sizes (
 * {@code span > 10}). It operates on {@link RandomAccessibleInterval}s of
 * {@link RealType}, i.e. explicit conversion to an integral image is <b>not</b>
 * required.
 * </p>
 *
 * @see ComputeLocalPhansalkarThreshold
 * @author Stefan Helfrich (University of Konstanz)
 * @implNote op name='threshold.localPhansalkar', priority='-101.'
 */
public class ComputeLocalPhansalkarThresholdIntegral<T extends RealType<T>>
	implements
	Computers.Arity4<RectangleNeighborhood<? extends Composite<DoubleType>>, T, Double, Double, BitType>
{

	@OpDependency(name = "stats.integralMean")
	private Computers.Arity1<RectangleNeighborhood<? extends Composite<DoubleType>>, DoubleType> integralMeanOp;

	@OpDependency(name = "stats.integralVariance")
	private Computers.Arity1<RectangleNeighborhood<? extends Composite<DoubleType>>, DoubleType> integralVarianceOp;

	/**
	 * Applies Phansalkar's rule to one pixel of an image normalized to [0, 1].
	 *
	 * @param inputNeighborhood the neighborhood of the pixel in the integral
	 *          images of order 1 and 2
	 * @param inputCenterPixel the pixel
	 * @param k the k parameter (default: 0.25)
	 * @param r the r parameter (default: 0.5)
	 * @param output true if the pixel is foreground
	 */
	@Override
	public void compute(
		final RectangleNeighborhood<? extends Composite<DoubleType>> inputNeighborhood,
		final T inputCenterPixel, @Nullable Double k, @Nullable Double r,
		final BitType output)
	{
		if (k == null) k = ComputeLocalPhansalkarThreshold.DEFAULT_K;
		if (r == null) r = ComputeLocalPhansalkarThreshold.DEFAULT_R;

		final var mean = new DoubleType();
		integralMeanOp.compute(inputNeighborhood, mean);

		final var variance = new DoubleType();
		integralVarianceOp.compute(inputNeighborhood, variance);
		final var n = Intervals.numElements(Intervals.expand(inputNeighborhood,
			-1));

		output.set(ComputeLocalPhansalkarThreshold.isForeground(inputCenterPixel
			.getRealDouble(), mean.get(), Math.sqrt(Thresholds.populationVariance(
				variance.get(), n)), k, r));
	}

}
