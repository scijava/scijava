/*-
 * #%L
 * GPL-licensed image processing operations for SciJava Ops, translated from copyleft ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.gpl.directionality;

/**
 * Least-squares fit of {@code y = a + (b - a) exp(-(x - c)^2 / (2 d^2))}, as
 * done by the {@code GAUSSIAN} fit of the ImageJ {@code CurveFitter}: the
 * linear parameters {@code a} and {@code b - a} are obtained by regression,
 * and the nonlinear ones {@code c} and {@code d} by a Nelder-Mead simplex
 * search started from the same initial values.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
final class GaussianFit {

	private static final int MAX_ITERATIONS = 4000;
	private static final int RESTARTS = 2;
	private static final double TOLERANCE = 1e-12;

	private final double[] x;
	private final double[] y;

	/** The fitted parameters {a, b, c, d}, with d >= 0. */
	final double[] params = new double[4];

	/** The goodness of fit, as reported by CurveFitter. */
	final double goodness;

	GaussianFit(final double[] x, final double[] y) {
		this.x = x;
		this.y = y;
		final int n = x.length;
		double xMin = Double.POSITIVE_INFINITY, xMax = Double.NEGATIVE_INFINITY;
		double yMin = Double.POSITIVE_INFINITY, yMax = Double.NEGATIVE_INFINITY;
		double xOfMax = x[0], yMean = 0;
		for (int i = 0; i < n; i++) {
			xMin = Math.min(xMin, x[i]);
			xMax = Math.max(xMax, x[i]);
			if (y[i] > yMax) {
				yMax = y[i];
				xOfMax = x[i];
			}
			yMin = Math.min(yMin, y[i]);
			yMean += y[i];
		}
		yMean /= n;
		final double d0 = 0.39894 * (xMax - xMin) * (yMean - yMin) / (yMax - yMin +
			1e-100);

		double[] best = simplex(new double[] { xOfMax, d0 }, new double[] { 0.2 *
			d0, 0.1 * d0 });
		double bestValue = sse(best, null);
		for (int r = 0; r < RESTARTS; r++) {
			final double step = 0.1 * Math.abs(best[1]) + 1e-10;
			final double[] p = simplex(best, new double[] { step, step });
			final double value = sse(p, null);
			if (!(value < bestValue)) break;
			best = p;
			bestValue = value;
		}
		final double[] ab = new double[2];
		final double sse = sse(best, ab);
		params[0] = ab[0];
		params[1] = ab[0] + ab[1];
		params[2] = best[0];
		params[3] = Math.abs(best[1]);

		double sumY = 0, sumY2 = 0;
		for (final double v : y) {
			sumY += v;
			sumY2 += v * v;
		}
		final double ssd = sumY2 - sumY * sumY / n;
		final int dof = n - 4;
		goodness = ssd > 0 && dof > 0 ? 1 - sse / ssd * n / dof : 0;
	}

	/**
	 * Computes the sum of squared residuals for the given {c, d}, with the
	 * offset and factor obtained by linear regression.
	 */
	private double sse(final double[] cd, final double[] abOut) {
		final int n = x.length;
		final double c = cd[0], d = cd[1];
		final double[] g = new double[n];
		double gMean = 0, yMean = 0;
		for (int i = 0; i < n; i++) {
			g[i] = Math.exp(-(x[i] - c) * (x[i] - c) / (2 * d * d));
			gMean += g[i];
			yMean += y[i];
		}
		gMean /= n;
		yMean /= n;
		double sgg = 0, sgy = 0;
		for (int i = 0; i < n; i++) {
			sgg += (g[i] - gMean) * (g[i] - gMean);
			sgy += (g[i] - gMean) * (y[i] - yMean);
		}
		final double factor = sgg > 0 ? sgy / sgg : 0;
		final double offset = yMean - factor * gMean;
		double sum = 0;
		for (int i = 0; i < n; i++) {
			final double r = y[i] - offset - factor * g[i];
			sum += r * r;
		}
		if (abOut != null) {
			abOut[0] = offset;
			abOut[1] = factor;
		}
		return Double.isNaN(sum) ? Double.POSITIVE_INFINITY : sum;
	}

	/** Nelder-Mead minimization of {@link #sse} over {c, d}. */
	private double[] simplex(final double[] start, final double[] steps) {
		final double[][] v = { start.clone(), { start[0] + steps[0], start[1] }, {
			start[0], start[1] + steps[1] } };
		final double[] f = new double[3];
		for (int i = 0; i < 3; i++)
			f[i] = sse(v[i], null);
		for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
			// Sort the vertices by increasing value.
			for (int i = 0; i < 2; i++) {
				for (int j = i + 1; j < 3; j++) {
					if (f[j] < f[i]) {
						final double tf = f[i];
						f[i] = f[j];
						f[j] = tf;
						final double[] tv = v[i];
						v[i] = v[j];
						v[j] = tv;
					}
				}
			}
			if (Math.abs(f[2] - f[0]) <= TOLERANCE * (Math.abs(f[0]) + Math.abs(
				f[2])) + 1e-300 && Math.abs(v[2][0] - v[0][0]) + Math.abs(v[2][1] -
					v[0][1]) <= 1e-12 * (Math.abs(v[0][0]) + Math.abs(v[0][1])) + 1e-300)
			{
				break;
			}
			final double[] centroid = { (v[0][0] + v[1][0]) / 2, (v[0][1] + v[1][1]) /
				2 };
			final double[] reflected = affine(centroid, v[2], -1);
			final double fr = sse(reflected, null);
			if (fr < f[0]) {
				final double[] expanded = affine(centroid, v[2], -2);
				final double fe = sse(expanded, null);
				if (fe < fr) {
					v[2] = expanded;
					f[2] = fe;
				}
				else {
					v[2] = reflected;
					f[2] = fr;
				}
			}
			else if (fr < f[1]) {
				v[2] = reflected;
				f[2] = fr;
			}
			else {
				final boolean outside = fr < f[2];
				final double[] contracted = affine(centroid, outside ? reflected : v[2],
					0.5);
				final double fc = sse(contracted, null);
				if (fc < (outside ? fr : f[2])) {
					v[2] = contracted;
					f[2] = fc;
				}
				else {
					for (int i = 1; i < 3; i++) {
						v[i] = affine(v[0], v[i], 0.5);
						f[i] = sse(v[i], null);
					}
				}
			}
		}
		int best = 0;
		for (int i = 1; i < 3; i++)
			if (f[i] < f[best]) best = i;
		return v[best];
	}

	/** Returns {@code origin + t * (point - origin)}. */
	private static double[] affine(final double[] origin, final double[] point,
		final double t)
	{
		return new double[] { origin[0] + t * (point[0] - origin[0]), origin[1] +
			t * (point[1] - origin[1]) };
	}
}
