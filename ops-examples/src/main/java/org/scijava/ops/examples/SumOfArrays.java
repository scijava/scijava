
package org.scijava.ops.examples;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import org.scijava.ops.AggregateOp;
import org.scijava.util.DoubleArray;
import org.scijava.util.IntArray;

/**
 * Takes a list of DoubleArray as inputs; produces the sum of the values.
 * <p>
 * The advantage of this structure is that the operation can be parallelized
 * into a tree of operations, which are combined toward the root of the tree.
 * </p>
 */
public class SumOfArrays implements AggregateOp<IntArray, DoubleArray> {

	@Override
	public BiFunction<DoubleArray, IntArray, DoubleArray> accumulator() {
		return new BiFunction<DoubleArray, IntArray, DoubleArray>() {

			@Override
			public DoubleArray apply(final DoubleArray zero, final IntArray in) {
				// NB: Do the operation in place on the intermediate buffer (zero).
				for (int i = 0; i < in.size(); i++) {
					zero.set(i, in.getValue(i) + zero.getValue(i));
				}
				return zero;
			}
		};
	}

	@Override
	public BinaryOperator<DoubleArray> combiner() {
		return new BinaryOperator<DoubleArray>() {

			@Override
			public DoubleArray apply(final DoubleArray in1, final DoubleArray in2) {
				// NB: Do the operation in place on the first intermediate buffer.
				for (int i = 0; i < in1.size(); i++) {
					in1.set(i, in1.getValue(i) + in2.getValue(i));
				}
				return in1;
			}

		};
	}

	@Override
	public DoubleArray createMemo(final IntArray in) {
		return new DoubleArray(in.size());
	}

}
