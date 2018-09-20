package org.scijava.ops.core;

import java.util.function.Consumer;

@FunctionalInterface
public interface Inplace<I> extends Consumer<I> {
	void mutate(I in1);

	@Override
	default void accept(I in) {
		mutate(in);
	}
}
