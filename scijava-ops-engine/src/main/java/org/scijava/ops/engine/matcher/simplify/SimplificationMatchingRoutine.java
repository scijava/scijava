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

package org.scijava.ops.engine.matcher.simplify;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.api.OpRequest;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.engine.MatchingConditions;
import org.scijava.ops.engine.OpCandidate;
import org.scijava.ops.engine.matcher.MatchingResult;
import org.scijava.ops.engine.matcher.OpMatcher;
import org.scijava.ops.engine.matcher.impl.RuntimeSafeMatchingRoutine;
import org.scijava.priority.Priority;
import org.slf4j.LoggerFactory;

public class SimplificationMatchingRoutine extends RuntimeSafeMatchingRoutine {

	protected static final Map<OpEnvironment, Map<String, SortedSet<OpInfo>>> seenNames = new HashMap<>();

	protected static void addSimpleInfosToCache(OpEnvironment env, String name) {
		Map<String, SortedSet<OpInfo>> seen = seenNames.computeIfAbsent(env, e -> new HashMap<>());
		if (!seen.containsKey(name)) {
			SortedSet<OpInfo> ops = new TreeSet<>();
			for (OpInfo info : env.infos(name)) {
				if (info.declaredHints().contains(BaseOpHints.Simplification.FORBIDDEN)) continue;
				if (info instanceof SimplifiedOpInfo) continue;
				try {
					ops.add(SimplificationUtils.simplifyInfo(env, info));
				}
				catch (OpMatchingException e) {
					LoggerFactory.getLogger(SimplificationMatchingRoutine.class) //
						.info("Could not simplify OpInfo " + info +
							" due to the following exception", e);
					ops.add(info);
				}
			}
			seen.put(name, ops);
		}
	}

	@Override
	public void checkSuitability(MatchingConditions conditions)
		throws OpMatchingException
	{
		if (conditions.hints().containsAny(BaseOpHints.Simplification.IN_PROGRESS,
			BaseOpHints.Simplification.FORBIDDEN)) //
			throw new OpMatchingException(
				"Simplification is not suitable: Simplification is disabled");
	}

	@Override
	public OpCandidate findMatch(MatchingConditions conditions, OpMatcher matcher,
			OpEnvironment env)
	{
		conditions = MatchingConditions.from(conditions.request(), conditions.hints().plus(
				BaseOpHints.Simplification.IN_PROGRESS));
		OpRequest request = conditions.request();
		Iterable<SimplifiedOpInfo> simpleInfos = getSimpleInfos(env, conditions.request().getName());
		// Pass 1 - Check simple infos
		final ArrayList<OpCandidate> candidates = new ArrayList<>();
		for (final SimplifiedOpInfo info: simpleInfos) {
			Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();
			if (typesMatch(info.opType(), conditions.request().getType(),
					typeVarAssigns))
			{
				OpCandidate candidate = new OpCandidate(env, request, info,
						typeVarAssigns);
				candidates.add(candidate);
			}
		}
		if (!candidates.isEmpty()) {
			final List<OpCandidate> matches = filterMatches(candidates);
			return new MatchingResult(candidates, matches, Collections.singletonList(request)).singleMatch();
		}

		// Pass 2 - Focus if needed
		SimplifiedOpRequest simpleReq = new SimplifiedOpRequest(request, env);
		for (final SimplifiedOpInfo info : simpleInfos) {
			Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();
			if (typesMatch(info.opType(), simpleReq.getType(),
					typeVarAssigns))
			{
				FocusedOpInfo focusedInfo = new FocusedOpInfo(info, simpleReq, env);
				OpCandidate candidate = new OpCandidate(env, request, focusedInfo,
						typeVarAssigns);
				candidates.add(candidate);
			}
		}
		List<OpRequest> reqs = Collections.singletonList(conditions.request());
		if (candidates.isEmpty()) {
			return MatchingResult.empty(reqs).singleMatch();
		}
		// narrow down candidates to the exact matches
		final List<OpCandidate> matches = filterMatches(candidates);
		return new MatchingResult(candidates, matches, reqs).singleMatch();
	}

	protected static SortedSet<SimplifiedOpInfo> getSimpleInfos(OpEnvironment env, String name)
	{
		addSimpleInfosToCache(env, name);
		TreeSet<SimplifiedOpInfo> simpleInfos = new TreeSet<>();
		for (OpInfo info: seenNames.get(env).get(name)) {
			if (info instanceof SimplifiedOpInfo) {
				simpleInfos.add((SimplifiedOpInfo) info);
			}
		}
		return simpleInfos;
	}

	protected static SortedSet<OpInfo> getInfos(OpEnvironment env, String name)
	{
		addSimpleInfosToCache(env, name);
		return seenNames.get(env).get(name);
	}

	@Override
	public double priority() {
		return Priority.VERY_LOW;
	}

}
