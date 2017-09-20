package org.scijava.ops;

import java.util.function.Predicate;

/**
 * An op that works like a {@link Predicate}
 * 
 * @author Gabriel Einsdorf
 * 
 * @param <I>
 */
@FunctionalInterface
public interface PredicateOp<I> extends FunctionOp<I, Boolean>, Predicate<I> {

	@Override
	default Boolean apply(I t) {
		return test(t);
	}
}
