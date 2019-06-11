package org.scijava.ops.core.computer;

import org.scijava.ops.core.Consumer5;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Computer4<I1, I2, I3, I4, O> extends Consumer5<I1, I2, I3, I4, O> {
	void compute(I1 in1, I2 in2, I3 in3, I4 in4, @Mutable O out);

	@Override
	default void accept(I1 t, I2 u, I3 v, I4 w, O x) {
		compute(t, u, v, w, x);
	}
}
