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

import java.lang.reflect.Array;
import java.util.function.Function;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.type.NativeType;
import net.imglib2.util.Intervals;

import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.ops.api.Op;
import org.scijava.ops.OpDependency;
import org.scijava.plugin.Plugin;

/**
 * Copying {@link ArrayImg} into another {@link ArrayImg}
 * 
 * @author Christian Dietz (University of Konstanz)
 * @param <T>
 */
@Plugin(type = Op.class, name = "copy, copy.img", priority = Priority.VERY_HIGH)
public class CopyArrayImg<T extends NativeType<T>, A extends ArrayDataAccess<A>>
		implements Computers.Arity1<ArrayImg<T, A>, ArrayImg<T, A>> {
	/**
	 * TODO
	 *
	 * @param input
	 * @param copy
	 */
	@Override
	public void compute(final ArrayImg<T, A> input, final ArrayImg<T, A> output) {

		if (!Intervals.equalDimensions(input, output))
			throw new IllegalArgumentException("The Dimensions of the input and copy images must be the same!");

		final Object inArray = input.update(null).getCurrentStorageArray();
		System.arraycopy(inArray, 0, output.update(null).getCurrentStorageArray(), 0, Array.getLength(inArray));

	}
}

@Plugin(type = Op.class, name = "copy, copy.img", priority = Priority.VERY_HIGH)
class CopyArrayImgFunction<T extends NativeType<T>, A extends ArrayDataAccess<A>>
		implements Function<ArrayImg<T, A>, ArrayImg<T, A>> {

	@OpDependency(name = "copy.img")
	private Computers.Arity1<ArrayImg<T, A>, ArrayImg<T, A>> copyOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param copy
	 */
	@Override
	public ArrayImg<T, A> apply(ArrayImg<T, A> input) {
		// NB: Workaround for ArrayImgFactory not overriding create(Dimensions, T).
		final long[] dims = new long[input.numDimensions()];
		input.dimensions(dims);
		final ArrayImg<T, ?> copy = input.factory().create(dims, input.firstElement().createVariable());

		// TODO: Find a way to guarantee the type.
		@SuppressWarnings("unchecked")
		final ArrayImg<T, A> typedCopy = (ArrayImg<T, A>) copy;
		copyOp.compute(input, typedCopy);
		
		return typedCopy;
	}

}
