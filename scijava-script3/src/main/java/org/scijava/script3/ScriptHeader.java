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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scijava.common3.Classes;
import org.scijava.struct.ItemIO;

/**
 * What a script declares about itself, read from its {@code #@} lines.
 * <p>
 * The syntax is SciJava Common's, because tens of thousands of scripts are
 * written in it:
 * </p>
 *
 * <pre>
 * #@ String name
 * #@ String(label = "Your name", value = "ada") name
 * #@ double(min = 0, max = 1) weight
 * #@ File(style = "directory") folder
 * #@output String greeting
 * #@script(menu = "Plugins&gt;My Script")
 * </pre>
 * <p>
 * The attributes are the same ones a Java {@code @Parameter} carries - label,
 * callback, validator, choices, min, max, style - and mean the same things,
 * which is the point: a script and a command differ in the language they are
 * written in and in nothing else.
 * </p>
 *
 * @author Curtis Rueden
 */
public class ScriptHeader {

	/** A parameter a script declares. */
	public static class Parameter {

		private final String name;
		private final Class<?> type;
		private final ItemIO io;
		private final Map<String, String> attrs;

		Parameter(final String name, final Class<?> type, final ItemIO io,
			final Map<String, String> attrs)
		{
			this.name = name;
			this.type = type;
			this.io = io;
			this.attrs = attrs;
		}

		public String name() {
			return name;
		}

		public Class<?> type() {
			return type;
		}

		public ItemIO io() {
			return io;
		}

		public Map<String, String> attrs() {
			return attrs;
		}

		@Override
		public String toString() {
			return io + " " + type.getSimpleName() + " " + name + " " + attrs;
		}
	}

	// NB: the attributes may attach to the direction or to the type --
	// `#@input(persist = false) boolean verbose` and
	// `#@ String(label = "Name") name` are both in the wild -- so the line is
	// taken apart rather than matched in one go.
	private static final Pattern LINE = Pattern.compile("^\\s*#@\\s*(\\S.*)$");

	// NB: a script may be executable in its own right.
	private static final Pattern SHEBANG = Pattern.compile("^#!(.*)$");

	private static final Pattern DIRECTION = Pattern.compile(
		"^(input|output|both)\\b(.*)$");

	private static final Pattern DIRECTIVE = Pattern.compile(
		"^\\s*#@script\\s*\\(([^)]*)\\)\\s*$");

	private final List<Parameter> parameters = new ArrayList<>();
	private final Map<String, String> directives = new LinkedHashMap<>();
	private String shebang;
	private String body;

	/** Reads what the given script declares. */
	public static ScriptHeader parse(final String code) {
		final ScriptHeader header = new ScriptHeader();
		final String[] lines = code.split("\r?\n", -1);
		for (int i = 0; i < lines.length; i++) {
			final String line = lines[i];
			final boolean declaration;
			final Matcher directive = DIRECTIVE.matcher(line);
			if (directive.matches()) {
				header.directives.putAll(attrs(directive.group(1)));
				declaration = true;
			}
			else {
				final Matcher matcher = LINE.matcher(line);
				declaration = matcher.matches();
				if (declaration) header.declare(matcher.group(1));
			}
			// NB: a declaration is blanked rather than removed, so that the line
			// numbers in the language's own error messages still point at the
			// line the author is looking at. `#@` is a comment in some languages
			// and a syntax error in others, so leaving it in is not an option.
			final Matcher shebang = SHEBANG.matcher(line);
			if (i == 0 && shebang.matches()) {
				// NB: as SciJava Common reads it: everything after #! names the
				// language, so `#!jython` and `#!/usr/bin/env jython` both say
				// Jython. A path is taken apart because that is how people write
				// shebangs everywhere else.
				final String said = shebang.group(1).trim();
				final String[] words = said.split("[\\s/]+");
				header.shebang = words.length == 0 ? said : words[words.length - 1];
				lines[i] = "";
				continue;
			}
			if (declaration) lines[i] = "";
		}
		header.body = String.join("\n", lines);
		return header;
	}

	/**
	 * Gets the script with its declarations blanked out: what the language
	 * itself is asked to run.
	 */
	public String body() {
		return body;
	}

	/** Gets the parameters the script declares, in the order it declares them. */
	public List<Parameter> parameters() {
		return List.copyOf(parameters);
	}

	/**
	 * Gets what the script says about itself: where it belongs in the menus,
	 * what to call it, what shortcut it answers to.
	 * <p>
	 * These are the {@code #@script(...)} keys, and they are the same names a
	 * {@code @Menu} annotation uses, so
	 * {@code org.scijava.command.CommandInfo.of} turns them into a menu entry
	 * directly.
	 * </p>
	 */
	public Map<String, String> directives() {
		return Map.copyOf(directives);
	}

	/**
	 * Gets the language this script says it is written in, if it says.
	 * <p>
	 * Two ways of saying it, because both are in use: {@code #@script(language
	 * = "jython")}, and a shebang line. This is what disambiguates the
	 * languages that share an extension - {@code .py} being Jython or Python
	 * depending on which is meant - and a script that says nothing is read
	 * according to its extension, as before.
	 * </p>
	 */
	public Optional<String> language() {
		final String declared = directives.get("language");
		if (declared != null && !declared.isEmpty()) return Optional.of(declared);
		return Optional.ofNullable(shebang);
	}

	// -- Helper methods --

	private void declare(final String declaration) {
		String rest = declaration.trim();

		// the direction, if stated: `#@output`, `#@both`
		String io = null;
		final Matcher direction = DIRECTION.matcher(rest);
		if (direction.matches()) {
			io = direction.group(1);
			rest = direction.group(2).trim();
		}

		// the attributes, wherever they were attached
		Map<String, String> attrs = Map.of();
		final int open = rest.indexOf('(');
		if (open >= 0) {
			final int close = rest.lastIndexOf(')');
			if (close < open) {
				throw new ScriptException("Unclosed attributes in: #@" + declaration);
			}
			attrs = attrs(rest.substring(open + 1, close));
			rest = (rest.substring(0, open) + " " + rest.substring(close + 1))
				.trim();
		}

		// what is left is the type and the name, or just the name
		final String[] words = rest.split("\\s+");
		if (words.length == 0 || words[0].isEmpty()) return; // nothing declared
		if (words.length > 2) {
			throw new ScriptException("Cannot read script parameter: #@" +
				declaration);
		}
		final String name = words[words.length - 1];
		final Class<?> type = words.length == 1 ? Object.class : type(words[0]);
		if (type == null) {
			throw new ScriptException("No such type in script parameter: " +
				words[0]);
		}
		parameters.add(new Parameter(name, type, io(io), attrs));
	}

	private static ItemIO io(final String io) {
		if (io == null || io.equals("input")) return ItemIO.INPUT;
		if (io.equals("output")) return ItemIO.OUTPUT;
		// NB: SciJava Common's `both` is this: the value goes in, and what the
		// script leaves behind comes out.
		return ItemIO.MUTABLE;
	}

	/** Resolves a type name, allowing the short names Java itself allows. */
	private static Class<?> type(final String name) {
		switch (name) {
			case "boolean":
				return boolean.class;
			case "byte":
				return byte.class;
			case "char":
				return char.class;
			case "short":
				return short.class;
			case "int":
				return int.class;
			case "long":
				return long.class;
			case "float":
				return float.class;
			case "double":
				return double.class;
			default:
				break;
		}
		final Class<?> direct = Classes.load(name, true);
		if (direct != null) return direct;
		// NB: the names a script writer actually types are unqualified.
		for (final String pkg : new String[] { "java.lang.", "java.util.",
			"java.io.", "java.math.", "java.nio.file." })
		{
			final Class<?> c = Classes.load(pkg + name, true);
			if (c != null) return c;
		}
		return null;
	}

	/** Reads {@code a = 1, b = "two"} into a map. */
	private static Map<String, String> attrs(final String text) {
		final Map<String, String> attrs = new LinkedHashMap<>();
		if (text == null || text.isBlank()) return attrs;
		for (final String pair : split(text)) {
			final int equals = pair.indexOf('=');
			if (equals < 0) {
				// NB: a bare word is a flag: `#@ boolean(persist) verbose`.
				attrs.put(pair.trim(), "true");
				continue;
			}
			attrs.put(pair.substring(0, equals).trim(), unquote(pair.substring(
				equals + 1).trim()));
		}
		return attrs;
	}

	/** Splits on commas, except inside quotes, where a comma is a comma. */
	private static List<String> split(final String text) {
		final List<String> parts = new ArrayList<>();
		final StringBuilder part = new StringBuilder();
		char quote = 0;
		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			if (quote != 0) {
				if (c == quote) quote = 0;
				part.append(c);
			}
			else if (c == '"' || c == '\'') {
				quote = c;
				part.append(c);
			}
			else if (c == ',') {
				parts.add(part.toString());
				part.setLength(0);
			}
			else part.append(c);
		}
		if (part.length() > 0) parts.add(part.toString());
		return parts;
	}

	private static String unquote(final String value) {
		if (value.length() < 2) return value;
		final char first = value.charAt(0);
		if ((first == '"' || first == '\'') && value.charAt(value.length() - 1) ==
			first)
		{
			return value.substring(1, value.length() - 1);
		}
		return value;
	}
}
