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

package org.scijava.ops.image.stats;

import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Op;

/**
 * Op to calculate the {@code stats.sum}.
 *
 * @author Gabriel Selzer
 * @param <I> input type
 * @param <O> output type
 * @implNote op names='stats.sum', priority='100.'
 */
public class DefaultSum<I extends RealType<I>, O extends RealType<O>> implements
	Computers.Arity1<RandomAccessibleInterval<I>, O>
{

	/**
	 * TODO
	 *
	 * @param input
	 * @param sum
	 */
	@Override
	public void compute(final RandomAccessibleInterval<I> input, final O sum) {
		sum.setZero();
        var chunkSums = LoopBuilder.setImages(input).multiThreaded()
			.forEachChunk(chunk -> {
                var chunkSum = sum.createVariable();
				chunkSum.setZero();
				chunk.forEachPixel(pixel -> chunkSum.setReal(chunkSum.getRealDouble() +
					pixel.getRealDouble()));
				return chunkSum;
			});

		for (var chunkSum : chunkSums)
			sum.add(chunkSum);
	}
}
