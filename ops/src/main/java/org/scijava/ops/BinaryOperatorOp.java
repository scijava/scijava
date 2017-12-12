package org.scijava.ops;

import java.util.function.BinaryOperator;

/**
 * BiFunctionOp that takes two arguments of the same type to produce a result of
 * that type.
 * 
 * @see BinaryOperator
 * @author Gabriel Einsdorf
 * 
 * @param <I>
 */
@FunctionalInterface
public interface BinaryOperatorOp<I> extends BiFunctionOp<I, I, I>, BinaryOperator<I> {

}
