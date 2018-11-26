package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer5;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace5First<IO, I2, I3, I4, I5> extends Consumer5<IO, I2, I3, I4, I5> {
	void mutate(@Mutable IO io, I2 in2, I3 in3, I4 in4, I5 in5);

	@Override
	default void accept(IO io, I2 in2, I3 in3, I4 in4, I5 in5) {
		mutate(io, in2, in3, in4, in5);
	}
}
