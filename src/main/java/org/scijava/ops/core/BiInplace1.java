package org.scijava.ops.core;

import java.util.function.BiConsumer;

import org.scijava.param.Mutable;

@FunctionalInterface
public interface BiInplace1<IO, I2> extends BiConsumer<IO, I2> {
	void mutate(@Mutable IO io, I2 in2);

	@Override
	default void accept(IO io, I2 in2) {
		mutate(io, in2);
	}
}
