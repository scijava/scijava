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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.script.ScriptContext;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.services.LanguageServer;
import org.scijava.script.ScriptLanguage;
import org.scijava.service.SciJavaService;

/**
 * Keeps the language servers of a context (see {@link LanguageServerPlugin}),
 * and hands callers one server per language that stands for all of them.
 *
 * @author Gabriel Selzer
 */
public interface LanguageServerService extends SciJavaService {

	/**
	 * Starts a session about a script of the given language: where clients
	 * (e.g. editors, or tools checking scripts) ask about it, in terms of its
	 * text and offsets. Set its file, if it has one, then
	 * {@link ScriptSession#update update} it with its text.
	 */
	ScriptSession session(ScriptLanguage language);

	/**
	 * As {@link #session(ScriptLanguage)}, for code typed into a running
	 * interpreter: its state reaches the servers via their
	 * {@link Environment#live()}. For a language no plugin serves, the
	 * interpreter's variables are completed (see {@link BindingsLanguageServer}).
	 */
	ScriptSession session(ScriptLanguage language, ScriptContext live);

	/**
	 * Starts a session about a script file, as it is on disk: of its file
	 * extension's language. Returns null if no language of that extension is
	 * served.
	 */
	ScriptSession session(File file) throws IOException;

	/**
	 * Gets a server for scripts of the given language, standing for all the
	 * servers plugins offer for it: it opens, changes and closes each document
	 * on every server for that script, and merges their answers. Each call
	 * gets its own (e.g. one per editor), sharing the servers behind it. For
	 * clients speaking the protocol themselves (e.g. via JSON-RPC); others
	 * use a {@link #session}.
	 */
	MergedLanguageServer server(ScriptLanguage language);

	/**
	 * As {@link #server(ScriptLanguage)}, for code typed into a running
	 * interpreter: its state reaches the servers via their
	 * {@link Environment#live()}.
	 */
	MergedLanguageServer server(ScriptLanguage language, ScriptContext live);

	/**
	 * Tells the servers that where scripts run may have changed without the
	 * scripts changing, e.g. as an environment finished building, or packages
	 * finished installing: each open script moves to the servers of where it
	 * runs now (see {@link LanguageServerPlugin#environment}), if that
	 * changed. For plugins, when what their environments depend on changes.
	 */
	void environmentsChanged();

	/** Whether any plugin serves scripts of the given language. */
	boolean supports(ScriptLanguage language);

	/**
	 * Starts (or reuses) a server process for an LSP language (e.g.
	 * {@code python}), as a plugin offers it (see {@link LspServerPlugin}), for
	 * scripts running in the given environment; or returns null if no plugin
	 * offers one. For {@link LanguageServerPlugin}s whose servers are
	 * processes.
	 */
	LanguageServer launch(String languageId, Environment environment);

	/**
	 * Finds the problems in a script, waiting for them: for tools that ask
	 * once, rather than editors. (For a script file, whose servers may depend
	 * on where it is, use a {@link #session(File)}.)
	 *
	 * @param language The script's language.
	 * @param text The script.
	 * @param timeout How long to wait for all servers, in milliseconds; those
	 *          not done by then are left out.
	 */
	default List<Diagnostic> diagnose(final ScriptLanguage language,
		final String text, final long timeout)
	{
		try (ScriptSession session = session(language)) {
			return session.update(text).diagnostics(timeout);
		}
	}

	/**
	 * Gets the variables a script's parameters ({@code #@} lines, and old-style
	 * {@code // @} lines in its header) define before it runs, by name, with
	 * their Java types ({@code Object} if unresolvable); in declaration order.
	 * For servers to know them (see {@link ScriptDocument}). Half-typed
	 * declarations are expected, and parsed quietly.
	 */
	Map<String, Class<?>> scriptParameters(String text);
}
