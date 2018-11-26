package org.scijava.ops.core.computer;

import java.util.function.Consumer;

import org.scijava.param.Mutable;

@FunctionalInterface
public interface NullaryComputer<O> extends Consumer<O> {
	void compute(@Mutable O out);

	@Override
	default void accept(O u) {
		compute(u);
	}
}
