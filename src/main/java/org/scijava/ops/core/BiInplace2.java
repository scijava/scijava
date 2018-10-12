package org.scijava.ops.core;

import java.util.function.BiConsumer;

import org.scijava.param.Mutable;

@FunctionalInterface
public interface BiInplace2<I1, IO> extends BiConsumer<I1, IO> {
	void mutate(I1 in1, @Mutable IO io);

	@Override
	default void accept(I1 in1, IO io) {
		mutate(in1, io);
	}
}
