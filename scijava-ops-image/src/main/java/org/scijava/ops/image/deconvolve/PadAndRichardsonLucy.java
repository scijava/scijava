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

package org.scijava.ops.image.deconvolve;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;
import org.scijava.progress.Progress;

/**
 * Richardson Lucy function op that operates on (@link RandomAccessibleInterval)
 * (Lucy, L. B. (1974). "An iterative technique for the rectification of
 * observed distributions".)
 *
 * @author Brian Northan
 * @param <I>
 * @param <O>
 * @param <K>
 * @param <C>
 * @implNote op names='deconvolve.richardsonLucy', priority='100.'
 */
public class PadAndRichardsonLucy<I extends RealType<I> & NativeType<I>, O extends RealType<O> & NativeType<O>, K extends RealType<K> & NativeType<K>, C extends ComplexType<C> & NativeType<C>>
	implements
	Functions.Arity9<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, O, C, Integer, Boolean, Boolean, long[], OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<O>>
{

	private Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> computeEstimateOp =
		getComputeEstimateOp();

	@OpDependency(name = "deconvolve.normalizationFactor")
	private Inplaces.Arity5_1<RandomAccessibleInterval<O>, Dimensions, Dimensions, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>> normalizer;

	@OpDependency(name = "deconvolve.firstGuess")
	private Functions.Arity3<RandomAccessibleInterval<I>, O, Dimensions, RandomAccessibleInterval<O>> firstGuess;

	@OpDependency(name = "math.multiply")
	private Computers.Arity2<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> multiplyOp;

	// TODO: can this go in AbstractFFTFilterF?
	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, O, RandomAccessibleInterval<O>> outputCreator;

	@OpDependency(name = "filter.padInputFFTMethods")
	private Functions.Arity4<RandomAccessibleInterval<I>, Dimensions, Boolean, OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<I>> padOp;

	@OpDependency(name = "filter.padShiftKernelFFTMethods")
	private BiFunction<RandomAccessibleInterval<K>, Dimensions, RandomAccessibleInterval<K>> padKernelOp;

	@OpDependency(name = "filter.createFFTOutput")
	private Functions.Arity3<Dimensions, C, Boolean, RandomAccessibleInterval<C>> createOp;

	@OpDependency(name = "deconvolve.richardsonLucy", hints = {
		"progress.TRACK" })
	private Computers.Arity12<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, //
			RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, Boolean, //
			Boolean, C, Integer, Boolean, //
			Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>>, //
			List<Inplaces.Arity1<RandomAccessibleInterval<O>>>, //
			RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> richardsonLucyOp;

	private Boolean nonCirculant;

	private Integer maxIterations;

	/**
	 * TODO
	 *
	 * @param input
	 * @param kernel
	 * @param outType
	 * @param complexType
	 * @param maxIterations max number of iterations
	 * @param nonCirculant indicates whether to use non-circulant edge handling
	 * @param accelerate indicates whether or not to use acceleration
	 * @param borderSize
	 * @param obfInput
	 * @return the output
	 */
	@Override
	public RandomAccessibleInterval<O> apply(RandomAccessibleInterval<I> input,
		RandomAccessibleInterval<K> kernel, O outType, C complexType,
		Integer maxIterations, @Nullable Boolean nonCirculant,
		@Nullable Boolean accelerate, @Nullable long[] borderSize,
		@Nullable OutOfBoundsFactory<I, RandomAccessibleInterval<I>> obfInput)
	{
		// default to circulant
		if (nonCirculant == null) {
			nonCirculant = false;
			this.nonCirculant = nonCirculant;
		}
		else {
			this.nonCirculant = nonCirculant;
		}

		// out of bounds factory will be different depending on if circulant or
		// non-circulant is used
		if (obfInput == null) {
			if (nonCirculant) {
				obfInput = new OutOfBoundsConstantValueFactory<>(Util
					.getTypeFromInterval(input).createVariable());
			}
			else {
				obfInput = new OutOfBoundsMirrorFactory<>(
					OutOfBoundsMirrorFactory.Boundary.SINGLE);
			}
		}

		// default to no acceleration
		if (accelerate == null) accelerate = false;

		this.maxIterations = maxIterations;

        var output = outputCreator.apply(input, outType);

		final var numDimensions = input.numDimensions();

		// 1. Calculate desired extended size of the image

		final var paddedSize = new long[numDimensions];

		if (borderSize == null) {
			// if no border size was passed in, then extend based on kernel size
			for (var d = 0; d < numDimensions; ++d) {
				paddedSize[d] = (int) input.dimension(d) + (int) kernel.dimension(d) -
					1;
			}

		}
		else {
			// if borderSize was passed in
			for (var d = 0; d < numDimensions; ++d) {

				paddedSize[d] = Math.max(kernel.dimension(d) + 2 * borderSize[d], input
					.dimension(d) + 2 * borderSize[d]);
			}
		}

		Progress.defineTotal(0, 1);
        var paddedInput = padOp.apply(input,
			new FinalDimensions(paddedSize), true, obfInput);

        var paddedKernel = padKernelOp.apply(kernel,
			new FinalDimensions(paddedSize));

		computeFilter(input, kernel, paddedInput, paddedKernel, output, paddedSize,
			complexType, accelerate);

		return output;
	}

	/**
	 * create a richardson lucy filter
	 */
	private
		Computers.Arity2<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<O>>
		createFilterComputer(RandomAccessibleInterval<I> unpaddedInput,
			RandomAccessibleInterval<K> unpaddedKernel,
			RandomAccessibleInterval<I> raiExtendedInput,
			RandomAccessibleInterval<K> raiExtendedKernel,
			RandomAccessibleInterval<C> fftImg, RandomAccessibleInterval<C> fftKernel,
			boolean accelerate, RandomAccessibleInterval<O> output)
	{
        var complexType = Util.getTypeFromInterval(fftImg).createVariable();

		// if non-circulant mode, set up the richardson-lucy computer in
		// non-circulant mode and return it
		if (nonCirculant) {
			Inplaces.Arity1<RandomAccessibleInterval<O>> normalizerSimplified = (
				io) -> {
				normalizer.mutate(io, unpaddedInput, unpaddedKernel, fftImg, fftKernel);
			};

            var list =
				new ArrayList<Inplaces.Arity1<RandomAccessibleInterval<O>>>();

			list.add(normalizerSimplified);

			return (input, kernel, out) -> {
				richardsonLucyOp.compute(input, kernel, fftImg, fftKernel, true, true,
					complexType, maxIterations, accelerate, computeEstimateOp, list,
					firstGuess.apply(raiExtendedInput, Util.getTypeFromInterval(out),
						out), out);
			};
		}

		// return a richardson lucy computer
		return (input, kernel, out) -> {
			richardsonLucyOp.compute(input, kernel, fftImg, fftKernel, true, true,
				complexType, maxIterations, accelerate, computeEstimateOp, null, null,
				out);
		};
	}

	/**
	 * set up and return the compute estimate op. This function can be over-ridden
	 * to implement different types of richardson lucy (like total variation
	 * richardson lucy)
	 *
	 * @return compute estimate op
	 */
	protected
		Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>>
		getComputeEstimateOp()
	{
		// TODO: can we now delete RichardsonLucyUpdate?
		return (in, out) -> {
			multiplyOp.compute(out, in, out);
		};
	}

	// TODO: can we move this to AbstractFFTFilterF?
	/**
	 * create FFT memory, create FFT filter and run it
	 */
	private void computeFilter(final RandomAccessibleInterval<I> input,
		final RandomAccessibleInterval<K> kernel,
		final RandomAccessibleInterval<I> paddedInput,
		final RandomAccessibleInterval<K> paddedKernel,
		RandomAccessibleInterval<O> output, long[] paddedSize, C complexType,
		boolean accelerate)
	{

        var fftInput = createOp.apply(new FinalDimensions(
			paddedSize), complexType, true);

        var fftKernel = createOp.apply(new FinalDimensions(
			paddedSize), complexType, true);

		// TODO: in this case it is difficult to match the filter op in the
		// 'initialize' as we don't know the size yet, thus we can't create
		// memory
		// for the FFTs
        var filter =
			createFilterComputer(input, kernel, paddedInput, paddedKernel, fftInput,
				fftKernel, accelerate, output);

		filter.compute(paddedInput, paddedKernel, output);
	}

}
