package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer4;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace4First<IO, I2, I3, I4> extends Consumer4<IO, I2, I3, I4> {
	void mutate(@Mutable IO io, I2 in2, I3 in3, I4 in4);

	@Override
	default void accept(IO io, I2 in2, I3 in3, I4 in4) {
		mutate(io, in2, in3, in4);
	}
}
