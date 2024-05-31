/*
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine.matcher.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.engine.matcher.OpCandidate;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpRequest;
import org.scijava.ops.engine.MatchingConditions;
import org.scijava.ops.engine.matcher.MatchingRoutine;
import org.scijava.ops.engine.matcher.OpMatcher;

/**
 * Default implementation of {@link OpMatcher}. Used for finding Ops which match
 * a {@link OpRequest request}.
 *
 * @author David Kolb
 */
public class DefaultOpMatcher implements OpMatcher {

	private final List<MatchingRoutine> matchers;

	public DefaultOpMatcher(Collection<? extends MatchingRoutine> matchers) {
		this.matchers = new ArrayList<>(matchers);
		Collections.sort(this.matchers, Collections.reverseOrder());
	}

	@Override
	public OpCandidate match(MatchingConditions conditions, OpEnvironment env) {
		List<OpMatchingException> exceptions = new ArrayList<>(matchers.size());
		// in priority order, search for a match
		for (MatchingRoutine r : matchers) {
			try {
				return r.match(conditions, this, env);
			}
			catch (OpMatchingException e) {
				exceptions.add(e);
			}
		}

		// in the case of no matches, throw an agglomerated exception to retain the
		// full exception history
		throw agglomeratedException(conditions.request(), exceptions, env);
	}

	private OpMatchingException agglomeratedException( //
		final OpRequest request, //
		final List<OpMatchingException> list, //
		final OpEnvironment env //
	) {
		OpMatchingException agglomerated = new OpMatchingException(
			"No match found");
		list.forEach(agglomerated::addSuppressed);
		return agglomerated;
	}
}
