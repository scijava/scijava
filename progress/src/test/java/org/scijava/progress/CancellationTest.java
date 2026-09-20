/*-
 * #%L
 * An interrupt-based subsystem for progress reporting.
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

package org.scijava.progress;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

/**
 * Tests that cancellation works the way the platform does it: a caller
 * cancels a {@link Future}, which interrupts the worker, and progress
 * reporting is where the work notices.
 *
 * @author Curtis Rueden
 */
public class CancellationTest {

	/** A long-running "op": a plain Function, which cannot throw. */
	private static class CountingOp implements Function<Long, Long> {

		final CountDownLatch started = new CountDownLatch(1);
		volatile long completed = 0;

		@Override
		public Long apply(final Long iterations) {
			Progress.register(this);
			Progress.defineTotal(iterations);
			for (long i = 0; i < iterations; i++) {
				// NB: the checkpoint is free, because this op already reports.
				Progress.update();
				completed = i + 1;
				started.countDown();
				try {
					Thread.sleep(1);
				}
				catch (final InterruptedException exc) {
					Thread.currentThread().interrupt();
				}
			}
			Progress.complete();
			return completed;
		}
	}

	/** Future.cancel(true) stops an op that reports progress. */
	@Test
	public void testFutureCancelStopsTheOp() throws Exception {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			final CountingOp op = new CountingOp();
			final Future<Long> future = executor.submit(() -> op.apply(1_000_000L));
			assertTrue(op.started.await(5, TimeUnit.SECONDS));

			assertTrue(future.cancel(true));
			assertThrows(CancellationException.class, () -> future.get(5,
				TimeUnit.SECONDS));

			// The op stopped early rather than running a million iterations.
			Thread.sleep(50);
			final long stoppedAt = op.completed;
			assertTrue(stoppedAt < 1_000_000L, "op ran to completion");
		}
		finally {
			executor.shutdownNow();
		}
	}

	/** The checkpoint throws, and leaves the interrupt flag set. */
	@Test
	public void testCheckCancellationRestoresTheFlag() throws Exception {
		final AtomicReference<CancellationException> thrown =
			new AtomicReference<>();
		final AtomicBoolean flagStillSet = new AtomicBoolean();
		final Thread worker = new Thread(() -> {
			Thread.currentThread().interrupt();
			try {
				Progress.checkCancellation();
			}
			catch (final CancellationException exc) {
				thrown.set(exc);
				flagStillSet.set(Thread.currentThread().isInterrupted());
			}
		});
		worker.start();
		worker.join(5000);

		assertNotNull(thrown.get(), "checkCancellation did not throw");
		assertTrue(flagStillSet.get(), "the interrupt flag must be restored");
	}

	/** An uninterrupted thread passes the checkpoint untouched. */
	@Test
	public void testCheckCancellationIsANoOpNormally() {
		Progress.checkCancellation();
		assertFalse(Thread.currentThread().isInterrupted());
	}

	/**
	 * An op cancelled outside a Future still aborts, and the exception reaches
	 * the caller rather than being swallowed.
	 */
	@Test
	public void testDirectInterruptionAborts() throws Exception {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			final CountingOp op = new CountingOp();
			final AtomicReference<Thread> workerThread = new AtomicReference<>();
			final Future<Long> future = executor.submit(() -> {
				workerThread.set(Thread.currentThread());
				return op.apply(1_000_000L);
			});
			assertTrue(op.started.await(5, TimeUnit.SECONDS));

			// NB: interrupt the thread directly, without cancelling the future.
			workerThread.get().interrupt();

			final ExecutionException exc = assertThrows(ExecutionException.class,
				() -> future.get(5, TimeUnit.SECONDS));
			assertTrue(exc.getCause() instanceof CancellationException,
				"expected a CancellationException, got: " + exc.getCause());
		}
		finally {
			executor.shutdownNow();
		}
	}
}
