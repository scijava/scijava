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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
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
import org.scijava.Context;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.Positions;
import org.scijava.log.LogService;
import org.scijava.log.StderrLogService;

/**
 * Tests {@link ServerProcesses} and {@link ProcessLanguageServer}, against a fake
 * language server.
 *
 * @author Gabriel Selzer
 */
public class ServerProcessesTest {

	private static final Environment ENV = new Environment().withInterpreter(
		"/env/python").withPaths(Collections.singletonList("/overlay"));

	private final List<FakeServer> started = new CopyOnWriteArrayList<>();
	// NB: Servers note their use by the system clock.
	private final AtomicLong now = new AtomicLong(System.currentTimeMillis());
	private ServerProcesses servers;

	private void setUp(final long idle) {
		servers = new ServerProcesses(new FakeLauncher(), new StderrLogService(), idle, 2,
			now::get);
	}

	@AfterEach
	public void tearDown() {
		if (servers != null) servers.shutdown();
	}

	@Test
	public void testOffset() {
		assertEquals(4, Positions.offset("ab\ncd", new Position(1, 1)));
		// Clamped to the line, and the text.
		assertEquals(2, Positions.offset("ab\ncd", new Position(0, 9)));
		assertEquals(5, Positions.offset("ab\ncd", new Position(7, 0)));
	}

	@Test
	public void testLaunched() throws Exception {
		// A server process, seen as a LanguageServer (for LanguageServerPlugins).
		setUp(60_000);
		final ProcessLanguageServer launched = (ProcessLanguageServer) servers
			.server(ENV);
		final List<org.eclipse.lsp4j.PublishDiagnosticsParams> published =
			new CopyOnWriteArrayList<>();
		launched.connect(new org.eclipse.lsp4j.services.LanguageClient() {

			@Override
			public void telemetryEvent(final Object o) {}

			@Override
			public void publishDiagnostics(
				final org.eclipse.lsp4j.PublishDiagnosticsParams p)
			{
				published.add(p);
			}

			@Override
			public void showMessage(final org.eclipse.lsp4j.MessageParams m) {}

			@Override
			public CompletableFuture<org.eclipse.lsp4j.MessageActionItem>
				showMessageRequest(final org.eclipse.lsp4j.ShowMessageRequestParams r)
			{
				return CompletableFuture.completedFuture(null);
			}

			@Override
			public void logMessage(final org.eclipse.lsp4j.MessageParams m) {}
		});
		final String uri = "untitled:/l.py";
		launched.didOpen(new DidOpenTextDocumentParams(
			new org.eclipse.lsp4j.TextDocumentItem(uri, "python", 1, "y = !!")));
		// Requests bring the process's copy up to date first.
		final List<CompletionItem> items = launched.completion(new CompletionParams(
			new org.eclipse.lsp4j.TextDocumentIdentifier(uri), new Position(0, 2)))
			.get(10, TimeUnit.SECONDS).getRight().getItems();
		assertEquals(3, items.size());
		final FakeServer server = started.get(0);
		assertEquals("y = !!", server.texts.get(uri));
		// Problems: published a moment after the last change, for its version.
		waitFor(() -> !published.isEmpty());
		assertEquals(Integer.valueOf(1), published.get(0).getVersion());
		assertEquals(2, published.get(0).getDiagnostics().size());
		launched.didClose(new DidCloseTextDocumentParams(
			new org.eclipse.lsp4j.TextDocumentIdentifier(uri)));
		waitFor(() -> server.events.contains("close " + uri));
	}

	@Test
	public void testPlugin() throws Exception {
		// Only the launcher given: the rest comes with the plugin.
		final Context context = new Context(LogService.class);
		try {
			final AbstractProcessLanguageServerPlugin plugin =
				new AbstractProcessLanguageServerPlugin()
				{

					@Override
					protected ServerLauncher launcher() {
						return new FakeLauncher();
					}
				};
			context.inject(plugin);
			// NB: Its log, declared by the superclass, is injected too.
			final java.lang.reflect.Field log =
				AbstractProcessLanguageServerPlugin.class.getDeclaredField("log");
			log.setAccessible(true);
			assertSame(context.getService(LogService.class), log.get(plugin));

			assertTrue(plugin.supports("python"));
			assertFalse(plugin.supports("groovy"));
			assertEquals(0, started.size()); // NB: Not until needed.

			final ProcessLanguageServer s = (ProcessLanguageServer) plugin.create(
				"python", ENV);
			final String uri = "untitled:/p.py";
			s.didOpen(new DidOpenTextDocumentParams(
				new org.eclipse.lsp4j.TextDocumentItem(uri, "python", 1, "x")));
			assertEquals(3, complete(s, uri).size());
			assertEquals(1, started.size());

			// Disposed: the processes stop.
			plugin.dispose();
			waitFor(() -> started.get(0).shutdown);
		}
		finally {
			context.dispose();
		}
	}

	@Test
	public void testRestartAndIdle() throws Exception {
		setUp(60_000);
		final ServerProcesses.Slot slot = servers.slot(ENV);
		final ServerProcess first = slot.process().get(10, TimeUnit.SECONDS);
		assertSame(slot, servers.slot(ENV));
		assertSame(first, slot.process().get(10, TimeUnit.SECONDS));
		// The server dies: the next request starts another.
		started.get(0).alive = false;
		final ServerProcess second = slot.process().get(10, TimeUnit.SECONDS);
		assertNotSame(first, second);
		assertEquals(2, started.size());

		// Unused for long: stopped, and not started again until asked.
		now.addAndGet(120_000);
		servers.stopIdle();
		waitFor(() -> started.get(1).shutdown);
		assertFalse(slot.running().isPresent());
		assertEquals(2, started.size());
		assertNotSame(second, slot.process().get(10, TimeUnit.SECONDS));
	}

	@Test
	public void testReleased() throws Exception {
		// Stopped: the environment is let go, and a new slot may take over.
		setUp(60_000);
		final ServerProcesses.Slot old = servers.slot(ENV);
		old.process().get(10, TimeUnit.SECONDS);
		now.addAndGet(120_000);
		servers.stopIdle();
		final ServerProcesses.Slot fresh = servers.slot(ENV);
		assertNotSame(old, fresh);
		final ServerProcess s = fresh.process().get(10, TimeUnit.SECONDS);
		// The old slot's users get the new slot's process: no second one.
		assertSame(s, old.process().get(10, TimeUnit.SECONDS));
		assertSame(s, old.running().get());
		assertEquals(2, started.size());
	}

	@Test
	public void testRetry() throws Exception {
		// Failed to start: tried again only after a while.
		final AtomicLong tries = new AtomicLong();
		servers = new ServerProcesses(new FakeLauncher() {

			@Override
			public Process start(final Environment env) throws IOException {
				tries.incrementAndGet();
				throw new IOException("No such server");
			}
		}, new StderrLogService(), 60_000, 2, now::get);
		final ServerProcesses.Slot slot = servers.slot(ENV);
		final CompletableFuture<ServerProcess> failed = slot.process();
		assertThrows(java.util.concurrent.ExecutionException.class, () -> failed
			.get(10, TimeUnit.SECONDS));
		waitFor(() -> slot.process() == failed); // NB: Its failure noted.
		assertEquals(1, tries.get());
		now.addAndGet(60_000);
		assertThrows(java.util.concurrent.ExecutionException.class, () -> slot
			.process().get(10, TimeUnit.SECONDS));
		assertEquals(2, tries.get());
	}

	@Test
	public void testTooMany() throws Exception {
		// At most two: a third stops the least recently used.
		setUp(60_000);
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
		waitFor(() -> started.get(1).shutdown);
		assertFalse(started.get(0).shutdown);
		assertSame(first, a.running().get());
		assertFalse(b.running().isPresent());
	}

	@Test
	public void testCloseOwnOnly() throws Exception {
		// Closing a document tells only its environment's process.
		setUp(60_000);
		final ProcessLanguageServer a = (ProcessLanguageServer) servers.server(
			ENV);
		final ProcessLanguageServer b = (ProcessLanguageServer) servers.server(ENV
			.withPaths(Collections.singletonList("/b")));
		final String uri = "untitled:/same.py";
		for (final ProcessLanguageServer s : Arrays.asList(a, b)) {
			s.didOpen(new DidOpenTextDocumentParams(
				new org.eclipse.lsp4j.TextDocumentItem(uri, "python", 1, "x")));
			complete(s, uri);
		}
		assertEquals(2, started.size());
		a.didClose(new DidCloseTextDocumentParams(
			new org.eclipse.lsp4j.TextDocumentIdentifier(uri)));
		waitFor(() -> started.get(0).events.contains("close " + uri));
		assertFalse(started.get(1).events.contains("close " + uri));
	}

	@Test
	public void testPosition() {
		assertEquals(new Position(0, 0), Positions.position("ab\ncd", 0));
		assertEquals(new Position(1, 1), Positions.position("ab\ncd", 4));
		// UTF-16 code units, as Java counts.
		assertEquals(new Position(1, 3), Positions.position("a\n😀b",
			5));
	}

	// -- Helper methods --

	private static List<CompletionItem> complete(final ProcessLanguageServer s,
		final String uri) throws Exception
	{
		return s.completion(new CompletionParams(
			new org.eclipse.lsp4j.TextDocumentIdentifier(uri), new Position(0, 1)))
			.get(10, TimeUnit.SECONDS).getRight().getItems();
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
	private class FakeLauncher implements ServerLauncher {

		@Override
		public String languageId() {
			return "python";
		}

		@Override
		public Process start(final Environment env)
			throws IOException
		{
			final PipedInputStream clientIn = new PipedInputStream(1 << 16);
			final PipedOutputStream serverOut = new PipedOutputStream(clientIn);
			final PipedInputStream serverIn = new PipedInputStream(1 << 16);
			final PipedOutputStream clientOut = new PipedOutputStream(serverIn);
			final FakeServer server = new FakeServer();
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

	/** A language server knowing a few things about json, and a function f. */
	private static final class FakeServer implements LanguageServer,
		TextDocumentService, WorkspaceService
	{

		private volatile boolean alive = true, shutdown, noActive, mute;
		private volatile org.eclipse.lsp4j.services.LanguageClient client;
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
			events.add("resolve " + item.getLabel());
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
			if (mute) return;
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
		public CompletableFuture<org.eclipse.lsp4j.Hover> hover(
			final org.eclipse.lsp4j.HoverParams params)
		{
			final String text = texts.get(params.getTextDocument().getUri());
			final String line = text.split("\n", -1)[params.getPosition()
				.getLine()];
			int start = params.getPosition().getCharacter(), end = start;
			while (start > 0 && Character.isLetter(line.charAt(start - 1))) start--;
			while (end < line.length() && Character.isLetter(line.charAt(end))) end++;
			if (start == end) return CompletableFuture.completedFuture(null);
			final org.eclipse.lsp4j.Hover h = new org.eclipse.lsp4j.Hover(
				new org.eclipse.lsp4j.MarkupContent("plaintext", "Docs of " + line
					.substring(start, end)), new org.eclipse.lsp4j.Range(new Position(
						params.getPosition().getLine(), start), new Position(params
							.getPosition().getLine(), end)));
			return CompletableFuture.completedFuture(h);
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
