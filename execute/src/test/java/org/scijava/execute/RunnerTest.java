/*
 * #%L
 * Running things that declare their inputs and outputs.
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

package org.scijava.execute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.scijava.priority.Priority;
import org.scijava.struct.ItemIO;

/**
 * Tests {@link Runner}: the processor chain, declining, and cancellation.
 *
 * @author Curtis Rueden
 */
public class RunnerTest {

	public static class Greet implements Runnable {

		@Parameter
		private String name;

		@Parameter(io = ItemIO.OUTPUT)
		private String greeting;

		@Override
		public void run() {
			greeting = "hello " + name;
		}
	}

	@Test
	public void testRunWithNoProcessors() throws Exception {
		final Runner runner = Runner.of(List.of(), List.of());
		final ExecutionResult result = runner.run(new Greet(), Map.of("name",
			"ada")).get(5, TimeUnit.SECONDS);
		assertTrue(result.isCompleted());
		assertEquals(Map.of("greeting", "hello ada"), result.outputs());
	}

	/** A preprocessor may supply an input the caller omitted. */
	@Test
	public void testPreprocessorSuppliesInput() throws Exception {
		final Preprocessor filler = execution -> execution.instance().member("name")
			.set("grace");
		final Runner runner = Runner.of(List.of(filler), List.of());
		final ExecutionResult result = runner.run(new Greet(), Map.of()).get(5,
			TimeUnit.SECONDS);
		assertEquals(Map.of("greeting", "hello grace"), result.outputs());
	}

	/** Declining is an ordinary outcome, carrying its reason and its author. */
	@Test
	public void testDecline() throws Exception {
		final Preprocessor refuser = execution -> execution.decline(
			"No image is open");
		final Runner runner = Runner.of(List.of(refuser), List.of());

		final ExecutionResult result = runner.run(new Greet(), Map.of("name",
			"ada")).get(5, TimeUnit.SECONDS);

		assertTrue(result.isDeclined());
		assertFalse(result.isCompleted());
		assertEquals("No image is open", result.reason().orElse(null));
		assertSame(refuser, result.declinedBy().orElse(null));
		assertTrue(result.outputs().isEmpty());
	}

	/**
	 * Two instances of one class in a chain: the result must name the one that
	 * actually declined, which a Class could not do.
	 */
	@Test
	public void testDeclinedByDistinguishesInstances() throws Exception {
		class Gate implements Preprocessor {

			private final boolean refuse;

			Gate(final boolean refuse) {
				this.refuse = refuse;
			}

			@Override
			public void process(final Execution execution) {
				if (refuse) execution.decline("refused");
			}
		}
		final Gate permissive = new Gate(false);
		final Gate strict = new Gate(true);
		final Runner runner = Runner.of(List.of(permissive, strict), List.of());

		final ExecutionResult result = runner.run(new Greet(), Map.of("name",
			"ada")).get(5, TimeUnit.SECONDS);
		assertSame(strict, result.declinedBy().orElse(null));
	}

	/** Once declined, later preprocessors do not run, nor does the object. */
	@Test
	public void testDeclineStopsTheChain() throws Exception {
		final List<String> calls = new ArrayList<>();
		final Preprocessor first = execution -> {
			calls.add("first");
			execution.decline("stop");
		};
		final Preprocessor second = execution -> calls.add("second");
		final Runner runner = Runner.of(List.of(first, second), List.of());

		runner.run(new Greet(), Map.of("name", "ada")).get(5, TimeUnit.SECONDS);
		assertEquals(List.of("first"), calls);
	}

	@Test
	public void testProcessorPriorityOrder() throws Exception {
		final List<String> order = new ArrayList<>();
		final Preprocessor low = new Preprocessor() {

			@Override
			public void process(final Execution execution) {
				order.add("low");
			}

			@Override
			public double priority() {
				return Priority.LOW;
			}
		};
		final Preprocessor high = new Preprocessor() {

			@Override
			public void process(final Execution execution) {
				order.add("high");
			}

			@Override
			public double priority() {
				return Priority.HIGH;
			}
		};
		// NB: supplied in the "wrong" order, so sorting is what decides.
		final Runner runner = Runner.of(List.of(low, high), List.of());
		runner.run(new Greet(), Map.of("name", "ada")).get(5, TimeUnit.SECONDS);
		assertEquals(List.of("high", "low"), order);
	}

	/** A postprocessor sees the outputs. */
	@Test
	public void testPostprocessorSeesOutputs() throws Exception {
		final List<Object> seen = new ArrayList<>();
		final Postprocessor recorder = execution -> seen.add(execution.outputs()
			.get("greeting"));
		final Runner runner = Runner.of(List.of(), List.of(recorder));

		runner.run(new Greet(), Map.of("name", "ada")).get(5, TimeUnit.SECONDS);
		assertEquals(List.of("hello ada"), seen);
	}

	/** A required input nobody supplied is an error, not a silent null. */
	@Test
	public void testUnsatisfiedRequiredInput() {
		final Runner runner = Runner.of(List.of(), List.of());
		final ExecutionException exc = assertThrows(ExecutionException.class,
			() -> runner.run(new Greet(), Map.of()).get(5, TimeUnit.SECONDS));
		assertTrue(exc.getCause() instanceof IllegalArgumentException);
		assertTrue(exc.getCause().getMessage().contains("name"), exc.getCause()
			.getMessage());
	}

	// -- Cancellation --

	public static class SlowGreet implements Runnable {

		static final CountDownLatch STARTED = new CountDownLatch(1);

		@Parameter
		private String name;

		@Parameter(io = ItemIO.OUTPUT)
		private String greeting;

		@Override
		public void run() {
			STARTED.countDown();
			try {
				Thread.sleep(30_000);
				greeting = "hello " + name;
			}
			catch (final InterruptedException exc) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Cancelling the future interrupts the run. NB: this is a different thing
	 * from declining - work was already under way.
	 */
	@Test
	public void testCancellationIsNotDeclining() throws Exception {
		final Runner runner = Runner.of(List.of(), List.of());
		final Future<ExecutionResult> future = runner.run(new SlowGreet(), Map.of(
			"name", "ada"));
		assertTrue(SlowGreet.STARTED.await(5, TimeUnit.SECONDS));

		assertTrue(future.cancel(true));
		// Cancellation surfaces as an exception, where declining is a result.
		assertThrows(CancellationException.class, () -> future.get(5,
			TimeUnit.SECONDS));
	}
}
