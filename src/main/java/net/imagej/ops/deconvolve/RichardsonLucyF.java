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

package net.imagej.ops.deconvolve;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;

import org.scijava.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer13;
import org.scijava.ops.core.function.Function11;
import org.scijava.ops.core.function.Function3;
import org.scijava.ops.core.function.Function4;
import org.scijava.ops.core.inplace.Inplace;
import org.scijava.ops.core.inplace.Inplace6First;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

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
 */
@Plugin(type = Op.class, name = "deconvolve.richardsonLucy", priority = Priority.HIGH)
@Parameter(key = "input")
@Parameter(key = "kernel")
@Parameter(key = "borderSize")
@Parameter(key = "obfInput")
@Parameter(key = "obfKernel")
@Parameter(key = "outType")
@Parameter(key = "fftType")
@Parameter(key = "maxIterations", description = "max number of iterations")
@Parameter(key = "nonCirculant", description = "indicates whether to use non-circulant edge handling")
@Parameter(key = "accelerate", description = "indicates whether or not to use acceleration")
@Parameter(key = "executorService")
@Parameter(key = "output", type = ItemIO.OUTPUT)
public class RichardsonLucyF<I extends RealType<I> & NativeType<I>, O extends RealType<O> & NativeType<O>, K extends RealType<K> & NativeType<K>, C extends ComplexType<C> & NativeType<C>>
		implements
		Function11<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, long[], OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, OutOfBoundsFactory<K, RandomAccessibleInterval<K>>, O, C, Integer, Boolean, Boolean, ExecutorService, RandomAccessibleInterval<O>> {

	private Computer<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> computeEstimateOp = getComputeEstimateOp();

	@OpDependency(name = "deconvolve.normalizationFactor")
	private Inplace6First<RandomAccessibleInterval<O>, Dimensions, Dimensions, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, ExecutorService> normalizer;

	@OpDependency(name = "deconvolve.firstGuess")
	private Function3<RandomAccessibleInterval<I>, O, Dimensions, RandomAccessibleInterval<O>> firstGuess;

	@OpDependency(name = "math.multiply")
	private BiComputer<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> multiplyOp;

	@OpDependency(name = "deconvolve.accelerate")
	private Inplace<RandomAccessibleInterval<O>> accelerator;

	// TODO: can this go in AbstractFFTFilterF?
	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, O, RandomAccessibleInterval<O>> outputCreator;

	@OpDependency(name = "filter.padInputFFTMethods")
	private Function4<RandomAccessibleInterval<I>, Dimensions, Boolean, OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<I>> padOp;

	@OpDependency(name = "filter.padShiftKernelFFTMethods")
	private BiFunction<RandomAccessibleInterval<K>, Dimensions, RandomAccessibleInterval<K>> padKernelOp;

	@OpDependency(name = "filter.createFFTOutput")
	private Function3<Dimensions, C, Boolean, RandomAccessibleInterval<C>> createOp;

	@OpDependency(name = "deconvolve.richardsonLucy")
	private Computer13<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, //
			RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, Boolean, //
			Boolean, C, Integer, Inplace<RandomAccessibleInterval<O>>, //
			Computer<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>>, //
			RandomAccessibleInterval<O>, List<Inplace<RandomAccessibleInterval<O>>>, //
			ExecutorService, RandomAccessibleInterval<O>> richardsonLucyOp;

	private Boolean nonCirculant;

	private Integer maxIterations;

	/**
	 * create a richardson lucy filter
	 */
	public BiComputer<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<O>> createFilterComputer(
			RandomAccessibleInterval<I> raiExtendedInput, RandomAccessibleInterval<K> raiExtendedKernel,
			RandomAccessibleInterval<C> fftImg, RandomAccessibleInterval<C> fftKernel, ExecutorService es,
			RandomAccessibleInterval<O> output) {
		C complexType = Util.getTypeFromInterval(fftImg).createVariable();
		
		// if non-circulant mode, set up the richardson-lucy computer in
		// non-circulant mode and return it
		if (nonCirculant) {
			// TODO should this be input and kernel instead of raiExtendedInput and
			// raiExtendedKernel?
			Inplace<RandomAccessibleInterval<O>> normalizerSimplified = (io) -> {
				normalizer.mutate(io, raiExtendedInput, raiExtendedKernel, fftImg, fftKernel, es);

			};

			ArrayList<Inplace<RandomAccessibleInterval<O>>> list = new ArrayList<>();

			list.add(normalizerSimplified);

			// TODO: should it be input instead of raiExtendedInput?
			Function<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>> fg = (in) -> firstGuess.apply(in,
					Util.getTypeFromInterval(output), raiExtendedInput);

			return (input, kernel, out) -> {
				richardsonLucyOp.compute(input, kernel, fftImg, fftKernel, true, true, complexType, maxIterations, accelerator,
						computeEstimateOp, fg.apply(raiExtendedInput), list, es, out);
			};
		}

		// return a richardson lucy computer
		return (input, kernel, out) -> {
			richardsonLucyOp.compute(input, kernel, fftImg, fftKernel, true, true, complexType, maxIterations, accelerator,
					computeEstimateOp, null, null, es, out);
		};
	}

	/**
	 * set up and return the compute estimate op. This function can be over-ridden
	 * to implement different types of richardson lucy (like total variation
	 * richardson lucy)
	 * 
	 * @return compute estimate op
	 */
	protected Computer<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> getComputeEstimateOp() {
		// TODO: can we now delete RichardsonLucyUpdate?
		return (in, out) -> {
			multiplyOp.compute(out, in, out);
		};
	}

	// TODO: can we move this to AbstractFFTFilterF?
	/**
	 * create FFT memory, create FFT filter and run it
	 */
	public void computeFilter(final RandomAccessibleInterval<I> input, final RandomAccessibleInterval<K> kernel,
			RandomAccessibleInterval<O> output, long[] paddedSize, C complexType, ExecutorService es) {

		RandomAccessibleInterval<C> fftInput = createOp.apply(new FinalDimensions(paddedSize), complexType, true);

		RandomAccessibleInterval<C> fftKernel = createOp.apply(new FinalDimensions(paddedSize), complexType, true);

		// TODO: in this case it is difficult to match the filter op in the
		// 'initialize' as we don't know the size yet, thus we can't create
		// memory
		// for the FFTs
		BiComputer<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<O>> filter = createFilterComputer(
				input, kernel, fftInput, fftKernel, es, output);

		filter.compute(input, kernel, output);
	}

	@Override
	public RandomAccessibleInterval<O> apply(RandomAccessibleInterval<I> input, RandomAccessibleInterval<K> kernel,
			long[] borderSize, OutOfBoundsFactory<I, RandomAccessibleInterval<I>> obfInput,
			OutOfBoundsFactory<K, RandomAccessibleInterval<K>> obfKernel, O outType, C complexType,
			Integer maxIterations, Boolean nonCirculant, Boolean accelerate, ExecutorService es) {

		this.nonCirculant = nonCirculant;
		this.maxIterations = maxIterations;

		RandomAccessibleInterval<O> output = outputCreator.apply(input, outType);

		final int numDimensions = input.numDimensions();

		// 1. Calculate desired extended size of the image

		final long[] paddedSize = new long[numDimensions];

		if (borderSize == null) {
			// if no border size was passed in, then extend based on kernel size
			for (int d = 0; d < numDimensions; ++d) {
				paddedSize[d] = (int) input.dimension(d) + (int) kernel.dimension(d) - 1;
			}

		} else {
			// if borderSize was passed in
			for (int d = 0; d < numDimensions; ++d) {

				paddedSize[d] = Math.max(kernel.dimension(d) + 2 * borderSize[d],
						input.dimension(d) + 2 * borderSize[d]);
			}
		}

		RandomAccessibleInterval<I> paddedInput = padOp.apply(input, new FinalDimensions(paddedSize), true, obfInput);

		RandomAccessibleInterval<K> paddedKernel = padKernelOp.apply(kernel, new FinalDimensions(paddedSize));

		computeFilter(paddedInput, paddedKernel, output, paddedSize, complexType, es);

		return output;
	}

}
