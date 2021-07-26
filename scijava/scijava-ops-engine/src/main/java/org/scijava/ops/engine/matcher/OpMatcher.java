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

package org.scijava.ops.engine.matcher;

import java.util.List;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpCandidate;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpRef;
import org.scijava.ops.engine.matcher.MatchingResult;

/**
 * Finds Ops which match an {@link OpRef}.
 *
 * @author Curtis Rueden
 */
//TODO javadoc
public interface OpMatcher {

	OpCandidate findSingleMatch(OpEnvironment env, OpRef ref);

	OpCandidate findSingleMatch(OpEnvironment env, OpRef ref, Hints hints);

	MatchingResult findMatch(OpEnvironment env, OpRef ref);

	MatchingResult findMatch(OpEnvironment env, OpRef ref, Hints hints);

	MatchingResult findMatch(OpEnvironment env, List<OpRef> refs);

	MatchingResult findMatch(OpEnvironment env, List<OpRef> refs, Hints hints);

	List<OpCandidate> findCandidates(OpEnvironment env, OpRef ref);

	List<OpCandidate> findCandidates(OpEnvironment env, OpRef ref, Hints hints);

	List<OpCandidate> findCandidates(OpEnvironment env, List<OpRef> refs);

	List<OpCandidate> findCandidates(OpEnvironment env, List<OpRef> refs, Hints hints);

	List<OpCandidate> filterMatches(List<OpCandidate> candidates);

	boolean typesMatch(OpCandidate candidate);
}
