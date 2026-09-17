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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;

/**
 * Tests {@link Executables}.
 *
 * @author Curtis Rueden
 */
public class ExecutablesTest {

	public static class AddNumbers implements Runnable {

		@Parameter
		private double a;

		@Parameter
		private double b;

		@Parameter(io = ItemIO.OUTPUT)
		private double result;

		@Override
		public void run() {
			result = a + b;
		}
	}

	@Test
	public void testRun() {
		final Map<String, Object> outputs = Executables.run(new AddNumbers(),
			Map.of("a", 2.0, "b", 3.0));
		assertEquals(Map.of("result", 5.0), outputs);
	}

	@Test
	public void testStructDescribesTheParameters() {
		final List<String> inputs = describe(AddNumbers.class, true);
		final List<String> outputs = describe(AddNumbers.class, false);
		assertEquals(List.of("a", "b"), inputs);
		assertEquals(List.of("result"), outputs);
	}

	@Test
	public void testUnknownParameterIsRejected() {
		final IllegalArgumentException exc = assertThrows(
			IllegalArgumentException.class, () -> Executables.run(new AddNumbers(),
				Map.of("a", 1.0, "b", 2.0, "nope", 3.0)));
		// The message should say what is actually available.
		assertTrue(exc.getMessage().contains("nope"), exc.getMessage());
		assertTrue(exc.getMessage().contains("a, b, result"), exc.getMessage());
	}

	public static class NeedsName implements Runnable {

		@Parameter
		private String name;

		@Parameter(required = false)
		private String nickname;

		@Parameter(io = ItemIO.OUTPUT)
		private String greeting;

		@Override
		public void run() {
			greeting = "hello " + (nickname == null ? name : nickname);
		}
	}

	@Test
	public void testMissingRequiredParameter() {
		final IllegalArgumentException exc = assertThrows(
			IllegalArgumentException.class, () -> Executables.run(new NeedsName(),
				Map.of()));
		assertTrue(exc.getMessage().contains("name"), exc.getMessage());
	}

	@Test
	public void testOptionalParameterMayBeOmitted() {
		assertEquals(Map.of("greeting", "hello ada"), Executables.run(
			new NeedsName(), Map.of("name", "ada")));
		assertEquals(Map.of("greeting", "hello duchess"), Executables.run(
			new NeedsName(), Map.of("name", "ada", "nickname", "duchess")));
	}

	/** Parameters declared by a base class are inherited. */
	public static abstract class HasBase implements Runnable {

		@Parameter
		protected int base;
	}

	public static class Doubler extends HasBase {

		@Parameter(io = ItemIO.OUTPUT)
		private int doubled;

		@Override
		public void run() {
			doubled = base * 2;
		}
	}

	@Test
	public void testInheritedParameters() {
		assertEquals(Map.of("doubled", 8), Executables.run(new Doubler(), Map.of(
			"base", 4)));
	}

	// -- Cancellation --

	public static class SlowTask implements Runnable {

		final CountDownLatch started = new CountDownLatch(1);

		@Parameter
		private int millis;

		@Parameter(io = ItemIO.OUTPUT)
		private String status;

		@Override
		public void run() {
			started.countDown();
			try {
				Thread.sleep(millis);
				status = "finished";
			}
			catch (final InterruptedException exc) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/** Cancelling the future stops the run, per the cancellation design. */
	@Test
	public void testCancellation() throws Exception {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			final SlowTask task = new SlowTask();
			final Future<Map<String, Object>> future = executor.submit(Executables
				.callable(task, Map.of("millis", 30_000)));
			assertTrue(task.started.await(5, TimeUnit.SECONDS));

			assertTrue(future.cancel(true));
			assertThrows(CancellationException.class, () -> future.get(5,
				TimeUnit.SECONDS));
			assertFalse("finished".equals(statusOf(task)), "the task ran to the end");
		}
		finally {
			executor.shutdownNow();
		}
	}

	private static String statusOf(final SlowTask task) {
		return (String) Executables.struct(SlowTask.class).createInstance(task)
			.member("status").get();
	}

	private static List<String> describe(final Class<?> type,
		final boolean inputs)
	{
		return Executables.struct(type).members().stream() //
			.filter(m -> inputs ? m.isInput() : m.isOutput()) //
			.map(Member::key) //
			.collect(Collectors.toList());
	}
}
