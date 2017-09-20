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
public interface BiPredicateOp<I1, I2> extends BiFunctionOp<I1, I2, Boolean>, BiPredicate<I1, I2> {
}
