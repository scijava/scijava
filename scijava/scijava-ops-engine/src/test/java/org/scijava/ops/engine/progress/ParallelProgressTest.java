
package org.scijava.ops.engine.progress;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;
import org.scijava.plugin.Plugin;

/**
 * Tests progress reporting in parallel situations
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = OpCollection.class)
public class ParallelProgressTest extends AbstractTestEnvironment {

	/**
	 * An Op that utilizes parallel progress reporting in one of its stages
	 */
	@OpField(names = "test.parallelProgressReporting")
	public final BiFunction<Integer, Integer, Integer> parallelProgressOp = (
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
	 * An Op that parallelizes its subtasks
	 * 
	 * @param dep the {@link OpDependency}
	 * @param numThreads the number of threads upon which to run {@code dep}
	 * @param iterationsPerThread the arugment passed to {@code dep}
	 * @return {@code numThreads * iterationsPerThread}
	 */
	@OpMethod(names = "test.parallelDependencyReporting", type = BiFunction.class)
	public static Integer parallelDepOp(@OpDependency(
		name = "test.progressReporter") Function<Integer, Integer> dep,
		Integer numThreads, Integer iterationsPerThread)
	{

		Progress.defineTotalProgress(0, numThreads);
		Thread[] threadArr = new Thread[numThreads];

		for (int i = 0; i < numThreads; i++) {
			threadArr[i] = new Thread(() -> {
				dep.apply(iterationsPerThread);
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
	 * Tests the ability for an Op to update its progress (specifically its stage
	 * progress) in parallel
	 */
	@Test
	public void testParallelProgressUpdating() {
		BiFunction<Integer, Integer, Integer> op = ops.op(
			"test.parallelProgressReporting").inType(Integer.class, Integer.class)
			.outType(Integer.class).function();
		int numThreads = 4;
		int iterationsPerThread = 1000;
		Progress.addListener(op, new ProgressListener() {

			double lastProgress = 0;

			@Override
			public void acknowledgeUpdate(Task task) {
				double progress = task.progress();
				Assert.assertTrue(lastProgress <= progress);
				lastProgress = progress;
			}

		});
		op.apply(numThreads, iterationsPerThread);
	}

	/**
	 * Tests the ability for an Op to update its progress (specifically its
	 * subtask progress) in parallel
	 */
	@Test
	public void testParallelDependencyUpdating() {
		BiFunction<Integer, Integer, Integer> op = ops.op(
			"test.parallelDependencyReporting").inType(Integer.class, Integer.class)
			.outType(Integer.class).function();
		int numThreads = 4;
		int iterationsPerThread = 1000;
		Progress.addListener(op, new ProgressListener() {

			double lastProgress = 0;

			@Override
			public void acknowledgeUpdate(Task task) {
				// TODO: Can we do better? Is there a way to guarantee that the
				// progress will not be updated again in between when we enter the
				// method and when we ask for the progress?
				double progress = task.progress();
				Assert.assertTrue(lastProgress <= progress);
				lastProgress = progress;
			}

		});
		op.apply(numThreads, iterationsPerThread);
	}

}
