package org.scijava.ops.core;

import java.util.function.Consumer;

import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace<I> extends Consumer<I> {
	void mutate(@Mutable I in1);

	@Override
	default void accept(I in) {
		mutate(in);
	}
}
