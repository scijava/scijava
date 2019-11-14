package org.scijava.ops.function;

import java.util.function.Supplier;

/**
 * A {@link Supplier} that creates a new object each time.
 * <p>
 * This is a functional interface whose functional method is {@link #create()}.
 * Invoking {@link Supplier#get()} simply delegates to {@link #create()}.
 * </p>
 * <p>
 * This interface can also be thought of as a nullary (arity 0) {@link Functions
 * function}, although the functional method is named {@code create} rather than
 * {@code apply}.
 * </p>
 *
 * @author Curtis Rueden
 * @param <O>
 *            The type of objects produced.
 */
public interface Producer<O> extends Supplier<O> {
	O create();

	@Override
	default O get() {
		return create();
	}
}
