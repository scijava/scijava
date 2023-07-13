
package org.scijava.progress;

import org.junit.jupiter.api.Assertions;

public class TestSuiteProgressListener implements ProgressListener {

	private final String id;
	private final long expectedIterations;
	private double currentIterations = 0;

	private boolean completed = false;

	public TestSuiteProgressListener(final long expectedIterations) {
		this.id = null;
		this.expectedIterations = expectedIterations;
	}

	public TestSuiteProgressListener(final long expectedIterations,
		final String id)
	{
		this.id = id;
		this.expectedIterations = expectedIterations;
	}

	@Override
	public void acknowledgeUpdate(Task task) {
		if (id != null && !task.id().equals(id)) return;
		if (!task.isComplete()) {
			Assertions.assertEquals(++currentIterations / expectedIterations, task
				.progress(), 1e-6);
		}
		else {
			Assertions.assertFalse(completed);
			Assertions.assertEquals(1., task.progress(), 1e-6);
			completed = true;
		}
	}

	public boolean isComplete() {
		return this.completed;
	}

	public void reset() {
		this.currentIterations = 0;
		this.completed = false;
	}

	public double progress() {
		return this.currentIterations / this.expectedIterations;
	}
}
