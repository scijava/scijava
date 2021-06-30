
package org.scijava.param;

import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.function.Container;
import org.scijava.function.Mutable;
import org.scijava.ops.FieldOpDependencyMember;
import org.scijava.ops.MethodParameterOpDependencyMember;
import org.scijava.ops.OpDependency;
import org.scijava.ops.OpDependencyMember;
import org.scijava.ops.OpInfo;
import org.scijava.ops.OpMethod;
import org.scijava.ops.simplify.Simplifier;
import org.scijava.ops.util.AnnotationUtils;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.types.Types;
import org.scijava.types.inference.GenericAssignability;
import org.scijava.util.ClassUtils;

/**
 * Utility functions for working with {@link org.scijava.param} classes.
 * 
 * @author Curtis Rueden
 * @author David Kolb
 */
public final class ParameterStructs {

	/**
	 * Convenience method to call {@link #structOf(Class)} dot {@link Struct#createInstance(Object)}
	 * 
	 * @param object
	 * @return
	 * @throws ValidityException
	 */
	public static <C> StructInstance<C> create(final C object)
		throws ValidityException
	{
		return structOf(object.getClass()).createInstance(object);
	}

	/**
	 * Convenience method to () -> parse(type)
	 * 
	 * @param type
	 * @return
	 * @throws ValidityException
	 */
	public static Struct structOf(final Class<?> type)
		throws ValidityException
	{
		final List<Member<?>> items = parse(type);
		return () -> items;
	}
	
	/**
	 * Convenience method to () -> parse(field)
	 * 
	 * @param field
	 * @return
	 * @throws ValidityException
	 */
	public static Struct structOf(final Field field)
			throws ValidityException
	{
		final List<Member<?>> items = parse(field);
		return () -> items;
	}
	
	//TODO: Javadoc
	public static Struct structOf(final OpInfo opInfo, final Type newType) throws ValidityException {
		final List<Member<?>> items = parse(opInfo, newType);
		return () -> items;
	}
	
	public static Struct structOf(final Class<?> c, final Method m)
			throws ValidityException
		{
			final List<Member<?>> items = parse(c, m);
			return () -> items;
		}

	/**
	 * Parses the specified functional class for @{@link Parameter} annotations. This consists of the following steps:
	 * <br><br>
	 * 1) First annotations on the class level are checked. These annotate the signature (parameters and return type) of
	 * the specified functional class (or subtype of one). The annotations re expected to be in the following order: 
	 * parameters, return type.
	 * <br>
	 * E.g. a {@link Function} may be annotated with two @{@link Parameter} annotations, where the first annotation will
	 * annotate the parameter and the second the return type of the functional method {@link Function#apply(Object)} of
	 * {@link Function}.
	 * <br><br>
	 * 2) Second, annotations on the fields of the specified class are checked.
	 * 
	 * @param type the class to parse, is expected to contain some functional interface in its hierarchy
	 * @return list of identified member instances
	 * @throws ValidityException if there are problems during parsing
	 */
	public static List<Member<?>> parse(final Class<?> type)
		throws ValidityException
	{
		if (type == null) return null;

		final ArrayList<Member<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();
		final Set<String> names = new HashSet<>();

		// NB: Reject abstract classes.
		checkModifiers(type.getName() + ": ", problems, type.getModifiers(), true, Modifier.ABSTRACT);

		// obtain a parameterData (preferably one that scrapes the javadoc)
		ParameterData paramData;
		try {
			paramData = new JavadocParameterData(type);
		} catch(NullPointerException | IllegalArgumentException e) {
			paramData = new SynthesizedParameterData();
		}

		parseFunctionalParameters(items, names, problems, type, paramData);

		// Parse field level @OpDependency annotations.
		parseFieldOpDependencies(items, problems, type);

		// Fail if there were any problems.
		if (!problems.isEmpty()) throw new ValidityException(problems);

		return items;
	}
	
	/**
	 * Parses the specified field for @{@link Parameter} annotations. Has the same behavior as the first parsing step
	 * of {@link #parse(Class)}. 
	 * 
	 * @param field the field to parse, is expected to contain some functional interface in the hierarchy of its type
	 * @return list of identified member instances
	 * @throws ValidityException if there are problems during parsing
	 */
	public static List<Member<?>> parse(final Field field) throws ValidityException {
		Class<?> c = field.getDeclaringClass();
		if (c == null || field == null) return null;

		// obtain a parameterData (preferably one that scrapes the javadoc)
		ParameterData paramData;
		try {
			paramData = new JavadocParameterData(field);
		} catch(IllegalArgumentException e) {
			paramData = new SynthesizedParameterData();
		}
		field.setAccessible(true);
		
		final ArrayList<Member<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();
		final Set<String> names = new HashSet<>();
		final Type fieldType = Types.fieldType(field, c);

		checkModifiers(field.toString() + ": ", problems, field.getModifiers(), false, Modifier.FINAL);
		parseFunctionalParameters(items, names, problems, fieldType, paramData); 
		// Fail if there were any problems.
		if (!problems.isEmpty()) {
			throw new ValidityException(problems);
		}

		return items;
	}

	//TODO: Javadoc
	public static List<Member<?>> parse(final OpInfo opInfo, final Type newType) throws ValidityException {
		final ArrayList<Member<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();
		final Set<String> names = new HashSet<>();

		// obtain a parameterData (preferably one that scrapes the javadoc)
		ParameterData paramData;
		try {
			paramData = new JavadocParameterData(opInfo, newType);
		} catch(IllegalArgumentException e) {
			paramData = new SynthesizedParameterData();
		}

		parseFunctionalParameters(items, names, problems, newType, paramData);

		// Fail if there were any problems.
		if (!problems.isEmpty()) {
			throw new ValidityException(problems);
		}

		return items;
	}

	public static List<Member<?>> parse(final Class<?> c, final Method method)
			throws ValidityException
		{
			if (c == null || method == null) return null;

			method.setAccessible(true);

			// obtain a parameterData (preferably one that scrapes the javadoc)
			ParameterData paramData;
			try {
				paramData = new JavadocParameterData(method);
			} catch(IllegalArgumentException e) {
				paramData = new SynthesizedParameterData();
			}

			final ArrayList<Member<?>> items = new ArrayList<>();
			final ArrayList<ValidityProblem> problems = new ArrayList<>();
			final Set<String> names = new HashSet<>();
			final OpMethod methodAnnotation = method.getAnnotation(OpMethod.class);
			
			// Determine functional type
			Type functionalType;
			try {
				functionalType = getOpMethodType(methodAnnotation.type(),
					method);
			}
			catch (IllegalArgumentException e) {
				problems.add(new ValidityProblem(e.getMessage()));
				functionalType = Types.parameterizeRaw(methodAnnotation.type());
			}
			
			// Parse method level @Parameter annotations.
			parseFunctionalParameters(items, names, problems, functionalType, paramData);

			// Parse method level @OpDependency annotations.
			parseMethodOpDependencies(items, method);

			// Fail if there were any problems.
			if (!problems.isEmpty()) throw new ValidityException(problems);

			return items;
		}

	private static java.lang.reflect.Parameter[] getOpDependencies(
		java.lang.reflect.Parameter[] methodParams)
	{
		return Arrays //
			.stream(methodParams) //
			.filter(param -> param.getAnnotation(OpDependency.class) != null) //
			.toArray(java.lang.reflect.Parameter[]::new);
	}
	
	private static java.lang.reflect.Parameter[] getOpParams(
		java.lang.reflect.Parameter[] methodParams)
	{
		return Arrays //
			.stream(methodParams) //
			.filter(param -> param.getAnnotation(OpDependency.class) == null) //
			.toArray(java.lang.reflect.Parameter[]::new);

	}

	private static Type[] getOpParamTypes(
		java.lang.reflect.Parameter[] methodParams)
	{
		return Arrays //
			.stream(methodParams) //
			.filter(param -> param.getAnnotation(OpDependency.class) == null) //
			.map(param -> param.getParameterizedType())
			.toArray(Type[]::new);

	}

	public static Type getOpMethodType(Class<?> opClass, Method opMethod)
	{
		// since type is a functional interface, it has (exactly) one abstract
		// declared method (the method that our OpMethod is emulating).
		Method abstractMethod = singularAbstractMethod(opClass);
		Type[] typeMethodParams = abstractMethod.getGenericParameterTypes();
		java.lang.reflect.Parameter[] opMethodParams = getOpParams(opMethod.getParameters());

		if (typeMethodParams.length != opMethodParams.length) {
			throw new IllegalArgumentException("Number of parameters in OpMethod" +
				opMethod +
				" does not match the required number of parameters for functional method of FunctionalInterface " +
				opClass);
		}
		Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();
		
		// map params of OpMethod to type variables of abstract method of functional
		// interface (along with return type if applicable)
		// TODO: not sure how this handles when there are type variables.
		GenericAssignability.inferTypeVariables(typeMethodParams, getOpParamTypes(opMethodParams), typeVarAssigns);
		if (abstractMethod.getReturnType() != void.class) {
			GenericAssignability.inferTypeVariables(new Type[] {abstractMethod.getGenericReturnType()}, new Type[] {opMethod.getGenericReturnType()}, typeVarAssigns);
		}
		
		// parameterize opClass 
		return Types.parameterize(opClass, typeVarAssigns);
	}

	public static Method singularAbstractMethod(Class<?> functionalInterface) {
		Method[] typeMethods = Arrays.stream(functionalInterface
			.getMethods()).filter(method -> Modifier.isAbstract(method
				.getModifiers())).toArray(Method[]::new);
		if (typeMethods.length != 1) {
			throw new IllegalArgumentException(functionalInterface +
				" should be a FunctionalInterface, however it has " +
				typeMethods.length + " abstract declared methods");
		}

		return typeMethods[0];
	}

	//TODO: Javadoc
	// TODO: We currently assume that simplifiers only exist for pure inputs
	public static List<Member<?>> parse(final OpInfo opInfo, final List<Simplifier<?, ?>> suppliers) throws ValidityException {
		final ArrayList<Member<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();

		Struct srcStruct = opInfo.struct();
		for (int i = 0; i < srcStruct.members().size(); i++) {
			Member<?> member = srcStruct.members().get(i);
			// FIXME: We currently assume that only pure inputs have simplifiers.
			if (!member.isInput() || member.isOutput()) {
				items.add(member);
				continue;
			}
			Type newType = suppliers.get(i).simpleType();
			items.add(new ConvertedParameterMember<>(member, newType));
		}

		// Fail if there were any problems.
		// TODO: can we delete this?
		if (!problems.isEmpty()) {
			throw new ValidityException(problems);
		}

		return items;
	}
	
	/**
	 * Returns a list of {@link FunctionalMethodType}s describing the input and
	 * output types of the functional method of the specified functional type. In
	 * doing so, the return type of the method will me marked as
	 * {@link ItemIO#OUTPUT} and the all method parameters as {@link ItemIO#OUTPUT},
	 * except for parameters annotated with {@link Container} or {@link Mutable}
	 * which will be marked as {@link ItemIO#CONTAINER} or {@link ItemIO#MUTABLE}
	 * respectively. If the specified type does not have a functional method in its
	 * hierarchy, {@code null} will be returned.<br>
	 * The order will be the following: method parameters from left to right, then
	 * return type.
	 * 
	 * @param functionalType
	 * @return
	 */
	public static List<FunctionalMethodType> findFunctionalMethodTypes(Type functionalType) {
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
		for (Type t : Types.getExactParameterTypes(functionalMethod, paramfunctionalType)) {
			final ItemIO ioType;
			if (AnnotationUtils.getMethodParameterAnnotation(functionalMethod, i, Container.class) != null)
				ioType = ItemIO.CONTAINER;
			else if (AnnotationUtils.getMethodParameterAnnotation(functionalMethod, i, Mutable.class) != null)
				ioType = ItemIO.MUTABLE;
			else
				ioType = ItemIO.INPUT;
			out.add(new FunctionalMethodType(t, ioType));
			i++;
		}
		
		Type returnType = Types.getExactReturnType(functionalMethod, paramfunctionalType);
		if (!returnType.equals(void.class)) {
			out.add(new FunctionalMethodType(returnType, ItemIO.OUTPUT));
		}
		
		return out;
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
			Type type, ParameterData data) {
		//Search for the functional method of 'type' and map its signature to ItemIO
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
		List<Member<?>> fmtMembers = data.synthesizeMembers(fmts);

		for (Member<?> m : fmtMembers) {
			String key = m.getKey();
			final Type itemType = m.getType();

			final boolean valid = checkValidity(m, key, Types.raw(itemType), false,
				names, problems);
			if (!valid) continue;
			items.add(m);
			names.add(m.getKey());
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

	private static void parseMethodOpDependencies(final List<Member<?>> items,
		final Method annotatedMethod)
	{
		final java.lang.reflect.Parameter[] methodParams = annotatedMethod
			.getParameters();
		final java.lang.reflect.Parameter[] opDependencyParams = getOpDependencies(methodParams);

		for (java.lang.reflect.Parameter param : opDependencyParams) {
			final OpDependency dependency = param.getAnnotation(OpDependency.class);
			final Type methodParamType = param.getParameterizedType();
			final Member<?> item = new MethodParameterOpDependencyMember<>(
				param, methodParamType, dependency);
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

	private static boolean checkValidity(Member<?> m, String name,
		Class<?> type, boolean isFinal, Set<String> names,
		ArrayList<ValidityProblem> problems)
	{
		boolean valid = true;

		if (names.contains(name)) {
			// NB: Shadowed parameters are bad because they are ambiguous.
			final String error = "Invalid duplicate parameter: " + name;
			problems.add(new ValidityProblem(error));
			valid = false;
		}

		if ((m.getIOType() == ItemIO.MUTABLE || m.getIOType() == ItemIO.CONTAINER) && isImmutable(type)) {
			// NB: The MUTABLE and CONTAINER types signify that the parameter
			// will be written to, but immutable parameters cannot be changed in
			// such a manner, so it makes no sense to label them as such.
			final String error = "Immutable " + m.getIOType() + " parameter: " + name + " (" + type.getName() + " is immutable)";
			problems.add(new ValidityProblem(error));
			valid = false;
		}

		return valid;
	}
	
	/**
	 * Attempts to find the single functional method of the specified
	 * class, by scanning the for functional interfaces. If there
	 * is no functional interface, null will be returned.
	 * 
	 * @param cls
	 * @return
	 */
	private static Method findFunctionalMethod(Class<?> cls) {
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
