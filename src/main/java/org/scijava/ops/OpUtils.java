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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.ops.matcher.DefaultOpTypeMatchingService;
import org.scijava.ops.matcher.MatchingResult;
import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpCandidate.StatusCode;
import org.scijava.ops.matcher.OpInfo;
import org.scijava.ops.matcher.OpRef;
import org.scijava.param.ParameterMember;
import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.ValueAccessible;

/**
 * Utility methods for working with ops.
 * 
 * @author Curtis Rueden
 * @author David Kolb
 */
public final class OpUtils {

	private OpUtils() {
		// NB: prevent instantiation of utility class.
	}

	// -- Utility methods --

	/**
	 * The canonical op name is defined as the first name in the list of op
	 * names used for each op. This method will call
	 * {@link #parseOpNames(String)} and return the first one.
	 * 
	 * @param names
	 * @return
	 */
	public static String getCanonicalOpName(String names) {
		return parseOpNames(names)[0];
	}

	/**
	 * Parses op names contained in specified String according to the following
	 * format:
	 * 
	 * <pre>
	 *  'prefix1'.'prefix2' , 'prefix1'.'prefix3'
	 * </pre>
	 * 
	 * E.g. "math.add, math.pow". </br>
	 * The name delimiter is a comma (,). Furthermore, names without prefixes
	 * are added. The above example will result in the following output:
	 * 
	 * <pre>
	 *  [math.add, add, math.pow, pow]
	 * </pre>
	 * 
	 * @param names
	 *            the string containing the names to parse
	 * @return
	 */
	public static String[] parseOpNames(String names) {
		return Arrays.stream(names.split(",")).map(s -> s.trim()).flatMap(s -> Arrays.stream(parseOpName(s)))
				.toArray(String[]::new);
	}

	/**
	 * Returns an array containing the specified name and the name without
	 * prefixes. Prefixes are assumed to be separated by a comma (,). E.g.
	 * "math.add" will result in [math.add, add].
	 * 
	 * @param name
	 *            the string containing the name to parse
	 * @return
	 */
	public static String[] parseOpName(String name) {
		String[] split = name.split("\\.");
		return new String[] { name, split[split.length - 1] };
	}

	public static List<MemberInstance<?>> inputs(StructInstance<?> op) {
		return op.members().stream() //
				.filter(memberInstance -> memberInstance.member().isInput()) //
				.collect(Collectors.toList());
	}

	public static List<Member<?>> inputs(OpCandidate candidate) {
		return inputs(candidate.struct());
	}

	public static List<Member<?>> inputs(final Struct struct) {
		return struct.members().stream() //
				.filter(member -> member.isInput()) //
				.collect(Collectors.toList());
	}
	
	public static Type[] inputTypes(OpCandidate candidate) {
		return getTypes(inputs(candidate.struct()));
	}

	public static List<Member<?>> outputs(OpCandidate candidate) {
		return outputs(candidate.struct());
	}

	public static Type[] outputTypes(OpCandidate candidate) {
		return getTypes(outputs(candidate.struct()));
	}

	public static List<Member<?>> outputs(final Struct struct) {
		return struct.members().stream() //
				.filter(member -> member.isOutput()) //
				.collect(Collectors.toList());
	}

	public static List<MemberInstance<?>> outputs(StructInstance<?> op) {
		return op.members().stream() //
				.filter(memberInstance -> memberInstance.member().isOutput()) //
				.collect(Collectors.toList());
	}
	
	public static Type[] types(OpCandidate candidate) {
		return getTypes(candidate.struct().members());
	}

	public static double getPriority(final OpCandidate candidate) {
		return candidate.opInfo().priority();
	}

	public static Type[] padTypes(final OpCandidate candidate, Type[] types) {
		return (Type[]) padArgs(candidate, false, (Object[])types);
	}
	
	public static Object[] padArgs(final OpCandidate candidate, final boolean secondary, Object... args) {
		List<Member<?>> members;
		String argName;
		if (secondary) {
			members = OpUtils.injectableMembers(candidate.struct());
			argName = "secondary args";
		} else {
			members = OpUtils.inputs(candidate.struct());
			argName = "args";
		}
		
		int inputCount = 0, requiredCount = 0;
		for (final Member<?> item : members) {
			inputCount++;
			if (isRequired(item))
				requiredCount++;
		}
		if (args.length == inputCount) {
			// correct number of arguments
			return args;
		}
		if (args.length > inputCount) {
			// too many arguments
			candidate.setStatus(StatusCode.TOO_MANY_ARGS,
					"\nNumber of " + argName + " given: " + args.length + "  >  " + 
					"Number of " + argName + " of op: " + inputCount);
			return null;
		}
		if (args.length < requiredCount) {
			// too few arguments
			candidate.setStatus(StatusCode.TOO_FEW_ARGS,
					"\nNumber of " + argName + " given: " + args.length + "  <  " + 
					"Number of required " + argName + " of op: " + requiredCount);
			return null;
		}

		// pad optional parameters with null (from right to left)
		final int argsToPad = inputCount - args.length;
		final int optionalCount = inputCount - requiredCount;
		final int optionalsToFill = optionalCount - argsToPad;
		final Object[] paddedArgs = new Object[inputCount];
		int argIndex = 0, paddedIndex = 0, optionalIndex = 0;
		for (final Member<?> item : members) {
			if (!isRequired(item) && optionalIndex++ >= optionalsToFill) {
				// skip this optional parameter (pad with null)
				paddedIndex++;
				continue;
			}
			paddedArgs[paddedIndex++] = args[argIndex++];
		}
		return paddedArgs;
	}
	
	public static boolean isRequired(final Member<?> item) {
		return item instanceof ParameterMember && //
				((ParameterMember<?>) item).isRequired();
	}
	
	public static List<Member<?>> injectableMembers(Struct struct) {
		return struct.members()
				.stream()
				.filter(m -> m instanceof ValueAccessible)
				.collect(Collectors.toList());
	}

	/**
	 * Checks if incomplete type matching could have occurred. If we have
	 * several matches that do not have equal output types, output types may not
	 * completely match the request as only raw type assignability will be checked
	 * at the moment.
	 * @see DefaultOpTypeMatchingService#typesMatch(OpCandidate)
	 * @param matches
	 * @return
	 */
	private static boolean typeCheckingIncomplete(List<OpCandidate> matches) {
		Type[] outputTypes = null;
		for (OpCandidate match : matches) {
			Type[] ts = getTypes(outputs(match));
			if (outputTypes == null || Arrays.deepEquals(outputTypes, ts)) {
				outputTypes = ts;
				continue;
			} else {
				return true;
			}
		}
		return false;
	}

	public static Type[] getTypes(List<Member<?>> members) {
		return members.stream().map(m -> m.getType()).toArray(Type[]::new);
	}

	/**
	 * Gets a string with an analysis of a particular match request failure.
	 * <p>
	 * This method is used to generate informative exception messages when no
	 * matches, or too many matches, are found.
	 * </p>
	 * 
	 * @param res
	 *            The result of type matching
	 * @return A multi-line string describing the situation: 1) the type of
	 *         match failure; 2) the list of matching ops (if any); 3) the
	 *         request itself; and 4) the list of candidates including status
	 *         (i.e., whether it matched, and if not, why not).
	 */
	public static String matchInfo(final MatchingResult res) {
		final StringBuilder sb = new StringBuilder();

		List<OpCandidate> candidates = res.getCandidates();
		List<OpCandidate> matches = res.getMatches();

		final OpRef ref = res.getOriginalQueries().get(0);
		if (matches.isEmpty()) {
			// no matches
			sb.append("No matching '" + ref.getLabel() + "' op\n");
		} else {
			// multiple matches
			final double priority = getPriority(matches.get(0));
			sb.append("Multiple '" + ref.getLabel() + "' ops of priority " + priority + ":\n");
			if (typeCheckingIncomplete(matches)) {
				sb.append("Incomplete output type checking may have occured!\n");
			}
			int count = 0;
			for (final OpCandidate match : matches) {
				sb.append(++count + ". ");
				sb.append(match.toString() + "\n");
			}
		}

		// fail, with information about the request and candidates
		sb.append("\n");
		sb.append("Request:\n");
		sb.append("-\t" + ref.toString() + "\n");
		sb.append("\n");
		sb.append("Candidates:\n");
		if (candidates.isEmpty()) {
			sb.append("-\t No candidates found!");
		}
		int count = 0;
		for (final OpCandidate candidate : candidates) {
			sb.append(++count + ". ");
			sb.append("\t" + opString(candidate.opInfo(), candidate.getStatusItem()) + "\n");
			final String status = candidate.getStatus();
			if (status != null)
				sb.append("\t" + status + "\n");
			if (candidate.getStatusCode() == StatusCode.DOES_NOT_CONFORM) {
				// TODO: Conformity not yet implemented
				// // show argument values when a contingent op rejects them
				// for (final ModuleItem<?> item : inputs(info)) {
				// final Object value = item.getValue(candidate.getModule());
				// sb.append("\t\t" + item.getName() + " = " + value + "\n");
				// }
			}
		}
		return sb.toString();
	}

	/**
	 * Gets a string describing the given op, highlighting the specific
	 * parameter.
	 * 
	 * @param info
	 *            The {@link OpInfo} metadata which describes the op.
	 * @param special
	 *            A parameter of particular interest when describing the op.
	 * @return A string describing the op.
	 */
	public static String opString(final OpInfo info, final Member<?> special) {
		final StringBuilder sb = new StringBuilder();
		final String outputString = paramString(outputs(info.struct()), null).trim();
		if (!outputString.isEmpty())
			sb.append("(" + outputString + ") =\n\t");
		sb.append(info.implementationName());
		sb.append("(" + paramString(inputs(info.struct()), special) + ")");
		return sb.toString();
	}

	/**
	 * Helper method of {@link #opString(OpInfo, Member)} which parses a set of
	 * items with a default delimiter of ","
	 */
	private static String paramString(final Iterable<Member<?>> items, final Member<?> special) {
		return paramString(items, special, ",");
	}

	/**
	 * As {@link #paramString(Iterable, Member, String)} with an optional
	 * delimiter.
	 */
	private static String paramString(final Iterable<Member<?>> items, final Member<?> special, final String delim) {
		return paramString(items, special, delim, false);
	}

	/**
	 * As {@link #paramString(Iterable, Member, String)} with a toggle to
	 * control if inputs are types only or include the names.
	 */
	private static String paramString(final Iterable<Member<?>> items, final Member<?> special, final String delim,
			final boolean typeOnly) {
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (final Member<?> item : items) {
			if (first)
				first = false;
			else
				sb.append(delim);
			sb.append("\n");
			if (item == special)
				sb.append("==>"); // highlight special item
			sb.append("\t\t");
			sb.append(item.getType().getTypeName());

			if (!typeOnly) {
				sb.append(" " + item.getKey());
				if (!((ParameterMember<?>) item).isRequired())
					sb.append("?");
			}
		}
		return sb.toString();
	}

	public static String opString(final OpInfo info) {
		final StringBuilder sb = new StringBuilder();
		sb.append(info.implementationName() + "(\n\t Inputs:\n");
		for (final Member<?> arg : info.inputs()) {
			sb.append("\t\t");
			sb.append(arg.getType().getTypeName());
			sb.append(" ");
			sb.append(arg.getKey());
			sb.append("\n");
		}
		sb.append("\t Outputs:\n");
		for (final Member<?> arg : info.outputs()) {
			sb.append("\t\t");
			sb.append(arg.getType().getTypeName());
			sb.append(" ");
			sb.append(arg.getKey());
			sb.append("\n");
		}
		sb.append(")\n");
		return sb.toString();
	}
}
