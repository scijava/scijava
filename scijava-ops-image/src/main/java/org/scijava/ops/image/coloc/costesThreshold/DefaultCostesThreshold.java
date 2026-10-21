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

package org.scijava.ops.image.coloc.costesThreshold;

import java.util.Arrays;
import java.util.function.BiFunction;

import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.IterablePair;

import org.scijava.ops.image.coloc.ColocUtil;

/**
 * Computes the automatic colocalization thresholds of Costes et al. (2004),
 * "Automatic and quantitative measurement of protein-protein colocalization in
 * live cells", Biophys J 86(6):3993-4003.
 * <p>
 * The second channel is regressed on the first by orthogonal (total least
 * squares) regression, giving the line {@code y = slope * x + intercept}. The
 * thresholds {@code t1} and {@code t2 = slope * t1 + intercept} then move
 * down this line until the Pearson correlation of the pixels below the
 * thresholds is no longer positive: the result is the highest {@code t1} for
 * which the pixels with {@code x <= t1} or {@code y <= t2} are uncorrelated or
 * anticorrelated. With these thresholds, {@code coloc.manders} gives the
 * thresholded coefficients tM1 and tM2.
 * </p>
 * <p>
 * The threshold is found by bisection between the minimum and maximum of the
 * first channel, which assumes that the Pearson correlation below the
 * thresholds increases with the threshold; for integer types the threshold
 * of the first channel is an integer. If even the correlation of all pixels
 * is not positive, the threshold is the maximum of the first channel.
 * </p>
 * <p>
 * Note that Coloc 2 (Fiji) reports the lowest values above the thresholds
 * instead, so for integer images its thresholds are the smallest integers
 * greater than these.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> type of the first image
 * @param <U> type of the second image
 * @implNote op names='coloc.costesThreshold'
 */
public class DefaultCostesThreshold<T extends RealType<T>, U extends RealType<U>>
	implements BiFunction<Iterable<T>, Iterable<U>, CostesResult>
{

	/**
	 * Computes the Costes thresholds of two images.
	 *
	 * @param image1 the first image
	 * @param image2 the second image, with the same size and iteration order as
	 *          the first
	 * @return the thresholds, the regression line and the Pearson correlation
	 *         below the thresholds
	 */
	@Override
	public CostesResult apply(final Iterable<T> image1,
		final Iterable<U> image2)
	{
		if (!ColocUtil.sameIterationOrder(image1, image2))
			throw new IllegalArgumentException(
				"Input images must have the same dimensionality and iteration order!");

		// Copy the samples, since they are visited many times.
		var x = new double[1024];
		var y = new double[1024];
		var n = 0;
		boolean integer1 = false;
		for (final var pair : new IterablePair<>(image1, image2)) {
			if (n == x.length) {
				x = Arrays.copyOf(x, 2 * n);
				y = Arrays.copyOf(y, 2 * n);
			}
			if (n == 0) integer1 = pair.getA() instanceof IntegerType;
			x[n] = pair.getA().getRealDouble();
			y[n] = pair.getB().getRealDouble();
			n++;
		}
		if (n < 2) throw new IllegalArgumentException(
			"At least two samples are needed.");

		// Orthogonal regression of y on x.
		double meanX = 0, meanY = 0;
		for (var i = 0; i < n; i++) {
			meanX += x[i];
			meanY += y[i];
		}
		meanX /= n;
		meanY /= n;
		double sxx = 0, syy = 0, sxy = 0;
		double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
		for (var i = 0; i < n; i++) {
			final var dx = x[i] - meanX;
			final var dy = y[i] - meanY;
			sxx += dx * dx;
			syy += dy * dy;
			sxy += dx * dy;
			min = Math.min(min, x[i]);
			max = Math.max(max, x[i]);
		}
		if (sxy == 0) throw new IllegalArgumentException(
			"The channels are uncorrelated: no regression line exists.");
		final var diff = syy - sxx;
		final var slope = (diff + Math.sqrt(diff * diff + 4 * sxy * sxy)) /
			(2 * sxy);
		final var intercept = meanY - slope * meanX;

		// Bisect for the highest threshold with a non-positive correlation below.
		final var rAll = pearsonsBelow(x, y, n, max, slope * max + intercept);
		if (!(rAll > 0)) {
			return new CostesResult(max, slope * max + intercept, slope, intercept,
				rAll);
		}
		var lo = min;
		var hi = max;
		final var tolerance = integer1 ? 1 : (max - min) * 1e-6;
		while (hi - lo > tolerance) {
			final var mid = integer1 ? Math.floor((lo + hi) / 2) : (lo + hi) / 2;
			if (pearsonsBelow(x, y, n, mid, slope * mid + intercept) > 0) hi = mid;
			else lo = mid;
		}
		final var t2 = slope * lo + intercept;
		return new CostesResult(lo, t2, slope, intercept, pearsonsBelow(x, y, n,
			lo, t2));
	}

	/**
	 * Computes the Pearson correlation of the samples which are not above both
	 * thresholds, or {@code NaN} if it is undefined.
	 */
	private static double pearsonsBelow(final double[] x, final double[] y,
		final int n, final double t1, final double t2)
	{
		long count = 0;
		double sx = 0, sy = 0;
		for (var i = 0; i < n; i++) {
			if (x[i] > t1 && y[i] > t2) continue;
			sx += x[i];
			sy += y[i];
			count++;
		}
		if (count < 2) return Double.NaN;
		final var mx = sx / count;
		final var my = sy / count;
		double sxx = 0, syy = 0, sxy = 0;
		for (var i = 0; i < n; i++) {
			if (x[i] > t1 && y[i] > t2) continue;
			final var dx = x[i] - mx;
			final var dy = y[i] - my;
			sxx += dx * dx;
			syy += dy * dy;
			sxy += dx * dy;
		}
		return sxy / Math.sqrt(sxx * syy);
	}
}
