package org.scijava.ops.core.inplace;

import org.scijava.ops.core.Consumer3;
import org.scijava.param.Mutable;

@FunctionalInterface
public interface Inplace3Second<I1, IO, I3> extends Consumer3<I1, IO, I3> {
	void mutate(I1 in1, @Mutable IO io, I3 in3);

	@Override
	default void accept(I1 in1, IO io, I3 in3) {
		mutate(in1, io, in3);
	}
}
