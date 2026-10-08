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
import java.util.Collections;
import java.util.List;
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
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
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
 * A session owns its server: it is the server's client. Get one from the
 * {@link LanguageServerService#session service}, which makes it a server of
 * its own.
 * </p>
 *
 * @author Gabriel Selzer
 */
public class ScriptSession implements AutoCloseable {

	private static final AtomicInteger UNTITLED = new AtomicInteger();

	private final LanguageServer server;
	private final ScriptLanguage language;
	private final Function<String, CompletableFuture<List<Diagnostic>>> allReported;
	private final String untitled;
	private volatile Supplier<File> file = () -> null;
	private volatile Logger log;
	private volatile Consumer<List<Diagnostic>> diagnosticsListener;

	private String uri;
	private String text;
	private int version;
	private List<Diagnostic> diagnostics = Collections.emptyList();
	private CompletableFuture<List<Diagnostic>> nextDiagnostics =
		new CompletableFuture<>();

	/**
	 * @param server The server, which the session owns.
	 * @param language The script's language, or null if unknown.
	 * @param allReported Gets the problems in a document once all its servers
	 *          reported them; or null if the server reports them at once.
	 */
	ScriptSession(final LanguageServer server, final ScriptLanguage language,
		final Function<String, CompletableFuture<List<Diagnostic>>> allReported)
	{
		this.server = server;
		this.language = language;
		this.allReported = allReported;
		final List<String> extensions = language == null ? Collections
			.emptyList() : language.getExtensions();
		untitled = "untitled:/script-" + UNTITLED.incrementAndGet() + (extensions
			.isEmpty() ? "" : "." + extensions.get(0));
	}

	/**
	 * Makes a session about a script, with the given server of its own (e.g. a
	 * fake one, in tests): the session becomes its client, so the server must
	 * not be shared. The server is taken to report each version's problems at
	 * once.
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
	 * As {@link #owning(LanguageServer, ScriptLanguage)}, for a server that
	 * stands for several (e.g. one per plugin), which report each version's
	 * problems separately.
	 *
	 * @param server The server, which the session owns.
	 * @param language The script's language, or null if unknown.
	 * @param allReported Gets the problems in a document once all its servers
	 *          reported them; or null if the server reports them at once.
	 */
	public static ScriptSession owning(final LanguageServer server,
		final ScriptLanguage language,
		final Function<String, CompletableFuture<List<Diagnostic>>> allReported)
	{
		return new ScriptSession(server, language, allReported).connect();
	}

	/** Becomes the client of its server. */
	ScriptSession connect() {
		if (server instanceof LanguageClientAware) {
			((LanguageClientAware) server).connect(new Client());
		}
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
		if (uri != null && !uri.equals(current)) close();
		if (uri == null) {
			uri = current;
			version = 1;
			text = newText;
			newVersion();
			server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
				new TextDocumentItem(uri, languageId(), version, text)));
		}
		else if (!newText.equals(text)) {
			text = newText;
			version++;
			newVersion();
			server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(
				new VersionedTextDocumentIdentifier(uri, version), Collections
					.singletonList(new TextDocumentContentChangeEvent(text))));
		}
		return this;
	}

	// -- Requests: about the given text (sent first, if new), at an offset --

	/**
	 * Completions at an offset. If some servers are slower, the list is an
	 * {@link UpdatingCompletionList}, whose update brings theirs.
	 */
	public synchronized CompletableFuture<CompletionList> completion(
		final String script, final int offset)
	{
		update(script);
		return server.getTextDocumentService().completion(new CompletionParams(
			identifier(), Positions.position(script, offset))).thenApply(
				answer -> answer == null ? new CompletionList() : answer.isRight()
					? answer.getRight() : new CompletionList(false, answer.getLeft()));
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
			new SignatureHelpParams(identifier(), Positions.position(script,
				offset)));
	}

	/** What is known about the code at an offset (null if nothing). */
	public synchronized CompletableFuture<Hover> hover(final String script,
		final int offset)
	{
		update(script);
		return server.getTextDocumentService().hover(new HoverParams(identifier(),
			Positions.position(script, offset)));
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
	 * @param timeout How long to wait, in milliseconds; servers not done by
	 *          then are left out.
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
			return (allReported == null ? next : allReported.apply(u)).get(timeout,
				TimeUnit.MILLISECONDS);
		}
		catch (final Exception exc) {
			return diagnostics(); // NB: Not all in time: those that were.
		}
	}

	/** Closes the server's copy of the script, if open. */
	@Override
	public synchronized void close() {
		if (uri == null) return;
		server.getTextDocumentService().didClose(new DidCloseTextDocumentParams(
			new TextDocumentIdentifier(uri)));
		uri = null;
		text = null;
		diagnostics = Collections.emptyList();
	}

	// -- Helper methods --

	private TextDocumentIdentifier identifier() {
		return new TextDocumentIdentifier(uri);
	}

	private String currentUri() {
		final File f = file.get();
		return f == null ? untitled : f.getAbsoluteFile().toURI().toString();
	}

	private String languageId() {
		return language == null ? "plaintext" : language.getLanguageName();
	}

	/** Problems reported from now on are those of a new version. */
	private void newVersion() {
		if (nextDiagnostics.isDone()) nextDiagnostics = new CompletableFuture<>();
	}

	/** Receives the server's messages about the script. */
	private final class Client implements LanguageClient {

		@Override
		public void publishDiagnostics(final PublishDiagnosticsParams p) {
			final List<Diagnostic> found = p.getDiagnostics() == null ? Collections
				.emptyList() : p.getDiagnostics();
			final CompletableFuture<List<Diagnostic>> next;
			synchronized (ScriptSession.this) {
				if (!p.getUri().equals(uri)) return;
				// NB: Those of an earlier version are out of date.
				if (p.getVersion() != null && p.getVersion() < version) return;
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
