/*
 * #%L
 * Sample commands and conformance tests, for every toolkit binding.
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

package org.scijava.ui3.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.scijava.command.ClassCommandInfo;
import org.scijava.command.Commands;
import org.scijava.command.MenuTree;
import org.scijava.context.Context;

/**
 * What every toolkit's menus must do, asserted once.
 *
 * @param <B> the toolkit's menu bar
 * @author Curtis Rueden
 */
public abstract class MenuConformance<B> {

	/** Builds this toolkit's menu bar from the given tree. */
	protected abstract B menuBar(MenuTree root);

	/** Gets the labels of the bar's top-level menus, in display order. */
	protected abstract List<String> topMenus(B bar);

	/**
	 * Gets the labels inside the menu at the given path, in display order.
	 *
	 * @param bar the menu bar to look in
	 * @param path the labels to descend through, e.g. {@code "Image", "Adjust"}
	 */
	protected abstract List<String> items(B bar, String... path);

	/** Menus come out in the declared order, not alphabetically. */
	@Test
	public void testMenuOrder() {
		assertEquals(List.of("File", "Image", "Process", "Analyze", "Help"),
			topMenus(menuBar()));
	}

	/** A menu path becomes menus, however deep, and a command an item. */
	@Test
	public void testNesting() {
		final B bar = menuBar();
		assertEquals(List.of("Adjust"), items(bar, "Image"));
		assertEquals(List.of("Brightness/Contrast..."), items(bar, "Image",
			"Adjust"));
		assertEquals(List.of("Open...", "Quit"), items(bar, "File"));
	}

	/** A command with no menu path is absent from the menus, not lost. */
	@Test
	public void testCommandWithNoMenuPath() {
		assertTrue(commands().stream().anyMatch(c -> c.className().endsWith(
			"SampleCommands$Hidden")), "the hidden command should be discovered");
		assertEquals(List.of("About..."), items(menuBar(), "Help"));
	}

	// -- Internal methods --

	/** Builds a bar holding the sample commands, and nothing else. */
	protected B menuBar() {
		// NB: only the sample commands, so that another component contributing
		// one of its own does not make these assertions brittle.
		final List<ClassCommandInfo> mine = commands().stream() //
			.filter(c -> c.className().contains("SampleCommands")) //
			.collect(Collectors.toList());
		return menuBar(MenuTree.of(mine));
	}

	/** Discovers the commands, as an application would. */
	protected static List<ClassCommandInfo> commands() {
		try (Context context = Context.create()) {
			return Commands.discover(context);
		}
	}
}
