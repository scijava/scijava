
package org.scijava.ops.engine.progress;

import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class StatusBasedProgressTest extends AbstractTestEnvironment {

	@OpField(names = "test.statusReporter")
	public final Function<Integer, Integer> statusSpinningOp = (in) -> {
		for (int i = 0; i < in; i++) {
			Progress.setStatus("Setting status: " + (i + 1));
		}
		return in;
	};

	@Test
	public void testStatusUpdate() {
		Function<Integer, Integer> op = ops.op("test.statusReporter").inType(
			Integer.class).outType(Integer.class).function();
		int numIterations = 10;
		Progress.addListener(op, new ProgressListener() {

			int numUpdates = 0;

			@Override
			public void acknowledgeUpdate(Task task) {
				if (numUpdates++ < numIterations) {
					Assert.assertTrue(task.status().equals("Setting status: " +
						(numUpdates)));
				}
				else {
					Assert.assertEquals(1., task.progress(), 1e-6);
				}
			}

		});
	}

}
