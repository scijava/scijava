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

package net.imagej.ops.threshold.localMean;

import net.imagej.ops.stats.IntegralMean;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleNeighborhood;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.composite.Composite;

import org.scijava.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Computers;
import org.scijava.param.Mutable;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * <p>
 * Local threshold method that uses the {@link IntegralMean} for the threshold
 * computation.
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
 * @see ComputeLocalMeanThreshold
 * @author Stefan Helfrich (University of Konstanz)
 */
@Plugin(type = Op.class, name = "threshold.localMean", priority = Priority.LOW -
	1)
@Parameter(key = "inputNeighborhood")
@Parameter(key = "inputCenterPixel")
@Parameter(key = "c")
@Parameter(key = "output", itemIO = ItemIO.BOTH)
public class ComputeLocalMeanThresholdIntegral<T extends RealType<T>> implements
	Computers.Arity3<RectangleNeighborhood<Composite<DoubleType>>, T, Double, BitType>
{

	@OpDependency(name = "stats.integralMean")
	private Computers.Arity1<RectangleNeighborhood<Composite<DoubleType>>, DoubleType> integralMeanOp;

	@Override
	public void compute(
		final RectangleNeighborhood<Composite<DoubleType>> inputNeighborhood,
		final T inputCenterPixel, final Double c, @Mutable final BitType output)
	{
		compute(inputNeighborhood, inputCenterPixel, c, integralMeanOp, output);
	}

	public static <T extends RealType<T>> void compute(
		final RectangleNeighborhood<Composite<DoubleType>> inputNeighborhood,
		final T inputCenterPixel, final Double c,
		final Computers.Arity1<RectangleNeighborhood<Composite<DoubleType>>, DoubleType> integralMeanOp,
		@Mutable final BitType output)
	{
		final DoubleType sum = new DoubleType();
		integralMeanOp.compute(inputNeighborhood, sum);

		// Subtract the contrast
		sum.sub(new DoubleType(c));

		// Set value
		final DoubleType centerPixelAsDoubleType = new DoubleType();
		centerPixelAsDoubleType.set(inputCenterPixel.getRealDouble());

		output.set(centerPixelAsDoubleType.compareTo(sum) > 0);
	}

}
