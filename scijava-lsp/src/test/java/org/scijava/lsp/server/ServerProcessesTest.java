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

package org.scijava.lsp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.scijava.lsp.server.FakeServers.ENV;
import static org.scijava.lsp.server.FakeServers.waitFor;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.lsp.Environment;
import org.scijava.log.StderrLogService;

/**
 * Tests {@link ServerProcesses}.
 *
 * @author Gabriel Selzer
 */
public class ServerProcessesTest {

	private final FakeServers.Launcher launcher = new FakeServers.Launcher();
	private final List<FakeServers.Server> started = launcher.started;
	// NB: Servers note their use by the system clock.
	private final AtomicLong now = new AtomicLong(System.currentTimeMillis());
	private ServerProcesses servers;

	@BeforeEach
	public void setUp() {
		servers = new ServerProcesses(launcher, new StderrLogService(), 60_000, 2,
			now::get);
	}

	@AfterEach
	public void tearDown() {
		if (servers != null) servers.shutdown();
	}

	/** Tests that one process is made per environment, shared by its slots. */
	@Test
	public void testShared() throws Exception {
		final ServerProcesses.Slot slot = servers.slot(ENV);
		final ServerProcesses.Slot anotherSlot = servers.slot(ENV);
		final ServerProcess first = slot.process().get(10, TimeUnit.SECONDS);
		assertSame(first, anotherSlot.process().get(10, TimeUnit.SECONDS));
		assertEquals(1, started.size());
	}

	/** Tests that a process that dies is replaced on the next request. */
	@Test
	public void testRestart() throws Exception {
		final ServerProcesses.Slot slot = servers.slot(ENV);
		final ServerProcess first = slot.process().get(10, TimeUnit.SECONDS);
		// If the server dies...
		started.get(0).alive = false;
		assertFalse(slot.running().isPresent());
		// ...the next request starts another.
		final ServerProcess second = slot.process().get(10, TimeUnit.SECONDS);
		assertNotSame(first, second);
		assertEquals(2, started.size());
	}

	/**
	 * Tests that a process unused for long is stopped, and not started again
	 * until asked.
	 */
	@Test
	public void testIdle() throws Exception {
		final ServerProcesses.Slot slot = servers.slot(ENV);
		final ServerProcess first = slot.process().get(10, TimeUnit.SECONDS);
		// Pretend a bunch of time passed so the server will seem idle
		now.addAndGet(120_000);
		servers.stopIdle();
		// Wait for the server to be shut down...
		waitFor(() -> started.get(0).shutdown);
		assertFalse(slot.running().isPresent());
		// ...and not started again until asked...
		assertEquals(1, started.size());
		// ...and then assert a new server starts up
		assertNotSame(first, slot.process().get(10, TimeUnit.SECONDS));
	}

	/**
	 * Tests that a slot whose process stopped is replaced: a new slot takes
	 * over its environment, and users of the old slot share the new slot's
	 * process, rather than starting a second one.
	 */
	@Test
	public void testStaleSlotReplaced() throws Exception {
		final ServerProcesses.Slot old = servers.slot(ENV);
		final ServerProcess first = old.process().get(10, TimeUnit.SECONDS);
		now.addAndGet(120_000);
		servers.stopIdle();
		// A new slot makes a new process...
		final ServerProcesses.Slot fresh = servers.slot(ENV);
		assertNotSame(old, fresh);
		final ServerProcess second = fresh.process().get(10, TimeUnit.SECONDS);
		assertNotSame(first, second);
		// ...which is then available from the old slot, without a third one
		assertSame(second, old.process().get(10, TimeUnit.SECONDS));
		assertEquals(2, started.size());
	}

	/**
	 * Tests that a process that fails to start is not tried again until the
	 * retry delay has passed: requests until then get the same failure.
	 */
	@Test
	public void testRetry() throws Exception {
		// Create a ServerProcesses whose starts always fail, counting the tries
		final AtomicLong tries = new AtomicLong();
		final ServerProcesses failing = new ServerProcesses(
			new FakeServers.Launcher()
		{

			@Override
			public Process start(final Environment env) throws IOException {
				tries.incrementAndGet();
				throw new IOException("No such server");
			}
		}, new StderrLogService(), 60_000, 2, now::get);
		try {
			// Ask for a ServerProcess, get a failure
			final ServerProcesses.Slot slot = failing.slot(ENV);
			final CompletableFuture<ServerProcess> failed = slot.process();
			assertThrows(ExecutionException.class, () -> failed.get(10,
				TimeUnit.SECONDS));
			assertEquals(1, tries.get());

			// Ask again, get another failure...
			assertSame(failed, slot.process());
			// ...but make sure we didn't actually try.
			assertEquals(1, tries.get());

			// After waiting for the retry delay...
			now.addAndGet(60_000);
			// ...try again...
			assertThrows(ExecutionException.class, () -> slot.process().get(10,
				TimeUnit.SECONDS));
			// ...and we should see another try
			assertEquals(2, tries.get());
		}
		finally {
			failing.shutdown();
		}
	}

	/**
	 * Tests that starting a process beyond the maximum (here, two) stops the
	 * least recently used one, and only that one.
	 */
	@Test
	public void testTooMany() throws Exception {
		final ServerProcesses.Slot a = servers.slot(ENV);
		final ServerProcesses.Slot b = servers.slot(ENV.withPaths(Collections
			.singletonList("/b")));
		final ServerProcesses.Slot c = servers.slot(ENV.withPaths(Collections
			.singletonList("/c")));
		final ServerProcess first = a.process().get(10, TimeUnit.SECONDS);
		Thread.sleep(20);
		b.process().get(10, TimeUnit.SECONDS);
		Thread.sleep(20);
		first.remote(); // NB: Used: now b is the least recently used.
		c.process().get(10, TimeUnit.SECONDS);
		// Starting the third process shuts down the second process
		waitFor(() -> started.get(1).shutdown);
		assertFalse(b.running().isPresent());
		// But the first process should still be running
		assertFalse(started.get(0).shutdown);
		assertSame(first, a.running().get());
	}

	/**
	 * Tests that shutting down fails, at once, the processes still starting,
	 * and those waiting to start: rather than leaving their users waiting.
	 */
	@Test
	public void testShutdownFailsStarting() throws Exception {
		final FakeServers.DelayingLauncher delaying =
			new FakeServers.DelayingLauncher();
		final ServerProcesses processes = new ServerProcesses(delaying,
			new StderrLogService(), 60_000, 2, now::get);
		// One process starts. Once it is starting, it won't finish (since we
		// never call DelayedFakeServer.finishInitializing), which means...
		final CompletableFuture<ServerProcess> starting = processes.slot(ENV)
			.process();
		delaying.awaitStart();
		// ...starting a second will wait its turn.
		final CompletableFuture<ServerProcess> waiting = processes.slot(ENV
			.withPaths(Collections.singletonList("/b"))).process();
		// Now shut down all processes.
		processes.shutdown();
		// Both should fail, at once.
		assertThrows(CancellationException.class, () -> waiting.getNow(null));
		assertThrows(CancellationException.class, () -> starting.getNow(null));
		// NB: The one waiting was never started at all.
		assertEquals(1, delaying.started.size());
	}

	/**
	 * Tests that shutting down stops a process still starting, rather than
	 * leaving it running.
	 */
	@Test
	public void testShutdownStopsStarting() throws Exception {
		final FakeServers.DelayingLauncher delaying =
			new FakeServers.DelayingLauncher();
		final ServerProcesses delayed = new ServerProcesses(delaying,
			new StderrLogService(), 60_000, 2, now::get);
		delayed.slot(ENV).process();
		final FakeServers.DelayedFakeServer late = delaying.awaitStart();
		delayed.shutdown();
		// NB: Stopped while it waits to be initialized, by the interruption of
		// its start; or, should it finish starting anyway, once it has.
		late.finishInitializing();
		waitFor(() -> !late.alive);
	}

	/** Tests that once shut down, no process is started: asking fails. */
	@Test
	public void testNoneAfterShutdown() throws Exception {
		final ServerProcesses.Slot slot = servers.slot(ENV);
		servers.shutdown();
		assertThrows(CancellationException.class, () -> slot.process().getNow(
			null));
		assertTrue(started.isEmpty());
	}

	/**
	 * Tests that a process that fails to initialize is stopped, and that
	 * any children it started are stopped as well.
	 */
	@Test
	public void testNestedProcessDestruction() throws Exception {
		final NestedServerLauncher nested = new NestedServerLauncher();
		try {
			// NB: The wrapper never answers, so initializing it times out. Started
			// directly, as ServerProcesses waits a minute for that.
			assertThrows(
				TimeoutException.class,
				() -> ServerProcess.start(nested, ENV, new StderrLogService(), 1000)
			);
			waitFor(() -> !nested.wrapper.isAlive());
			waitFor(() -> !nested.child.isAlive());
		}
		finally {
			// NB: Whatever happened, leave nothing running.
			if (nested.child != null) nested.child.destroyForcibly();
			if (nested.wrapper != null) nested.wrapper.destroyForcibly();
		}
	}

	/**
	 * Starts a wrapper (see {@link Wrapper}) {@link Process} which starts a child
	 * of its own.
	 */
	static class NestedServerLauncher implements ServerLauncher {

		volatile ProcessHandle wrapper, child;

		@Override
		public String language() {
			return "python";
		}

		@Override
		public Process start(final Environment env) throws IOException {
			// Starts a JVM process running the main class of Wrapper with a
			// "wrap" arg
			final Process process = new ProcessBuilder(java(), "-cp", System
				.getProperty("java.class.path"), Wrapper.class.getName(), "wrap")
					.redirectError(ProcessBuilder.Redirect.DISCARD).start();
			wrapper = process.toHandle();
			try {
				// Wait until the wrapper starts up its child to return.
				// NB: Notes the child now: once the wrapper is gone, it is no one's
				// descendant, so it could not be found to check.
				waitFor(() -> javaChild(process).isPresent());
			}
			catch (final InterruptedException exc) {
				throw new IOException(exc);
			}
			child = javaChild(process).get();
			return process;
		}

		/**
		 * The child the wrapper started, if yet. NB: Not e.g. the console host
		 * Windows starts for it too.
		 */
		private static Optional<ProcessHandle> javaChild(final Process process) {
			return process.children().filter(h -> h.info().command().map(c -> c
				.toLowerCase().matches(".*[\\\\/]java(\\.exe)?")).orElse(false))
				.findAny();
		}
	}

	/**
	 * A process that sleeps forever; given {@code wrap}, it first starts another
	 * (which just sleeps), sharing its input and output.
	 */
	public static class Wrapper {

		public static void main(final String... args) throws Exception {
			if (args.length > 0 && args[0].equals("wrap")) {
				// If asked to wrap start up another instance.
				new ProcessBuilder(
					 java(),
					"-cp",
					System.getProperty("java.class.path"),
					Wrapper.class.getName()
				).inheritIO().start();
			}
			// This Process does nothing (specifically, it DOESN'T speak LSP)
			Thread.sleep(Long.MAX_VALUE);
		}
	}

	/** The java executable of this JVM. */
	private static String java() {
		return Paths.get(System.getProperty("java.home"), "bin", "java")
			.toString();
	}
}
