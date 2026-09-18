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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.scijava.command.CommandInfo;
import org.scijava.command.Commands;
import org.scijava.command.MenuTree;
import org.scijava.context.Context;

/**
 * Tests that the menu bar matches the commands the index describes.
 *
 * @author Curtis Rueden
 */
@DisabledIf("isHeadless")
public class SwingMenusTest {

	static boolean isHeadless() {
		return GraphicsEnvironment.isHeadless();
	}

	/** The menus come out in the declared order, not alphabetically. */
	@Test
	public void testMenuOrder() {
		final JMenuBar bar = menuBar();
		assertEquals(List.of("File", "Image", "Process", "Analyze", "Help"),
			labels(bar));
	}

	/** A menu path becomes menus, however deep, and a command becomes an item. */
	@Test
	public void testNesting() {
		final JMenuBar bar = menuBar();
		final JMenu image = menu(bar, "Image");
		assertEquals(List.of("Adjust"), labels(image));
		final JMenu adjust = (JMenu) image.getItem(0);
		assertEquals(List.of("Brightness/Contrast..."), labels(adjust));
	}

	/** A command with no menu path is absent from the menus, not lost. */
	@Test
	public void testCommandWithNoMenuPath() {
		final List<CommandInfo> commands = commands();
		assertTrue(commands.stream().anyMatch(c -> c.className().endsWith(
			"ShellCommands$Hidden")), "the hidden command should be discovered");
		assertEquals(List.of("About..."), labels(menu(menuBar(), "Help")));
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

		final JMenuItem open = (JMenuItem) menu(menuBar(), "File").getItem(0);
		assertNotNull(open.getAccelerator());
	}

	// -- Helper methods --

	private static List<CommandInfo> commands() {
		try (Context context = Context.create()) {
			return Commands.discover(context);
		}
	}

	private static JMenuBar menuBar() {
		// NB: the commands under test are the shell's; anything else another
		// test contributes would only make this brittle.
		final List<CommandInfo> mine = commands().stream() //
			.filter(c -> c.className().contains("ShellCommands")) //
			.collect(Collectors.toList());
		return SwingMenus.create(MenuTree.of(mine), command -> {});
	}

	private static JMenu menu(final JMenuBar bar, final String label) {
		for (int i = 0; i < bar.getMenuCount(); i++) {
			if (bar.getMenu(i).getText().equals(label)) return bar.getMenu(i);
		}
		throw new AssertionError("No such menu: " + label);
	}

	private static List<String> labels(final JMenuBar bar) {
		final List<String> labels = new ArrayList<>();
		for (int i = 0; i < bar.getMenuCount(); i++)
			labels.add(bar.getMenu(i).getText());
		return labels;
	}

	private static List<String> labels(final JMenu menu) {
		final List<String> labels = new ArrayList<>();
		for (int i = 0; i < menu.getItemCount(); i++)
			labels.add(menu.getItem(i).getText());
		return labels;
	}
}
