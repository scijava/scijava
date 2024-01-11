/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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

package org.scijava.ops.image.slice;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

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
 * @implNote op names='slice', priority='10000.'
 */
public class SliceRAI2RAI<I, O> implements
		Computers.Arity4<RandomAccessibleInterval<I>, Computers.Arity1<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>>, int[], Boolean, RandomAccessibleInterval<O>> {

	/**
	 * TODO
	 *
	 * @param input
	 * @param Op
	 * @param axisIndices
	 * @param dropSingleDimensions
	 * @param output
	 */
	@Override
	public void compute( //
			final RandomAccessibleInterval<I> input, //
			final Computers.Arity1<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>> op, //
			final int[] axisIndices, //
			@Nullable Boolean dropSingleDimensions, //
			final RandomAccessibleInterval<O> output //
	) {
		if (dropSingleDimensions == null) {
			dropSingleDimensions = true;
		}

		SlicesII<I> slicedInput = new SlicesII<>(input, axisIndices, dropSingleDimensions);
		SlicesII<O> slicedOutput = new SlicesII<>(output, axisIndices, dropSingleDimensions);
		
		//TODO: can we make this more efficient?
		Cursor<RandomAccessibleInterval<I>> slicedInputCursor = slicedInput.cursor();
		Cursor<RandomAccessibleInterval<O>> slicedOutputCursor = slicedOutput.cursor();
		while(slicedInputCursor.hasNext()) {
			op.compute(slicedInputCursor.next(), slicedOutputCursor.next());
		}
	}

}
