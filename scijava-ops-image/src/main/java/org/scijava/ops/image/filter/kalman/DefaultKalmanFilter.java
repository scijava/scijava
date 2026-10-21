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

package org.scijava.ops.image.filter.kalman;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Kalman stack filter: recursive per-pixel denoising of a time-lapse (or z)
 * series along one dimension.
 * <p>
 * Each pixel is modelled as a constant state {@code x} observed through noisy
 * measurements {@code z_t} (the successive frames). The scalar Kalman
 * estimator starts from {@code x_0 = z_0} with error variance {@code P_0 = 1}
 * and, for each later frame, predicts {@code x_t^- = x_(t-1)} and computes the
 * Kalman gain {@code K_t = P_(t-1) / (P_(t-1) + R)}, where {@code R} is the
 * measurement noise variance relative to the initial error variance; then
 * {@code P_t = (1 - K_t) P_(t-1)}, so that {@code K_t = 1 / (t + R)}. The
 * prediction bias {@code g} blends the Kalman estimate with the new frame:
 * </p>
 * <pre>
 * x_t = g (x_t^- + K_t (z_t - x_t^-)) + (1 - g) z_t
 * </pre>
 * <p>
 * With {@code g = 1}, this is the optimal estimate of a constant (with
 * {@code R = 1}, the running mean of all frames so far); smaller values of
 * {@code g} let the estimate follow changes, converging to an exponential
 * moving average with weight {@code 1 - g} on the newest frame. The output
 * frame {@code t} is the estimate {@code x_t}.
 * </p>
 * <p>
 * Implemented from the standard recursive (Kalman) estimator, e.g. R. E.
 * Kalman, "A new approach to linear filtering and prediction problems", J.
 * Basic Eng. 82(1):35-45, 1960; and G. Welch and G. Bishop, "An introduction
 * to the Kalman filter", UNC-Chapel Hill TR 95-041. The parameters follow the
 * Kalman stack filter for ImageJ (C. P. Mauer), whose code was not consulted.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @param <V> output pixel type
 * @implNote op names='filter.kalman'
 */
public class DefaultKalmanFilter<T extends RealType<T>, V extends RealType<V>>
	implements
	Computers.Arity4<RandomAccessibleInterval<T>, Integer, Double, Double, RandomAccessibleInterval<V>>
{

	/**
	 * Applies the Kalman filter along one dimension.
	 *
	 * @param input the image series
	 * @param dim the dimension along which to filter, e.g. time or z (default:
	 *          the last dimension)
	 * @param gain the prediction bias g within [0, 1]: the weight of the Kalman
	 *          estimate against the newest frame (default: 0.8)
	 * @param noiseVariance the measurement noise variance R, relative to the
	 *          error variance of the initial estimate (default: 0.05)
	 * @param output the filtered series, with the same dimensions as the input
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		@Nullable final Integer dim, @Nullable final Double gain,
		@Nullable final Double noiseVariance,
		final RandomAccessibleInterval<V> output)
	{
		final int d = dim == null ? input.numDimensions() - 1 : dim;
		final double g = gain == null ? 0.8 : gain;
		final double r = noiseVariance == null ? 0.05 : noiseVariance;
		if (d < 0 || d >= input.numDimensions()) {
			throw new IllegalArgumentException("No such dimension: " + d);
		}
		if (!(g >= 0 && g <= 1)) {
			throw new IllegalArgumentException("Gain must be within [0, 1]: " + g);
		}
		if (!(r >= 0)) {
			throw new IllegalArgumentException(
				"Noise variance must be non-negative: " + r);
		}
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output must have the same dimensions");
		}

		final var first = Views.hyperSlice(input, d, input.min(d));
		final var estimate = ArrayImgs.doubles(first.dimensionsAsLongArray());
		var p = 1.0;
		final var frames = input.dimension(d);
		for (long t = 0; t < frames; t++) {
			final var z = Views.hyperSlice(input, d, input.min(d) + t);
			final var out = Views.hyperSlice(output, d, output.min(d) + t);
			final double a;
			if (t == 0) {
				a = 1;
			}
			else {
				final var k = p + r == 0 ? 1 : p / (p + r);
				p = (1 - k) * p;
				a = g * k + 1 - g;
			}
			LoopBuilder.setImages(z, estimate, out).multiThreaded().forEachPixel((
				zi, x, o) -> {
				final var v = x.get() + a * (zi.getRealDouble() - x.get());
				x.set(v);
				o.setReal(v);
			});
		}
	}
}
