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

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests a number of basic progress reporting scenarios.
 *
 * @author Gabriel Selzer
 */
public class DefaultProgressTest {

	/**
	 * Function that never operates its progress during its operation.
	 * 
	 * @input size the number of indices desired in the returned array
	 * @output an {@code int[]} of size {@code size}
	 */
	public final Function<Integer, int[]> arrayCreator2 = (size) -> {
		int[] arr = new int[size];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = 1;
		}
		return arr;
	};

	/**
	 * BiFunction that updates its progress
	 * {@code numIterations*iterationsPerStage} times during its operation.
	 * 
	 * @input numStages the number of defined stages this task should perform
	 * @input iterationsPerStage the number of times the task should update its
	 *        progress.
	 * @output the number of <b>total</b> iterations performed
	 */
	public final BiFunction<Integer, Integer, Integer> doubleIterator = (
		numStages, iterationsPerStage) -> {
		// set up progress reporter
		Progress.defineTotalProgress(numStages);
		for (int j = 0; j < numStages; j++) {
			Progress.setStageMax(iterationsPerStage);

			for (int i = 0; i < iterationsPerStage; i++) {
				Progress.update();
			}
		}
		return numStages * iterationsPerStage;
	};

	/**
	 * Function that updates its progress {@code iterations} times during its
	 * operation.
	 * 
	 * @input iterations the number of times the task should update its progress.
	 * @output the number of iterations performed
	 */
	public final Function<Integer, Integer> iterator = (iterations) -> {
		// set up progress reporter
		Progress.defineTotalProgress(1);
		Progress.setStageMax(iterations);

		for (int i = 0; i < iterations; i++) {
			Progress.update();
		}
		return iterations;
	};

	/**
	 * Tests progress listening when an progressible {@link Object} never
	 * explicitly updates its own progress. We thus expect to be notified
	 * <b>only</b> when the {@code Object} terminates.
	 */
	@Test
	public void testUpdateWithoutDefinition() {
		Function<Integer, int[]> progressible = arrayCreator2;

		Progress.addListener(progressible, new ProgressListener() {

			int timesUpdated = 0;

			@Override
			public void acknowledgeUpdate(Task task) {
				if (timesUpdated > 0) Assertions.fail(
					"If the Task doesn't call update, the listener should only be called upon the Task's completion!");
				timesUpdated++;
				Assertions.assertEquals(task.progress(), 1., 1e-6);
			}

		});
		Progress.register(progressible);
		progressible.apply(3);
		Progress.complete();
	}

	/**
	 * Tests progress listening on a simple task with only one processing stage
	 * and no dependencies.
	 */
	@Test
	public void testSimpleReporter() {
		// obtain the task
		Function<Integer, Integer> progressible = iterator;

		int numIterations = 100;
		Progress.addListener(progressible, new ProgressListener() {

			double currentIterations = 1;
			double maxIterations = numIterations;

			@Override
			public void acknowledgeUpdate(Task task) {
				Assertions.assertEquals(Math.min(1., currentIterations++ /
					maxIterations), task.progress(), 1e-6);
			}
		});
		Progress.register(progressible);
		progressible.apply(numIterations);
		Progress.complete();

	}

	/**
	 * Tests progress listening on a simple task with multiple processing stages
	 * and no dependencies.
	 */
	@Test
	public void testMultiStageReporter() {
		// obtain the task
		BiFunction<Integer, Integer, Integer> progressible = doubleIterator;

		int numIterations = 100;
		int numStages = 10;
		Progress.addListener(progressible, new ProgressListener() {

			double currentIterations = 1;
			double maxIterations = numStages * numIterations;

			@Override
			public void acknowledgeUpdate(Task task) {
				Assertions.assertEquals(Math.min(1., currentIterations++ /
					maxIterations), task.progress(), 1e-6);
			}
		});
		Progress.register(progressible);
		progressible.apply(numStages, numIterations);
		Progress.complete();
	}

	@Test
	public void testGlobalProgressListener() {
		BiFunction<Integer, Integer, Integer> progressible = doubleIterator;

		int numIterations = 4;
		int numStages = 2;


		ProgressListener global = new ProgressListener() {

			double currentIterations = 1;
			final double maxIterations = numStages * numIterations;
			boolean alreadyCompleted = false;

			@Override
			public void acknowledgeUpdate(final Task task) {
				if (!task.isComplete()) {
					var expected = currentIterations / maxIterations;
					var actual = task.progress();
					Assertions.assertEquals(expected, actual, 1e-6);
					currentIterations++;
				}
				else {
					// Assert complete listen only happens once, when iterations = max
					Assertions.assertFalse(alreadyCompleted);
					Assertions.assertEquals(currentIterations, maxIterations);
					alreadyCompleted = true;
				}
			}
		};
		Progress.addGlobalListener(global);

		Progress.register(progressible);
		progressible.apply(numStages, numIterations);
		Progress.complete();
	}


}
