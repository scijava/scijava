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

package org.scijava.ops.image.filter.fftSize;

import java.util.Arrays;

import net.imglib2.Dimensions;
import net.imglib2.algorithm.fft2.FFTMethods;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import org.scijava.function.Functions;

/**
 * Op that calculates FFT sizes.
 *
 * @author Brian Northan
 * @implNote op names='filter.fftSize'
 */
public class ComputeFFTSize implements
	Functions.Arity5<Dimensions, long[], long[], Boolean, Boolean, Pair<long[], long[]>>
{

	/**
	 * TODO
	 *
	 * @param inputDimensions
	 * @param paddedSize
	 * @param fftSize
	 * @param forward
	 * @param fast
	 * @return the outputs
	 */
	@Override
	public Pair<long[], long[]> apply(Dimensions inputDimensions,
		long[] paddedSize, long[] fftSize, Boolean forward, Boolean fast)
	{

		long[] paddedOutput = Arrays.copyOf(paddedSize, paddedSize.length);
		long[] fftOutput = Arrays.copyOf(fftSize, fftSize.length);

		if (fast && forward) {

			FFTMethods.dimensionsRealToComplexFast(inputDimensions, paddedOutput,
				fftOutput);

		}
		else if (!fast && forward) {
			FFTMethods.dimensionsRealToComplexSmall(inputDimensions, paddedOutput,
				fftOutput);

		}
		if (fast && !forward) {

			FFTMethods.dimensionsComplexToRealFast(inputDimensions, paddedOutput,
				fftOutput);

		}
		else if (!fast && !forward) {

			FFTMethods.dimensionsComplexToRealSmall(inputDimensions, paddedOutput,
				fftOutput);

		}

		return new ValuePair<>(paddedOutput, fftOutput);

	}

}
