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

package org.scijava.code.lsp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

import org.scijava.code.api.EnvironmentContext;
import org.scijava.log.Logger;

/**
 * The language servers of one kind (see {@link LspServerSpec}), one per
 * environment scripts run in: started when first needed (in the background),
 * restarted if they stop, and stopped when unused for a while or when too many
 * run.
 *
 * @author Gabriel Selzer
 */
public final class LspServers {

	/** How long to wait for a server to initialize, in milliseconds. */
	private static final long INIT_TIMEOUT = 60_000;

	/** How long to wait before starting a server that failed again, in ms. */
	private static final long RETRY_DELAY = 30_000;

	private final LspServerSpec spec;
	private final Logger log;
	private final long idle;
	private final int max;
	private final LongSupplier clock;

	/** The servers, by environment; least recently used first. */
	private final Map<EnvironmentContext, Entry> servers = new LinkedHashMap<>(
		16, 0.75f, true);

	/** Starts servers, one at a time, off the callers' threads. */
	private final ExecutorService starter;

	/** Stops idle servers. */
	private final ScheduledExecutorService reaper;

	/**
	 * @param spec How to start a server.
	 * @param log Where to report problems.
	 * @param idle How long a server may go unused before it is stopped, in ms.
	 * @param max How many servers may run at once.
	 */
	public LspServers(final LspServerSpec spec, final Logger log,
		final long idle, final int max)
	{
		this(spec, log, idle, max, System::currentTimeMillis);
	}

	LspServers(final LspServerSpec spec, final Logger log, final long idle,
		final int max, final LongSupplier clock)
	{
		this.spec = spec;
		this.log = log;
		this.idle = idle;
		this.max = max;
		this.clock = clock;
		final String name = "scijava-lsp-" + spec.languageId();
		starter = Executors.newSingleThreadExecutor(daemon(name));
		reaper = Executors.newSingleThreadScheduledExecutor(daemon(name +
			"-reaper"));
		final long period = Math.max(1000, idle / 4);
		reaper.scheduleWithFixedDelay(this::stopIdle, period, period,
			TimeUnit.MILLISECONDS);
	}

	/**
	 * Gets the server for scripts running in the given environment, starting
	 * it if needed. Never blocks.
	 *
	 * @return The server, once started; fails if it could not be.
	 */
	public synchronized CompletableFuture<LspServer> server(
		final EnvironmentContext env)
	{
		final Entry known = servers.get(env);
		if (known != null && known.usable(clock.getAsLong())) return known.server;
		if (known != null) stop(known);

		final Entry entry = new Entry();
		entry.server = CompletableFuture.supplyAsync(() -> {
			try {
				log.debug("Starting " + spec.languageId() + " language server for " +
					env);
				return LspServer.start(spec, env, log, INIT_TIMEOUT);
			}
			catch (final Exception exc) {
				log.warn("Could not start " + spec.languageId() +
					" language server for " + env, exc);
				throw new CompletionException(exc);
			}
		}, starter);
		entry.server.whenComplete((s, error) -> {
			if (error != null) entry.failed = clock.getAsLong();
		});
		servers.put(env, entry);

		// Too many? Stop the least recently used.
		final Iterator<Entry> it = servers.values().iterator();
		while (servers.size() > max && it.hasNext()) {
			final Entry eldest = it.next();
			it.remove();
			stop(eldest);
		}
		return entry.server;
	}

	/** Calls {@code action} on each running server. */
	public void forEach(final Consumer<LspServer> action) {
		final List<Entry> entries;
		synchronized (this) {
			entries = new ArrayList<>(servers.values());
		}
		for (final Entry e : entries) {
			final LspServer s = e.server.getNow(null);
			if (s != null) action.accept(s);
		}
	}

	/** Stops all servers. */
	public synchronized void shutdown() {
		for (final Entry e : servers.values()) stop(e);
		servers.clear();
		reaper.shutdownNow();
		starter.shutdownNow();
	}

	/** Stops the servers unused for too long. */
	synchronized void stopIdle() {
		final long now = clock.getAsLong();
		final Iterator<Entry> it = servers.values().iterator();
		while (it.hasNext()) {
			final Entry e = it.next();
			final LspServer s = e.server.isDone() && !e.server
				.isCompletedExceptionally() ? e.server.join() : null;
			if (s != null && now - s.lastUsed() > idle) {
				log.debug("Stopping idle " + spec.languageId() + " language server");
				it.remove();
				stop(e);
			}
		}
	}

	// -- Helper methods --

	private void stop(final Entry e) {
		// NB: Stop it once started, should it still be starting.
		e.server.thenAcceptAsync(LspServer::shutdown, starter);
	}

	private static java.util.concurrent.ThreadFactory daemon(final String name) {
		return r -> {
			final Thread t = new Thread(r, name);
			t.setDaemon(true);
			return t;
		};
	}

	private final class Entry {

		private CompletableFuture<LspServer> server;
		private volatile long failed = -1;

		/** Whether the server is starting or running, or failed too recently. */
		private boolean usable(final long now) {
			if (!server.isDone()) return true;
			if (server.isCompletedExceptionally()) return now - failed < RETRY_DELAY;
			return server.join().isAlive();
		}
	}
}
