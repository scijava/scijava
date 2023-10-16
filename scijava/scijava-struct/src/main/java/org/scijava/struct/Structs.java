/*-
 * #%L
 * A lightweight framework for collecting Members
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

package org.scijava.struct;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.scijava.common3.Classes;

public final class Structs {

	private Structs() {
		// NB: Prevent instantiation of utility class.
	}

	public static StructInstance<?> expand(final StructInstance<?> parent,
		final String key)
	{
		return expand(parent.member(key));
	}

	public static <T> StructInstance<T> expand(
		final MemberInstance<T> memberInstance)
	{
		if (!memberInstance.member().isStruct()) return null;
		return memberInstance.member().childStruct().createInstance(//
			memberInstance.get());
	}

	public static <S> Struct from(S source, Type structType,
		@SuppressWarnings("unchecked") MemberParser<S, ? extends Member<?>>... parsers)
	{
		List<Member<?>> members = new ArrayList<>();
		for (MemberParser<S, ? extends Member<?>> p : parsers) {
			members.addAll(p.parse(source, structType));
		}
		return () -> members;
	}

	/**
	 * Helper to check for several modifiers at once.
	 * 
	 * @param message
	 * @param actualModifiers
	 * @param requiredModifiers
	 */
	public static void checkModifiers(String message,
			final int actualModifiers, final boolean negate, final int... requiredModifiers) {
		for (int mod : requiredModifiers) {
			if (negate) {
				if ((actualModifiers & mod) != 0) {
					throw new IllegalArgumentException(message + "Illegal modifier. Must not be " + Modifier.toString(mod));
				}
			} else {
				throw new IllegalArgumentException(message + "Illegal modifier. Must be " + Modifier.toString(mod));
			}
		}
	}

	public static boolean isImmutable(final Class<?> type) {
		// NB: All eight primitive types, as well as the boxed primitive
		// wrapper classes, as well as strings, are immutable objects.
		return Classes.isNumber(type) || Classes.isText(type) || //
			Classes.isBoolean(type);
	}

}
