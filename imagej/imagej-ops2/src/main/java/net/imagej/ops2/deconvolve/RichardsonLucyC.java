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

package net.imagej.ops2.deconvolve;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;
import org.scijava.plugin.Plugin;

/**
 * Richardson Lucy algorithm for (@link RandomAccessibleInterval) (Lucy, L. B.
 * (1974). "An iterative technique for the rectification of observed
 * distributions".)
 * 
 * @author Brian Northan
 * @param <I>
 * @param <O>
 * @param <K>
 * @param <C>
 * @implNote op names='deconvolve.richardsonLucy', priority='100.'
 */
public class RichardsonLucyC<I extends RealType<I>, O extends RealType<O>, K extends RealType<K>, C extends ComplexType<C>>
		implements Computers.Arity13<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<C>, //
			RandomAccessibleInterval<C>, Boolean, Boolean, C, Integer, Inplaces.Arity1<RandomAccessibleInterval<O>>, //
			Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>>, RandomAccessibleInterval<O>, //
			List<Inplaces.Arity1<RandomAccessibleInterval<O>>>, ExecutorService, RandomAccessibleInterval<O>> {

	// /**
	// * Op that computes Richardson Lucy update, can be overridden to implement
	// * variations of the algorithm (like RichardsonLucyTV)
	// */
	// private UnaryComputerOp<RandomAccessibleInterval<O>,
	// RandomAccessibleInterval<O>> updateOp =
	// null;
	//
	// /**
	// * The current estimate, by passing in the current estimate the user can
	// * define the starting point (first guess), if no starting estimate is
	// * provided the default starting point will be the input image
	// */
	// private RandomAccessibleInterval<O> raiExtendedEstimate;
	//
	// /**
	// * A list of optional constraints that are applied at the end of each
	// * iteration (ie can be used to achieve noise removal, non-circulant
	// * normalization, etc.)
	// */
	// private ArrayList<UnaryInplaceOp<RandomAccessibleInterval<O>,
	// RandomAccessibleInterval<O>>> iterativePostProcessingOps =
	// null;

	// private BinaryComputerOp<RandomAccessibleInterval<I>,
	// RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> rlCorrectionOp;
	@OpDependency(name = "deconvolve.richardsonLucyCorrection")
	private Computers.Arity5<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>, //
	RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, ExecutorService, //
	RandomAccessibleInterval<O>> rlCorrectionOp;

	@OpDependency(name = "create.img")
	private BiFunction<Interval, O, Img<O>> createOp;

	@OpDependency(name = "filter.fft")
	// private UnaryComputerOp<RandomAccessibleInterval<K>,
	// RandomAccessibleInterval<C>> fftKernelOp;
	private Computers.Arity2<RandomAccessibleInterval<K>, ExecutorService, RandomAccessibleInterval<C>> fftKernelOp;

	@OpDependency(name = "filter.convolve")
	private Computers.Arity7<RandomAccessibleInterval<O>, RandomAccessibleInterval<K>, //
			RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, Boolean, Boolean, //
			ExecutorService, RandomAccessibleInterval<O>> convolverOp;

	@OpDependency(name = "copy.rai")
	private Computers.Arity1<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>> copyOp;

	@OpDependency(name = "copy.rai")
	private Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> copy2Op;

	/**
	 * TODO
	 *
	 * @param input
	 * @param kernel
	 * @param fftInput
	 * @param fftKernel
	 * @param performInputFFT
	 * @param performKernelFFT
	 * @param complexType
	 * @param maxIterations
	 * @param accelerator
	 * @param updateOp by default, this should be RichardsonLucyUpdate
	 * @param raiExtendedEstimate
	 * @param iterativePostProcessingOps
	 * @param executorService
	 * @param output
	 */
	@Override
	public void compute(RandomAccessibleInterval<I> in, RandomAccessibleInterval<K> kernel,
			RandomAccessibleInterval<C> fftInput, RandomAccessibleInterval<C> fftKernel, Boolean performInputFFT,
			Boolean performKernelFFT, C complexType, Integer maxIterations,
			Inplaces.Arity1<RandomAccessibleInterval<O>> accelerator,
			Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> updateOp,
			RandomAccessibleInterval<O> raiExtendedEstimate,
			List<Inplaces.Arity1<RandomAccessibleInterval<O>>> iterativePostProcessingOps, ExecutorService es,
			RandomAccessibleInterval<O> out) {

		// TODO: can these be deleted?
		// // create FFT input memory if needed
		// if (getFFTInput() == null) {
		// setFFTInput(getCreateOp().calculate(in));
		// }
		//
		// // create FFT kernel memory if needed
		// if (getFFTKernel() == null) {
		// setFFTKernel(getCreateOp().calculate(in));
		// }

		// if a starting point for the estimate was not passed in then create
		// estimate Img and use the input as the starting point
		if (raiExtendedEstimate == null) {

			raiExtendedEstimate = createOp.apply(in, Util.getTypeFromInterval(out));

			copyOp.compute(in, raiExtendedEstimate);
		}

		// create image for the reblurred
		RandomAccessibleInterval<O> raiExtendedReblurred = createOp.apply(in, Util.getTypeFromInterval(out));

		// perform fft of psf
		fftKernelOp.compute(kernel, es, fftKernel);

		// -- perform iterations --

		for (int i = 0; i < maxIterations; i++) {

			// create reblurred by convolving kernel with estimate
			// NOTE: the FFT of the PSF of the kernel has been passed in as a
			// parameter. when the op was set up, and computed above, so we can use
			// compute
			convolverOp.compute(raiExtendedEstimate, null, fftInput, fftKernel, true, false, es,
				raiExtendedReblurred);

			// compute correction factor
			rlCorrectionOp.compute(in, raiExtendedReblurred, fftInput, fftKernel, es, raiExtendedReblurred);

			// perform update to calculate new estimate
			updateOp.compute(raiExtendedReblurred, raiExtendedEstimate);

			// apply post processing
			if (iterativePostProcessingOps != null) {
				for (Inplaces.Arity1<RandomAccessibleInterval<O>> pp : iterativePostProcessingOps) {
					pp.mutate(raiExtendedEstimate);
				}
			}

			// accelerate the algorithm by taking a larger step
			// TODO: do we need the nullcheck?
			if (accelerator != null) {
				accelerator.mutate(raiExtendedEstimate);
			}
		}

		// -- copy crop padded back to original size

		final long[] start = new long[out.numDimensions()];
		final long[] end = new long[out.numDimensions()];

		for (int d = 0; d < out.numDimensions(); d++) {
			start[d] = 0;
			end[d] = start[d] + out.dimension(d) - 1;
		}

		copy2Op.compute(Views.interval(raiExtendedEstimate, new FinalInterval(start, end)), out);
	}
}
