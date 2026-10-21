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
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.image.color.deconvolve.ColourDeconvolution;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Converts an image between colour spaces: RGB (gamma-encoded sRGB), linear
 * RGB, HSB/HSV, CIE XYZ, CIELAB, CIELUV and YCbCr.
 * <p>
 * The three colour components lie along one dimension of the image. The
 * conversions follow the sRGB standard (IEC 61966-2-1, D65 white) and CIE
 * 15:2004; see {@link ColorSpace} for the formulas and value ranges of each
 * space. RGB components are divided by {@code rgbMax} on input and multiplied
 * by it on output, so that, e.g., 8-bit images are read in {@code [0, 255]}.
 * Results are not clipped to the sRGB gamut.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @see <a href="https://webstore.iec.ch/publication/6169">IEC 61966-2-1:1999,
 *      Multimedia systems and equipment - Colour measurement and management -
 *      Part 2-1: Default RGB colour space - sRGB</a>
 * @implNote op names='color.convert'
 */
public class ColorConvert<T extends RealType<T>> implements
	Functions.Arity5<RandomAccessibleInterval<T>, String, String, Integer, Double, RandomAccessibleInterval<DoubleType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> createOp;

	/**
	 * Converts an image between colour spaces.
	 *
	 * @param input the image, with the three colour components along one
	 *          dimension
	 * @param from the colour space of the input: RGB, linearRGB, HSB (or HSV),
	 *          XYZ, Lab, Luv or YCbCr
	 * @param to the colour space of the output, as for {@code from}
	 * @param channelDim the dimension holding the colour components (default:
	 *          the last dimension of size 3)
	 * @param rgbMax the value of full intensity for RGB components, in both
	 *          input and output (default: the maximum value of the pixel type
	 *          when converting from RGB or linear RGB with an integer type,
	 *          otherwise 1)
	 * @return the converted image, with the same dimensions as the input
	 */
	@Override
	public RandomAccessibleInterval<DoubleType> apply(
		final RandomAccessibleInterval<T> input, final String from,
		final String to, @Nullable final Integer channelDim,
		@Nullable final Double rgbMax)
	{
		final var src = ColorSpace.byName(from);
		final var dst = ColorSpace.byName(to);
		final int cd = channelDim == null ? ColourDeconvolution.defaultChannelDim(
			input) : channelDim;
		final double scale = rgbMax == null ? defaultRGBMax(input, src) : rgbMax;
		final var output = createOp.apply(input, new DoubleType());
		convert(input, src, dst, cd, scale, output);
		return output;
	}

	static <T extends RealType<T>> double defaultRGBMax(
		final RandomAccessibleInterval<T> input, final ColorSpace from)
	{
		final var type = Util.getTypeFromInterval(input);
		return from.isRGB() && type instanceof IntegerType ? type.getMaxValue()
			: 1;
	}

	/**
	 * Converts the colours of an image.
	 *
	 * @param input the input image
	 * @param from the colour space of the input
	 * @param to the colour space of the output
	 * @param channelDim the dimension holding the colour components
	 * @param rgbMax the value of full intensity for RGB components
	 * @param output the output image, with the same dimensions as the input
	 */
	public static <T extends RealType<T>> void convert(
		final RandomAccessibleInterval<T> input, final ColorSpace from,
		final ColorSpace to, final int channelDim, final double rgbMax,
		final RandomAccessibleInterval<DoubleType> output)
	{
		if (channelDim < 0 || channelDim >= input.numDimensions() || input
			.dimension(channelDim) != 3)
		{
			throw new IllegalArgumentException("Channel dimension " + channelDim +
				" must exist and have size 3");
		}
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output must have the same dimensions");
		}
		if (!(rgbMax > 0)) {
			throw new IllegalArgumentException("rgbMax must be positive: " +
				rgbMax);
		}
		final var inScale = from.isRGB() ? 1 / rgbMax : 1;
		final var outScale = to.isRGB() ? rgbMax : 1;
		final long cMin = input.min(channelDim);
		final long oMin = output.min(channelDim);
		LoopBuilder.setImages( //
			Views.hyperSlice(input, channelDim, cMin), //
			Views.hyperSlice(input, channelDim, cMin + 1), //
			Views.hyperSlice(input, channelDim, cMin + 2), //
			Views.hyperSlice(output, channelDim, oMin), //
			Views.hyperSlice(output, channelDim, oMin + 1), //
			Views.hyperSlice(output, channelDim, oMin + 2) //
		).multiThreaded().forEachPixel((i0, i1, i2, o0, o1, o2) -> {
			final var c = new double[] { i0.getRealDouble() * inScale, //
				i1.getRealDouble() * inScale, i2.getRealDouble() * inScale };
			ColorSpace.convert(from, to, c, c);
			o0.set(c[0] * outScale);
			o1.set(c[1] * outScale);
			o2.set(c[2] * outScale);
		});
	}
}
