/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * LocalThresholdMethod using Niblack's thresholding method.
 *
 * @author Jonathan Hale
 * @author Stefan Helfrich (University of Konstanz)
 * @implNote op names='threshold.localNiblack', priority='-100.'
 */
public class ComputeLocalNiblackThreshold<T extends RealType<T>> implements
	Computers.Arity4<Iterable<T>, T, Double, Double, BitType>
{

	@OpDependency(name = "stats.mean")
	private Computers.Arity1<Iterable<T>, DoubleType> meanOp;

	@OpDependency(name = "stats.stdDev")
	private Computers.Arity1<Iterable<T>, DoubleType> stdDeviationOp;

	/**
	 * TODO
	 *
	 * @param inputNeighborhood
	 * @param inputCenterPixel
	 * @param c
	 * @param k
	 * @param output
	 */
	@Override
	public void compute(final Iterable<T> inputNeighborhood,
		final T inputCenterPixel, final Double c, final Double k,
		final BitType output)
	{
		compute(inputNeighborhood, inputCenterPixel, c, k, meanOp, stdDeviationOp,
			output);
	}

	public static <T extends RealType<T>> void compute(
		final Iterable<T> inputNeighborhood, final T inputCenterPixel,
		final Double c, final Double k,
		final Computers.Arity1<Iterable<T>, DoubleType> meanOp,
		final Computers.Arity1<Iterable<T>, DoubleType> stdDeviationOp,
		final BitType output)
	{
		final DoubleType m = new DoubleType();
		meanOp.compute(inputNeighborhood, m);

		final DoubleType stdDev = new DoubleType();
		stdDeviationOp.compute(inputNeighborhood, stdDev);

		output.set(inputCenterPixel.getRealDouble() > m.getRealDouble() + k * stdDev
			.getRealDouble() - c);
	}

}
