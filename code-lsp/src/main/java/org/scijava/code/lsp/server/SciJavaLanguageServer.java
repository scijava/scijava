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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionOptions;
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
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ServerInfo;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.scijava.Context;
import org.scijava.code.lsp.LanguageServerService;
import org.scijava.code.lsp.MergedLanguageServer;
import org.scijava.code.lsp.UpdatingCompletionList;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptService;

/**
 * A language server for SciJava scripts, for editors outside SciJava (e.g. VS
 * Code): the {@link LanguageServerService}'s servers, for every script
 * language they support, with SciJava's own understanding of scripts (e.g.
 * their {@code #@} parameters).
 * <p>
 * The language of a document is its file extension's, as the
 * {@link ScriptService} knows it. Slower completions (see
 * {@link UpdatingCompletionList}) are awaited a while, since an outside
 * editor would not ask again.
 * </p>
 *
 * @author Gabriel Selzer
 * @see Main
 */
public class SciJavaLanguageServer implements LanguageServer,
	TextDocumentService, WorkspaceService, LanguageClientAware
{

	/** How long to wait for slower completions, in milliseconds. */
	private static final long UPDATE_WAIT = 2000;

	private final ScriptService scripts;
	private final LanguageServerService servers;

	/** The server of each language, once asked about. */
	private final Map<ScriptLanguage, MergedLanguageServer> merged =
		new ConcurrentHashMap<>();

	/** The server of each open document, by URI. */
	private final Map<String, MergedLanguageServer> open =
		new ConcurrentHashMap<>();

	/** The server whose completions were last given, for resolving them. */
	private volatile MergedLanguageServer lastCompleted;

	private final ExecutorService workers = Executors.newCachedThreadPool(r -> {
		final Thread t = new Thread(r, "scijava-language-server");
		t.setDaemon(true);
		return t;
	});

	private volatile LanguageClient client;
	private volatile Runnable onExit = () -> {};

	public SciJavaLanguageServer(final Context context) {
		scripts = context.service(ScriptService.class);
		servers = context.service(LanguageServerService.class);
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
		capabilities.setSignatureHelpProvider(new SignatureHelpOptions(Arrays
			.asList("(", ",")));
		capabilities.setHoverProvider(true);
		final InitializeResult result = new InitializeResult(capabilities);
		result.setServerInfo(new ServerInfo("SciJava"));
		return CompletableFuture.completedFuture(result);
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		for (final MergedLanguageServer server : merged.values()) {
			server.shutdown();
		}
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
		final MergedLanguageServer server = serverFor(uri);
		if (server == null) return;
		open.put(uri, server);
		server.didOpen(params);
	}

	@Override
	public void didChange(final DidChangeTextDocumentParams params) {
		final MergedLanguageServer server = open.get(params.getTextDocument()
			.getUri());
		if (server != null) server.didChange(params);
	}

	@Override
	public void didClose(final DidCloseTextDocumentParams params) {
		final String uri = params.getTextDocument().getUri();
		final MergedLanguageServer server = open.remove(uri);
		if (server != null) server.didClose(params);
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
		final MergedLanguageServer server = open.get(params.getTextDocument()
			.getUri());
		if (server == null) return CompletableFuture.completedFuture(Either
			.forRight(new CompletionList()));
		return server.completion(params).thenApplyAsync(answer -> {
			CompletionList list = answer.isRight() ? answer.getRight()
				: new CompletionList(false, answer.getLeft());
			if (list instanceof UpdatingCompletionList) {
				final CompletionList better = await(((UpdatingCompletionList) list)
					.update());
				if (better != null) list = better;
			}
			lastCompleted = server;
			// NB: A plain list, for the wire.
			return Either.forRight(new CompletionList(list.isIncomplete(), list
				.getItems()));
		}, workers);
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(
		final CompletionItem item)
	{
		final MergedLanguageServer server = lastCompleted;
		return server == null ? CompletableFuture.completedFuture(item) : server
			.resolveCompletionItem(item);
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(
		final SignatureHelpParams params)
	{
		final MergedLanguageServer server = open.get(params.getTextDocument()
			.getUri());
		return server == null ? CompletableFuture.completedFuture(null) : server
			.signatureHelp(params);
	}

	@Override
	public CompletableFuture<Hover> hover(final HoverParams params) {
		final MergedLanguageServer server = open.get(params.getTextDocument()
			.getUri());
		return server == null ? CompletableFuture.completedFuture(null) : server
			.hover(params);
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

	/**
	 * The server for a document, by its file extension's language; or null if
	 * it is of no language served.
	 */
	private MergedLanguageServer serverFor(final String uri) {
		final String path = path(uri);
		final String name = path != null ? path : uri;
		final int dot = name.lastIndexOf('.');
		final ScriptLanguage language = dot < 0 ? null : scripts
			.getLanguageByExtension(name.substring(dot + 1));
		if (language == null || !servers.supports(language)) return null;
		return merged.computeIfAbsent(language, l -> {
			final MergedLanguageServer server = servers.server(l);
			server.connect(new Forwarder());
			return server;
		});
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

	/** Passes the servers' messages on to the editor. */
	private final class Forwarder implements LanguageClient {

		@Override
		public void publishDiagnostics(final PublishDiagnosticsParams p) {
			final LanguageClient c = client;
			if (c != null) c.publishDiagnostics(p);
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
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void logMessage(final MessageParams m) {
			final LanguageClient c = client;
			if (c != null) c.logMessage(m);
		}
	}
}
