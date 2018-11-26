package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer3;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace3First<IO, I2, I3> extends Consumer3<IO, I2, I3> {
	void mutate(@Mutable IO io, I2 in2, I3 in3);

	@Override
	default void accept(IO io, I2 in2, I3 in3) {
		mutate(io, in2, in3);
	}
}
