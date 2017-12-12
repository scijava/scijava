
package org.scijava.ops;

/**
 * reduce N->1 -- Function<Iterable<I>, O> -- Data apply(Iterable<Data> in) --
 * Note this is special case of {@link ReduceOp}.
 */
public interface ReduceToOneOp<I, O> extends FunctionOp<Iterable<I>, O> {
}
