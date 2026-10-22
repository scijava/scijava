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

package org.scijava.ops.image.threshold.localNiblack;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleNeighborhood;
import net.imglib2.converter.Converter;
import net.imglib2.converter.RealDoubleConverter;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.composite.Composite;

import org.scijava.function.Computers;
import org.scijava.ops.image.threshold.Thresholds;
import org.scijava.ops.spi.OpDependency;

/**
 * <p>
 * Niblack's local thresholding algorithm, with the population standard
 * deviation (see {@link ComputeLocalNiblackThreshold}).
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
 * @see ComputeLocalNiblackThreshold
 * @author Stefan Helfrich (University of Konstanz)
 * @implNote op name='threshold.localNiblack', priority='-101.'
 */
public class ComputeLocalNiblackThresholdIntegral<T extends RealType<T>, U extends RealType<U>>
	implements
	Computers.Arity4<RectangleNeighborhood<? extends Composite<U>>, T, Double, Double, BitType>
{

	@OpDependency(name = "stats.integralMean")
	private Computers.Arity1<RectangleNeighborhood<? extends Composite<U>>, DoubleType> integralMeanOp;

	@OpDependency(name = "stats.integralVariance")
	private Computers.Arity1<RectangleNeighborhood<? extends Composite<U>>, DoubleType> integralVarianceOp;

	/**
	 * Applies Niblack's rule to one pixel.
	 *
	 * @param inputNeighborhood the neighborhood of the pixel in the integral
	 *          images of order 1 and 2
	 * @param inputCenterPixel the pixel
	 * @param c the constant subtracted from the threshold (Fiji default: 0)
	 * @param k the weight of the standard deviation (Fiji default: 0.2 for
	 *          bright objects)
	 * @param output true if the pixel is foreground
	 */
	@Override
	public void compute(
		final RectangleNeighborhood<? extends Composite<U>> inputNeighborhood,
		final T inputCenterPixel, final Double c, final Double k,
		final BitType output)
	{
		final var threshold = new DoubleType(0.0d);

		final var mean = new DoubleType();
		integralMeanOp.compute(inputNeighborhood, mean);

		threshold.add(mean);

		final var variance = new DoubleType();
		integralVarianceOp.compute(inputNeighborhood, variance);

		final var n = Intervals.numElements(Intervals.expand(inputNeighborhood,
			-1));
		final var stdDev = new DoubleType(Math.sqrt(Thresholds.populationVariance(
			variance.get(), n)));
		stdDev.mul(k);

		threshold.add(stdDev);

		// Subtract the contrast
		threshold.sub(new DoubleType(c));

		// Set value
		final Converter<T, DoubleType> conv = new RealDoubleConverter<>();
		final var centerPixelAsDoubleType = variance; // NB: Reuse
		// DoubleType
		conv.convert(inputCenterPixel, centerPixelAsDoubleType);

		output.set(centerPixelAsDoubleType.compareTo(threshold) > 0);
	}

}
