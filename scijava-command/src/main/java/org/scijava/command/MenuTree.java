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
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A tree of menu entries, built from the menu paths of commands.
 * <p>
 * This is a model, not a widget: it knows about labels, order, shortcuts and
 * icons, and nothing about Swing, JavaFX or the platform. A user interface
 * walks it and builds whatever its toolkit calls a menu.
 * </p>
 * <p>
 * Building one loads no command classes. Every entry here comes from the
 * annotation index, and the command behind a leaf is loaded when somebody
 * actually runs it.
 * </p>
 * <p>
 * NB: SciJava Common called this {@code ShadowMenu}, which also implemented
 * {@code Collection<ModuleInfo>}, {@code Runnable} and {@code Comparable} and
 * carried a context. Here it is a tree and nothing else; running a leaf is the
 * caller's business, through {@code Runner}.
 * </p>
 *
 * @author Curtis Rueden
 */
public class MenuTree {

	/** The separator between elements of a menu path. */
	public static final String SEPARATOR = ">";

	private final String label;
	private final CommandInfo command;
	private final Map<String, MenuTree> children = new LinkedHashMap<>();

	private MenuTree(final String label, final CommandInfo command) {
		this.label = label;
		this.command = command;
	}

	/**
	 * Builds a menu tree from the given commands.
	 * <p>
	 * Commands with no menu path, and those marked not visible, are left out:
	 * they remain perfectly runnable, they simply do not appear.
	 * </p>
	 *
	 * @param commands the commands to arrange
	 * @return the root of the tree, whose children are the top-level menus
	 */
	public static MenuTree of(final Collection<CommandInfo> commands) {
		final MenuTree root = new MenuTree("", null);
		for (final CommandInfo command : commands) {
			if (!command.isVisible()) continue;
			final Optional<String> path = command.menuPath();
			if (path.isEmpty()) continue;
			root.add(command, path.get().split(SEPARATOR));
		}
		root.sort();
		return root;
	}

	/** Gets this entry's label. Empty for the root. */
	public String label() {
		return label;
	}

	/** Gets the command this entry runs, if it is a leaf. */
	public Optional<CommandInfo> command() {
		return Optional.ofNullable(command);
	}

	/** Gets whether this entry runs a command, rather than holding others. */
	public boolean isLeaf() {
		return command != null;
	}

	/** Gets the entries directly beneath this one, in display order. */
	public List<MenuTree> children() {
		return List.copyOf(children.values());
	}

	/** Gets the child with the given label, if there is one. */
	public Optional<MenuTree> child(final String label) {
		return Optional.ofNullable(children.get(label));
	}

	/**
	 * Gets the entry at the given path, if there is one.
	 *
	 * @param path a {@code >}-separated path, as {@link Menu#path()} uses
	 */
	public Optional<MenuTree> find(final String path) {
		MenuTree current = this;
		for (final String element : path.split(SEPARATOR)) {
			final Optional<MenuTree> child = current.child(element.trim());
			if (child.isEmpty()) return Optional.empty();
			current = child.get();
		}
		return Optional.of(current);
	}

	/** Gets every command in this subtree, in display order. */
	public List<CommandInfo> leaves() {
		final List<CommandInfo> leaves = new ArrayList<>();
		collectLeaves(leaves);
		return leaves;
	}

	@Override
	public String toString() {
		return label + (isLeaf() ? "" : children.keySet());
	}

	// -- Helper methods --

	private void add(final CommandInfo command, final String[] path) {
		add(command, path, 0);
	}

	private void add(final CommandInfo command, final String[] path,
		final int depth)
	{
		final String element = path[depth].trim();
		final boolean last = depth == path.length - 1;
		if (last) {
			// NB: two commands may claim one path; the first wins, so that an
			// unlucky collision does not silently discard the earlier entry.
			children.putIfAbsent(element, new MenuTree(element, command));
			return;
		}
		final MenuTree child = children.computeIfAbsent(element, //
			l -> new MenuTree(l, null));
		child.add(command, path, depth + 1);
	}

	/** Orders each level by weight, then by label. */
	private void sort() {
		final List<MenuTree> sorted = new ArrayList<>(children.values());
		sorted.sort(Comparator.comparingDouble(MenuTree::weight) //
			.thenComparing(MenuTree::label));
		children.clear();
		for (final MenuTree child : sorted) {
			children.put(child.label(), child);
			child.sort();
		}
	}

	/**
	 * Gets the weight this entry sorts by: a leaf's own, or the lightest of the
	 * entries beneath it.
	 * <p>
	 * NB: a submenu has no weight of its own, so it takes the position of its
	 * most prominent child. Otherwise every submenu would sort equally and fall
	 * back to alphabetical order.
	 * </p>
	 */
	private double weight() {
		if (isLeaf()) return command.weight();
		return children.values().stream() //
			.mapToDouble(MenuTree::weight) //
			.min().orElse(Double.POSITIVE_INFINITY);
	}

	private void collectLeaves(final List<CommandInfo> leaves) {
		if (isLeaf()) {
			leaves.add(command);
			return;
		}
		for (final MenuTree child : children.values())
			child.collectLeaves(leaves);
	}
}
