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

package org.scijava.ops.image.filter.sigma;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.Nullable;

/**
 * Default implementation of {@link SigmaFilterOp}.
 *
 * @author Jonathan Hale (University of Konstanz)
 * @param <T> type
 * @implNote op names='filter.sigma', priority='-100.'
 */
public class DefaultSigmaFilter<T extends RealType<T>, V extends RealType<V>>
	implements
	Computers.Arity5<RandomAccessibleInterval<T>, Shape, Double, Double, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<V>>
{

	@OpDependency(name = "stats.variance")
	private Computers.Arity1<Iterable<T>, DoubleType> varianceOp;

	@OpDependency(name = "map.neighborhood")
	private Computers.Arity3<RandomAccessibleInterval<T>, Shape, Computers.Arity2<Iterable<T>, T, V>, RandomAccessibleInterval<V>> mapper;

	/**
	 * TODO
	 *
	 * @param input
	 * @param inputNeighborhoodShape
	 * @param range
	 * @param minPixelFraction
	 * @param outOfBoundsFactory
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Shape inputNeighborhoodShape, final Double range,
		final Double minPixelFraction,
		@Nullable OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final RandomAccessibleInterval<V> output)
	{
		if (outOfBoundsFactory == null) outOfBoundsFactory =
			new OutOfBoundsMirrorFactory<>(OutOfBoundsMirrorFactory.Boundary.SINGLE);

		if (range <= 0) throw new IllegalArgumentException(
			"range must be positive!");
		Computers.Arity2<Iterable<T>, T, V> mappedOp = (in1, in2, out) -> op
			.compute(in1, in2, range, minPixelFraction, out);
		RandomAccessibleInterval<T> extended = Views.interval((Views.extend(input,
			outOfBoundsFactory)), input);
		mapper.compute(extended, inputNeighborhoodShape, mappedOp, output);
	}

	final Computers.Arity4<Iterable<T>, T, Double, Double, V> op =
		new Computers.Arity4<Iterable<T>, T, Double, Double, V>()
		{

			@Override
			public void compute(final Iterable<T> neighborhood, final T center,
				final Double range, final Double minPixelFraction, final V output)
		{

				DoubleType varianceResult = new DoubleType();
				varianceOp.compute(neighborhood, varianceResult);
				double varianceValue = varianceResult.getRealDouble() * range;

				final double centerValue = center.getRealDouble();
				double sumAll = 0;
				double sumWithin = 0;
				long countAll = 0;
				long countWithin = 0;

				for (T neighbor : neighborhood) {
					final double pixelValue = neighbor.getRealDouble();
					final double diff = centerValue - pixelValue;

					sumAll += pixelValue;
					++countAll;

					if (diff > varianceValue || diff < -varianceValue) {
						continue;
					}

					// pixel within variance range
					sumWithin += pixelValue;
					++countWithin;
				}

				if (countWithin < (int) (minPixelFraction * countAll)) {
					output.setReal(sumAll / countAll); // simply mean
				}
				else {
					// mean over pixels in variance range only
					output.setReal(sumWithin / countWithin);
				}
			}

		};

}
