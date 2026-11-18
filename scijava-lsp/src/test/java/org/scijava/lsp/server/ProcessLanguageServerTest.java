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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.scijava.lsp.server.FakeServers.ENV;
import static org.scijava.lsp.server.FakeServers.complete;
import static org.scijava.lsp.server.FakeServers.open;
import static org.scijava.lsp.server.FakeServers.waitFor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.lsp.Environment;
import org.scijava.log.StderrLogService;

/**
 * Tests {@link ProcessLanguageServer}.
 *
 * @author Gabriel Selzer
 */
public class ProcessLanguageServerTest {

	private final FakeServers.Launcher launcher = new FakeServers.Launcher();
	private final List<FakeServers.Server> started = launcher.started;
	private ServerProcesses servers;

	@BeforeEach
	public void setUp() {
		servers = new ServerProcesses(launcher, new StderrLogService(), 60_000, 2,
			System::currentTimeMillis);
	}

	@AfterEach
	public void tearDown() {
		servers.shutdown();
	}

	/**
	 * Tests that a request sends the process the document's current text
	 * first, and passes on the process's answer.
	 */
	@Test
	public void testSync() throws Exception {
		final ProcessLanguageServer s = (ProcessLanguageServer) servers.server(
			ENV);
		// Opening a script only notes its text...
		final String uri = "untitled:/l.py";
		open(s, uri, "y = !!");
		assertEquals(0, started.size());
		// ...which a request sends to the process as a question is asked.
		assertEquals(3, complete(s, uri).size());
		assertEquals("y = !!", started.get(0).texts.get(uri));
	}

	/**
	 * Tests that the process's problems with a document are published to the
	 * client, a moment after its last change, for its version.
	 */
	@Test
	public void testDiagnostics() throws Exception {
		final ProcessLanguageServer s = (ProcessLanguageServer) servers.server(
			ENV);
		// Connect a client that records all diagnostics.
		final List<PublishDiagnosticsParams> published = connect(s);
		open(s, "untitled:/l.py", "y = !!");
		// The fake server complains about the first line, and about the "!!".
		waitFor(() -> !published.isEmpty());
		assertEquals(Integer.valueOf(1), published.get(0).getVersion());
		assertEquals(2, published.get(0).getDiagnostics().size());
	}

	/** Tests that closing a document tells the process. */
	@Test
	public void testClose() throws Exception {
		final ProcessLanguageServer s = (ProcessLanguageServer) servers.server(
			ENV);
		final String uri = "untitled:/l.py";
		open(s, uri, "x");
		complete(s, uri); // NB: Starts the process.
		s.didClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(
			uri)));
		waitFor(() -> started.get(0).events.contains("close " + uri));
	}

	/**
	 * Tests that closing a document tells only its environment's process, not
	 * those of other environments with a document of the same URI.
	 */
	@Test
	public void testCloseOwnOnly() throws Exception {
		final ProcessLanguageServer a = (ProcessLanguageServer) servers.server(
			ENV);
		final ProcessLanguageServer b = (ProcessLanguageServer) servers.server(ENV
			.withPaths(Collections.singletonList("/b")));
		final String uri = "untitled:/same.py";
		for (final ProcessLanguageServer s : Arrays.asList(a, b)) {
			open(s, uri, "x");
			complete(s, uri);
		}
		assertEquals(2, started.size());
		a.didClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(
			uri)));
		waitFor(() -> started.get(0).events.contains("close " + uri));
		assertFalse(started.get(1).events.contains("close " + uri));
	}

	/**
	 * Tests that initializing the server says what its process can do, as far
	 * as passed on: those of its features passed on, as it said; and documents
	 * synchronized by their whole text, whatever it said.
	 */
	@Test
	public void testCapabilities() throws Exception {
		final ProcessLanguageServer s = (ProcessLanguageServer) servers.server(
			ENV);
		final ServerCapabilities c = s.initialize(new InitializeParams()).get(10,
			TimeUnit.SECONDS).getCapabilities();
		// NB: The fake server completes (resolving completions), but has no
		// hover or signature help...
		assertTrue(c.getCompletionProvider().getResolveProvider());
		assertNull(c.getHoverProvider());
		assertNull(c.getSignatureHelpProvider());
		// ...and its definitions and incremental changes are not passed on.
		assertNull(c.getDefinitionProvider());
		assertEquals(TextDocumentSyncKind.Full, c.getTextDocumentSync()
			.getLeft());
	}

	/**
	 * Tests that initializing a server whose process cannot start says it can
	 * do nothing, but still syncs documents by their whole text.
	 */
	@Test
	public void testFallbackCapabilities() throws Exception {
		final ServerProcesses failing = new ServerProcesses(
			new FakeServers.Launcher()
		{

			@Override
			public Process start(final Environment env) throws IOException {
				throw new IOException("No such server");
			}
		}, new StderrLogService(), 60_000, 2, System::currentTimeMillis);
		try {
			final ProcessLanguageServer s = (ProcessLanguageServer) failing.server(
				ENV);
			final ServerCapabilities c = s.initialize(new InitializeParams()).get(
				10, TimeUnit.SECONDS).getCapabilities();
			assertNull(c.getCompletionProvider());
			assertNull(c.getHoverProvider());
			assertNull(c.getSignatureHelpProvider());
			assertEquals(TextDocumentSyncKind.Full, c.getTextDocumentSync()
				.getLeft());
		}
		finally {
			failing.shutdown();
		}
	}

	/**
	 * Tests that a request about a document closed while the process starts is
	 * answered (with nothing), without opening the document in the process.
	 */
	@Test
	public void testClosedDuringStartup() throws Exception {
		final FakeServers.DelayingLauncher launcher =
			new FakeServers.DelayingLauncher();
		final ServerProcesses delayed = servers(launcher);
		try {
			final ProcessLanguageServer s = (ProcessLanguageServer) delayed.server(
				ENV);
			final String uri = "untitled:/l.py";
			open(s, uri, "x");
			// Asked while the process starts...
			final CompletableFuture<?> answer = s.completion(new CompletionParams(
				new TextDocumentIdentifier(uri), new Position(0, 1)));
			final FakeServers.DelayedFakeServer late = launcher.awaitStart();
			// ...then closed, before it has finished starting.
			s.didClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(
				uri)));
			late.finishInitializing();
			assertNull(answer.get(10, TimeUnit.SECONDS));
			assertFalse(late.texts.containsKey(uri));
		}
		finally {
			delayed.shutdown();
		}
	}

	/**
	 * Sometimes language server startup is slow.
	 * This test ensures that, when multiple changes are made
	 * during server startup, only the most recent change is sent
	 * to the server.
	 */
	@Test
	public void testChangedDuringStartup() throws Exception {
		final FakeServers.DelayingLauncher launcher =
			new FakeServers.DelayingLauncher();
		final ServerProcesses delayed = servers(launcher);
		try {
			final ProcessLanguageServer s = (ProcessLanguageServer) delayed.server(
				ENV);
			final List<PublishDiagnosticsParams> published = connect(s);
			final String uri = "untitled:/l.py";
			open(s, uri, "old");
			// Version 1's diagnosis waits for the process to start...
			final FakeServers.DelayedFakeServer late = launcher.awaitStart();
			s.didChange(new DidChangeTextDocumentParams(
				new VersionedTextDocumentIdentifier(uri, 2), Collections.singletonList(
					new TextDocumentContentChangeEvent("new"))));
			// ...as does version 2's, once the pause after the change has passed.
			// NB: No sign of that but time.
			Thread.sleep(3 * PAUSE);
			late.finishInitializing();
			// Once version 2's problems are published, the process has it...
			waitFor(() -> published.stream().anyMatch(p -> p.getVersion() == 2));
			// ...and was never told version 1, which had become stale.
			assertEquals("new", late.texts.get(uri));
			assertEquals(Collections.singletonList("open " + uri + " 1"),
				late.events);
		}
		finally {
			delayed.shutdown();
		}
	}

	// -- Helper methods --

	/**
	 * How long a document must stay unchanged before its problems are asked
	 * for, as {@link ProcessLanguageServer} has it.
	 */
	private static final long PAUSE = Long.getLong("scijava.lsp.pause", 300);

	/** Processes started by the given launcher. */
	private static ServerProcesses servers(final FakeServers.Launcher launcher) {
		return new ServerProcesses(launcher, new StderrLogService(), 60_000, 2,
			System::currentTimeMillis);
	}

	/**
	 * Connects a client to a server, as an editor would.
	 *
	 * @return The diagnostics published to the client, as they come.
	 */
	private static List<PublishDiagnosticsParams> connect(
		final ProcessLanguageServer s)
	{
		final List<PublishDiagnosticsParams> published =
			new CopyOnWriteArrayList<>();
		s.connect(new LanguageClient() {

			@Override
			public void publishDiagnostics(final PublishDiagnosticsParams p) {
				published.add(p);
			}

			@Override
			public void telemetryEvent(final Object o) {}

			@Override
			public void showMessage(final MessageParams m) {}

			@Override
			public CompletableFuture<MessageActionItem> showMessageRequest(
				final ShowMessageRequestParams r)
			{
				return CompletableFuture.completedFuture(null);
			}

			@Override
			public void logMessage(final MessageParams m) {}
		});
		return published;
	}
}
