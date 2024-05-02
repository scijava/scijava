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

package org.scijava.ops.image.filter.fft;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Function that uses FFTMethods to perform a forward FFT
 *
 * @author Brian Northan
 * @param <T> TODO Documentation
 * @param <C> TODO Documentation
 * @implNote op names='filter.fft', priority='100.'
 */
public class FFTMethodsOpF<T extends RealType<T>, C extends ComplexType<C>>
	implements
	Functions.Arity4<RandomAccessibleInterval<T>, C, long[], Boolean, RandomAccessibleInterval<C>>
{

	@OpDependency(name = "filter.padInputFFTMethods")
	private Functions.Arity4<RandomAccessibleInterval<T>, Dimensions, Boolean, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> padOp;

	@OpDependency(name = "filter.createFFTOutput")
	private Functions.Arity3<Dimensions, C, Boolean, RandomAccessibleInterval<C>> createOp;

	@OpDependency(name = "filter.fft")
	private Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<C>> fftMethodsOp;

	/**
	 * Note that if fast is true the input will be extended to the next fast FFT
	 * size. If false the input will be computed using the original input
	 * dimensions (if possible). If the input dimensions are not supported by the
	 * underlying FFT implementation the input will be extended to the nearest
	 * size that is supported.
	 */
	/**
	 * TODO
	 *
	 * @param input
	 * @param fftType the complex type of the output
	 * @param borderSize the size of border to apply in each dimension
	 * @param fast whether to perform a fast FFT; default true
	 * @return the output
	 */
	@Override
	public RandomAccessibleInterval<C> apply( //
		final RandomAccessibleInterval<T> input, //
		final C fftType, //
		@Nullable long[] borderSize, //
		@Nullable Boolean fast //
	) {

		if (fast == null) {
			fast = true;
		}
		// calculate the padded size
		long[] paddedSize = new long[input.numDimensions()];

		for (int d = 0; d < input.numDimensions(); d++) {
			paddedSize[d] = input.dimension(d);

			if (borderSize != null) {
				paddedSize[d] += borderSize[d];
			}
		}

		Dimensions paddedDimensions = new FinalDimensions(paddedSize);

		// create the complex output
		RandomAccessibleInterval<C> output = createOp.apply(paddedDimensions,
			fftType, fast);

		// pad the input
		RandomAccessibleInterval<T> paddedInput = padOp.apply(input,
			paddedDimensions, fast, null);

		// compute and return fft
		fftMethodsOp.compute(paddedInput, output);

		return output;

	}

}
