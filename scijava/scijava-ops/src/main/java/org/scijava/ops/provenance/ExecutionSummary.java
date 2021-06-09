package org.scijava.ops.provenance;

import java.util.List;
import java.util.Optional;

/**
 * Describes the execution of an operation, uniquely identifiable by a Object of type {@code T}, on a set of arguments
 * @author G
 *
 */
public interface ExecutionSummary<T> {

	List<Object> arugments();

	boolean hasOutput();

	Object output();

	T executor();

	boolean hasStarted();

	boolean hasCompleted();

	void recordStart();

	void recordCompletion(Object output);

	boolean isInput(Object o);

	boolean isOutput(Object o);

}
