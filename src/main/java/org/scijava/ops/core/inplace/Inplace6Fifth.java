package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer6;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace6Fifth<I1, I2, I3, I4, IO, I6> extends Consumer6<I1, I2, I3, I4, IO, I6> {
	void mutate(I1 in1, I2 in2, I3 in3, I4 in4, @Mutable IO io, I6 in6);

	@Override
	default void accept(I1 in1, I2 in2, I3 in3, I4 in4, IO io, I6 in6) {
		mutate(in1, in2, in3, in4, io, in6);
	}
}
