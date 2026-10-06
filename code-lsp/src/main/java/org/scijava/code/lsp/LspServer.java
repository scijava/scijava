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

import java.util.Collections;
import java.util.HashMap;
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
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
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
import org.scijava.code.api.EnvironmentContext;
import org.scijava.log.Logger;

/**
 * A running, initialized language server, and the documents opened in it.
 *
 * @author Gabriel Selzer
 * @see LspServers
 */
public final class LspServer {

	/** Threads reading servers' messages; daemons, so as not to keep the JVM. */
	private static final ExecutorService LISTENERS = Executors
		.newCachedThreadPool(r -> {
			final Thread t = new Thread(r, "scijava-lsp-listener");
			t.setDaemon(true);
			return t;
		});

	private final String languageId;
	private final LspConnection connection;
	private final LanguageServer remote;
	private final Future<Void> listening;

	/** Open documents' versions and texts, by URI. */
	private final Map<String, Document> documents = new HashMap<>();

	private volatile long lastUsed = System.currentTimeMillis();

	private LspServer(final String languageId, final LspConnection connection,
		final LanguageServer remote, final Future<Void> listening)
	{
		this.languageId = languageId;
		this.connection = connection;
		this.remote = remote;
		this.listening = listening;
	}

	/**
	 * Starts and initializes a server. Blocks until it is ready.
	 *
	 * @param spec How to start it.
	 * @param env Where the scripts it will analyze run.
	 * @param log Where to report the server's messages.
	 * @param timeout How long to wait for it to initialize, in milliseconds.
	 */
	public static LspServer start(final LspServerSpec spec,
		final EnvironmentContext env, final Logger log, final long timeout)
		throws Exception
	{
		final LspConnection connection = spec.start(env);
		try {
			final Launcher<LanguageServer> launcher = LSPLauncher
				.createClientLauncher(new Client(log), connection.input(), connection
					.output(), LISTENERS, null);
			final Future<Void> listening = launcher.startListening();
			final LanguageServer remote = launcher.getRemoteProxy();

			final InitializeParams init = new InitializeParams();
			init.setProcessId((int) ProcessHandle.current().pid());
			init.setClientInfo(new org.eclipse.lsp4j.ClientInfo("SciJava"));
			init.setCapabilities(capabilities());
			final String root = spec.rootUri(env);
			if (root != null) {
				init.setRootUri(root);
				init.setWorkspaceFolders(Collections.singletonList(new WorkspaceFolder(
					root, "scripts")));
			}
			init.setInitializationOptions(spec.initializationOptions(env));
			remote.initialize(init).get(timeout, TimeUnit.MILLISECONDS);
			remote.initialized(new InitializedParams());
			return new LspServer(spec.languageId(), connection, remote, listening);
		}
		catch (final Exception exc) {
			connection.close();
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
		return connection.isAlive() && !listening.isDone();
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
			connection.close();
			listening.cancel(true);
		}
	}

	// -- Helper methods --

	private void sync(final String uri, final String text) {
		lastUsed = System.currentTimeMillis();
		final TextDocumentService docs = remote.getTextDocumentService();
		final Document doc = documents.get(uri);
		if (doc == null) {
			documents.put(uri, new Document(text));
			docs.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri,
				languageId, 1, text)));
		}
		else if (!doc.text.equals(text)) {
			doc.text = text;
			doc.version++;
			docs.didChange(new DidChangeTextDocumentParams(
				new VersionedTextDocumentIdentifier(uri, doc.version), Collections
					.singletonList(new TextDocumentContentChangeEvent(text))));
		}
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

	/** Receives the server's messages. */
	private static final class Client implements LanguageClient {

		private final Logger log;

		private Client(final Logger log) {
			this.log = log;
		}

		@Override
		public void telemetryEvent(final Object object) {
			// NB: Not of interest.
		}

		@Override
		public void publishDiagnostics(final PublishDiagnosticsParams diagnostics) {
			// NB: Not shown (yet).
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
