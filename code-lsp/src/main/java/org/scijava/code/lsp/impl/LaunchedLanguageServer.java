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

package org.scijava.code.lsp.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.scijava.code.lsp.Environment;

/**
 * A language server process (see {@link LspServers}), seen as a
 * {@link LanguageServer}: started when first asked, restarted if it stops,
 * stopped when idle. Each document is brought up to date on the process just
 * before each request; and, for diagnostics, a moment after it last changed.
 *
 * @author Gabriel Selzer
 */
public class LaunchedLanguageServer implements LanguageServer,
	TextDocumentService, WorkspaceService, LanguageClientAware
{

	/**
	 * How long a document must stay unchanged before the process is asked for
	 * its problems, in milliseconds. TODO: Settle (around 300 ms?).
	 */
	private static final long PAUSE = Long.getLong("scijava.lsp.pause", 300);

	/** How long to wait for the process's problems, in milliseconds. */
	private static final long DIAGNOSE_TIMEOUT = 30_000;

	private static final ScheduledExecutorService TIMER = Executors
		.newSingleThreadScheduledExecutor(r -> {
			final Thread t = new Thread(r, "scijava-lsp-pause");
			t.setDaemon(true);
			return t;
		});

	private final LspServers servers;
	private final Environment environment;
	private volatile LanguageClient client;

	/** The open documents' texts and versions, by URI. */
	private final Map<String, Doc> docs = new ConcurrentHashMap<>();

	public LaunchedLanguageServer(final LspServers servers,
		final Environment environment)
	{
		this.servers = servers;
		this.environment = environment;
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
		// NB: The process is initialized when started (see LspServers).
		return CompletableFuture.completedFuture(new InitializeResult(
			new ServerCapabilities()));
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		for (final String uri : docs.keySet()) {
			servers.forEach(s -> s.close(uri));
		}
		docs.clear();
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void exit() {
		// NB: The process belongs to the LspServers.
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
		final String uri = params.getTextDocument().getUri();
		changed(uri, params.getTextDocument().getText(), params.getTextDocument()
			.getVersion());
	}

	@Override
	public void didChange(final DidChangeTextDocumentParams params) {
		if (params.getContentChanges().isEmpty()) return;
		changed(params.getTextDocument().getUri(), params.getContentChanges().get(
			params.getContentChanges().size() - 1).getText(), params
				.getTextDocument().getVersion());
	}

	@Override
	public void didClose(final DidCloseTextDocumentParams params) {
		final String uri = params.getTextDocument().getUri();
		final Doc doc = docs.remove(uri);
		if (doc != null && doc.pending != null) doc.pending.cancel(false);
		servers.forEach(s -> s.close(uri));
	}

	@Override
	public void didSave(final DidSaveTextDocumentParams params) {
		// NB: Changes come with didChange.
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
		completion(final CompletionParams params)
	{
		return request(params.getTextDocument().getUri(), s -> s.completion(
			params));
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(
		final CompletionItem item)
	{
		return servers.server(environment).thenCompose(s -> s.remote()
			.getTextDocumentService().resolveCompletionItem(item));
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(
		final SignatureHelpParams params)
	{
		return request(params.getTextDocument().getUri(), s -> s.signatureHelp(
			params));
	}

	@Override
	public CompletableFuture<Hover> hover(final HoverParams params) {
		return request(params.getTextDocument().getUri(), s -> s.hover(params));
	}

	// -- WorkspaceService --

	@Override
	public void didChangeConfiguration(final DidChangeConfigurationParams p) {}

	@Override
	public void didChangeWatchedFiles(final DidChangeWatchedFilesParams p) {}

	// -- Helper methods --

	private <T> CompletableFuture<T> request(final String uri,
		final Function<TextDocumentService, CompletableFuture<T>> request)
	{
		final Doc doc = docs.get(uri);
		if (doc == null) return CompletableFuture.completedFuture(null);
		final String text = doc.text;
		// NB: Cancelling the answer cancels the request, which tells the process
		// ($/cancelRequest).
		final CompletableFuture<T> answer = new CompletableFuture<>();
		servers.server(environment).thenAccept(s -> {
			if (answer.isDone()) return;
			final CompletableFuture<T> raw = s.request(uri, text, request);
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

	/** Notes a document's new text; asks for its problems after a pause. */
	private void changed(final String uri, final String text,
		final int version)
	{
		final Doc doc = docs.computeIfAbsent(uri, k -> new Doc());
		synchronized (doc) {
			doc.text = text;
			doc.version = version;
			if (doc.pending != null) doc.pending.cancel(false);
			doc.pending = TIMER.schedule(() -> diagnose(uri, text, version), PAUSE,
				TimeUnit.MILLISECONDS);
		}
	}

	private void diagnose(final String uri, final String text,
		final int version)
	{
		servers.server(environment).thenCompose(s -> s.diagnostics(uri, text))
			.orTimeout(DIAGNOSE_TIMEOUT, TimeUnit.MILLISECONDS).thenAccept(found -> {
				final Doc doc = docs.get(uri);
				final LanguageClient c = client;
				// NB: Only if still current.
				if (doc == null || doc.version != version || c == null) return;
				c.publishDiagnostics(new PublishDiagnosticsParams(uri, found,
					version));
			});
	}

	private static final class Doc {

		private String text;
		private int version;
		private ScheduledFuture<?> pending;
	}
}
