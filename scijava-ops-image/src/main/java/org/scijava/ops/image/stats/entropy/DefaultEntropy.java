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

package org.scijava.ops.image.stats.entropy;

import java.util.function.BiFunction;

import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Computes the Shannon entropy, in bits, of the intensity distribution of an
 * image: {@code H = -sum_i p_i log2(p_i)}, where {@code p_i} is the fraction
 * of samples in bin {@code i} of the histogram computed by
 * {@code image.histogram} (bins spaced uniformly between the minimum and the
 * maximum value). See Shannon (1948), "A Mathematical Theory of
 * Communication", Bell System Technical Journal 27:379-423.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> type of the image
 * @implNote op names='stats.entropy'
 */
public class DefaultEntropy<T extends RealType<T>> implements
	BiFunction<Iterable<T>, Integer, Double>
{

	@OpDependency(name = "image.histogram")
	private BiFunction<Iterable<T>, Integer, Histogram1d<T>> histogramOp;

	/**
	 * Computes the Shannon entropy of an image.
	 *
	 * @param input the image
	 * @param bins the number of histogram bins (default 256)
	 * @return the entropy in bits, between 0 and {@code log2(bins)}
	 */
	@Override
	public Double apply(final Iterable<T> input, @Nullable Integer bins) {
		if (bins == null) bins = 256;
		final var histogram = histogramOp.apply(input, bins);
		return entropy(histogram.toLongArray());
	}

	/**
	 * Computes the Shannon entropy, in bits, of the given counts.
	 *
	 * @param counts the counts of each bin
	 * @return the entropy of the normalized counts
	 */
	public static double entropy(final long[] counts) {
		long total = 0;
		for (final var c : counts)
			total += c;
		if (total == 0) return 0;
		double h = 0;
		for (final var c : counts) {
			if (c == 0) continue;
			final double p = (double) c / total;
			h -= p * Math.log(p);
		}
		return h / Math.log(2);
	}
}
