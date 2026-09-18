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

/**
 * Builds a toolkit's menus from a {@link MenuTree}.
 * <p>
 * The walk itself is the same everywhere - descend the tree, make a menu for
 * each branch and an item for each leaf - so {@link Menus} does it, and a
 * toolkit implements only the four things it alone can do. What an item
 * <em>does</em> when chosen is the implementation's business, typically a
 * callback it was constructed with.
 * </p>
 * <p>
 * NB: a menu bar and a menu are different types in every toolkit worth
 * supporting, which is why there are two type parameters and why adding to the
 * bar is separate from adding to a menu.
 * </p>
 *
 * @param <B> the toolkit's menu bar
 * @param <M> the toolkit's menu
 * @author Curtis Rueden
 */
public interface MenuCreator<B, M> {

	/** Adds a top-level menu to the bar. */
	M topMenu(MenuTree node, B bar);

	/** Adds a menu inside another menu. */
	M subMenu(MenuTree node, M parent);

	/** Adds a command to a menu. */
	void item(MenuTree leaf, M parent);

	/**
	 * Adds a command directly to the bar, for a one-element menu path.
	 * <p>
	 * Rare, and some toolkits cannot do it at all; the default puts the command
	 * in a menu of its own name rather than losing it.
	 * </p>
	 */
	default void topItem(final MenuTree leaf, final B bar) {
		item(leaf, topMenu(leaf, bar));
	}
}
