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

package org.scijava.ops.image.gpl.mcib3d;

/**
 * Least-squares fit of {@code y = a + (b - a) exp(-(x - c)^2 / (2 d^2))} to a
 * profile, as done in the 3D ImageJ Suite with the {@code GAUSSIAN} fit of the
 * ImageJ {@code CurveFitter}. The linear parameters {@code a} and
 * {@code b - a} are obtained by linear regression for each candidate
 * {@code (c, d)}, which is searched by a restarted Nelder-Mead simplex.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
final class ProfileGaussianFit {

	private static final int MAX_ITERATIONS = 10000;
	private static final int MAX_RESTARTS = 20;

	private final double[] x;
	private final double[] y;
	private final double[] g;

	private ProfileGaussianFit(final double[] x, final double[] y) {
		this.x = x;
		this.y = y;
		this.g = new double[x.length];
	}

	/**
	 * Fits a Gaussian to a profile.
	 *
	 * @param x the abscissae
	 * @param y the profile values
	 * @param c0 the initial center
	 * @param d0 the initial standard deviation
	 * @return the fitted {a, b, c, d}, with d &gt;= 0, or {@code null} if the
	 *         fit failed
	 */
	static double[] fit(final double[] x, final double[] y, final double c0,
		final double d0)
	{
		final ProfileGaussianFit f = new ProfileGaussianFit(x, y);
		double[] best = { c0, d0 };
		double bestValue = f.sse(best, null);
		double[] steps = { Math.max(1, Math.abs(d0)), 0.5 * Math.abs(d0) };
		for (int r = 0; r <= MAX_RESTARTS; r++) {
			final double[] p = f.simplex(best, steps);
			final double value = f.sse(p, null);
			if (r > 0 && !(value < bestValue * (1 - 1e-12))) {
				if (value < bestValue) {
					best = p;
					bestValue = value;
				}
				break;
			}
			if (value <= bestValue) {
				best = p;
				bestValue = value;
			}
			final double s = 0.1 * Math.abs(best[1]) + 1e-6;
			steps = new double[] { s, s };
		}
		final double[] ab = new double[2];
		f.sse(best, ab);
		final double[] params = { ab[0], ab[0] + ab[1], best[0], Math.abs(
			best[1]) };
		for (final double p : params) {
			if (!Double.isFinite(p)) return null;
		}
		return params;
	}

	/**
	 * Computes the sum of squared residuals for the given {c, d}, with the
	 * offset and factor obtained by linear regression.
	 */
	private double sse(final double[] cd, final double[] abOut) {
		final int n = x.length;
		final double c = cd[0], d = cd[1];
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
			sort(v, f);
			final double spread = Math.abs(v[2][0] - v[0][0]) + Math.abs(v[2][1] -
				v[0][1]);
			if (Math.abs(f[2] - f[0]) <= 1e-14 * Math.abs(f[0]) + 1e-300 &&
				spread <= 1e-12 * (Math.abs(v[0][0]) + Math.abs(v[0][1]) + 1))
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
		sort(v, f);
		return v[0];
	}

	/** Sorts the simplex vertices by increasing value. */
	private static void sort(final double[][] v, final double[] f) {
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
	}

	/** Returns {@code origin + t * (point - origin)}. */
	private static double[] affine(final double[] origin, final double[] point,
		final double t)
	{
		return new double[] { origin[0] + t * (point[0] - origin[0]), origin[1] +
			t * (point[1] - origin[1]) };
	}
}
