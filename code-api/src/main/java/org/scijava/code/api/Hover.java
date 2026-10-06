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

import java.util.concurrent.CompletableFuture;

/**
 * What is known about the code at a position, e.g. the documentation of the
 * function named there, as shown when the mouse rests on it. Computed by
 * {@link CodeCompleter#hover}.
 *
 * @author Gabriel Selzer
 */
public final class Hover {

	/** Nothing known. */
	public static final Hover NONE = new Hover(null, -1, -1);

	private final String text;
	private final int start;
	private final int end;
	private final CompletableFuture<Hover> update;

	/**
	 * @param text What is known, as HTML (without {@code <html>}); or null.
	 * @param start Where the code it is about starts (e.g. the name's first
	 *          character), or -1 if unknown.
	 * @param end Where that code ends (exclusive), or -1 if unknown.
	 */
	public Hover(final String text, final int start, final int end) {
		this(text, start, end, null);
	}

	private Hover(final String text, final int start, final int end,
		final CompletableFuture<Hover> update)
	{
		this.text = text;
		this.start = start;
		this.end = end;
		this.update = update;
	}

	/** What is known, as HTML (without {@code <html>}); or null. */
	public String text() {
		return text;
	}

	/** Where the code it is about starts, or -1 if unknown. */
	public int start() {
		return start;
	}

	/** Where the code it is about ends (exclusive), or -1 if unknown. */
	public int end() {
		return end;
	}

	/** Whether nothing is known. */
	public boolean isEmpty() {
		return text == null || text.isEmpty();
	}

	/**
	 * Returns a copy of this, announcing a better answer to come (e.g. from a
	 * slower analysis), as {@link CompletionResult#withUpdate} does for
	 * completions.
	 */
	public Hover withUpdate(final CompletableFuture<Hover> better) {
		return new Hover(text, start, end, better);
	}

	/** The better answer to come, or null if none. */
	public CompletableFuture<Hover> update() {
		return update;
	}
}
