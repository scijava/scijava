/*-
 * #%L
 * Common functionality widely used across SciJava modules.
 * %%
 * Copyright (C) 2021 - 2025 SciJava developers.
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

package org.scijava.common3;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link Strings}.
 *
 * @author Curtis Rueden
 */
public class StringsTest {

	@Test
	public void testPadEnd() {
		assertEquals("ab   ", Strings.padEnd("ab", 5));
		assertEquals("ab...", Strings.padEnd("ab", 5, '.'));
		assertEquals("abcdef", Strings.padEnd("abcdef", 3));
		assertEquals("", Strings.padEnd("", 0));
		assertNull(Strings.padEnd(null, 5));
	}

	@Test
	public void testPadStart() {
		assertEquals("   ab", Strings.padStart("ab", 5));
		assertEquals("...ab", Strings.padStart("ab", 5, '.'));
		assertEquals("abcdef", Strings.padStart("abcdef", 3));
		assertNull(Strings.padStart(null, 5));
	}

	@Test
	public void testIsNullOrEmpty() {
		assertTrue(Strings.isNullOrEmpty(null));
		assertTrue(Strings.isNullOrEmpty(""));
		assertFalse(Strings.isNullOrEmpty(" "));
		assertFalse(Strings.isNullOrEmpty("x"));
	}

	@Test
	public void testSplitUnquoted() {
		assertArrayEquals(new String[] { "a", "b", "c" }, //
			Strings.splitUnquoted("a,b,c", ","));
		// NB: separators inside double quotes do not split.
		assertArrayEquals(new String[] { "a", "\"b,c\"" }, //
			Strings.splitUnquoted("a,\"b,c\"", ","));
		// NB: the separator is a literal, not a regular expression.
		assertArrayEquals(new String[] { "a", "b" }, //
			Strings.splitUnquoted("a.b", "."));
		// NB: trailing empty strings are retained.
		assertArrayEquals(new String[] { "a", "", "" }, //
			Strings.splitUnquoted("a,,", ","));
	}
}
