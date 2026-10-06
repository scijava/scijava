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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.scijava.code.api.Completion;
import org.scijava.code.api.EnvironmentContext;
import org.scijava.code.api.ScriptDialect;
import org.scijava.code.api.ScriptDocument;
import org.scijava.code.api.SignatureHelp.Fit;
import org.scijava.log.StderrLogService;

/**
 * Tests {@link LspClient} and {@link LspServers}, against a fake language
 * server.
 *
 * @author Gabriel Selzer
 */
public class LspClientTest {

	/** Declares parameters Python-style: {@code name: type}. */
	private static final ScriptDialect DIALECT = new ScriptDialect() {

		@Override
		public String runtimeType(final Class<?> type) {
			return type.getSimpleName().toLowerCase();
		}

		@Override
		public String declare(final String name, final String type) {
			return name + ": " + type;
		}
	};

	private static final EnvironmentContext ENV = new EnvironmentContext(
		"/env/python", Collections.singletonList("/overlay"), null);

	private final List<FakeServer> started = new CopyOnWriteArrayList<>();
	// NB: Servers note their use by the system clock.
	private final AtomicLong now = new AtomicLong(System.currentTimeMillis());
	private LspServers servers;
	private LspClient client;

	private void setUp(final long idle) {
		servers = new LspServers(new FakeSpec(), new StderrLogService(), idle, 2,
			now::get);
		// Understands signatures like "def f(a, b) -> T".
		client = new LspClient(servers, now::get) {

			@Override
			protected Completion.Builder callable(final String name,
				final String signature)
			{
				final int open = signature.indexOf('('), close = signature.indexOf(
					')');
				if (open < 0 || close < 0) return null;
				final List<Completion.Parameter> params = new ArrayList<>();
				for (final String p : signature.substring(open + 1, close).split(",")) {
					if (!p.trim().isEmpty()) params.add(new Completion.Parameter(p
						.trim(), null));
				}
				final int arrow = signature.indexOf("->");
				return Completion.builder(name).parameters(params).returnType(
					arrow < 0 ? null : signature.substring(arrow + 2).trim());
			}
		};
	}

	@AfterEach
	public void tearDown() {
		if (servers != null) servers.shutdown();
	}

	private static ScriptDocument doc(final String script) {
		final Map<String, Class<?>> params = new LinkedHashMap<>();
		params.put("sigma", Double.class);
		return ScriptDocument.of(script, params, DIALECT, ENV);
	}

	@Test
	public void testCompletion() throws Exception {
		setUp(60_000);
		final String script = "import json\njson.du";
		final List<Completion> completions = client.complete(doc(script),
			"untitled:/a.py", script.length(), 1).get(10, TimeUnit.SECONDS);
		final FakeServer server = started.get(0);

		// The server got the transformed script, and the options for its
		// environment.
		assertEquals("sigma: double\n" + script, server.texts.get(
			"untitled:/a.py"));
		assertEquals("{\"workspace\":{\"environmentPath\":\"/env/python\",\"" +
			"extraPaths\":[\"/overlay\"]}}", String.valueOf(server.options));
		// It was asked about the caret, shifted by the declaration line.
		assertEquals(new Position(2, "json.du".length()), server.positions.get(0));

		// In the server's (sortText) order; the first resolved, so callable.
		assertEquals(Arrays.asList("dumps", "dump", "decoder"), names(completions));
		final Completion dumps = completions.get(0);
		assertEquals(Completion.Kind.METHOD, dumps.kind());
		assertEquals("[obj, indent]", String.valueOf(dumps.parameters()));
		assertEquals("str", dumps.returnType());
		assertEquals("<pre>Docs of dumps &amp; co.</pre>", dumps.description());
		// Unresolved functions are offered by name, with documentation on demand.
		final Completion dump = completions.get(1);
		assertEquals(Completion.Kind.OTHER, dump.kind());
		assertTrue(dump.parameters().isEmpty());
		assertEquals("<pre>Docs of dump &amp; co.</pre>", dump.description());
		assertEquals(Completion.Kind.PACKAGE, completions.get(2).kind());
	}

	@Test
	public void testReuseAndSupersede() throws Exception {
		setUp(60_000);
		final String script = "import json\njson.du";
		final ScriptDocument doc = doc(script);
		final CompletableFuture<List<Completion>> first = client.complete(doc,
			"untitled:/a.py", script.length(), 0);
		first.get(10, TimeUnit.SECONDS);
		// The same question: the same answer, without asking the server again.
		assertSame(first, client.complete(doc, "untitled:/a.py", script.length(),
			0));
		final FakeServer server = started.get(0);
		assertEquals(1, server.positions.size());

		// A slow question, superseded by another: cancelled, also on the server.
		server.hold = new CompletableFuture<>();
		final CompletableFuture<List<Completion>> slow = client.complete(doc,
			"untitled:/a.py", script.length() - 1, 0);
		waitFor(() -> server.held.size() == 1);
		final CompletableFuture<List<Completion>> next = client.complete(doc,
			"untitled:/a.py", script.length() - 2, 0);
		assertThrows(CancellationException.class, () -> slow.get(10,
			TimeUnit.SECONDS));
		waitFor(() -> server.held.get(0).isCancelled());
		server.hold.complete(null);
		assertEquals(3, next.get(10, TimeUnit.SECONDS).size());
		server.hold = null;
		// A cancelled answer is not reused.
		assertNotSame(slow, client.complete(doc, "untitled:/a.py", script.length() -
			1, 0));
	}

	@Test
	public void testDocumentSync() throws Exception {
		setUp(60_000);
		client.complete(doc("x = 1\nx."), "untitled:/a.py", 8, 0).get(10,
			TimeUnit.SECONDS);
		final FakeServer server = started.get(0);
		client.complete(doc("x = 2\nx."), "untitled:/a.py", 8, 0).get(10,
			TimeUnit.SECONDS);
		assertEquals(Arrays.asList("open untitled:/a.py 1",
			"change untitled:/a.py 2"), server.events);
		assertEquals("sigma: double\nx = 2\nx.", server.texts.get(
			"untitled:/a.py"));
		client.close("untitled:/a.py");
		waitFor(() -> server.events.size() == 3);
		assertEquals("close untitled:/a.py", server.events.get(2));
	}

	@Test
	public void testSignatureHelp() throws Exception {
		setUp(60_000);
		final String script = "f(1, ";
		org.scijava.code.api.SignatureHelp help = client.signatureHelp(doc(script),
			"untitled:/a.py", script.length(), 1, 5).get(10, TimeUnit.SECONDS);
		assertEquals(2, help.signatures().size());
		final Completion f = help.signatures().get(0).callable();
		assertEquals("f", f.insertionText());
		assertEquals("[x, y=1]", String.valueOf(f.parameters()));
		assertEquals(Fit.UNKNOWN, help.signatures().get(0).fit());
		assertEquals("<pre>Docs of f</pre>", f.description());
		// The server's active parameter, and the caller's call start.
		assertEquals(1, help.activeParameter());
		assertEquals(1, help.callStart());

		// Without an active parameter from the server: the caller's.
		started.get(0).noActive = true;
		help = client.signatureHelp(doc(script + "2, "), "untitled:/a.py", script
			.length() + 3, 1, 2).get(10, TimeUnit.SECONDS);
		assertEquals(2, help.activeParameter());
		// No signatures: none.
		help = client.signatureHelp(doc("x"), "untitled:/a.py", 1, 0, 0).get(10,
			TimeUnit.SECONDS);
		assertTrue(help.isEmpty());
	}

	@Test
	public void testRestartAndIdle() throws Exception {
		setUp(60_000);
		final LspServer first = servers.server(ENV).get(10, TimeUnit.SECONDS);
		assertSame(first, servers.server(ENV).get(10, TimeUnit.SECONDS));
		// The server dies: the next request starts another.
		started.get(0).alive = false;
		final LspServer second = servers.server(ENV).get(10, TimeUnit.SECONDS);
		assertNotSame(first, second);
		assertEquals(2, started.size());

		// Unused for long: stopped.
		now.addAndGet(120_000);
		servers.stopIdle();
		waitFor(() -> started.get(1).shutdown);
		assertNotSame(second, servers.server(ENV).get(10, TimeUnit.SECONDS));
	}

	@Test
	public void testPosition() {
		assertEquals(new Position(0, 0), LspClient.position("ab\ncd", 0));
		assertEquals(new Position(1, 1), LspClient.position("ab\ncd", 4));
		// UTF-16 code units, as Java counts.
		assertEquals(new Position(1, 3), LspClient.position("a\n😀b",
			5));
		assertEquals("untitled:/script.py", LspClient.uri(null, "script.py"));
		assertFalse(LspClient.uri("x.py", "script.py").startsWith("untitled"));
	}

	// -- Helper methods --

	private static List<String> names(final List<Completion> completions) {
		final List<String> names = new ArrayList<>();
		for (final Completion c : completions) names.add(c.insertionText());
		return names;
	}

	private static void waitFor(final java.util.function.BooleanSupplier c)
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
	private final class FakeSpec implements LspServerSpec {

		@Override
		public String languageId() {
			return "python";
		}

		@Override
		public LspConnection start(final EnvironmentContext env)
			throws IOException
		{
			final PipedInputStream clientIn = new PipedInputStream(1 << 16);
			final PipedOutputStream serverOut = new PipedOutputStream(clientIn);
			final PipedInputStream serverIn = new PipedInputStream(1 << 16);
			final PipedOutputStream clientOut = new PipedOutputStream(serverIn);
			final FakeServer server = new FakeServer();
			LSPLauncher.createServerLauncher(server, serverIn, serverOut)
				.startListening();
			started.add(server);
			return new LspConnection() {

				@Override
				public InputStream input() {
					return clientIn;
				}

				@Override
				public OutputStream output() {
					return clientOut;
				}

				@Override
				public boolean isAlive() {
					return server.alive;
				}

				@Override
				public void close() {
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
		public Object initializationOptions(final EnvironmentContext env) {
			final Map<String, Object> workspace = new LinkedHashMap<>();
			workspace.put("environmentPath", env.interpreter());
			workspace.put("extraPaths", env.searchPaths());
			return Collections.singletonMap("workspace", workspace);
		}

		@Override
		public String rootUri(final EnvironmentContext env) {
			return "file:///scripts";
		}
	}

	/** A language server knowing a few things about json, and a function f. */
	private static final class FakeServer implements LanguageServer,
		TextDocumentService, WorkspaceService
	{

		private volatile boolean alive = true, shutdown, noActive;
		private volatile Object options;
		private final Map<String, String> texts = new LinkedHashMap<>();
		private final List<String> events = new CopyOnWriteArrayList<>();
		private final List<Position> positions = new CopyOnWriteArrayList<>();
		/** If set, completion requests wait for this (and are kept as held). */
		private volatile CompletableFuture<Void> hold;
		private final List<CompletableFuture<Either<List<CompletionItem>, CompletionList>>> held =
			new CopyOnWriteArrayList<>();

		@Override
		public CompletableFuture<InitializeResult> initialize(
			final InitializeParams params)
		{
			options = params.getInitializationOptions();
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
			positions.add(params.getPosition());
			final List<CompletionItem> items = new ArrayList<>();
			items.add(item("decoder", CompletionItemKind.Module, "c"));
			items.add(item("dump", CompletionItemKind.Function, "b"));
			items.add(item("dumps", CompletionItemKind.Function, "a"));
			final Either<List<CompletionItem>, CompletionList> answer = Either
				.forRight(new CompletionList(items));
			if (hold == null) return CompletableFuture.completedFuture(answer);
			final CompletableFuture<Either<List<CompletionItem>, CompletionList>> f =
				new CompletableFuture<>();
			held.add(f);
			hold.thenRun(() -> f.complete(answer));
			return f;
		}

		@Override
		public CompletableFuture<CompletionItem> resolveCompletionItem(
			final CompletionItem item)
		{
			item.setDetail("def " + item.getLabel() + "(obj, indent) -> str");
			item.setDocumentation("Docs of " + item.getLabel() + " & co.");
			return CompletableFuture.completedFuture(item);
		}

		@Override
		public CompletableFuture<SignatureHelp> signatureHelp(
			final SignatureHelpParams params)
		{
			if (params.getPosition().getCharacter() < 2) {
				return CompletableFuture.completedFuture(null);
			}
			final SignatureInformation f = new SignatureInformation("def f(x, y=1)");
			f.setDocumentation("Docs of f");
			f.setParameters(Arrays.asList(new ParameterInformation("x"),
				new ParameterInformation("y=1")));
			final SignatureInformation g = new SignatureInformation("def f(z)");
			return CompletableFuture.completedFuture(new SignatureHelp(Arrays.asList(
				f, g), 0, noActive ? null : 1));
		}

		@Override
		public void didOpen(final DidOpenTextDocumentParams params) {
			texts.put(params.getTextDocument().getUri(), params.getTextDocument()
				.getText());
			events.add("open " + params.getTextDocument().getUri() + " " + params
				.getTextDocument().getVersion());
		}

		@Override
		public void didChange(final DidChangeTextDocumentParams params) {
			texts.put(params.getTextDocument().getUri(), params.getContentChanges()
				.get(0).getText());
			events.add("change " + params.getTextDocument().getUri() + " " + params
				.getTextDocument().getVersion());
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
