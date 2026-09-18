/*
 * #%L
 * AWT widgets and platform plumbing, with no Swing anywhere.
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

package org.scijava.awt;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.function.Consumer;

import org.scijava.command3.Accelerator;
import org.scijava.command3.CommandInfo;
import org.scijava.command3.MenuCreator;
import org.scijava.command3.MenuTree;
import org.scijava.command3.Menus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds an AWT menu bar from a {@link MenuTree}.
 *
 * @author Curtis Rueden
 */
public class AwtMenus implements MenuCreator<MenuBar, Menu> {

	private static final Logger log = LoggerFactory.getLogger(AwtMenus.class);

	private final Consumer<CommandInfo> onSelect;

	public AwtMenus(final Consumer<CommandInfo> onSelect) {
		this.onSelect = onSelect;
	}

	/** Builds a menu bar holding the given menu tree. */
	public static MenuBar create(final MenuTree root,
		final Consumer<CommandInfo> onSelect)
	{
		return Menus.build(root, new MenuBar(), new AwtMenus(onSelect));
	}

	@Override
	public Menu topMenu(final MenuTree node, final MenuBar bar) {
		final Menu menu = new Menu(node.label());
		bar.add(menu);
		return menu;
	}

	@Override
	public Menu subMenu(final MenuTree node, final Menu parent) {
		final Menu menu = new Menu(node.label());
		parent.add(menu);
		return menu;
	}

	@Override
	public void item(final MenuTree leaf, final Menu parent) {
		final CommandInfo command = leaf.entry().orElseThrow();
		final MenuItem item = new MenuItem(leaf.label());
		shortcut(command.accelerator().orElse(null)).ifPresent(item::setShortcut);
		// NB: AWT menu items have no icon at all, so iconPath is ignored here.
		item.addActionListener(e -> onSelect.accept(command));
		parent.add(item);
	}

	// -- Helper methods --

	/**
	 * Turns an accelerator into an AWT menu shortcut.
	 * <p>
	 * NB: {@link MenuShortcut} can express the platform's menu shortcut and
	 * shift, and nothing else - no alt, and no shortcut without the menu
	 * modifier. An accelerator AWT cannot say is dropped rather than
	 * approximated into a different key combination.
	 * </p>
	 */
	static Optional<MenuShortcut> shortcut(final String accelerator) {
		final Optional<Accelerator> parsed = Accelerator.parse(accelerator);
		if (parsed.isEmpty()) return Optional.empty();
		final Accelerator a = parsed.get();
		if (!a.isShortcut() || a.isAlt() || !a.isCharacter()) {
			if (a.hasModifiers()) {
				log.debug("AWT cannot express the accelerator {}", accelerator);
			}
			return Optional.empty();
		}
		final int key = KeyEvent.getExtendedKeyCodeForChar(a.key().charAt(0));
		return Optional.of(new MenuShortcut(key, a.isShift()));
	}
}
