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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.scijava.command.ExecutableInfo;
import org.scijava.command.MenuTree;
import org.scijava.script3.ScriptFinder;

/**
 * Tests that scripts reach the menus the same way commands do.
 * <p>
 * This is what a menu built from several sources looks like: an annotated Java
 * class and a Groovy file in a directory arrive as the same kind of thing, and
 * the menu cannot tell them apart.
 * </p>
 *
 * @author Curtis Rueden
 */
@DisabledIf("isHeadless")
public class ScriptMenuTest {

	static boolean isHeadless() {
		return GraphicsEnvironment.isHeadless();
	}

	/** A script's declared menu path puts it where it asked to be. */
	@Test
	public void testDeclaredMenuPath() {
		final JMenuBar bar = menuBar();
		final JMenuItem item = item(menu(bar, "Help"), "Say Hello From A Script");
		assertNotNull(item, "the script declared this menu path itself");
		assertNotNull(item.getAccelerator(), "and this accelerator");
	}

	/** A script that declares nothing is placed by the directory it sits in. */
	@Test
	public void testDirectoryDerivedMenuPath() {
		final JMenu filters = (JMenu) item(menu(menuBar(), "Process"), "Filters");
		final List<String> labels = new ArrayList<>();
		for (int i = 0; i < filters.getItemCount(); i++)
			labels.add(filters.getItem(i).getText());

		// NB: the Java command and the script, side by side, in weight order.
		assertTrue(labels.contains("Gaussian Blur..."), labels.toString());
		assertTrue(labels.contains("Blur It"), labels.toString());
	}

	/** What the menu holds is the thing to run, script or command alike. */
	@Test
	public void testScriptRunsFromItsMenuEntry() {
		final ExecutableInfo hello = MenuTree.of(entries()) //
			.find("Help>Say Hello From A Script").orElseThrow() //
			.entry().orElseThrow();

		final var instance = hello.create();
		instance.run();

		assertEquals("hello world", instance.parameters().member("greeting")
			.get());
	}

	// -- Helper methods --

	private static List<ExecutableInfo> entries() {
		final List<ExecutableInfo> entries = new ArrayList<>();
		try (final org.scijava.context.Context context = //
			org.scijava.context.Context.create())
		{
			org.scijava.command.Commands.discover(context).stream() //
				.filter(c -> c.className().contains("SampleCommands")) //
				.forEach(entries::add);
		}
		new ScriptFinder().find(scripts()).forEach(found -> entries.add(ExecutableInfo
			.of(found.script(), found.metadata())));
		return entries;
	}

	private static Path scripts() {
		try {
			return Paths.get(ScriptMenuTest.class.getResource("/scripts").toURI());
		}
		catch (final URISyntaxException exc) {
			throw new IllegalStateException(exc);
		}
	}

	private static JMenuBar menuBar() {
		return SwingMenus.create(MenuTree.of(entries()), entry -> {});
	}

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
		throw new AssertionError("No such item: " + label + " in " + menu
			.getText());
	}
}
