package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer6;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace6First<IO, I2, I3, I4, I5, I6> extends Consumer6<IO, I2, I3, I4, I5, I6> {
	void mutate(@Mutable IO io, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6);

	@Override
	default void accept(IO io, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6) {
		mutate(io, in2, in3, in4, in5, in6);
	}
}
