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

package org.scijava.ops.image.filter.weightedMedian;

import java.util.ArrayList;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Weighted median filter.
 * <p>
 * Each output pixel is the weighted median of the input values in a
 * neighborhood given by a weight kernel: the smallest value {@code v} such that
 * the summed weight of all neighborhood values {@code <= v} is at least half of
 * the total weight. With uniform weights over an odd number of pixels, this is
 * the ordinary median. The kernel may have any number of dimensions (matching
 * the input) and must have an odd size in every dimension; its center pixel is
 * aligned with the output pixel. Weights must be non-negative; pixels with
 * zero weight are ignored.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @param <K> kernel pixel type
 * @param <V> output pixel type
 * @implNote op names='filter.weightedMedian'
 */
public class DefaultWeightedMedian<T extends RealType<T>, K extends RealType<K>, V extends RealType<V>>
	implements
	Computers.Arity3<RandomAccessibleInterval<T>, RandomAccessibleInterval<K>, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<V>>
{

	/**
	 * Applies the weighted median filter.
	 *
	 * @param input the image to filter
	 * @param kernel the weights of the neighborhood, centered on each pixel
	 * @param outOfBounds how to extend the input beyond its bounds (default:
	 *          repeat the border values)
	 * @param output the filtered image
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final RandomAccessibleInterval<K> kernel,
		@Nullable OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBounds,
		final RandomAccessibleInterval<V> output)
	{
		final var n = input.numDimensions();
		if (kernel.numDimensions() != n) {
			throw new IllegalArgumentException("Kernel must have " + n +
				" dimensions");
		}
		for (var d = 0; d < n; d++) {
			if (kernel.dimension(d) % 2 == 0) {
				throw new IllegalArgumentException(
					"Kernel must have an odd size in every dimension");
			}
		}
		if (outOfBounds == null) outOfBounds = new OutOfBoundsBorderFactory<>();

		// Collect the offsets and weights of the nonzero kernel entries.
		final var offsetList = new ArrayList<long[]>();
		final var weightList = new ArrayList<Double>();
		final var kc = Views.flatIterable(kernel).localizingCursor();
		while (kc.hasNext()) {
			final var w = kc.next().getRealDouble();
			if (w < 0) {
				throw new IllegalArgumentException("Kernel weights must be >= 0");
			}
			if (w == 0) continue;
			final var offset = new long[n];
			for (var d = 0; d < n; d++) {
				offset[d] = kc.getLongPosition(d) - (kernel.min(d) + kernel.max(d)) /
					2;
			}
			offsetList.add(offset);
			weightList.add(w);
		}
		final var count = offsetList.size();
		if (count == 0) throw new IllegalArgumentException(
			"Kernel has no positive weights");
		final var offsets = offsetList.toArray(new long[0][]);
		final var weights = weightList.stream().mapToDouble(Double::doubleValue)
			.toArray();
		double totalWeight = 0;
		for (final var w : weights)
			totalWeight += w;
		final var halfWeight = totalWeight / 2;

		final var extended = Views.extend(input, outOfBounds);
		LoopBuilder.setImages(Intervals.positions(output), output).multiThreaded()
			.forEachChunk(chunk -> {
				final var ra = extended.randomAccess();
				final var values = new double[count];
				final var w = new double[count];
				final var pos = new long[n];
				chunk.forEachPixel((p, o) -> {
					p.localize(pos);
					for (var i = 0; i < count; i++) {
						for (var d = 0; d < n; d++) {
							ra.setPosition(pos[d] + offsets[i][d], d);
						}
						values[i] = ra.get().getRealDouble();
						w[i] = weights[i];
					}
					o.setReal(weightedMedian(values, w, count, halfWeight));
				});
				return null;
			});
	}

	/**
	 * Computes the weighted median of the given values. Both arrays are
	 * reordered in the process.
	 */
	static double weightedMedian(final double[] values, final double[] weights,
		final int count, final double halfWeight)
	{
		sortPairs(values, weights, count);
		double cumulative = 0;
		for (var i = 0; i < count; i++) {
			cumulative += weights[i];
			if (cumulative >= halfWeight) return values[i];
		}
		return values[count - 1];
	}

	/** Sorts values ascending, permuting weights along (Shell sort). */
	private static void sortPairs(final double[] v, final double[] w,
		final int count)
	{
		var gap = 1;
		while (gap < count / 3)
			gap = 3 * gap + 1;
		for (; gap > 0; gap /= 3) {
			for (var i = gap; i < count; i++) {
				final var tv = v[i];
				final var tw = w[i];
				var j = i;
				while (j >= gap && v[j - gap] > tv) {
					v[j] = v[j - gap];
					w[j] = w[j - gap];
					j -= gap;
				}
				v[j] = tv;
				w[j] = tw;
			}
		}
	}
}
