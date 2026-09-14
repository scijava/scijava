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

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.scijava.script.ScriptLanguage;

/**
 * A request for code completion at a particular point in a script.
 * <p>
 * This bundles the full source text and the caret offset, plus optional handles
 * to a live {@link ScriptEngine}/{@link ScriptContext} (present when completing
 * in an interactive interpreter, where variable bindings can be inspected). It
 * replaces the old {@code (code, startIndex, engine)} argument triple with a
 * single self-describing object, and provides convenience accessors that most
 * completers need.
 * </p>
 *
 * @author Curtis Rueden
 * @see CodeCompleter
 */
public final class CompletionRequest {

	private final String text;
	private final int offset;
	private final ScriptLanguage language;
	private final ScriptEngine engine;
	private final ScriptContext context;

	public CompletionRequest(final String text, final int offset,
		final ScriptLanguage language, final ScriptEngine engine,
		final ScriptContext context)
	{
		if (offset < 0 || offset > text.length()) {
			throw new IndexOutOfBoundsException("offset " + offset + " out of [0, " +
				text.length() + "]");
		}
		this.text = text;
		this.offset = offset;
		this.language = language;
		this.engine = engine;
		this.context = context;
	}

	/** Creates a request over the whole text with the caret at its end. */
	public CompletionRequest(final String text, final ScriptLanguage language,
		final ScriptEngine engine)
	{
		this(text, text.length(), language, engine, //
			engine == null ? null : engine.getContext());
	}

	/** The full source text being edited. */
	public String text() {
		return text;
	}

	/** The caret offset within {@link #text()}. */
	public int offset() {
		return offset;
	}

	/** The script language being completed, or {@code null} if unknown. */
	public ScriptLanguage language() {
		return language;
	}

	/** A live script engine, or {@code null} when completing statically. */
	public ScriptEngine engine() {
		return engine;
	}

	/** A live script context, or {@code null}. */
	public ScriptContext context() {
		return context;
	}

	/** The source text from the start of the document up to the caret. */
	public String textToCaret() {
		return text.substring(0, offset);
	}

	/** The current line up to the caret (no trailing newline). */
	public String lineToCaret() {
		final int nl = text.lastIndexOf('\n', offset - 1);
		return text.substring(nl + 1, offset);
	}
}
