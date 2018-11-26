package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer4;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace4Fourth<I1, I2, I3, IO> extends Consumer4<I1, I2, I3, IO> {
	void mutate(I1 in1, I2 in2, I3 in3, @Mutable IO io);

	@Override
	default void accept(I1 in1, I2 in2, I3 in3, IO io) {
		mutate(in1, in2, in3, io);
	}
}
