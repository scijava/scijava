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

package org.scijava.ops.engine;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.scijava.ValidityProblem;
import org.scijava.ops.engine.OpCandidate.StatusCode;
import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.ValidityException;
import org.scijava.struct.ValueAccessible;
import org.scijava.types.Types;

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
		return Arrays.stream(names.split(",")).map(s -> s.trim())
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
		if (name == null || name.isEmpty()) {
			return new String[]{};
		}
		if (name.contains(".")) {
			String[] split = name.split("\\.");
			return new String[] { name, split[split.length - 1] };
		} else {
			return new String[]{name};
		}
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

	public static Type[] inputTypes(Struct struct) {
		return getTypes(inputs(struct));
	}

	public static Member<?> output(OpCandidate candidate) {
		return candidate.opInfo().output();
	}

	public static Type outputType(OpCandidate candidate) {
		return output(candidate).getType();
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

	public static void checkHasSingleOutput(Struct struct) throws ValidityException {
		final int numOutputs = OpUtils.outputs(struct).size();
		if (numOutputs != 1) {
			final String error = numOutputs == 0 //
				? "No output parameters specified. Must specify exactly one." //
				: "Multiple output parameters specified. Only a single output is allowed.";
			throw new ValidityException(Collections.singletonList(new ValidityProblem(error)));
		}
	}

	public static List<OpDependencyMember<?>> dependencies(Struct struct) {
		return struct.members().stream() //
			.filter(member -> member instanceof OpDependencyMember) //
			.map(member -> (OpDependencyMember<?>) member) //
			.collect(Collectors.toList());
	}

	public static Type[] types(OpCandidate candidate) {
		return getTypes(candidate.struct().members());
	}

	public static double getPriority(final OpCandidate candidate) {
		return candidate.priority();
	}

	public static Type[] padTypes(final OpCandidate candidate, Type[] types) {
		final Object[] padded = padArgs(candidate, false, (Object[]) types);
		return Arrays.copyOf(padded, padded.length, Type[].class);
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
			if (!item.isRequired())
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
			if (!item.isRequired() && optionalIndex++ >= optionalsToFill) {
				// skip this optional parameter (pad with null)
				paddedIndex++;
				continue;
			}
			paddedArgs[paddedIndex++] = args[argIndex++];
		}
		return paddedArgs;
	}

	public static List<Member<?>> injectableMembers(Struct struct) {
		return struct.members()
				.stream()
				.filter(m -> m instanceof ValueAccessible)
				.collect(Collectors.toList());
	}

	/**
	 * Checks if incomplete type matching could have occurred. If we have
	 * several matches that do not have equal output types, the output type may not
	 * completely match the request as only raw type assignability will be checked
	 * at the moment.
	 * @param matches
	 * @return
	 */
	public static boolean typeCheckingIncomplete(List<OpCandidate> matches) {
		Type outputType = null;
		for (OpCandidate match : matches) {
			Type ts = output(match).getType();
			if (outputType == null || Objects.equals(outputType, ts)) {
				outputType = ts;
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

	public static String opString(final OpInfo info) {
		return opString(info, null);
	}

	public static String opString(final OpInfo info, final Member<?> special) {
		final StringBuilder sb = new StringBuilder();
		sb.append(info.implementationName() + "(\n\t Inputs:\n");
		for (final Member<?> arg : info.inputs()) {
			appendParam(sb, arg, special);
		}
		sb.append("\t Outputs:\n");
		appendParam(sb, info.output(), special);
		sb.append(")\n");
		return sb.toString();
	}

	private static void appendParam(final StringBuilder sb, final Member<?> arg,
		final Member<?> special)
	{
		if (arg == special) sb.append("==> \t"); // highlight special item
		else sb.append("\t\t");
		sb.append(arg.getType().getTypeName());
		sb.append(" ");
		sb.append(arg.getKey());
		if (!arg.getDescription().isEmpty()) {
			sb.append(" -> ");
			sb.append(arg.getDescription());
		}
		sb.append("\n");
		return;
	}

	public static Class<?> findFirstImplementedFunctionalInterface(final OpRef opRef) {
		final Class<?> functionalInterface = OpUtils
			.findFunctionalInterface(Types.raw(opRef.getType()));
		if (functionalInterface != null) {
			return functionalInterface;
		}
		return null;
	}

	/**
	 * Searches for a {@code @FunctionalInterface} annotated interface in the 
	 * class hierarchy of the specified type. The first one that is found will
	 * be returned. If no such interface can be found, null will be returned.
	 * 
	 * @param type
	 * @return
	 */
	public static Class<?> findFunctionalInterface(Class<?> type) {
		if (type == null) return null;
		if (type.getAnnotation(FunctionalInterface.class) != null) return type;
		for (Class<?> iface : type.getInterfaces()) {
			final Class<?> result = findFunctionalInterface(iface);
			if (result != null) return result;
		}
		return findFunctionalInterface(type.getSuperclass());
	}

	/**
	 * Attempts to find the single functional method of the specified
	 * class, by scanning the for functional interfaces. If there
	 * is no functional interface, null will be returned.
	 * 
	 * @param cls
	 * @return
	 */
	public static Method findFunctionalMethod(Class<?> cls) {
		Class<?> iFace = findFunctionalInterface(cls);
		if (iFace == null) {
			return null;
		}
		
		List<Method> nonDefaults = Arrays.stream(iFace.getMethods())
				.filter(m -> !m.isDefault()).collect(Collectors.toList());
		
		// The single non default method must be the functional one
		if (nonDefaults.size() != 1) {
			for (Class<?> i : iFace.getInterfaces()) {
				final Method result = findFunctionalMethod(i);
				if (result != null) return result;
			}
		}
		
		return nonDefaults.get(0);
	}
}
