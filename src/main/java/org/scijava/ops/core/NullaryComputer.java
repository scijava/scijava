package org.scijava.ops.core;

import java.util.function.Consumer;

@FunctionalInterface
public interface NullaryComputer<O> extends Consumer<O> {
	void compute(O out);

	@Override
	default void accept(O u) {
		compute(u);
	}
}
