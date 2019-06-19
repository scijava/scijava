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

import java.util.function.Function;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.function.Function3;
import org.scijava.ops.util.Adapt;

/**
 * Abstract class for FFT based filter computers
 * 
 * @author Brian Northan
 * @param <I>
 * @param <O>
 *            gene
 * @param <K>
 * @param <C>
 */
public abstract class AbstractFFTFilterC<I extends RealType<I>, O extends RealType<O>, K extends RealType<K>, C extends ComplexType<C>>{

	/**
	 * Buffer to be used to store FFTs for input. Size of fftInput must correspond
	 * to the fft size of raiExtendedInput
	 */
	private RandomAccessibleInterval<C> fftInput;

	/**
	 * Buffer to be used to store FFTs for kernel. Size of fftKernel must correspond
	 * to the fft size of raiExtendedKernel
	 */
	private RandomAccessibleInterval<C> fftKernel;

	/**
	 * boolean indicating that the input FFT has already been calculated
	 */
	private boolean performInputFFT = true;

	/**
	 * boolean indicating that the kernel FFT has already been calculated
	 */
	private boolean performKernelFFT = true;

	/**
	 * FFT type
	 */
	private C fftType;

	/**
	 * Op used to create the complex FFTs
	 * NOTE: Boolean is always true.
	 */
	@OpDependency(name = "filter.createFFTOutput")
	private Function3<Dimensions, C, Boolean, RandomAccessibleInterval<C>> createOp;

	protected RandomAccessibleInterval<C> getFFTInput() {
		return fftInput;
	}

	public void setFFTInput(RandomAccessibleInterval<C> fftInput) {
		this.fftInput = fftInput;
	}

	protected RandomAccessibleInterval<C> getFFTKernel() {
		return fftKernel;
	}

	public void setFFTKernel(RandomAccessibleInterval<C> fftKernel) {
		this.fftKernel = fftKernel;
	}

	protected boolean getPerformInputFFT() {
		return performInputFFT;
	}

	protected boolean getPerformKernelFFT() {
		return performKernelFFT;
	}

	public Function<Dimensions, RandomAccessibleInterval<C>> getCreateOp() {
		return Adapt.Functions.asFunction(createOp, fftType, true);
	}

}
