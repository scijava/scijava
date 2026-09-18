/*
 * #%L
 * AWT widgets and platform plumbing, with no Swing anywhere.
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

package org.scijava.awt;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Getting on and off the event dispatch thread.
 * <p>
 * NB: {@link EventQueue} rather than {@code SwingUtilities}, which merely
 * forwards to it. The dispatch thread belongs to AWT, so code that only needs
 * to reach it - a harvester, a status bar, a progress report - need not drag
 * in Swing to do so. This is what the Swing binding shares with the AWT one,
 * and it is the <em>only</em> kind of thing they share: no widget is common
 * between them, {@link java.awt.Choice} and {@code JComboBox} having nothing
 * to do with one another.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class Edt {

	private Edt() {
		// prevent instantiation of utility class
	}

	/** Runs the given work on the dispatch thread, and waits for it. */
	public static void runAndWait(final Runnable work) {
		get(() -> {
			work.run();
			return null;
		});
	}

	/**
	 * Runs the given work on the dispatch thread and returns what it produced.
	 *
	 * @param <T> what the work produces
	 * @param work what to do on the dispatch thread
	 * @return its result
	 * @throws IllegalStateException if the work threw, or the wait was
	 *           interrupted
	 */
	public static <T> T get(final Supplier<T> work) {
		if (EventQueue.isDispatchThread()) return work.get();
		final AtomicReference<T> result = new AtomicReference<>();
		try {
			EventQueue.invokeAndWait(() -> result.set(work.get()));
		}
		catch (final InterruptedException exc) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted waiting for the EDT", exc);
		}
		catch (final InvocationTargetException exc) {
			final Throwable cause = exc.getCause();
			if (cause instanceof RuntimeException) throw (RuntimeException) cause;
			throw new IllegalStateException(cause);
		}
		return result.get();
	}

	/** Runs the given work on the dispatch thread, without waiting. */
	public static void later(final Runnable work) {
		EventQueue.invokeLater(work);
	}
}
