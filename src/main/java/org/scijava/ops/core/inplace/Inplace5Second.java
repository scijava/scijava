package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer5;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace5Second<I1, IO, I3, I4, I5> extends Consumer5<I1, IO, I3, I4, I5> {
	void mutate(I1 in1, @Mutable IO io, I3 in3, I4 in4, I5 in5);

	@Override
	default void accept(I1 in1, IO io, I3 in3, I4 in4, I5 in5) {
		mutate(in1, io, in3, in4, in5);
	}
}
