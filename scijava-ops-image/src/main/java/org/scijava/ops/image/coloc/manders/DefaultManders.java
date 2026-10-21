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

package org.scijava.ops.image.coloc.manders;

import net.imglib2.type.numeric.RealType;
import net.imglib2.util.IterablePair;

import org.scijava.function.Functions;
import org.scijava.ops.image.coloc.ColocUtil;
import org.scijava.ops.spi.Nullable;

/**
 * Computes the Manders colocalization coefficients M1 and M2 of two channels
 * (Manders, Verbeek &amp; Aten (1993), "Measurement of co-localization of
 * objects in dual-colour confocal images", J. Microsc. 169(3):375-382).
 * <p>
 * With thresholds {@code t1} and {@code t2},
 * </p>
 *
 * <pre>
 * M1 = sum(x where y &gt; t2) / sum(x)
 * M2 = sum(y where x &gt; t1) / sum(y)
 * </pre>
 * <p>
 * where {@code x} and {@code y} are the values of the first and second
 * channel and the sums run over all pixels. M1 is thus the fraction of the
 * intensity of the first channel found in pixels where the second channel is
 * above its threshold. With the default thresholds of zero these are the
 * original coefficients of Manders et al.; with the thresholds of
 * {@code coloc.costesThreshold} they are the thresholded coefficients tM1 and
 * tM2 of Costes et al. (2004), Biophys J 86(6):3993-4003. A coefficient is
 * {@code NaN} when its channel has no intensity.
 * </p>
 * <p>
 * Note that Coloc 2 (Fiji) counts pixels equal to a nonzero threshold as
 * above it; for integer images, its tM1 and tM2 at thresholds {@code (t1, t2)}
 * equal these coefficients at {@code (t1 - 1, t2 - 1)}.
 * </p>
 * <p>
 * To restrict the computation to a region of interest, pass views of both
 * images sampled within the same region.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> type of the first image
 * @param <U> type of the second image
 * @implNote op names='coloc.manders'
 */
public class DefaultManders<T extends RealType<T>, U extends RealType<U>>
	implements
	Functions.Arity4<Iterable<T>, Iterable<U>, Double, Double, MandersResult>
{

	/**
	 * Computes the Manders coefficients of two images.
	 *
	 * @param image1 the first image
	 * @param image2 the second image, with the same size and iteration order as
	 *          the first
	 * @param threshold1 the threshold of the first channel (default 0)
	 * @param threshold2 the threshold of the second channel (default 0)
	 * @return the coefficients M1 and M2
	 */
	@Override
	public MandersResult apply(final Iterable<T> image1,
		final Iterable<U> image2, @Nullable Double threshold1,
		@Nullable Double threshold2)
	{
		if (!ColocUtil.sameIterationOrder(image1, image2))
			throw new IllegalArgumentException(
				"Input images must have the same dimensionality and iteration order!");
		final double t1 = threshold1 == null ? 0 : threshold1;
		final double t2 = threshold2 == null ? 0 : threshold2;

		double sum1 = 0, sum2 = 0, coloc1 = 0, coloc2 = 0;
		for (final var pair : new IterablePair<>(image1, image2)) {
			final var x = pair.getA().getRealDouble();
			final var y = pair.getB().getRealDouble();
			sum1 += x;
			sum2 += y;
			if (y > t2) coloc1 += x;
			if (x > t1) coloc2 += y;
		}
		return new MandersResult(sum1 == 0 ? Double.NaN : coloc1 / sum1,
			sum2 == 0 ? Double.NaN : coloc2 / sum2);
	}
}
