/*-
 * #%L
 * An interrupt-based subsystem for progress reporting.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Tests progress reporting for Task with subtasks
 *
 * @author Gabriel Selzer
 */
public class SubtaskProgressTest {

	/**
	 * Function that updates its progress {@code iterations} times during its
	 * operation.
	 *
	 * @input iterations the number of times the task should update its progress.
	 * @output the number of iterations performed
	 */
	public final Function<Integer, Integer> iterator = (iterations) -> {
		// set up progress reporter
		Progress.defineTotal(iterations);

		for (int i = 0; i < iterations; i++) {
			Progress.update();
		}
		return iterations;
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

		int numIterations = 2;
		String id = "Running a complex Task with subtasks";
		Progress.addListener(progressible, new Consumer<>() {

			int itr = 0;
			/* Expected list of progress pings */
			final double[] expProgress = new double[] { 0.0, // Register - parent
				0.0, // Register - child 1
				1.0 / 6, // Update 1 - child 1
				2.0 / 6, // Update 2 - child 1
				2.0 / 6, // Complete - child 1
				3.0 / 6, // Update 1 - parent
				4.0 / 6, // Update 2 - parent
				4.0 / 6, // Register - child 2
				5.0 / 6, // Update 1 - child 1
				1.0, // Update 2 - child 2
				1.0, // Complete - child 2
				1.0 // Complete - parent
			};

			@Override
			public void accept(Task task) {
				// Check that the progress is what we expect
				Assertions.assertEquals( //
					expProgress[itr++], //
					task.progress(), //
					1e-6 //
				);
				// Check that, once the parent is complete, the task is complete
				Assertions.assertEquals( //
					(itr == expProgress.length), //
					task.isComplete() //
				);
			}

		});
		Progress.register(progressible, id);
		progressible.apply(numIterations);
		Progress.complete();
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

		int numIterations = 2;
		String id = "Running a task with subtasks";
		Progress.addListener(progressible, new Consumer<>() {

			int itr = 0;
			/* Expected list of progress pings */
			final double[] expProgress = new double[] { 0.0, // Register - parent
				0.0, // Register - child 1
				1.0 / 6, // Update 1 - child 1
				2.0 / 6, // Update 2 - child 1
				2.0 / 6, // Complete - child 1
				2.0 / 6, // Register - child 2
				3.0 / 6, // Update 1 - child 2
				4.0 / 6, // Update 2 - child 2
				4.0 / 6, // Complete - child 2
				4.0 / 6, // Register - child 3
				5.0 / 6, // Update 1 - child 3
				1.0, // Update 2 - child 3
				1.0, // Complete - child 3
				1.0 // Complete - parent
			};

			@Override
			public void accept(Task task) {
				// Check that the progress is what we expect
				Assertions.assertEquals( //
					expProgress[itr++], //
					task.progress(), //
					1e-6 //
				);
				// Check that, once the parent is complete, the task is complete
				Assertions.assertEquals( //
					(itr == expProgress.length), //
					task.isComplete() //
				);
			}

		});
		Progress.register(progressible, id);
		progressible.apply(numIterations);
		Progress.complete();
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

	private final Function<Integer, Integer> dependency;

	public DependentComplexProgressReportingTask(
		Function<Integer, Integer> dependency)
	{
		this.dependency = dependency;
	}

	@Override
	public Integer apply(Integer t) {
		Progress.defineTotal(t, 2);
		callDep(t, 1);

		for (int i = 0; i < t; i++) {
			Progress.update();
		}

		callDep(t, 2);
		return 3 * t;
	}

	private void callDep(Integer t, Integer i) {
		Progress.register(dependency, "Pass " + i + " on dependency");
		dependency.apply(t);
		Progress.complete();
	}

}

/**
 * Task used to test progress reporting. This task <b>only</b> calls the
 * dependency, and does no processing on its own.
 *
 * @author Gabriel Selzer
 */
class DependentProgressReportingTask implements Function<Integer, Integer> {

	private final Function<Integer, Integer> dependency;

	public DependentProgressReportingTask(Function<Integer, Integer> dependency) {
		this.dependency = dependency;
	}

	@Override
	public Integer apply(Integer t) {
		Progress.defineTotal(0, 3);
		for (int i = 0; i < 3; i++) {
			Progress.register(dependency, "Pass " + i + " of dependency");
			dependency.apply(t);
			Progress.complete();
		}
		return 3 * t;
	}

}
