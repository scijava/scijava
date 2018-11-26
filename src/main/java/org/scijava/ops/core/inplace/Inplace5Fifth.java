package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer5;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace5Fifth<I1, I2, I3, I4, IO> extends Consumer5<I1, I2, I3, I4, IO> {
	void mutate(I1 in1, I2 in2, I3 in3, I4 in4, @Mutable IO io);

	@Override
	default void accept(I1 in1, I2 in2, I3 in3, I4 in4, IO io) {
		mutate(in1, in2, in3, in4, io);
	}
}
