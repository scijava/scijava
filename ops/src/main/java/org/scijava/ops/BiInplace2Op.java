package org.scijava.ops;

import java.util.function.BiConsumer;

/** Binary inplace computation mutating the second input. */
@FunctionalInterface
public interface BiInplace2Op<I1, IO> extends BiConsumer<I1, IO> {}
