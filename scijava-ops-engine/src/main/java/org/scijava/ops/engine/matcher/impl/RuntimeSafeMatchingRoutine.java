/*-
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.api.OpRequest;
import org.scijava.ops.engine.MatchingConditions;
import org.scijava.ops.engine.matcher.OpCandidate;
import org.scijava.ops.engine.matcher.OpCandidate.StatusCode;
import org.scijava.ops.engine.matcher.MatchingResult;
import org.scijava.ops.engine.matcher.MatchingRoutine;
import org.scijava.ops.engine.matcher.OpMatcher;
import org.scijava.priority.Priority;
import org.scijava.struct.Member;
import org.scijava.types.Types;
import org.scijava.types.Types.TypeVarInfo;
import org.scijava.types.infer.GenericAssignability;

public class RuntimeSafeMatchingRoutine implements MatchingRoutine {

	@Override
	public void checkSuitability(MatchingConditions conditions)
		throws OpMatchingException
	{
		// NB: Runtime-safe matching should always be allowed
	}

	@Override
	public OpCandidate findMatch(MatchingConditions conditions, OpMatcher matcher,
		OpEnvironment env)
	{
		final ArrayList<OpCandidate> candidates = new ArrayList<>();

		for (final OpInfo info : getInfos(env, conditions)) {
			Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();
			if (typesMatch(info.opType(), conditions.request().type(),
				typeVarAssigns))
			{
				OpCandidate candidate = new OpCandidate(env, conditions.request(), info,
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

	/**
	 * Performs several checks, whether the specified candidate:</br>
	 * </br>
	 * * {@link #outputsMatch(OpCandidate, HashMap)}</br>
	 * * has a matching number of args</br>
	 * * {@link #missArgs(OpCandidate, Type[])}</br>
	 * </br>
	 * then returns the candidates which fulfill these criteria.
	 *
	 * @param candidates the candidates to check
	 * @return candidates passing checks
	 */
	private List<OpCandidate> checkCandidates(
		final List<OpCandidate> candidates)
	{
		final ArrayList<OpCandidate> validCandidates = new ArrayList<>();
		for (final OpCandidate candidate : candidates) {
			final Type[] args = candidate.paddedArgs();
			if (args == null) continue;
			if (missArgs(candidate, args)) continue;
			validCandidates.add(candidate);
		}
		return validCandidates;
	}

	protected List<OpCandidate> filterMatches(
		final List<OpCandidate> candidates)
	{
		final List<OpCandidate> validCandidates = checkCandidates(candidates);

		// List of valid candidates needs to be sorted according to priority.
		// This is used as an optimization in order to not look at ops with
		// lower priority than the already found one.
		validCandidates.sort((c1, c2) -> Double.compare(c2.priority(), c1
			.priority()));

		List<OpCandidate> matches;
		matches = filterMatches(validCandidates, this::typesPerfectMatch);
		if (!matches.isEmpty()) return matches;

		matches = filterMatches(validCandidates, this::typesMatch);
		return matches;
	}

	/**
	 * Filters specified list of candidates using specified predicate. This method
	 * stops filtering when the priority decreases. Expects list of candidates to
	 * be sorted in non-ascending order.
	 *
	 * @param candidates the candidates to filter
	 * @param filter the condition
	 * @return candidates passing test of condition and having highest priority
	 */
	private List<OpCandidate> filterMatches(final List<OpCandidate> candidates,
		final Predicate<OpCandidate> filter)
	{
		final ArrayList<OpCandidate> matches = new ArrayList<>();
		double priority = Double.NaN;
		for (final OpCandidate candidate : candidates) {
			final double p = candidate.priority();
			if (p != priority && !matches.isEmpty()) {
				// NB: Lower priority was reached; stop looking for any more
				// matches.
				break;
			}
			priority = p;

			if (filter.test(candidate)) {
				matches.add(candidate);
			}
		}
		return matches;
	}

	private Iterable<OpInfo> getInfos(OpEnvironment env,
		MatchingConditions conditions)
	{
		return env.infos(conditions.request().name(), conditions.hints());
	}

	/**
	 * Checks whether the output types of the candidate are applicable to the
	 * input types of the {@link OpRequest}. Sets candidate status code if there
	 * are too many, too few, or not matching types.
	 *
	 * @param candidate the candidate to check inputs for
	 * @param typeBounds possibly predetermined type bounds for type variables
	 * @return whether the input types match
	 */
	private boolean inputsMatch(final OpCandidate candidate,
		HashMap<TypeVariable<?>, TypeVarInfo> typeBounds)
	{
		if (checkCandidates(Collections.singletonList(candidate)).isEmpty())
			return false;
		final Type[] reqArgTypes = candidate.paddedArgs();
		final Type reqType = candidate.getRequest().type();
		final Type infoType = candidate.opInfo().opType();
		Type implementedInfoType = Types.superTypeOf(infoType, Types.raw(
			reqType));
		if (!(implementedInfoType instanceof ParameterizedType)) {
			throw new UnsupportedOperationException(
				"Op type is not a ParameterizedType; we don't know how to deal with these yet.");
		}
		Type[] implTypeParams = ((ParameterizedType) implementedInfoType)
			.getActualTypeArguments();
		Type[] candidateArgTypes = candidate.opInfo().struct().members().stream()//
			.map(member -> member.isInput() ? member.type() : null) //
			.toArray(Type[]::new);
		for (int i = 0; i < implTypeParams.length; i++) {
			if (candidateArgTypes[i] == null) implTypeParams[i] = null;
		}
		candidateArgTypes = Arrays.stream(implTypeParams) //
			.filter(Objects::nonNull).toArray(Type[]::new);

		if (reqArgTypes == null) return true; // no constraints on output types

		if (candidateArgTypes.length < reqArgTypes.length) {
			candidate.setStatus(StatusCode.TOO_FEW_ARGS);
			return false;
		}
		else if (candidateArgTypes.length > reqArgTypes.length) {
			candidate.setStatus(StatusCode.TOO_MANY_ARGS);
			return false;
		}

		int conflictingIndex = Types.isApplicable(reqArgTypes, candidateArgTypes,
			typeBounds);
		if (conflictingIndex != -1) {
			final Type to = reqArgTypes[conflictingIndex];
			final Type from = candidateArgTypes[conflictingIndex];
			candidate.setStatus(StatusCode.ARG_TYPES_DO_NOT_MATCH, //
				"request=" + to.getTypeName() + ", actual=" + from.getTypeName());
			return false;
		}
		return true;
	}

	/**
	 * Determines if the candidate has some arguments missing.
	 * <p>
	 * Helper method of {@link #filterMatches(List)}.
	 * </p>
	 */
	private boolean missArgs(final OpCandidate candidate,
		final Type[] paddedArgs)
	{
		int i = 0;
		for (final Member<?> member : candidate.opInfo().inputs()) {
			if (paddedArgs[i++] == null && member.isRequired()) {
				candidate.setStatus(StatusCode.REQUIRED_ARG_IS_NULL, null, member);
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the output type of the candidate matches the output type of
	 * the {@link OpRequest}. Sets candidate status code if they are not matching.
	 *
	 * @param candidate the candidate to check output for
	 * @param typeBounds possibly predetermined type bounds for type variables
	 * @return whether the output types match
	 */
	private boolean outputsMatch(final OpCandidate candidate,
		HashMap<TypeVariable<?>, TypeVarInfo> typeBounds)
	{
		final Type reqOutType = candidate.getRequest().outType();
		if (reqOutType == null) return true; // no constraints on output types

		if (candidate.opInfo().output().isInput()) return true;
		final Type candidateOutType = candidate.opInfo().outputType();
		final int conflictingIndex = MatchingUtils.checkGenericOutputsAssignability(
			new Type[] { candidateOutType }, new Type[] { reqOutType }, typeBounds);
		if (conflictingIndex != -1) {
			candidate.setStatus(StatusCode.OUTPUT_TYPES_DO_NOT_MATCH, //
				"request=" + reqOutType.getTypeName() + ", actual=" + candidateOutType
					.getTypeName());
			return false;
		}
		return true;
	}

	/**
	 * Checks whether the arg types of the candidate satisfy the padded arg types
	 * of the candidate. Sets candidate status code if there are too many, too
	 * few, not matching arg types or if a match was found.
	 *
	 * @param candidate the candidate to check args for
	 * @return whether the arg types are satisfied
	 */
	private boolean typesMatch(final OpCandidate candidate) {
		HashMap<TypeVariable<?>, TypeVarInfo> typeBounds = new HashMap<>();
		if (!inputsMatch(candidate, typeBounds)) {
			return false;
		}
		if (!outputsMatch(candidate, typeBounds)) {
			return false;
		}
		candidate.setStatus(StatusCode.MATCH);
		return true;
	}

	/**
	 * Determines whether the specified type satisfies the op's required types
	 * using {@link Types#isApplicable(Type[], Type[])}.
	 */
	protected boolean typesMatch(final Type opType, final Type reqType,
		final Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		if (reqType == null) return true;
		try {
			if (reqType instanceof ParameterizedType) {
				if (!GenericAssignability.checkGenericAssignability(opType,
					(ParameterizedType) reqType, typeVarAssigns, true))
				{
					return false;
				}
			}
			else {
				if (!Types.isAssignable(opType, reqType)) {
					return false;
				}
			}
		}
		catch (IllegalStateException e) {
			return false;
		}
		return true;
	}

	/**
	 * Determine if the arguments and the output types of the candidate perfectly
	 * match with the request.
	 */
	private boolean typesPerfectMatch(final OpCandidate candidate) {
		int i = 0;
		Type[] paddedArgs = candidate.paddedArgs();
		for (final Type t : candidate.opInfo().inputTypes()) {
			if (paddedArgs[i] != null) {
				if (!t.equals(paddedArgs[i])) return false;
			}
			i++;
		}

		final Type outputType = candidate.getRequest().outType();
		if (!Objects.equals(outputType, candidate.opInfo().outputType()))
			return false;

		candidate.setStatus(StatusCode.MATCH);
		return true;
	}

	@Override
	public double priority() {
		return Priority.HIGH;
	}

}
