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

package net.imagej.ops2.filter.pad;

import java.util.function.Function;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ComplexType;

import org.scijava.Priority;
import org.scijava.function.Functions;
import org.scijava.ops.api.Op;
import org.scijava.ops.OpDependency;
import org.scijava.ops.util.Adapt;
import org.scijava.plugin.Plugin;

/**
 * Op used to pad a kernel to a size that is compatible with FFTMethods and
 * shift the center of the kernel to the origin
 * 
 * @author bnorthan
 * @param <T>
 * @param <I>
 * @param <O>
 */
@Plugin(type = Op.class, name = "filter.padShiftKernelFFTMethods",
	priority = Priority.HIGH)
public class PadShiftKernelFFTMethods<T extends ComplexType<T>, I extends RandomAccessibleInterval<T>, O extends RandomAccessibleInterval<T>>
	extends PadShiftKernel<T, I, O>
{

	@OpDependency(name = "filter.fftSize")
	private Functions.Arity3<Dimensions, Boolean, Boolean, long[][]> fftSizeOp;

	@Override
	protected Function<Dimensions, long[][]> getFFTSizeOp(){
		return Adapt.FunctionAdapt.asFunction(fftSizeOp, true, true);
	}

}
