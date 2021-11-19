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

package net.imagej.ops2.filter.fft;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.img.Img;

import org.scijava.function.Functions;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Function that creates an output for FFTMethods FFT
 *
 * @author Brian Northan
 * @param <T>
 */
@Plugin(type = Op.class, name = "filter.createFFTOutput")
public class CreateOutputFFTMethods<T> implements Functions.Arity3<Dimensions, T, Boolean, Img<T>> {

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, T, Img<T>> create;

	/**
	 * TODO
	 *
	 * @param Dimensions
	 * @param outType
	 * @param fast
	 * @return the output
	 */
	@Override
	public Img<T> apply(Dimensions paddedDimensions, T outType, Boolean fast) {

		Dimensions paddedFFTMethodsFFTDimensions = FFTMethodsUtility.getFFTDimensionsRealToComplex(fast,
				paddedDimensions);

		return create.apply(paddedFFTMethodsFFTDimensions, outType);
	}

}

@Plugin(type = Op.class, name = "filter.createFFTOutput")
class CreateOutputFFTMethodsSimple<T> implements BiFunction<Dimensions, T, Img<T>> {
	@OpDependency(name = "filter.createFFTOutput")
	private Functions.Arity3<Dimensions, T, Boolean, Img<T>> create;

	/**
	 * TODO
	 *
	 * @param Dimensions
	 * @param outType
	 * @return the output
	 */
	@Override
	public Img<T> apply(Dimensions paddedDimensions, T outType) {
		return create.apply(paddedDimensions, outType, true);
	}
}
