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

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Bins an image into a new image of the same type: downsamples it by an
 * integer factor along each dimension, reducing each block of input samples
 * to a single output sample. See {@link DefaultBin} for details.
 * <p>
 * Note that results are clamped to the range of the input type, so
 * {@code "sum"} binning of integer images can saturate; to avoid that, call
 * the computer version of {@code transform.bin} with a wider output type.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> image type
 * @implNote op names='transform.bin'
 */
public class DefaultBinFunction<T extends RealType<T> & NativeType<T>>
	implements
	Functions.Arity3<RandomAccessibleInterval<T>, long[], String, RandomAccessibleInterval<T>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, T, RandomAccessibleInterval<T>> createOp;

	/**
	 * Bins {@code input} into a newly created image.
	 *
	 * @param input the image to bin
	 * @param factors the bin factor for each dimension; must be positive. If
	 *          fewer factors than dimensions are given, the remaining
	 *          dimensions are not binned (factor 1).
	 * @param method the block reduction method: {@code "mean"} (default),
	 *          {@code "sum"}, {@code "min"}, {@code "max"}, {@code "median"} or
	 *          {@code "mode"}
	 * @return the binned image, with each dimension {@code d} of size
	 *         {@code floor(input.dimension(d) / factors[d])}, and zero min
	 */
	@Override
	public RandomAccessibleInterval<T> apply(
		final RandomAccessibleInterval<T> input, final long[] factors,
		@Nullable final String method)
	{
		final var f = Bins.factors(factors, input.numDimensions());
		final var m = Bins.method(method);
		final var output = createOp.apply(Bins.outputDimensions(input, f), input
			.getType().createVariable());
		Bins.bin(input, f, m, output);
		return output;
	}
}
