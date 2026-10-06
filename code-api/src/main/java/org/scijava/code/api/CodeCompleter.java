/*-
 * #%L
 * Core API for SciJava code intelligence features.
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

package org.scijava.code.api;

/**
 * Generates code completion suggestions for a fragment of source code.
 * <p>
 * This is the toolkit-agnostic heart of SciJava's code completion: an
 * implementation analyzes a {@link CompletionRequest} (the source text and caret
 * position, optionally a live script engine) and returns a
 * {@link CompletionResult}. It knows nothing about any widget toolkit, so the
 * same completer drives the Swing script editor, an interactive REPL, or any
 * other front end.
 * </p>
 * <p>
 * To contribute completion for a script language, implement
 * {@link CodeCompleterPlugin} (which is discovered automatically) rather than
 * this bare interface.
 * </p>
 *
 * @author Hadrien Mary
 * @author Curtis Rueden
 * @see CodeCompleterPlugin
 * @see CodeCompletionService
 */
@FunctionalInterface
public interface CodeCompleter {

	/**
	 * Computes completion suggestions for the given request.
	 *
	 * @param request the source text, caret position and optional engine context
	 * @return the suggestions, never {@code null} (use {@link CompletionResult#EMPTY})
	 */
	CompletionResult complete(CompletionRequest request);

	/**
	 * Gives the completer a chance to get ready for completing in the given
	 * script, e.g. when it is opened in an editor, so that the first completion
	 * is fast. Implementations must return quickly, doing any slow work in the
	 * background. The default does nothing.
	 *
	 * @param request the script, as for {@link #complete}
	 */
	default void prepare(final CompletionRequest request) {
		// NB: Nothing to prepare.
	}

	/**
	 * Tells the completer that a script is no longer edited (e.g. its editor
	 * was closed), so that it can release what it keeps for completing in it,
	 * e.g. the script's document in a language server. (Changes need no
	 * notice: each request carries the script's whole text.) The default does
	 * nothing.
	 *
	 * @param request the script, as for {@link #prepare}
	 */
	default void closed(final CompletionRequest request) {
		// NB: Nothing to release.
	}

	/**
	 * Gets the signatures of the call around the request's caret (e.g. all
	 * overloads of {@code max} in {@code Math.max(1.5, |)}), rated against the
	 * arguments typed so far, and ordered as the completer sees fit (e.g. best
	 * fit first). The default offers none.
	 * <p>
	 * Like {@link #complete}, this is called as the user types, so it should
	 * answer quickly, announcing slower answers via
	 * {@link SignatureHelp#withUpdate}.
	 * </p>
	 *
	 * @param request the source text and caret, as for {@link #complete}
	 * @return the signatures, never {@code null} (use {@link SignatureHelp#NONE})
	 */
	default SignatureHelp signatureHelp(final CompletionRequest request) {
		return SignatureHelp.NONE;
	}

	/**
	 * Gets how this completer's language turns a SciJava script into plain code
	 * of the language (see {@link ScriptDocument}), or null if it does not. The
	 * default does not.
	 */
	default ScriptDialect dialect() {
		return null;
	}
}
