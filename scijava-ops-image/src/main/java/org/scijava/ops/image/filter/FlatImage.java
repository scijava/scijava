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

package org.scijava.ops.image.filter;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * Utility methods for filters that operate on an image copied into a flat
 * {@code double[]} array, in flat iteration order (dimension 0 fastest).
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class FlatImage {

	private FlatImage() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Gets the number of array elements needed to store {@code perPixel}
	 * values for each pixel of an image.
	 *
	 * @param dims the image dimensions
	 * @param perPixel the number of values stored per pixel
	 * @return the number of elements
	 * @throws IllegalArgumentException if the elements do not fit in an array
	 */
	public static int size(final Dimensions dims, final int perPixel) {
		final var size = Intervals.numElements(dims);
		if (size * perPixel > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image is too large: " + size +
				" pixels");
		}
		return (int) size;
	}

	/**
	 * Computes the array stride of each dimension.
	 *
	 * @param dims the image dimensions
	 * @return the strides, with {@code strides[0] == 1}
	 */
	public static int[] strides(final long[] dims) {
		final var strides = new int[dims.length];
		for (var d = 0; d < dims.length; d++)
			strides[d] = d == 0 ? 1 : strides[d - 1] * (int) dims[d - 1];
		return strides;
	}

	/**
	 * Copies an image into a new array, in flat iteration order.
	 *
	 * @param image the image
	 * @return the pixel values
	 * @throws IllegalArgumentException if the image does not fit in an array
	 */
	public static double[] read(
		final RandomAccessibleInterval<? extends RealType<?>> image)
	{
		final var values = new double[size(image, 1)];
		var i = 0;
		for (final RealType<?> t : Views.flatIterable(image))
			values[i++] = t.getRealDouble();
		return values;
	}

	/**
	 * Copies an array into an image, in flat iteration order, clamping the
	 * values to the range of the image type.
	 *
	 * @param values the pixel values
	 * @param image the image to write into
	 */
	public static void write(final double[] values,
		final RandomAccessibleInterval<? extends RealType<?>> image)
	{
		var i = 0;
		for (final RealType<?> t : Views.flatIterable(image)) {
			t.setReal(Math.max(t.getMinValue(), Math.min(t.getMaxValue(),
				values[i++])));
		}
	}

	/**
	 * Advances a position by one pixel in flat iteration order, wrapping to
	 * zero after the last pixel.
	 *
	 * @param pos the position to advance
	 * @param dims the image dimensions
	 */
	public static void increment(final long[] pos, final long[] dims) {
		for (var d = 0; d < pos.length; d++) {
			if (++pos[d] < dims[d]) return;
			pos[d] = 0;
		}
	}

	/**
	 * Advances a position by one pixel in flat iteration order, wrapping to
	 * zero after the last pixel.
	 *
	 * @param pos the position to advance
	 * @param dims the image dimensions
	 */
	public static void increment(final int[] pos, final long[] dims) {
		for (var d = 0; d < pos.length; d++) {
			if (++pos[d] < dims[d]) return;
			pos[d] = 0;
		}
	}
}
