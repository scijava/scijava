package org.scijava.ops;

import java.util.function.BiFunction;

@FunctionalInterface
public interface BinaryFunctionOp<I1, I2, O> extends BiFunction<I1, I2, O>, Op {
}
