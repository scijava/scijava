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

package net.imagej.ops.project;

import java.util.Iterator;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;

import org.scijava.Priority;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = Op.class, name = "project", priority = Priority.LOW)
@Parameter(key = "input")
@Parameter(key = "op")
@Parameter(key = "dim")
@Parameter(key = "output", itemIO = ItemIO.BOTH)
public class ProjectRAIToIterableInterval<T, V>
		implements Computer3<RandomAccessibleInterval<T>, Computer<Iterable<T>, V>, Integer, IterableInterval<V>> {

	@Override
	public void compute(final RandomAccessibleInterval<T> input, final Computer<Iterable<T>, V> method,
			final Integer dim, final IterableInterval<V> output) {
		if (input.numDimensions() != output.numDimensions() + 1)
			throw new IllegalArgumentException("Input must have one more dimension than output!");
		if (dim >= input.numDimensions())
			throw new IllegalArgumentException("The dimension provided to compute over does not exitst in the input!");

		final Cursor<V> cursor = output.localizingCursor();
		final RandomAccess<T> access = input.randomAccess();

		while (cursor.hasNext()) {
			cursor.fwd();
			for (int d = 0; d < input.numDimensions(); d++) {
				if (d != dim) {
					access.setPosition(cursor.getIntPosition(d - (d > dim ? -1 : 0)), d);
				}
			}

			method.compute(new DimensionIterable(input.dimension(dim), access, dim), cursor.get());
		}
	}

	final class DimensionIterable implements Iterable<T> {

		private final long size;
		private final RandomAccess<T> access;
		private final int dim;

		public DimensionIterable(final long size, final RandomAccess<T> access, final int dim) {
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
