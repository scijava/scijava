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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.Structs;

/**
 * Runs objects that declare their inputs and outputs with {@link Parameter}.
 * <p>
 * This is the SciJava Common module layer, rebuilt on {@code scijava-struct}:
 * a class describes its inputs and outputs as annotated fields, a caller
 * supplies values by name, and the outputs come back by name.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class Executables {

	private static final FieldParameterMemberParser PARSER =
		new FieldParameterMemberParser();

	private Executables() {
		// NB: prevent instantiation of utility class.
	}

	/**
	 * Describes the given class as an {@link Executable}.
	 *
	 * @param type a class whose {@link Parameter} fields declare its parameters
	 * @return a description of it
	 */
	public static Executable of(final Class<? extends Runnable> type) {
		return new JavaExecutable(type);
	}

	/**
	 * Describes an already-constructed object as an {@link Executable} whose
	 * {@code create} yields that same object.
	 * <p>
	 * NB: unlike {@link #of(Class)}, this cannot give each run its own
	 * instance, so two concurrent runs would share parameter values. It exists
	 * for the simple case of running an object one has in hand.
	 * </p>
	 */
	public static Executable executableOf(final Runnable object) {
		final Struct struct = struct(object.getClass());
		return new Executable() {

			@Override
			public String name() {
				return object.getClass().getName();
			}

			@Override
			public Struct struct() {
				return struct;
			}

			@Override
			public ExecutableInstance create() {
				final StructInstance<?> parameters = struct.createInstance(object);
				return new ExecutableInstance() {

					@Override
					public Executable executable() {
						return Executables.executableOf(object);
					}

					@Override
					public StructInstance<?> parameters() {
						return parameters;
					}

					@Override
					public void run() {
						object.run();
					}
				};
			}
		};
	}

	/** Describes the inputs and outputs of the given class. */
	public static Struct struct(final Class<?> type) {
		return Structs.from(type, type, PARSER);
	}

	/**
	 * Runs the given object, supplying the named inputs and returning the
	 * outputs.
	 * <p>
	 * Cancellation is thread interruption, as everywhere else: an interrupted
	 * run throws {@link CancellationException} rather than returning partial
	 * outputs. Submit to an {@link java.util.concurrent.ExecutorService} and
	 * cancel the {@link java.util.concurrent.Future} to use it.
	 * </p>
	 *
	 * @param executable the object to run
	 * @param inputs values for its input parameters, by name
	 * @return its output parameters, by name
	 * @throws IllegalArgumentException if a required input is missing, or an
	 *           input is not a parameter of this object. NB: a required input
	 *           must be supplied here even if the field already holds a value,
	 *           since a primitive field always holds one
	 */
	public static Map<String, Object> run(final Runnable executable,
		final Map<String, Object> inputs)
	{
		final StructInstance<?> instance = bind(struct(executable.getClass())
			.createInstance(executable), inputs, true);
		checkCancellation();
		executable.run();
		checkCancellation();
		return outputs(instance);
	}

	/**
	 * Gets a {@link Callable} that runs the given object, for submission to an
	 * executor.
	 *
	 * @param executable the object to run
	 * @param inputs values for its input parameters, by name
	 * @return a callable yielding its output parameters, by name
	 */
	public static Callable<Map<String, Object>> callable(
		final Runnable executable, final Map<String, Object> inputs)
	{
		return () -> run(executable, inputs);
	}

	/**
	 * Binds the given input values onto the object's parameters.
	 *
	 * @param requireInputs whether to insist that required inputs be supplied
	 *          now. A {@link Runner} passes false, since a preprocessor may
	 *          supply them later.
	 */
	static StructInstance<?> bind(final StructInstance<?> instance,
		final Map<String, Object> inputs, final boolean requireInputs)
	{
		final Struct struct = instance.struct();

		for (final String key : inputs.keySet()) {
			if (instance.member(key) == null) {
				throw new IllegalArgumentException("No such parameter: " + key + //
					". This object declares " + names(struct));
			}
		}
		for (final MemberInstance<?> member : instance.members()) {
			if (!member.member().isInput()) continue;
			final String key = member.member().key();
			if (inputs.containsKey(key)) {
				member.set(inputs.get(key));
			}
			else if (requireInputs && member.member().isRequired()) {
				// NB: require the value to be supplied, rather than checking
				// whether the field is null. A primitive field is never null -- a
				// missing required `double` would silently run as 0.0.
				throw new IllegalArgumentException("Missing required parameter: " + //
					key);
			}
		}
		return instance;
	}

	/** Collects the object's output values. */
	static Map<String, Object> outputs(final StructInstance<?> instance) {
		final Map<String, Object> outputs = new LinkedHashMap<>();
		for (final MemberInstance<?> member : instance.members()) {
			if (member.member().isOutput()) outputs.put(member.member().key(), //
				member.get());
		}
		return outputs;
	}

	private static String names(final Struct struct) {
		final StringBuilder sb = new StringBuilder();
		for (final Member<?> member : struct) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(member.key());
		}
		return sb.length() == 0 ? "no parameters" : sb.toString();
	}

	static void checkCancellation() {
		// NB: cancellation is thread interruption; restore the flag so callers
		// further up still see it.
		if (Thread.interrupted()) {
			Thread.currentThread().interrupt();
			throw new CancellationException("Execution cancelled");
		}
	}
}
