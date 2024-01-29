/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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

package org.scijava.ops.image.threshold;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpExecutionException;

import org.scijava.ops.image.stats.IntegralMean;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleNeighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.RectangleShape.NeighborhoodsAccessible;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;

/**
 * Apply a local thresholding method to an image using integral images for speed
 * up, optionally using an out of bounds strategy.
 *
 * @author Stefan Helfrich (University of Konstanz)
 */
public abstract class ApplyLocalThresholdIntegral<T extends RealType<T>, U extends RealType<U>> {

	private final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> DEFAULT_OUT_OF_BOUNDS_FACTORY =
		new OutOfBoundsBorderFactory<>();

	public OutOfBoundsFactory<T, RandomAccessibleInterval<T>>
		defaultOutOfBoundsFactory()
	{
		return DEFAULT_OUT_OF_BOUNDS_FACTORY;
	}

	// TODO: The only reason this class is not fully static (but also serves as
	// abstract base class), is to be able to use op dependencies here to avoid
	// boilerplate code in extending classes. Is there some better way to do this?

	@OpDependency(name = "image.integral")
	private Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>> integralImgOp;

	@OpDependency(name = "image.squareIntegral")
	private Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>> squareIntegralImgOp;

	protected Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>>
		getIntegralImageOp(final int integralImageOrder)
	{
		if (integralImageOrder == 1) return integralImgOp;
		else if (integralImageOrder == 2) return squareIntegralImgOp;
		else throw new OpExecutionException(
			"Threshold op requires to compute an integral image of order " +
				integralImageOrder +
				". There is no op available to do that (available orders are: 1, 2).");
	}

	protected void compute(final RandomAccessibleInterval<T> input,
		RectangleShape inputNeighborhoodShape,
		OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final List<Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>>> integralImageOps,
		final Computers.Arity2<RectangleNeighborhood<? extends Composite<U>>, T, BitType> thresholdOp,
		final RandomAccessibleInterval<BitType> output)
	{
		if (outOfBoundsFactory == null) outOfBoundsFactory =
			defaultOutOfBoundsFactory();
		// Increase span of shape by 1 to return correct values together with
		// the integralSum operation
		inputNeighborhoodShape = new RectangleShape(inputNeighborhoodShape
			.getSpan() + 1, false);

		final List<RandomAccessibleInterval<U>> listOfIntegralImages =
			new ArrayList<>(integralImageOps.size());
		for (final Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>> //
		integralImageOp : integralImageOps) {
			final RandomAccessibleInterval<U> requiredIntegralImg = getIntegralImage(
				input, inputNeighborhoodShape, outOfBoundsFactory, integralImageOp);
			listOfIntegralImages.add(requiredIntegralImg);
		}

		// Composite image of integral images of all orders
		final RandomAccessibleInterval<U> stacked = Views.stack(
			listOfIntegralImages);
		// NB Views.collapse returns a RandomAccessibleInterval<? extends
		// GenericComposite<U>>. We know that any subclass of GenericComposite<U> is
		// inherently a Composite<U>, but due to generic typing we cannot simply
		// cast a RAI<? extends GenericComposite<U>> to a RAI<Composite<U>>. This
		// raw cast allows us to get a RAI<Composite<U>> in a way that satisfies
		// javac.
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final RandomAccessibleInterval<Composite<U>> compositeRAI =
			(RandomAccessibleInterval) Views.collapse(stacked);
		final RandomAccessibleInterval<Composite<U>> extendedCompositeRAI =
			removeLeadingZeros(compositeRAI, inputNeighborhoodShape);

		RandomAccessibleInterval<RectangleNeighborhood<Composite<U>>> neighborhoodsRAI =
			asRectangularNeighborhoodInterval(inputNeighborhoodShape,
				extendedCompositeRAI);

		LoopBuilder.setImages(neighborhoodsRAI, input, output) //
			.multiThreaded() //
			.forEachPixel(thresholdOp::compute);
	}

	private <A> RandomAccessibleInterval<RectangleNeighborhood<A>>
		asRectangularNeighborhoodInterval(RectangleShape inputNeighborhoodShape,
			final RandomAccessibleInterval<A> extendedCompositeRAI)
	{
		final NeighborhoodsAccessible<A> neighborhoods = inputNeighborhoodShape
			.neighborhoodsRandomAccessibleSafe(extendedCompositeRAI);
		final IntervalView<Neighborhood<A>> interval = Views.interval(neighborhoods,
			extendedCompositeRAI);

		if (!(Util.getTypeFromInterval(
			interval) instanceof RectangleNeighborhood))
		{
			throw new IllegalStateException(
				"RectangleShape did not produce a RandomAccess<RectangleNeighborhood>!");
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final RandomAccessibleInterval<RectangleNeighborhood<A>> result =
			(RandomAccessibleInterval) interval;
		return result;
	}

	/**
	 * Computes integral images of a given order and extends them such that
	 * {@link IntegralMean} et al work with them.
	 *
	 * @param input The RAI for which an integral image is computed
	 * @return An extended integral image for the input RAI
	 */
	private RandomAccessibleInterval<U> getIntegralImage(
		final RandomAccessibleInterval<T> input, final RectangleShape shape,
		final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>> integralOp)
	{
		final ExtendedRandomAccessibleInterval<T, RandomAccessibleInterval<T>> extendedInput =
			Views.extend(input, outOfBoundsFactory);
		final FinalInterval expandedInterval = Intervals.expand(input, shape
			.getSpan() - 1l);
		final IntervalView<T> offsetInterval2 = Views.offsetInterval(extendedInput,
			expandedInterval);
		final RandomAccessibleInterval<U> img = integralOp.apply(offsetInterval2);
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
			Views.extendValue(input, realZero.getRealFloat());
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

}
