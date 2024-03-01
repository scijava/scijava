/*-
 * #%L
 * SciJava Progress: An Interrupt-Based Framework for Progress Reporting.
 * %%
 * Copyright (C) 2021 - 2023 SciJava developers.
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

package org.scijava.progress;

import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests a number of basic progress reporting scenarios.
 *
 * @author Gabriel Selzer
 */
public class DefaultProgressTestListeners {

	private static final long NUM_ITERATIONS = 100;
	private static final long NUM_STAGES = 10;

	/**
	 * Tests progress listening when an progressible {@link Object} never
	 * explicitly updates its own progress. We thus expect to be notified
	 * <b>only</b> when the {@code Object} terminates.
	 */
	@Test
	public void testUpdateWithoutDefinition() {
		Supplier<Long> progressible = () -> {
			int foo = 0;
			for (int i = 0; i < NUM_ITERATIONS; i++) {
				foo = foo + 1;
			}
			return NUM_ITERATIONS;
		};
		// Add the ProgressListener
		var listener = new TestSuiteProgressListener(0);
		ProgressListeners.addListener(progressible, listener);
		// Register the Task
		Task t = new Task(progressible);
		// Run the Task
		t.start();
		progressible.get();
		t.complete();
		// Complete the Task
		Assertions.assertTrue(listener.isComplete());
	}

	/**
	 * Tests progress listening on a simple task with only one processing stage
	 * and no dependencies.
	 */
	@Test
	public void testSingleStageReporter() {
		// Define the task
		Supplier<Long> progressible = new Supplier<>() {

			private final Task t = new Task(this);

			@Override
			public Long get() {
				t.start();
				// set up Task
				t.defineTotalProgress(1);

				// run
				t.setStageMax(NUM_ITERATIONS);
				for (int i = 0; i < NUM_ITERATIONS; i++) {
					t.update();
				}

				// complete
				t.complete();
				return NUM_ITERATIONS;
			}
		};

		// Add the ProgressListener
		var listener = new TestSuiteProgressListener(NUM_ITERATIONS);
		ProgressListeners.addListener(progressible, listener);
		// Run the Progressible
		progressible.get();
		Assertions.assertTrue(listener.isComplete());
	}

	/**
	 * Tests running a progressible multiple times, and ensures progress is reset
	 * each time.
	 */
	@Test
	public void testMultipleExecutions() {
		Supplier<Long> progressible = new Supplier<Long>() {

			private final Task t = new Task(this);
			@Override
			public Long get() {
				t.start();
				// set up Task
				t.reset();
				t.defineTotalProgress(1);

				t.setStageMax(NUM_ITERATIONS);
				for (int i = 0; i < NUM_ITERATIONS; i++) {
					t.update();
				}
				t.complete();
				return NUM_ITERATIONS;
			}
		};
		// Add the ProgressListener
		var listener = new TestSuiteProgressListener(NUM_ITERATIONS);
		ProgressListeners.addListener(progressible, listener);
		// Run the Task
		progressible.get();
		// Complete the Task
		Assertions.assertTrue(listener.isComplete());
		// Reset the listener
		listener.reset();
		// Run the Task
		progressible.get();
		// Complete the Task
		Assertions.assertTrue(listener.isComplete());
	}

	/**
	 * Tests progress listening on a simple task with multiple processing stages
	 * and no dependencies.
	 */
	@Test
	public void testMultiStageReporter() {
		// Define the task
		Supplier<Long> progressible = new Supplier<Long>() {

			private final Task t = new Task(this);
			@Override
			public Long get() {
				t.start();
				// set up progress reporter
				t.defineTotalProgress(NUM_STAGES);
				for (int j = 0; j < NUM_STAGES; j++) {
					t.setStageMax(NUM_ITERATIONS);

					for (int i = 0; i < NUM_ITERATIONS; i++) {
						t.update();
					}
				}
				t.complete();
				return NUM_STAGES * NUM_ITERATIONS;
			}
		};

		// Add the ProgressListener
		var expected = NUM_ITERATIONS * NUM_STAGES;
		var listener = new TestSuiteProgressListener(expected);
		ProgressListeners.addListener(progressible, listener);
		// Run the Task
		progressible.get();
		// Complete the Task
		Assertions.assertTrue(listener.isComplete());
	}

	/**
	 * Test that {@link ProgressListener}s added at a global level can listen to a
	 * progressible object without being explicitly linked
	 */
	@Test
	public void testGlobalProgressListener() {
		var id = "This is a global listener";

		// Define the task
		Supplier<Long> progressible = new Supplier<>() {

			private final Task task = new Task(this, id);

			@Override
			public Long get() {
				task.start();
				// set up progress reporter
				task.defineTotalProgress(1);
				task.setStageMax(NUM_ITERATIONS);

				for (int i = 0; i < NUM_ITERATIONS; i++) {
					task.update();
				}
				task.complete();
				return NUM_ITERATIONS;
			}
		};
		// Add the ProgressListener
		var listener = new TestSuiteProgressListener(NUM_ITERATIONS, id);
		ProgressListeners.addGlobalListener(listener);
		// Run the Task
		progressible.get();
		Assertions.assertTrue(listener.isComplete());
	}

	/**
	 * Test that {@link ProgressListener}s added at a global level can determine
	 * whether to listen to a {@link Task} by looking at its description.
	 */
	@Test
	public void testProgressListenerIdentifiers() {
		var goodId = "This one should be listened to";
		var badId = "This one should not be listened to";
		// Define the task
		Supplier<Long> progressible = new Supplier<Long>() {

			private final Task t = new Task(this, goodId);
			@Override
			public Long get() {
				t.start();
				t.defineTotalProgress(1);
				// set up progress reporter
				t.setStageMax(NUM_ITERATIONS);

				for (int i = 0; i < NUM_ITERATIONS; i++) {
					t.update();
				}
				t.complete();
				return NUM_ITERATIONS;
			}
		};
		// Add a ProgressListener that should acknowledge this task
		var goodListener = new TestSuiteProgressListener(NUM_ITERATIONS, goodId);
		ProgressListeners.addGlobalListener(goodListener);
		// Create a second global listener that will not acknowledge this task
		var badListener = new TestSuiteProgressListener(NUM_ITERATIONS, badId);
		ProgressListeners.addGlobalListener(badListener);
		// Run the Task
		progressible.get();
		Assertions.assertTrue(goodListener.isComplete());
		// Assert that badListener didn't hear anything
		Assertions.assertEquals(0., badListener.progress(), 1e-6);
	}

}

