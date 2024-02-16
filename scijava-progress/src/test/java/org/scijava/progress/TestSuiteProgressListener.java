
package org.scijava.progress;

import org.junit.jupiter.api.Assertions;

public class TestSuiteProgressListener implements ProgressListener {

	private final String id;
	private final long expectedIterations;
	private double currentIterations = 0;

	private boolean registered = false;
	private boolean completed = false;

	public TestSuiteProgressListener(final long expectedIterations) {
		this(expectedIterations, null);
	}

	public TestSuiteProgressListener(final long expectedIterations,
		final String id)
	{
		this.id = id;
		this.expectedIterations = expectedIterations;
	}

	@Override
	public void acknowledgeUpdate(Task task) {
		if (id != null && !task.description().equals(id)) return;
		// Registration ping
		if (!registered) {
			registered = true;
			return;
		}
		// Progress pings
		if (!task.isComplete()) {
			Assertions.assertEquals(++currentIterations / expectedIterations, task
				.progress(), 1e-6);
		}
		// Completion ping
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
		this.registered = false;
		this.completed = false;
	}

	public double progress() {
		return this.currentIterations / this.expectedIterations;
	}
}
