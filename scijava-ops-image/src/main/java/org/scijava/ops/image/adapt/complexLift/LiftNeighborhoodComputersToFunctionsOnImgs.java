/*-
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.adapt.complexLift;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.img.Img;
import net.imglib2.util.Util;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Producer;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpMethod;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.type.Type;
import net.imglib2.type.operators.SetZero;
import net.imglib2.view.Views;

public final class LiftNeighborhoodComputersToFunctionsOnImgs {

	private LiftNeighborhoodComputersToFunctionsOnImgs() {
		// prevent instantiation of static utility class
	}

	/**
	 * @param creator an Op that can create the output image
	 * @param op the original Op, operating on {@link Neighborhood}s
	 * @return {@code op} lifted to operate on {@link RandomAccessibleInterval}s.
	 * @implNote op names='engine.adapt', priority='100.',
	 *           type='java.util.function.Function'
	 */
	public static <T, U> BiFunction<RandomAccessibleInterval<T>, Shape, Img<T>>
		adaptUsingShape(@OpDependency(name = "engine.create", hints = {
			"adaptation.FORBIDDEN" }) BiFunction<Dimensions, T, Img<T>> creator,
			Computers.Arity1<Neighborhood<T>, T> op)
	{
        var oobf //
			= new OutOfBoundsMirrorFactory<T, RandomAccessibleInterval<T>>(
				OutOfBoundsMirrorFactory.Boundary.SINGLE);
		return (in, shape) -> {
			var output = creator.apply(in, Util.getTypeFromInterval(in));
			var extended = Views.extend(in, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in);
			LoopBuilder.setImages(intervaled, output).forEachPixel(op);
			return output;
		};
	}

	/**
	 * @param creator an Op that can create the output image
	 * @param op the original Op, operating on {@link Neighborhood}s
	 * @return {@code op} lifted to operate on {@link RandomAccessibleInterval}s.
	 * @implNote op names='engine.adapt', priority='100.',
	 *           type='java.util.function.Function'
	 */
	@OpMethod(names = "engine.adapt", type = Function.class)
	public static <T, F extends RandomAccessibleInterval<T>>
		Functions.Arity3<F, Shape, OutOfBoundsFactory<T, ? super F>, Img<T>>
		adaptUsingShapeAndOOBF(@OpDependency(name = "engine.create", hints = {
			"adaptation.FORBIDDEN" }) BiFunction<Dimensions, T, Img<T>> creator,
			Computers.Arity1<Neighborhood<T>, T> op)
	{
		return (in, shape, oobf) -> {
			var output = creator.apply(in, Util.getTypeFromInterval(in));
			var extended = Views.extend(in, oobf);
			var neighborhoods = shape.neighborhoodsRandomAccessible(extended);
			var intervaled = Views.interval(neighborhoods, in);
			LoopBuilder.setImages(intervaled, output).forEachPixel(op);
			return output;
		};
	}

}
