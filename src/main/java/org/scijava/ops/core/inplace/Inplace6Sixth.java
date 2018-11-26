package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer6;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace6Sixth<I1, I2, I3, I4, I5, IO> extends Consumer6<I1, I2, I3, I4, I5, IO> {
	void mutate(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, @Mutable IO io);

	@Override
	default void accept(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, IO io) {
		mutate(in1, in2, in3, in4, in5, io);
	}
}
