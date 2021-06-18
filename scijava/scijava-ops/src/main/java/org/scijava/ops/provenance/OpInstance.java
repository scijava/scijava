
package org.scijava.ops.provenance;

import java.util.Objects;

import org.scijava.ops.OpInfo;

/**
 * An Op Instance
 *
 * @author Gabriel Selzer
 */
public class OpInstance {

	/** The {@link OpInfo} responsible for creating {@link OpInstance#op} */
	private final OpInfo info;
	/** The {@link Object} created by {@link OpInstance#info} */
	private final Object op;

	public OpInstance(OpInfo info, Object op) {
		this.info = info;
		this.op = op;
	}

	/**
	 * Returns the {@link OpInfo} responsible for creating the instance.
	 *
	 * @return the {@link OpInfo}
	 */
	public OpInfo info() {
		return info;
	}

	/**
	 * Returns the instance of the Op (created by {@link OpInstance#info}).
	 *
	 * @return the {@link Object} instance
	 */
	public Object op() {
		return op;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof OpInstance)) return false;
		OpInstance thatInstance = (OpInstance) that;
		boolean infosEqual = info().equals(thatInstance.info());
		boolean objectsEqual = op().equals(thatInstance.op());
		return infosEqual && objectsEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(info, op);
	}

}
