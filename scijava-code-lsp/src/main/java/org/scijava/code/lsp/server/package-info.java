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
 * A toolkit for offering language servers that run as processes, e.g. an
 * existing server for some language, speaking LSP over its standard input and
 * output.
 * <p>
 * {@link org.scijava.code.lsp.server.ServerProcesses} keeps the server
 * processes of one kind: one per environment scripts run in, started when
 * first needed (in the background), restarted if they stop, and stopped when
 * unused for a while. Each is seen as a
 * {@link org.eclipse.lsp4j.services.LanguageServer} of its own.
 * </p>
 * <h2>Offering a server of your own</h2>
 * <ol>
 * <li>Implement {@link org.scijava.code.lsp.server.ServerLauncher}: the
 * language it serves, and how to start its process for an environment, e.g.
 * against that environment's interpreter and extra search paths. Optionally,
 * also the server's initialization options and workspace root.</li>
 * <li>Offer it as a {@link org.scijava.code.lsp.LanguageServerPlugin}, which
 * keeps one {@code ServerProcesses} of it, gets each environment's server from
 * it, and shuts it down when disposed (stopping the processes).</li>
 * <li>For scripts of a SciJava script language to be served by it, a
 * {@link org.scijava.code.lsp.ScriptDialect} must make them code of that
 * language.</li>
 * </ol>
 * <p>
 * For example, for a server run as {@code my-language-server --stdio}:
 * </p>
 * <pre>
 * class MyLauncher implements ServerLauncher {
 *
 *   &#64;Override
 *   public String languageId() {
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
 * public class MyServerPlugin implements LanguageServerPlugin, Disposable {
 *
 *   &#64;Parameter
 *   private LogService log;
 *
 *   private ServerProcesses servers;
 *
 *   &#64;Override
 *   public boolean supports(String languageId) {
 *     return "mylang".equals(languageId);
 *   }
 *
 *   &#64;Override
 *   public synchronized LanguageServer create(String languageId,
 *     Environment environment)
 *   {
 *     if (servers == null) servers = new ServerProcesses(new MyLauncher(), log);
 *     return servers.server(environment);
 *   }
 *
 *   &#64;Override
 *   public synchronized void dispose() {
 *     if (servers != null) servers.shutdown();
 *   }
 * }
 * </pre>
 * <p>
 * scijava-code-lsp-jedi's {@code JediLauncher} and {@code PythonServerPlugin}
 * do this for jedi-language-server, building the environment it runs in the
 * first time it is needed. (Servers written in Java can run in this JVM
 * instead: see {@link org.scijava.code.lsp.jvm.AbstractLanguageServer}.)
 * </p>
 */
package org.scijava.code.lsp.server;
