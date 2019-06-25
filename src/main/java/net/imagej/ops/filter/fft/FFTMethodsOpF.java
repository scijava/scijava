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

package net.imagej.ops.filter.fft;

import java.util.concurrent.ExecutorService;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;

import org.scijava.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.function.Function3;
import org.scijava.ops.core.function.Function6;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Function that uses FFTMethods to perform a forward FFT
 * 
 * @author Brian Northan
 * @param <T>
 *            TODO Documentation
 * @param <C>
 *            TODO Documentation
 */
@Plugin(type = Op.class, name = "filter.FFT", priority = Priority.HIGH)
@Parameter(key = "input")
@Parameter(key = "borderSize", description = "the size of border to apply in each dimension")
@Parameter(key = "fast", description = "whether to perform a fast FFT")
@Parameter(key = "outOfBoundsFactory", description = "used to extend the image")
@Parameter(key = "fftType", description = "the complex type of the output")
@Parameter(key = "executorService")
@Parameter(key = "output", type = ItemIO.OUTPUT)
public class FFTMethodsOpF<T extends RealType<T>, C extends ComplexType<C>> implements
		Function6<RandomAccessibleInterval<T>, long[], Boolean, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, Type<C>, ExecutorService, RandomAccessibleInterval<C>> {

	@OpDependency(name = "filter.pad")
	private Function3<RandomAccessibleInterval<T>, Dimensions, Boolean, RandomAccessibleInterval<T>> padOp;

	@OpDependency(name = "filter.createFFTOutput")
	private Function3<Dimensions, Type<C>, Boolean, RandomAccessibleInterval<C>> createOp;

	@OpDependency(name = "filter.fft")
	private BiComputer<RandomAccessibleInterval<T>, ExecutorService, RandomAccessibleInterval<C>> fftMethodsOp;

	/**
	 * Note that if fast is true the input will be extended to the next fast FFT
	 * size. If false the input will be computed using the original input dimensions
	 * (if possible). If the input dimensions are not supported by the underlying
	 * FFT implementation the input will be extended to the nearest size that is
	 * supported.
	 */
	@Override
	public RandomAccessibleInterval<C> apply(final RandomAccessibleInterval<T> input, final long[] borderSize,
			final Boolean fast, final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> obf, final Type<C> fftType,
			final ExecutorService es) {
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
		RandomAccessibleInterval<C> output = createOp.apply(paddedDimensions, fftType, fast);

		// pad the input
		RandomAccessibleInterval<T> paddedInput = padOp.apply(input, paddedDimensions, fast);

		// compute and return fft
		fftMethodsOp.compute(paddedInput, es, output);

		return output;

	}

}