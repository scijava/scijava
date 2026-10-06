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

import java.io.IOException;

import org.scijava.code.api.EnvironmentContext;

/**
 * How to start a language server for scripts that run in a given environment,
 * e.g. jedi-language-server, told to use the environment's Python.
 *
 * @author Gabriel Selzer
 * @see LspServers
 */
public interface LspServerSpec {

	/** The LSP language identifier of the documents, e.g. {@code python}. */
	String languageId();

	/**
	 * Starts a server. Called on a background thread; may be slow (e.g. build
	 * the environment the server runs in).
	 *
	 * @param env Where the scripts the server will analyze run.
	 */
	LspConnection start(EnvironmentContext env) throws IOException;

	/**
	 * The {@code initializationOptions} to send the server, e.g. the
	 * interpreter and extra paths of {@code env}; or null for none. Must
	 * serialize to JSON (e.g. maps, lists, strings, numbers, booleans).
	 */
	default Object initializationOptions(final EnvironmentContext env) {
		return null;
	}

	/**
	 * The URI of the workspace root, or null for none. (Some servers, e.g.
	 * jedi-language-server, apply their initialization options only with a
	 * root.)
	 */
	default String rootUri(final EnvironmentContext env) {
		return null;
	}
}
