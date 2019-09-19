package org.scijava.ops.core.computer;

import org.scijava.ops.core.Consumer9;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Computer8<I1, I2, I3, I4, I5, I6, I7, I8, O> extends Consumer9<I1, I2, I3, I4, I5, I6, I7, I8, O> {
	void compute(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, @Mutable O out);

	@Override
	default void accept(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, O out) {
		compute(in1, in2, in3, in4, in5, in6, in7, in8, out);
	}
}
