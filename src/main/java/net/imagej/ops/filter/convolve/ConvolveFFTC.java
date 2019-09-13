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

package net.imagej.ops.filter.convolve;

import java.util.concurrent.ExecutorService;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;

import org.scijava.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer5;
import org.scijava.ops.core.computer.Computer7;
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
@Plugin(type = Op.class, name = "filter.convolve", priority = Priority.LOW)
@Parameter(key = "input")
@Parameter(key = "kernel")
@Parameter(key = "fftInput")
@Parameter(key = "fftKernel")
@Parameter(key = "performInputFFT")
@Parameter(key = "performKernelFFT")
@Parameter(key = "executorService")
@Parameter(key = "output", type = ItemIO.BOTH)
public class ConvolveFFTC<I extends RealType<I>, O extends RealType<O>, K extends RealType<K>, C extends ComplexType<C>>
		implements Computer7<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, Boolean, Boolean, ExecutorService, RandomAccessibleInterval<O>> {

	@OpDependency(name = "math.multiply")
	private BiComputer<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>> mul;

	@OpDependency(name = "filter.linearFilter")
	private Computer8<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, Boolean, Boolean, ExecutorService, BiComputer<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>>, RandomAccessibleInterval<O>> linearFilter;

	/**
	 * Call the linear filter that is set up to perform convolution
	 */
	@Override
	public void compute(RandomAccessibleInterval<I> in, RandomAccessibleInterval<K> kernel,
			RandomAccessibleInterval<C> fftInput, RandomAccessibleInterval<C> fftKernel, Boolean performInputFFT,
			Boolean performKernelFFT, ExecutorService es, RandomAccessibleInterval<O> out) {
		linearFilter.compute(in, kernel, fftInput, fftKernel, performInputFFT, performKernelFFT, es, mul, out);
	}
}

@Plugin(type = Op.class, name = "filter.convolve", priority = Priority.LOW)
@Parameter(key = "input")
@Parameter(key = "kernel")
@Parameter(key = "fftInput")
@Parameter(key = "fftKernel")
@Parameter(key = "executorService")
@Parameter(key = "output", type = ItemIO.BOTH)
class ConvolveFFTCSimple<I extends RealType<I>, O extends RealType<O>, K extends RealType<K>, C extends ComplexType<C>>
		implements
		Computer5<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, ExecutorService, RandomAccessibleInterval<O>> {

	@OpDependency(name = "filter.convolve")
	private Computer7<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, Boolean, Boolean, ExecutorService, RandomAccessibleInterval<O>> convolveOp;

	@Override
	public void compute(RandomAccessibleInterval<I> in, RandomAccessibleInterval<K> kernel,
			RandomAccessibleInterval<C> fftInput, RandomAccessibleInterval<C> fftKernel, ExecutorService es,
			RandomAccessibleInterval<O> output) {
		convolveOp.compute(in, kernel, fftInput, fftKernel, true, true, es, output);
	}
}
