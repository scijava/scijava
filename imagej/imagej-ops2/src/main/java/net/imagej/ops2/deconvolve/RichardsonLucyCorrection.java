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

import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;

import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.ops.Op;
import org.scijava.ops.OpDependency;
import org.scijava.plugin.Plugin;

/**
 * Computes Richardson Lucy correction factor for (@link
 * RandomAccessibleInterval) (Lucy, L. B. (1974).
 * "An iterative technique for the rectification of observed distributions".)
 * 
 * @author Brian Northan
 * @param <I>
 * @param <O>
 * @param <C>
 */
@Plugin(type = Op.class, name = "deconvolve.richardsonLucyCorrection",
	priority = Priority.HIGH)
public class RichardsonLucyCorrection<I extends RealType<I>, O extends RealType<O>, C extends ComplexType<C>>
	implements
	Computers.Arity5<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, ExecutorService, RandomAccessibleInterval<O>>
{

	/** fft of reblurred (will be computed) **/
	private RandomAccessibleInterval<C> fftBuffer;

	/** fft of kernel (needs to be previously computed) **/
	private RandomAccessibleInterval<C> fftKernel;
	
	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, O, Img<O>> create;
	
	@OpDependency(name = "copy.rai")
	private Function<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> copy;

//	@OpDependency(name = "math.divide") TODO: match an op here?
	private BiConsumer<RandomAccessibleInterval<O>, RandomAccessibleInterval<I>> divide = (denomResult, numer) -> {
		final O tmp = Util.getTypeFromInterval(denomResult).createVariable();
		LoopBuilder.setImages(denomResult, numer).forEachPixel((d, n) -> {
			if (d.getRealFloat() > 0) {
				tmp.setReal(n.getRealFloat());
				tmp.div(d);
				d.set(tmp);
			}
			else d.setZero();
		});
	};

	@OpDependency(name = "filter.correlate")
	private Computers.Arity7<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>, //
		RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, Boolean, //
		Boolean, ExecutorService, RandomAccessibleInterval<O>> correlateOp;

	/**
	 * computes the correction factor of the Richardson Lucy Algorithm
	 *
	 * @param input
	 * @param reblurred
	 * @param fftBuffer
	 * @param fftKernel
	 * @param executorService
	 * @param output
	 */
	@Override
	public void compute(RandomAccessibleInterval<I> observed,
		RandomAccessibleInterval<O> reblurred,
		RandomAccessibleInterval<C> fftBuffer,
		RandomAccessibleInterval<C> fftKernel,
		ExecutorService es,
		RandomAccessibleInterval<O> correction)
	{
		// divide observed image by reblurred
		// reblurred = observed / reblurred
		divide.accept(reblurred, observed);

		// correlate with psf to compute the correction factor
		// Note: FFT of psf is pre-computed and set as an input parameter of the op
		correlateOp.compute(reblurred, null, fftBuffer, fftKernel, true, false, es, correction);

	}

}
