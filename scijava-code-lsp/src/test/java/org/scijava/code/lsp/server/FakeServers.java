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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.scijava.code.lsp.Environment;

/**
 * Fake language server processes, connected by pipes, for tests.
 *
 * @author Gabriel Selzer
 */
final class FakeServers {

	private FakeServers() {
		// NB: Prevent instantiation of utility class.
	}

	/** An environment for fake servers. */
	static final Environment ENV = new Environment().withInterpreter(
		"/env/python").withPaths(Collections.singletonList("/overlay"));

	/** Opens a document, with the given text, in a server. */
	static void open(final ProcessLanguageServer s, final String uri,
		final String text)
	{
		s.didOpen(new DidOpenTextDocumentParams(
			new org.eclipse.lsp4j.TextDocumentItem(uri, "python", 1, text)));
	}

	/** Asks a server for completions in a document. */
	static List<CompletionItem> complete(final ProcessLanguageServer s,
		final String uri) throws Exception
	{
		return s.completion(new CompletionParams(
			new org.eclipse.lsp4j.TextDocumentIdentifier(uri), new Position(0, 1)))
			.get(10, TimeUnit.SECONDS).getRight().getItems();
	}

	/** Waits (up to 10 seconds) for a condition to hold. */
	static void waitFor(final java.util.function.BooleanSupplier c)
		throws InterruptedException
	{
		final long end = System.currentTimeMillis() + 10_000;
		while (!c.getAsBoolean()) {
			if (System.currentTimeMillis() > end) throw new AssertionError(
				"Timed out");
			Thread.sleep(10);
		}
	}

	/** Starts fake servers, connected by pipes. */
	static class Launcher implements ServerLauncher {

		/** The servers started, in order. */
		final List<Server> started = new CopyOnWriteArrayList<>();

		@Override
		public String language() {
			return "python";
		}

		@Override
		public Process start(final Environment env) throws IOException {
			final PipedInputStream clientIn = new PipedInputStream(1 << 16);
			final PipedOutputStream serverOut = new PipedOutputStream(clientIn);
			final PipedInputStream serverIn = new PipedInputStream(1 << 16);
			final PipedOutputStream clientOut = new PipedOutputStream(serverIn);
			final Server server = new Server();
			final org.eclipse.lsp4j.jsonrpc.Launcher<org.eclipse.lsp4j.services.LanguageClient> launcher =
				LSPLauncher.createServerLauncher(server, serverIn, serverOut);
			server.client = launcher.getRemoteProxy();
			launcher.startListening();
			started.add(server);
			return new Process() {

				@Override
				public InputStream getInputStream() {
					return clientIn;
				}

				@Override
				public OutputStream getOutputStream() {
					return clientOut;
				}

				@Override
				public InputStream getErrorStream() {
					return InputStream.nullInputStream();
				}

				@Override
				public boolean isAlive() {
					return server.alive;
				}

				@Override
				public int exitValue() {
					if (server.alive) throw new IllegalThreadStateException();
					return 0;
				}

				@Override
				public int waitFor() throws InterruptedException {
					while (server.alive) Thread.sleep(10);
					return 0;
				}

				@Override
				public void destroy() {
					server.alive = false;
					try {
						clientOut.close();
						serverOut.close();
					}
					catch (final IOException exc) {
						// NB: Closed anyway.
					}
				}
			};
		}

		@Override
		public Object initializationOptions(final Environment env) {
			final Map<String, Object> workspace = new LinkedHashMap<>();
			env.interpreter().ifPresent(p -> workspace.put("environmentPath", p));
			workspace.put("extraPaths", env.paths());
			return Collections.singletonMap("workspace", workspace);
		}

		@Override
		public String rootUri(final Environment env) {
			return "file:///scripts";
		}
	}

	/** A language server knowing a few things about json. */
	static final class Server implements LanguageServer, TextDocumentService,
		WorkspaceService
	{

		volatile boolean alive = true, shutdown;
		final Map<String, String> texts = new LinkedHashMap<>();
		final List<String> events = new CopyOnWriteArrayList<>();
		private volatile org.eclipse.lsp4j.services.LanguageClient client;

		@Override
		public CompletableFuture<InitializeResult> initialize(
			final InitializeParams params)
		{
			final ServerCapabilities capabilities = new ServerCapabilities();
			capabilities.setCompletionProvider(new CompletionOptions(true, null));
			return CompletableFuture.completedFuture(new InitializeResult(
				capabilities));
		}

		@Override
		public CompletableFuture<Object> shutdown() {
			shutdown = true;
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void exit() {
			alive = false;
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
		public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
			completion(final CompletionParams params)
		{
			final List<CompletionItem> items = new ArrayList<>();
			items.add(item("decoder", CompletionItemKind.Module, "c"));
			items.add(item("dump", CompletionItemKind.Function, "b"));
			items.add(item("dumps", CompletionItemKind.Function, "a"));
			return CompletableFuture.completedFuture(Either.forRight(
				new CompletionList(items)));
		}

		@Override
		public void didOpen(final DidOpenTextDocumentParams params) {
			texts.put(params.getTextDocument().getUri(), params.getTextDocument()
				.getText());
			events.add("open " + params.getTextDocument().getUri() + " " + params
				.getTextDocument().getVersion());
			publish(params.getTextDocument().getUri(), params.getTextDocument()
				.getVersion());
		}

		@Override
		public void didChange(final DidChangeTextDocumentParams params) {
			texts.put(params.getTextDocument().getUri(), params.getContentChanges()
				.get(0).getText());
			events.add("change " + params.getTextDocument().getUri() + " " + params
				.getTextDocument().getVersion());
			publish(params.getTextDocument().getUri(), params.getTextDocument()
				.getVersion());
		}

		/** Complains about each "!!", and about the first line, always. */
		private void publish(final String uri, final int version) {
			final List<org.eclipse.lsp4j.Diagnostic> list = new ArrayList<>();
			list.add(diagnostic(0, 0, 1, "First line"));
			final String[] lines = texts.get(uri).split("\n", -1);
			for (int i = 0; i < lines.length; i++) {
				final int bang = lines[i].indexOf("!!");
				if (bang >= 0) list.add(diagnostic(i, bang, bang + 2, "Bangs"));
			}
			client.publishDiagnostics(new org.eclipse.lsp4j.PublishDiagnosticsParams(
				uri, list, version));
		}

		private static org.eclipse.lsp4j.Diagnostic diagnostic(final int line,
			final int start, final int end, final String message)
		{
			final org.eclipse.lsp4j.Diagnostic d = new org.eclipse.lsp4j.Diagnostic(
				new org.eclipse.lsp4j.Range(new Position(line, start), new Position(
					line, end)), message);
			d.setSeverity(org.eclipse.lsp4j.DiagnosticSeverity.Warning);
			d.setSource("fake");
			return d;
		}

		@Override
		public void didClose(final DidCloseTextDocumentParams params) {
			events.add("close " + params.getTextDocument().getUri());
		}

		@Override
		public void didSave(final DidSaveTextDocumentParams params) {}

		@Override
		public void didChangeConfiguration(
			final DidChangeConfigurationParams params)
		{}

		@Override
		public void didChangeWatchedFiles(
			final DidChangeWatchedFilesParams params)
		{}

		private static CompletionItem item(final String label,
			final CompletionItemKind kind, final String sort)
		{
			final CompletionItem item = new CompletionItem(label);
			item.setKind(kind);
			item.setInsertText(label);
			item.setSortText(sort);
			return item;
		}
	}
}
