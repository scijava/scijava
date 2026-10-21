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

package org.scijava.ops.image.transform.bin;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Bins an image: downsamples it by an integer factor along each dimension,
 * reducing each block of {@code factors[0] × factors[1] × ...} input samples
 * to a single output sample.
 * <p>
 * Supported reduction methods are {@code "mean"} (the default), {@code "sum"},
 * {@code "min"}, {@code "max"}, {@code "median"} and {@code "mode"} (the most
 * frequent value, ties resolved toward the smallest value; the right choice
 * for label images). Each output dimension is the corresponding input
 * dimension divided by its factor, rounded down: incomplete blocks at the
 * upper edges are discarded, as in ImageJ's <em>Image &gt; Transform &gt;
 * Bin</em>. Results are clamped to the range of the output type, and rounded
 * (half up) for integer output types.
 * </p>
 * <p>
 * Unlike {@code transform.subsampleView}, which simply decimates, this op
 * aggregates every input sample.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <I> input type
 * @param <O> output type
 * @implNote op names='transform.bin'
 */
public class DefaultBin<I extends RealType<I>, O extends RealType<O>>
	implements
	Computers.Arity3<RandomAccessibleInterval<I>, long[], String, RandomAccessibleInterval<O>>
{

	/**
	 * Bins {@code input} into {@code output}.
	 *
	 * @param input the image to bin
	 * @param factors the bin factor for each dimension; must be positive. If
	 *          fewer factors than dimensions are given, the remaining
	 *          dimensions are not binned (factor 1).
	 * @param method the block reduction method: {@code "mean"} (default),
	 *          {@code "sum"}, {@code "min"}, {@code "max"}, {@code "median"} or
	 *          {@code "mode"}
	 * @param output the binned image, with each dimension {@code d} of size
	 *          {@code floor(input.dimension(d) / factors[d])}
	 */
	@Override
	public void compute(final RandomAccessibleInterval<I> input,
		final long[] factors, @Nullable final String method,
		final RandomAccessibleInterval<O> output)
	{
		final var f = Bins.factors(factors, input.numDimensions());
		Bins.bin(input, f, Bins.method(method), output);
	}
}
