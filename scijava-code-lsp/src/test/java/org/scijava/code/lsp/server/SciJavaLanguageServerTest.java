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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptEngine;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.code.api.AbstractCodeCompleterPlugin;
import org.scijava.code.api.CodeCompleterPlugin;
import org.scijava.code.api.CodeCompletionService;
import org.scijava.code.api.Completion;
import org.scijava.code.api.CompletionRequest;
import org.scijava.code.api.CompletionResult;
import org.scijava.code.api.Diagnostic;
import org.scijava.code.api.Hover;
import org.scijava.code.api.SignatureHelp;
import org.scijava.code.api.SignatureHelp.Fit;
import org.scijava.plugin.Plugin;
import org.scijava.script.AbstractScriptLanguage;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptService;

/**
 * Tests {@link SciJavaLanguageServer}, as an editor would use it, with a fake
 * language and completer.
 *
 * @author Gabriel Selzer
 */
public class SciJavaLanguageServerTest {

	private static final String URI = "file:///scripts/hello.fake";

	private Context context;
	private LanguageServer server;
	private final LinkedBlockingQueue<PublishDiagnosticsParams> diagnostics =
		new LinkedBlockingQueue<>();

	@BeforeEach
	public void setUp() throws Exception {
		context = new Context(ScriptService.class, CodeCompletionService.class);
		final SciJavaLanguageServer impl = new SciJavaLanguageServer(context);
		final PipedInputStream clientIn = new PipedInputStream(1 << 16);
		final PipedOutputStream serverOut = new PipedOutputStream(clientIn);
		final PipedInputStream serverIn = new PipedInputStream(1 << 16);
		final PipedOutputStream clientOut = new PipedOutputStream(serverIn);
		final Launcher<LanguageClient> serverSide = LSPLauncher
			.createServerLauncher(impl, serverIn, serverOut);
		impl.connect(serverSide.getRemoteProxy());
		serverSide.startListening();
		final Launcher<LanguageServer> clientSide = LSPLauncher
			.createClientLauncher(new Client(), clientIn, clientOut);
		clientSide.startListening();
		server = clientSide.getRemoteProxy();
		final InitializeResult init = server.initialize(new InitializeParams()).get(
			10, TimeUnit.SECONDS);
		assertTrue(init.getCapabilities().getHoverProvider().getLeft());
	}

	@AfterEach
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void testCompletion() throws Exception {
		open("x = thing.ma");
		final CompletionList list = server.getTextDocumentService().completion(
			new CompletionParams(new TextDocumentIdentifier(URI), new Position(0,
				12))).get(10, TimeUnit.SECONDS).getRight();
		// The completer's slower, better answer: awaited.
		assertEquals(2, list.getItems().size());
		final CompletionItem max = list.getItems().get(0);
		assertEquals("thing.max", max.getLabel());
		assertEquals(CompletionItemKind.Method, max.getKind());
		assertEquals("thing.max(double a, double b) -> double", max.getDetail());
		// Replacing what the completer said to.
		assertEquals(4, max.getTextEdit().getLeft().getRange().getStart()
			.getCharacter());
		assertEquals(12, max.getTextEdit().getLeft().getRange().getEnd()
			.getCharacter());
		// Documentation on demand, as plain text.
		final CompletionItem resolved = server.getTextDocumentService()
			.resolveCompletionItem(max).get(10, TimeUnit.SECONDS);
		assertEquals("The larger of a & b.", resolved.getDocumentation().getRight()
			.getValue());
	}

	@Test
	public void testSignatureHelpAndHover() throws Exception {
		open("thing.max(1, ");
		final org.eclipse.lsp4j.SignatureHelp help = server
			.getTextDocumentService().signatureHelp(new SignatureHelpParams(
				new TextDocumentIdentifier(URI), new Position(0, 13))).get(10,
					TimeUnit.SECONDS);
		assertEquals("thing.max(double a, double b) -> double", help.getSignatures()
			.get(0).getLabel());
		assertEquals(1, help.getActiveParameter());
		assertEquals(2, help.getSignatures().get(0).getParameters().size());

		final org.eclipse.lsp4j.Hover hover = server.getTextDocumentService()
			.hover(new HoverParams(new TextDocumentIdentifier(URI), new Position(0,
				7))).get(10, TimeUnit.SECONDS);
		assertEquals("max: the larger.", hover.getContents().getRight()
			.getValue());
		assertEquals(6, hover.getRange().getStart().getCharacter());
		assertEquals(9, hover.getRange().getEnd().getCharacter());
	}

	@Test
	public void testDiagnostics() throws Exception {
		open("ok\n!!\n");
		PublishDiagnosticsParams p = diagnostics.poll(10, TimeUnit.SECONDS);
		assertEquals(URI, p.getUri());
		assertEquals(1, p.getDiagnostics().size());
		assertEquals(1, p.getDiagnostics().get(0).getRange().getStart().getLine());
		assertEquals("Bangs", p.getDiagnostics().get(0).getMessage().getLeft());
		// Fixed: none.
		server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(
			new VersionedTextDocumentIdentifier(URI, 2), Collections.singletonList(
				new TextDocumentContentChangeEvent("ok\n"))));
		p = diagnostics.poll(10, TimeUnit.SECONDS);
		assertTrue(p.getDiagnostics().isEmpty());
	}

	@Test
	public void testUnknownLanguage() throws Exception {
		server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
			new TextDocumentItem("file:///x.cobol", "cobol", 1, "x")));
		assertTrue(server.getTextDocumentService().completion(new CompletionParams(
			new TextDocumentIdentifier("file:///x.cobol"), new Position(0, 1))).get(
				10, TimeUnit.SECONDS).getRight().getItems().isEmpty());
		assertNull(server.getTextDocumentService().hover(new HoverParams(
			new TextDocumentIdentifier("file:///x.cobol"), new Position(0, 0))).get(
				10, TimeUnit.SECONDS));
	}

	@Test
	public void testPlain() {
		assertEquals("a < b\nc", SciJavaLanguageServer.plain(
			"<pre>a &lt; b</pre><br>c"));
		assertNull(SciJavaLanguageServer.plain("<p></p>"));
	}

	private void open(final String text) {
		server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
			new TextDocumentItem(URI, "fake", 1, text)));
	}

	/** Collects the server's diagnostics. */
	private final class Client implements LanguageClient {

		@Override
		public void telemetryEvent(final Object object) {}

		@Override
		public void publishDiagnostics(final PublishDiagnosticsParams p) {
			diagnostics.add(p);
		}

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

	// -- A fake language, and its completer --

	@Plugin(type = ScriptLanguage.class)
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

	@Plugin(type = CodeCompleterPlugin.class)
	public static class FakeCompleter extends AbstractCodeCompleterPlugin {

		private static final Completion MAX = Completion.builder("thing.max").kind(
			Completion.Kind.METHOD).parameters(Arrays.asList(new Completion.Parameter(
				"a", "double"), new Completion.Parameter("b", "double"))).returnType(
					"double").description("<pre>The larger of a &amp; b.</pre>")
			.build();

		@Override
		public boolean supports(final ScriptLanguage language) {
			return language instanceof FakeLanguage;
		}

		@Override
		public CompletionResult complete(final CompletionRequest request) {
			final int start = request.text().lastIndexOf(' ', request.offset() -
				1) + 1;
			// At once: nothing; a moment later: more.
			return new CompletionResult(Collections.emptyList(), start).withUpdate(
				CompletableFuture.supplyAsync(() -> new CompletionResult(Arrays.asList(
					MAX, Completion.of("thing.mass")), start)));
		}

		@Override
		public SignatureHelp signatureHelp(final CompletionRequest request) {
			return new SignatureHelp(Collections.singletonList(
				new SignatureHelp.Signature(MAX, Fit.MATCH)), 1, request.text()
					.indexOf('('));
		}

		@Override
		public Hover hover(final CompletionRequest request) {
			return new Hover("max: the <i>larger</i>.", 6, 9);
		}

		@Override
		public CompletableFuture<List<Diagnostic>> diagnose(
			final CompletionRequest request)
		{
			final int bangs = request.text().indexOf("!!");
			return CompletableFuture.completedFuture(bangs < 0 ? Collections
				.emptyList() : Collections.singletonList(new Diagnostic(bangs, bangs +
					2, Diagnostic.Severity.ERROR, "Bangs", "fake")));
		}
	}
}
