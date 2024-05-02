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

package org.scijava.ops.image.threshold.minimum;

import org.scijava.ops.image.threshold.AbstractComputeThresholdHistogram;
import org.scijava.ops.image.threshold.Thresholds;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;

// NB - this plugin adapted from Gabriel Landini's code of his AutoThreshold
// plugin found in Fiji (version 1.14).

/**
 * Implements a minimum threshold method by Prewitt &amp; Mendelsohn.
 *
 * @author Barry DeZonia
 * @author Gabriel Landini
 * @implNote op names='threshold.minimum', priority='100.'
 */
public class ComputeMinimumThreshold<T extends RealType<T>> extends
	AbstractComputeThresholdHistogram<T>
{

	/**
	 * TODO
	 *
	 * @param hist the {@link Histogram1d}
	 * @return the Minimum threshold value
	 */
	@Override
	public long computeBin(final Histogram1d<T> hist) {
		final long[] histogram = hist.toLongArray();
		return computeBin(histogram);
	}

	/**
	 * J. M. S. Prewitt and M. L. Mendelsohn, "The analysis of cell images,"<br>
	 * in<br>
	 * Annals of the New York Academy of Sciences, vol. 128, pp. 1035-1053,<br>
	 * 1966.<br>
	 * ported to ImageJ plugin by G.Landini from Antti Niemisto's Matlab<br>
	 * code<br>
	 * (relicensed BSD 2-12-13)<br>
	 * Original Matlab code Copyright (C) 2004 Antti Niemisto<br>
	 * See http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide<br>
	 * presentation and the original Matlab code.<br>
	 * //<br>
	 * Assumes a bimodal histogram. The histogram needs is smoothed (using a<br>
	 * running average of size 3, iteratively) until there are only two<br>
	 * local<br>
	 * maxima.<br>
	 * Threshold t is such that ytâˆ’1 > yt â‰¤ yt+1.<br>
	 * Images with histograms having extremely unequal peaks or a broad and<br>
	 * ??at valley are unsuitable for this method. <br>
	 */
	public static long computeBin(final long[] histogram) {
		if (histogram.length < 2) return 0;
		int iter = 0;
		int max = -1;
		final double[] iHisto = new double[histogram.length];

		for (int i = 0; i < histogram.length; i++) {
			iHisto[i] = histogram[i];
			if (histogram[i] > 0) max = i;
		}
		final double[] tHisto = new double[iHisto.length];

		while (!Thresholds.bimodalTest(iHisto)) {
			// smooth with a 3 point running mean filter
			for (int i = 1; i < histogram.length - 1; i++)
				tHisto[i] = (iHisto[i - 1] + iHisto[i] + iHisto[i + 1]) / 3;
			// 0 outside
			tHisto[0] = (iHisto[0] + iHisto[1]) / 3;
			// 0 outside
			tHisto[histogram.length - 1] = (iHisto[histogram.length - 2] +
				iHisto[histogram.length - 1]) / 3;
			System.arraycopy(tHisto, 0, iHisto, 0, iHisto.length);
			iter++;
			if (iter > 10000) {
				throw new IllegalStateException(
					"Minimum Threshold not found after 10000 iterations.");
			}
		}
		// The threshold is the minimum between the two peaks.
		// NB - BDZ updated code after ij-devel mailing list communication with
		// Antti Niemisto on 2-18-13 post 1.03 release of toolbox
		final double[] y = iHisto;
		for (int k = 1; k < max; k++) {
			// IJ.log(" "+i+" "+iHisto[i]);
			if (y[k - 1] > y[k] && y[k + 1] >= y[k]) return k;
		}
		return -1;
	}

}
