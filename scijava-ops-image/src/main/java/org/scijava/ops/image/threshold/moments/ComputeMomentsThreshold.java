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

package org.scijava.ops.image.threshold.moments;

import org.scijava.ops.image.threshold.AbstractComputeThresholdHistogram;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;

// NB - this plugin adapted from Gabriel Landini's code of his AutoThreshold
// plugin found in Fiji (version 1.14).

/**
 * Implements a moments based threshold method by Tsai.
 *
 * @author Barry DeZonia
 * @author Gabriel Landini
 * @implNote op names='threshold.moments', priority='100.'
 */
public class ComputeMomentsThreshold<T extends RealType<T>> extends
	AbstractComputeThresholdHistogram<T>
{

	/**
	 * TODO
	 *
	 * @param hist the {@link Histogram1d}
	 * @return the Moments threshold value
	 */
	@Override
	public long computeBin(final Histogram1d<T> hist) {
		final long[] histogram = hist.toLongArray();
		return computeBin(histogram);
	}

	/**
	 * W. Tsai, "Moment-preserving thresholding: a new approach,"
	 * Computers.Arity1<br>
	 * Vision, Graphics, and Image Processing, vol. 29, pp. 377-393, 1985.<br>
	 * Ported to ImageJ plugin by G.Landini from the the open source project<br>
	 * FOURIER 0.8 by M. Emre Celebi , Department of Computer Science,<br>
	 * Louisiana State University in Shreveport, Shreveport, LA 71115, USA<br>
	 * http://sourceforge.net/projects/fourier-ipal<br>
	 * http://www.lsus.edu/faculty/~ecelebi/fourier.htm
	 */
	public static long computeBin(final long[] histogram) {
		double total = 0;
		final double m0 = 1.0;
		double m1 = 0.0, m2 = 0.0, m3 = 0.0, sum = 0.0, p0 = 0.0;
		double cd, c0, c1, z0, z1; /* auxiliary variables */
		int threshold = -1;

		final double[] histo = new double[histogram.length];

		for (int i = 0; i < histogram.length; i++)
			total += histogram[i];

		for (int i = 0; i < histogram.length; i++)
			histo[i] = histogram[i] / total; // normalised histogram

		/* Calculate the first, second, and third order moments */
		for (int i = 0; i < histogram.length; i++) {
			m1 += i * histo[i];
			m2 += i * i * histo[i];
			m3 += i * i * i * histo[i];
		}
		/*
		 * First 4 moments of the gray-level image should match the first 4
		 * moments of the target binary image. This leads to 4 equalities whose
		 * solutions are given in the Appendix of Ref. 1
		 */
		cd = m0 * m2 - m1 * m1;
		c0 = (-m2 * m2 + m1 * m3) / cd;
		c1 = (m0 * -m3 + m2 * m1) / cd;
		z0 = 0.5 * (-c1 - Math.sqrt(c1 * c1 - 4.0 * c0));
		z1 = 0.5 * (-c1 + Math.sqrt(c1 * c1 - 4.0 * c0));
		p0 = (z1 - m1) / (z1 - z0); /*
																* Fraction of the object pixels in the
																* target binary image
																*/

		// The threshold is the gray-level closest
		// to the p0-tile of the normalized histogram
		sum = 0;
		for (int i = 0; i < histogram.length; i++) {
			sum += histo[i];
			if (sum > p0) {
				threshold = i;
				break;
			}
		}
		return threshold;
	}

}
