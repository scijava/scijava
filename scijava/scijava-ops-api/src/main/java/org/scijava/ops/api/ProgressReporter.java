package org.scijava.ops.api;


public interface ProgressReporter {

	default void reportElement() {
		reportElements(1);
	}

	void reportElements(long completedElements);

	void reportCompletion();

	double getProgress();

	boolean isCompleted();

}
