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

package org.scijava.ops.image.threshold.localSauvola;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleNeighborhood;
import net.imglib2.converter.Converter;
import net.imglib2.converter.RealDoubleConverter;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.composite.Composite;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.Nullable;

/**
 * <p>
 * Local thresholding algorithm as proposed by Sauvola et al.
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
 * @see ComputeLocalSauvolaThreshold
 * @author Stefan Helfrich (University of Konstanz)
 * @implNote op name='threshold.localSauvola', priority='-101.'
 */
public class ComputeLocalSauvolaThresholdIntegral<T extends RealType<T>>
	implements
	Computers.Arity4<RectangleNeighborhood<? extends Composite<DoubleType>>, T, Double, Double, BitType>
{

	public static final double DEFAULT_K = 0.5;
	public static final double DEFAULT_R = 0.5;

	@OpDependency(name = "stats.integralMean")
	private Computers.Arity1<RectangleNeighborhood<? extends Composite<DoubleType>>, DoubleType> integralMeanOp;

	@OpDependency(name = "stats.integralVariance")
	private Computers.Arity1<RectangleNeighborhood<? extends Composite<DoubleType>>, DoubleType> integralVarianceOp;

	/**
	 * TODO
	 *
	 * @param inputNeighborhood
	 * @param inputCenterPixel
	 * @param k
	 * @param r
	 * @param output
	 */
	@Override
	public void compute(
		final RectangleNeighborhood<? extends Composite<DoubleType>> inputNeighborhood,
		final T inputCenterPixel, @Nullable Double k, @Nullable Double r,
		final BitType output)
	{
		if (k == null) k = DEFAULT_K;
		if (r == null) r = DEFAULT_R;

		final var mean = new DoubleType();
		integralMeanOp.compute(inputNeighborhood, mean);

		final var variance = new DoubleType();
		integralVarianceOp.compute(inputNeighborhood, variance);

		final var stdDev = new DoubleType(Math.sqrt(variance.get()));

		final var threshold = new DoubleType(mean.getRealDouble() * (1.0d +
			k * ((Math.sqrt(stdDev.getRealDouble()) / r) - 1.0)));

		// Set value
		final Converter<T, DoubleType> conv = new RealDoubleConverter<>();
		final var centerPixelAsDoubleType = variance; // NB: Reuse
		// DoubleType
		conv.convert(inputCenterPixel, centerPixelAsDoubleType);

		output.set(centerPixelAsDoubleType.compareTo(threshold) > 0);
	}

}
