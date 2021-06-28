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

package net.imagej.ops2.filter.pad;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.ops.Op;
import org.scijava.ops.OpDependency;
import org.scijava.plugin.Plugin;

/**
 * Op used to pad a kernel and shift the center of the kernel to the origin
 * 
 * @author bnorthan
 * @param <T>
 * @param <I>
 * @param <O>
 */
@Plugin(type = Op.class, name = "filter.padShiftKernel")
public class PadShiftKernel<T extends ComplexType<T>, I extends RandomAccessibleInterval<T>, O extends RandomAccessibleInterval<T>>
	implements BiFunction<I, Dimensions, O> 
{

	@OpDependency(name = "filter.padIntervalCentered")
	private BiFunction<I, Dimensions, Interval> paddingIntervalCentered;

	@OpDependency(name = "filter.padIntervalOrigin")
	private BiFunction<I, Interval, Interval> paddingIntervalOrigin;

	@Override
	@SuppressWarnings("unchecked")
	/**
	 * TODO
	 *
	 * @param kernel
	 * @param paddedDimensions
	 * @return the output
	 */
	public O apply(final I kernel, final Dimensions paddedDimensions) {

		Dimensions paddedFFTInputDimensions;

		// if an fftsize op has been set recompute padded size
		if (getFFTSizeOp() != null) {
			long[][] sizes = getFFTSizeOp().apply(paddedDimensions);

			paddedFFTInputDimensions = new FinalDimensions(sizes[0]);
		}
		else {
			paddedFFTInputDimensions = paddedDimensions;

		}

		// compute where to place the final Interval for the kernel so that the
		// coordinate in the center
		// of the kernel is shifted to position (0,0).

		final Interval kernelConvolutionInterval = paddingIntervalCentered.apply(
			kernel, paddedFFTInputDimensions);

		final Interval kernelConvolutionIntervalOrigin = paddingIntervalOrigin
			.apply(kernel, kernelConvolutionInterval);

		return (O) Views.interval(Views.extendPeriodic(Views.interval(Views
			.extendValue(kernel, Util.getTypeFromInterval(kernel).createVariable()),
			kernelConvolutionInterval)), kernelConvolutionIntervalOrigin);

	}

	protected Function<Dimensions, long[][]> getFFTSizeOp() {
		return null;
	}

}
