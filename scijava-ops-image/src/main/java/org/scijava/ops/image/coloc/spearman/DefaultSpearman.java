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

package org.scijava.ops.image.coloc.spearman;

import java.util.Arrays;
import java.util.function.BiFunction;

import net.imglib2.type.numeric.RealType;
import net.imglib2.util.IterablePair;

import org.scijava.ops.image.coloc.ColocUtil;

/**
 * Computes the Spearman rank correlation coefficient of two images: the
 * Pearson correlation of the ranks of their values, where tied values get the
 * mean of the ranks they span (Spearman (1904), "The proof and measurement of
 * association between two things", Am J Psychol 15(1):72-101). It is
 * {@code NaN} if either image is constant.
 * <p>
 * The t-statistic of the coefficient {@code rho} for {@code n} pixels is
 * {@code rho * sqrt((n - 2) / (1 - rho^2))} with {@code n - 2} degrees of
 * freedom.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> type of the first image
 * @param <U> type of the second image
 * @implNote op names='coloc.spearman'
 */
public class DefaultSpearman<T extends RealType<T>, U extends RealType<U>>
	implements BiFunction<Iterable<T>, Iterable<U>, Double>
{

	/**
	 * Computes the Spearman rank correlation of two images.
	 *
	 * @param image1 the first image
	 * @param image2 the second image, with the same size and iteration order as
	 *          the first
	 * @return the Spearman rank correlation coefficient
	 */
	@Override
	public Double apply(final Iterable<T> image1, final Iterable<U> image2) {
		if (!ColocUtil.sameIterationOrder(image1, image2))
			throw new IllegalArgumentException(
				"Input images must have the same dimensionality and iteration order!");
		var x = new double[1024];
		var y = new double[1024];
		var n = 0;
		for (final var pair : new IterablePair<>(image1, image2)) {
			if (n == x.length) {
				x = Arrays.copyOf(x, 2 * n);
				y = Arrays.copyOf(y, 2 * n);
			}
			x[n] = pair.getA().getRealDouble();
			y[n] = pair.getB().getRealDouble();
			n++;
		}
		final var rx = ranks(x, n);
		final var ry = ranks(y, n);

		// Note: the mean rank is (n + 1) / 2 in both channels.
		final var mean = (n + 1) / 2.0;
		double sxx = 0, syy = 0, sxy = 0;
		for (var i = 0; i < n; i++) {
			final var dx = rx[i] - mean;
			final var dy = ry[i] - mean;
			sxx += dx * dx;
			syy += dy * dy;
			sxy += dx * dy;
		}
		return sxy / Math.sqrt(sxx * syy);
	}

	/** Computes the 1-based ranks of the values, averaging ties. */
	private static double[] ranks(final double[] values, final int n) {
		final var order = new Integer[n];
		for (var i = 0; i < n; i++)
			order[i] = i;
		Arrays.sort(order, (a, b) -> Double.compare(values[a], values[b]));
		final var ranks = new double[n];
		var start = 0;
		while (start < n) {
			var end = start + 1;
			while (end < n && values[order[end]] == values[order[start]])
				end++;
			// Positions start..end-1 hold ties, with ranks start+1..end.
			final var rank = (start + 1 + end) / 2.0;
			for (var i = start; i < end; i++)
				ranks[order[i]] = rank;
			start = end;
		}
		return ranks;
	}
}
