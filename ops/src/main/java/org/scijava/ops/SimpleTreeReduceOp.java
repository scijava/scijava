package org.scijava.ops;

import java.util.function.BinaryOperator;

/**
 * Special case of {@link TreeReduceOp} where the aggreagtor and the combiner do
 * the same thing, producing outputs the same type as the input, with no need
 * for an initial zero value.
 */
@FunctionalInterface
public interface SimpleTreeReduceOp<T> extends BinaryOperator<T>
{}
