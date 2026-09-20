/*
 * #%L
 * Common functionality widely used across SciJava modules.
 * %%
 * Copyright (C) 2021 - 2025 SciJava developers.
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

package org.scijava.common3;

import java.util.regex.Pattern;

/**
 * Useful methods for working with {@link String}s.
 *
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public final class Strings {

	/** The character used for padding when none is given. */
	public static final char DEFAULT_PAD_CHAR = ' ';

	private Strings() {
		// NB: prevent instantiation of utility class.
	}

	/**
	 * Splits the given string around occurrences of the separator, ignoring
	 * separators that fall inside double quotes.
	 *
	 * @param s the string to split
	 * @param separator the separator, treated as a literal, not a regex
	 * @return the resulting substrings, including trailing empty ones
	 */
	public static String[] splitUnquoted(final String s,
		final String separator)
	{
		// See https://stackoverflow.com/a/1757107/1919049
		return s.split(Pattern.quote(separator) +
			"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	}

	/** Gets whether the given string is {@code null} or of length zero. */
	public static boolean isNullOrEmpty(final String s) {
		return s == null || s.isEmpty();
	}

	/**
	 * Pads the end of the given string with {@link #DEFAULT_PAD_CHAR} until it
	 * reaches the given length.
	 */
	public static String padEnd(final String s, final int length) {
		return padEnd(s, length, DEFAULT_PAD_CHAR);
	}

	/**
	 * Pads the end of the given string with the given character until it
	 * reaches the given length. A string already that long is returned as is.
	 */
	public static String padEnd(final String s, final int length,
		final char padChar)
	{
		return pad(s, length, padChar, false);
	}

	/**
	 * Pads the start of the given string with {@link #DEFAULT_PAD_CHAR} until
	 * it reaches the given length.
	 */
	public static String padStart(final String s, final int length) {
		return padStart(s, length, DEFAULT_PAD_CHAR);
	}

	/**
	 * Pads the start of the given string with the given character until it
	 * reaches the given length. A string already that long is returned as is.
	 */
	public static String padStart(final String s, final int length,
		final char padChar)
	{
		return pad(s, length, padChar, true);
	}

	private static String pad(final String s, final int length,
		final char padChar, final boolean atStart)
	{
		if (s == null) return null;
		final int padding = length - s.length();
		if (padding <= 0) return s;
		final StringBuilder sb = new StringBuilder(length);
		if (!atStart) sb.append(s);
		for (int i = 0; i < padding; i++)
			sb.append(padChar);
		if (atStart) sb.append(s);
		return sb.toString();
	}
}
