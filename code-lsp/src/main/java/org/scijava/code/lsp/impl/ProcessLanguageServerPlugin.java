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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.lsp4j.services.LanguageServer;
import org.scijava.Disposable;
import org.scijava.Priority;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerPlugin;
import org.scijava.code.lsp.ServerProcessPlugin;
import org.scijava.log.LogService;
import org.scijava.log.StderrLogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.script.ScriptLanguage;

/**
 * Serves each LSP language a {@link ServerProcessPlugin} offers servers for
 * (e.g. {@code python}), via those server processes (see
 * {@link ProcessLanguageServer}): shared, one per environment scripts run in,
 * and stopped when disposed. At a low priority, so that plugins dedicated to a
 * language come first.
 *
 * @author Gabriel Selzer
 */
@Plugin(type = LanguageServerPlugin.class, priority = Priority.LOW)
public class ProcessLanguageServerPlugin implements LanguageServerPlugin,
	Disposable
{

	/** How long a server process may go unused before it stops, in ms. */
	private static final long IDLE = Long.getLong("scijava.lsp.idle", 10 * 60 *
		1000);

	/**
	 * How many server processes of a language may run at once (one per
	 * environment scripts run in).
	 */
	private static final int MAX = Integer.getInteger("scijava.lsp.max", 4);

	@Parameter
	private PluginService pluginService;

	@Parameter(required = false)
	private LogService log;

	/** Server processes, by LSP language identifier. */
	private final Map<String, ServerProcesses> processes = new HashMap<>();

	private List<ServerProcessPlugin> plugins;

	@Override
	public boolean supports(final String languageId) {
		return plugin(languageId) != null;
	}

	/**
	 * Serves no SciJava language: server processes see scripts as written, so
	 * they serve sessions only via plugins dedicated to a language (e.g.
	 * declaring its scripts' parameters).
	 */
	@Override
	public boolean supports(final ScriptLanguage language) {
		return false;
	}

	@Override
	public LanguageServer create(final String languageId,
		final Environment environment)
	{
		return new ProcessLanguageServer(processes(languageId), environment);
	}

	// -- Disposable methods --

	@Override
	public synchronized void dispose() {
		for (final ServerProcesses s : processes.values()) {
			s.shutdown();
		}
		processes.clear();
	}

	// -- Helper methods --

	/**
	 * The server processes for an LSP language, as the first plugin offering
	 * them says.
	 *
	 * @throws IllegalArgumentException If no plugin offers them.
	 */
	private synchronized ServerProcesses processes(final String languageId) {
		final ServerProcesses known = processes.get(languageId);
		if (known != null) return known;
		final ServerProcessPlugin p = plugin(languageId);
		if (p == null) {
			throw new IllegalArgumentException(
				"No ServerProcessPlugin offers language: " + languageId);
		}
		final ServerProcesses s = new ServerProcesses(p, log != null ? log
			: new StderrLogService(), IDLE, MAX);
		processes.put(languageId, s);
		return s;
	}

	/** The first plugin offering server processes for a language, or null. */
	private synchronized ServerProcessPlugin plugin(final String languageId) {
		if (plugins == null) {
			plugins = pluginService.createInstancesOfType(ServerProcessPlugin.class);
		}
		for (final ServerProcessPlugin p : plugins) {
			if (languageId.equals(p.languageId())) return p;
		}
		return null;
	}
}
