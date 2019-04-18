package org.scijava.ops.core;

import org.scijava.param.Mutable;

@FunctionalInterface
public interface BiComputer<I1, I2, O> extends TriConsumer<I1, I2, O> {
	void compute(I1 in1, I2 in2, @Mutable O out);

	@Override
	default void accept(I1 t, I2 u, O v) {
		compute(t, u, v);
	}
}
