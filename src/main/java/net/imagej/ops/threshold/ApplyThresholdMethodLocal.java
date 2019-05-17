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

import java.util.function.BiFunction;

import net.imagej.ops.filter.AbstractCenterAwareNeighborhoodBasedFilter;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleNeighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.composite.Composite;

import org.scijava.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.ops.core.computer.Computer5;
import org.scijava.param.Mutable;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

public final class ApplyThresholdMethodLocal {

	/**
	 * Ops which compute and apply a local threshold to an image.
	 *
	 * @author Stefan Helfrich (University of Konstanz)
	 */
	private ApplyThresholdMethodLocal() {
		// NB: Prevent instantiation of utility class.
	}

	@Plugin(type = Op.class, name = "threshold.localBernsen")
	@Parameter(key = "input")
	@Parameter(key = "inputNeighborhoodShape")
	@Parameter(key = "contrastThreshold")
	@Parameter(key = "halfMaxValue")
	@Parameter(key = "outOfBoundsFactory", required = false)
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class LocalBernsen<T extends RealType<T>> extends
		AbstractCenterAwareNeighborhoodBasedFilter<T, BitType> implements
		Computer5<RandomAccessibleInterval<T>, Shape, Double, Double, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
				IterableInterval<BitType>> {

		@OpDependency(name = "threshold.localBernsen")
		private Computer4<Iterable<T>, T, Double, Double, BitType> computeThresholdOp;

		@Override
		public void compute(final RandomAccessibleInterval<T> input,
			final Shape inputNeighborhoodShape, final Double contrastThreshold,
			final Double halfMaxValue,
			final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
			@Mutable final IterableInterval<BitType> output)
		{
			final BiComputer<Iterable<T>, T, BitType> parametrizedComputeThresholdOp = //
				(i1, i2, o) -> computeThresholdOp.compute(i1, i2, contrastThreshold,
					halfMaxValue, o);
			computeInternal(input, inputNeighborhoodShape, outOfBoundsFactory,
				parametrizedComputeThresholdOp, output);
		}
	}

	@Plugin(type = Op.class, name = "threshold.localContrast")
	@Parameter(key = "input")
	@Parameter(key = "inputNeighborhoodShape")
	@Parameter(key = "outOfBoundsFactory", required = false)
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class LocalContrast<T extends RealType<T>> extends
		AbstractCenterAwareNeighborhoodBasedFilter<T, BitType> implements
		Computer3<RandomAccessibleInterval<T>, Shape, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
				IterableInterval<BitType>> {

		@OpDependency(name = "threshold.localContrast")
		private BiComputer<Iterable<T>, T, BitType> computeThresholdOp;

		@Override
		public void compute(final RandomAccessibleInterval<T> input,
			final Shape inputNeighborhoodShape,
			final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
			@Mutable final IterableInterval<BitType> output)
		{
			computeInternal(input, inputNeighborhoodShape, outOfBoundsFactory,
				computeThresholdOp, output);
		}
	}

	@Plugin(type = Op.class, name = "threshold.localMean",
		priority = Priority.LOW)
	@Parameter(key = "input")
	@Parameter(key = "inputNeighborhoodShape")
	@Parameter(key = "c")
	@Parameter(key = "outOfBoundsFactory", required = false)
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class LocalMean<T extends RealType<T>> extends
		AbstractCenterAwareNeighborhoodBasedFilter<T, BitType> implements
		Computer4<RandomAccessibleInterval<T>, Shape, Double, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
				IterableInterval<BitType>> {

		@OpDependency(name = "threshold.localMean")
		private Computer3<Iterable<T>, T, Double, BitType> computeThresholdOp;

		@Override
		public void compute(final RandomAccessibleInterval<T> input,
			final Shape inputNeighborhoodShape, final Double c,
			final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
			@Mutable final IterableInterval<BitType> output)
		{
			if (inputNeighborhoodShape instanceof RectangleShape &&
				((RectangleShape) inputNeighborhoodShape).getSpan() > 2)
			{
				throw new IllegalStateException(
					"Local mean thresholding is not applicable to rectangle neighborhoods of spans larger than two.");
			}
			final BiComputer<Iterable<T>, T, BitType> parametrizedComputeThresholdOp = //
				(i1, i2, o) -> computeThresholdOp.compute(i1, i2, c, o);
			computeInternal(input, inputNeighborhoodShape, outOfBoundsFactory,
				parametrizedComputeThresholdOp, output);
		}
	}

	@Plugin(type = Op.class, name = "threshold.localMean",
		priority = Priority.LOW - 1)
	@Parameter(key = "input")
	@Parameter(key = "inputNeighborhoodShape")
	@Parameter(key = "c")
	@Parameter(key = "outOfBoundsFactory", required = false)
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class LocalMeanIntegral<T extends RealType<T>> extends
		LocalThresholdIntegral<T> implements
		Computer4<RandomAccessibleInterval<T>, RectangleShape, Double, //
				OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, IterableInterval<BitType>> {

		@OpDependency(name = "threshold.localMean")
		private Computer3<RectangleNeighborhood<Composite<DoubleType>>, T, Double, BitType> computeThresholdOp;

		public LocalMeanIntegral() {
			super(new int[] { 1 });
		}

		@Override
		public void compute(final RandomAccessibleInterval<T> input,
			final RectangleShape inputNeighborhoodShape, final Double c,
			final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
			final IterableInterval<BitType> output)
		{
			final BiComputer<RectangleNeighborhood<Composite<DoubleType>>, T, BitType> parametrizedComputeThresholdOp = //
				(i1, i2, o) -> computeThresholdOp.compute(i1, i2, c, o);
			computeInternal(input, inputNeighborhoodShape, outOfBoundsFactory,
				parametrizedComputeThresholdOp, output);
		}
	}

	@Plugin(type = Op.class, name = "threshold.localMedian")
	@Parameter(key = "input")
	@Parameter(key = "inputNeighborhoodShape")
	@Parameter(key = "c")
	@Parameter(key = "outOfBoundsFactory", required = false)
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class LocalMedian<T extends RealType<T>> extends
		AbstractCenterAwareNeighborhoodBasedFilter<T, BitType> implements
		Computer4<RandomAccessibleInterval<T>, Shape, Double, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
				IterableInterval<BitType>> {

		@OpDependency(name = "threshold.localMedian")
		private Computer3<Iterable<T>, T, Double, BitType> computeThresholdOp;

		@Override
		public void compute(final RandomAccessibleInterval<T> input,
			final Shape inputNeighborhoodShape, final Double c,
			final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
			@Mutable final IterableInterval<BitType> output)
		{
			final BiComputer<Iterable<T>, T, BitType> parametrizedComputeThresholdOp = //
				(i1, i2, o) -> computeThresholdOp.compute(i1, i2, c, o);
			computeInternal(input, inputNeighborhoodShape, outOfBoundsFactory,
				parametrizedComputeThresholdOp, output);
		}
	}

	@Plugin(type = Op.class, name = "threshold.localMidGrey",
		priority = Priority.LOW)
	@Parameter(key = "input")
	@Parameter(key = "inputNeighborhoodShape")
	@Parameter(key = "c")
	@Parameter(key = "outOfBoundsFactory", required = false)
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class LocalMidGrey<T extends RealType<T>> extends
		AbstractCenterAwareNeighborhoodBasedFilter<T, BitType> implements
		Computer4<RandomAccessibleInterval<T>, Shape, Double, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
				IterableInterval<BitType>> {

		@OpDependency(name = "threshold.localMidGrey")
		private Computer3<Iterable<T>, T, Double, BitType> computeThresholdOp;

		@Override
		public void compute(final RandomAccessibleInterval<T> input,
			final Shape inputNeighborhoodShape, final Double c,
			final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
			@Mutable final IterableInterval<BitType> output)
		{
			final BiComputer<Iterable<T>, T, BitType> parametrizedComputeThresholdOp = //
				(i1, i2, o) -> computeThresholdOp.compute(i1, i2, c, o);
			computeInternal(input, inputNeighborhoodShape, outOfBoundsFactory,
				parametrizedComputeThresholdOp, output);
		}
	}

	@Plugin(type = Op.class, name = "threshold.localNiblack",
		priority = Priority.LOW)
	@Parameter(key = "input")
	@Parameter(key = "inputNeighborhoodShape")
	@Parameter(key = "c")
	@Parameter(key = "k")
	@Parameter(key = "outOfBoundsFactory", required = false)
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class LocalNiblack<T extends RealType<T>> extends
		AbstractCenterAwareNeighborhoodBasedFilter<T, BitType> implements
		Computer5<RandomAccessibleInterval<T>, Shape, Double, Double, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
				IterableInterval<BitType>> {

		@OpDependency(name = "threshold.localNiblack")
		private Computer4<Iterable<T>, T, Double, Double, BitType> computeThresholdOp;

		@Override
		public void compute(final RandomAccessibleInterval<T> input,
			final Shape inputNeighborhoodShape, final Double c, final Double k,
			final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
			@Mutable final IterableInterval<BitType> output)
		{
			if (inputNeighborhoodShape instanceof RectangleShape &&
				((RectangleShape) inputNeighborhoodShape).getSpan() > 2)
			{
				throw new IllegalStateException(
					"Local Niblack thresholding is not applicable to rectangle neighborhoods of spans larger than two.");
			}
			final BiComputer<Iterable<T>, T, BitType> parametrizedComputeThresholdOp = //
				(i1, i2, o) -> computeThresholdOp.compute(i1, i2, c, k, o);
			computeInternal(input, inputNeighborhoodShape, outOfBoundsFactory,
				parametrizedComputeThresholdOp, output);
		}
	}

	@Plugin(type = Op.class, name = "threshold.localNiblack",
		priority = Priority.LOW - 1)
	@Parameter(key = "input")
	@Parameter(key = "inputNeighborhoodShape")
	@Parameter(key = "c")
	@Parameter(key = "k")
	@Parameter(key = "outOfBoundsFactory", required = false)
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class LocalNiblackIntegral<T extends RealType<T>> extends
		LocalThresholdIntegral<T> implements
		Computer5<RandomAccessibleInterval<T>, RectangleShape, Double, Double, //
				OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, IterableInterval<BitType>> {

		@OpDependency(name = "threshold.localNiblack")
		private Computer4<RectangleNeighborhood<Composite<DoubleType>>, T, Double, Double, BitType> computeThresholdOp;

		public LocalNiblackIntegral() {
			super(new int[] { 1, 2 });
		}

		@Override
		public void compute(final RandomAccessibleInterval<T> input,
			final RectangleShape inputNeighborhoodShape, final Double c,
			final Double k,
			final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
			final IterableInterval<BitType> output)
		{
			final BiComputer<RectangleNeighborhood<Composite<DoubleType>>, T, BitType> parametrizedComputeThresholdOp = //
				(i1, i2, o) -> computeThresholdOp.compute(i1, i2, c, k, o);
			computeInternal(input, inputNeighborhoodShape, outOfBoundsFactory,
				parametrizedComputeThresholdOp, output);
		}
	}

	@Plugin(type = Op.class, name = "threshold.localPhansalkar",
		priority = Priority.LOW)
	@Parameter(key = "input")
	@Parameter(key = "inputNeighborhoodShape")
	@Parameter(key = "k", required = false)
	@Parameter(key = "r", required = false)
	@Parameter(key = "outOfBoundsFactory", required = false)
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class LocalPhansalkar<T extends RealType<T>> extends
		AbstractCenterAwareNeighborhoodBasedFilter<T, BitType> implements
		Computer5<RandomAccessibleInterval<T>, Shape, Double, Double, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
				IterableInterval<BitType>> {

		@OpDependency(name = "threshold.localPhansalkar")
		private Computer4<Iterable<T>, T, Double, Double, BitType> computeThresholdOp;

		@Override
		public void compute(final RandomAccessibleInterval<T> input,
			final Shape inputNeighborhoodShape, final Double k, final Double r,
			final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
			@Mutable final IterableInterval<BitType> output)
		{
			if (inputNeighborhoodShape instanceof RectangleShape &&
				((RectangleShape) inputNeighborhoodShape).getSpan() > 2)
			{
				throw new IllegalStateException(
					"Local Phansalkar thresholding is not applicable to rectangle neighborhoods of spans larger than two.");
			}
			final BiComputer<Iterable<T>, T, BitType> parametrizedComputeThresholdOp = //
				(i1, i2, o) -> computeThresholdOp.compute(i1, i2, k, r, o);
			computeInternal(input, inputNeighborhoodShape, outOfBoundsFactory,
				parametrizedComputeThresholdOp, output);
		}
	}

	@Plugin(type = Op.class, name = "threshold.localPhansalkar",
		priority = Priority.LOW - 1)
	@Parameter(key = "input")
	@Parameter(key = "inputNeighborhoodShape")
	@Parameter(key = "k", required = false)
	@Parameter(key = "r", required = false)
	@Parameter(key = "outOfBoundsFactory", required = false)
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class LocalPhansalkarIntegral<T extends RealType<T>> extends
		LocalThresholdIntegral<T> implements
		Computer5<RandomAccessibleInterval<T>, RectangleShape, Double, Double, //
				OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, IterableInterval<BitType>> {

		@OpDependency(name = "threshold.localPhansalkar")
		private Computer4<RectangleNeighborhood<Composite<DoubleType>>, T, Double, Double, BitType> computeThresholdOp;

		public LocalPhansalkarIntegral() {
			super(new int[] { 1, 2 });
		}

		@Override
		public void compute(final RandomAccessibleInterval<T> input,
			final RectangleShape inputNeighborhoodShape, final Double k,
			final Double r,
			final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
			final IterableInterval<BitType> output)
		{
			final BiComputer<RectangleNeighborhood<Composite<DoubleType>>, T, BitType> parametrizedComputeThresholdOp = //
				(i1, i2, o) -> computeThresholdOp.compute(i1, i2, k, r, o);
			computeInternal(input, inputNeighborhoodShape, outOfBoundsFactory,
				parametrizedComputeThresholdOp, output);
		}
	}

	@Plugin(type = Op.class, name = "threshold.localSauvola",
		priority = Priority.LOW)
	@Parameter(key = "input")
	@Parameter(key = "inputNeighborhoodShape")
	@Parameter(key = "k", required = false)
	@Parameter(key = "r", required = false)
	@Parameter(key = "outOfBoundsFactory", required = false)
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class LocalSauvola<T extends RealType<T>> extends
		AbstractCenterAwareNeighborhoodBasedFilter<T, BitType> implements
		Computer5<RandomAccessibleInterval<T>, Shape, Double, Double, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
				IterableInterval<BitType>> {

		@OpDependency(name = "threshold.localSauvola")
		private Computer4<Iterable<T>, T, Double, Double, BitType> computeThresholdOp;

		@Override
		public void compute(final RandomAccessibleInterval<T> input,
			final Shape inputNeighborhoodShape, final Double k, final Double r,
			final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
			@Mutable final IterableInterval<BitType> output)
		{
			if (inputNeighborhoodShape instanceof RectangleShape &&
				((RectangleShape) inputNeighborhoodShape).getSpan() > 2)
			{
				throw new IllegalStateException(
					"Local Sauvola thresholding is not applicable to rectangle neighborhoods of spans larger than two.");
			}
			final BiComputer<Iterable<T>, T, BitType> parametrizedComputeThresholdOp = //
				(i1, i2, o) -> computeThresholdOp.compute(i1, i2, k, r, o);
			computeInternal(input, inputNeighborhoodShape, outOfBoundsFactory,
				parametrizedComputeThresholdOp, output);
		}
	}

	@Plugin(type = Op.class, name = "threshold.localSauvola",
		priority = Priority.LOW - 1)
	@Parameter(key = "input")
	@Parameter(key = "inputNeighborhoodShape")
	@Parameter(key = "k", required = false)
	@Parameter(key = "r", required = false)
	@Parameter(key = "outOfBoundsFactory", required = false)
	@Parameter(key = "output", type = ItemIO.BOTH)
	public static class LocalSauvolaIntegral<T extends RealType<T>> extends
		LocalThresholdIntegral<T> implements
		Computer5<RandomAccessibleInterval<T>, RectangleShape, Double, Double, //
				OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, IterableInterval<BitType>> {

		@OpDependency(name = "threshold.localSauvola")
		private Computer4<RectangleNeighborhood<Composite<DoubleType>>, T, Double, Double, BitType> computeThresholdOp;

		public LocalSauvolaIntegral() {
			super(new int[] { 1, 2 });
		}

		@Override
		public void compute(final RandomAccessibleInterval<T> input,
			final RectangleShape inputNeighborhoodShape, final Double k,
			final Double r,
			final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
			final IterableInterval<BitType> output)
		{
			final BiComputer<RectangleNeighborhood<Composite<DoubleType>>, T, BitType> parametrizedComputeThresholdOp = //
				(i1, i2, o) -> computeThresholdOp.compute(i1, i2, k, r, o);
			computeInternal(input, inputNeighborhoodShape, outOfBoundsFactory,
				parametrizedComputeThresholdOp, output);
		}
	}

	// Histogram-based threshold methods, applied locally:

	@Plugin(type = Op.class, name = "threshold.huang")
	public static class LocalHuang<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.huang")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.ij1")
	public static class LocalIJ1<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.ij1")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.intermodes")
	public static class LocalIntermodes<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.intermodes")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.isoData")
	public static class LocalIsoData<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.isoData")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.li")
	public static class LocalLi<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.li")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.maxEntropy")
	public static class LocalMaxEntropy<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.maxEntropy")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.maxLikelihood")
	public static class LocalMaxLikelihood<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.maxLikelihood")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.mean")
	public static class LocalHistogramBasedMean<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.mean")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.minError")
	public static class LocalMinError<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.minError")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.minimum")
	public static class LocalMinimum<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.minimum")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.moments")
	public static class LocalMoments<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.moments")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.otsu")
	public static class LocalOtsu<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.otsu")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.percentile")
	public static class LocalPercentile<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.percentile")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.renyiEntropy")
	public static class LocalRenyiEntropy<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.renyiEntropy")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.rosin")
	public static class LocalRosin<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.rosin")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.shanbhag")
	public static class LocalShanbhag<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.shanbhag")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.triangle")
	public static class LocalTriangle<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.triangle")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Plugin(type = Op.class, name = "threshold.yen")
	public static class LocalYen<T extends RealType<T>> extends
		AbstractApplyLocalHistogramBasedThreshold<T>
	{

		@OpDependency(name = "threshold.yen")
		private Computer<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computer<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	@Parameter(key = "input")
	@Parameter(key = "inputNeighborhoodShape")
	@Parameter(key = "outOfBoundsFactory", required = false)
	@Parameter(key = "output", type = ItemIO.BOTH)
	private abstract static class AbstractApplyLocalHistogramBasedThreshold<T extends RealType<T>>
		extends AbstractCenterAwareNeighborhoodBasedFilter<T, BitType> implements
		Computer3<RandomAccessibleInterval<T>, Shape, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
				IterableInterval<BitType>> {

		// TODO: Once optional primary parameters are supported by the matching
		// system, this can be made a unary function (drop integer parameter, which
		// is the number of bins of the histogram).
		@OpDependency(name = "image.histogram")
		private BiFunction<Iterable<T>, Integer, Histogram1d<T>> createHistogramOp;

		// TODO: Would be cool if BiComputer<T, T, BitType> would be matched.
		@OpDependency(name = "threshold.apply")
		private BiComputer<Comparable<? super T>, T, BitType> applyThresholdOp;

		private BiComputer<Iterable<T>, T, BitType> thresholdOp;

		@Override
		public void compute(final RandomAccessibleInterval<T> input,
			final Shape inputNeighborhoodShape,
			final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
			@Mutable final IterableInterval<BitType> output)
		{
			if (thresholdOp == null) thresholdOp = getThresholdOp();
			computeInternal(input, inputNeighborhoodShape, outOfBoundsFactory,
				thresholdOp, output);
		}

		private BiComputer<Iterable<T>, T, BitType> getThresholdOp() {
			final Computer<Histogram1d<T>, T> computeThresholdOp =
				getComputeThresholdOp();
			return new BiComputer<Iterable<T>, T, BitType>() {

				@Override
				public void compute(final Iterable<T> inputNeighborhood,
					final T inputCenterPixel, @Mutable final BitType output)
				{
					final Histogram1d<T> histogram = createHistogramOp.apply(
						inputNeighborhood, null);
					final T threshold = inputNeighborhood.iterator().next()
						.createVariable();
					computeThresholdOp.compute(histogram, threshold);
					applyThresholdOp.compute(inputCenterPixel, threshold, output);
				}
			};
		}

		protected abstract Computer<Histogram1d<T>, T> getComputeThresholdOp();
	}

}
