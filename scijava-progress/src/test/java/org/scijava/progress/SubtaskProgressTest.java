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

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests progress reporting for Task with subtasks
 *
 * @author Gabriel Selzer
 */
public class SubtaskProgressTest {

	/**
	 * Function that updates its progress {@code iterations} times during its
	 * operation.
	 */
	public final Function<Integer, Integer> iterator = new Function<>() {
		private int i = 1;

		@Override
		public Integer apply(Integer iterations) {
			Task t = new Task(this, "Pass " + i++ + " on dependency");
			t.start();
			// set up progress reporter
			t.defineTotalProgress(1);
			t.setStageMax(iterations);

			for (int i = 0; i < iterations; i++) {
				t.update();
			}
			t.complete();
			return iterations;
		}
	};

	/**
	 * Tests the ability of an Task to update its progress when a dependency
	 * updates its progresgs
	 */
	@Test
	public void testComplexDependentReporter() {
		// obtain the Task
		Function<Integer, Integer> progressible = //
			new DependentComplexProgressReportingTask(iterator);

		int numIterations = 100;
		String id = "Running a complex Task with subtasks";
		ProgressListeners.addListener(progressible, new ProgressListener() {

			int subtasksCompleted = 0;
			double middleStageCurrent = 1;
			final double middleStageMax = numIterations;
			boolean started = false;

			@Override
			public void acknowledgeUpdate(Task task) {
				if (!started) {
					started = true;
					return;
				}
				if (subtasksCompleted == 0) {
					Assertions.assertEquals(1. / 3, task.progress(), 1e-6);
					subtasksCompleted++;
				}
				else if (middleStageCurrent <= middleStageMax) {
					Assertions.assertEquals((1. + middleStageCurrent++ / middleStageMax) /
						3, task.progress(), 1e-6);
				}
				else {
					Assertions.assertEquals(1., task.progress(), 1e-6);
				}

				Assertions.assertEquals(id, task.description());

			}

		});
		progressible.apply(numIterations);
	}

	/**
	 * Tests the ability of an Task to update its progress <b>both</b> when it and
	 * when its dependencies update their progress
	 */
	@Test
	public void testDependentReporter() {
		// obtain the Task
		Function<Integer, Integer> progressible = //
			new DependentProgressReportingTask(iterator);

		int numIterations = 100;
		String id = "Running a task with subtasks";
		ProgressListeners.addListener(progressible, new ProgressListener() {

			double currentIterations = 0;
			final double maxIterations = 3;

			@Override
			public void acknowledgeUpdate(Task task) {
				if (!task.isComplete()) {
					Assertions.assertEquals( //
						Math.min(currentIterations, maxIterations) / maxIterations, //
						task.progress(), //
						1e-6 //
					);
					currentIterations++;
				}
				Assertions.assertEquals(id, task.description());
			}

		});
		progressible.apply(numIterations);
	}

}

/**
 * Task used to test progress reporting. This task calls the dependency
 * <b>and</b> and does processing on its own.
 *
 * @author Gabriel Selzer
 */
class DependentComplexProgressReportingTask implements
	Function<Integer, Integer>
{

	private static String id = "Running a complex Task with subtasks";
	private Function<Integer, Integer> dependency;

	public DependentComplexProgressReportingTask(
		Function<Integer, Integer> dependency)
	{
		this.dependency = dependency;
	}

	@Override
	public Integer apply(Integer t) {
		Task task = new Task(this, id);
		task.defineTotalProgress(1, 2);
		task.start();

		dependency.apply(t);

		task.setStageMax(t);
		for (int i = 0; i < t; i++) {
			task.update();
		}

		dependency.apply(t);
		task.complete();
		return 3 * t;
	}
}

/**
 * Task used to test progress reporting. This task <b>only</b> calls the
 * dependency, and does no processing on its own.
 *
 * @author Gabriel Selzer
 */
class DependentProgressReportingTask implements Function<Integer, Integer> {

	private Function<Integer, Integer> dependency;

	public DependentProgressReportingTask(Function<Integer, Integer> dependency) {
		this.dependency = dependency;
	}

	@Override
	public Integer apply(Integer t) {
		Task task = new Task(this, "Running a task with subtasks");
		task.defineTotalProgress(0, 3);
		task.start();
		for (int i = 0; i < 3; i++) {
			dependency.apply(t);
		}
		task.complete();
		return 3 * t;
	}

}
