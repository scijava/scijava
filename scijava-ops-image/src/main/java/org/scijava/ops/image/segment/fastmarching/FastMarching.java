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

package org.scijava.ops.image.segment.fastmarching;

import java.util.Arrays;

/**
 * Fast marching solver of the Eikonal equation {@code |grad T| F = 1} on an
 * n-dimensional regular grid, after J. A. Sethian ("A fast marching level set
 * method for monotonically advancing fronts", <i>PNAS</i> 93(4):
 * 1591&ndash;1595, 1996).
 * <p>
 * Pixels are accepted in order of increasing arrival time, using a binary
 * heap. The arrival time of a pixel is obtained from its accepted neighbors by
 * solving the upwind discretization of the Eikonal equation, either with
 * first-order one-sided differences or, where two accepted pixels are
 * available in a row, with second-order ones (J. A. Sethian, <i>Level Set
 * Methods and Fast Marching Methods</i>, 2nd ed., Cambridge University Press,
 * 1999, section 8.6). Ties in the heap are broken by the flat pixel index, so
 * the result is deterministic.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class FastMarching {

	private static final byte FAR = 0;
	private static final byte TRIAL = 1;
	private static final byte KNOWN = 2;

	private final long[] dims;
	private final int[] strides;
	private final double[] invH2;
	private final double[] speed;
	private final boolean secondOrder;

	private final double[] times;
	private final byte[] state;

	// Binary min-heap of (time, index) entries, with lazy deletion.
	private double[] heapKey = new double[64];
	private int[] heapIndex = new int[64];
	private int heapSize;

	// Scratch space for the local updates.
	private final double[] coefs;
	private final double[] centers;
	private final int[] coord;
	private final int[] pos;

	/**
	 * Creates a solver.
	 *
	 * @param dims the grid dimensions
	 * @param spacing the grid spacing along each dimension
	 * @param speed the propagation speed of each pixel, in flat order (first
	 *          dimension fastest); pixels whose speed is not positive (or
	 *          {@code NaN}) are never reached, unless they are seeds
	 * @param secondOrder whether to use second-order upwind differences where
	 *          possible
	 */
	public FastMarching(final long[] dims, final double[] spacing,
		final double[] speed, final boolean secondOrder)
	{
		final var n = dims.length;
		if (spacing.length != n) {
			throw new IllegalArgumentException("Expected " + n +
				" spacing values but got " + spacing.length);
		}
		this.dims = dims;
		this.speed = speed;
		this.secondOrder = secondOrder;
		strides = new int[n];
		invH2 = new double[n];
		var stride = 1L;
		for (var d = 0; d < n; d++) {
			if (!(spacing[d] > 0) || Double.isInfinite(spacing[d])) {
				throw new IllegalArgumentException("Invalid spacing: " + Arrays
					.toString(spacing));
			}
			strides[d] = (int) stride;
			invH2[d] = 1 / (spacing[d] * spacing[d]);
			stride *= dims[d];
		}
		if (stride != speed.length) {
			throw new IllegalArgumentException("Speed array has " + speed.length +
				" elements but the grid has " + stride);
		}
		times = new double[speed.length];
		Arrays.fill(times, Double.POSITIVE_INFINITY);
		state = new byte[speed.length];
		coefs = new double[n];
		centers = new double[n];
		coord = new int[n];
		pos = new int[n];
	}

	/**
	 * Propagates the front from the given seed pixels, which get arrival time
	 * 0, until all reachable pixels are accepted or the arrival time exceeds
	 * {@code stopTime}.
	 *
	 * @param seeds the flat indices of the seed pixels
	 * @param stopTime the time after which propagation stops; pixels not
	 *          accepted by then keep an infinite arrival time
	 * @return the arrival times, in flat order
	 */
	public double[] propagate(final int[] seeds, final double stopTime) {
		for (final var s : seeds) {
			times[s] = 0;
			state[s] = KNOWN;
		}
		for (final var s : seeds)
			updateNeighbors(s);
		while (heapSize > 0) {
			final var t = heapKey[0];
			final var i = heapIndex[0];
			pop();
			if (state[i] == KNOWN || t != times[i]) continue;
			if (t > stopTime) break;
			state[i] = KNOWN;
			updateNeighbors(i);
		}
		for (var i = 0; i < times.length; i++) {
			if (state[i] != KNOWN) times[i] = Double.POSITIVE_INFINITY;
		}
		return times;
	}

	private void updateNeighbors(final int index) {
		position(index, coord);
		for (var d = 0; d < dims.length; d++) {
			if (coord[d] > 0) update(index - strides[d]);
			if (coord[d] < dims[d] - 1) update(index + strides[d]);
		}
	}

	private void position(final int index, final int[] p) {
		var rem = index;
		for (var d = dims.length - 1; d >= 0; d--) {
			p[d] = rem / strides[d];
			rem -= p[d] * strides[d];
		}
	}

	private void update(final int index) {
		if (state[index] == KNOWN) return;
		final var f = speed[index];
		if (!(f > 0)) return;
		final var t = solve(index, 1 / (f * f));
		if (t < times[index]) {
			times[index] = t;
			if (state[index] == FAR) state[index] = TRIAL;
			push(t, index);
		}
	}

	/**
	 * Computes the upwind arrival time of a pixel from its accepted neighbors.
	 */
	private double solve(final int index, final double rhs) {
		position(index, pos);
		var m = 0;
		var anySecond = false;
		for (var d = 0; d < dims.length; d++) {
			final var s = strides[d];
			var best = Double.POSITIVE_INFINITY;
			var side = 0;
			if (pos[d] > 0 && state[index - s] == KNOWN) {
				best = times[index - s];
				side = -1;
			}
			if (pos[d] < dims[d] - 1 && state[index + s] == KNOWN && times[index +
				s] < best)
			{
				best = times[index + s];
				side = 1;
			}
			if (side == 0) continue;
			var c = invH2[d];
			var b = best;
			if (secondOrder) {
				final var p2 = pos[d] + 2 * side;
				if (p2 >= 0 && p2 < dims[d]) {
					final var j = index + 2 * side * s;
					if (state[j] == KNOWN && times[j] <= best) {
						// One-sided difference (3 T - 4 T1 + T2) / (2 h).
						c = 2.25 * invH2[d];
						b = (4 * best - times[j]) / 3;
						anySecond = true;
					}
				}
			}
			coefs[m] = c;
			centers[m] = b;
			m++;
		}
		final var t = solveSorted(m, rhs);
		if (!Double.isNaN(t) || !anySecond) return t;
		// Note: With second-order terms, the quadratic can lack a real root;
		// the first-order scheme always has one, so it is used instead.
		return firstOrder(index, rhs);
	}

	private double firstOrder(final int index, final double rhs) {
		var m = 0;
		for (var d = 0; d < dims.length; d++) {
			final var s = strides[d];
			var best = Double.POSITIVE_INFINITY;
			if (pos[d] > 0 && state[index - s] == KNOWN) best = times[index - s];
			if (pos[d] < dims[d] - 1 && state[index + s] == KNOWN) best = Math.min(
				best, times[index + s]);
			if (best == Double.POSITIVE_INFINITY) continue;
			coefs[m] = invH2[d];
			centers[m] = best;
			m++;
		}
		return solveSorted(m, rhs);
	}

	/**
	 * Solves {@code sum_k c_k (T - b_k)^2 = rhs} for its largest root, adding
	 * the terms in increasing order of {@code b_k} for as long as they lie
	 * upwind of the solution.
	 */
	private double solveSorted(final int m, final double rhs) {
		for (var i = 1; i < m; i++) {
			final var b = centers[i];
			final var c = coefs[i];
			var j = i - 1;
			while (j >= 0 && centers[j] > b) {
				centers[j + 1] = centers[j];
				coefs[j + 1] = coefs[j];
				j--;
			}
			centers[j + 1] = b;
			coefs[j + 1] = c;
		}
		double a = 0, bSum = 0, cSum = 0;
		var t = Double.NaN;
		for (var k = 0; k < m; k++) {
			a += coefs[k];
			bSum += coefs[k] * centers[k];
			cSum += coefs[k] * centers[k] * centers[k];
			final var disc = bSum * bSum - a * (cSum - rhs);
			if (disc < 0) return Double.NaN;
			t = (bSum + Math.sqrt(disc)) / a;
			if (k + 1 < m && t <= centers[k + 1]) break;
		}
		return t;
	}

	// -- Heap --

	private void push(final double key, final int index) {
		if (heapSize == heapKey.length) {
			heapKey = Arrays.copyOf(heapKey, 2 * heapSize);
			heapIndex = Arrays.copyOf(heapIndex, 2 * heapSize);
		}
		var i = heapSize++;
		while (i > 0) {
			final var p = (i - 1) / 2;
			if (!less(key, index, heapKey[p], heapIndex[p])) break;
			heapKey[i] = heapKey[p];
			heapIndex[i] = heapIndex[p];
			i = p;
		}
		heapKey[i] = key;
		heapIndex[i] = index;
	}

	private void pop() {
		heapSize--;
		if (heapSize == 0) return;
		final var key = heapKey[heapSize];
		final var index = heapIndex[heapSize];
		var i = 0;
		while (true) {
			var child = 2 * i + 1;
			if (child >= heapSize) break;
			if (child + 1 < heapSize && less(heapKey[child + 1], heapIndex[child +
				1], heapKey[child], heapIndex[child])) child++;
			if (!less(heapKey[child], heapIndex[child], key, index)) break;
			heapKey[i] = heapKey[child];
			heapIndex[i] = heapIndex[child];
			i = child;
		}
		heapKey[i] = key;
		heapIndex[i] = index;
	}

	private static boolean less(final double k1, final int i1, final double k2,
		final int i2)
	{
		return k1 < k2 || (k1 == k2 && i1 < i2);
	}
}
