
package org.scijava.struct;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.scijava.ValidityProblem;
import org.scijava.types.Types;

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

	public static <S> Struct from(S source, Type structType, List<ValidityProblem> problems,
		@SuppressWarnings("unchecked") MemberParser<S, ? extends Member<?>>... parsers)
	{
		List<Member<?>> members = new ArrayList<>();
		for (MemberParser<S, ? extends Member<?>> p : parsers) {
			try {
				members.addAll(p.parse(source, structType));
			}
			catch (ValidityException e) {
				problems.addAll(e.problems());
			}
		}
		return () -> members;
	}

	/**
	 * Helper to check for several modifiers at once.
	 * 
	 * @param message
	 * @param problems
	 * @param actualModifiers
	 * @param requiredModifiers
	 */
	public static void checkModifiers(String message, final ArrayList<ValidityProblem> problems,
			final int actualModifiers, final boolean negate, final int... requiredModifiers) {
		for (int mod : requiredModifiers) {
			if (negate) {
				if ((actualModifiers & mod) != 0) {
					problems.add(
							new ValidityProblem(message + "Illegal modifier. Must not be " + Modifier.toString(mod)));
				}
			} else {
				if ((actualModifiers & mod) == 0) {
					problems.add(new ValidityProblem(message + "Illegal modifier. Must be " + Modifier.toString(mod)));
				}
			}
		}
	}

	public static boolean checkValidity(Member<?> m, Supplier<String> name, Class<?> type,
		boolean isFinal, ArrayList<ValidityProblem> problems)
	{
		boolean valid = true;

		if ((m.getIOType() == ItemIO.MUTABLE || m
			.getIOType() == ItemIO.CONTAINER) && Structs.isImmutable(type))
		{
			// NB: The MUTABLE and CONTAINER types signify that the parameter
			// will be written to, but immutable parameters cannot be changed in
			// such a manner, so it makes no sense to label them as such.
			final String error = "Immutable " + m.getIOType() + " parameter: " +
				name.get() + " (" + type.getName() + " is immutable)";
			problems.add(new ValidityProblem(error));
			valid = false;
		}
	
		return valid;
	}

	public static boolean isImmutable(final Class<?> type) {
		// NB: All eight primitive types, as well as the boxed primitive
		// wrapper classes, as well as strings, are immutable objects.
		return Types.isNumber(type) || Types.isText(type) || //
			Types.isBoolean(type);
	}

}
