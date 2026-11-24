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

package org.scijava.ops.image.image.bleach;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Histogram matching (histogram specification): maps the intensities of an
 * image so that their distribution matches that of a reference image.
 * <p>
 * Let {@code F(v)} be the fraction of input pixels with value at most
 * {@code v}, and {@code G(r)} the same for the reference. Each input value
 * {@code v} is mapped to the smallest reference value {@code r} with
 * {@code G(r) >= F(v)}. Output values are therefore always values occurring
 * in the reference; matching an image to itself is the identity. The images
 * need not have the same size. Exact values are used, without binning.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @param <U> reference pixel type
 * @param <V> output pixel type
 * @see <a href="https://en.wikipedia.org/wiki/Histogram_matching">Histogram
 *      matching</a>
 * @implNote op names='image.histogramMatch'
 */
public class HistogramMatch<T extends RealType<T>, U extends RealType<U>, V extends RealType<V>>
	implements
	Computers.Arity2<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, RandomAccessibleInterval<V>>
{

	/**
	 * Matches the histogram of an image to that of a reference.
	 *
	 * @param input the image whose intensities are mapped
	 * @param reference the image with the desired intensity distribution
	 * @param output the mapped image
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final RandomAccessibleInterval<U> reference,
		final RandomAccessibleInterval<V> output)
	{
		Bleach.matchHistogram(input, reference, output);
	}
}

/**
 * Matches the histogram of every frame of a time series to that of a
 * reference frame (see the two-image version of
 * {@code image.histogramMatch}).
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @param <V> output pixel type
 * @implNote op names='image.histogramMatch'
 */
class HistogramMatchFrames<T extends RealType<T>, V extends RealType<V>>
	implements
	Computers.Arity3<RandomAccessibleInterval<T>, Integer, Integer, RandomAccessibleInterval<V>>
{

	/**
	 * Matches the histogram of each frame to that of a reference frame.
	 *
	 * @param input the time series
	 * @param timeDim the index of the time dimension
	 * @param referenceFrame the index of the reference frame, relative to the
	 *          start of the time dimension (default: 0)
	 * @param output the matched time series
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Integer timeDim, @Nullable final Integer referenceFrame,
		final RandomAccessibleInterval<V> output)
	{
		Bleach.checkDim(input, timeDim);
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output must have the same dimensions");
		}
		final var frames = input.dimension(timeDim);
		final long ref = referenceFrame == null ? 0 : referenceFrame;
		if (ref < 0 || ref >= frames) {
			throw new IllegalArgumentException("Invalid reference frame: " + ref);
		}
		final var reference = Bleach.frame(input, timeDim, ref);
		for (var t = 0; t < frames; t++) {
			Bleach.matchHistogram(Bleach.frame(input, timeDim, t), reference, Bleach
				.frame(output, timeDim, t));
		}
	}
}
