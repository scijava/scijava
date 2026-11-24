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

package org.scijava.ops.image.threshold.hysteresis;

import java.util.BitSet;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.image.util.Neighborhoods;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Hysteresis thresholding, as introduced by Canny (1986) for edge linking.
 * <p>
 * A pixel belongs to the foreground if its value is at least {@code high}, or
 * if its value is at least {@code low} and it is connected to such a pixel by
 * a path of pixels whose values are all at least {@code low}. Connectivity is
 * given by a {@link Shape}: the neighbors of a pixel are the pixels of the
 * shape's neighborhood around it (made symmetric, so that connectivity is
 * symmetric even for an asymmetric shape). The result therefore keeps every
 * connected component of {@code input >= low} that contains at least one
 * pixel with {@code input >= high}, and removes all others. NaN pixels are never
 * foreground, and do not connect their neighbors.
 * </p>
 * <p>
 * Implemented from the description in the paper, as a breadth-first flood
 * fill seeded at the strong pixels.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @see <a href="https://doi.org/10.1109/TPAMI.1986.4767851">J. Canny, "A
 *      Computational Approach to Edge Detection", IEEE Trans. Pattern Anal.
 *      Mach. Intell. 8(6):679-698, 1986</a>
 * @implNote op names='threshold.hysteresis'
 */
public class DefaultHysteresisThreshold<T extends RealType<T>> implements
	Functions.Arity4<RandomAccessibleInterval<T>, Double, Double, Shape, RandomAccessibleInterval<BitType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, BitType, RandomAccessibleInterval<BitType>> createOp;

	/**
	 * Applies hysteresis thresholding.
	 *
	 * @param input the image to threshold
	 * @param low the low threshold: weak pixels have values of at least this
	 * @param high the high threshold: strong (seed) pixels have values of at
	 *          least this; it must not be less than {@code low}
	 * @param connectivity the neighborhood defining which pixels are connected
	 *          (default: all pixels within distance 1 in every dimension, i.e.
	 *          8-connectivity in 2D and 26-connectivity in 3D)
	 * @return a binary image of the same dimensions as the input, true for
	 *         foreground pixels
	 */
	@Override
	public RandomAccessibleInterval<BitType> apply(
		final RandomAccessibleInterval<T> input, final Double low,
		final Double high, @Nullable final Shape connectivity)
	{
		if (low > high) {
			throw new IllegalArgumentException("Low threshold " + low +
				" exceeds high threshold " + high);
		}
		final var shape = connectivity == null ? new RectangleShape(1, true)
			: connectivity;
		final var n = input.numDimensions();
		final var size = Intervals.numElements(input);
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image is too large: " + size +
				" pixels");
		}
		final var count = (int) size;
		final var dims = input.dimensionsAsLongArray();
		final var strides = new long[n];
		for (var d = 0; d < n; d++)
			strides[d] = d == 0 ? 1 : strides[d - 1] * dims[d - 1];
		final var offsets = Neighborhoods.neighborOffsets(shape, n);

		final var weak = new BitSet(count);
		final var strong = new BitSet(count);
		var i = 0;
		for (final var t : Views.flatIterable(input)) {
			final var v = t.getRealDouble();
			if (v >= low) weak.set(i);
			if (v >= high) strong.set(i);
			i++;
		}

		final var result = new BitSet(count);
		final var queue = new int[count];
		final var pos = new long[n];
		var tail = 0;
		for (var s = strong.nextSetBit(0); s >= 0; s = strong.nextSetBit(s + 1)) {
			result.set(s);
			queue[tail++] = s;
		}
		for (var head = 0; head < tail; head++) {
			final var index = queue[head];
			var rem = (long) index;
			for (var d = n - 1; d >= 0; d--) {
				pos[d] = rem / strides[d];
				rem %= strides[d];
			}
			for (final var offset : offsets) {
				long neighbor = 0;
				var inside = true;
				for (var d = 0; d < n; d++) {
					final var p = pos[d] + offset[d];
					if (p < 0 || p >= dims[d]) {
						inside = false;
						break;
					}
					neighbor += p * strides[d];
				}
				if (!inside) continue;
				final var ni = (int) neighbor;
				if (weak.get(ni) && !result.get(ni)) {
					result.set(ni);
					queue[tail++] = ni;
				}
			}
		}

		final var output = createOp.apply(input, new BitType());
		i = 0;
		for (final var t : Views.flatIterable(output))
			t.set(result.get(i++));
		return output;
	}
}
