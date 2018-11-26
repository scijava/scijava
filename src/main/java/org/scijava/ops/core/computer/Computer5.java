package org.scijava.ops.core.computer;

import org.scijava.ops.core.Consumer6;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Computer5<I1, I2, I3, I4, I5, O> extends Consumer6<I1, I2, I3, I4, I5, O> {
	void compute(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, @Mutable O out);

	@Override
	default void accept(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, O out) {
		compute(in1, in2, in3, in4, in5, out);
	}
}
