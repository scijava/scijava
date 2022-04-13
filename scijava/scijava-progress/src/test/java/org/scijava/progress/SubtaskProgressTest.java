
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
	 * Tests the ability of an Task to update its progress when a dependency
	 * updates its progresgs
	 */
	@Test
	public void testComplexDependentReporter() {
		// obtain the Task
		Function<Integer, Integer> progressible = //
			new DependentComplexProgressReportingTask(iterator);

		int numIterations = 100;
		Progress.addListener(progressible, new ProgressListener() {

			int subtasksCompleted = 0;
			double middleStageCurrent = 1;
			double middleStageMax = numIterations;

			@Override
			public void acknowledgeUpdate(Task task) {
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

			}

		});
		Progress.register(progressible);
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

		int numIterations = 100;
		Progress.addListener(progressible, new ProgressListener() {

			double currentIterations = 1;
			double maxIterations = 3;

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

}

/**
 * Task used to test progress reporting. This task calls the dependency
 * <b>and</b> and does processing on its own.
 *
 * @author Gabriel Selzer
 * @see DefaultProgressTest#iterator
 */
class DependentComplexProgressReportingTask implements
	Function<Integer, Integer>
{

	private Function<Integer, Integer> dependency;

	public DependentComplexProgressReportingTask(
		Function<Integer, Integer> dependency)
	{
		this.dependency = dependency;
	}

	@Override
	public Integer apply(Integer t) {
		Progress.defineTotalProgress(1, 2);
		callDep(t);

		Progress.setStageMax(t);
		for (int i = 0; i < t; i++) {
			Progress.update();
		}

		callDep(t);
		return 3 * t;
	}

	private void callDep(Integer t) {
		Progress.register(dependency);
		dependency.apply(t);
		Progress.complete();
	}

}

/**
 * Task used to test progress reporting. This task <b>only</b> calls the
 * dependency, and does no processing on its own.
 *
 * @author Gabriel Selzer
 * @see DefaultProgressTest#iterator
 */
class DependentProgressReportingTask implements Function<Integer, Integer> {

	private Function<Integer, Integer> dependency;

	public DependentProgressReportingTask(Function<Integer, Integer> dependency) {
		this.dependency = dependency;
	}

	@Override
	public Integer apply(Integer t) {
		Progress.defineTotalProgress(0, 3);
		for (int i = 0; i < 3; i++) {
			Progress.register(dependency);
			dependency.apply(t);
			Progress.complete();
		}
		return 3 * t;
	}

}
