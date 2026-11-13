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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.lsp4j.Position;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link Positions}.
 *
 * @author Gabriel Selzer
 */
public class PositionsTest {

	@Test
	public void testOffset() {
		assertEquals(4, Positions.offset("ab\ncd", new Position(1, 1)));
		// Clamped to the line, and the text.
		assertEquals(2, Positions.offset("ab\ncd", new Position(0, 9)));
		assertEquals(5, Positions.offset("ab\ncd", new Position(7, 0)));
	}

	@Test
	public void testPosition() {
		assertEquals(new Position(0, 0), Positions.position("ab\ncd", 0));
		assertEquals(new Position(1, 1), Positions.position("ab\ncd", 4));
		// UTF-16 code units, as Java counts.
		assertEquals(new Position(1, 3), Positions.position("a\n😀b", 5));
		// Clamped to the text.
		assertEquals(new Position(1, 2), Positions.position("ab\ncd", 99));
		assertEquals(new Position(0, 0), Positions.position("ab\ncd", -1));
	}

	@Test
	public void testCrlf() {
		// Lines after a \r\n start where they should, both ways.
		final String text = "ab\r\ncd";
		assertEquals(5, Positions.offset(text, new Position(1, 1)));
		assertEquals(new Position(1, 1), Positions.position(text, 5));
	}

	@Test
	public void testCallStart() {
		// The call around the caret: its opening parenthesis.
		assertEquals(1, Positions.callStart("f(a, ", 5));
		// Closed calls within it are skipped.
		assertEquals(1, Positions.callStart("f(g(x), ", 8));
		// Within a list or block, not a call.
		assertEquals(-1, Positions.callStart("[1, ", 4));
		assertEquals(-1, Positions.callStart("{", 1));
		// Only on the caret's line.
		assertEquals(-1, Positions.callStart("f(\n", 3));
		// Not once the call is closed.
		assertEquals(-1, Positions.callStart("f(x)", 4));
	}
}
