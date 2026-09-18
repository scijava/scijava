/*
 * #%L
 * JavaFX widgets, and a dialog to harvest inputs with them.
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

package org.scijava.javafx;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;

import org.scijava.command3.Accelerator;
import org.scijava.command3.CommandInfo;
import org.scijava.command3.MenuCreator;
import org.scijava.command3.MenuTree;
import org.scijava.command3.Menus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a JavaFX menu bar from a {@link MenuTree}.
 *
 * @author Curtis Rueden
 */
public class FxMenus implements MenuCreator<MenuBar, Menu> {

	private static final Logger log = LoggerFactory.getLogger(FxMenus.class);

	private final Consumer<CommandInfo> onSelect;

	public FxMenus(final Consumer<CommandInfo> onSelect) {
		this.onSelect = onSelect;
	}

	/** Builds a menu bar holding the given menu tree. */
	public static MenuBar create(final MenuTree root,
		final Consumer<CommandInfo> onSelect)
	{
		return Menus.build(root, new MenuBar(), new FxMenus(onSelect));
	}

	@Override
	public Menu topMenu(final MenuTree node, final MenuBar bar) {
		final Menu menu = new Menu(node.label());
		bar.getMenus().add(menu);
		return menu;
	}

	@Override
	public Menu subMenu(final MenuTree node, final Menu parent) {
		final Menu menu = new Menu(node.label());
		parent.getItems().add(menu);
		return menu;
	}

	@Override
	public void item(final MenuTree leaf, final Menu parent) {
		final CommandInfo command = leaf.entry().orElseThrow();
		final MenuItem item = new MenuItem(leaf.label());
		command.accelerator().map(FxMenus::accelerator).ifPresent(
			item::setAccelerator);
		command.iconPath().map(this::icon).ifPresent(item::setGraphic);
		item.setOnAction(e -> onSelect.accept(command));
		parent.getItems().add(item);
	}

	// -- Helper methods --

	/**
	 * Turns an accelerator into a JavaFX key combination.
	 * <p>
	 * One with no modifiers is handed to
	 * {@link KeyCombination#keyCombination(String)}, so JavaFX's own notation
	 * ({@code "Ctrl+Shift+N"}) works too.
	 * </p>
	 *
	 * @return the combination, or null if it cannot be read
	 */
	static KeyCombination accelerator(final String accelerator) {
		final Optional<Accelerator> parsed = Accelerator.parse(accelerator);
		if (parsed.isEmpty()) return null;
		final Accelerator a = parsed.get();
		if (!a.hasModifiers()) {
			try {
				return KeyCombination.keyCombination(a.key());
			}
			catch (final IllegalArgumentException exc) {
				log.warn("Unreadable accelerator: {}", accelerator);
				return null;
			}
		}
		if (!a.isCharacter()) {
			log.warn("Unreadable accelerator: {}", accelerator);
			return null;
		}
		final List<Modifier> modifiers = new ArrayList<>();
		// NB: SHORTCUT_DOWN is JavaFX saying what Swing needs a Toolkit call for.
		if (a.isShortcut()) modifiers.add(KeyCombination.SHORTCUT_DOWN);
		if (a.isAlt()) modifiers.add(KeyCombination.ALT_DOWN);
		if (a.isShift()) modifiers.add(KeyCombination.SHIFT_DOWN);
		return new KeyCharacterCombination(a.key(), modifiers.toArray(
			new Modifier[0]));
	}

	private ImageView icon(final String path) {
		final java.net.URL url = getClass().getResource(path);
		if (url == null) {
			log.warn("No such icon: {}", path);
			return null;
		}
		return new ImageView(new Image(url.toExternalForm()));
	}
}
