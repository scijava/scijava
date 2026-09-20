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

package org.scijava.command3;

/**
 * Walks a {@link MenuTree} into a toolkit's menus.
 *
 * @author Curtis Rueden
 */
public final class Menus {

	private Menus() {
		// prevent instantiation of utility class
	}

	/**
	 * Builds the given menu tree into the given menu bar.
	 *
	 * @param root the tree to build, as {@link MenuTree#of} returns
	 * @param bar the toolkit's menu bar, which is filled in
	 * @param creator makes the toolkit's menus and items
	 * @return the menu bar, for chaining
	 */
	public static <B, M> B build(final MenuTree root, final B bar,
		final MenuCreator<B, M> creator)
	{
		for (final MenuTree child : root.children()) {
			if (child.isLeaf()) creator.topItem(child, bar);
			else fill(child, creator.topMenu(child, bar), creator);
		}
		return bar;
	}

	// -- Helper methods --

	private static <B, M> void fill(final MenuTree node, final M menu,
		final MenuCreator<B, M> creator)
	{
		for (final MenuTree child : node.children()) {
			if (child.isLeaf()) creator.item(child, menu);
			else fill(child, creator.subMenu(child, menu), creator);
		}
	}
}
