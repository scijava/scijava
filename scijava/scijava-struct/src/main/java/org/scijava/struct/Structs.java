
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
