/* #%L
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

package org.scijava.ops.image.morphology.reconstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.ops.image.util.Neighborhoods;

/**
 * A flattened n-dimensional pixel grid (first dimension varying fastest)
 * together with a symmetric neighborhood (the connectivity), as used by the
 * reconstruction-based ops of this package.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
final class PixelGrid {

	/** Number of dimensions. */
	final int n;

	/** Size of each dimension. */
	final int[] dims;

	/** Linear index step of each dimension. */
	final int[] strides;

	/** Total number of pixels. */
	final int size;

	/** Neighbor offsets (symmetric, excluding the origin). */
	final int[][] offsets;

	/** Linear index step of each neighbor offset. */
	final int[] steps;

	/** Indices of the offsets preceding the origin in raster order. */
	final int[] causal;

	/** Indices of the offsets following the origin in raster order. */
	final int[] anticausal;

	/**
	 * Creates a grid with the dimensions of the given interval.
	 *
	 * @param interval the image whose dimensions to use
	 * @param connectivity the neighborhood defining which pixels are adjacent,
	 *          or null for full connectivity (a {@link RectangleShape} of span
	 *          1, i.e. 8-connectivity in 2D and 26-connectivity in 3D)
	 */
	PixelGrid(final RandomAccessibleInterval<?> interval,
		final Shape connectivity)
	{
		n = interval.numDimensions();
		if (Intervals.numElements(interval) > Integer.MAX_VALUE - 8) {
			throw new IllegalArgumentException(
				"Images with more than 2^31 pixels are not supported");
		}
		dims = new int[n];
		strides = new int[n];
		var s = 1;
		for (var d = 0; d < n; d++) {
			dims[d] = (int) interval.dimension(d);
			strides[d] = s;
			s *= dims[d];
		}
		size = s;

		offsets = Neighborhoods.neighborOffsets(connectivity, n);
		steps = new int[offsets.length];
		final List<Integer> before = new ArrayList<>();
		final List<Integer> after = new ArrayList<>();
		for (var k = 0; k < offsets.length; k++) {
			var step = 0;
			for (var d = 0; d < n; d++)
				step += offsets[k][d] * strides[d];
			steps[k] = step;
			if (precedes(offsets[k])) before.add(k);
			else after.add(k);
		}
		causal = before.stream().mapToInt(Integer::intValue).toArray();
		anticausal = after.stream().mapToInt(Integer::intValue).toArray();
	}

	/**
	 * Tests whether the neighbor at the given offset lies inside the grid.
	 *
	 * @param pos the position of the center pixel
	 * @param k the index of the neighbor offset
	 * @return true if the neighbor is inside the grid
	 */
	boolean contains(final int[] pos, final int k) {
		final var o = offsets[k];
		for (var d = 0; d < n; d++) {
			final var c = pos[d] + o[d];
			if (c < 0 || c >= dims[d]) return false;
		}
		return true;
	}

	/**
	 * Computes the position of the pixel with the given linear index.
	 *
	 * @param index the linear index
	 * @param pos the array to fill with the position
	 */
	void position(int index, final int[] pos) {
		for (var d = 0; d < n; d++) {
			pos[d] = index % dims[d];
			index /= dims[d];
		}
	}

	/**
	 * Advances a position by one pixel in raster order.
	 *
	 * @param pos the position to advance
	 */
	void forward(final int[] pos) {
		for (var d = 0; d < n; d++) {
			if (++pos[d] < dims[d]) return;
			pos[d] = 0;
		}
	}

	/**
	 * Moves a position back by one pixel in raster order.
	 *
	 * @param pos the position to move
	 */
	void backward(final int[] pos) {
		for (var d = 0; d < n; d++) {
			if (--pos[d] >= 0) return;
			pos[d] = dims[d] - 1;
		}
	}

	/**
	 * Tests whether a position lies on the border of the grid.
	 *
	 * @param pos the position to test
	 * @return true if any coordinate is at the minimum or maximum of its
	 *         dimension
	 */
	boolean onBorder(final int[] pos) {
		for (var d = 0; d < n; d++) {
			if (pos[d] == 0 || pos[d] == dims[d] - 1) return true;
		}
		return false;
	}

	/**
	 * Copies the values of an image into a new array, in flat iteration order.
	 *
	 * @param image the image to copy
	 * @return the values of the image
	 * @throws IllegalArgumentException if the image contains NaN, which the
	 *           raster scans would otherwise spread over the whole output (as
	 *           in {@code ComponentTree})
	 */
	double[] values(final RandomAccessibleInterval<? extends RealType<?>> image) {
		checkDimensions(image);
		final var values = new double[size];
		var i = 0;
		for (final var t : Views.flatIterable(image)) {
			final var v = t.getRealDouble();
			if (Double.isNaN(v)) {
				throw new IllegalArgumentException("NaN values are not supported");
			}
			values[i++] = v;
		}
		return values;
	}

	/**
	 * Writes values into an image, in flat iteration order.
	 *
	 * @param values the values to write
	 * @param image the image to write into
	 */
	void write(final double[] values,
		final RandomAccessibleInterval<? extends RealType<?>> image)
	{
		checkDimensions(image);
		var i = 0;
		for (final var t : Views.flatIterable(image))
			t.setReal(values[i++]);
	}

	/**
	 * Copies the values of a binary image into a new array, in flat iteration
	 * order.
	 *
	 * @param image the image to copy
	 * @return the flags of the image
	 */
	boolean[] flags(
		final RandomAccessibleInterval<? extends BooleanType<?>> image)
	{
		checkDimensions(image);
		final var flags = new boolean[size];
		var i = 0;
		for (final var t : Views.flatIterable(image))
			flags[i++] = t.get();
		return flags;
	}

	/**
	 * Writes flags into a binary image, in flat iteration order.
	 *
	 * @param flags the flags to write
	 * @param image the image to write into
	 */
	void write(final boolean[] flags,
		final RandomAccessibleInterval<? extends BooleanType<?>> image)
	{
		checkDimensions(image);
		var i = 0;
		for (final var t : Views.flatIterable(image))
			t.set(flags[i++]);
	}

	/**
	 * Checks that an image has the dimensions of this grid.
	 *
	 * @param image the image to check
	 */
	void checkDimensions(final RandomAccessibleInterval<?> image) {
		if (image.numDimensions() != n) {
			throw new IllegalArgumentException("Expected " + n +
				" dimensions but got " + image.numDimensions());
		}
		for (var d = 0; d < n; d++) {
			if (image.dimension(d) != dims[d]) {
				throw new IllegalArgumentException("Image dimensions " + Arrays
					.toString(image.dimensionsAsLongArray()) + " do not match " + Arrays
						.toString(dims));
			}
		}
	}

	/**
	 * Tests whether an offset precedes the origin in raster order (the last
	 * dimension being the most significant).
	 */
	private static boolean precedes(final int[] offset) {
		for (var d = offset.length - 1; d >= 0; d--) {
			if (offset[d] != 0) return offset[d] < 0;
		}
		return false;
	}
}
