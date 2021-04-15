/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops2.threshold.localNiblack;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleNeighborhood;
import net.imglib2.converter.Converter;
import net.imglib2.converter.RealDoubleConverter;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.composite.Composite;

import org.scijava.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.functions.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * <p>
 * Niblack's local thresholding algorithm.
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
 */
@Plugin(type = Op.class, name = "threshold.localNiblack",
	priority = Priority.LOW - 1)
@Parameter(key = "inputNeighborhood")
@Parameter(key = "inputCenterPixel")
@Parameter(key = "c")
@Parameter(key = "k")
@Parameter(key = "output")
public class ComputeLocalNiblackThresholdIntegral<T extends RealType<T>, U extends RealType<U>>
	implements
	Computers.Arity4<RectangleNeighborhood<? extends Composite<U>>, T, Double, Double, BitType>
{

	@OpDependency(name = "stats.integralMean")
	private Computers.Arity1<RectangleNeighborhood<? extends Composite<U>>, DoubleType> integralMeanOp;

	@OpDependency(name = "stats.integralVariance")
	private Computers.Arity1<RectangleNeighborhood<? extends Composite<U>>, DoubleType> integralVarianceOp;

	@Override
	public void compute(
		final RectangleNeighborhood<? extends Composite<U>> inputNeighborhood,
		final T inputCenterPixel, final Double c, final Double k,
		final BitType output)
	{
		final DoubleType threshold = new DoubleType(0.0d);

		final DoubleType mean = new DoubleType();
		integralMeanOp.compute(inputNeighborhood, mean);

		threshold.add(mean);

		final DoubleType variance = new DoubleType();
		integralVarianceOp.compute(inputNeighborhood, variance);

		final DoubleType stdDev = new DoubleType(Math.sqrt(variance.get()));
		stdDev.mul(k);

		threshold.add(stdDev);

		// Subtract the contrast
		threshold.sub(new DoubleType(c));

		// Set value
		final Converter<T, DoubleType> conv = new RealDoubleConverter<>();
		final DoubleType centerPixelAsDoubleType = variance; // NB: Reuse
		// DoubleType
		conv.convert(inputCenterPixel, centerPixelAsDoubleType);

		output.set(centerPixelAsDoubleType.compareTo(threshold) > 0);
	}

}
