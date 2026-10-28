/*
 * #%L
 * Settings a user can read and edit, in one TOML file.
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

package org.scijava.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests reading and writing the TOML a settings file is made of.
 *
 * @author Curtis Rueden
 */
public class TomlTest {

	/** The shapes a settings file is actually made of. */
	@Test
	public void testTablesAndScalars() {
		final Toml toml = Toml.parse("" + //
			"[sc.fiji.commands.Blur]\n" + //
			"sigma = 2.5\n" + //
			"iterations = 3\n" + //
			"edges = \"Reflect\"\n" + //
			"verbose = true\n");

		assertEquals(List.of("", "sc.fiji.commands.Blur"), toml.tables());
		assertEquals(2.5, toml.get("sc.fiji.commands.Blur", "sigma"));
		assertEquals(3L, toml.get("sc.fiji.commands.Blur", "iterations"));
		assertEquals("Reflect", toml.get("sc.fiji.commands.Blur", "edges"));
		assertEquals(true, toml.get("sc.fiji.commands.Blur", "verbose"));
	}

	/** A script's identifier is a path, so its table name is quoted. */
	@Test
	public void testQuotedTableName() {
		final Toml toml = Toml.parse( //
			"[\"script:/home/curtis/Blur_It.groovy\"]\nsigma = 1.0\n");

		assertEquals(1.0, toml.get("script:/home/curtis/Blur_It.groovy", "sigma"));
	}

	/** Numbers in every spelling TOML allows. */
	@Test
	public void testNumbers() {
		final Toml toml = Toml.parse("" + //
			"a = 1_000_000\n" + "b = 0xff\n" + "c = 0o755\n" + "d = 0b1010\n" + //
			"e = -2.5e3\n" + "f = inf\n" + "g = -inf\n" + "h = nan\n");

		assertEquals(1000000L, toml.get(Toml.ROOT, "a"));
		assertEquals(255L, toml.get(Toml.ROOT, "b"));
		assertEquals(493L, toml.get(Toml.ROOT, "c"));
		assertEquals(10L, toml.get(Toml.ROOT, "d"));
		assertEquals(-2500.0, toml.get(Toml.ROOT, "e"));
		assertEquals(Double.POSITIVE_INFINITY, toml.get(Toml.ROOT, "f"));
		assertEquals(Double.NEGATIVE_INFINITY, toml.get(Toml.ROOT, "g"));
		assertTrue(Double.isNaN((Double) toml.get(Toml.ROOT, "h")));
	}

	/** All four date-and-time types, which a date widget will want. */
	@Test
	public void testMoments() {
		final Toml toml = Toml.parse("" + //
			"date = 2026-09-18\n" + //
			"time = 14:30:00\n" + //
			"stamp = 2026-09-18T14:30:00\n" + //
			"zoned = 2026-09-18T14:30:00+02:00\n");

		assertEquals(LocalDate.of(2026, 9, 18), toml.get(Toml.ROOT, "date"));
		assertEquals(LocalTime.of(14, 30), toml.get(Toml.ROOT, "time"));
		assertEquals(LocalDateTime.of(2026, 9, 18, 14, 30), toml.get(Toml.ROOT,
			"stamp"));
		assertEquals(OffsetDateTime.parse("2026-09-18T14:30:00+02:00"), toml.get(
			Toml.ROOT, "zoned"));
	}

	/** Strings in all four spellings, including what a text area holds. */
	@Test
	public void testStrings() {
		final Toml toml = Toml.parse("" + //
			"basic = \"a\\tb\\nc\"\n" + //
			"literal = 'C:\\Users\\curtis'\n" + //
			"notes = \"\"\"\nline one\nline two\n\"\"\"\n" + //
			"raw = '''\nas written\n'''\n");

		assertEquals("a\tb\nc", toml.get(Toml.ROOT, "basic"));
		assertEquals("C:\\Users\\curtis", toml.get(Toml.ROOT, "literal"));
		assertEquals("line one\nline two\n", toml.get(Toml.ROOT, "notes"));
		assertEquals("as written\n", toml.get(Toml.ROOT, "raw"));
	}

	/** Arrays, which is how a List&lt;File&gt; parameter persists. */
	@Test
	public void testArrays() {
		final Toml toml = Toml.parse("" + //
			"inputs = [\"/data/a.tif\", \"/data/b.tif\"]\n" + //
			"weights = [\n  1.0,\n  2.0, # the second one\n  3.0,\n]\n");

		assertEquals(List.of("/data/a.tif", "/data/b.tif"), toml.get(Toml.ROOT,
			"inputs"));
		assertEquals(List.of(1.0, 2.0, 3.0), toml.get(Toml.ROOT, "weights"));
	}

	/** What a person wrote in the file is still there after a save. */
	@Test
	public void testCommentsSurviveAWrite() {
		final String original = "" + //
			"# Settings for my analysis\n" + //
			"\n" + //
			"[sc.fiji.commands.Blur]\n" + //
			"# 0.7 because the detector is noisy\n" + //
			"sigma = 0.7\n";

		final Toml toml = Toml.parse(original);
		toml.set("sc.fiji.commands.Blur", "sigma", 1.4);
		final String written = toml.write();

		assertTrue(written.contains("# Settings for my analysis"), written);
		assertTrue(written.contains("# 0.7 because the detector is noisy"),
			written);
		assertTrue(written.contains("sigma = 1.4"), written);
	}

	/** Everything written can be read back as what it was. */
	@Test
	public void testRoundTrip() {
		final Toml toml = Toml.empty();
		toml.set("a.b.C", "text", "hello");
		toml.set("a.b.C", "multi", "one\ntwo");
		toml.set("a.b.C", "count", 42L);
		toml.set("a.b.C", "ratio", 0.25);
		toml.set("a.b.C", "on", true);
		toml.set("a.b.C", "when", LocalDate.of(2026, 9, 18));
		toml.set("a.b.C", "files", List.of("/a", "/b"));
		toml.set("with spaces", "key", "value");

		final Toml again = Toml.parse(toml.write());

		assertEquals("hello", again.get("a.b.C", "text"));
		assertEquals("one\ntwo", again.get("a.b.C", "multi"));
		assertEquals(42L, again.get("a.b.C", "count"));
		assertEquals(0.25, again.get("a.b.C", "ratio"));
		assertEquals(true, again.get("a.b.C", "on"));
		assertEquals(LocalDate.of(2026, 9, 18), again.get("a.b.C", "when"));
		assertEquals(List.of("/a", "/b"), again.get("a.b.C", "files"));
		assertEquals("value", again.get("with spaces", "key"));
	}

	/** What is not supported says so, with a line number to look at. */
	@Test
	public void testUnsupportedConstructs() {
		final TomlException inline = assertThrows(TomlException.class, //
			() -> Toml.parse("[t]\nx = 1\npoint = { a = 1, b = 2 }\n"));
		assertEquals(3, inline.line());
		assertTrue(inline.getMessage().contains("inline tables"), inline
			.getMessage());

		final TomlException arrayOfTables = assertThrows(TomlException.class, //
			() -> Toml.parse("[[products]]\nname = \"x\"\n"));
		assertTrue(arrayOfTables.getMessage().contains("arrays of tables"),
			arrayOfTables.getMessage());
	}

	/** A malformed file names the line rather than guessing. */
	@Test
	public void testMalformed() {
		final TomlException exc = assertThrows(TomlException.class, //
			() -> Toml.parse("[t]\ngood = 1\nbad value\n"));
		assertEquals(3, exc.line());
	}

	/** A value TOML cannot hold is refused when it is set, not when written. */
	@Test
	public void testUnrepresentableValue() {
		final Toml toml = Toml.empty();
		assertThrows(IllegalArgumentException.class, //
			() -> toml.set("t", "k", new Object()));
	}
}
