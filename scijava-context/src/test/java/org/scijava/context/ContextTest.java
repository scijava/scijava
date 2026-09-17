/*
 * #%L
 * An application container: services, discovered and wired.
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

package org.scijava.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.scijava.priority.Priority;

/**
 * Tests {@link Context}.
 *
 * @author Curtis Rueden
 */
public class ContextTest {

	// -- Test services --

	public interface Greeter extends Service {

		String greet();
	}

	public interface Recorder extends Service {

		List<String> log();
	}

	public static class DefaultRecorder implements Recorder {

		private final List<String> log = new ArrayList<>();

		@Override
		public List<String> log() {
			return log;
		}

		@Override
		public void dispose() {
			log.add("disposed recorder");
		}
	}

	/** Depends on {@link Recorder}, acquired during initialization. */
	public static class DefaultGreeter implements Greeter {

		private Recorder recorder;

		@Override
		public void initialize(final Context context) {
			recorder = context.service(Recorder.class);
			recorder.log().add("greeter initialized");
		}

		@Override
		public String greet() {
			return "hello";
		}

		@Override
		public void dispose() {
			// NB: dependencies must still work while shutting down.
			recorder.log().add("disposed greeter");
		}
	}

	@Test
	public void testServiceLookupAndDependency() {
		try (final Context context = Context.of(new DefaultGreeter(),
			new DefaultRecorder()))
		{
			final Greeter greeter = context.service(Greeter.class);
			assertEquals("hello", greeter.greet());
			assertTrue(context.service(Recorder.class).log().contains(
				"greeter initialized"));
		}
	}

	/** The same instance comes back every time. */
	@Test
	public void testServicesAreSingletonsPerContext() {
		try (final Context context = Context.of(new DefaultRecorder())) {
			assertSame(context.service(Recorder.class), context.service(
				Recorder.class));
		}
	}

	@Test
	public void testMissingService() {
		try (final Context context = Context.of()) {
			final NoSuchServiceException exc = assertThrows(
				NoSuchServiceException.class, () -> context.service(Greeter.class));
			assertEquals(Greeter.class, exc.serviceType());
			assertTrue(context.optionalService(Greeter.class).isEmpty());
		}
	}

	/** Disposal runs in reverse creation order, so dependencies outlive users. */
	@Test
	public void testDisposalOrder() {
		final DefaultRecorder recorder = new DefaultRecorder();
		final Context context = Context.of(recorder, new DefaultGreeter());
		context.service(Greeter.class);
		context.dispose();

		final int greeterAt = recorder.log().indexOf("disposed greeter");
		final int recorderAt = recorder.log().indexOf("disposed recorder");
		assertTrue(greeterAt >= 0, "greeter was never disposed");
		assertTrue(recorderAt >= 0, "recorder was never disposed");
		assertTrue(greeterAt < recorderAt,
			"a service was disposed before the one that depends on it");
	}

	@Test
	public void testDisposeIsIdempotentAndBlocksLookups() {
		final Context context = Context.of(new DefaultRecorder());
		context.dispose();
		assertTrue(context.isDisposed());
		// Disposing twice is harmless.
		context.dispose();
		assertThrows(IllegalStateException.class, () -> context.service(
			Recorder.class));
	}

	/** Two contexts share nothing: not services, not the event bus. */
	@Test
	public void testContextsAreIsolated() {
		try (final Context first = Context.of(new DefaultRecorder());
				final Context second = Context.of(new DefaultRecorder()))
		{
			assertNotSame(first.service(Recorder.class), second.service(
				Recorder.class));
			assertNotSame(first.events(), second.events());

			first.service(Recorder.class).log().add("only in first");
			assertFalse(second.service(Recorder.class).log().contains(
				"only in first"));
		}
	}

	/** Disposing the context closes its bus, so services need no cleanup. */
	@Test
	public void testDisposeClosesTheEventBus() {
		final Context context = Context.of(new DefaultRecorder());
		final List<Object> heard = new ArrayList<>();
		context.events().subscribe(String.class, heard::add);
		context.events().publish("before");
		context.dispose();
		context.events().publish("after");
		assertEquals(List.of("before"), heard);
	}

	// -- Mutual dependency --

	public interface Ping extends Service {

		Pong pong();
	}

	public interface Pong extends Service {

		Ping ping();
	}

	public static class DefaultPing implements Ping {

		private Pong pong;

		@Override
		public void initialize(final Context context) {
			pong = context.service(Pong.class);
		}

		@Override
		public Pong pong() {
			return pong;
		}
	}

	public static class DefaultPong implements Pong {

		private Ping ping;

		@Override
		public void initialize(final Context context) {
			ping = context.service(Ping.class);
		}

		@Override
		public Ping ping() {
			return ping;
		}
	}

	/**
	 * Two services may depend on each other. This resolves only because
	 * dependencies are acquired during initialization rather than in the
	 * constructor - constructor injection could not do it at all.
	 */
	@Test
	public void testMutualDependencyResolves() {
		try (final Context context = Context.of(new DefaultPing(),
			new DefaultPong()))
		{
			final Ping ping = context.service(Ping.class);
			final Pong pong = context.service(Pong.class);
			assertSame(pong, ping.pong());
			assertSame(ping, pong.ping());
		}
	}

	// -- Failure during initialization --

	public interface Fragile extends Service {}

	public static class DefaultFragile implements Fragile {

		@Override
		public void initialize(final Context context) {
			throw new IllegalStateException("cannot start");
		}
	}

	/** A service that fails to initialize says which one, and why. */
	@Test
	public void testInitializationFailureIsReported() {
		try (final Context context = Context.of(new DefaultFragile())) {
			final ServiceException exc = assertThrows(ServiceException.class, //
				() -> context.service(Fragile.class));
			assertTrue(exc.getMessage().contains("DefaultFragile"), exc.getMessage());
			assertEquals("cannot start", exc.getCause().getMessage());
		}
	}

	// -- Priority --

	public static class LowGreeter implements Greeter {

		@Override
		public String greet() {
			return "low";
		}

		@Override
		public double priority() {
			return Priority.LOW;
		}
	}

	public static class HighGreeter implements Greeter {

		@Override
		public String greet() {
			return "high";
		}

		@Override
		public double priority() {
			return Priority.HIGH;
		}
	}

	/** Where several implementations provide a type, the highest wins. */
	@Test
	public void testHighestPriorityWins() {
		try (final Context context = Context.of(new LowGreeter(),
			new HighGreeter()))
		{
			assertEquals("high", context.service(Greeter.class).greet());
		}
		// NB: declaration order must not decide it.
		try (final Context context = Context.of(new HighGreeter(),
			new LowGreeter()))
		{
			assertEquals("high", context.service(Greeter.class).greet());
		}
	}

	@Test
	public void testServiceIsFoundByItsInterface() {
		try (final Context context = Context.of(new LowGreeter())) {
			assertInstanceOf(LowGreeter.class, context.service(Greeter.class));
		}
	}
}
