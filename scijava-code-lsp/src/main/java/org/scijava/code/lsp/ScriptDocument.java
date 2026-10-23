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

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 * A SciJava script as plain code of its language (a "virtual document"), for
 * tools that know the language but not SciJava, e.g. language servers: its
 * script parameters ({@code #@}) declared with the types they have at run
 * time, and the lines SciJava removes before it runs (e.g. those {@code #@}
 * lines) blanked. (A {@link org.scijava.code.lsp.ScriptSession} shows its
 * server this text.)
 * <p>
 * The transformation only prepends a header (see {@link ScriptDialect#header})
 * and blanks lines in place (with spaces), so positions in the script just
 * shift by the header (see {@link #toTransformed(int)} and
 * {@link #toOriginal(int)}). Offsets count
 * {@code char}s, i.e. UTF-16 code units, as do Java strings and the Language
 * Server Protocol.
 * </p>
 *
 * @author Gabriel Selzer
 * @see ScriptDialect
 */
public final class ScriptDocument {

	private final String original;
	private final String text;
	private final String languageId;
	/** Where the script runs, as its dialect says, given its file. */
	private final BiFunction<String, Optional<File>, Environment> where;
	/** Number of characters prepended. */
	private final int headerLength;
	private final Set<String> hiddenNames;

	private ScriptDocument(final String original, final String text,
		final String languageId,
		final BiFunction<String, Optional<File>, Environment> where,
		final int headerLength, final Set<String> hiddenNames)
	{
		this.original = original;
		this.text = text;
		this.languageId = languageId;
		this.where = where;
		this.headerLength = headerLength;
		this.hiddenNames = Collections.unmodifiableSet(hiddenNames);
	}

	/**
	 * A script as it is, as plain text ({@code plaintext}): nothing declared,
	 * nothing blanked.
	 */
	public static ScriptDocument of(final String script) {
		return new ScriptDocument(script, script, "plaintext", (t,
			f) -> new Environment(), 0, Collections.emptySet());
	}

	/**
	 * Transforms a script.
	 *
	 * @param script The script's source code.
	 * @param variables The variables defined before the script runs (e.g. its
	 *          script parameters), by name.
	 * @param removedLines The lines (from 0) to blank: those SciJava removes
	 *          before running the script (e.g. its {@code #@} lines).
	 * @param dialect How the script's language declares them.
	 */
	public static ScriptDocument of(final String script,
		final Map<String, ScriptDialect.Variable> variables,
		final Set<Integer> removedLines, final ScriptDialect dialect)
	{
		final ScriptDialect.Header h = dialect.header(variables);
		final String header = h.code().isEmpty() ? "" : h.code() + "\n";
		final Set<String> hidden = new LinkedHashSet<>();
		for (final String name : h.hiddenNames()) {
			if (!mentions(script, name)) hidden.add(name);
		}
		return new ScriptDocument(script, header + blank(script, removedLines),
			dialect.languageId(), dialect::environment, header.length(),
			hidden);
	}

	/**
	 * The names to leave out of completion: those only the header binds (see
	 * {@link ScriptDialect.Header#hiddenNames()}), which the script does not
	 * mention.
	 */
	Set<String> hiddenNames() {
		return hiddenNames;
	}

	/** The script as plain code of its language. */
	public String text() {
		return text;
	}

	/**
	 * The LSP language of the {@link #text()} (e.g. {@code python}), as its
	 * dialect says; or {@code plaintext}, for a script as it is.
	 */
	String languageId() {
		return languageId;
	}

	/**
	 * Gets where the script runs, as its dialect says (see
	 * {@link ScriptDialect#environment}); or a blank {@link Environment}, for a
	 * script as it is.
	 *
	 * @param file The script's file, if it has one.
	 */
	Environment environment(final Optional<File> file) {
		return where.apply(original, file);
	}

	/** Maps an offset in the script to one in {@link #text()}. */
	int toTransformed(final int offset) {
		return offset + headerLength;
	}

	/**
	 * Maps an offset in {@link #text()} to one in the script; or
	 * returns -1 if it lies in the header prepended.
	 */
	public int toOriginal(final int offset) {
		return offset < headerLength ? -1 : offset - headerLength;
	}

	/** Maps a position in the script to one in {@link #text()}. */
	Position toTransformed(final Position p) {
		return Positions.position(text, toTransformed(Positions.offset(original,
			p)));
	}

	/**
	 * Maps a position in {@link #text()} to one in the script; or returns null
	 * if it lies in the header prepended.
	 */
	Position toOriginal(final Position p) {
		final int offset = toOriginal(Positions.offset(text, p));
		return offset < 0 ? null : Positions.position(original, offset);
	}

	/**
	 * Maps a range in {@link #text()} to one in the script; or returns null if
	 * either end lies in the header prepended.
	 */
	Range toOriginal(final Range r) {
		final Position start = toOriginal(r.getStart());
		final Position end = toOriginal(r.getEnd());
		return start == null || end == null ? null : new Range(start, end);
	}

	// -- Helper methods --

	/** Whether the script mentions a name, e.g. to import it itself. */
	private static boolean mentions(final String script, final String name) {
		return Pattern.compile("(?<![\\w.])" + Pattern.quote(name) + "(?!\\w)")
			.matcher(script).find();
	}

	/** Replaces the characters of the given lines with spaces. */
	private static String blank(final String script, final Set<Integer> lines) {
		final StringBuilder sb = new StringBuilder(script);
		int start = 0;
		for (int line = 0; start <= script.length(); line++) {
			int end = script.indexOf('\n', start);
			if (end < 0) end = script.length();
			// NB: Keep a carriage return, so the line structure is unchanged.
			final int contentEnd = end > start && script.charAt(end - 1) == '\r'
				? end - 1 : end;
			if (lines.contains(line)) {
				for (int i = start; i < contentEnd; i++) sb.setCharAt(i, ' ');
			}
			start = end + 1;
		}
		return sb.toString();
	}
}
