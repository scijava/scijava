/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.transform.project.project;

import java.util.Iterator;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.util.Intervals;

import net.imglib2.view.Views;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.OpDependency;

/**
 * <b>Projection</b> is the act of creating 1-dimensional slices of an n-dimensional image,
 * reducing that slice down to a single value, and combining those images back into a (n-1)-dimensional array
 *
 * @param <T> the type of input image elements
 * @param <V> the type of output image elements
 * @implNote op name='transform.project', priority='99.', hints="adaptation.FORBIDDEN"
 * @see ProjectParallelFunction for an Op that creates its own output
 */
public class ProjectParallelComputer<T, V> implements
	Computers.Arity3<RandomAccessibleInterval<T>, Computers.Arity1<? super RandomAccessibleInterval<T>, V>, Integer, RandomAccessibleInterval<V>>
{

	@OpDependency(name="transform.hyperSliceView")
	Functions.Arity3<RandomAccessibleInterval<T>, Integer, Long, RandomAccessibleInterval<T>> slicer;

	/**
	 * Projects {@code op} along 1-dimensional slices (along dimension {@code dim}) of {@code input}
	 *
	 * @param input the input {@code n}-dimensional dataset
	 * @param op the Op to project over {@code dim}
	 * @param dim the dimension along {@code input} to project
	 * @param output the output {@code n-1}-dimensional dataset
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		Computers.Arity1<? super RandomAccessibleInterval<T>, V> op, Integer dim,
		final RandomAccessibleInterval<V> output) {
		// TODO this first check is too simple, but for now ok
		if (input.numDimensions() != output.numDimensions() + 1) //
			throw new IllegalArgumentException(
				"ERROR: input image must have one dimension more than output image!");
		if (input.numDimensions() <= dim) //
			throw new IllegalArgumentException(
				"ERROR: input image must contain dimension " + dim);

		LoopBuilder.setImages(output, Intervals.positions(output)).multiThreaded()
			.forEachPixel((pixel, position) -> {
				var ra = input;
				for (var d = 0; d < position.numDimensions(); d++) {
					ra = slicer.apply(ra, d < dim ? 0 : 1, position.getLongPosition(d));
				}
				op.compute(ra, pixel);
			});
	}

	final class DimensionIterable implements Iterable<T> {

		private final long size;
		private final int dim;
		private final RandomAccess<T> access;

		public DimensionIterable(final long size, final int dim,
			final RandomAccess<T> access)
		{
			this.size = size;
			this.dim = dim;
			this.access = access;
		}

		@Override
		public Iterator<T> iterator() {
			return new Iterator<T>() {

				int k = -1;

				@Override
				public boolean hasNext() {
					return k < size - 1;
				}

				@Override
				public T next() {
					k++;
					access.setPosition(k, dim);
					return access.get();
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("Not supported");
				}
			};
		}
	}
}
