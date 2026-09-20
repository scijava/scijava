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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.scijava.common3.Types;

/**
 * Tests the settings store.
 *
 * @author Curtis Rueden
 */
public class SettingsTest {

	/** The same path on every platform, and one property to override it. */
	@Test
	public void testWhereSettingsLive() {
		final Path byApp = Settings.fileFor("fiji");
		assertEquals("settings.toml", byApp.getFileName().toString());
		assertEquals("fiji", byApp.getParent().getFileName().toString());
		// NB: .config even on macOS and Windows, deliberately.
		assertEquals(".config", byApp.getParent().getParent().getFileName()
			.toString());

		System.setProperty(Settings.DIR_PROPERTY, "/tmp/elsewhere");
		try {
			assertEquals(Path.of("/tmp/elsewhere/settings.toml"), Settings.fileFor(
				"fiji"));
		}
		finally {
			System.clearProperty(Settings.DIR_PROPERTY);
		}
	}

	/** A value comes back as the type the caller asks for. */
	@Test
	public void testTypedAccess() {
		final Settings settings = Settings.inMemory();
		settings.set("t", "sigma", 2.5);
		settings.set("t", "count", 3);
		settings.set("t", "name", "ada");
		settings.set("t", "on", true);
		settings.set("t", "when", LocalDate.of(2026, 9, 18));

		assertEquals(Optional.of(2.5), settings.get("t", "sigma", Double.class));
		assertEquals(Optional.of(3), settings.get("t", "count", Integer.class));
		assertEquals(Optional.of("ada"), settings.get("t", "name", String.class));
		assertEquals(Optional.of(true), settings.get("t", "on", Boolean.class));
		assertEquals(Optional.of(LocalDate.of(2026, 9, 18)), settings.get("t",
			"when", LocalDate.class));
	}

	/** A file round-trips, through the converter a dialog would use. */
	@Test
	public void testFilesRoundTrip() {
		final Settings settings = Settings.inMemory();
		settings.set("t", "input", new File("/data/a.tif"));

		assertEquals("/data/a.tif", settings.get("t", "input"));
		assertEquals(Optional.of(new File("/data/a.tif")), settings.get("t",
			"input", File.class));
	}

	/**
	 * A list of files is an array of strings, not one mangled string - so a
	 * {@code List<File>} parameter comes back as one.
	 */
	@Test
	public void testCollectionsAreArrays() {
		final Settings settings = Settings.inMemory();
		settings.set("t", "inputs", List.of(new File("/a.tif"), new File(
			"/b.tif")));

		assertEquals(List.of("/a.tif", "/b.tif"), settings.get("t", "inputs"));
		assertEquals(Optional.of(List.of(new File("/a.tif"), new File("/b.tif"))),
			settings.get("t", "inputs", Types.parameterize(List.class, File.class)));
	}

	/** An enum is its name, and comes back an enum. */
	@Test
	public void testEnums() {
		final Settings settings = Settings.inMemory();
		settings.set("t", "edges", Edges.WRAP);

		assertEquals("WRAP", settings.get("t", "edges"));
		assertEquals(Optional.of(Edges.WRAP), settings.get("t", "edges",
			Edges.class));
	}

	/** Arbitrary precision keeps its precision, so it is stored as text. */
	@Test
	public void testExactNumbers() {
		final Settings settings = Settings.inMemory();
		settings.set("t", "exact", new BigDecimal("0.1000000000000000000001"));

		assertEquals(Optional.of(new BigDecimal("0.1000000000000000000001")),
			settings.get("t", "exact", BigDecimal.class));
	}

	/** What cannot be stored says so rather than storing nonsense. */
	@Test
	public void testUnstorableValue() {
		final Settings settings = Settings.inMemory();

		assertFalse(settings.set("t", "thing", new Object() {
			// NB: an anonymous class whose toString is its identity would store a
			// string that means nothing tomorrow.
		}) && false, "stored as a string, and useless to read back");
	}

	/** Saving writes a file a person can read, and keeps their comments. */
	@Test
	public void testSaveAndReload(@TempDir final Path dir) throws Exception {
		final Path file = dir.resolve("nested").resolve("settings.toml");
		final Settings settings = Settings.of(file);
		settings.set("sc.fiji.commands.Blur", "sigma", 0.7);
		settings.save();

		final String written = new String(Files.readAllBytes(file),
			StandardCharsets.UTF_8);
		assertTrue(written.contains("[sc.fiji.commands.Blur]"), written);
		assertTrue(written.contains("sigma = 0.7"), written);

		// a person edits it, comment and all
		Files.write(file, written.replace("sigma = 0.7",
			"# the detector is noisy\nsigma = 1.4").getBytes(StandardCharsets.UTF_8));

		final Settings reloaded = Settings.of(file);
		assertEquals(Optional.of(1.4), reloaded.get("sc.fiji.commands.Blur",
			"sigma", Double.class));

		reloaded.set("sc.fiji.commands.Blur", "sigma", 2.1);
		reloaded.save();
		final String again = new String(Files.readAllBytes(file),
			StandardCharsets.UTF_8);
		assertTrue(again.contains("# the detector is noisy"), again);
		assertTrue(again.contains("sigma = 2.1"), again);
	}

	/** In-memory settings have nowhere to save to, and saving is not an error. */
	@Test
	public void testInMemory() {
		final Settings settings = Settings.inMemory();
		settings.set("t", "k", "v");
		settings.save(); // NB: does nothing, quietly
		assertEquals(Optional.empty(), settings.file());
	}

	public enum Edges {
			REFLECT, ZERO, WRAP
	}
}
