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

import org.scijava.ops.image.gpl.util.GaussianCurveFit;

/**
 * Gaussian fit of a directionality histogram, as done by the {@code GAUSSIAN}
 * fit of the ImageJ {@code CurveFitter}: a {@link GaussianCurveFit} started
 * from the same initial values, restarted at most twice.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
final class GaussianFit {

	private static final int MAX_ITERATIONS = 4000;
	private static final int RESTARTS = 2;
	private static final double TOLERANCE = 1e-12;

	private static final GaussianCurveFit.Convergence CONVERGED = (best, worst,
		spread, scale) -> Math.abs(worst - best) <= TOLERANCE * (Math.abs(best) +
			Math.abs(worst)) + 1e-300 && spread <= 1e-12 * scale + 1e-300;

	/** The fitted parameters {a, b, c, d}, with d >= 0. */
	final double[] params;

	/** The goodness of fit, as reported by CurveFitter. */
	final double goodness;

	GaussianFit(final double[] x, final double[] y) {
		final GaussianCurveFit fit = new GaussianCurveFit(x, y);
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

		double[] best = fit.simplex(new double[] { xOfMax, d0 }, new double[] {
			0.2 * d0, 0.1 * d0 }, MAX_ITERATIONS, CONVERGED);
		double bestValue = fit.sse(best, null);
		for (int r = 0; r < RESTARTS; r++) {
			final double step = 0.1 * Math.abs(best[1]) + 1e-10;
			final double[] p = fit.simplex(best, new double[] { step, step },
				MAX_ITERATIONS, CONVERGED);
			final double value = fit.sse(p, null);
			if (!(value < bestValue)) break;
			best = p;
			bestValue = value;
		}
		params = fit.params(best);
		final double sse = fit.sse(best, null);

		double sumY = 0, sumY2 = 0;
		for (final double v : y) {
			sumY += v;
			sumY2 += v * v;
		}
		final double ssd = sumY2 - sumY * sumY / n;
		final int dof = n - 4;
		goodness = ssd > 0 && dof > 0 ? 1 - sse / ssd * n / dof : 0;
	}
}
