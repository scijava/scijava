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

import java.io.InputStream;
import java.io.PrintStream;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.scijava.Context;

/**
 * Runs the {@link SciJavaLanguageServer} over standard input and output, for
 * an editor to start, e.g. with Fiji's classpath:
 *
 * <pre>
 * java -cp "Fiji/jars/*;Fiji/plugins/*" org.scijava.code.lsp.server.Main
 * </pre>
 *
 * @author Gabriel Selzer
 */
public final class Main {

	private Main() {
		// NB: Prevent instantiation.
	}

	public static void main(final String[] args) throws Exception {
		// NB: Standard output is the protocol's: whatever else would be printed
		// there (e.g. SciJava's log) goes to standard error.
		final InputStream in = System.in;
		final PrintStream out = System.out;
		System.setOut(System.err);

		final Context context = new Context();
		final SciJavaLanguageServer server = new SciJavaLanguageServer(context);
		final Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(
			server, in, out);
		server.connect(launcher.getRemoteProxy());
		server.setOnExit(() -> {
			context.dispose();
			System.exit(0);
		});
		// Serve until the editor closes the connection.
		launcher.startListening().get();
		context.dispose();
		System.exit(0);
	}
}
