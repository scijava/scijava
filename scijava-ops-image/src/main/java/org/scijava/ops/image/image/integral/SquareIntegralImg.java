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

package org.scijava.ops.image.image.integral;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;

/**
 * <p>
 * <i>n</i>-dimensional squared integral image that stores sums using
 * {@code RealType}. Care must be taken that sums do not overflow the capacity
 * of {@code RealType} (i.e. {@link DoubleType} or {@link LongType}). Instead of
 * the sum of values the sum of squared values is computed.
 * </p>
 *
 * @param <I> The type of the input image.
 * @author Stefan Helfrich (University of Konstanz)
 * @implNote op name='image.squareIntegral', priority='99.'
 */
public class SquareIntegralImg<I extends RealType<I>, O extends RealType<O>>
	extends AbstractIntegralImg<I, O>
{

	@Override
	public
		Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>>
		getComputer(final int dimension)
	{
		if (dimension == 0) {
			return computeSquareAndAdd;
		}

		return computeAdd;
	}
}
