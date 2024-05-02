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

package org.scijava.ops.image.transform.project.project;

import java.util.Iterator;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;

import org.scijava.function.Computers;

/**
 * @implNote op names='transform.project', priority='-100.'
 */
public class ProjectRAIToIterableInterval<T, V> implements
	Computers.Arity3<RandomAccessibleInterval<T>, Computers.Arity1<Iterable<T>, V>, Integer, IterableInterval<V>>
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
		final Computers.Arity1<Iterable<T>, V> method, final Integer dim,
		final IterableInterval<V> output)
	{
		if (input.numDimensions() != output.numDimensions() + 1)
			throw new IllegalArgumentException(
				"Input must have one more dimension than output!");
		if (dim >= input.numDimensions()) throw new IllegalArgumentException(
			"The dimension provided to compute over does not exist in the input!");

		final Cursor<V> cursor = output.localizingCursor();
		final RandomAccess<T> access = input.randomAccess();

		while (cursor.hasNext()) {
			cursor.fwd();
			for (int d = 0; d < input.numDimensions(); d++) {
				if (d != dim) {
					access.setPosition(cursor.getIntPosition(d - (d > dim ? -1 : 0)), d);
				}
			}

			method.compute(new DimensionIterable(input.dimension(dim), access, dim),
				cursor.get());
		}
	}

	final class DimensionIterable implements Iterable<T> {

		private final long size;
		private final RandomAccess<T> access;
		private final int dim;

		public DimensionIterable(final long size, final RandomAccess<T> access,
			final int dim)
		{
			this.size = size;
			this.access = access;
			this.dim = dim;
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
