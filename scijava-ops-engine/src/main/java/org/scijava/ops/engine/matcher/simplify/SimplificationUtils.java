/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.matcher.simplify;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Container;
import org.scijava.function.Mutable;
import org.scijava.ops.api.*;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.engine.util.Infos;
import org.scijava.ops.engine.util.internal.AnnotationUtils;
import org.scijava.types.Any;
import org.scijava.types.Nil;
import org.scijava.types.Types;
import org.scijava.types.inference.FunctionalInterfaces;
import org.scijava.types.inference.GenericAssignability;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public final class SimplificationUtils {

	private SimplificationUtils() {
		// Prevent instantiation of static utility class
	}

	/**
	 * Determines the {@link Type} of a retyped Op using its old {@code Type}, a
	 * new set of {@code args} and a new {@code outType}. Used to create
	 * {@link SimplifiedOpRequest}s. This method assumes that:
	 * <ul>
	 * <li>{@code originalOpRefType} is (or is a subtype of) some
	 * {@link FunctionalInterface}</li>
	 * <li>all {@link TypeVariable}s declared by that {@code FunctionalInterface}
	 * are present in the signature of that interface's single abstract
	 * method.</li>
	 * </ul>
	 *
	 * @param originalOpType - the {@link Type} declared by the source
	 *          {@link OpRequest}
	 * @param newArgs - the new argument {@link Type}s requested by the
	 *          {@link OpRequest}.
	 * @param newOutType - the new output {@link Type} requested by the
	 *          {@link OpRequest}.
	 * @return - a new {@code type} for a {@link SimplifiedOpRequest}.
	 */
	public static ParameterizedType retypeOpType(Type originalOpType,
		Type[] newArgs, Type newOutType)
	{
		// only retype types that we know how to retype
		Class<?> opType = Types.raw(originalOpType);
		Method fMethod = FunctionalInterfaces.functionalMethodOf(opType);

		Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();

		// solve input types
		Type[] genericParameterTypes = paramTypesFromOpType(opType, fMethod);
		GenericAssignability.inferTypeVariables(genericParameterTypes, newArgs,
			typeVarAssigns);

		// solve output type
		Type genericReturnType = returnTypeFromOpType(opType, fMethod);
		if (genericReturnType != void.class) {
			GenericAssignability.inferTypeVariables(new Type[] { genericReturnType },
				new Type[] { newOutType }, typeVarAssigns);
		}

		// build new (read: simplified) Op type
		return Types.parameterize(opType, typeVarAssigns);
	}

	static Type[] paramTypesFromOpType(Class<?> opType, Method fMethod) {
		Type[] genericParameterTypes = fMethod.getGenericParameterTypes();
		if (fMethod.getDeclaringClass().equals(opType))
			return genericParameterTypes;
		return typesFromOpType(opType, fMethod, genericParameterTypes);

	}

	static Type returnTypeFromOpType(Class<?> opType, Method fMethod) {
		Type genericReturnType = fMethod.getGenericReturnType();
		if (fMethod.getDeclaringClass().equals(opType)) return genericReturnType;
		return typesFromOpType(opType, fMethod, genericReturnType)[0];
	}

	private static Type[] typesFromOpType(Class<?> opType, Method fMethod,
		Type... types)
	{
		Map<TypeVariable<?>, Type> map = new HashMap<>();
		Class<?> declaringClass = fMethod.getDeclaringClass();
		Type genericDeclaringClass = Types.parameterizeRaw(declaringClass);
		Type genericClass = Types.parameterizeRaw(opType);
		Type superGenericClass = Types.getExactSuperType(genericClass,
			declaringClass);
		GenericAssignability.inferTypeVariables(new Type[] {
			genericDeclaringClass }, new Type[] { superGenericClass }, map);

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
	 * @return the index of the mutable argument (or -1 iff the output is
	 *         returned).
	 */
	public static int findMutableArgIndex(Class<?> c) {
		Method fMethod = FunctionalInterfaces.functionalMethodOf(c);
		for (int i = 0; i < fMethod.getParameterCount(); i++) {
			if (AnnotationUtils.getMethodParameterAnnotation(fMethod, i,
				Mutable.class) != null) return i;
			if (AnnotationUtils.getMethodParameterAnnotation(fMethod, i,
				Container.class) != null) return i;
		}
		return -1;
	}

	public static SimplifiedOpInfo simplifyInfo(OpEnvironment env, OpInfo info) {
		Hints h = new Hints(BaseOpHints.Adaptation.FORBIDDEN,
			BaseOpHints.Simplification.FORBIDDEN);
		List<RichOp<Function<?, ?>>> inFocusers = new ArrayList<>();
		Type[] args = info.inputTypes().toArray(Type[]::new);
		for (Type arg : args) {
			var inNil = Nil.of(arg);
			try {
				var focuser = env.unary("engine.focus", h).inType(Any.class).outType(
					inNil).function();
				inFocusers.add(org.scijava.ops.api.Ops.rich(focuser));
			}
			catch (OpMatchingException e) {
				var identity = env.unary("engine.identity", h).inType(inNil).outType(
					inNil).function();
				inFocusers.add(org.scijava.ops.api.Ops.rich(identity));
			}
		}
		var outNil = Nil.of(info.outputType());
		RichOp<Function<?, ?>> outSimplifier;
		try {
			var simplifier = env.unary("engine.simplify", h).inType(outNil).outType(
				Object.class).function();
			outSimplifier = org.scijava.ops.api.Ops.rich(simplifier);
		}
		catch (OpMatchingException e) {
			var identity = env.unary("engine.identity", h).inType(outNil).outType(
				outNil).function();
			outSimplifier = org.scijava.ops.api.Ops.rich(identity);
		}

		RichOp<Computers.Arity1<?, ?>> copyOp = getCopyOp(env, info, inFocusers,
			outSimplifier, h);
		return new SimplifiedOpInfo(info, inFocusers, outSimplifier, copyOp);
	}

	/**
	 * Helper method that finds the {@code engine.copy} Op needed for a
	 * {@link SimplifiedOpInfo}
	 *
	 * @param env the {@link OpEnvironment} containing {@code engine.copy} Ops.
	 * @param info the original {@link OpInfo}.
	 * @param inFocusers the {@code engine.focus} Ops used to focus the inputs to
	 *          the simplified Op
	 * @param outSimplifier the {@code engine.simplify} Op used to simplify the
	 *          output for the simplified Op
	 * @param h {@link Hints} to be used in matching the copy Op.
	 * @return a {@code engine.copy} Op
	 */
	private static RichOp<Computers.Arity1<?, ?>> getCopyOp( //
		OpEnvironment env, //
		OpInfo info, //
		List<RichOp<Function<?, ?>>> inFocusers, //
		RichOp<Function<?, ?>> outSimplifier, //
		Hints h //
	) {
		int ioIndex = SimplificationUtils.findMutableArgIndex(Types.raw(info
			.opType()));
		// If IO index is -1, output is returned - no need to copy.
		if (ioIndex == -1) {
			return null;
		}
		// Otherwise, we need an Op to convert the simple output back into the
		// pre-focused input
		// Determine simple output type.
		var simpleOut = outType(info.outputType(), outSimplifier);
		// Determine unfocused input type.
		var focuserInfo = Ops.info(inFocusers.get(ioIndex));
		Map<TypeVariable<?>, Type> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables( //
			new Type[] { focuserInfo.outputType() }, //
			new Type[] { info.inputTypes().get(ioIndex) }, //
			typeAssigns //
		);
		var unfocusedInput = Nil.of(Types.mapVarToTypes( //
			focuserInfo.inputTypes().get(0), //
			typeAssigns //
		));
		// Match a copier
		return Ops.rich(env.unary("engine.copy", h) //
			.inType(simpleOut) //
			.outType(unfocusedInput) //
			.computer());
	}

	private static Nil<?> outType(Type originalOutput,
		RichOp<Function<?, ?>> outputSimplifier)
	{
		Map<TypeVariable<?>, Type> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables( //
			new Type[] { org.scijava.ops.api.Ops.info(outputSimplifier).inputTypes()
				.get(0) }, //
			new Type[] { originalOutput }, //
			typeAssigns //
		);
		Type outType = Types.mapVarToTypes(org.scijava.ops.api.Ops.info(
			outputSimplifier).outputType(), typeAssigns);
		return Nil.of(outType);
	}

	/**
	 * Creates a Class given an Op and a set of simplifiers. This class:
	 * <ul>
	 * <li>is of the same functional type as the given Op</li>
	 * <li>has type arguments that are of the simplified form of the type
	 * arguments of the given Op (these arguments are dictated by the list of
	 * {@code Simplifier}s.</li>
	 * <li>
	 *
	 * @param originalOp - the Op that will be simplified
	 * @return a wrapper of {@code originalOp} taking arguments that are then
	 *         mutated to satisfy {@code originalOp}, producing outputs that are
	 *         then mutated to satisfy the desired output of the wrapper.
	 * @throws Throwable in the case of an error
	 */
	public static Object javassistOp( //
		Object originalOp, //
		OpInfo alteredInfo, //
		List<Function<?, ?>> inputProcessors, //
		Function<?, ?> outputProcessor, //
		Computers.Arity1<?, ?> copyOp //
	) throws Throwable {
		ClassPool pool = ClassPool.getDefault();

		// Create wrapper class
		String className = formClassName(alteredInfo);
		Class<?> c;
		try {
			c = pool.getClassLoader().loadClass(className);
		}
		catch (ClassNotFoundException e) {
			CtClass cc = generateSimplifiedWrapper( //
				pool, //
				className, //
				alteredInfo, //
				inputProcessors, //
				//
				copyOp //
			);
			c = cc.toClass(MethodHandles.lookup());
		}

		// Return Op instance
		return c.getDeclaredConstructor(constructorClasses(alteredInfo,
			copyOp != null)).newInstance(constructorArgs(inputProcessors,
				outputProcessor, copyOp, originalOp));
	}

	private static Class<?>[] constructorClasses( //
		OpInfo originalInfo, //
		boolean addCopyOp //
	) {
		// there are 2*numInputs input mutators, 2 output mutators
		int numMutators = originalInfo.inputTypes().size() + 1;
		// orignal Op plus a output copier if applicable
		int numOps = addCopyOp ? 2 : 1;
		Class<?>[] args = new Class<?>[numMutators + numOps];
		for (int i = 0; i < numMutators; i++)
			args[i] = Function.class;
		args[args.length - numOps] = Types.raw(originalInfo.opType());
		if (addCopyOp) args[args.length - 1] = Computers.Arity1.class;
		return args;

	}

	private static Object[] constructorArgs( //
		List<Function<?, ?>> inputProcessors, //
		Function<?, ?> outputProcessor, //
		Computers.Arity1<?, ?> outputCopier, //
		Object op //
	) {
		List<Object> args = new ArrayList<>(inputProcessors);
		args.add(outputProcessor);
		args.add(op);
		if (outputCopier != null) {
			args.add(outputCopier);
		}
		return args.toArray();
	}

	// TODO: consider correctness
	private static String formClassName(OpInfo altered) {
		// package name - required to be this package for the Lookup to work
		String packageName = SimplificationUtils.class.getPackageName();
		StringBuilder sb = new StringBuilder(packageName + ".");

		// class name
		String implementationName = altered.implementationName();
		String className = implementationName.replaceAll("[^a-zA-Z0-9.\\-]", "_");
		className = className.substring(className.lastIndexOf(".") + 1);
		if (className.chars().anyMatch(c -> !Character.isJavaIdentifierPart(c)))
			throw new IllegalArgumentException(className +
				" is not a valid class name!");

		sb.append(className);
		return sb.toString();
	}

	private static CtClass generateSimplifiedWrapper(ClassPool pool, //
		String className, //
		OpInfo altered, //
		List<Function<?, ?>> inputProcessors, //
		//
		Computers.Arity1<?, ?> outputCopier //
	) throws Throwable {
		CtClass cc = pool.makeClass(className);
		Class<?> rawType = Types.raw(altered.opType());

		// Add implemented interface
		CtClass jasOpType = pool.get(rawType.getName());
		cc.addInterface(jasOpType);

		// Add input focuser fields
		generateNFields(pool, cc, "inputProcessor", inputProcessors.size());

		// Add output simplifier field
		generateNFields(pool, cc, "outputProcessor", 1);

		// Add Op field
		CtField opField = createOpField(pool, cc, rawType, "op");
		cc.addField(opField);

		// Add copy Op field iff not pure output
		if (outputCopier != null) {
			CtField copyOpField = createOpField(pool, cc, Computers.Arity1.class,
				"copyOp");
			cc.addField(copyOpField);
		}

		// Add constructor to take the Simplifiers, as well as the original op.
		CtConstructor constructor = CtNewConstructor.make(createConstructor(cc, //
			altered, //
			inputProcessors.size(), //
			outputCopier != null //
		), cc);
		cc.addConstructor(constructor);

		// add functional interface method
		Class<?> opType = Types.raw(altered.opType());
		int ioIndex = Infos.IOIndex(altered);
		CtMethod functionalMethod = CtNewMethod.make(createFunctionalMethod( //
			opType, //
			ioIndex, //
			altered, //
			inputProcessors, //
			//
			outputCopier //
		), cc);
		cc.addMethod(functionalMethod);
		return cc;
	}

	private static void generateNFields(ClassPool pool, CtClass cc, String base,
		int numFields) throws NotFoundException, CannotCompileException
	{
		for (int i = 0; i < numFields; i++) {
			CtField f = createMutatorField(pool, cc, base + i);
			cc.addField(f);
		}
	}

	private static CtField createMutatorField(ClassPool pool, CtClass cc,
		String name) throws NotFoundException, CannotCompileException
	{
		CtClass fType = pool.get(Function.class.getName());
		CtField f = new CtField(fType, name, cc);
		f.setModifiers(Modifier.PRIVATE + Modifier.FINAL);
		return f;
	}

	private static CtField createOpField(ClassPool pool, CtClass cc,
		Class<?> opType, String fieldName) throws NotFoundException,
		CannotCompileException
	{
		CtClass fType = pool.get(opType.getName());
		CtField f = new CtField(fType, fieldName, cc);
		f.setModifiers(Modifier.PRIVATE + Modifier.FINAL);
		return f;
	}

	private static String createConstructor(CtClass cc, OpInfo altered, //
		int numInputProcessors, //
		boolean hasCopyOp //
	) {
		StringBuilder sb = new StringBuilder();
		// constructor signature
		sb.append("public ").append(cc.getSimpleName()).append("(");
		Class<?> depClass = Function.class;
		// input focusers
		for (int i = 0; i < numInputProcessors; i++) {
			sb.append(depClass.getName()).append(" inputProcessor").append(i);
			sb.append(",");
		}
		// output simplifier
		sb.append(depClass.getName()).append(" outputProcessor0");
		sb.append(",");
		// op
		sb.append(" ").append(Types.raw(altered.opType()).getName()).append(" op");
		// copy op
		if (hasCopyOp) {
			Class<?> copyOpClass = Computers.Arity1.class;
			sb.append(", ").append(copyOpClass.getName()).append(" copyOp");
		}
		sb.append(") {");

		// assign dependencies to field
		for (int i = 0; i < numInputProcessors; i++) {
			sb.append("this.inputProcessor") //
				.append(i) //
				.append(" = inputProcessor") //
				.append(i) //
				.append(";");
		}
		sb.append("this.outputProcessor0" + " = outputProcessor0" + ";");
		sb.append("this.op = op;");
		if (hasCopyOp) {
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
	 * {@link SimplifiedOpRequest}</li>
	 * <li>Focus the simplified inputs using the {@link Function}s provided by the
	 * {@link SimplifiedOpInfo}</li>
	 * <li>Call the {@code Op} using the focused inputs.</li>
	 * </ol>
	 * <b>NB</b> The Javassist compiler
	 * <a href="https://www.javassist.org/tutorial/tutorial3.html#generics">does
	 * not fully support generics</a>, so we must ensure that the types are raw.
	 * At compile time, the raw types are equivalent to the generic types, so this
	 * should not pose any issues.
	 *
	 * @return a {@link String} that can be used by
	 *         {@link CtMethod#make(String, CtClass)} to generate the functional
	 *         method of the simplified Op
	 */
	private static String createFunctionalMethod(Class<?> opType, int ioIndex,
		OpInfo altered, List<Function<?, ?>> inputProcessors,
		Computers.Arity1<?, ?> outputCopier)
	{
		StringBuilder sb = new StringBuilder();

		// determine the name of the functional method
		Method m = FunctionalInterfaces.functionalMethodOf(opType);
		// determine the name of the output:
		String opOutput = "originalOut";
		if (ioIndex > -1) {
			opOutput = "processed" + ioIndex;
		}

		// -- signature -- //
		sb.append(generateSignature(m));

		// -- body --//

		// preprocessing
		sb.append(" {");
		sb.append(fMethodPreprocessing(inputProcessors));

		// processing
		sb.append(fMethodProcessing(m, opOutput, ioIndex, altered));

		// postprocessing
		sb.append(fMethodPostprocessing( //
			opOutput, //
			ioIndex, //
			//
			outputCopier //
		));

		// if pure output, return it
		if (ioIndex == -1) {
			sb.append("return processedOutput;");
		}
		sb.append("}");
		return sb.toString();
	}

	private static String generateSignature(Method m) {
		StringBuilder sb = new StringBuilder();
		String methodName = m.getName();

		// method modifiers
		boolean isVoid = m.getReturnType() == void.class;
		sb.append("public ") //
			.append(isVoid ? "void" : "Object") //
			.append(" ") //
			.append(methodName) //
			.append("(");

		int inputs = m.getParameterCount();
		for (int i = 0; i < inputs; i++) {
			sb.append(" Object in").append(i);
			if (i < inputs - 1) sb.append(",");
		}

		sb.append(" )");

		return sb.toString();
	}

	private static String fMethodProcessing(Method m, String opOutput,
		int ioIndex, OpInfo altered)
	{
		StringBuilder sb = new StringBuilder();
		// declare / assign Op's original output
		if (ioIndex == -1) {
			sb.append("Object ").append(opOutput).append(" = ");
		}
		// call the op
		sb.append("op.").append(m.getName()).append("(");
		int numInputs = altered.inputTypes().size();
		for (int i = 0; i < numInputs; i++) {
			sb.append(" processed").append(i);
			if (i + 1 < numInputs) sb.append(",");
		}
		sb.append(");");
		return sb.toString();
	}

	private static String fMethodPostprocessing(String opOutput, int ioIndex,
		Computers.Arity1<?, ?> outputCopier)
	{
		StringBuilder sb = new StringBuilder();

		// simplify output
		sb.append("Object processedOutput = outputProcessor0.apply(").append(
			opOutput).append(");");

		// call copy op iff it exists
		if (outputCopier != null) {
			String originalIOArg = "in" + ioIndex;
			sb.append("copyOp.compute(processedOutput, ") //
				.append(originalIOArg) //
				.append(");");
		}

		return sb.toString();
	}

	private static String fMethodPreprocessing(
		List<Function<?, ?>> inputProcessors)
	{
		StringBuilder sb = new StringBuilder();

		// focus all inputs
		for (int i = 0; i < inputProcessors.size(); i++) {
			sb.append("Object processed") //
				.append(i) //
				.append(" = inputProcessor") //
				.append(i) //
				.append(".apply(in") //
				.append(i) //
				.append(");");
		}

		return sb.toString();
	}

	/**
	 * {@link Class}es of array types return "[]" when
	 * {@link Class#getSimpleName()} is called. Those characters are invalid in a
	 * class name, so we exchange them for the suffix "_Arr".
	 *
	 * @param t - the {@link Type} for which we need a name
	 * @return - a name that is legal as part of a class name.
	 */
	static String getClassName(Type t) {
		Class<?> clazz = Types.raw(t);
		String className = clazz.getSimpleName();
		if (className.chars().allMatch(Character::isJavaIdentifierPart))
			return className;
		if (clazz.isArray()) return clazz.getComponentType().getSimpleName() +
			"_Arr";
		return className;
	}
}
