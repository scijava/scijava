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

package org.scijava.ops.image.filter.kuwahara;

import java.util.Arrays;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Kuwahara filter: edge-preserving smoothing in <i>n</i> dimensions.
 * <p>
 * The {@code size}<sup>n</sup> window around each pixel is split into the
 * 2<sup>n</sup> overlapping sub-windows (quadrants in 2D, octants in 3D) of
 * side {@code (size + 1) / 2} that have the pixel as a corner. The output is
 * the mean of the sub-window with the lowest variance (the first one, in case
 * of ties). Window means and variances are computed in constant time from
 * integral images of the values and of their squares.
 * </p>
 * <p>
 * Implemented from the description by M. Kuwahara, K. Hachimura, S. Eiho and
 * M. Kinoshita, "Processing of RI-angiocardiographic images", in K. Preston and
 * M. Onoe (eds.), Digital Processing of Biomedical Images, Plenum Press, pp.
 * 187-202, 1976. For 2D 8-bit images, the result agrees with the Fiji
 * Kuwahara_Filter plugin up to rounding, away from the image border.
 * </p>
 * <p>
 * Results are clamped to the range of the output type, so an integer output
 * saturates rather than wrapping around.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @param <V> output pixel type
 * @implNote op names='filter.kuwahara'
 */
public class DefaultKuwahara<T extends RealType<T>, V extends RealType<V>>
	implements
	Computers.Arity3<RandomAccessibleInterval<T>, Integer, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<V>>
{

	@OpDependency(name = "image.integral")
	private Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<DoubleType>> integralOp;

	@OpDependency(name = "image.squareIntegral")
	private Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<DoubleType>> squareIntegralOp;

	/**
	 * Applies the Kuwahara filter.
	 *
	 * @param input the image to filter
	 * @param size the (odd) side length of the full window, in pixels (default:
	 *          5)
	 * @param outOfBoundsFactory the strategy for values beyond the image border
	 *          (default: mirror, without repeating the border pixel)
	 * @param output the filtered image
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		@Nullable final Integer size,
		@Nullable OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final RandomAccessibleInterval<V> output)
	{
		final int windowSize = size == null ? 5 : size;
		if (windowSize < 3 || windowSize % 2 == 0) {
			throw new IllegalArgumentException(
				"Window size must be odd and at least 3: " + windowSize);
		}
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output must have the same dimensions");
		}
		if (outOfBoundsFactory == null) outOfBoundsFactory =
			new OutOfBoundsMirrorFactory<>(OutOfBoundsMirrorFactory.Boundary.SINGLE);

		final var n = input.numDimensions();
		final var r = (windowSize - 1) / 2;

		// The padded input, with zero min.
		final var padded = Views.zeroMin(Views.interval(Views.extend(input,
			outOfBoundsFactory), Intervals.expand(input, r)));

		// Integral images with a leading row of zeros along each dimension, so
		// that sums[i] holds the sum of padded[0..i-1].
		final var dims = padded.dimensionsAsLongArray();
		final var integralDims = new long[n];
		for (var d = 0; d < n; d++)
			integralDims[d] = dims[d] + 1;
		final var sums = ArrayImgs.doubles(integralDims);
		final var squares = ArrayImgs.doubles(integralDims);
		final var inner = new FinalInterval(ones(n), dims);
		integralOp.compute(padded, Views.zeroMin(Views.interval(sums, inner)));
		squareIntegralOp.compute(padded, Views.zeroMin(Views.interval(squares,
			inner)));

		final var sumAccess = sums.randomAccess();
		final var sqAccess = squares.randomAccess();
		final var subwindows = 1 << n;
		final var count = Math.pow(r + 1, n);
		final var lo = new long[n];
		final var hi = new long[n];
		final var outMin = output.minAsLongArray();
		final var cursor = Views.flatIterable(output).localizingCursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			var bestVar = Double.POSITIVE_INFINITY;
			var bestMean = 0.0;
			for (var w = 0; w < subwindows; w++) {
				for (var d = 0; d < n; d++) {
					// Position in padded coordinates is (pos - min + r).
					final var c = cursor.getLongPosition(d) - outMin[d] + r;
					lo[d] = (w >> d & 1) == 0 ? c - r : c;
					hi[d] = lo[d] + r + 1;
				}
				final var mean = boxSum(sumAccess, lo, hi) / count;
				final var variance = boxSum(sqAccess, lo, hi) / count - mean * mean;
				if (variance < bestVar) {
					bestVar = variance;
					bestMean = mean;
				}
			}
			final var o = cursor.get();
			o.setReal(Math.max(o.getMinValue(), Math.min(o.getMaxValue(),
				bestMean)));
		}
	}

	/**
	 * Sums the values in the half-open box {@code [lo, hi)} using an integral
	 * image with a leading zero row, by inclusion-exclusion over its corners.
	 */
	private static double boxSum(final RandomAccess<DoubleType> integral,
		final long[] lo, final long[] hi)
	{
		final var n = lo.length;
		var sum = 0.0;
		for (var corner = 0; corner < 1 << n; corner++) {
			var sign = 1;
			for (var d = 0; d < n; d++) {
				if ((corner >> d & 1) == 0) {
					integral.setPosition(hi[d], d);
				}
				else {
					integral.setPosition(lo[d], d);
					sign = -sign;
				}
			}
			sum += sign * integral.get().get();
		}
		return sum;
	}

	private static long[] ones(final int n) {
		final var a = new long[n];
		Arrays.fill(a, 1);
		return a;
	}
}
