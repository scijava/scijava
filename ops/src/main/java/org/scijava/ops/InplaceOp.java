
package org.scijava.ops;

import java.util.function.Consumer;

/** Unary inplace computation. */
@FunctionalInterface
public interface InplaceOp<IO> extends Consumer<IO> {}
