package org.scijava.ops.core;

import java.util.function.Supplier;

public interface Source<O> extends Supplier<O> {
	O create();

	@Override
	default O get() {
		return create();
	}
}
