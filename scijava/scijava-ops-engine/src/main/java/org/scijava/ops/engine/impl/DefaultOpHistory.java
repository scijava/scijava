
package org.scijava.ops.engine.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.OpHistory;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.RichOp;

/**
 * Log describing each execution of an Op. This class is designed to answer two
 * questions:
 * <ol>
 * <li>Given an {@link Object} output (e.g. a {@code List<String>}), what Op(s)
 * mutated that output?
 * <li>Given an {@link Object} op, what {@link OpInfo}s were utilized to
 * implement that Op's functionality?
 * </ol>
 * The answers to these two questions allow users to produce an entire
 * {@code List<Graph<OpInfo>>}, containing all of the information needed to
 * reproduce any {@link Object} output.
 * <p>
 * Note that SciJava Ops is responsible for logging the returns to <b>any</b>
 * matching calls here, but with some effort the user or other applications
 * could also contribute their algorithms to the history.
 *
 * @author Gabe Selzer
 */
public class DefaultOpHistory implements OpHistory {

	// -- DATA STRCUTURES -- //

	/**
	 * {@link Map} responsible for recording the {@link InfoChain} of
	 * {@link OpInfo}s involved to produce the result of a particular matching
	 * call
	 */
	private final Map<RichOp<?>, InfoChain> dependencyChain = new WeakHashMap<>();

	private final Map<Object, List<RichOp<?>>> mutationMap = new WeakHashMap<>();

	// -- USER API -- //

	/**
	 * Returns the list of executions on {@link Object} {@code o} recorded in the
	 * history
	 *
	 * @param o the {@link Object} of interest
	 * @return an {@link Iterable} of all executions upon {@code o}
	 */
	@Override
	public List<RichOp<?>> executionsUpon(Object o) {
		if (o.getClass().isPrimitive()) throw new IllegalArgumentException(
			"Cannot determine the executions upon a primitive as they are passed by reference!");
		return mutationMap.getOrDefault(o, Collections.emptyList());
	}

	@Override
	public InfoChain opExecutionChain(Object op) {
		return dependencyChain.get(op);
	}

	// -- HISTORY MAINTENANCE API -- //

	@Override
	public void logOp(RichOp<?> op) {
		dependencyChain.put(op, op.infoChain());
	}

	@Override
	public void logOutput(RichOp<?> op, Object output) {
		if (!mutationMap.containsKey(output)) updateList(output);
		resolveExecution(op, output);
	}

	// -- HELPER METHODS -- //

	private void updateList(Object output) {
		synchronized (mutationMap) {
			mutationMap.putIfAbsent(output, new ArrayList<>());
		}
	}

	private void resolveExecution(RichOp<?> op, Object output) {
		List<RichOp<?>> l = mutationMap.get(output);
		// HACK: sometimes, l can be null. Don't yet know why
		if (l != null) {
			synchronized (l) {
				l.add(op);
			}
		}
	}

	public void resetHistory() {
		mutationMap.clear();
		dependencyChain.clear();
	}

}
