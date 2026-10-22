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

package org.scijava.ops.image.labeling.shapeBasedAverage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.morphology.distance.DistanceTransform;
import net.imglib2.algorithm.morphology.distance.DistanceTransform.DISTANCE_TYPE;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.ops.spi.OpDependency;

/**
 * Combines several label images of the same scene into one by shape-based
 * averaging (Rohlfing &amp; Maurer 2007), e.g. for atlas-based label fusion.
 * <p>
 * For each input {@code k} and label {@code l}, the signed Euclidean
 * distance map {@code d_kl} is computed: at a pixel outside the region of
 * {@code l}, the distance to the nearest pixel of {@code l}; at a pixel
 * inside it, minus the distance to the nearest pixel with another label.
 * Every output pixel is assigned the label with the smallest mean signed
 * distance {@code (1/K) Σ_k d_kl}, i.e. the label whose averaged shape it
 * lies deepest inside. Unlike voting, this yields smooth, averaged shapes:
 * averaging two shifted copies of a disk gives a disk at the midpoint.
 * </p>
 * <p>
 * Every pixel value is a label, including 0 (the background). Where a label
 * is absent from an input, its distance in that input is taken to be the
 * length of the image diagonal. Ties are resolved in favor of the smallest
 * label value. Distances are measured in pixels with the exact Euclidean
 * distance transform of imglib2.
 * </p>
 * <p>
 * Implemented from the paper.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <I> label pixel type
 * @see <a href="https://doi.org/10.1109/TIP.2006.884936">T. Rohlfing and C. R.
 *      Maurer, Jr., "Shape-based averaging", IEEE Trans. Image Process.
 *      16(1):153-161, 2007</a>
 * @implNote op names='labeling.shapeBasedAverage'
 */
public class ShapeBasedAverage<I extends IntegerType<I>> implements
	Function<List<RandomAccessibleInterval<I>>, RandomAccessibleInterval<I>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, I, RandomAccessibleInterval<I>> createOp;

	/**
	 * Computes the shape-based average.
	 *
	 * @param inputs the label images, all with the same dimensions
	 * @return the averaged label image
	 */
	@Override
	public RandomAccessibleInterval<I> apply(
		final List<RandomAccessibleInterval<I>> inputs)
	{
		if (inputs.isEmpty()) {
			throw new IllegalArgumentException("No label images given");
		}
		final var first = inputs.get(0);
		for (final var in : inputs) {
			if (!Intervals.equalDimensions(first, in)) {
				throw new IllegalArgumentException(
					"All label images must have the same dimensions");
			}
		}
		final var size = Intervals.numElements(first);
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image is too large: " + size +
				" pixels");
		}
		final var count = (int) size;
		final var dims = first.dimensionsAsLongArray();
		var diag = 0.0;
		for (final var d : dims)
			diag += (double) d * d;
		diag = Math.sqrt(diag);

		final var labels = new ArrayList<long[]>();
		final var all = new TreeSet<Long>();
		for (final var in : inputs) {
			final var values = new long[count];
			var p = 0;
			for (final var t : Views.flatIterable(in)) {
				values[p] = t.getIntegerLong();
				all.add(values[p++]);
			}
			labels.add(values);
		}

		final var best = new long[count];
		final var bestDist = new double[count];
		Arrays.fill(bestDist, Double.POSITIVE_INFINITY);
		final var mean = new double[count];
		final var inside = new boolean[count];
		for (final long label : all) {
			Arrays.fill(mean, 0);
			for (final var values : labels) {
				var any = false;
				var allInside = true;
				for (var p = 0; p < count; p++) {
					inside[p] = values[p] == label;
					any |= inside[p];
					allInside &= inside[p];
				}
				if (!any) {
					for (var p = 0; p < count; p++)
						mean[p] += diag;
					continue;
				}
				final var toInside = distances(inside, true, dims);
				final var toOutside = allInside ? null : distances(inside, false,
					dims);
				for (var p = 0; p < count; p++) {
					mean[p] += inside[p] ? (toOutside == null ? -diag : -toOutside[p])
						: toInside[p];
				}
			}
			// Note: The sum orders labels like the mean, as K is constant.
			for (var p = 0; p < count; p++) {
				if (mean[p] < bestDist[p]) {
					bestDist[p] = mean[p];
					best[p] = label;
				}
			}
		}

		final var output = createOp.apply(first, first.getType()
			.createVariable());
		var p = 0;
		for (final var t : Views.flatIterable(output))
			t.setInteger(best[p++]);
		return output;
	}

	/**
	 * Computes the Euclidean distance of each pixel to the nearest pixel whose
	 * mask value equals {@code target}.
	 */
	private static double[] distances(final boolean[] mask,
		final boolean target, final long[] dims)
	{
		final var d = new double[mask.length];
		for (var p = 0; p < mask.length; p++)
			d[p] = mask[p] == target ? 0 : 1e20;
		DistanceTransform.transform(ArrayImgs.doubles(d, dims),
			DISTANCE_TYPE.EUCLIDIAN);
		for (var p = 0; p < d.length; p++)
			d[p] = Math.sqrt(d[p]);
		return d;
	}
}
