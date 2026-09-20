/*-
 * #%L
 * SciJava library facilitating consistent parallel processing.
 * %%
 * Copyright (C) 2021 - 2025 SciJava developers.
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

package org.scijava.concurrent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

/**
 * Tests that cancelling a thread which farmed work out to a
 * {@link TaskExecutor} reaches the workers, and is still visible afterward.
 * <p>
 * The interrupt flag does not inherit to worker threads, so this propagation
 * is deliberate rather than automatic.
 * </p>
 *
 * @author Curtis Rueden
 */
public class CancellationTest {

	/**
	 * Interrupting the calling thread cancels the workers, surfaces as a
	 * {@link CancellationException}, and leaves the caller's interrupt flag set.
	 */
	@Test
	public void testInterruptionReachesWorkers() throws Exception {
		final CountDownLatch allStarted = new CountDownLatch(4);
		final AtomicInteger interruptedWorkers = new AtomicInteger();
		final AtomicReference<RuntimeException> thrown = new AtomicReference<>();
		final AtomicBoolean flagStillSet = new AtomicBoolean();

		final Thread caller = new Thread(() -> {
			try (final TaskExecutor executor = TaskExecutors.fixedThreadPool(4)) {
				executor.forEach(List.of(1, 2, 3, 4), i -> {
					allStarted.countDown();
					try {
						// NB: long enough that the interrupt lands mid-flight.
						Thread.sleep(30_000);
					}
					catch (final InterruptedException exc) {
						interruptedWorkers.incrementAndGet();
						Thread.currentThread().interrupt();
					}
				});
			}
			catch (final RuntimeException exc) {
				thrown.set(exc);
				flagStillSet.set(Thread.currentThread().isInterrupted());
			}
		});
		caller.start();
		assertTrue(allStarted.await(10, TimeUnit.SECONDS), "workers never started");

		caller.interrupt();
		caller.join(10_000);

		final RuntimeException exc = thrown.get();
		assertNotNull(exc, "the caller was not cancelled");
		assertTrue(exc instanceof CancellationException,
			"expected CancellationException, got: " + exc);
		assertTrue(flagStillSet.get(),
			"the caller's interrupt flag must be restored");
		assertTrue(interruptedWorkers.get() > 0,
			"cancellation never reached the workers");
	}
}
