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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.scijava.context.Context;

/**
 * Tests {@link MenuTree}.
 *
 * @author Curtis Rueden
 */
public class MenuTreeTest {

	@Test
	public void testTreeStructure() {
		try (final Context context = Context.create()) {
			final MenuTree root = MenuTree.of(Commands.discover(context));

			// Top-level menus.
			final List<String> menus = labels(root);
			assertTrue(menus.contains("Image"), menus.toString());
			assertTrue(menus.contains("Help"), menus.toString());
			assertTrue(menus.contains("Analyze"), menus.toString());

			// A nested path becomes nested entries.
			final MenuTree adjust = root.find("Image>Adjust").orElseThrow();
			assertFalse(adjust.isLeaf());
			assertEquals(List.of("Brightness/Contrast..."), labels(adjust));

			final MenuTree leaf = root.find("Image>Adjust>Brightness/Contrast...")
				.orElseThrow();
			assertTrue(leaf.isLeaf());
			assertEquals(DeepCommand.class.getName(), leaf.entry().orElseThrow()
				.name());
		}
	}

	/** Entries sort by weight; a submenu takes the weight of its lightest child. */
	@Test
	public void testOrdering() {
		try (final Context context = Context.create()) {
			final MenuTree root = MenuTree.of(Commands.discover(context));
			final List<String> menus = labels(root);

			// Analyze holds CountBeans (3), Image holds DeepCommand (5), and
			// Help's lightest is SayHello (12).
			assertEquals(List.of("Analyze", "Image", "Help"), menus);

			// Within Help: Say Hello... (12) before Never Run (50).
			assertEquals(List.of("Say Hello...", "Never Run"), labels(root.find(
				"Help").orElseThrow()));
		}
	}

	/** A command asking not to be shown is absent, though still runnable. */
	@Test
	public void testInvisibleCommandIsNotInTheTree() {
		try (final Context context = Context.create()) {
			final MenuTree root = MenuTree.of(Commands.discover(context));
			assertTrue(root.find("Help>Should Not Appear").isEmpty());

			// ...but it was discovered, and could be run directly.
			assertTrue(Commands.discover(context).stream() //
				.anyMatch(c -> HiddenCommand.class.getName().equals(c.className())));
		}
	}

	/** A command with no menu path simply does not appear. */
	@Test
	public void testCommandWithoutMenuPathIsNotInTheTree() {
		try (final Context context = Context.create()) {
			final MenuTree root = MenuTree.of(Commands.discover(context));
			final List<String> classNames = root.leaves().stream() //
				.map(ExecutableInfo::name) //
				.collect(Collectors.toList());
			assertFalse(classNames.contains(Headless.class.getName()));
		}
	}

	/** Building the whole tree loads no command classes. */
	@Test
	public void testTreeIsBuiltFromMetadataAlone() {
		try (final Context context = Context.create()) {
			final MenuTree root = MenuTree.of(Commands.discover(context));
			final MenuTree never = root.find("Help>Never Run").orElseThrow();
			assertEquals("Never Run", never.label());
			assertFalse(constructed("org.scijava.command.NeverRun"),
				"building a menu tree must not construct commands");
		}
	}

	private static List<String> labels(final MenuTree tree) {
		return tree.children().stream() //
			.map(MenuTree::label) //
			.collect(Collectors.toList());
	}

	private static boolean constructed(final String className) {
		try {
			return Class.forName(className).getField("constructed").getBoolean(null);
		}
		catch (final ReflectiveOperationException exc) {
			throw new AssertionError(exc);
		}
	}
}
