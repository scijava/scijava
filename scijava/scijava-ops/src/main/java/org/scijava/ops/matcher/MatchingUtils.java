/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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

package org.scijava.ops.matcher;

import com.google.common.base.Objects;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.scijava.types.Any;
import org.scijava.types.Nil;
import org.scijava.types.Types;
import org.scijava.types.Types.TypeVarFromParameterizedTypeInfo;
import org.scijava.types.Types.TypeVarInfo;

public final class MatchingUtils {

	private MatchingUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Checks for raw assignability. TODO This method is not yet fully
	 * implemented. The correct behavior should be as follows. Suppose we have a
	 * generic typed method like:
	 *
	 * <pre>
	 *public static &lt;N&gt; List&lt;N&gt; foo(N in) {
	 *	...
	 *}
	 * </pre>
	 *
	 * This method should discern if the following assignments would be legal,
	 * possibly using predetermined {@link TypeVariable} assignments:
	 *
	 * <pre>
	 *List&lt;Integer&gt; listOfInts = foo(new Integer(0)) //legal
	 *List&lt;Number&gt; listOfNumbers = foo(new Integer(0)) //legal
	 *List&lt;? extends Number&gt; listOfBoundedWildcards = foo(new Integer(0)) //legal
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
	 * checkGenericOutputsAssignability(nilN.getType(), nilInteger.getType, ...)
	 * checkGenericOutputsAssignability(nilN.getType(), nilNumber.getType, ...)
	 * checkGenericOutputsAssignability(nilN.getType(), nilWildcardNumber.getType, ...)
	 * </pre>
	 *
	 * Using a map where N was already bound to Integer (N -> Integer.class). This
	 * method is useful for the following scenario: During ops matching, we first
	 * check if the arguments (inputs) of the requested op are applicable to the
	 * arguments of an op candidate. During this process, possible type variables
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
			HashMap<TypeVariable<?>, TypeVarInfo> typeBounds) {
		for (int i = 0; i < froms.length; i++) {
			Type from = froms[i];
			Type to = tos[i];
			
			if (to instanceof Any) continue;

			if (from instanceof TypeVariable) {
				TypeVarInfo typeVarInfo = typeBounds.get(from);
				// HACK: we CAN assign, for example, a Function<Iterable<N>, O> to a
				// Function<Iterable<Integer>, Double>,
				// because in this situation O is not bounded to any other types. However
				// isAssignable will fail,
				// since we cannot just cast Double to O without that required knowledge that O
				// can be fixed to Double.
				// We get around this by recording in typeBounds that our previously unbounded
				// TypeVariable (from) \
				// is now fixed to (to), then simply assigning (from) to (to), since from only
				// has one bound, being to.
				if (typeVarInfo == null) {
					TypeVariable<?> fromTypeVar = (TypeVariable<?>) from;
					TypeVarFromParameterizedTypeInfo fromInfo = new TypeVarFromParameterizedTypeInfo(fromTypeVar);
					fromInfo.fixBounds(to, true);
					typeBounds.put(fromTypeVar, fromInfo);
					from = to;
				}
				// similar to the above, if we know that O is already bound to a Type, and that
				// Type is to, then we can assign this without any issues.
				else {
					if(typeVarInfo.allowType(to, true)) from = to;
				}
			}

			if (!Types.isAssignable(Types.raw(from), Types.raw(to)))
				return i;
		}
		return -1;
	}

	/**
	 * Checks whether it would be legal to assign the {@link Type} source to the
	 * specified {@link ParameterizedType} destination (which could possibly be a
	 * supertype of the source type). Thereby, possible {@link TypeVariable}s
	 * contained in the parameters of the source are tried to be inferred in the
	 * sense of empty angle brackets when a new object is created:
	 *
	 * <pre>
	 * 
	 * List&lt;Integer&gt; listOfInts = new ArrayList&lt;&gt;();
	 * </pre>
	 *
	 * Hence, the types to put between the brackets are tried to be determined.
	 * Inference will be done by simple matching of an encountered
	 * {@link TypeVariable} in the source to the corresponding type in the
	 * parameters of the destination. If an {@link TypeVariable} is encountered
	 * more than once, the corresponding type in the destination needs to
	 * perfectly match. Else, false will be returned.</br>
	 * </br>
	 * Examples:
	 * <ul>
	 * If we have a class:
	 * <li>
	 *
	 * <pre>
	 * class NumberSupplier&lt;M extends Number&gt; implements Supplier&lt;M&gt;
	 * </li>
	 * </ul>
	 * <ul>
	 * The following check will return true:
	 * <li>
	 *
	 * <pre>
	 * checkGenericAssignability(NumberSupplier.class, new
	 * Nil&lt;Supplier&lt;Double&gt;&gt;() {}.getType())</li>
	 * </ul>
	 * </ul>
	 * <ul>
	 * Which will check if the following assignment would be legal:
	 * <li>
	 *
	 * <pre>
	 * Supplier&lt;Double&gt; list = new NumberSupplier&lt;&gt;()</li>
	 * </ul>
	 * </ul>
	 * <ul>
	 * Here, the parameter {@code <M extends Number>} can be inferred to be of
	 * type {@code Double} from the type {@code Supplier<Double>}
	 * </ul>
	 * <ul>
	 * Consequently the following will return false:
	 * <li>
	 *
	 * <pre>
	 * checkGenericAssignability(NumberSupplier.class, new
	 * Nil&lt;Supplier&lt;String&gt;&gt;() {}.getType())</li>
	 * </ul>
	 * <ul>
	 * {@code <M extends Number>} can't be inferred, as type {@code String} is not
	 * within the bounds of {@code M}.
	 * </ul>
	 * <ul>
	 * Furthermore, the following will return false for:
	 * {@code class NumberFunc<M extends Number> implements Function<M, M>}:
	 * <li>
	 *
	 * <pre>
	 * checkGenericAssignability(NumberSupplier.class, new
	 * Nil&lt;Function&lt;Double, Integer&gt;&gt;() {}.getType())</li>
	 * </ul>
	 * <ul>
	 * {@code <M extends Number>} can't be inferred, as types {@code Double} and
	 * {@code Integer} are ambiguous for {@code M}.
	 * </ul>
	 *
	 * @param src the type for which assignment should be checked from
	 * @param dest the parameterized type for which assignment should be checked
	 *          to
	 * @param typeVarAssigns the map of {@link TypeVariable}s to {@link Type}s to
	 *          populate with what would occur in this scenario; must be empty or
	 *          null
	 * @param safeAssignability used to determine if we want to check if the
	 *          src->dest assignment would be safely assignable even though it
	 *          would cause a compiler error if we explicitly tried to do this
	 *          (useful pretty much only for Op matching)
	 * @return whether and assignment of source to destination would be a legal
	 *         java statement
	 */
	public static boolean checkGenericAssignability(Type src,
		ParameterizedType dest, Map<TypeVariable<?>, Type> typeVarAssigns,
		boolean safeAssignability)
	{
		if (typeVarAssigns == null) {
			typeVarAssigns = new HashMap<>();
		}
		else if (!typeVarAssigns.isEmpty()) {
			throw new IllegalArgumentException(
				"Expected empty typeVarAssigns but contained " + typeVarAssigns.size() +
					" entries");
		}

		// fail fast when raw types are not assignable
		if (!Types.isAssignable(Types.raw(src), Types.raw(dest))) return false;

		// when raw types are assignable, check the type variables of src and dest
		Type[] srcTypes = typeParamsAgainstClass(src, Types.raw(dest));
		Type[] destTypes = dest.getActualTypeArguments();

		// if there are no type parameters in src (w.r.t. dest), do a basic
		// assignability check.
		if (srcTypes.length == 0) return Types.isAssignable(src, dest);
		// if there are type parameters, do a more complicated assignability check.
		boolean result = checkGenericAssignability(srcTypes, destTypes, src, dest,
			typeVarAssigns, safeAssignability);
		return result;
	}

	/**
	 * Obtains the type parameters of {@link Type} {@code src} <b>with respect
	 * to</b> the {@link Class} {@code dest}. When {@code src} has no type
	 * parameters (or is not a subclass of {@code dest}), an empty array is
	 * returned.
	 *
	 * @param src - the {@code Type} whose type parameters will be returned.
	 * @param superclass - the {@code Class} against which we want the type parameters of {@code src}
	 * @return an array of {@code Type}s denoting the type
	 */
	private static Type[] typeParamsAgainstClass(Type src, Class<?> superclass) {
		// only classes and ParameterizedTypes can have type parameters
		if (!(src instanceof Class || src instanceof ParameterizedType)) return new Type[0];
		Type superSrc = superType(src, superclass);
		if (superSrc == null) return new Type[0];
		if (superSrc instanceof ParameterizedType) return ((ParameterizedType) superSrc).getActualTypeArguments();
		return getParams(Types.raw(src), superclass);
	}

	/**
	 * This method is designed to handle edge cases when calling
	 * {@link Types#getExactSuperType(Type, Class)}. When
	 * {@code getExactSuperType} returns an error, this usually implies some funny
	 * business going on with the {@link Type} that was passed to it. We are not
	 * interested in supporting this business, since it usually results from poor
	 * practice in Class construction. TODO: determine some way of conveying to
	 * the user that GenTyRef doesn't like their inputs.
	 *
	 * @param src
	 * @param superClass
	 * @return - the supertype of {@code src} with rawtype {@code superClass}, or
	 *         {@code null} if no such supertype exists.
	 */
	private static Type superType(Type src, Class<?> superClass) {
			try {
				return Types.getExactSuperType(src, superClass);
			} catch (AssertionError e) {
				// can be thrown when
				return null;
			}
	}

	/**
	 * @param src
	 *            the type for which assignment should be checked from
	 * @param dest
	 *            the parameterized type for which assignment should be checked to
	 * @param safeAssignability
	 *            used to determine if we want to check if the src->dest assignment
	 *            would be safely assignable even though it would cause a compiler
	 *            error if we explicitly tried to do this (useful pretty much only
	 *            for Op matching)
	 * @return whether and assignment of source to destination would be a legal java
	 *         statement
	 */
	public static boolean checkGenericAssignability(Type src, ParameterizedType dest, boolean safeAssignability) {
		return checkGenericAssignability(src, dest, null, safeAssignability);
	}

	/**
	 * @param srcTypes the Type arguments for the source Type
	 * @param destTypes the Type arguments for the destination Type
	 * @param src the type for which assignment should be checked from
	 * @param dest the parameterized type for which assignment should be checked
	 *          to
	 * @param typeVarAssigns the map of {@link TypeVariable}s to
	 *          {@link TypeMapping}s that would occur in this scenario
	 * @param safeAssignability used to determine if we want to check if the
	 *          src->dest assignment would be safely assignable even though it
	 *          would cause a compiler error if we explicitly tried to do this
	 *          (useful pretty much only for Op matching)
	 * @return whether and assignment of source to destination would be a legal
	 *         java statement
	 */
	private static boolean checkGenericAssignability(Type[] srcTypes, Type[] destTypes, Type src, Type dest,
			Map<TypeVariable<?>, Type> typeVarAssigns, boolean safeAssignability) {
		// if the number of type arguments does not match, the types can't be
		// assignable
		if (srcTypes.length != destTypes.length) {
			return false;
		}
		
		try {
			// Try to infer type variables contained in the type arguments of
			// sry
			inferTypeVariables(srcTypes, destTypes, typeVarAssigns);
		} catch (IllegalArgumentException e) {
			// types can't be inferred
			// TODO: Consider the situations in which it is okay that the type
			// variables cannot be inferred. For example, if we have a
			// Function<Comparable<T>, Comparable<T>> and we ask for a
			// Function<Double, Object>, it is okay that we cannot infer the T of
			// Comparable<T> from Object since a Comparable<T> is an Object for any T.
			// It would be nice if we could just return false any time we catch a
			// TypeInferenceException, but until we sort this out, we cannot do so.
			return safeAssignability && isSafeAssignable(destTypes, typeVarAssigns, src, dest);
		}

		// Map TypeVariables in src to Types
		Class<?> matchingRawType = Types.raw(dest);
		Type[] mappedSrcTypes = mapVarToTypes(srcTypes, typeVarAssigns);
		Type inferredSrcType = Types.parameterize(matchingRawType, mappedSrcTypes);

		// Check assignability
		if (Types.isAssignable(inferredSrcType, dest, typeVarAssigns)) return true;

		return safeAssignability && isSafeAssignable(destTypes, typeVarAssigns, src,
			dest);
	}

	/**
	 * We know that the special types for the Op candidate and what we asked for are
	 * the same (i.e. that we are trying to determine if one Function can be
	 * assigned to another Function). There are some situations (that are
	 * particularly common when using ops.run()) where the Function SHOULD NOT
	 * NORMALLY MATCH UP but WE KNOW IT WILL BE SAFE TO ASSIGN. This method attempts
	 * to tease those situations out as a last resort.
	 * 
	 * @param destTypes
	 *            - the array of Parameterized types of the OpInfo we called the
	 *            matcher on (in the case of ops.run(), it is a Type array of the
	 *            types of the args we passed through.)
	 * @param typeVarAssigns
	 *            - a Map of all of the Type Variables already determined.
	 * @param dest
	 *            - the speical type of the Op that we want to find a match for
	 *            (determined by the user / ops.run())
	 * @return boolean - true if we can safely match this Op even though the types
	 *         do not directly match up. False otherwise.
	 */
	public static boolean isSafeAssignable(Type[] destTypes, Map<TypeVariable<?>, Type> typeVarAssigns, Type src,
			Type dest) {

		Method[] destMethods = Arrays.stream(Types.raw(dest).getDeclaredMethods())
				.filter(method -> Modifier.isAbstract(method.getModifiers())).toArray(Method[]::new);
		if (destMethods.length == 0) {
			throw new IllegalArgumentException(src + " does not have an abstract method!");
		}
		Type[] params = Types.getExactParameterTypes(destMethods[0], src);
		Type returnType = Types.getExactReturnType(destMethods[0], src);
		for (int i = 0; i < params.length; i++) {
			if (!Types.isAssignable(destTypes[i], params[i], typeVarAssigns))
				return false;
		}

		// Computers will have void as their return type, meaning that there is no
		// output to check.
		if (returnType == void.class)
			return true;

		return Types.isAssignable(returnType, destTypes[destTypes.length - 1], typeVarAssigns);
	}

	/**
	 * Exception indicating that type vars could not be inferred.
	 */
	static class TypeInferenceException extends RuntimeException {
		/**
		 *
		 */
		private static final long serialVersionUID = 7147530827546663700L;
		
		public TypeInferenceException() {
			super();
		}
		
		public TypeInferenceException(String message) {
			super(message);
		}
	}

	/**
	 * Map type vars in specified type list to types using the specified map. In
	 * doing so, type vars mapping to other type vars will not be followed but
	 * just replaced.
	 *
	 * @param typesToMap
	 * @param typeAssigns
	 * @return a copy of {@code typesToMap} in which the {@link TypeVariable}s
	 *         (that are present in {@code typeAssigns}) are mapped to the
	 *         associated values within the {@code Map}.
	 */
	private static Type[] mapVarToTypes(Type[] typesToMap, Map<TypeVariable<?>, Type> typeAssigns) {
		return Arrays.stream(typesToMap).map(type -> Types.unrollVariables(typeAssigns, type, false))
				.toArray(Type[]::new);
	}

	/**
	 * Tries to infer type vars contained in types from corresponding types from
	 * inferFrom, putting them into the specified map. <b>When a
	 * {@link TypeInferenceException} is thrown, the caller should assume that
	 * some of the mappings within {@code typeMappings} are incorrect.</b>
	 *
	 * @param types - the types containing {@link TypeVariable}s
	 * @param inferFroms - the types used to infer the {@link TypeVariable}s
	 *          within {@code types}
	 * @param typeVarAssigns - the mapping of {@link TypeVariable}s to
	 *          {@link Type}s
	 */
	public static void inferTypeVariables(Type[] types, Type[] inferFroms, Map<TypeVariable<?>, Type> typeVarAssigns) {
		Map<TypeVariable<?>, TypeMapping> typeMappings = new HashMap<>();
		try {
			inferTypeVariables(types, inferFroms, typeMappings, true);
			typeVarAssigns.putAll(new TypeVarAssigns(typeMappings));
		}
		catch (TypeInferenceException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	private static void inferTypeVariables(Type[] types, Type[] inferFroms,
		Map<TypeVariable<?>, TypeMapping> typeMappings, boolean malleable) 
	{
		// Ensure that the user has not passed a null map
		if (typeMappings == null) throw new IllegalArgumentException(
			"Type Variable map is null, cannot store mappings of TypeVariables to Types!");

		if (types.length != inferFroms.length) throw new TypeInferenceException(
			"Could not infer type variables: Type arrays must be of the same size");

		for (int i = 0; i < types.length; i++) {
			inferTypeVariables(types[i], inferFroms[i], typeMappings, malleable);
		}
		// Check if the inferred types satisfy their bounds
		// TODO: can we do this in an efficient manner?
		TypeVarAssigns typeVarAssigns = new TypeVarAssigns(typeMappings);
		if (!Types.typesSatisfyVariables(typeVarAssigns)) {
			throw new TypeInferenceException();
		}
	}
	
	/**
	 * Tries to infer type vars contained in types from corresponding types from
	 * inferFrom, putting them into the specified map. <b>When a
	 * {@link TypeInferenceException} is thrown, the caller should assume that
	 * some of the mappings within {@code typeMappings} are incorrect.</b>
	 *
	 * @param type
	 * @param inferFrom
	 * @param typeMappings
	 */
	static void inferTypeVariablesWithTypeMappings(Type type[], Type[] inferFrom,
		Map<TypeVariable<?>, TypeMapping> typeMappings)
	{
		inferTypeVariables(type, inferFrom, typeMappings, true);
	}
	
	/**
	 * Tries to infer type vars contained in types from corresponding types from
	 * inferFrom, putting them into the specified map. <b>When a
	 * {@link TypeInferenceException} is thrown, the caller should assume that
	 * some of the mappings within {@code typeMappings} are incorrect.</b>
	 *
	 * @param type
	 * @param inferFrom
	 * @param typeMappings
	 */
	static void inferTypeVariables(Type type, Type inferFrom, Map<TypeVariable<?>, TypeMapping> typeMappings) {
		inferTypeVariables(type, inferFrom, typeMappings, true);
	}

	private static void inferTypeVariables(Type type, Type inferFrom,
		Map<TypeVariable<?>, TypeMapping> typeMappings, boolean malleable) 
	{
		if (type instanceof TypeVariable) {
			inferTypeVariables((TypeVariable<?>) type, inferFrom, typeMappings, malleable);
		}
		else if (type instanceof ParameterizedType) {
			inferTypeVariables((ParameterizedType) type, inferFrom, typeMappings);
		}
		else if (type instanceof WildcardType) {
			inferTypeVariables((WildcardType) type, inferFrom, typeMappings);
		}
		else if (type instanceof GenericArrayType) {
			inferTypeVariables((GenericArrayType) type, inferFrom, typeMappings);
		}
		else if (type instanceof Class) {
			inferTypeVariables((Class<?>) type, inferFrom, typeMappings);
		}

	}

	private static void inferTypeVariables(TypeVariable<?> type, Type inferFrom,
		Map<TypeVariable<?>, TypeMapping> typeMappings, boolean malleable)
	{
		TypeMapping typeData = typeMappings.get(type);
		// If current is not null then we have already encountered that
		// variable. If so, we require them to be exactly the same, and throw a
		// TypeInferenceException if they are not.
		if (typeData != null) {
			typeData.refine(inferFrom, malleable);
		}
		else {
			resolveTypeInMap(type, inferFrom, typeMappings, malleable);
			// Bounds could also contain type vars, hence possibly go into
			// recursion
			for (Type bound : type.getBounds()) {
				if (bound instanceof TypeVariable && typeMappings.get(bound) != null) {
					// If the bound of the current var (let's call it A) to
					// infer is also a var (let's call it B):
					// If we already encountered B, we check if the current
					// type to infer from is assignable to
					// the already inferred type for B. In this case we do
					// not require equality as one var is
					// bounded by another and it is not the same. E.g.
					// assume we want to infer the types of vars:
					// - - - A extends Number, B extends A
					// From types:
					// - - - Number, Double
					// First A is bound to Number, next B to Double. Then we
					// check the bounds for B. We encounter A,
					// for which we already inferred Number. Hence, it
					// suffices to check whether Double can be assigned
					// to Number, it does not have to be equal as it is just
					// a transitive bound for B.
					Type typeAssignForBound = typeMappings.get(bound).getType();
					if (!Types.isAssignable(inferFrom, typeAssignForBound)) {
						throw new TypeInferenceException();
					}
				}
				else {
					// Else go into recursion as we encountered a new var.
					inferTypeVariables(bound, inferFrom, typeMappings);
				}
			}

		}
	}

	private static void inferTypeVariables(ParameterizedType type, Type inferFrom,
		Map<TypeVariable<?>, TypeMapping> typeMappings)
	{
		if (inferFrom instanceof WildcardType) {
			inferFrom = getInferrableBound((WildcardType) inferFrom);
		}
		if (inferFrom instanceof Any) {
			Any any = (Any) inferFrom;
			mapTypeVarsToAny(type, any, typeMappings);
			return;
		}
		// Finding the supertype here is really important. Suppose that we are
		// inferring from a StrangeThing<Long> extends Thing<Double> and our
		// Op requires a Thing<T>. We need to ensure that T gets
		// resolved to a Double and NOT a Long.
		Type superInferFrom = Types.getExactSuperType(inferFrom, Types.raw(type));
		if (superInferFrom instanceof ParameterizedType) {
			ParameterizedType paramInferFrom = (ParameterizedType) superInferFrom;
			inferTypeVariables(type.getActualTypeArguments(), paramInferFrom
				.getActualTypeArguments(), typeMappings, false);
		}
		else if (superInferFrom instanceof Class) {
			TypeVarAssigns typeVarAssigns = new TypeVarAssigns(typeMappings);
			Type mappedType = Types.mapVarToTypes(type, typeVarAssigns);
			// Use isAssignable to attempt to infer the type variables present in type
			if (!Types.isAssignable(superInferFrom, mappedType, typeVarAssigns)) {
				throw new TypeInferenceException(inferFrom +
					" cannot be implicitly cast to " + mappedType +
					", thus it is impossible to infer type variables for " + inferFrom);
			}
		}
		// -- edge cases -> do our best -- //
		else if (superInferFrom == null) {
			// edge case 1: if inferFrom is an Object, superInferFrom will be null
			// when type is some interface.
			if (Object.class.equals(inferFrom)) {
				mapTypeVarsToAny(type, typeMappings);
				return;
			}
			// edge case 2: if inferFrom is a superType of type, we can get (some of)
			// the types of type by finding the exact superType of type w.r.t.
			// inferFrom.
			Type superTypeOfType = Types.getExactSuperType(type, Types.raw(
				inferFrom));
			if (superTypeOfType == null) {
				throw new TypeInferenceException(inferFrom +
					" cannot be implicitly cast to " + type +
					", thus it is impossible to infer type variables for " + inferFrom);
			}
			inferTypeVariables(superTypeOfType, inferFrom, typeMappings, false);
			mapTypeVarsToAny(type, typeMappings);
		}
		// TODO: elaborate
		else throw new IllegalStateException(superInferFrom +
			" is the supertype of " + inferFrom + " with respect to " + type +
			", however this cannot be (since " + type +
			" is a ParamterizedType)! (Only a ParameterizedType, Class, or null " +
			"can be returned from Types.getExactSuperType when it is called with a ParameterizedType!)");
	}

	private static void mapTypeVarsToAny(Type type,
		Map<TypeVariable<?>, TypeMapping> typeMappings)
	{
		mapTypeVarsToAny(type, new Any(), typeMappings);
	}

	private static void mapTypeVarsToAny(Type type, Any any,
		Map<TypeVariable<?>, TypeMapping> typeMappings)
	{
		if (!Types.containsTypeVars(type)) return;

		if (type instanceof TypeVariable) {
			if (typeMappings.containsKey(type)) return;
			TypeVariable<?> typeVar = (TypeVariable<?>) type;
			typeMappings.put(typeVar, suitableTypeMapping(typeVar, any, true));
		}
		else if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			Type[] typeParams = pType.getActualTypeArguments();
			for (Type typeParam : typeParams) {
				mapTypeVarsToAny(typeParam, typeMappings);
			}
		}
		else if (type instanceof WildcardType) {
			WildcardType wildcard = (WildcardType) type;
			for (Type lowerBound : wildcard.getLowerBounds())
				mapTypeVarsToAny(lowerBound, typeMappings);
			for (Type upperBound : wildcard.getUpperBounds())
				mapTypeVarsToAny(upperBound, typeMappings);
		}
		else if (type instanceof Class) {
			Class<?> clazz = (Class<?>) type;
			for (Type typeParam : clazz.getTypeParameters())
				mapTypeVarsToAny(typeParam, typeMappings);
		}
	}

	private static void inferTypeVariables(WildcardType type, Type inferFrom, Map<TypeVariable<?>, TypeMapping> typeMappings) {
		Type inferrableBound = getInferrableBound(type);
		if (inferFrom instanceof WildcardType) {
			// NB if both type and inferFrom are Wildcards, it doesn't really matter
			// (for the purpose of Type inference) whether those Wildcards have a
			// defined upper or lower bound. It is only important that we compare
			// those defined bounds, even if one is an upper bound and the other is a
			// lower bound. If the Wildcards are not assignable (which is (always?)
			// the case when one bound is an upper bound and the other is a lower
			// bound), it is still possible to infer the type variables; despite doing
			// so, checkGenericAssignability will return false.
			inferFrom = getInferrableBound((WildcardType) inferFrom);
		}
		if (inferrableBound instanceof TypeVariable<?>) {
			resolveTypeInMap((TypeVariable<?>) inferrableBound, inferFrom, typeMappings, true);
		}
		else if (inferrableBound instanceof ParameterizedType) {
			ParameterizedType parameterizedUpperBound =
				(ParameterizedType) inferrableBound;
			inferTypeVariables(parameterizedUpperBound, inferFrom, typeMappings,
				true);
		}
		// TODO: consider checking inferrableBounds instanceof Class
	}
	
	private static void resolveTypeInMap(TypeVariable<?> typeVar, Type newType,
		Map<TypeVariable<?>, TypeMapping> typeMappings, boolean malleability)
	{
		if (typeMappings.containsKey(typeVar)) {
			typeMappings.get(typeVar).refine(newType, malleability);
		}
		else {
			typeMappings.put(typeVar, suitableTypeMapping(typeVar, newType,
				malleability));
		}
	}
	
	private static TypeMapping suitableTypeMapping(TypeVariable<?> typeVar,
		Type newType, boolean malleability) 
	{
		if (newType instanceof WildcardType) {
			return new WildcardTypeMapping(typeVar, (WildcardType) newType,
				malleability);
		}
		return new TypeMapping(typeVar, newType, malleability);
	}

	private static void inferTypeVariables(Class<?> type, Type inferFrom, Map<TypeVariable<?>, TypeMapping> typeMappings) {
		if( inferFrom instanceof TypeVariable<?>){
			TypeVarAssigns typeVarAssigns = new TypeVarAssigns(typeMappings);
			// If current type var is absent put it to the map. Otherwise,
			// we already encountered that var.
			// Hence, we require them to be exactly the same.
			if(Types.isAssignable(type, inferFrom, typeVarAssigns)) {
			Type current = typeVarAssigns.putIfAbsent((TypeVariable<?>) inferFrom, type);
				if (current != null) {
					if (current instanceof Any) {
						typeVarAssigns.put((TypeVariable<?>) inferFrom, type);
					}
					else if (!Objects.equal(type, current)) {
						throw new TypeInferenceException();
					}
				}
			}
		}
	}

	private static void inferTypeVariables(GenericArrayType type, Type inferFrom,
		Map<TypeVariable<?>, TypeMapping> typeMappings)
	{
		if (inferFrom instanceof Class<?> && ((Class<?>) inferFrom).isArray()) {
			Type componentType = type.getGenericComponentType();
			Type componentInferFrom = ((Class<?>) inferFrom).getComponentType();
			inferTypeVariables(componentType, componentInferFrom, typeMappings);
		}
		else if (inferFrom instanceof WildcardType) {
			Type inferrableBound = getInferrableBound((WildcardType) inferFrom);
			inferTypeVariables(type, inferrableBound, typeMappings);
		}
		else throw new TypeInferenceException(inferFrom +
			" cannot be implicitly cast to " + type +
			", thus it is impossible to infer type variables for " + inferFrom);
	}

	/**
	 * Current java language specifications allow either:
	 * <ul>
	 * <li>one {@code Object} upper bound and one lower bound
	 * <li>one (arbitrary) upper bound and no lower bounds
	 * </ul>
	 * We rely on this fact for the purposes of inferring type variables.
	 *
	 * @param type
	 * @return the <b>singular</b> {@link Type} that bounds this
	 *         {@link TypeVariable}. The returned {@code Type} could be
	 *         <b>either</b> a lower <b>or</b> upper bound (we do not care for the
	 *         sole purpose of type inference).
	 */
	private static Type getInferrableBound(WildcardType type) {
		Type[] lBounds = type.getLowerBounds();
		Type[] uBounds = type.getUpperBounds();
		if (lBounds.length == 1 && uBounds.length == 1 &&
			uBounds[0] == Object.class) return lBounds[0];
		else if (lBounds.length == 0 && uBounds.length == 1) return uBounds[0];
		else throw new IllegalArgumentException(
			"Illegal WildcardType: Current Java Language Specification does not allow " +
				type + " to simultaneously have upper bounds " + uBounds +
				" and lower bounds " + lBounds);
	}

	/**
	 * Finds the type parameters of the most specific super type of the specified
	 * subType whose erasure is the specified superErasure. Hence, will return the
	 * type parameters of superErasure possibly narrowed down by subType. If
	 * superErasure is not raw or not a super type of subType, an empty array will
	 * be returned.
	 *
	 * @param subType
	 *            the type to narrow down type parameters
	 * @param superErasure
	 *            the erasure of an super type of subType to get the parameters from
	 * @return type parameters of superErasure possibly narrowed down by subType, or
	 *         empty type array if no exists or superErasure is not a super type of
	 *         subtype
	 */
	public static Type[] getParams(Class<?> subType, Class<?> superErasure) {
		Type pt = Types.parameterizeRaw(subType);
		Type superType = Types.getExactSuperType(pt, superErasure);
		if (superType != null && superType instanceof ParameterizedType) {
			return ((ParameterizedType) superType).getActualTypeArguments();
		}
		return new Type[0];
	}

	/**
	 * Gets the "useful" class information carries on the given object, which
	 * depends on the actual type of the object.
	 */
	public static Class<?> getClass(final Object obj) {
		if (obj == null)
			return null;
		if (obj instanceof Nil)
			return getClass(((Nil<?>) obj).getType());
		if (obj instanceof Class)
			return (Class<?>) obj;
		if (obj instanceof ParameterizedType)
			return (Class<?>) ((ParameterizedType) obj).getRawType();
		return obj.getClass();
	}

	/**
	 * Finds the levels of casting between <code>origin</code> and
	 * <code>dest</code>. Returns 0 if dest and origin are the same. Returns -1 if
	 * dest is not assignable from origin.
	 */
	public static int findCastLevels(final Class<?> dest, final Class<?> origin) {
		if (dest.equals(origin))
			return 0;

		int level = 1;
		Class<?> currType = origin;
		// BFS if dest is an interface
		if (dest.isInterface()) {
			final HashSet<String> seen = new HashSet<>();
			final ArrayList<Type> currIfaces = new ArrayList<>(Arrays.asList(currType.getGenericInterfaces()));
			do {
				final ArrayList<Type> nextIfaces = new ArrayList<>();
				for (final Type iface : currIfaces) {
					if (seen.contains(iface.getTypeName()))
						continue;

					final Class<?> cls = getClass(iface);
					if (cls.equals(dest))
						return level;
					seen.add(iface.getTypeName());
					nextIfaces.addAll(Arrays.asList(cls.getGenericInterfaces()));
				}
				currIfaces.clear();
				currIfaces.addAll(nextIfaces);
				if (currType.getSuperclass() != null) {
					currType = currType.getSuperclass();
					currIfaces.addAll(Arrays.asList(currType.getGenericInterfaces()));
				}
				level++;
			} while (!currIfaces.isEmpty() || currType.getSuperclass() != null);
		}
		// otherwise dest is a class, so search the list of ancestors
		else {
			while (currType.getSuperclass() != null) {
				currType = currType.getSuperclass();
				if (currType.equals(dest))
					return level;
				level++;
			}
		}
		return -1;
	}
	
	/**
	 * A data structure retaining information about the mapping of a
	 * {@link TypeVariable} to a {@link Type} within a type-inferring context.
	 * 
	 * @author Gabriel Selzer
	 */
	static class TypeMapping {

		protected final TypeVariable<?> typeVar;
		protected Type mappedType;

		/**
		 * A boolean describing whether {@code mappedType} can be mutated in within
		 * this set of {@link Type}s. The most common scenario in which a
		 * {@link Type} <b>cannot</b> be mutated is when it is a type parameter of a
		 * {@link ParameterizedType}. Once {@code malleable} is set to
		 * {@code false}, {@code mappedType} <b>cannot</b> change, and
		 * {@link TypeMapping#refine(Type, boolean)} will throw a
		 * {@link TypeInferenceException} so long as {@code newType} is not the
		 * exact same {@code Type} as {@mappedType}.
		 */
		boolean malleable;

		public TypeMapping(TypeVariable<?> typeVar, Type mappedType,
			boolean malleable)
		{
			this.typeVar = typeVar;
			this.mappedType = mappedType;
			this.malleable = malleable;
		}

		/**
		 * Attempts to accommodate {@code newType} into the current mapping between
		 * {@code typeVar} and {@code mappedType} <em>given</em> the existing
		 * malleability of {@code mappedType} and the malleability imposed by
		 * {@code newType}. If {@code newType} cannot be accommodated, a
		 * {@link TypeInferenceException} will be thrown. Note that it is not a
		 * guarantee that either the existing {@code mappedType} or {@code newType}
		 * will become the new {@link #mappedType} after the method ends;
		 * {@link #mappedType} could be a supertype of these two {@link Type}s.
		 * 
		 * @param otherType - the type that will be refined into {@link #mappedType}
		 * @param newTypeMalleability - the malleability of {@code otherType},
		 *          determined by the context from which {@code otherType} came.
		 */
		public void refine(Type otherType, boolean newTypeMalleability)
		{
			malleable &= newTypeMalleability;
			if (mappedType instanceof Any) {
				mappedType = otherType;
				return;
			}
			if (otherType instanceof Any) {
				return;
			}
			if (malleable) {
				// TODO: consider the correct value of that boolean
				Type superType = Types.greatestCommonSuperType(new Type[] { otherType,
					mappedType }, false);
				if (Types.isAssignable(superType, typeVar)) {
					mappedType = superType;
					return;
				}
				throw new TypeInferenceException(typeVar +
					" cannot simultaneoustly be mapped to " + otherType + " and " +
					mappedType);
			}
			if (Objects.equal(mappedType, otherType)) return;
			throw new TypeInferenceException(typeVar +
				" cannot simultaneoustly be mapped to " + otherType + " and " +
				mappedType);
		}

		/**
		 * @return the {@link Type} associated with this {@link TypeVariable}
		 */
		public Type getType() {
			return mappedType;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(typeVar, mappedType, malleable);
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof TypeMapping)) return false;
			final TypeMapping that = (TypeMapping) o;
			return Objects.equal(typeVar, that.typeVar) && //
				Objects.equal(mappedType, that.mappedType) && //
				Objects.equal(malleable, that.malleable);
		}

		@Override
		public String toString() {
			return mappedType.toString();
		}
	}
	
	/**
	 * A data structure retaining information about the mapping of a
	 * {@link TypeVariable} to a {@link Type} bounded by a {@link WildcardType}
	 * within a type-inferring context.
	 * 
	 * @author Gabriel Selzer
	 */
	static class WildcardTypeMapping extends TypeMapping {
		
		private List<Type> lowerBoundList;

		public WildcardTypeMapping(TypeVariable<?> typeVar, WildcardType mappedType,
			boolean malleable) 
		{
			super(typeVar, getUpperBound(mappedType), malleable);
			lowerBoundList = new ArrayList<>();
			Type mappedTypeLowerBound = getLowerBound(mappedType);
			if (mappedTypeLowerBound != null) {
				lowerBoundList.add(mappedTypeLowerBound);
			}
		}	
		
		/**
		 * Attempts to accommodate {@code newType} into the current mapping between
		 * {@code typeVar} and {@code mappedType} <em>given</em> the existing
		 * malleability of {@code mappedType} and the malleability imposed by
		 * {@code newType}. If {@code newType} cannot be accommodated, a
		 * {@link TypeInferenceException} will be thrown. Note that it is not a
		 * guarantee that either the existing {@code mappedType} or {@code newType}
		 * will become the new {@link #mappedType} after the method ends;
		 * {@link #mappedType} could be a supertype of these two {@link Type}s.
		 * 
		 * @param otherType - the type that will be refined into {@link #mappedType}
		 * @param newTypeMalleability - the malleability of {@code otherType},
		 *          determined by the context from which {@code otherType} came.
		 */
		@Override
		public void refine(Type otherType, boolean newTypeMalleability)
		{
			if (otherType instanceof WildcardType) {
				refineWildcard((WildcardType) otherType, newTypeMalleability);
			}
			else {
				super.refine(otherType, newTypeMalleability);
			}
			for (Type lowerBound : lowerBoundList) {
				if (!Types.isAssignable(lowerBound, mappedType))
					throw new TypeInferenceException(typeVar +
						" cannot simultaneoustly be mapped to " + otherType + " and " +
						mappedType);
			}
		}

		private void refineWildcard(WildcardType otherType,
			boolean newTypeMalleability)
		{
			Type otherLowerBound = getLowerBound(otherType);
			if (otherLowerBound != null) {
				lowerBoundList.add(otherLowerBound);
			}
			Type otherUpperBound = getUpperBound(otherType);
			super.refine(otherUpperBound, newTypeMalleability);
		}

		/**
		 * Current Java Language Specifications allow only one upper bound on any
		 * {@link WildcardType}. This method returns that singular bound, or
		 * {@code null} if this {@code WildcardType} has no upper bound.
		 *
		 * @param newType - the {@link WildcardType} for which we will find the
		 *          upper bound
		 * @return the upper bound of {@code newType}
		 */
		private static Type getUpperBound(WildcardType newType) {
			Type[] upperBounds = newType.getUpperBounds();
			if (upperBounds.length == 0) {
				return Object.class;
			}
			else if (upperBounds.length == 1) {
				return upperBounds[0];
			}
			else {
				throw new TypeInferenceException(newType + //
					" is an impossible WildcardType. " + //
					"The Java language specification currently prevents multiple upper bounds " + //
					Arrays.toString(upperBounds)); //
			}
		}

		/**
		 * Current Java Language Specifications allow only one lower bound on any
		 * {@link WildcardType}. This method returns that singular bound, or
		 * {@code null} if this {@code WildcardType} has no lower bound.
		 *
		 * @param newType - the {@link WildcardType} for which we will find the
		 *          lower bound
		 * @return the lower bound of {@code newType}
		 */
		private static Type getLowerBound(WildcardType newType) {
			Type[] lowerBounds = newType.getLowerBounds();
			if (lowerBounds.length == 0) {
				return null;
			}
			else if (lowerBounds.length == 1) {
				return lowerBounds[0];
			}
			throw new TypeInferenceException(newType + //
				" is an impossible WildcardType. " + //
				"The Java language specification currently prevents multiple lower bounds " + //
				Arrays.toString(lowerBounds)); //
		}
	}
	
	/**
	 * Adapter class wrapping a {@code Map<TypeVariable, TypeMapping>} into a
	 * {@code Map<TypeVariable, Type>} for use with {@link Types} API.
	 */
	private static class TypeVarAssigns implements Map<TypeVariable<?>, Type> {

		private Map<TypeVariable<?>, TypeMapping> map;

		public TypeVarAssigns(Map<TypeVariable<?>, TypeMapping> map) {
			this.map = map;
		}

		@Override
		public boolean containsKey( Object key) {
			return map.containsKey(key);
		}

		@Override
		public Type put(TypeVariable<?> typeVar, Type type) {
			final TypeMapping previousMapping = //
				map.put(typeVar, suitableTypeMapping(typeVar, type, isMalleable(typeVar)));
			return previousMapping == null ? null : previousMapping.getType();
		}

		@Override
		public Type putIfAbsent(TypeVariable<?> typeVar, Type type) {
			if (!map.containsKey(typeVar)) return put(typeVar, type);
			return get(typeVar);
		}

		@Override
		public Type get(Object key) {
			TypeMapping value = map.get(key);
			return value == null ? null : value.getType();
		}
		
		@Override
		public int size() {
			return map.size();
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}

		@Override
		public boolean containsValue(Object value) {
			return map.containsValue(value);
		}

		@Override
		public Type remove(Object key) {
			TypeMapping value = map.remove(key);
			return value == null ? null : value.getType();
		}

		@Override
		public void putAll(Map<? extends TypeVariable<?>, ? extends Type> m) {
			for (Map.Entry<? extends TypeVariable<?>, ? extends Type> e : m.entrySet()) {
				put(e.getKey(), e.getValue());
			}
		}

		@Override
		public void clear() {
			map.clear();
		}

		@Override
		public Set<TypeVariable<?>> keySet() {
			return map.keySet();
		}

		@Override
		public Collection<Type> values() {
			return map.values().stream().map(v -> v.getType()).collect(Collectors.toList());
		}

		@Override
		public Set<Entry<TypeVariable<?>, Type>> entrySet() {
			return map.entrySet().stream().map(e -> {
				return new Map.Entry<TypeVariable<?>, Type>() {

					@Override
					public TypeVariable<?> getKey() {
						return e.getKey();
					}

					@Override
					public Type getValue() {
						return get(getKey());
					}

					@Override
					public Type setValue(Type value) {
						return put(getKey(), value);
					}
				};
			}).collect(Collectors.toSet());
		}

		private boolean isMalleable(TypeVariable<?> typeVar) {
			return map.containsKey(typeVar) && map.get(typeVar).malleable;
		}
	}
}
