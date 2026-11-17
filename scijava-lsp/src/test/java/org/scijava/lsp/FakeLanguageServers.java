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

package org.scijava.lsp;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
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
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.script.AbstractScriptLanguage;
import org.scijava.script.ScriptLanguage;

/**
 * A fake script language, its dialect, and plugins serving it with fake
 * in-process servers, for tests; and helpers.
 * <p>
 * NB: Its plugins are in every test context's plugin index; the servers they
 * create are recorded in {@link #SERVERS}, which tests clear first.
 * </p>
 *
 * @author Gabriel Selzer
 */
public final class FakeLanguageServers {

	private FakeLanguageServers() {
		// NB: Prevent instantiation of utility class.
	}

	/** The fake servers created, in order. */
	public static final List<FakeServer> SERVERS = new CopyOnWriteArrayList<>();

	/** The server created i-th, once created. */
	static FakeServer server(final int i) {
		waitFor(() -> SERVERS.size() > i);
		return SERVERS.get(i);
	}

	/** Waits (up to 10 seconds) for a condition to hold. */
	static void waitFor(final BooleanSupplier condition) {
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

	/** The labels of some completions, in order. */
	static List<String> labels(final CompletionList list) {
		return list.getItems().stream().map(CompletionItem::getLabel).collect(
			Collectors.toList());
	}

	/** A live interpreter's context, with a few variables. */
	static ScriptContext live() {
		final ScriptContext live = new SimpleScriptContext();
		live.setAttribute("text", "hello", ScriptContext.ENGINE_SCOPE);
		live.setAttribute("count", 3, ScriptContext.ENGINE_SCOPE);
		return live;
	}

	/** A script language no plugin serves, without a dialect. */
	static ScriptLanguage unserved() {
		return new AbstractScriptLanguage() {

			@Override
			public ScriptEngine getScriptEngine() {
				return null;
			}
		};
	}

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
	 * {@code #@script(env=<interpreter>)} says, if any.
	 */
	@Plugin(type = ScriptDialect.class)
	public static class FakeDialect implements ScriptDialect {

		@Override
		public boolean supports(final ScriptLanguage language) {
			return language instanceof FakeLanguage;
		}

		@Override
		public String outputLanguage() {
			return "fake";
		}

		@Override
		public Environment environment(final String text, final File file) {
			for (final String line : text.split("\n")) {
				if (line.startsWith("#@script(env=")) {
					final String env = line.substring(13, line.length() - 1);
					return new Environment().withInterpreter(env);
				}
			}
			return new Environment();
		}

		@Override
		public Header header(final Map<String, Variable> variables) {
			if (variables.isEmpty()) {
				return new Header("", Collections.emptySet());
			}
			final List<String> code = new ArrayList<>();
			code.add("import mass");
			variables.forEach((name, v) -> code.add(name + ": " + v.type()
				.getSimpleName()));
			return new Header(String.join("; ", code), Collections.singleton(
				"mass"));
		}
	}

	/** Serves the fake language (as {@code fake}): the one asked. */
	@Plugin(type = LanguageServerPlugin.class, priority = Priority.HIGH)
	public static class HighPlugin implements LanguageServerPlugin {

		/** The server to create next, if not a new one (e.g. a special one). */
		static volatile FakeServer next;

		@Override
		public boolean supports(final String language) {
			return "fake".equals(language);
		}


		@Override
		public LanguageServer create(final String language,
			final Environment environment)
		{
			final FakeServer special = next;
			return register(special != null ? special : new FakeServer("high",
				language, environment));
		}
	}

	/** Serves the fake language too, at a lower priority: never asked. */
	@Plugin(type = LanguageServerPlugin.class, priority = Priority.LOW)
	public static class LowPlugin implements LanguageServerPlugin {

		@Override
		public boolean supports(final String language) {
			return "fake".equals(language);
		}


		@Override
		public LanguageServer create(final String language,
			final Environment environment)
		{
			return register(new FakeServer("low", language, environment));
		}
	}

	private static FakeServer register(final FakeServer s) {
		SERVERS.add(s);
		return s;
	}

	/** A fake in-process server, recording what it is told. */
	public static class FakeServer implements LanguageServer, TextDocumentService,
		WorkspaceService, LanguageClientAware
	{

		public final String name;
		/** The LSP language it was created for, if any. */
		public final String language;
		final Environment environment;
		final List<String> events = new CopyOnWriteArrayList<>();
		/** The URIs of the documents opened, in order. */
		final List<String> opened = new CopyOnWriteArrayList<>();
		final List<Position> asked = new CopyOnWriteArrayList<>();
		LanguageClient client;

		FakeServer(final String name, final Environment environment) {
			this(name, null, environment);
		}

		FakeServer(final String name, final String language,
			final Environment environment)
		{
			this.name = name;
			this.language = language;
			this.environment = environment;
		}

		/** Where its scripts run. */
		public Environment environment() {
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
