/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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

package org.scijava.ops.image.filter.fft;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Function that uses FFTMethods to perform a forward FFT
 *
 * @author Brian Northan
 * @param <T> TODO Documentation
 */
public class FFTMethodsOp<T extends RealType<T>> {

	/**
	 * Note that if fast is true the input will be extended to the next fast FFT
	 * size. If false the input will be computed using the original input
	 * dimensions (if possible). If the input dimensions are not supported by the
	 * underlying FFT implementation the input will be extended to the nearest
	 * size that is supported. TODO
	 *
	 * @param FFTMethodsOpF dependency to FFTMethodsOpF
	 * @param input input image
	 * @param borderSize the size of border to apply in each dimension
	 * @param fast whether to perform a fast FFT; default true
	 * @return the output
	 * @implNote op names='filter.fft', priority='100.'
	 */

	public static <T extends RealType<T>>
		RandomAccessibleInterval<ComplexFloatType> run( //
			@OpDependency(
				name = "filter.fft") Functions.Arity4<RandomAccessibleInterval<T>, ComplexFloatType, long[], Boolean, RandomAccessibleInterval<ComplexFloatType>> FFTMethodsOpF,
			final RandomAccessibleInterval<T> input, //
			@Nullable long[] borderSize, //
			@Nullable Boolean fast //
	) {

		return FFTMethodsOpF.apply(input, new ComplexFloatType(), borderSize, fast);

	}

}
