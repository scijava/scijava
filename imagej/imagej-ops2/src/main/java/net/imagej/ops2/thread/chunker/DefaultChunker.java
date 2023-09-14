/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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

package net.imagej.ops2.thread.chunker;

import java.util.ArrayList;
import java.util.List;

import org.scijava.concurrent.Parallelization;
import org.scijava.function.Inplaces;

/**
 * Simple default implementation of a {@link ChunkerOp}. The list of
 * elements is chunked into equally sized (besides the last one), disjoint
 * chunks, which are processed in parallel. The stepSize is set to one, i.e.
 * each chunk consists of consecutive elements.
 * 
 * @author Christian Dietz (University of Konstanz)
 *@implNote op names='thread.chunker'
 */
public class DefaultChunker implements Inplaces.Arity2_1<Chunk, Long> {

	private final int STEP_SIZE = 1;

	/**
	 * TODO
	 *
	 * @param chunk
	 * @param numberOfElements
	 */
	@Override
	public void mutate(final Chunk chunk, final Long numberOfElements) {

		// TODO: is there a better way to determine the optimal chunk size?
		
		final long numSteps = Math.max(1, 
			(long) (numberOfElements / Runtime.getRuntime().availableProcessors())) ;
		
		final int numChunks = (int) (numberOfElements / numSteps);

		List<Runnable> list = new ArrayList<>();

		for (int i = 0; i < numChunks - 1; i++) {
			final long j = i;
			list.add(() -> chunk.execute(j * numSteps, STEP_SIZE, numSteps));
		}

		// last chunk additionally add the rest of elements
		list.add(() -> chunk.execute((numChunks - 1) * numSteps, STEP_SIZE, (int) (numSteps + (numberOfElements % numSteps))));

		Parallelization.getTaskExecutor().runAll(list);
	}

}
