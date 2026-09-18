/*
 * #%L
 * Discoverable commands, with the metadata a menu is built from.
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

package org.scijava.command;

import java.util.Map;
import java.util.Optional;

import org.scijava.execute.Executable;
import org.scijava.execute.ExecutableInstance;
import org.scijava.struct.Struct;

/**
 * A command: something a user can run, and what is needed to present it.
 * <p>
 * This is the word's whole meaning here. A {@link ClassCommandInfo} is a
 * command whose implementation is a class, read from the annotation index; a
 * script is a command whose implementation is source in some language, and
 * says the same things in its {@code #@script} directives; a SciJava Common
 * {@code ModuleInfo} will be a command through the legacy bridge. None is a
 * special case of the others, and an application gathers them from as many
 * sources as it has without anything downstream learning there was more than
 * one.
 * </p>
 * <p>
 * NB: SciJava Common called this a <em>module</em>, and had to, because
 * "command" there meant the Java-class kind specifically. Naming the general
 * thing after what users actually call it costs one word - the class-based
 * kind is a {@code ClassCommandInfo} - and retires a vocabulary nobody
 * enjoyed.
 * </p>
 * <p>
 * NB: <strong>a menu path is optional, and this is not a menu entry.</strong>
 * Plenty of runnable things belong in no menu: a command invoked by name from
 * a script, one reached only through a search bar, one that exists to be
 * called by something else. {@link MenuTree} takes the subset that has a path,
 * and the rest stay perfectly runnable - which is why this is named for what
 * it is rather than for the one place it is most often shown.
 * </p>
 * <p>
 * NB: it is itself an {@link Executable}, so launching one needs no lookup:
 * what the menu - or the search bar, or the command line - holds is the thing
 * to run.
 * </p>
 *
 * @author Curtis Rueden
 */
public interface CommandInfo extends Executable {

	/**
	 * Gets where this sits in the menus, if anywhere.
	 * <p>
	 * Empty means it appears in no menu, which is an ordinary thing to be.
	 * </p>
	 */
	Optional<String> menuPath();

	/** Gets the label to display. */
	String label();

	/** Gets the keyboard shortcut, if there is one. */
	default Optional<String> accelerator() {
		return Optional.empty();
	}

	/** Gets the icon resource, if there is one. */
	default Optional<String> iconPath() {
		return Optional.empty();
	}

	/** Gets how this sorts among its menu siblings; lower comes first. */
	default double weight() {
		return Double.POSITIVE_INFINITY;
	}

	/** Gets whether this should appear in menus at all. */
	default boolean isVisible() {
		return true;
	}

	/**
	 * Describes anything runnable, from metadata written the way a
	 * {@code @Menu} annotation writes it.
	 * <p>
	 * This is what a script's {@code #@script(...)} directives become, and what
	 * any other source of runnable things can produce without knowing about
	 * commands at all. Metadata it does not carry - a menu path included - is
	 * simply absent.
	 * </p>
	 *
	 * @param executable the thing to run - a script, typically
	 * @param attrs its presentation metadata: {@code menu} or {@code path},
	 *          {@code label}, {@code accelerator}, {@code iconPath},
	 *          {@code weight}, {@code visible}. All of it is optional,
	 *          including the menu path
	 * @return the description
	 */
	static CommandInfo of(final Executable executable,
		final Map<String, String> attrs)
	{
		return new CommandInfo() {

			@Override
			public Optional<String> menuPath() {
				// NB: `menu` is what a script writer types; `path` is what the
				// annotation calls it. Both mean the same thing, and insisting on
				// one of them would only be a way to be unhelpful.
				return attr("menu").or(() -> attr("path"));
			}

			@Override
			public String label() {
				return attr("label") //
					.or(() -> menuPath().map(p -> p.substring(p.lastIndexOf('>') + 1))) //
					.orElseGet(executable::name);
			}

			@Override
			public Optional<String> accelerator() {
				return attr("accelerator");
			}

			@Override
			public Optional<String> iconPath() {
				return attr("iconPath");
			}

			@Override
			public double weight() {
				return attr("weight").map(Double::parseDouble) //
					.orElse(Double.POSITIVE_INFINITY);
			}

			@Override
			public boolean isVisible() {
				return !"false".equals(attrs.get("visible"));
			}

			@Override
			public String name() {
				return attr("name").orElseGet(executable::name);
			}

			@Override
			public Struct struct() {
				return executable.struct();
			}

			@Override
			public ExecutableInstance create() {
				return executable.create();
			}

			@Override
			public String toString() {
				return name() + menuPath().map(p -> " [" + p + "]").orElse("");
			}

			private Optional<String> attr(final String key) {
				final String value = attrs.get(key);
				return value == null || value.isEmpty() ? Optional.empty() //
					: Optional.of(value);
			}
		};
	}
}
