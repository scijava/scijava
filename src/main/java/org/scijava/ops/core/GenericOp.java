package org.scijava.ops.core;

import org.scijava.ops.matcher.OpInfo;
import org.scijava.ops.types.GenericTyped;

public abstract class GenericOp implements GenericTyped{

	OpInfo opInfo;
	
	public GenericOp(OpInfo opInfo) {
		this.opInfo = opInfo;
	}
	
	public OpInfo getOpInfo() {
		return opInfo;
	}

}
