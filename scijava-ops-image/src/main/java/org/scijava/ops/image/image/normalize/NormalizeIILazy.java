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

package org.scijava.ops.image.image.normalize;

import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * Normalizes an {@link RandomAccessibleInterval} given its minimum and maximum to
 * another range defined by minimum and maximum.
 * 
 * TODO: Should this be a scale op?
 * 
 * @author Christian Dietz (University of Konstanz)
 * @author Leon Yang
 * @param <I> - the type of the input image
 * @param <O> - the type of the output image
 *@implNote op names='image.normalize'
 */
public class NormalizeIILazy<I extends RealType<I>, O extends RealType<O>>
		implements Computers.Arity1<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>> {

	@OpDependency(name = "stats.minMax")
	private Function<RandomAccessibleInterval<I>, Pair<I, I>> minMaxFunc;

	@OpDependency(name = "image.normalize")
	private Computers.Arity5<RandomAccessibleInterval<I>, I, I, O, O, RandomAccessibleInterval<O>> normalizerFunc;

	/**
	 * TODO
	 *
	 * @param input
	 * @param output
	 */
	@Override
	public void compute(RandomAccessibleInterval<I> img, RandomAccessibleInterval<O> output) {
		Pair<I, I> sourceMinMax = minMaxFunc.apply(img);
		O min = Util.getTypeFromInterval(output).createVariable();
		min.setReal(min.getMinValue());
		O max = Util.getTypeFromInterval(output).createVariable();
		max.setReal(max.getMaxValue());

		normalizerFunc.accept(img, sourceMinMax.getA(), sourceMinMax.getB(), min, max, output);
	}

}
