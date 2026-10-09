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

/**
 * Code intelligence for SciJava scripts (completions, signature help, hover
 * and diagnostics), in the terms of the Language Server Protocol, via lsp4j.
 * <p>
 * This package brokers between scripts and the language servers that know
 * about them. {@link org.scijava.code.lsp.LanguageServerPlugin}s offer lsp4j
 * {@link org.eclipse.lsp4j.services.LanguageServer}s for script languages.
 * Clients (e.g. an editor) ask about a script through a
 * {@link org.scijava.code.lsp.ScriptSession} from the
 * {@link org.scijava.code.lsp.LanguageServerService}, in terms of the
 * script's text and offsets into it (see
 * {@link org.scijava.code.lsp.Positions}); behind it is a server of its own,
 * from the highest-priority plugin serving the script's language.
 * </p>
 * <p>
 * Servers need know nothing of SciJava scripts. A SciJava script's {@code #@}
 * parameters define variables before it runs, which plain code of its
 * language would declare. A {@link org.scijava.code.lsp.ScriptDocument} is
 * the script as such code, with its parameters declared with their run-time
 * types, as a {@link org.scijava.code.lsp.ScriptDialect} says. A session shows
 * its server this text (in the dialect of the plugin offering the server),
 * and maps positions in the server's answers back to the script.
 * </p>
 * <p>
 * The subpackages are for those writing language servers:
 * {@link org.scijava.code.lsp.jvm} to write them in Java, and
 * {@link org.scijava.code.lsp.launch} to run them as processes.
 * </p>
 */
package org.scijava.code.lsp;
