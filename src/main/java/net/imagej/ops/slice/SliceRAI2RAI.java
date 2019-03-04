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

package net.imagej.ops.slice;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;

import org.scijava.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * {@link SliceOp} implementation for {@link RandomAccessibleInterval} input and
 * {@link RandomAccessibleInterval} output.
 * 
 * <p>
 * The input {@link RandomAccessibleInterval} will be wrapped into a
 * {@link SlicesII}, so that the given Op can compute on a per-slice base.
 * </p>
 * 
 * @author Christian Dietz (University of Konstanz)
 * @author Martin Horn (University of Konstanz)
 */
@Plugin(type = Op.class, name = "slice", priority = Priority.VERY_HIGH)
@Parameter(key = "input")
@Parameter(key = "Op")
@Parameter(key = "axisIndices")
@Parameter(key = "dropSingleDimensions")
@Parameter(key = "output", type = ItemIO.BOTH)
public class SliceRAI2RAI<I, O> implements
		Computer4<RandomAccessibleInterval<I>, Computer<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>>, int[], Boolean, RandomAccessibleInterval<O>> {

	// Note that to satisfy the Type Matcher we have to match a BiComputer with
	// II<RAI<O>> as the output, we cannot match against SlicesII<O>. This is
	// because there is no map Op that guarantees a SlicesII<O> as output, but there
	// is a map BiComputer<Iterable<I>, Computer<I, O>, Iterable<O>>. The matcher
	// can cast the input SlicesII to an Iterable because that is a widening cast,
	// but it cannot do the same with the output. However this wi
	@OpDependency(name = "map")
	private BiComputer<SlicesII<I>, Computer<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>>, IterableInterval<RandomAccessibleInterval<O>>> mapper;

	@Override
	public void compute(final RandomAccessibleInterval<I> input,
			final Computer<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>> op, final int[] axisIndices,
			final Boolean dropSingleDimensions, final RandomAccessibleInterval<O> output) {
		mapper.compute(new SlicesII<>(input, axisIndices, dropSingleDimensions), op,
				new SlicesII<>(output, axisIndices, dropSingleDimensions));
	}

}
