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

package net.imagej.ops2.copy;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.Dimensions;
import net.imglib2.IterableInterval;

import org.scijava.function.Computers;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.util.Maps;
import org.scijava.plugin.Plugin;

/**
 * Copies an {@link IterableInterval} into another {@link IterableInterval}
 * 
 * @author Christian Dietz (University of Konstanz)
 * @param <T>
 */
@Plugin(type = Op.class, name = "copy, copy.iterableInterval", priority = 1.0)
public class CopyII<T> implements Computers.Arity1<IterableInterval<T>, IterableInterval<T>> {

	// used internally
	//TODO: make sure that the matcher autoLifts the type copying Op
	@OpDependency(name = "copy.type")
	private Computers.Arity1<T, T> copyOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param output
	 */
	@Override
	public void compute(final IterableInterval<T> input, final IterableInterval<T> output) {
		if (!input.iterationOrder().equals(output.iterationOrder()))
			throw new IllegalArgumentException("input and output must be of the same dimensions!");
		Computers.Arity1<Iterable<T>, Iterable<T>> mapped = Maps.ComputerMaps.Iterables.liftBoth(copyOp);
		mapped.compute(input, output);
	}
}

@Plugin(type = Op.class, name = "copy, copy.iterableInterval", priority = 1.0)
class CopyIIFunction<T> implements Function<IterableInterval<T>, IterableInterval<T>> {

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, T, IterableInterval<T>> imgCreator;
	@OpDependency(name = "copy.iterableInterval")
	private Computers.Arity1<IterableInterval<T>, IterableInterval<T>> copyOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @return the output
	 */
	@Override
	public IterableInterval<T> apply(IterableInterval<T> input) {
		IterableInterval<T> output = imgCreator.apply(input, input.firstElement());
		copyOp.compute(input, output);
		return output;
	}

}
