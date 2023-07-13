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
public class DefaultProgressTest {

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
		Progress.addListener(progressible, listener);
		// Register the Task
		Progress.register(progressible);
		// Run the Task
		progressible.get();
		// Complete the Task
		Progress.complete();
		Assertions.assertTrue(listener.isComplete());
	}

	/**
	 * Tests progress listening on a simple task with only one processing stage
	 * and no dependencies.
	 */
	@Test
	public void testSingleStageReporter() {
		// Define the task
		Supplier<Long> progressible = () -> {
			// set up progress reporter
			Progress.defineTotalProgress(1);
			Progress.setStageMax(NUM_ITERATIONS);

			for (int i = 0; i < NUM_ITERATIONS; i++) {
				Progress.update();
			}
			return NUM_ITERATIONS;
		};

		// Add the ProgressListener
		var listener = new TestSuiteProgressListener(NUM_ITERATIONS);
		Progress.addListener(progressible, listener);
		// Register the Task
		Progress.register(progressible);
		// Run the Task
		progressible.get();
		// Complete the Task
		Progress.complete();
		Assertions.assertTrue(listener.isComplete());
	}

	/**
	 * Tests running a progressible multiple times, and ensures progress is reset
	 * each time.
	 */
	@Test
	public void testMultipleExecutions() {
		// Define the task
		Supplier<Long> progressible = () -> {
			// set up progress reporter
			Progress.defineTotalProgress(1);
			Progress.setStageMax(NUM_ITERATIONS);

			for (int i = 0; i < NUM_ITERATIONS; i++) {
				Progress.update();
			}
			return NUM_ITERATIONS;
		};
		// Add the ProgressListener
		var listener = new TestSuiteProgressListener(NUM_ITERATIONS);
		Progress.addListener(progressible, listener);
		// Register the Task
		Progress.register(progressible);
		// Run the Task
		progressible.get();
		// Complete the Task
		Progress.complete();
		Assertions.assertTrue(listener.isComplete());
		// Reset the listener
		listener.reset();
		// Register the Task
		Progress.register(progressible);
		// Run the Task
		progressible.get();
		// Complete the Task
		Progress.complete();
	}

	/**
	 * Tests progress listening on a simple task with multiple processing stages
	 * and no dependencies.
	 */
	@Test
	public void testMultiStageReporter() {
		// Define the task
		Supplier<Long> progressible = () -> {
			// set up progress reporter
			Progress.defineTotalProgress(NUM_STAGES);
			for (int j = 0; j < NUM_STAGES; j++) {
				Progress.setStageMax(NUM_ITERATIONS);

				for (int i = 0; i < NUM_ITERATIONS; i++) {
					Progress.update();
				}
			}
			return NUM_STAGES * NUM_ITERATIONS;
		};

		// Add the ProgressListener
		var expected = NUM_ITERATIONS * NUM_STAGES;
		var listener = new TestSuiteProgressListener(expected);
		Progress.addListener(progressible, listener);
		// Register the Task
		Progress.register(progressible);
		// Run the Task
		progressible.get();
		// Complete the Task
		Progress.complete();
		Assertions.assertTrue(listener.isComplete());
	}

	/**
	 * Test that {@link ProgressListener}s added at a global level can listen to a
	 * progressible object without being explicitly linked
	 */
	@Test
	public void testGlobalProgressListener() {
		// Define the task
		Supplier<Long> progressible = () -> {
			// set up progress reporter
			Progress.defineTotalProgress(1);
			Progress.setStageMax(NUM_ITERATIONS);

			for (int i = 0; i < NUM_ITERATIONS; i++) {
				Progress.update();
			}
			return NUM_ITERATIONS;
		};
		// Add the ProgressListener
		var id = "This is a global listener";
		var listener = new TestSuiteProgressListener(NUM_ITERATIONS, id);
		Progress.addGlobalListener(listener);
		// Register the Task
		Progress.register(progressible, id);
		// Run the Task
		progressible.get();
		// Complete the Task
		Progress.complete();
		Assertions.assertTrue(listener.isComplete());
	}

	/**
	 * Test that {@link ProgressListener}s added at a global level can listen to a
	 * progressible object without being explicitly linked
	 */
	@Test
	public void testProgressListenerIdentifiers() {
		// Define the task
		Supplier<Long> progressible = () -> {
			// set up progress reporter
			Progress.defineTotalProgress(1);
			Progress.setStageMax(NUM_ITERATIONS);

			for (int i = 0; i < NUM_ITERATIONS; i++) {
				Progress.update();
			}
			return NUM_ITERATIONS;
		};
		// Add a ProgressListener that should acknowledge this task
		var goodId = "This one should be listened to";
		var goodListener = new TestSuiteProgressListener(NUM_ITERATIONS, goodId);
		Progress.addGlobalListener(goodListener);
		// Register the Task with goodId
		Progress.register(progressible, goodId);
		// Create a second global listener that will not acknowledge this task
		var badId = "This one should not be listened to";
		var badListener = new TestSuiteProgressListener(NUM_ITERATIONS, badId);
		Progress.addGlobalListener(badListener);
		// Run the Task
		progressible.get();
		// Complete the Task
		Progress.complete();
		Assertions.assertTrue(goodListener.isComplete());
		// Assert that badListener didn't hear anything
		Assertions.assertEquals(0., badListener.progress(), 1e-6);
	}

}
