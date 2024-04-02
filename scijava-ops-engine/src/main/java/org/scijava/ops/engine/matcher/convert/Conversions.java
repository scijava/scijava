/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

import org.scijava.function.Computers;
import org.scijava.function.Container;
import org.scijava.function.Mutable;
import org.scijava.ops.api.*;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.types.Any;
import org.scijava.types.Nil;
import org.scijava.types.Types;
import org.scijava.types.inference.FunctionalInterfaces;
import org.scijava.types.inference.GenericAssignability;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.Function;

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
	 * @param t - the {@link Type} extending a {@link FunctionalInterface}
	 * @return the index of the mutable argument (or -1 iff the output is
	 *         returned).
	 */
	public static int mutableIndexOf(Type t) {
		var method = FunctionalInterfaces.functionalMethodOf(t);
		var params = method.getAnnotatedParameterTypes();
		for (int i = 0; i < params.length; i++) {
			if (params[i].isAnnotationPresent(Container.class)) return i;
			if (params[i].isAnnotationPresent(Mutable.class)) return i;
		}
		return -1;
	}

	/**
	 * Tries to convert an {@link OpInfo}, such that its parameter types match the
	 * argument types of a given {@link OpRequest}.
	 *
	 * @param env the {@link OpEnvironment} used for creating conversions
	 * @param info the {@link OpInfo} to convert
	 * @param request the {@link OpRequest} defining the needed I/O types.
	 * @return a {@link ConvertedOpInfo}, if the conversion is possible.
	 */
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

	/**
	 * Converts an {@link OpInfo}, such that its parameter types match the
	 * argument types of a given {@link OpRequest}.
	 *
	 * @param env the {@link OpEnvironment} used for creating conversions
	 * @param info the {@link OpInfo} to convert
	 * @param request the {@link OpRequest} defining the needed I/O types.
	 * @return a {@link ConvertedOpInfo} that incorporates conversions for each
	 *         I/O parameter of {@code info}
	 */
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
		final Map<TypeVariable<?>, Type> vars = new HashMap<>();
		// Find input converters
		Type[] fromArgs = request.getArgs();
		List<Type> toArgs = inputTypesAgainst(info, Types.raw(reqType));
		List<RichOp<Function<?, ?>>> preConverters = new ArrayList<>();
		for (int i = 0; i < fromArgs.length; i++) {
			var opt = findConverter(fromArgs[i], toArgs.get(i), vars, env, h);
			preConverters.add(opt);
		}

		// Find output converter
		Optional<ConvertedOpInfo> opt;
		// Attempt 1: Functions
		opt = postprocessFunction(info, request, preConverters, vars, env, h);
		if (opt.isPresent()) {
			return opt.get();
		}
		// Attempt 2: Computer with identity mutable output
		opt = postprocessIdentity(info, request, preConverters, vars, env);
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
	 * Derives the input types of {@link OpInfo} {@code info}, if it were to be
	 * matched as an instance of {@link Class} {@code against}. The primary use
	 * case for this method is when {@code info} implements some subclass of
	 * {@code against} that mutates the parameters.
	 *
	 * @param info the {@link OpInfo} whose input types should be inferred
	 * @param against the {@link Class} that has an input argument ordering
	 * @return the input types
	 */
	private static List<Type> inputTypesAgainst(OpInfo info, Class<?> against) {
		List<Type> types = info.inputTypes();
		int fromIoIndex = mutableIndexOf(info.opType());
		int toIoIndex = mutableIndexOf(against);
		if (fromIoIndex != toIoIndex) {
			types.add(toIoIndex, types.remove(fromIoIndex));
		}
		return types;
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
		int ioIndex = mutableIndexOf(request.getType());
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
			Arrays.asList(request.getArgs()), //
			postConverter, //
			request.getOutType(), //
			null, //
			env, //
			vars //
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
	 * @param env the {@link OpEnvironment} used to match Ops necessary to create
	 *          the {@code ConvertedOpInfo}
	 * @return a {@link ConvertedOpInfo}, aligning {@code info} to {@code request}
	 *         if that is possible
	 */
	private static Optional<ConvertedOpInfo> postprocessIdentity(OpInfo info,
		OpRequest request, List<RichOp<Function<?, ?>>> preConverters,
		Map<TypeVariable<?>, Type> vars, OpEnvironment env)
	{
		// This procedure only applies to Ops with mutable outputs
		int ioIndex = mutableIndexOf(request.getType());
		if (ioIndex == -1) {
			return Optional.empty();
		}
		// And only applies when the mutable index was "converted" with identity
		if (Ops.info(preConverters.get(ioIndex)).names().contains(
			"engine.identity"))
		{
			// In this case, we need neither a postprocessor nor a copier,
			// because the mutable output was directly edited.
			return Optional.of(new ConvertedOpInfo( //
				info, //
				request.getType(), //
				preConverters, //
				Arrays.asList(request.getArgs()), //
				null, //
				request.getOutType(), //
				null, //
				env, //
				vars //
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
		int ioIndex = mutableIndexOf(request.getType());
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
				Arrays.asList(request.getArgs()), //
				postConverter, //
				request.getOutType(), //
				copyOp, //
				env, //
				vars //
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
		int ioIndex = mutableIndexOf(request.getType());
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
				Arrays.asList(request.getArgs()), //
				postConverter, //
				request.getOutType(), //
				copyOp, //
				env, //
				vars //
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
	private static RichOp<Function<?, ?>> findConverter(Type from, Type to,
		Map<TypeVariable<?>, Type> vars, OpEnvironment env, Hints hints)
	{
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
		return Ops.rich(op);
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
		if (t instanceof TypeVariable<?>) {
			TypeVariable<?> tv = (TypeVariable<?>) t;
			return Nil.of(new Any(tv.getBounds()));
		}
		var vars = new HashMap<TypeVariable<?>, Type>();
		for (Type typeParam : typeParams) {
			if (typeParam instanceof TypeVariable<?>) {
				// Get the type variable
				TypeVariable<?> from = (TypeVariable<?>) typeParam;
				// Create a wildcard type with the type variable bounds
				Type to = new Any(from.getBounds());
				vars.put(from, to);
			}
		}
		return Nil.of(Types.mapVarToTypes(t, vars));
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
