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

package org.scijava.ops.image.thread.chunker;

import java.util.ArrayList;
import java.util.List;

import org.scijava.concurrent.Parallelization;
import org.scijava.function.Inplaces;

/**
 * Implementation of a {@link ChunkerOp} that interleaves the chunks. In a
 * element enumeration from 1..n with <b>k</b> {@link Chunk}s the first one will
 * process the elements 1, k+1, 2k+1, ... the second chunk executable 2, k+2,
 * 2k+2 and so on.
 *
 * @author Michael Zinsmaier (University of Konstanz)
 * @implNote op names='thread.chunker', priority='-10000.'
 */
public class ChunkerInterleaved implements Inplaces.Arity2_1<Chunk, Long> {

	private String cancellationMsg;

	/**
	 * TODO
	 *
	 * @param chunk
	 * @param numberOfElements
	 */
	@Override
	public void mutate(final Chunk chunk, final Long numberOfElements) {

		final int numThreads = Runtime.getRuntime().availableProcessors();
		final int numStepsFloor = (int) (numberOfElements / numThreads);
		final int remainder = numberOfElements.intValue() - (numStepsFloor *
			numThreads);

		final List<Runnable> list = new ArrayList<>(numThreads);

		for (int i = 0; i < numThreads; i++) {
			final int j = i;

			list.add(() -> {

				if (j < remainder) {
					chunk.execute(j, numThreads, (numStepsFloor + 1));
				}
				else {
					chunk.execute(j, numThreads, numStepsFloor);
				}
			});
		}

		Parallelization.getTaskExecutor().runAll(list);
	}

}
