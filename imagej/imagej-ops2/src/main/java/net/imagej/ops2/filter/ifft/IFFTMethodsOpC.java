/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops2.filter.ifft;

import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fft2.FFTMethods;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;
import org.scijava.plugin.Plugin;

/**
 * Inverse FFT computer that operates on an RAI and wraps FFTMethods. The input
 * and output size must conform to supported FFT size. Use
 * {@link net.imagej.ops2.filter.fftSize.ComputeFFTSize} to calculate the
 * supported FFT size.
 * 
 * @author Brian Northan
 * @param <C>
 * @param <T>
 */
@Plugin(type = Op.class, name = "filter.ifft")
public class IFFTMethodsOpC<C extends ComplexType<C>, T extends RealType<T>>
		implements Computers.Arity2<RandomAccessibleInterval<C>, ExecutorService, RandomAccessibleInterval<T>> {

	@OpDependency(name = "copy.rai")
	private Function<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>> copyOp;

	/**
	 * Compute an ND inverse FFT
	 */
	/**
	 * TODO
	 *
	 * @param input
	 * @param executorService
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<C> input, final ExecutorService es,
			final RandomAccessibleInterval<T> output) {
		if (!conforms(input))
			throw new IllegalArgumentException("The input image dimensions to not conform to a supported FFT size");

		final RandomAccessibleInterval<C> temp = copyOp.apply(input);

		for (int d = input.numDimensions() - 1; d > 0; d--)
			FFTMethods.complexToComplex(temp, d, false, true, es);

		FFTMethods.complexToReal(temp, output, FFTMethods.unpaddingIntervalCentered(temp, output), 0, true, es);
	}

	/**
	 * Make sure that the input size conforms to a supported FFT size.
	 */
	public boolean conforms(RandomAccessibleInterval<C> input) {

		long[] paddedDimensions = new long[input.numDimensions()];
		long[] realDimensions = new long[input.numDimensions()];

		boolean fastSizeConforms = false;

		FFTMethods.dimensionsComplexToRealFast(input, paddedDimensions, realDimensions);

		if (FFTMethods.dimensionsEqual(input, paddedDimensions) == true) {
			fastSizeConforms = true;
		}

		boolean smallSizeConforms = false;

		FFTMethods.dimensionsComplexToRealSmall(input, paddedDimensions, realDimensions);

		if (FFTMethods.dimensionsEqual(input, paddedDimensions) == true) {
			smallSizeConforms = true;
		}

		return fastSizeConforms || smallSizeConforms;

	}

}
