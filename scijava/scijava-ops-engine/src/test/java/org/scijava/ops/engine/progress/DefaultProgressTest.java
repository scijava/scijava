
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
	public void testSimpleReporter() {
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
		op.apply(numIterations);

	}

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
		Progress.addListener(op, (t) -> {
			testProgress(t.progress(), numStages * numIterations);
		});
		op.apply(numStages, numIterations);
	}

	@Test
	public void testDependentReporter() {
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
		op.apply(numIterations);

	}

	@Test
	public void testComplexDependentReporter() {
		// obtain the Op
		Function<Integer, Integer> op = //
			ops.op("test.dependentComplexProgressReporter") //
				.inType(Integer.class) //
				.outType(Integer.class) //
				.function();

		int numIterations = 100;
		Progress.addListener(op, (t) -> {
			testComplexProgress(t.progress(), numIterations);
		});
		op.apply(numIterations);
	}

	boolean firstStageComplete = false;

	private void testComplexProgress(double progress, int numIterations) {
		if(!firstStageComplete) {
			Assert.assertEquals(1. / 3, progress, 1e-6);
			firstStageComplete = true;
			return;
		}
		else if(numUpdates < numIterations) {
			double expected = (1. + (double) ++numUpdates / numIterations) / 3;
			Assert.assertEquals(expected, progress, 1e-6);
		}
		else {
			Assert.assertEquals(1., progress, 1e-6);
		}
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

@Plugin(type = Op.class, name = "test.dependentComplexProgressReporter")
class DependentComplexProgressReportingOp implements Function<Integer, Integer> {

	@OpDependency(name = "test.progressReporter")
	private Function<Integer, Integer> dependency;

	@Override
	public Integer apply(Integer t) {
		Progress.defineTotalProgress(1, 2);
		dependency.apply(t);

		Progress.setStageMax(t);
		for (int i = 0; i < t; i++) {
			Progress.update();
		}

		dependency.apply(t);
		return 3 * t;
	}

}
