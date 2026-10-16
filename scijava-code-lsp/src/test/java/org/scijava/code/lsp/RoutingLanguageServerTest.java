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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link RoutingLanguageServer}, around fake servers.
 *
 * @author Gabriel Selzer
 */
public class RoutingLanguageServerTest {

	private static final String URI = "untitled:/m.py";

	private static final Environment A = new Environment("/a/python", null,
		null);

	private static final Environment B = new Environment("/b/python", null,
		null);

	/** The servers launched, in order. */
	private final List<Fake> launched = new CopyOnWriteArrayList<>();

	/** The problems the client heard of. */
	private final List<PublishDiagnosticsParams> heard =
		new CopyOnWriteArrayList<>();

	@Test
	public void testMoves() throws Exception {
		// Each document is analyzed by the server of where its script runs, as
		// its text says here.
		final RoutingLanguageServer server = new RoutingLanguageServer(env -> {
			final Fake f = new Fake(env);
			launched.add(f);
			return f;
		}, doc -> doc.getText().startsWith("#B") ? B : A);
		server.connect(client());
		server.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(URI,
			"python", 1, "x = 1")));
		complete(server);
		assertEquals(1, launched.size());
		final Fake a = launched.get(0);
		assertEquals(A, a.environment);
		assertEquals(Collections.singletonList("open x = 1"), a.events.subList(0,
			1));

		// Changed, but still running there: the server is told of the change.
		change(server, 2, "x = 2");
		complete(server);
		assertEquals(1, launched.size());
		assertTrue(a.events.contains("change x = 2"));

		// It runs elsewhere now: the next request goes there, and the server of
		// where it ran lets it go (and, with nothing left to analyze, stops).
		change(server, 3, "#B\nx = 1");
		final List<CompletionItem> items = complete(server);
		assertEquals(2, launched.size());
		final Fake b = launched.get(1);
		assertEquals(B, b.environment);
		assertTrue(b.events.contains("open #B\nx = 1"));
		assertTrue(a.events.contains("close"));
		assertTrue(a.events.contains("shutdown"));

		// Completions are resolved by the server that made them.
		server.resolveCompletionItem(items.get(0)).get(10, TimeUnit.SECONDS);
		assertTrue(b.events.contains("resolve dumps"));
		assertFalse(a.events.contains("resolve dumps"));

		// Problems: heard only from the server the document is open on.
		a.publish();
		b.publish();
		waitFor(() -> !heard.isEmpty());
		assertEquals(1, heard.size());
		assertEquals("/b/python", heard.get(0).getDiagnostics().get(0)
			.getMessage().getLeft());

		// Closed: let go, the server staying (e.g. for the document under another
		// name), until this is shut down.
		server.didClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(
			URI)));
		waitFor(() -> b.events.contains("close"));
		assertFalse(b.events.contains("shutdown"));
		server.shutdown().get(10, TimeUnit.SECONDS);
		server.exit();
		assertTrue(b.events.contains("shutdown"));
		assertTrue(b.events.contains("exit"));
		// Nothing more is launched.
		server.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(URI,
			"python", 1, "x = 1")));
		assertEquals(null, server.completion(new CompletionParams(
			new TextDocumentIdentifier(URI), new Position(0, 0))).get(10,
				TimeUnit.SECONDS));
		assertEquals(2, launched.size());
	}

	// -- Helper methods --

	private static List<CompletionItem> complete(final RoutingLanguageServer s)
		throws Exception
	{
		return s.completion(new CompletionParams(new TextDocumentIdentifier(URI),
			new Position(0, 0))).get(10, TimeUnit.SECONDS).getRight().getItems();
	}

	private static void change(final RoutingLanguageServer s, final int version,
		final String text)
	{
		s.didChange(new DidChangeTextDocumentParams(
			new VersionedTextDocumentIdentifier(URI, version), Collections
				.singletonList(new TextDocumentContentChangeEvent(text))));
	}

	private static void waitFor(final BooleanSupplier condition)
		throws InterruptedException
	{
		final long deadline = System.currentTimeMillis() + 10_000;
		while (!condition.getAsBoolean()) {
			if (System.currentTimeMillis() > deadline) {
				throw new AssertionError("Timed out");
			}
			Thread.sleep(10);
		}
	}

	private LanguageClient client() {
		return new LanguageClient() {

			@Override
			public void publishDiagnostics(final PublishDiagnosticsParams p) {
				heard.add(p);
			}

			@Override
			public void telemetryEvent(final Object object) {}

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
		};
	}

	/** A server of an environment, recording what it is told. */
	private static final class Fake implements LanguageServer,
		TextDocumentService, WorkspaceService, LanguageClientAware
	{

		private final Environment environment;
		private final List<String> events = new CopyOnWriteArrayList<>();
		private LanguageClient client;

		private Fake(final Environment environment) {
			this.environment = environment;
		}

		/** Reports a problem, naming where it runs. */
		private void publish() {
			client.publishDiagnostics(new PublishDiagnosticsParams(URI, Collections
				.singletonList(new Diagnostic(new Range(new Position(0, 0),
					new Position(0, 1)), environment.interpreter()))));
		}

		@Override
		public void connect(final LanguageClient c) {
			client = c;
		}

		@Override
		public CompletableFuture<InitializeResult> initialize(
			final InitializeParams params)
		{
			return CompletableFuture.completedFuture(new InitializeResult(
				new ServerCapabilities()));
		}

		@Override
		public CompletableFuture<Object> shutdown() {
			events.add("shutdown");
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void exit() {
			events.add("exit");
		}

		@Override
		public TextDocumentService getTextDocumentService() {
			return this;
		}

		@Override
		public WorkspaceService getWorkspaceService() {
			return this;
		}

		@Override
		public void didOpen(final DidOpenTextDocumentParams p) {
			events.add("open " + p.getTextDocument().getText());
		}

		@Override
		public void didChange(final DidChangeTextDocumentParams p) {
			events.add("change " + p.getContentChanges().get(0).getText());
		}

		@Override
		public void didClose(final DidCloseTextDocumentParams p) {
			events.add("close");
		}

		@Override
		public void didSave(final DidSaveTextDocumentParams p) {}

		@Override
		public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
			completion(final CompletionParams p)
		{
			events.add("complete");
			final CompletionItem item = new CompletionItem("dumps");
			item.setData("dumps");
			return CompletableFuture.completedFuture(Either.forRight(
				new CompletionList(false, new CopyOnWriteArrayList<>(Collections
					.singletonList(item)))));
		}

		@Override
		public CompletableFuture<CompletionItem> resolveCompletionItem(
			final CompletionItem item)
		{
			events.add("resolve " + item.getData());
			return CompletableFuture.completedFuture(item);
		}

		@Override
		public void didChangeConfiguration(final DidChangeConfigurationParams p) {}

		@Override
		public void didChangeWatchedFiles(final DidChangeWatchedFilesParams p) {}
	}
}
