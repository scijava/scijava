/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.engine.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpHistory;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.RichOp;
import org.scijava.priority.Priority;

/**
 * Log describing each execution of an Op. This class is designed to answer two
 * questions:
 * <ol>
 * <li>Given an {@link Object} output (e.g. a {@code List<String>}), what Op(s)
 * mutated that output?</li>
 * <li>Given an {@link Object} op, what {@link OpInfo}s were utilized to
 * implement that Op's functionality?</li>
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
	 * {@link Map} responsible for recording the {@link InfoTree} of
	 * {@link OpInfo}s involved to produce the result of a particular matching
	 * call
	 */
	private final Map<RichOp<?>, InfoTree> dependencyChain = new WeakHashMap<>();

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
	public InfoTree infoTree(Object op) {
		if (op instanceof RichOp<?>) {
			return dependencyChain.get(op);
		}
		throw new IllegalArgumentException("Object " + op +
			" is not an Op known to this OpHistory!");
	}

	// -- HISTORY MAINTENANCE API -- //

	@Override
	public void logOp(RichOp<?> op) {
		dependencyChain.put(op, op.infoTree());
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

	@Override
	public double getPriority() {
		return Priority.VERY_LOW;
	}
}
