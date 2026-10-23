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

import org.eclipse.lsp4j.Position;

/**
 * Converts between offsets in a text and LSP positions: a line, and a
 * character in that line, counted in UTF-16 code units (as Java does).
 *
 * @author Gabriel Selzer
 */
public final class Positions {

	private Positions() {
		// NB: Prevent instantiation of utility class.
	}

	/** Converts an offset to an LSP position (clamped to the text). */
	public static Position position(final String text, final int offset) {
		final int end = Math.max(0, Math.min(offset, text.length()));
		int line = 0, start = 0;
		for (int i = 0; i < end; i++) {
			if (text.charAt(i) == '\n') {
				line++;
				start = i + 1;
			}
		}
		return new Position(line, end - start);
	}

	/** Converts an LSP position to an offset (clamped to the text). */
	public static int offset(final String text, final Position position) {
		int start = 0;
		for (int line = 0; line < position.getLine(); line++) {
			final int nl = text.indexOf('\n', start);
			if (nl < 0) return text.length();
			start = nl + 1;
		}
		int end = text.indexOf('\n', start);
		if (end < 0) end = text.length();
		return Math.min(start + position.getCharacter(), end);
	}
}
