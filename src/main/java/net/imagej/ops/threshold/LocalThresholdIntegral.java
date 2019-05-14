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

package net.imagej.ops.threshold;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.imagej.ops.stats.IntegralMean;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleNeighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;

import org.scijava.ops.OpDependency;
import org.scijava.ops.OpExecutionException;
import org.scijava.ops.core.computer.BiComputer;

/**
 * Apply a local thresholding method to an image using integral images for speed
 * up, optionally using a out of bounds strategy.
 *
 * @author Stefan Helfrich (University of Konstanz)
 */
public abstract class LocalThresholdIntegral<T extends RealType<T>> {

	private static final OutOfBoundsFactory<?, ?> DEFAULT_OUT_OF_BOUNDS_FACTORY =
		new OutOfBoundsBorderFactory<>();

	@SuppressWarnings("unchecked")
	public static <I> OutOfBoundsFactory<I, RandomAccessibleInterval<I>>
		defaultOutOfBoundsFactory()
	{
		return (OutOfBoundsFactory<I, RandomAccessibleInterval<I>>) DEFAULT_OUT_OF_BOUNDS_FACTORY;
	}

	private final int[] requiredIntegralImageOrders;

	@OpDependency(name = "image.integral")
	private Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<RealType<?>>> integralImgOp;

	@OpDependency(name = "image.squareIntegral")
	private Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<RealType<?>>> squareIntegralImgOp;

	public LocalThresholdIntegral(final int[] requiredIntegralImageOrders) {
		this.requiredIntegralImageOrders = requiredIntegralImageOrders;
	}

	@SuppressWarnings("rawtypes")
	protected void computeInternal(final RandomAccessibleInterval<T> input,
		RectangleShape inputNeighborhoodShape,
		OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final BiComputer<RectangleNeighborhood<Composite<DoubleType>>, T, BitType> thresholdOp,
		final IterableInterval<BitType> output)
	{
		if (outOfBoundsFactory == null) outOfBoundsFactory =
			defaultOutOfBoundsFactory();
		// Increase span of shape by 1 to return correct values together with
		// the integralSum operation
		inputNeighborhoodShape = new RectangleShape(inputNeighborhoodShape
			.getSpan() + 1, false);

		final List<RandomAccessibleInterval<RealType>> listOfIntegralImages =
			new ArrayList<>();
		for (final int order : requiredIntegralImageOrders) {
			final RandomAccessibleInterval<RealType> requiredIntegralImg =
				getIntegralImage(input, inputNeighborhoodShape, outOfBoundsFactory,
					order);
			listOfIntegralImages.add(requiredIntegralImg);
		}

		// Composite image of integral images of order 1 and 2
		final RandomAccessibleInterval<RealType> stacked = Views.stack(
			listOfIntegralImages);
		final RandomAccessibleInterval<? extends Composite<RealType>> compositeRAI =
			Views.collapse(stacked);
		final RandomAccessibleInterval<? extends Composite<RealType>> extendedCompositeRAI =
			removeLeadingZeros(compositeRAI, inputNeighborhoodShape);

		final IterableInterval<? extends Neighborhood<? extends Composite<RealType>>> neighborhoods =
			inputNeighborhoodShape.neighborhoodsSafe(extendedCompositeRAI);

		// TODO: Typing
		map(neighborhoods, input, (BiComputer) thresholdOp, output);
	}

	/**
	 * Computes integral images of a given order and extends them such that
	 * {@link IntegralMean} et al work with them.
	 *
	 * @param input The RAI for which an integral image is computed
	 * @param order
	 * @return An extended integral image for the input RAI
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private RandomAccessibleInterval<RealType> getIntegralImage(
		final RandomAccessibleInterval<T> input, final RectangleShape shape,
		final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final int order)
	{
		final ExtendedRandomAccessibleInterval<T, RandomAccessibleInterval<T>> extendedInput =
			Views.extend(input, outOfBoundsFactory);
		final FinalInterval expandedInterval = Intervals.expand(input, shape
			.getSpan() - 1l);
		final IntervalView<T> offsetInterval2 = Views.offsetInterval(extendedInput,
			expandedInterval);

		RandomAccessibleInterval<RealType> img = null;
		switch (order) {
			case 1:
				img = (RandomAccessibleInterval) integralImgOp.apply(offsetInterval2);
				break;
			case 2:
				img = (RandomAccessibleInterval) squareIntegralImgOp.apply(
					offsetInterval2);
				break;
			default:
				throw new OpExecutionException(
					"Threshold op requires an integral image of order " + order +
						". This is not available (available orders: 1, 2).");
		}
		return addLeadingZeros(img);
	}

	/**
	 * Add 0s before axis minimum.
	 *
	 * @param input Input RAI
	 * @return An extended and cropped version of input
	 */
	private static <T extends RealType<T>> RandomAccessibleInterval<T>
		addLeadingZeros(final RandomAccessibleInterval<T> input)
	{
		final long[] min = Intervals.minAsLongArray(input);
		final long[] max = Intervals.maxAsLongArray(input);

		for (int i = 0; i < max.length; i++) {
			min[i]--;
		}

		final T realZero = Util.getTypeFromInterval(input).copy();
		realZero.setZero();

		final ExtendedRandomAccessibleInterval<T, RandomAccessibleInterval<T>> extendedImg =
			Views.extendValue(input, realZero);
		final IntervalView<T> offsetInterval = Views.interval(extendedImg, min,
			max);

		return Views.zeroMin(offsetInterval);
	}

	/**
	 * Removes leading 0s from integral image after composite creation.
	 *
	 * @param input Input RAI (can be a RAI of Composite)
	 * @return An extended and cropped version of input
	 */
	private static <T> RandomAccessibleInterval<T> removeLeadingZeros(
		final RandomAccessibleInterval<T> input, final RectangleShape shape)
	{
		// Remove 0s from integralImg by shifting its interval by +1
		final long[] min = Intervals.minAsLongArray(input);
		final long[] max = Intervals.maxAsLongArray(input);

		for (int d = 0; d < input.numDimensions(); ++d) {
			final int correctedSpan = shape.getSpan() - 1;
			min[d] += (1 + correctedSpan);
			max[d] -= correctedSpan;
		}

		// Define the Interval on the infinite random accessibles
		final FinalInterval interval = new FinalInterval(min, max);

		return Views.offsetInterval(Views.extendBorder(input), interval);
	}

	private static <I1, I2, O> void map(
		final IterableInterval<? extends I1> inputNeighborhoods,
		final RandomAccessibleInterval<I2> inputCenterPixels,
		final BiComputer<I1, I2, O> filterOp, final IterableInterval<O> output)
	{
		// TODO: This used to be done via a net.imagej.ops.Ops.Map meta op. We may
		// want to revert to that approach if this proves to be too inflexible.
		final Cursor<? extends I1> neighborhoodCursor = inputNeighborhoods
			.localizingCursor();
		final RandomAccess<I2> centerPixelsAccess = inputCenterPixels
			.randomAccess();
		final Cursor<O> outputCursor = output.cursor();
		while (neighborhoodCursor.hasNext()) {
			neighborhoodCursor.fwd();
			centerPixelsAccess.setPosition(neighborhoodCursor);
			filterOp.compute(neighborhoodCursor.get(), centerPixelsAccess.get(),
				outputCursor.next());
		}
	}

}
