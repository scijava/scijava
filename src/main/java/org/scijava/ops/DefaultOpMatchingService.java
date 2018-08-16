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

package org.scijava.ops;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.Initializable;
import org.scijava.log.LogService;
import org.scijava.module.Module;
import org.scijava.ops.OpCandidate.StatusCode;
import org.scijava.param.ParameterMember;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.StructInstance;
import org.scijava.util.Types;

/**
 * Default service for finding ops which match a request.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultOpMatchingService extends AbstractService implements
	OpMatchingService
{

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	// -- OpMatchingService methods --

	@Override
	public OpCandidate findMatch(final OpEnvironment ops, final OpRef ref) {
		return findMatch(ops, Collections.singletonList(ref));
	}

	@Override
	public OpCandidate findMatch(final OpEnvironment ops,
		final List<OpRef> refs)
	{
		// find candidates with matching name & type
		final List<OpCandidate> candidates = findCandidates(ops, refs);
		assertCandidates(candidates, refs.get(0));

		// narrow down candidates to the exact matches
		final List<OpCandidate> matches = filterMatches(candidates);

		return singleMatch(candidates, matches);
	}

	@Override
	public List<OpCandidate> findCandidates(final OpEnvironment ops,
		final OpRef ref)
	{
		return findCandidates(ops, Collections.singletonList(ref));
	}

	@Override
	public List<OpCandidate> findCandidates(final OpEnvironment ops,
		final List<OpRef> refs)
	{
		final ArrayList<OpCandidate> candidates = new ArrayList<>();
		for (final OpInfo info : ops.infos()) {
			for (final OpRef ref : refs) {
				if (isCandidate(info, ref)) {
					candidates.add(new OpCandidate(ops, ref, info));
				}
			}
		}
		return candidates;
	}

	@Override
	public List<OpCandidate> filterMatches(final List<OpCandidate> candidates) {
		final List<OpCandidate> validCandidates = validCandidates(candidates);

		List<OpCandidate> matches;

		matches = filterMatches(validCandidates, (cand) -> typesPerfectMatch(cand));
		if (!matches.isEmpty()) return matches;

		matches = castMatches(validCandidates);
		if (!matches.isEmpty()) return matches;

		// NB: Not implemented yet
//		matches = filterMatches(validCandidates, (cand) -> losslessMatch(cand));
//		if (!matches.isEmpty()) return matches;

		matches = filterMatches(validCandidates, (cand) -> typesMatch(cand));
		return matches;
	}

	@Override
	public Object match(final OpCandidate candidate) {
		if (!valid(candidate)) return null;
		if (!outputsMatch(candidate)) return null;
		final Object[] args = padArgs(candidate);
		return args == null ? null : match(candidate, args);
	}

	@Override
	public boolean typesMatch(final OpCandidate candidate) {
		if (!valid(candidate)) return false;
		final Object[] args = padArgs(candidate);
		return args == null ? false : typesMatch(candidate, args) < 0;
	}

	@Override
	public <C> StructInstance<C> assignInputs(final StructInstance<C> op,
		final Object... args)
	{
		int i = 0;
		for (final MemberInstance<?> memberInstance : inputs(op)) {
			// TODO: Value coercion / conversion?
			memberInstance.set(args[i++]);
			// TODO: Resolve input? What happens if we don't?
		}
		return op;
	}

	private List<MemberInstance<?>> inputs(StructInstance<?> op) {
		return op.members().stream() //
			.filter(member -> member.member().isInput()) //
			.collect(Collectors.toList());
	}

	private List<Member<?>> inputs(OpCandidate candidate) {
		return candidate.struct().members().stream() //
			.filter(member -> member.isInput()) //
			.collect(Collectors.toList());
	}

	private List<Member<?>> outputs(OpCandidate candidate) {
		return candidate.struct().members().stream() //
			.filter(member -> member.isOutput()) //
			.collect(Collectors.toList());
	}

	@Override
	public Object[] padArgs(final OpCandidate candidate) {
		int inputCount = 0, requiredCount = 0;
		for (final Member<?> item : candidate.struct().members()) {
			inputCount++;
			if (isRequired(item)) requiredCount++;
		}
		final Object[] args = candidate.getRef().getArgs();
		if (args.length == inputCount) {
			// correct number of arguments
			return args;
		}
		if (args.length > inputCount) {
			// too many arguments
			candidate.setStatus(StatusCode.TOO_MANY_ARGS, args.length + " > " +
				inputCount);
			return null;
		}
		if (args.length < requiredCount) {
			// too few arguments
			candidate.setStatus(StatusCode.TOO_FEW_ARGS, args.length + " < " +
				requiredCount);
			return null;
		}

		// pad optional parameters with null (from right to left)
		final int argsToPad = inputCount - args.length;
		final int optionalCount = inputCount - requiredCount;
		final int optionalsToFill = optionalCount - argsToPad;
		final Object[] paddedArgs = new Object[inputCount];
		int argIndex = 0, paddedIndex = 0, optionalIndex = 0;
		for (final Member<?> item : candidate.struct().members()) {
			if (!isRequired(item) && optionalIndex++ >= optionalsToFill) {
				// skip this optional parameter (pad with null)
				paddedIndex++;
				continue;
			}
			paddedArgs[paddedIndex++] = args[argIndex++];
		}
		return paddedArgs;
	}

	// -- Helper methods --

	private boolean isRequired(final Member<?> item) {
		return item instanceof ParameterMember && //
			((ParameterMember<?>) item).isRequired();
	}

	/** Helper method of {@link #findCandidates}. */
	private boolean isCandidate(final OpInfo info, final OpRef ref) {
		if (!info.nameMatches(ref.getName())) return false;

		// the name matches; now check the class
		return ref.typesMatch(info.opClass());
	}

	/** Helper method of {@link #findMatch}. */
	private void assertCandidates(final List<OpCandidate> candidates,
		final OpRef ref)
	{
		if (candidates.isEmpty()) {
			throw new IllegalArgumentException("No candidate '" + ref.getLabel() +
				"' ops");
		}
	}

	/**
	 * Gets a list of valid candidates injected with padded arguments.
	 * <p>
	 * Helper method of {@link #filterMatches}.
	 * </p>
	 * 
	 * @param candidates list of candidates
	 * @return a list of valid candidates with arguments injected
	 */
	private List<OpCandidate> validCandidates(
		final List<OpCandidate> candidates)
	{
		final ArrayList<OpCandidate> validCandidates = new ArrayList<>();
		for (final OpCandidate candidate : candidates) {
			if (!valid(candidate) || !outputsMatch(candidate)) continue;
			final Object[] args = padArgs(candidate);
			if (args == null) continue;
			candidate.setArgs(args);
			if (missArgs(candidate)) continue;
			validCandidates.add(candidate);
		}
		return validCandidates;
	}

	/**
	 * Determines if the candidate arguments match with lossless conversion. Needs
	 * support from the conversion in the future.
	 */
	@SuppressWarnings("unused")
	private boolean losslessMatch(final OpCandidate candidate) {
		// NB: Not yet implemented
		return false;
	}

	/**
	 * Filters out candidates that pass the given filter.
	 * <p>
	 * Helper method of {@link #filterMatches(List)}.
	 * </p>
	 */
	private List<OpCandidate> filterMatches(final List<OpCandidate> candidates,
		final Predicate<OpCandidate> filter)
	{
		final ArrayList<OpCandidate> matches = new ArrayList<>();
		double priority = Double.NaN;
		for (final OpCandidate candidate : candidates) {
			final double p = getPriority(candidate);
			if (p != priority && !matches.isEmpty()) {
				// NB: Lower priority was reached; stop looking for any more matches.
				break;
			}
			priority = p;

			if (filter.test(candidate)) { // TODO: Contingent conformance?
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
	private boolean missArgs(final OpCandidate candidate) {
		int i = 0;
		for (final Member<?> member : inputs(candidate)) {
			if (candidate.getArgs()[i++] == null && isRequired(member)) {
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
		final Object[] args = candidate.getArgs();
		for (final Member<?> member : inputs(candidate)) {
			if (args[i] != null) {
				final Class<?> typeClass = member.getRawType();
				final Class<?> argClass = OpMatchingUtil.getClass(args[i]);
				if (!typeClass.equals(argClass)) return false;
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
			final double p = getPriority(candidate);
			if (p != priority && !matches.isEmpty()) {
				// NB: Lower priority was reached; stop looking for any more matches.
				break;
			}
			priority = p;

			final int nextLevels = findCastLevels(candidate);
			if (nextLevels < 0 || nextLevels > minLevels) continue;

			// TODO: Contingent conformance?

			if (nextLevels < minLevels) {
				matches.clear();
				minLevels = nextLevels;
			}
			matches.add(candidate);
		}
		return matches;
	}

	private double getPriority(final OpCandidate candidate) {
		// TODO: Think about what to do about non @Plugin-based ops...?
		// What if there is no annotation? How to discern a priority?
		return candidate.opInfo().getAnnotation().priority();
	}

	/**
	 * Find the total levels of casting needed for the candidate to match with the
	 * reference.
	 * <p>
	 * Helper method of {@link #filterMatches(List)}.
	 * </p>
	 */
	private int findCastLevels(final OpCandidate candidate) {
		int level = 0, i = 0;
		final Object[] args = candidate.getArgs();
		for (final Member<?> member : inputs(candidate)) {
			final Class<?> type = member.getRawType();
			if (args[i] != null) {
				final int currLevel = OpMatchingUtil.findCastLevels(type, OpMatchingUtil
					.getClass(args[i]));
				if (currLevel < 0) return -1;
				level += currLevel;
			}
			i++;
		}
		return level;
	}

	/**
	 * Extracts and returns the single match from the given list of matches,
	 * executing the linked {@link Module}'s initializer if applicable. If there
	 * is not exactly one match, an {@link IllegalArgumentException} is thrown
	 * with an analysis of the problem(s).
	 * <p>
	 * Helper method of {@link #findMatch}.
	 * </p>
	 * 
	 * @param candidates The original unfiltered list of candidates, used during
	 *          the analysis if there was a problem finding exactly one match.
	 * @param matches The list of matching candidates.
	 * @return The single matching candidate, with its module initialized.
	 * @throws IllegalArgumentException If there is not exactly one matching
	 *           candidate.
	 */
	private OpCandidate singleMatch(final List<OpCandidate> candidates,
		final List<OpCandidate> matches)
	{
		if (matches.size() == 1) {
			// a single match: initialize and return it
			final StructInstance<?> m = matches.get(0).getModule();
			if (log.isDebug()) {
				log.debug("Selected '" + matches.get(0).getRef().getLabel() + "' op: " +
					m.object().getClass().getName());
			}

			// initialize the op, if appropriate
			if (m.object() instanceof Initializable) {
				((Initializable) m.object()).initialize();
			}

			return matches.get(0);
		}

//		final String analysis = OpUtils.matchInfo(candidates, matches);
		throw new IllegalArgumentException("TODO dump analysis");
	}

	/**
	 * Verifies that the given candidate's module is valid.
	 * <p>
	 * Helper method of {@link #match(OpCandidate)}.
	 * </p>
	 */
	private boolean valid(final OpCandidate candidate) {
		// TODO: candidate validity? Struct no longer has this thing.
		return true;
//		candidate.setStatus(StatusCode.INVALID_MODULE);
//		return false;
	}

	/**
	 * Verifies that the given candidate's output types match those of the op.
	 * <p>
	 * Helper method of {@link #match(OpCandidate)}.
	 * </p>
	 */
	private boolean outputsMatch(final OpCandidate candidate) {
		final Collection<Type> outTypes = candidate.getRef().getOutTypes();
		if (outTypes == null) return true; // no constraints on output types

		final Iterator<Member<?>> outItems = outputs(candidate).iterator();
		for (final Type outType : outTypes) {
			if (!outItems.hasNext()) {
				candidate.setStatus(StatusCode.TOO_FEW_OUTPUTS);
				return false;
			}
			// FIXME: Use generic assignability test, once it exists.
			final Class<?> raw = Types.raw(outType);
			final Class<?> outItemClass = outItems.next().getRawType();
			if (!Types.isAssignable(outItemClass, raw)) {
				candidate.setStatus(StatusCode.OUTPUT_TYPES_DO_NOT_MATCH, //
					"request=" + raw.getName() + ", actual=" + outItemClass.getName());
				return false;
			}
		}
		return true;
	}

	/** Helper method of {@link #match(OpCandidate)}. */
	private Object match(final OpCandidate candidate, final Object[] args) {
		// check that each parameter is compatible with its argument
		final int badIndex = typesMatch(candidate, args);
		if (badIndex >= 0) {
			final String message = typeClashMessage(candidate, args, badIndex);
			candidate.setStatus(StatusCode.ARG_TYPES_DO_NOT_MATCH, message);
			return null;
		}

		// create module and assign the inputs
		final Object op = createOp(candidate, args);

		// found a match!
		return op;
	}

	/**
	 * Checks that each parameter is type-compatible with its corresponding
	 * argument.
	 */
	private int typesMatch(final OpCandidate candidate, final Object[] args) {
		int i = 0;
		for (final Member<?> item : inputs(candidate)) {
			if (!canAssign(candidate, args[i], item)) return i;
			i++;
		}
		return -1;
	}

	/** Helper method of {@link #match(OpCandidate, Object[])}. */
	private String typeClashMessage(final OpCandidate candidate,
		final Object[] args, final int index)
	{
		int i = 0;
		for (final Member<?> item : inputs(candidate)) {
			if (i++ == index) {
				final Object arg = args[index];
				final String argType = arg == null ? "null" : arg.getClass().getName();
				final Type inputType = item.getType();
				return index + ": cannot coerce " + argType + " -> " + inputType;
			}
		}
		throw new IllegalArgumentException("Invalid index: " + index);
	}

	/** Helper method of {@link #match(OpCandidate, Object[])}. */
	private Object createOp(final OpCandidate candidate,
		final Object... args)
	{
		final Class<?> opClass = candidate.opInfo().opClass();
		final Object object;
		try {
			// TODO: Consider whether this is really the best way to
			// instantiate the op class here. No framework usage?
			// E.g., what about pluginService.createInstance?
			object = opClass.newInstance();
		}
		catch (final InstantiationException | IllegalAccessException e) {
			// TODO: Think about whether exception handling here should be different.
			log.error("Cannot instantiate op: " + opClass.getName(), e);
			return null;
		}
		final StructInstance<Object> op = candidate.struct().createInstance(object);
		candidate.setModule(op);

		// populate the inputs and return the op
		return assignInputs(op, args);
	}

	/** Helper method of {@link #match(OpCandidate, Object[])}. */
	private boolean canAssign(final OpCandidate candidate, final Object arg,
		final Member<?> item)
	{
		if (arg == null) {
			if (isRequired(item)) {
				candidate.setStatus(StatusCode.REQUIRED_ARG_IS_NULL, null, item);
				return false;
			}
			return true;
		}

		final Type type = item.getType();
		return Types.isAssignable(arg.getClass(), type);
		// TODO: Type coercion / conversion?
	}
}