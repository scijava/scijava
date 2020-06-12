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

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Functions;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Normalizes an {@link IterableInterval} given its minimum and maximum to
 * another range defined by minimum and maximum.
 * 
 * @author Christian Dietz (University of Konstanz)
 * @author Leon Yang
 * @param <T>
 */
@Plugin(type = Op.class, name = "image.normalize")
@Parameter(key = "input")
@Parameter(key = "sourceMin")
@Parameter(key = "sourceMax")
@Parameter(key = "targetMin")
@Parameter(key = "targetMax")
@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
public class NormalizeIIFunction<I extends RealType<I>, O extends RealType<O>>
		implements Functions.Arity5<IterableInterval<I>, I, I, O, O, IterableInterval<O>> {

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, O, IterableInterval<O>> imgCreator;

	@OpDependency(name = "image.normalize")
	private Computers.Arity5<IterableInterval<I>, I, I, O, O, IterableInterval<O>> normalizer;

	@Override
	public IterableInterval<O> apply(final IterableInterval<I> input, final I sourceMin, final I sourceMax,
			final O targetMin, final O targetMax) {
		IterableInterval<O> output = imgCreator.apply(input, targetMin);
		normalizer.compute(input, sourceMin, sourceMax, targetMin, targetMax, output);
		return output;
	}
}
