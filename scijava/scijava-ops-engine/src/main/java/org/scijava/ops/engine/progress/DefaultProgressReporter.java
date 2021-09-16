package org.scijava.ops.engine.progress;

import java.util.concurrent.atomic.AtomicLong;

import org.scijava.ops.api.ProgressReporter;

public class DefaultProgressReporter implements ProgressReporter {

	private final AtomicLong completedElements;
	private final long numElements;

	public DefaultProgressReporter(long numElements) {
		this.completedElements = new AtomicLong(0);
		this.numElements = numElements;
	}

	@Override
	public void reportElements(long completedElements) {
		this.completedElements.getAndAdd(completedElements);
		if (this.completedElements.longValue() > numElements)
			throw new IllegalStateException(
				"More elements were completed than were declared to exist!");
	}

	@Override
	public void reportCompletion() {
		this.completedElements.set(numElements);
	}

	@Override
	public double getProgress() {
		//NB we use doubleValue here to get a floating point result
		return Math.max(0, Math.min(1, completedElements.doubleValue() / numElements));
	}

}
