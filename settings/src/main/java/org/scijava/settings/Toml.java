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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A TOML document: tables of values, and the comments around them.
 * <p>
 * Enough TOML for settings, and no more: tables, bare and quoted keys,
 * strings (basic, literal, and both multiline forms), integers in decimal,
 * hex, octal and binary, floats including {@code inf} and {@code nan},
 * booleans, all four date-and-time types, and arrays of any of those. Inline
 * tables and arrays of tables are refused by line number rather than
 * misunderstood.
 * </p>
 * <p>
 * NB: <strong>comments are kept.</strong> This file is one a person edits, and
 * a store that swallowed the {@code # why this is 0.7} somebody left beside a
 * value is a store they would stop editing. A comment belongs to whatever
 * follows it, and comes back out above it.
 * </p>
 * <p>
 * NB: the API is deliberately small - parse, get, set, write - so that a real
 * TOML library can take its place if settings ever outgrow this.
 * </p>
 *
 * @author Curtis Rueden
 */
public class Toml {

	/** The name of the table holding values written before any header. */
	public static final String ROOT = "";

	/** One value, and the comments written above it. */
	private static class Entry {

		final List<String> comments;
		Object value;

		Entry(final List<String> comments, final Object value) {
			this.comments = new ArrayList<>(comments);
			this.value = value;
		}
	}

	/** One table, in the order its keys were written. */
	private static class Table {

		final List<String> comments = new ArrayList<>();
		final Map<String, Entry> entries = new LinkedHashMap<>();
	}

	private final Map<String, Table> tables = new LinkedHashMap<>();
	private final List<String> trailing = new ArrayList<>();

	/** Creates an empty document. */
	public static Toml empty() {
		return new Toml();
	}

	/**
	 * Reads a document.
	 *
	 * @param text the TOML
	 * @return the document
	 * @throws TomlException if it cannot be read
	 */
	public static Toml parse(final String text) {
		return new Parser(text).parse();
	}

	/** Gets the table names, in the order they appear. */
	public List<String> tables() {
		return List.copyOf(tables.keySet());
	}

	/** Gets whether the document has the given table. */
	public boolean hasTable(final String table) {
		return tables.containsKey(table);
	}

	/** Gets a table's values, in the order they appear. */
	public Map<String, Object> table(final String table) {
		final Table t = tables.get(table);
		if (t == null) return Map.of();
		final Map<String, Object> values = new LinkedHashMap<>();
		t.entries.forEach((key, entry) -> values.put(key, entry.value));
		return values;
	}

	/** Gets one value, or null if there is none. */
	public Object get(final String table, final String key) {
		final Table t = tables.get(table);
		if (t == null) return null;
		final Entry entry = t.entries.get(key);
		return entry == null ? null : entry.value;
	}

	/**
	 * Sets one value, keeping whatever comments were written around it.
	 *
	 * @param table the table, {@link #ROOT} for the values above any header
	 * @param key the key
	 * @param value a string, number, boolean, date, time, or list of those
	 */
	public void set(final String table, final String key, final Object value) {
		check(value);
		final Table t = tables.computeIfAbsent(table, name -> new Table());
		final Entry entry = t.entries.get(key);
		if (entry == null) t.entries.put(key, new Entry(List.of(), value));
		else entry.value = value;
	}

	/** Removes one value. */
	public void remove(final String table, final String key) {
		final Table t = tables.get(table);
		if (t != null) t.entries.remove(key);
	}

	/** Removes a table and everything in it. */
	public void removeTable(final String table) {
		tables.remove(table);
	}

	/** Writes the document out, comments and order intact. */
	public String write() {
		final StringBuilder sb = new StringBuilder();
		tables.forEach((name, table) -> {
			if (table.entries.isEmpty() && !name.equals(ROOT)) return;
			table.comments.forEach(c -> sb.append(c).append('\n'));
			if (!name.equals(ROOT)) {
				sb.append('[').append(writeKey(name)).append("]\n");
			}
			table.entries.forEach((key, entry) -> {
				entry.comments.forEach(c -> sb.append(c).append('\n'));
				sb.append(writeKey(key)).append(" = ").append(writeValue(entry.value))
					.append('\n');
			});
			sb.append('\n');
		});
		trailing.forEach(c -> sb.append(c).append('\n'));
		return sb.toString();
	}

	@Override
	public String toString() {
		return write();
	}

	// -- Helper methods --

	/** Refuses a value TOML cannot hold, rather than writing nonsense. */
	private static void check(final Object value) {
		if (value == null) throw new IllegalArgumentException("No value");
		if (value instanceof String || value instanceof Boolean || //
			value instanceof Long || value instanceof Integer || //
			value instanceof Double || value instanceof Float || //
			value instanceof LocalDate || value instanceof LocalTime || //
			value instanceof LocalDateTime || value instanceof OffsetDateTime)
		{
			return;
		}
		if (value instanceof List) {
			((List<?>) value).forEach(Toml::check);
			return;
		}
		throw new IllegalArgumentException("TOML cannot hold a " + value.getClass()
			.getName());
	}

	/** Writes a key, quoting it where TOML would not take it bare. */
	private static String writeKey(final String key) {
		if (key.isEmpty()) return "\"\"";
		for (int i = 0; i < key.length(); i++) {
			final char c = key.charAt(i);
			final boolean bare = Character.isLetterOrDigit(c) && c < 128 || //
				c == '_' || c == '-' || c == '.';
			if (!bare) return quote(key);
		}
		return key;
	}

	private static String writeValue(final Object value) {
		if (value instanceof String) {
			final String s = (String) value;
			// NB: a value with newlines in it is written the way TOML writes one,
			// so that a text area's contents stay readable in the file.
			if (s.indexOf('\n') >= 0) {
				return "\"\"\"\n" + s.replace("\\", "\\\\").replace("\"\"\"",
					"\\\"\\\"\\\"") + "\"\"\"";
			}
			return quote(s);
		}
		if (value instanceof Double || value instanceof Float) {
			final double d = ((Number) value).doubleValue();
			if (Double.isNaN(d)) return "nan";
			if (d == Double.POSITIVE_INFINITY) return "inf";
			if (d == Double.NEGATIVE_INFINITY) return "-inf";
			return String.valueOf(d);
		}
		if (value instanceof List) {
			final StringBuilder sb = new StringBuilder("[");
			final List<?> list = (List<?>) value;
			for (int i = 0; i < list.size(); i++) {
				if (i > 0) sb.append(", ");
				sb.append(writeValue(list.get(i)));
			}
			return sb.append(']').toString();
		}
		// NB: numbers, booleans and the date-and-time types all write themselves
		// in exactly the form TOML wants.
		return String.valueOf(value);
	}

	private static String quote(final String s) {
		final StringBuilder sb = new StringBuilder("\"");
		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			switch (c) {
				case '"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\r':
					sb.append("\\r");
					break;
				default:
					if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
					else sb.append(c);
			}
		}
		return sb.append('"').toString();
	}

	/** Reads the text into tables, keeping the comments. */
	private static class Parser {

		private final String s;
		private int i;
		private int line = 1;

		private Toml toml = new Toml();
		private Table current;
		private final List<String> pending = new ArrayList<>();

		Parser(final String text) {
			this.s = text;
			current = toml.tables.computeIfAbsent(ROOT, name -> new Table());
		}

		Toml parse() {
			while (true) {
				skipSpace();
				if (eof()) break;
				final char c = peek();
				if (c == '\n') {
					next();
					line++;
					continue;
				}
				if (c == '#') {
					pending.add(readComment());
					continue;
				}
				if (c == '[') {
					readTableHeader();
					continue;
				}
				readEntry();
			}
			// NB: comments with nothing after them belong to the end of the file.
			toml.trailing.addAll(pending);
			pending.clear();
			return toml;
		}

		private void readTableHeader() {
			next(); // [
			if (!eof() && peek() == '[') {
				throw new TomlException("arrays of tables are not supported here",
					line);
			}
			skipSpace();
			final String name = readKey();
			skipSpace();
			if (eof() || next() != ']') {
				throw new TomlException("expected ']' after a table name", line);
			}
			final Table table = toml.tables.computeIfAbsent(name,
				n -> new Table());
			table.comments.addAll(pending);
			pending.clear();
			current = table;
			endOfLine();
		}

		private void readEntry() {
			final String key = readKey();
			skipSpace();
			if (eof() || next() != '=') {
				throw new TomlException("expected '=' after the key '" + key + "'",
					line);
			}
			skipSpace();
			final Object value = readValue();
			current.entries.put(key, new Entry(pending, value));
			pending.clear();
			endOfLine();
		}

		/** Reads a key: bare, quoted, or dotted; dotted keys stay one name. */
		private String readKey() {
			final StringBuilder sb = new StringBuilder();
			while (true) {
				skipSpace();
				if (eof()) throw new TomlException("expected a key", line);
				final char c = peek();
				if (c == '"' || c == '\'') sb.append(readString());
				else {
					final int start = i;
					while (!eof() && isBareKeyChar(peek()))
						next();
					if (i == start) {
						throw new TomlException("expected a key, found '" + peek() + "'",
							line);
					}
					sb.append(s, start, i);
				}
				skipSpace();
				if (!eof() && peek() == '.') {
					next();
					sb.append('.');
					continue;
				}
				return sb.toString();
			}
		}

		private Object readValue() {
			if (eof()) throw new TomlException("expected a value", line);
			final char c = peek();
			if (c == '"' || c == '\'') return readString();
			if (c == '[') return readArray();
			if (c == '{') {
				throw new TomlException("inline tables are not supported here", line);
			}
			return readBareValue();
		}

		private List<Object> readArray() {
			next(); // [
			final List<Object> list = new ArrayList<>();
			while (true) {
				skipArraySpace();
				if (eof()) throw new TomlException("unclosed array", line);
				if (peek() == ']') {
					next();
					return list;
				}
				list.add(readValue());
				skipArraySpace();
				if (eof()) throw new TomlException("unclosed array", line);
				if (peek() == ',') {
					next();
					continue;
				}
				if (peek() != ']') {
					throw new TomlException("expected ',' or ']' in an array", line);
				}
			}
		}

		/** Reads a bare value: a number, a boolean, or a date. */
		private Object readBareValue() {
			final int start = i;
			while (!eof() && "\n,]#".indexOf(peek()) < 0)
				next();
			final String text = s.substring(start, i).trim();
			if (text.isEmpty()) throw new TomlException("expected a value", line);
			if (text.equals("true")) return Boolean.TRUE;
			if (text.equals("false")) return Boolean.FALSE;

			final Object moment = readMoment(text);
			if (moment != null) return moment;

			try {
				return readNumber(text);
			}
			catch (final NumberFormatException exc) {
				throw new TomlException("cannot read the value '" + text + "'", line);
			}
		}

		/** Reads one of TOML's four date-and-time types, or nothing. */
		private Object readMoment(final String text) {
			try {
				if (text.matches("\\d{4}-\\d{2}-\\d{2}")) return LocalDate.parse(text);
				if (text.matches("\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?")) {
					return LocalTime.parse(text);
				}
				final String normalized = text.replaceFirst("^(\\d{4}-\\d{2}-\\d{2}) ",
					"$1T");
				if (normalized.matches(
					"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?"))
				{
					return LocalDateTime.parse(normalized);
				}
				if (normalized.matches(".*(Z|[+-]\\d{2}:\\d{2})$") && normalized
					.contains("T"))
				{
					return OffsetDateTime.parse(normalized);
				}
			}
			catch (final java.time.format.DateTimeParseException exc) {
				throw new TomlException("cannot read the date '" + text + "'", line);
			}
			return null;
		}

		private Object readNumber(final String text) {
			final String cleaned = text.replace("_", "");
			if (cleaned.equals("inf") || cleaned.equals("+inf")) {
				return Double.POSITIVE_INFINITY;
			}
			if (cleaned.equals("-inf")) return Double.NEGATIVE_INFINITY;
			if (cleaned.equals("nan") || cleaned.equals("+nan") || cleaned.equals(
				"-nan")) return Double.NaN;
			if (cleaned.startsWith("0x")) {
				return Long.parseLong(cleaned.substring(2), 16);
			}
			if (cleaned.startsWith("0o")) {
				return Long.parseLong(cleaned.substring(2), 8);
			}
			if (cleaned.startsWith("0b")) {
				return Long.parseLong(cleaned.substring(2), 2);
			}
			if (cleaned.indexOf('.') >= 0 || cleaned.indexOf('e') >= 0 || cleaned
				.indexOf('E') >= 0)
			{
				return Double.valueOf(cleaned);
			}
			return Long.valueOf(cleaned);
		}

		/** Reads a string in any of TOML's four spellings. */
		private String readString() {
			final char quote = next();
			final boolean multiline = !eof() && peek() == quote && i + 1 < s
				.length() && s.charAt(i + 1) == quote;
			if (multiline) {
				next();
				next();
				// NB: a newline straight after the opening quotes is not content.
				if (!eof() && peek() == '\n') {
					next();
					line++;
				}
			}
			final StringBuilder sb = new StringBuilder();
			while (true) {
				if (eof()) throw new TomlException("unclosed string", line);
				final char c = next();
				if (c == '\n') line++;
				if (c == quote) {
					if (!multiline) return sb.toString();
					if (i + 1 < s.length() && s.charAt(i) == quote && s.charAt(i + 1) ==
						quote)
					{
						next();
						next();
						return sb.toString();
					}
					sb.append(c);
					continue;
				}
				if (c == '\\' && quote == '"') {
					sb.append(readEscape(multiline));
					continue;
				}
				sb.append(c);
			}
		}

		private String readEscape(final boolean multiline) {
			if (eof()) throw new TomlException("unfinished escape", line);
			final char c = next();
			switch (c) {
				case '"':
					return "\"";
				case '\\':
					return "\\";
				case 'b':
					return "\b";
				case 't':
					return "\t";
				case 'n':
					return "\n";
				case 'f':
					return "\f";
				case 'r':
					return "\r";
				case 'u':
					return readCodePoint(4);
				case 'U':
					return readCodePoint(8);
				case '\n':
					if (!multiline) {
						throw new TomlException("a line cannot end inside a string", line);
					}
					// NB: a backslash before a newline swallows the whitespace after
					// it, which is how TOML lets a long line be wrapped.
					line++;
					while (!eof() && Character.isWhitespace(peek())) {
						if (next() == '\n') line++;
					}
					return "";
				default:
					throw new TomlException("unknown escape '\\" + c + "'", line);
			}
		}

		private String readCodePoint(final int digits) {
			if (i + digits > s.length()) {
				throw new TomlException("unfinished escape", line);
			}
			final String hex = s.substring(i, i + digits);
			i += digits;
			try {
				return new String(Character.toChars(Integer.parseInt(hex, 16)));
			}
			catch (final IllegalArgumentException exc) {
				throw new TomlException("'" + hex + "' is not a code point", line);
			}
		}

		private String readComment() {
			final int start = i;
			while (!eof() && peek() != '\n')
				next();
			return s.substring(start, i).stripTrailing();
		}

		/** Insists that nothing but a comment follows on this line. */
		private void endOfLine() {
			skipSpace();
			if (eof()) return;
			if (peek() == '#') {
				// NB: a trailing comment belongs to the value it follows, and is
				// kept above it rather than beside it. Better than losing it.
				pending.add(readComment());
				return;
			}
			if (peek() != '\n') {
				throw new TomlException("unexpected '" + peek() + "' at the end of a" +
					" line", line);
			}
			next();
			line++;
		}

		private void skipSpace() {
			while (!eof() && (peek() == ' ' || peek() == '\t'))
				next();
		}

		/** Inside an array, newlines and comments are just whitespace. */
		private void skipArraySpace() {
			while (!eof()) {
				final char c = peek();
				if (c == ' ' || c == '\t') next();
				else if (c == '\n') {
					next();
					line++;
				}
				else if (c == '#') readComment();
				else return;
			}
		}

		private static boolean isBareKeyChar(final char c) {
			return Character.isLetterOrDigit(c) && c < 128 || c == '_' || c == '-';
		}

		private boolean eof() {
			return i >= s.length();
		}

		private char peek() {
			return s.charAt(i);
		}

		private char next() {
			return s.charAt(i++);
		}
	}
}
