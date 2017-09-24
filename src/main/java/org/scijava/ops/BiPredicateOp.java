package org.scijava.ops;

import java.util.function.BiPredicate;

/**
 * An op that works like a {@link BiPredicate}.
 * 
 * @author Gabriel Einsdorf
 * 
 * @param <I1>
 * @param <I2>
 */
@FunctionalInterface
public interface BiPredicateOp<I1, I2> extends BiPredicate<I1, I2> {
}
