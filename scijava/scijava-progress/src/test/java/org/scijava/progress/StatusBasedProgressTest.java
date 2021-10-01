
package org.scijava.progress;

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StatusBasedProgressTest {

	public final Function<Integer, Integer> statusSpinningTask = (in) -> {
		for (int i = 0; i < in; i++) {
			Progress.setStatus("Setting status: " + (i + 1));
		}
		return in;
	};

	@Test
	public void testStatusUpdate() {
		Function<Integer, Integer> progressible = statusSpinningTask;
		int numIterations = 10;
		Progress.addListener(progressible, new ProgressListener() {

			int numUpdates = 0;

			@Override
			public void acknowledgeUpdate(Task task) {
				if (numUpdates++ < numIterations) {
					Assertions.assertTrue(task.status().equals("Setting status: " +
						(numUpdates)));
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

}
