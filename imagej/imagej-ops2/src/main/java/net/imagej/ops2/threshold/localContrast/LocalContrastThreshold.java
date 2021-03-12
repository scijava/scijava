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

package net.imagej.ops2.threshold.localContrast;

import net.imagej.ops2.filter.ApplyCenterAwareNeighborhoodBasedFilter;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * @author Jonathan Hale
 * @author Stefan Helfrich (University of Konstanz)
 */
@Plugin(type = Op.class, name = "threshold.localContrast")
public class LocalContrastThreshold<T extends RealType<T>> implements
	Computers.Arity3<RandomAccessibleInterval<T>, Shape, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
			RandomAccessibleInterval<BitType>> {

	@OpDependency(name = "threshold.localContrast")
	private Computers.Arity2<Iterable<T>, T, BitType> computeThresholdOp;

	@Override
	/**
	 * TODO
	 *
	 * @param input
	 * @param inputNeighborhoodShape
	 * @param outOfBoundsFactory
	 * @param output
	 */
	public void compute(final RandomAccessibleInterval<T> input,
		final Shape inputNeighborhoodShape,
		final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final RandomAccessibleInterval<BitType> output)
	{
		compute(input, inputNeighborhoodShape, outOfBoundsFactory,
			computeThresholdOp, output);
	}

	public static <T extends RealType<T>> void compute(
		final RandomAccessibleInterval<T> input, final Shape inputNeighborhoodShape,
		final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final Computers.Arity2<Iterable<T>, T, BitType> computeThresholdOp,
		final RandomAccessibleInterval<BitType> output)
	{
		ApplyCenterAwareNeighborhoodBasedFilter.compute(input,
			inputNeighborhoodShape, outOfBoundsFactory, computeThresholdOp, output);
	}

}
