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
 * This op follows the algorithm documented for the Kalman stack filter for
 * ImageJ by C. P. Mauer (<a href=
 * "https://imagej.net/ij/plugins/kalman.html">imagej.net/ij/plugins/kalman</a>).
 * Each pixel is modelled as a constant state observed through noisy
 * measurements {@code z_t} (the successive frames) with noise variance
 * {@code R}. The estimate is seeded with the first frame, {@code x_0 = z_0},
 * and the error variance with the noise variance, {@code P_0 = R}. For each
 * later frame, the previous estimate {@code x_(t-1)} is the prediction, and
 * </p>
 * <pre>
 * K_t = P_(t-1) / (P_(t-1) + R)
 * x_t = g x_(t-1) + (1 - g) z_t + K_t (z_t - x_(t-1))
 * P_t = (1 - K_t) P_(t-1)
 * </pre>
 * <p>
 * where {@code g} is the filter gain. The output frame {@code t} is the
 * estimate {@code x_t}. Since {@code P_0 = R}, the Kalman gain is
 * {@code K_t = 1 / (t + 1)} whatever the value of {@code R}, so the noise
 * variance does not affect the result and is not a parameter of this op.
 * With {@code g = 1}, the estimate is the running mean of all
 * frames so far. For smaller {@code g}, the Kalman term vanishes over time and
 * the estimate converges to an exponential moving average with weight
 * {@code 1 - g} on the newest frame. Each update moves the estimate by the
 * fraction {@code 1 - g + K_t} of the residual {@code z_t - x_(t-1)}; this
 * fraction exceeds 1, so that the estimate overshoots, if {@code g < K_t}.
 * Results are clamped to the range of the output type.
 * </p>
 * <p>
 * Implemented from the documented algorithm, whose code was not consulted;
 * see also R. E. Kalman, "A new approach to linear filtering and prediction
 * problems", J. Basic Eng. 82(1):35-45, 1960; and G. Welch and G. Bishop, "An
 * introduction to the Kalman filter", UNC-Chapel Hill TR 95-041.
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
	Computers.Arity3<RandomAccessibleInterval<T>, Integer, Double, RandomAccessibleInterval<V>>
{

	/**
	 * Applies the Kalman filter along one dimension.
	 *
	 * @param input the image series
	 * @param dim the dimension along which to filter, e.g. time or z (default:
	 *          the last dimension)
	 * @param gain the filter gain g within [0, 1]: the weight of the prediction
	 *          against the newest frame (default: 0.8)
	 * @param output the filtered series, with the same dimensions as the input
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		@Nullable final Integer dim, @Nullable final Double gain,
		final RandomAccessibleInterval<V> output)
	{
		final int d = dim == null ? input.numDimensions() - 1 : dim;
		final double g = gain == null ? 0.8 : gain;
		if (d < 0 || d >= input.numDimensions()) {
			throw new IllegalArgumentException("No such dimension: " + d);
		}
		if (!(g >= 0 && g <= 1)) {
			throw new IllegalArgumentException("Gain must be within [0, 1]: " + g);
		}
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output must have the same dimensions");
		}

		final var first = Views.hyperSlice(input, d, input.min(d));
		final var estimate = ArrayImgs.doubles(first.dimensionsAsLongArray());
		final var frames = input.dimension(d);
		for (long t = 0; t < frames; t++) {
			final var z = Views.hyperSlice(input, d, input.min(d) + t);
			final var out = Views.hyperSlice(output, d, output.min(d) + t);
			final double a;
			if (t == 0) {
				a = 1;
			}
			else {
				// x_t = g x + (1 - g) z + K (z - x) = x + (1 - g + K) (z - x)
				a = 1 - g + 1.0 / (t + 1);
			}
			LoopBuilder.setImages(z, estimate, out).multiThreaded().forEachPixel((
				zi, x, o) -> {
				final var v = x.get() + a * (zi.getRealDouble() - x.get());
				x.set(v);
				o.setReal(Math.max(o.getMinValue(), Math.min(o.getMaxValue(), v)));
			});
		}
	}
}
