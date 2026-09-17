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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.scijava.execute.Behavior;
import org.scijava.execute.Executable;
import org.scijava.execute.ExecutableInstance;
import org.scijava.execute.Executables;
import org.scijava.execute.Parameter;
import org.scijava.execute.ParameterMember;
import org.scijava.execute.Parameters;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

/**
 * Tests callbacks and validation - including from a script, which SciJava
 * Common never managed.
 *
 * @author Curtis Rueden
 */
public class ParameterModelTest {

	// -- Callbacks, in Java --

	public static class Rectangle implements Runnable {

		@Parameter(callback = "widthChanged")
		private int width = 4;

		@Parameter
		private int height = 3;

		@Parameter(io = ItemIO.OUTPUT)
		private int area = 12;

		@SuppressWarnings("unused")
		private void widthChanged() {
			// NB: a callback may change anything, not only its own parameter.
			area = width * height;
		}

		@Override
		public void run() {
			area = width * height;
		}
	}

	@Test
	public void testCallbackChangesAnotherParameter() {
		final ParameterModel model = model(new Rectangle());

		final List<ObservableStruct.Change> changes = model.set("width", 10);

		assertEquals(30, model.get("area"));
		// The change to area is reported, not merely applied.
		assertTrue(changes.stream().anyMatch(c -> "area".equals(c.key())),
			changes.toString());
	}

	// -- A callback pair that would otherwise never stop --

	public static class Celsius implements Runnable {

		@Parameter(callback = "celsiusChanged")
		private double celsius = 0;

		@Parameter(callback = "fahrenheitChanged")
		private double fahrenheit = 32;

		@SuppressWarnings("unused")
		private void celsiusChanged() {
			fahrenheit = celsius * 9 / 5 + 32;
		}

		@SuppressWarnings("unused")
		private void fahrenheitChanged() {
			celsius = (fahrenheit - 32) * 5 / 9;
		}

		@Override
		public void run() {}
	}

	/** Two parameters that set each other must settle rather than loop. */
	@Test
	public void testMutualCallbacksTerminate() {
		final ParameterModel model = model(new Celsius());
		model.set("celsius", 100.0);
		assertEquals(212.0, model.get("fahrenheit"));
	}

	// -- Validation --

	public static class Bounded implements Runnable {

		@Parameter(validator = "checkCount")
		private int count = 5;

		@SuppressWarnings("unused")
		private String checkCount(final Object value) {
			final int n = (Integer) value;
			return n > 0 && n <= 10 ? null : "Count must be between 1 and 10";
		}

		@Override
		public void run() {}
	}

	@Test
	public void testValidation() {
		final ParameterModel model = model(new Bounded());
		assertTrue(model.isValid());
		assertTrue(model.problems().isEmpty());

		model.set("count", 42);
		assertFalse(model.isValid());
		assertEquals(Map.of("count", "Count must be between 1 and 10"), model
			.problems());

		model.set("count", 7);
		assertTrue(model.isValid());
	}

	// -- The same, from a script --

	/**
	 * Stands in for a script: parameters declared in a header, values in engine
	 * bindings, and behaviors that are functions in the script rather than Java
	 * methods.
	 * <p>
	 * This is what SciJava Common could not express. Its callbacks were method
	 * names resolved by Java reflection, so a script had nothing to name; here
	 * a name is resolved by whatever kind of code the executable is.
	 * </p>
	 */
	private static class FakeScript implements Executable {

		private final StructInstance<Map<String, Object>> parameters;
		private final Map<String, Behavior> functions = new HashMap<>();

		FakeScript() {
			// #@ String name (callback = "nameChanged")
			// #@ boolean shout
			// #@output String greeting
			parameters = Parameters.builder() //
				.add("name", String.class, ItemIO.INPUT, //
					Map.of(ParameterMember.CALLBACK, "nameChanged"), "ada") //
				.add("shout", Boolean.class, ItemIO.INPUT, Map.of(), false) //
				.add("greeting", String.class, ItemIO.OUTPUT, Map.of(), "hello ada") //
				.build();

			// def nameChanged() { greeting = ... }
			functions.put("nameChanged", args -> {
				final Map<String, Object> values = parameters.object();
				final String greeting = "hello " + values.get("name");
				values.put("greeting", Boolean.TRUE.equals(values.get("shout")) //
					? greeting.toUpperCase() : greeting);
				return null;
			});
			// def checkName(value) { ... }
			functions.put("checkName", args -> {
				final String value = String.valueOf(args[0]);
				return value.isEmpty() ? "A name is required" : null;
			});
		}

		@Override
		public String name() {
			return "greet.groovy";
		}

		@Override
		public Struct struct() {
			return parameters.struct();
		}

		@Override
		public ExecutableInstance create() {
			return new ExecutableInstance() {

				@Override
				public Executable executable() {
					return FakeScript.this;
				}

				@Override
				public StructInstance<?> parameters() {
					return parameters;
				}

				@Override
				public void run() {
					functions.get("nameChanged").invoke();
				}

				@Override
				public Optional<Behavior> behavior(final String name) {
					return Optional.ofNullable(functions.get(name));
				}
			};
		}
	}

	/** A script's callback runs when its parameter changes. */
	@Test
	public void testScriptCallback() {
		final ParameterModel model = new ParameterModel(new FakeScript().create());
		assertEquals("hello ada", model.get("greeting"));

		model.set("name", "grace");
		assertEquals("hello grace", model.get("greeting"));

		// A second parameter the callback reads, proving it sees whole state.
		model.set("shout", true);
		model.set("name", "ada");
		assertEquals("HELLO ADA", model.get("greeting"));
	}

	/** A script's validator reports problems the same way a Java one does. */
	@Test
	public void testScriptValidator() {
		final FakeScript script = new FakeScript();
		final StructInstance<Map<String, Object>> parameters = Parameters
			.builder() //
			.add("name", String.class, ItemIO.INPUT, //
				Map.of(ParameterMember.VALIDATOR, "checkName"), "ada") //
			.build();
		final ExecutableInstance base = script.create();
		final ParameterModel model = new ParameterModel(new ExecutableInstance() {

			@Override
			public Executable executable() {
				return base.executable();
			}

			@Override
			public StructInstance<?> parameters() {
				return parameters;
			}

			@Override
			public void run() {}

			@Override
			public Optional<Behavior> behavior(final String name) {
				return base.behavior(name);
			}
		});

		assertTrue(model.isValid());
		model.set("name", "");
		assertEquals(Map.of("name", "A name is required"), model.problems());
	}

	private static ParameterModel model(final Runnable object) {
		final Executable executable = Executables.executableOf(object);
		return new ParameterModel(executable.create());
	}
}
