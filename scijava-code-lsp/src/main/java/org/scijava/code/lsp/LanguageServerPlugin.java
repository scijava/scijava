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

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.services.LanguageServer;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.script.ScriptLanguage;

/**
 * Offers language servers for scripts of some languages: in-process
 * implementations of lsp4j's {@link LanguageServer}, or proxies to server
 * processes (see {@link LanguageServerService#launch}).
 * <p>
 * Several plugins may serve one language (e.g. an instant in-process analysis
 * and a slower, smarter server process); {@link LanguageServerService} merges
 * their answers, in the plugins' priority order.
 * </p>
 *
 * @author Gabriel Selzer
 */
public interface LanguageServerPlugin extends SciJavaPlugin {

	/** Whether this serves scripts of the given language. */
	boolean supports(ScriptLanguage language);

	/**
	 * Gets where the given script will run, as far as this plugin's servers need
	 * to know: read from its text (e.g. inline metadata) and its URI (e.g. a
	 * file path). Servers are kept one per environment. Called whenever the
	 * script changes, so it must be quick. The default: no particular
	 * environment (one server for all scripts).
	 */
	default Environment environment(final TextDocumentItem document) {
		return Environment.NONE;
	}

	/**
	 * Creates a server for scripts running in the given environment. The
	 * service initializes it.
	 */
	LanguageServer create(Environment environment);
}
