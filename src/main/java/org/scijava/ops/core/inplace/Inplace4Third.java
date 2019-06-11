package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer4;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace4Third<I1, I2, IO, I4> extends Consumer4<I1, I2, IO, I4> {
	void mutate(I1 in1, I2 in2, @Mutable IO io, I4 in4);

	@Override
	default void accept(I1 in1, I2 in2, IO io, I4 in4) {
		mutate(in1, in2, io, in4);
	}
}
