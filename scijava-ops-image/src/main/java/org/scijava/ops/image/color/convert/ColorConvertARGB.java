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

package org.scijava.ops.image.color.convert;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ops.spi.OpDependency;

/**
 * Converts a packed {@link ARGBType} (sRGB) image to another colour space. The
 * alpha channel is ignored. See {@link ColorConvert} and {@link ColorSpace}
 * for details.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @implNote op names='color.convert'
 */
public class ColorConvertARGB implements
	BiFunction<RandomAccessibleInterval<ARGBType>, String, RandomAccessibleInterval<DoubleType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> createOp;

	/**
	 * Converts an ARGB image to another colour space.
	 *
	 * @param input the ARGB image
	 * @param to the colour space of the output: RGB, linearRGB, HSB (or HSV),
	 *          XYZ, Lab, Luv or YCbCr (RGB components are output in
	 *          {@code [0, 255]})
	 * @return the converted image, with one more dimension than the input;
	 *         the last dimension holds the three colour components
	 */
	@Override
	public RandomAccessibleInterval<DoubleType> apply(
		final RandomAccessibleInterval<ARGBType> input, final String to)
	{
		final var dst = ColorSpace.byName(to);
		final var rgb = Converters.argbChannels(input, 1, 2, 3);
		final var output = createOp.apply(rgb, new DoubleType());
		ColorConvert.convert(rgb, ColorSpace.RGB, dst, rgb.numDimensions() - 1,
			255, output);
		return output;
	}
}
