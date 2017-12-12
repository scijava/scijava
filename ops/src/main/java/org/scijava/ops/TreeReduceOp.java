package org.scijava.ops;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

	// tree-reduce N->M -- Aggregator composes (not extends):
	//             -- BiFunction<I, O (memo), O>
	//             -- BinaryOperator<O>
	//             -- O createIdentity(I in)
/** See: https://stackoverflow.com/a/38949457/1207769 */
public interface TreeReduceOp<I, O> {

	/**
	 * NB: The infrastructure that executes the tree reduce must guarantee that
	 * multiple threads do not access the same zero element at the same time in a
	 * thread-unsafe way. In other words: implementors can perform operations
	 * on zero elements in this method which are not thread safe.
	 */
	BiFunction<O, I, O> accumulator();
	BinaryOperator<O> combiner();
	O createZero(I in);
}