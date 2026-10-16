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

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.script.ScriptContext;

import org.eclipse.lsp4j.services.LanguageServer;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerPlugin;
import org.scijava.code.lsp.LanguageServerService;
import org.scijava.code.lsp.ScriptSession;
import org.scijava.code.lsp.ScriptDialect;
import org.scijava.code.lsp.ScriptDocument;
import org.scijava.code.lsp.server.RoutingLanguageServer;
import org.scijava.Disposable;
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
 * Each session shows its servers the script as plain code, as the
 * highest-priority dialect of its language turns it; and gets servers of its
 * own, from the highest-priority plugin serving the code's language, one per
 * environment the script runs in. Plugins are disposed with the context, e.g.
 * to stop the server processes they keep.
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
	private LogService log;

	private final ScriptParameters parameters = new ScriptParameters(
		this::context);

	private List<LanguageServerPlugin> plugins;

	private List<ScriptDialect> dialects;

	@Override
	public ScriptSession session(final ScriptLanguage language) {
		return session(language, null);
	}

	@Override
	public ScriptSession session(final ScriptLanguage language,
		final ScriptContext live)
	{
		// The script is code of the language its dialect says, analyzed by the
		// highest-priority plugin serving that language.
		// TODO: Consider adding a "merged" LanguageServer that can wrangle all
		// matching servers. This could allow custom auto-completions (e.g.
		// SciJava Ops) and/or simplify servers combining sources themselves
		// (e.g. scijava-code-lsp-jython's PythonCompanion).
		final ScriptDialect dialect = dialect(language);
		if (dialect == null || !supports(dialect.languageId())) {
			// NB: Without servers, the interpreter's variables (if any) are
			// completed.
			return ScriptSession.owning(new BindingsLanguageServer(live), language)
				.setLogger(log);
		}
		final String languageId = dialect.languageId();
		final LanguageServer server = new RoutingLanguageServer(env -> server(
			languageId, env.withLive(live)), item -> dialect.environment(item
				.getText(), file(item.getUri())));
		return ScriptSession.owning(server, language, languageId,
			text -> ScriptDocument.of(text, scriptParameters(text), dialect))
			.setLogger(log);
	}

	@Override
	public boolean supports(final ScriptLanguage language) {
		final ScriptDialect dialect = dialect(language);
		return dialect != null && supports(dialect.languageId());
	}

	@Override
	public boolean supports(final String languageId) {
		return plugin(languageId) != null;
	}

	@Override
	public LanguageServer launch(final String languageId,
		final Environment environment)
	{
		final LanguageServerPlugin plugin = plugin(languageId);
		if (plugin == null) {
			throw new IllegalArgumentException(
				"No LanguageServerPlugin serves language: " + languageId);
		}
		return plugin.create(languageId, environment);
	}

	@Override
	public Map<String, Class<?>> scriptParameters(final String text) {
		return parameters.of(text);
	}

	// -- Disposable methods --

	@Override
	public synchronized void dispose() {
		dialects = null;
		// NB: The plugins this created, e.g. those keeping server processes.
		if (plugins == null) return;
		for (final LanguageServerPlugin p : plugins) {
			if (p instanceof Disposable) ((Disposable) p).dispose();
		}
		plugins = null;
	}

	// -- Helper methods --

	/**
	 * A session's server of an environment; or, if none can be created, one
	 * completing the interpreter's variables (if any).
	 */
	private LanguageServer server(final String languageId,
		final Environment environment)
	{
		try {
			return launch(languageId, environment);
		}
		catch (final RuntimeException | LinkageError exc) {
			if (log != null) log.warn("Could not create a language server for " +
				languageId, exc);
			return new BindingsLanguageServer(environment.live());
		}
	}

	/** The file of a {@code file:} URI, or null. */
	private static File file(final String uri) {
		try {
			final URI u = URI.create(uri);
			return "file".equals(u.getScheme()) ? new File(u) : null;
		}
		catch (final IllegalArgumentException exc) {
			return null;
		}
	}

	/** The highest-priority dialect of a script language, or null if none. */
	private synchronized ScriptDialect dialect(final ScriptLanguage language) {
		if (language == null) return null;
		if (dialects == null) {
			dialects = pluginService.createInstancesOfType(ScriptDialect.class);
		}
		return first(dialects, d -> d.supports(language));
	}

	/**
	 * The highest-priority plugin serving an LSP language, or null if none.
	 */
	private synchronized LanguageServerPlugin plugin(final String languageId) {
		if (plugins == null) {
			plugins = pluginService.createInstancesOfType(
				LanguageServerPlugin.class);
		}
		return first(plugins, p -> p.supports(languageId));
	}

	/** The first plugin matching, or null if none. */
	private <T> T first(final List<T> candidates, final Predicate<T> matches) {
		for (final T p : candidates) {
			try {
				if (matches.test(p)) return p;
			}
			catch (final RuntimeException | LinkageError exc) {
				if (log != null) log.debug(exc);
			}
		}
		return null;
	}
}
