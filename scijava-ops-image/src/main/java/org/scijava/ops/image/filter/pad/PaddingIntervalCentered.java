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

package org.scijava.ops.image.filter.pad;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fft2.FFTMethods;
import net.imglib2.type.numeric.ComplexType;

/**
 * Op used to calculate and return a centered padding interval given an input
 * RAI and the desired padded dimensions
 *
 * @author Brian Northan
 * @param <T>
 * @param <I>
 * @param <O>
 * @implNote op names='filter.padIntervalCentered', priority='100.'
 */
public class PaddingIntervalCentered<T extends ComplexType<T>, I extends RandomAccessibleInterval<T>, O extends Interval>
	implements BiFunction<I, Dimensions, O>
{

	/**
	 * TODO
	 *
	 * @param input
	 * @param paddedDimensions
	 * @return the output
	 */
	@Override
	@SuppressWarnings("unchecked")
	public O apply(final I input, final Dimensions paddedDimensions) {

		final long[] paddedSize = new long[paddedDimensions.numDimensions()];
		paddedDimensions.dimensions(paddedSize);

		O inputInterval = (O) FFTMethods.paddingIntervalCentered(input,
			FinalDimensions.wrap(paddedSize));

		return inputInterval;
	}
}
