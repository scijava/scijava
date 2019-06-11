package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer5;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace5Fourth<I1, I2, IO, I4, I5> extends Consumer5<I1, I2, IO, I4, I5> {
	void mutate(I1 in1, I2 in2, @Mutable IO io, I4 in4, I5 in5);

	@Override
	default void accept(I1 in1, I2 in2, IO io, I4 in4, I5 in5) {
		mutate(in1, in2, io, in4, in5);
	}
}
