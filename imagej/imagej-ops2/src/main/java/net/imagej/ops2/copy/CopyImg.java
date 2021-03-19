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

import java.util.function.Function;

import net.imglib2.img.Img;
import net.imglib2.type.NativeType;

import org.scijava.function.Computers;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Copying {@link Img} into another {@link Img}. Exists mainly for convenience
 * reasons.
 * 
 * @author Christian Dietz (University of Konstanz)
 * @param <T>
 */
@Plugin(type = Op.class, name = "copy, copy.img")
public class CopyImg<T extends NativeType<T>> implements Computers.Arity1<Img<T>, Img<T>> {

	@OpDependency(name = "copy.iterableInterval")
	private Computers.Arity1<Iterable<T>, Iterable<T>> copyComputer;

	/**
	 * TODO
	 *
	 * @param input
	 * @param output
	 */
	@Override
	public void compute(final Img<T> input, final Img<T> output) {
		if (!input.iterationOrder().equals(output.iterationOrder()))
			throw new IllegalArgumentException("input and output must have the same iteration order!");
		copyComputer.compute(input, output);
	}
}

@Plugin(type = Op.class, name = "copy, copy.img")
class CopyImgFunction<T extends NativeType<T>> implements Function<Img<T>, Img<T>> {

	@OpDependency(name = "copy.img")
	private Computers.Arity1<Img<T>, Img<T>> copyComputer;

	@OpDependency(name = "create.img")
	private Function<Img<T>, Img<T>> createFunc;

	/**
	 * TODO
	 *
	 * @param input
	 * @return the output
	 */
	@Override
	public Img<T> apply(Img<T> input) {
		Img<T> output = createFunc.apply(input);
		copyComputer.compute(input, output);
		return output;
	}

}
