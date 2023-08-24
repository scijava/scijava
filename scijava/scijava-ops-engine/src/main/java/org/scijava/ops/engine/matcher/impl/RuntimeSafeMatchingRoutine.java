
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

import org.scijava.ops.api.OpRetrievalException;
import org.scijava.ops.engine.OpCandidate;
import org.scijava.ops.engine.OpCandidate.StatusCode;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRef;
import org.scijava.ops.engine.OpUtils;
import org.scijava.ops.engine.MatchingConditions;
import org.scijava.ops.engine.matcher.MatchingResult;
import org.scijava.ops.engine.matcher.MatchingRoutine;
import org.scijava.ops.engine.matcher.OpMatcher;
import org.scijava.priority.Priority;
import org.scijava.struct.Member;
import org.scijava.types.Types;
import org.scijava.types.Types.TypeVarInfo;
import org.scijava.types.inference.GenericAssignability;

public class RuntimeSafeMatchingRoutine implements MatchingRoutine {

	@Override
	public void checkSuitability(MatchingConditions conditions)
		throws OpRetrievalException
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
			if (typesMatch(info.opType(), conditions.ref().getType(),
				typeVarAssigns))
			{
				OpCandidate candidate = new OpCandidate(env, conditions.ref(), info,
					typeVarAssigns);
				candidates.add(candidate);
			}
		}
		List<OpRef> refs = Collections.singletonList(conditions.ref());
		if (candidates.isEmpty()) {
			return MatchingResult.empty(refs).singleMatch();
		}
		// narrow down candidates to the exact matches
		final List<OpCandidate> matches = filterMatches(candidates);
		return new MatchingResult(candidates, matches, refs).singleMatch();

	}

	/**
	 * Performs several checks, whether the specified candidate:</br>
	 * </br>
	 * * {@link #outputsMatch(OpCandidate, HashMap)}</br>
	 * * has a matching number of args</br>
	 * * {@link #missArgs(OpCandidate, Type[])}</br>
	 * </br>
	 * then returns the candidates which fulfill this criteria.
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

	private List<OpCandidate> filterMatches(final List<OpCandidate> candidates) {
		final List<OpCandidate> validCandidates = checkCandidates(candidates);

		// List of valid candidates needs to be sorted according to priority.
		// This is used as an optimization in order to not look at ops with
		// lower priority than the already found one.
		validCandidates.sort((c1, c2) -> Double.compare(c2.priority(), c1
			.priority()));

		List<OpCandidate> matches;
		matches = filterMatches(validCandidates, (cand) -> typesPerfectMatch(cand));
		if (!matches.isEmpty()) return matches;

		matches = filterMatches(validCandidates, (cand) -> typesMatch(cand));
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

	protected Iterable<OpInfo> getInfos(OpEnvironment env,
		MatchingConditions conditions)
	{
		return env.infos(conditions.ref().getName(), conditions.hints());
	}

	/**
	 * Checks whether the output types of the candidate are applicable to the
	 * input types of the {@link OpRef}. Sets candidate status code if there are
	 * too many, to few, or not matching types.
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
		final Type[] refArgTypes = candidate.paddedArgs();
		final Type refType = candidate.getRef().getType();
		final Type infoType = candidate.opInfo().opType();
		Type implementedInfoType = Types.getExactSuperType(infoType, Types.raw(
			refType));
		if (!(implementedInfoType instanceof ParameterizedType)) {
			throw new UnsupportedOperationException(
				"Op type is not a ParameterizedType; we don't know how to deal with these yet.");
		}
		Type[] implTypeParams = ((ParameterizedType) implementedInfoType)
			.getActualTypeArguments();
		Type[] candidateArgTypes = candidate.opInfo().struct().members().stream()//
			.map(member -> member.isInput() ? member.getType() : null) //
			.toArray(Type[]::new);
		for (int i = 0; i < implTypeParams.length; i++) {
			if (candidateArgTypes[i] == null) implTypeParams[i] = null;
		}
		candidateArgTypes = Arrays.stream(implTypeParams) //
			.filter(t -> t != null).toArray(Type[]::new);

		if (refArgTypes == null) return true; // no constraints on output types

		if (candidateArgTypes.length < refArgTypes.length) {
			candidate.setStatus(StatusCode.TOO_FEW_ARGS);
			return false;
		}
		else if (candidateArgTypes.length > refArgTypes.length) {
			candidate.setStatus(StatusCode.TOO_MANY_ARGS);
			return false;
		}

		int conflictingIndex = Types.isApplicable(refArgTypes, candidateArgTypes,
			typeBounds);
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
	 * the {@link OpRef}. Sets candidate status code if they are not matching.
	 *
	 * @param candidate the candidate to check output for
	 * @param typeBounds possibly predetermined type bounds for type variables
	 * @return whether the output types match
	 */
	private boolean outputsMatch(final OpCandidate candidate,
		HashMap<TypeVariable<?>, TypeVarInfo> typeBounds)
	{
		final Type refOutType = candidate.getRef().getOutType();
		if (refOutType == null) return true; // no constraints on output types

		if (candidate.opInfo().output().isInput()) return true;
		final Type candidateOutType = candidate.opInfo().outputType();
		final int conflictingIndex = MatchingUtils.checkGenericOutputsAssignability(
			new Type[] { candidateOutType }, new Type[] { refOutType }, typeBounds);
		if (conflictingIndex != -1) {
			candidate.setStatus(StatusCode.OUTPUT_TYPES_DO_NOT_MATCH, //
				"request=" + refOutType.getTypeName() + ", actual=" + candidateOutType
					.getTypeName());
			return false;
		}
		return true;
	}

	/**
	 * Checks whether the arg types of the candidate satisfy the padded arg types
	 * of the candidate. Sets candidate status code if there are too many, to
	 * few,not matching arg types or if a match was found.
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
	private boolean typesMatch(final Type opType, final Type refType,
		final Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		if (refType == null) return true;
		if (refType instanceof ParameterizedType) {
			if (!GenericAssignability.checkGenericAssignability(opType,
				(ParameterizedType) refType, typeVarAssigns, true))
			{
				return false;
			}
		}
		else {
			if (!Types.isAssignable(opType, refType)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determine if the arguments and the output types of the candidate perfectly
	 * match with the reference.
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

		final Type outputType = candidate.getRef().getOutType();
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
