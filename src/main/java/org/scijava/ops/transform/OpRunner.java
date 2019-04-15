package org.scijava.ops.transform;

import org.scijava.types.Nil;

public interface OpRunner<O> extends KnowsTypes {
	O run(Object[] args);

	@Override
	default Nil<?>[] outTypes() {
		return new Nil<?>[] { new Nil<O>() {
		} };
	}
}
