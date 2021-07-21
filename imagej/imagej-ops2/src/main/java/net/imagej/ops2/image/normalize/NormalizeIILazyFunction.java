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

package net.imagej.ops2.image.normalize;

import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.api.Op;
import org.scijava.ops.OpDependency;
import org.scijava.plugin.Plugin;

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
 */
@Plugin(type = Op.class, name = "image.normalize")
public class NormalizeIILazyFunction<I extends RealType<I>>
		implements Function<RandomAccessibleInterval<I>, RandomAccessibleInterval<I>> {

	@OpDependency(name = "create.img")
	private Function<RandomAccessibleInterval<I>, RandomAccessibleInterval<I>> createFunc;

	@OpDependency(name = "image.normalize")
	private Computers.Arity1<RandomAccessibleInterval<I>, RandomAccessibleInterval<I>> normalizer;

	/**
	 * TODO
	 *
	 * @param input
	 * @return the output
	 */
	@Override
	public RandomAccessibleInterval<I> apply(RandomAccessibleInterval<I> img) {
		RandomAccessibleInterval<I> output = createFunc.apply(img);
		normalizer.compute(img, output);
		return output;
	}

}
