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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.scijava.context.Context;
import org.scijava.execute.ExecutionResult;
import org.scijava.execute.Runner;

/**
 * Tests {@link Commands}: discovery, the metadata a menu is built from, and
 * running what was found.
 *
 * @author Curtis Rueden
 */
public class CommandsTest {

	@Test
	public void testDiscovery() {
		try (final Context context = Context.create()) {
			final List<String> names = Commands.discover(context).stream() //
				.map(ClassCommandInfo::className) //
				.collect(Collectors.toList());
			assertTrue(names.contains(SayHello.class.getName()), names.toString());
			assertTrue(names.contains(CountBeans.class.getName()), names.toString());
			assertTrue(names.contains(Headless.class.getName()), names.toString());
		}
	}

	/**
	 * The point of the two-index join: a menu is built from metadata alone,
	 * with no command class loaded.
	 */
	@Test
	public void testMenuMetadataWithoutLoadingCommands() {
		try (final Context context = Context.create()) {
			final ClassCommandInfo hello = find(context, SayHello.class);

			assertEquals("Help>Say Hello...", hello.menuPath().orElse(null));
			assertEquals("Say Hello...", hello.label());
			assertEquals("^H", hello.accelerator().orElse(null));
			assertEquals("/icons/hello.png", hello.iconPath().orElse(null));
			assertEquals(12.0, hello.weight());
			assertTrue(hello.isVisible());

			// NB: asserted on a command no test runs. A flag on SayHello would
			// prove nothing, since another test runs it and JUnit does not order
			// methods by declaration.
			final ClassCommandInfo never = find(context, NeverRun.class);
			assertEquals("Help>Never Run", never.menuPath().orElse(null));
			assertFalse(constructed("org.scijava.command.NeverRun"),
				"building a menu must not construct commands");
		}
	}

	/** Menu order is by weight: a whole menu sorts without loading anything. */
	@Test
	public void testMenuOrdering() {
		try (final Context context = Context.create()) {
			// NB: restricted to the commands this test is about. Asserting the
			// whole list would break whenever another test adds a fixture, which
			// is exactly what happened when the menu tree tests arrived.
			final List<String> ofInterest = List.of("Count Beans", "Say Hello...",
				"Never Run");
			final List<String> labels = Commands.discover(context).stream() //
				.map(ClassCommandInfo::label) //
				.filter(ofInterest::contains) //
				.collect(Collectors.toList());
			// Weights: CountBeans 3, SayHello 12, NeverRun 50.
			assertEquals(ofInterest, labels);
		}
	}

	/** A command needs no menu entry; it is simply not shown. */
	@Test
	public void testCommandWithoutAMenuEntry() {
		try (final Context context = Context.create()) {
			final ClassCommandInfo headless = find(context, Headless.class);
			assertTrue(headless.menuPath().isEmpty());
			// Its label falls back to something usable rather than being null.
			assertEquals(Headless.class.getName(), headless.label());
		}
	}

	/** What was discovered can then be run, through the ordinary runner. */
	@Test
	public void testRunADiscoveredCommand() throws Exception {
		try (final Context context = Context.create()) {
			final ClassCommandInfo hello = find(context, SayHello.class);
			final Runner runner = Runner.of(List.of(), List.of());

			final ExecutionResult result = runner.run(hello, Map.of("name", "ada"))
				.get(5, TimeUnit.SECONDS);

			assertTrue(result.isCompleted());
			assertEquals(Map.of("greeting", "hello ada"), result.outputs());
			// Only now is the class loaded and constructed.
			assertTrue(constructed("org.scijava.command.SayHello"));
		}
	}

	/** Each run gets its own instance, so concurrent runs cannot collide. */
	@Test
	public void testEachRunHasItsOwnInstance() throws Exception {
		try (final Context context = Context.create()) {
			final ClassCommandInfo beans = find(context, CountBeans.class);
			final Runner runner = Runner.of(List.of(), List.of());

			final ExecutionResult first = runner.run(beans, Map.of("beans", 3)).get(5,
				TimeUnit.SECONDS);
			final ExecutionResult second = runner.run(beans, Map.of("beans", 7)).get(
				5, TimeUnit.SECONDS);

			assertEquals(Map.of("tally", "3 beans"), first.outputs());
			assertEquals(Map.of("tally", "7 beans"), second.outputs());
		}
	}

	/**
	 * A command author writes one {@code opens}, to the container.
	 * <p>
	 * The execution layer reads {@code @Parameter} fields through a lookup the
	 * context supplies, so it needs no access of its own. Were that to regress,
	 * running a command would fail - but this asserts the intent directly, so
	 * the reason is obvious rather than inferred from a stack trace.
	 * </p>
	 */
	@Test
	public void testOneOpensIsEnough() {
		final Module module = Command.class.getModule();
		assertTrue(module.isNamed(), "not running on the module path");
		final Module context = Context.class.getModule();
		final Module execute = org.scijava.execute.Executables.class.getModule();

		assertTrue(module.isOpen("org.scijava.command", context),
			"commands must be open to the container");
		assertFalse(module.isOpen("org.scijava.command", execute),
			"commands should not need opening to the execution layer as well");
	}

	private static ClassCommandInfo find(final Context context, final Class<?> type) {
		return Commands.discover(context).stream() //
			.filter(c -> type.getName().equals(c.className())) //
			.findFirst().orElseThrow();
	}

	/**
	 * NB: read reflectively. Referring to the class directly would load it,
	 * which is the very thing under test.
	 */
	private static boolean constructed(final String className) {
		try {
			return Class.forName(className).getField("constructed").getBoolean(null);
		}
		catch (final ReflectiveOperationException exc) {
			throw new AssertionError(exc);
		}
	}

	/**
	 * Something runnable need not be in a menu, and is no less runnable for it.
	 * <p>
	 * NB: this is why the type is {@code CommandInfo} rather than
	 * {@code MenuEntry}. A command reached by name, by a search bar or by
	 * another command is an ordinary thing to have; the menu tree simply takes
	 * the subset with a path.
	 * </p>
	 */
	@Test
	public void testRunnableWithoutAMenuPath() {
		try (final Context context = Context.create()) {
			final CommandInfo hidden = Commands.discover(context).stream() //
				.filter(c -> c.className().equals(Headless.class.getName())) //
				.findFirst().orElseThrow();

			assertTrue(hidden.menuPath().isEmpty(), "declares no menu path");
			assertFalse(hidden.label().isEmpty(), "but still has a label");
			assertNotNull(hidden.create(), "and is still runnable");

			assertTrue(MenuTree.of(List.of(hidden)).children().isEmpty(),
				"so the menu tree leaves it out");
		}
	}
}
