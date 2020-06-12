/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2019 SciJava Ops developers.
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

package org.scijava.types;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.scijava.types.Types;

/**
 * Additional functions beyond {@link Types} for working with generic types,
 * fields and methods.
 *
 * @author Gabe Selzer
 */
public final class TypeTools {

	private TypeTools() {
		// NB: Prevent instantiation of utility class.
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
	 * @return -1 if the args satisfy the params, otherwise the index of the arg
	 *         that does not satisfy its parameter.
	 */
	public static int satisfies(final Type[] args, final Type[] params) {
		// create a HashMap to monitor the restrictions of the type variables.
		final HashMap<TypeVariable<?>, TypeVarInfo> typeBounds = new HashMap<>();

		return satisfies(args, params, typeBounds);

	}

	public static int satisfies(final Type[] args, final Type[] params,
			final HashMap<TypeVariable<?>, TypeVarInfo> typeBounds)
	{
		if (args.length != params.length) {
			throw new IllegalArgumentException("src and dest lengths differ");
		}

		for (int i = 0; i < params.length; i++) {
			// First, check raw type assignability.
			if (!satisfiesRawTypes(args[i], params[i])) return i;

			if (params[i] instanceof ParameterizedType) {
				if (!satisfiesParameterizedTypes(args[i], (ParameterizedType) params[i],
							typeBounds)) return i;
			}
			else if (params[i] instanceof TypeVariable) {
				if (!satisfiesTypeVariable(args[i], (TypeVariable<?>) params[i],
							typeBounds)) return i;
			}
			else if (params[i] instanceof WildcardType) {
				if (!satisfiesWildcardType(args[i], (WildcardType) params[i]))
					return i;
			}
			else if (params[i] instanceof GenericArrayType) {
				if (!satisfiesGenericArrayType(args[i], (GenericArrayType) params[i],
							typeBounds)) return i;
			}
			//final Type t = TypeToken.of(params[i]).resolveType(args[i]).getType();
			//                     System.out.println("src[" + i + "] = " + t);
		}
		return -1;
	}

	// TODO: Expose this functionality in org.scijava.types.Types.
	private static Method typesSatisfyVariables;

	public static boolean satisfies(
			final Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		//return Types.TypeUtils.typesSatisfyVariables(typeVarAssigns);
		try {
			if (typesSatisfyVariables == null) {
				final Class<?> typeUtilsClass = //
					Arrays.stream(Types.class.getDeclaredClasses())//
						.filter(c -> c.getSimpleName().equals("TypeUtils"))//
						.findFirst().get();
				typesSatisfyVariables = //
					typeUtilsClass.getMethod("typesSatisfyVariables", Map.class);
			}
			return (Boolean) typesSatisfyVariables.invoke(null, typeVarAssigns);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException exc)
		{
			throw new RuntimeException(exc);
		}
	}

	private static boolean satisfiesRawTypes(final Type arg, final Type param) {
		final List<Class<?>> srcClasses = Types.raws(arg);
		final List<Class<?>> destClasses = Types.raws(param);
		for (final Class<?> destClass : destClasses) {
			final Optional<Class<?>> first = srcClasses.stream().filter(
					srcClass -> destClass.isAssignableFrom(srcClass)).findFirst();
			if (!first.isPresent()) {
				// TODO can we remove this?
				//                             throw new IllegalArgumentException("Argument #" + i + //
				//                                     " (" + Types.name(src[i]) + ") is not assignable to " + //
				//                                     "destination type (" + Types.name(dest[i]) + ")");
				return false;
			}
		}
		return true;
	}

	private static boolean satisfiesParameterizedTypes(final Type arg,
			final ParameterizedType param,
			final HashMap<TypeVariable<?>, TypeVarInfo> typeBounds)
	{
		// get an array of the destination parameter types
		final Type[] destTypes = param.getActualTypeArguments();

		// get an array of the source argument types
		final Type[] srcTypes = new Type[destTypes.length];
		for (int i = 0; i < srcTypes.length; i++) {
			srcTypes[i] = Types.param(arg, Types.raw(arg), i);
		}
		// check to see if any of the Types of this ParameterizedType are
		// TypeVariables, if so restrict them to the type parameter of the argument.
		for (int i = 0; i < destTypes.length; i++) {
			final Type destType = destTypes[i];
			if (destType instanceof TypeVariable<?>) {
				final Type srcType = srcTypes[i];
				final TypeVariable<?> destTypeVar = (TypeVariable<?>) destType;
				if (!satisfiesTypeParameter(srcType, destTypeVar, typeBounds))
					return false;
			}
		}
		// recursively run satisfies on these arrays
		return satisfies(srcTypes, destTypes, typeBounds) == -1;
	}

	/**
	 * Determines whether or not the {@link Type} {@code arg} can satisfy the
	 * {@link TypeVariable} {@code param} given the preexisting limitations of
	 * {@code param}. If it is determined that this replacement is allowed the
	 * {@link TypeVariable} is then restriced to the {@link Type} of {@code arg}
	 * (if this is not desired use {@link #satisfiesTypeVariable} instead}.
	 *
	 * @param arg - a Type Parameter for a given {@link ParameterizedType}
	 * @param param - a {@link TypeVariable} that could potentially be replaced
	 *          with {@code arg}
	 * @param typeBounds - a {@link HashMap} containing the current restrictions
	 *          of the {@link TypeVariable}s
	 * @return {@code boolean} - true if the replacement of {@code param} with
	 *         {@code arg} is allowed.
	 */
	private static boolean satisfiesTypeParameter(final Type arg,
		final TypeVariable<?> param,
		final HashMap<TypeVariable<?>, TypeVarInfo> typeBounds)
	{
		// add the type variable to the HashMap if it does not yet exist.
		if (!typeBounds.containsKey(param)) {
			final TypeVariable<?> newTypeVar = param;
			typeBounds.put(newTypeVar, new TypeVarInfo(newTypeVar));
		}
		// attempt to restrict the bounds of the type variable to the argument.
		if (!typeBounds.get(param).fixBounds(arg)) return false;

		// TODO do we need type parameter recursion here as we do in
		// satisfiesTypeVariable?

		return true;
	}

	private static boolean satisfiesTypeVariable(final Type arg,
			final TypeVariable<?> param,
			final HashMap<TypeVariable<?>, TypeVarInfo> typeBounds)
	{
		// if the TypeVariable is not already in the hashMap add it.
		if (!typeBounds.containsKey(param)) typeBounds.put(param, new TypeVarInfo(
					param));
		// check to make sure that arg is a viable replacement for the type
		// variable.
		if (!typeBounds.get(param).allowType(arg)) return false;

		// if the type variable refers to some parameterized type (e.g. T extends
		// List<?>), call satisfiesTypeParameters on the bounds of param that
		// are ParameterizedTypes.
		final Type[] paramBounds = typeBounds.get(param).upperBounds;
		for (final Type paramBound : paramBounds) {
			// only have to check the bounds of the
			if (paramBound instanceof ParameterizedType) {

				final ParameterizedType paramBoundType = (ParameterizedType) paramBound;
				final Type[] paramBoundTypes = paramBoundType.getActualTypeArguments();
				for (int i = 0; i < paramBoundTypes.length; i++) {
					final Type argType = Types.param(arg, Types.raw(param), i);
					if (paramBoundTypes[i] instanceof TypeVariable<?> &&
							!satisfiesTypeParameter(argType,
								(TypeVariable<?>) paramBoundTypes[i], typeBounds)) return false;
					else if (satisfies(new Type[] { argType }, new Type[] {
						paramBoundTypes[i] }, typeBounds) != -1) return false;
				}
			}
		}

		return true;
	}

	private static boolean satisfiesWildcardType(final Type arg,
			final WildcardType param)
	{
		final Type[] upperBounds = param.getUpperBounds();
		final Type[] lowerBounds = param.getLowerBounds();
		final Class<?> argType = Types.raw(arg);
		for (final Type upperBound : upperBounds) {
			// check that the argument can satisfy the parameter
			final Class<?> upperType = Types.raw(upperBound);
			if (!upperType.isAssignableFrom(argType)) return false;
		}
		for (final Type lowerBound : lowerBounds) {
			final Class<?> lowerType = Types.raw(lowerBound);
			// check that the argument can satisfy the parameter
			if (!argType.isAssignableFrom(lowerType)) return false;
		}

		return true;
	}

	private static boolean satisfiesGenericArrayType(final Type arg,
			final GenericArrayType param,
			final HashMap<TypeVariable<?>, TypeVarInfo> typeBounds)
	{
		// get a class object of the component type of each array
		final Type argComponent = Types.component(arg);
		final Type paramComponent = Types.component(param);

		if (paramComponent instanceof ParameterizedType) {
			// TODO are these casts safe?
			final ParameterizedType argType = (ParameterizedType) argComponent;
			final ParameterizedType paramType = (ParameterizedType) paramComponent;

			if (!satisfiesParameterizedTypes(argType, paramType, typeBounds))
				return false;
		}

		else if (paramComponent instanceof TypeVariable) {
			// TODO are these casts safe?
			final TypeVariable<?> paramType = (TypeVariable<?>) paramComponent;

			if (!satisfiesTypeVariable(argComponent, paramType, typeBounds))
				return false;
		}

		// TODO is this necessary? It is only necessary if the component is allowed
		// to be
		// something other than a ParameterizedType or a TypeVariable.
		final Class<?> argClass = Types.raw(argComponent);
		final Class<?> paramClass = Types.raw(paramComponent);
		if (!paramClass.isAssignableFrom(argClass)) return false;
		return true;
	}

	/**
	 * Maintains info about an {@link TypeVariable}.
	 */
	private static class TypeVarInfo {

		private final TypeVariable<?> var;
		private final Type[] upperBounds;
		private List<Type> types;

		public TypeVarInfo(final TypeVariable<?> var) {
			this.var = var;
			this.upperBounds = var.getBounds();
			this.types = new ArrayList<>();
		}

		/**
		 * Sets the type of this {@link TypeVarInfo} to the type parameter, if it is
		 * allowed. If the type parameter is not contained within the bounds of the
		 * {@link TypeVariable}, then we return false, and the type of the
		 * {@code TypeVariable} is not changed. N.B. if this {@code TypeVariable} is
		 * being used in an {@link ParameterizedType} then
		 * {@link TypeVarInfo#fixBounds} should be used instead.
		 *
		 * @param type - the {@link Type} we are trying to assign to this
		 *          {@link TypeVarInfo}.
		 * @return {@code boolean} - false if the {@code Type} is not assignable to
		 *         this {@code TypeVarInfo}
		 */
		public boolean allowType(final Type type) {
			final Class<?> typeClass = Types.raw(type);
			// make sure that type extends all of the bounds of the type variable
			for (final Type upperBound : upperBounds) {
				if (!Types.raw(upperBound).isAssignableFrom(typeClass)) return false;
			}

			types.add(type);
			return true;
		}

		/**
		 * If a {@link TypeVariable} is used in a {@link ParameterizedType}, the
		 * bounds have to be fixed such that the upperBound is the {@link Type}
		 * parameterizing that {@link ParameterizedType}.
		 */
		public boolean fixBounds(final Type bound) {
			for (int i = 0; i < upperBounds.length; i++) {
				if (Types.raw(upperBounds[i]).isAssignableFrom(Types.raw(bound)))
					upperBounds[i] = bound;
				else return false;
			}

			// make sure that all of the types that already fit in this variable
			// before the variable was fixed still are allowed by the type variable.
			final List<Type> temp = types;
			temp.add(bound);
			types = new ArrayList<>();
			for (final Type type : temp) {
				if (!allowType(type)) return false;
			}

			return true;
		}
	}
}
