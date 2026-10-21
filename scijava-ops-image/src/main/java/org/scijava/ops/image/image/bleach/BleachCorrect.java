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
 * Photobleaching correction of a time series.
 * <p>
 * Each frame (hyperslice along {@code timeDim}) is corrected with one of the
 * following methods:
 * </p>
 * <ul>
 * <li>{@code "ratio"}: frame {@code t} is multiplied by {@code m_0 / m_t},
 * where {@code m_t} is the mean intensity of frame {@code t}, so that all
 * frames have the mean intensity of the first frame.</li>
 * <li>{@code "exponential"}: the decay model {@code f(t) = a exp(-b t) + c}
 * is fitted to the frame means by Levenberg-Marquardt least squares, and
 * frame {@code t} is multiplied by {@code f(0) / f(t)}. This is less
 * sensitive than the ratio method to intensity changes that are not caused
 * by bleaching. The fitted parameters are available from
 * {@code image.bleachFit}.</li>
 * <li>{@code "histogram"}: the intensity histogram of each frame is matched
 * to that of the first frame (see {@code image.histogramMatch}). This also
 * corrects for contrast changes, but alters intensity relationships, so it
 * is meant for segmentation rather than quantification.</li>
 * </ul>
 * <p>
 * Values are clamped to the range of the output type. These are the standard
 * methods described by Miura (2020); this implementation was written from
 * that description.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @param <V> output pixel type
 * @see <a href="https://doi.org/10.12688/f1000research.27171.1">K. Miura,
 *      "Bleach correction ImageJ plugin for compensating the photobleaching
 *      of time-lapse sequences", F1000Research 9:1494, 2020</a>
 * @implNote op names='image.bleachCorrect'
 */
public class BleachCorrect<T extends RealType<T>, V extends RealType<V>>
	implements
	Computers.Arity3<RandomAccessibleInterval<T>, Integer, String, RandomAccessibleInterval<V>>
{

	/**
	 * Corrects photobleaching.
	 *
	 * @param input the time series to correct
	 * @param timeDim the index of the time dimension
	 * @param method the correction method: ratio, exponential or histogram
	 *          (default: ratio)
	 * @param output the corrected time series
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Integer timeDim, @Nullable final String method,
		final RandomAccessibleInterval<V> output)
	{
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output must have the same dimensions");
		}
		Bleach.checkDim(input, timeDim);
		final var m = method == null ? "ratio" : method;
		final var frames = input.dimension(timeDim);
		if (m.equals("histogram")) {
			final var reference = Bleach.frame(input, timeDim, 0);
			for (var t = 0; t < frames; t++) {
				Bleach.matchHistogram(Bleach.frame(input, timeDim, t), reference,
					Bleach.frame(output, timeDim, t));
			}
			return;
		}
		final var means = Bleach.frameMeans(input, timeDim);
		final double[] model;
		if (m.equals("ratio")) model = means;
		else if (m.equals("exponential")) {
			final var p = Bleach.fitExponential(means);
			model = new double[means.length];
			for (var t = 0; t < model.length; t++)
				model[t] = Bleach.EXP_OFFSET.value(t, p);
		}
		else throw new IllegalArgumentException("Unknown method: " + m);
		for (var t = 0; t < frames; t++) {
			final var factor = model[t] == 0 ? 1 : model[0] / model[t];
			Bleach.scale(Bleach.frame(input, timeDim, t), factor, Bleach.frame(
				output, timeDim, t));
		}
	}
}
