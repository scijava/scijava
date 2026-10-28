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

import net.imglib2.Dimensions;
import net.imglib2.algorithm.fft2.FFTMethods;

import org.scijava.function.Functions;

/**
 * Op that calculates FFT sizes.
 *
 * @author Brian Northan
 * @implNote op names='filter.fftSize'
 */
public class ComputeFFTMethodsSize implements
	Functions.Arity3<Dimensions, Boolean, Boolean, long[][]>
{

	/**
	 * TODO
	 *
	 * @param dimensions
	 * @param forward
	 * @param fast
	 * @return the output
	 */
	@Override
	public long[][] apply(Dimensions dimensions, Boolean forward, Boolean fast) {

        var size = new long[2][];
		size[0] = new long[dimensions.numDimensions()];
		size[1] = new long[dimensions.numDimensions()];

		if (fast && forward) {

			FFTMethods.dimensionsRealToComplexFast(dimensions, size[0], size[1]);

		}
		else if (!fast && forward) {
			FFTMethods.dimensionsRealToComplexSmall(dimensions, size[0], size[1]);

		}
		if (fast && !forward) {

			FFTMethods.dimensionsComplexToRealFast(dimensions, size[0], size[1]);

		}
		else if (!fast && !forward) {

			FFTMethods.dimensionsComplexToRealSmall(dimensions, size[0], size[1]);
		}

		return size;

	}

}
