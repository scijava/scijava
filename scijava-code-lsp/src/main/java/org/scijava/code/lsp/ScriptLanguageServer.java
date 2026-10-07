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

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.eclipse.lsp4j.CompletionItem;
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
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * Base class for in-process language servers of SciJava script languages:
 * keeps the open documents (full sync), answers requests by asking the
 * subclass about a {@link Document} and offset, resolves documentation on
 * demand (see {@link #describe}), and looks for problems a moment after each
 * change (see {@link #diagnose}).
 *
 * @author Gabriel Selzer
 */
public abstract class ScriptLanguageServer implements LanguageServer,
	TextDocumentService, WorkspaceService, LanguageClientAware
{

	/**
	 * How long a document must stay unchanged before its problems are looked
	 * for, in milliseconds. (Editors send every keystroke.)
	 */
	private static final long PAUSE = Long.getLong("scijava.lsp.pause", 300);

	/** How many completions' documentation to remember. */
	private static final int REMEMBERED = 5000;

	private static final ScheduledExecutorService TIMER = Executors
		.newSingleThreadScheduledExecutor(r -> {
			final Thread t = new Thread(r, "scijava-script-server");
			t.setDaemon(true);
			return t;
		});

	private final Environment environment;
	private volatile LanguageClient client;
	private final Map<String, Document> docs = new ConcurrentHashMap<>();
	private final Map<Integer, Supplier<String>> descriptions =
		new ConcurrentHashMap<>();
	private final AtomicInteger ids = new AtomicInteger();

	protected ScriptLanguageServer(final Environment environment) {
		this.environment = environment == null ? Environment.NONE : environment;
	}

	/** Where the scripts run (e.g. a live interpreter's state). */
	public Environment environment() {
		return environment;
	}

	/** An open script. */
	public static final class Document {

		private final String uri;
		private final String path;
		private volatile String text;
		private volatile int version;
		private ScheduledFuture<?> pending;

		Document(final String uri, final String text, final int version) {
			this.uri = uri;
			this.path = pathOf(uri);
			this.text = text;
			this.version = version;
		}

		public String uri() {
			return uri;
		}

		/** The script's file path, or null if unsaved. */
		public String path() {
			return path;
		}

		public String text() {
			return text;
		}

		public int version() {
			return version;
		}

		/** The text of the line of an offset, up to it. */
		public String lineTo(final int offset) {
			final String t = text;
			final int end = Math.min(offset, t.length());
			return t.substring(t.lastIndexOf('\n', end - 1) + 1, end);
		}

		/** The range between two offsets. */
		public Range range(final int start, final int end) {
			return new Range(Positions.position(text, Math.min(start, text
				.length())), Positions.position(text, Math.min(end, text.length())));
		}
	}

	// -- To implement --

	/** Gets the completions at an offset of a script. */
	protected abstract CompletionList complete(Document doc, int offset);

	/** Gets the signatures of the call around an offset, or null. */
	protected SignatureHelp signatureHelp(final Document doc, final int offset) {
		return null;
	}

	/** Gets what is known about the code at an offset, or null. */
	protected Hover hover(final Document doc, final int offset) {
		return null;
	}

	/** Finds the problems in a script. The default finds none. */
	protected List<Diagnostic> diagnose(final Document doc) {
		return Collections.emptyList();
	}

	/** Notes that a script was opened (e.g. to get ready for it). */
	protected void opened(final Document doc) {}

	/** Notes that a script was closed (e.g. to release what is kept for it). */
	protected void closed(final Document doc) {}

	// -- Helpers --

	/**
	 * Attaches documentation to a completion, computed only when shown (when
	 * the client resolves the completion).
	 *
	 * @param html Supplies the documentation, as HTML (sent as Markdown, which
	 *          allows HTML).
	 */
	protected CompletionItem describe(final CompletionItem item,
		final Supplier<String> html)
	{
		if (html == null) return item;
		if (descriptions.size() > REMEMBERED) descriptions.clear();
		final int id = ids.incrementAndGet();
		descriptions.put(id, html);
		item.setData(id);
		return item;
	}

	/**
	 * Makes a list of completions replacing the text from {@code start} to the
	 * caret (those without an edit of their own), in the given order.
	 */
	protected static CompletionList list(final Document doc, final int start,
		final int caret, final List<CompletionItem> items)
	{
		final Range range = doc.range(start, caret);
		for (int i = 0; i < items.size(); i++) {
			final CompletionItem item = items.get(i);
			if (item.getTextEdit() == null) {
				final String newText = item.getInsertText() != null ? item
					.getInsertText() : item.getLabel();
				item.setTextEdit(Either.forLeft(new TextEdit(range, newText)));
			}
			if (item.getSortText() == null) item.setSortText(String.format("%06d",
				i));
		}
		return new CompletionList(false, items);
	}

	/** An edit inserting text at an offset. */
	protected static TextEdit insert(final Document doc, final int offset,
		final String text)
	{
		return new TextEdit(doc.range(offset, offset), text);
	}

	/** A problem, between two offsets. */
	protected static Diagnostic diagnostic(final Document doc, final int start,
		final int end, final DiagnosticSeverity severity, final String message,
		final String source)
	{
		final Diagnostic d = new Diagnostic(doc.range(start, Math.max(start, end)),
			message);
		d.setSeverity(severity);
		d.setSource(source);
		return d;
	}

	// -- LanguageServer --

	@Override
	public void connect(final LanguageClient c) {
		client = c;
	}

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

	// -- TextDocumentService --

	@Override
	public void didOpen(final DidOpenTextDocumentParams params) {
		final TextDocumentItem item = params.getTextDocument();
		final Document doc = new Document(item.getUri(), item.getText(), item
			.getVersion());
		docs.put(doc.uri, doc);
		try {
			opened(doc);
		}
		catch (final RuntimeException | LinkageError exc) {
			// NB: Getting ready is optional.
		}
		scheduleDiagnosis(doc);
	}

	@Override
	public void didChange(final DidChangeTextDocumentParams params) {
		final Document doc = docs.get(params.getTextDocument().getUri());
		if (doc == null || params.getContentChanges().isEmpty()) return;
		doc.text = params.getContentChanges().get(params.getContentChanges()
			.size() - 1).getText();
		doc.version = params.getTextDocument().getVersion();
		scheduleDiagnosis(doc);
	}

	@Override
	public void didClose(final DidCloseTextDocumentParams params) {
		final Document doc = docs.remove(params.getTextDocument().getUri());
		if (doc == null) return;
		synchronized (doc) {
			if (doc.pending != null) doc.pending.cancel(false);
		}
		closed(doc);
	}

	@Override
	public void didSave(final DidSaveTextDocumentParams params) {}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
		completion(final CompletionParams params)
	{
		final Document doc = docs.get(params.getTextDocument().getUri());
		if (doc == null) return CompletableFuture.completedFuture(Either.forRight(
			new CompletionList()));
		final CompletionList list = complete(doc, Positions.offset(doc.text,
			params.getPosition()));
		return CompletableFuture.completedFuture(Either.forRight(list == null
			? new CompletionList() : list));
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(
		final CompletionItem item)
	{
		final Supplier<String> html = descriptions.get(id(item.getData()));
		if (html == null) return CompletableFuture.completedFuture(item);
		return CompletableFuture.supplyAsync(() -> {
			final String doc = html.get();
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
		final Document doc = docs.get(params.getTextDocument().getUri());
		return CompletableFuture.completedFuture(doc == null ? null
			: signatureHelp(doc, Positions.offset(doc.text, params.getPosition())));
	}

	@Override
	public CompletableFuture<Hover> hover(final HoverParams params) {
		final Document doc = docs.get(params.getTextDocument().getUri());
		return CompletableFuture.completedFuture(doc == null ? null : hover(doc,
			Positions.offset(doc.text, params.getPosition())));
	}

	// -- WorkspaceService --

	@Override
	public void didChangeConfiguration(final DidChangeConfigurationParams p) {}

	@Override
	public void didChangeWatchedFiles(final DidChangeWatchedFilesParams p) {}

	// -- Helper methods --

	private void scheduleDiagnosis(final Document doc) {
		synchronized (doc) {
			if (doc.pending != null) doc.pending.cancel(false);
			doc.pending = TIMER.schedule(() -> {
				final int version = doc.version;
				List<Diagnostic> found;
				try {
					found = diagnose(doc);
				}
				catch (final RuntimeException | LinkageError exc) {
					found = Collections.emptyList();
				}
				final LanguageClient c = client;
				// NB: Only if still current.
				if (c == null || docs.get(doc.uri) != doc || doc.version != version) {
					return;
				}
				c.publishDiagnostics(new PublishDiagnosticsParams(doc.uri, found ==
					null ? Collections.emptyList() : found, version));
			}, PAUSE, TimeUnit.MILLISECONDS);
		}
	}

	private static int id(final Object data) {
		if (data instanceof Number) return ((Number) data).intValue();
		if (data instanceof com.google.gson.JsonPrimitive) {
			return ((com.google.gson.JsonPrimitive) data).getAsInt();
		}
		return -1;
	}

	private static String pathOf(final String uri) {
		try {
			final URI u = URI.create(uri);
			return "file".equals(u.getScheme()) ? new File(u).getPath() : null;
		}
		catch (final IllegalArgumentException exc) {
			return null;
		}
	}
}
