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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.scijava.command3.MenuTree;
import org.scijava.ui3.test.MenuConformance;

/**
 * Runs the shared menu conformance suite against the Swing menus, and adds
 * what is Swing's own.
 *
 * @author Curtis Rueden
 */
@DisabledIf("isHeadless")
public class SwingMenusTest extends MenuConformance<JMenuBar> {

	static boolean isHeadless() {
		return GraphicsEnvironment.isHeadless();
	}

	@Override
	protected JMenuBar menuBar(final MenuTree root) {
		return SwingMenus.create(root, command -> {});
	}

	@Override
	protected List<String> topMenus(final JMenuBar bar) {
		final List<String> labels = new ArrayList<>();
		for (int i = 0; i < bar.getMenuCount(); i++)
			labels.add(bar.getMenu(i).getText());
		return labels;
	}

	@Override
	protected List<String> items(final JMenuBar bar, final String... path) {
		JMenu menu = menu(bar, path[0]);
		for (int i = 1; i < path.length; i++)
			menu = (JMenu) item(menu, path[i]);
		final List<String> labels = new ArrayList<>();
		for (int i = 0; i < menu.getItemCount(); i++)
			labels.add(menu.getItem(i).getText());
		return labels;
	}

	/** Accelerators reach the items, in ImageJ's notation and in Swing's. */
	@Test
	public void testAccelerators() {
		final int shortcut = Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMaskEx();
		assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcut), SwingMenus
			.keyStroke("^O"));
		assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcut |
			InputEvent.SHIFT_DOWN_MASK), SwingMenus.keyStroke("^+C"));
		assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK),
			SwingMenus.keyStroke("!N"));
		assertEquals(KeyStroke.getKeyStroke("control shift N"), SwingMenus.keyStroke(
			"control shift N"));
		assertNull(SwingMenus.keyStroke(""));

		final JMenuItem open = (JMenuItem) item(menu(menuBar(), "File"),
			"Open...");
		assertNotNull(open.getAccelerator());
	}

	// -- Helper methods --

	private static JMenu menu(final JMenuBar bar, final String label) {
		for (int i = 0; i < bar.getMenuCount(); i++) {
			if (bar.getMenu(i).getText().equals(label)) return bar.getMenu(i);
		}
		throw new AssertionError("No such menu: " + label);
	}

	private static JMenuItem item(final JMenu menu, final String label) {
		for (int i = 0; i < menu.getItemCount(); i++) {
			if (menu.getItem(i).getText().equals(label)) return menu.getItem(i);
		}
		throw new AssertionError("No such item: " + label);
	}
}
