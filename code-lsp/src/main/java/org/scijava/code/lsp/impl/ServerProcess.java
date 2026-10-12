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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.HoverCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.PublishDiagnosticsCapabilities;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SignatureHelpCapabilities;
import org.eclipse.lsp4j.SignatureInformationCapabilities;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.ServerProcessPlugin;
import org.scijava.log.Logger;

/**
 * A running, initialized language server, and the documents opened in it.
 *
 * @author Gabriel Selzer
 * @see ServerProcesses
 */
public final class ServerProcess {

	/** Threads reading servers' messages; daemons, so as not to keep the JVM. */
	private static final ExecutorService LISTENERS = Executors
		.newCachedThreadPool(r -> {
			final Thread t = new Thread(r, "scijava-lsp-listener");
			t.setDaemon(true);
			return t;
		});

	private final String languageId;
	private final Process process;
	private final LanguageServer remote;
	private final Future<Void> listening;

	/** Open documents' versions and texts, by URI. */
	private final Map<String, Document> documents = new HashMap<>();

	/** Diagnostics the server published. */
	private final Published published;

	private volatile long lastUsed = System.currentTimeMillis();

	private ServerProcess(final String languageId, final Process process,
		final LanguageServer remote, final Future<Void> listening,
		final Published published)
	{
		this.languageId = languageId;
		this.published = published;
		this.process = process;
		this.remote = remote;
		this.listening = listening;
	}

	/**
	 * Starts and initializes a server. Blocks until it is ready.
	 *
	 * @param plugin How to start it.
	 * @param env Where the scripts it will analyze run.
	 * @param log Where to report the server's messages.
	 * @param timeout How long to wait for it to initialize, in milliseconds.
	 */
	public static ServerProcess start(final ServerProcessPlugin plugin,
		final Environment env, final Logger log, final long timeout)
		throws Exception
	{
		final Process process = plugin.start(env);
		try {
			final Published published = new Published();
			final Launcher<LanguageServer> launcher = LSPLauncher
				.createClientLauncher(new Client(log, published), process
					.getInputStream(), process.getOutputStream(), LISTENERS, null);
			final Future<Void> listening = launcher.startListening();
			final LanguageServer remote = launcher.getRemoteProxy();

			final InitializeParams init = new InitializeParams();
			init.setProcessId((int) ProcessHandle.current().pid());
			init.setClientInfo(new org.eclipse.lsp4j.ClientInfo("SciJava"));
			init.setCapabilities(capabilities());
			final String root = plugin.rootUri(env);
			if (root != null) {
				init.setRootUri(root);
				init.setWorkspaceFolders(Collections.singletonList(new WorkspaceFolder(
					root, "scripts")));
			}
			init.setInitializationOptions(plugin.initializationOptions(env));
			remote.initialize(init).get(timeout, TimeUnit.MILLISECONDS);
			remote.initialized(new InitializedParams());
			return new ServerProcess(plugin.languageId(), process, remote, listening,
				published);
		}
		catch (final Exception exc) {
			process.destroy();
			throw exc;
		}
	}

	/**
	 * Sends a request about a document, first bringing the server's copy of the
	 * document up to date (opening it, if needed).
	 *
	 * @param uri The document's URI.
	 * @param text The document's text.
	 * @param request Sends the request.
	 * @return The server's answer.
	 */
	public synchronized <T> CompletableFuture<T> request(final String uri,
		final String text,
		final Function<TextDocumentService, CompletableFuture<T>> request)
	{
		// NB: Synchronized, so that no other request comes between the document
		// update and this request.
		sync(uri, text);
		return request.apply(remote.getTextDocumentService());
	}

	/**
	 * Gets the diagnostics of a document, as the server publishes them once
	 * it has analyzed this text of it (first bringing the server's copy of the
	 * document up to date).
	 */
	public synchronized CompletableFuture<List<Diagnostic>> diagnostics(
		final String uri, final String text)
	{
		final boolean changed = sync(uri, text);
		final int version = documents.get(uri).version;
		return published.await(uri, version, !changed);
	}

	/** Opens a document, or brings the server's copy up to date. */
	public synchronized void open(final String uri, final String text) {
		sync(uri, text);
	}

	/** Closes a document, if open. */
	public synchronized void close(final String uri) {
		if (documents.remove(uri) == null || !isAlive()) return;
		remote.getTextDocumentService().didClose(new DidCloseTextDocumentParams(
			new TextDocumentIdentifier(uri)));
	}

	/** The server's proxy, e.g. for requests not about a document. */
	public LanguageServer remote() {
		lastUsed = System.currentTimeMillis();
		return remote;
	}

	/** Whether the server still runs. */
	public boolean isAlive() {
		return process.isAlive() && !listening.isDone();
	}

	/** When the server was last used, in milliseconds since the epoch. */
	public long lastUsed() {
		return lastUsed;
	}

	/** Stops the server, asking it nicely first. */
	public void shutdown() {
		try {
			if (isAlive()) {
				remote.shutdown().get(2, TimeUnit.SECONDS);
				remote.exit();
			}
		}
		catch (final Exception exc) {
			// NB: Stopped below, regardless.
		}
		finally {
			process.destroy();
			listening.cancel(true);
		}
	}

	// -- Helper methods --

	/** Brings the server's copy of a document up to date: whether it was not. */
	private boolean sync(final String uri, final String text) {
		lastUsed = System.currentTimeMillis();
		final TextDocumentService docs = remote.getTextDocumentService();
		final Document doc = documents.get(uri);
		if (doc == null) {
			documents.put(uri, new Document(text));
			published.sent(uri, 1);
			docs.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri,
				languageId, 1, text)));
			return true;
		}
		if (doc.text.equals(text)) return false;
		doc.text = text;
		doc.version++;
		published.sent(uri, doc.version);
		docs.didChange(new DidChangeTextDocumentParams(
			new VersionedTextDocumentIdentifier(uri, doc.version), Collections
				.singletonList(new TextDocumentContentChangeEvent(text))));
		return true;
	}

	/** What this client can do: plain text documentation, no snippets. */
	private static ClientCapabilities capabilities() {
		final CompletionItemCapabilities item = new CompletionItemCapabilities(
			false);
		item.setDocumentationFormat(Collections.singletonList(
			MarkupKind.PLAINTEXT));
		final CompletionCapabilities completion = new CompletionCapabilities(item);
		final SignatureInformationCapabilities signature =
			new SignatureInformationCapabilities(Collections.singletonList(
				MarkupKind.PLAINTEXT));
		final TextDocumentClientCapabilities text =
			new TextDocumentClientCapabilities();
		text.setCompletion(completion);
		text.setSignatureHelp(new SignatureHelpCapabilities(signature, false));
		text.setHover(new HoverCapabilities(Collections.singletonList(
			MarkupKind.PLAINTEXT), false));
		final PublishDiagnosticsCapabilities diagnostics =
			new PublishDiagnosticsCapabilities();
		diagnostics.setVersionSupport(true);
		text.setPublishDiagnostics(diagnostics);
		final ClientCapabilities capabilities = new ClientCapabilities();
		capabilities.setTextDocument(text);
		return capabilities;
	}

	private static final class Document {

		private String text;
		private int version = 1;

		private Document(final String text) {
			this.text = text;
		}
	}

	/**
	 * Diagnostics the server published, by document URI; and those awaited.
	 * A server publishes diagnostics whenever it likes (e.g. after each change
	 * of a document), not on request.
	 */
	private static final class Published {

		private final Map<String, List<Diagnostic>> latest = new HashMap<>();
		/** The last version sent of each document. */
		private final Map<String, Integer> sent = new HashMap<>();
		/** Since when (which version) each document's latest diagnostics are. */
		private final Map<String, Integer> since = new HashMap<>();
		private final Map<String, List<Waiter>> waiting = new HashMap<>();

		private synchronized void sent(final String uri, final int version) {
			sent.put(uri, version);
		}

		/**
		 * Gets the diagnostics for (at least) the given version of a document.
		 *
		 * @param known Whether this version was analyzed before (unchanged).
		 */
		private synchronized CompletableFuture<List<Diagnostic>> await(
			final String uri, final int version, final boolean known)
		{
			final Integer have = since.get(uri);
			if (known && have != null && have >= version) {
				return CompletableFuture.completedFuture(latest.get(uri));
			}
			final Waiter w = new Waiter(version);
			waiting.computeIfAbsent(uri, k -> new ArrayList<>()).add(w);
			return w.future;
		}

		private synchronized void publish(final PublishDiagnosticsParams p) {
			final String uri = p.getUri();
			// NB: Without a version, the diagnostics are for the last one sent.
			final int version = p.getVersion() != null ? p.getVersion() : sent
				.getOrDefault(uri, 0);
			final List<Diagnostic> list = p.getDiagnostics() == null ? Collections
				.emptyList() : p.getDiagnostics();
			latest.put(uri, list);
			since.put(uri, version);
			final List<Waiter> ws = waiting.get(uri);
			if (ws == null) return;
			ws.removeIf(w -> {
				if (w.version > version) return false;
				w.future.complete(list);
				return true;
			});
		}

		private static final class Waiter {

			private final int version;
			private final CompletableFuture<List<Diagnostic>> future =
				new CompletableFuture<>();

			private Waiter(final int version) {
				this.version = version;
			}
		}
	}

	/** Receives the server's messages. */
	private static final class Client implements LanguageClient {

		private final Logger log;
		private final Published published;

		private Client(final Logger log, final Published published) {
			this.log = log;
			this.published = published;
		}

		@Override
		public void telemetryEvent(final Object object) {
			// NB: Not of interest.
		}

		@Override
		public void publishDiagnostics(final PublishDiagnosticsParams diagnostics) {
			published.publish(diagnostics);
		}

		@Override
		public void showMessage(final MessageParams message) {
			if (message.getType() == MessageType.Error) {
				log.warn("Language server: " + message.getMessage());
			}
			else log.debug("Language server: " + message.getMessage());
		}

		@Override
		public CompletableFuture<MessageActionItem> showMessageRequest(
			final ShowMessageRequestParams request)
		{
			log.debug("Language server: " + request.getMessage());
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void logMessage(final MessageParams message) {
			log.debug("Language server: " + message.getMessage());
		}
	}
}
