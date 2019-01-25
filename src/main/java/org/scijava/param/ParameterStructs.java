
package org.scijava.param;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.scijava.ops.FieldOpDependencyMember;
import org.scijava.ops.OpDependency;
import org.scijava.ops.OpDependencyMember;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.util.ClassUtils;
import org.scijava.util.Types;

/**
 * Utility functions for working with {@link org.scijava.param} classes.
 * 
 * @author Curtis Rueden
 */
public final class ParameterStructs {

	public static <C> StructInstance<C> create(final C object)
		throws ValidityException
	{
		return structOf(object.getClass()).createInstance(object);
	}

	public static Struct structOf(final Class<?> type)
		throws ValidityException
	{
		final List<Member<?>> items = parse(type);
		return () -> items;
	}
	
	public static Struct structOf(final Class<?> c, final Field f)
			throws ValidityException
	{
		final List<Member<?>> items = parse(c, f);
		return () -> items;
	}

	// TODO parse methods need to be way more dry
	public static List<Member<?>> parse(final Class<?> type)
		throws ValidityException
	{
		if (type == null) return null;

		final ArrayList<Member<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();
		final Set<String> names = new HashSet<>();

		// NB: Reject abstract classes.
		checkModifiers(type.getName() + ": ", problems, type.getModifiers(), true, Modifier.ABSTRACT);

		// Parse class level (i.e., generic) @Parameter annotations.
		final Class<?> paramsClass = findParametersDeclaration(type);
		if (paramsClass != null) {
			parseFunctionalParameters(items, names, problems, paramsClass, type);
		}

		// Parse field level @OpDependency annotations.
		parseFieldOpDependencies(items, problems, type);

		// Fail if there were any problems.
		if (!problems.isEmpty()) throw new ValidityException(problems);

		return items;
	}
	
	public static List<Member<?>> parse(final Class<?> c, final Field field) throws ValidityException {
		if (c == null || field == null) return null;

		field.setAccessible(true);
		
		final ArrayList<Member<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();
		final Set<String> names = new HashSet<>();
		final Type fieldType = Types.fieldType(field, c);

		checkModifiers(field.toString() + ": ", problems, field.getModifiers(), false, Modifier.FINAL);
		parseFunctionalParameters(items, names, problems, field, fieldType);

		// Fail if there were any problems.
		if (!problems.isEmpty()) {
			throw new ValidityException(problems);
		}

		return items;
	}

	public static <T> Field field(final Member<T> item) {
		if (item instanceof FieldParameterMember) {
			final FieldParameterMember<T> fpItem = (FieldParameterMember<T>) item;
			return fpItem.getField();
		}
		return null;
	}

	// -- Helper methods --
	
	/**
	 * Helper to check for several modifiers at once.
	 * 
	 * @param message
	 * @param problems
	 * @param actualModifiers
	 * @param requiredModifiers
	 */
	private static void checkModifiers(String message, final ArrayList<ValidityProblem> problems,
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
	
	private static void parseFunctionalParameters(final ArrayList<Member<?>> items, final Set<String> names, final ArrayList<ValidityProblem> problems,
			AnnotatedElement annotationBearer, Type type) {		
		Parameter[] annotations = parameters(annotationBearer);
		
		final Class<?> functionalType = findFunctionalInterface(Types.raw(type), annotations.length);
		if (functionalType == null) {
			problems.add(new ValidityProblem("Could not find functional interface of " + type.getTypeName() + " with the required number of "
					+ "type parameters: " + annotations.length));
		} else {
			// TODO: Consider allowing partial override of class @Parameters.
			for (int i=0; i<annotations.length; i++) {
				String key = annotations[i].key();
				
				Type paramType = type;
				if (type instanceof Class) {
					paramType = Types.parameterizeRaw((Class<?>) type);
				}
				
				final Type itemType = Types.param(paramType, functionalType, i);
				final Class<?> rawItemType = Types.raw(itemType);
				final boolean valid = checkValidity(annotations[i], key, rawItemType, false,
						names, problems);
				if (!valid) continue; // NB: Skip invalid parameters.
				
				// add item to the list
				try {
					final ParameterMember<?> item = //
							new FunctionalParameterMember<>(itemType, annotations[i]);
					names.add(key);
					items.add(item);
				}
				catch (final ValidityException exc) {
					problems.addAll(exc.problems());
				}
			}
		}
	}

	private static void parseFieldOpDependencies(final List<Member<?>> items,
		final List<ValidityProblem> problems, Class<?> annotatedClass)
	{
		final List<Field> fields = ClassUtils.getAnnotatedFields(annotatedClass,
			OpDependency.class);
		for (final Field f : fields) {
			f.setAccessible(true);
			final boolean isFinal = Modifier.isFinal(f.getModifiers());
			if (isFinal) {
				final String name = f.getName();
				// Final fields are bad because they cannot be modified.
				final String error = "Invalid final Op dependency field: " + name;
				problems.add(new ValidityProblem(error));
				// Skip invalid Op dependencies.
				continue;
			}
			final OpDependencyMember<?> item = new FieldOpDependencyMember<>(f,
				annotatedClass);
			items.add(item);
		}
	}

	private static boolean isImmutable(final Class<?> type) {
		// NB: All eight primitive types, as well as the boxed primitive
		// wrapper classes, as well as strings, are immutable objects.
		return Types.isNumber(type) || Types.isText(type) || //
				Types.isBoolean(type);
	}

	/**
	 * Finds the class declaring {@code @Parameter} annotations. They might be on
	 * this type, on a supertype, or an implemented interface.
	 */
	private static Class<?> findParametersDeclaration(final Class<?> type) {
		if (type == null) return null;
		final Deque<Class<?>> types = new ArrayDeque<>();
		types.add(type);
		while (!types.isEmpty()) {
			final Class<?> candidate = types.pop();
			if (candidate.getAnnotation(Parameters.class) != null || 
					candidate.getAnnotation(Parameter.class) != null) return candidate;
			final Class<?> superType = candidate.getSuperclass() ;
			if (superType != null) types.add(superType);
			types.addAll(Arrays.asList(candidate.getInterfaces()));
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
	 * Searches for a {@code @FunctionalInterface} annotated interface in the 
	 * class hierarchy of the specified type with the specified number of
	 * type parameters. After the first interface is found, its super interfaces
	 * will be checked. If no such interface can be found, null will be returned.
	 * 
	 * @param type
	 * @param numberOfParams
	 * @return
	 */
	public static Class<?> findFunctionalInterface(Class<?> type, int numberOfParams) {
		Class<?> i = findFunctionalInterface(type);
		if (i == null) {
			return null;
		}
		if (i != null && i.getTypeParameters().length == numberOfParams) {
			return i;
		} else {
			for (Class<?> iface : i.getInterfaces()) {
				final Class<?> result = findFunctionalInterface(iface, numberOfParams);
				if (result != null) return result;
			}
		}
		return null;
	}

	public static boolean checkValidity(Parameter param, String name,
		Class<?> type, boolean isFinal, Set<String> names,
		ArrayList<ValidityProblem> problems)
	{
		boolean valid = true;

		final boolean isMessage = param.visibility() == ItemVisibility.MESSAGE;
		if (isFinal && !isMessage) {
			// NB: Final parameters are bad because they cannot be modified.
			final String error = "Invalid final parameter: " + name;
			problems.add(new ValidityProblem(error));
			valid = false;
		}

		if (names.contains(name)) {
			// NB: Shadowed parameters are bad because they are ambiguous.
			final String error = "Invalid duplicate parameter: " + name;
			problems.add(new ValidityProblem(error));
			valid = false;
		}

		if (param.itemIO() == ItemIO.BOTH && isImmutable(type)) {
			// NB: The BOTH type signifies that the parameter will be changed
			// in-place somehow. But immutable parameters cannot be changed in
			// such a manner, so it makes no sense to label them as BOTH.
			final String error = "Immutable BOTH parameter: " + name;
			problems.add(new ValidityProblem(error));
			valid = false;
		}

		return valid;
	}
	
	public static Parameter[] parameters(final AnnotatedElement element) {
		final Parameters params = element.getAnnotation(Parameters.class);
		if (params != null) {
			return params.value();
		}
		final Parameter p = element.getAnnotation(Parameter.class);
		return p == null ? new Parameter[0] : new Parameter[] { p };
	}
	
	/**
	 * Returns a list of {@link FunctionalMethodType}s describing the input and output
	 * types of the functional method of the specified functional type. In doing so,
	 * the return type of the method will me marked as {@link ItemIO#OUTPUT} and the
	 * all method parameters as {@link ItemIO#OUTPUT}, except for parameters annotated
	 * with {@link Mutable} which will be marked as {@link ItemIO#BOTH}. If the specified
	 * type does not have a functional method in its hierarchy, null will be
	 * returned.
	 * 
	 * @param functionalType
	 * @return
	 */
	public static List<FunctionalMethodType> findFunctionalMethodTypes(Type functionalType) {
		Method functionalMethod = findFunctionalMethod(Types.raw(functionalType));
		if (functionalMethod == null) return null;
		List<FunctionalMethodType> out = new ArrayList<>();
		int i = 0;
		for (Type t : Types.getExactParameterTypes(functionalMethod, functionalType)) {
			boolean isMutable = getMethodParameterAnnotation(functionalMethod, i, Mutable.class) != null;
			out.add(new FunctionalMethodType(t, isMutable ? ItemIO.BOTH : ItemIO.INPUT));
			i++;
		}
		
		Type returnType = Types.getExactReturnType(functionalMethod, functionalType);
		if (!returnType.equals(void.class)) {
			out.add(new FunctionalMethodType(returnType, ItemIO.OUTPUT));
		}
		
		return out;
	}
	
	/**
	 * Attempt to retrieve the specified annotation from the i'th parameter
	 * of the specified method. This method will only find annotations with:
	 * <pre>@Target(ElementType.TYPE_USE)</pre>
	 * If the ElementType is different or no annotation with specified type
	 * is present, null is returned. 
	 * 
	 * @param method
	 * @param i
	 * @param annotationClass
	 * @return
	 */
	public static <A extends Annotation> A getMethodParameterAnnotation(Method method, int i, Class<A> annotationClass) {
		AnnotatedType[] params = method.getAnnotatedParameterTypes();
		if (i >= params.length) {
			return null;
		}
		return params[i].getAnnotation(annotationClass);
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
