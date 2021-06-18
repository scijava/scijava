
package org.scijava.ops.provenance;

import java.util.UUID;

import org.scijava.ops.OpInfo;
import org.scijava.ops.impl.DefaultOpEnvironment;

/**
 * Describes the execution of an Op
 *
 * @author Gabe Selzer
 */
public class OpExecutionSummary {

	/** The identifier identifying the matching call */
	private final UUID executionHash;

	/** The {@link OpInfo} responsible for creating {@code instance} */
	private final OpInfo info;

	/** The {@link Object} created by {@code info} */
	private final Object instance;

	/** The {@link Object} returned by the call to {@link DefaultOpEnvironment} */
	private final Object wrappedInstance;

	/**
	 * The {@link Object} produced by this execution of {@code wrappedInstance}
	 * (or {@code instance}, if {@code wrappedInstance} is {@code null}.
	 */
	private final Object output;

	public OpExecutionSummary(UUID executionHash, OpInfo info, Object instance,
		Object wrappedOp, Object output)
	{
		this.executionHash = executionHash;
		this.info = info;
		this.instance = instance;
		this.wrappedInstance = wrappedOp;
		this.output = output;
	}

	public OpExecutionSummary(UUID executionHash, OpInfo info, Object instance,
		Object output)
	{
		this.executionHash = executionHash;
		this.info = info;
		this.instance = instance;
		this.wrappedInstance = null;
		this.output = output;
	}

	/**
	 * Returns the output {@link Object} of this execution
	 *
	 * @return the output of the execution
	 */
	public Object output() {
		return output;
	}

	/**
	 * Returns the {@link Object} whose execution has been tracked by this
	 * summary.
	 *
	 * @return the executor
	 */
	public Object executor() {
		return instance;
	}

	/**
	 * Returns the wrapping of {@link OpExecutionSummary#instance}, if it exists
	 *
	 * @return the wrapping of {@link OpExecutionSummary#instance}
	 */
	public Object wrappedExecutor() {
		return wrappedInstance;
	}

	/**
	 * Describes whether {@code o} is the output of this
	 * {@link OpExecutionSummary}
	 *
	 * @param o the {@link Object} that might be {@link OpExecutionSummary#output}
	 * @return true iff {@code o == output}
	 */
	public boolean isOutput(Object o) {
		return output == o;
	}

	/**
	 * Returns the {@link OpInfo} responsible for creating this
	 * {@link OpExecutionSummary#instance}
	 *
	 * @return the {@link OpInfo}
	 */
	public OpInfo info() {
		return info;
	}

	/**
	 * Returns the {@link UUID} identifying the Op chain responsible for creating
	 * this {@link OpExecutionSummary#instance}
	 *
	 * @return the identifying {@link UUID}
	 */
	public UUID executionTreeHash() {
		return executionHash;
	}

}
