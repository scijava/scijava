/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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

package org.scijava.ops.image.filter.ifft;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fft2.FFTMethods;
import net.imglib2.type.numeric.ComplexType;

import org.scijava.function.Inplaces;

/**
 * Inverse FFT inplace operator -- complex to complex only, output size must
 * conform to supported FFT size. Use
 * {@link org.scijava.ops.image.filter.fftSize.ComputeFFTSize} to calculate the
 * supported FFT size.
 * 
 * @author Brian Northan
 *@implNote op names='filter.ifft'
 */
public class IFFTMethodsOpI<C extends ComplexType<C>>
		implements Inplaces.Arity1<RandomAccessibleInterval<C>> {

	/**
	 * Compute an ND inverse FFT
	 */
	/**
	 * TODO
	 *
	 * @param inout
	 */
	@Override
	public void mutate(final RandomAccessibleInterval<C> inout) {
		if (!conforms(inout))
			throw new IllegalArgumentException("The input size does not conform to a supported FFT size!");
		for (int d = inout.numDimensions() - 1; d >= 0; d--)
			FFTMethods.complexToComplex(inout, d, false, true);
	}

	/**
	 * Make sure that the input size conforms to a supported FFT size.
	 */
	public boolean conforms(final RandomAccessibleInterval inout) {

		long[] paddedDimensions = new long[inout.numDimensions()];

		boolean fastSizeConforms = false;

		FFTMethods.dimensionsComplexToComplexFast(inout, paddedDimensions);

		if (FFTMethods.dimensionsEqual(inout, paddedDimensions) == true) {
			fastSizeConforms = true;
		}

		boolean smallSizeConforms = false;

		FFTMethods.dimensionsComplexToComplexSmall(inout, paddedDimensions);

		if (FFTMethods.dimensionsEqual(inout, paddedDimensions) == true) {
			smallSizeConforms = true;
		}

		return fastSizeConforms || smallSizeConforms;

	}

}
