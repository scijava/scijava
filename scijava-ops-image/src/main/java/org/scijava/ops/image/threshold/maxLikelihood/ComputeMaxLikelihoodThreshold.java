/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.threshold.maxLikelihood;

import org.scijava.ops.image.threshold.AbstractComputeThresholdHistogram;
import org.scijava.ops.image.threshold.Thresholds;
import org.scijava.ops.image.threshold.minimum.ComputeMinimumThreshold;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;

// This plugin code ported from the original MatLab code of the max likelihood
// threshold method (th_maxlik) as written in Antti Niemisto's 1.03 version of
// the HistThresh Toolbox (relicensed BSD 2-12-13)

/**
 * Implements a maximum likelihood threshold method by Dempster, Laird,
 * {@literal &} Rubin and Glasbey.
 *
 * @author Barry DeZonia
 * @implNote op names='threshold.maxLikelihood'
 */
public class ComputeMaxLikelihoodThreshold<T extends RealType<T>> extends
	AbstractComputeThresholdHistogram<T>
{

	private static final int MAX_ATTEMPTS = 10000;

	/**
	 * TODO
	 *
	 * @param hist the {@link Histogram1d}
	 * @return the Max Likelihood threshold value
	 */
	@Override
	public long computeBin(final Histogram1d<T> hist) {
		final long[] histogram = hist.toLongArray();
		return computeBin(histogram);
	}

	/**
	 * T = th_maxlik(I,n)
	 * <P>
	 * Find a global threshold for a grayscale image using the maximum<br>
	 * likelihood via expectation maximization method.
	 * <P>
	 * In: I grayscale image n maximum graylevel (defaults to 255)
	 * <P>
	 * Out: T threshold
	 * <P>
	 * References:
	 * <P>
	 * A. P. Dempster, N. M. Laird, and D. B. Rubin, "Maximum likelihood<br>
	 * from incomplete data via the EM algorithm," Journal of the Royal<br>
	 * Statistical Society, Series B, vol. 39, pp. 1-38, 1977.
	 * <P>
	 * C. A. Glasbey,<br>
	 * "An analysis of histogram-based thresholding algorithms," CVGIP:<br>
	 * Graphical Models and Image Processing, vol. 55, pp. 532-537, 1993.
	 * <P>
	 * Copyright (C) 2004-2013 Antti Niemistˆ See README for more copyright<br>
	 * information.
	 */
	public static long computeBin(final long[] histogram) {
		final int n = histogram.length - 1;

		// I == the Image as double data

		// % Calculate the histogram.
		// y = hist(I(:),0:n);
		final long[] y = histogram;

		// % The initial estimate for the threshold is found with the MINIMUM
		// % algorithm.
		final int T = (int) ComputeMinimumThreshold.computeBin(histogram);

		// NB: T might be -1 if ComputeMinimumThreshold doesn't converge
		if (T < 0) {
			return 0;
		}

		final double eps = 0.0000001;

		// % Calculate initial values for the statistics.
		double mu = Thresholds.B(y, T) / Thresholds.A(y, T);
		double nu = (Thresholds.B(y, n) - Thresholds.B(y, T)) / (Thresholds.A(y,
			n) - Thresholds.A(y, T));
		double p = Thresholds.A(y, T) / Thresholds.A(y, n);
		double q = (Thresholds.A(y, n) - Thresholds.A(y, T)) / Thresholds.A(y, n);
		double sigma2 = Thresholds.C(y, T) / Thresholds.A(y, T) - (mu * mu);
		double tau2 = (Thresholds.C(y, n) - Thresholds.C(y, T)) / (Thresholds.A(y,
			n) - Thresholds.A(y, T)) - (nu * nu);

		// % Return if sigma2 or tau2 are zero, to avoid division by zero
		if (sigma2 == 0 || tau2 == 0) return 0;

		double mu_prev = Double.NaN;
		double nu_prev = Double.NaN;
		double p_prev = Double.NaN;
		double q_prev = Double.NaN;
		double sigma2_prev = Double.NaN;
		double tau2_prev = Double.NaN;

		final double[] ind = indices(n + 1);
		final double[] ind2 = new double[n + 1];
		final double[] phi = new double[n + 1];
		final double[] gamma = new double[n + 1];
		final double[] tmp1 = new double[n + 1];
		final double[] tmp2 = new double[n + 1];
		final double[] tmp3 = new double[n + 1];
		final double[] tmp4 = new double[n + 1];

		sqr(ind, ind2);

		int attempts = 0;
		while (true) {
			if (attempts++ > MAX_ATTEMPTS) {
				throw new IllegalStateException(
					"Max likelihood method not converging after " + MAX_ATTEMPTS +
						" attempts.");
			}
			for (int i = 0; i <= n; i++) {
				final double dmu2 = (i - mu) * (i - mu);
				final double dnu2 = (i - nu) * (i - nu);
				phi[i] = p / Math.sqrt(sigma2) * Math.exp(-dmu2 / (2 * sigma2)) / (p /
					Math.sqrt(sigma2) * Math.exp(-dmu2 / (2 * sigma2)) + (q / Math.sqrt(
						tau2)) * Math.exp(-dnu2 / (2 * tau2)));
			}

			minus(1, phi, gamma);
			final double F = mul(phi, y);
			final double G = mul(gamma, y);
			p_prev = p;
			q_prev = q;
			mu_prev = mu;
			nu_prev = nu;
			sigma2_prev = nu;
			tau2_prev = nu;
			final double Ayn = Thresholds.A(y, n);
			p = F / Ayn;
			q = G / Ayn;
			scale(ind, phi, tmp1);
			mu = mul(tmp1, y) / F;
			scale(ind, gamma, tmp2);
			nu = mul(tmp2, y) / G;
			scale(ind2, phi, tmp3);
			sigma2 = mul(tmp3, y) / F - (mu * mu);
			scale(ind2, gamma, tmp4);
			tau2 = mul(tmp4, y) / G - (nu * nu);

			if (Math.abs(mu - mu_prev) < eps) break;
			if (Math.abs(nu - nu_prev) < eps) break;
			if (Math.abs(p - p_prev) < eps) break;
			if (Math.abs(q - q_prev) < eps) break;
			if (Math.abs(sigma2 - sigma2_prev) < eps) break;
			if (Math.abs(tau2 - tau2_prev) < eps) break;
		}

		// % The terms of the quadratic equation to be solved.
		final double w0 = 1 / sigma2 - 1 / tau2;
		final double w1 = mu / sigma2 - nu / tau2;
		final double w2 = (mu * mu) / sigma2 - (nu * nu) / tau2 + Math.log10(
			(sigma2 * (q * q)) / (tau2 * (p * p)));

		// % If the threshold would be imaginary, return with threshold set to
		// zero.
		final double sqterm = w1 * w1 - w0 * w2;
		if (sqterm < 0) {
			throw new IllegalStateException(
				"Max likelihood threshold would be imaginary");
		}

		// % The threshold is the integer part of the solution of the quadratic
		// % equation.
		return (int) Math.floor((w1 + Math.sqrt(sqterm)) / w0);
	}

	// does a single row*col multiplcation of a matrix multiply

	private static double mul(final double[] row, final long col[]) {
		if (row.length != col.length) throw new IllegalArgumentException(
			"row/col lengths differ");
		double sum = 0;
		for (int i = 0; i < row.length; i++) {
			sum += row[i] * col[i];
		}
		return sum;
	}

	private static void scale(final double[] list1, final double[] list2,
		final double[] output)
	{
		if ((list1.length != list2.length) || (list1.length != output.length)) {
			throw new IllegalArgumentException("list lengths differ");
		}
		for (int i = 0; i < list1.length; i++)
			output[i] = list1[i] * list2[i];
	}

	private static void sqr(final double[] in, final double[] out) {
		if (in.length != out.length) {
			throw new IllegalArgumentException("list lengths differ");
		}
		for (int i = 0; i < in.length; i++)
			out[i] = in[i] * in[i];
	}

	private static double[] indices(final int n) {
		final double[] indices = new double[n];
		for (int i = 0; i < n; i++)
			indices[i] = i;
		return indices;
	}

	private static void minus(final long num, final double[] phi,
		final double[] gamma)
	{
		if (phi.length != gamma.length) {
			throw new IllegalArgumentException("list lengths differ");
		}
		for (int i = 0; i < phi.length; i++) {
			gamma[i] = num - phi[i];
		}
	}

}
