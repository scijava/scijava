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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
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
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
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
import org.scijava.code.api.CodeCompletionService;
import org.scijava.code.api.ScriptDialect;
import org.scijava.code.api.ScriptDocument;
import org.scijava.code.lsp.RatedSignatureInformation.Fit;
import org.scijava.plugin.Plugin;
import org.scijava.script.AbstractScriptLanguage;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptService;

/**
 * Tests {@link LanguageServerService} and {@link MergedLanguageServer}, with
 * fake in-process servers; and {@link TransformingLanguageServer}, and the
 * code-api adapter.
 *
 * @author Gabriel Selzer
 */
public class LanguageServersTest {

	private static final String URI = "untitled:/a.merge";

	/** The fake servers created, by name ("fast", "slow", "env:A", ...). */
	static final Map<String, FakeServer> SERVERS = new LinkedHashMap<>();

	private Context context;
	private LanguageServerService service;
	private ScriptLanguage merge;
	private final List<PublishDiagnosticsParams> published =
		new CopyOnWriteArrayList<>();

	@BeforeEach
	public void setUp() {
		synchronized (SERVERS) {
			SERVERS.clear();
		}
		context = new Context(ScriptService.class, CodeCompletionService.class,
			LanguageServerService.class);
		service = context.service(LanguageServerService.class);
		merge = context.service(ScriptService.class).getLanguageByName("Merge");
	}

	@AfterEach
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void testCompletionsMerged() throws Exception {
		final MergedLanguageServer server = open("x.ma");
		final CompletionList first = complete(server, 0, 4);
		// The fast server's at once (by label, having no sortText); the slow
		// one's to come.
		assertTrue(first instanceof UpdatingCompletionList);
		assertEquals(Arrays.asList("mass", "max", "maz", "maz", "maz"), labels(
			first));
		final CompletionList all = ((UpdatingCompletionList) first).update().get(10,
			TimeUnit.SECONDS);
		// In priority order; "max" once (the slow server's is the same as the
		// fast one's); all of the fast server's "maz" (its own, even the same
		// twice, e.g. a method inherited twice).
		assertEquals(Arrays.asList("mass", "max", "maz", "maz", "maz", "map"),
			labels(all));
		final List<String> sorted = all.getItems().stream().map(
			CompletionItem::getSortText).collect(Collectors.toList());
		final List<String> resorted = new ArrayList<>(sorted);
		Collections.sort(resorted);
		assertEquals(sorted, resorted);

		// Resolving asks the server the completion came from, with its own data.
		final CompletionItem map = all.getItems().get(5);
		final CompletionItem resolved = server.resolveCompletionItem(map).get(10,
			TimeUnit.SECONDS);
		assertEquals("slow resolved map (data 3)", resolved.getDetail());
		assertEquals(map.getData(), resolved.getData());
	}

	@Test
	public void testDocumentSync() throws Exception {
		final MergedLanguageServer server = open("x = 1");
		change(server, "x = 2", 2);
		awaitEvents("fast", 2);
		assertEquals(Arrays.asList("open 1 x = 1", "change 2 x = 2"), SERVERS.get(
			"fast").events);
		awaitEvents("slow", 2);
		assertEquals(SERVERS.get("fast").events, SERVERS.get("slow").events);
		server.didClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(
			URI)));
		awaitEvents("fast", 3);
		assertEquals("close", SERVERS.get("fast").events.get(2));
	}

	@Test
	public void testEnvironmentMove() throws Exception {
		final MergedLanguageServer server = open("y = 1");
		awaitEvents("env:A", 1);
		// The script now runs elsewhere: it moves to that environment's server.
		change(server, "#env=B\ny = 1", 2);
		awaitEvents("env:A", 2);
		awaitEvents("env:B", 1);
		assertEquals("close", SERVERS.get("env:A").events.get(1));
		assertEquals("open 2 #env=B\ny = 1", SERVERS.get("env:B").events.get(0));
	}

	@Test
	public void testLiveEnvironment() throws Exception {
		final ScriptContext live = new SimpleScriptContext();
		final MergedLanguageServer server = service.server(merge, live);
		server.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(URI,
			"merge", 1, "z")));
		awaitEvents("env:A", 1);
		assertSame(live, SERVERS.get("env:A").environment.live());
	}

	@Test
	public void testDiagnostics() throws Exception {
		final MergedLanguageServer server = service.server(merge);
		server.connect(new Client());
		server.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(URI,
			"merge", 1, "oops")));
		server.awaitDiagnostics(URI).get(10, TimeUnit.SECONDS);
		// All servers' problems, together.
		assertEquals(Arrays.asList("fast: oops", "slow: oops"), messages(server
			.diagnostics(URI)));
		final PublishDiagnosticsParams last = published.get(published.size() - 1);
		assertEquals(2, last.getDiagnostics().size());

		// Problems of an earlier version are out of date.
		change(server, "fine", 2);
		server.awaitDiagnostics(URI).get(10, TimeUnit.SECONDS);
		SERVERS.get("slow").publish(URI, 1, "stale");
		assertEquals(Collections.emptyList(), messages(server.diagnostics(URI)));

		// For tools: diagnose and wait.
		assertEquals(Arrays.asList("fast: bad", "slow: bad"), messages(service
			.diagnose(merge, "untitled:/check.merge", "bad", 10_000)));
	}

	@Test
	public void testSignatureHelpAndHover() throws Exception {
		final MergedLanguageServer server = open("max(1, ");
		// The fast server has none: the slow one's.
		final SignatureHelp help = server.signatureHelp(new SignatureHelpParams(
			new TextDocumentIdentifier(URI), new Position(0, 7))).get(10,
				TimeUnit.SECONDS);
		assertEquals("max(int a, int b)", help.getSignatures().get(0).getLabel());
		assertEquals(Fit.CONVERSION, RatedSignatureInformation.fitOf(help
			.getSignatures().get(0)));
		assertEquals(Fit.UNKNOWN, RatedSignatureInformation.fitOf(
			new org.eclipse.lsp4j.SignatureInformation("plain")));
		// Both have hover: the first's.
		final Hover hover = server.hover(new HoverParams(new TextDocumentIdentifier(
			URI), new Position(0, 1))).get(10, TimeUnit.SECONDS);
		assertEquals("fast hover", hover.getContents().getRight().getValue());
	}

	@Test
	public void testTransforming() throws Exception {
		final FakeServer inner = new FakeServer("inner", Environment.NONE, false);
		final ScriptDialect dialect = new ScriptDialect() {

			@Override
			public String runtimeType(final Class<?> type) {
				return "float";
			}

			@Override
			public String declare(final String name, final String type) {
				return name + ": " + type;
			}
		};
		final Map<String, Class<?>> params = Collections.singletonMap("sigma",
			double.class);
		final TransformingLanguageServer server = new TransformingLanguageServer(
			inner, text -> ScriptDocument.of(text, params, dialect, null));
		server.connect(new Client());
		server.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(URI,
			"merge", 1, "x.ma")));
		// The server sees the transformed script.
		assertEquals("open 1 sigma: float\nx.ma", inner.events.get(0));
		final CompletionList list = server.completion(new CompletionParams(
			new TextDocumentIdentifier(URI), new Position(0, 4))).get(10,
				TimeUnit.SECONDS).getRight();
		// Asked one line down; its edits mapped back up.
		assertEquals(new Position(1, 4), inner.asked.get(0));
		assertEquals(new Range(new Position(0, 2), new Position(0, 4)), list
			.getItems().get(0).getTextEdit().getLeft().getRange());
		// Diagnostics mapped back; those in the added line left out.
		inner.publish(URI, 1, "oops", new Range(new Position(0, 0), new Position(0,
			5)), new Range(new Position(1, 0), new Position(1, 1)));
		final PublishDiagnosticsParams p = published.get(published.size() - 1);
		assertEquals(1, p.getDiagnostics().size());
		assertEquals(new Range(new Position(0, 0), new Position(0, 1)), p
			.getDiagnostics().get(0).getRange());
	}

	@Test
	public void testCodeCompleterAdapter() throws Exception {
		// The "Fake" language's completer (see SciJavaLanguageServerTest), via
		// the adapter.
		final ScriptLanguage fake = context.service(ScriptService.class)
			.getLanguageByName("Fake");
		final MergedLanguageServer server = service.server(fake);
		server.connect(new Client());
		final String uri = "file:///scripts/hello.fake";
		server.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri,
			"fake", 1, "x = thing.ma")));
		CompletionList list = server.completion(new CompletionParams(
			new TextDocumentIdentifier(uri), new Position(0, 12))).get(10,
				TimeUnit.SECONDS).getRight();
		if (list instanceof UpdatingCompletionList) {
			list = ((UpdatingCompletionList) list).update().get(10,
				TimeUnit.SECONDS);
		}
		final CompletionItem max = list.getItems().get(0);
		assertEquals("thing.max", max.getLabel());
		// A callable: a snippet, with its typed parameters as label details.
		assertEquals("thing.max(${1:a}, ${2:b})", max.getTextEdit().getLeft()
			.getNewText());
		assertEquals(new Range(new Position(0, 4), new Position(0, 12)), max
			.getTextEdit().getLeft().getRange());
		assertEquals("(double a, double b)", max.getLabelDetails().getDetail());
		assertEquals("double", max.getLabelDetails().getDescription());
		final CompletionItem resolved = server.resolveCompletionItem(max).get(10,
			TimeUnit.SECONDS);
		assertEquals("<pre>The larger of a &amp; b.</pre>", resolved
			.getDocumentation().getRight().getValue());

		final SignatureHelp help = server.signatureHelp(new SignatureHelpParams(
			new TextDocumentIdentifier(uri), new Position(0, 12))).get(10,
				TimeUnit.SECONDS);
		assertEquals("thing.max(double a, double b) -> double", help
			.getSignatures().get(0).getLabel());
		assertEquals(Fit.MATCH, RatedSignatureInformation.fitOf(help
			.getSignatures().get(0)));
		assertEquals(1, help.getActiveParameter());
		assertNotNull(server.hover(new HoverParams(new TextDocumentIdentifier(uri),
			new Position(0, 7))).get(10, TimeUnit.SECONDS));

		server.didChange(new DidChangeTextDocumentParams(
			new VersionedTextDocumentIdentifier(uri, 2), Collections.singletonList(
				new TextDocumentContentChangeEvent("y = !!"))));
		server.awaitDiagnostics(uri).get(10, TimeUnit.SECONDS);
		assertEquals(Collections.singletonList("Bangs"), messages(server
			.diagnostics(uri)));
	}

	@Test
	public void testSupports() {
		assertTrue(service.supports(merge));
		assertFalse(service.server(merge).language() == null);
		assertNull(service.launch("cobol", Environment.NONE));
	}

	// -- Helper methods --

	private MergedLanguageServer open(final String text) {
		final MergedLanguageServer server = service.server(merge);
		server.connect(new Client());
		server.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(URI,
			"merge", 1, text)));
		return server;
	}

	private static void change(final MergedLanguageServer server,
		final String text, final int version)
	{
		server.didChange(new DidChangeTextDocumentParams(
			new VersionedTextDocumentIdentifier(URI, version), Collections
				.singletonList(new TextDocumentContentChangeEvent(text))));
	}

	private static CompletionList complete(final MergedLanguageServer server,
		final int line, final int character) throws Exception
	{
		return server.completion(new CompletionParams(new TextDocumentIdentifier(
			URI), new Position(line, character))).get(10, TimeUnit.SECONDS)
			.getRight();
	}

	private static List<String> labels(final CompletionList list) {
		return list.getItems().stream().map(CompletionItem::getLabel).collect(
			Collectors.toList());
	}

	private static List<String> messages(final List<Diagnostic> diagnostics) {
		return diagnostics.stream().map(d -> d.getMessage().getLeft()).collect(
			Collectors.toList());
	}

	private static void awaitEvents(final String server, final int n)
		throws InterruptedException
	{
		final long end = System.currentTimeMillis() + 10_000;
		while (true) {
			final FakeServer s;
			synchronized (SERVERS) {
				s = SERVERS.get(server);
			}
			if (s != null && s.events.size() >= n) return;
			if (System.currentTimeMillis() > end) throw new AssertionError(
				"Timed out waiting for " + server);
			Thread.sleep(10);
		}
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

	@Plugin(type = ScriptLanguage.class)
	public static class MergeLanguage extends AbstractScriptLanguage {

		@Override
		public ScriptEngine getScriptEngine() {
			return null;
		}

		@Override
		public List<String> getNames() {
			return Collections.singletonList("Merge");
		}

		@Override
		public List<String> getExtensions() {
			return Collections.singletonList("merge");
		}
	}

	/** Answers at once: max and mass; hover; no signatures. */
	@Plugin(type = LanguageServerPlugin.class, priority = Priority.HIGH)
	public static class FastPlugin implements LanguageServerPlugin {

		@Override
		public boolean supports(final ScriptLanguage language) {
			return language instanceof MergeLanguage;
		}

		@Override
		public LanguageServer create(final Environment environment) {
			return register(new FakeServer("fast", environment, false));
		}
	}

	/** Answers slowly: max (again) and map; signatures; hover. */
	@Plugin(type = LanguageServerPlugin.class, priority = Priority.NORMAL)
	public static class SlowPlugin implements LanguageServerPlugin {

		@Override
		public boolean supports(final ScriptLanguage language) {
			return language instanceof MergeLanguage;
		}

		@Override
		public LanguageServer create(final Environment environment) {
			return register(new FakeServer("slow", environment, true));
		}
	}

	/** One server per environment: "#env=B" in the script means B. */
	@Plugin(type = LanguageServerPlugin.class, priority = Priority.LOW)
	public static class EnvPlugin implements LanguageServerPlugin {

		@Override
		public boolean supports(final ScriptLanguage language) {
			return language instanceof MergeLanguage;
		}

		@Override
		public Environment environment(final TextDocumentItem document) {
			return new Environment(document.getText().contains("#env=B") ? "B"
				: "A", null, null, null);
		}

		@Override
		public LanguageServer create(final Environment environment) {
			final FakeServer s = new FakeServer("env:" + environment.interpreter(),
				environment, false);
			s.silent = true;
			return register(s);
		}
	}

	private static FakeServer register(final FakeServer s) {
		synchronized (SERVERS) {
			SERVERS.put(s.name, s);
		}
		return s;
	}

	/** A fake in-process server, recording what it is told. */
	static class FakeServer implements LanguageServer, TextDocumentService,
		WorkspaceService, LanguageClientAware
	{

		final String name;
		final Environment environment;
		final boolean slow;
		boolean silent;
		final List<String> events = new CopyOnWriteArrayList<>();
		final List<Position> asked = new CopyOnWriteArrayList<>();
		LanguageClient client;

		FakeServer(final String name, final Environment environment,
			final boolean slow)
		{
			this.name = name;
			this.environment = environment;
			this.slow = slow;
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
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void exit() {}

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

		/** Complains about the text; unless silent, or the text is "fine". */
		private void diagnose(final String uri, final int version,
			final String text)
		{
			if (client == null) return;
			if (silent || text.equals("fine")) {
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
			for (final String label : slow ? new String[] { "max", "map" }
				: new String[] { "max", "mass" })
			{
				final CompletionItem item = new CompletionItem(label);
				item.setTextEdit(Either.forLeft(new TextEdit(word, label)));
				item.setData(label.length());
				items.add(item);
			}
			if (!slow && name.equals("fast")) {
				// Overloads: the same name, different parameters.
				for (final String params : new String[] { "(int a)", "(int a)",
					"(double a)" })
				{
					final CompletionItem item = new CompletionItem("maz");
					item.setTextEdit(Either.forLeft(new TextEdit(word, "maz")));
					final org.eclipse.lsp4j.CompletionItemLabelDetails details =
						new org.eclipse.lsp4j.CompletionItemLabelDetails();
					details.setDetail(params);
					item.setLabelDetails(details);
					items.add(item);
				}
			}
			final Either<List<CompletionItem>, CompletionList> answer = Either
				.forRight(new CompletionList(items));
			if (!slow) return CompletableFuture.completedFuture(answer);
			return CompletableFuture.supplyAsync(() -> {
				try {
					Thread.sleep(200);
				}
				catch (final InterruptedException exc) {
					Thread.currentThread().interrupt();
				}
				return answer;
			});
		}

		@Override
		public CompletableFuture<CompletionItem> resolveCompletionItem(
			final CompletionItem item)
		{
			item.setDetail(name + " resolved " + item.getLabel() + " (data " + item
				.getData() + ")");
			return CompletableFuture.completedFuture(item);
		}

		@Override
		public CompletableFuture<SignatureHelp> signatureHelp(
			final SignatureHelpParams p)
		{
			if (!slow) return CompletableFuture.completedFuture(null);
			return CompletableFuture.completedFuture(new SignatureHelp(Collections
				.singletonList(new RatedSignatureInformation("max(int a, int b)", Arrays
					.asList(new ParameterInformation("int a"), new ParameterInformation(
						"int b")), Fit.CONVERSION)), 0, 1));
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
