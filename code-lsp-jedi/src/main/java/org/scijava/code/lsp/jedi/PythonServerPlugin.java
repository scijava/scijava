/*-
 * #%L
 * Python code intelligence for SciJava, via jedi-language-server.
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

package org.scijava.code.lsp.jedi;

import org.eclipse.lsp4j.services.LanguageServer;
import org.scijava.Disposable;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerPlugin;
import org.scijava.code.lsp.server.ServerProcesses;
import org.scijava.log.LogService;
import org.scijava.log.StderrLogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Serves {@code python}: jedi-language-server (see {@link JediLauncher}), one
 * process per environment, made to work well in an editor (see
 * {@link PythonLanguageServer}). A server analyzes scripts in exactly the
 * environment it is given. The processes stop when this is disposed.
 *
 * @author Gabriel Selzer
 */
@Plugin(type = LanguageServerPlugin.class, name = "Python")
public class PythonServerPlugin implements LanguageServerPlugin, Disposable {

	/** How many completions get signatures, once a word is begun. */
	private static final int SIGNATURES = 30;

	@Parameter(required = false)
	private LogService log;

	private ServerProcesses jedi;

	@Override
	public boolean supports(final String languageId) {
		return "python".equals(languageId);
	}

	@Override
	public LanguageServer create(final String languageId,
		final Environment environment)
	{
		return new PythonLanguageServer(jedi().server(environment), SIGNATURES);
	}

	// -- Disposable methods --

	@Override
	public synchronized void dispose() {
		if (jedi != null) jedi.shutdown();
		jedi = null;
	}

	// -- Helper methods --

	/** Gets the jedi-language-server processes, creating them on first use. */
	private synchronized ServerProcesses jedi() {
		if (jedi == null) {
			jedi = new ServerProcesses(new JediLauncher(), log != null ? log
				: new StderrLogService());
		}
		return jedi;
	}
}
