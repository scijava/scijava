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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
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
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * A language server for scripts whose text says where they run (which may
 * change as they are edited): sends each document to a server of where its
 * script runs now, as asked just before each request about it, and moves it
 * to another as that changes. A server a document moves away from is shut
 * down, if left with no documents; the others, once this is.
 * <p>
 * Sessions route their scripts so (see {@link ScriptDialect#environment}); a
 * server may route the documents it is given too (e.g. by packages they
 * declare).
 * </p>
 * <p>
 * Where scripts run is asked off the callers' threads (e.g. an editor's), as
 * that may look at files; one document at a time, in order, so that each
 * server is told of a document's changes before it is asked about it.
 * </p>
 *
 * @author Gabriel Selzer
 */
public final class RoutingLanguageServer implements LanguageServer, TextDocumentService,
	WorkspaceService, LanguageClientAware
{

	private static final ExecutorService WHERE = Executors
		.newSingleThreadExecutor(r -> {
			final Thread t = new Thread(r, "code-lsp-routing");
			t.setDaemon(true);
			return t;
		});

	private final Function<Environment, LanguageServer> launch;
	private final Function<TextDocumentItem, Environment> environment;

	/** The servers, by where the scripts they analyze run. */
	private final Map<Environment, LanguageServer> servers =
		new ConcurrentHashMap<>();

	/** The open documents, by URI. */
	private final Map<String, Doc> docs = new ConcurrentHashMap<>();

	/** The servers shut down, to tell them to exit. */
	private final List<LanguageServer> stopped = new CopyOnWriteArrayList<>();

	/** Whether this was shut down (set on {@link #WHERE} only). */
	private boolean shut;

	private volatile LanguageClient client;

	/**
	 * @param launch Gets a new server, for scripts running in an environment.
	 * @param environment Where the script of a document runs now.
	 */
	public RoutingLanguageServer(final Function<Environment, LanguageServer> launch,
		final Function<TextDocumentItem, Environment> environment)
	{
		this.launch = launch;
		this.environment = environment;
	}

	// -- LanguageClientAware --

	@Override
	public void connect(final LanguageClient c) {
		client = c;
	}

	// -- LanguageServer --

	@Override
	public CompletableFuture<InitializeResult> initialize(
		final InitializeParams params)
	{
		// NB: Each server is initialized as it is launched.
		return CompletableFuture.completedFuture(new InitializeResult(
			new ServerCapabilities()));
	}

	@Override
	public void initialized(final InitializedParams params) {}

	@Override
	public CompletableFuture<Object> shutdown() {
		// NB: After the documents' pending changes (e.g. closing them).
		return CompletableFuture.supplyAsync(() -> {
			shut = true;
			final List<CompletableFuture<Object>> all = new ArrayList<>();
			for (final LanguageServer s : servers.values()) {
				all.add(s.shutdown());
				stopped.add(s);
			}
			servers.clear();
			docs.clear();
			return all;
		}, WHERE).thenCompose(all -> CompletableFuture.allOf(all.toArray(
			new CompletableFuture[0]))).thenApply(x -> null);
	}

	@Override
	public void exit() {
		for (final LanguageServer s : stopped) s.exit();
	}

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
		final Doc doc = new Doc(item);
		docs.put(item.getUri(), doc);
		WHERE.execute(() -> route(doc));
	}

	@Override
	public void didChange(final DidChangeTextDocumentParams params) {
		final Doc doc = docs.get(params.getTextDocument().getUri());
		if (doc == null || params.getContentChanges().isEmpty()) return;
		doc.change(params.getContentChanges().get(params.getContentChanges()
			.size() - 1).getText(), params.getTextDocument().getVersion());
		WHERE.execute(() -> route(doc));
	}

	@Override
	public void didClose(final DidCloseTextDocumentParams params) {
		final Doc doc = docs.remove(params.getTextDocument().getUri());
		// NB: Its server stays, e.g. for the document under another name.
		if (doc != null) WHERE.execute(() -> leave(doc, false));
	}

	@Override
	public void didSave(final DidSaveTextDocumentParams params) {
		// NB: Changes come with didChange.
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
		completion(final CompletionParams params)
	{
		return request(params.getTextDocument().getUri(), (s, documents) -> documents
			.completion(params).thenApply(answer -> mark(answer, s)));
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(
		final CompletionItem item)
	{
		// NB: Resolved by the server that completed it.
		if (!(item.getData() instanceof Origin)) {
			return CompletableFuture.completedFuture(item);
		}
		final Origin origin = (Origin) item.getData();
		item.setData(origin.data);
		return origin.server.getTextDocumentService().resolveCompletionItem(item)
			.handle((resolved, error) -> {
				final CompletionItem out = resolved != null ? resolved : item;
				out.setData(origin);
				return out;
			});
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(
		final SignatureHelpParams params)
	{
		return request(params.getTextDocument().getUri(), (s,
			documents) -> documents.signatureHelp(params));
	}

	@Override
	public CompletableFuture<Hover> hover(final HoverParams params) {
		return request(params.getTextDocument().getUri(), (s,
			documents) -> documents.hover(params));
	}

	// -- WorkspaceService --

	@Override
	public void didChangeConfiguration(final DidChangeConfigurationParams p) {}

	@Override
	public void didChangeWatchedFiles(final DidChangeWatchedFilesParams p) {}

	// -- Helper methods --

	/**
	 * Asks the server of where a document's script runs now (once it is up to
	 * date there). NB: Cancelling the answer cancels the request.
	 */
	private <T> CompletableFuture<T> request(final String uri,
		final Request<T> request)
	{
		final Doc doc = docs.get(uri);
		if (doc == null) return CompletableFuture.completedFuture(null);
		final CompletableFuture<T> answer = new CompletableFuture<>();
		CompletableFuture.supplyAsync(() -> route(doc), WHERE).thenAccept(s -> {
			if (answer.isDone()) return;
			if (s == null) {
				answer.complete(null); // NB: Shut down.
				return;
			}
			final CompletableFuture<T> raw = request.ask(s, s
				.getTextDocumentService());
			answer.whenComplete((r, error) -> {
				if (answer.isCancelled()) raw.cancel(true);
			});
			raw.whenComplete((r, error) -> {
				if (error != null) answer.completeExceptionally(error);
				else answer.complete(r);
			});
		}).exceptionally(error -> {
			answer.completeExceptionally(error);
			return null;
		});
		return answer;
	}

	/**
	 * Brings a document up to date on the server of where its script runs now:
	 * if that changed, closes it on the server of where it ran, and opens it on
	 * this one. Called on {@link #WHERE} only.
	 *
	 * @return The server, or null if this was shut down.
	 */
	private LanguageServer route(final Doc doc) {
		if (shut) return null;
		final TextDocumentItem item = doc.item();
		final LanguageServer target = server(environment.apply(item));
		if (doc.server != target) {
			leave(doc, true);
			// NB: First, so that its problems (e.g. found at once) are heard.
			doc.server = target;
			target.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
				item));
		}
		else if (doc.sent != item.getVersion()) {
			target.getTextDocumentService().didChange(new DidChangeTextDocumentParams(
				new VersionedTextDocumentIdentifier(item.getUri(), item.getVersion()),
				Collections.singletonList(new TextDocumentContentChangeEvent(item
					.getText()))));
		}
		doc.sent = item.getVersion();
		return target;
	}

	/**
	 * Closes a document on its server. Called on {@link #WHERE} only.
	 *
	 * @param moving Whether the document moves to another server: if so, its
	 *          server is shut down, if it has no other documents.
	 */
	private void leave(final Doc doc, final boolean moving) {
		final LanguageServer s = doc.server;
		if (s == null || shut) return;
		doc.server = null;
		s.getTextDocumentService().didClose(new DidCloseTextDocumentParams(
			new TextDocumentIdentifier(doc.uri)));
		if (!moving) return;
		for (final Doc other : docs.values()) {
			if (other.server == s) return;
		}
		servers.values().remove(s);
		s.shutdown();
	}

	/** The server of an environment, launching it if needed. */
	private LanguageServer server(final Environment env) {
		return servers.computeIfAbsent(env, e -> {
			final LanguageServer s = launch.apply(e);
			if (s instanceof LanguageClientAware) {
				((LanguageClientAware) s).connect(new Forwarding(s));
			}
			s.initialize(new InitializeParams()).thenRun(() -> s.initialized(
				new InitializedParams()));
			return s;
		});
	}

	/** Marks each completion with the server that made it, for resolving. */
	private static Either<List<CompletionItem>, CompletionList> mark(
		final Either<List<CompletionItem>, CompletionList> answer,
		final LanguageServer s)
	{
		if (answer == null) return null;
		final List<CompletionItem> items = answer.isRight() ? answer.getRight()
			.getItems() : answer.getLeft();
		if (items != null) {
			for (final CompletionItem item : items) {
				item.setData(new Origin(s, item.getData()));
			}
		}
		return answer;
	}

	/** Asks a server something. */
	private interface Request<T> {

		CompletableFuture<T> ask(LanguageServer server,
			TextDocumentService documents);
	}

	/** An open document. */
	private static final class Doc {

		private final String uri;
		private final String languageId;
		private String text;
		private int version;
		/** The server it is open on (set on {@link #WHERE} only). */
		private volatile LanguageServer server;
		/** The version its server last got (on {@link #WHERE} only). */
		private int sent = -1;

		private Doc(final TextDocumentItem item) {
			uri = item.getUri();
			languageId = item.getLanguageId();
			text = item.getText();
			version = item.getVersion();
		}

		private synchronized void change(final String newText,
			final int newVersion)
		{
			text = newText;
			version = newVersion;
		}

		private synchronized TextDocumentItem item() {
			return new TextDocumentItem(uri, languageId, version, text);
		}
	}

	/** The server a completion was made by, and the data it came with. */
	private static final class Origin {

		private final LanguageServer server;
		private final Object data;

		private Origin(final LanguageServer server, final Object data) {
			this.server = server;
			this.data = data;
		}
	}

	/**
	 * Passes on what a server tells the client: its problems with a document
	 * only while the document is open on it.
	 */
	private final class Forwarding implements LanguageClient {

		private final LanguageServer server;

		private Forwarding(final LanguageServer server) {
			this.server = server;
		}

		@Override
		public void publishDiagnostics(final PublishDiagnosticsParams p) {
			final LanguageClient c = client;
			final Doc doc = docs.get(p.getUri());
			if (c != null && doc != null && doc.server == server) {
				c.publishDiagnostics(p);
			}
		}

		@Override
		public void telemetryEvent(final Object object) {}

		@Override
		public void showMessage(final MessageParams m) {
			final LanguageClient c = client;
			if (c != null) c.showMessage(m);
		}

		@Override
		public CompletableFuture<MessageActionItem> showMessageRequest(
			final ShowMessageRequestParams r)
		{
			final LanguageClient c = client;
			return c != null ? c.showMessageRequest(r) : CompletableFuture
				.completedFuture(null);
		}

		@Override
		public void logMessage(final MessageParams m) {
			final LanguageClient c = client;
			if (c != null) c.logMessage(m);
		}
	}
}
