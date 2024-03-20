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
import java.util.function.Function;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
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

/**
 * Richardson Lucy with total variation function op that operates on (@link
 * RandomAccessibleInterval) (Richardson-Lucy algorithm with total variation
 * regularization for 3D confocal microscope deconvolution Microsc Res Rech 2006
 * Apr; 69(4)- 260-6)
 *
 * @author bnorthan
 * @param <I>
 * @param <O>
 * @param <K>
 * @param <C>
 * @implNote op names='deconvolve.richardsonLucyTV', priority='100.'
 */
public class PadAndRichardsonLucyTV<I extends RealType<I> & NativeType<I>, O extends RealType<O> & NativeType<O>, K extends RealType<K> & NativeType<K>, C extends ComplexType<C> & NativeType<C>>
	implements
	Functions.Arity10<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, O, C, Integer, Boolean, Boolean, Float, long[], OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<O>>
{

	@OpDependency(name = "deconvolve.richardsonLucyUpdate")
	private Computers.Arity3<RandomAccessibleInterval<O>, Float, RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> updateOp;

	private float regularizationFactor;

	private Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> computeEstimateOp =
		getComputeEstimateOp();

	@OpDependency(name = "deconvolve.normalizationFactor")
	private Inplaces.Arity5_1<RandomAccessibleInterval<O>, Dimensions, Dimensions, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>> normalizer;

	@OpDependency(name = "deconvolve.firstGuess")
	private Functions.Arity3<RandomAccessibleInterval<I>, O, Dimensions, RandomAccessibleInterval<O>> firstGuess;

	@OpDependency(name = "deconvolve.accelerate")
	private Inplaces.Arity1<RandomAccessibleInterval<O>> accelerator;

	// TODO: can this go in AbstractFFTFilterF?
	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, O, RandomAccessibleInterval<O>> outputCreator;

	@OpDependency(name = "filter.padInputFFTMethods")
	private Functions.Arity4<RandomAccessibleInterval<I>, Dimensions, Boolean, OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<I>> padOp;

	@OpDependency(name = "filter.padShiftKernelFFTMethods")
	private BiFunction<RandomAccessibleInterval<K>, Dimensions, RandomAccessibleInterval<K>> padKernelOp;

	@OpDependency(name = "filter.createFFTOutput")
	private Functions.Arity3<Dimensions, C, Boolean, RandomAccessibleInterval<C>> createOp;

	@OpDependency(name = "deconvolve.richardsonLucy")
	private Computers.Arity12<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, //
			RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, Boolean, //
			Boolean, C, Integer, Inplaces.Arity1<RandomAccessibleInterval<O>>, //
			Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>>, //
			List<Inplaces.Arity1<RandomAccessibleInterval<O>>>, RandomAccessibleInterval<O>, //
			RandomAccessibleInterval<O>> richardsonLucyOp;

	private Boolean nonCirculant;

	private Integer maxIterations;

	/**
	 * create a richardson lucy filter
	 */
	public
		Computers.Arity2<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<O>>
		createFilterComputer(RandomAccessibleInterval<I> raiExtendedInput,
			RandomAccessibleInterval<K> raiExtendedKernel,
			RandomAccessibleInterval<C> fftImg, RandomAccessibleInterval<C> fftKernel,
			RandomAccessibleInterval<O> output)
	{

		final C complexType = Util.getTypeFromInterval(fftImg).createVariable();
		// if non-circulant mode, set up the richardson-lucy computer in
		// non-circulant mode and return it
		if (nonCirculant) {
			// TODO should this be input and kernel instead of raiExtendedInput and
			// raiExtendedKernel?
			Inplaces.Arity1<RandomAccessibleInterval<O>> normalizerSimplified = (
				io) -> {
				normalizer.mutate(io, raiExtendedInput, raiExtendedKernel, fftImg,
					fftKernel);

			};

			ArrayList<Inplaces.Arity1<RandomAccessibleInterval<O>>> list =
				new ArrayList<>();

			list.add(normalizerSimplified);

			// TODO: should it be input instead of raiExtendedInput?
			Function<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>> fg = (
				in) -> firstGuess.apply(in, Util.getTypeFromInterval(output),
					raiExtendedInput);

			return (input, kernel, out) -> {
				richardsonLucyOp.compute(input, kernel, fftImg, fftKernel, true, true,
					complexType, maxIterations, accelerator, computeEstimateOp, list, fg
						.apply(raiExtendedInput), out);
			};
		}

		// return a richardson lucy computer
		return (input, kernel, out) -> {
			richardsonLucyOp.compute(input, kernel, fftImg, fftKernel, true, true,
				complexType, maxIterations, accelerator, computeEstimateOp, null, null,
				out);
		};
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
	 * TODO
	 *
	 * @param input
	 * @param kernel
	 * @param outType
	 * @param complexType
	 * @param maxIterations max number of iterations
	 * @param nonCirculant indicates whether to use non-circulant edge handling
	 * @param accelerate indicates whether or not to use acceleration
	 * @param regularizationFactor
	 * @param borderSize
	 * @param obfInput
	 * @return the deconvolution of the input data
	 */
	@Override
	public RandomAccessibleInterval<O> apply(RandomAccessibleInterval<I> input,
		RandomAccessibleInterval<K> kernel, O outType, C complexType,
		Integer maxIterations, Boolean nonCirculant, Boolean accelerate,
		Float regularizationFactor, @Nullable long[] borderSize,
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

			if (!nonCirculant) {
				obfInput = new OutOfBoundsMirrorFactory<>(
					OutOfBoundsMirrorFactory.Boundary.SINGLE);
			}
			else if (nonCirculant) {
				obfInput = new OutOfBoundsConstantValueFactory<>(Util
					.getTypeFromInterval(input).createVariable());
			}
		}

		this.maxIterations = maxIterations;
		this.regularizationFactor = regularizationFactor;

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

		RandomAccessibleInterval<I> paddedInput = padOp.apply(input,
			new FinalDimensions(paddedSize), true, obfInput);

		RandomAccessibleInterval<K> paddedKernel = padKernelOp.apply(kernel,
			new FinalDimensions(paddedSize));

		computeFilter(paddedInput, paddedKernel, output, paddedSize, complexType);

		return output;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected
		Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>>
		getComputeEstimateOp()
	{
		// create Richardson Lucy TV update op, this will override the base RL
		// Update.
		return (in, out) -> {
			updateOp.compute(in, regularizationFactor, null, out);
		};
	}
}
