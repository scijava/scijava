package org.scijava.ops.transform;

import org.scijava.types.Nil;

public interface KnowsTypes {
	Nil<?>[] inTypes();

	Nil<?>[] outTypes();
}