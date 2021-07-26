package org.scijava.ops.engine.simplify;

import com.google.common.collect.Streams;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.scijava.function.Computers;
import org.scijava.function.Container;
import org.scijava.function.Mutable;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRef;
import org.scijava.ops.api.OpUtils;
import org.scijava.ops.engine.util.AnnotationUtils;
import org.scijava.types.Nil;
import org.scijava.types.Types;
import org.scijava.types.inference.GenericAssignability;
import org.scijava.types.inference.InterfaceInference;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class SimplificationUtils {

	/**
	 * Determines the {@link Type} of a retyped Op using its old {@code Type}, a
	 * new set of {@code args} and a new {@code outType}. Used to create
	 * {@link SimplifiedOpRef}s. This method assumes that:
	 * <ul>
	 * <li>{@code originalOpRefType} is (or is a subtype of) some
	 * {@link FunctionalInterface}
	 * <li>all {@link TypeVariable}s declared by that {@code FunctionalInterface}
	 * are present in the signature of that interface's single abstract method.
	 * </ul>
	 * 
	 * @param originalOpType - the {@link Type} declared by the source
	 *          {@link OpRef}
	 * @param newArgs - the new argument {@link Type}s requested by the
	 *          {@link OpRef}.
	 * @param newOutType - the new output {@link Type} requested by the
	 *          {@link OpRef}.
	 * @return - a new {@code type} for a {@link SimplifiedOpRef}.
	 */
	public static ParameterizedType retypeOpType(Type originalOpType, Type[] newArgs, Type newOutType) {
			// only retype types that we know how to retype
			if (!(originalOpType instanceof ParameterizedType))
				throw new IllegalStateException("We hadn't thought about this yet.");
			Class<?> opType = Types.raw(originalOpType);
			Method fMethod = OpUtils.findFunctionalMethod(opType);

			Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();

			// solve input types
			Type[] genericParameterTypes = paramTypesFromOpType(opType, fMethod);
			GenericAssignability.inferTypeVariables(genericParameterTypes, newArgs, typeVarAssigns);

			// solve output type
			Type genericReturnType = returnTypeFromOpType(opType, fMethod);
			if (genericReturnType != void.class) {
				GenericAssignability.inferTypeVariables(new Type[] {genericReturnType}, new Type[] {newOutType}, typeVarAssigns);
			}

			// build new (read: simplified) Op type
			return Types.parameterize(opType, typeVarAssigns);
	}

	static Type[] paramTypesFromOpType(Class<?> opType,
		Method fMethod)
	{
		Type[] genericParameterTypes = fMethod.getGenericParameterTypes();
		if(fMethod.getDeclaringClass().equals(opType))
			return genericParameterTypes;
		return typesFromOpType(opType, fMethod, genericParameterTypes);
		
	}

	static Type returnTypeFromOpType(Class<?> opType,
		Method fMethod)
	{
		Type genericReturnType = fMethod.getGenericReturnType();
		if(fMethod.getDeclaringClass().equals(opType))
			return genericReturnType;
		return typesFromOpType(opType, fMethod, genericReturnType)[0];
	}

	private static Type[] typesFromOpType(Class<?> opType, Method fMethod, Type... types) {
		Map<TypeVariable<?>, Type> map = new HashMap<>();
		Class<?> declaringClass = fMethod.getDeclaringClass();
		Type genericDeclaringClass = Types.parameterizeRaw(declaringClass);
		Type genericClass = Types.parameterizeRaw(opType);
		Type superGenericClass = Types.getExactSuperType(genericClass, declaringClass);
		GenericAssignability.inferTypeVariables(new Type[] {genericDeclaringClass}, new Type[] {superGenericClass}, map);

		return Types.mapVarToTypes(types, map);
	}

	/**
	 * Finds the {@link Mutable} or {@link Container} argument of a
	 * {@link FunctionalInterface}'s singular abstract method. If there is no
	 * argument annotated with {@code Mutable} or {@code Container}, then it is
	 * assumed that no arguments are mutable and that the output of the functional
	 * {@link Method} is its output. We also assume that only one argument is
	 * annotated.
	 * 
	 * @param c - the {@link Class} extending a {@link FunctionalInterface}
	 * @return the index of the mutable argument (or -1 iff the output is returned).
	 */
	public static int findMutableArgIndex(Class<?> c) {
		Method fMethod = OpUtils.findFunctionalMethod(c);
		for (int i = 0; i < fMethod.getParameterCount(); i++) {
			if (AnnotationUtils.getMethodParameterAnnotation(fMethod, i,
				Mutable.class) != null) return i;
			if (AnnotationUtils.getMethodParameterAnnotation(fMethod, i,
				Container.class) != null) return i;
		}
		return -1;
	}

	/**
	 * Given a simplifier or focuser (henceforth called mutators), it is often
	 * helpful to discern its output/input type given its input/output type.
	 * We can discern the unknown type by
	 * first inferring the type variables of {@code mutatorInferFrom} from
	 * {@code inferFrom}. We can then use these mappings to resolve the type
	 * variables of {@code unresolvedType}. This method assumes that:
	 * <ul>
	 * <li> {@code inferFrom} is assignable to {@code mutatorInferFrom}
	 * <li> There are no vacuous generics in {@code unresolvedType}
	 * </ul>
	 * 
	 * @param inferFrom - the concrete input/output type that we would like to pass to the mutator.
	 * @param mutatorInferFrom - the (possibly generic) input/output type of the mutator {@link OpInfo}
	 * @param unresolvedType - the (possibly generic) output/input type of the mutator {@link OpInfo}
	 * @return - the concrete output/input type obtained/given by the mutator (restricted by {@code inferFrom}
	 */
	public static Type resolveMutatorTypeArgs(Type inferFrom, Type mutatorInferFrom, Type unresolvedType) {
		if(!Types.containsTypeVars(unresolvedType)) return unresolvedType;
		Map<TypeVariable<?>, Type> map = new HashMap<>();
		GenericAssignability.inferTypeVariables(new Type[] {mutatorInferFrom}, new Type[] {inferFrom}, map);
		return Types.mapVarToTypes(unresolvedType, map);
	}

	/**
	 * Finds required mutators (i.e. simplifier or focuser).
	 * 
	 * @param env - the {@link OpEnvironment} to query for the needed Op
	 * @param mutatorInfos - the {@link OpInfo}s for which Ops are needed
	 * @param originalInputs - the concrete input {@link Type} of the Op.
	 * @param mutatedInputs - the concrete output {@link Type} of the Op.
	 * @return a set of {@code Op}s, instantiated from {@code mutatorInfo}s, that
	 *         satisfy the prescribed input/output types.
	 */
	public static List<Function<?, ?>> findArgMutators(OpEnvironment env, List<OpInfo> mutatorInfos,
		Type[] originalInputs, Type[] mutatedInputs)
	{
		if (mutatorInfos.size() != originalInputs.length)
			throw new IllegalStateException(
				"Mismatch between number of argument mutators and arguments in ref:\n");
		
		List<Function<?, ?>> mutators = new ArrayList<>();
		for(int i = 0; i < mutatorInfos.size(); i++) {
			Function<?, ?> mutator = findArgMutator(env, mutatorInfos.get(i), originalInputs[i], mutatedInputs[i]);
			mutators.add(mutator);
		}
		return mutators;
	}
	
	public static Function<?, ?> findArgMutator(OpEnvironment env, OpInfo mutatorInfo, Type originalInput, Type mutatedInput){
			Type opType = Types.parameterize(Function.class, new Type[] {originalInput, mutatedInput});
			Function<?, ?> mutator = (Function<?, ?>) env.op(mutatorInfo,
				Nil.of(opType), new Nil<?>[] { Nil.of(originalInput) }, Nil.of(
					mutatedInput));
			return mutator;
	}

	/**
	 * Uses Google Guava to generate a list of permutations of each available
	 * simplification possibility
	 */
	public static List<List<OpInfo>> simplifyArgs(OpEnvironment env, Type... t){
		return Arrays.stream(t) //
				.map(type -> getSimplifiers(env, type)) //
				.collect(Collectors.toList());
	}

	/**
	 * Obtains all {@link Simplifier}s known to the environment that can operate
	 * on {@code t}. If no {@code Simplifier}s are known to explicitly work on
	 * {@code t}, an {@link Identity} simplifier will be created.
	 * 
	 * @param t - the {@link Type} we are interested in simplifying.
	 * @return a list of {@link Simplifier}s that can simplify {@code t}.
	 */
	public static List<OpInfo> getSimplifiers(OpEnvironment env, Type t) {
		// TODO: optimize
		Stream<OpInfo> infoStream = StreamSupport.stream(env.infos("simplify").spliterator(), true);
		List<OpInfo> list = infoStream//
				.filter(info -> Function.class.isAssignableFrom(Types.raw(info.opType()))) //
				.filter(info -> Types.isAssignable(t, info.inputs().get(0).getType())) //
				.collect(Collectors.toList());
		return list;
	}

	/**
	 * Uses Google Guava to generate a list of permutations of each available
	 * simplification possibility
	 */
	public static List<List<OpInfo>> focusArgs(OpEnvironment env, Type... t){
		return Arrays.stream(t) //
			.map(type -> getFocusers(env, type)) //
			.collect(Collectors.toList());
	}


	/**
	 * Obtains all {@link Simplifier}s known to the environment that can operate
	 * on {@code t}. If no {@code Simplifier}s are known to explicitly work on
	 * {@code t}, an {@link Identity} simplifier will be created.
	 * 
	 * @param t - the {@link Type} we are interested in simplifying.
	 * @return a list of {@link Simplifier}s that can simplify {@code t}.
	 */
	public static List<OpInfo> getFocusers(OpEnvironment env, Type t) {
		// TODO: optimize
		Stream<OpInfo> infoStream = StreamSupport.stream(env.infos("focus").spliterator(), true);
		List<OpInfo> list = infoStream//
				.filter(info -> Function.class.isAssignableFrom(Types.raw(info.opType()))) //
				.filter(info -> Types.isAssignable(t, info.output().getType())) //
				.collect(Collectors.toList());
		return list;
	}
	
	/**
	 * Creates a Class given an Op and a set of {@link Simplifier}s. This class:
	 * <ul>
	 * <li>is of the same functional type as the given Op
	 * <li>has type arguments that are of the simplified form of the type
	 * arguments of the given Op (these arguments are dictated by the list of
	 * {@code Simplifier}s.
	 * <li>
	 * 
	 * @param originalOp - the Op that will be simplified
	 * @param metadata - the information required to simplify {@code originalOp}.
	 * @return a wrapper of {@code originalOp} taking arguments that are then
	 *         mutated to satisfy {@code originalOp}, producing outputs that are
	 *         then mutated to satisfy the desired output of the wrapper.
	 * @throws Throwable
	 */
	protected static Object javassistOp(Object originalOp, SimplificationMetadata metadata) throws Throwable {
		ClassPool pool = ClassPool.getDefault();

		// Create wrapper class
		String className = formClassName(metadata);
		Class<?> c;
		try {
			c = pool.getClassLoader().loadClass(className);
		}
		catch (ClassNotFoundException e) {
			CtClass cc = generateSimplifiedWrapper(pool, className, metadata);
			c = cc.toClass(MethodHandles.lookup());
		}

		// Return Op instance
		return c.getDeclaredConstructor(metadata.constructorClasses())
			.newInstance(metadata.constructorArgs(originalOp));
	}

	// TODO: consider correctness
	private static String formClassName(SimplificationMetadata metadata) {
		// package name - required to be this package for the Lookup to work
		String packageName = SimplificationUtils.class.getPackageName();
		StringBuilder sb = new StringBuilder(packageName + ".");

		// class name
		String implementationName = metadata.info().implementationName();
		String originalName = implementationName.substring(implementationName
			.lastIndexOf('.') + 1); // we only want the class name
		Stream<String> memberNames = //
			Streams.concat(Arrays.stream(metadata.originalInputs()), //
				Stream.of(metadata.originalOutput())) //
				.map(type -> getClassName(Types.raw(type)));
		Iterable<String> iterableNames = (Iterable<String>) memberNames::iterator;
		String simplifiedParameters = String.join("_", iterableNames);
		String className = originalName.concat("_simplified_" + simplifiedParameters);
		if(className.chars().anyMatch(c -> !Character.isJavaIdentifierPart(c)))
			throw new IllegalArgumentException(className + " is not a valid class name!");

		sb.append(className);
		return sb.toString();
	}

	/**
	 * {@link Class}es of array types return "[]" when
	 * {@link Class#getSimpleName()} is called. Those characters are invalid in a
	 * class name, so we exchange them for the suffix "_Arr".
	 * 
	 * @param clazz - the {@link Class} for which we need a name
	 * @return - a name that is legal as part of a class name.
	 */
	private static String getClassName(Class<?> clazz) {
		String className = clazz.getSimpleName();
		if(className.chars().allMatch(c -> Character.isJavaIdentifierPart(c)))
			return className;
		if(clazz.isArray())
			return clazz.getComponentType().getSimpleName() + "_Arr";
		return className;
	}

	private static CtClass generateSimplifiedWrapper(ClassPool pool, String className, SimplificationMetadata metadata) throws Throwable
	{
		CtClass cc = pool.makeClass(className);

		// Add implemented interface
		CtClass jasOpType = pool.get(metadata.opType().getName());
		cc.addInterface(jasOpType);

		// Add input simplifier fields
		generateNFields(pool, cc, "inputSimplifier", metadata.numInputs());

		// Add input focuser fields
		generateNFields(pool, cc, "inputFocuser", metadata.numInputs());
		
		// Add output simplifier field
		generateNFields(pool, cc, "outputSimplifier", 1);
		
		// Add output focuser field
		generateNFields(pool, cc, "outputFocuser", 1);

		// Add Op field
		CtField opField = createOpField(pool, cc, metadata.opType(), "op");
		cc.addField(opField);

		// Add copy Op field iff not pure output
		if(metadata.hasCopyOp()) {
			CtField copyOpField = createOpField(pool, cc, Computers.Arity1.class,
				"copyOp");
			cc.addField(copyOpField);
		}

		// Add constructor to take the Simplifiers, as well as the original op.
		CtConstructor constructor = CtNewConstructor.make(createConstructor(cc,
			metadata), cc);
		cc.addConstructor(constructor);

		// add functional interface method
		CtMethod functionalMethod = CtNewMethod.make(createFunctionalMethod(metadata),
			cc);
		cc.addMethod(functionalMethod);
		return cc;
	}

	private static void generateNFields(ClassPool pool, CtClass cc, String base,
		int numFields) throws NotFoundException, CannotCompileException
	{
		for (int i = 0; i < numFields; i++) {
			CtField f = createMutatorField(pool, cc, Function.class, base + i);
			cc.addField(f);
		}
	}

	private static CtField createMutatorField(ClassPool pool, CtClass cc, Class<?> fieldType, String name) throws NotFoundException,
	CannotCompileException
{
	CtClass fType = pool.get(fieldType.getName());
	CtField f = new CtField(fType, name, cc);
	f.setModifiers(Modifier.PRIVATE + Modifier.FINAL);
	return f;
}

	private static CtField createOpField(ClassPool pool, CtClass cc, Class<?> opType, String fieldName)
			throws NotFoundException, CannotCompileException
		{
			CtClass fType = pool.get(opType.getName());
			CtField f = new CtField(fType, fieldName, cc);
			f.setModifiers(Modifier.PRIVATE + Modifier.FINAL);
			return f;
		}

	private static String createConstructor(CtClass cc, SimplificationMetadata metadata) {
		StringBuilder sb = new StringBuilder();
		// constructor signature
		sb.append("public " + cc.getSimpleName() + "(");
		Class<?> depClass = Function.class;
		// input simplifiers
		for (int i = 0; i < metadata.numInputs(); i++) {
			sb.append(depClass.getName() + " inputSimplifier" + i);
			sb.append(",");
		}
		// input focusers
		for (int i = 0; i < metadata.numInputs(); i++) {
			sb.append(depClass.getName() + " inputFocuser" + i);
			sb.append(",");
		}
		// output simplifier
		sb.append(depClass.getName() + " outputSimplifier0" );
		sb.append(",");
		// output focuser
		sb.append(depClass.getName() + " outputFocuser0" );
		sb.append(",");
		// op
		Class<?> opClass = metadata.opType();
		sb.append(" " + opClass.getName() + " op");
		// copy op
		if(metadata.hasCopyOp()) {
			Class<?> copyOpClass = Computers.Arity1.class;
			sb.append(", " + copyOpClass.getName() + " copyOp");
		}
		sb.append(") {");

		// assign dependencies to field
		for (int i = 0; i < metadata.numInputs(); i++) {
			sb.append("this.inputSimplifier" + i + " = inputSimplifier" + i + ";");
		}
		for (int i = 0; i < metadata.numInputs(); i++) {
			sb.append("this.inputFocuser" + i + " = inputFocuser" + i + ";");
		}
		sb.append("this.outputSimplifier0" + " = outputSimplifier0" + ";");
		sb.append("this.outputFocuser0" + " = outputFocuser0" + ";");
		sb.append("this.op = op;");
		if(metadata.hasCopyOp()) {
			sb.append("this.copyOp = copyOp;");
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Creates the functional method of a simplified Op. This functional method
	 * must:
	 * <ol>
	 * <li>Simplify all inputs using the {@link Function}s provided by the
	 * {@link SimplifiedOpRef}
	 * <li>Focus the simplified inputs using the {@link Function}s provided by the
	 * {@link SimplifiedOpInfo}
	 * <li>Call the {@code Op} using the focused inputs.
	 * </ol>
	 * <b>NB</b> The Javassist compiler
	 * <a href="https://www.javassist.org/tutorial/tutorial3.html#generics">does
	 * not fully support generics</a>, so we must ensure that the types are raw.
	 * At compile time, the raw types are equivalent to the generic types, so this
	 * should not pose any issues.
	 * 
	 * @param metadata - the {@link SimplificationMetadata} containing the
	 *          information needed to write the method.
	 * @return a {@link String} that can be used by
	 *         {@link CtMethod#make(String, CtClass)} to generate the functional
	 *         method of the simplified Op
	 */
	private static String createFunctionalMethod(SimplificationMetadata metadata) {
		StringBuilder sb = new StringBuilder();

		// determine the name of the functional method
		Method m = InterfaceInference.singularAbstractMethod(metadata.opType());
		// determine the name of the output:
		String opOutput = "";
		int ioIndex = metadata.ioArgIndex();
		if(ioIndex == -1) {
			opOutput = "originalOut";
		}
		else {
			opOutput = "focused" + ioIndex;
		}

		//-- signature -- //
		sb.append(generateSignature(m));

		//-- body --//

		// preprocessing
		sb.append(" {");
		sb.append(fMethodPreprocessing(metadata));

		// processing
		sb.append(fMethodProcessing(metadata, m, opOutput));

		// postprocessing
		sb.append(fMethodPostprocessing(metadata, opOutput));

		// if pure output, return it
		if (metadata.pureOutput()) {
			sb.append("return out;");
		}
		sb.append("}");
		return sb.toString();
	}

	private static String generateSignature(Method m) {
		StringBuilder sb = new StringBuilder();
		String methodName = m.getName();

		// method modifiers
		boolean isVoid = m.getReturnType() == void.class;
		sb.append("public " + (isVoid ? "void" : "Object") + " " + methodName +
			"(");

		int inputs = m.getParameterCount();
		for (int i = 0; i < inputs; i++) {
			sb.append(" Object in" + i);
			if (i < inputs - 1) sb.append(",");
		}

		sb.append(" )");

		return sb.toString();
	}

	private static String fMethodProcessing(SimplificationMetadata metadata,
		Method m, String opOutput)
	{
		StringBuilder sb = new StringBuilder();
		// declare / assign Op's original output
		if (metadata.pureOutput()) {
			sb.append(metadata.originalOutput().getTypeName() + " " + opOutput +
				" = ");
			sb.append("(" + metadata.originalOutput().getTypeName() + ") ");
		}
		// call the op
		sb.append("op." + m.getName() + "(");
		for (int i = 0; i < metadata.numInputs(); i++) {
			sb.append(" focused" + i);
			if (i + 1 < metadata.numInputs()) sb.append(",");
		}
		sb.append(");");
		return sb.toString();
	}

	private static String fMethodPostprocessing(SimplificationMetadata metadata, String opOutput) {
		StringBuilder sb = new StringBuilder();

		// simplify output
		Type original = Types.raw(metadata.originalOutput());
		Type simple = Types.raw(metadata.simpleOutput());
		sb.append(simple.getTypeName() + " simpleOut = (" + simple
			.getTypeName() + ") outputSimplifier0.apply((" + original
				.getTypeName() + ") " + opOutput + ");");	
	
		Type focused = Types.raw(metadata.focusedOutput());
		Type unfocused = Types.raw(metadata.unfocusedOutput());
		sb.append(focused.getTypeName() + " out = (" + focused
			.getTypeName() + ") outputFocuser0.apply((" + unfocused
				.getTypeName() + ") simpleOut);");

		// call copy op iff it exists
		if(metadata.hasCopyOp()) {
			int ioIndex = metadata.ioArgIndex();
			Type ioType = metadata.originalInputs()[ioIndex];
			String originalIOArg = "in" + ioIndex;
			sb.append("copyOp.compute((" + focused.getTypeName() + ") out, (" + ioType.getTypeName() + ") " + originalIOArg + ");");
			
		}

		return sb.toString();
	}

	private static String fMethodPreprocessing(SimplificationMetadata metadata) {
		StringBuilder sb = new StringBuilder();

		// simplify all inputs
		for (int i = 0; i < metadata.numInputs(); i++) {
			Type focused = Types.raw(metadata.originalInputs()[i]);
			Type simple = Types.raw(metadata.simpleInputs()[i]);
			sb.append(simple.getTypeName() + " simple" + i + " = (" + simple
				.getTypeName() + ") inputSimplifier" + i + ".apply((" + focused
					.getTypeName() + ") in" + i + ");");
		}

		// focus all inputs
		for (int i = 0; i < metadata.numInputs(); i++) {
			Type focused = Types.raw(metadata.focusedInputs()[i]);
			Type unfocused = Types.raw(metadata.unfocusedInputs()[i]);
			sb.append(focused.getTypeName() + " focused" + i + " = (" + focused
				.getTypeName() + ") inputFocuser" + i + ".apply((" + unfocused
					.getTypeName() + ") simple" + i + ");");
		}

		return sb.toString();
	}


}
