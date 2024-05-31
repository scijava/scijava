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

package org.scijava.ops.image.map.neighborhood;

import org.scijava.function.Computers;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.view.Views;

/**
 * Evaluates a computer Op for each {@link Neighborhood} on the input
 * {@link RandomAccessibleInterval} and sets the value of the corresponding
 * pixel on the output {@link IterableInterval}. Similar to
 * {@link DefaultMapNeighborhood}, but passes the center pixel to the op as
 * well.
 *
 * @author Jonathan Hale (University of Konstanz)
 * @author Stefan Helfrich (University of Konstanz)
 * @implNote op names='map.neighborhood'
 */
public class MapNeighborhoodWithCenter<I, O> implements
	Computers.Arity3<RandomAccessibleInterval<I>, Shape, Computers.Arity2<Iterable<I>, I, O>, IterableInterval<O>>
{

	/**
	 * TODO
	 *
	 * @param input
	 * @param shape
	 * @param centerAwareOp
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<I> input,
		final Shape shape, final Computers.Arity2<Iterable<I>, I, O> centerAwareOp,
		final IterableInterval<O> output)
	{
		// TODO can we do this through a mapper/LoopBuilder?
		RandomAccess<I> inRA = input.randomAccess();
		Cursor<Neighborhood<I>> neighborhoodsCursor = shape.neighborhoodsSafe(input)
			.cursor();
		Cursor<O> outCursor = output.cursor();
		while (outCursor.hasNext()) {
			outCursor.fwd();
			inRA.setPosition(outCursor);
			neighborhoodsCursor.fwd();
			centerAwareOp.compute(neighborhoodsCursor.get(), inRA.get(), outCursor
				.get());
		}
	}

}

/**
 * Evaluates a computer for each {@link Neighborhood} on the input
 * {@link RandomAccessibleInterval} and sets the value of the corresponding
 * pixel on the output {@link RandomAccessibleInterval}. Similar to
 * {@link DefaultMapNeighborhood}, but passes the center pixel to the op as
 * well. This Op is set to higher priority than
 * {@link MapNeighborhoodWithCenter} since uses {@link LoopBuilder} to
 * multi-thread the process. Note that this process should be thread-safe so
 * long as the computer is state-less (which is a part of the contract for Ops).
 * We also assume that the input and output have identical dimensions.
 *
 * @author Gabriel Selzer
 * @implNote op names='map.neighborhood', priority='100.'
 */
class MapNeighborhoodWithCenterAllRAI<I, O> implements
	Computers.Arity3<RandomAccessibleInterval<I>, Shape, Computers.Arity2<Iterable<I>, I, O>, RandomAccessibleInterval<O>>
{

	/**
	 * TODO
	 *
	 * @param input
	 * @param shape
	 * @param centerAwareOp
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<I> input,
		final Shape shape, final Computers.Arity2<Iterable<I>, I, O> centerAwareOp,
		final RandomAccessibleInterval<O> output)
	{
		// generate a neighborhood image with the bounds of the input
		RandomAccessibleInterval<Neighborhood<I>> neighborhoodInput = Views
			.interval(shape.neighborhoodsRandomAccessibleSafe(input), input);

		LoopBuilder.setImages(neighborhoodInput, input, output).multiThreaded()
			.forEachPixel(centerAwareOp::compute);
	}

}
