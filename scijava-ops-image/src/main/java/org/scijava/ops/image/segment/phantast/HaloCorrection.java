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

package org.scijava.ops.image.segment.phantast;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * Halo artifact correction of PHANTAST, for 2D binary masks of phase contrast
 * images. See {@link DefaultPhantast}.
 * <p>
 * Translated from {@code PHANTAST_.haloCorrection} and
 * {@code PHANTAST_.getDirectionImage} of the PHANTAST plugin for Fiji,
 * Copyright (c) 2013-2014, Nicolas Jaccard, University College London, used
 * under the BSD 3-Clause license.
 * </p>
 * <p>
 * The original PHANTAST license notice follows.
 * </p>
 *
 * <pre>
 * Copyright (c) 2013, Nicolas Jaccard
 * Department of Biochemical Engineering, UCL
 * Centre for Mathematics and Physics in the Life Sciences and Experimental
 * Biology, UCL
 * The British Heart Foundation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution
 *   * Neither the names of the University College London or British Heart
 *     Foundation nor the names of its contributors may be used to endorse
 *     or promote products derived from this software without specific
 *     prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 *
 * @author Nicolas Jaccard
 * @author Curtis Rueden
 */
final class HaloCorrection {

	private HaloCorrection() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * The eight Kirsch compass kernels, in row-major order with the first row
	 * at {@code y - 1}, for the directions E, NE, N, NW, W, SW, S, SE (with
	 * north pointing towards decreasing {@code y}).
	 */
	private static final int[][] KIRSCH = { //
		{ -3, -3, 5, -3, 0, 5, -3, -3, 5 }, //
		{ -3, 5, 5, -3, 0, 5, -3, -3, -3 }, //
		{ 5, 5, 5, -3, 0, -3, -3, -3, -3 }, //
		{ 5, 5, -3, 5, 0, -3, -3, -3, -3 }, //
		{ 5, -3, -3, 5, 0, -3, 5, -3, -3 }, //
		{ -3, -3, -3, 5, 0, -3, 5, 5, -3 }, //
		{ -3, -3, -3, -3, 0, -3, 5, 5, 5 }, //
		{ -3, -3, -3, -3, 0, 5, -3, 5, 5 } };

	/** The (x, y) offsets of the directions E, NE, N, NW, W, SW, S, SE. */
	private static final int[][] OFFSETS = { { 1, 0 }, { 1, -1 }, { 0, -1 }, {
		-1, -1 }, { -1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 } };

	/**
	 * For each direction, the indices into {@link #OFFSETS} of the cone of
	 * three directions centered on it.
	 */
	private static final int[][] CONES = { { 0, 1, 7 }, { 1, 0, 2 }, { 2, 1,
		3 }, { 3, 2, 4 }, { 4, 3, 5 }, { 5, 4, 6 }, { 6, 5, 7 }, { 7, 0, 6 } };

	/**
	 * Shrinks the given mask in place to remove halo artifacts.
	 *
	 * @param mask the 2D binary mask to correct
	 * @param image the 2D phase contrast image, with the same interval as the
	 *          mask
	 */
	static <T extends RealType<T>> void apply(
		final RandomAccessibleInterval<BitType> mask,
		final RandomAccessibleInterval<T> image)
	{
		if (!Intervals.equals(mask, image)) {
			throw new IllegalArgumentException(
				"Mask and image intervals do not match");
		}
		final var w = (int) mask.dimension(0);
		final var h = (int) mask.dimension(1);
		if ((long) w * h > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image is too large");
		}

		// Copy the mask and the image into flat arrays.
		final var fg = new boolean[w * h];
		final var pixels = new float[w * h];
		final var mc = Views.flatIterable(mask).cursor();
		final var ic = Views.flatIterable(image).cursor();
		for (var i = 0; i < fg.length; i++) {
			fg[i] = mc.next().get();
			pixels[i] = ic.next().getRealFloat();
		}

		final var direction = directions(pixels, w, h);

		// Start from the outline: foreground pixels with a background pixel
		// among their 8 neighbors.
		var queue = new IntList();
		for (var y = 0; y < h; y++) {
			for (var x = 0; x < w; x++) {
				if (fg[x + y * w] && touchesBackground(fg, w, h, x, y)) {
					queue.add(x + y * w);
				}
			}
		}

		// Walk from each queued pixel into the foreground, along the cone of
		// the local gradient direction, removing the pixels that lead somewhere.
		final var visited = new boolean[w * h];
		while (queue.size > 0) {
			final var next = new IntList();
			final var toRemove = new IntList();
			for (var q = 0; q < queue.size; q++) {
				final var i = queue.data[q];
				if (visited[i]) continue;
				final var x = i % w;
				final var y = i / w;
				if (x <= 0 || y <= 0 || x >= w - 1 || y >= h - 1) continue;
				visited[i] = true;
				var validPath = false;
				for (final var d : CONES[direction[i]]) {
					final var n = (x + OFFSETS[d][0]) + (y + OFFSETS[d][1]) * w;
					if (fg[n]) {
						validPath = true;
						next.add(n);
					}
				}
				if (validPath) toRemove.add(i);
			}
			for (var r = 0; r < toRemove.size; r++) {
				fg[toRemove.data[r]] = false;
			}
			queue = next;
		}

		// Write the corrected mask back.
		final var oc = Views.flatIterable(mask).cursor();
		for (final var b : fg) {
			oc.next().set(b);
		}
	}

	private static boolean touchesBackground(final boolean[] fg, final int w,
		final int h, final int x, final int y)
	{
		for (var yy = Math.max(0, y - 1); yy <= Math.min(h - 1, y + 1); yy++) {
			for (var xx = Math.max(0, x - 1); xx <= Math.min(w - 1, x + 1); xx++) {
				if (!fg[xx + yy * w]) return true;
			}
		}
		return false;
	}

	/**
	 * Computes, for each pixel, the index of the Kirsch compass kernel with the
	 * maximal response (the first one in case of ties), i.e. the direction of
	 * increasing intensity. The image border is extended by replication.
	 */
	private static byte[] directions(final float[] pixels, final int w,
		final int h)
	{
		final var direction = new byte[w * h];
		final var nbh = new float[9];
		for (var y = 0; y < h; y++) {
			for (var x = 0; x < w; x++) {
				var k = 0;
				for (var dy = -1; dy <= 1; dy++) {
					final var yy = Math.min(h - 1, Math.max(0, y + dy));
					for (var dx = -1; dx <= 1; dx++) {
						final var xx = Math.min(w - 1, Math.max(0, x + dx));
						nbh[k++] = pixels[xx + yy * w];
					}
				}
				var best = 0;
				var max = Double.NEGATIVE_INFINITY;
				for (var d = 0; d < KIRSCH.length; d++) {
					var sum = 0.0;
					for (var j = 0; j < 9; j++) {
						sum += KIRSCH[d][j] * nbh[j];
					}
					// Note: ImageJ stores the responses as floats before comparing them.
					final var response = (float) sum;
					if (response > max) {
						max = response;
						best = d;
					}
				}
				direction[x + y * w] = (byte) best;
			}
		}
		return direction;
	}

	/** A minimal growable list of primitive ints. */
	private static final class IntList {

		private int[] data = new int[16];
		private int size;

		private void add(final int value) {
			if (size == data.length) {
				data = java.util.Arrays.copyOf(data, 2 * size);
			}
			data[size++] = value;
		}
	}
}
