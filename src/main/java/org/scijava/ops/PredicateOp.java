package org.scijava.ops;

import java.util.function.Predicate;

/**
 * An op that works like a {@link Predicate}
 * 
 * @author Gabriel Einsdorf
 * 
 * @param <I>
 */
public interface PredicateOp<I> extends FunctionOp<I, Boolean> , Predicate<I>{

}
