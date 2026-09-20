/*
 * #%L
 * The model behind a parameter dialog: groups, dependencies, validation.
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

package org.scijava.harvest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.scijava.execute.Executable;
import org.scijava.execute.ExecutableInstance;
import org.scijava.execute.Executables;
import org.scijava.execute.Parameter;
import org.scijava.execute.Parameters;
import org.scijava.struct.StructInstance;

/**
 * Exercises the parameter tree against the dialog behaviours actually asked
 * for: a chosen implementation bringing its own parameters, a static nested
 * group, an "advanced" toggle, and a group whose size depends on a value.
 *
 * @author Curtis Rueden
 */
public class ParameterTreeTest {

	// -- 1. Chosen implementation: the value decides the parameters --

	public interface Joke {}

	public static class KnockKnock implements Joke {

		@Parameter(label = "Who's there?")
		private String whosThere = "Boo";

		@Parameter
		private String punchline = "Boo hoo";
	}

	public static class OneLiner implements Joke {

		@Parameter
		private String setup = "I told my wife she was drawing her eyebrows too high";
	}

	public static class TellJoke implements Runnable {

		@Parameter
		private String teller = "Chuckles";

		@Parameter(label = "Favorite joke")
		private Joke joke = new KnockKnock();

		@Override
		public void run() {}
	}

	@Test
	public void testChosenImplementationBringsItsOwnParameters() {
		final TellJoke command = new TellJoke();
		final ExecutableInstance instance = instanceOf(command);
		final ParameterTree tree = ParameterTree.of(instance.parameters(),
			instance);

		final ParameterNode joke = tree.find("joke").orElseThrow();
		assertEquals(List.of("Who's there?", "Punchline"), labels(joke.children()));

		// Choose a different joke: the sub-parameters change with it.
		final ObservableStruct observable = new ObservableStruct(instance
			.parameters());
		observable.set("joke", new OneLiner());
		final ParameterTree rebuilt = ParameterTree.of(instance.parameters(),
			instance);
		assertEquals(List.of("Setup"), labels(rebuilt.find("joke").orElseThrow()
			.children()));
	}

	// -- 2. Static nested group --

	public static class Settings {

		@Parameter
		private int iterations = 10;

		@Parameter
		private double tolerance = 0.01;
	}

	public static class WithSettings implements Runnable {

		@Parameter
		private String input = "image.tif";

		@Parameter
		private Settings settings = new Settings();

		@Override
		public void run() {}
	}

	@Test
	public void testStaticNestedGroup() {
		final ExecutableInstance instance = instanceOf(new WithSettings());
		final ParameterTree tree = ParameterTree.of(instance.parameters(),
			instance);
		assertEquals(List.of("Iterations", "Tolerance"), labels(tree.find(
			"settings").orElseThrow().children()));
	}

	// -- 3. An "advanced" toggle revealing a group --

	@Group(name = "Advanced", collapsible = true, collapsed = true,
		visibleWhen = "showAdvanced")
	public static class WithAdvanced implements Runnable {

		@Parameter
		private String file = "data.csv";

		@Parameter(label = "Show advanced options")
		private boolean advanced = false;

		@Parameter(group = "Advanced")
		private int threads = 4;

		@Parameter(group = "Advanced")
		private boolean verbose = false;

		@SuppressWarnings("unused")
		private boolean showAdvanced() {
			return advanced;
		}

		@Override
		public void run() {}
	}

	@Test
	public void testAdvancedGroupAppearsWhenToggled() {
		final WithAdvanced command = new WithAdvanced();
		final ExecutableInstance instance = instanceOf(command);

		// Hidden to begin with: the group is absent, not merely empty.
		ParameterTree tree = ParameterTree.of(instance.parameters(), instance);
		assertEquals(List.of("File", "Show advanced options"), labels(tree
			.nodes()));

		// Toggle it on.
		final ObservableStruct observable = new ObservableStruct(instance
			.parameters());
		observable.set("advanced", true);
		tree = ParameterTree.of(instance.parameters(), instance);

		assertEquals(List.of("File", "Show advanced options", "Advanced"), labels(
			tree.nodes()));
		final ParameterNode advanced = tree.nodes().get(2);
		assertTrue(advanced.isGroup());
		assertTrue(advanced.isCollapsible());
		assertTrue(advanced.isCollapsed());
		assertEquals(List.of("Threads", "Verbose"), labels(advanced.children()));
	}

	// -- 4. A group whose size depends on a value --

	@Group(name = "Dimensions", membersFrom = "dimensionMembers",
		after = "numDims")
	public static class WithDimensions implements Runnable {

		@Parameter
		private String dataset = "stack.tif";

		@Parameter(callback = "dimsChanged")
		private int numDims = 2;

		/**
		 * The generated parameters, rebuilt whenever the count changes.
		 * <p>
		 * NB: these cannot be fields. A class's fields are fixed when it is
		 * compiled, and the number wanted here is not known until the user types
		 * it, so they are built to order over a map - the same primitive a script
		 * header needs.
		 * </p>
		 */
		private StructInstance<Map<String, Object>> dims = buildDims(2);

		@SuppressWarnings("unused")
		private void dimsChanged() {
			dims = buildDims(numDims);
		}

		@SuppressWarnings("unused")
		private Object dimensionMembers() {
			return dims;
		}

		private static StructInstance<Map<String, Object>> buildDims(
			final int count)
		{
			final Parameters.Builder builder = Parameters.builder();
			for (int i = 0; i < count; i++)
				builder.add("dim" + i, String.class, "d" + i);
			return builder.build();
		}

		@Override
		public void run() {}
	}

	@Test
	public void testGeneratedGroupFollowsAValue() {
		final WithDimensions command = new WithDimensions();
		final ExecutableInstance instance = instanceOf(command);

		ParameterTree tree = ParameterTree.of(instance.parameters(), instance);
		assertEquals(List.of("Dataset", "Num dims", "Dimensions"), labels(tree
			.nodes()));
		// Two dimensions to begin with.
		assertEquals(List.of("Dim0", "Dim1"), labels(tree.nodes().get(2)
			.children()));

		// Ask for four: the callback rebuilds the group, and the tree follows.
		final ParameterModel model = new ParameterModel(instance);
		model.set("numDims", 4);
		tree = model.tree();
		assertEquals(List.of("Dim0", "Dim1", "Dim2", "Dim3"), labels(tree.nodes()
			.get(2).children()));
	}

	// -- Helpers --

	private static ExecutableInstance instanceOf(final Runnable object) {
		final Executable executable = Executables.executableOf(object);
		return executable.create();
	}

	private static List<String> labels(final List<ParameterNode> nodes) {
		return nodes.stream().map(ParameterNode::label).collect(Collectors
			.toList());
	}
}
