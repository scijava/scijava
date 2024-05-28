/*-
 * #%L
 * Fluorescence lifetime analysis in SciJava Ops.
 * %%
 * Copyright (C) 2024 SciJava developers.
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

package org.scijava.ops.starter;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * @author Edward Evans
 * @param <I> input type
 * @implNote op names='filter.gaussianSub', priority='100.'
 */
public class GaussianSubtraction<I extends RealType<I>> implements
		BiFunction<RandomAccessibleInterval<I>, Double, RandomAccessibleInterval<DoubleType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> createOp;

	@OpDependency(name = "filter.gauss")
	private BiFunction<RandomAccessibleInterval<I>, Double, RandomAccessibleInterval<DoubleType>> gaussOp;

	@OpDependency(name = "math.sub")
	private Computers.Arity2<RandomAccessibleInterval<I>, RandomAccessibleInterval<DoubleType>, RandomAccessibleInterval<DoubleType>> subOp;

	/**
	 * Gaussian blur subtraction Op. This performs basic feature extraction by
	 * subtracting the input image with a Gaussian blurred copy, leaving the
	 * non-blurred structures.
	 *
	 * @param input input image
	 * @param sigma sigma value for gaussian blur
	 * @return guassian blur subtracted image
	 */
	@Override
	public RandomAccessibleInterval<DoubleType> apply(
			RandomAccessibleInterval<I> input, Double sigma)
	{
		RandomAccessibleInterval<DoubleType> blur = gaussOp.apply(input, sigma);
		RandomAccessibleInterval<DoubleType> output = createOp.apply(input,
				new DoubleType());
		subOp.compute(input, blur, output);
		return output;
	}
}
