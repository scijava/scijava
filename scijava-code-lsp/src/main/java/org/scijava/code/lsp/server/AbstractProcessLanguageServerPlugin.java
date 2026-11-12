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

package org.scijava.code.lsp.server;

import org.eclipse.lsp4j.services.LanguageServer;
import org.scijava.Disposable;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerPlugin;
import org.scijava.log.LogService;
import org.scijava.log.Logger;
import org.scijava.log.StderrLogService;
import org.scijava.plugin.Parameter;

/**
 * Creates a {@link LanguageServerPlugin} wrapping an external language server.
 * <p>
 * Implementations only need to specify the {@link ServerLauncher}
 * implementation via {@link #launcher}, but can also overwrite {@link #create}
 * to e.g. augment the plain language server with additional features.
 * </p>
 *
 * @author Gabriel Selzer
 */
public abstract class AbstractProcessLanguageServerPlugin implements
	LanguageServerPlugin, Disposable
{

	@Parameter(required = false)
	private LogService log;

	private ServerLauncher launcher;
	private ServerProcesses servers;

	/**
	 * How to start the server. Called once, when first needed.
	 */
	protected abstract ServerLauncher launcher();

	/**
	 * Serves the language its {@link #launcher()} does; none if there is no
	 * launcher.
	 */
	@Override
	public boolean supports(final String language) {
		final ServerLauncher l;
		try {
			l = launcherOnce();
		}
		catch (final RuntimeException exc) {
			// NB: Asked about every language: lest this break serving the others.
			log().warn("No launcher from " + getClass().getName(), exc);
			return false;
		}
		return l.languageId().equals(language);
	}

	/**
	 * Gets the server process of the given environment, seen as a
	 * {@link LanguageServer} of its own. Requests fail while the process cannot
	 * be started. Shutting the server down closes its documents, but leaves the
	 * process (see {@link #dispose}).
	 */
	@Override
	public LanguageServer create(final String language,
		final Environment environment)
	{
		return servers().server(environment);
	}

	// -- Disposable methods --

	/** Stops the processes; those asked for later start anew. */
	@Override
	public synchronized void dispose() {
		if (servers != null) servers.shutdown();
		servers = null;
	}

	// -- Helper methods --

	private synchronized ServerLauncher launcherOnce() {
		if (launcher == null) launcher = launcher();
		return launcher;
	}

	/** Gets the server processes, creating them on first use. */
	private synchronized ServerProcesses servers() {
		if (servers == null) {
			servers = new ServerProcesses(launcherOnce(), log());
		}
		return servers;
	}

	private Logger log() {
		return log != null ? log : new StderrLogService();
	}
}
