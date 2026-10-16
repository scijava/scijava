/*-
 * #%L
 * Python code intelligence for SciJava, via jedi-language-server.
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

package org.scijava.code.lsp.jedi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;

/**
 * Reads the dependencies a Python script declares inline, in a
 * <a href="https://packaging.python.org/en/latest/specifications/inline-script-metadata/">PEP
 * 723</a> metadata block:
 *
 * <pre>
 * # /// script
 * # dependencies = ["tifffile", "scikit-image"]
 * # ///
 * </pre>
 *
 * @author Gabriel Selzer
 */
final class InlineMetadata {

	/**
	 * Regular expression for metadata blocks, from the specification's
	 * reference implementation.
	 */
	private static final Pattern BLOCK = Pattern.compile(
		"(?m)^# /// (?<type>[a-zA-Z0-9-]+)$\\s(?<content>(^#(| .*)$\\s)+)^# ///$");

	private InlineMetadata() {
		// Prevent instantiation of utility class.
	}

	/**
	 * Extracts the TOML content of a script's {@code script} metadata block.
	 *
	 * @param script Source code of the script.
	 * @return The block's TOML content, or null if there is no block.
	 * @throws IllegalArgumentException If there is more than one block.
	 */
	static String extract(final String script) {
		// NB: Normalize line endings, since the pattern expects one whitespace
		// character between lines.
		final Matcher m = BLOCK.matcher(script.replace("\r\n", "\n"));
		String content = null;
		while (m.find()) {
			if (!"script".equals(m.group("type"))) continue;
			if (content != null) {
				throw new IllegalArgumentException(
					"Multiple '# /// script' metadata blocks found");
			}
			content = uncomment(m.group("content"));
		}
		return content;
	}

	/**
	 * Gets the {@code dependencies} (PEP 508 requirement strings, e.g.
	 * {@code "numpy > 2"}) of a {@code script} metadata block.
	 *
	 * @param metadata TOML content of the metadata block.
	 * @return The dependencies, in declaration order; empty if none.
	 * @throws IllegalArgumentException If the metadata is not valid.
	 */
	static List<String> dependencies(final String metadata) {
		final JsonNode parsed;
		try {
			parsed = new TomlMapper().readTree(metadata);
		}
		catch (final IOException e) {
			throw new IllegalArgumentException(
				"Invalid TOML in script metadata: " + e.getMessage(), e);
		}
		final JsonNode deps = parsed == null ? null : parsed.get("dependencies");
		if (deps == null) return Collections.emptyList();
		if (!deps.isArray()) {
			throw new IllegalArgumentException("dependencies must be an array");
		}
		final List<String> out = new ArrayList<>();
		for (final JsonNode dep : deps) {
			if (!dep.isTextual()) {
				throw new IllegalArgumentException("dependencies must be strings");
			}
			out.add(dep.asText());
		}
		return out;
	}

	/** Removes the comment prefix from each line, as the specification says. */
	private static String uncomment(final String content) {
		final StringBuilder sb = new StringBuilder();
		for (final String line : content.split("\n", -1)) {
			if (line.startsWith("# ")) sb.append(line, 2, line.length());
			else if (line.startsWith("#")) sb.append(line, 1, line.length());
			sb.append('\n');
		}
		return sb.toString();
	}
}
