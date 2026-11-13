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

	/**
	 * A Python-like dialect: arrays become NumPy arrays (imported as a private
	 * alias, {@code _np}); strings stay; nothing else is declared, so its header
	 * is empty when there are no such parameters.
	 */
	private static final ScriptDialect PYTHON = new ScriptDialect() {

		@Override
		public boolean supports(final ScriptLanguage language) {
			return false;
		}

		@Override
		public String outputLanguage() {
			return "python";
		}

		@Override
		public Header header(final Map<String, Variable> variables) {
			final List<String> code = new ArrayList<>();
			final boolean numpy = variables.values().stream().anyMatch(v -> v.type()
				.isArray());
			if (numpy) code.add("import numpy as _np");
			variables.forEach((name, v) -> {
				if (v.type().isArray()) code.add(name + ": _np.ndarray");
				else if (v.type() == String.class) code.add(name + ": str");
			});
			return new Header(String.join("; ", code), numpy ? Collections
				.singleton("_np") : Collections.emptySet());
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

	/**
	 * Tests how a script's parameters are declared:
	 * <ul>
	 * <li>By the dialect's header, on a line of its own, with the parameter
	 * lines blanked</li>
	 * <li>With the names the header binds only to declare them (e.g. a module)
	 * hidden, unless the script itself mentions them</li>
	 * </ul>
	 */
	@Test
	public void testDeclarations() {
		// Create a ScriptDocument describing a 2-parameter script.
		final String script = "#@ double[] a\n#@ String s\na.";
		final ScriptDocument doc = ScriptDocument.of(
			script,
			params("a", double[].class, "s", String.class),
			lines(0, 1),
			PYTHON
		);
		// The converted text should be the header...
		final String expectedText = "import numpy as _np; a: _np.ndarray; s: str\n" +
		// ...plus the script where the parameters are blanked
			blanked(script.substring(0, script.lastIndexOf('\n'))) + "\na.";
		assertEquals(expectedText, doc.text());

		// The script doesn't mention "_np", so it should be hidden from completions...
		assertEquals(Collections.singleton("_np"), doc.hiddenNames());
		// ...even if the script imports numpy itself...
		assertEquals(Collections.singleton("_np"), ScriptDocument.of(
			"import numpy as np\n" + script, params("a", double[].class), lines(),
			PYTHON).hiddenNames());
		// ...but if it did mention "_np", it should not be hidden.
		assertTrue(ScriptDocument.of(script + "\n_np = 1", params("a",
			double[].class), lines(), PYTHON).hiddenNames().isEmpty());
		// A name merely starting with "_np" is not a mention.
		assertEquals(Collections.singleton("_np"), ScriptDocument.of(script +
			"\n_npish = 1", params("a", double[].class), lines(), PYTHON)
			.hiddenNames());
	}

	/**
	 * Tests that a dialect declaring nothing adds no header line: the text, and
	 * so every offset, is unchanged.
	 */
	@Test
	public void testEmptyHeader() {
		final String script = "x = 1\nx.";
		final ScriptDocument doc = ScriptDocument.of(script, params(), lines(),
			PYTHON);
		assertEquals(script, doc.text());
		assertEquals(5, doc.toTransformed(5));
		assertEquals(5, doc.toOriginal(5));
		assertEquals(new Position(1, 1), doc.toTransformed(new Position(1, 1)));
		assertEquals(new Position(1, 1), doc.toOriginal(new Position(1, 1)));
	}

	/**
	 * Tests that every character of a removed line is replaced with a space,
	 * but for its line break (including the CR of a CRLF).
	 */
	@Test
	public void testBlanking() {
		final String script = "#@ String s\r\nprintln s\r\n#@output x\n# end";
		final ScriptDocument doc = ScriptDocument.of(script, params("s",
			String.class), lines(0, 2), PYTHON);
		assertEquals("s: str\n" +
			"           \r\nprintln s\r\n          \n# end", doc.text());
		// Blanking keeps every offset, and line breaks (incl. CR) in place.
		assertEquals(doc.text().length() - doc.toTransformed(0), script
			.length());
		assertEquals(doc.text().split("\n", -1).length - 1, script.split("\n",
			-1).length);
	}

	/**
	 * Tests that offsets shift by the header's length, and positions (and
	 * ranges) by its one line; and that those within the header map to
	 * nothing.
	 */
	@Test
	public void testMapping() {
		// NB: With a CRLF, which positions must count as one line break.
		final String script = "#@ String s\r\ns.len";
		final ScriptDocument doc = ScriptDocument.of(script, params("s",
			String.class), lines(0), PYTHON);
		final int header = "s: str\n".length();
		// Offsets: shifted by the header's length...
		assertEquals(header + 3, doc.toTransformed(3));
		assertEquals(3, doc.toOriginal(header + 3));
		// ...and positions by its line.
		assertEquals(new Position(2, 1), doc.toTransformed(new Position(1, 1)));
		assertEquals(new Position(1, 1), doc.toOriginal(new Position(2, 1)));
		assertEquals(new Range(new Position(0, 0), new Position(1, 1)), doc
			.toOriginal(new Range(new Position(1, 0), new Position(2, 1))));
		// The header maps to nothing.
		assertEquals(-1, doc.toOriginal(header - 1));
		assertNull(doc.toOriginal(new Position(0, 3)));
		assertNull(doc.toOriginal(new Range(new Position(0, 0), new Position(1,
			1))));
	}

	/**
	 * Tests that a header with a line break (LF or CR) is rejected, as it would
	 * disrupt translation logic (see {@link #testMapping()})
	 */
	@Test
	public void testOneLineHeader() {
		assertThrows(IllegalArgumentException.class,
			() -> new ScriptDialect.Header("import numpy\nx: int", Collections
				.emptySet()));
		assertThrows(IllegalArgumentException.class,
			() -> new ScriptDialect.Header("x: int\r", Collections.emptySet()));
	}

	/**
	 * Tests that a script without a dialect is plain text, as it is: nothing
	 * declared or blanked, not even its {@code #@} lines.
	 */
	@Test
	public void testPlainText() {
		final String script = "#@ String s\ns.";
		final ScriptDocument doc = ScriptDocument.of(script);
		assertEquals("plaintext", doc.language());
		assertEquals(script, doc.text());
		assertEquals(0, doc.toTransformed(0));
		assertTrue(doc.hiddenNames().isEmpty());
	}

	/** The script, with every character but its line breaks blanked. */
	private static String blanked(final String script) {
		return script.replaceAll("[^\r\n]", " ");
	}
}
