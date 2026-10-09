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

import javax.script.ScriptContext;

import org.eclipse.lsp4j.services.LanguageServer;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerPlugin;
import org.scijava.code.lsp.LanguageServerService;
import org.scijava.code.lsp.ScriptSession;
import org.scijava.code.lsp.ScriptDialect;
import org.scijava.code.lsp.ScriptDocument;
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
 * Each session gets a server of its own, from the highest-priority plugin
 * serving its language, and shows it scripts as plain code in the plugin's
 * dialect. (Server processes behind them are shared; see {@link #launch}.)
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

	@Override
	public ScriptSession session(final ScriptLanguage language) {
		return session(language, null);
	}

	@Override
	public ScriptSession session(final ScriptLanguage language,
		final ScriptContext live)
	{
		// The highest-priority plugin serves the language.
		// TODO: Consider adding a "merged" LanguageServer that can wrangle all
		// matching servers. This could allow custom auto-completions (e.g.
		// SciJava Ops) and/or simplify servers combining sources themselves
		// (e.g. scripting-jython's PythonCompanion, scripting-appose-python's
		// ApposePythonLanguageServer).
		final LanguageServerPlugin plugin = plugin(language);
		LanguageServer server = null;
		ScriptDialect dialect = null;
		if (plugin != null) {
			try {
				server = plugin.create(Environment.NONE.withLive(live));
				dialect = plugin.dialect();
			}
			catch (final RuntimeException | LinkageError exc) {
				if (log != null) log.warn("Could not create a language server: " +
					plugin.getClass().getName(), exc);
			}
		}
		// NB: Without a server, the interpreter's variables (if any) are
		// completed.
		if (server == null) server = new BindingsLanguageServer(live);
		final ScriptDialect d = dialect;
		return ScriptSession.owning(server, language, d == null ? null
			: text -> ScriptDocument.of(text, scriptParameters(text), d)).setLogger(
				log);
	}

	@Override
	public boolean supports(final ScriptLanguage language) {
		return plugin(language) != null;
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
	public Map<String, Class<?>> scriptParameters(final String text) {
		return parameters.of(text);
	}

	// -- Helper methods --

	/**
	 * The highest-priority plugin serving a language, or null if none.
	 *
	 * @param language the {@link ScriptLanguage} to match
	 * @return a {@link LanguageServerPlugin} handling {@code language}
	 */
	private synchronized LanguageServerPlugin plugin(
		final ScriptLanguage language)
	{
		if (plugins == null) {
			plugins = pluginService.createInstancesOfType(
				LanguageServerPlugin.class);
		}
		if (language == null) return null;
		for (final LanguageServerPlugin p : plugins) {
			try {
				if (p.supports(language)) return p;
			}
			catch (final RuntimeException | LinkageError exc) {
				if (log != null) log.debug(exc);
			}
		}
		return null;
	}
}
