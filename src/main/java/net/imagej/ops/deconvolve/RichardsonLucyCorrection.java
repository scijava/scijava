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

import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.Computer5;
import org.scijava.ops.core.computer.Computer7;
import org.scijava.ops.core.inplace.Inplace3First;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

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
@Parameter(key = "input")
@Parameter(key = "reblurred")
@Parameter(key = "fftBuffer")
@Parameter(key = "fftKernel")
@Parameter(key = "executorService")
@Parameter(key = "output", type = ItemIO.BOTH)
public class RichardsonLucyCorrection<I extends RealType<I>, O extends RealType<O>, C extends ComplexType<C>>
	implements
	Computer5<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, ExecutorService, RandomAccessibleInterval<O>>
{

	/** fft of reblurred (will be computed) **/
	private RandomAccessibleInterval<C> fftBuffer;

	/** fft of kernel (needs to be previously computed) **/
	private RandomAccessibleInterval<C> fftKernel;
	
	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, O, Img<O>> create;
	
	@OpDependency(name = "copy.rai")
	private Function<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> copy;

	//TODO is this allowed (to divide an O by an I)? Should it be?
//	@OpDependency(name = "math.divide") TODO: allow the matcher to fix this
	private Inplace3First<IterableInterval<O>, RandomAccessibleInterval<I>, Double> divide = (io, in2, in3) -> {
		Cursor<O> ioCursor = io.cursor();
		RandomAccess<I> inRA = in2.randomAccess();
		
		while(ioCursor.hasNext()) {
			Double val1 = ioCursor.next().getRealDouble();
			inRA.setPosition(ioCursor);
			Double val2 = inRA.get().getRealDouble();
			if(val2 != 0) System.out.println(val2);
			if(val1 == 0) ioCursor.next().setReal(in3);
			else ioCursor.next().setReal(val2 / val1);
		}
	};

	@OpDependency(name = "filter.correlate")
	private Computer7<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>, //
		RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, Boolean, //
		Boolean, ExecutorService, RandomAccessibleInterval<O>> correlateOp;

	/**
	 * computes the correction factor of the Richardson Lucy Algorithm
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
		divide.mutate(Views.iterable(reblurred), observed, 0.0);

		// correlate with psf to compute the correction factor
		// Note: FFT of psf is pre-computed and set as an input parameter of the op
		correlateOp.compute(reblurred, null, fftBuffer, fftKernel, true, false, es, correction);

	}

}
