
package org.scijava.progress;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests progress reporting in parallel situations
 * 
 * @author Gabriel Selzer
 */
public class ParallelProgressTest {

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
	 * A task that utilizes parallel progress reporting in one of its stages
	 */
	public final BiFunction<Integer, Integer, Integer> parallelProgressTask = (
		numThreads, iterationsPerThread) -> {

		Progress.defineTotalProgress(1);
		Progress.setStageMax(numThreads * iterationsPerThread);
		Thread[] threadArr = new Thread[numThreads];

		for (int i = 0; i < numThreads; i++) {
			threadArr[i] = new Thread(() -> {
				for (int j = 0; j < iterationsPerThread; j++) {
					Progress.update();
				}
			});
			threadArr[i].start();
		}
		try {
			for (int i = 0; i < numThreads; i++)
				threadArr[i].join();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	};

	/**
	 * A task that parallelizes its subtasks
	 * 
	 * @param dep the subtask
	 * @param numThreads the number of threads upon which to run {@code dep}
	 * @param iterationsPerThread the arugment passed to {@code dep}
	 * @return {@code numThreads * iterationsPerThread}
	 */
	public static Integer parallelDepTask(Function<Integer, Integer> dep,
		Integer numThreads, Integer iterationsPerThread)
	{

		Progress.defineTotalProgress(0, numThreads);
		Thread[] threadArr = new Thread[numThreads];

		for (int i = 0; i < numThreads; i++) {
			threadArr[i] = new Thread(() -> {
				Progress.register(dep);
				dep.apply(iterationsPerThread);
				Progress.complete();
			});
			threadArr[i].start();
		}
		try {
			for (int i = 0; i < numThreads; i++)
				threadArr[i].join();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return numThreads * iterationsPerThread;
	}

	/**
	 * Tests the ability for a task to update its progress (specifically its stage
	 * progress) in parallel
	 */
	@Test
	public void testParallelProgressUpdating() {
		BiFunction<Integer, Integer, Integer> task = parallelProgressTask;
		int numThreads = 4;
		int iterationsPerThread = 1000;
		Progress.addListener(task, new ProgressListener() {

			double lastProgress = 0;

			@Override
			public void acknowledgeUpdate(Task t) {
				double progress = t.progress();
				Assertions.assertTrue(lastProgress <= progress);
				lastProgress = progress;
			}

		});
		Progress.register(task);
		task.apply(numThreads, iterationsPerThread);
		Progress.complete();
	}

	/**
	 * Tests the ability for a task to update its progress (specifically its
	 * subtask progress) in parallel
	 */
	@Test
	public void testParallelDependencyUpdating() {
		BiFunction<Integer, Integer, Integer> task = (in1, in2) -> parallelDepTask(
			iterator, in1, in2);
		int numThreads = 4;
		int iterationsPerThread = 1000;
		Progress.addListener(task, new ProgressListener() {

			double lastProgress = 0;

			@Override
			public void acknowledgeUpdate(Task t) {
				// TODO: Can we do better? Is there a way to guarantee that the
				// progress will not be updated again in between when we enter the
				// method and when we ask for the progress?
				double progress = t.progress();
				Assertions.assertTrue(lastProgress <= progress);
				lastProgress = progress;
			}

		});
		Progress.register(task);
		task.apply(numThreads, iterationsPerThread);
		Progress.complete();
	}

}
