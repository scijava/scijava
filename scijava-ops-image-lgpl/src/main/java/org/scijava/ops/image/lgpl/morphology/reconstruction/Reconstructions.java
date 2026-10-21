/*-
 * #%L
 * LGPL-licensed image processing operations for SciJava Ops, translated from LGPL ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.lgpl.morphology.reconstruction;

import java.util.Arrays;

import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

/**
 * Algorithms shared by the ops of this package, operating on flattened pixel
 * arrays described by a {@link PixelGrid}.
 * <p>
 * Grayscale reconstruction uses the hybrid algorithm of Vincent: one raster
 * scan and one anti-raster scan propagate values through the causal and
 * anti-causal halves of the neighborhood, after which the pixels that may
 * still propagate are processed with a FIFO queue until stability.
 * </p>
 * <p>
 * References:
 * </p>
 * <ul>
 * <li>L. Vincent, "Morphological grayscale reconstruction in image analysis:
 * applications and efficient algorithms", IEEE Transactions on Image
 * Processing 2(2):176-201, 1993.</li>
 * <li>P. Soille, <em>Morphological Image Analysis: Principles and
 * Applications</em>, 2nd ed., Springer, 2003, chapter 6.</li>
 * </ul>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
final class Reconstructions {

	private Reconstructions() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes the reconstruction by dilation of a marker under a mask, in
	 * place. The marker is first clipped to be at most the mask.
	 *
	 * @param marker the marker values, replaced by the reconstruction
	 * @param mask the mask values
	 * @param grid the pixel grid and connectivity
	 */
	static void dilate(final double[] marker, final double[] mask,
		final PixelGrid grid)
	{
		final var size = grid.size;
		final var steps = grid.steps;
		final var pos = new int[grid.n];

		// Forward scan, with the causal half of the neighborhood.
		for (var i = 0; i < size; i++, grid.forward(pos)) {
			var v = marker[i];
			for (final var k : grid.causal) {
				if (grid.contains(pos, k)) v = Math.max(v, marker[i + steps[k]]);
			}
			marker[i] = Math.min(v, mask[i]);
		}

		// Backward scan, with the anti-causal half of the neighborhood. Pixels
		// which could still raise an anti-causal neighbor are queued.
		final var queue = new IntQueue();
		grid.backward(pos);
		for (var i = size - 1; i >= 0; i--, grid.backward(pos)) {
			var v = marker[i];
			for (final var k : grid.anticausal) {
				if (grid.contains(pos, k)) v = Math.max(v, marker[i + steps[k]]);
			}
			v = Math.min(v, mask[i]);
			marker[i] = v;
			for (final var k : grid.anticausal) {
				if (!grid.contains(pos, k)) continue;
				final var q = i + steps[k];
				if (marker[q] < v && marker[q] < mask[q]) {
					queue.add(i);
					break;
				}
			}
		}

		// Propagation until stability.
		while (!queue.isEmpty()) {
			final var p = queue.remove();
			grid.position(p, pos);
			final var v = marker[p];
			for (var k = 0; k < steps.length; k++) {
				if (!grid.contains(pos, k)) continue;
				final var q = p + steps[k];
				if (marker[q] < v && marker[q] != mask[q]) {
					marker[q] = Math.min(v, mask[q]);
					queue.add(q);
				}
			}
		}
	}

	/**
	 * Computes the reconstruction by erosion of a marker over a mask, in place.
	 * The marker is first clipped to be at least the mask.
	 *
	 * @param marker the marker values, replaced by the reconstruction
	 * @param mask the mask values
	 * @param grid the pixel grid and connectivity
	 */
	static void erode(final double[] marker, final double[] mask,
		final PixelGrid grid)
	{
		negate(marker);
		final var negMask = mask.clone();
		negate(negMask);
		dilate(marker, negMask, grid);
		negate(marker);
	}

	/**
	 * Finds the regional maxima of an image: the connected plateaus of constant
	 * value with no neighbor of higher value.
	 *
	 * @param values the image values
	 * @param grid the pixel grid and connectivity
	 * @return a flag per pixel, true for regional maxima
	 */
	static boolean[] regionalMaxima(final double[] values,
		final PixelGrid grid)
	{
		final var size = grid.size;
		final var steps = grid.steps;
		final var pos = new int[grid.n];
		final var visited = new boolean[size];
		final var result = new boolean[size];
		final var plateau = new IntQueue();
		for (var start = 0; start < size; start++) {
			if (visited[start]) continue;
			final var v = values[start];
			var maximum = true;
			plateau.clear();
			plateau.add(start);
			visited[start] = true;
			// Note: the queue keeps all its elements, so that after the flood
			// fill they can be listed again to mark the plateau.
			for (var head = 0; head < plateau.length(); head++) {
				final var p = plateau.get(head);
				grid.position(p, pos);
				for (var k = 0; k < steps.length; k++) {
					if (!grid.contains(pos, k)) continue;
					final var q = p + steps[k];
					final var w = values[q];
					if (w > v) maximum = false;
					else if (w == v && !visited[q]) {
						visited[q] = true;
						plateau.add(q);
					}
				}
			}
			if (maximum) {
				for (var j = 0; j < plateau.length(); j++)
					result[plateau.get(j)] = true;
			}
		}
		return result;
	}

	/**
	 * Finds the regional minima of an image: the connected plateaus of constant
	 * value with no neighbor of lower value.
	 *
	 * @param values the image values (not modified)
	 * @param grid the pixel grid and connectivity
	 * @return a flag per pixel, true for regional minima
	 */
	static boolean[] regionalMinima(final double[] values,
		final PixelGrid grid)
	{
		final var negated = values.clone();
		negate(negated);
		return regionalMaxima(negated, grid);
	}

	/**
	 * Computes the h-maxima transform: the reconstruction by dilation of
	 * {@code f - h} under {@code f}, which suppresses the maxima whose dynamic
	 * is at most {@code h}.
	 *
	 * @param values the image values, replaced by the transform
	 * @param h the height
	 * @param lowest the lowest value representable by the image type
	 * @param grid the pixel grid and connectivity
	 */
	static void hMaxima(final double[] values, final double h,
		final double lowest, final PixelGrid grid)
	{
		checkHeight(h);
		final var mask = values.clone();
		for (var i = 0; i < values.length; i++)
			values[i] = Math.max(values[i] - h, lowest);
		dilate(values, mask, grid);
	}

	/**
	 * Computes the h-minima transform: the reconstruction by erosion of
	 * {@code f + h} over {@code f}, which suppresses the minima whose dynamic
	 * is at most {@code h}.
	 *
	 * @param values the image values, replaced by the transform
	 * @param h the height
	 * @param highest the highest value representable by the image type
	 * @param grid the pixel grid and connectivity
	 */
	static void hMinima(final double[] values, final double h,
		final double highest, final PixelGrid grid)
	{
		checkHeight(h);
		final var mask = values.clone();
		for (var i = 0; i < values.length; i++)
			values[i] = Math.min(values[i] + h, highest);
		erode(values, mask, grid);
	}

	/**
	 * Gets the smallest increment between two values of a type: 1 for integer
	 * types and 0 for floating point types.
	 *
	 * @param type an instance of the type
	 * @return the increment
	 */
	static double increment(final RealType<?> type) {
		return type instanceof IntegerType ? 1 : 0;
	}

	/**
	 * Gets the lowest value of a type: its minimum value for integer types and
	 * negative infinity for floating point types.
	 *
	 * @param type an instance of the type
	 * @return the lowest value
	 */
	static double lowest(final RealType<?> type) {
		return type instanceof IntegerType ? type.getMinValue()
			: Double.NEGATIVE_INFINITY;
	}

	/**
	 * Gets the highest value of a type: its maximum value for integer types and
	 * positive infinity for floating point types.
	 *
	 * @param type an instance of the type
	 * @return the highest value
	 */
	static double highest(final RealType<?> type) {
		return type instanceof IntegerType ? type.getMaxValue()
			: Double.POSITIVE_INFINITY;
	}

	/**
	 * Creates a marker equal to the given values on the border of the grid, and
	 * to their minimum (or maximum) elsewhere.
	 *
	 * @param values the image values
	 * @param grid the pixel grid
	 * @param max true to fill the interior with the maximum, false for the
	 *          minimum
	 * @return the marker values
	 */
	static double[] borderMarker(final double[] values, final PixelGrid grid,
		final boolean max)
	{
		var fill = max ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		for (final var v : values)
			fill = max ? Math.max(fill, v) : Math.min(fill, v);
		final var marker = new double[grid.size];
		final var pos = new int[grid.n];
		for (var i = 0; i < grid.size; i++, grid.forward(pos))
			marker[i] = grid.onBorder(pos) ? values[i] : fill;
		return marker;
	}

	private static void checkHeight(final double h) {
		if (!(h >= 0)) {
			throw new IllegalArgumentException("Height must be non-negative: " + h);
		}
	}

	private static void negate(final double[] values) {
		for (var i = 0; i < values.length; i++)
			values[i] = -values[i];
	}

	/** A growable FIFO queue of pixel indices. */
	static final class IntQueue {

		private int[] data = new int[256];
		private int head;
		private int tail;

		void add(final int value) {
			if (tail == data.length) {
				if (head > data.length / 2) {
					// Reclaim the space of removed elements.
					System.arraycopy(data, head, data, 0, tail - head);
					tail -= head;
					head = 0;
				}
				else data = Arrays.copyOf(data, 2 * data.length);
			}
			data[tail++] = value;
		}

		int remove() {
			return data[head++];
		}

		boolean isEmpty() {
			return head == tail;
		}

		void clear() {
			head = tail = 0;
		}

		/** @return the number of elements added since the last clear */
		int length() {
			return tail;
		}

		/**
		 * @param index the index of the element, counted since the last clear
		 * @return the element
		 */
		int get(final int index) {
			return data[index];
		}
	}
}
