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

package org.scijava.ops.image.threshold;

import java.util.function.Predicate;

import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

// Ported from Antti Niemisto's HistThresh Matlab toolbox
//   Relicensed BSD 2-12-13

/**
 * Utility methods used by the various threshold methods.
 *
 * @author Barry DeZonia
 * @author Gabriel Landini
 */
public final class Thresholds {

	private Thresholds() {
		// NB: Prevent instantiation of utility class.
	}

	public static boolean bimodalTest(double[] y) {
        var len = y.length;
        var modes = 0;

		for (var k = 1; k < len - 1; k++) {
			if (y[k - 1] < y[k] && y[k + 1] < y[k]) {
				modes++;
				if (modes > 2) return false;
			}
		}
		return (modes == 2);
	}

	/**
	 * The partial sum A from C. A. Glasbey, "An analysis of histogram-based
	 * thresholding algorithms," CVGIP: Graphical Models and Image Processing,
	 * vol. 55, pp. 532-537, 1993.
	 */
	public static double A(long[] y, int j) {
		double x = 0;
		for (var i = 0; i <= j; i++)
			x += y[i];
		return x;
	}

	/**
	 * The partial sum B from C. A. Glasbey, "An analysis of histogram-based
	 * thresholding algorithms," CVGIP: Graphical Models and Image Processing,
	 * vol. 55, pp. 532-537, 1993.
	 */
	public static double B(long[] y, int j) {
		double x = 0;
		for (var i = 0; i <= j; i++)
			x += y[i] * i;
		return x;
	}

	/**
	 * The partial sum C from C. A. Glasbey, "An analysis of histogram-based
	 * thresholding algorithms," CVGIP: Graphical Models and Image Processing,
	 * vol. 55, pp. 532-537, 1993.
	 */
	public static double C(long[] y, int j) {
		double x = 0;
		for (var i = 0; i <= j; i++)
			x += y[i] * i * i;
		return x;
	}

	/**
	 * The partial sum D from C. A. Glasbey, "An analysis of histogram-based
	 * thresholding algorithms," CVGIP: Graphical Models and Image Processing,
	 * vol. 55, pp. 532-537, 1993.
	 */
	public static double D(long[] y, int j) {
		double x = 0;
		for (var i = 0; i <= j; i++)
			x += y[i] * i * i * i;
		return x;
	}

	/**
	 * Maximum number of grey levels for which {@link #histogram} uses one bin
	 * per grey level.
	 */
	public static final long MAX_LEVEL_BINS = 65536;

	/**
	 * Number of bins {@link #histogram} uses for non-integer data and for
	 * integer data with more than {@link #MAX_LEVEL_BINS} grey levels.
	 */
	public static final int DEFAULT_NUM_BINS = 256;

	/**
	 * Creates the histogram used by the histogram-based threshold methods.
	 * <p>
	 * The histogram spans the range {@code [min, max]} of the included values.
	 * For {@link IntegerType} data with at most {@link #MAX_LEVEL_BINS} grey
	 * levels in that range, it has exactly one bin per grey level, centered on
	 * that grey level, as in ImageJ's Auto_Threshold plugin. Otherwise, it has
	 * {@link #DEFAULT_NUM_BINS} equally sized bins.
	 * </p>
	 *
	 * @param values the values to count
	 * @param include which values to count, or {@code null} to count all values
	 * @return the histogram, or {@code null} if no value is included
	 */
	public static <T extends RealType<T>> Histogram1d<T> histogram(
		final Iterable<T> values, final Predicate<T> include)
	{
		return histogram(values, include, MAX_LEVEL_BINS);
	}

	/**
	 * Maximum number of grey levels for which the local histogram-based
	 * threshold methods use one histogram bin per grey level (see
	 * {@link #histogram(Iterable, Predicate, long)}).
	 */
	public static final long MAX_LOCAL_LEVEL_BINS = 256;

	/**
	 * Creates the histogram used by the histogram-based threshold methods, with
	 * a limit on the number of bins per grey level.
	 * <p>
	 * The histogram spans the range {@code [min, max]} of the included values.
	 * For {@link IntegerType} data with at most {@code maxLevelBins} grey levels
	 * in that range, it has exactly one bin per grey level, centered on that
	 * grey level. Otherwise, it has {@link #DEFAULT_NUM_BINS} equally sized
	 * bins.
	 * </p>
	 *
	 * @param values the values to count
	 * @param include which values to count, or {@code null} to count all values
	 * @param maxLevelBins the maximum number of grey levels with one bin each
	 * @return the histogram, or {@code null} if no value is included
	 */
	public static <T extends RealType<T>> Histogram1d<T> histogram(
		final Iterable<T> values, final Predicate<T> include,
		final long maxLevelBins)
	{
		var min = Double.POSITIVE_INFINITY;
		var max = Double.NEGATIVE_INFINITY;
		var integer = true;
		for (final var v : values) {
			if (include != null && !include.test(v)) continue;
			integer &= v instanceof IntegerType;
			final var d = v.getRealDouble();
			if (d < min) min = d;
			if (d > max) max = d;
		}
		if (min > max) return null;
		final Real1dBinMapper<T> mapper;
		final var levels = (long) (max - min) + 1;
		if (min == max || integer && levels <= maxLevelBins) {
			mapper = new Real1dBinMapper<>(min - 0.5, max + 0.5, levels, false);
		}
		else {
			mapper = new Real1dBinMapper<>(min, max, DEFAULT_NUM_BINS, false);
		}
		final var histogram = new Histogram1d<>(mapper);
		for (final var v : values) {
			if (include == null || include.test(v)) histogram.increment(v);
		}
		return histogram;
	}

	/**
	 * Detects a bilevel histogram, i.e. one with exactly two non-empty bins.
	 * Mirroring ImageJ's Auto_Threshold plugin, the threshold of such a
	 * histogram is the bin just below the upper non-empty bin, so that exactly
	 * the pixels of the upper level are foreground.
	 *
	 * @param histogram the histogram to check
	 * @return the threshold bin, or -1 if the histogram is not bilevel
	 */
	public static long bilevelBin(final Histogram1d<?> histogram) {
		var nonEmpty = 0;
		var upper = -1L;
		for (var i = 0L; i < histogram.getBinCount(); i++) {
			if (histogram.frequency(i) == 0) continue;
			if (++nonEmpty > 2) return -1;
			upper = i;
		}
		return nonEmpty == 2 ? upper - 1 : -1;
	}

	/**
	 * Computes the mean and the population standard deviation (normalized by
	 * the number of values {@code n}, not {@code n - 1}) of some values, as
	 * ImageJ's {@code RankFilters} do for the local thresholds of Fiji's
	 * Auto_Local_Threshold plugin.
	 *
	 * @param values the values
	 * @return the mean and the population standard deviation
	 */
	public static double[] meanAndPopulationStdDev(
		final Iterable<? extends RealType<?>> values)
	{
		var n = 0L;
		var sum = 0.0;
		for (final var v : values) {
			sum += v.getRealDouble();
			n++;
		}
		final var mean = sum / n;
		var sum2 = 0.0;
		for (final var v : values) {
			final var d = v.getRealDouble() - mean;
			sum2 += d * d;
		}
		return new double[] { mean, Math.sqrt(sum2 / n) };
	}

	/**
	 * Converts the sample variance of {@code n} values, as computed by
	 * {@code stats.integralVariance}, to their population variance.
	 *
	 * @param sampleVariance the sample variance
	 * @param n the number of values
	 * @return the population variance
	 */
	public static double populationVariance(final double sampleVariance,
		final long n)
	{
		return n > 1 ? Math.max(0, sampleVariance) * (n - 1) / n : 0;
	}
}
