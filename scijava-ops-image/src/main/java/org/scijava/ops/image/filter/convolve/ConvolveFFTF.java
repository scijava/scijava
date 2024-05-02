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

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Convolve op for (@link Img)
 *
 * @author Brian Northan
 * @param <I>
 * @param <O>
 * @param <K>
 * @param <C>
 * @implNote op names='filter.convolve'
 */
public class ConvolveFFTF<I extends RealType<I> & NativeType<I>, O extends RealType<O> & NativeType<O>, K extends RealType<K> & NativeType<K>, C extends ComplexType<C> & NativeType<C>>
	/* extends AbstractFFTFilterF<I, O, K, C> */ implements
	Functions.Arity6<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, O, C, long[], OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<O>>
{

	// TODO: can this go in AbstractFFTFilterF?
	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, O, RandomAccessibleInterval<O>> outputCreator;

	@OpDependency(name = "filter.padInputFFTMethods")
	private Functions.Arity4<RandomAccessibleInterval<I>, Dimensions, Boolean, OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<I>> padOp;

	@OpDependency(name = "filter.padShiftKernelFFTMethods")
	private BiFunction<RandomAccessibleInterval<K>, Dimensions, RandomAccessibleInterval<K>> padKernelOp;

	@OpDependency(name = "filter.createFFTOutput")
	private Functions.Arity3<Dimensions, C, Boolean, RandomAccessibleInterval<C>> createOp;

	@OpDependency(name = "filter.convolve")
	private Computers.Arity6<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, Boolean, Boolean, RandomAccessibleInterval<O>> convolveOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param kernel
	 * @param outType
	 * @param complexType
	 * @param borderSize
	 * @param obfInput
	 * @return the output
	 */
	@Override
	public RandomAccessibleInterval<O> apply(
		final RandomAccessibleInterval<I> input,
		final RandomAccessibleInterval<K> kernel, final O outType,
		final C complexType, @Nullable final long[] borderSize,
		@Nullable OutOfBoundsFactory<I, RandomAccessibleInterval<I>> obfInput)
	{

		RandomAccessibleInterval<O> output = outputCreator.apply(input, outType);

		final int numDimensions = input.numDimensions();

		// 1. Calculate desired extended size of the image

		final long[] paddedSize = new long[numDimensions];

		if (borderSize == null) {
			// if no border size was passed in, then extend based on kernel size
			for (int d = 0; d < numDimensions; ++d) {
				paddedSize[d] = (int) input.dimension(d) + (int) kernel.dimension(d) -
					1;
			}

		}
		else {
			// if borderSize was passed in
			for (int d = 0; d < numDimensions; ++d) {

				paddedSize[d] = Math.max(kernel.dimension(d) + 2 * borderSize[d], input
					.dimension(d) + 2 * borderSize[d]);
			}
		}

		if (obfInput == null) {
			obfInput = new OutOfBoundsConstantValueFactory<>(Util.getTypeFromInterval(
				input).createVariable());
		}

		RandomAccessibleInterval<I> paddedInput = padOp.apply(input,
			new FinalDimensions(paddedSize), true, obfInput);

		RandomAccessibleInterval<K> paddedKernel = padKernelOp.apply(kernel,
			new FinalDimensions(paddedSize));

		computeFilter(paddedInput, paddedKernel, output, paddedSize, complexType);

		return output;
	}

	// TODO: can we move this to AbstractFFTFilterF?
	/**
	 * create FFT memory, create FFT filter and run it
	 */
	public void computeFilter(final RandomAccessibleInterval<I> input,
		final RandomAccessibleInterval<K> kernel,
		RandomAccessibleInterval<O> output, long[] paddedSize, C complexType)
	{

		RandomAccessibleInterval<C> fftInput = createOp.apply(new FinalDimensions(
			paddedSize), complexType, true);

		RandomAccessibleInterval<C> fftKernel = createOp.apply(new FinalDimensions(
			paddedSize), complexType, true);

		// TODO: in this case it is difficult to match the filter op in the
		// 'initialize' as we don't know the size yet, thus we can't create
		// memory
		// for the FFTs
		Computers.Arity2<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<O>> filter =
			createFilterComputer(input, kernel, fftInput, fftKernel, output);

		filter.compute(input, kernel, output);
	}

	/**
	 * create a convolve filter computer
	 */
	public
		Computers.Arity2<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<O>>
		createFilterComputer(RandomAccessibleInterval<I> raiExtendedInput,
			RandomAccessibleInterval<K> raiExtendedKernel,
			RandomAccessibleInterval<C> fftImg, RandomAccessibleInterval<C> fftKernel,
			RandomAccessibleInterval<O> output)
	{
		return (in1, in2, out) -> convolveOp.compute(in1, in2, fftImg, fftKernel,
			true, true, out);
	}

}

/**
 * @implNote op names='filter.convolve', priority='100.'
 */
class SimpleConvolveFFTF<I extends RealType<I> & NativeType<I>, O extends RealType<O> & NativeType<O>, K extends RealType<K> & NativeType<K>, C extends ComplexType<C> & NativeType<C>>
	implements
	BiFunction<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<FloatType>>
{

	@OpDependency(name = "filter.convolve")
	Functions.Arity6<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, FloatType, ComplexFloatType, long[], OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<FloatType>> convolveOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param kernel
	 * @return the output
	 */
	@Override
	public RandomAccessibleInterval<FloatType> apply(
		RandomAccessibleInterval<I> input, RandomAccessibleInterval<K> kernel)
	{
		OutOfBoundsFactory<I, RandomAccessibleInterval<I>> obfInput =
			new OutOfBoundsConstantValueFactory<>(Util.getTypeFromInterval(input)
				.createVariable());
		return convolveOp.apply(input, kernel, new FloatType(),
			new ComplexFloatType(), null, obfInput);
	}

}
