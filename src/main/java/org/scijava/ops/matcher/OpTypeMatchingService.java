/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package org.scijava.ops.matcher;

import java.util.List;

import org.scijava.module.Module;
import org.scijava.module.ModuleInfo;
import org.scijava.ops.OpEnvironment;
import org.scijava.service.SciJavaService;
import org.scijava.struct.StructInstance;

/**
 * Interface for services that find ops which match an {@link OpRef}.
 * 
 * @author Curtis Rueden
 */
public interface OpTypeMatchingService extends SciJavaService {

	/**
	 * Finds and initializes the best module matching the given op name and/or
	 * type + arguments. An {@link OpCandidate} containing the matching module
	 * will be returned.
	 * 
	 * @param ops
	 *            The pool from which candidate ops should be drawn.
	 * @param ref
	 *            The op reference describing the op to match.
	 * @return An {@link OpCandidate} containing the module which wraps the best
	 *         op, with populated inputs, ready to run.
	 * @throws IllegalArgumentException
	 *             if there is no match, or if there is more than one match at
	 *             the same priority.
	 */
	MatchingResult findMatch(OpEnvironment ops, OpRef ref);

	/**
	 * Finds and initializes the best module matching any of the given op name
	 * and/or type + arguments. An {@link OpCandidate} containing the matching
	 * module will be returned.
	 * 
	 * @param ops
	 *            The pool from which candidate ops should be drawn.
	 * @param refs
	 *            The op references describing the op to match.
	 * @return An {@link OpCandidate} containing the module which wraps the best
	 *         op, with populated inputs, ready to run.
	 * @throws IllegalArgumentException
	 *             if there is no match, or if there is more than one match at
	 *             the same priority.
	 */
	MatchingResult findMatch(OpEnvironment ops, List<OpRef> refs);

	/**
	 * Builds a list of candidate ops which might match the given op reference.
	 * 
	 * @param ops
	 *            The pool from which candidate ops should be drawn.
	 * @param ref
	 *            The op reference describing the op to match.
	 * @return The list of candidate operations.
	 */
	List<OpCandidate> findCandidates(OpEnvironment ops, OpRef ref);

	/**
	 * Builds a list of candidate ops which might match one of the given op
	 * references.
	 * 
	 * @param ops
	 *            The pool from which candidate ops should be drawn.
	 * @param refs
	 *            The op references describing the op to match.
	 * @return The list of candidate operations.
	 */
	List<OpCandidate> findCandidates(OpEnvironment ops, List<OpRef> refs);

	/**
	 * Filters a list of ops to those matching the given arguments.
	 * 
	 * @param candidates
	 *            The list of op candidates to scan for matches.
	 * @return The list of matching op candidates, with associated
	 *         {@link Module} instances attached.
	 */
	List<OpCandidate> filterMatches(List<OpCandidate> candidates);

	/**
	 * Checks that each parameter is type-compatible with its corresponding
	 * argument.
	 */
	boolean typesMatch(OpCandidate candidate);
}
