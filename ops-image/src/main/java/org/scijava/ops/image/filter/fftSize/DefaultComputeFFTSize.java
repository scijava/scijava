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

package org.scijava.ops.image.filter.fftSize;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;

/**
 * Op that calculates FFT fast sizes according to the following logic. If
 * powerOfTwo=true compute next power of 2 If powerOfTwo=false compute next
 * smooth size
 *
 * @author Brian Northan
 * @implNote op names='filter.fftSize'
 */
public class DefaultComputeFFTSize implements
	BiFunction<Dimensions, Boolean, long[][]>
{

	/**
	 * TODO
	 *
	 * @param dimensions
	 * @param powerOfTwo
	 * @return the outputSizes
	 */
	@Override
	public long[][] apply(Dimensions dimensions, Boolean powerOfTwo) {

        var size = new long[2][];
		size[0] = new long[dimensions.numDimensions()];
		size[1] = new long[dimensions.numDimensions()];

		for (var i = 0; i < dimensions.numDimensions(); i++) {
			// real size
			if (powerOfTwo) {
				size[0][i] = NextPowerOfTwo.nextPowerOfTwo(dimensions.dimension(i));
			}
			else {
				size[0][i] = NextSmoothNumber.nextSmooth((int) dimensions.dimension(i));
			}
			// complex size
			if (i == 0) {
				size[1][i] = size[0][i] / 2 + 1;
			}
			else {
				size[1][i] = size[0][i];
			}
		}

		return size;

	}

}
