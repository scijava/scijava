
package org.scijava.ops.engine.progress;

import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;
import org.scijava.plugin.Plugin;

/**
 * Tests progress reporting for Ops with {@link OpDependency}s.
 *
 * @author Gabriel Selzer
 */
public class DependencyProgressTest extends AbstractTestEnvironment {

	/**
	 * Tests the ability of an Op to update its progress when a dependency updates
	 * its progress
	 */
	@Test
	public void testComplexDependentReporter() {
		// obtain the Op
		Function<Integer, Integer> op = //
			ops.op("test.dependentComplexProgressReporter") //
				.inType(Integer.class) //
				.outType(Integer.class) //
				.function();

		int numIterations = 100;
		Progress.addListener(op, new ProgressListener() {

			int subtasksCompleted = 0;
			double middleStageCurrent = 1;
			double middleStageMax = numIterations;

			@Override
			public void updateProgress(Task task) {
				if (subtasksCompleted == 0) {
					Assert.assertEquals(1. / 3, task.progress(), 1e-6);
					subtasksCompleted++;
				}
				else if (middleStageCurrent <= middleStageMax) {
					Assert.assertEquals((1. + middleStageCurrent++ / middleStageMax) / 3,
						task.progress(), 1e-6);
				}
				else {
					Assert.assertEquals(1., task.progress(), 1e-6);
				}

			}

		});
		op.apply(numIterations);
	}

	/**
	 * Tests the ability of an Op to update its progress <b>both</b> when it and
	 * when its dependencies update their progress
	 */
	@Test
	public void testDependentReporter() {
		// obtain the Op
		Function<Integer, Integer> op = //
			ops.op("test.dependentProgressReporter") //
				.inType(Integer.class) //
				.outType(Integer.class) //
				.function();

		int numIterations = 100;
		Progress.addListener(op, new ProgressListener() {

			double currentIterations = 1;
			double maxIterations = 3;

			@Override
			public void updateProgress(Task task) {
				Assert.assertEquals(Math.min(1., currentIterations++ / maxIterations),
					task.progress(), 1e-6);
			}

		});
		op.apply(numIterations);

	}

}

/**
 * Op used to test progress reporting. This op calls the dependency <b>and</b>
 * and does processing on its own.
 *
 * @author Gabriel Selzer
 * @see DefaultProgressTest#iterator
 */
@Plugin(type = Op.class, name = "test.dependentComplexProgressReporter")
class DependentComplexProgressReportingOp implements
	Function<Integer, Integer>
{

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

/**
 * Op used to test progress reporting. This op <b>only</b> calls the dependency,
 * and does no processing on its own.
 *
 * @author Gabriel Selzer
 * @see DefaultProgressTest#iterator
 */
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
