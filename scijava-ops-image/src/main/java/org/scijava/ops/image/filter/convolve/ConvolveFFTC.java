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

package org.scijava.ops.image.filter.convolve;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.Nullable;

/**
 * Convolve op for (@link RandomAccessibleInterval)
 *
 * @author Brian Northan
 * @param <I>
 * @param <O>
 * @param <K>
 * @param <C>
 * @implNote op names='filter.convolve', priority='-100.'
 */
public class ConvolveFFTC<I extends RealType<I>, O extends RealType<O>, K extends RealType<K>, C extends ComplexType<C>>
	implements
	Computers.Arity6<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, Boolean, Boolean, RandomAccessibleInterval<O>>
{

	@OpDependency(name = "math.multiply")
	private Computers.Arity2<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>> mul;

	@OpDependency(name = "filter.linearFilter")
	private Computers.Arity7<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, Boolean, Boolean, Computers.Arity2<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<O>> linearFilter;

	/**
	 * Call the linear filter that is set up to perform convolution
	 */
	/**
	 * TODO
	 *
	 * @param in
	 * @param kernel
	 * @param fftInput
	 * @param fftKernel
	 * @param performInputFFT
	 * @param performKernelFFT
	 * @param out
	 */
	@Override
	public void compute(RandomAccessibleInterval<I> in,
		RandomAccessibleInterval<K> kernel, RandomAccessibleInterval<C> fftInput,
		RandomAccessibleInterval<C> fftKernel, @Nullable Boolean performInputFFT,
		@Nullable Boolean performKernelFFT, RandomAccessibleInterval<O> out)
	{
		if (performInputFFT == null) performInputFFT = true;
		if (performKernelFFT == null) performKernelFFT = true;
		linearFilter.compute(in, kernel, performInputFFT, performKernelFFT, mul,
			fftInput, fftKernel, out);
	}
}
