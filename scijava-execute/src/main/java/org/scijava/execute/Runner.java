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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.scijava.struct.MemberInstance;
import org.scijava.struct.StructInstance;

/**
 * Runs objects through a chain of {@link Preprocessor}s and
 * {@link Postprocessor}s.
 * <p>
 * {@link #run} is always asynchronous, returning a
 * {@code Future<ExecutionResult>}: the future carries the asynchrony and
 * cancellation, and the result carries what happened. A caller wanting to
 * block writes {@code run(...).get()} - one verb covers both.
 * </p>
 * <p>
 * Cancellation is thread interruption throughout: cancelling the future
 * interrupts the run, which stops between processors and inside any work that
 * checks for it.
 * </p>
 *
 * @author Curtis Rueden
 */
public class Runner {

	private final ExecutorService executor;
	private final List<Preprocessor> preprocessors;
	private final List<Postprocessor> postprocessors;

	/**
	 * Creates a runner with the given processor chain.
	 *
	 * @param executor where runs happen; the caller owns its lifecycle
	 * @param preprocessors preparation steps, applied highest priority first
	 * @param postprocessors output handling, applied highest priority first
	 */
	public Runner(final ExecutorService executor,
		final List<Preprocessor> preprocessors,
		final List<Postprocessor> postprocessors)
	{
		this.executor = executor;
		this.preprocessors = sorted(preprocessors, Preprocessor::priority);
		this.postprocessors = sorted(postprocessors, Postprocessor::priority);
	}

	/**
	 * Creates a runner with its own executor, whose threads are daemons so
	 * that they never hold the JVM open.
	 */
	public static Runner of(final List<Preprocessor> preprocessors,
		final List<Postprocessor> postprocessors)
	{
		return new Runner(daemonExecutor(), preprocessors, postprocessors);
	}

	/**
	 * Runs the given executable: preprocess, run, postprocess.
	 *
	 * @param executable what to run
	 * @param inputs values for its input parameters, by name
	 * @return the outcome - outputs, or why the run was declined
	 */
	public Future<ExecutionResult> run(final Executable executable,
		final Map<String, Object> inputs)
	{
		return executor.submit(() -> execute(executable, inputs));
	}

	/**
	 * Runs a Java class whose {@link Parameter} fields declare its parameters.
	 * Sugar for the common case; the object itself is used, rather than a fresh
	 * instance.
	 *
	 * @param executable the object to run
	 * @param inputs values for its input parameters, by name
	 * @return the outcome - outputs, or why the run was declined
	 */
	public Future<ExecutionResult> run(final Runnable executable,
		final Map<String, Object> inputs)
	{
		return run(Executables.executableOf(executable), inputs);
	}

	/** Gets the preprocessors, highest priority first. */
	public List<Preprocessor> preprocessors() {
		return List.copyOf(preprocessors);
	}

	/** Gets the postprocessors, highest priority first. */
	public List<Postprocessor> postprocessors() {
		return List.copyOf(postprocessors);
	}

	// -- Helper methods --

	private ExecutionResult execute(final Executable executable,
		final Map<String, Object> inputs)
	{
		final ExecutableInstance instance = executable.create();
		final DefaultExecution execution = new DefaultExecution(instance, //
			Executables.bind(instance.parameters(), inputs, false));

		for (final Preprocessor preprocessor : preprocessors) {
			Executables.checkCancellation();
			preprocessor.process(execution);
			if (execution.isDeclined()) {
				// NB: later preprocessors are skipped -- the run is off.
				return ExecutionResult.declined(execution.reason().orElse(null),
					preprocessor);
			}
		}

		// NB: preprocessors may have supplied missing inputs, so requirements
		// are checked here rather than at binding time.
		checkRequiredInputs(execution.instance());

		Executables.checkCancellation();
		instance.run();
		Executables.checkCancellation();

		execution.complete(Executables.outputs(execution.instance()));
		for (final Postprocessor postprocessor : postprocessors) {
			postprocessor.process(execution);
		}
		return ExecutionResult.completed(execution.outputs());
	}

	private static void checkRequiredInputs(final StructInstance<?> instance) {
		for (final MemberInstance<?> member : instance.members()) {
			if (!member.member().isInput()) continue;
			if (!member.member().isRequired()) continue;
			if (member.get() == null) {
				throw new IllegalArgumentException("Missing required parameter: " + //
					member.member().key() + ". No preprocessor supplied it.");
			}
		}
	}

	private static <P> List<P> sorted(final List<P> processors,
		final java.util.function.ToDoubleFunction<P> priority)
	{
		final List<P> list = new ArrayList<>(processors);
		list.sort(Comparator.comparingDouble(priority).reversed());
		return list;
	}

	private static ExecutorService daemonExecutor() {
		final AtomicLong counter = new AtomicLong();
		final ThreadFactory factory = r -> {
			final Thread thread = new Thread(r, "scijava-execute-" + counter
				.incrementAndGet());
			thread.setDaemon(true);
			return thread;
		};
		return Executors.newCachedThreadPool(factory);
	}

	/** The execution handed to each processor. */
	private static class DefaultExecution implements Execution {

		private final ExecutableInstance instance;
		private final StructInstance<?> parameters;
		private String reason;
		private boolean declined;
		private Map<String, Object> outputs = Map.of();

		DefaultExecution(final ExecutableInstance instance,
			final StructInstance<?> parameters)
		{
			this.instance = instance;
			this.parameters = parameters;
		}

		void complete(final Map<String, Object> outputs) {
			this.outputs = outputs;
		}

		@Override
		public Object executable() {
			return instance;
		}

		@Override
		public StructInstance<?> instance() {
			return parameters;
		}

		@Override
		public void decline(final String reason) {
			this.declined = true;
			this.reason = reason;
		}

		@Override
		public boolean isDeclined() {
			return declined;
		}

		@Override
		public Optional<String> reason() {
			return Optional.ofNullable(reason);
		}

		@Override
		public Map<String, Object> outputs() {
			return outputs;
		}
	}
}
