/*-
 * #%L
 * Jython code intelligence for SciJava, via Jython's own parser.
 * %%
 * Copyright (C) 2020 - 2025 SciJava developers.
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
package org.scijava.code.lsp.jython;

import java.io.File;

import org.eclipse.lsp4j.services.LanguageServer;
import org.scijava.Priority;
import org.scijava.code.lsp.LanguageServerService;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Offers an in-process Jython {@link LanguageServer}
 *
 * @author Gabriel Selzer
 * @see JythonLanguageServer
 */
@Plugin(type = LanguageServerPlugin.class, name = "Jython",
	priority = Priority.HIGH)
public class JythonLanguageServerPlugin implements LanguageServerPlugin {

	@Parameter(required = false)
	private LanguageServerService servers;

	private JavaStubs stubs;

	@Override
	public boolean supports(final String languageId) {
		// NB: Not "python", which this builds on (see PythonCompanion).
		return "jython".equals(languageId);
	}

	@Override
	public LanguageServer create(final String languageId,
		final Environment environment)
	{
		return new JythonLanguageServer( //
			environment, //
			PythonCompanion.launch(servers, stubs()) //
		);
	}

	/** Sets the stubs, e.g. to write them elsewhere in tests. */
	synchronized void setStubs(final JavaStubs stubs) {
		this.stubs = stubs;
	}

	/** Gets the stubs of Java packages, creating them on first use. */
	synchronized JavaStubs stubs() {
		if (stubs != null) return stubs;
		final String dir = System.getProperty("scijava.jython.stubs");
		stubs = new JavaStubs(dir != null ? new File(dir) : new File(System
			.getProperty("user.home"), ".cache" + File.separator + "scijava" +
				File.separator + "jython-stubs"), Thread.currentThread()
					.getContextClassLoader());
		return stubs;
	}

}
