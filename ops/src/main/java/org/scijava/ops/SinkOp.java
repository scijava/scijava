
package org.scijava.ops;

import java.util.function.Consumer;

/** {@link Consumer}, as a plugin. */
@FunctionalInterface
public interface SinkOp<I> extends Consumer<I> {}
