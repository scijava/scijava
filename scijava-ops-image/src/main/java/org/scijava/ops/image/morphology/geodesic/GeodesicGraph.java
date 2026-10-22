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

package org.scijava.ops.image.morphology.geodesic;

import java.util.ArrayList;
import java.util.Arrays;

import net.imglib2.algorithm.neighborhood.RectangleShape;

import org.scijava.ops.image.util.Neighborhoods;

/**
 * A pixel grid graph for shortest-path (geodesic) distance propagation within
 * regions. Each pixel belongs to a region (a non-negative id) or to none
 * (id -1); edges connect pixels of the same region along the primitive
 * offsets of the {@code (2r+1)^n} neighborhood, weighted by their Euclidean
 * length in the units of the pixel spacing. Distances are computed with
 * the algorithm of Dijkstra.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
final class GeodesicGraph {

	final int size;
	final int[] region;
	final double[] dist;
	final int[] pred;

	private final int n;
	private final int[] dims;
	private final int[] strides;
	private final int[][] offsets;
	private final int[] deltas;
	private final double[] weights;
	private final int[] via1;
	private final int[] via2;

	GeodesicGraph(final long[] dimensions, final int[] region,
		final double[] spacing, final int radius)
	{
		if (radius != 1 && radius != 2) {
			throw new IllegalArgumentException(
				"Neighborhood radius must be 1 or 2, but was " + radius);
		}
		n = dimensions.length;
		dims = new int[n];
		strides = new int[n];
		long total = 1;
		for (var d = 0; d < n; d++) {
			dims[d] = (int) dimensions[d];
			strides[d] = (int) total;
			total *= dimensions[d];
		}
		if (total > Integer.MAX_VALUE - 8) {
			throw new IllegalArgumentException("Image too large: " + total +
				" pixels");
		}
		size = (int) total;
		this.region = region;
		dist = new double[size];
		pred = new int[size];

		final var offs = new ArrayList<int[]>();
		for (final var v : Neighborhoods.rasterOffsets(new RectangleShape(radius,
			false), n))
		{
			if (isPrimitive(v)) offs.add(v);
		}
		final var count = offs.size();
		offsets = offs.toArray(new int[count][]);
		deltas = new int[count];
		weights = new double[count];
		via1 = new int[count];
		via2 = new int[count];
		for (var k = 0; k < count; k++) {
			final var o = offsets[k];
			double w2 = 0;
			var max = 0;
			for (var d = 0; d < n; d++) {
				deltas[k] += o[d] * strides[d];
				w2 += o[d] * spacing[d] * o[d] * spacing[d];
				max = Math.max(max, Math.abs(o[d]));
			}
			weights[k] = Math.sqrt(w2);
			if (max > 1) {
				// Note: a step longer than one pixel must pass through one of the two
				// pixels nearest to its midpoint, so it cannot jump across a gap.
				for (var d = 0; d < n; d++) {
					via1[k] += Math.floorDiv(o[d], 2) * strides[d];
					via2[k] += -Math.floorDiv(-o[d], 2) * strides[d];
				}
			}
		}
	}

	/** Checks that the offset is non-zero and its components are coprime. */
	private static boolean isPrimitive(final int[] v) {
		var g = 0;
		for (final var c : v)
			g = gcd(g, Math.abs(c));
		return g == 1;
	}

	private static int gcd(final int a, final int b) {
		return b == 0 ? a : gcd(b, a % b);
	}

	/**
	 * Computes the geodesic distance from the nearest seed to every pixel of
	 * the seeds' regions. Pixels outside any region get {@code NaN}; region
	 * pixels not reachable from any seed get positive infinity. Seeds outside
	 * any region are ignored. Afterwards, {@link #pred} holds the predecessor
	 * of each reached pixel on a shortest path (-1 for seeds).
	 */
	void propagate(final int[] seeds, final int seedCount) {
		for (var i = 0; i < size; i++) {
			dist[i] = region[i] < 0 ? Double.NaN : Double.POSITIVE_INFINITY;
			pred[i] = -1;
		}
		final var heap = new Heap(Math.max(16, seedCount));
		for (var s = 0; s < seedCount; s++) {
			final var seed = seeds[s];
			if (region[seed] < 0 || dist[seed] == 0) continue;
			dist[seed] = 0;
			heap.push(0, seed);
		}
		final var pos = new int[n];
		while (!heap.isEmpty()) {
			final var key = heap.minKey();
			final var p = heap.pop();
			if (key > dist[p]) continue;
			var rem = p;
			for (var d = 0; d < n; d++) {
				pos[d] = rem % dims[d];
				rem /= dims[d];
			}
			final var id = region[p];
			for (var k = 0; k < offsets.length; k++) {
				if (!inBounds(pos, offsets[k])) continue;
				final var q = p + deltas[k];
				if (region[q] != id) continue;
				if (via1[k] != via2[k] && region[p + via1[k]] != id &&
					region[p + via2[k]] != id) continue;
				final var dq = key + weights[k];
				if (dq < dist[q]) {
					dist[q] = dq;
					pred[q] = p;
					heap.push(dq, q);
				}
			}
		}
	}

	private boolean inBounds(final int[] pos, final int[] offset) {
		for (var d = 0; d < n; d++) {
			final var c = pos[d] + offset[d];
			if (c < 0 || c >= dims[d]) return false;
		}
		return true;
	}

	/**
	 * Checks whether the pixel lies on the edge of the grid or has a face
	 * neighbor in another region.
	 */
	boolean isBorder(final int p) {
		var rem = p;
		for (var d = 0; d < n; d++) {
			final var c = rem % dims[d];
			rem /= dims[d];
			if (c == 0 || c == dims[d] - 1) return true;
			if (region[p - strides[d]] != region[p] || //
				region[p + strides[d]] != region[p]) return true;
		}
		return false;
	}

	/** Decodes the grid position of a pixel index. */
	long[] position(final int p) {
		final var pos = new long[n];
		var rem = p;
		for (var d = 0; d < n; d++) {
			pos[d] = rem % dims[d];
			rem /= dims[d];
		}
		return pos;
	}

	/**
	 * Returns the reached pixel with the largest finite distance (the first one
	 * on ties), or -1 if no pixel was reached.
	 */
	int farthest() {
		var best = -1;
		var max = Double.NEGATIVE_INFINITY;
		for (var i = 0; i < size; i++) {
			final var d = dist[i];
			if (d > max && d < Double.POSITIVE_INFINITY) {
				max = d;
				best = i;
			}
		}
		return best;
	}

	/** A binary min-heap of pixel indices keyed by distance. */
	private static final class Heap {

		private double[] keys;
		private int[] values;
		private int count;

		Heap(final int capacity) {
			keys = new double[capacity];
			values = new int[capacity];
		}

		boolean isEmpty() {
			return count == 0;
		}

		double minKey() {
			return keys[0];
		}

		void push(final double key, final int value) {
			if (count == keys.length) {
				keys = Arrays.copyOf(keys, 2 * count);
				values = Arrays.copyOf(values, 2 * count);
			}
			var i = count++;
			while (i > 0) {
				final var parent = (i - 1) / 2;
				if (keys[parent] <= key) break;
				keys[i] = keys[parent];
				values[i] = values[parent];
				i = parent;
			}
			keys[i] = key;
			values[i] = value;
		}

		int pop() {
			final var top = values[0];
			final var key = keys[--count];
			final var value = values[count];
			var i = 0;
			while (true) {
				var child = 2 * i + 1;
				if (child >= count) break;
				if (child + 1 < count && keys[child + 1] < keys[child]) child++;
				if (keys[child] >= key) break;
				keys[i] = keys[child];
				values[i] = values[child];
				i = child;
			}
			keys[i] = key;
			values[i] = value;
			return top;
		}
	}
}
