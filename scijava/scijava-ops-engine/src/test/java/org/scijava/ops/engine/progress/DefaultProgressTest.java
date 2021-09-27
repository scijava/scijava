
package org.scijava.ops.engine.progress;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.Plugin;

/**
 * Tests a number of basic progress reporting scenarios.
 *
 * @author Gabriel Selzer
 */
@Plugin(type = OpCollection.class)
public class DefaultProgressTest extends AbstractTestEnvironment {

	/**
	 * Op that never operates its progress during its operation.
	 * 
	 * @input size the number of indices desired in the returned array
	 * @output an {@code int[]} of size {@code size}
	 */
	@OpField(names = "test.nonProgressibleOp")
	public final Function<Integer, int[]> arrayCreator2 = (size) -> {
		int[] arr = new int[size];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = 1;
		}
		return arr;
	};

	/**
	 * Op that updates its progress {@code numIterations*iterationsPerStage} times
	 * during its operation.
	 * 
	 * @input numStages the number of defined stages this Op should perform
	 * @input iterationsPerStage the number of times the Op should update its
	 *        progress.
	 * @output the number of <b>total</b> iterations performed
	 */
	@OpField(names = "test.progressReporter")
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
	 * Op that updates its progress {@code iterations} times during its operation.
	 * 
	 * @input iterations the number of times the Op should update its progress.
	 * @output the number of iterations performed
	 */
	@OpField(names = "test.progressReporter")
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
	 * Tests progress listening when an Op never explicitly updates its own
	 * progress. We thus expect to be notified <b>only</b> when the Op terminates.
	 */
	@Test
	public void testUpdateWithoutDefinition() {
		Function<Integer, int[]> op = ops.op("test.nonProgressibleOp").inType(
			Integer.class).outType(int[].class).function();
		Progress.addListener(op, new ProgressListener() {

			int timesUpdated = 0;

			@Override
			public void updateProgress(Task task) {
				if (timesUpdated > 0) Assert.fail(
					"If the Op doesn't call update, the listener should only be called upon the Op's completion!");
				timesUpdated++;
				Assert.assertEquals(task.progress(), 1., 1e-6);
			}

		});
		op.apply(3);
	}

	/**
	 * Tests progress listening on a simple Op with only one processing stage and
	 * no dependencies.
	 */
	@Test
	public void testSimpleReporter() {
		// obtain the Op
		Function<Integer, Integer> op = //
			ops.op("test.progressReporter") //
				.inType(Integer.class) //
				.outType(Integer.class) //
				.function();

		int numIterations = 100;
		Progress.addListener(op, new ProgressListener() {

			double currentIterations = 1;
			double maxIterations = numIterations;

			@Override
			public void updateProgress(Task task) {
				Assert.assertEquals(Math.min(1., currentIterations++ / maxIterations),
					task.progress(), 1e-6);
			}
		});
		op.apply(numIterations);

	}

	/**
	 * Tests progress listening on a simple Op with multiple processing stages and
	 * no dependencies.
	 */
	@Test
	public void testMultiStageReporter() {
		// obtain the Op
		BiFunction<Integer, Integer, Integer> op = //
			ops.op("test.progressReporter") //
				.inType(Integer.class, Integer.class) //
				.outType(Integer.class) //
				.function();

		int numIterations = 100;
		int numStages = 10;
		Progress.addListener(op, new ProgressListener() {

			double currentIterations = 1;
			double maxIterations = numStages * numIterations;

			@Override
			public void updateProgress(Task task) {
				Assert.assertEquals(Math.min(1., currentIterations++ / maxIterations),
					task.progress(), 1e-6);
			}
		});
		op.apply(numStages, numIterations);
	}

}
