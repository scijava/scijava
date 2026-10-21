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

package org.scijava.ops.image.threshold.huang;

import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.image.threshold.AbstractComputeThresholdHistogram;

/**
 * Implements Huang's fuzzy thresholding method, which picks the threshold
 * minimizing the fuzziness of the image, measured with Shannon's entropy
 * function.
 * <p>
 * For a threshold {@code t}, each grey level {@code g} belongs to its class
 * (background if {@code g <= t}, foreground otherwise) with the membership
 * {@code 1 / (1 + |g - m| / C)}, where {@code m} is the mean grey level of the
 * class and {@code C} the difference between the highest and lowest grey level
 * of the image. The fuzziness is the histogram-weighted sum of the Shannon
 * entropy of the memberships. Ties are resolved in favor of the lowest
 * threshold.
 * </p>
 * <p>
 * This corresponds to the "Huang" method of ImageJ Auto_Threshold. Its cost is
 * quadratic in the number of grey levels; {@code threshold.huang2} is a faster
 * variant.
 * </p>
 * <p>
 * L.-K. Huang and M.-J. J. Wang, "Image thresholding by minimizing the
 * measures of fuzziness," Pattern Recognition, vol. 28, no. 1, pp. 41-51,
 * 1995.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @implNote op names='threshold.huang'
 */
public class ComputeHuangThreshold<T extends RealType<T>> extends
	AbstractComputeThresholdHistogram<T>
{

	/**
	 * Computes the Huang threshold bin of a histogram.
	 *
	 * @param hist the {@link Histogram1d}
	 * @return the Huang threshold bin
	 */
	@Override
	public long computeBin(final Histogram1d<T> hist) {
		return computeBin(hist.toLongArray());
	}

	/**
	 * Computes the Huang threshold bin of a histogram.
	 *
	 * @param histogram the bin counts
	 * @return the threshold bin; pixels in higher bins are foreground
	 */
	public static long computeBin(final long[] histogram) {
		var lo = 0;
		while (lo < histogram.length && histogram[lo] == 0)
			lo++;
		var hi = histogram.length - 1;
		while (hi > lo && histogram[hi] == 0)
			hi--;
		if (lo >= hi) return lo < histogram.length ? lo : 0;

		final double c = hi - lo;
		double count0 = 0, sum0 = 0, count1 = 0, sum1 = 0;
		for (var g = lo; g <= hi; g++) {
			count1 += histogram[g];
			sum1 += (double) g * histogram[g];
		}
		var best = Double.POSITIVE_INFINITY;
		var bestBin = lo;
		for (var t = lo; t < hi; t++) {
			count0 += histogram[t];
			sum0 += (double) t * histogram[t];
			count1 -= histogram[t];
			sum1 -= (double) t * histogram[t];
			// Note: Empty bins split the levels like the bin below them.
			if (histogram[t] == 0) continue;
			final var mean0 = sum0 / count0;
			final var mean1 = sum1 / count1;
			double fuzziness = 0;
			for (var g = lo; g <= hi; g++) {
				if (histogram[g] == 0) continue;
				final var m = g <= t ? mean0 : mean1;
				fuzziness += histogram[g] * entropy(1 / (1 + Math.abs(g - m) / c));
			}
			if (fuzziness < best) {
				best = fuzziness;
				bestBin = t;
			}
		}
		return bestBin;
	}

	/** Shannon's entropy function of a membership value. */
	private static double entropy(final double mu) {
		if (mu <= 0 || mu >= 1) return 0;
		return -mu * Math.log(mu) - (1 - mu) * Math.log(1 - mu);
	}

}
