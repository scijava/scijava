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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptContext;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.script.ScriptLanguage;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default implementation of {@link LanguageServerService}.
 * <p>
 * Servers are created once per plugin and {@link Environment}, initialized,
 * and shared by all {@link MergedLanguageServer}s. Each server's diagnostics
 * reach the merged server that has the document open.
 * </p>
 *
 * @author Gabriel Selzer
 */
@Plugin(type = Service.class)
public class DefaultLanguageServerService extends AbstractService implements
	LanguageServerService
{

	@Parameter
	private PluginService pluginService;

	@Parameter(required = false)
	private LspService lspService;

	@Parameter(required = false)
	private LogService log;

	private final ScriptParameters parameters = new ScriptParameters(
		this::context);

	private List<LanguageServerPlugin> plugins;

	/** The servers, by plugin and environment. */
	private final Map<Key, CompletableFuture<LanguageServer>> servers =
		new HashMap<>();

	/** Which merged server has each document open. */
	private final Map<String, MergedLanguageServer> owners =
		new ConcurrentHashMap<>();

	@Override
	public MergedLanguageServer server(final ScriptLanguage language) {
		return server(language, null);
	}

	@Override
	public MergedLanguageServer server(final ScriptLanguage language,
		final ScriptContext live)
	{
		return new MergedLanguageServer(this, language, plugins(language), live);
	}

	@Override
	public boolean supports(final ScriptLanguage language) {
		return !plugins(language).isEmpty();
	}

	@Override
	public LanguageServer launch(final String languageId,
		final Environment environment)
	{
		final LspServers s = lspService == null ? null : lspService.servers(
			languageId);
		return s == null ? null : new LaunchedLanguageServer(s, environment);
	}

	@Override
	public List<Diagnostic> diagnose(final ScriptLanguage language,
		final String uri, final String text, final long timeout)
	{
		final MergedLanguageServer server = server(language);
		server.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri,
			language.getLanguageName(), 1, text)));
		try {
			server.awaitDiagnostics(uri).get(timeout, TimeUnit.MILLISECONDS);
		}
		catch (final Exception exc) {
			// NB: Not all servers in time: those that were.
		}
		final List<Diagnostic> found = server.diagnostics(uri);
		server.didClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(
			uri)));
		return found;
	}

	@Override
	public Map<String, Class<?>> scriptParameters(final String text) {
		return parameters.of(text);
	}

	@Override
	public synchronized void dispose() {
		for (final CompletableFuture<LanguageServer> f : servers.values()) {
			f.thenAccept(s -> {
				try {
					s.shutdown().get(2, TimeUnit.SECONDS);
					s.exit();
				}
				catch (final Exception exc) {
					// NB: Stopping anyway.
				}
			});
		}
		servers.clear();
	}

	// -- Internal methods --

	/** Gets the (initialized) server of a plugin for an environment. */
	synchronized CompletableFuture<LanguageServer> server(
		final LanguageServerPlugin plugin, final Environment environment)
	{
		final Key key = new Key(plugin, environment);
		CompletableFuture<LanguageServer> known = servers.get(key);
		if (known != null && !known.isCompletedExceptionally()) return known;
		final LanguageServer server;
		try {
			server = plugin.create(environment);
		}
		catch (final RuntimeException | LinkageError exc) {
			if (log != null) log.warn("Could not create a language server: " +
				plugin.getClass().getName(), exc);
			return failed(exc);
		}
		if (server instanceof LanguageClientAware) {
			((LanguageClientAware) server).connect(new Dispatcher(server));
		}
		known = server.initialize(new InitializeParams()).thenApply(result -> {
			server.initialized(new InitializedParams());
			return server;
		});
		servers.put(key, known);
		return known;
	}

	/** Notes which merged server has a document open (null: none). */
	void own(final String uri, final MergedLanguageServer owner) {
		if (owner == null) owners.remove(uri);
		else owners.put(uri, owner);
	}

	LogService logger() {
		return log;
	}

	// -- Helper methods --

	/** The plugins serving a language, highest priority first. */
	private synchronized List<LanguageServerPlugin> plugins(
		final ScriptLanguage language)
	{
		if (plugins == null) {
			plugins = pluginService.createInstancesOfType(
				LanguageServerPlugin.class);
		}
		final List<LanguageServerPlugin> out = new ArrayList<>();
		if (language == null) return out;
		for (final LanguageServerPlugin p : plugins) {
			try {
				if (p.supports(language)) out.add(p);
			}
			catch (final RuntimeException | LinkageError exc) {
				if (log != null) log.debug(exc);
			}
		}
		return Collections.unmodifiableList(out);
	}

	private static <T> CompletableFuture<T> failed(final Throwable t) {
		final CompletableFuture<T> f = new CompletableFuture<>();
		f.completeExceptionally(t);
		return f;
	}

	private static final class Key {

		private final LanguageServerPlugin plugin;
		private final Environment environment;

		private Key(final LanguageServerPlugin plugin,
			final Environment environment)
		{
			this.plugin = plugin;
			this.environment = environment;
		}

		@Override
		public boolean equals(final Object o) {
			return o instanceof Key && ((Key) o).plugin == plugin && ((Key) o).environment
				.equals(environment);
		}

		@Override
		public int hashCode() {
			return Objects.hash(System.identityHashCode(plugin), environment);
		}
	}

	/** Passes a server's diagnostics to the merged server owning the document. */
	private final class Dispatcher implements LanguageClient {

		private final LanguageServer server;

		private Dispatcher(final LanguageServer server) {
			this.server = server;
		}

		@Override
		public void publishDiagnostics(final PublishDiagnosticsParams p) {
			final MergedLanguageServer owner = owners.get(p.getUri());
			if (owner != null) owner.received(server, p);
		}

		@Override
		public void telemetryEvent(final Object object) {}

		@Override
		public void showMessage(final MessageParams m) {
			if (log != null) log.debug("Language server: " + m.getMessage());
		}

		@Override
		public CompletableFuture<MessageActionItem> showMessageRequest(
			final ShowMessageRequestParams r)
		{
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void logMessage(final MessageParams m) {
			if (log != null) log.debug("Language server: " + m.getMessage());
		}
	}
}
