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

package org.scijava.ops.image.filter.wavelet;

import java.util.Arrays;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Wavelet denoising by hard thresholding of the starlet (&agrave; trous)
 * transform, as described by Starck and Murtagh.
 * <p>
 * The image is decomposed into {@code nScales} wavelet planes and a residual
 * (see {@code filter.waveletATrous}). The standard deviation of the
 * (assumed white Gaussian) noise is estimated from the finest plane as
 * {@code sigma = MAD(w_1) / 0.6745 / e_1}, where {@code MAD} is the median
 * absolute deviation from the median and {@code e_j} is the standard
 * deviation of the coefficients of plane {@code j} for unit-variance white
 * noise (computed exactly from the filter, for the number of non-singleton
 * dimensions of the input). Each coefficient of plane {@code j} is kept if
 * its magnitude exceeds {@code k * sigma * e_j}, and set to zero otherwise.
 * The result is the residual plus all thresholded planes.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @see "J.-L. Starck and F. Murtagh, Astronomical Image and Data Analysis,
 *      2nd ed., Springer, 2006"
 * @implNote op names='filter.waveletDenoise'
 */
public class WaveletDenoise<T extends RealType<T>> implements
	Functions.Arity3<RandomAccessibleInterval<T>, Integer, Double, RandomAccessibleInterval<FloatType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> createOp;

	/**
	 * Denoises an image.
	 *
	 * @param input the image to denoise
	 * @param nScales the number of wavelet scales (default: 4)
	 * @param k the threshold, in multiples of the noise standard deviation of
	 *          each scale (default: 3)
	 * @return the denoised image
	 */
	@Override
	public RandomAccessibleInterval<FloatType> apply(
		final RandomAccessibleInterval<T> input, @Nullable final Integer nScales,
		@Nullable final Double k)
	{
		final int scales = nScales == null ? 4 : nScales;
		final double kSigma = k == null ? 3 : k;
		final var size = Intervals.numElements(input);
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image is too large: " + size +
				" pixels");
		}
		var nDims = 0;
		for (var d = 0; d < input.numDimensions(); d++)
			if (input.dimension(d) > 1) nDims++;
		final var levels = Starlet.noiseLevels(scales, nDims);
		final var planes = Starlet.decompose(input, scales, () -> createOp.apply(
			input, new FloatType()));
		final var sigma = mad(planes.get(0), (int) size) / 0.6745 / levels[0];

		final var output = planes.get(scales);
		for (var j = 0; j < scales; j++) {
			final var t = kSigma * sigma * levels[j];
			LoopBuilder.setImages(planes.get(j), output).forEachPixel((w, o) -> {
				if (Math.abs(w.getRealDouble()) > t) o.add(w);
			});
		}
		return output;
	}

	/** Computes the median absolute deviation from the median. */
	private static double mad(final RandomAccessibleInterval<FloatType> img,
		final int size)
	{
		final var values = new double[size];
		var i = 0;
		for (final var t : Views.flatIterable(img))
			values[i++] = t.getRealDouble();
		final var median = median(values);
		for (i = 0; i < size; i++)
			values[i] = Math.abs(values[i] - median);
		return median(values);
	}

	private static double median(final double[] values) {
		final var sorted = values.clone();
		Arrays.sort(sorted);
		final var m = sorted.length / 2;
		return sorted.length % 2 == 1 ? sorted[m] : (sorted[m - 1] + sorted[m]) /
			2;
	}
}
