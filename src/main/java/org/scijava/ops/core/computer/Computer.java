package org.scijava.ops.core.computer;

import java.util.function.BiConsumer;

import org.scijava.param.Mutable;

@FunctionalInterface
public interface Computer<I1, O> extends BiConsumer<I1, O> {
	void compute(I1 in1, @Mutable O out);

	@Override
	default void accept(I1 t, O u) {
		compute(t, u);
	}
}
