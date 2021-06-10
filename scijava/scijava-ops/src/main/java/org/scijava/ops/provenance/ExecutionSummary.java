package org.scijava.ops.provenance;

/**
 * Describes the execution of an operation, uniquely identifiable by a Object of type {@code T}
 * @author G
 *
 */
public interface ExecutionSummary<T> {

	boolean hasOutput();

	Object output();

	T executor();

	boolean hasStarted();

	boolean hasCompleted();

	void recordStart();

	void recordCompletion(Object output);
	
	boolean isOutput(Object o);

}
