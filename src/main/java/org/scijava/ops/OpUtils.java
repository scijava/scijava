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
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.ops.matcher.MatchingResult;
import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpInfo;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.matcher.OpCandidate.StatusCode;
import org.scijava.param.ParameterMember;
import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

/**
 * Utility methods for working with ops. In particular, this class contains
 * handy methods for generating human-readable strings describing ops and match
 * requests against them.
 * 
 * @author Curtis Rueden
 */
public final class OpUtils {

	private OpUtils() {
		// NB: prevent instantiation of utility class.
	}

	// -- Utility methods --

	public static Object[] args(final Object[] latter, final Object... former) {
		final Object[] result = new Object[former.length + latter.length];
		int i = 0;
		for (final Object o : former) {
			result[i++] = o;
		}
		for (final Object o : latter) {
			result[i++] = o;
		}
		return result;
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
		return inputs(candidate.struct()).stream().map(m -> m.getType()).toArray(Type[]::new);
	}

	public static List<Member<?>> outputs(OpCandidate candidate) {
		return outputs(candidate.struct());
	}
	
	public static Type[] outputTypes(OpCandidate candidate) {
		return outputs(candidate.struct()).stream().map(m -> m.getType()).toArray(Type[]::new);
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

	public static double getPriority(final OpCandidate candidate) {
		// TODO: Think about what to do about non @Plugin-based ops...?
		// What if there is no annotation? How to discern a priority?
		return candidate.opInfo().getAnnotation().priority();
	}
	
	public static Type[] padArgs(final OpCandidate candidate) {
		int inputCount = 0, requiredCount = 0;
		for (final Member<?> item : OpUtils.inputs(candidate.struct())) {
			inputCount++;
			if (isRequired(item))
				requiredCount++;
		}
		final Type[] args = candidate.getRef().getArgs();
		if (args.length == inputCount) {
			// correct number of arguments
			return args;
		}
		if (args.length > inputCount) {
			// too many arguments
			candidate.setStatus(StatusCode.TOO_MANY_ARGS, args.length + " > " + inputCount);
			return null;
		}
		if (args.length < requiredCount) {
			// too few arguments
			candidate.setStatus(StatusCode.TOO_FEW_ARGS, args.length + " < " + requiredCount);
			return null;
		}

		// pad optional parameters with null (from right to left)
		final int argsToPad = inputCount - args.length;
		final int optionalCount = inputCount - requiredCount;
		final int optionalsToFill = optionalCount - argsToPad;
		final Type[] paddedArgs = new Type[inputCount];
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
	
	public static boolean isRequired(final Member<?> item) {
		return item instanceof ParameterMember && //
				((ParameterMember<?>) item).isRequired();
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

		final OpRef ref = candidates.get(0).getRef();
		if (matches.isEmpty()) {
			// no matches
			sb.append("No matching '" + ref.getLabel() + "' op\n");
		} else {
			// multiple matches
			final double priority = getPriority(matches.get(0));
			sb.append("Multiple '" + ref.getLabel() + "' ops of priority " + priority + ":\n");
			int count = 0;
			for (final OpCandidate match : matches) {
				sb.append(++count + ". ");
				sb.append(match.toString() + "\n");
			}
		}

		// fail, with information about the request and candidates
		sb.append("\n");
		sb.append("Request:\n");
		sb.append("-\t" + opString(ref.getLabel(), ref.getArgs()) + "\n");
		sb.append("\n");
		sb.append("Candidates:\n");
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
	 * Gets a string describing the given op request.
	 * 
	 * @param name
	 *            The op's name.
	 * @param args
	 *            The op's input arguments.
	 * @return A string describing the op request.
	 */
	public static String opString(final String name, final Type... args) {
		// TODO: add description of outputs
		final StringBuilder sb = new StringBuilder();
		sb.append(name + "(\n\t\t");
		boolean first = true;
		for (final Type arg : args) {
			if (first)
				first = false;
			else
				sb.append(",\n\t\t");
			if (arg == null)
				sb.append("null");
			else {
				// NB: Class instance used to mark argument type.
				sb.append(arg.getTypeName());
			} 
		}
		sb.append(")");
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
		sb.append(info.opClass().getName());
		sb.append("(" + paramString(inputs(info.struct()), special) + ")");
		return sb.toString();
	}

	/**
	 * Helper method of {@link #opString(OpInfo, Member)} which parses a
	 * set of items with a default delimiter of ","
	 */
	private static String paramString(final Iterable<Member<?>> items, final Member<?> special) {
		return paramString(items, special, ",");
	}

	/**
	 * As {@link #paramString(Iterable, Member, String)} with an optional delimiter.
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
}
