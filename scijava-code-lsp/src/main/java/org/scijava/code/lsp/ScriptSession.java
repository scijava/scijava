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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.InsertReplaceEdit;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
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
import org.scijava.log.Logger;
import org.scijava.script.ScriptLanguage;

/**
 * A SciJava script, as language servers know it: the place where a client
 * (e.g. an editor, or a tool checking a script) asks about it, in terms of
 * its text and offsets into it.
 * <p>
 * The session keeps its server's copy of the script up to date (opening it,
 * sending each new text, closing it), names it (its file's URI if it has one,
 * otherwise an {@code untitled:} one; a new file, e.g. after Save As, closes
 * the old document and opens the new one), and receives the problems the
 * server finds in it. Each request carries the text it is about, and the
 * offset in that text; positions in answers are positions in that text (see
 * {@link Positions}). Answers are lsp4j's own types.
 * </p>
 * <p>
 * The server need not know SciJava scripts: given a transformation (e.g. by
 * the {@link LanguageServerPlugin#dialect() dialect} of the plugin offering
 * the server), the session shows the server each script as plain code of its
 * language (see {@link ScriptDocument}), and maps positions both ways.
 * Whatever the server says about the lines the transformation added is left
 * out, as are completions of names only those lines bind (see
 * {@link ScriptDocument#added()}).
 * </p>
 * <p>
 * A session owns its server: it is the server's client, initializes it, and
 * shuts it down when closed. Get one from the
 * {@link LanguageServerService#session service}, which makes it a server of
 * its own.
 * </p>
 *
 * @author Gabriel Selzer
 */
public class ScriptSession implements AutoCloseable {

	private static final AtomicInteger UNTITLED = new AtomicInteger();

	/** How long to wait for a server to initialize, in milliseconds. */
	private static final long INITIALIZE_TIMEOUT = 10_000;

	private final LanguageServer server;
	private final ScriptLanguage language;
	private final Function<String, ScriptDocument> transform;
	private final String untitled;
	private volatile Supplier<File> file = () -> null;
	private volatile Logger log;
	private volatile Consumer<List<Diagnostic>> diagnosticsListener;

	private String uri;
	private String text;
	/** The text, as the server sees it. */
	private ScriptDocument doc;
	private int version;
	private List<Diagnostic> diagnostics = Collections.emptyList();
	private CompletableFuture<List<Diagnostic>> nextDiagnostics =
		new CompletableFuture<>();

	/**
	 * @param server The server, which the session owns.
	 * @param language The script's language, or null if unknown.
	 * @param transform Makes scripts what the server sees; or null, to show
	 *          them as they are.
	 */
	private ScriptSession(final LanguageServer server,
		final ScriptLanguage language,
		final Function<String, ScriptDocument> transform)
	{
		this.server = server;
		this.language = language;
		this.transform = transform == null ? ScriptDocument::of : transform;
		final List<String> extensions = language == null ? Collections
			.emptyList() : language.getExtensions();
		untitled = "untitled:/script-" + UNTITLED.incrementAndGet() + (extensions
			.isEmpty() ? "" : "." + extensions.get(0));
	}

	/**
	 * Makes a session about a script, with the given server of its own (e.g. a
	 * fake one, in tests): the session becomes its client, so the server must
	 * not be shared. The server is initialized now, and shut down when the
	 * session is closed.
	 *
	 * @param server The server, which the session owns.
	 * @param language The script's language, or null if unknown.
	 */
	public static ScriptSession owning(final LanguageServer server,
		final ScriptLanguage language)
	{
		return owning(server, language, null);
	}

	/**
	 * As {@link #owning(LanguageServer, ScriptLanguage)}, showing the server
	 * each script transformed (e.g. into plain code of its language).
	 *
	 * @param server The server, which the session owns.
	 * @param language The script's language, or null if unknown.
	 * @param transform Makes scripts what the server sees; or null, to show
	 *          them as they are.
	 */
	public static ScriptSession owning(final LanguageServer server,
		final ScriptLanguage language,
		final Function<String, ScriptDocument> transform)
	{
		return new ScriptSession(server, language, transform).connect();
	}

	/**
	 * Becomes the client of its server, and initializes it: as LSP says, it
	 * awaits the server's answer before telling it anything else. (Servers in
	 * process answer at once.)
	 */
	private ScriptSession connect() {
		if (server instanceof LanguageClientAware) {
			((LanguageClientAware) server).connect(new Client());
		}
		try {
			final CompletableFuture<InitializeResult> init = server.initialize(
				new InitializeParams());
			if (init != null) init.get(INITIALIZE_TIMEOUT, TimeUnit.MILLISECONDS);
		}
		catch (final Exception exc) {
			// NB: Not initialized (in time): asked anyway, answering what it can.
		}
		server.initialized(new InitializedParams());
		return this;
	}

	/**
	 * Sets a supplier of the script's file, asked each time the script is
	 * updated, e.g. so that Save As is followed.
	 */
	public ScriptSession setFile(final Supplier<File> file) {
		this.file = file == null ? () -> null : file;
		return this;
	}

	/** Sets where to report the server's messages, or null for nowhere. */
	public ScriptSession setLogger(final Logger log) {
		this.log = log;
		return this;
	}

	/**
	 * Sets who to tell about the problems in the script, each time the server
	 * reports them (on the server's thread).
	 */
	public ScriptSession setDiagnosticsListener(
		final Consumer<List<Diagnostic>> listener)
	{
		diagnosticsListener = listener;
		return this;
	}

	/**
	 * Tells the server the script's text: opening it, the first time (or under
	 * a new name); otherwise, if it changed, sending it as the new version.
	 */
	public synchronized ScriptSession update(final String newText) {
		final String current = currentUri();
		if (uri != null && !uri.equals(current)) closeDocument();
		if (uri == null) {
			uri = current;
			version = 1;
			text = newText;
			doc = transform.apply(text);
			newVersion();
			server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
				new TextDocumentItem(uri, languageId(), version, doc.text())));
		}
		else if (!newText.equals(text)) {
			text = newText;
			doc = transform.apply(text);
			version++;
			newVersion();
			server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(
				new VersionedTextDocumentIdentifier(uri, version), Collections
					.singletonList(new TextDocumentContentChangeEvent(doc.text()))));
		}
		return this;
	}

	// -- Requests: about the given text (sent first, if new), at an offset --

	/** Completions at an offset: all servers', once all have answered. */
	public synchronized CompletableFuture<CompletionList> completion(
		final String script, final int offset)
	{
		update(script);
		final ScriptDocument d = doc;
		return server.getTextDocumentService().completion(new CompletionParams(
			identifier(), position(offset))).thenApply(answer -> answer == null
				? new CompletionList() : back(d, answer.isRight() ? answer.getRight()
					: new CompletionList(false, answer.getLeft())));
	}

	/** A completion, with what it takes long to know (e.g. documentation). */
	public CompletableFuture<CompletionItem> resolve(final CompletionItem item) {
		return server.getTextDocumentService().resolveCompletionItem(item);
	}

	/** The signatures of the call around an offset (null if none). */
	public synchronized CompletableFuture<SignatureHelp> signatureHelp(
		final String script, final int offset)
	{
		update(script);
		return server.getTextDocumentService().signatureHelp(
			new SignatureHelpParams(identifier(), position(offset)));
	}

	/** What is known about the code at an offset (null if nothing). */
	public synchronized CompletableFuture<Hover> hover(final String script,
		final int offset)
	{
		update(script);
		final ScriptDocument d = doc;
		return server.getTextDocumentService().hover(new HoverParams(identifier(),
			position(offset))).thenApply(h -> {
				if (h == null || h.getRange() == null) return h;
				final Hover out = new Hover();
				out.setContents(h.getContents());
				out.setRange(d.toOriginal(h.getRange()));
				return out;
			});
	}

	// -- Problems --

	/** The problems last reported for the script. */
	public synchronized List<Diagnostic> diagnostics() {
		return diagnostics;
	}

	/**
	 * Waits for the problems in the text last sent: for tools that ask once,
	 * rather than editors.
	 *
	 * @param timeout How long to wait, in milliseconds; if the server is not
	 *          done by then, the problems reported so far.
	 */
	public List<Diagnostic> diagnostics(final long timeout) {
		final String u;
		final CompletableFuture<List<Diagnostic>> next;
		synchronized (this) {
			u = uri;
			next = nextDiagnostics;
		}
		if (u == null) return Collections.emptyList();
		try {
			return next.get(timeout, TimeUnit.MILLISECONDS);
		}
		catch (final Exception exc) {
			return diagnostics(); // NB: Not in time: those reported so far.
		}
	}

	/** Closes the server's copy of the script, and shuts the server down. */
	@Override
	public synchronized void close() {
		closeDocument();
		try {
			final CompletableFuture<Object> done = server.shutdown();
			if (done != null) done.thenRun(server::exit);
		}
		catch (final RuntimeException | LinkageError exc) {
			if (log != null) log.debug("Language server shutdown failed", exc);
		}
	}

	// -- Helper methods --

	/** Closes the server's copy of the script, if open. */
	private void closeDocument() {
		if (uri == null) return;
		server.getTextDocumentService().didClose(new DidCloseTextDocumentParams(
			new TextDocumentIdentifier(uri)));
		uri = null;
		text = null;
		doc = null;
		diagnostics = Collections.emptyList();
	}

	/** Where an offset into the script is, in what the server sees. */
	private Position position(final int offset) {
		return Positions.position(doc.text(), doc.toTransformed(offset));
	}

	private TextDocumentIdentifier identifier() {
		return new TextDocumentIdentifier(uri);
	}

	private String currentUri() {
		final File f = file.get();
		return f == null ? untitled : f.getAbsoluteFile().toURI().toString();
	}

	/**
	 * The LSP language of the script: its language's name, in lower case (see
	 * {@link LanguageServerPlugin#supports(String)}).
	 */
	private String languageId() {
		return language == null ? "plaintext" : language.getLanguageName()
			.toLowerCase(Locale.ROOT);
	}

	/** Problems reported from now on are those of a new version. */
	private void newVersion() {
		if (nextDiagnostics.isDone()) nextDiagnostics = new CompletableFuture<>();
	}

	/**
	 * Maps completions' edits back, leaving out those in the added lines, and
	 * those of names only the added lines bind.
	 */
	private static CompletionList back(final ScriptDocument doc,
		final CompletionList list)
	{
		final List<CompletionItem> items = new ArrayList<>();
		for (final CompletionItem item : list.getItems()) {
			if (doc.added().contains(leadingName(item.getLabel()))) continue;
			if (item.getTextEdit() != null) {
				if (item.getTextEdit().isLeft()) {
					final TextEdit e = item.getTextEdit().getLeft();
					final Range r = doc.toOriginal(e.getRange());
					if (r == null) continue;
					item.setTextEdit(Either.forLeft(new TextEdit(r, e.getNewText())));
				}
				else {
					final InsertReplaceEdit e = item.getTextEdit().getRight();
					final Range insert = doc.toOriginal(e.getInsert());
					final Range replace = doc.toOriginal(e.getReplace());
					if (insert == null || replace == null) continue;
					item.setTextEdit(Either.forRight(new InsertReplaceEdit(e
						.getNewText(), insert, replace)));
				}
			}
			if (item.getAdditionalTextEdits() != null) {
				final List<TextEdit> edits = new ArrayList<>();
				for (final TextEdit e : item.getAdditionalTextEdits()) {
					final Range r = doc.toOriginal(e.getRange());
					if (r != null) edits.add(new TextEdit(r, e.getNewText()));
				}
				item.setAdditionalTextEdits(edits);
			}
			items.add(item);
		}
		return new CompletionList(list.isIncomplete(), items);
	}

	/**
	 * The name a completion's label starts with, e.g. {@code java} for
	 * {@code java.lang} and {@code max} for {@code max(a, b)}.
	 */
	private static String leadingName(final String label) {
		int end = 0;
		while (end < label.length() && (Character.isLetterOrDigit(label.charAt(
			end)) || label.charAt(end) == '_')) end++;
		return label.substring(0, end);
	}

	/** Maps problems back, leaving out those in the added lines. */
	private static List<Diagnostic> back(final ScriptDocument doc,
		final List<Diagnostic> diagnostics)
	{
		final List<Diagnostic> out = new ArrayList<>();
		for (final Diagnostic d : diagnostics) {
			final Range r = doc.toOriginal(d.getRange());
			if (r == null) continue;
			d.setRange(r);
			out.add(d);
		}
		return out;
	}

	/** Receives the server's messages about the script. */
	private final class Client implements LanguageClient {

		@Override
		public void publishDiagnostics(final PublishDiagnosticsParams p) {
			final List<Diagnostic> found;
			final CompletableFuture<List<Diagnostic>> next;
			synchronized (ScriptSession.this) {
				if (!p.getUri().equals(uri)) return;
				// NB: Those of an earlier version are out of date.
				if (p.getVersion() != null && p.getVersion() < version) return;
				found = p.getDiagnostics() == null ? Collections.emptyList() : back(
					doc, p.getDiagnostics());
				diagnostics = found;
				next = nextDiagnostics;
			}
			next.complete(found);
			final Consumer<List<Diagnostic>> listener = diagnosticsListener;
			if (listener != null) listener.accept(found);
		}

		@Override
		public void telemetryEvent(final Object object) {}

		@Override
		public void showMessage(final MessageParams m) {
			logMessage(m);
		}

		@Override
		public CompletableFuture<MessageActionItem> showMessageRequest(
			final ShowMessageRequestParams r)
		{
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void logMessage(final MessageParams m) {
			final Logger l = log;
			if (l != null) l.debug("Language server: " + m.getMessage());
		}
	}
}
