/*-
 * #%L
 * Language Server Protocol bridge for SciJava code intelligence.
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

package org.scijava.code.lsp.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import org.eclipse.lsp4j.services.LanguageServer;
import org.scijava.code.lsp.Environment;
import org.scijava.log.Logger;

/**
 * The language server processes of one kind (see {@link ServerLauncher}), one
 * per environment scripts run in: started when first needed (in the
 * background), restarted if they stop, and stopped when unused for a while or
 * when too many run. Each is seen as a {@link LanguageServer} of its own via
 * {@link #server}, which knows only its own environment's process (its
 * {@link Slot}).
 *
 * @author Gabriel Selzer
 */
final class ServerProcesses {

	/** How long to wait for a server to initialize, in milliseconds. */
	private static final long INIT_TIMEOUT = 60_000;

	/** How long to wait before starting a server that failed again, in ms. */
	private static final long RETRY_DELAY = 30_000;

	/** How long a server may go unused before it stops, by default, in ms. */
	private static final long IDLE = Long.getLong("scijava.lsp.idle", 10 * 60 *
		1000);

	/** How many servers may run at once, by default. */
	private static final int MAX = Integer.getInteger("scijava.lsp.max", 4);

	private final ServerLauncher launcher;
	private final Logger log;
	private final long idle;
	private final int max;
	private final LongSupplier clock;

	/**
	 * The slots with a process, starting or started, by environment. NB: Only
	 * those, lest an environment (e.g. a live interpreter's context) be kept
	 * after its process stops.
	 */
	private final Map<Environment, Slot> slots = new HashMap<>();

	/** Starts servers, one at a time, off the callers' threads. */
	private final ExecutorService starter;

	/** Stops idle servers. */
	private final ScheduledExecutorService reaper;

	/**
	 * Servers stopped after ten minutes unused, four at most (or as the system
	 * properties {@code scijava.lsp.idle}, in ms, and {@code scijava.lsp.max}
	 * say).
	 *
	 * @param launcher How to start a server.
	 * @param log Where to report problems.
	 */
	ServerProcesses(final ServerLauncher launcher, final Logger log) {
		this(launcher, log, IDLE, MAX, System::currentTimeMillis);
	}

	ServerProcesses(final ServerLauncher launcher, final Logger log,
		final long idle, final int max, final LongSupplier clock)
	{
		this.launcher = launcher;
		this.log = log;
		this.idle = idle;
		this.max = max;
		this.clock = clock;
		final String name = "scijava-lsp-" + launcher.language();
		starter = Executors.newSingleThreadExecutor(daemon(name));
		reaper = Executors.newSingleThreadScheduledExecutor(daemon(name +
			"-reaper"));
		final long period = Math.max(1000, idle / 4);
		reaper.scheduleWithFixedDelay(this::stopIdle, period, period,
			TimeUnit.MILLISECONDS);
	}

	/**
	 * Gets a server for scripts running in the given environment: the server
	 * process of that environment, started when first asked (and again if it
	 * stops), seen as a {@link LanguageServer} of its own. Requests fail while
	 * the process cannot be started. Shutting it down closes its documents,
	 * but leaves the process (see {@link #shutdown}).
	 */
	LanguageServer server(final Environment env) {
		return new ProcessLanguageServer(slot(env));
	}

	/** Gets the slot of the given environment's process. */
	synchronized Slot slot(final Environment env) {
		final Slot known = slots.get(env);
		return known != null ? known : new Slot(env);
	}

	/** Stops all servers. */
	synchronized void shutdown() {
		reaper.shutdownNow();
		for (final Slot slot : slots.values()) {
			final CompletableFuture<ServerProcess> s = slot.server;
			if (s == null) continue;
			// NB: Not via the starter, which stops next (dropping what it has not
			// begun): those started, now; those starting, once started.
			if (s.isDone()) s.thenAccept(ServerProcess::shutdown);
			else s.thenAcceptAsync(ServerProcess::shutdown);
			slot.server = null;
		}
		slots.clear();
		starter.shutdownNow();
	}

	/** Stops the servers unused for too long. */
	synchronized void stopIdle() {
		final long now = clock.getAsLong();
		for (final Slot slot : new ArrayList<>(slots.values())) {
			final ServerProcess s = slot.started();
			if (s != null && now - s.lastUsed() > idle) {
				log.debug("Stopping idle " + launcher.language() +
					" language server for " + slot.env);
				slot.stop();
			}
		}
	}

	// -- Helper methods --

	/** Too many running or starting? Stops the least recently used. */
	private void evict(final Slot keep) {
		final List<Slot> live = new ArrayList<>();
		for (final Slot slot : slots.values()) {
			final ServerProcess s = slot.started();
			final boolean starting = slot.server != null && !slot.server.isDone();
			if (starting || s != null && s.isAlive()) live.add(slot);
		}
		while (live.size() > max) {
			Slot eldest = null;
			for (final Slot slot : live) {
				if (slot == keep) continue;
				if (eldest == null || slot.lastUsed() < eldest.lastUsed()) {
					eldest = slot;
				}
			}
			if (eldest == null) return;
			live.remove(eldest);
			eldest.stop();
		}
	}

	private static java.util.concurrent.ThreadFactory daemon(final String name) {
		return r -> {
			final Thread t = new Thread(r, name);
			t.setDaemon(true);
			return t;
		};
	}

	/**
	 * An indirect reference to a {@link ServerProcess}.
	 * <p>
	 * NB: Its state is guarded by the {@link ServerProcesses}.
	 * </p>
	 */
	final class Slot {

		private final Environment env;

		/** The current process, started or starting; null if none. */
		private CompletableFuture<ServerProcess> server;

		/** When the current process failed to start; -1 if it did not. */
		private long failed = -1;

		private Slot(final Environment env) {
			this.env = env;
		}

		/**
		 * Gets the process, starting it if needed. Never blocks.
		 *
		 * @return The process, once started; fails if it could not be.
		 */
		CompletableFuture<ServerProcess> process() {
			synchronized (ServerProcesses.this) {
				// NB: Replaced while this one had no process? Ask that one.
				final Slot known = slots.get(env);
				if (known != null && known != this) return known.process();
				if (server != null && usable(clock.getAsLong())) return server;
				stop();
				start();
				slots.put(env, this);
				evict(this);
				return server;
			}
		}

		/** Gets the process, if started and alive. Never starts one. */
		Optional<ServerProcess> running() {
			synchronized (ServerProcesses.this) {
				final Slot known = slots.get(env);
				if (known != null && known != this) return known.running();
				final ServerProcess s = started();
				return s != null && s.isAlive() ? Optional.of(s) : Optional.empty();
			}
		}

		/** Stops the process, once started, should it still be starting. */
		private void stop() {
			slots.remove(env, this);
			if (server == null) return;
			server.thenAcceptAsync(ServerProcess::shutdown, starter);
			server = null;
		}

		private void start() {
			failed = -1;
			final CompletableFuture<ServerProcess> s = CompletableFuture
				.supplyAsync(() -> {
					try {
						log.debug("Starting " + launcher.language() +
							" language server for " + env);
						return ServerProcess.start(launcher, env, log, INIT_TIMEOUT);
					}
					catch (final Exception exc) {
						log.warn("Could not start " + launcher.language() +
							" language server for " + env, exc);
						throw new CompletionException(exc);
					}
				}, starter);
			// NB: Before the callback, which runs at once if the start failed.
			server = s;
			s.whenComplete((started, error) -> {
				if (error == null) return;
				synchronized (ServerProcesses.this) {
					if (server != s) return;
					failed = clock.getAsLong();
					// NB: Holds no process. Its users still wait out RETRY_DELAY.
					slots.remove(env, this);
				}
			});
		}

		/** The process, if started (successfully); null otherwise. */
		private ServerProcess started() {
			return server != null && server.isDone() && !server
				.isCompletedExceptionally() ? server.join() : null;
		}

		/** When last used; one starting counts as just used. */
		private long lastUsed() {
			final ServerProcess s = started();
			return s != null ? s.lastUsed() : Long.MAX_VALUE;
		}

		/** Whether the process is starting or running, or failed too recently. */
		private boolean usable(final long now) {
			if (!server.isDone()) return true;
			if (server.isCompletedExceptionally()) {
				return failed < 0 || now - failed < RETRY_DELAY;
			}
			return server.join().isAlive();
		}
	}
}
