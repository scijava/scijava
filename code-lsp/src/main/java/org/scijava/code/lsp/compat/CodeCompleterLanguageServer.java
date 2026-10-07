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

package org.scijava.code.lsp.compat;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionItemLabelDetails;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DiagnosticSeverity;
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
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.scijava.code.api.CodeCompleter;
import org.scijava.code.api.CodeCompletionService;
import org.scijava.code.api.Completion;
import org.scijava.code.api.CompletionRequest;
import org.scijava.code.api.CompletionResult;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LspClient;
import org.scijava.code.lsp.RatedSignatureInformation;
import org.scijava.code.lsp.RatedSignatureInformation.Fit;
import org.scijava.code.lsp.UpdatingCompletionList;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptService;

/**
 * Offers code-api's {@link CodeCompleter}s as an in-process language server,
 * until each language has a server of its own. TEMP: Removed with code-api.
 * <p>
 * A document's completer is its language's (by name, as its
 * {@link TextDocumentItem#getLanguageId() language identifier}). Callables
 * become snippets ({@code max(${1:a}, ${2:b})}) with their typed parameters as
 * label details ({@code (double a, double b)}); descriptions (HTML) become
 * Markdown documentation, which allows HTML.
 * </p>
 *
 * @author Gabriel Selzer
 */
public class CodeCompleterLanguageServer implements LanguageServer,
	TextDocumentService, WorkspaceService, LanguageClientAware
{

	/** How many completions to remember, for resolving their documentation. */
	private static final int REMEMBERED = 5000;

	/**
	 * How long a document must stay unchanged before its problems are looked
	 * for, in milliseconds. (Editors send every keystroke.)
	 */
	private static final long PAUSE = Long.getLong("scijava.lsp.pause", 300);

	private static final ScheduledExecutorService TIMER = Executors
		.newSingleThreadScheduledExecutor(r -> {
			final Thread t = new Thread(r, "scijava-completer-pause");
			t.setDaemon(true);
			return t;
		});

	private final CodeCompletionService completion;
	private final ScriptService scripts;
	private final Environment environment;

	/** A completer for all documents, if given (instead of each language's). */
	private final CodeCompleter fixed;
	private final ScriptLanguage fixedLanguage;
	private volatile LanguageClient client;

	private final Map<String, Doc> docs = new ConcurrentHashMap<>();
	private final Map<Integer, Completion> remembered = new ConcurrentHashMap<>();
	private final AtomicInteger ids = new AtomicInteger();

	public CodeCompleterLanguageServer(final CodeCompletionService completion,
		final ScriptService scripts, final Environment environment)
	{
		this.completion = completion;
		this.scripts = scripts;
		this.environment = environment;
		fixed = null;
		fixedLanguage = null;
	}

	/**
	 * Offers one completer, for documents of any language identifier, e.g. in
	 * tests.
	 *
	 * @param completer The completer.
	 * @param language The language of the documents (may be null).
	 */
	public CodeCompleterLanguageServer(final CodeCompleter completer,
		final ScriptLanguage language)
	{
		completion = null;
		scripts = null;
		environment = Environment.NONE;
		fixed = completer;
		fixedLanguage = language;
	}

	@Override
	public void connect(final LanguageClient c) {
		client = c;
	}

	// -- LanguageServer --

	@Override
	public CompletableFuture<InitializeResult> initialize(
		final InitializeParams params)
	{
		final ServerCapabilities capabilities = new ServerCapabilities();
		capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
		capabilities.setCompletionProvider(new CompletionOptions(true,
			Collections.singletonList(".")));
		capabilities.setSignatureHelpProvider(new SignatureHelpOptions(Arrays
			.asList("(", ",")));
		capabilities.setHoverProvider(true);
		return CompletableFuture.completedFuture(new InitializeResult(
			capabilities));
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

	// -- TextDocumentService: documents --

	@Override
	public void didOpen(final DidOpenTextDocumentParams params) {
		final TextDocumentItem item = params.getTextDocument();
		final Doc doc = new Doc(item.getUri(), fixed != null ? fixedLanguage
			: scripts.getLanguageByName(item.getLanguageId()), item.getText(), item
				.getVersion());
		docs.put(item.getUri(), doc);
		if (!served(doc)) return;
		final CompletionRequest request = request(doc, doc.text.length());
		completer(doc).prepare(request);
		diagnose(doc);
	}

	@Override
	public void didChange(final DidChangeTextDocumentParams params) {
		final Doc doc = docs.get(params.getTextDocument().getUri());
		if (doc == null || params.getContentChanges().isEmpty()) return;
		doc.text = params.getContentChanges().get(params.getContentChanges()
			.size() - 1).getText();
		doc.version = params.getTextDocument().getVersion();
		if (served(doc)) diagnose(doc);
	}

	@Override
	public void didClose(final DidCloseTextDocumentParams params) {
		final Doc doc = docs.remove(params.getTextDocument().getUri());
		if (doc != null && served(doc)) completer(doc).closed(request(doc, 0));
	}

	@Override
	public void didSave(final DidSaveTextDocumentParams params) {}

	// -- TextDocumentService: requests --

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
		completion(final CompletionParams params)
	{
		final Doc doc = docs.get(params.getTextDocument().getUri());
		if (doc == null || !served(doc)) {
			return CompletableFuture.completedFuture(Either.forRight(
				new CompletionList()));
		}
		final String text = doc.text;
		final int offset = LspClient.offset(text, params.getPosition());
		final CompletionResult result = completer(doc).complete(request(doc,
			offset));
		final CompletionList list = list(result, text, offset);
		if (result.update() == null) {
			return CompletableFuture.completedFuture(Either.forRight(list));
		}
		final CompletableFuture<CompletionList> update = result.update()
			.thenApply(better -> better == null ? null : list(better, text,
				offset));
		return CompletableFuture.completedFuture(Either.forRight(
			new UpdatingCompletionList(list.getItems(), update)));
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(
		final CompletionItem item)
	{
		final Completion c = remembered.get(id(item.getData()));
		if (c == null) return CompletableFuture.completedFuture(item);
		return CompletableFuture.supplyAsync(() -> {
			final String doc = c.description();
			if (doc != null && !doc.isEmpty()) {
				item.setDocumentation(new MarkupContent(MarkupKind.MARKDOWN, doc));
			}
			return item;
		});
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(
		final SignatureHelpParams params)
	{
		final Doc doc = docs.get(params.getTextDocument().getUri());
		if (doc == null || !served(doc)) {
			return CompletableFuture.completedFuture(null);
		}
		final org.scijava.code.api.SignatureHelp h = completer(doc).signatureHelp(
			request(doc, LspClient.offset(doc.text, params.getPosition())));
		if (h == null) return CompletableFuture.completedFuture(null);
		final CompletableFuture<org.scijava.code.api.SignatureHelp> settled = h
			.update() == null ? CompletableFuture.completedFuture(h) : h.update()
				.handle((better, error) -> better != null ? better : h);
		return settled.thenApply(CodeCompleterLanguageServer::signatureHelp);
	}

	@Override
	public CompletableFuture<Hover> hover(final HoverParams params) {
		final Doc doc = docs.get(params.getTextDocument().getUri());
		if (doc == null || !served(doc)) {
			return CompletableFuture.completedFuture(null);
		}
		final String text = doc.text;
		final org.scijava.code.api.Hover h = completer(doc).hover(request(doc,
			LspClient.offset(text, params.getPosition())));
		if (h == null) return CompletableFuture.completedFuture(null);
		final CompletableFuture<org.scijava.code.api.Hover> settled = h
			.isEmpty() && h.update() != null ? h.update().handle((better,
				error) -> better != null ? better : h) : CompletableFuture
					.completedFuture(h);
		return settled.thenApply(x -> {
			if (x == null || x.isEmpty()) return null;
			final Hover out = new Hover(new MarkupContent(MarkupKind.MARKDOWN, x
				.text()));
			if (x.start() >= 0 && x.end() >= x.start()) {
				out.setRange(new Range(LspClient.position(text, Math.min(x.start(),
					text.length())), LspClient.position(text, Math.min(x.end(), text
						.length()))));
			}
			return out;
		});
	}

	// -- WorkspaceService --

	@Override
	public void didChangeConfiguration(final DidChangeConfigurationParams p) {}

	@Override
	public void didChangeWatchedFiles(final DidChangeWatchedFilesParams p) {}

	// -- Helper methods --

	private CodeCompleter completer(final Doc doc) {
		return fixed != null ? fixed : completion.getCompleter(doc.language);
	}

	private boolean served(final Doc doc) {
		return fixed != null || doc.language != null;
	}

	private CompletionRequest request(final Doc doc, final int offset) {
		return new CompletionRequest(doc.text, Math.min(offset, doc.text.length()),
			doc.language, null, environment.live(), path(doc.uri));
	}

	/** Looks for a document's problems a moment after it last changed. */
	private void diagnose(final Doc doc) {
		synchronized (doc) {
			if (doc.pending != null) doc.pending.cancel(false);
			doc.pending = TIMER.schedule(() -> diagnoseNow(doc), PAUSE,
				TimeUnit.MILLISECONDS);
		}
	}

	private void diagnoseNow(final Doc doc) {
		final String text = doc.text;
		final int version = doc.version;
		completer(doc).diagnose(request(doc, text.length())).thenAccept(found -> {
			final LanguageClient c = client;
			// NB: Only if still current.
			if (c == null || docs.get(doc.uri) != doc || doc.version != version) {
				return;
			}
			final List<org.eclipse.lsp4j.Diagnostic> out = new ArrayList<>();
			for (final org.scijava.code.api.Diagnostic d : found) {
				final org.eclipse.lsp4j.Diagnostic diagnostic =
					new org.eclipse.lsp4j.Diagnostic();
				diagnostic.setRange(new Range(LspClient.position(text, Math.min(d
					.start(), text.length())), LspClient.position(text, Math.min(d.end(),
						text.length()))));
				diagnostic.setMessage(d.message());
				diagnostic.setSeverity(severity(d.severity()));
				diagnostic.setSource(d.source());
				out.add(diagnostic);
			}
			c.publishDiagnostics(new PublishDiagnosticsParams(doc.uri, out,
				version));
		});
	}

	/** Converts a completion result, as an edit of the given text. */
	private CompletionList list(final CompletionResult result, final String text,
		final int offset)
	{
		final Range range = new Range(LspClient.position(text, Math.max(0, Math
			.min(result.replaceStart(), offset))), LspClient.position(text,
				offset));
		// NB: By relevance where given, otherwise in the completer's order.
		final List<Completion> completions = new ArrayList<>(result.completions());
		completions.sort(Comparator.comparingDouble(c -> -c.relevance()));
		final List<CompletionItem> items = new ArrayList<>();
		for (int i = 0; i < completions.size(); i++) {
			items.add(item(completions.get(i), i, range, text));
		}
		return new CompletionList(false, items);
	}

	private CompletionItem item(final Completion c, final int index,
		final Range range, final String text)
	{
		final CompletionItem item = new CompletionItem(c.displayText());
		item.setKind(kind(c.kind()));
		item.setFilterText(c.insertionText());
		item.setSortText(String.format("%06d", index));
		item.setDetail(c.summary());
		String newText = c.insertionText();
		if (c.isCallable()) {
			// Parameters: as tab stops, and (typed) as label details.
			final String name = stripParens(c.insertionText());
			final StringBuilder snippet = new StringBuilder(escape(name)).append(
				'(');
			final StringBuilder typed = new StringBuilder("(");
			final List<Completion.Parameter> params = c.parameters();
			for (int i = 0; i < params.size(); i++) {
				final Completion.Parameter p = params.get(i);
				if (i > 0) {
					snippet.append(", ");
					typed.append(", ");
				}
				final String pname = p.name() != null ? p.name() : "arg" + i;
				snippet.append("${").append(i + 1).append(':').append(escape(pname))
					.append('}');
				typed.append(p);
			}
			newText = snippet.append(')').toString();
			item.setInsertTextFormat(InsertTextFormat.Snippet);
			final CompletionItemLabelDetails details =
				new CompletionItemLabelDetails();
			details.setDetail(typed.append(')').toString());
			details.setDescription(c.returnType());
			item.setLabelDetails(details);
		}
		item.setTextEdit(Either.forLeft(new TextEdit(range, newText)));
		if (!c.additionalEdits().isEmpty()) {
			final List<TextEdit> edits = new ArrayList<>();
			for (final Completion.TextEdit e : c.additionalEdits()) {
				edits.add(new TextEdit(new Range(LspClient.position(text, e.start()),
					LspClient.position(text, e.end())), e.newText()));
			}
			item.setAdditionalTextEdits(edits);
		}
		// Remembered, for its documentation, on demand.
		final int id = ids.incrementAndGet();
		if (remembered.size() > REMEMBERED) remembered.clear();
		remembered.put(id, c);
		item.setData(id);
		return item;
	}

	private static SignatureHelp signatureHelp(
		final org.scijava.code.api.SignatureHelp h)
	{
		if (h == null || h.isEmpty()) return null;
		final List<SignatureInformation> signatures = new ArrayList<>();
		for (final org.scijava.code.api.SignatureHelp.Signature s : h
			.signatures())
		{
			final Completion c = s.callable();
			final List<ParameterInformation> params = new ArrayList<>();
			final StringBuilder label = new StringBuilder(stripParens(c
				.insertionText())).append('(');
			for (int i = 0; i < c.parameters().size(); i++) {
				final String p = String.valueOf(c.parameters().get(i));
				if (i > 0) label.append(", ");
				label.append(p);
				params.add(new ParameterInformation(p));
			}
			label.append(')');
			if (c.returnType() != null) label.append(" -> ").append(c
				.returnType());
			signatures.add(new RatedSignatureInformation(label.toString(), params,
				Fit.valueOf(s.fit().name())));
		}
		return new SignatureHelp(signatures, 0, Math.max(0, h
			.activeParameter()));
	}

	private static int id(final Object data) {
		if (data instanceof Number) return ((Number) data).intValue();
		if (data instanceof com.google.gson.JsonPrimitive) {
			return ((com.google.gson.JsonPrimitive) data).getAsInt();
		}
		return -1;
	}

	/** The file path of a {@code file:} URI, or null. */
	private static String path(final String uri) {
		try {
			final URI u = URI.create(uri);
			return "file".equals(u.getScheme()) ? new File(u).getPath() : null;
		}
		catch (final IllegalArgumentException exc) {
			return null;
		}
	}

	private static String stripParens(final String name) {
		return name.endsWith("()") ? name.substring(0, name.length() - 2) : name;
	}

	/** Escapes a snippet's special characters. */
	private static String escape(final String s) {
		return s.replace("\\", "\\\\").replace("$", "\\$").replace("}", "\\}");
	}

	private static CompletionItemKind kind(final Completion.Kind kind) {
		switch (kind) {
			case METHOD:
				return CompletionItemKind.Method;
			case FIELD:
				return CompletionItemKind.Field;
			case CLASS:
				return CompletionItemKind.Class;
			case PACKAGE:
			case IMPORT:
				return CompletionItemKind.Module;
			case KEYWORD:
				return CompletionItemKind.Keyword;
			case VARIABLE:
				return CompletionItemKind.Variable;
			case SNIPPET:
				return CompletionItemKind.Snippet;
			default:
				return CompletionItemKind.Text;
		}
	}

	private static DiagnosticSeverity severity(
		final org.scijava.code.api.Diagnostic.Severity s)
	{
		switch (s) {
			case WARNING:
				return DiagnosticSeverity.Warning;
			case INFO:
				return DiagnosticSeverity.Information;
			case HINT:
				return DiagnosticSeverity.Hint;
			default:
				return DiagnosticSeverity.Error;
		}
	}

	private static final class Doc {

		private final String uri;
		private final ScriptLanguage language;
		private volatile String text;
		private volatile int version;
		private ScheduledFuture<?> pending;

		private Doc(final String uri, final ScriptLanguage language,
			final String text, final int version)
		{
			this.uri = uri;
			this.language = language;
			this.text = text;
			this.version = version;
		}
	}
}
