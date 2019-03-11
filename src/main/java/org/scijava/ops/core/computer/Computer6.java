package org.scijava.ops.core.computer;

import org.scijava.ops.core.Consumer7;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Computer6<I1, I2, I3, I4, I5, I6, O> extends Consumer7<I1, I2, I3, I4, I5, I6, O> {
	void compute(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, @Mutable O out);

	@Override
	default void accept(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, O out) {
		compute(in1, in2, in3, in4, in5, in6, out);
	}
}
