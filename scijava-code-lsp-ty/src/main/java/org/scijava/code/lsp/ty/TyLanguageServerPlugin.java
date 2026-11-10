/*-
 * #%L
 * Python code intelligence for SciJava, via ty.
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

package org.scijava.code.lsp.ty;

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
 * Serves {@code python}: ty's language server (see {@link TyLauncher}), one
 * process per environment, made to work well in an editor (see
 * {@link TyLanguageServer}). A server analyzes scripts in exactly the
 * environment it is given. The processes stop when this is disposed.
 *
 * @author Gabriel Selzer
 */
@Plugin(type = LanguageServerPlugin.class, name = "Python")
public class TyLanguageServerPlugin implements LanguageServerPlugin,
	Disposable
{

	@Parameter(required = false)
	private LogService log;

	private ServerProcesses ty;

	@Override
	public boolean supports(final String languageId) {
		return "python".equals(languageId);
	}

	@Override
	public LanguageServer create(final String languageId,
		final Environment environment)
	{
		return new TyLanguageServer(ty().server(environment));
	}

	// -- Disposable methods --

	@Override
	public synchronized void dispose() {
		if (ty != null) ty.shutdown();
		ty = null;
	}

	// -- Helper methods --

	/** Gets the ty server processes, creating them on first use. */
	private synchronized ServerProcesses ty() {
		if (ty == null) {
			ty = new ServerProcesses(new TyLauncher(), log != null ? log
				: new StderrLogService());
		}
		return ty;
	}
}
