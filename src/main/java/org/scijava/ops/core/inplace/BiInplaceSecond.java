package org.scijava.ops.core.inplace;

import java.util.function.BiConsumer;

import org.scijava.param.Mutable;

@FunctionalInterface
public interface BiInplaceSecond<I1, IO> extends BiConsumer<I1, IO> {
	void mutate(I1 in1, @Mutable IO io);

	@Override
	default void accept(I1 in1, IO io) {
		mutate(in1, io);
	}
}
