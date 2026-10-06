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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * The streams over which a language server speaks the Language Server
 * Protocol: usually a server process's standard output and input.
 *
 * @author Gabriel Selzer
 */
public interface LspConnection {

	/** The server's messages. */
	InputStream input();

	/** Where to send the server messages. */
	OutputStream output();

	/** Whether the server still runs. */
	boolean isAlive();

	/** Ends the connection, stopping the server if it still runs. */
	void close();

	/**
	 * A connection to a server process, via its standard input and output.
	 * (Its standard error must be redirected or consumed by whoever started
	 * it, lest the process block once the pipe fills.)
	 */
	static LspConnection of(final Process process) {
		return new LspConnection() {

			@Override
			public InputStream input() {
				return process.getInputStream();
			}

			@Override
			public OutputStream output() {
				return process.getOutputStream();
			}

			@Override
			public boolean isAlive() {
				return process.isAlive();
			}

			@Override
			public void close() {
				process.destroy();
			}
		};
	}
}
