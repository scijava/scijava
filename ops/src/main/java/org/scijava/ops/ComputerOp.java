package org.scijava.ops;

import java.util.function.BiConsumer;

/** Unary computer. */
@FunctionalInterface
public interface ComputerOp<I, O> extends BiConsumer<I, O>
{
}