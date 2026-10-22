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

package org.scijava.ops.image.image.sdt;

import java.util.Arrays;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Signed Euclidean distance transform of a binary mask: positive inside the
 * mask, negative outside.
 * <p>
 * Each foreground pixel gets its distance to the nearest background pixel,
 * and each background pixel minus its distance to the nearest foreground
 * pixel, both computed with {@code image.distanceTransform}. Both are then
 * reduced by half a pixel (in the calibrated units of the nearest neighbour
 * step), so that values are {@code ±0.5} next to the boundary and the zero
 * level set lies between foreground and background pixels. If the mask has no
 * background (or no foreground), the missing distances are replaced by the
 * length of the image diagonal.
 * </p>
 * <p>
 * Signed distance maps are the basis of shape-based interpolation (Raya and
 * Udupa 1990; Herman, Zheng and Bucholtz 1992); see
 * {@code morphology.interpolateSlices}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <B> mask type
 * @see <a href="https://doi.org/10.1109/42.52985">S. P. Raya and J. K. Udupa,
 *      "Shape-based interpolation of multidimensional objects", IEEE Trans.
 *      Med. Imaging 9(1):32-42, 1990</a>
 * @implNote op names='image.signedDistanceTransform'
 */
public class SignedDistanceTransform<B extends BooleanType<B>> implements
	BiFunction<RandomAccessibleInterval<B>, double[], RandomAccessibleInterval<FloatType>>
{

	@OpDependency(name = "image.distanceTransform")
	private Computers.Arity2<RandomAccessibleInterval<BitType>, double[], RandomAccessibleInterval<DoubleType>> dtOp;

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> createOp;

	/**
	 * Computes the signed distance transform.
	 *
	 * @param mask the binary mask
	 * @param calibration the pixel spacing along each dimension (default: 1 in
	 *          every dimension)
	 * @return the signed distances, positive inside the mask
	 */
	@Override
	public RandomAccessibleInterval<FloatType> apply(
		final RandomAccessibleInterval<B> mask,
		@Nullable final double[] calibration)
	{
		final var n = mask.numDimensions();
		final double[] cal;
		if (calibration == null) {
			cal = new double[n];
			Arrays.fill(cal, 1);
		}
		else if (calibration.length != n) {
			throw new IllegalArgumentException("Expected " + n +
				" calibration values but got " + calibration.length);
		}
		else cal = calibration;

		var hasFg = false;
		var hasBg = false;
		for (final var b : net.imglib2.view.Views.iterable(mask)) {
			if (b.get()) hasFg = true;
			else hasBg = true;
			if (hasFg && hasBg) break;
		}
		var diagonal = 0.0;
		var step = Double.POSITIVE_INFINITY;
		for (var d = 0; d < n; d++) {
			diagonal += Math.pow(mask.dimension(d) * cal[d], 2);
			step = Math.min(step, cal[d]);
		}
		diagonal = Math.sqrt(diagonal);
		final var half = step / 2;

		final var dims = mask.dimensionsAsLongArray();
		final var inside = ArrayImgs.doubles(dims);
		final var outside = ArrayImgs.doubles(dims);
		final RandomAccessibleInterval<BitType> fg = Converters.convert(mask, (a,
			b) -> b.set(a.get()), new BitType());
		final RandomAccessibleInterval<BitType> bg = Converters.convert(mask, (a,
			b) -> b.set(!a.get()), new BitType());
		if (hasFg && hasBg) {
			dtOp.compute(fg, cal, inside);
			dtOp.compute(bg, cal, outside);
		}

		final var output = createOp.apply(mask, new FloatType());
		final var fgOnly = !hasBg;
		final var bgOnly = !hasFg;
		final var big = diagonal;
		LoopBuilder.setImages(mask, inside, outside, output).forEachPixel((m, i,
			o, s) -> {
			if (m.get()) s.setReal((fgOnly ? big : i.get()) - half);
			else s.setReal(half - (bgOnly ? big : o.get()));
		});
		return output;
	}
}
