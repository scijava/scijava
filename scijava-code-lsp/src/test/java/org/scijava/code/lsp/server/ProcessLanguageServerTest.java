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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.scijava.code.lsp.server.FakeServers.ENV;
import static org.scijava.code.lsp.server.FakeServers.complete;
import static org.scijava.code.lsp.server.FakeServers.open;
import static org.scijava.code.lsp.server.FakeServers.waitFor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

	// -- Helper methods --

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
