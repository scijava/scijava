/*
 * #%L
 * Running commands and scripts from a command line.
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

package org.scijava.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.scijava.command3.CommandInfo;

/**
 * Finding the command a user meant.
 * <p>
 * People do not type class names. They type what they see in the menus - "
 * Gaussian Blur", "Process&gt;Filters&gt;Gaussian Blur..." - or the tail of a
 * class name, and they type it in whatever case they please. So the lookup
 * tries the exact things first and the forgiving things after, and when two
 * commands answer equally it says so rather than picking one.
 * </p>
 *
 * @author Curtis Rueden
 */
public class CommandLookup {

	/** What a lookup found: nothing, one thing, or too many things. */
	public static class Result {

		private final List<CommandInfo> matches;

		Result(final List<CommandInfo> matches) {
			this.matches = matches;
		}

		public boolean isFound() {
			return matches.size() == 1;
		}

		public boolean isAmbiguous() {
			return matches.size() > 1;
		}

		public CommandInfo command() {
			return matches.get(0);
		}

		/** Gets everything that answered, for an error message to list. */
		public List<CommandInfo> matches() {
			return List.copyOf(matches);
		}
	}

	private final List<CommandInfo> commands;

	public CommandLookup(final List<CommandInfo> commands) {
		this.commands = List.copyOf(commands);
	}

	/**
	 * Finds the command the given text names.
	 *
	 * @param text a class name, a menu path, a label, or the tail of any of them
	 * @return what was found
	 */
	public Result find(final String text) {
		if (text == null || text.isEmpty()) return new Result(List.of());

		// exactly what it is called
		List<CommandInfo> found = matching(c -> c.name().equals(text));
		if (!found.isEmpty()) return new Result(found);

		// exactly where it lives, or what it is labelled
		final String path = text.replace('/', '>');
		found = matching(c -> c.menuPath().map(p -> equalsLoosely(p, path)) //
			.orElse(false) || equalsLoosely(c.label(), text));
		if (!found.isEmpty()) return new Result(found);

		// NB: the tail of a class name, which is what a user of an inner class
		// would otherwise have to spell with a dollar sign.
		found = matching(c -> c.name().endsWith("." + text) || c.name().endsWith(
			"$" + text));
		if (!found.isEmpty()) return new Result(found);

		// anything that contains it
		return new Result(matching(c -> contains(c.label(), text) || //
			c.menuPath().map(p -> contains(p, text)).orElse(false)));
	}

	/** Gets the commands whose label or path contains the given text. */
	public List<CommandInfo> list(final String filter) {
		if (filter == null || filter.isEmpty()) return commands;
		return matching(c -> contains(c.label(), filter) || //
			c.menuPath().map(p -> contains(p, filter)).orElse(false) || //
			contains(c.name(), filter));
	}

	// -- Helper methods --

	private List<CommandInfo> matching(
		final java.util.function.Predicate<CommandInfo> test)
	{
		final List<CommandInfo> found = new ArrayList<>();
		for (final CommandInfo command : commands) {
			if (test.test(command)) found.add(command);
		}
		return found;
	}

	/** Compares ignoring case, spacing and a trailing ellipsis. */
	private static boolean equalsLoosely(final String a, final String b) {
		return normalize(a).equals(normalize(b));
	}

	private static boolean contains(final String haystack, final String needle) {
		return haystack != null && normalize(haystack).contains(normalize(needle));
	}

	private static String normalize(final String text) {
		return text == null ? "" : text.toLowerCase(Locale.ROOT) //
			.replace("...", "").replace(" ", "");
	}
}
