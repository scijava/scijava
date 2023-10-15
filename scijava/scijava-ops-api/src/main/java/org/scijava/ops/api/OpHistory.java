/*-
 * #%L
 * SciJava Operations API: Outward-facing Interfaces used by the SciJava Operations framework.
 * %%
 * Copyright (C) 2021 - 2023 SciJava developers.
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

package org.scijava.ops.api;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import org.scijava.discovery.Discoverer;
import org.scijava.priority.Prioritized;

/**
 * Log describing each execution of an Op. This class is designed to answer two
 * questions:
 * <ol>
 * <li>What Op(s) produced and/or mutated this output?</li>
 * <li>Given an {@link Object} op, what {@link OpInfo}s (including dependencies)
 * were utilized to implement that Op's functionality?</li>
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
public interface OpHistory extends Prioritized<OpHistory> {

	static OpHistory getOpHistory() {
		Optional<OpHistory> historyOptional = Discoverer //
				.using(ServiceLoader::load) //
				.discoverMax(OpHistory.class);
		if (historyOptional.isEmpty()){
			throw new RuntimeException("No OpEnvironment provided!");
		}
		return historyOptional.get();
	}

	// -- USER API -- //

	/**
	 * Describes the known executions upon {@link Object} {@code o} recorded in
	 * the history
	 * 
	 * @param o the {@link Object} of interest
	 * @return a {@link List} of all executions upon {@code o}
	 */
	List<RichOp<?>> executionsUpon(Object o);

	/**
	 * Returns the hierarchy of {@link OpInfo}s describing the dependency tree of
	 * the {@link Object} {@code op}.
	 * 
	 * @param op the {@link Object} returned by a matching call. NB {@code op}
	 *          <b>must</b> be the {@link Object} returned by the outermost
	 *          matching call, as the dependency {@link Object}s are not recorded.
	 * @return the {@link InfoTree} describing the dependency tree
	 */
	InfoTree infoTree(Object op);

	default String signatureOf(Object op) {
		return infoTree(op).signature();
	}

	// -- HISTORY MAINTENANCE API -- //

	/**
	 * Logs the creation of {@link RichOp}
	 * 
	 * @param op the {@link RichOp} containing relevant information
	 */
	void logOp(RichOp<?> op);

	/**
	 * Logs the {@link Object} output of the {@link RichOp} {@code op}.
	 * @param op the {@link RichOp} producing {@code output}
	 * @param output the {@link Object} output of {@code e}
	 */
	void logOutput(RichOp<?> op, Object output);

}
