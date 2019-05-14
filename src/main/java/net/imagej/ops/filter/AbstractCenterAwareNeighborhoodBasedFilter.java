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

package net.imagej.ops.filter;

import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.view.Views;

import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.param.Mutable;

public abstract class AbstractCenterAwareNeighborhoodBasedFilter<I, O>
	implements
	Computer4<RandomAccessibleInterval<I>, Shape, OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, //
			BiComputer<Iterable<I>, I, O>, IterableInterval<O>> {

	private static final OutOfBoundsFactory<?, ?> DEFAULT_OUT_OF_BOUNDS_FACTORY =
		new OutOfBoundsBorderFactory<>();

	@SuppressWarnings("unchecked")
	public static <I> OutOfBoundsFactory<I, RandomAccessibleInterval<I>>
		defaultOutOfBoundsFactory()
	{
		return (OutOfBoundsFactory<I, RandomAccessibleInterval<I>>) DEFAULT_OUT_OF_BOUNDS_FACTORY;
	}

	private UnaryComputerOp<RandomAccessibleInterval<I>, IterableInterval<O>> map;

	// TODO: Op dependency?
	private BiComputer<IterableInterval<Neighborhood<I>>, RandomAccessibleInterval<I>, IterableInterval<O>> newMap;

	// @Override
	// public void initialize() {
	// TODO: Lift (also see newMap). Do we want a "real" lifting mechanism here
	// (org.scijava.ops.util.Maps) or does it suffice to be ad-hoc here?
	// map = Computers.unary(ops(), Map.class, out(), in(), shape, filterOp);
	// }

	@Override
	public void compute(final RandomAccessibleInterval<I> input,
		final Shape neighborhoodShape,
		OutOfBoundsFactory<I, RandomAccessibleInterval<I>> outOfBoundsFactory,
		final BiComputer<Iterable<I>, I, O> filterOp,
		@Mutable final IterableInterval<O> output)
	{
		if (outOfBoundsFactory == null) outOfBoundsFactory =
			defaultOutOfBoundsFactory();

		final IterableInterval<Neighborhood<I>> inputNeighborhoods =
			neighborhoodShape.neighborhoodsSafe(Views.interval((Views.extend(input,
				outOfBoundsFactory)), input));
		final RandomAccessibleInterval<I> inputCenterPixels = input;
		newMap.compute(inputNeighborhoods, inputCenterPixels, output);
	}

}
