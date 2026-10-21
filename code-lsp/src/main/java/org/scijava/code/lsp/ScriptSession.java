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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.script.ScriptContext;

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
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.ShowMessageRequestParams;
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
import org.scijava.log.LogService;
import org.scijava.log.Logger;
import org.scijava.script.ScriptLanguage;

/**
 * A {@link LanguageServerService} session for a given SciJava script.
 * <p>
 * A session controls its own {@link LanguageServer}: it a server of its own, which it shuts down when closed; it
 * keeps the server's copy of the script up to date when mutated by the
 * client. The server sees the script as plain code of its language (see
 * {@link ScriptDocument}); the session maps positions in its answers back,
 * leaving out what concerns only the lines it added.
 * </p>
 * <p>
 * The server analyzes the script where it runs, as its document says (see
 * {@link ScriptDocument#environment}). As that changes (e.g. as the script's
 * declared environment is edited), the session moves to a new server, of the
 * new environment. It talks to its server off the caller's thread, telling
 * and asking it everything in order.
 * </p>
 *
 * @author Gabriel Selzer
 */
public final class ScriptSession implements AutoCloseable {

	private static final AtomicInteger UNTITLED = new AtomicInteger();

	/** How long to wait for a server to initialize, in milliseconds. */
	private static final long INITIALIZE_TIMEOUT = 10_000;

	/** Where sessions talk to their servers (each in order). */
	private static final ExecutorService TALK = Executors.newCachedThreadPool(
		r -> {
			final Thread t = new Thread(r, "code-lsp-session");
			t.setDaemon(true);
			return t;
		});

	private final Function<Environment, LanguageServer> launch;
	private final Function<String, ScriptDocument> transform;
	private final String untitled;
	private volatile Supplier<File> file = () -> null;
	private final Logger log;
	private volatile Consumer<List<Diagnostic>> diagnosticsListener;

	/** The server, once all it was told or asked so far is sent. */
	private CompletableFuture<Optional<LanguageServer>> server =
		CompletableFuture.completedFuture(Optional.empty());
	/** Where the script runs, as the server is launched for. */
	private Optional<Environment> environment = Optional.empty();
	/** How many servers were moved to: which one is meant, as of now. */
	private int generation;
	/** The server whose messages are heard (set as it is launched). */
	private volatile LanguageServer current;
	/** Whether closed: then, servers are told nothing more. */
	private boolean closed;
	/** The server that made the last completions (see {@link #resolve}). */
	private int completedBy;

	private String uri;
	private String text;
	/** The text, as the server sees it. */
	private ScriptDocument doc;
	private int version;

	/**
	 * Starts a session about a script of the given language: its servers are
	 * of the LSP language its dialect says, from the highest-priority plugin
	 * serving it (see {@link LanguageServerService#launch}), and see the script
	 * as plain code of that language. If the language has no dialect, or no
	 * plugin serves it, the session completes nothing.
	 *
	 * @param servers Where the dialect and servers come from.
	 * @param language The script's language.
	 */
	public ScriptSession(final LanguageServerService servers,
		final ScriptLanguage language)
	{
		this(servers, language, Optional.empty());
	}

	/**
	 * As {@link #ScriptSession(LanguageServerService, ScriptLanguage)}, for
	 * code typed into a running interpreter: its state reaches the servers via
	 * their {@link Environment#live()}. If the language has no dialect, or no
	 * plugin serves it, the interpreter's variables are completed.
	 *
	 * @param servers Where the dialect and servers come from.
	 * @param language The interpreter's language.
	 * @param live The interpreter's state.
	 */
	public ScriptSession(final LanguageServerService servers,
		final ScriptLanguage language, final ScriptContext live)
	{
		this(servers, language, Optional.of(live));
	}

	private ScriptSession(final LanguageServerService servers,
		final ScriptLanguage language, final Optional<ScriptContext> live)
	{
		this.launch = launcher(servers, language, live);
		this.transform = transformer(servers, language);
		untitled = "untitled:/script-" + UNTITLED.incrementAndGet();
		log = servers.context().getService(LogService.class);
	}

	/**
	 * Sets a supplier of the script's file, asked each time the script is
	 * updated, e.g. so that Save As is followed.
	 */
	public void setFile(final Supplier<File> file) {
		this.file = file == null ? () -> null : file;
	}

	/**
	 * Sets who to tell about the problems in the script, each time the server
	 * reports them (on the server's thread): those of the text last sent, with
	 * positions in the script.
	 */
	public void setDiagnosticsListener(
		final Consumer<List<Diagnostic>> listener)
	{
		diagnosticsListener = listener;
	}

	/**
	 * Tells the server the script's text: opening it, the first time (or under
	 * a new name, or on a new server); otherwise, if it changed, sending it as
	 * the new version. First, if where the script runs changed (which it may,
	 * even if its text did not), moves to a server of where it runs now.
	 */
	public synchronized void update(final String newText) {
		final String current = currentUri();
		if (uri != null && !uri.equals(current)) closeDocument();
		if (uri == null) {
			uri = current;
			version = 1;
			text = newText;
			doc = transform.apply(text);
			follow();
			open();
		}
		else if (!newText.equals(text)) {
			text = newText;
			doc = transform.apply(text);
			version++;
			if (follow()) open();
			else {
				final VersionedTextDocumentIdentifier id =
					new VersionedTextDocumentIdentifier(uri, version);
				final String code = doc.text();
				tell(s -> s.getTextDocumentService().didChange(
					new DidChangeTextDocumentParams(id, Collections.singletonList(
						new TextDocumentContentChangeEvent(code)))));
			}
		}
		// NB: Where it runs may change without its text changing, e.g. once its
		// environment is built.
		else if (follow()) open();
	}

	// -- Requests: about the given text (sent first, if new), at an offset --

	/** Completions at an offset: all servers', once all have answered. */
	public synchronized CompletableFuture<CompletionList> completion(
		final String script, final int offset)
	{
		update(script);
		final ScriptDocument d = doc;
		final CompletionParams params = new CompletionParams(identifier(),
			position(offset));
		completedBy = generation;
		return ask(s -> s.getTextDocumentService().completion(params))
			.thenApply(
			answer -> answer == null ? new CompletionList() : back(d, answer
				.isRight() ? answer.getRight() : new CompletionList(false, answer
					.getLeft())));
	}

	/**
	 * A completion, with what it takes long to know (e.g. documentation); or
	 * null, if the server that made it is gone (e.g. as where the script runs
	 * changed since).
	 */
	public synchronized CompletableFuture<CompletionItem> resolve(
		final CompletionItem item)
	{
		return ask(completedBy, s -> s.getTextDocumentService()
			.resolveCompletionItem(item));
	}

	/** The signatures of the call around an offset (null if none). */
	public synchronized CompletableFuture<SignatureHelp> signatureHelp(
		final String script, final int offset)
	{
		update(script);
		final SignatureHelpParams params = new SignatureHelpParams(identifier(),
			position(offset));
		return ask(s -> s.getTextDocumentService().signatureHelp(
			params));
	}

	/** What is known about the code at an offset (null if nothing). */
	public synchronized CompletableFuture<Hover> hover(final String script,
		final int offset)
	{
		update(script);
		final ScriptDocument d = doc;
		final HoverParams params = new HoverParams(identifier(), position(
			offset));
		return ask(s -> s.getTextDocumentService().hover(params))
			.thenApply(h -> {
			if (h == null || h.getRange() == null) return h;
			final Hover out = new Hover();
			out.setContents(h.getContents());
			out.setRange(d.toOriginal(h.getRange()));
			return out;
		});
	}

	/**
	 * Closes the server's copy of the script, and shuts the server down
	 * (waiting a while for all it was told to be sent). A closed session tells
	 * servers nothing more, and answers nothing.
	 */
	@Override
	public void close() {
		final CompletableFuture<Optional<LanguageServer>> done;
		synchronized (this) {
			if (closed) return;
			closeDocument();
			closed = true;
			server = server.thenApplyAsync(s -> {
				s.ifPresent(this::stop);
				return Optional.empty();
			}, TALK);
			done = server;
		}
		try {
			done.get(INITIALIZE_TIMEOUT, TimeUnit.MILLISECONDS);
		}
		catch (final Exception exc) {
			final Logger l = log;
			if (l != null) l.debug("Language server shutdown failed", exc);
		}
	}

	// -- Helper methods --

	/**
	 * Gets servers for scripts of a language: of the LSP language its dialect
	 * says; or, without one (or if one cannot be created), servers completing
	 * the interpreter's variables (if any).
	 */
	private static Function<Environment, LanguageServer> launcher(
		final LanguageServerService servers, final ScriptLanguage language,
		final Optional<ScriptContext> live)
	{
		final LogService log = servers.context().getService(LogService.class);
		final Function<Environment, Environment> typedInto = env -> live.map(
			env::withLive).orElse(env);
		try {
			final String languageId = servers.dialect(language).languageId();
			if (servers.supports(languageId)) return env -> {
				try {
					return servers.launch(languageId, typedInto.apply(env));
				}
				catch (final RuntimeException | LinkageError exc) {
					if (log != null) log.warn("Could not create a language server for " +
						languageId, exc);
					return new BindingsLanguageServer(typedInto.apply(env));
				}
			};
		}
		catch (final IllegalArgumentException exc) {
			// NB: The language has no dialect.
		}
		return env -> new BindingsLanguageServer(typedInto.apply(env));
	}

	/**
	 * Makes scripts of a language plain code of the LSP language its dialect
	 * says, if served; otherwise, shows them as they are.
	 */
	private static Function<String, ScriptDocument> transformer(
		final LanguageServerService servers, final ScriptLanguage language)
	{
		try {
			final ScriptDialect dialect = servers.dialect(language);
			if (servers.supports(dialect.languageId())) {
				return text -> servers.document(text, dialect);
			}
		}
		catch (final IllegalArgumentException exc) {
			// NB: The language has no dialect.
		}
		return ScriptDocument::of;
	}

	/**
	 * Moves to a server of where the script runs now, if that changed (or if
	 * there is none yet), unless closed: shutting down the server moved away
	 * from, after all it was told and asked so far.
	 *
	 * @return Whether it moved: if so, the new server knows nothing yet (e.g.
	 *         of the open document).
	 */
	private boolean follow() {
		final Environment env = doc.environment(Optional.ofNullable(file.get()));
		if (closed || environment.equals(Optional.of(env))) return false;
		environment = Optional.of(env);
		generation++;
		server = server.thenApplyAsync(old -> {
			old.ifPresent(this::stop);
			try {
				return Optional.of(start(env));
			}
			catch (final RuntimeException | LinkageError exc) {
				final Logger l = log;
				if (l != null) l.warn("Could not start a language server", exc);
				return Optional.empty();
			}
		}, TALK);
		return true;
	}

	/** Tells the server something, after all it was told so far. */
	private void tell(final Consumer<LanguageServer> message) {
		if (closed) return;
		server = server.thenApplyAsync(s -> {
			try {
				s.ifPresent(message);
			}
			catch (final RuntimeException | LinkageError exc) {
				final Logger l = log;
				if (l != null) l.debug("Language server failed", exc);
			}
			return s;
		}, TALK);
	}

	/**
	 * Asks the server something, after all it was told so far. The answer is
	 * null if there is no server (e.g. once closed).
	 */
	private <T> CompletableFuture<T> ask(
		final Function<LanguageServer, CompletableFuture<T>> question)
	{
		return ask(generation, question);
	}

	/**
	 * Asks the server moved to {@code of}-th something, if it is still the one
	 * meant: e.g. about something it made. Otherwise, the answer is null.
	 */
	private <T> CompletableFuture<T> ask(final int of,
		final Function<LanguageServer, CompletableFuture<T>> question)
	{
		if (closed || of != generation) {
			return CompletableFuture.completedFuture(null);
		}
		final CompletableFuture<T> answer = new CompletableFuture<>();
		server = server.thenApplyAsync(s -> {
			try {
				final CompletableFuture<T> a = s.isPresent() ? question.apply(s.get())
					: null;
				if (a == null) answer.complete(null);
				else a.whenComplete((r, exc) -> {
					if (exc != null) answer.completeExceptionally(exc);
					else answer.complete(r);
				});
			}
			catch (final RuntimeException | LinkageError exc) {
				answer.completeExceptionally(exc);
			}
			return s;
		}, TALK);
		return answer;
	}

	/**
	 * Launches a server of an environment, becomes its client, and initializes
	 * it: as LSP says, it awaits the server's answer before telling it anything
	 * else. (Servers in process answer at once.)
	 */
	private LanguageServer start(final Environment env) {
		final LanguageServer s = launch.apply(env);
		current = s;
		if (s instanceof LanguageClientAware) {
			((LanguageClientAware) s).connect(new Client(s));
		}
		try {
			final CompletableFuture<InitializeResult> init = s.initialize(
				new InitializeParams());
			if (init != null) init.get(INITIALIZE_TIMEOUT, TimeUnit.MILLISECONDS);
		}
		catch (final Exception exc) {
			// NB: Not initialized (in time): asked anyway, answering what it can.
		}
		s.initialized(new InitializedParams());
		return s;
	}

	/** Shuts a server down. */
	private void stop(final LanguageServer s) {
		try {
			final CompletableFuture<Object> done = s.shutdown();
			if (done != null) done.thenRun(s::exit);
		}
		catch (final RuntimeException | LinkageError exc) {
			final Logger l = log;
			if (l != null) l.debug("Language server shutdown failed", exc);
		}
	}

	/** Opens the server's copy of the script. */
	private void open() {
		final TextDocumentItem item = new TextDocumentItem(uri, doc.languageId(),
			version, doc.text());
		tell(s -> s.getTextDocumentService().didOpen(
			new DidOpenTextDocumentParams(item)));
	}

	/** Closes the server's copy of the script, if open. */
	private void closeDocument() {
		if (uri == null) return;
		final TextDocumentIdentifier id = new TextDocumentIdentifier(uri);
		tell(s -> s.getTextDocumentService().didClose(
			new DidCloseTextDocumentParams(id)));
		uri = null;
		text = null;
		doc = null;
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
	 * Maps completions' edits back, leaving out those in the added lines, and
	 * those of names only the added lines bind.
	 */
	private static CompletionList back(final ScriptDocument doc,
		final CompletionList list)
	{
		final List<CompletionItem> items = new ArrayList<>();
		for (final CompletionItem item : list.getItems()) {
			if (doc.hiddenNames().contains(leadingName(item.getLabel()))) {
				continue;
			}
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

	/** Hears the problems the current server finds, on its thread. */
	private void heard(final PublishDiagnosticsParams p) {
		final List<Diagnostic> found;
		synchronized (this) {
			if (!p.getUri().equals(uri)) return;
			// NB: Those of an earlier version are out of date.
			if (p.getVersion() != null && p.getVersion() < version) return;
			found = p.getDiagnostics() == null ? Collections.emptyList() : back(
				doc, p.getDiagnostics());
		}
		final Consumer<List<Diagnostic>> listener = diagnosticsListener;
		if (listener != null) listener.accept(found);
	}

	// -- Helper classes --

	/** Receives a server's messages. */
	private final class Client implements LanguageClient {

		/** The server whose messages these are. */
		private final LanguageServer source;

		private Client(final LanguageServer source) {
			this.source = source;
		}

		@Override
		public void publishDiagnostics(final PublishDiagnosticsParams p) {
			// NB: Those of a server moved away from are out of date.
			if (source == current) heard(p);
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
