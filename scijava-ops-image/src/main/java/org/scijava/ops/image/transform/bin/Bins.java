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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.iterator.LocalizingZeroMinIntervalIterator;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * Utility methods backing the {@code transform.bin} ops.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
final class Bins {

	private Bins() {
		// NB: Prevent instantiation of utility class.
	}

	/** The supported block reduction methods. */
	enum Method {
			MEAN, SUM, MIN, MAX, MEDIAN, MODE
	}

	/**
	 * Parses the given reduction method name (case-insensitive). A {@code null}
	 * name yields {@link Method#MEAN}.
	 */
	static Method method(final String name) {
		if (name == null) return Method.MEAN;
		switch (name.trim().toLowerCase()) {
			case "mean":
			case "average":
				return Method.MEAN;
			case "sum":
				return Method.SUM;
			case "min":
				return Method.MIN;
			case "max":
				return Method.MAX;
			case "median":
				return Method.MEDIAN;
			case "mode":
				return Method.MODE;
			default:
				throw new IllegalArgumentException("Unknown bin method: '" + name +
					"'. Expected one of: mean, sum, min, max, median, mode.");
		}
	}

	/**
	 * Validates the given bin factors against an image of dimensionality
	 * {@code numDimensions}, padding missing trailing factors with 1.
	 */
	static long[] factors(final long[] factors, final int numDimensions) {
		if (factors == null) throw new NullPointerException(
			"Bin factors must be given");
		if (factors.length > numDimensions) throw new IllegalArgumentException(
			"Too many bin factors (" + factors.length + ") for a " + numDimensions +
				"-dimensional image");
		final var f = new long[numDimensions];
		Arrays.fill(f, 1);
		for (var d = 0; d < factors.length; d++) {
			if (factors[d] < 1) throw new IllegalArgumentException(
				"Bin factors must be positive, but factor " + d + " is " + factors[d]);
			f[d] = factors[d];
		}
		return f;
	}

	/**
	 * Computes the dimensions of the binned image: each input dimension divided
	 * by its bin factor, rounded down (incomplete trailing blocks are dropped).
	 */
	static Dimensions outputDimensions(final Dimensions input,
		final long[] factors)
	{
		final var dims = new long[input.numDimensions()];
		for (var d = 0; d < dims.length; d++) {
			dims[d] = input.dimension(d) / factors[d];
			if (dims[d] < 1) throw new IllegalArgumentException("Bin factor " +
				factors[d] + " exceeds size " + input.dimension(d) + " of dimension " +
				d);
		}
		return new FinalDimensions(dims);
	}

	/**
	 * Bins {@code input} into {@code output}, whose dimensions must equal
	 * {@link #outputDimensions(Dimensions, long[])}.
	 */
	static <I extends RealType<I>, O extends RealType<O>> void bin(
		final RandomAccessibleInterval<I> input, final long[] factors,
		final Method method, final RandomAccessibleInterval<O> output)
	{
		final var n = input.numDimensions();
		final var expected = outputDimensions(input, factors);
		if (output.numDimensions() != n) throw new IllegalArgumentException(
			"Output must have " + n + " dimensions");
		for (var d = 0; d < n; d++) {
			if (output.dimension(d) != expected.dimension(d)) {
				throw new IllegalArgumentException("Output dimension " + d + " is " +
					output.dimension(d) + " but should be " + expected.dimension(d));
			}
		}

		long blockSize = 1;
		for (final var f : factors)
			blockSize *= f;
		if (blockSize > Integer.MAX_VALUE) throw new IllegalArgumentException(
			"Bin block too large: " + blockSize + " elements");
		final var values = new double[(int) blockSize];

		final var outType = output.getType();
		final var round = outType instanceof IntegerType;
		final var outMin = outType.getMinValue();
		final var outMax = outType.getMaxValue();

		final var inRA = input.randomAccess();
		final var outCursor = Views.flatIterable(output).localizingCursor();
		final var block = new LocalizingZeroMinIntervalIterator(factors);
		final var blockMin = new long[n];
		final Map<Double, Integer> counts = method == Method.MODE ? new HashMap<>()
			: null;

		while (outCursor.hasNext()) {
			outCursor.fwd();
			for (var d = 0; d < n; d++) {
				blockMin[d] = input.min(d) + //
					(outCursor.getLongPosition(d) - output.min(d)) * factors[d];
			}
			block.reset();
			var i = 0;
			while (block.hasNext()) {
				block.fwd();
				for (var d = 0; d < n; d++) {
					inRA.setPosition(blockMin[d] + block.getLongPosition(d), d);
				}
				values[i++] = inRA.get().getRealDouble();
			}

			var v = reduce(values, method, counts);
			// Note: Values are rounded half up, as ImageJ's Image > Transform > Bin does.
			if (round) v = Math.floor(v + 0.5);
			outCursor.get().setReal(Math.max(outMin, Math.min(outMax, v)));
		}
	}

	private static double reduce(final double[] values, final Method method,
		final Map<Double, Integer> counts)
	{
		switch (method) {
			case SUM: {
				double sum = 0;
				for (final var v : values)
					sum += v;
				return sum;
			}
			case MIN: {
				var min = Double.POSITIVE_INFINITY;
				for (final var v : values)
					if (v < min) min = v;
				return min;
			}
			case MAX: {
				var max = Double.NEGATIVE_INFINITY;
				for (final var v : values)
					if (v > max) max = v;
				return max;
			}
			case MEDIAN: {
				Arrays.sort(values);
				final var k = values.length / 2;
				return values.length % 2 == 0 ? 0.5 * (values[k - 1] + values[k])
					: values[k];
			}
			case MODE: {
				counts.clear();
				var mode = values[0];
				var best = 0;
				for (final var v : values) {
					final int c = counts.merge(v, 1, Integer::sum);
					// Note: Ties are resolved in favor of the smallest value.
					if (c > best || (c == best && v < mode)) {
						best = c;
						mode = v;
					}
				}
				return mode;
			}
			case MEAN:
			default: {
				double sum = 0;
				for (final var v : values)
					sum += v;
				return sum / values.length;
			}
		}
	}
}
