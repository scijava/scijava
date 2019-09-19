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

package net.imagej.ops.filter;

import java.util.concurrent.ExecutorService;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;

import org.scijava.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer8;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Convolve op for (@link RandomAccessibleInterval)
 * 
 * @author Brian Northan
 * @param <I>
 * @param <O>
 * @param <K>
 * @param <C>
 */
@Plugin(type = Op.class, name = "filter.linearFilter", priority = Priority.LOW)
@Parameter(key = "input")
@Parameter(key = "kernel")
@Parameter(key = "fftInput")
@Parameter(key = "fftKernel")
@Parameter(key = "performInputFFT")
@Parameter(key = "executorService")
@Parameter(key = "output", type = ItemIO.BOTH)
public class FFTMethodsLinearFFTFilterC<I extends RealType<I>, O extends RealType<O>, K extends RealType<K>, C extends ComplexType<C>>
		extends AbstractFFTFilterC<I, O, K, C> implements
		Computer8<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, Boolean, Boolean, ExecutorService, BiComputer<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>>, RandomAccessibleInterval<O>> {

	// TODO: should this be a parameter? figure out best way to override
	// frequencyOp
	// @Parameter
	// private BiComputer<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>,
	// RandomAccessibleInterval<C>> frequencyOp;

	@OpDependency(name = "filter.fft")
	private BiComputer<RandomAccessibleInterval<I>, ExecutorService, RandomAccessibleInterval<C>> fftInOp;

	@OpDependency(name = "filter.fft")
	private BiComputer<RandomAccessibleInterval<K>, ExecutorService, RandomAccessibleInterval<C>> fftKernelOp;

	@OpDependency(name = "filter.ifft")
	private BiComputer<RandomAccessibleInterval<C>, ExecutorService, RandomAccessibleInterval<O>> ifftOp;

	/**
	 * Perform convolution by multiplying the FFTs in the frequency domain
	 */
	@Override
	public void compute(final RandomAccessibleInterval<I> in, final RandomAccessibleInterval<K> kernel,
			final RandomAccessibleInterval<C> fftInput, final RandomAccessibleInterval<C> fftKernel,
			final Boolean performInputFFT, final Boolean performKernelFFT, final ExecutorService es,
			final BiComputer<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>> frequencyOp,
			final RandomAccessibleInterval<O> out) {
		// create FFT input memory if needed
		if (getFFTInput() == null) {
			setFFTInput(getCreateOp().apply(in));
		}

		// create FFT kernel memory if needed
		if (getFFTKernel() == null) {
			setFFTKernel(getCreateOp().apply(in));
		}

		// perform input FFT if needed
		if (getPerformInputFFT()) {
			fftInOp.compute(in, es, getFFTInput());
		}

		// perform kernel FFT if needed
		if (getPerformKernelFFT()) {
			fftKernelOp.compute(kernel, es, getFFTKernel());
		}

		// perform the operation in frequency domain (ie multiplication for
		// convolution, complex conjugate multiplication for correlation,
		// Wiener Filter, etc.)
		frequencyOp.compute(getFFTInput(), getFFTKernel(), getFFTInput());

		// perform inverse fft
		ifftOp.compute(getFFTInput(), es, out);
		// linearFilter.compute(in, kernel, out);
	}
}
