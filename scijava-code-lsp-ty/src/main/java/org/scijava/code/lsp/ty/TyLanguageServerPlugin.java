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
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerPlugin;
import org.scijava.code.lsp.server.AbstractProcessLanguageServerPlugin;
import org.scijava.code.lsp.server.ServerLauncher;
import org.scijava.plugin.Plugin;

/**
 * A {@code ty}-based {@link LanguageServerPlugin}.
 *
 * @author Gabriel Selzer
 */
@Plugin(type = LanguageServerPlugin.class, name = "Python")
public class TyLanguageServerPlugin extends
	AbstractProcessLanguageServerPlugin
{

	@Override
	protected ServerLauncher launcher() {
		return new TyLauncher();
	}

	@Override
	public LanguageServer create(final String languageId,
		final Environment environment)
	{
		// Wrap a plain Ty server with additional features
		return new TyLanguageServer(super.create(languageId, environment));
	}
}
