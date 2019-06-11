package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer7;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace7Second<I1, IO, I3, I4, I5, I6, I7> extends Consumer7<I1, IO, I3, I4, I5, I6, I7> {
	void mutate(I1 in1, @Mutable IO io, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7);

	@Override
	default void accept(I1 in1, IO io, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7) {
		mutate(in1, io, in3, in4, in5, in6, in7);
	}
}
