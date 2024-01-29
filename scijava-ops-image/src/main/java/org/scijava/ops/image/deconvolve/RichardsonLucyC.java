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

package org.scijava.ops.image.deconvolve;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

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
	implements
	Computers.Arity12<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, RandomAccessibleInterval<C>, //
			RandomAccessibleInterval<C>, Boolean, Boolean, C, Integer, Inplaces.Arity1<RandomAccessibleInterval<O>>, //
			Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>>, //
			List<Inplaces.Arity1<RandomAccessibleInterval<O>>>, RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> {

	@OpDependency(name = "deconvolve.richardsonLucyUpdate")
	private Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> updateOp;

	@OpDependency(name = "deconvolve.richardsonLucyCorrection")
	private Computers.Arity4< //
			RandomAccessibleInterval<I>, //
			RandomAccessibleInterval<O>, //
			RandomAccessibleInterval<C>, //
			RandomAccessibleInterval<C>, //
			RandomAccessibleInterval<O> //
	> rlCorrectionOp;

	@OpDependency(name = "create.img")
	private BiFunction<Interval, O, Img<O>> createOp;

	@OpDependency(name = "filter.fft")
	private Computers.Arity1<RandomAccessibleInterval<K>, RandomAccessibleInterval<C>> fftKernelOp;

	@OpDependency(name = "filter.convolve")
	private Computers.Arity6<RandomAccessibleInterval<O>, RandomAccessibleInterval<K>, //
			RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, //
			Boolean, Boolean, RandomAccessibleInterval<O>> convolverOp;

	@OpDependency(name = "copy.rai")
	private Computers.Arity1<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>> copyOp;

	@OpDependency(name = "copy.rai")
	private Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> copy2Op;

	/**
	 * TODO
	 *
	 * @param in the input data
	 * @param kernel the kernel
	 * @param fftInput A buffer to be used to store kernel FFTs.
	 * @param fftKernel A buffer to be used to store kernel FFTs.
	 * @param performInputFFT boolean indicating that the input FFT has already
	 *          been calculated. If true, the FFT will be taken on the input.
	 * @param performKernelFFT boolean indicating that the kernel FFT has already
	 *          been calculated. If true, the FFT will be taken on the kernel.
	 * @param complexType An instance of the type to be used in the Fourier space.
	 * @param maxIterations Maximum number of iterations to perform.
	 * @param accelerator An op which implements an acceleration strategy (takes a
	 *          larger step at each iteration).
	 * @param updateOp Op that computes Richardson Lucy update, can be overridden
	 *          to implement variations of the algorithm (like RichardsonLucyTV).
	 * @param iterativePostProcessingOps A list of optional constraints that are
	 *          applied at the end of each iteration (ie can be used to achieve
	 *          noise removal, non-circulant normalization, etc.).
	 * @param raiExtendedEstimate The current estimate, by passing in the current
	 *          estimate the user can define the starting point (first guess), if
	 *          no starting estimate is provided the default starting point will
	 *          be the input image.
	 * @param out the output buffer
	 */
	@Override
	public void compute(RandomAccessibleInterval<I> in, //
		RandomAccessibleInterval<K> kernel, //
		RandomAccessibleInterval<C> fftInput, //
		RandomAccessibleInterval<C> fftKernel, //
		Boolean performInputFFT, //
		Boolean performKernelFFT, //
		C complexType, //
		Integer maxIterations, //
		@Nullable Inplaces.Arity1<RandomAccessibleInterval<O>> accelerator, //
		@Nullable Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> updateOp, //
		@Nullable List<Inplaces.Arity1<RandomAccessibleInterval<O>>> iterativePostProcessingOps, //
		@Nullable RandomAccessibleInterval<O> raiExtendedEstimate, //
		RandomAccessibleInterval<O> out //
	) {

		// If the accelerator is null, make a No-op placeholder
		if (accelerator == null) {
			accelerator = (t) -> {};
		}
		// If the update Op is null, use the default
		if (updateOp == null) {
			updateOp = this.updateOp;
		}
		// If the user does not want any postprocessing, pass an empty list
		if (iterativePostProcessingOps == null) {
			iterativePostProcessingOps = Collections.emptyList();
		}
		// if a starting point for the estimate was not passed in then create
		// estimate Img and use the input as the starting point
		if (raiExtendedEstimate == null) {

			raiExtendedEstimate = createOp.apply(in, Util.getTypeFromInterval(out));

			copyOp.compute(in, raiExtendedEstimate);
		}

		// create image for the reblurred
		RandomAccessibleInterval<O> raiExtendedReblurred = createOp.apply(in, Util
			.getTypeFromInterval(out));

		// perform fft of psf
		fftKernelOp.compute(kernel, fftKernel);

		// -- perform iterations --

		for (int i = 0; i < maxIterations; i++) {

			// create reblurred by convolving kernel with estimate
			// NOTE: the FFT of the PSF of the kernel has been passed in as a
			// parameter. when the op was set up, and computed above, so we can use
			// compute
			convolverOp.compute(raiExtendedEstimate, null, fftInput, fftKernel, true,
				false, raiExtendedReblurred);

			// compute correction factor
			rlCorrectionOp.compute(in, raiExtendedReblurred, fftInput, fftKernel,
				raiExtendedReblurred);

			// perform update to calculate new estimate
			updateOp.compute(raiExtendedReblurred, raiExtendedEstimate);

			// apply post processing
			for (Inplaces.Arity1<RandomAccessibleInterval<O>> pp : iterativePostProcessingOps) {
				pp.mutate(raiExtendedEstimate);
			}

			// accelerate the algorithm by taking a larger step
			accelerator.mutate(raiExtendedEstimate);
		}

		// -- copy crop padded back to original size

		final long[] start = new long[out.numDimensions()];
		final long[] end = new long[out.numDimensions()];

		for (int d = 0; d < out.numDimensions(); d++) {
			start[d] = 0;
			end[d] = start[d] + out.dimension(d) - 1;
		}

		copy2Op.compute(Views.interval(raiExtendedEstimate, new FinalInterval(start,
			end)), out);
	}
}
