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

package org.scijava.ops.engine.matcher.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.scijava.ops.api.OpCandidate;
import org.scijava.ops.api.OpCandidate.StatusCode;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpRef;
import org.scijava.ops.api.OpUtils;
import org.scijava.ops.api.features.MatchingConditions;
import org.scijava.ops.api.features.MatchingRoutine;
import org.scijava.ops.api.features.OpMatcher;
import org.scijava.ops.api.features.OpMatchingException;
import org.scijava.service.AbstractService;
import org.scijava.struct.Member;
import org.scijava.types.Types;
import org.scijava.types.Types.TypeVarInfo;

/**
 * Default implementation of {@link OpMatcher}. Used for finding Ops which match
 * a {@link OpRef request}.
 *
 * @author David Kolb
 */
public class DefaultOpMatcher extends AbstractService implements OpMatcher {

	private final List<MatchingRoutine> matchers;

	public DefaultOpMatcher(Collection<? extends MatchingRoutine> matchers) {
		this.matchers = new ArrayList<>(matchers);
		Collections.sort(this.matchers, Collections.reverseOrder());
	}

	private OpMatchingException agglomeratedException(
		List<OpMatchingException> list)
	{
		OpMatchingException agglomerated = new OpMatchingException(
			"No MatchingRoutine was able to produce a match!");
		for (int i = 0; i < list.size(); i++) {
			agglomerated.addSuppressed(list.get(i));
		}
		return agglomerated;
	}

	// -- Helper methods --

	/**
	 * Performs several checks, whether the specified candidate:</br>
	 * </br>
	 * * {@link #isValid(OpCandidate)}</br>
	 * * {@link #outputsMatch(OpCandidate, HashMap)}</br>
	 * * has a matching number of args</br>
	 * * {@link #missArgs(OpCandidate, Type[])}</br>
	 * </br>
	 * then returns the candidates which fulfill this criteria.
	 *
	 * @param candidates
	 *            the candidates to check
	 * @return candidates passing checks
	 */
	private List<OpCandidate> checkCandidates(final List<OpCandidate> candidates) {
		final ArrayList<OpCandidate> validCandidates = new ArrayList<>();
		for (final OpCandidate candidate : candidates) {
			if (!isValid(candidate))
				continue;
			final Type[] args = candidate.paddedArgs();
			if (args == null)
				continue;
			if (missArgs(candidate, args))
				continue;
			validCandidates.add(candidate);
		}
		return validCandidates;
	}

	/**
	 * Checks whether the output types of the candidate are applicable to the
	 * input types of the {@link OpRef}. Sets candidate status code if there are
	 * too many, to few, or not matching types.
	 *
	 * @param candidate
	 *            the candidate to check inputs for
	 * @param typeBounds
	 *            possibly predetermined type bounds for type variables
	 * @return whether the input types match
	 */
	private boolean inputsMatch(final OpCandidate candidate, HashMap<TypeVariable<?>, TypeVarInfo> typeBounds) {
		if (checkCandidates(Collections.singletonList(candidate)).isEmpty())
			return false;
		final Type[] refArgTypes = candidate.paddedArgs();
		final Type refType = candidate.getRef().getType();
		final Type infoType = candidate.opInfo().opType();
		Type[] candidateArgTypes = OpUtils.inputTypes(candidate);
		Type implementedInfoType = Types.getExactSuperType(infoType, Types.raw(
			refType));
		if (!(implementedInfoType instanceof ParameterizedType)) {
			throw new UnsupportedOperationException(
				"Op type is not a ParameterizedType; we don't know how to deal with these yet.");
		}
			Type[] implTypeParams = ((ParameterizedType) implementedInfoType)
				.getActualTypeArguments();
			candidateArgTypes = candidate.opInfo().struct().members().stream()//
				.map(member -> member.isInput() ? member.getType() : null) //
				.toArray(Type[]::new);
			for (int i = 0; i < implTypeParams.length; i++) {
				if (candidateArgTypes[i] == null) implTypeParams[i] = null;
			}
			candidateArgTypes = Arrays.stream(implTypeParams) //
				.filter(t -> t != null).toArray(Type[]::new);

		if (refArgTypes == null)
			return true; // no constraints on output types

		if (candidateArgTypes.length < refArgTypes.length) {
			candidate.setStatus(StatusCode.TOO_FEW_ARGS);
			return false;
		} else if (candidateArgTypes.length > refArgTypes.length) {
			candidate.setStatus(StatusCode.TOO_MANY_ARGS);
			return false;
		}

		int conflictingIndex = Types.isApplicable(refArgTypes, candidateArgTypes, typeBounds);
		if (conflictingIndex != -1) {
			final Type to = refArgTypes[conflictingIndex];
			final Type from = candidateArgTypes[conflictingIndex];
			candidate.setStatus(StatusCode.ARG_TYPES_DO_NOT_MATCH, //
					"request=" + to.getTypeName() + ", actual=" + from.getTypeName());
			return false;
		}
		return true;
	}

	/**
	 * Determines if the specified candidate is valid and sets status code if
	 * not.
	 *
	 * @param candidate
	 *            the candidate to check
	 * @return whether the candidate is valid
	 */
	private boolean isValid(final OpCandidate candidate) {
		if (candidate.opInfo().isValid()) {
			return true;
		}
		candidate.setStatus(StatusCode.INVALID_STRUCT);
		return false;
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

		// in the case of no matches, throw an agglomerated exception
		throw agglomeratedException(exceptions);
	}

	/**
	 * Determines if the candidate has some arguments missing.
	 * <p>
	 * Helper method of {@link #filterMatches(List)}.
	 * </p>
	 */
	private boolean missArgs(final OpCandidate candidate, final Type[] paddedArgs) {
		int i = 0;
		for (final Member<?> member : OpUtils.inputs(candidate)) {
			if (paddedArgs[i++] == null && member.isRequired()) {
				candidate.setStatus(StatusCode.REQUIRED_ARG_IS_NULL, null, member);
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the output type of the candidate matches the output type
	 * of the {@link OpRef}. Sets candidate status code if they are not matching.
	 *
	 * @param candidate
	 *            the candidate to check output for
	 * @param typeBounds
	 *            possibly predetermined type bounds for type variables
	 * @return whether the output types match
	 */
	private boolean outputsMatch(final OpCandidate candidate, HashMap<TypeVariable<?>, TypeVarInfo> typeBounds) {
		final Type refOutType = candidate.getRef().getOutType();
		if (refOutType == null)
			return true; // no constraints on output types

		if(candidate.opInfo().output().isInput()) return true;
		final Type candidateOutType = OpUtils.outputType(candidate);
		final int conflictingIndex = MatchingUtils.checkGenericOutputsAssignability(new Type[] { candidateOutType },
			new Type[] { refOutType }, typeBounds);
		if (conflictingIndex != -1) {
			candidate.setStatus(StatusCode.OUTPUT_TYPES_DO_NOT_MATCH, //
					"request=" + refOutType.getTypeName() + ", actual=" + candidateOutType.getTypeName());
			return false;
		}
		return true;
	}

	/**
	 * Checks whether the arg types of the candidate satisfy the padded arg
	 * types of the candidate. Sets candidate status code if there are too many,
	 * to few,not matching arg types or if a match was found.
	 *
	 * @param candidate
	 *            the candidate to check args for
	 * @return whether the arg types are satisfied
	 */
	@Override
	public boolean typesMatch(final OpCandidate candidate) {
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
}
