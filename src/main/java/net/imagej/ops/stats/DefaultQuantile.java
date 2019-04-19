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

package net.imagej.ops.stats;

import java.util.Iterator;

import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;
import org.scijava.util.ArrayUtils;
import org.scijava.util.DoubleArray;

/**
 * {@link Op} to calculate the n-th {@code stats.percentile}.
 * 
 * @author Daniel Seebacher (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @author Jan Eglinger
 * @param <I> input type
 * @param <O> output type
 */
@Plugin(type = Op.class, name = "stats.quantile")
@Parameter(key = "iterableInput")
@Parameter(key = "quantile", min = "0.0", max = "1.0")
@Parameter(key = "output", type = ItemIO.BOTH)
public class DefaultQuantile<I extends RealType<I>, O extends RealType<O>>
implements BiComputer<Iterable<I>, Double, O>
{

//	@Parameter(min = "0.0", max = "1.0")
//	private double quantile;
	
	@Override
	public void compute(final Iterable<I> input, final Double quantile, final O output) {
		final DoubleArray statistics;
		if(input instanceof IterableInterval) {
			statistics = new DoubleArray(0);
			statistics.ensureCapacity(ArrayUtils.safeMultiply32(Intervals.numElements((IterableInterval<?>)input)));
		}
		else 
			statistics = new DoubleArray();

		final Iterator<I> it = input.iterator();
		int counter = 0;
		while (it.hasNext()) {
			statistics.addValue(counter++, it.next().getRealDouble());
		}

		output.setReal(select(statistics, 0, statistics.size() - 1, (int) (statistics
			.size() * quantile)));
	}

	/**
	 * Returns the value of the kth lowest element. Do note that for nth lowest
	 * element, k = n - 1.
	 */
	private double select(final DoubleArray array, final int inLeft,
		final int inRight, final int k)
	{

		int left = inLeft;
		int right = inRight;

		while (true) {

			if (right <= left + 1) {

				if (right == left + 1 && array.getValue(right) < array.getValue(left)) {
					swap(array, left, right);
				}

				return array.getValue(k);

			}
			final int middle = left + right >>> 1;
			swap(array, middle, left + 1);

			if (array.getValue(left) > array.getValue(right)) {
				swap(array, left, right);
			}

			if (array.getValue(left + 1) > array.getValue(right)) {
				swap(array, left + 1, right);
			}

			if (array.getValue(left) > array.getValue(left + 1)) {
				swap(array, left, left + 1);
			}

			int i = left + 1;
			int j = right;
			final double pivot = array.getValue(left + 1);

			while (true) {
				do
					++i;
				while (array.getValue(i) < pivot);
				do
					--j;
				while (array.getValue(j) > pivot);

				if (j < i) {
					break;
				}

				swap(array, i, j);
			}

			array.set(left + 1, array.getValue(j));
			array.set(j, pivot);

			if (j >= k) {
				right = j - 1;
			}

			if (j <= k) {
				left = i;
			}
		}
	}

	/** Helper method for swapping array entries */
	private void swap(final DoubleArray array, final int a, final int b) {
		final double temp = array.getValue(a);
		array.set(a, array.getValue(b));
		array.set(b, temp);
	}
}
