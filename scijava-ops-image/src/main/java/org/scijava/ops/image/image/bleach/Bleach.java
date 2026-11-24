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

package org.scijava.ops.image.image.bleach;

import java.util.Arrays;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.SimpleCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

/**
 * Utility methods for photobleaching correction and histogram matching.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
final class Bleach {

	/** The function {@code a * exp(-b t) + c} with parameters (a, b, c). */
	static final ParametricUnivariateFunction EXP_OFFSET =
		new ParametricUnivariateFunction()
		{

			@Override
			public double value(final double t, final double... p) {
				return p[0] * Math.exp(-p[1] * t) + p[2];
			}

			@Override
			public double[] gradient(final double t, final double... p) {
				final var e = Math.exp(-p[1] * t);
				return new double[] { e, -p[0] * t * e, 1 };
			}
		};

	private Bleach() {
		// Prevent instantiation of utility class.
	}

	/** Checks that a dimension index is valid for the given image. */
	static void checkDim(final RandomAccessibleInterval<?> img, final int d) {
		if (d < 0 || d >= img.numDimensions()) {
			throw new IllegalArgumentException("Invalid dimension " + d +
				" for a " + img.numDimensions() + "-dimensional image");
		}
	}

	/** Returns the hyperslice at index {@code t} (relative to the minimum). */
	static <T> RandomAccessibleInterval<T> frame(
		final RandomAccessibleInterval<T> img, final int timeDim, final long t)
	{
		return Views.hyperSlice(img, timeDim, img.min(timeDim) + t);
	}

	/** Computes the mean intensity of each frame along the given dimension. */
	static <T extends RealType<T>> double[] frameMeans(
		final RandomAccessibleInterval<T> input, final int timeDim)
	{
		checkDim(input, timeDim);
		final var means = new double[(int) input.dimension(timeDim)];
		for (var t = 0; t < means.length; t++) {
			double sum = 0;
			long count = 0;
			for (final var v : frame(input, timeDim, t)) {
				sum += v.getRealDouble();
				count++;
			}
			means[t] = sum / count;
		}
		return means;
	}

	/**
	 * Fits {@code y = a * exp(-b t) + c} to the given values (with
	 * {@code t = 0, 1, ...}) by Levenberg-Marquardt least squares.
	 *
	 * @return the parameters {@code (a, b, c)}
	 */
	static double[] fitExponential(final double[] y) {
		var min = Double.POSITIVE_INFINITY;
		var max = Double.NEGATIVE_INFINITY;
		for (final var v : y) {
			min = Math.min(min, v);
			max = Math.max(max, v);
		}
		final var mean = Arrays.stream(y).average().orElse(0);
		if (y.length < 3 || !(max - min > 1e-12 * Math.max(1, Math.abs(max)))) {
			return new double[] { 0, 0, mean };
		}
		// Initial guess: linear regression of log(y - c0) against t.
		final var c0 = min - 0.1 * (max - min);
		double st = 0, sz = 0, stt = 0, stz = 0;
		for (var t = 0; t < y.length; t++) {
			final var z = Math.log(y[t] - c0);
			st += t;
			sz += z;
			stt += t * t;
			stz += t * z;
		}
		final var n = y.length;
		final var slope = (n * stz - st * sz) / (n * stt - st * st);
		final var intercept = (sz - slope * st) / n;
		final var points = new WeightedObservedPoints();
		for (var t = 0; t < n; t++)
			points.add(t, y[t]);
		return SimpleCurveFitter.create(EXP_OFFSET, new double[] { Math.exp(
			intercept), -slope, c0 }).withMaxIterations(10000).fit(points.toList());
	}

	/**
	 * Writes {@code input * factor} to {@code output}, clamped to the range of
	 * the output type.
	 */
	static <T extends RealType<T>, V extends RealType<V>> void scale(
		final RandomAccessibleInterval<T> input, final double factor,
		final RandomAccessibleInterval<V> output)
	{
		final var c = Views.flatIterable(input).cursor();
		final var o = Views.flatIterable(output).cursor();
		while (c.hasNext()) {
			final var out = o.next();
			final var v = c.next().getRealDouble() * factor;
			out.setReal(Math.max(out.getMinValue(), Math.min(out.getMaxValue(), v)));
		}
	}

	/**
	 * Maps the values of {@code input} so that their cumulative distribution
	 * matches that of {@code reference}. A value {@code v} of the input, with
	 * {@code F(v)} the fraction of input values less than or equal to
	 * {@code v}, maps to the smallest reference value {@code r} such that the
	 * fraction of reference values less than or equal to {@code r} is at least
	 * {@code F(v)}.
	 */
	static <T extends RealType<T>, U extends RealType<U>, V extends RealType<V>>
		void matchHistogram(final RandomAccessibleInterval<T> input,
			final RandomAccessibleInterval<U> reference,
			final RandomAccessibleInterval<V> output)
	{
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output must have the same dimensions");
		}
		final var in = values(input);
		final var ref = values(reference);
		if (ref.length == 0) {
			throw new IllegalArgumentException("Reference image is empty");
		}
		final var sortedIn = in.clone();
		Arrays.sort(sortedIn);
		Arrays.sort(ref);
		var i = 0;
		for (final var o : Views.flatIterable(output)) {
			final var atMost = upperBound(sortedIn, in[i++]);
			final var k = (int) Math.ceil((double) atMost * ref.length /
				sortedIn.length) - 1;
			o.setReal(ref[Math.max(0, Math.min(ref.length - 1, k))]);
		}
	}

	private static <T extends RealType<T>> double[] values(
		final RandomAccessibleInterval<T> img)
	{
		final var size = Intervals.numElements(img);
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image is too large: " + size +
				" pixels");
		}
		final var values = new double[(int) size];
		var i = 0;
		for (final var t : Views.flatIterable(img))
			values[i++] = t.getRealDouble();
		return values;
	}

	/** Returns the number of elements of a sorted array that are at most v. */
	private static int upperBound(final double[] sorted, final double v) {
		int lo = 0, hi = sorted.length;
		while (lo < hi) {
			final var mid = (lo + hi) >>> 1;
			if (sorted[mid] <= v) lo = mid + 1;
			else hi = mid;
		}
		return lo;
	}
}
