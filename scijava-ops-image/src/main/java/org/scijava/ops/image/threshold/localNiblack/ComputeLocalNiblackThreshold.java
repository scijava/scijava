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

import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.image.threshold.Thresholds;

/**
 * Niblack's local threshold: a pixel is foreground if it is strictly above
 * {@code mean + k * sd - c}, with {@code mean} and {@code sd} the mean and the
 * population standard deviation (as in Fiji's Auto_Local_Threshold plugin) of
 * its neighborhood.
 * <p>
 * Niblack W. (1986) "An Introduction to Digital Image Processing",
 * Prentice-Hall.
 * </p>
 *
 * @author Jonathan Hale
 * @author Stefan Helfrich (University of Konstanz)
 * @implNote op names='threshold.localNiblack', priority='-100.'
 */
public class ComputeLocalNiblackThreshold<T extends RealType<T>> implements
	Computers.Arity4<Iterable<T>, T, Double, Double, BitType>
{

	/**
	 * Applies Niblack's rule to one pixel.
	 *
	 * @param inputNeighborhood the neighborhood of the pixel
	 * @param inputCenterPixel the pixel
	 * @param c the constant subtracted from the threshold (Fiji default: 0)
	 * @param k the weight of the standard deviation (Fiji default: 0.2 for
	 *          bright objects)
	 * @param output true if the pixel is foreground
	 */
	@Override
	public void compute(final Iterable<T> inputNeighborhood,
		final T inputCenterPixel, final Double c, final Double k,
		final BitType output)
	{
		final var stats = Thresholds.meanAndPopulationStdDev(inputNeighborhood);
		output.set(inputCenterPixel.getRealDouble() > stats[0] + k * stats[1] -
			c);
	}

}
