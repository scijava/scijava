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

package org.scijava.ops.image.filter.flatfield;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Flat-field correction of an image using a reference flat-field image and an
 * optional dark-field image:
 *
 * <pre>
 * out = (in - dark) / (flat - dark) &times; mean(flat - dark)
 * </pre>
 * <p>
 * Without a dark-field image, this reduces to
 * {@code out = in / flat × mean(flat)}. Pixels where
 * {@code flat - dark == 0} are set to {@code in - dark}. The input, flat and
 * dark images must have the same dimensions; they may have any number of
 * dimensions. The computation is done in double precision, and results are
 * clamped to the range of the output type (so with an integer output,
 * {@code in < dark} gives 0 and overly bright pixels saturate).
 * </p>
 * <p>
 * Based on the "Flat-field correction" macro of the BioVoxxel Toolbox by Jan
 * Brocher / BioVoxxel (BSD 3-Clause license, Copyright (C) Jan Brocher /
 * BioVoxxel). The optional dark-field subtraction is an addition to the
 * original, which only divides by the flat-field. The RGB (HSB brightness)
 * branch of the original is not ported; apply this op to a brightness channel
 * instead.
 * </p>
 * <p>
 * The original BioVoxxel Toolbox license notice follows.
 * </p>
 *
 * <pre>
 * Copyright (c) 2015, BioVoxxel / Jan Brocher
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of BioVoxxel nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Jan Brocher (BioVoxxel)
 * @param <T> input pixel type
 * @param <U> flat-field pixel type
 * @param <D> dark-field pixel type
 * @param <V> output pixel type
 * @see <a href="https://github.com/biovoxxel/BioVoxxel-Toolbox">BioVoxxel
 *      Toolbox</a>
 * @implNote op names='filter.flatFieldCorrect'
 */
public class FlatFieldCorrect<T extends RealType<T>, U extends RealType<U>, D extends RealType<D>, V extends RealType<V>>
	implements
	Computers.Arity3<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, RandomAccessibleInterval<D>, RandomAccessibleInterval<V>>
{

	/**
	 * Applies flat-field correction.
	 *
	 * @param input the image to correct
	 * @param flat the flat-field image, i.e. an image of a uniform specimen
	 *          acquired under the same illumination
	 * @param dark the dark-field image, i.e. an image acquired without
	 *          illumination (default: no dark-field subtraction)
	 * @param output the corrected image
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final RandomAccessibleInterval<U> flat,
		@Nullable final RandomAccessibleInterval<D> dark,
		final RandomAccessibleInterval<V> output)
	{
		if (!Intervals.equalDimensions(input, flat)) {
			throw new IllegalArgumentException(
				"Input and flat-field images must have the same dimensions");
		}
		if (dark != null && !Intervals.equalDimensions(input, dark)) {
			throw new IllegalArgumentException(
				"Input and dark-field images must have the same dimensions");
		}

		// Compute mean(flat - dark).
		double sum = 0;
		long count = 0;
		final var flatCursor = Views.flatIterable(flat).cursor();
		final var darkCursor = dark == null ? null : Views.flatIterable(dark)
			.cursor();
		while (flatCursor.hasNext()) {
			sum += flatCursor.next().getRealDouble() - (darkCursor == null ? 0
				: darkCursor.next().getRealDouble());
			count++;
		}
		final var mean = sum / count;

		if (dark == null) {
			LoopBuilder.setImages(input, flat, output).forEachPixel((i, f, o) -> o
				.setReal(clamp(o, correct(i.getRealDouble(), f.getRealDouble(),
					mean))));
		}
		else {
			LoopBuilder.setImages(input, flat, dark, output).forEachPixel((i, f, d,
				o) -> {
				final var dv = d.getRealDouble();
				o.setReal(clamp(o, correct(i.getRealDouble() - dv, f.getRealDouble() -
					dv, mean)));
			});
		}
	}

	private static double clamp(final RealType<?> o, final double v) {
		return Math.max(o.getMinValue(), Math.min(o.getMaxValue(), v));
	}

	private static double correct(final double signal, final double flat,
		final double mean)
	{
		return flat == 0 ? signal : signal / flat * mean;
	}
}
