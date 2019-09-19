package org.scijava.ops.transform;

import org.scijava.ops.types.Any;
import org.scijava.ops.types.Nil;

public interface OpRunner extends KnowsTypes {
	Object run(Object[] args);
	
	Object getAdaptedOp();

	@Override
	default Nil<?> outType() {
		return new Nil<Any>() { @Override public Any getType() {return new Any();}};
	}
}
