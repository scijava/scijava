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

import net.imagej.ops.special.computer.BinaryComputerOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.param.Mutable;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

public final class ApplyThresholdMethodLocal {

	/**
	 * Ops that apply a global threshold locally to a
	 * {@link RandomAccessibleInterval}.
	 *
	 * @author Stefan Helfrich (University of Konstanz)
	 */
	private ApplyThresholdMethodLocal() {
		// NB: Prevent instantiation of utility class.
	}

	public static class HuangLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.huang")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class IJ1Local<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.ij1")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class IntermodesLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.intermodes")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class IsoDataLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.isoData")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class LiLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.li")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class MaxEntropyLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.maxEntropy")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class MaxLikelihoodLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.maxLikelihood")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class MeanLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.mean")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class MinErrorLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.minError")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class MinimumLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.minimum")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class MomentsLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.moments")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class OtsuLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.otsu")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class PercentileLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.percentile")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class RenyiEntropyLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.renyiEntropy")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class RosinLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.rosin")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class ShanbhagLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.shanbhag")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class TriangleLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.triangle")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	public static class YenLocal<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThresholdToImage<T>
	{

		@OpDependency(name = "threshold.yen")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	private abstract static class AbstractApplyLocalHistogramBasedThresholdToImage<T extends RealType<T>> {

		@OpDependency(name = "threshold.apply")
		private Computer3<Iterable<T>, T, Computer<Histogram1d<T>, T>, BitType> applyThresholdOp;

		protected abstract Computer<Histogram1d<T>, T> getComputeThresholdOp();

		// TODO: Implement & lift. Should extend
		// ApplyCenterAwareNeighborhoodBasedFilter, see there.
	}

	// TODO: "apply" is not entirely correct because we also compute the threshold
	// here.
	@Plugin(type = Op.class, name = "threshold.apply")
	@Parameter(key = "inputNeighborhood")
	@Parameter(key = "inputCenterPixel")
	@Parameter(key = "computeThresholdOp")
	@Parameter(key = "output", type = ItemIO.BOTH)
	private static class ApplyLocalHistogramBasedThreshold<T extends RealType<T>>
		implements Computer3<Iterable<T>, T, Computer<Histogram1d<T>, T>, BitType>
	{

		@OpDependency(name = "image.histogram")
		private Function<Iterable<T>, Histogram1d<T>> createHistogramOp;

		@OpDependency(name = "threshold.apply")
		private BinaryComputerOp<T, T, BitType> applyThresholdOp;

		@Override
		public void compute(final Iterable<T> inputNeighborhood,
			final T inputCenterPixel,
			final Computer<Histogram1d<T>, T> computeThresholdOp,
			@Mutable final BitType output)
		{
			final Histogram1d<T> histogram = createHistogramOp.apply(
				inputNeighborhood);
			final T threshold = inputNeighborhood.iterator().next().createVariable();
			computeThresholdOp.compute(histogram, threshold);
			applyThresholdOp.compute(inputCenterPixel, threshold, output);
		}
	}

}
