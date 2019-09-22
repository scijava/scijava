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

package net.imagej.ops.threshold.localBernsen;

import net.imagej.ops.filter.ApplyCenterAwareNeighborhoodBasedFilter;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.ops.core.computer.Computer5;
import org.scijava.param.Mutable;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * @author Jonathan Hale
 * @author Stefan Helfrich (University of Konstanz)
 * @param <T> input type
 */
@Plugin(type = Op.class, name = "threshold.localBernsen")
@Parameter(key = "input")
@Parameter(key = "inputNeighborhoodShape")
@Parameter(key = "contrastThreshold")
@Parameter(key = "halfMaxValue")
@Parameter(key = "outOfBoundsFactory", required = false)
@Parameter(key = "output", itemIO = ItemIO.BOTH)
public class LocalBernsenThreshold<T extends RealType<T>> implements
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
		compute(input, inputNeighborhoodShape, contrastThreshold, halfMaxValue,
			outOfBoundsFactory, computeThresholdOp, output);
	}

	public static <T extends RealType<T>> void compute(
		final RandomAccessibleInterval<T> input, final Shape inputNeighborhoodShape,
		final Double contrastThreshold, final Double halfMaxValue,
		final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final Computer4<Iterable<T>, T, Double, Double, BitType> computeThresholdOp,
		@Mutable final IterableInterval<BitType> output)
	{
		final BiComputer<Iterable<T>, T, BitType> parametrizedComputeThresholdOp = //
			(i1, i2, o) -> computeThresholdOp.compute(i1, i2, contrastThreshold,
				halfMaxValue, o);
		ApplyCenterAwareNeighborhoodBasedFilter.compute(input,
			inputNeighborhoodShape, outOfBoundsFactory,
			parametrizedComputeThresholdOp, output);
	}

}
