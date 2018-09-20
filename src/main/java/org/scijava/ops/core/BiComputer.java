package org.scijava.ops.core;

@FunctionalInterface
public interface BiComputer<I1, I2, O> extends TriConsumer<I1, I2, O> {
	void compute(I1 in1, I2 in2, O out);

	@Override
	default void accept(I1 t, I2 u, O v) {
		compute(t, u, v);
	}
}
