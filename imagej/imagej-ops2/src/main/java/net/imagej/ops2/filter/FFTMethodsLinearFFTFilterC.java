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

package net.imagej.ops2.filter;

import java.util.concurrent.ExecutorService;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;

import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;
import org.scijava.plugin.Plugin;

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
		implements Computers.Arity8<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, Boolean, Boolean, ExecutorService, Computers.Arity2<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>>, RandomAccessibleInterval<O>> {

	@OpDependency(name = "filter.fft")
	private Computers.Arity2<RandomAccessibleInterval<I>, ExecutorService, RandomAccessibleInterval<C>> fftInOp;

	@OpDependency(name = "filter.fft")
	private Computers.Arity2<RandomAccessibleInterval<K>, ExecutorService, RandomAccessibleInterval<C>> fftKernelOp;

	@OpDependency(name = "filter.ifft")
	private Computers.Arity2<RandomAccessibleInterval<C>, ExecutorService, RandomAccessibleInterval<O>> ifftOp;
	
	@OpDependency(name = "filter.createFFTOutput")
	private Functions.Arity3<Dimensions, C, Boolean, RandomAccessibleInterval<C>> createOp;

	/**
	 * Perform convolution by multiplying the FFTs in the frequency domain
	 */
	/**
	 * TODO
	 *
	 * @param input
	 * @param kernel
	 * @param fftInput
	 * @param fftKernel
	 * @param performInputFFT
	 * @param performKernelFFT
	 * @param executorService
	 * @param frequencyOp
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<I> in, final RandomAccessibleInterval<K> kernel,
			final RandomAccessibleInterval<C> fftInput, final RandomAccessibleInterval<C> fftKernel,
			final Boolean performInputFFT, final Boolean performKernelFFT, final ExecutorService es,
			final Computers.Arity2<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>> frequencyOp,
			final RandomAccessibleInterval<O> out) {
		final C fftType = Util.getTypeFromInterval(fftInput);

		RandomAccessibleInterval<C> inputFFT = fftInput;
		RandomAccessibleInterval<C> kernelFFT = fftKernel;
		
		// create FFT input memory if needed
		if (inputFFT == null)
			inputFFT = createOp.apply(in, fftType, true);

		// create FFT kernel memory if needed
		if (kernelFFT == null) {
			kernelFFT = createOp.apply(in, fftType, true);
		}

		// perform input FFT if needed
		if (performInputFFT) {
			fftInOp.compute(in, es, inputFFT);
		}

		// perform kernel FFT if needed
		if (performKernelFFT) {
			fftKernelOp.compute(kernel, es, kernelFFT);
		}

		// perform the operation in frequency domain (ie multiplication for
		// convolution, complex conjugate multiplication for correlation,
		// Wiener Filter, etc.)
		frequencyOp.compute(inputFFT, kernelFFT, inputFFT);

		// perform inverse fft
		ifftOp.compute(inputFFT, es, out);
		// linearFilter.compute(in, kernel, out);
	}
}
