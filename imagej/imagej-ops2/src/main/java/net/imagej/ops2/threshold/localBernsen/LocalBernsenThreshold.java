/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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

package net.imagej.ops2.threshold.localBernsen;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.Nullable;

/**
 * @author Jonathan Hale
 * @author Stefan Helfrich (University of Konstanz)
 * @param <T> input type
 *@implNote op names='threshold.localBernsen'
 */
public class LocalBernsenThreshold<T extends RealType<T>> implements
	Computers.Arity5<RandomAccessibleInterval<T>, Shape, Double, Double, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
			RandomAccessibleInterval<BitType>> {

	@OpDependency(name = "threshold.localBernsen")
	private Computers.Arity4<Iterable<T>, T, Double, Double, BitType> computeThresholdOp;

	@OpDependency(name = "filter.applyCenterAware")
	private Computers.Arity4<RandomAccessibleInterval<T>, Computers.Arity2<Iterable<T>, T, BitType>, Shape, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<BitType>> applyFilterOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param inputNeighborhoodShape
	 * @param contrastThreshold
	 * @param halfMaxValue
	 * @param outOfBoundsFactory (required = false)
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Shape inputNeighborhoodShape, final Double contrastThreshold,
		final Double halfMaxValue,
		@Nullable OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final RandomAccessibleInterval<BitType> output)
	{
		if (outOfBoundsFactory == null) outOfBoundsFactory =
				new OutOfBoundsBorderFactory<>();
		compute(input, inputNeighborhoodShape, contrastThreshold, halfMaxValue,
			outOfBoundsFactory, computeThresholdOp, output);
	}

	public void compute(
		final RandomAccessibleInterval<T> input, final Shape inputNeighborhoodShape,
		final Double contrastThreshold, final Double halfMaxValue,
		final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final Computers.Arity4<Iterable<T>, T, Double, Double, BitType> computeThresholdOp,
		final RandomAccessibleInterval<BitType> output)
	{
		final Computers.Arity2<Iterable<T>, T, BitType> parametrizedComputeThresholdOp = //
			(i1, i2, o) -> computeThresholdOp.compute(i1, i2, contrastThreshold,
				halfMaxValue, o);
		applyFilterOp.compute(input, parametrizedComputeThresholdOp,
			inputNeighborhoodShape, outOfBoundsFactory,
			 output);
	}

}
