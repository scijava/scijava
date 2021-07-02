
package org.scijava.ops.struct;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.scijava.ValidityProblem;
import org.scijava.function.Container;
import org.scijava.function.Mutable;
import org.scijava.ops.OpUtils;
import org.scijava.ops.ValidityException;
import org.scijava.ops.util.AnnotationUtils;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.types.Types;
import org.scijava.types.inference.InterfaceInference;

public class Structs {

	@SafeVarargs
	public static <S> Struct from(S source, List<ValidityProblem> problems,
		MemberParser<S, ? extends Member<?>>... parsers)
	{
		List<Member<?>> members = new ArrayList<>();
		for (MemberParser<S, ? extends Member<?>> p : parsers) {
			try {
				members.addAll(p.parse(source));
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

	public static void parseFunctionalParameters(
		final ArrayList<SynthesizedParameterMember<?>> items, final Set<String> names,
		final ArrayList<ValidityProblem> problems, Type type, ParameterData data)
	{
		// Search for the functional method of 'type' and map its signature to
		// ItemIO
		List<FunctionalMethodType> fmts;
		try {
			fmts = findFunctionalMethodTypes(type);
		}
		catch (IllegalArgumentException e) {
			problems.add(new ValidityProblem("Could not find functional method of " +
				type.getTypeName()));
			return;
		}

		// Synthesize members
		List<SynthesizedParameterMember<?>> fmtMembers = data.synthesizeMembers(fmts);

		for (SynthesizedParameterMember<?> m : fmtMembers) {
			String key = m.getKey();
			final Type itemType = m.getType();

			final boolean valid = checkValidity(m, key, Types.raw(itemType), false,
				names, problems);
			if (!valid) continue;
			items.add(m);
			names.add(m.getKey());
		}
	}

	/**
	 * Returns a list of {@link FunctionalMethodType}s describing the input and
	 * output types of the functional method of the specified functional type. In
	 * doing so, the return type of the method will me marked as
	 * {@link ItemIO#OUTPUT} and the all method parameters as
	 * {@link ItemIO#OUTPUT}, except for parameters annotated with
	 * {@link Container} or {@link Mutable} which will be marked as
	 * {@link ItemIO#CONTAINER} or {@link ItemIO#MUTABLE} respectively. If the
	 * specified type does not have a functional method in its hierarchy,
	 * {@code null} will be returned.<br>
	 * The order will be the following: method parameters from left to right, then
	 * return type.
	 * 
	 * @param functionalType
	 * @return
	 */
	public static List<FunctionalMethodType> findFunctionalMethodTypes(
		Type functionalType)
	{
		Method functionalMethod = findFunctionalMethod(Types.raw(functionalType));
		if (functionalMethod == null) throw new IllegalArgumentException("Type " +
			functionalType +
			" is not a functional type, thus its functional method types cannot be determined");

		Type paramfunctionalType = functionalType;
		if (functionalType instanceof Class) {
			paramfunctionalType = Types.parameterizeRaw((Class<?>) functionalType);
		}

		List<FunctionalMethodType> out = new ArrayList<>();
		int i = 0;
		for (Type t : Types.getExactParameterTypes(functionalMethod,
			paramfunctionalType))
		{
			final ItemIO ioType;
			if (AnnotationUtils.getMethodParameterAnnotation(functionalMethod, i,
				Container.class) != null) ioType = ItemIO.CONTAINER;
			else if (AnnotationUtils.getMethodParameterAnnotation(functionalMethod, i,
				Mutable.class) != null) ioType = ItemIO.MUTABLE;
			else ioType = ItemIO.INPUT;
			out.add(new FunctionalMethodType(t, ioType));
			i++;
		}

		Type returnType = Types.getExactReturnType(functionalMethod,
			paramfunctionalType);
		if (!returnType.equals(void.class)) {
			out.add(new FunctionalMethodType(returnType, ItemIO.OUTPUT));
		}

		return out;
	}

	/**
	 * Attempts to find the single functional method of the specified class, by
	 * scanning the for functional interfaces. If there is no functional
	 * interface, null will be returned.
	 * 
	 * @param cls
	 * @return
	 */
	private static Method findFunctionalMethod(Class<?> cls) {
		Class<?> iFace = OpUtils.findFunctionalInterface(cls);
		if (iFace == null) {
			return null;
		}

		List<Method> nonDefaults = Arrays.stream(iFace.getMethods()).filter(m -> !m
			.isDefault()).collect(Collectors.toList());

		// The single non default method must be the functional one
		if (nonDefaults.size() != 1) {
			for (Class<?> i : iFace.getInterfaces()) {
				final Method result = findFunctionalMethod(i);
				if (result != null) return result;
			}
		}

		return nonDefaults.get(0);
	}

	private static boolean checkValidity(Member<?> m, String name, Class<?> type,
		boolean isFinal, Set<String> names, ArrayList<ValidityProblem> problems)
	{
		boolean valid = true;

		if (names.contains(name)) {
			// NB: Shadowed parameters are bad because they are ambiguous.
			final String error = "Invalid duplicate parameter: " + name;
			problems.add(new ValidityProblem(error));
			valid = false;
		}

		if ((m.getIOType() == ItemIO.MUTABLE || m
			.getIOType() == ItemIO.CONTAINER) && isImmutable(type))
		{
			// NB: The MUTABLE and CONTAINER types signify that the parameter
			// will be written to, but immutable parameters cannot be changed in
			// such a manner, so it makes no sense to label them as such.
			final String error = "Immutable " + m.getIOType() + " parameter: " +
				name + " (" + type.getName() + " is immutable)";
			problems.add(new ValidityProblem(error));
			valid = false;
		}

		return valid;
	}

	private static boolean isImmutable(final Class<?> type) {
		// NB: All eight primitive types, as well as the boxed primitive
		// wrapper classes, as well as strings, are immutable objects.
		return Types.isNumber(type) || Types.isText(type) || //
			Types.isBoolean(type);
	}

	/**
	 * Convenience method to call
	 * {@link Structs#from(Object, List, MemberParser...)} dot
	 * {@link Struct#createInstance(Object)}
	 * 
	 * @param object the {@link Object} from which the {@link StructInstance} is
	 *          created.
	 * @return an instance of the {@link Struct} created from {@code object}
	 * @throws ValidityException
	 */
	public static <C> StructInstance<C> create(final C object)
		throws ValidityException
	{
		Struct s = from(object.getClass(), new ArrayList<>(),
			new ClassParameterMemberParser(), new ClassOpDependencyMemberParser());
		return s.createInstance(object);
	}

	// TODO: extract this method to a more general utility class
	public static Method findFMethod(Class<?> c) {
			Class<?> fIface = OpUtils.findFunctionalInterface(c);
			if(fIface == null) throw new IllegalArgumentException("Class " + c +" does not implement a functional interface!");
			return InterfaceInference.singularAbstractMethod(fIface);
	}

}
