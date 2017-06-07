
package org.scijava.ops.examples;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import org.scijava.ops.TreeReduceOp;
import org.scijava.plugin.Plugin;

/**
 * Takes a list of doubles as inputs; produces the sum of the values.
 * <p>
 * The advantage of this structure is that the operation can be parallelized
 * into a tree of operations, which are combined toward the root of the tree.
 * </p>
 */
@Plugin(type = TreeReduceOp.class)
public class SumOfIntegers implements TreeReduceOp<Integer, Long> {

	@Override
	public BiFunction<Long, Integer, Long> accumulator() {
		return new BiFunction<Long, Integer, Long>() {

			@Override
			public Long apply(final Long t, final Integer u) {
				return t + u;
			}

		};
	}

	@Override
	public BinaryOperator<Long> combiner() {
		return new BinaryOperator<Long>() {

			@Override
			public Long apply(final Long t, final Long u) {
				return t + u;
			}
		};
	}

	@Override
	public Long createZero(final Integer in) {
		return 0L;
	}

}
