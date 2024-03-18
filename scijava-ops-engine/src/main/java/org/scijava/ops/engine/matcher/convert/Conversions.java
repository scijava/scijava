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

package org.scijava.ops.engine.matcher.convert;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Container;
import org.scijava.function.Mutable;
import org.scijava.ops.api.*;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.engine.util.internal.AnnotationUtils;
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

public final class Conversions {

	private Conversions() {
		// Prevent instantiation of static utility class
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
	public static int mutableIndexOf(Class<?> c) {
		Method fMethod = FunctionalInterfaces.functionalMethodOf(c);
		for (int i = 0; i < fMethod.getParameterCount(); i++) {
			if (AnnotationUtils.getMethodParameterAnnotation(fMethod, i,
				Mutable.class) != null) return i;
			if (AnnotationUtils.getMethodParameterAnnotation(fMethod, i,
				Container.class) != null) return i;
		}
		return -1;
	}

	public static List<Type> inputTypesAgainst(OpInfo info, Class<?> against) {
		List<Type> types = info.inputTypes();
		int fromIoIndex = ioIndex(info.opType());
		int toIoIndex = ioIndex(against);
		if (fromIoIndex != toIoIndex) {
			types.add(toIoIndex, types.remove(fromIoIndex));
		}
		return types;
	}

	private static int ioIndex(Type cls) {
		var method = FunctionalInterfaces.functionalMethodOf(cls);
		var params = method.getAnnotatedParameterTypes();
		for (int i = 0; i < params.length; i++) {
			if (params[i].isAnnotationPresent(Container.class)) return i;
			if (params[i].isAnnotationPresent(Mutable.class)) return i;
		}
		return -1;
	}

	public static Optional<ConvertedOpInfo> tryConvert(OpEnvironment env,
		OpInfo info, OpRequest request)
	{
		try {
			return Optional.ofNullable(convert(env, info, request));
		}
		catch (Throwable t) {
			return Optional.empty();
		}
	}

	private static ConvertedOpInfo convert(OpEnvironment env, OpInfo info,
		OpRequest request)
	{
		// fail fast if clearly inconvertible
		Type opType = info.opType();
		Type reqType = request.getType();
		if (!Types.isAssignable(Types.raw(opType), Types.raw(reqType))) {
			return null;
		}

		Hints h = new Hints( //
			BaseOpHints.Adaptation.FORBIDDEN, //
			BaseOpHints.Conversion.FORBIDDEN, //
			BaseOpHints.History.IGNORE //
		);
		final RichOp<Function<?, ?>> identity = identityOp(env);
		final Map<TypeVariable<?>, Type> vars = new HashMap<>();
		// Find input converters
		Type[] fromArgs = request.getArgs();
		List<Type> toArgs = inputTypesAgainst(info, Types.raw(reqType));
		List<RichOp<Function<?, ?>>> preConverters = new ArrayList<>();
		for (int i = 0; i < fromArgs.length; i++) {
			var opt = findConverter(fromArgs[i], toArgs.get(i), vars, env, h);
			preConverters.add(opt.orElse(identity));
		}

		// Find output converter
		Optional<ConvertedOpInfo> opt;
		// Attempt 1: Functions
		opt = postprocessFunction(info, request, preConverters, vars, env, h);
		if (opt.isPresent()) {
			return opt.get();
		}
		// Attempt 2: Computer with identity mutable output
		opt = postprocessIdentity(info, request, preConverters, identity, vars, env,
			h);
		if (opt.isPresent()) {
			return opt.get();
		}
		// Attempt 3: Computer with convert and copy of mutable output
		opt = postprocessConvertAndCopy(info, request, preConverters, vars, env, h);
		if (opt.isPresent()) {
			return opt.get();
		}
		// Attempt 4: Computer with just copy of mutable output
		opt = postprocessCopy(info, request, preConverters, vars, env, h);
		return opt.orElse(null);
	}

	/**
	 * Helper that completes the conversion of {@code info}, which refers to a
	 * {@code Function} op.
	 *
	 * @param info the original {@link OpInfo}
	 * @param request the original {@link OpRequest}
	 * @param preConverters the {@link List} of {@link RichOp}s responsible for
	 *          converting the inputs to the {@link ConvertedOpInfo}
	 * @param vars the {@link Map} of {@link TypeVariable}s to {@link Type}s
	 *          necessary for the conversion between {@code info} and
	 *          {@code request}
	 * @param env the {@link OpEnvironment} used to match Ops necessary to create
	 *          the {@code ConvertedOpInfo}
	 * @param hints the {@link Hints} used during Op matching calls to {@code env}
	 * @return a {@link ConvertedOpInfo}, aligning {@code info} to {@code request}
	 *         if that is possible
	 */
	private static Optional<ConvertedOpInfo> postprocessFunction(OpInfo info,
		OpRequest request, List<RichOp<Function<?, ?>>> preConverters,
		Map<TypeVariable<?>, Type> vars, OpEnvironment env, Hints hints)
	{
		// This procedure only applies to functions
		int ioIndex = ioIndex(request.getType());
		if (ioIndex > -1) {
			return Optional.empty();
		}
		// for functions, we only need a postconverter
		var fromOut = Nil.of(Types.mapVarToTypes(info.outputType(), vars));
		var toOut = Nil.of(request.getOutType());
		RichOp<Function<?, ?>> postConverter = Ops.rich(env.unary("engine.convert",
			hints).inType(fromOut).outType(toOut).function());
		return Optional.of(new ConvertedOpInfo( //
			info, //
			request.getType(), //
			preConverters, //
			postConverter, //
			null, //
			env //
		));
	}

	/**
	 * Helper that completes the conversion of {@code info}, which refers to an Op
	 * with a {@link Mutable} output. This particular pathway of
	 * {@link ConvertedOpInfo} creation provides convenience when the
	 * {@link Mutable} output was not actually converted, but was instead "edited"
	 * with {@code identity}.
	 *
	 * @param info the original {@link OpInfo}
	 * @param request the original {@link OpRequest}
	 * @param preConverters the {@link List} of {@link RichOp}s responsible for
	 *          converting the inputs to the {@link ConvertedOpInfo}
	 * @param identity an Op that simply returns the input value
	 * @param vars the {@link Map} of {@link TypeVariable}s to {@link Type}s
	 *          necessary for the conversion between {@code info} and
	 *          {@code request}
	 * @param env the {@link OpEnvironment} used to match Ops necessary to create
	 *          the {@code ConvertedOpInfo}
	 * @param hints the {@link Hints} used during Op matching calls to {@code env}
	 * @return a {@link ConvertedOpInfo}, aligning {@code info} to {@code request}
	 *         if that is possible
	 */
	private static Optional<ConvertedOpInfo> postprocessIdentity(OpInfo info,
		OpRequest request, List<RichOp<Function<?, ?>>> preConverters,
		RichOp<Function<?, ?>> identity, Map<TypeVariable<?>, Type> vars,
		OpEnvironment env, Hints hints)
	{
		// This procedure only applies to Ops with mutable outputs
		int ioIndex = ioIndex(request.getType());
		if (ioIndex == -1) {
			return Optional.empty();
		}
		// And only applies when the mutable index was "converted" with identity
		if (preConverters.get(ioIndex) == identity) {
			// In this case, we need neither a postprocessor nor a copier,
			// because the mutable output was directly edited.
			return Optional.of(new ConvertedOpInfo( //
				info, //
				request.getType(), //
				preConverters, //
				identity, //
				null, //
				env //
			));
		}
		return Optional.empty();
	}

	/**
	 * Helper that completes the conversion of {@code info}, which refers to an Op
	 * with a {@link Mutable} output. This particular pathway of
	 * {@link ConvertedOpInfo} creation relies on <b>both</b> a
	 * {@code engine.convert} Op and a {@code engine.copy} Op to directly copy the
	 * output of the underlying Op into the pre-allocated user output.
	 *
	 * @param info the original {@link OpInfo}
	 * @param request the original {@link OpRequest}
	 * @param preConverters the {@link List} of {@link RichOp}s responsible for
	 *          converting the inputs to the {@link ConvertedOpInfo}
	 * @param vars the {@link Map} of {@link TypeVariable}s to {@link Type}s
	 *          necessary for the conversion between {@code info} and
	 *          {@code request}
	 * @param env the {@link OpEnvironment} used to match Ops necessary to create
	 *          the {@code ConvertedOpInfo}
	 * @param hints the {@link Hints} used during Op matching calls to {@code env}
	 * @return a {@link ConvertedOpInfo}, aligning {@code info} to {@code request}
	 *         if that is possible
	 */
	private static Optional<ConvertedOpInfo> postprocessConvertAndCopy(
		OpInfo info, OpRequest request, List<RichOp<Function<?, ?>>> preConverters,
		Map<TypeVariable<?>, Type> vars, OpEnvironment env, Hints hints)
	{
		// This procedure only applies to Ops with mutable outputs
		int ioIndex = ioIndex(request.getType());
		if (ioIndex == -1) {
			return Optional.empty();
		}
		try {
			var fromOut = Nil.of(Types.mapVarToTypes(info.outputType(), vars));
			var toOut = Nil.of(request.getOutType());
			// First, we convert the output to the type the user requested
			RichOp<Function<?, ?>> postConverter = Ops.rich( //
				env.unary("engine.convert", hints) //
					.inType(fromOut) //
					.outType(toOut) //
					.function() //
			);
			// Then, we copy the converted output back into the user's object.
			RichOp<Computers.Arity1<?, ?>> copyOp = Ops.rich(env.unary("engine.copy",
				hints) //
				.inType(toOut) //
				.outType(toOut) //
				.computer() //
			);
			return Optional.of(new ConvertedOpInfo( //
				info, //
				request.getType(), //
				preConverters, //
				postConverter, //
				copyOp, //
				env //
			));
		}
		catch (OpMatchingException e) {
			return Optional.empty();
		}
	}

	/**
	 * Helper that completes the conversion of {@code info}, which refers to an Op
	 * with a {@link Mutable} output. This particular pathway of
	 * {@link ConvertedOpInfo} creation relies on an {@code engine.copy} Op to
	 * directly copy the output of the underlying Op into the pre-allocated user
	 * output.
	 *
	 * @param info the original {@link OpInfo}
	 * @param request the original {@link OpRequest}
	 * @param preConverters the {@link List} of {@link RichOp}s responsible for
	 *          converting the inputs to the {@link ConvertedOpInfo}
	 * @param vars the {@link Map} of {@link TypeVariable}s to {@link Type}s
	 *          necessary for the conversion between {@code info} and
	 *          {@code request}
	 * @param env the {@link OpEnvironment} used to match Ops necessary to create
	 *          the {@code ConvertedOpInfo}
	 * @param hints the {@link Hints} used during Op matching calls to {@code env}
	 * @return a {@link ConvertedOpInfo}, aligning {@code info} to {@code request}
	 *         if that is possible
	 */
	private static Optional<ConvertedOpInfo> postprocessCopy(OpInfo info,
		OpRequest request, List<RichOp<Function<?, ?>>> preConverters,
		Map<TypeVariable<?>, Type> vars, OpEnvironment env, Hints hints)
	{
		// This procedure only applies to Ops with mutable outputs
		int ioIndex = ioIndex(request.getType());
		if (ioIndex == -1) {
			return Optional.empty();
		}
		try {
			var fromOut = Nil.of(Types.mapVarToTypes(info.outputType(), vars));
			var toOut = Nil.of(request.getOutType());
			// This is really just a placeholder.
			RichOp<Function<?, ?>> postConverter = Ops.rich( //
				env.unary("engine.identity", hints) //
					.inType(fromOut) //
					.outType(fromOut) //
					.function() //
			);
			// We try to copy the output directly from the op output into the user's
			// object
			RichOp<Computers.Arity1<?, ?>> copyOp = Ops.rich( //
				env.unary("engine.copy", hints) //
					.inType(fromOut) //
					.outType(toOut) //
					.computer() //
			);
			return Optional.of(new ConvertedOpInfo( //
				info, //
				request.getType(), //
				preConverters, //
				postConverter, //
				copyOp, //
				env //
			));
		}
		catch (OpMatchingException e) {
			return Optional.empty();
		}
	}

	/**
	 * Helper method to find a converter from a user argument of type {@code from}
	 * to an Op parameter of type {@code to}
	 *
	 * @param from the {@link Type} of a user argument
	 * @param to the {@link Type} of an Op parameter
	 * @param vars the {@link Map} of {@link TypeVariable}s to {@code Type}s
	 *          created to this point, throughout the conversion
	 * @param env the {@link OpEnvironment} used for matching converter Ops
	 * @param hints the {@link Hints} to use in matching
	 * @return a rich converter Op, if it is both necessary and can be found
	 */
	private static Optional<RichOp<Function<?, ?>>> findConverter(Type from,
		Type to, Map<TypeVariable<?>, Type> vars, OpEnvironment env, Hints hints)
	{
		// If the request argument can be assigned to the info parameter directly,
		// we don't need to call a preconverter
		if (Types.isAssignable(from, to)) {
			return Optional.empty();
		}
		// If direct assignment fails, we need a preconverter
		var source = Nil.of(from);
		// If the op parameter type has type variables that have been mapped
		// already, substitute those mappings in.
		var preDest = Types.mapVarToTypes(to, vars);
		// Remaining type variables are unlikely to be matched directly. We thus
		// replace them with wildcards, bounded by the same bounds.
		var dest = wildcardVacuousTypeVars(preDest);
		// match the Op
		var op = env.unary("engine.convert", hints) //
			.inType(source) //
			.outType(dest) //
			.function();
		var rich = Ops.rich(op);
		// The resulting Op can give us further information about type variable
		// mappings - let's find them
		resolveTypes(from, preDest, rich, vars);
		return Optional.of(Ops.rich(op));
	}

	private static void resolveTypes(Type source, Type dest,
		RichOp<? extends Function<?, ?>> rich, Map<TypeVariable<?>, Type> vars)
	{
		Type reqType = Types.parameterize(Function.class, new Type[] { source,
			dest });
		Type infoType = rich.instance().getType();
		GenericAssignability.inferTypeVariables(new Type[] { reqType }, new Type[] {
			infoType }, vars);
	}

	/**
	 * Suppose we are trying to find a converter to convert a {@code double[]}
	 * user input into a {@code List<N extends Number>} op input. <b>It is very
	 * unlikely that we have an Op {@code Function<double, List<N>>} because that
	 * {@code N} is vacuous</b>. What is much more likely, and workable from the
	 * point of the converted Op, is to find an Op that returns a list of
	 * {@link Double}s, {@link Float}s, etc. We can specify this in the matching
	 * constraints by replacing all type variables with wildcards bounded by the
	 * bounds of the type variable.
	 *
	 * @param t a {@link Type} that contains some "vacuous" {@link TypeVariable}s.
	 * @return a copy of {@code t} but with all vacuous type variables replaced
	 *         with wildcards
	 */
	private static Nil<?> wildcardVacuousTypeVars(final Type t) {
		Type[] typeParams = Types.typeParamsAgainstClass(t, Types.raw(t));
		var vars = new HashMap<TypeVariable<?>, Type>();
		for (Type typeParam : typeParams) {
			if (typeParam instanceof TypeVariable<?>) {
				// Get the type variable
				TypeVariable<?> from = (TypeVariable<?>) typeParam;
				// Create a wildcard type with the type variable bounds
				Type to = Types.wildcard(from.getBounds(), null);
				vars.put(from, to);
			}
		}
		return Nil.of(Types.mapVarToTypes(t, vars));
	}

	private static <T> RichOp<Function<?, ?>> identityOp(OpEnvironment env) {
		Nil<T> t = new Nil<>() {};
		var op = env.unary("engine.identity") //
			.inType(t) //
			.outType(t) //
			.function();
		return Ops.rich(op);
	}

	/**
	 * Helper method that finds the {@code engine.copy} Op needed for a
	 * {@link ConvertedOpInfo}
	 *
	 * @param env the {@link OpEnvironment} containing {@code engine.copy} Ops.
	 * @param info the original {@link OpInfo}.
	 * @param request the {@link OpRequest}
	 * @param hints {@link Hints} to be used in matching the copy Op.
	 * @return a {@code engine.copy} Op
	 */
	private static RichOp<Computers.Arity1<?, ?>> getCopyOp( //
		OpEnvironment env, //
		OpInfo info, //
		OpRequest request, Hints hints //
	) {
		int ioIndex = Conversions.mutableIndexOf(Types.raw(info.opType()));
		// If IO index is -1, output is returned - no need to copy.
		if (ioIndex == -1) {
			return null;
		}
		// Match a copier
		var outType = Nil.of(request.getOutType());
		return Ops.rich(env.unary("engine.copy", hints) //
			.inType(outType) //
			.outType(outType) //
			.computer());
	}

	/**
	 * Creates a Converted Op class. This class:
	 * <ul>
	 * <li>is of the same functional type as the given Op</li>
	 * <li>has type arguments that are of the converted form of the type arguments
	 * of the given Op (these arguments are dictated by the
	 * {@code preconverters}s.</li>
	 * <li>
	 *
	 * @param originalOp - the Op that will be converted
	 * @return a wrapper of {@code originalOp} taking arguments that are then
	 *         mutated to satisfy {@code originalOp}, producing outputs that are
	 *         then mutated to satisfy the desired output of the wrapper.
	 * @throws Throwable in the case of an error
	 */
	public static Object javassistOp( //
		Object originalOp, //
		OpInfo alteredInfo, //
		List<Function<?, ?>> preconverters, //
		Function<?, ?> postconverter, //
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
			CtClass cc = generateConvertedClass( //
				pool, //
				className, //
				alteredInfo, //
				preconverters, //
				//
				copyOp //
			);
			c = cc.toClass(MethodHandles.lookup());
		}

		// Return Op instance
		return c.getDeclaredConstructor(constructorClasses(alteredInfo,
			copyOp != null)).newInstance(constructorArgs(preconverters, postconverter,
				copyOp, originalOp));
	}

	private static Class<?>[] constructorClasses( //
		OpInfo originalInfo, //
		boolean addCopyOp //
	) {
		// there are 2*numInputs input mutators, 2 output mutators
		int numMutators = originalInfo.inputTypes().size() + 1;
		// original Op plus a output copier if applicable
		int numOps = addCopyOp ? 2 : 1;
		Class<?>[] args = new Class<?>[numMutators + numOps];
		for (int i = 0; i < numMutators; i++)
			args[i] = Function.class;
		args[args.length - numOps] = Types.raw(originalInfo.opType());
		if (addCopyOp) args[args.length - 1] = Computers.Arity1.class;
		return args;

	}

	private static Object[] constructorArgs( //
		List<Function<?, ?>> preconverters, //
		Function<?, ?> postconverter, //
		Computers.Arity1<?, ?> outputCopier, //
		Object op //
	) {
		List<Object> args = new ArrayList<>(preconverters);
		args.add(postconverter);
		args.add(op);
		if (outputCopier != null) {
			args.add(outputCopier);
		}
		return args.toArray();
	}

	// TODO: consider correctness
	private static String formClassName(OpInfo altered) {
		// package name - required to be this package for the Lookup to work
		String packageName = Conversions.class.getPackageName();
		StringBuilder sb = new StringBuilder(packageName + ".");

		// class name
		String implementationName = altered.implementationName();
		String className = implementationName.replaceAll("[^a-zA-Z0-9\\-]", "_");
		if (className.chars().anyMatch(c -> !Character.isJavaIdentifierPart(c)))
			throw new IllegalArgumentException(className +
				" is not a valid class name!");

		sb.append(className);
		return sb.toString();
	}

	private static CtClass generateConvertedClass(ClassPool pool, //
		String className, //
		OpInfo altered, //
		List<Function<?, ?>> preconverters, //
		Computers.Arity1<?, ?> outputCopier //
	) throws Throwable {
		CtClass cc = pool.makeClass(className);
		Class<?> rawType = Types.raw(altered.opType());

		// Add implemented interface
		CtClass jasOpType = pool.get(rawType.getName());
		cc.addInterface(jasOpType);

		// Add preconverter fields
		generateNFields(pool, cc, "preconverter", preconverters.size());

		// Add postconverter field
		generateNFields(pool, cc, "postconverter", 1);

		// Add Op field
		CtField opField = createOpField(pool, cc, rawType, "op");
		cc.addField(opField);

		// Add copy Op field iff not pure output
		if (outputCopier != null) {
			CtField copyOpField = createOpField(pool, cc, Computers.Arity1.class,
				"copyOp");
			cc.addField(copyOpField);
		}

		// Add constructor to take the converters, as well as the original op.
		CtConstructor constructor = CtNewConstructor.make(createConstructor(cc, //
			altered, //
			preconverters.size(), //
			outputCopier != null //
		), cc);
		cc.addConstructor(constructor);

		// add functional interface method
		Class<?> opType = Types.raw(altered.opType());
		int ioIndex = ioIndex(opType);
		CtMethod functionalMethod = CtNewMethod.make(createFunctionalMethod( //
			opType, //
			ioIndex, //
			altered, //
			preconverters, //
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
		// preconverter
		for (int i = 0; i < numInputProcessors; i++) {
			sb.append(depClass.getName()).append(" preconverter").append(i);
			sb.append(",");
		}
		// postconverter
		sb.append(depClass.getName()).append(" postconverter0");
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
			sb.append("this.preconverter") //
				.append(i) //
				.append(" = preconverter") //
				.append(i) //
				.append(";");
		}
		sb.append("this.postconverter0" + " = postconverter0" + ";");
		sb.append("this.op = op;");
		if (hasCopyOp) {
			sb.append("this.copyOp = copyOp;");
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Creates the functional method of a converted Op. This functional method
	 * must:
	 * <ol>
	 * <li>Preconvert all Op inputs.</li>
	 * <li>Call the {@code Op} using the converted inputs.</li>
	 * <li>Postconvert the Op output.</li>
	 * </ol>
	 * <b>NB</b> The Javassist compiler
	 * <a href="https://www.javassist.org/tutorial/tutorial3.html#generics">does
	 * not fully support generics</a>, so we must ensure that the types are raw.
	 * At compile time, the raw types are equivalent to the generic types, so this
	 * should not pose any issues.
	 *
	 * @return a {@link String} that can be used by
	 *         {@link CtMethod#make(String, CtClass)} to generate the functional
	 *         method of the converted Op
	 */
	private static String createFunctionalMethod(Class<?> opType, int ioIndex,
		OpInfo altered, List<Function<?, ?>> preconverters,
		Computers.Arity1<?, ?> copier)
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
		sb.append(fMethodPreprocessing(preconverters));

		// processing
		sb.append(fMethodProcessing(m, opOutput, ioIndex, altered));

		// postprocessing
		sb.append(fMethodPostprocessing( //
			opOutput, //
			ioIndex, //
			copier //
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

		// postconvert output
		sb.append("Object processedOutput = postconverter0.apply(").append(opOutput)
			.append(");");

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
		List<Function<?, ?>> preconverter)
	{
		StringBuilder sb = new StringBuilder();

		// focus all inputs
		for (int i = 0; i < preconverter.size(); i++) {
			sb.append("Object processed") //
				.append(i) //
				.append(" = preconverter") //
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
