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

package org.scijava.ops.image.threshold.localBernsen;

import java.util.function.Function;

import org.scijava.ops.image.threshold.localMidGrey.ComputeLocalMidGreyThreshold;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * Local threshold method which is similar to
 * {@link ComputeLocalMidGreyThreshold}, but uses a constant value rather than
 * the value of the input pixel when the contrast in the neighborhood of that
 * pixel is too small.
 * <p>
 * The local mid grey is {@code (min + max) / 2} of the neighborhood; for
 * {@link IntegerType} images, it is rounded down to an integer grey level, as
 * in Fiji's Auto_Local_Threshold plugin. If the local contrast
 * {@code max - min} is below the contrast threshold, a pixel is foreground if
 * the mid grey is at least {@code halfMaxValue}; otherwise, it is foreground
 * if it is at least the mid grey.
 * </p>
 *
 * @author Jonathan Hale
 * @author Stefan Helfrich (University of Konstanz)
 * @param <T> input type
 * @implNote op names='threshold.localBernsen'
 */
public class ComputeLocalBernsenThreshold<T extends RealType<T>> implements
	Computers.Arity4<Iterable<T>, T, Double, Double, BitType>
{

	@OpDependency(name = "stats.minMax")
	private Function<Iterable<T>, Pair<T, T>> minMaxOp;

	/**
	 * Applies Bernsen's rule to one pixel.
	 *
	 * @param inputNeighborhood the neighborhood of the pixel
	 * @param inputCenterPixel the pixel
	 * @param contrastThreshold the local contrast below which a neighborhood
	 *          counts as uniform (Fiji default: 15)
	 * @param halfMaxValue the mid grey at or above which a uniform
	 *          neighborhood is foreground (Fiji default: 128 for 8-bit images)
	 * @param output true if the pixel is foreground
	 */
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
		final Function<Iterable<T>, Pair<T, T>> minMaxOp, final BitType output)
	{
		final var outputs = minMaxOp.apply(inputNeighborhood);
		final var minValue = outputs.getA().getRealDouble();
		final var maxValue = outputs.getB().getRealDouble();
		final var mid = (maxValue + minValue) / 2.0;
		final var midGrey = inputCenterPixel instanceof IntegerType ? Math.floor(
			mid) : mid;

		if ((maxValue - minValue) < contrastThreshold) {
			output.set(midGrey >= halfMaxValue);
		}
		else {
			output.set(inputCenterPixel.getRealDouble() >= midGrey);
		}
	}

}
