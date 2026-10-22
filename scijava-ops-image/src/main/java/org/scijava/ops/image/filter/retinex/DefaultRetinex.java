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

package org.scijava.ops.image.filter.retinex;

import java.util.List;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Multi-scale retinex (MSR) of a single-channel image, for dynamic range
 * compression and removal of smooth illumination variations. All dimensions
 * are smoothed, so a multichannel image passed without a channel dimension is
 * treated as a single channel, with the surrounds mixing the channels; use the
 * {@code filter.retinex} variant with {@code channelDim} for colour images.
 * See {@link Retinex} for details.
 * <p>
 * Implemented from D. J. Jobson, Z. Rahman and G. A. Woodell, "A multiscale
 * retinex for bridging the gap between color images and the human observation
 * of scenes", IEEE Trans. Image Process. 6(7):965-976, 1997. Each scale is
 * applied to the original image.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @implNote op names='filter.retinex'
 */
public class DefaultRetinex<T extends RealType<T>> implements
	Functions.Arity3<RandomAccessibleInterval<T>, double[], Double, RandomAccessibleInterval<FloatType>>
{

	@OpDependency(name = "filter.gauss")
	private Computers.Arity2<RandomAccessibleInterval<DoubleType>, Double, RandomAccessibleInterval<DoubleType>> gaussOp;

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> createOp;

	/**
	 * Computes the multi-scale retinex.
	 *
	 * @param input the image
	 * @param sigmas the Gaussian surround scales, in pixels, weighted equally
	 *          (default: 15, 80 and 250)
	 * @param dynamic the half-width, in standard deviations around the mean, of
	 *          the range mapped to [0, 1]; 0 returns the raw retinex values
	 *          (default: 1.2)
	 * @return the retinex image
	 */
	@Override
	public RandomAccessibleInterval<FloatType> apply(
		final RandomAccessibleInterval<T> input, @Nullable final double[] sigmas,
		@Nullable final Double dynamic)
	{
		final var output = createOp.apply(input, new FloatType());
		Retinex.compute(List.of(input), sigmas, false, dynamic, gaussOp, List.of(
			output));
		return output;
	}
}
