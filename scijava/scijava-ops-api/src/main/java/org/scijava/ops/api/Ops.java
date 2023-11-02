/*-
 * #%L
 * SciJava Operations API: Outward-facing Interfaces used by the SciJava Operations framework.
 * %%
 * Copyright (C) 2021 - 2023 SciJava developers.
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

package org.scijava.ops.api;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generic Ops utilities
 *
 * @author Gabriel Selzer
 */
public final class Ops {

	private Ops() {}

	/**
	 * Convenience function for determining whether {@code op} is a
	 * {@link RichOp}.
	 * 
	 * @param op the Op
	 * @return true iff {@code op} is a {@link RichOp}
	 */
	public static boolean isRich(Object op) {
		return op instanceof RichOp;
	}

	/**
	 * Convenience function for getting the {@link RichOp} of {@code op}
	 * 
	 * @param op the Op
	 * @return the {@link RichOp} wrapping {@code op}
	 * @param <T> the type of {@code op}
	 * @throws IllegalArgumentException when a {@link RichOp} cannot be obtained
	 *           for {@code op}
	 */
	@SuppressWarnings("unchecked")
	public static <T> RichOp<T> rich(T op) {
		if (!isRich(op)) {
			throw new IllegalArgumentException(op + " is not a RichOp!");
		}
		return (RichOp<T>) op;
	}
	/**
	 * Convenience function for getting the {@link OpInfo} of {@code op}
	 * 
	 * @param op the Op
	 * @return the {@link OpInfo} that generated {@code op}
	 * @param <T> the type of {@code op}
	 * @throws IllegalArgumentException if {@code op} is not an Op
	 */
	public static <T> OpInfo info(T op) {
		return rich(op).instance().infoTree().info();
	}

	/**
	 * Convenience function for accessing {@link RichOp#recordExecutions(boolean)}
	 *
	 * @param op the Op
	 * @param record true iff {@code op} should record its executions
	 * @param <T> the type of the Op
	 * @throws IllegalArgumentException if {@code op} is not an Op
	 */
	public static <T> void recordExecutions(T op, boolean record) {
		rich(op).recordExecutions(record);
	}

	/**
	 * Convenience function for accessing {@link RichOp#isRecordingExecutions()}
	 *
	 * @param op the Op
	 * @param <T> the type of the Op
	 * @return true iff Op is recording its executions
	 */
	public static <T> boolean isRecordingExecutions(T op) {
		return isRich(op) && rich(op).isRecordingExecutions();
	}


	/**
	 * Searches for a {@code @FunctionalInterface} annotated interface in the
	 * class hierarchy of the specified type. The first one that is found will be
	 * returned. If no such interface can be found, null will be returned.
	 * 
	 * @param type some {@link Class}, possibly implementing a
	 *          {@link FunctionalInterface}
	 * @return the {@link FunctionalInterface} implemented by {@code type}, or
	 *         {@code null} if {@code type} does not implement a
	 *         {@link FunctionalInterface}.
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
	 * Attempts to find the single functional method of the specified class, by
	 * scanning the for functional interfaces. If there is no functional
	 * interface, null will be returned.
	 * 
	 * @param cls the {@link Class} implmenting some {@link FunctionalInterface}
	 * @return the functional {@link Method} of {@code cls}
	 * @throws IllegalArgumentException if {@code cls} does not implement a
	 *           {@link FunctionalInterface}
	 */
	public static Method findFunctionalMethod(Class<?> cls) {
		Method m = fMethod(cls);
		if (m == null) {
			throw new IllegalArgumentException("Op type" + cls.getName() +
				" does not have a functional method!");
		}
		return m;
	}

	private static Method fMethod(Class<?> cls) {
		Class<?> iFace = findFunctionalInterface(cls);
		if (iFace == null) {
			return null;
		}

		List<Method> nonDefaults = Arrays.stream(iFace.getMethods()) //
				.filter(m -> !m.isDefault()) //
				.collect(Collectors.toList());

		// The single non default method must be the functional one
		if (nonDefaults.size() != 1) {
			for (Class<?> i : iFace.getInterfaces()) {
				try {
					return findFunctionalMethod(i);
				} catch (IllegalArgumentException e) {
					// Try another interface
				}
			}
		}

		return nonDefaults.get(0);
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
		return Arrays.stream(names.split(",")).map(s -> s.trim())
				.toArray(String[]::new);
	}
}
