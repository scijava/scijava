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

package org.scijava.ops.image.features.hog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

import org.scijava.ops.image.thread.chunker.Chunk;
import org.scijava.ops.image.thread.chunker.CursorBasedChunk;
import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.RectangleShape.NeighborhoodsAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import net.imglib2.view.composite.GenericComposite;

import org.scijava.concurrent.Parallelization;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.ops.spi.OpDependency;

/**
 * Calculates a histogram of oriented gradients which is a feature descriptor.
 * The technique first calculates the partial derivatives and then for each
 * pixel a histogram of gradient directions by summing up the magnitudes of the
 * neighbored (@param spanOfNeighborhood) pixels. The directions are divided in
 * (@param numOrientations) bins. The output is 3d: for each bin an own channel.
 * Input can be either a 2D image or a 3D image where the third dimension is
 * interpreted as color channel (e.g. RGB, LAB, ...). The algorithm is based on
 * the paper "Histograms of Oriented Gradients for Human Detection" by Navneet
 * Dalal and Bill Triggs, published 2005.
 *
 * @author Simon Schmid (University of Konstanz)
 * @implNote op names='features.hog'
 */
public class HistogramOfOrientedGradients2D<T extends RealType<T>> implements
	Computers.Arity3<RandomAccessibleInterval<T>, Integer, Integer, RandomAccessibleInterval<T>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> createImgOp;

	@OpDependency(name = "thread.chunker")
	private Inplaces.Arity2_1<Chunk, Long> chunkerOp;

	private Converter<T, FloatType> converterToFloat;

	private Converter<GenericComposite<FloatType>, FloatType> converterGetMax;

	/**
	 * TODO
	 *
	 * @param in
	 * @param numOrientations
	 * @param spanOfNeighborhood
	 * @param out
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void compute(RandomAccessibleInterval<T> in, Integer numOrientations,
		Integer spanOfNeighborhood, RandomAccessibleInterval<T> out)
	{
		Interval imgOpInterval = new FinalInterval(in.dimension(0), in.dimension(
			1));

		converterToFloat = (arg0, arg1) -> arg1.setReal(arg0.getRealFloat());

		converterGetMax = (input, output) -> {
			int idx = 0;
			float max = 0;
			for (int i = 0; i < in.dimension(2); i++) {
				if (Math.abs(input.get(i).getRealFloat()) > max) {
					max = Math.abs(input.get(i).getRealFloat());
					idx = i;
				}
			}
			output.setReal(input.get(idx).getRealFloat());
		};

		if (in.numDimensions() > 3 || in.numDimensions() < 2)
			throw new IllegalArgumentException(
				"Input image is of an unsupported number of dimensions");
		final RandomAccessible<FloatType> convertedIn = Converters.convert(Views
			.extendMirrorDouble(in), converterToFloat, new FloatType());

		// compute partial derivative for each dimension
		RandomAccessibleInterval<FloatType> derivative0 = createImgOp.apply(
			imgOpInterval, new FloatType());
		RandomAccessibleInterval<FloatType> derivative1 = createImgOp.apply(
			imgOpInterval, new FloatType());

		// case of grayscale image
		if (in.numDimensions() == 2) {
			PartialDerivative.gradientCentralDifference(convertedIn, derivative0, 0);
			PartialDerivative.gradientCentralDifference(convertedIn, derivative1, 1);
		}
		// case of color image
		else {
			List<RandomAccessibleInterval<FloatType>> listDerivs0 = new ArrayList<>();
			List<RandomAccessibleInterval<FloatType>> listDerivs1 = new ArrayList<>();
			for (int i = 0; i < in.dimension(2); i++) {
				final RandomAccessibleInterval<FloatType> deriv0 = createImgOp.apply(
					imgOpInterval, new FloatType());
				final RandomAccessibleInterval<FloatType> deriv1 = createImgOp.apply(
					imgOpInterval, new FloatType());
				PartialDerivative.gradientCentralDifference(Views.interval(convertedIn,
					new long[] { 0, 0, i }, new long[] { in.max(0), in.max(1), i }),
					deriv0, 0);
				PartialDerivative.gradientCentralDifference(Views.interval(convertedIn,
					new long[] { 0, 0, i }, new long[] { in.max(0), in.max(1), i }),
					deriv1, 1);
				listDerivs0.add(deriv0);
				listDerivs1.add(deriv1);
			}
			derivative0 = Converters.convert(Views.collapse(Views.stack(listDerivs0)),
				converterGetMax, new FloatType());
			derivative1 = Converters.convert(Views.collapse(Views.stack(listDerivs1)),
				converterGetMax, new FloatType());
		}
		final RandomAccessibleInterval<FloatType> finalderivative0 = derivative0;
		final RandomAccessibleInterval<FloatType> finalderivative1 = derivative1;

		// compute angles and magnitudes
		final RandomAccessibleInterval<FloatType> angles = createImgOp.apply(
			imgOpInterval, new FloatType());
		final RandomAccessibleInterval<FloatType> magnitudes = createImgOp.apply(
			imgOpInterval, new FloatType());

		final CursorBasedChunk chunkable = new CursorBasedChunk() {

			@Override
			public void execute(long startIndex, long stepSize, long numSteps) {
				final Cursor<FloatType> cursorAngles = Views.flatIterable(angles)
					.localizingCursor();
				final Cursor<FloatType> cursorMagnitudes = Views.flatIterable(
					magnitudes).localizingCursor();
				final Cursor<FloatType> cursorDerivative0 = Views.flatIterable(
					finalderivative0).localizingCursor();
				final Cursor<FloatType> cursorDerivative1 = Views.flatIterable(
					finalderivative1).localizingCursor();

				setToStart(cursorAngles, startIndex);
				setToStart(cursorMagnitudes, startIndex);
				setToStart(cursorDerivative0, startIndex);
				setToStart(cursorDerivative1, startIndex);

				for (long i = 0; i < numSteps; i++) {
					final float x = cursorDerivative0.get().getRealFloat();
					final float y = cursorDerivative1.get().getRealFloat();
					cursorAngles.get().setReal(getAngle(x, y));
					cursorMagnitudes.get().setReal(getMagnitude(x, y));

					cursorAngles.jumpFwd(stepSize);
					cursorMagnitudes.jumpFwd(stepSize);
					cursorDerivative0.jumpFwd(stepSize);
					cursorDerivative1.jumpFwd(stepSize);
				}
			}
		};

		chunkerOp.mutate(chunkable, Views.flatIterable(magnitudes).size());

		// stores each Thread to execute
		final List<Runnable> listCallables = new ArrayList<>();

		// compute descriptor (default 3x3, i.e. 9 channels: one channel for
		// each bin)
		final RectangleShape shape = new RectangleShape(spanOfNeighborhood, false);
		final NeighborhoodsAccessible<FloatType> neighborHood = shape
			.neighborhoodsRandomAccessible(angles);

		for (int i = 0; i < in.dimension(0); i++) {
			listCallables.add(new ComputeDescriptor(Views.interval(convertedIn, in),
				i, angles.randomAccess(), magnitudes.randomAccess(),
				(RandomAccess<FloatType>) out.randomAccess(), neighborHood
					.randomAccess(), numOrientations));
		}

		Parallelization.getTaskExecutor().runAll(listCallables);

		listCallables.clear();
	}

	private class ComputeDescriptor implements Runnable {

		final private RandomAccessibleInterval<FloatType> in;
		final private long i;
		final private RandomAccess<FloatType> raAngles;
		final private RandomAccess<FloatType> raMagnitudes;
		final private RandomAccess<FloatType> raOut;
		final private RandomAccess<Neighborhood<FloatType>> raNeighbor;
		final private Integer numOrientations;

		public ComputeDescriptor(final RandomAccessibleInterval<FloatType> in,
			final long i, final RandomAccess<FloatType> raAngles,
			final RandomAccess<FloatType> raMagnitudes,
			final RandomAccess<FloatType> raOut,
			final RandomAccess<Neighborhood<FloatType>> raNeighbor,
			final Integer numOrientations)
		{
			this.in = in;
			this.i = i;
			this.raAngles = raAngles;
			this.raMagnitudes = raMagnitudes;
			this.raOut = raOut;
			this.raNeighbor = raNeighbor;
			this.numOrientations = numOrientations;
		}

		public void run() {

			final FinalInterval interval = new FinalInterval(in.dimension(0), in
				.dimension(1));
			for (int j = 0; j < in.dimension(1); j++) {
				// sum up the magnitudes of all bins in a neighborhood
				raNeighbor.setPosition(new long[] { i, j });
				final Cursor<FloatType> cursorNeighborHood = raNeighbor.get().cursor();
				while (cursorNeighborHood.hasNext()) {
					cursorNeighborHood.next();
					if (Intervals.contains(interval, cursorNeighborHood)) {
						raAngles.setPosition(cursorNeighborHood);
						raMagnitudes.setPosition(cursorNeighborHood);
						raOut.setPosition(new long[] { i, j, (int) (raAngles.get()
							.getRealFloat() / (360 / numOrientations) - 0.5) });
						raOut.get().add(raMagnitudes.get());
					}
				}
			}
		}
	}

	// returns the signed angle of a vector
	private double getAngle(final double x, final double y) {
		float angle = (float) Math.toDegrees(Math.atan2(x, y));
		if (angle < 0) {
			angle += 360;
		}
		return angle;
	}

	// returns the magnitude of a vector
	private double getMagnitude(final double x, final double y) {
		return Math.sqrt(x * x + y * y);
	}
}

/**
 * @implNote op names='features.hog'
 */
class HistogramOfOrientedGradients2DFunction<T extends RealType<T>> implements
	Functions.Arity3<RandomAccessibleInterval<T>, Integer, Integer, RandomAccessibleInterval<T>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, T, RandomAccessibleInterval<T>> outputCreator;

	@OpDependency(name = "features.hog")
	private Computers.Arity3<RandomAccessibleInterval<T>, Integer, Integer, RandomAccessibleInterval<T>> hogOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param numOrientations
	 * @param spanOfNeighborhood
	 * @return the output
	 */
	@Override
	public RandomAccessibleInterval<T> apply(
		final RandomAccessibleInterval<T> input, final Integer numOrientations,
		final Integer spanOfNeighborhood)
	{
		final T inType = Util.getTypeFromInterval(input);
		RandomAccessibleInterval<T> output = outputCreator.apply(new FinalInterval(
			input.dimension(0), input.dimension(1), numOrientations), inType);
		hogOp.compute(input, numOrientations, spanOfNeighborhood, output);
		return output;
	}

}
