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
 * Maps positions between a script and its {@link ScriptDocument transformed
 * text}. The transformation only prepends a header and blanks lines in place
 * (replacing their characters with spaces), so positions in the script just
 * shift by the header: by its length in characters, and by its line count in
 * lines; columns are unchanged.
 * <p>
 * Offsets and columns count {@code char}s, i.e. UTF-16 code units, as do
 * Java strings and the Language Server Protocol.
 * </p>
 *
 * @author Gabriel Selzer
 */
public final class PositionMap {

	/** The identity: nothing prepended. */
	public static final PositionMap IDENTITY = new PositionMap(0, 0);

	private final int headerLength;
	private final int headerLines;

	/**
	 * @param headerLength Number of characters prepended.
	 * @param headerLines Number of lines prepended (each ending in a newline).
	 */
	public PositionMap(final int headerLength, final int headerLines) {
		this.headerLength = headerLength;
		this.headerLines = headerLines;
	}

	/** Maps an offset in the script to one in the transformed text. */
	public int toTransformed(final int offset) {
		return offset + headerLength;
	}

	/**
	 * Maps an offset in the transformed text to one in the script, or returns
	 * -1 if it lies in the prepended header.
	 */
	public int toOriginal(final int offset) {
		return offset < headerLength ? -1 : offset - headerLength;
	}

	/** Maps a (0-based) line in the script to one in the transformed text. */
	public int lineToTransformed(final int line) {
		return line + headerLines;
	}

	/**
	 * Maps a (0-based) line in the transformed text to one in the script, or
	 * returns -1 if it lies in the prepended header.
	 */
	public int lineToOriginal(final int line) {
		return line < headerLines ? -1 : line - headerLines;
	}

	/** Number of characters prepended. */
	public int headerLength() {
		return headerLength;
	}

	/** Number of lines prepended. */
	public int headerLines() {
		return headerLines;
	}
}
