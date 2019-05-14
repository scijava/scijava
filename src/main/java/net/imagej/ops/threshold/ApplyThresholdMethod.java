/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2016 Board of Regents of the University of
 * Wisconsin-Madison and University of Konstanz.
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

package net.imagej.ops.threshold;

import java.util.function.Function;

import net.imglib2.histogram.Histogram1d;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.computer.Computer;

/**
 * Ops which compute and apply a global threshold to an {@link Img}.
 *
 * @author Christian Dietz (University of Konstanz)
 * @author Curtis Rueden
 * @author Brian Northan
 */
public final class ApplyThresholdMethod {

	private ApplyThresholdMethod() {
		// NB: Prevent instantiation of utility class.
	}

	public static class Huang<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.huang")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class IJ1<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.ij1")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class Intermodes<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.intermodes")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class IsoData<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.isoData")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class Li<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.li")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class MaxEntropy<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.maxEntropy")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class MaxLikelihood<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.maxLikelihood")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class Mean<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.mean")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class MinError<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.minError")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class Minimum<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.minimum")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class Moments<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.moments")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class Otsu<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.otsu")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class Percentile<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.percentile")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class RenyiEntropy<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.renyiEntropy")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class Rosin<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.rosin")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class Shanbhag<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.shanbhag")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class Triangle<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.triangle")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class Yen<T extends RealType<T>> extends
		AbstractApplyGlobalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.yen")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	private abstract static class AbstractApplyGlobalHistogramBasedThreshold<T extends RealType<T>>
		extends AbstractApplyGlobalThreshold<T>
	{

		@OpDependency(name = "image.histogram")
		private Function<Iterable<T>, Histogram1d<T>> createHistogramOp;

		@Override
		protected T computeThreshold(final Iterable<T> input) {
			final Histogram1d<T> inputHistogram = createHistogramOp.apply(input);
			final T threshold = input.iterator().next().createVariable();
			final Computer<Histogram1d<T>, T> computeThresholdOp =
				getComputeThresholdOp();
			computeThresholdOp.compute(inputHistogram, threshold);
			return threshold;
		}

		protected abstract Computer<Histogram1d<T>, T> getComputeThresholdOp();
	}

}
