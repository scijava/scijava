/*
 * #%L
 * Running scripts as things that declare their inputs and outputs.
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

package org.scijava.script3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.scijava.execute.Behavior;
import org.scijava.execute.ExecutableInstance;
import org.scijava.execute.ExecutionResult;
import org.scijava.execute.Preprocessor;
import org.scijava.execute.Runner;

/**
 * Tests a script as an executable, against a real language.
 * <p>
 * Groovy is the engine here, but nothing in these tests is about Groovy: they
 * are about a script being a thing that declares its inputs and outputs, and
 * therefore running the way everything else does.
 * </p>
 *
 * @author Curtis Rueden
 */
public class GroovyScriptTest {

	private final Scripts scripts = Scripts.get();

	/** Groovy is found through its JSR-223 engine, with nothing declared. */
	@Test
	public void testLanguageDiscovery() {
		final Optional<ScriptLanguage> groovy = scripts.language("groovy");
		assertTrue(groovy.isPresent(), "available languages: " + scripts
			.languages());
		assertTrue(scripts.language(".groovy").isPresent(), "extension with a dot");
		assertTrue(scripts.language("Groovy").isPresent(), "name, as a user says");
	}

	/** A script's declared inputs and outputs are its parameters. */
	@Test
	public void testScriptIsAnExecutable() {
		final ScriptExecutable script = greeting();

		assertEquals(List.of("name", "shout", "greeting"), script.struct()
			.members().stream().map(m -> m.key()).collect(java.util.stream.Collectors
				.toList()));
		assertTrue(script.struct().members().get(0).isInput());
		assertTrue(script.struct().members().get(2).isOutput());
	}

	/** It runs, and what it assigned comes back as its outputs. */
	@Test
	public void testRun() {
		final Map<String, Object> outputs = run(greeting(), Map.of( //
			"name", "ada", "shout", false));

		assertEquals("hello ada", outputs.get("greeting"));
	}

	/** Supplied values are converted to the declared types, as for a command. */
	@Test
	public void testInputsAreConverted() {
		// NB: strings for everything, which is all a command line ever has.
		final Map<String, Object> outputs = run(greeting(), Map.of( //
			"name", "ada", "shout", "true"));

		assertEquals("HELLO ADA", outputs.get("greeting"));
	}

	/**
	 * A script's callback runs, which SciJava Common could never do.
	 * <p>
	 * It resolved {@code callback = "foo"} by reflecting a Java method, leaving
	 * a script nothing to name. Here the name is resolved by whatever kind of
	 * code the executable is - and for a script, that means a function in the
	 * script.
	 * </p>
	 */
	@Test
	public void testScriptCallback() {
		final ScriptExecutable script = scripts.of("callbacks.groovy", "" + //
			"#@ double(callback = \"celsiusChanged\") celsius\n" + //
			"#@ double fahrenheit\n" + //
			"def celsiusChanged() {\n" + //
			"  fahrenheit = celsius * 9 / 5 + 32\n" + //
			"}\n", "groovy");

		final ExecutableInstance instance = script.create();
		instance.parameters().member("celsius").set(100.0);

		final Optional<Behavior> callback = instance.behavior("celsiusChanged");
		assertTrue(callback.isPresent(), "the script's own function");
		callback.get().invoke();

		assertEquals(212.0, instance.parameters().member("fahrenheit").get());
	}

	/** A behavior the script does not define is absent, not an error. */
	@Test
	public void testMissingBehavior() {
		final ExecutableInstance instance = greeting().create();
		assertTrue(instance.behavior("noSuchFunction").isPresent(),
			"an Invocable engine offers any name...");
		assertThrowsScriptException(() -> instance.behavior("noSuchFunction").get()
			.invoke());
	}

	/** A script reaches the same processors, and can be declined the same way. */
	@Test
	public void testPreprocessorsApplyToScripts() throws Exception {
		final Preprocessor filler = execution -> execution.instance().member("name")
			.set("grace");
		final Runner runner = Runner.of(List.of(filler), List.of());

		final Future<ExecutionResult> future = runner.run(greeting(), Map.of( //
			"shout", false));
		final ExecutionResult result = future.get();

		assertFalse(result.isDeclined());
		assertEquals("hello grace", result.outputs().get("greeting"));
	}

	/** A failing script says what went wrong, rather than failing silently. */
	@Test
	public void testFailingScript() {
		final ScriptExecutable script = scripts.of("bad.groovy", //
			"#@output String result\nthis is not groovy\n", "groovy");

		assertThrowsScriptException(() -> run(script, Map.of()));
	}

	// -- Helper methods --

	private ScriptExecutable greeting() {
		return scripts.of("greeting.groovy", "" + //
			"#@ String name\n" + //
			"#@ boolean shout\n" + //
			"#@output String greeting\n" + //
			"greeting = \"hello \" + name\n" + //
			"if (shout) greeting = greeting.toUpperCase()\n", "groovy");
	}

	/** Runs a script the way anything else is run: through a Runner. */
	private static Map<String, Object> run(final ScriptExecutable script,
		final Map<String, Object> inputs)
	{
		try {
			final ExecutionResult result = Runner.of(List.of(), List.of()) //
				.run(script, inputs).get();
			assertFalse(result.isDeclined(), "should not have been declined");
			return result.outputs();
		}
		catch (final InterruptedException exc) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(exc);
		}
		catch (final java.util.concurrent.ExecutionException exc) {
			// NB: unwrap, so a failing script reports as a script failure.
			if (exc.getCause() instanceof RuntimeException) {
				throw (RuntimeException) exc.getCause();
			}
			throw new IllegalStateException(exc.getCause());
		}
	}

	private static void assertThrowsScriptException(final Runnable work) {
		try {
			work.run();
		}
		catch (final ScriptException exc) {
			return;
		}
		catch (final RuntimeException exc) {
			if (exc.getCause() instanceof ScriptException) return;
			throw exc;
		}
		throw new AssertionError("Expected a ScriptException");
	}
}
