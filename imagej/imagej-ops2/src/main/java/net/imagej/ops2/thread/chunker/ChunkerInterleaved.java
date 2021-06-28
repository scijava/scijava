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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.scijava.Priority;
import org.scijava.function.Inplaces;
import org.scijava.ops.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Implementation of a {@link ChunkerOp} that interleaves the chunks. In a
 * element enumeration from 1..n with <b>k</b> {@link Chunk}s the
 * first one will process the elements 1, k+1, 2k+1, ... the second chunk
 * executable 2, k+2, 2k+2 and so on.
 * 
 * @author Michael Zinsmaier (University of Konstanz)
 */
@Plugin(type = Op.class, name = "thread.chunker", priority = Priority.VERY_LOW)
public class ChunkerInterleaved implements Inplaces.Arity3_1<Chunk, Long, ExecutorService>{

	private String cancellationMsg;

	/**
	 * TODO
	 *
	 * @param chunk
	 * @param numberOfElements
	 * @param executorService
	 */
	@Override
	public void mutate(final Chunk chunk, final Long numberOfElements, final ExecutorService es) {

		final int numThreads = Runtime.getRuntime().availableProcessors();
		final int numStepsFloor = (int) (numberOfElements / numThreads);
		final int remainder = numberOfElements.intValue() - (numStepsFloor * numThreads);

		final ArrayList<Future<?>> futures = new ArrayList<>(numThreads);

		for (int i = 0; i < numThreads; i++) {
			final int j = i;

			futures.add(es.submit(new Runnable() {

				@Override
				public void run() {
					if (j < remainder) {
						chunk.execute(j, numThreads, (numStepsFloor + 1));
					}
					else {
						chunk.execute(j, numThreads, numStepsFloor);
					}
				}
			}));
		}

		for (final Future<?> future : futures) {
			try {
				future.get();
			}
			catch (final Exception e) {
				cancellationMsg = e.getMessage();
				break;
			}
		}
	}

}
