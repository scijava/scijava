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

package org.scijava.ops.image.segment.levelset;

import java.util.function.UnaryOperator;

/**
 * Explicit finite-difference evolution of a level-set function on an
 * n-dimensional grid, for the region-based model of Chan and Vese and the
 * geodesic active contours of Caselles, Kimmel and Sapiro. The level-set
 * function is positive inside the contour.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @see LevelSetFunction
 */
final class LevelSetEvolution {

	/** Interval (in iterations) between reinitializations. */
	static final int REINIT_INTERVAL = 5;

	private final int n;
	private final int size;
	private final long[] dims;
	private final int[] strides;
	private final double[] h;
	private final double hMin;

	// Scratch space for the per-pixel computations.
	private final int[] pos;
	private final int[] lo;
	private final int[] hi;
	private final double[] grad;

	LevelSetEvolution(final long[] dims, final double[] spacing) {
		n = dims.length;
		this.dims = dims;
		strides = new int[n];
		h = spacing.clone();
		var stride = 1L;
		var min = Double.POSITIVE_INFINITY;
		for (var d = 0; d < n; d++) {
			if (!(spacing[d] > 0) || Double.isInfinite(spacing[d])) {
				throw new IllegalArgumentException("Invalid calibration: " + spacing[d]);
			}
			strides[d] = (int) stride;
			stride *= dims[d];
			min = Math.min(min, spacing[d]);
		}
		size = (int) stride;
		hMin = min;
		pos = new int[n];
		lo = new int[n];
		hi = new int[n];
		grad = new double[n];
	}

	// -- Chan-Vese --

	/**
	 * Evolves the level-set function by the gradient flow of the Chan-Vese
	 * energy {@code mu Length + nu Area + sum_inside (u - c1)^2 + sum_outside
	 * (u - c2)^2}.
	 *
	 * @return the number of iterations performed
	 */
	int chanVese(final double[] phi, final double[] u, final double mu,
		final double nu, final int maxIterations, final double tolerance,
		final UnaryOperator<double[]> sdt)
	{
		final var eps = hMin;
		final var deltaMax = 1 / (Math.PI * eps);
		final var dtCurvature = mu > 0 ? 0.9 * hMin * hMin / (2 * n * mu *
			deltaMax) : Double.POSITIVE_INFINITY;
		final var update = new double[size];
		final var check = new Convergence(phi, tolerance);
		var it = 0;
		while (it < maxIterations) {
			double s1 = 0, s2 = 0;
			long n1 = 0;
			for (var i = 0; i < size; i++) {
				if (phi[i] > 0) {
					s1 += u[i];
					n1++;
				}
				else s2 += u[i];
			}
			final var c1 = n1 > 0 ? s1 / n1 : 0;
			final var c2 = n1 < size ? s2 / (size - n1) : 0;
			var maxUpdate = 0.0;
			resetPosition();
			for (var i = 0; i < size; i++) {
				final var p = phi[i];
				final var delta = eps / (Math.PI * (eps * eps + p * p));
				final var k = mu == 0 ? 0 : curvature(phi, i);
				final var a = u[i] - c1;
				final var b = u[i] - c2;
				update[i] = delta * (mu * k - nu - a * a + b * b);
				maxUpdate = Math.max(maxUpdate, Math.abs(update[i]));
				advancePosition();
			}
			// Note: The time step is chosen for the front to move by at most half a
			// pixel, within the stability limit of the curvature term.
			final var dt = maxUpdate > 0 ? Math.min(dtCurvature, 0.5 * hMin /
				maxUpdate) : 0;
			for (var i = 0; i < size; i++)
				phi[i] += dt * update[i];
			it++;
			if (it % REINIT_INTERVAL == 0) {
				reinitialize(phi, sdt);
				if (check.converged(phi)) break;
			}
		}
		return it;
	}

	// -- Geodesic active contours --

	/**
	 * Evolves the level-set function by
	 * {@code phi_t = g (alpha kappa + c) |grad phi| + beta grad g . grad phi},
	 * with upwind differences for the propagation and advection terms.
	 *
	 * @return the number of iterations performed
	 */
	int geodesic(final double[] phi, final double[] g, final double alpha,
		final double c, final double beta, final int maxIterations,
		final double tolerance, final UnaryOperator<double[]> sdt)
	{
		// Precompute the gradient of the edge map.
		final var gradG = new double[n][size];
		var gMax = 0.0;
		final var gradMax = new double[n];
		resetPosition();
		for (var i = 0; i < size; i++) {
			gMax = Math.max(gMax, g[i]);
			for (var d = 0; d < n; d++) {
				final var v = (g[hi[d]] - g[lo[d]]) / ((hi[d] - lo[d]) / strides[d] *
					h[d]);
				gradG[d][i] = Double.isNaN(v) ? 0 : v;
				gradMax[d] = Math.max(gradMax[d], Math.abs(gradG[d][i]));
			}
			advancePosition();
		}
		var cfl = Math.abs(c) * gMax / hMin;
		for (var d = 0; d < n; d++)
			cfl += Math.abs(beta) * gradMax[d] / h[d];
		var dt = cfl > 0 ? 0.5 / cfl : 1;
		if (alpha > 0 && gMax > 0) dt = Math.min(dt, 0.9 * hMin * hMin / (2 * n *
			alpha * gMax));

		final var update = new double[size];
		final var check = new Convergence(phi, tolerance);
		var it = 0;
		while (it < maxIterations) {
			resetPosition();
			for (var i = 0; i < size; i++) {
				final var p = phi[i];
				final var f = c * g[i];
				double up = 0, adv = 0, mean = 0;
				for (var d = 0; d < n; d++) {
					final var dm = lo[d] == i ? 0 : (p - phi[lo[d]]) / h[d];
					final var dp = hi[d] == i ? 0 : (phi[hi[d]] - p) / h[d];
					if (f > 0) {
						final var m = Math.min(dm, 0);
						final var q = Math.max(dp, 0);
						up += m * m + q * q;
					}
					else {
						final var m = Math.max(dm, 0);
						final var q = Math.min(dp, 0);
						up += m * m + q * q;
					}
					final var gd = gradG[d][i];
					adv += gd * (gd < 0 ? dm : dp);
					mean += (dm * dm + dp * dp) / 2;
				}
				var term = f * Math.sqrt(up) + beta * adv;
				if (alpha != 0) {
					term += alpha * g[i] * curvature(phi, i) * Math.sqrt(mean);
				}
				update[i] = term;
				advancePosition();
			}
			for (var i = 0; i < size; i++)
				phi[i] += dt * update[i];
			it++;
			if (it % REINIT_INTERVAL == 0) {
				reinitialize(phi, sdt);
				if (check.converged(phi)) break;
			}
		}
		return it;
	}

	// -- Shared helpers --

	/**
	 * Replaces the level-set function by a signed distance function with the
	 * same zero level set. Pixels next to the interface keep their sub-pixel
	 * position estimate {@code phi / |grad phi|}; all others get the signed
	 * distance of the binary mask {@code phi > 0}.
	 */
	void reinitialize(final double[] phi, final UnaryOperator<double[]> sdt) {
		final var dist = sdt.apply(phi);
		resetPosition();
		for (var i = 0; i < size; i++) {
			final var inside = phi[i] > 0;
			var front = false;
			for (var d = 0; d < n && !front; d++) {
				front = (phi[lo[d]] > 0) != inside || (phi[hi[d]] > 0) != inside;
			}
			if (front) {
				centralGradient(phi, i);
				final var norm = gradientNorm();
				var v = norm > 0 ? phi[i] / norm : dist[i];
				// Keep the sign, and stay within one pixel of the interface.
				if (inside) v = Math.min(Math.max(v, Double.MIN_VALUE), hMin);
				else v = Math.max(Math.min(v, 0), -hMin);
				dist[i] = v;
			}
			advancePosition();
		}
		System.arraycopy(dist, 0, phi, 0, size);
	}

	/**
	 * Mean curvature {@code div(grad phi / |grad phi|)}, as the difference of
	 * the unit normals between the pixel and its neighbors, so that it is also
	 * defined (and large) at isolated extrema of the level-set function.
	 */
	private double curvature(final double[] phi, final int i) {
		var k = 0.0;
		for (var d = 0; d < n; d++) {
			if (hi[d] != i) k += normal(phi, i, i, hi[d], d) / h[d];
			if (lo[d] != i) k -= normal(phi, i, lo[d], i, d) / h[d];
		}
		return k;
	}

	/**
	 * Component along dimension {@code d} of the unit normal halfway between
	 * the adjacent pixels {@code a} and {@code b = a + stride_d}, which share
	 * all other coordinates with pixel {@code i}.
	 */
	private double normal(final double[] phi, final int i, final int a,
		final int b, final int d)
	{
		final var gd = (phi[b] - phi[a]) / h[d];
		var s = gd * gd;
		for (var e = 0; e < n; e++) {
			if (e == d) continue;
			final var oe = hi[e] - i;
			final var me = lo[e] - i;
			final var steps = (oe - me) / strides[e];
			if (steps == 0) continue;
			final var ge = (phi[a + oe] - phi[a + me] + phi[b + oe] - phi[b + me]) /
				(2 * steps * h[e]);
			s += ge * ge;
		}
		return s > 0 ? gd / Math.sqrt(s) : 0;
	}

	private void centralGradient(final double[] phi, final int i) {
		for (var d = 0; d < n; d++) {
			final var steps = (hi[d] - lo[d]) / strides[d];
			grad[d] = steps == 0 ? 0 : (phi[hi[d]] - phi[lo[d]]) / (steps * h[d]);
		}
	}

	/** Norm of the gradient last computed by {@link #centralGradient}. */
	private double gradientNorm() {
		var s = 0.0;
		for (var d = 0; d < n; d++)
			s += grad[d] * grad[d];
		return Math.sqrt(s);
	}

	private int index;

	private void resetPosition() {
		index = 0;
		for (var d = 0; d < n; d++)
			pos[d] = 0;
		neighbors();
	}

	private void advancePosition() {
		index++;
		for (var d = 0; d < n; d++) {
			if (++pos[d] < dims[d]) break;
			pos[d] = 0;
		}
		if (index < size) neighbors();
	}

	/** Neighbor indices along each dimension, clamped at the borders. */
	private void neighbors() {
		for (var d = 0; d < n; d++) {
			lo[d] = pos[d] > 0 ? index - strides[d] : index;
			hi[d] = pos[d] < dims[d] - 1 ? index + strides[d] : index;
		}
	}

	/**
	 * Convergence test: the contour has converged when the number of pixels
	 * that changed sides since the previous test is at most {@code tolerance}
	 * times the number of inside pixels.
	 */
	private static final class Convergence {

		private final boolean[] previous;
		private final double tolerance;

		Convergence(final double[] phi, final double tolerance) {
			previous = new boolean[phi.length];
			for (var i = 0; i < phi.length; i++)
				previous[i] = phi[i] > 0;
			this.tolerance = tolerance;
		}

		boolean converged(final double[] phi) {
			long changed = 0, inside = 0;
			for (var i = 0; i < phi.length; i++) {
				final var in = phi[i] > 0;
				if (in != previous[i]) changed++;
				if (in) inside++;
				previous[i] = in;
			}
			return changed <= tolerance * Math.max(inside, 1);
		}
	}
}
