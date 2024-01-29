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

package org.scijava.ops.image.image.integral;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;

/**
 * Abstract base class for <i>n</i>-dimensional integral images.
 *
 * @param <I> The type of the input image.
 * @author Stefan Helfrich (University of Konstanz)
 */
public abstract class AbstractIntegralImg<I extends RealType<I>, O extends RealType<O>>
	implements
	Computers.Arity1<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>>
{

	@Override
	public void compute(final RandomAccessibleInterval<I> input,
		final RandomAccessibleInterval<O> output)
	{
		// make sure the input and output have the same iteration order
		if (!Views.iterable(input).iterationOrder().equals(Views.iterable(output)
			.iterationOrder())) throw new IllegalArgumentException(
				"Input and Output images must have the same iteration order!");

		// We need an intermediary to accumulate the difference over multiple
		// dimensions. We want to make it of type O so that the following for loop
		// runs
		// nice, but to do that we need the intermediary to start with the input
		// data
		// (so we convert the input to type O here)
		RandomAccessibleInterval<O> generalizedInput = Converters.convert(input,
			(Converter<I, O>) (arg0, arg1) -> arg1.setReal(arg0.getRealDouble()),
			Views.iterable(output).firstElement().createVariable());

		// Create integral image
		for (int i = 0; i < input.numDimensions(); ++i) {
			// Slicewise integral addition in one direction
			// TODO can we find a way to parallelize this?
			for (int j = 0; j < input.dimension(i); j++)
				getComputer(i).compute(Views.hyperSlice(generalizedInput, i, j), Views
					.hyperSlice(output, i, j));
			generalizedInput = output;
		}
	}

	/**
	 * Implements the row-wise addition required for computations of integral
	 * images.
	 */
	public abstract
		Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>>
		getComputer(int dimension);

	/*
	 * Computers used in the row-wise addition
	 */

	public final Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> computeAdd =
		(input, output) -> {

			final Cursor<O> inputCursor = Views.iterable(input).cursor();
			final Cursor<O> outputCursor = Views.iterable(output).cursor();

			double tmp = 0.0d;
			while (outputCursor.hasNext()) {

				final O inputValue = inputCursor.next();
				final O outputValue = outputCursor.next();

				tmp += inputValue.getRealDouble();

				outputValue.setReal(tmp);
			}
		};

	public final Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> computeSquareAndAdd =
		(input, output) -> {

			final Cursor<O> inputCursor = Views.iterable(input).cursor();
			final Cursor<O> outputCursor = Views.iterable(output).cursor();

			double tmp = 0.0d;
			while (outputCursor.hasNext()) {

				final O inputValue = inputCursor.next();
				final O outputValue = outputCursor.next();

				tmp += Math.pow(inputValue.getRealDouble(), 2);

				outputValue.setReal(tmp);
			}
		};

}
