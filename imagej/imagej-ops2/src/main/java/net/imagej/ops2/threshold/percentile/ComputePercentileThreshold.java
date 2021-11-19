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

package net.imagej.ops2.threshold.percentile;

import net.imagej.ops2.threshold.AbstractComputeThresholdHistogram;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;

import org.scijava.Priority;
import org.scijava.ops.core.Op;
import org.scijava.plugin.Plugin;

// NB - this plugin adapted from Gabriel Landini's code of his AutoThreshold
// plugin found in Fiji (version 1.14).

/**
 * Implements a percentile threshold method by Doyle.
 *
 * @author Barry DeZonia
 * @author Gabriel Landini
 */
@Plugin(type = Op.class, name = "threshold.percentile",
	priority = Priority.HIGH)
public class ComputePercentileThreshold<T extends RealType<T>> extends
	AbstractComputeThresholdHistogram<T>
{

	@Override
	public long computeBin(final Histogram1d<T> hist) {
		final long[] histogram = hist.toLongArray();
		return computeBin(histogram);
	}

	/**
	 * W. Doyle,"Operation useful for similarity-invariant pattern
	 * recognition,"<br>
	 * Journal of the Association for Computing Machinery, vol. 9,pp.<br>
	 * 259-267,<br>
	 * 1962.<br>
	 * ported to ImageJ plugin by G.Landini from Antti Niemisto's Matlab<br>
	 * code<br>
	 * (relicensed BSD 2-12-13)<br>
	 * Original Matlab code Copyright (C) 2004 Antti Niemisto<br>
	 * See http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide<br>
	 * presentation and the original Matlab code.
	 */
	public static long computeBin(final long[] histogram) {
		int threshold = -1;
		final double ptile = 0.5; // default fraction of foreground pixels
		final double[] avec = new double[histogram.length];

		for (int i = 0; i < histogram.length; i++)
			avec[i] = 0.0;

		final double total = partialSum(histogram, histogram.length - 1);
		double temp = 1.0;
		for (int i = 0; i < histogram.length; i++) {
			avec[i] = Math.abs((partialSum(histogram, i) / total) - ptile);
			// IJ.log("Ptile["+i+"]:"+ avec[i]);
			if (avec[i] < temp) {
				temp = avec[i];
				threshold = i;
			}
		}
		return threshold;
	}

	private static double partialSum(final long[] y, final int j) {
		double x = 0;
		for (int i = 0; i <= j; i++)
			x += y[i];
		return x;
	}

}
