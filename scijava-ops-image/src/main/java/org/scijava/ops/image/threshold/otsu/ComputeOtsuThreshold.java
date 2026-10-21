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

package org.scijava.ops.image.threshold.otsu;

import org.scijava.ops.image.threshold.AbstractComputeThresholdHistogram;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;

/**
 * Implements Otsu's threshold method.
 *
 * @author Barry DeZonia
 * @author Gabriel Landini
 * @implNote op names='threshold.otsu', priority='100.'
 */
public class ComputeOtsuThreshold<T extends RealType<T>> extends
	AbstractComputeThresholdHistogram<T>
{

	/**
	 * TODO
	 *
	 * @param hist the {@link Histogram1d}
	 * @return the Otsu threshold value
	 */
	@Override
	public long computeBin(final Histogram1d<T> hist) {
		final var histogram = hist.toLongArray();
		return computeBin(histogram);
	}

	/**
	 * Computes the bin maximizing the between-class variance
	 * {@code (mu_T * omega(k) - mu(k))^2 / (omega(k) * (1 - omega(k)))}, where
	 * {@code omega(k)} and {@code mu(k)} are the zeroth and first cumulative
	 * moments of the normalized histogram up to bin {@code k}, and {@code mu_T}
	 * is the mean (Otsu 1979, eq. 18). All bins are candidates, and ties are
	 * resolved in favor of the lowest bin, as in ImageJ Auto_Threshold since
	 * 2017.
	 * <p>
	 * N. Otsu, "A threshold selection method from gray-level histograms," IEEE
	 * Trans. Systems, Man, and Cybernetics, vol. 9, no. 1, pp. 62-66, 1979.
	 * </p>
	 */
	public static long computeBin(final long[] histogram) {
		long total = 0;
		double sum = 0;
		for (var i = 0; i < histogram.length; i++) {
			total += histogram[i];
			sum += (double) i * histogram[i];
		}
		final var meanTotal = sum / total;
		// Note: Using counts rather than probabilities scales all variances by
		// the same factor, and lets empty classes be detected exactly.
		long count = 0;
		double moment = 0, best = Double.NEGATIVE_INFINITY;
		var bestBin = 0;
		for (var k = 0; k < histogram.length; k++) {
			count += histogram[k];
			moment += (double) k * histogram[k];
			if (count == 0 || count == total) continue;
			final var d = meanTotal * count - moment;
			final var bcv = d * d / ((double) count * (total - count));
			if (bcv > best) {
				best = bcv;
				bestBin = k;
			}
		}
		return bestBin;
	}

}
