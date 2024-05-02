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

package org.scijava.ops.image.threshold.yen;

import org.scijava.ops.image.threshold.AbstractComputeThresholdHistogram;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;

// NB - this plugin adapted from Gabriel Landini's code of his AutoThreshold
// plugin found in Fiji (version 1.14).

/**
 * Implements Yen's threshold method (Yen, Chang, {@literal &} Chang, and Sezgin
 * {@literal &} Sankur).
 *
 * @author Barry DeZonia
 * @author Gabriel Landini
 * @implNote op names='threshold.yen', priority='100.'
 */
public class ComputeYenThreshold<T extends RealType<T>> extends
	AbstractComputeThresholdHistogram<T>
{

	/**
	 * TODO
	 *
	 * @param hist the {@link Histogram1d}
	 * @return the Yen threshold value
	 */
	@Override
	public long computeBin(final Histogram1d<T> hist) {
		final long[] histogram = hist.toLongArray();
		return computeBin(histogram);
	}

	/**
	 * Implements Yen thresholding method<br>
	 * 1) Yen J.C., Chang F.J., and Chang S. (1995) "A New Criterion<br>
	 * for Automatic Multilevel Thresholding" IEEE Trans. on Image<br>
	 * Processing, 4(3): 370-378<br>
	 * 2) Sezgin M. and Sankur B. (2004) "Survey over Image Thresholding<br>
	 * Techniques and Quantitative Performance Evaluation" Journal of<br>
	 * Electronic Imaging, 13(1): 146-165<br>
	 * http://citeseer.ist.psu.edu/sezgin04survey.html
	 * <P>
	 * M. Emre Celebi<br>
	 * 06.15.2007<br>
	 * Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8<br>
	 * routines
	 */
	public static long computeBin(final long[] histogram) {
		int threshold;
		int ih, it;
		double crit;
		double max_crit;
		final double[] norm_histo = new double[histogram.length]; /*
																															* normalized
																															* histogram
																															*/
		final double[] P1 = new double[histogram.length]; /*
																											* cumulative normalized
																											* histogram
																											*/
		final double[] P1_sq = new double[histogram.length];
		final double[] P2_sq = new double[histogram.length];

		long total = 0;
		for (ih = 0; ih < histogram.length; ih++)
			total += histogram[ih];

		for (ih = 0; ih < histogram.length; ih++)
			norm_histo[ih] = (double) histogram[ih] / total;

		P1[0] = norm_histo[0];
		for (ih = 1; ih < histogram.length; ih++)
			P1[ih] = P1[ih - 1] + norm_histo[ih];

		P1_sq[0] = norm_histo[0] * norm_histo[0];
		for (ih = 1; ih < histogram.length; ih++)
			P1_sq[ih] = P1_sq[ih - 1] + norm_histo[ih] * norm_histo[ih];

		P2_sq[histogram.length - 1] = 0.0;
		for (ih = histogram.length - 2; ih >= 0; ih--)
			P2_sq[ih] = P2_sq[ih + 1] + norm_histo[ih + 1] * norm_histo[ih + 1];

		/* Find the threshold that maximizes the criterion */
		threshold = -1;
		max_crit = Double.NEGATIVE_INFINITY;
		for (it = 0; it < histogram.length; it++) {
			crit = -1.0 * ((P1_sq[it] * P2_sq[it]) > 0.0 ? Math.log(P1_sq[it] *
				P2_sq[it]) : 0.0) + 2 * ((P1[it] * (1.0 - P1[it])) > 0.0 ? Math.log(
					P1[it] * (1.0 - P1[it])) : 0.0);
			if (crit > max_crit) {
				max_crit = crit;
				threshold = it;
			}
		}
		return threshold;
	}

}
