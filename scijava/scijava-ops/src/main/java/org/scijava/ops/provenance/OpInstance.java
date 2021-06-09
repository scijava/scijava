package org.scijava.ops.provenance;

import java.util.Objects;

import org.scijava.ops.OpInfo;

public class OpInstance {

	private final OpInfo info;
	private final Object op;

	public OpInstance(OpInfo info, Object op) {
		this.info = info;
		this.op = op;
	}

	public OpInfo getInfo() {
		return info;
	}

	public Object getOp() {
		return op;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof OpInstance)) return false;
		OpInstance thatInstance = (OpInstance) that;
		boolean infosEqual = getInfo().equals(thatInstance.getInfo());
		boolean objectsEqual = getOp().equals(thatInstance.getOp());
		return infosEqual && objectsEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(info, op);
	}

}
