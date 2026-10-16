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

import org.eclipse.lsp4j.services.LanguageServer;
import org.scijava.plugin.SciJavaPlugin;

/**
 * A {@link SciJavaPlugin} that can create {@link LanguageServer}s.
 * <p>
 * Servers need not know SciJava scripts: given the plugin's
 * {@link #dialect() dialect}, sessions show them each script as plain code of
 * its language, its parameters declared (see
 * {@link ScriptDocument}).
 * </p>
 *
 * @author Gabriel Selzer
 */
public interface LanguageServerPlugin extends SciJavaPlugin {

	/**
	 * Declares whether {@link LanguageServer}s can be created to satisfy
	 * scripts of the given LSP language (e.g. {@code groovy}), as
	 * {@link LanguageServerService#launch} asks; and so scripts of the SciJava
	 * language of that name, in lower case (e.g. {@code Groovy}), as sessions
	 * ask (see {@link LanguageServerService#session}). NB: Not one whose
	 * servers this launches itself (e.g. a plugin building on {@code python}
	 * servers serves a language of its own).
	 */
	boolean supports(String languageId);

	/**
	 * Creates a server for documents of an LSP language this serves: for a
	 * session (see {@link LanguageServerService#session}), which initializes
	 * it, and shuts it down when closed; or for whoever
	 * {@link LanguageServerService#launch launches} it.
	 * <p>
	 * NB: A plugin may know better where its scripts run (e.g. as their text
	 * says, which may change as they are edited), and analyze them there,
	 * whatever {@code environment} says.
	 * </p>
	 *
	 * @param languageId The LSP language asked for, one this
	 *          {@link #supports(String) serves} (e.g. {@code groovy}).
	 * @param environment Where the scripts run, as far as known: e.g., for
	 *          code typed into a running interpreter, its state (see
	 *          {@link Environment#live()}).
	 */
	LanguageServer create(String languageId, Environment environment);

	/**
	 * How this plugin's servers see scripts: as plain code of their language,
	 * written in this dialect; or, if null (the default), as written.
	 */
	default ScriptDialect dialect() {
		return null;
	}
}
