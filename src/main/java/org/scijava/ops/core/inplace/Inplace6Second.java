package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer6;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace6Second<I1, IO, I3, I4, I5, I6> extends Consumer6<I1, IO, I3, I4, I5, I6> {
	void mutate(I1 in1, @Mutable IO io, I3 in3, I4 in4, I5 in5, I6 in6);

	@Override
	default void accept(I1 in1, IO io, I3 in3, I4 in4, I5 in5, I6 in6) {
		mutate(in1, io, in3, in4, in5, in6);
	}
}
