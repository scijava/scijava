/*
 * #%L
 * Running scripts as things that declare their inputs and outputs.
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

package org.scijava.script3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Tests finding scripts, and what they say about themselves.
 *
 * @author Curtis Rueden
 */
public class ScriptFinderTest {

	/** A script says where it belongs; a directory says it for the rest. */
	@Test
	public void testMetadata() throws URISyntaxException {
		final List<ScriptFinder.Found> found = new ScriptFinder().find(scripts());
		assertEquals(2, found.size(), found.toString());

		final Map<String, String> declared = metadata(found, "Say_Hello.groovy");
		assertEquals("Help>Say Hello From A Script", declared.get("menu"));
		assertEquals("^H", declared.get("accelerator"));
		assertEquals("50", declared.get("weight"));

		// NB: this one declares nothing, so where it sits is what it means -
		// underscores read as spaces, extension dropped.
		final Map<String, String> implied = metadata(found, "Blur_It.groovy");
		assertEquals("Process>Filters>Blur It", implied.get("menu"));
	}

	/** What was found runs, with its declared defaults. */
	@Test
	public void testFoundScriptRuns() throws Exception {
		final ScriptExecutable hello = script(new ScriptFinder().find(scripts()),
			"Say_Hello.groovy");

		final var instance = hello.create();
		instance.run();

		assertEquals("hello world", instance.parameters().member("greeting")
			.get());
	}

	/** A script may name its own language, which settles a shared extension. */
	@Test
	public void testDeclaredLanguage() {
		final Scripts scripts = Scripts.get();

		// NB: were Jython installed, this .py would be read as Groovy anyway.
		final ScriptExecutable script = scripts.of("mystery.py", //
			"#@script(language = \"groovy\")\n#@output String x\nx = \"hi\"\n");
		assertEquals("Groovy", script.language().name());

		final ScriptExecutable shebanged = scripts.of("mystery.py", //
			"#!/usr/bin/env groovy\n#@output String x\nx = \"hi\"\n");
		assertEquals("Groovy", shebanged.language().name());
	}

	/** A file no language claims is skipped, not fatal. */
	@Test
	public void testUnknownExtensionsAreSkipped() {
		final Optional<ScriptLanguage> none = Scripts.get().language("xyzzy");
		assertTrue(none.isEmpty(), "no language should claim .xyzzy");
		// NB: the scripts directory holds only .groovy files here, but a real
		// one holds text files, images and languages this install lacks.
		assertEquals(2, new ScriptFinder().find(scripts()).size());
	}

	// -- Helper methods --

	private static Path scripts() {
		try {
			return Paths.get(ScriptFinderTest.class.getResource("/scripts").toURI());
		}
		catch (final URISyntaxException exc) {
			throw new IllegalStateException(exc);
		}
	}

	private static Map<String, String> metadata(
		final List<ScriptFinder.Found> found, final String name)
	{
		return found.stream().filter(f -> f.script().name().equals(name)) //
			.findFirst().orElseThrow().metadata();
	}

	private static ScriptExecutable script(final List<ScriptFinder.Found> found,
		final String name)
	{
		return found.stream().filter(f -> f.script().name().equals(name)) //
			.findFirst().orElseThrow().script();
	}
}
