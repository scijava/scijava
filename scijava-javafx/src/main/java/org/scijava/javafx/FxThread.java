/*
 * #%L
 * JavaFX widgets, and a dialog to harvest inputs with them.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.javafx;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import javafx.application.Platform;

/**
 * Getting on and off the JavaFX application thread.
 * <p>
 * NB: this is the one place where JavaFX is meaningfully harder than Swing.
 * There is no {@code invokeAndWait}, the toolkit must be started exactly once
 * before anything touches it, and it shuts itself down when the last window
 * closes unless told otherwise - all of which a caller running a command on a
 * background thread should not have to know.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class FxThread {

	private static boolean started;

	private FxThread() {
		// prevent instantiation of utility class
	}

	/**
	 * Starts the JavaFX toolkit, if it is not already running.
	 * <p>
	 * NB: it is deliberately not {@code Application.launch}. That method takes
	 * over the process - it must be called from the main thread, and returns
	 * only when the last window closes - which is exactly wrong for a toolkit
	 * binding that an application, a script or a test may use. Starting the
	 * platform directly leaves the caller in charge.
	 * </p>
	 */
	public static synchronized void start() {
		if (started) return;
		final CountDownLatch ready = new CountDownLatch(1);
		try {
			Platform.startup(ready::countDown);
		}
		catch (final IllegalStateException exc) {
			// NB: already running, which is fine - somebody else started it.
			ready.countDown();
		}
		await(ready);
		// NB: otherwise the platform shuts down for good when the last window
		// closes, and the next dialog would never appear.
		Platform.setImplicitExit(false);
		started = true;
	}

	/** Runs the given work on the JavaFX thread, and waits for it to finish. */
	public static void runAndWait(final Runnable work) {
		get(() -> {
			work.run();
			return null;
		});
	}

	/**
	 * Runs the given work on the JavaFX thread and returns what it produced.
	 *
	 * @param <T> what the work produces
	 * @param work what to do on the JavaFX thread
	 * @return its result
	 */
	public static <T> T get(final Supplier<T> work) {
		start();
		if (Platform.isFxApplicationThread()) return work.get();
		final AtomicReference<T> result = new AtomicReference<>();
		final AtomicReference<RuntimeException> failure = new AtomicReference<>();
		final CountDownLatch done = new CountDownLatch(1);
		Platform.runLater(() -> {
			try {
				result.set(work.get());
			}
			catch (final RuntimeException exc) {
				failure.set(exc);
			}
			finally {
				done.countDown();
			}
		});
		await(done);
		if (failure.get() != null) throw failure.get();
		return result.get();
	}

	// -- Helper methods --

	private static void await(final CountDownLatch latch) {
		try {
			latch.await();
		}
		catch (final InterruptedException exc) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted waiting for JavaFX", exc);
		}
	}
}
