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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.ops.OpEnvironment;
import org.scijava.ops.OpUtils;
import org.scijava.ops.matcher.OpCandidate.StatusCode;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.struct.Member;
import org.scijava.struct.StructInstance;
import org.scijava.util.Types;

/**
 * Default service for finding ops which match a request.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultOpTypeMatchingService extends AbstractService implements OpTypeMatchingService {

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	// -- OpMatchingService methods --

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
		for (final OpInfo info : ops.infos()) {
			for (final OpRef ref : refs) {
				if (ref.typesMatch(info.opClass())) {
					candidates.add(new OpCandidate(ops, ref, info));
				}
			}
		}
		return candidates;
	}

	@Override
	public List<OpCandidate> filterMatches(final List<OpCandidate> candidates) {
		final List<OpCandidate> validCandidates = checkCandidates(candidates);

		List<OpCandidate> matches;
		matches = filterMatches(validCandidates, (cand) -> typesPerfectMatch(cand));
		if (!matches.isEmpty())
			return matches;

		// Do we need this anymore?
		// matches = castMatches(validCandidates);
		// if (!matches.isEmpty())
		// return matches;

		matches = filterMatches(validCandidates, (cand) -> typesMatch(cand));
		return matches;
	}

	@Override
	public StructInstance<?> match(final OpCandidate candidate) {
		if (checkCandidates(Collections.singletonList(candidate)).isEmpty() || !typesMatch(candidate))
			return null;
		return candidate.createOp();
	}


	/**
	 * Checks whether the arg types of the candidate satisfy the padded arg types of the candidate.
	 * Sets candidate status code if there are too many, to few,not matching arg types or if a match was found.
	 * 
	 * @param candidate the candidate to check args for
	 * @return whether the arg types are satisfied
	 */
	@Override
	public boolean typesMatch(final OpCandidate candidate) {
		if (checkCandidates(Collections.singletonList(candidate)).isEmpty())
			return false;
		final Type[] refArgTypes = candidate.paddedArgs();
		final Type[] candidateArgTypes = OpUtils.inputTypes(candidate);

		if (refArgTypes == null)
			return true; // no constraints on output types

		if (candidateArgTypes.length < refArgTypes.length) {
			candidate.setStatus(StatusCode.TOO_FEW_ARGS);
			return false;
		} else if (candidateArgTypes.length > refArgTypes.length) {
			candidate.setStatus(StatusCode.TOO_MANY_ARGS);
			return false;
		}

		int conflictingIndex = Types.isApplicable(refArgTypes, candidateArgTypes);
		if (conflictingIndex != -1) {
			final Type to = refArgTypes[conflictingIndex];
			final Type from = candidateArgTypes[conflictingIndex];
			candidate.setStatus(StatusCode.ARG_TYPES_DO_NOT_MATCH, //
					"request=" + to.getTypeName() + ", actual=" + from.getTypeName());
			return false;
		}
		candidate.setStatus(StatusCode.MATCH);
		return true;
	}

	// -- Helper methods --

	/**
	 * Performs several checks, whether the specified candidate:</br></br>
	 * * {@link #isValid(OpCandidate)}</br>
	 * * {@link #outputsMatch(OpCandidate)}</br>
	 * * has a matching number of args</br>
	 * * {@link #missArgs(OpCandidate, Type[])}</br></br>
	 * then returns the candidates which fulfill this criteria.
	 * 
	 * @param candidates the candidates to check
	 * @return candidates passing checks
	 */
	private List<OpCandidate> checkCandidates(final List<OpCandidate> candidates) {
		final ArrayList<OpCandidate> validCandidates = new ArrayList<>();
		for (final OpCandidate candidate : candidates) {
			if (!isValid(candidate) || !outputsMatch(candidate))
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
	 * Filters specified list of candidates using specified predicate.
	 * This method stops filtering when the priority decreases.
	 * 
	 * @param candidates the candidates to filter
	 * @param filter the condition
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
	 * Determine if the arguments of the candidate perfectly match with the
	 * reference.
	 * <p>
	 * Helper method of {@link #filterMatches(List)}.
	 * </p>
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
		return true;
	}

	/**
	 * Extracts a list of candidates that requires casting to match with the
	 * reference.
	 * <p>
	 * Helper method of {@link #filterMatches(List)}.
	 * </p>
	 */
	private List<OpCandidate> castMatches(final List<OpCandidate> candidates) {
		final ArrayList<OpCandidate> matches = new ArrayList<>();
		int minLevels = Integer.MAX_VALUE;
		double priority = Double.NaN;
		for (final OpCandidate candidate : candidates) {
			final double p = OpUtils.getPriority(candidate);
			if (p != priority && !matches.isEmpty()) {
				// NB: Lower priority was reached; stop looking for any more
				// matches.
				break;
			}
			priority = p;

			final int nextLevels = findCastLevels(candidate);
			if (nextLevels < 0 || nextLevels > minLevels)
				continue;
			if (nextLevels < minLevels) {
				matches.clear();
				minLevels = nextLevels;
			}
			matches.add(candidate);
		}
		return matches;
	}

	/**
	 * Find the total levels of casting needed for the candidate to match with
	 * the reference.
	 * <p>
	 * Helper method of {@link #filterMatches(List)}.
	 * </p>
	 */
	private int findCastLevels(final OpCandidate candidate) {
		final Type[] paddedArgs = candidate.paddedArgs();
		int level = 0, i = 0;
		for (final Member<?> member : OpUtils.inputs(candidate)) {
			final Class<?> type = member.getRawType();
			if (paddedArgs[i] != null) {
				final int currLevel = OpMatchingUtil.findCastLevels(type, OpMatchingUtil.getClass(paddedArgs[i]));
				if (currLevel < 0)
					return -1;
				level += currLevel;
			}
			i++;
		}
		return level;
	}

	/**
	 * Determines if the specified candidate is valid and sets status code if not.
	 * 
	 * @param candidate the candidate to check
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
	 * Checks whether the output types of the candidate satisfy the output types of the {@link OpRef}.
	 * Sets candidate status code if there are too many, to few, or not matching types.
	 * 
	 * @param candidate the candidate to check outputs for
	 * @return whether the output types are satisfied
	 */
	private boolean outputsMatch(final OpCandidate candidate) {
		final Type[] refOutTypes = candidate.getRef().getOutTypes();
		if (refOutTypes == null)
			return true; // no constraints on output types

		Type[] candidateOutTypes = OpUtils.outputTypes(candidate);
		if (candidateOutTypes.length < refOutTypes.length) {
			candidate.setStatus(StatusCode.TOO_FEW_OUTPUTS);
			return false;
		} else if (candidateOutTypes.length > refOutTypes.length) {
			candidate.setStatus(StatusCode.TOO_MANY_OUTPUTS);
			return false;
		}

		int conflictingIndex = Types.isApplicable(candidateOutTypes, refOutTypes);
		if (conflictingIndex != -1) {
			final Type to = refOutTypes[conflictingIndex];
			final Type from = candidateOutTypes[conflictingIndex];
			candidate.setStatus(StatusCode.OUTPUT_TYPES_DO_NOT_MATCH, //
					"request=" + to.getTypeName() + ", actual=" + from.getTypeName());
			return false;
		}
		return true;
	}

	private String typeClashMessage(final OpCandidate candidate, final Type[] args, final int index) {
		int i = 0;
		for (final Member<?> item : OpUtils.inputs(candidate)) {
			if (i++ == index) {
				final Type arg = args[index];
				final String argType = arg == null ? "null" : arg.getClass().getName();
				final Type inputType = item.getType();
				return index + ": cannot coerce " + argType + " -> " + inputType;
			}
		}
		throw new IllegalArgumentException("Invalid index: " + index);
	}
}
