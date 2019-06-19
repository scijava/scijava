package org.scijava.ops.transform;

import org.scijava.ops.types.Nil;

public interface OpRunner<O> extends KnowsTypes {
	O run(Object[] args);
	
	Object getAdaptedOp();

	@Override
	default Nil<?> outType() {
		return new Nil<O>() {};
	}
}
