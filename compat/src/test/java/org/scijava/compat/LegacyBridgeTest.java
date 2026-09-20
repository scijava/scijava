/*
 * #%L
 * Presenting SciJava Common's modules as SciJava3 commands.
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

package org.scijava.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.command3.CommandInfo;
import org.scijava.command3.MenuTree;
import org.scijava.execute.Behavior;
import org.scijava.execute.ExecutableInstance;
import org.scijava.execute.ExecutionResult;
import org.scijava.execute.ParameterMember;
import org.scijava.execute.Preprocessor;
import org.scijava.execute.Runner;

/**
 * Tests that SciJava Common's modules arrive as SciJava3 commands.
 *
 * @author Curtis Rueden
 */
public class LegacyBridgeTest {

	private static org.scijava.Context legacy;

	@BeforeAll
	public static void startLegacyContext() {
		legacy = new org.scijava.Context();
	}

	@AfterAll
	public static void disposeLegacyContext() {
		if (legacy != null) legacy.dispose();
	}

	/** Every legacy module is a command, without anything being loaded. */
	@Test
	public void testDiscovery() {
		final List<CommandInfo> commands = Legacy.commands(legacy);

		assertTrue(commands.size() > 2, "found " + commands.size());
		assertTrue(names(commands).contains(LegacyCommands.LegacyBlur.class
			.getName()), names(commands).toString());
	}

	/** Its menu metadata translates, so it lands where it always did. */
	@Test
	public void testMenuMetadata() {
		final CommandInfo blur = command(LegacyCommands.LegacyBlur.class);

		assertEquals(Optional.of("Process>Filters>Legacy Blur..."), blur
			.menuPath());
		assertEquals("Legacy Blur...", blur.label());
		assertEquals(Optional.of("/icons/blur.png"), blur.iconPath());
		assertEquals(7.0, blur.weight());
		assertTrue(blur.isVisible());
		// NB: translated into SciJava3's notation, not Swing's -- so JavaFX and
		// AWT can read it too.
		assertEquals(Optional.of("^L"), blur.accelerator());
	}

	/** And it sits in a SciJava3 menu tree beside anything else. */
	@Test
	public void testInAMenuTree() {
		final MenuTree filters = MenuTree.of(Legacy.commands(legacy)) //
			.find("Process>Filters").orElseThrow();

		assertEquals(List.of("Legacy Blur..."), filters.children().stream() //
			.map(MenuTree::label).collect(Collectors.toList()));
	}

	/** Service parameters are the context's business, not the user's. */
	@Test
	public void testInjectedParametersAreHidden() {
		final CommandInfo dynamic = command(LegacyCommands.LegacyDynamic.class);

		final List<String> keys = dynamic.struct().members().stream() //
			.map(m -> m.key()).collect(Collectors.toList());

		// NB: DynamicCommand declares context, commandService, pluginService and
		// moduleService as @Parameter fields, SciJava Common having used one
		// annotation for injection and for inputs. A dialog must not ask for
		// them.
		assertEquals(List.of("name"), keys);
	}

	/** Its parameters translate, with the metadata a dialog needs. */
	@Test
	public void testParameters() {
		final CommandInfo blur = command(LegacyCommands.LegacyBlur.class);
		final var members = blur.struct().members();

		assertEquals(List.of("sigma", "edges", "input", "result"), members
			.stream().map(m -> m.key()).collect(Collectors.toList()));

		final ParameterMember<?> sigma = (ParameterMember<?>) members.get(0);
		assertEquals(Optional.of("Blur radius"), sigma.attr(
			ParameterMember.LABEL));
		// NB: "0.0" rather than "0": SciJava Common parsed the annotation's
		// string into the parameter's own type, and the bridge reports what it
		// holds. Either spelling converts to the same double.
		assertEquals(Optional.of("0.0"), sigma.attr(ParameterMember.MIN));
		assertEquals(Optional.of("20.0"), sigma.attr(ParameterMember.MAX));
		assertEquals(Optional.of("0.5"), sigma.attr(ParameterMember.STEP_SIZE));
		assertEquals(Optional.of("slider"), sigma.attr(ParameterMember.STYLE));

		final ParameterMember<?> edges = (ParameterMember<?>) members.get(1);
		assertEquals(Optional.of("Reflect,Zero,Wrap"), edges.attr(
			ParameterMember.CHOICES));

		assertTrue(members.get(0).isRequired());
		assertFalse(members.get(2).isRequired(), "input is not required");
		assertTrue(members.get(3).isOutput(), "result is an output");
	}

	/** It runs through the SciJava3 Runner, with SciJava3 inputs. */
	@Test
	public void testRunsThroughTheSciJava3Runner() throws Exception {
		final CommandInfo blur = command(LegacyCommands.LegacyBlur.class);

		final Future<ExecutionResult> future = Runner.of(List.of(), List.of()) //
			.run(blur, Map.of("sigma", "3.5", "edges", "Wrap"));
		final ExecutionResult result = future.get();

		assertFalse(result.isDeclined());
		// NB: "3.5" was a string, and the parameter is a double: convert3 did
		// that, exactly as it does for a SciJava3 command.
		assertEquals("Legacy blur: sigma=3.5 edges=Wrap", result.outputs().get(
			"result"));
	}

	/** SciJava3's preprocessors apply to it, including a harvester. */
	@Test
	public void testSciJava3PreprocessorsApply() throws Exception {
		final Preprocessor filler = execution -> execution.instance().member(
			"edges").set("Zero");
		final CommandInfo blur = command(LegacyCommands.LegacyBlur.class);

		final ExecutionResult result = Runner.of(List.of(filler), List.of()) //
			.run(blur, Map.of("sigma", 1.0)).get();

		assertEquals("Legacy blur: sigma=1.0 edges=Zero", result.outputs().get(
			"result"));
	}

	/** A legacy callback fires the SciJava3 way, so its dialog still behaves. */
	@Test
	public void testLegacyCallback() {
		final CommandInfo temperature = command(
			LegacyCommands.LegacyTemperature.class);
		final ExecutableInstance instance = temperature.create();
		instance.parameters().member("celsius").set(100.0);

		final Optional<Behavior> callback = instance.behavior("celsiusChanged");
		assertTrue(callback.isPresent(), "the legacy callback, by its name");
		callback.get().invoke();

		assertEquals(212.0, instance.parameters().member("fahrenheit").get());
	}

	/**
	 * A DynamicCommand's run-time parameters come across too.
	 * <p>
	 * NB: SciJava3 offers no way to write one, and does not need to in order to
	 * present one. What the module reports when asked is what the dialog shows.
	 * </p>
	 */
	@Test
	public void testDynamicCommand() {
		final CommandInfo dynamic = command(LegacyCommands.LegacyDynamic.class);

		// NB: creating it initializes it, which is when it builds its extra
		// parameter -- before anything would harvest.
		final ExecutableInstance instance = dynamic.create();

		final List<String> keys = instance.parameters().members().stream() //
			.map(m -> m.member().key()).collect(Collectors.toList());
		assertEquals(List.of("name", "addedAtRuntime"), keys);
		assertEquals(42, instance.parameters().member("addedAtRuntime").get());
	}

	// -- Helper methods --

	private static CommandInfo command(final Class<?> type) {
		return Legacy.commands(legacy).stream() //
			.filter(c -> c.name().equals(type.getName())) //
			.findFirst().orElseThrow(() -> new AssertionError("Not found: " + type));
	}

	private static List<String> names(final List<CommandInfo> commands) {
		return commands.stream().map(CommandInfo::name).collect(Collectors
			.toList());
	}
}
