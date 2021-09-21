
package org.scijava.ops.engine.progress;

import org.scijava.ops.api.ProgressReporter;

/**
 * A {@link ProgressReporter} with two states: Incomplete and Complete.
 *
 * @author Gabriel Selzer
 */
public class BinaryProgressReporter implements ProgressReporter {

	private boolean hasCompleted;

	public BinaryProgressReporter() {
		hasCompleted = false;
	}

	@Override
	public void reportElements(long completedElements) {
		throw new UnsupportedOperationException(
			"A BinaryProgressReporter is incapable of reporting elements; " +
				"use another ProgressReporter implementation to record elements!");
	}

	@Override
	public double getProgress() {
		return hasCompleted ? 1.0 : 0.0;
	}

	@Override
	public void reportCompletion() {
		hasCompleted = true;
	}

	@Override
	public boolean isCompleted() {
		return hasCompleted;
	}

}
