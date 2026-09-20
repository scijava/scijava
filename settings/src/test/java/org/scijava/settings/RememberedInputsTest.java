/*
 * #%L
 * Settings a user can read and edit, in one TOML file.
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

package org.scijava.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.scijava.execute.ExecutionResult;
import org.scijava.execute.Executables;
import org.scijava.execute.Parameter;
import org.scijava.execute.Preprocessor;
import org.scijava.execute.Runner;
import org.scijava.struct.ItemIO;

/**
 * Tests that a command remembers what the user chose.
 *
 * @author Curtis Rueden
 */
public class RememberedInputsTest {

	/** What was chosen once is filled in the next time. */
	@Test
	public void testValuesCarryOver() throws Exception {
		final Settings settings = Settings.inMemory();

		run(settings, new Blur(), Map.of("sigma", 3.5, "edges", "Wrap"));

		final Blur second = new Blur();
		run(settings, second, Map.of());

		assertEquals(3.5, second.sigma);
		assertEquals("Wrap", second.edges);
	}

	/**
	 * The values survive a run that failed, which is the whole reason saving
	 * happens before the run rather than after it.
	 */
	@Test
	public void testValuesSurviveAFailedRun() {
		final Settings settings = Settings.inMemory();

		final ExecutionException failure = org.junit.jupiter.api.Assertions
			.assertThrows(ExecutionException.class, () -> run(settings, new Blur(),
				Map.of("sigma", 9.5, "edges", "Zero", "explode", true)));
		assertTrue(failure.getCause().getMessage().contains("boom"), failure
			.getCause().getMessage());

		// NB: the user is about to try again, and this is exactly when being
		// made to retype everything is most infuriating.
		assertEquals(Optional.of(9.5), settings.get(Blur.class.getName(), "sigma",
			Double.class));
		assertEquals(Optional.of("Zero"), settings.get(Blur.class.getName(),
			"edgePolicy", String.class));
	}

	/** What the caller supplied wins over what was remembered. */
	@Test
	public void testSuppliedValueWins() throws Exception {
		final Settings settings = Settings.inMemory();
		run(settings, new Blur(), Map.of("sigma", 3.5, "edges", "Wrap"));

		final Blur second = new Blur();
		run(settings, second, Map.of("sigma", 1.0));

		assertEquals(1.0, second.sigma, "the caller said so");
		assertEquals("Wrap", second.edges, "and the rest was remembered");
	}

	/** A parameter that says not to be remembered is not remembered. */
	@Test
	public void testPersistFalse() throws Exception {
		final Settings settings = Settings.inMemory();

		run(settings, new Blur(), Map.of("sigma", 3.5, "edges", "Wrap",
			"seed", 12345L));

		assertFalse(settings.get(Blur.class.getName(), "seed", Long.class)
			.isPresent(), "a one-off value should not carry over");
	}

	/** A parameter may be remembered under a name of its own choosing. */
	@Test
	public void testPersistKey() throws Exception {
		final Settings settings = Settings.inMemory();

		run(settings, new Blur(), Map.of("sigma", 3.5, "edges", "Wrap"));

		assertEquals(Optional.of("Wrap"), settings.get(Blur.class.getName(),
			"edgePolicy", String.class), "stored under its persistKey");
	}

	/** Outputs are not inputs, and are not remembered. */
	@Test
	public void testOutputsAreNotRemembered() throws Exception {
		final Settings settings = Settings.inMemory();

		run(settings, new Blur(), Map.of("sigma", 3.5, "edges", "Wrap"));

		assertFalse(settings.get(Blur.class.getName(), "result", String.class)
			.isPresent());
	}

	/** Persistence is a pair of processors, so a caller may leave them out. */
	@Test
	public void testWithoutTheProcessors() throws Exception {
		final Settings settings = Settings.inMemory();
		final Blur blur = new Blur();

		Runner.of(List.of(), List.of()).run(Executables.executableOf(blur,
			MethodHandles.lookup()), Map.of("sigma", 3.5, "edges", "Wrap")).get();

		assertTrue(settings.toml().tables().stream().allMatch(t -> settings.toml()
			.table(t).isEmpty()), "nothing was remembered");
	}

	// -- Helper methods --

	private static ExecutionResult run(final Settings settings,
		final Blur command, final Map<String, Object> inputs) throws Exception
	{
		final List<Preprocessor> chain = List.of(new LoadInputs(settings),
			new SaveInputs(settings));
		// NB: this test's own lookup, rather than the module opening a package
		// it has no plugins in. A container would supply one; here the test is
		// the container.
		return Runner.of(chain, List.of()).run(Executables.executableOf(command,
			MethodHandles.lookup()), inputs).get();
	}

	/** A command with something worth remembering, and something not. */
	public static class Blur implements Runnable {

		@Parameter
		private double sigma = 2;

		@Parameter(persistKey = "edgePolicy")
		private String edges = "Reflect";

		@Parameter(persist = false, required = false)
		private Long seed;

		@Parameter(required = false)
		private boolean explode;

		@Parameter(io = ItemIO.OUTPUT)
		private String result;

		@Override
		public void run() {
			if (explode) throw new IllegalStateException("boom");
			result = "blurred " + sigma + " " + edges;
		}
	}
}
