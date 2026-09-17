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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

/**
 * Proves that {@link Executable} admits things that are not Java classes with
 * annotated fields - a script being the case that matters.
 * <p>
 * The executable here declares its parameters from a map, as a script declares
 * them in its header, and stores their values in a map, as a script engine
 * holds bindings. No {@code @Parameter} field appears anywhere, and yet it runs
 * through the same {@link Runner}, with the same preprocessors and the same
 * result.
 * </p>
 *
 * @author Curtis Rueden
 */
public class ScriptLikeExecutableTest {

	/** A parameter declared by name and type, as a script header does. */
	private static class ScriptMember<T> implements Member<T> {

		private final String key;
		private final Type type;
		private final ItemIO io;

		ScriptMember(final String key, final Type type, final ItemIO io) {
			this.key = key;
			this.type = type;
			this.io = io;
		}

		@Override
		public String key() {
			return key;
		}

		@Override
		public Type type() {
			return type;
		}

		@Override
		public ItemIO getIOType() {
			return io;
		}

		@Override
		public MemberInstance<T> createInstance(final Object o) {
			@SuppressWarnings("unchecked")
			final Map<String, Object> bindings = (Map<String, Object>) o;
			return new MemberInstance<>() {

				@Override
				public Member<T> member() {
					return ScriptMember.this;
				}

				@Override
				public boolean isReadable() {
					return true;
				}

				@Override
				public boolean isWritable() {
					return true;
				}

				@Override
				@SuppressWarnings("unchecked")
				public T get() {
					return (T) bindings.get(key);
				}

				@Override
				public void set(final Object value) {
					bindings.put(key, value);
				}
			};
		}
	}

	/** Stands in for a script: parameters from a header, values in bindings. */
	private static class FakeScript implements Executable {

		private final List<Member<?>> members = List.of( //
			new ScriptMember<String>("name", String.class, ItemIO.INPUT), //
			new ScriptMember<String>("shout", String.class, ItemIO.OUTPUT));

		@Override
		public String name() {
			return "greet.groovy";
		}

		@Override
		public Struct struct() {
			return () -> members;
		}

		@Override
		public ExecutableInstance create() {
			final Map<String, Object> bindings = new HashMap<>();
			final StructInstance<?> parameters = struct().createInstance(bindings);
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
					// As a script engine would: read bindings, write bindings.
					bindings.put("shout", ("hello " + bindings.get("name"))
						.toUpperCase());
				}
			};
		}
	}

	@Test
	public void testAScriptShapedExecutableRuns() throws Exception {
		final Runner runner = Runner.of(List.of(), List.of());
		final ExecutionResult result = runner.run(new FakeScript(), Map.of("name",
			"ada")).get(5, TimeUnit.SECONDS);

		assertTrue(result.isCompleted());
		assertEquals(Map.of("shout", "HELLO ADA"), result.outputs());
	}

	/** It reaches the same preprocessors, and can be declined the same way. */
	@Test
	public void testProcessorsApplyToScriptsToo() throws Exception {
		final List<String> seen = new ArrayList<>();
		final Preprocessor filler = execution -> {
			seen.add(((ExecutableInstance) execution.executable()).executable()
				.name());
			execution.instance().member("name").set("grace");
		};
		final Runner runner = Runner.of(List.of(filler), List.of());

		final ExecutionResult result = runner.run(new FakeScript(), Map.of()).get(5,
			TimeUnit.SECONDS);
		assertEquals(List.of("greet.groovy"), seen);
		assertEquals(Map.of("shout", "HELLO GRACE"), result.outputs());
	}

	/** Each run gets its own bindings, so concurrent runs cannot collide. */
	@Test
	public void testEachRunHasItsOwnState() throws Exception {
		final FakeScript script = new FakeScript();
		final Runner runner = Runner.of(List.of(), List.of());

		final ExecutionResult first = runner.run(script, Map.of("name", "ada")).get(
			5, TimeUnit.SECONDS);
		final ExecutionResult second = runner.run(script, Map.of("name", "grace"))
			.get(5, TimeUnit.SECONDS);

		assertEquals(Map.of("shout", "HELLO ADA"), first.outputs());
		assertEquals(Map.of("shout", "HELLO GRACE"), second.outputs());
	}
}
