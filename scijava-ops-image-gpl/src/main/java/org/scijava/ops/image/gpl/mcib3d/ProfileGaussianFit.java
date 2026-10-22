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

import org.scijava.ops.image.gpl.util.GaussianCurveFit;

/**
 * Gaussian fit of a radial profile, as done in the 3D ImageJ Suite with the
 * {@code GAUSSIAN} fit of the ImageJ {@code CurveFitter}: a
 * {@link GaussianCurveFit} restarted until it stops improving.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
final class ProfileGaussianFit {

	private static final int MAX_ITERATIONS = 10000;
	private static final int MAX_RESTARTS = 20;

	private static final GaussianCurveFit.Convergence CONVERGED = (best, worst,
		spread, scale) -> Math.abs(worst - best) <= 1e-14 * Math.abs(best) +
			1e-300 && spread <= 1e-12 * (scale + 1);

	private ProfileGaussianFit() {
		// Prevent instantiation of utility class.
	}

	/**
	 * Fits a Gaussian to a profile.
	 *
	 * @param x the abscissae
	 * @param y the profile values
	 * @param c0 the initial center
	 * @param d0 the initial standard deviation
	 * @return the fitted {a, b, c, d}, with d &gt;= 0, or {@code null} if the fit
	 *         failed
	 */
	static double[] fit(final double[] x, final double[] y, final double c0,
		final double d0)
	{
		final GaussianCurveFit f = new GaussianCurveFit(x, y);
		double[] best = { c0, d0 };
		double bestValue = f.sse(best, null);
		double[] steps = { Math.max(1, Math.abs(d0)), 0.5 * Math.abs(d0) };
		for (int r = 0; r <= MAX_RESTARTS; r++) {
			final double[] p = f.simplex(best, steps, MAX_ITERATIONS, CONVERGED);
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
		final double[] params = f.params(best);
		for (final double p : params) {
			if (!Double.isFinite(p)) return null;
		}
		return params;
	}
}
