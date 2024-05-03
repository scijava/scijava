/*-
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2024 ImageJ developers.
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

package org.scijava.ops.image.coloc.saca;

import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.function.Function;

import org.scijava.progress.Progress;
import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * @author Shulei Wang
 * @author Curtis Rueden
 * @author Ellen TA Dobson
 * @author Edward Evans
 * @param <I> input type
 * @implNote op names='coloc.saca.heatmapZScore', priority='100.'
 */

public class SACAHeatmapZScore<I extends RealType<I>> implements
	Computers.Arity5<RandomAccessibleInterval<I>, RandomAccessibleInterval<I>, I, I, Long, RandomAccessibleInterval<DoubleType>>
{

	@OpDependency(name = "image.histogram")
	private Function<Iterable<I>, Histogram1d<I>> histOp;

	@OpDependency(name = "threshold.otsu")
	private Function<Histogram1d<I>, I> otsuOp;

	/**
	 * Spatially Adaptive Colocalization Analysis (SACA) Z-score heatmap. This Op
	 * utilizes the SACA framework to identify colocalized pixels and their
	 * strength as a Z-score heatmap. The Z-score heatmap is a produced by
	 * Shulei's adaptive smoothed kendall tau algorithim. SACA was adapted from
	 * Shulei's java code for AdaptiveSmoothedKendallTau in his RKColocal package
	 * (https://github.com/lakerwsl/RKColocal/blob/master/RKColocal_0.0.1.0000.tar.gz).
	 *
	 * @param image1 input image 1
	 * @param image2 input image 2
	 * @param thres1 threshold 1 value; otsu threshold applied if null
	 * @param thres2 threshold 2 value; otsu threshold applied if null
	 * @param seed seed value to use; default 0xdeadbeefL
	 * @param result Adaptive smoothed kendall tau Z-score heatmap (i.e.
	 *          colocalization strength)
	 */

	@Override
	public void compute(final RandomAccessibleInterval<I> image1,
		final RandomAccessibleInterval<I> image2,

		@Nullable I thres1, @Nullable I thres2, @Nullable Long seed,
		RandomAccessibleInterval<DoubleType> result)
	{
		// ensure images have the same dimensions
		FinalDimensions dims1 = new FinalDimensions(image1.dimensionsAsLongArray());
		FinalDimensions dims2 = new FinalDimensions(image2.dimensionsAsLongArray());
		if (!(Intervals.equalDimensions(dims1, dims2))) {
			throw new IllegalArgumentException(
				"Input image dimensions do not match.");
		}

		// ensure images are no more than 2 dimensional
		if (image1.numDimensions() > 2 || image2.numDimensions() > 2) {
			throw new IllegalArgumentException(
				"Only 2D images are supported by SACA. See https://github.com/scijava/scijava/issues/212");
		}

		// set seed, compute thresholds and create empty result if necessary
		if (seed == null) seed = 0xdeadbeefL;
		if (thres1 == null) thres1 = otsuOp.apply(histOp.apply(Views.iterable(
			image1)));
		if (thres2 == null) thres2 = otsuOp.apply(histOp.apply(Views.iterable(
			image2)));

		AdaptiveSmoothedKendallTau.execute(image1, image2, result, thres1, thres2,
			seed);
	}
}
