
package org.scijava.ops.engine.progress;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class DefaultProgressTest extends AbstractTestEnvironment {

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

	@Test
	public void testSimpleReporter() throws InterruptedException {
		// obtain the Op
		Function<Integer, Integer> op = //
			ops.op("test.progressReporter") //
				.inType(Integer.class) //
				.outType(Integer.class) //
				.function();

		int numIterations = 100;
		Progress.addListener(op, (t) -> {
			testProgress(t.progress(), numIterations);
		});
		Thread t = new Thread(() -> op.apply(numIterations));
		t.start();
		t.join();
		Assert.assertEquals(numIterations, this.numUpdates);

	}

	@Test
	public void testMultiStageReporter() throws InterruptedException {
		// obtain the Op
		BiFunction<Integer, Integer, Integer> op = //
			ops.op("test.progressReporter") //
				.inType(Integer.class, Integer.class) //
				.outType(Integer.class) //
				.function();

		int numIterations = 100;
		int numStages = 10;
		Progress.addListener(op, (t) -> {
			testProgress(t.progress(), numStages * numIterations);
		});
		Thread t = new Thread(() -> op.apply(numStages, numIterations));
		t.start();
		t.join();
		Assert.assertEquals(numStages * numIterations, this.numUpdates);

	}

	@Test
	public void testDependentReporter() throws InterruptedException {
		// obtain the Op
		Function<Integer, Integer> op = //
			ops.op("test.dependentProgressReporter") //
				.inType(Integer.class) //
				.outType(Integer.class) //
				.function();

		int numIterations = 100;
		Progress.addListener(op, (t) -> {
			testProgress(t.progress(), 3);
		});
		Thread t = new Thread(() -> op.apply(numIterations));
		t.start();
		t.join();
		Assert.assertEquals(3, this.numUpdates);

	}

	private int numUpdates = 0;

	private void testProgress(double progress, int numIterations) {
		Assert.assertEquals((double) ++numUpdates / numIterations, progress, 1e-6);
	}

}

@Plugin(type = Op.class, name = "test.dependentProgressReporter")
class DependentProgressReportingOp implements Function<Integer, Integer> {

	@OpDependency(name = "test.progressReporter")
	private Function<Integer, Integer> dependency;

	@Override
	public Integer apply(Integer t) {
		Progress.defineTotalProgress(0, 3);
		for (int i = 0; i < 3; i++) {
			dependency.apply(t);
		}
		return 3 * t;
	}

}
