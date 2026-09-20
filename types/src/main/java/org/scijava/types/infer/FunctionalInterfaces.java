/*-
 * #%L
 * SciJava library for generic type reasoning.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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

package org.scijava.types.infer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.scijava.common3.Types;

public final class FunctionalInterfaces {

	private FunctionalInterfaces() {
		// Prevent instantiation of static utility class
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
	public static Class<?> findFrom(Type type) {
		return findFrom(Types.raw(type));
	}

	private static Class<?> findFrom(Class<?> type) {
		if (type == null) return null;
		if (type.getAnnotation(FunctionalInterface.class) != null) return type;
		for (var iface : type.getInterfaces()) {
			final var result = findFrom(iface);
			if (result != null) return result;
		}
		return findFrom(type.getSuperclass());
	}

	public static Method functionalMethodOf(Type type) {
        var functionalInterface = findFrom(type);
		if (functionalInterface != null) {
			return functionalMethodOf(functionalInterface);
		}
		throw new IllegalArgumentException( //
			type + " does not implement a FunctionalInterface!" //
		);
	}

	private static Method functionalMethodOf(Class<?> functionalInterface) {
        var typeMethods = Arrays.stream(functionalInterface.getMethods())
			.filter(method -> Modifier.isAbstract(method.getModifiers())).toArray(
				Method[]::new);
		if (typeMethods.length != 1) {
			throw new IllegalArgumentException(functionalInterface +
				" should be a FunctionalInterface, however it has " +
				typeMethods.length + " abstract declared methods");
		}

		return typeMethods[0];
	}

}
