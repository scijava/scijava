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

package net.imagej.ops.filter;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.view.Views;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer3;

public abstract class AbstractNeighborhoodBasedFilter<I, O> implements
		Computer3<RandomAccessibleInterval<I>, Shape, OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, IterableInterval<O>> {

	@OpDependency(name = "map.neighborhood")
	private Computer3<RandomAccessibleInterval<I>, Shape, Computer<Iterable<I>, O>, IterableInterval<O>> map;

	@Override
	public void compute(RandomAccessibleInterval<I> input, Shape shape,
			OutOfBoundsFactory<I, RandomAccessibleInterval<I>> outOfBoundsFactory, IterableInterval<O> output) {
		Computer<Iterable<I>, O> filterOp = unaryComputer();
		map.compute(Views.interval(Views.extend(input, outOfBoundsFactory), input), shape, filterOp, output);
	}

	/**
	 * @return the Computer to map to all neighborhoods of input to output.
	 */
	protected abstract Computer<Iterable<I>, O> unaryComputer();

}
