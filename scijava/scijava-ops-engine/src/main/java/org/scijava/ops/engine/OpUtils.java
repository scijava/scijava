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
import java.util.*;
import java.util.stream.Collectors;

import org.scijava.ops.api.OpCandidate;
import org.scijava.common3.validity.ValidityException;
import org.scijava.common3.validity.ValidityProblem;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRef;
import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
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

	public static void checkHasSingleOutput(Struct struct) throws
			ValidityException
	{
		final long numOutputs = struct.members().stream() //
			.filter(m -> m.isOutput()).count();
		if (numOutputs != 1) {
			final String error = numOutputs == 0 //
				? "No output parameters specified. Must specify exactly one." //
				: "Multiple output parameters specified. Only a single output is allowed.";
			throw new ValidityException(Collections.singletonList(new ValidityProblem(error)));
		}
	}

	public static Type[] types(OpCandidate candidate) {
		return getTypes(candidate.struct().members());
	}

	public static double getPriority(final OpCandidate candidate) {
		return candidate.priority();
	}

	public static Type[] getTypes(List<Member<?>> members) {
		return members.stream().map(m -> m.getType()).toArray(Type[]::new);
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

	/**
	 * Returns the index of the argument that is both the input and the output. <b>If there is no such argument (i.e. the Op produces a pure output), -1 is returned</b>
	 *
	 * @return the index of the mutable argument.
	 */
	public static int ioArgIndex(final OpInfo info) {
		List<Member<?>> inputs = info.inputs();
		Optional<Member<?>>
				ioArg = inputs.stream().filter(m -> m.isInput() && m.isOutput()).findFirst();
		if(ioArg.isEmpty()) return -1;
		Member<?> ioMember = ioArg.get();
		return inputs.indexOf(ioMember);
	}

	public static boolean hasPureOutput(final OpInfo info) {
		return ioArgIndex(info) == -1;
	}
}
