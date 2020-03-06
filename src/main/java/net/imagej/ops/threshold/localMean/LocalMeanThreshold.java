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

package net.imagej.ops.threshold.localMean;

import java.util.Arrays;
import java.util.function.Function;

import net.imagej.ops.filter.ApplyCenterAwareNeighborhoodBasedFilter;
import net.imagej.ops.threshold.ApplyLocalThresholdIntegral;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleNeighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.composite.Composite;

import org.scijava.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.param.Mutable;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Implementation of the local mean threshold method for images. Makes use of
 * integral images for an improved execution speed of the threshold computation,
 * depending on its parameterization.
 *
 * @author Jonathan Hale (University of Konstanz)
 * @author Martin Horn (University of Konstanz)
 * @author Stefan Helfrich (University of Konstanz)
 */
@Plugin(type = Op.class, name = "threshold.localMean", priority = Priority.LOW)
@Parameter(key = "input")
@Parameter(key = "inputNeighborhoodShape")
@Parameter(key = "c")
@Parameter(key = "outOfBoundsFactory", required = false)
@Parameter(key = "output", itemIO = ItemIO.BOTH)
public class LocalMeanThreshold<T extends RealType<T>> extends
	ApplyLocalThresholdIntegral<T> implements
	Computers.Arity4<RandomAccessibleInterval<T>, Shape, Double, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
			IterableInterval<BitType>> {

	private static final int INTEGRAL_IMAGE_ORDER = 1;

	@OpDependency(name = "threshold.localMean")
	private Computers.Arity3<Iterable<T>, T, Double, BitType> computeThresholdNonIntegralOp;

	@OpDependency(name = "threshold.localMean")
	private Computers.Arity3<RectangleNeighborhood<Composite<DoubleType>>, T, Double, BitType> computeThresholdIntegralOp;

	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Shape inputNeighborhoodShape, final Double c,
		final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		@Mutable final IterableInterval<BitType> output)
	{
		// Use integral images for sufficiently large windows.
		if (inputNeighborhoodShape instanceof RectangleShape &&
			((RectangleShape) inputNeighborhoodShape).getSpan() > 2)
		{
			computeIntegral(input, (RectangleShape) inputNeighborhoodShape, c,
				outOfBoundsFactory, getIntegralImageOp(INTEGRAL_IMAGE_ORDER),
				computeThresholdIntegralOp, output);
		}
		else {
			computeNonIntegral(input, inputNeighborhoodShape, c, outOfBoundsFactory,
				computeThresholdNonIntegralOp, output);
		}
	}

	public static <T extends RealType<T>> void computeNonIntegral(
		final RandomAccessibleInterval<T> input, final Shape inputNeighborhoodShape,
		final Double c,
		final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final Computers.Arity3<Iterable<T>, T, Double, BitType> computeThresholdOp,
		@Mutable final IterableInterval<BitType> output)
	{
		final Computers.Arity2<Iterable<T>, T, BitType> parametrizedComputeThresholdOp = //
			(i1, i2, o) -> computeThresholdOp.compute(i1, i2, c, o);
		ApplyCenterAwareNeighborhoodBasedFilter.compute(input,
			inputNeighborhoodShape, outOfBoundsFactory,
			parametrizedComputeThresholdOp, output);
	}

	public static <T extends RealType<T>> void computeIntegral(
		final RandomAccessibleInterval<T> input,
		final RectangleShape inputNeighborhoodShape, final Double c,
		final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<? extends RealType<?>>> integralImageOp,
		final Computers.Arity3<RectangleNeighborhood<Composite<DoubleType>>, T, Double, BitType> computeThresholdOp,
		@Mutable final IterableInterval<BitType> output)
	{
		final Computers.Arity2<RectangleNeighborhood<Composite<DoubleType>>, T, BitType> parametrizedComputeThresholdOp = //
			(i1, i2, o) -> computeThresholdOp.compute(i1, i2, c, o);
		ApplyLocalThresholdIntegral.compute(input, inputNeighborhoodShape,
			outOfBoundsFactory, Arrays.asList(integralImageOp),
			parametrizedComputeThresholdOp, output);
	}

}
