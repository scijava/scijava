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

package org.scijava.ops.image.threshold.localMean;

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
import net.imglib2.view.composite.Composite;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.Nullable;

/**
 * Implementation of the local mean threshold method for images. Makes use of
 * integral images for an improved execution speed of the threshold computation,
 * depending on its parameterization.
 *
 * @author Jonathan Hale (University of Konstanz)
 * @author Martin Horn (University of Konstanz)
 * @author Stefan Helfrich (University of Konstanz)
 * @implNote op names='threshold.localMean', priority='-100.'
 */
public class LocalMeanThreshold<T extends RealType<T>> extends
	ApplyLocalThresholdIntegral<T, DoubleType> implements
	Computers.Arity4<RandomAccessibleInterval<T>, Shape, Double, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
			RandomAccessibleInterval<BitType>> {

	private static final int INTEGRAL_IMAGE_ORDER = 1;

	@OpDependency(name = "threshold.localMean")
	private Computers.Arity3<Iterable<T>, T, Double, BitType> computeThresholdNonIntegralOp;

	@OpDependency(name = "threshold.localMean")
	private Computers.Arity3<RectangleNeighborhood<? extends Composite<DoubleType>>, T, Double, BitType> computeThresholdIntegralOp;

	@OpDependency(name = "filter.applyCenterAware")
	private Computers.Arity4<RandomAccessibleInterval<T>, Computers.Arity2<Iterable<T>, T, BitType>, Shape, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<BitType>> applyFilterOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param inputNeighborhoodShape
	 * @param c
	 * @param outOfBoundsFactory
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Shape inputNeighborhoodShape, final Double c,
		@Nullable OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final RandomAccessibleInterval<BitType> output)
	{
		// Use integral images for sufficiently large windows.
		RectangleShape rShape = inputNeighborhoodShape instanceof RectangleShape
			? (RectangleShape) inputNeighborhoodShape : null;
		if (rShape != null && rShape.getSpan() > 2 && !rShape.isSkippingCenter()) {
			// NB: under these conditions, the RectangleShape will produce
			// RectangleNeighborhoods (which is needed to perform the computations via
			// an IntegralImg).
			computeIntegral(input, rShape, c, outOfBoundsFactory, getIntegralImageOp(
				INTEGRAL_IMAGE_ORDER), computeThresholdIntegralOp, output);
		}
		else {
			computeNonIntegral(input, inputNeighborhoodShape, c, outOfBoundsFactory,
				computeThresholdNonIntegralOp, output);
		}
	}

	public void computeNonIntegral(final RandomAccessibleInterval<T> input,
		final Shape inputNeighborhoodShape, final Double c,
		final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final Computers.Arity3<Iterable<T>, T, Double, BitType> computeThresholdOp,
		final RandomAccessibleInterval<BitType> output)
	{
		final Computers.Arity2<Iterable<T>, T, BitType> parameterizedComputeThresholdOp = //
			(i1, i2, o) -> computeThresholdOp.compute(i1, i2, c, o);
		applyFilterOp.compute(input, parameterizedComputeThresholdOp,
			inputNeighborhoodShape, outOfBoundsFactory, output);
	}

	public void computeIntegral(final RandomAccessibleInterval<T> input,
		final RectangleShape inputNeighborhoodShape, final Double c,
		final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<DoubleType>> integralImageOp,
		final Computers.Arity3<RectangleNeighborhood<? extends Composite<DoubleType>>, T, Double, BitType> computeThresholdOp,
		final RandomAccessibleInterval<BitType> output)
	{
		final Computers.Arity2<RectangleNeighborhood<? extends Composite<DoubleType>>, T, BitType> parameterizedComputeThresholdOp = //
			(i1, i2, o) -> computeThresholdOp.compute(i1, i2, c, o);
		compute(input, inputNeighborhoodShape, outOfBoundsFactory, Arrays.asList(
			integralImageOp), parameterizedComputeThresholdOp, output);
	}

}
