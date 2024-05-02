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

package org.scijava.ops.image.filter.dog;

import java.util.function.Function;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * Low-level difference of Gaussians (DoG) implementation which leans on other
 * ops to do the work.
 *
 * @author Christian Dietz (University of Konstanz)
 * @author Curtis Rueden
 * @param <T>
 * @implNote op names='filter.dog'
 */
public class DefaultDoG<T extends NumericType<T> & NativeType<T>> implements
	Computers.Arity3<RandomAccessibleInterval<T>, Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>, Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>>
{

	@OpDependency(name = "create.img")
	private Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> tmpCreator;

	@OpDependency(name = "math.subtract")
	private Computers.Arity2<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> subtracter;

	/**
	 * TODO
	 *
	 * @param input
	 * @param gauss1
	 * @param gauss2
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gauss1,
		final Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gauss2,
		final RandomAccessibleInterval<T> output)
	{
		// input may potentially be translated
		final long[] translation = new long[input.numDimensions()];
		input.min(translation);

		final IntervalView<T> tmpInterval = Views.interval(Views.translate(
			(RandomAccessible<T>) tmpCreator.apply(input), translation), output);

		gauss1.compute(input, tmpInterval);
		gauss2.compute(input, output);

		// TODO: is this safe?
		subtracter.compute(output, tmpInterval, output);
	}

}
