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

package org.scijava.ops.image.deconvolve.wiener;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Tikhonov-regularized inverse filter with float output, a convenience for
 * {@link TikhonovFilter}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @implNote op names='deconvolve.tikhonov', priority='100.'
 */
public class SimpleTikhonovFilter<I extends RealType<I> & NativeType<I>, K extends RealType<K> & NativeType<K>>
	implements
	Functions.Arity4<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, Double, Integer, RandomAccessibleInterval<FloatType>>
{

	@OpDependency(name = "deconvolve.tikhonov")
	private Functions.Arity8<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, FloatType, ComplexFloatType, Double, Integer, long[], OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<FloatType>> tikhonovOp;

	/**
	 * Deconvolves an image with a Tikhonov-regularized inverse filter.
	 *
	 * @param input the image to deconvolve
	 * @param kernel the PSF, normalized to unit sum
	 * @param lambda the regularization parameter, at least 0 (default 1e-3)
	 * @param order the derivative order of the regularization operator: 0 for
	 *          identity, 1 for gradient, or 2 for Laplacian (default 2)
	 * @return the deconvolved image, with the dimensions of the input
	 */
	@Override
	public RandomAccessibleInterval<FloatType> apply( //
		final RandomAccessibleInterval<I> input, //
		final RandomAccessibleInterval<K> kernel, //
		@Nullable final Double lambda, //
		@Nullable final Integer order //
	) {
		return tikhonovOp.apply(input, kernel, new FloatType(),
			new ComplexFloatType(), lambda, order, null, null);
	}
}
