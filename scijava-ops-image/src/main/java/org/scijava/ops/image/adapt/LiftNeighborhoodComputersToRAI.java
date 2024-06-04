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

package org.scijava.ops.image.adapt;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.view.Views;
import org.scijava.function.Computers;

public final class LiftNeighborhoodComputersToRAI {

	private LiftNeighborhoodComputersToRAI() {
		// prevent instantiation of static utility class
	}

	/**
	 * @param op the original Op, operating on {@link Neighborhood}s
	 * @return {@code op} lifted to operate on {@link RandomAccessibleInterval}s.
	 * @implNote op names='engine.adapt', priority='100.', type=Function
	 */
	public static <T, U>
		Computers.Arity2<RandomAccessibleInterval<T>, Shape, RandomAccessibleInterval<U>>
		adapt1UsingShape(Computers.Arity1<Neighborhood<T>, U> op)
	{
		OutOfBoundsMirrorFactory<T, RandomAccessibleInterval<T>> oobf //
			= new OutOfBoundsMirrorFactory<>(
				OutOfBoundsMirrorFactory.Boundary.SINGLE);
		return (in, shape, out) -> {
			var extended = Views.extend(in, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in);
			LoopBuilder.setImages(intervaled, out).forEachPixel(op);
		};
	}

	/**
	 * @param op the original Op, operating on {@link Neighborhood}s
	 * @return {@code op} lifted to operate on {@link RandomAccessibleInterval}s.
	 * @implNote op names='engine.adapt', priority='100.', type=Function
	 */
	public static <T, U, F extends RandomAccessibleInterval<T>, G extends F>
		Computers.Arity3<G, Shape, OutOfBoundsFactory<T, F>, RandomAccessibleInterval<U>>
		adapt1UsingShapeAndOOBF(Computers.Arity1<Neighborhood<T>, U> op)
	{
		return (in, shape, oobf, out) -> {
			var extended = Views.extend(in, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in);
			LoopBuilder.setImages(intervaled, out).forEachPixel(op);
		};
	}

	/**
	 * @param op the original Op, operating on {@link Neighborhood}s
	 * @return {@code op} lifted to operate on {@link RandomAccessibleInterval}s.
	 * @implNote op names='engine.adapt', priority='100.', type=Function
	 */
	public static <T, U, V>
		Computers.Arity3<RandomAccessibleInterval<T>, V, Shape, RandomAccessibleInterval<U>>
		adapt2UsingShape(Computers.Arity2<Neighborhood<T>, V, U> op)
	{
		OutOfBoundsMirrorFactory<T, RandomAccessibleInterval<T>> oobf //
			= new OutOfBoundsMirrorFactory<>(
				OutOfBoundsMirrorFactory.Boundary.SINGLE);
		return (in1, in2, shape, out) -> {
			var extended = Views.extend(in1, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in1);
			LoopBuilder.setImages(intervaled, out).forEachPixel((in, container) -> op
				.compute(in, in2, container));
		};
	}

	/**
	 * @param op the original Op, operating on {@link Neighborhood}s
	 * @return {@code op} lifted to operate on {@link RandomAccessibleInterval}s.
	 * @implNote op names='engine.adapt', priority='100.', type=Function
	 */
	public static <T, U, V, F extends RandomAccessibleInterval<T>>
		Computers.Arity4<F, V, Shape, OutOfBoundsFactory<T, F>, RandomAccessibleInterval<U>>
		adapt2UsingShapeAndOOBF(Computers.Arity2<Neighborhood<T>, V, U> op)
	{
		return (in1, in2, shape, oobf, out) -> {
			var extended = Views.extend(in1, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in1);
			LoopBuilder.setImages(intervaled, out).forEachPixel((in, container) -> op
				.compute(in, in2, container));
		};
	}

	/**
	 * @param op the original Op, operating on {@link Neighborhood}s
	 * @return {@code op} lifted to operate on {@link RandomAccessibleInterval}s.
	 * @implNote op names='engine.adapt', priority='100.', type=Function
	 */
	public static <T, U, V, W>
		Computers.Arity4<RandomAccessibleInterval<T>, V, W, Shape, RandomAccessibleInterval<U>>
		adapt3UsingShape(Computers.Arity3<Neighborhood<T>, V, W, U> op)
	{
		OutOfBoundsMirrorFactory<T, RandomAccessibleInterval<T>> oobf //
			= new OutOfBoundsMirrorFactory<>(
				OutOfBoundsMirrorFactory.Boundary.SINGLE);
		return (in1, in2, in3, shape, out) -> {
			var extended = Views.extend(in1, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in1);
			LoopBuilder.setImages(intervaled, out).forEachPixel((in, container) -> op
				.compute(in, in2, in3, container));
		};
	}

	/**
	 * @param op the original Op, operating on {@link Neighborhood}s
	 * @return {@code op} lifted to operate on {@link RandomAccessibleInterval}s.
	 * @implNote op names='engine.adapt', priority='100.', type=Function
	 */
	public static <T, U, V, W, F extends RandomAccessibleInterval<T>>
		Computers.Arity5<F, V, W, Shape, OutOfBoundsFactory<T, F>, RandomAccessibleInterval<U>>
		adapt3UsingShapeAndOOBF(Computers.Arity3<Neighborhood<T>, V, W, U> op)
	{
		return (in1, in2, in3, shape, oobf, out) -> {
			var extended = Views.extend(in1, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in1);
			LoopBuilder.setImages(intervaled, out).forEachPixel((in, container) -> op
				.compute(in, in2, in3, container));
		};
	}
}
