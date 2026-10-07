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
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.code.lsp.Callable;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerPlugin;
import org.scijava.code.lsp.LanguageServerService;
import org.scijava.code.lsp.RatedSignatureInformation.Fit;
import org.scijava.code.lsp.ScriptLanguageServer;
import org.scijava.plugin.Plugin;
import org.scijava.script.AbstractScriptLanguage;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptService;

/**
 * Tests {@link SciJavaLanguageServer}, as an editor would use it, with a fake
 * language and server.
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
		context = new Context(ScriptService.class, LanguageServerService.class);
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
		// The server's slower answer: awaited.
		assertEquals(2, list.getItems().size());
		final CompletionItem max = list.getItems().get(0);
		assertEquals("thing.max", max.getLabel());
		assertEquals(CompletionItemKind.Method, max.getKind());
		assertEquals("(double a, double b)", max.getLabelDetails().getDetail());
		// Replacing what the server said to.
		assertEquals(4, max.getTextEdit().getLeft().getRange().getStart()
			.getCharacter());
		assertEquals(12, max.getTextEdit().getLeft().getRange().getEnd()
			.getCharacter());
		// Documentation on demand.
		final CompletionItem resolved = server.getTextDocumentService()
			.resolveCompletionItem(max).get(10, TimeUnit.SECONDS);
		assertEquals("The larger of a &amp; b.", resolved.getDocumentation()
			.getRight().getValue());
	}

	@Test
	public void testSignatureHelpAndHover() throws Exception {
		open("thing.max(1, ");
		final SignatureHelp help = server
			.getTextDocumentService().signatureHelp(new SignatureHelpParams(
				new TextDocumentIdentifier(URI), new Position(0, 13))).get(10,
					TimeUnit.SECONDS);
		assertEquals("thing.max(double a, double b) -> double", help.getSignatures()
			.get(0).getLabel());
		assertEquals(1, help.getActiveParameter());
		assertEquals(2, help.getSignatures().get(0).getParameters().size());

		final Hover hover = server.getTextDocumentService()
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

	@Plugin(type = LanguageServerPlugin.class)
	public static class FakeServerPlugin implements LanguageServerPlugin {

		@Override
		public boolean supports(final ScriptLanguage language) {
			return language instanceof FakeLanguage;
		}

		@Override
		public LanguageServer create(final Environment environment) {
			return new FakeServer();
		}
	}

	private static class FakeServer extends ScriptLanguageServer {

		private static final Callable MAX = new Callable("thing.max", Arrays.asList(
			new Callable.Param("a", "double"), new Callable.Param("b", "double")),
			"double");

		private FakeServer() {
			super(Environment.NONE);
		}

		@Override
		public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
			completion(final CompletionParams params)
		{
			// Slower than the merged server waits.
			return CompletableFuture.supplyAsync(() -> {
				try {
					Thread.sleep(200);
				}
				catch (final InterruptedException exc) {
					Thread.currentThread().interrupt();
				}
				return FakeServer.super.completion(params).join();
			});
		}

		@Override
		protected CompletionList complete(final Document doc, final int offset) {
			final int start = doc.text().lastIndexOf(' ', offset - 1) + 1;
			return list(doc, start, offset, Arrays.asList(describe(MAX.kind(
				CompletionItemKind.Method).toItem(), () -> "The larger of a &amp; b."),
				new CompletionItem("thing.mass")));
		}

		@Override
		protected SignatureHelp signatureHelp(final Document doc,
			final int offset)
		{
			return new SignatureHelp(Collections.singletonList(MAX.toSignature(
				Fit.MATCH)), 0, 1);
		}

		@Override
		protected Hover hover(final Document doc, final int offset) {
			final Hover hover = new Hover(new MarkupContent(MarkupKind.MARKDOWN,
				"max: the larger."));
			hover.setRange(doc.range(6, 9));
			return hover;
		}

		@Override
		protected List<Diagnostic> diagnose(final Document doc) {
			final int bangs = doc.text().indexOf("!!");
			return bangs < 0 ? Collections.emptyList() : Collections.singletonList(
				diagnostic(doc, bangs, bangs + 2, DiagnosticSeverity.Error, "Bangs",
					"fake"));
		}
	}
}
