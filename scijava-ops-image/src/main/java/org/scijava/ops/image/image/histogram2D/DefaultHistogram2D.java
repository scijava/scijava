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

package org.scijava.ops.image.image.histogram2D;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.util.IterablePair;
import net.imglib2.util.Pair;

import org.scijava.function.Functions;
import org.scijava.ops.image.coloc.ColocUtil;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Computes the joint (two-dimensional) intensity histogram of two images, also
 * known as the 2D scatter plot or cytofluorogram of colocalization analysis.
 * <p>
 * Each channel is binned uniformly between its own minimum and maximum: a
 * value {@code v} goes into bin {@code floor((v - min) / (max - min) * bins)},
 * with the maximum going into the last bin. The result is an image of size
 * {@code bins1 x bins2} whose value at {@code (i, j)} is the number of sample
 * pairs whose first value falls into bin {@code i} of the first channel and
 * whose second value falls into bin {@code j} of the second channel.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> type of the first image
 * @param <U> type of the second image
 * @implNote op names='image.histogram2D'
 */
public class DefaultHistogram2D<T extends RealType<T>, U extends RealType<U>>
	implements
	Functions.Arity4<Iterable<T>, Iterable<U>, Integer, Integer, Img<LongType>>
{

	public static final int DEFAULT_NUM_BINS = 256;

	@OpDependency(name = "stats.minMax")
	private Function<Iterable<T>, Pair<T, T>> minMax1;

	@OpDependency(name = "stats.minMax")
	private Function<Iterable<U>, Pair<U, U>> minMax2;

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, LongType, Img<LongType>> createImg;

	/**
	 * Computes the joint histogram of two images.
	 *
	 * @param image1 the first image, binned along the first histogram axis
	 * @param image2 the second image, binned along the second histogram axis;
	 *          it must have the same size and iteration order as the first
	 * @param bins1 the number of bins of the first channel (default 256)
	 * @param bins2 the number of bins of the second channel (default: same as
	 *          bins1)
	 * @return the joint histogram, a {@code bins1 x bins2} image of counts
	 */
	@Override
	public Img<LongType> apply(final Iterable<T> image1,
		final Iterable<U> image2, @Nullable Integer bins1,
		@Nullable Integer bins2)
	{
		if (!ColocUtil.sameIterationOrder(image1, image2))
			throw new IllegalArgumentException(
				"Input images must have the same dimensionality and iteration order!");
		if (bins1 == null) bins1 = DEFAULT_NUM_BINS;
		if (bins2 == null) bins2 = bins1;
		if (bins1 < 1 || bins2 < 1) throw new IllegalArgumentException(
			"The number of bins must be positive.");

		final var mm1 = minMax1.apply(image1);
		final var mm2 = minMax2.apply(image2);
		final var min1 = mm1.getA().getRealDouble();
		final var max1 = mm1.getB().getRealDouble();
		final var min2 = mm2.getA().getRealDouble();
		final var max2 = mm2.getB().getRealDouble();

		final var histogram = createImg.apply(new FinalDimensions(bins1, bins2),
			new LongType());
		final var ra = histogram.randomAccess();
		for (final var pair : new IterablePair<>(image1, image2)) {
			ra.setPosition(bin(pair.getA().getRealDouble(), min1, max1, bins1), 0);
			ra.setPosition(bin(pair.getB().getRealDouble(), min2, max2, bins2), 1);
			ra.get().inc();
		}
		return histogram;
	}

	private static int bin(final double v, final double min, final double max,
		final int bins)
	{
		if (!(max > min)) return 0;
		final var b = (int) Math.floor((v - min) / (max - min) * bins);
		return Math.max(0, Math.min(bins - 1, b));
	}
}
