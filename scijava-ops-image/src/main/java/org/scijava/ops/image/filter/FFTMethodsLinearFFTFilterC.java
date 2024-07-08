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

package org.scijava.ops.image.filter;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Convolve op for (@link RandomAccessibleInterval)
 *
 * @author Brian Northan
 * @param <I>
 * @param <O>
 * @param <K>
 * @param <C>
 * @implNote op names='filter.linearFilter', priority='-100.'
 */
public class FFTMethodsLinearFFTFilterC<I extends RealType<I>, O extends RealType<O>, K extends RealType<K>, C extends ComplexType<C>>
	implements
	Computers.Arity7<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, Boolean, Boolean, Computers.Arity2<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<O>>
{

	@OpDependency(name = "filter.fft")
	private Computers.Arity1<RandomAccessibleInterval<I>, RandomAccessibleInterval<C>> fftInOp;

	@OpDependency(name = "filter.fft")
	private Computers.Arity1<RandomAccessibleInterval<K>, RandomAccessibleInterval<C>> fftKernelOp;

	@OpDependency(name = "filter.ifft")
	private Computers.Arity1<RandomAccessibleInterval<C>, RandomAccessibleInterval<O>> ifftOp;

	@OpDependency(name = "filter.createFFTOutput")
	private Functions.Arity3<Dimensions, C, Boolean, RandomAccessibleInterval<C>> createOp;

	/**
	 * Perform convolution by multiplying the FFTs in the frequency domain
	 */
	/**
	 * TODO
	 *
	 * @param in
	 * @param kernel
	 * @param performInputFFT
	 * @param performKernelFFT
	 * @param frequencyOp
	 * @param fftInput
	 * @param fftKernel
	 * @param out
	 */
	@Override
	public void compute(final RandomAccessibleInterval<I> in,
		final RandomAccessibleInterval<K> kernel, final Boolean performInputFFT,
		final Boolean performKernelFFT,
		final Computers.Arity2<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>> frequencyOp,
		@Nullable RandomAccessibleInterval<C> fftInput,
		@Nullable RandomAccessibleInterval<C> fftKernel,
		final RandomAccessibleInterval<O> out)
	{
		final var fftType = Util.getTypeFromInterval(fftInput);

        var inputFFT = fftInput;
        var kernelFFT = fftKernel;

		// create FFT input memory if needed
		if (inputFFT == null) inputFFT = createOp.apply(in, fftType, true);

		// create FFT kernel memory if needed
		if (kernelFFT == null) {
			kernelFFT = createOp.apply(in, fftType, true);
		}

		// perform input FFT if needed
		if (performInputFFT) {
			fftInOp.compute(in, inputFFT);
		}

		// perform kernel FFT if needed
		if (performKernelFFT) {
			fftKernelOp.compute(kernel, kernelFFT);
		}

		// perform the operation in frequency domain (ie multiplication for
		// convolution, complex conjugate multiplication for correlation,
		// Wiener Filter, etc.)
		frequencyOp.compute(inputFFT, kernelFFT, inputFFT);

		// perform inverse fft
		ifftOp.compute(inputFFT, out);
		// linearFilter.compute(in, kernel, out);
	}
}
