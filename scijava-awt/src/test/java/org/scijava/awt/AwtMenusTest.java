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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.scijava.command.MenuTree;
import org.scijava.ui3.test.MenuConformance;

/**
 * Runs the shared menu conformance suite against the AWT menus.
 *
 * @author Curtis Rueden
 */
@DisabledIf("isHeadless")
public class AwtMenusTest extends MenuConformance<MenuBar> {

	static boolean isHeadless() {
		return GraphicsEnvironment.isHeadless();
	}

	@Override
	protected MenuBar menuBar(final MenuTree root) {
		return AwtMenus.create(root, command -> {});
	}

	@Override
	protected List<String> topMenus(final MenuBar bar) {
		final List<String> labels = new ArrayList<>();
		for (int i = 0; i < bar.getMenuCount(); i++)
			labels.add(bar.getMenu(i).getLabel());
		return labels;
	}

	@Override
	protected List<String> items(final MenuBar bar, final String... path) {
		Menu menu = menu(bar, path[0]);
		for (int i = 1; i < path.length; i++)
			menu = (Menu) item(menu, path[i]);
		final List<String> labels = new ArrayList<>();
		for (int i = 0; i < menu.getItemCount(); i++)
			labels.add(menu.getItem(i).getLabel());
		return labels;
	}

	/**
	 * An accelerator AWT can express reaches the item; one it cannot is
	 * dropped rather than approximated into some other key combination.
	 */
	@Test
	public void testShortcutsWithinAwtsMeans() {
		final Optional<MenuShortcut> open = AwtMenus.shortcut("^O");
		assertTrue(open.isPresent());
		assertEquals(new MenuShortcut(KeyEvent.VK_O, false), open.get());

		final Optional<MenuShortcut> shifted = AwtMenus.shortcut("^+C");
		assertTrue(shifted.isPresent());
		assertEquals(new MenuShortcut(KeyEvent.VK_C, true), shifted.get());

		// NB: MenuShortcut has no alt, and no way to say "no menu modifier".
		assertFalse(AwtMenus.shortcut("!N").isPresent());
		assertFalse(AwtMenus.shortcut("F5").isPresent());
		assertFalse(AwtMenus.shortcut("").isPresent());

		final MenuItem openItem = item(menu(menuBar(), "File"), "Open...");
		assertEquals(new MenuShortcut(KeyEvent.VK_O, false), openItem
			.getShortcut());
	}

	// -- Helper methods --

	private static Menu menu(final MenuBar bar, final String label) {
		for (int i = 0; i < bar.getMenuCount(); i++) {
			if (bar.getMenu(i).getLabel().equals(label)) return bar.getMenu(i);
		}
		throw new AssertionError("No such menu: " + label);
	}

	private static MenuItem item(final Menu menu, final String label) {
		for (int i = 0; i < menu.getItemCount(); i++) {
			if (menu.getItem(i).getLabel().equals(label)) return menu.getItem(i);
		}
		throw new AssertionError("No such item: " + label);
	}
}
