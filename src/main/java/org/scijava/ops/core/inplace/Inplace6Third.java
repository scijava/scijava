package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer6;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace6Third<I1, I2, IO, I4, I5, I6> extends Consumer6<I1, I2, IO, I4, I5, I6> {
	void mutate(I1 in1, I2 in2, @Mutable IO io, I4 in4, I5 in5, I6 in6);

	@Override
	default void accept(I1 in1, I2 in2, IO io, I4 in4, I5 in5, I6 in6) {
		mutate(in1, in2, io, in4, in5, in6);
	}
}
