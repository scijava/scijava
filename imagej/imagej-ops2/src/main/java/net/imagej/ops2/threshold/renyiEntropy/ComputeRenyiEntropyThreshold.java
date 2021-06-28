/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops2.threshold.renyiEntropy;

import net.imagej.ops2.threshold.AbstractComputeThresholdHistogram;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;

import org.scijava.Priority;
import org.scijava.ops.Op;
import org.scijava.plugin.Plugin;

// NB - this plugin adapted from Gabriel Landini's code of his AutoThreshold
// plugin found in Fiji (version 1.14).

/**
 * Implements a Renyi entropy based threshold method by Kapur, Sahoo,
 * {@literal &} Wong.
 *
 * @author Barry DeZonia
 * @author Gabriel Landini
 */

@Plugin(type = Op.class, name = "threshold.renyiEntropy",
	priority = Priority.HIGH)
public class ComputeRenyiEntropyThreshold<T extends RealType<T>> extends
	AbstractComputeThresholdHistogram<T>
{

	@Override
	public long computeBin(final Histogram1d<T> hist) {
		final long[] histogram = hist.toLongArray();
		return computeBin(histogram);
	}

	/**
	 * Kapur J.N., Sahoo P.K., and Wong A.K.C. (1985) "A New Method for<br>
	 * Gray-Level Picture Thresholding Using the Entropy of the Histogram"<br>
	 * Graphical Models and Image Processing, 29(3): 273-285<br>
	 * M. Emre Celebi<br>
	 * 06.15.2007<br>
	 * Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8<br>
	 * routines
	 */
	public static long computeBin(final long[] histogram) {
		int threshold;
		int opt_threshold;

		int ih, it;
		int first_bin;
		int last_bin;
		int tmp_var;
		int t_star1, t_star2, t_star3;
		int beta1, beta2, beta3;
		double alpha;/* alpha parameter of the method */
		double term;
		double tot_ent; /* total entropy */
		double max_ent; /* max entropy */
		double ent_back; /* entropy of the background pixels at a given threshold */
		double ent_obj; /* entropy of the object pixels at a given threshold */
		double omega;
		final double[] norm_histo = new double[histogram.length]; /*
																															* normalized
																															* histogram
																															*/
		final double[] P1 = new double[histogram.length]; /*
																											* cumulative normalized
																											* histogram
																											*/
		final double[] P2 = new double[histogram.length];

		int total = 0;
		for (ih = 0; ih < histogram.length; ih++)
			total += histogram[ih];

		for (ih = 0; ih < histogram.length; ih++)
			norm_histo[ih] = (double) histogram[ih] / total;

		P1[0] = norm_histo[0];
		P2[0] = 1.0 - P1[0];
		for (ih = 1; ih < histogram.length; ih++) {
			P1[ih] = P1[ih - 1] + norm_histo[ih];
			P2[ih] = 1.0 - P1[ih];
		}

		/* Determine the first non-zero bin */
		first_bin = 0;
		for (ih = 0; ih < histogram.length; ih++) {
			if (!(Math.abs(P1[ih]) < 2.220446049250313E-16)) {
				first_bin = ih;
				break;
			}
		}

		/* Determine the last non-zero bin */
		last_bin = histogram.length - 1;
		for (ih = histogram.length - 1; ih >= first_bin; ih--) {
			if (!(Math.abs(P2[ih]) < 2.220446049250313E-16)) {
				last_bin = ih;
				break;
			}
		}

		/* Maximum Entropy Thresholding - BEGIN */
		/* ALPHA = 1.0 */
		/*
		 * Calculate the total entropy each gray-level and find the threshold
		 * that maximizes it
		 */
		threshold = 0; // was MIN_INT in original code, but if an empty image is
		// processed it gives an error later on.
		max_ent = 0.0;

		for (it = first_bin; it <= last_bin; it++) {
			/* Entropy of the background pixels */
			ent_back = 0.0;
			for (ih = 0; ih <= it; ih++) {
				if (histogram[ih] != 0) {
					ent_back -= (norm_histo[ih] / P1[it]) * Math.log(norm_histo[ih] /
						P1[it]);
				}
			}

			/* Entropy of the object pixels */
			ent_obj = 0.0;
			for (ih = it + 1; ih < histogram.length; ih++) {
				if (histogram[ih] != 0) {
					ent_obj -= (norm_histo[ih] / P2[it]) * Math.log(norm_histo[ih] /
						P2[it]);
				}
			}

			/* Total entropy */
			tot_ent = ent_back + ent_obj;

			// IJ.log(""+max_ent+" "+tot_ent);

			if (max_ent < tot_ent) {
				max_ent = tot_ent;
				threshold = it;
			}
		}
		t_star2 = threshold;

		/* Maximum Entropy Thresholding - END */
		threshold = 0; // was MIN_INT in original code, but if an empty image is
		// processed it gives an error later on.
		max_ent = 0.0;
		alpha = 0.5;
		term = 1.0 / (1.0 - alpha);
		for (it = first_bin; it <= last_bin; it++) {
			/* Entropy of the background pixels */
			ent_back = 0.0;
			for (ih = 0; ih <= it; ih++)
				ent_back += Math.sqrt(norm_histo[ih] / P1[it]);

			/* Entropy of the object pixels */
			ent_obj = 0.0;
			for (ih = it + 1; ih < histogram.length; ih++)
				ent_obj += Math.sqrt(norm_histo[ih] / P2[it]);

			/* Total entropy */
			tot_ent = term * ((ent_back * ent_obj) > 0.0 ? Math.log(ent_back *
				ent_obj) : 0.0);

			if (tot_ent > max_ent) {
				max_ent = tot_ent;
				threshold = it;
			}
		}

		t_star1 = threshold;

		threshold = 0; // was MIN_INT in original code, but if an empty image is
		// processed it gives an error later on.
		max_ent = 0.0;
		alpha = 2.0;
		term = 1.0 / (1.0 - alpha);
		for (it = first_bin; it <= last_bin; it++) {
			/* Entropy of the background pixels */
			ent_back = 0.0;
			for (ih = 0; ih <= it; ih++)
				ent_back += (norm_histo[ih] * norm_histo[ih]) / (P1[it] * P1[it]);

			/* Entropy of the object pixels */
			ent_obj = 0.0;
			for (ih = it + 1; ih < histogram.length; ih++)
				ent_obj += (norm_histo[ih] * norm_histo[ih]) / (P2[it] * P2[it]);

			/* Total entropy */
			tot_ent = term * ((ent_back * ent_obj) > 0.0 ? Math.log(ent_back *
				ent_obj) : 0.0);

			if (tot_ent > max_ent) {
				max_ent = tot_ent;
				threshold = it;
			}
		}

		t_star3 = threshold;

		/* Sort t_star values */
		if (t_star2 < t_star1) {
			tmp_var = t_star1;
			t_star1 = t_star2;
			t_star2 = tmp_var;
		}
		if (t_star3 < t_star2) {
			tmp_var = t_star2;
			t_star2 = t_star3;
			t_star3 = tmp_var;
		}
		if (t_star2 < t_star1) {
			tmp_var = t_star1;
			t_star1 = t_star2;
			t_star2 = tmp_var;
		}

		/* Adjust beta values */
		if (Math.abs(t_star1 - t_star2) <= 5) {
			if (Math.abs(t_star2 - t_star3) <= 5) {
				beta1 = 1;
				beta2 = 2;
				beta3 = 1;
			}
			else {
				beta1 = 0;
				beta2 = 1;
				beta3 = 3;
			}
		}
		else {
			if (Math.abs(t_star2 - t_star3) <= 5) {
				beta1 = 3;
				beta2 = 1;
				beta3 = 0;
			}
			else {
				beta1 = 1;
				beta2 = 2;
				beta3 = 1;
			}
		}
		// IJ.log(""+t_star1+" "+t_star2+" "+t_star3);
		/* Determine the optimal threshold value */
		omega = P1[t_star3] - P1[t_star1];
		opt_threshold = (int) (t_star1 * (P1[t_star1] + 0.25 * omega * beta1) +
			0.25 * t_star2 * omega * beta2 + t_star3 * (P2[t_star3] + 0.25 * omega *
				beta3));

		return opt_threshold;
	}

}
