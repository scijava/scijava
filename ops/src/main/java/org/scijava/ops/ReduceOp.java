
package org.scijava.ops;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

// reduce      N->M -- BiConsumer<Iterable<I>, Consumer<O>> void accept(Iterable<Data> in, Consumer<O> out)
@FunctionalInterface
public interface ReduceOp<I, O> extends BiConsumer<Iterable<I>, Consumer<O>> {}
