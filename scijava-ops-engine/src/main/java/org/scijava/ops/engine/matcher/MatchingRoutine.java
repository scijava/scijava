/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.engine.MatchingConditions;

/**
 * A plugin type employing a particular strategy to generate an
 * {@link OpCandidate}.
 *
 * @author Gabriel Selzer
 */
public interface MatchingRoutine extends Comparable<MatchingRoutine> {

	void checkSuitability(MatchingConditions conditions)
		throws OpMatchingException;

	@Override
	default int compareTo(MatchingRoutine o) {
		return (int) (priority() - o.priority());
	}

	OpCandidate findMatch(MatchingConditions conditions, OpMatcher matcher,
		OpEnvironment env) throws OpMatchingException;

	/**
	 * Generates an {@link OpCandidate} from the Ops in the provided
	 * {@link OpEnvironment}, conforming to the provided
	 * {@link MatchingConditions}
	 *
	 * @param conditions the {@link MatchingConditions} the returned Op must
	 *          conform to
	 * @param matcher the {@link OpMatcher} responsible for matching
	 * @param env the {@link OpEnvironment} containing the Ops able to be matched
	 * @return an {@link OpCandidate}
	 */
	default OpCandidate match(MatchingConditions conditions, OpMatcher matcher,
		OpEnvironment env)
	{
		checkSuitability(conditions);
		return findMatch(conditions, matcher, env);
	}

	/**
	 * The priority of this {@link MatchingRoutine}
	 *
	 * @return the priority
	 */
	double priority();

}
