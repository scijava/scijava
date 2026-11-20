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

package org.scijava.ops.image.coloc.mutualInformation;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;

import org.scijava.function.Functions;
import org.scijava.ops.image.stats.entropy.DefaultEntropy;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Computes the mutual information, in bits, of the intensities of two images:
 * {@code MI = H(X) + H(Y) - H(X, Y)}, where {@code H(X)} and {@code H(Y)} are
 * the Shannon entropies of the two channels and {@code H(X, Y)} is their
 * joint entropy, all estimated from the joint histogram computed by
 * {@code image.histogram2D}. Mutual information is zero for independent
 * channels and equals {@code H(X)} when one channel is a one-to-one function
 * of the other. See Cover &amp; Thomas (2006), "Elements of Information
 * Theory", 2nd edition, chapter 2.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> type of the first image
 * @param <U> type of the second image
 * @implNote op names='coloc.mutualInformation'
 */
public class DefaultMutualInformation<T extends RealType<T>, U extends RealType<U>>
	implements Functions.Arity3<Iterable<T>, Iterable<U>, Integer, Double>
{

	@OpDependency(name = "image.histogram2D")
	private Functions.Arity4<Iterable<T>, Iterable<U>, Integer, Integer, Img<LongType>> histogramOp;

	/**
	 * Computes the mutual information of two images.
	 *
	 * @param image1 the first image
	 * @param image2 the second image, with the same size and iteration order as
	 *          the first
	 * @param bins the number of histogram bins of each channel (default 256)
	 * @return the mutual information in bits
	 */
	@Override
	public Double apply(final Iterable<T> image1, final Iterable<U> image2,
		@Nullable Integer bins)
	{
		if (bins == null) bins = 256;
		final var histogram = histogramOp.apply(image1, image2, bins, bins);
		final var b1 = (int) histogram.dimension(0);
		final var b2 = (int) histogram.dimension(1);
		final var joint = new long[b1 * b2];
		final var marginal1 = new long[b1];
		final var marginal2 = new long[b2];
		final var cursor = histogram.localizingCursor();
		while (cursor.hasNext()) {
			final var count = cursor.next().get();
			final var i = cursor.getIntPosition(0);
			final var j = cursor.getIntPosition(1);
			joint[i + b1 * j] = count;
			marginal1[i] += count;
			marginal2[j] += count;
		}
		final var mi = DefaultEntropy.entropy(marginal1) + DefaultEntropy.entropy(
			marginal2) - DefaultEntropy.entropy(joint);
		// Note: rounding can make the MI of independent images slightly negative.
		return Math.max(0, mi);
	}

}
