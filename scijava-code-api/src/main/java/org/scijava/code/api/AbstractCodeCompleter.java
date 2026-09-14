/*-
 * #%L
 * Core API for SciJava code intelligence features.
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

package org.scijava.code.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

/**
 * Base {@link CodeCompleter} that produces suggestions by reflecting over the
 * variables bound in a live {@link ScriptEngine}.
 * <p>
 * It handles two cases, based on the token immediately preceding the caret:
 * </p>
 * <ul>
 * <li>{@code variable.prefix} &mdash; the public fields and methods of the bound
 * variable's runtime class whose names start with {@code prefix}; and</li>
 * <li>{@code prefix} &mdash; the names of bound variables starting with
 * {@code prefix}.</li>
 * </ul>
 * <p>
 * This works for any JSR-223 language and is the default used by
 * {@link CodeCompletionService} when no language-specific completer is
 * registered. Subclasses may override {@link #complete} to add richer behavior.
 * </p>
 *
 * @author Hadrien Mary
 * @author Curtis Rueden
 */
public abstract class AbstractCodeCompleter implements CodeCompleter {

	/** Matches a trailing {@code something.prefix} token before the caret. */
	private static final Pattern DOT_TOKEN = Pattern.compile(
		"([A-Za-z_][A-Za-z0-9_]*)\\.([A-Za-z0-9_]*)$");

	/** Matches a trailing bare identifier token before the caret. */
	private static final Pattern WORD_TOKEN = Pattern.compile(
		"([A-Za-z_][A-Za-z0-9_]*)$");

	@Override
	public CompletionResult complete(final CompletionRequest request) {
		final ScriptEngine engine = request.engine();
		if (engine == null) return CompletionResult.EMPTY;

		final String line = request.lineToCaret();
		final int caret = request.offset();

		final Matcher dot = DOT_TOKEN.matcher(line);
		if (dot.find()) {
			final String varName = dot.group(1);
			final String prefix = dot.group(2);
			final int replaceStart = caret - prefix.length();
			return sorted(memberCompletions(engine, varName, prefix), replaceStart);
		}

		final Matcher word = WORD_TOKEN.matcher(line);
		if (word.find()) {
			final String prefix = word.group(1);
			final int replaceStart = caret - prefix.length();
			return sorted(variableCompletions(engine, prefix), replaceStart);
		}

		return sorted(variableCompletions(engine, ""), caret);
	}

	// -- Helper methods --

	private List<Completion> variableCompletions(final ScriptEngine engine,
		final String prefix)
	{
		final String lp = prefix.toLowerCase();
		final List<Completion> matches = new ArrayList<>();
		final Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		if (bindings == null) return matches;
		for (final String key : bindings.keySet()) {
			if (key.toLowerCase().startsWith(lp)) {
				matches.add(Completion.builder(key) //
					.kind(Completion.Kind.VARIABLE).build());
			}
		}
		return matches;
	}

	private List<Completion> memberCompletions(final ScriptEngine engine,
		final String varName, final String prefix)
	{
		final Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		if (bindings == null || !bindings.containsKey(varName)) {
			return new ArrayList<>();
		}
		final Object value = bindings.get(varName);
		if (value == null) return new ArrayList<>();
		return Completions.membersOf(value.getClass(), prefix, false);
	}

	private CompletionResult sorted(final List<Completion> matches,
		final int replaceStart)
	{
		// De-duplicate by insertion text, keeping the first occurrence.
		final Map<String, Completion> unique = new LinkedHashMap<>();
		for (final Completion c : matches) unique.putIfAbsent(c.insertionText(), c);

		final List<Completion> result = new ArrayList<>(unique.values());
		result.sort(Comparator.comparing(c -> c.insertionText().toLowerCase()));
		return new CompletionResult(result, replaceStart);
	}
}
