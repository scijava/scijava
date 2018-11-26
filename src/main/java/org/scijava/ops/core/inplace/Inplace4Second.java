package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer4;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace4Second<I1, IO, I3, I4> extends Consumer4<I1, IO, I3, I4> {
	void mutate(I1 in1, @Mutable IO io, I3 in3, I4 in4);

	@Override
	default void accept(I1 in1, IO io, I3 in3, I4 in4) {
		mutate(in1, io, in3, in4);
	}
}
