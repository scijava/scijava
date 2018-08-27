package org.scijava.ops;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface BiInplace2<I1, IO> extends BiConsumer<I1, IO> {
	void mutate(I1 in1, IO io);

	@Override
	default void accept(I1 in1, IO io) {
		mutate(in1, io);
	}
}
