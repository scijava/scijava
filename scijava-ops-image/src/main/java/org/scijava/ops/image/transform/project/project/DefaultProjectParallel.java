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

import org.scijava.function.Computers;

/**
 * @param <T>
 * @param <V>
 * @implNote op name='transform.project', priority='99.'
 */
public class DefaultProjectParallel<T, V> implements
	Computers.Arity3<RandomAccessibleInterval<T>, Computers.Arity1<Iterable<T>, V>, Integer, RandomAccessibleInterval<V>>
{

	/**
	 * TODO
	 *
	 * @param input
	 * @param op
	 * @param dim
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		Computers.Arity1<Iterable<T>, V> op, Integer dim,
		final RandomAccessibleInterval<V> output)
	{
		// TODO this first check is too simple, but for now ok
		if (input.numDimensions() != output.numDimensions() + 1) //
			throw new IllegalArgumentException(
				"ERROR: input image must have one dimension more than output image!");
		if (input.numDimensions() <= dim) //
			throw new IllegalArgumentException(
				"ERROR: input image must contain dimension " + dim);

		LoopBuilder.setImages(output, Intervals.positions(output)).multiThreaded()
			.forEachChunk(chunk -> {
                var chunkRA = input.randomAccess();
				chunk.forEachPixel((pixel, position) -> {
					for (var d = 0; d < input.numDimensions(); d++) {
						if (d != dim) {
							chunkRA.setPosition(position.getIntPosition(d - (d > dim ? 1
								: 0)), d);
						}
					}

					op.compute(new DimensionIterable(input.dimension(dim), dim, chunkRA),
						pixel);

				});

				return null;
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
