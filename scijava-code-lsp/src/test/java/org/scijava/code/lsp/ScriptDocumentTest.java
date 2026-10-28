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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;
import org.scijava.code.lsp.ScriptDialect.Variable;
import org.scijava.script.ScriptLanguage;

/**
 * Tests {@link ScriptDocument}.
 *
 * @author Gabriel Selzer
 */
public class ScriptDocumentTest {

	/** A Python-like dialect: arrays become NumPy arrays; strings stay. */
	private static final ScriptDialect PYTHON = new ScriptDialect() {

		@Override
		public boolean supports(final ScriptLanguage language) {
			return false;
		}

		@Override
		public String languageId() {
			return "python";
		}

		@Override
		public Header header(final Map<String, Variable> variables) {
			final List<String> code = new ArrayList<>();
			final boolean numpy = variables.values().stream().anyMatch(v -> v.type()
				.isArray());
			if (numpy) code.add("import numpy");
			variables.forEach((name, v) -> {
				if (v.type().isArray()) code.add(name + ": numpy.ndarray");
				else if (v.type() == String.class) code.add(name + ": str");
			});
			code.add("task: object");
			return new Header(String.join("; ", code), numpy ? Collections
				.singleton("numpy") : Collections.emptySet());
		}
	};

	/** A Groovy-like dialect: Java types. */
	private static final ScriptDialect GROOVY = new ScriptDialect() {

		@Override
		public boolean supports(final ScriptLanguage language) {
			return false;
		}

		@Override
		public String languageId() {
			return "groovy";
		}

		@Override
		public Header header(final Map<String, Variable> variables) {
			final List<String> code = new ArrayList<>();
			variables.forEach((name, v) -> code.add(v.type().getName() + " " +
				name));
			return new Header(String.join("; ", code), Collections.emptySet());
		}
	};

	private static Map<String, Variable> params(final Object... namesAndTypes) {
		final Map<String, Variable> params = new LinkedHashMap<>();
		for (int i = 0; i < namesAndTypes.length; i += 2) {
			params.put((String) namesAndTypes[i], Variable.ofType(
				(Class<?>) namesAndTypes[i + 1]));
		}
		return params;
	}

	private static Set<Integer> lines(final Integer... lines) {
		return new HashSet<>(Arrays.asList(lines));
	}

	@Test
	public void testDeclarations() {
		final String script = "#@ double[] a\n#@ String s\n#@ Integer n\na.";
		final ScriptDocument doc = ScriptDocument.of(script, params("a",
			double[].class, "s", String.class, "n", Integer.class), lines(0, 1,
				2), PYTHON);
		// The dialect's header, on a line of its own; the parameter lines
		// blanked.
		assertEquals("import numpy; a: numpy.ndarray; s: str; task: object\n" +
			blanked(script.substring(0, script.lastIndexOf('\n'))) + "\na.", doc
				.text());
		// The module is the header's alone, unless the script mentions it.
		assertEquals(Collections.singleton("numpy"), doc.hiddenNames());
		assertTrue(ScriptDocument.of("import numpy as np\n" + script, params("a",
			double[].class), lines(), PYTHON).hiddenNames().isEmpty());
		assertTrue(ScriptDocument.of(script + "\nnumpyish = 1", params("a",
			double[].class), lines(), PYTHON).hiddenNames().contains("numpy"));
	}

	@Test
	public void testIdentity() {
		final String script = "x = 1\nx.";
		final ScriptDocument doc = ScriptDocument.of(script, params(), lines(),
			GROOVY);
		assertEquals(script, doc.text());
		assertEquals(5, doc.toTransformed(5));
		assertEquals(5, doc.toOriginal(5));
		assertEquals(new Position(1, 1), doc.toTransformed(new Position(1, 1)));
	}

	@Test
	public void testBlanking() {
		final String script = "#@ String s\r\nprintln s\r\n#@output x\n# end";
		final ScriptDocument doc = ScriptDocument.of(script, params("s",
			String.class), lines(0, 2), GROOVY);
		assertEquals("java.lang.String s\n" +
			"           \r\nprintln s\r\n          \n# end", doc.text());
		// Blanking keeps every offset, and line breaks (incl. CR) in place.
		assertEquals(doc.text().length() - doc.toTransformed(0), script
			.length());
		assertEquals(doc.text().split("\n", -1).length - 1, script.split("\n",
			-1).length);
	}

	@Test
	public void testRoundTrips() {
		// NB: Includes a CRLF and a surrogate pair (two Java chars).
		final String script = "#@ String s\r\nt = \"😀\"\r\ns.len";
		final ScriptDocument doc = ScriptDocument.of(script, params("s",
			String.class), lines(0), PYTHON);
		for (int i = 0; i <= script.length(); i++) {
			final int t = doc.toTransformed(i);
			assertEquals(i, doc.toOriginal(t));
			// The text after a position is the same in both, but for the
			// blanked parameter line.
			final String after = doc.text().substring(t);
			assertEquals(script.length() - i, after.length());
			if (i >= script.indexOf('\r')) {
				assertEquals(script.substring(i), after);
			}
		}
		for (int line = 0; line < 3; line++) {
			final Position p = new Position(line, 2);
			final Position t = doc.toTransformed(p);
			assertEquals(new Position(line + 1, 2), t);
			assertEquals(p, doc.toOriginal(t));
		}
		// The header maps to nothing.
		assertEquals(-1, doc.toOriginal(0));
		assertEquals(-1, doc.toOriginal(doc.toTransformed(0) - 1));
		assertNull(doc.toOriginal(new Position(0, 0)));
		assertNull(doc.toOriginal(new Range(new Position(0, 0), new Position(1,
			1))));
		assertEquals(new Range(new Position(0, 0), new Position(1, 1)), doc
			.toOriginal(new Range(new Position(1, 0), new Position(2, 1))));
	}

	@Test
	public void testOneLineHeader() {
		// A header is one line: positions shift by just that line.
		assertThrows(IllegalArgumentException.class,
			() -> new ScriptDialect.Header("import numpy\nx: int", Collections
				.emptySet()));
		assertThrows(IllegalArgumentException.class,
			() -> new ScriptDialect.Header("x: int\r", Collections.emptySet()));
	}

	@Test
	public void testAsIs() {
		final String script = "#@ String s\ns.";
		final ScriptDocument doc = ScriptDocument.of(script);
		assertEquals(script, doc.text());
		assertEquals(0, doc.toTransformed(0));
		assertTrue(doc.hiddenNames().isEmpty());
	}

	/** The script, with every character but its line breaks blanked. */
	private static String blanked(final String script) {
		return script.replaceAll("[^\r\n]", " ");
	}
}
