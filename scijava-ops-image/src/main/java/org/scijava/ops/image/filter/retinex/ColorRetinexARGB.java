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

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Multi-scale retinex with colour restoration (MSRCR) of a packed
 * {@link ARGBType} image. The alpha channel is ignored. See {@link Retinex}
 * for details.
 * <p>
 * Implemented from D. J. Jobson, Z. Rahman and G. A. Woodell, "A multiscale
 * retinex for bridging the gap between color images and the human observation
 * of scenes", IEEE Trans. Image Process. 6(7):965-976, 1997.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @implNote op names='filter.retinex'
 */
public class ColorRetinexARGB implements
	Functions.Arity3<RandomAccessibleInterval<ARGBType>, double[], Double, RandomAccessibleInterval<FloatType>>
{

	@OpDependency(name = "filter.gauss")
	private Computers.Arity2<RandomAccessibleInterval<DoubleType>, Double, RandomAccessibleInterval<DoubleType>> gaussOp;

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> createOp;

	/**
	 * Computes the multi-scale retinex with colour restoration.
	 *
	 * @param input the ARGB image
	 * @param sigmas the Gaussian surround scales, in pixels, weighted equally
	 *          (default: 15, 80 and 250)
	 * @param dynamic the half-width, in standard deviations around the mean, of
	 *          the range mapped to [0, 1]; 0 returns the raw retinex values
	 *          (default: 1.2)
	 * @return the retinex image, with one more dimension than the input; the
	 *         last dimension holds the red, green and blue channels
	 */
	@Override
	public RandomAccessibleInterval<FloatType> apply(
		final RandomAccessibleInterval<ARGBType> input,
		@Nullable final double[] sigmas, @Nullable final Double dynamic)
	{
		final var rgb = Converters.argbChannels(input, 1, 2, 3);
		final var output = createOp.apply(rgb, new FloatType());
		final var last = rgb.numDimensions() - 1;
		Retinex.compute(Retinex.channels(rgb, last), sigmas, true, dynamic,
			gaussOp, Retinex.channels(output, last));
		return output;
	}
}
