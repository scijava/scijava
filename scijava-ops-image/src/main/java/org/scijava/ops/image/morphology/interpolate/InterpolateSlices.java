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

package org.scijava.ops.image.morphology.interpolate;

import java.util.ArrayList;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.image.util.Calibrations;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Shape-based interpolation of a binary mask between sparsely annotated
 * slices.
 * <p>
 * Slices along {@code dim} that contain at least one foreground pixel are
 * annotated; all other slices are empty. For each pair of consecutive
 * annotated slices {@code a < b}, every empty slice {@code z} between them is
 * filled where the linear interpolation of their signed distance maps (from
 * {@code image.signedDistanceTransform}, positive inside),
 * {@code ((b - z) sdt_a + (z - a) sdt_b) / (b - a)}, is non-negative.
 * Annotated slices are copied unchanged, and empty slices before the first or
 * after the last annotated slice stay empty. This is the usual way to
 * complete a manual 3D segmentation drawn on a few slices.
 * </p>
 * <p>
 * Implemented from S. P. Raya and J. K. Udupa, "Shape-based interpolation of
 * multidimensional objects", IEEE Trans. Med. Imaging 9(1):32-42, 1990, and
 * G. T. Herman, J. Zheng and C. A. Bucholtz, "Shape-based interpolation", IEEE
 * Comput. Graph. Appl. 12(3):69-79, 1992, with exact Euclidean instead of
 * chamfer distances.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <B> mask type
 * @implNote op names='morphology.interpolateSlices'
 */
public class InterpolateSlices<B extends BooleanType<B>> implements
	Functions.Arity3<RandomAccessibleInterval<B>, Integer, double[], RandomAccessibleInterval<BitType>>
{

	@OpDependency(name = "image.signedDistanceTransform")
	private BiFunction<RandomAccessibleInterval<B>, double[], RandomAccessibleInterval<FloatType>> sdtOp;

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, BitType, RandomAccessibleInterval<BitType>> createOp;

	/**
	 * Interpolates the empty slices of a mask.
	 *
	 * @param mask the mask, at least 2D, with foreground drawn on some slices
	 * @param dim the dimension along which slices are taken (default: the last
	 *          dimension)
	 * @param calibration the pixel spacing along each dimension of the mask,
	 *          including {@code dim} (default: 1 in every dimension); only the
	 *          spacing within a slice matters, since the interpolation between
	 *          slices is linear in the slice index
	 * @return the interpolated mask
	 */
	@Override
	public RandomAccessibleInterval<BitType> apply(
		final RandomAccessibleInterval<B> mask, @Nullable final Integer dim,
		@Nullable final double[] calibration)
	{
		final var n = mask.numDimensions();
		if (n < 2) throw new IllegalArgumentException("Mask must be at least 2D");
		final int d = dim == null ? n - 1 : dim;
		if (d < 0 || d >= n) {
			throw new IllegalArgumentException("No such dimension: " + d);
		}
		final var sliceCalibration = sliceCalibration(calibration, n, d);
		final var output = createOp.apply(mask, new BitType());
		final long mMin = mask.min(d), oMin = output.min(d);

		final var annotated = new ArrayList<Long>();
		for (long z = 0; z < mask.dimension(d); z++) {
			final var slice = Views.hyperSlice(mask, d, mMin + z);
			var any = false;
			for (final var b : Views.iterable(slice)) {
				if (b.get()) {
					any = true;
					break;
				}
			}
			if (!any) continue;
			annotated.add(z);
			LoopBuilder.setImages(slice, Views.hyperSlice(output, d, oMin + z))
				.forEachPixel((i, o) -> o.set(i.get()));
		}

		RandomAccessibleInterval<FloatType> sdtB = null;
		for (var i = 0; i + 1 < annotated.size(); i++) {
			final long a = annotated.get(i), b = annotated.get(i + 1);
			final var sdtA = sdtB != null ? sdtB : sdtOp.apply(Views.hyperSlice(mask,
				d, mMin + a), sliceCalibration);
			sdtB = sdtOp.apply(Views.hyperSlice(mask, d, mMin + b),
				sliceCalibration);
			final var span = (double) (b - a);
			for (var z = a + 1; z < b; z++) {
				final var wa = (b - z) / span;
				final var wb = (z - a) / span;
				LoopBuilder.setImages(sdtA, sdtB, Views.hyperSlice(output, d, oMin +
					z)).forEachPixel((sa, sb, o) -> o.set(wa * sa.get() + wb * sb
						.get() >= 0));
			}
		}
		return output;
	}

	/** Drops the spacing along {@code d} from a validated calibration. */
	private static double[] sliceCalibration(final double[] calibration,
		final int n, final int d)
	{
		if (calibration == null) return null;
		final var c = Calibrations.spacing(calibration, n, "calibration");
		final var slice = new double[n - 1];
		var j = 0;
		for (var i = 0; i < n; i++)
			if (i != d) slice[j++] = c[i];
		return slice;
	}
}
