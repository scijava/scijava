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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.context.Context;
import org.scijava.discovery.Discovery;
import org.scijava.index.Index;
import org.scijava.index.IndexItem;

/**
 * Finds the commands available to an application.
 * <p>
 * Commands are declared with {@code @Plugin(type = Command.class)} and, where
 * they belong in a menu, {@link Menu}. Those are two separate indexes, joined
 * here on the implementation class name - so listing every command, with its
 * menu path, label, icon and accelerator, loads <strong>no command
 * classes</strong>.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class Commands {

	private Commands() {
		// NB: prevent instantiation of utility class.
	}

	/**
	 * Lists the commands available to the given context.
	 *
	 * @param context the application context
	 * @return the commands, ordered by menu weight and then by label
	 */
	public static List<CommandInfo> discover(final Context context) {
		final Map<String, Map<String, String>> menus = menusByClassName( //
			Thread.currentThread().getContextClassLoader());

		final List<CommandInfo> commands = new ArrayList<>();
		for (final Discovery<Command> discovery : context.plugins(Command.class)) {
			commands.add(new CommandInfo(discovery, menus.getOrDefault( //
				discovery.implClassName(), Map.of())));
		}
		commands.sort(Comparator.comparingDouble(CommandInfo::weight) //
			.thenComparing(CommandInfo::label));
		return commands;
	}

	/**
	 * Reads the {@link Menu} index, keyed by implementation class name.
	 * <p>
	 * NB: this is the join that lets presentation metadata stack onto a plugin
	 * declaration instead of living inside it. Each annotation has its own
	 * index; both are keyed by the class they annotate.
	 * </p>
	 */
	private static Map<String, Map<String, String>> menusByClassName(
		final ClassLoader classLoader)
	{
		final Map<String, Map<String, String>> menus = new HashMap<>();
		for (final IndexItem<Menu> item : Index.load(Menu.class, classLoader)) {
			final Menu menu = item.annotation();
			final Map<String, String> attrs = new HashMap<>();
			attrs.put("path", menu.path());
			attrs.put("weight", Double.toString(menu.weight()));
			attrs.put("accelerator", menu.accelerator());
			attrs.put("iconPath", menu.iconPath());
			attrs.put("selectable", Boolean.toString(menu.selectable()));
			attrs.put("selectionGroup", menu.selectionGroup());
			attrs.put("visible", Boolean.toString(menu.visible()));
			menus.put(item.className(), attrs);
		}
		return menus;
	}
}
