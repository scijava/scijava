package org.scijava.ops;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface Computer<I1, O> extends BiConsumer<I1, O> {
	void compute(I1 in1, O out);

	@Override
	default void accept(I1 t, O u) {
		compute(t, u);
	}
}
