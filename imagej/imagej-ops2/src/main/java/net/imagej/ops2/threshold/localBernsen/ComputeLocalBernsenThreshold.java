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

package net.imagej.ops2.threshold.localBernsen;

import java.util.function.Function;

import net.imagej.ops2.threshold.localMidGrey.ComputeLocalMidGreyThreshold;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.functions.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Local threshold method which is similar to
 * {@link ComputeLocalMidGreyThreshold}, but uses a constant value rather than
 * the value of the input pixel when the contrast in the neighborhood of that
 * pixel is too small.
 * 
 * @author Jonathan Hale
 * @author Stefan Helfrich (University of Konstanz)
 * @param <T> input type
 */
@Plugin(type = Op.class, name = "threshold.localBernsen")
@Parameter(key = "inputNeighborhood")
@Parameter(key = "inputCenterPixel")
@Parameter(key = "contrastThreshold")
@Parameter(key = "halfMaxValue")
@Parameter(key = "output")
public class ComputeLocalBernsenThreshold<T extends RealType<T>> implements
	Computers.Arity4<Iterable<T>, T, Double, Double, BitType>
{

	@OpDependency(name = "stats.minMax")
	private Function<Iterable<T>, Pair<T, T>> minMaxOp;

	@Override
	public void compute(final Iterable<T> inputNeighborhood,
		final T inputCenterPixel, final Double contrastThreshold,
		final Double halfMaxValue, final BitType output)
	{
		compute(inputNeighborhood, inputCenterPixel, contrastThreshold,
			halfMaxValue, minMaxOp, output);
	}

	public static <T extends RealType<T>> void compute(
		final Iterable<T> inputNeighborhood, final T inputCenterPixel,
		final Double contrastThreshold, final Double halfMaxValue,
		final Function<Iterable<T>, Pair<T, T>> minMaxOp,
		final BitType output)
	{
		final Pair<T, T> outputs = minMaxOp.apply(inputNeighborhood);
		final double minValue = outputs.getA().getRealDouble();
		final double maxValue = outputs.getB().getRealDouble();
		final double midGrey = (maxValue + minValue) / 2.0;

		if ((maxValue - minValue) < contrastThreshold) {
			output.set(midGrey >= halfMaxValue);
		}
		else {
			output.set(inputCenterPixel.getRealDouble() >= midGrey);
		}
	}

}
