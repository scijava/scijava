package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer6;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace6Fourth<I1, I2, I3, IO, I5, I6> extends Consumer6<I1, I2, I3, IO, I5, I6> {
	void mutate(I1 in1, I2 in2, I3 in3, @Mutable IO io, I5 in5, I6 in6);

	@Override
	default void accept(I1 in1, I2 in2, I3 in3, IO io, I5 in5, I6 in6) {
		mutate(in1, in2, in3, io, in5, in6);
	}
}
