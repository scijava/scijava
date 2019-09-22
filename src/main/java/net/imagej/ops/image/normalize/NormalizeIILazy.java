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

package net.imagej.ops.image.normalize;

import java.util.function.Function;

import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer5;
import org.scijava.param.Mutable;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Normalizes an {@link IterableInterval} given its minimum and maximum to
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
@Parameter(key = "input")
@Parameter(key = "output", itemIO = ItemIO.BOTH)
public class NormalizeIILazy<I extends RealType<I>, O extends RealType<O>>
		implements Computer<IterableInterval<I>, IterableInterval<O>> {

	@OpDependency(name = "stats.minMax")
	private Function<IterableInterval<I>, Pair<I, I>> minMaxFunc;

	@OpDependency(name = "image.normalize")
	private Computer5<IterableInterval<I>, I, I, O, O, IterableInterval<O>> normalizerFunc;

	@Override
	public void compute(IterableInterval<I> img, @Mutable IterableInterval<O> output) {
		Pair<I, I> sourceMinMax = minMaxFunc.apply(img);
		O min = output.firstElement().createVariable();
		min.setReal(min.getMinValue());
		O max = output.firstElement().createVariable();
		max.setReal(max.getMaxValue());

		normalizerFunc.accept(img, sourceMinMax.getA(), sourceMinMax.getB(), min, max, output);
	}

}
