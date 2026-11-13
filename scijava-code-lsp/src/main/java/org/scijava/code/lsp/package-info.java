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
 * SciJava scripts are a major use case for this library; however, the
 * {@link org.scijava.code.lsp.LanguageServerService} is written to offer
 * {@link org.eclipse.lsp4j.services.LanguageServer}s more generally.
 * {@link org.scijava.code.lsp.ScriptDialect}s convert SciJava scripts to plain
 * code that language servers can analyze, and clients ask about a script
 * through a {@link org.scijava.code.lsp.ScriptSession}.
 * </p>
 * <p>
 * Some subpackages are useful for writing language servers:
 * {@link org.scijava.code.lsp.server} (servers that run as processes) and
 * {@link org.scijava.code.lsp.jvm} (servers written in Java).
 * </p>
 */
package org.scijava.code.lsp;
