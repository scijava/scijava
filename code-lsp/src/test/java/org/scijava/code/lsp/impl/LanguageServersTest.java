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

package org.scijava.code.lsp.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

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
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerPlugin;
import org.scijava.code.lsp.LanguageServerService;
import org.scijava.code.lsp.Positions;
import org.scijava.code.lsp.ScriptSession;
import org.scijava.code.lsp.ScriptDialect;
import org.scijava.plugin.Plugin;
import org.scijava.script.AbstractScriptLanguage;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptService;

/**
 * Tests {@link LanguageServerService} and {@link ScriptSession}, with fake
 * in-process servers; and {@link BindingsLanguageServer}.
 *
 * @author Gabriel Selzer
 */
public class LanguageServersTest {

	private static final String URI = "untitled:/a.fake";

	/** The fake servers created, in order. */
	static final List<FakeServer> SERVERS = new CopyOnWriteArrayList<>();

	private Context context;
	private LanguageServerService service;
	private ScriptLanguage fake;
	private final List<PublishDiagnosticsParams> published =
		new CopyOnWriteArrayList<>();

	@BeforeEach
	public void setUp() {
		SERVERS.clear();
		context = new Context(ScriptService.class,
			LanguageServerService.class);
		service = context.service(LanguageServerService.class);
		fake = context.service(ScriptService.class).getLanguageByName("Fake");
	}

	@AfterEach
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void testHighestPriorityServes() throws Exception {
		try (ScriptSession session = service.session(fake)) {
			final CompletionList list = session.completion("x.ma", 4).get(10,
				TimeUnit.SECONDS);
			// Only the highest-priority plugin's server, created for the session.
			assertEquals(Arrays.asList("max", "mass"), labels(list));
			assertEquals(1, SERVERS.size());
			assertEquals("high", SERVERS.get(0).name);
			// For the LSP language the script language's dialect names.
			assertEquals("fake", SERVERS.get(0).languageId);
		}
		// Each session gets a server of its own.
		try (ScriptSession other = service.session(fake)) {
			other.completion("x", 1).get(10, TimeUnit.SECONDS);
			assertEquals(2, SERVERS.size());
		}
	}

	@Test
	public void testLifeCycle() throws Exception {
		final ScriptSession session = service.session(fake);
		// Created once there is a script, and initialized at once; told about the
		// script as it changes.
		session.update("x = 1");
		final FakeServer server = server(0);
		waitFor(() -> server.events.size() >= 2);
		session.update("x = 2");
		waitFor(() -> server.events.size() >= 3);
		assertEquals(Arrays.asList("initialized", "open 1 x = 1",
			"change 2 x = 2"), server.events);
		// Closed: the script, then the server.
		session.close();
		waitFor(() -> server.events.size() >= 6);
		assertEquals(Arrays.asList("close", "shutdown", "exit"), server.events
			.subList(3, 6));
	}

	@Test
	public void testAwaitsInitialization() {
		// A server answering initialize later (e.g. a process starting).
		final FakeServer late = new FakeServer("late", Environment.NONE) {

			@Override
			public CompletableFuture<InitializeResult> initialize(
				final InitializeParams params)
			{
				return CompletableFuture.supplyAsync(() -> {
					try {
						Thread.sleep(100);
					}
					catch (final InterruptedException exc) {
						Thread.currentThread().interrupt();
					}
					events.add("answered");
					return new InitializeResult(new ServerCapabilities());
				});
			}
		};
		try (ScriptSession session = ScriptSession.owning(late, fake)) {
			session.update("x");
		}
		// Told nothing before it answered.
		assertEquals(Arrays.asList("answered", "initialized", "open 1 x"),
			late.events.subList(0, 3));
	}

	@Test
	public void testLiveEnvironment() {
		final ScriptContext live = new SimpleScriptContext();
		try (ScriptSession session = service.session(fake, live)) {
			session.update("x");
			assertSame(live, server(0).environment().live());
		}
		try (ScriptSession session = service.session(fake)) {
			session.update("x");
			assertNull(server(1).environment().live());
		}
		// Where the script runs, as its dialect says.
		try (ScriptSession session = service.session(fake, live)) {
			session.update("#env /env/bin/fake\nx");
			assertEquals("/env/bin/fake", server(2).environment().interpreter());
			assertSame(live, server(2).environment().live());
		}
	}

	@Test
	public void testSession() throws Exception {
		final List<List<Diagnostic>> heard = new CopyOnWriteArrayList<>();
		final ScriptSession session = service.session(fake)
			.setDiagnosticsListener(heard::add);
		// Opened with its first text; untitled, with its language's extension.
		session.update("x = 1");
		final FakeServer server = server(0);
		waitFor(() -> server.events.size() >= 2);
		// The same text again: nothing sent; another: the next version.
		session.update("x = 1").update("x.ma");
		final List<String> events = server.events;
		waitFor(() -> events.size() >= 3);
		assertEquals(Arrays.asList("open 1 x = 1", "change 2 x.ma"), events
			.subList(1, 3));
		final List<String> opened = server.opened;
		assertTrue(opened.get(0).startsWith("untitled:/script-"));
		assertTrue(opened.get(0).endsWith(".fake"));

		// Asked about a text (the current one: nothing sent), at an offset.
		final CompletionList list = session.completion("x.ma", 4).get(10,
			TimeUnit.SECONDS);
		assertEquals(new Position(0, 4), server.asked.get(server.asked.size() -
			1));
		assertTrue(labels(list).contains("max"));
		assertEquals(3, events.size());

		// The problems in the current text.
		assertEquals(Collections.singletonList("high: x.ma"), messages(session
			.diagnostics(10_000)));
		assertFalse(heard.isEmpty());

		// Saved as a file: closed, and opened anew as the file.
		final File file = new File("scripts", "a.fake");
		session.setFile(() -> file).update("x.ma");
		waitFor(() -> events.size() >= 5);
		assertEquals(Arrays.asList("close", "open 1 x.ma"), events.subList(3, 5));
		assertEquals(file.getAbsoluteFile().toURI().toString(), opened.get(1));
		session.close();
		waitFor(() -> events.size() >= 6);
		assertEquals("close", events.get(5));
	}

	@Test
	public void testDiagnostics() throws Exception {
		try (ScriptSession session = service.session(fake)) {
			assertEquals(Collections.singletonList("high: oops"), messages(session
				.update("oops").diagnostics(10_000)));
			final FakeServer server = server(0);
			// Problems of an earlier version are out of date.
			assertEquals(Collections.emptyList(), messages(session.update("fine")
				.diagnostics(10_000)));
			server.publish(server.opened.get(0), 1, "stale");
			assertEquals(Collections.emptyList(), messages(session.diagnostics()));
		}
	}

	@Test
	public void testSignatureHelpAndHover() throws Exception {
		try (ScriptSession session = service.session(fake)) {
			final SignatureHelp help = session.signatureHelp("max(1, ", 7).get(10,
				TimeUnit.SECONDS);
			assertEquals("max(int a, int b)", help.getSignatures().get(0)
				.getLabel());
			final Hover hover = session.hover("max(1, ", 1).get(10,
				TimeUnit.SECONDS);
			assertEquals("high hover", hover.getContents().getRight().getValue());
		}
	}

	@Test
	public void testTransforming() throws Exception {
		final String script = "#@ double sigma\nx.ma";
		try (ScriptSession session = service.session(fake)) {
			final CompletionList list = session.completion(script, script.length())
				.get(10, TimeUnit.SECONDS);
			final FakeServer server = server(0);
			// The server sees plain code, in the script language's dialect.
			assertEquals("open 1 import mass; sigma: double\n" + script, server.events
				.get(1));
			// Asked one line down; its edits mapped back up.
			assertEquals(new Position(2, 4), server.asked.get(0));
			// Names only the declarations import are not completed.
			assertEquals(Collections.singletonList("max"), labels(list));
			assertEquals(new Range(new Position(1, 2), new Position(1, 4)), list
				.getItems().get(0).getTextEdit().getLeft().getRange());
			// Problems mapped back; those in the added line left out.
			server.publish(server.opened.get(0), 1, "oops", new Range(new Position(
				0, 0), new Position(0, 5)), new Range(new Position(2, 0),
					new Position(2, 1)));
			assertEquals(1, session.diagnostics().size());
			assertEquals(new Range(new Position(1, 0), new Position(1, 1)), session
				.diagnostics().get(0).getRange());
		}
	}

	@Test
	public void testBindings() throws Exception {
		// Code typed into an interpreter whose language has no servers: its
		// variables, and their values' members.
		final SimpleScriptContext live = new SimpleScriptContext();
		live.setAttribute("text", "hello", ScriptContext.ENGINE_SCOPE);
		live.setAttribute("count", 3, ScriptContext.ENGINE_SCOPE);
		final BindingsLanguageServer server = new BindingsLanguageServer(live);
		assertEquals(Collections.singletonList("count"), labels(complete(server,
			"x = co")));
		assertEquals(Arrays.asList("count", "text"), labels(complete(server, "")));
		final CompletionList members = complete(server, "text.toUpp");
		assertEquals(Arrays.asList("toUpperCase", "toUpperCase"), labels(members));
		final CompletionItem upper = members.getItems().get(0);
		assertTrue(upper.getTextEdit().getLeft().getNewText().startsWith(
			"toUpperCase("));
		assertEquals(new Range(new Position(0, 5), new Position(0, 10)), upper
			.getTextEdit().getLeft().getRange());
		// Unknown variables: nothing.
		assertTrue(complete(server, "nothing.x").getItems().isEmpty());

		// A session about code typed into an interpreter of a language no plugin
		// serves: the same.
		final ScriptLanguage unserved = new AbstractScriptLanguage() {

			@Override
			public ScriptEngine getScriptEngine() {
				return null;
			}
		};
		try (ScriptSession session = service.session(unserved, live)) {
			assertEquals(Collections.singletonList("count"), labels(session
				.completion("x = co", 6).get(10, TimeUnit.SECONDS)));
		}
		// Without an interpreter: nothing, and no server created.
		try (ScriptSession session = service.session(unserved)) {
			assertTrue(session.completion("x = co", 6).get(10, TimeUnit.SECONDS)
				.getItems().isEmpty());
		}
		assertTrue(SERVERS.isEmpty());
	}

	@Test
	public void testSupports() {
		assertTrue(service.supports(fake));
		assertFalse(service.supports((ScriptLanguage) null));
		// By LSP language.
		assertTrue(service.supports("fake"));
		assertFalse(service.supports("cobol"));
	}

	@Test
	public void testLaunch() {
		// A server, from the highest-priority plugin serving the
		// language: a new one, in the environment given.
		final Environment env = new Environment("/env/bin/fake", Collections
			.emptyList(), null);
		final LanguageServer server = service.launch("fake", env);
		assertSame(SERVERS.get(0), server);
		assertEquals("high", SERVERS.get(0).name);
		assertEquals("fake", SERVERS.get(0).languageId);
		assertSame(env, SERVERS.get(0).environment());
		// No plugin, no server.
		assertThrows(IllegalArgumentException.class, () -> service.launch("cobol",
			Environment.NONE));
	}

	// -- Helper methods --

	/** The server created i-th, once created. */
	private static FakeServer server(final int i) {
		waitFor(() -> SERVERS.size() > i);
		return SERVERS.get(i);
	}

	private static void waitFor(final BooleanSupplier condition) {
		final long deadline = System.currentTimeMillis() + 10_000;
		while (!condition.getAsBoolean()) {
			if (System.currentTimeMillis() > deadline) {
				throw new AssertionError("Timed out");
			}
			try {
				Thread.sleep(10);
			}
			catch (final InterruptedException exc) {
				throw new AssertionError(exc);
			}
		}
	}

	private static CompletionList complete(final LanguageServer server,
		final String text) throws Exception
	{
		final String uri = "untitled:/live";
		server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
			new TextDocumentItem(uri, "live", 1, text)));
		try {
			return server.getTextDocumentService().completion(new CompletionParams(
				new TextDocumentIdentifier(uri), Positions.position(text, text
					.length()))).get(10, TimeUnit.SECONDS).getRight();
		}
		finally {
			server.getTextDocumentService().didClose(new DidCloseTextDocumentParams(
				new TextDocumentIdentifier(uri)));
		}
	}

	private static List<String> labels(final CompletionList list) {
		return list.getItems().stream().map(CompletionItem::getLabel).collect(
			Collectors.toList());
	}

	private static List<String> messages(final List<Diagnostic> diagnostics) {
		return diagnostics.stream().map(d -> d.getMessage().getLeft()).collect(
			Collectors.toList());
	}

	private final class Client implements LanguageClient {

		@Override
		public void publishDiagnostics(final PublishDiagnosticsParams p) {
			published.add(p);
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
	}

	// -- A fake language, and its servers --

	@Plugin(type = ScriptLanguage.class, name = "Fake")
	public static class FakeLanguage extends AbstractScriptLanguage {

		@Override
		public ScriptEngine getScriptEngine() {
			return null;
		}

		@Override
		public List<String> getNames() {
			return Collections.singletonList("Fake");
		}

		@Override
		public List<String> getExtensions() {
			return Collections.singletonList("fake");
		}
	}

	/**
	 * Turns fake scripts into {@code fake} code; they run where a line
	 * {@code #env <interpreter>} says, if any.
	 */
	@Plugin(type = ScriptDialect.class)
	public static class FakeDialect implements ScriptDialect {

		@Override
		public boolean supports(final ScriptLanguage language) {
			return language instanceof FakeLanguage;
		}

		@Override
		public String languageId() {
			return "fake";
		}

		@Override
		public Environment environment(final String text, final File file) {
			for (final String line : text.split("\n")) {
				if (line.startsWith("#env ")) {
					return new Environment(line.substring(5), null, null);
				}
			}
			return Environment.NONE;
		}

		@Override
		public String runtimeType(final Class<?> type) {
			return type.getSimpleName();
		}

		@Override
		public String declare(final String name, final String type) {
			return name + ": " + type;
		}

		@Override
		public List<String> imports(final Collection<String> types) {
			return types.isEmpty() ? Collections.emptyList() : Collections
				.singletonList("import mass");
		}

		@Override
		public String imported(final String statement) {
			return statement.substring("import ".length());
		}
	}

	/** Serves the fake language (as {@code fake}): the one asked. */
	@Plugin(type = LanguageServerPlugin.class, priority = Priority.HIGH)
	public static class HighPlugin implements LanguageServerPlugin {

		@Override
		public boolean supports(final String languageId) {
			return "fake".equals(languageId);
		}


		@Override
		public LanguageServer create(final String languageId,
			final Environment environment)
		{
			return register(new FakeServer("high", languageId, environment));
		}
	}

	/** Serves the fake language too, at a lower priority: never asked. */
	@Plugin(type = LanguageServerPlugin.class, priority = Priority.LOW)
	public static class LowPlugin implements LanguageServerPlugin {

		@Override
		public boolean supports(final String languageId) {
			return "fake".equals(languageId);
		}


		@Override
		public LanguageServer create(final String languageId,
			final Environment environment)
		{
			return register(new FakeServer("low", languageId, environment));
		}
	}

	private static FakeServer register(final FakeServer s) {
		SERVERS.add(s);
		return s;
	}

	/** A fake in-process server, recording what it is told. */
	static class FakeServer implements LanguageServer, TextDocumentService,
		WorkspaceService, LanguageClientAware
	{

		final String name;
		/** The LSP language it was created for, if any. */
		final String languageId;
		final Environment environment;
		final List<String> events = new CopyOnWriteArrayList<>();
		/** The URIs of the documents opened, in order. */
		final List<String> opened = new CopyOnWriteArrayList<>();
		final List<Position> asked = new CopyOnWriteArrayList<>();
		LanguageClient client;

		FakeServer(final String name, final Environment environment) {
			this(name, null, environment);
		}

		FakeServer(final String name, final String languageId,
			final Environment environment)
		{
			this.name = name;
			this.languageId = languageId;
			this.environment = environment;
		}

		/** Where its scripts run. */
		Environment environment() {
			return environment;
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
		public void initialized(final InitializedParams params) {
			events.add("initialized");
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
			final TextDocumentItem d = p.getTextDocument();
			opened.add(d.getUri());
			events.add("open " + d.getVersion() + " " + d.getText());
			diagnose(d.getUri(), d.getVersion(), d.getText());
		}

		@Override
		public void didChange(final DidChangeTextDocumentParams p) {
			final String text = p.getContentChanges().get(0).getText();
			events.add("change " + p.getTextDocument().getVersion() + " " + text);
			diagnose(p.getTextDocument().getUri(), p.getTextDocument().getVersion(),
				text);
		}

		/** Complains about the text, unless it is "fine". */
		private void diagnose(final String uri, final int version,
			final String text)
		{
			if (client == null) return;
			if (text.equals("fine")) {
				client.publishDiagnostics(new PublishDiagnosticsParams(uri,
					Collections.emptyList(), version));
			}
			else publish(uri, version, text);
		}

		@Override
		public void didClose(final DidCloseTextDocumentParams p) {
			events.add("close");
		}

		@Override
		public void didSave(final DidSaveTextDocumentParams p) {}

		void publish(final String uri, final int version, final String message,
			final Range... ranges)
		{
			final List<Diagnostic> list = new ArrayList<>();
			final Range[] rs = ranges.length > 0 ? ranges : new Range[] { new Range(
				new Position(0, 0), new Position(0, 1)) };
			for (final Range r : rs) {
				list.add(new Diagnostic(r, ranges.length > 0 ? message : name + ": " +
					message));
			}
			client.publishDiagnostics(new PublishDiagnosticsParams(uri, list,
				version));
		}

		@Override
		public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
			completion(final CompletionParams p)
		{
			asked.add(p.getPosition());
			final Position at = p.getPosition();
			final Range word = new Range(new Position(at.getLine(), Math.max(0, at
				.getCharacter() - 2)), at);
			final List<CompletionItem> items = new ArrayList<>();
			for (final String label : new String[] { "max", "mass" }) {
				final CompletionItem item = new CompletionItem(label);
				item.setTextEdit(Either.forLeft(new TextEdit(word, label)));
				items.add(item);
			}
			return CompletableFuture.completedFuture(Either.forRight(
				new CompletionList(items)));
		}

		@Override
		public CompletableFuture<SignatureHelp> signatureHelp(
			final SignatureHelpParams p)
		{
			return CompletableFuture.completedFuture(new SignatureHelp(Collections
				.singletonList(new SignatureInformation("max(int a, int b)",
					(String) null, Arrays.asList(new ParameterInformation("int a"),
						new ParameterInformation("int b")))), 0, 1));
		}

		@Override
		public CompletableFuture<Hover> hover(final HoverParams p) {
			return CompletableFuture.completedFuture(new Hover(new MarkupContent(
				"plaintext", name + " hover")));
		}

		@Override
		public void didChangeConfiguration(final DidChangeConfigurationParams p) {}

		@Override
		public void didChangeWatchedFiles(final DidChangeWatchedFilesParams p) {}
	}
}
