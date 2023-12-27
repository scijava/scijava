/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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

package org.scijava.ops.image.threshold.intermodes;

import org.scijava.ops.image.threshold.AbstractComputeThresholdHistogram;
import org.scijava.ops.image.threshold.Thresholds;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.spi.OpExecutionException;

// NB - this plugin adapted from Gabriel Landini's code of his AutoThreshold
// plugin found in Fiji (version 1.14).

/**
 * Implements an intermodes threshold method by Prewitt {@literal &} Mendelsohn.
 *
 * @author Barry DeZonia
 * @author Gabriel Landini
 * @implNote op names='threshold.intermodes', priority='100.'
 */
public class ComputeIntermodesThreshold<T extends RealType<T>> extends
	AbstractComputeThresholdHistogram<T>
{

	/**
	 *
	 * @param hist the {@link Histogram1d}
	 * @return the Intermodes threshold
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
	 * <br>
	 * Assumes a bimodal histogram. The histogram needs is smoothed (using a<br>
	 * running average of size 3, iteratively) until there are only two<br>
	 * local<br>
	 * maxima.<br>
	 * j and k<br>
	 * Threshold t is (j+k)/2.<br>
	 * Images with histograms having extremely unequal peaks or a broad and<br>
	 * ??at valley are unsuitable for this method.
	 */
	public static long computeBin(final long[] histogram) {
		final double[] iHisto = new double[histogram.length];
		int iter = 0;
		int threshold = -1;
		for (int i = 0; i < histogram.length; i++)
			iHisto[i] = histogram[i];

		while (!Thresholds.bimodalTest(iHisto)) {
			// smooth with a 3 point running mean filter
			double previous = 0, current = 0, next = iHisto[0];
			for (int i = 0; i < histogram.length - 1; i++) {
				previous = current;
				current = next;
				next = iHisto[i + 1];
				iHisto[i] = (previous + current + next) / 3;
			}
			iHisto[histogram.length - 1] = (current + next) / 3;
			iter++;
			if (iter > 10000) {
				throw new OpExecutionException(
					"Intermodes Threshold not found after 10000 iterations.");
			}
		}

		// The threshold is the mean between the two peaks.
		int tt = 0;
		for (int i = 1; i < histogram.length - 1; i++) {
			if (iHisto[i - 1] < iHisto[i] && iHisto[i + 1] < iHisto[i]) {
				tt += i;
				// IJ.log("mode:" +i);
			}
		}
		threshold = (int) Math.floor(tt / 2.0);
		return threshold;
	}

}
