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

import org.eclipse.lsp4j.services.LanguageServer;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerPlugin;
import org.scijava.code.lsp.LanguageServerService;
import org.scijava.code.lsp.ScriptDialect;
import org.scijava.code.lsp.ScriptDocument;
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
 * The highest-priority dialect of a script language makes its scripts plain
 * code; the highest-priority plugin serving that code's language serves it.
 * Plugins are disposed with the context, e.g. to stop the server processes
 * they keep.
 * </p>
 *
 * @author Gabriel Selzer
 */
@Plugin(type = Service.class)
public class DefaultLanguageServerService extends AbstractService implements
	LanguageServerService {

	@Parameter
	private PluginService pluginService;

	@Parameter(required = false)
	private LogService log;

	private final ScriptProcessing processing = new ScriptProcessing(
			this::context);

	private List<LanguageServerPlugin> plugins;

	@Override
	public ScriptDocument document(final String script,
		final ScriptDialect dialect)
	{
		final ScriptProcessing.Processed p = processing.of(script);
		return ScriptDocument.of(script, p.parameters, p.removedLines, dialect);
	}

	@Override
	public boolean supports(final ScriptLanguage language) {
		try {
			return supports(dialect(language).languageId());
		}
		catch (final IllegalArgumentException exc) {
			return false;
		}
	}

	@Override
	public boolean supports(final String languageId) {
		try {
			plugin(languageId);
			return true;
		}
		catch (final IllegalArgumentException exc) {
			return false;
		}
	}

	@Override
	public LanguageServer launch(final String languageId,
		final Environment environment)
	{
		return plugin(languageId).create(languageId, environment);
	}

	@Override
	public Map<String, Class<?>> scriptParameters(final String text) {
		return processing.of(text).parameters;
	}

	// -- Disposable methods --

	@Override
	public synchronized void dispose() {
		// NB: The plugins this created, e.g. those keeping server processes.
		if (plugins == null) return;
		for (final LanguageServerPlugin p : plugins) {
			if (p instanceof Disposable) ((Disposable) p).dispose();
		}
		plugins = null;
	}

	// -- Helper methods --

	/**
	 * The highest-priority dialect of a script language.
	 *
	 * @throws IllegalArgumentException If the language has none.
	 */
	@Override
	public synchronized ScriptDialect dialect(final ScriptLanguage language) {
		for (var p : pluginService.createInstancesOfType(ScriptDialect.class)) {
			if (p.supports(language)) return p;
		}
		throw new IllegalArgumentException(
			"No ScriptDialect for script language: " + language);
	}

	/**
	 * The highest-priority plugin serving an LSP language.
	 *
	 * @throws IllegalArgumentException If no plugin serves the language.
	 */
	private synchronized LanguageServerPlugin plugin(final String languageId) {
		if (plugins == null) {
			plugins = pluginService.createInstancesOfType(
				LanguageServerPlugin.class);
		}
		for (var p : plugins) {
			if (p.supports(languageId)) return p;
		}
		throw new IllegalArgumentException(
			"No LanguageServerPlugin serves language: " + languageId);
	}
}