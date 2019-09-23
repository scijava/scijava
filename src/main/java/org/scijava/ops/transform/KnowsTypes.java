package org.scijava.ops.transform;

import org.scijava.ops.types.Nil;

public interface KnowsTypes {
	Nil<?>[] inTypes();

	Nil<?> outType();
}
