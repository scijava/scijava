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

import org.scijava.log.Logger;
import org.scijava.ops.OpEnvironment;
import org.scijava.ops.OpUtils;
import org.scijava.ops.matcher.OpCandidate.StatusCode;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
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

	private final Logger log;

	public DefaultOpMatcher(final Logger log) {
		this.log = log;
	}

	@Override
	public OpCandidate findSingleMatch(final OpEnvironment ops, final OpRef ref) throws OpMatchingException {
		return findMatch(ops, ref).singleMatch();
	}

	@Override
	public MatchingResult findMatch(final OpEnvironment ops, final OpRef ref) {
		return findMatch(ops, Collections.singletonList(ref));
	}

	@Override
	public MatchingResult findMatch(final OpEnvironment ops, final List<OpRef> refs) {
		// find candidates with matching name & type
		final List<OpCandidate> candidates = findCandidates(ops, refs);
		if (candidates.isEmpty()) {
			return MatchingResult.empty(refs);
		}
		// narrow down candidates to the exact matches
		final List<OpCandidate> matches = filterMatches(candidates);
		return new MatchingResult(candidates, matches, refs);
	}

	@Override
	public List<OpCandidate> findCandidates(final OpEnvironment ops, final OpRef ref) {
		return findCandidates(ops, Collections.singletonList(ref));
	}

	@Override
	public List<OpCandidate> findCandidates(final OpEnvironment ops, final List<OpRef> refs) {
		final ArrayList<OpCandidate> candidates = new ArrayList<>();
		for (final OpRef ref : refs) {
			for (final OpInfo info : ops.infos(ref.getName())) {
				Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();
				if (ref.typesMatch(info.opType(), typeVarAssigns)) {
					candidates.add(new OpCandidate(ops, log, ref, info, typeVarAssigns));
				}
			}
		}
		return candidates;
	}

	@Override
	public List<OpCandidate> filterMatches(final List<OpCandidate> candidates) {
		final List<OpCandidate> validCandidates = checkCandidates(candidates);

		// List of valid candidates needs to be sorted according to priority.
		// This is used as an optimization in order to not look at ops with
		// lower priority than the already found one.
		validCandidates.sort((c1, c2) -> Double.compare(c2.opInfo().priority(), c1.opInfo().priority()));

		List<OpCandidate> matches;
		matches = filterMatches(validCandidates, (cand) -> typesPerfectMatch(cand));
		if (!matches.isEmpty())
			return matches;

		matches = filterMatches(validCandidates, (cand) -> typesMatch(cand));
		return matches;
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
	 * Filters specified list of candidates using specified predicate. This
	 * method stops filtering when the priority decreases. Expects list of candidates
	 * to be sorted in non-ascending order.
	 *
	 * @param candidates
	 *            the candidates to filter
	 * @param filter
	 *            the condition
	 * @return candidates passing test of condition and having highest priority
	 */
	private List<OpCandidate> filterMatches(final List<OpCandidate> candidates, final Predicate<OpCandidate> filter) {
		final ArrayList<OpCandidate> matches = new ArrayList<>();
		double priority = Double.NaN;
		for (final OpCandidate candidate : candidates) {
			final double p = OpUtils.getPriority(candidate);
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

	/**
	 * Determines if the candidate has some arguments missing.
	 * <p>
	 * Helper method of {@link #filterMatches(List)}.
	 * </p>
	 */
	private boolean missArgs(final OpCandidate candidate, final Type[] paddedArgs) {
		int i = 0;
		for (final Member<?> member : OpUtils.inputs(candidate)) {
			if (paddedArgs[i++] == null && OpUtils.isRequired(member)) {
				candidate.setStatus(StatusCode.REQUIRED_ARG_IS_NULL, null, member);
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine if the arguments and the output types of the candidate
	 * perfectly match with the reference.
	 */
	private boolean typesPerfectMatch(final OpCandidate candidate) {
		int i = 0;
		Type[] paddedArgs = candidate.paddedArgs();
		for (final Type t : OpUtils.inputTypes(candidate)) {
			if (paddedArgs[i] != null) {
				if (!t.equals(paddedArgs[i]))
					return false;
			}
			i++;
		}

		final Type outputType = candidate.getRef().getOutType();
		if (!Objects.equals(outputType, OpUtils.outputType(candidate)))
			return false;

		candidate.setStatus(StatusCode.MATCH);
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
		final Type[] refTypes = candidate.getRef().getTypes();
		final Type infoType = candidate.opInfo().opType();
		Type[] candidateArgTypes = OpUtils.inputTypes(candidate);
		for (Type refType : refTypes) {
			//TODO: can this be simplified?
			Type implementedInfoType = Types.getExactSuperType(infoType, Types.raw(refType));
			if (implementedInfoType instanceof ParameterizedType) {
				Type[] implTypeParams = ((ParameterizedType) implementedInfoType).getActualTypeArguments();
				candidateArgTypes = candidate.opInfo().struct().members().stream()//
						.map(member -> member.isInput() ? member.getType() : null) //
						.toArray(Type[]::new);
				for (int i = 0; i < implTypeParams.length; i++) {
					if (candidateArgTypes[i] == null)
						implTypeParams[i] = null;
				}
				candidateArgTypes = Arrays.stream(implTypeParams) //
						.filter(t -> t != null).toArray(Type[]::new);
				break;
			}
		}

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
}
