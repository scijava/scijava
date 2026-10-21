/*
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

package org.scijava.ops.image.transform.focus;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Computes the index map (height map) of a focal series: for every pixel, the
 * index of the slice with the highest focus measure, as used by the max
 * method of {@code transform.focusStack} (see there for the focus measure).
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @implNote op names='transform.focusStackIndex'
 */
public class FocusStackIndex<T extends RealType<T>> implements
	Functions.Arity3<RandomAccessibleInterval<T>, Integer, Double, RandomAccessibleInterval<IntType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> createOp;

	@OpDependency(name = "filter.gauss")
	private Computers.Arity3<RandomAccessibleInterval<T>, double[], OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<FloatType>> gaussOp;

	@OpDependency(name = "filter.gauss")
	private Computers.Arity3<RandomAccessibleInterval<FloatType>, double[], OutOfBoundsFactory<FloatType, RandomAccessibleInterval<FloatType>>, RandomAccessibleInterval<FloatType>> gaussFloatOp;

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, IntType, RandomAccessibleInterval<IntType>> createIntOp;

	/**
	 * Computes the index of the sharpest slice per pixel.
	 *
	 * @param input the focal series
	 * @param focusDim the index of the focus (z) dimension
	 * @param sigma the standard deviation, in pixels, of the Gaussian window
	 *          over which the focus measure is averaged (default: 2)
	 * @return the slice indices (starting at 0), with the focus dimension
	 *         removed
	 */
	@Override
	public RandomAccessibleInterval<IntType> apply(
		final RandomAccessibleInterval<T> input, final Integer focusDim,
		@Nullable final Double sigma)
	{
		final var stacker = new FocusStacker<>(input, focusDim, sigma, false,
			createOp, gaussOp, gaussFloatOp);
		final var output = createIntOp.apply(Views.hyperSlice(input, focusDim,
			input.min(focusDim)), new IntType());
		var i = 0;
		for (final var t : Views.flatIterable(output))
			t.set(stacker.index[i++]);
		return output;
	}
}
