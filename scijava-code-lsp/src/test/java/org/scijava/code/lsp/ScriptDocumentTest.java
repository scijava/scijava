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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;
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
		public String runtimeType(final Class<?> type) {
			if (type.isArray()) return "numpy.ndarray";
			if (type == String.class) return "str";
			return null;
		}

		@Override
		public String declare(final String name, final String type) {
			return name + ": " + type;
		}

		@Override
		public List<String> imports(final Collection<String> types) {
			final TreeSet<String> modules = new TreeSet<>();
			for (final String t : types) {
				final int dot = t.lastIndexOf('.');
				if (dot > 0) modules.add("import " + t.substring(0, dot));
			}
			return Arrays.asList(modules.toArray(new String[0]));
		}

		@Override
		public String imported(final String statement) {
			return statement.substring("import ".length());
		}

		@Override
		public List<String> extraDeclarations() {
			return Collections.singletonList("task: object");
		}
	};

	/** A Groovy-like dialect: Java types; {@code #} lines are not Groovy. */
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
		public String runtimeType(final Class<?> type) {
			return type.getName();
		}

		@Override
		public String declare(final String name, final String type) {
			return type + " " + name;
		}

		@Override
		public boolean blanks(final String line) {
			return line.startsWith("#");
		}
	};

	private static Map<String, Class<?>> params(final Object... namesAndTypes) {
		final Map<String, Class<?>> params = new LinkedHashMap<>();
		for (int i = 0; i < namesAndTypes.length; i += 2) {
			params.put((String) namesAndTypes[i], (Class<?>) namesAndTypes[i + 1]);
		}
		return params;
	}

	@Test
	public void testDeclarations() {
		final String script = "#@ double[] a\n#@ String s\n#@ Integer n\na.";
		final ScriptDocument doc = ScriptDocument.of(script, params("a",
			double[].class, "s", String.class, "n", Integer.class), PYTHON);
		assertEquals("import numpy; a: numpy.ndarray; s: str; task: object\n" +
			script, doc.text());
		assertEquals(script, doc.original());
		// Parameters without a run-time type are kept, but not declared.
		assertEquals(Arrays.asList("a", "s", "n"), Arrays.asList(doc.parameters()
			.keySet().toArray()));
		assertNull(doc.parameters().get("n"));
		assertEquals("numpy.ndarray", doc.parameters().get("a"));
		// The module is the declarations' alone, unless the script mentions it.
		assertEquals(Collections.singleton("numpy"), doc.added());
		assertTrue(ScriptDocument.of("import numpy as np\n" + script, params("a",
			double[].class), PYTHON).added().isEmpty());
		assertTrue(ScriptDocument.of(script + "\nnumpyish = 1", params("a",
			double[].class), PYTHON).added().contains("numpy"));
	}

	@Test
	public void testIdentity() {
		final String script = "x = 1\nx.";
		final ScriptDocument doc = ScriptDocument.of(script, params(), GROOVY);
		assertEquals(script, doc.text());
		assertEquals(5, doc.toTransformed(5));
		assertEquals(5, doc.toOriginal(5));
		assertEquals(new Position(1, 1), doc.toTransformed(new Position(1, 1)));
	}

	@Test
	public void testBlanking() {
		final String script = "#@ String s\r\nprintln s\r\n#@output x\n# end";
		final ScriptDocument doc = ScriptDocument.of(script, params("s",
			String.class), GROOVY);
		assertEquals("java.lang.String s\n" +
			"           \r\nprintln s\r\n          \n     ", doc.text());
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
			String.class), PYTHON);
		for (int i = 0; i <= script.length(); i++) {
			final int t = doc.toTransformed(i);
			assertEquals(i, doc.toOriginal(t));
			// The text after a position is the same in both.
			assertEquals(script.substring(i), doc.text().substring(t));
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
	public void testAsIs() {
		final String script = "#@ String s\ns.";
		final ScriptDocument doc = ScriptDocument.of(script);
		assertEquals(script, doc.text());
		assertEquals(script, doc.original());
		assertEquals(0, doc.toTransformed(0));
		assertTrue(doc.parameters().isEmpty());
	}
}
