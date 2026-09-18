/*
 * #%L
 * Swing widgets, and a dialog to harvest inputs with them.
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

package org.scijava.swing;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.scijava.command.Accelerator;
import org.scijava.command.ExecutableInfo;
import org.scijava.command.MenuCreator;
import org.scijava.command.MenuTree;
import org.scijava.command.Menus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a Swing menu bar from a {@link MenuTree}.
 * <p>
 * Building one loads no command classes: every label, accelerator and icon
 * here came out of the annotation index, and the command behind an item is
 * loaded when somebody chooses it.
 * </p>
 *
 * @author Curtis Rueden
 */
public class SwingMenus implements MenuCreator<JMenuBar, JMenu> {

	private static final Logger log = LoggerFactory.getLogger(SwingMenus.class);

	private final Consumer<ExecutableInfo> onSelect;

	/**
	 * @param onSelect what to do with the command the user chose. It is called
	 *          on the event dispatch thread, so it should hand the work
	 *          elsewhere rather than run it there.
	 */
	public SwingMenus(final Consumer<ExecutableInfo> onSelect) {
		this.onSelect = onSelect;
	}

	/** Builds a menu bar holding the given menu tree. */
	public static JMenuBar create(final MenuTree root,
		final Consumer<ExecutableInfo> onSelect)
	{
		return Menus.build(root, new JMenuBar(), new SwingMenus(onSelect));
	}

	@Override
	public JMenu topMenu(final MenuTree node, final JMenuBar bar) {
		final JMenu menu = new JMenu(node.label());
		bar.add(menu);
		return menu;
	}

	@Override
	public JMenu subMenu(final MenuTree node, final JMenu parent) {
		final JMenu menu = new JMenu(node.label());
		parent.add(menu);
		return menu;
	}

	@Override
	public void item(final MenuTree leaf, final JMenu parent) {
		final ExecutableInfo command = leaf.entry().orElseThrow();
		final JMenuItem item = new JMenuItem(leaf.label());
		command.accelerator().map(SwingMenus::keyStroke).ifPresent(
			item::setAccelerator);
		command.iconPath().map(this::icon).ifPresent(item::setIcon);
		item.addActionListener(e -> onSelect.accept(command));
		parent.add(item);
	}

	// -- Helper methods --

	/**
	 * Turns an accelerator into a Swing keystroke.
	 * <p>
	 * A shortcut with no modifiers is handed to
	 * {@link KeyStroke#getKeyStroke(String)}, so Swing's own notation
	 * ({@code "control shift N"}) works too.
	 * </p>
	 *
	 * @return the keystroke, or null if it cannot be read
	 */
	static KeyStroke keyStroke(final String accelerator) {
		final Optional<Accelerator> parsed = Accelerator.parse(accelerator);
		if (parsed.isEmpty()) return null;
		final Accelerator a = parsed.get();
		if (!a.hasModifiers()) return KeyStroke.getKeyStroke(a.key());
		if (!a.isCharacter()) {
			log.warn("Unreadable accelerator: {}", accelerator);
			return null;
		}
		int modifiers = 0;
		if (a.isShortcut()) modifiers |= Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMaskEx();
		if (a.isAlt()) modifiers |= InputEvent.ALT_DOWN_MASK;
		if (a.isShift()) modifiers |= InputEvent.SHIFT_DOWN_MASK;
		return KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar(a.key()
			.charAt(0)), modifiers);
	}

	private ImageIcon icon(final String path) {
		// NB: the icon is a resource of whichever component declared the command,
		// so a missing one is that component's problem and not worth failing the
		// whole menu over.
		final URL url = getClass().getResource(path);
		if (url == null) {
			log.warn("No such icon: {}", path);
			return null;
		}
		return new ImageIcon(url);
	}
}
