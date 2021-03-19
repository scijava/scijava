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

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Calculate non-circulant first guess. This is used as part of the Boundary
 * condition handling scheme described here
 * http://bigwww.epfl.ch/deconvolution/challenge2013/index.html?p=doc_math_rl)
 *
 * @author Brian Northan
 * @param <I>
 * @param <O>
 * @param <K>
 * @param <C>
 */

@Plugin(type = Op.class, name = "deconvolve.firstGuess", priority = Priority.LOW)
public class NonCirculantFirstGuess<I extends RealType<I>, O extends RealType<O>, K extends RealType<K>, C extends ComplexType<C>>
	implements
	Functions.Arity3<RandomAccessibleInterval<I>, O, Dimensions, RandomAccessibleInterval<O>>
{


	@OpDependency(name = "create.img")
	BiFunction<Dimensions, O, Img<O>> create;

	@OpDependency(name = "stats.sum")
	Computers.Arity1<Iterable<I>, DoubleType> sum;

	/**
	 * k is the size of the measurement window. That is the size of the acquired
	 * image before extension, k is required to calculate the non-circulant
	 * normalization factor
	 */
	/**
	 * TODO
	 *
	 * @param input
	 * @param outType
	 * @param k
	 * @return the output
	 */
	@Override
	public RandomAccessibleInterval<O> apply(RandomAccessibleInterval<I> in, final O outType, final Dimensions k) {

		final Img<O> firstGuess = create.apply(in, outType);

		// set first guess to be a constant = to the average value

		// so first compute the sum...
		final DoubleType s = new DoubleType();
		sum.compute(Views.iterable(in), s);

		// then the number of pixels
		long numPixels = 1;

		for (int d = 0; d < k.numDimensions(); d++) {
			numPixels = numPixels * k.dimension(d);
		}

		// then the average value...
		final double average = s.getRealDouble() / (numPixels);

		// set first guess as the average value computed above (TODO: use fill op)
		for (final O type : firstGuess) {
			type.setReal(average);
		}

		return firstGuess;
	}
}
