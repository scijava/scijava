package org.scijava.ops.provenance;

/**
 * Describes the execution of an operation, uniquely identifiable by a Object of type {@code T}
 * @author G
 *
 */
public interface ExecutionSummary<T> {

	Object output();

	T executor();

	boolean isOutput(Object o);

}
