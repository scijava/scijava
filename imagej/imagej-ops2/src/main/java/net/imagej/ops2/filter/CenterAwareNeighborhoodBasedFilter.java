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

package net.imagej.ops2.filter;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.function.Container;
import org.scijava.ops.spi.Nullable;

/**
 *
 * @author Curtis Rueden
 * @author Mark Hiner
 * @param <I> input type
 * @param <O> output type
 * @implNote op names='filter.applyCenterAware'
 */
public class CenterAwareNeighborhoodBasedFilter<I, O> implements
		Computers.Arity4<RandomAccessibleInterval<I>, Computers.Arity2<Iterable<I>, I, O>, Shape, OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<O>> {

	/**
	 * TODO
	 *
	 * @param input
	 * @param filterOp
	 * @param inputNeighborhoodShape
	 * @param outOfBoundsFactory
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<I> input, //
		final Computers.Arity2<Iterable<I>, I, O> filterOp, //
		final Shape inputNeighborhoodShape, //
		@Nullable OutOfBoundsFactory<I, RandomAccessibleInterval<I>> outOfBoundsFactory, //
		@Container final RandomAccessibleInterval<O> output)
	{
		if (outOfBoundsFactory == null) outOfBoundsFactory =
			new OutOfBoundsBorderFactory<>();
		final RandomAccessibleInterval<I> inputCenterPixels = Views.interval(Views
			.extend(input, outOfBoundsFactory), input);
		final RandomAccessible<? extends Iterable<I>> inputNeighborhoods =
			inputNeighborhoodShape.neighborhoodsRandomAccessibleSafe(inputCenterPixels);
		final RandomAccessibleInterval<? extends Iterable<I>> inputNeighborhoodsRAI = Views.interval(inputNeighborhoods, input);
		LoopBuilder.setImages(inputNeighborhoodsRAI, inputCenterPixels, output)
			.multiThreaded() //
			.forEachPixel(filterOp::compute);
	}
}
