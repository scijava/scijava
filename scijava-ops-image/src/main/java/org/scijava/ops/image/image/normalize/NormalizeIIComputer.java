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

package org.scijava.ops.image.image.normalize;

import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * Normalizes an {@link RandomAccessibleInterval} given its minimum and maximum
 * to another range defined by minimum and maximum.
 *
 * @author Christian Dietz (University of Konstanz)
 * @author Leon Yang
 * @param <I> - the type of the input
 * @param <O> - the type of the output in which we will return the normalized
 *          input.
 * @implNote op names='image.normalize'
 */
public class NormalizeIIComputer<I extends RealType<I>, O extends RealType<O>>
	implements
	Computers.Arity5<RandomAccessibleInterval<I>, I, I, O, O, RandomAccessibleInterval<O>>
{

	private NormalizeRealTypeComputer<I, O> normalizer;

	@OpDependency(name = "stats.minMax")
	private Function<RandomAccessibleInterval<I>, Pair<I, I>> minMaxFunc;

	private double[] getBounds(final RandomAccessibleInterval<I> input,
		final I sourceMin, final I sourceMax, final O targetMin, final O targetMax)
	{
		// the four elements are source min, source max, target min, and target max.
		final double[] result = new double[4];
		if (minMaxFunc != null) {
			final Pair<I, I> minMax = minMaxFunc.apply(input);
			result[0] = (sourceMin == null ? minMax.getA() : sourceMin)
				.getRealDouble();
			result[1] = (sourceMax == null ? minMax.getB() : sourceMax)
				.getRealDouble();
		}
		else {
			result[0] = sourceMin.getRealDouble();
			result[1] = sourceMax.getRealDouble();
		}
		final I first = Util.getTypeFromInterval(input);
		result[2] = targetMin == null ? first.getMinValue() : targetMin
			.getRealDouble();
		result[3] = targetMax == null ? first.getMaxValue() : targetMax
			.getRealDouble();
		return result;
	}

	/**
	 * TODO
	 *
	 * @param input
	 * @param sourceMin
	 * @param sourceMax
	 * @param targetMin
	 * @param targetMax
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<I> input,
		final I sourceMin, final I sourceMax, final O targetMin, final O targetMax,
		final RandomAccessibleInterval<O> output)
	{
		normalizer = new NormalizeRealTypeComputer<>();
		final double[] bounds = getBounds(input, sourceMin, sourceMax, targetMin,
			targetMax);
		normalizer.setup(bounds[0], bounds[1], bounds[2], bounds[3]);
		LoopBuilder.setImages(input, output).multiThreaded().forEachPixel((in,
			out) -> {
			normalizer.compute(in, out);
		});
	}
}
