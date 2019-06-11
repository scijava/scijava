package org.scijava.ops.core.computer;

import org.scijava.ops.core.Consumer4;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Computer3<I1, I2, I3, O> extends Consumer4<I1, I2, I3, O> {
	void compute(I1 in1, I2 in2, I3 in3, @Mutable O out);

	@Override
	default void accept(I1 t, I2 u, I3 v, O w) {
		compute(t, u, v, w);
	}
}
