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

import java.util.Map;

import javax.script.ScriptContext;

import org.eclipse.lsp4j.services.LanguageServer;
import org.scijava.script.ScriptLanguage;
import org.scijava.service.SciJavaService;

/**
 * Serves SciJava scripts with language servers: a {@link ScriptSession} about
 * a script shows servers of its own (from the highest-priority
 * {@link LanguageServerPlugin} serving its {@link ScriptDialect}'s language)
 * the script as plain code. Servers of an LSP language can also be
 * {@link #launch launched} directly.
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
	 * interpreter's variables are completed.
	 */
	ScriptSession session(ScriptLanguage language, ScriptContext live);

	/** Whether any plugin serves scripts of the given language. */
	boolean supports(ScriptLanguage language);

	/**
	 * Whether any plugin serves documents of the given LSP language (e.g.
	 * {@code python}): i.e. whether {@link #launch} can give a server for it.
	 */
	boolean supports(String languageId);

	/**
	 * Gets a new server for an LSP language (e.g. {@code groovy}, or
	 * {@code python}), from the highest-priority {@link LanguageServerPlugin}
	 * serving it (e.g. in-process, or a proxy to a server process). It sees
	 * documents as plain code of the language, and
	 * analyzes them as running in the given environment. The caller
	 * initializes it, and shuts it down.
	 *
	 * @param environment Where the scripts run.
	 * @throws IllegalArgumentException If no plugin serves the language.
	 */
	LanguageServer launch(String languageId, Environment environment);

	/**
	 * Gets the variables a script's parameters ({@code #@} lines, and old-style
	 * {@code // @} lines in its header) define before it runs, by name, with
	 * their Java types ({@code Object} if unresolvable); in declaration order.
	 * Sessions declare them for their servers (see
	 * {@link ScriptDocument}). Half-typed declarations
	 * are expected, and parsed quietly.
	 */
	Map<String, Class<?>> scriptParameters(String text);
}
