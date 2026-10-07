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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A SciJava script as plain code of its language (a "virtual document"), for
 * tools that know the language but not SciJava, e.g. language servers: its
 * script parameters ({@code #@}) declared with the types they have at run
 * time, and lines that are not of the language blanked.
 * <p>
 * The transformation only prepends one header line and blanks lines in place
 * (with spaces), so {@link #map() positions} map back trivially.
 * </p>
 *
 * @author Gabriel Selzer
 * @see ScriptDialect
 */
public final class ScriptDocument {

	private final String original;
	private final String text;
	private final PositionMap map;
	private final Map<String, String> parameters;
	private final Environment environment;

	private ScriptDocument(final String original, final String text,
		final PositionMap map, final Map<String, String> parameters,
		final Environment environment)
	{
		this.original = original;
		this.text = text;
		this.map = map;
		this.parameters = Collections.unmodifiableMap(parameters);
		this.environment = environment;
	}

	/**
	 * Transforms a script.
	 *
	 * @param script The script's source code.
	 * @param parameters The variables defined before the script runs (e.g. its
	 *          script parameters, as {@link LanguageServerService#scriptParameters}
	 *          gives them), by name, with their Java types.
	 * @param dialect How the script's language declares them.
	 * @param environment Where the script runs (null if unknown).
	 */
	public static ScriptDocument of(final String script,
		final Map<String, Class<?>> parameters, final ScriptDialect dialect,
		final Environment environment)
	{
		// The parameters' run-time types, and their declarations.
		final Map<String, String> types = new LinkedHashMap<>();
		final List<String> declarations = new ArrayList<>();
		for (final Map.Entry<String, Class<?>> p : parameters.entrySet()) {
			final String type = dialect.runtimeType(p.getValue());
			types.put(p.getKey(), type);
			if (type != null) declarations.add(dialect.declare(p.getKey(), type));
		}
		declarations.addAll(dialect.extraDeclarations());
		final Set<String> used = new LinkedHashSet<>(types.values());
		used.remove(null);
		final List<String> statements = new ArrayList<>(dialect.imports(used));
		statements.addAll(declarations);
		final String header = statements.isEmpty() ? "" : String.join(dialect
			.separator(), statements) + "\n";

		return new ScriptDocument(script, header + blank(script, dialect),
			new PositionMap(header.length(), header.isEmpty() ? 0 : 1), types,
			environment == null ? Environment.NONE : environment);
	}

	/** The script, as given. */
	public String original() {
		return original;
	}

	/** The script as plain code of its language. */
	public String text() {
		return text;
	}

	/** Maps positions between {@link #original()} and {@link #text()}. */
	public PositionMap map() {
		return map;
	}

	/**
	 * The variables defined before the script runs, by name, with their
	 * run-time types as written in the language (null if not declared).
	 */
	public Map<String, String> parameters() {
		return parameters;
	}

	/** Where the script runs. */
	public Environment environment() {
		return environment;
	}

	// -- Helper methods --

	/** Replaces the characters of the lines to blank with spaces. */
	private static String blank(final String script, final ScriptDialect dialect) {
		final StringBuilder sb = new StringBuilder(script);
		int start = 0;
		while (start <= script.length()) {
			int end = script.indexOf('\n', start);
			if (end < 0) end = script.length();
			// NB: Keep a carriage return, so the line structure is unchanged.
			final int contentEnd = end > start && script.charAt(end - 1) == '\r'
				? end - 1 : end;
			if (dialect.blanks(script.substring(start, contentEnd))) {
				for (int i = start; i < contentEnd; i++) sb.setCharAt(i, ' ');
			}
			start = end + 1;
		}
		return sb.toString();
	}
}
