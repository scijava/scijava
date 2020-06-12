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

package net.imagej.ops2.filter;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.view.Views;

import org.scijava.ops.function.Computers;
import org.scijava.param.Mutable;

public final class ApplyCenterAwareNeighborhoodBasedFilter<I, O> {

	private static final OutOfBoundsFactory<?, ?> DEFAULT_OUT_OF_BOUNDS_FACTORY =
		new OutOfBoundsBorderFactory<>();

	@SuppressWarnings("unchecked")
	public static <I> OutOfBoundsFactory<I, RandomAccessibleInterval<I>>
		defaultOutOfBoundsFactory()
	{
		return (OutOfBoundsFactory<I, RandomAccessibleInterval<I>>) DEFAULT_OUT_OF_BOUNDS_FACTORY;
	}

	private ApplyCenterAwareNeighborhoodBasedFilter() {
		// Utility class
	}

	public static <I, O> void compute(final RandomAccessibleInterval<I> input,
		final Shape inputNeighborhoodShape,
		OutOfBoundsFactory<I, RandomAccessibleInterval<I>> outOfBoundsFactory,
		final Computers.Arity2<Iterable<I>, I, O> filterOp,
		@Mutable final IterableInterval<O> output)
	{
		if (outOfBoundsFactory == null) outOfBoundsFactory =
			defaultOutOfBoundsFactory();
		final RandomAccessibleInterval<I> inputCenterPixels = Views.interval(Views
			.extend(input, outOfBoundsFactory), input);
		final IterableInterval<? extends Iterable<I>> inputNeighborhoods =
			inputNeighborhoodShape.neighborhoodsSafe(inputCenterPixels);
		map(inputNeighborhoods, inputCenterPixels, filterOp, output);
	}

	private static <I1, I2, O> void map(
		final IterableInterval<? extends I1> inputNeighborhoods,
		final RandomAccessibleInterval<I2> inputCenterPixels,
		final Computers.Arity2<I1, I2, O> filterOp, final IterableInterval<O> output)
	{
		// TODO: This used to be done via a net.imagej.ops2.Ops.Map meta op. We may
		// want to revert to that approach if this proves to be too inflexible.
		// (Parallelization would be useful, for instance.) In this case, we would
		// need to make this static class a proper op, again (or let clients pass a
		// mapper op).
		final Cursor<? extends I1> neighborhoodCursor = inputNeighborhoods
			.localizingCursor();
		final RandomAccess<I2> centerPixelsAccess = inputCenterPixels
			.randomAccess();
		final Cursor<O> outputCursor = output.cursor();
		while (neighborhoodCursor.hasNext()) {
			neighborhoodCursor.fwd();
			centerPixelsAccess.setPosition(neighborhoodCursor);
			filterOp.compute(neighborhoodCursor.get(), centerPixelsAccess.get(),
				outputCursor.next());
		}
	}

}
