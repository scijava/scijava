/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.threshold.localPhansalkar;

import java.util.Arrays;
import java.util.function.Function;

import org.scijava.ops.image.threshold.ApplyLocalThresholdIntegral;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleNeighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Pair;
import net.imglib2.view.composite.Composite;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Phansalkar's local threshold. Like the Phansalkar method of Fiji's
 * Auto_Local_Threshold plugin, the input is first stretched linearly from its
 * minimum and maximum values to [0, 1], for which Phansalkar's fixed
 * parameters p = 2 and q = 10 are intended. Pixels strictly above the
 * threshold are foreground.
 *
 * @see ComputeLocalPhansalkarThreshold
 * @implNote op names='threshold.localPhansalkar', priority='-100.'
 */
public class LocalPhansalkarThreshold<T extends RealType<T>> extends
	ApplyLocalThresholdIntegral<T, DoubleType> implements
	Computers.Arity5<RandomAccessibleInterval<T>, Shape, Double, Double, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
			RandomAccessibleInterval<BitType>> {

	private static final int INTEGRAL_IMAGE_ORDER_1 = 1;
	private static final int INTEGRAL_IMAGE_ORDER_2 = 2;

	@OpDependency(name = "stats.minMax")
	private Function<RandomAccessibleInterval<T>, Pair<T, T>> minMaxOp;

	@OpDependency(name = "stats.mean")
	private Computers.Arity1<Iterable<T>, DoubleType> meanOp;

	@OpDependency(name = "stats.stdDev")
	private Computers.Arity1<Iterable<T>, DoubleType> stdDeviationOp;

	@OpDependency(name = "stats.integralMean")
	private Computers.Arity1<RectangleNeighborhood<? extends Composite<DoubleType>>, DoubleType> integralMeanOp;

	@OpDependency(name = "stats.integralVariance")
	private Computers.Arity1<RectangleNeighborhood<? extends Composite<DoubleType>>, DoubleType> integralVarianceOp;

	@OpDependency(name = "filter.applyCenterAware")
	private Computers.Arity4<RandomAccessibleInterval<T>, Computers.Arity2<Iterable<T>, T, BitType>, Shape, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<BitType>> applyFilterOp;

	/**
	 * Computes Phansalkar's local threshold of an image.
	 *
	 * @param input the image to threshold, which is stretched to [0, 1] first
	 * @param inputNeighborhoodShape the shape of the local neighborhood
	 * @param k the k parameter (default 0.25)
	 * @param r the r parameter (default 0.5)
	 * @param outOfBoundsFactory the out-of-bounds strategy (default border)
	 * @param output the mask, true where a pixel is above its local threshold
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Shape inputNeighborhoodShape, @Nullable Double k,
		@Nullable Double r,
		@Nullable OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final RandomAccessibleInterval<BitType> output)
	{
		if (k == null) k = ComputeLocalPhansalkarThreshold.DEFAULT_K;
		if (r == null) r = ComputeLocalPhansalkarThreshold.DEFAULT_R;
		final var minMax = minMaxOp.apply(input);
		final var min = minMax.getA().getRealDouble();
		final var range = minMax.getB().getRealDouble() - min;
		final var scale = range > 0 ? 1 / range : 1;

		// Use integral images for sufficiently large windows.
		var rShape = inputNeighborhoodShape instanceof RectangleShape
			? (RectangleShape) inputNeighborhoodShape : null;
		if (rShape != null && rShape.getSpan() > 2 && !rShape.isSkippingCenter()) {
			// NB: under these conditions, the RectangleShape will produce
			// RectangleNeighborhoods (which is needed to perform the computations via
			// an IntegralImg).
			computeIntegral(input, rShape, k, r, min, scale, outOfBoundsFactory,
				output);
		}
		else {
			computeNonIntegral(input, inputNeighborhoodShape, k, r, min, scale,
				outOfBoundsFactory, output);
		}
	}

	private void computeNonIntegral(final RandomAccessibleInterval<T> input,
		final Shape inputNeighborhoodShape, final double k, final double r,
		final double min, final double scale,
		final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final RandomAccessibleInterval<BitType> output)
	{
		final Computers.Arity2<Iterable<T>, T, BitType> thresholdOp = //
			(neighborhood, center, o) -> {
				final var mean = new DoubleType();
				meanOp.compute(neighborhood, mean);
				final var stdDev = new DoubleType();
				stdDeviationOp.compute(neighborhood, stdDev);
				o.set(ComputeLocalPhansalkarThreshold.isForeground( //
					(center.getRealDouble() - min) * scale, (mean.get() - min) * scale,
					stdDev.get() * scale, k, r));
			};
		applyFilterOp.compute(input, thresholdOp, inputNeighborhoodShape,
			outOfBoundsFactory, output);
	}

	private void computeIntegral(final RandomAccessibleInterval<T> input,
		final RectangleShape inputNeighborhoodShape, final double k,
		final double r, final double min, final double scale,
		final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final RandomAccessibleInterval<BitType> output)
	{
		final Computers.Arity2<RectangleNeighborhood<? extends Composite<DoubleType>>, T, BitType> thresholdOp = //
			(neighborhood, center, o) -> {
				final var mean = new DoubleType();
				integralMeanOp.compute(neighborhood, mean);
				final var variance = new DoubleType();
				integralVarianceOp.compute(neighborhood, variance);
				o.set(ComputeLocalPhansalkarThreshold.isForeground( //
					(center.getRealDouble() - min) * scale, (mean.get() - min) * scale,
					Math.sqrt(variance.get()) * scale, k, r));
			};
		compute(input, inputNeighborhoodShape, outOfBoundsFactory, Arrays.asList(
			getIntegralImageOp(INTEGRAL_IMAGE_ORDER_1), getIntegralImageOp(
				INTEGRAL_IMAGE_ORDER_2)), thresholdOp, output);
	}

}
