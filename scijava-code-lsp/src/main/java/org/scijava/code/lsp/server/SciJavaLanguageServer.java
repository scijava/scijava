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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
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
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ServerInfo;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.scijava.Context;
import org.scijava.code.api.CodeCompleter;
import org.scijava.code.api.CodeCompletionService;
import org.scijava.code.api.Completion;
import org.scijava.code.api.CompletionRequest;
import org.scijava.code.api.CompletionResult;
import org.scijava.code.lsp.LspClient;
import org.scijava.log.LogService;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptService;

/**
 * A language server for SciJava scripts, for editors outside SciJava (e.g. VS
 * Code): the completions, signature help, hover and diagnostics of SciJava's
 * code completers (see {@link CodeCompletionService}), for every script
 * language they support, with SciJava's own understanding of scripts (e.g.
 * their {@code #@} parameters).
 * <p>
 * The language of a document is its file extension's, as the
 * {@link ScriptService} knows it. Slower answers (see
 * {@link CompletionResult#update()}) are awaited a while, since an external
 * editor shows nothing in between.
 * </p>
 *
 * @author Gabriel Selzer
 * @see Main
 */
public class SciJavaLanguageServer implements LanguageServer,
	TextDocumentService, WorkspaceService, LanguageClientAware
{

	/** How long to wait for slower, better answers, in milliseconds. */
	private static final long UPDATE_WAIT = 2000;

	/** How many signatures get their documentation. */
	private static final int DOCUMENTED_SIGNATURES = 3;

	private final ScriptService scripts;
	private final CodeCompletionService completion;
	private final LogService log;

	/** The open documents' texts, by URI. */
	private final Map<String, String> texts = new ConcurrentHashMap<>();

	/** The last completions, for resolving their documentation. */
	private volatile List<Completion> lastCompletions = Collections.emptyList();

	private final ExecutorService workers = Executors.newCachedThreadPool(r -> {
		final Thread t = new Thread(r, "scijava-language-server");
		t.setDaemon(true);
		return t;
	});

	private volatile LanguageClient client;
	private volatile Runnable onExit = () -> {};

	public SciJavaLanguageServer(final Context context) {
		scripts = context.service(ScriptService.class);
		completion = context.service(CodeCompletionService.class);
		log = context.getService(LogService.class);
	}

	/** Sets what to do when the client says to exit (e.g. end the process). */
	public void setOnExit(final Runnable onExit) {
		this.onExit = onExit;
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
		capabilities.setSignatureHelpProvider(new SignatureHelpOptions(
			java.util.Arrays.asList("(", ",")));
		capabilities.setHoverProvider(true);
		final InitializeResult result = new InitializeResult(capabilities);
		result.setServerInfo(new ServerInfo("SciJava"));
		return CompletableFuture.completedFuture(result);
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		workers.shutdown();
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void exit() {
		onExit.run();
	}

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
		final String uri = params.getTextDocument().getUri();
		texts.put(uri, params.getTextDocument().getText());
		final CompletionRequest request = request(uri, 0);
		if (request != null) {
			workers.execute(() -> completer(request).prepare(request));
		}
		diagnose(uri);
	}

	@Override
	public void didChange(final DidChangeTextDocumentParams params) {
		final String uri = params.getTextDocument().getUri();
		// NB: Full synchronization: the last change is the whole text.
		if (params.getContentChanges().isEmpty()) return;
		texts.put(uri, params.getContentChanges().get(params.getContentChanges()
			.size() - 1).getText());
		diagnose(uri);
	}

	@Override
	public void didClose(final DidCloseTextDocumentParams params) {
		final String uri = params.getTextDocument().getUri();
		final CompletionRequest request = request(uri, 0);
		if (request != null) completer(request).closed(request);
		texts.remove(uri);
		final LanguageClient c = client;
		if (c != null) c.publishDiagnostics(new PublishDiagnosticsParams(uri,
			Collections.emptyList()));
	}

	@Override
	public void didSave(final DidSaveTextDocumentParams params) {
		// NB: Changes come with didChange.
	}

	// -- TextDocumentService: requests --

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
		completion(final CompletionParams params)
	{
		return CompletableFuture.supplyAsync(() -> {
			final String uri = params.getTextDocument().getUri();
			final CompletionRequest request = request(uri, params.getPosition());
			if (request == null) return Either.forRight(new CompletionList());
			CompletionResult result = completer(request).complete(request);
			if (result.update() != null) {
				final CompletionResult better = await(result.update());
				if (better != null) result = better;
			}
			final String text = request.text();
			final Range range = new Range(LspClient.position(text, result
				.replaceStart()), LspClient.position(text, request.offset()));
			final List<CompletionItem> items = new ArrayList<>();
			final List<Completion> completions = result.completions();
			for (int i = 0; i < completions.size(); i++) {
				items.add(item(completions.get(i), i, range, text));
			}
			lastCompletions = completions;
			return Either.forRight(new CompletionList(false, items));
		}, workers);
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(
		final CompletionItem item)
	{
		return CompletableFuture.supplyAsync(() -> {
			final List<Completion> completions = lastCompletions;
			final Object data = item.getData();
			final int index = data instanceof Number ? ((Number) data).intValue()
				: data instanceof com.google.gson.JsonPrimitive
					? ((com.google.gson.JsonPrimitive) data).getAsInt() : -1;
			if (index >= 0 && index < completions.size()) {
				final String doc = plain(completions.get(index).description());
				if (doc != null) item.setDocumentation(new MarkupContent(
					MarkupKind.PLAINTEXT, doc));
			}
			return item;
		}, workers);
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(
		final SignatureHelpParams params)
	{
		return CompletableFuture.supplyAsync(() -> {
			final CompletionRequest request = request(params.getTextDocument()
				.getUri(), params.getPosition());
			if (request == null) return null;
			org.scijava.code.api.SignatureHelp help = completer(request)
				.signatureHelp(request);
			if (help.update() != null) {
				final org.scijava.code.api.SignatureHelp better = await(help
					.update());
				if (better != null) help = better;
			}
			if (help.isEmpty()) return null;
			final List<SignatureInformation> signatures = new ArrayList<>();
			for (final org.scijava.code.api.SignatureHelp.Signature s : help
				.signatures())
			{
				final Completion c = s.callable();
				final List<ParameterInformation> params_ = new ArrayList<>();
				for (final Completion.Parameter p : c.parameters()) {
					params_.add(new ParameterInformation(p.toString()));
				}
				final SignatureInformation info = new SignatureInformation(signature(
					c));
				info.setParameters(params_);
				if (signatures.size() < DOCUMENTED_SIGNATURES) {
					final String doc = plain(c.description());
					if (doc != null) info.setDocumentation(new MarkupContent(
						MarkupKind.PLAINTEXT, doc));
				}
				signatures.add(info);
			}
			return new SignatureHelp(signatures, 0, Math.max(0, help
				.activeParameter()));
		}, workers);
	}

	@Override
	public CompletableFuture<Hover> hover(final HoverParams params) {
		return CompletableFuture.supplyAsync(() -> {
			final CompletionRequest request = request(params.getTextDocument()
				.getUri(), params.getPosition());
			if (request == null) return null;
			org.scijava.code.api.Hover hover = completer(request).hover(request);
			if (hover.isEmpty() && hover.update() != null) {
				final org.scijava.code.api.Hover better = await(hover.update());
				if (better != null) hover = better;
			}
			final String text = plain(hover.text());
			if (text == null) return null;
			final Hover h = new Hover(new MarkupContent(MarkupKind.PLAINTEXT, text));
			if (hover.start() >= 0 && hover.end() >= hover.start()) {
				h.setRange(new Range(LspClient.position(request.text(), hover
					.start()), LspClient.position(request.text(), hover.end())));
			}
			return h;
		}, workers);
	}

	// -- WorkspaceService --

	@Override
	public void didChangeConfiguration(final DidChangeConfigurationParams p) {
		// NB: Nothing to configure.
	}

	@Override
	public void didChangeWatchedFiles(final DidChangeWatchedFilesParams p) {
		// NB: Nothing watched.
	}

	// -- Helper methods --

	/** Finds the problems in a document, and tells the client. */
	private void diagnose(final String uri) {
		final CompletionRequest request = request(uri, 0);
		if (request == null) return;
		final String text = request.text();
		completer(request).diagnose(request).thenAccept(found -> {
			final LanguageClient c = client;
			// NB: Only if still current.
			if (c == null || !text.equals(texts.get(uri))) return;
			final List<Diagnostic> diagnostics = new ArrayList<>();
			for (final org.scijava.code.api.Diagnostic d : found) {
				final Diagnostic diagnostic = new Diagnostic(new Range(LspClient
					.position(text, Math.min(d.start(), text.length())), LspClient
						.position(text, Math.min(d.end(), text.length()))), d.message());
				diagnostic.setSeverity(severity(d.severity()));
				diagnostic.setSource(d.source() != null ? d.source() : "scijava");
				diagnostics.add(diagnostic);
			}
			c.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
		}).exceptionally(error -> {
			if (log != null) log.debug("Diagnosis failed: " + uri, error);
			return null;
		});
	}

	private CompletionRequest request(final String uri, final Position p) {
		final String text = texts.get(uri);
		return text == null ? null : request(uri, LspClient.offset(text, p));
	}

	/**
	 * A request about a document, at an offset; or null if the document is not
	 * open, or of no known language.
	 */
	private CompletionRequest request(final String uri, final int offset) {
		final String text = texts.get(uri);
		if (text == null) return null;
		final String path = path(uri);
		final String name = path != null ? path : uri;
		final int dot = name.lastIndexOf('.');
		final ScriptLanguage language = dot < 0 ? null : scripts
			.getLanguageByExtension(name.substring(dot + 1));
		if (language == null) return null;
		return new CompletionRequest(text, Math.min(offset, text.length()),
			language, null, null, path);
	}

	private CodeCompleter completer(final CompletionRequest request) {
		return completion.getCompleter(request.language());
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

	private static <T> T await(final CompletableFuture<T> update) {
		try {
			return update.get(UPDATE_WAIT, TimeUnit.MILLISECONDS);
		}
		catch (final Exception exc) {
			return null; // NB: Not in time, or failed.
		}
	}

	private static CompletionItem item(final Completion c, final int index,
		final Range range, final String text)
	{
		final CompletionItem item = new CompletionItem(c.displayText() != null ? c
			.displayText() : c.insertionText());
		item.setKind(kind(c.kind()));
		item.setTextEdit(Either.forLeft(new TextEdit(range, c.insertionText())));
		item.setFilterText(c.insertionText());
		// NB: Keep the completer's order.
		item.setSortText(String.format("%06d", index));
		item.setDetail(c.isCallable() ? signature(c) : c.summary());
		item.setData(index);
		if (!c.additionalEdits().isEmpty()) {
			final List<TextEdit> edits = new ArrayList<>();
			for (final Completion.TextEdit e : c.additionalEdits()) {
				edits.add(new TextEdit(new Range(LspClient.position(text, e.start()),
					LspClient.position(text, e.end())), e.newText()));
			}
			item.setAdditionalTextEdits(edits);
		}
		return item;
	}

	/** Renders a callable's signature, e.g. {@code max(double a, double b)}. */
	static String signature(final Completion c) {
		String name = c.insertionText();
		if (name.endsWith("()")) name = name.substring(0, name.length() - 2);
		final StringBuilder sb = new StringBuilder(name).append('(');
		final List<Completion.Parameter> params = c.parameters();
		for (int i = 0; i < params.size(); i++) {
			if (i > 0) sb.append(", ");
			sb.append(params.get(i));
		}
		sb.append(')');
		if (c.returnType() != null) sb.append(" -> ").append(c.returnType());
		return sb.toString();
	}

	/** Converts the completers' HTML to plain text; null if empty. */
	static String plain(final String html) {
		if (html == null) return null;
		final String text = html.replaceAll("(?i)<br\\s*/?>|</p>|<p>|<hr>", "\n")
			.replaceAll("<[^>]+>", "").replace("&lt;", "<").replace("&gt;", ">")
			.replace("&quot;", "\"").replace("&#39;", "'").replace("&nbsp;", " ")
			.replace("&amp;", "&").trim();
		return text.isEmpty() ? null : text;
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
}
