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

package net.imagej.ops2.thread.chunker;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.scijava.function.Inplaces;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Simple default implementation of a {@link ChunkerOp}. The list of
 * elements is chunked into equally sized (besides the last one), disjoint
 * chunks, which are processed in parallel. The stepSize is set to one, i.e.
 * each chunk consists of consecutive elements.
 * 
 * @author Christian Dietz (University of Konstanz)
 */
@Plugin(type = Op.class, name = "thread.chunker")
public class DefaultChunker implements Inplaces.Arity3_1<Chunk, Long, ExecutorService> {

	private final int STEP_SIZE = 1;

	@Override
	/**
	 * TODO
	 *
	 * @param chunk
	 * @param numberOfElements
	 * @param executorService
	 */
	public void mutate(final Chunk chunk, final Long numberOfElements, final ExecutorService es) {

		// TODO: is there a better way to determine the optimal chunk size?
		
		final long numSteps = Math.max(1, 
			(long) (numberOfElements / Runtime.getRuntime().availableProcessors())) ;
		
		final int numChunks = (int) (numberOfElements / numSteps);

		final ArrayList<Future<?>> futures = new ArrayList<>(numChunks);

		for (int i = 0; i < numChunks - 1; i++) {
			final long j = i;

		futures.add(es.submit(new Runnable() {

				@Override
				public void run() {
					chunk.execute(j * numSteps, STEP_SIZE, numSteps);
				}
			}));
		}

		// last chunk additionally add the rest of elements
		futures.add(es.submit(new Runnable() {

			@Override
			public void run() {
				chunk.execute((numChunks - 1) * numSteps, STEP_SIZE,
					(int) (numSteps + (numberOfElements % numSteps)));
			}
		}));

		for (final Future<?> future : futures) {
			try {
				future.get();
			}
			catch (final InterruptedException exc) {
				throw new RuntimeException(exc);
			}
			catch (final ExecutionException exc) {
				throw new RuntimeException(exc);
			}
		}
	}

}
