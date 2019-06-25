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

import net.imagej.ops.filter.AbstractFFTFilterF;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;

import org.scijava.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.ops.util.Adapt;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Convolve op for (@link Img)
 * 
 * @author Brian Northan
 * @param <I>
 * @param <O>
 * @param <K>
 * @param <C>
 */
@Plugin(type = Op.class, name = "filter.convolve", priority = Priority.HIGH)
@Parameter(key = "input")
@Parameter(key = "kernel")
@Parameter(key = "borderSize")
@Parameter(key = "obfInput")
@Parameter(key = "obfKernel")
@Parameter(key = "fftType")
@Parameter(key = "outType")
@Parameter(key = "output", type = ItemIO.BOTH)
public class ConvolveFFTF<I extends RealType<I> & NativeType<I>, O extends RealType<O> & NativeType<O>, K extends RealType<K> & NativeType<K>, C extends ComplexType<C> & NativeType<C>>
		extends AbstractFFTFilterF<I, O, K, C> {

	@OpDependency(name = "filter.convolve")
	private Computer4<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<O>> convolveOp;

	/**
	 * create a convolve filter computer
	 */
	@Override
	public BiComputer<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<O>> createFilterComputer(
			RandomAccessibleInterval<I> raiExtendedInput, RandomAccessibleInterval<K> raiExtendedKernel,
			RandomAccessibleInterval<C> fftImg, RandomAccessibleInterval<C> fftKernel,
			RandomAccessibleInterval<O> output) {
		return Adapt.Computers.asBiComputer(convolveOp, fftImg, fftKernel);
	}

}