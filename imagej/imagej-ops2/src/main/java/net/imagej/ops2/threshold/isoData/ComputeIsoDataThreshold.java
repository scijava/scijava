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

package net.imagej.ops2.threshold.isoData;

import net.imagej.ops2.threshold.AbstractComputeThresholdHistogram;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;

import org.scijava.Priority;
import org.scijava.ops.api.Op;
import org.scijava.ops.OpExecutionException;
import org.scijava.plugin.Plugin;

// NB - this plugin adapted from Gabriel Landini's code of his AutoThreshold
// plugin found in Fiji (version 1.14).

/**
 * Implements an IsoData (intermeans) threshold method by Ridler {@literal &}
 * Calvard.
 *
 * @author Barry DeZonia
 * @author Gabriel Landini
 */
@Plugin(type = Op.class, name = "threshold.isoData", priority = Priority.HIGH)
public class ComputeIsoDataThreshold<T extends RealType<T>> extends
	AbstractComputeThresholdHistogram<T>
{

	/**
	 * TODO
	 *
	 * @param inputHistogram
	 * @param output
	 */
	@Override
	public long computeBin(final Histogram1d<T> hist) {
		final long[] histogram = hist.toLongArray();
		return computeBin(histogram);
	}

	/**
	 * Also called intermeans<br>
	 * Iterative procedure based on the isodata algorithm [T.W. Ridler,<br>
	 * S. Calvard, Picture thresholding using an iterative selection method,<br>
	 * IEEE Trans. System, Man and Cybernetics, SMC-8 (1978) 630-632.]<br>
	 * The procedure divides the image into objects and background by taking<br>
	 * an<br>
	 * initial threshold, then the averages of the pixels at or below the<br>
	 * threshold and pixels above are computed. The averages of those two<br>
	 * values<br>
	 * are computed, the threshold is incremented and the process is<br>
	 * repeated<br>
	 * until the threshold is larger than the composite average. That is,<br>
	 * threshold = (average background + average objects)/2<br>
	 * The code in ImageJ that implements this function is the<br>
	 * getAutoThreshold() method in the ImageProcessor class.<br>
	 * <br>
	 * From: Tim Morris (dtm@ap.co.umist.ac.uk)<br>
	 * Subject: Re: Thresholding method?<br>
	 * posted to sci.image.processing on 1996/06/24<br>
	 * The algorithm implemented in NIH Image sets the threshold as that<br>
	 * grey<br>
	 * value, G, for which the average of the averages of the grey values<br>
	 * below and above G is equal to G. It does this by initialising G to<br>
	 * the<br>
	 * lowest sensible value and iterating:<br>
	 * <br>
	 * L = the average grey value of pixels with intensities < G<br>
	 * H = the average grey value of pixels with intensities > G<br>
	 * is G = (L + H)/2?<br>
	 * yes => exit<br>
	 * no => increment G and repeat<br>
	 * <br>
	 * There is a discrepancy with IJ because of slightly different methods
	 */
	public static long computeBin(final long[] histogram) {
		long l, toth, totl, h;
		int i, g = 0;
		for (i = 1; i < histogram.length; i++) {
			if (histogram[i] > 0) {
				g = i + 1;
				break;
			}
		}
		while (true) {
			l = 0;
			totl = 0;
			for (i = 0; i < g; i++) {
				totl = totl + histogram[i];
				l = l + (histogram[i] * i);
			}
			h = 0;
			toth = 0;
			for (i = g + 1; i < histogram.length; i++) {
				toth += histogram[i];
				h += (histogram[i] * i);
			}
			if (totl > 0 && toth > 0) {
				l /= totl;
				h /= toth;
				if (g == (int) Math.round((l + h) / 2.0)) break;
			}
			g++;
			if (g > histogram.length - 2) {
				throw new OpExecutionException("IsoData Threshold not found.");
			}
		}
		return g;
	}

}
