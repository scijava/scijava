
package org.scijava.ops;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** (flat)map -- 1->N */
@FunctionalInterface
public interface FlatMapOp<I, O> extends BiConsumer<I, Consumer<O>> {}
