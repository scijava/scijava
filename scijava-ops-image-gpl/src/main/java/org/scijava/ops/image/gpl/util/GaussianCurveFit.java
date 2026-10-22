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

package org.scijava.ops.image.gpl.util;

/**
 * Least-squares fit of {@code y = a + (b - a) exp(-(x - c)^2 / (2 d^2))}, the
 * {@code GAUSSIAN} fit of the ImageJ {@code CurveFitter}: for each candidate
 * {@code (c, d)}, the linear parameters {@code a} and {@code b - a} are
 * obtained by linear regression, and {@code (c, d)} is searched by a
 * Nelder-Mead simplex. The callers choose the iteration limit, the convergence
 * test and the restart policy, to match the plugin they port.
 * <p>
 * This is an internal helper of the ops in this module, not a public API.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class GaussianCurveFit {

	/** Convergence test of the simplex search. */
	@FunctionalInterface
	public interface Convergence {

		/**
		 * @param best the lowest sum of squares of the simplex vertices
		 * @param worst the highest sum of squares of the simplex vertices
		 * @param spread the L1 distance between the best and worst vertices
		 * @param scale the L1 norm of the best vertex
		 * @return whether the search has converged
		 */
		boolean test(double best, double worst, double spread, double scale);
	}

	private final double[] x;
	private final double[] y;
	private final double[] g;

	/**
	 * @param x the abscissae
	 * @param y the values to fit
	 */
	public GaussianCurveFit(final double[] x, final double[] y) {
		this.x = x;
		this.y = y;
		this.g = new double[x.length];
	}

	/**
	 * Computes the sum of squared residuals for the given {c, d}, with the offset
	 * and factor obtained by linear regression.
	 *
	 * @param cd the center and standard deviation
	 * @param abOut if not null, receives the offset {@code a} and the factor
	 *          {@code b - a}
	 * @return the sum of squared residuals, or infinity if it is NaN
	 */
	public double sse(final double[] cd, final double[] abOut) {
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

	/**
	 * Minimizes {@link #sse} over {c, d} by a Nelder-Mead simplex search.
	 *
	 * @param start the initial {c, d}
	 * @param steps the initial simplex steps along c and d
	 * @param maxIterations the maximum number of iterations
	 * @param converged the convergence test, applied before each iteration
	 * @return the best vertex found
	 */
	public double[] simplex(final double[] start, final double[] steps,
		final int maxIterations, final Convergence converged)
	{
		final double[][] v = { start.clone(), { start[0] + steps[0], start[1] }, {
			start[0], start[1] + steps[1] } };
		final double[] f = new double[3];
		for (int i = 0; i < 3; i++)
			f[i] = sse(v[i], null);
		for (int iter = 0; iter < maxIterations; iter++) {
			sort(v, f);
			final double spread = Math.abs(v[2][0] - v[0][0]) + Math.abs(v[2][1] -
				v[0][1]);
			if (converged.test(f[0], f[2], spread, Math.abs(v[0][0]) + Math.abs(
				v[0][1])))
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
		// Note: The sort keeps the first vertex among equal values.
		sort(v, f);
		return v[0];
	}

	/**
	 * Returns the fitted parameters {a, b, c, d}, with d &gt;= 0, for the given
	 * {c, d}.
	 */
	public double[] params(final double[] cd) {
		final double[] ab = new double[2];
		sse(cd, ab);
		return new double[] { ab[0], ab[0] + ab[1], cd[0], Math.abs(cd[1]) };
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
