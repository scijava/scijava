package org.scijava.ops;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface BiInplace1<IO, I2> extends BiConsumer<IO, I2> {
	void mutate(IO io, I2 in2);

	@Override
	default void accept(IO io, I2 in2) {
		mutate(io, in2);
	}
}
