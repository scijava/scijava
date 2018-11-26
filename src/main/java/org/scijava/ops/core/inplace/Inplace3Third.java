package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer3;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace3Third<I1, I2, IO> extends Consumer3<I1, I2, IO> {
	void mutate(I1 in1, I2 in2, @Mutable IO io);

	@Override
	default void accept(I1 in1, I2 in2, IO io) {
		mutate(in1, in2, io);
	}
}
