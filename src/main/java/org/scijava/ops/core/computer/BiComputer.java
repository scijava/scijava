package org.scijava.ops.core.computer;

import org.scijava.ops.core.Consumer3;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface BiComputer<I1, I2, O> extends Consumer3<I1, I2, O> {
	void compute(I1 in1, I2 in2, @Mutable O out);

	@Override
	default void accept(I1 t, I2 u, O v) {
		compute(t, u, v);
	}
}
