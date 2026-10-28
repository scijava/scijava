/*-
 * #%L
 * Jython code intelligence for SciJava, via Jython's own parser.
 * %%
 * Copyright (C) 2020 - 2025 SciJava developers.
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
package org.scijava.code.lsp.jython;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.scijava.code.lsp.Positions;

/**
 * Tools for importing Java classes as their completions are accepted.
 * <p>
 * For example, if you type {@code ArrayLi} and accept {@code ArrayList}, the
 * completion also carries an edit that adds
 * {@code from java.util import ArrayList} to the script (after its last
 * import), unless the class is imported already (see {@link #autoImportEdit}).
 * </p>
 *
 * @author Albert Cardona
 */
final class JythonImports {

	private static final Pattern importPattern = Pattern.compile(
		"^(from[ \\t]+([a-zA-Z_][a-zA-Z0-9._]*)[ \\t]+|)import[ \\t]+([a-zA-Z_][a-zA-Z0-9_]*[ \\ta-zA-Z0-9_,]*)[ \\t]*([\\\\]*|)[  \\t]*(#.*|)$");
	private static final Pattern tripleQuotePattern = Pattern.compile("\"\"\"");

	private JythonImports() {
		// prevent instantiation
	}

	/** A single imported class, with its declaration line number. */
	public static final class Import {

		public final String className, alias;
		public final int lineNumber;

		public Import(final String className, final String alias,
			final int lineNumber)
		{
			this.className = className;
			this.alias = null != alias ? alias : className.substring(className
				.lastIndexOf('.') + 1);
			this.lineNumber = lineNumber;
		}

		public Import(final String packageName, final String[] parts,
			final int lineNumber)
		{
			this(packageName + "." + parts[0], 3 == parts.length ? parts[2] : null,
				lineNumber);
		}
	}

	/** Scans the whole script for imported classes, keyed by alias/simple name. */
	public static HashMap<String, Import> findImportedClasses(final String text) {
		final HashMap<String, Import> importedClasses = new HashMap<>();
		String packageName = "";
		boolean endingBackslash = false;
		boolean insideTripleQuotes = false;

		final String[] lines = text.split("\n");
		for (int i = 0; i < lines.length; ++i) {
			final String line = lines[i];
			final String trimmed = line.trim();
			if (0 == trimmed.length() || '#' == trimmed.charAt(0)) continue;
			final Matcher mq = tripleQuotePattern.matcher(line);
			int n_triple_quotes = 0;
			while (mq.find()) ++n_triple_quotes;
			if (insideTripleQuotes) {
				if (0 != n_triple_quotes % 2) insideTripleQuotes = false;
				continue;
			}
			else if (0 != n_triple_quotes % 2) {
				insideTripleQuotes = true;
				continue;
			}
			if (endingBackslash) {
				String importLine = line;
				final int backslash = line.lastIndexOf('\\');
				if (backslash > -1) importLine = importLine.substring(0, backslash);
				else {
					final int sharp = importLine.lastIndexOf('#');
					if (sharp > -1) importLine = importLine.substring(0, sharp);
				}
				for (final String simpleClassName : importLine.split(",")) {
					final Import im = new Import(packageName, simpleClassName.trim().split(
						"\\s"), i);
					importedClasses.put(im.alias, im);
				}
				endingBackslash = -1 != backslash;
				continue;
			}
			final Matcher m = importPattern.matcher(line);
			if (m.find()) {
				packageName = null == m.group(2) ? "" : m.group(2);
				for (final String simpleClassName : m.group(3).split(",")) {
					final Import im = new Import(packageName, simpleClassName.trim().split(
						"\\s"), i);
					importedClasses.put(im.alias, im);
				}
				endingBackslash = null != m.group(4) && m.group(4).length() > 0 &&
					'\\' == m.group(4).charAt(0);
			}
		}
		return importedClasses;
	}

	/**
	 * The statement importing a class, e.g. {@code from java.io import File}.
	 */
	static String importStatement(final String className) {
		final int idot = className.lastIndexOf('.');
		if (-1 == idot) return "import " + className;
		return "from " + className.substring(0, idot) + " import " + className
			.substring(idot + 1);
	}

	/**
	 * Computes the edit that inserts {@code importStatement} after the last
	 * existing import (or at the top), or {@code null} if {@code className} is
	 * already imported.
	 */
	public static TextEdit autoImportEdit(final String text,
		final String className, final String importStatement)
	{
		final HashMap<String, Import> imported = findImportedClasses(text);
		for (final Import im : imported.values()) {
			if (im.className.equals(className)) return null; // already imported
		}
		int lastImportLine = 0;
		for (final Import im : imported.values()) {
			if (im.lineNumber > lastImportLine) lastImportLine = im.lineNumber;
		}
		final int targetLine = imported.isEmpty() ? 0 : lastImportLine + 1;
		final int offset = offsetOfLine(text, targetLine);
		final Position at = Positions.position(text, Math.min(offset, text
			.length()));
		return new TextEdit(new Range(at, at), importStatement + "\n");
	}

	/** Character offset of the start of the given (0-based) line in {@code text}. */
	private static int offsetOfLine(final String text, final int line) {
		int offset = 0;
		for (int l = 0; l < line; l++) {
			final int nl = text.indexOf('\n', offset);
			if (nl < 0) return text.length();
			offset = nl + 1;
		}
		return offset;
	}
}
