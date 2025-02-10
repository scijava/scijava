/*
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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

package org.scijava.ops.engine.matcher.impl;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.scijava.common3.Any;
import org.scijava.common3.Types;

public final class MatchingUtils {

	private MatchingUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Checks for raw assignability.
	 * <p>
	 * TODO: This method is not yet fully implemented.
	 *  The correct behavior should be as follows.
	 *  Suppose we have a generic typed method like:
	 * </p>
	 *
	 * <pre>
	 * public static &lt;N&gt; List&lt;N&gt; foo(N in) {
	 *   ...
	 * }
	 * </pre>
	 *
	 * This method should discern if the following assignments would be legal,
	 * possibly using predetermined {@link TypeVariable} assignments:
	 *
	 * <pre>
	 * List&lt;Integer&gt; listOfInts = foo(new Integer(0)) //legal
	 * List&lt;Number&gt; listOfNumbers = foo(new Integer(0)) //legal
	 * List&lt;? extends Number&gt; listOfBoundedWildcards = foo(new Integer(0)) //legal
	 * </pre>
	 *
	 * The corresponding calls to this method would be:
	 *
	 * <pre>
	 * Nil&lt;List&lt;N&gt;&gt; nilN = new Nil&lt;List&lt;N&gt;&gt;(){}
	 * Nil&lt;List&lt;Integer&gt;&gt; nilInteger = new Nil&lt;List&lt;Integer&gt;&gt;(){}
	 * Nil&lt;List&lt;Number&gt;&gt; nilNumber = new Nil&lt;List&lt;Number&gt;&gt;(){}
	 * Nil&lt;List&lt;? extends Number&gt;&gt; nilWildcardNumber = new Nil&lt;List&lt;? extends Number&gt;&gt;(){}
	 *
	 * checkGenericOutputsAssignability(nilN.type(), nilInteger.type(), ...)
	 * checkGenericOutputsAssignability(nilN.type(), nilNumber.type(), ...)
	 * checkGenericOutputsAssignability(nilN.type(), nilWildcardNumber.type(), ...)
	 * </pre>
	 *
	 * Using a map where N was already bound to Integer (N -> Integer.class). This
	 * method is useful for the following scenario: During ops matching, we first
	 * check if the arguments (inputs) of the requested op are applicable to the
	 * arguments of an Op candidate. During this process, possible type variables
	 * may be inferred. The can then be used with this method to find out if the
	 * outputs of the op candidate would be assignable to the output of the
	 * requested op.
	 *
	 * @param froms
	 * @param tos
	 * @param typeBounds
	 * @return the index {@code i} such that {@code from[i]} cannot be assigned to
	 *         {@code to[i]}, or {@code -1} iff {@code from[i]} can be assigned to
	 *         {@code to[i]} for all {@code 0 <= i < from.length}.
	 */
	static int checkGenericOutputsAssignability(Type[] froms, Type[] tos,
		HashMap<TypeVariable<?>, TypeVarInfo> typeBounds)
	{
		for (var i = 0; i < froms.length; i++) {
            var from = froms[i];
            var to = tos[i];

			if (Any.is(to)) continue;

			if (from instanceof TypeVariable) {
                var typeVarInfo = typeBounds.get(from);
				// HACK: we CAN assign, for example, a Function<Iterable<N>, O> to a
				// Function<Iterable<Integer>, Double>, because in this situation O is
				// not bounded to any other types. However isAssignable will fail,
				// since we cannot just cast Double to O without that required
				// knowledge that O can be fixed to Double. We get around this by
				// recording in `typeBounds` that our previously unbounded TypeVariable
				// `from` is now fixed to `to`, then simply assigning `from` to `to`,
				// since `from` only has one bound, being `to`.
				if (typeVarInfo == null) {
                    var fromTypeVar = (TypeVariable<?>) from;
                    var fromInfo = new TypeVarInfo(fromTypeVar);
					fromInfo.fixBounds(to, true);
					typeBounds.put(fromTypeVar, fromInfo);
					from = to;
				}
				// similar to the above, if we know that O is already bound to a Type,
				// and that Type is to, then we can assign this without any issues.
				else {
					if (typeVarInfo.allowType(to, true)) from = to;
				}
			}

			if (!Types.isAssignable(Types.raw(from), Types.raw(to))) return i;
		}
		return -1;
	}

	/**
	 * Discerns whether it would be legal to pass a sequence of references of the
	 * given source types to a method with parameters typed according to the
	 * specified sequence of destination types.
	 * <p>
	 * An example: suppose you have a method
	 * {@code <T extends Number> mergeLists(List<T> list1, List<T> list2)}. It is
	 * legal to pass two {@code List<Integer>} instances to the method, but
	 * illegal to pass a {@code List<Integer>} and {@code List<Double>} because
	 * {@code T} cannot be both {@code Integer} and {@code Double} simultaneously.
	 * </p>
	 *
	 * @param args
	 * @param params
	 * @return -1 if the args satisfy the params, otherwise the index of the arg
	 *         that does not satisfy its parameter.
	 */
	public static int isApplicable(final Type[] args, final Type[] params) {
		// create a HashMap to monitor the restrictions of the type variables.
		final var typeBounds = new HashMap<TypeVariable<?>, TypeVarInfo>();

		return isApplicable(args, params, typeBounds);
	}

	public static int isApplicable(final Type[] args, final Type[] params,
		final Map<TypeVariable<?>, TypeVarInfo> typeBounds)
	{
		if (args.length != params.length) {
			throw new IllegalArgumentException("src and dest lengths differ");
		}

		for (var i = 0; i < params.length; i++) {
            var arg = args[i];
            var param = params[i];

			// if arg is an Any, it must be applicable to param.
			if (isApplicableToRawTypes(arg, Any.class)) continue;

			// First, check raw type assignability.
			if (!isApplicableToRawTypes(arg, param)) return i;

			if (param instanceof ParameterizedType) {
				if (!isApplicableToParameterizedTypes(arg,
					(ParameterizedType) param, typeBounds)) return i;
			}
			else if (param instanceof TypeVariable) {
				if (!isApplicableToTypeVariable(arg,
					(TypeVariable<?>) param, typeBounds)) return i;
			}
			else if (param instanceof WildcardType) {
				if (!isApplicableToWildcardType(arg, (WildcardType) param)) return i;
			}
			else if (param instanceof GenericArrayType) {
				if (!isApplicableToGenericArrayType(arg, (GenericArrayType) param,
					typeBounds)) return i;
			}
		}
		return -1;
	}

	private static boolean isApplicableToRawTypes(
		final Type arg, final Type param)
	{
		if (Any.is(arg)) return true;
		final var srcClasses = Types.raws(arg);
		final var destClasses = Types.raws(param);
		for (final var destClass : destClasses) {
			final var first = srcClasses.stream()
				.filter(srcClass -> Types.isAssignable(srcClass, destClass))
				.findFirst();
			if (first.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private static boolean isApplicableToParameterizedTypes(
		final Type arg,
		final ParameterizedType param,
		final Map<TypeVariable<?>, TypeVarInfo> typeBounds
	) {
		if (arg instanceof Class) {
            var paramRaw = Types.raw(param);
			return Types.isAssignable(arg, paramRaw);
		}

		// get an array of the destination parameter types
        var destTypes = param.getActualTypeArguments();
        var srcTypes = new Type[destTypes.length];

		// get an array of the source argument types
        var superType = Types.superTypeOf(arg, Types.raw(param));
		if (!(superType instanceof ParameterizedType)) return false;
		srcTypes = ((ParameterizedType) superType).getActualTypeArguments();

		// Create a list to collect the indices of destination parameters that are
		// type vars. If a type vars is contain within a parameterized type if must
		// not be checked recursively in the last call of this method. If done, one
		// loses the information that the type var was contained in a parameterized
		// type. Hence, it will be handled like a normal type var which allow more
		// assignability regarding wildcards compared to type vars contained in
		// parameterized types.
		List<Integer> ignoredIndices = new ArrayList<>();
		// check to see if any of the Types of this ParameterizedType are
		// TypeVariables, if so restrict them to the type parameter of the argument.
		for (var i = 0; i < destTypes.length; i++) {
			final var destType = destTypes[i];
			if (destType instanceof TypeVariable<?>) {
				final var srcType = srcTypes[i];
				final var destTypeVar = (TypeVariable<?>) destType;
				if (Any.is(srcType)) continue;
				if (!isApplicableToTypeParameter(srcType, destTypeVar, typeBounds))
					return false;
				ignoredIndices.add(i);
			}
		}
		srcTypes = filterIndices(srcTypes, ignoredIndices);
		destTypes = filterIndices(destTypes, ignoredIndices);
		// recursively check these arrays
		return isApplicable(srcTypes, destTypes, typeBounds) == -1;
	}

	/**
	 * Determines whether or not the {@link Type} {@code arg} can satisfy the
	 * {@link TypeVariable} {@code param} given the preexisting limitations of
	 * {@code param}. If it is determined that this replacement is allowed the
	 * {@link TypeVariable} is then restricted to the {@link Type} of {@code arg}
	 *
	 * @param arg - a Type Parameter for a given {@link ParameterizedType}
	 * @param param - a {@link TypeVariable} that could potentially be replaced
	 *          with {@code arg}
	 * @param typeBounds - a {@link HashMap} containing the current restrictions
	 *          of the {@link TypeVariable}s
	 * @return {@code boolean} - true if the replacement of {@code param} with
	 *         {@code arg} is allowed.
	 */
	private static boolean isApplicableToTypeParameter(
		final Type arg,
		final TypeVariable<?> param,
		final Map<TypeVariable<?>, TypeVarInfo> typeBounds
	) {
		// add the type variable to the HashMap if it does not yet exist.
		if (!typeBounds.containsKey(param)) {
			typeBounds.put(param, new TypeVarInfo(param) {
				@Override
				public boolean allowType(Type type, boolean refuseWildcards) {
					// NB: Hardcode refuseWildcards to true. Necessary for TypeVariables
					// which were contained in ParameterizedTypes. Behavior tested by
					// TypesTest#testSatisfiesWildcardsInParameterizedType().
					return super.allowType(type, true);
				}
			});
		}
		// attempt to restrict the bounds of the type variable to the argument.
		// We call the fixBounds method the refuseWildcards flag to be true,
		// as the type variable was contained in a parameterized type. Hence,
		// the var is not applicable for any wildcards if it already allows
		// any type.
		if (!typeBounds.get(param).fixBounds(arg, true)) return false;

		// if the type variable refers to some parameterized type
		// (e.g. T extends List<?>), recurse on the bounds of param
		// that are ParameterizedTypes.
		return isApplicableToTypeVariableBounds(arg, param, typeBounds);
	}

	private static boolean isApplicableToTypeVariable(
		final Type arg,
		final TypeVariable<?> param,
		final Map<TypeVariable<?>, TypeVarInfo> typeBounds
	) {
		// if the TypeVariable is not already in the hashMap add it.
		if (!typeBounds.containsKey(param)) {
			typeBounds.put(param, new TypeVarInfo(param));
		}
		// check to make sure that arg is a viable replacement for the type
		// variable.
		// We call the fixBounds method the refuseWildcards flag to be false,
		// as the type variable was not contained in a parameterized type.
		// Hence, the var may be applicable to wildcards
		if (!typeBounds.get(param).allowType(arg, false)) return false;

		// if the type variable refers to some parameterized type
		// (e.g. T extends List<?>), recurse on the bounds of param
		// that are ParameterizedTypes.
		return isApplicableToTypeVariableBounds(arg, param, typeBounds);
	}

	/**
	 * Determines whether or not the {@link Type} {@code arg} can satisfy the
	 * bounds of {@link TypeVariable} {@code param} given the preexisting
	 * limitations of {@code param}.
	 *
	 * @param arg - a Type Parameter for a given {@link ParameterizedType}
	 * @param param - a {@link TypeVariable} that could potentially be replaced
	 *          with {@code arg}
	 * @param typeBounds - a {@link HashMap} containing the current restrictions
	 *          of the {@link TypeVariable}s
	 * @return {@code boolean} - true if {@code arg} can satisfy the bounds of
	 *         {@code param}.
	 */
	private static boolean isApplicableToTypeVariableBounds(
		final Type arg,
		final TypeVariable<?> param,
		final Map<TypeVariable<?>, TypeVarInfo> typeBounds
	) {
		final var paramBounds = typeBounds.get(param).upperBounds;
		for (final var paramBound : paramBounds) {
			// only have to check the bounds of the
			if (paramBound instanceof ParameterizedType) {

				final var paramBoundType = (ParameterizedType) paramBound;
				final var paramBoundTypes = paramBoundType.getActualTypeArguments();
				final var argTypes = Types.typeParamsOf(arg, Types.raw(paramBoundType));
				for (var i = 0; i < paramBoundTypes.length; i++) {
					// Get the type parameter of arg from the bound type which we know
					// is parameterized.
					final var argType = i < argTypes.length ? argTypes[i] : null;
					if (argType == null) return false;
					if (paramBoundTypes[i] instanceof TypeVariable<?> &&
						!isApplicableToTypeParameter(argType,
							(TypeVariable<?>) paramBoundTypes[i], typeBounds)) return false;
					else if (isApplicable(new Type[] { argType }, new Type[] {
						paramBoundTypes[i] }, typeBounds) != -1) return false;
				}
			}
		}
		return true;
	}

	private static boolean isApplicableToWildcardType(
		final Type arg, final WildcardType param)
	{
		final var upperBounds = param.getUpperBounds();
		final var lowerBounds = param.getLowerBounds();
		final var argType = Types.raw(arg);
		for (final var upperBound : upperBounds) {
			// check that the argument can satisfy the parameter
			final var upperType = Types.raw(upperBound);
			if (!Types.isAssignable(argType, upperType)) return false;
		}
		for (final var lowerBound : lowerBounds) {
			final var lowerType = Types.raw(lowerBound);
			// check that the argument can satisfy the parameter
			if (!Types.isAssignable(lowerType, argType)) return false;
		}

		return true;
	}

	private static boolean isApplicableToGenericArrayType(
		final Type arg,
		final GenericArrayType param,
		final Map<TypeVariable<?>, TypeVarInfo> typeBounds
	) {
		// get a class object of the component type of each array
		final var argComponent = Types.component(arg);
		final var paramComponent = Types.component(param);

		if (paramComponent instanceof ParameterizedType) {
			// TODO are these casts safe?
			final var argType = (ParameterizedType) argComponent;
			final var paramType = (ParameterizedType) paramComponent;

			if (!isApplicableToParameterizedTypes(argType, paramType, typeBounds))
				return false;
		}

		else if (paramComponent instanceof TypeVariable) {
			// TODO are these casts safe?
			final var paramType = (TypeVariable<?>) paramComponent;

			if (!isApplicableToTypeVariable(argComponent, paramType, typeBounds))
				return false;
		}

		// TODO is this necessary? It is only necessary if the component is allowed
		// to be
		// something other than a ParameterizedType or a TypeVariable.
		final var argClass = Types.raw(argComponent);
		final var paramClass = Types.raw(paramComponent);
		return Types.isAssignable(argClass, paramClass);
	}

	private static Type[] filterIndices(Type[] types, List<Integer> indices) {
		return IntStream.range(0, types.length)
			.filter(i -> !indices.contains(i))
			.mapToObj(i -> types[i])
			.toArray(Type[]::new);
	}

	// -- Helper classes --

	/** Maintains info about a {@link TypeVariable}. */
	public static class TypeVarInfo {

		private final TypeVariable<?> var;
		private final Type[] upperBounds;
		private Set<Type> types;

		public TypeVarInfo(final TypeVariable<?> var) {
			this.var = var;
			this.upperBounds = var.getBounds();
			this.types = new HashSet<>();
		}

		public boolean typesContainWildcard() {
			return types.stream().anyMatch(t -> t instanceof WildcardType);
		}

		public boolean wildcardAllowedInParameterizedType(Type type) {
			if (typesContainWildcard()) {
				return false;
			}
			if (!types.isEmpty() && type instanceof WildcardType) {
				return false;
			}

			return true;
		}

		/**
		 * Adds the type of this {@link TypeVarInfo} to the type parameter, if it is
		 * allowed. If the type parameter is not contained within the bounds of the
		 * {@link TypeVariable}, then we return false, and the type of the
		 * {@code TypeVariable} is not changed.
		 * <p>
		 * NB: If this {@code TypeVariable} is being used in a
		 * {@link ParameterizedType} then {@link TypeVarInfo#fixBounds} should be
		 * used instead. Furthermore, it can be specified if wildcards should be
		 * refused, which means that the method will return false if:
		 * </p>
		 * <ul>
		 * <li>a wildcard is already allowed for this type var</li>
		 * <li>{@code bound} is a wildcard and other types are already allowed</li>
		 * </ul>
		 * This is useful if the type variable was contained in a parameterized
		 * type, hence it only allows wildcards if it was not bound to a type yet.
		 *
		 * @param type - the {@link Type} we are trying to assign to this
		 *          {@link TypeVarInfo}.
		 * @return {@code boolean} - false if the {@code Type} is not assignable to
		 *         this {@code TypeVarInfo}
		 */
		public boolean allowType(final Type type, boolean refuseWildcards) {
			if (refuseWildcards && !wildcardAllowedInParameterizedType(type)) {
				return false;
			}

			final var typeClass = Types.raw(type);
			// make sure that type extends all of the bounds of the type variable
			for (final var upperBound : upperBounds) {
				if (!Types.raw(upperBound).isAssignableFrom(typeClass)) return false;
			}

			types.add(type);
			return true;
		}

		/**
		 * If a {@link TypeVariable} is used in a {@link ParameterizedType}, the
		 * bounds have to be fixed such that the upperBound is the {@link Type}
		 * parameterizing that {@link ParameterizedType}. Furthermore, it can be
		 * specified if wildcards should be refused, which means that the method
		 * will return false if:
		 * <ul>
		 * <li>a wildcard is already allowed for this type var</li>
		 * <li>{@code bound} is a wildcard and other types are already allowed</li>
		 * </ul>
		 * This is useful if the type variable was contained in a parameterized
		 * type, hence it only allows wildcards if it was not bound to a type yet.
		 *
		 * @param bound
		 * @param refuseWildcards
		 */
		public boolean fixBounds(final Type bound, boolean refuseWildcards) {
			if (refuseWildcards && !wildcardAllowedInParameterizedType(bound)) {
				return false;
			}

			for (var i = 0; i < upperBounds.length; i++) {
				if (Types.raw(upperBounds[i]).isAssignableFrom(Types.raw(bound)))
					upperBounds[i] = bound;
				else return false;
			}

			// make sure that all of the types that already fit in this variable
			// before the variable was fixed still are allowed by the type variable.
			final var temp = types;
			temp.add(bound);
			types = new HashSet<>();
			for (final var type : temp) {
				if (!allowType(type, false)) return false;
			}
			return true;
		}

		@Override
		public String toString() {
            var s = new StringBuilder();
			s.append(this.getClass().getSimpleName());
			s.append(": ");
			s.append(var.getName());
			s.append("\n\t\t");
			s.append("of Types:\n\t\t\t");
			for (var t : types) {
				s.append(t.getTypeName());
				s.append("\n\t\t\t");
			}
			s.delete(s.length() - 1, s.length());
			s.append("with upper Bounds:\n\t\t\t");
			for (var t : upperBounds) {
				s.append(t.getTypeName());
				s.append("\n\t\t\t");
			}
			s.delete(s.length() - 3, s.length());
			s.append("\n");
			return s.toString();
		}
	}
}
