/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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

package net.imagej.ops2.threshold;

import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Ops which compute and apply a local threshold to an image.
 *
 * @author Stefan Helfrich (University of Konstanz)
 */
public final class ApplyThresholdMethodLocal {

	private ApplyThresholdMethodLocal() {
		// NB: Prevent instantiation of utility class.
	}

	/**
 *@implNote op names='threshold.huang'
 */
	public static class LocalHuang<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.huang")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.ij1'
 */
	public static class LocalIJ1<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.ij1")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.intermodes'
 */
	public static class LocalIntermodes<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.intermodes")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.isoData'
 */
	public static class LocalIsoData<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.isoData")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.li'
 */
	public static class LocalLi<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.li")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.maxEntropy'
 */
	public static class LocalMaxEntropy<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.maxEntropy")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.maxLikelihood'
 */
	public static class LocalMaxLikelihood<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.maxLikelihood")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.mean'
 */
	public static class LocalHistogramBasedMean<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.mean")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.minError'
 */
	public static class LocalMinError<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.minError")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.minimum'
 */
	public static class LocalMinimum<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.minimum")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.moments'
 */
	public static class LocalMoments<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.moments")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.otsu'
 */
	public static class LocalOtsu<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.otsu")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.percentile'
 */
	public static class LocalPercentile<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.percentile")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.renyiEntropy'
 */
	public static class LocalRenyiEntropy<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.renyiEntropy")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.rosin'
 */
	public static class LocalRosin<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.rosin")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.shanbhag'
 */
	public static class LocalShanbhag<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.shanbhag")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.triangle'
 */
	public static class LocalTriangle<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.triangle")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
 *@implNote op names='threshold.yen'
 */
	public static class LocalYen<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.yen")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	private abstract static class AbstractApplyLocalHistogramBasedThreshold<T extends RealType<T>>
		implements
		Computers.Arity3<RandomAccessibleInterval<T>, Shape, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
				RandomAccessibleInterval<BitType>> {

		@OpDependency(name = "image.histogram")
		private Function<Iterable<T>, Histogram1d<T>> createHistogramOp;

		// TODO: Would be cool if Computers.Arity2<T, T, BitType> would be matched.
		@OpDependency(name = "threshold.apply")
		private Computers.Arity2<Comparable<? super T>, T, BitType> applyThresholdOp;

		@OpDependency(name = "filter.applyCenterAware")
		private Computers.Arity4<RandomAccessibleInterval<T>, Computers.Arity2<Iterable<T>, T, BitType>, Shape, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<BitType>> applyFilterOp;

		private Computers.Arity2<Iterable<T>, T, BitType> thresholdOp;

		/**
		 * TODO
		 *
		 * @param input
		 * @param inputNeighborhoodShape
		 * @param outOfBoundsFactory (required = false)
		 * @param output
		 */
		@Override
		public void compute(final RandomAccessibleInterval<T> input,
			final Shape inputNeighborhoodShape,
			@Nullable OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
			final RandomAccessibleInterval<BitType> output)
		{
			if (outOfBoundsFactory == null) outOfBoundsFactory =
					new OutOfBoundsBorderFactory<>();
			if (thresholdOp == null) thresholdOp = getThresholdOp();
			applyFilterOp.compute(input, thresholdOp,
				inputNeighborhoodShape, outOfBoundsFactory, output);
		}

		private Computers.Arity2<Iterable<T>, T, BitType> getThresholdOp() {
			final Computers.Arity1<Histogram1d<T>, T> computeThresholdOp =
				getComputeThresholdOp();
			return new Computers.Arity2<Iterable<T>, T, BitType>() {

				@Override
				public void compute(final Iterable<T> inputNeighborhood,
					final T inputCenterPixel, final BitType output)
				{
					final Histogram1d<T> histogram = createHistogramOp.apply(
						inputNeighborhood);
					final T threshold = inputNeighborhood.iterator().next()
						.createVariable();
					computeThresholdOp.compute(histogram, threshold);
					applyThresholdOp.compute(inputCenterPixel, threshold, output);
				}
			};
		}

		protected abstract Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp();
	}

}
