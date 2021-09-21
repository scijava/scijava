
package org.scijava.ops.engine.progress;

import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class DefaultProgressTest extends AbstractTestEnvironment {

	@OpField(names = "test.progressReporter")
	public final Function<Integer, Integer> iterator = (iterations) -> {
		// set up progress reporter
		Progress.defineStages(1);
		Progress.maxForStage(0, iterations);

		for(int i = 0; i < iterations; i++) {
			Progress.update();
		}
		return iterations;
	};

	@Test
	public void testLongOp() throws InterruptedException {
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

	private int numUpdates = 0;

	private void testProgress(double progress, int numIterations) {
		Assert.assertEquals((double) ++numUpdates / numIterations, progress, 1e-6);
	}

}
