
package org.scijava.ops;

import java.util.function.Consumer;

/** (flat)map -- 1->N */
@FunctionalInterface
public interface BiMapOp<I1, I2, O> extends TriConsumer<I1, I2, Consumer<O>> {}
