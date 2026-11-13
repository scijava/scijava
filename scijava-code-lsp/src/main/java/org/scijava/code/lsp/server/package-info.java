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
 * Tooling for managing inter-process language servers.
 * <p>
 * {@link org.scijava.code.lsp.server.ServerLauncher} implementations describe
 * how a language server can be started up in a new process.
 * </p>
 * <p>
 * {@link org.scijava.code.lsp.server.AbstractProcessLanguageServerPlugin}
 * implementations wrap processes started with a
 * {@link org.scijava.code.lsp.server.ServerLauncher} into a
 * {@link org.eclipse.lsp4j.services.LanguageServer}. Override
 * {@link org.scijava.code.lsp.server.AbstractProcessLanguageServerPlugin#create}
 * to write a thin wrapper over the existing language server for fine-tuning.
 * </p>
 * <p>
 * For example, for a server run as {@code my-language-server --stdio}:
 * </p>
 * <pre>
 * class MyLauncher implements ServerLauncher {
 *
 *   &#64;Override
 *   public String language() {
 *     return "mylang";
 *   }
 *
 *   &#64;Override
 *   public Process start(Environment env) throws IOException {
 *     return new ProcessBuilder("my-language-server", "--stdio")
 *       // NB: Lest a full pipe block the server.
 *       .redirectError(ProcessBuilder.Redirect.DISCARD).start();
 *   }
 * }
 *
 * &#64;Plugin(type = LanguageServerPlugin.class)
 * public class MyServerPlugin extends AbstractProcessLanguageServerPlugin {
 *
 *   &#64;Override
 *   protected ServerLauncher launcher() {
 *     return new MyLauncher();
 *   }
 * }
 * </pre>
 */
package org.scijava.code.lsp.server;
