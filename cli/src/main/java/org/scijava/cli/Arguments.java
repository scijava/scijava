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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A command line, taken apart.
 * <p>
 * The grammar is the one every command line has: words, and options that may
 * carry a value.
 * </p>
 *
 * <pre>
 * scijava Gaussian Blur --sigma 2.5 --edges Wrap
 * scijava blur.groovy --sigma=2.5
 * scijava --list filters
 * </pre>
 * <p>
 * NB: an option's value may follow it or be joined with {@code =}, and an
 * option with no value is {@code true} - which is what lets a boolean
 * parameter be a bare flag. Nothing here knows what any of the options mean;
 * {@code scijava-convert3} turns the strings into whatever the parameters
 * declared, exactly as it does for a script or a dialog.
 * </p>
 *
 * @author Curtis Rueden
 */
public class Arguments {

	private final List<String> words = new ArrayList<>();
	private final Map<String, String> options = new LinkedHashMap<>();

	/** Takes apart a command line. */
	public static Arguments parse(final String... args) {
		final Arguments parsed = new Arguments();
		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			if (!arg.startsWith("--")) {
				parsed.words.add(arg);
				continue;
			}
			final String option = arg.substring(2);
			final int equals = option.indexOf('=');
			if (equals >= 0) {
				parsed.options.put(option.substring(0, equals), option.substring(
					equals + 1));
				continue;
			}
			// NB: the next argument is this option's value, unless it is itself an
			// option or there is nothing left -- in which case the option is a flag.
			if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
				parsed.options.put(option, args[++i]);
			}
			else parsed.options.put(option, "true");
		}
		return parsed;
	}

	/** Gets the words: what to run, and anything else not an option. */
	public List<String> words() {
		return List.copyOf(words);
	}

	/** Gets the options, in the order they were given. */
	public Map<String, String> options() {
		return Map.copyOf(options);
	}

	/** Gets whether the given option was given. */
	public boolean has(final String name) {
		return options.containsKey(name);
	}

	/** Gets an option's value, or the given default if it was not given. */
	public String get(final String name, final String defaultValue) {
		return options.getOrDefault(name, defaultValue);
	}

	/** Removes an option, so that what remains is the parameters. */
	public String take(final String name) {
		return options.remove(name);
	}

	/** Gets the words joined by spaces: a menu path, a label, a class name. */
	public String identifier() {
		return String.join(" ", words);
	}
}
