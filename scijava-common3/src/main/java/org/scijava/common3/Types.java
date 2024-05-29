/*
 * #%L
 * Common functionality widely used across SciJava modules.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
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

package org.scijava.common3;

// Portions of this class were adapted from the
// org.apache.commons.lang3.reflect.TypeUtils and
// org.apache.commons.lang3.Validate classes of
// Apache Commons Lang 3.4, which is distributed
// under the Apache 2 license.
// See lines below starting with "BEGIN FORK OF APACHE COMMONS LANG".
//
// Portions of this class were adapted from the GenTyRef project
// by Wouter Coekaerts, which is distributed under the Apache 2 license.
// See lines below starting with "BEGIN FORK OF GENTYREF".
//
// See NOTICE.txt for further details on third-party licenses.

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import org.scijava.common3.Classes;

/**
 * Utility class for working with generic types, fields and methods.
 * <p>
 * Logic and inspiration were drawn from the following excellent libraries:
 * <ul>
 * <li>Google Guava's {@code com.google.common.reflect} package.</li>
 * <li>Apache Commons Lang 3's {@code org.apache.commons.lang3.reflect} package.
 * </li>
 * <li><a href="https://github.com/coekarts/gentyref">GenTyRef</a> (Generic Type
 * Reflector), a library for runtime generic type introspection.</li>
 * </ul>
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 * @author David Kolb
 */
public final class Types {

	private Types() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Gets a string representation of the given type.
	 *
	 * @param t Type whose name is desired.
	 * @return The name of the given type.
	 */
	public static String name(final Type t) {
		if (t instanceof Class) {
			final Class<?> c = (Class<?>) t;
			return c.isArray() ? name(component(c)) + "[]" : c.getName();
		}
		return t.toString();
	}

	/**
	 * Gets the (first) raw class of the given type.
	 * <ul>
	 * <li>If the type is a {@code Class} itself, the type itself is returned.
	 * </li>
	 * <li>If the type is a {@link ParameterizedType}, the raw type of the
	 * parameterized type is returned.</li>
	 * <li>If the type is a {@link GenericArrayType}, the returned type is the
	 * corresponding array class. For example: {@code List<Integer>[] => List[]}.
	 * </li>
	 * <li>If the type is a type variable or wildcard type, the raw type of the
	 * first upper bound is returned. For example:
	 * {@code <X extends Foo & Bar> => Foo}.</li>
	 * </ul>
	 * <p>
	 * If you want <em>all</em> raw classes of the given type, use {@link #raws}.
	 * </p>
	 *
	 * @param type The type from which to discern the (first) raw class.
	 * @return The type's first raw class.
	 */
	public static Class<?> raw(final Type type) {
		if (type == null) return null;
		if (type instanceof Class) return (Class<?>) type;
		final List<Class<?>> c = raws(type);
		if (c.isEmpty()) return null;
		return c.get(0);
	}

	/**
	 * Gets all raw classes corresponding to the given type.
	 * <p>
	 * For example, a type parameter {@code A extends Number & Iterable} will
	 * return both {@link Number} and {@link Iterable} as its raw classes.
	 * </p>
	 *
	 * @param type The type from which to discern the raw classes.
	 * @return List of the type's raw classes.
	 * @see #raw
	 */
	public static List<Class<?>> raws(final Type type) {
		if (type == null) return null;
		return GenericTypeReflector.getUpperBoundClassAndInterfaces(type);
	}

	/**
	 * Gets the array type&mdash;which might be a {@link Class} or a
	 * {@link GenericArrayType} depending on the argument&mdash;corresponding to
	 * the given element type.
	 * <p>
	 * For example, {@code arrayType(double.class)} returns {@code double[].class}.
	 * </p>
	 * <p>
	 * This is the opposite of {@link #component(Type)}.
	 * </p>
	 *
	 * @param componentType The type of elements which the array possesses
	 * @see #component
	 */
	public static Type array(final Type componentType) {
		if (componentType == null) return null;
		if (componentType instanceof Class) {
			return Classes.array((Class<?>) componentType);
		}
		return new TypeUtils.GenericArrayTypeImpl(componentType);
	}

	/**
	 * Gets the component type of the given array type, or null if not an array.
	 * <p>
	 * If you have a {@link Class}, you can call {@link Class#getComponentType()}
	 * for a narrower return type.
	 * </p>
	 * <p>
	 * This is the opposite of {@link #array(Type)}.
	 * </p>
	 */
	public static Type component(final Type type) {
		if (type instanceof Class) {
			return ((Class<?>) type).getComponentType();
		}
		if (type instanceof GenericArrayType) {
			return ((GenericArrayType) type).getGenericComponentType();
		}
		return null;
	}

	/**
	 * Returns the exact generic type of the given field, as viewed from the
	 * given type. This may be narrower than what {@link Field#getGenericType()}
	 * returns, if the field is declared in a superclass, or {@code type} has a
	 * type parameter that is used in the type of the field.
	 * <p>
	 * For example, suppose we have the following three classes:
	 * </p>
	 *
	 * <pre>
	 * public class Thing&lt;T&gt; {
	 * 	 public T thing;
	 * }
	 *
	 * public class NumberThing&lt;N extends Number&gt; extends Thing&lt;N&gt; {}
	 *
	 * public class IntegerThing extends NumberThing&lt;Integer&gt; {}
	 * </pre>
	 *
	 * Then this method operates as follows:
	 *
	 * <pre>
	 * field = Classes.field(Thing.class, "thing");
	 *
	 * field.getType(); // Object
	 * field.getGenericType(); // T
	 *
	 * Types.typeOf(field, Thing.class); // T
	 * Types.typeOf(field, NumberThing.class); // N extends Number
	 * Types.typeOf(field, IntegerThing.class); // Integer
	 * </pre>
	 */
	public static Type typeOf(final Field field, final Class<?> type) {
		final Type pType = parameterize(type);
		return GenericTypeReflector.getExactFieldType(field, pType);
	}

	/**
	 * Determines the greatest common supertype of all types in the input array.
	 *
	 * @param types The array of subtypes, for which the supertype is found.
	 * @param wildcardSingleIface The default behavior, when the method finds
	 *          multiple suitable interfaces, is to create a wildcard with all
	 *          suitable interfaces as upper bounds. If this {@code boolean} is
	 *          set to true, a wildcard will also be created if only one suitable
	 *          interface is found. If false, the return will be the interface
	 *          itself (i.e. not a wildcard).
	 * @return a {@link Type} that is a supertype of all {@link Type}s in the
	 *         types array.
	 */
	public static Type superTypeOf(Type[] types,
		final boolean wildcardSingleIface)
	{
		types = Arrays.stream(types).filter(t -> !(Any.is(t))).toArray(Type[]::new);

		// return answer quick if the answer is trivial
		if (types.length == 0) return null;
		if (types.length == 1) return types[0];

		// make sure that all types are supported
		// TODO: are there any other types that aren't fully supported?
		for (int i = 0; i < types.length; i++) {
			if (types[i] instanceof TypeVariable<?>) {
				TypeVariable<?> typeVar = (TypeVariable<?>) types[i];
				types[i] = superTypeOf(typeVar.getBounds(), false);
			}
			// wildcards themselves are not supported, however we know that the
			// greatest superType of any wildcard is its upper bound
			if (types[i] instanceof WildcardType) {
				WildcardType wildcard = (WildcardType) types[i];
				types[i] = superTypeOf(wildcard.getUpperBounds(), false);
			}
		}

		// We can effectively find the greatest common super type by assuming that
		// either:
		// 1) superType extends a superclass of all types in types (this check is
		// symmetric)
		// 2) superType implements an interface implemented by all types in typws
		// (this
		// check is symmetric)

		Type superType = types[0];

		// 1)
		// prefer any non-Object superClass over implemented interfaces.
		while (raw(superType) != Object.class) {
			// check assignability of superclass with type params
			Type pType = superTypeOf(types[0], raw(superType));
			if (isAssignable(types, pType) == -1) return superType;

			// if we have a parameterizedtype whose rawtype is assignable to all of
			// types,
			// we just have to resolve each of pType's type variables.
			if (pType instanceof ParameterizedType && //
				isAssignable(types, raw(superType)) == -1)
			{
				ParameterizedType[] castedTypes = new ParameterizedType[types.length];
				castedTypes[0] = (ParameterizedType) pType;
				// generate parameterizedTypes of each type in types w.r.t. supertype
				for (int i = 1; i < castedTypes.length; i++) {
					Type t = superTypeOf(types[i], raw(superType));
					if (!(t instanceof ParameterizedType)) continue;
					castedTypes[i] = (ParameterizedType) t;
				}
				// resolve each of the i type variables of superType using
				// greatestCommonSupertype
				Type[] resolvedTypeArgs = new Type[castedTypes[0]
					.getActualTypeArguments().length];
				for (int i = 0; i < resolvedTypeArgs.length; i++) {
					Type[] typeVarsI = new Type[types.length];
					for (int j = 0; j < typeVarsI.length; j++) {
						typeVarsI[j] = castedTypes[j].getActualTypeArguments()[i];
					}
					// If each of these types implements some recursive interface, e.g.
					// Comparable, the best we can do is return an unbounded wildcard.
					if (Arrays.equals(types, typeVarsI)) resolvedTypeArgs[i] = wildcard();
					else {
						final Type[] upper = { superTypeOf(typeVarsI, true) };
						final Type[] lower = new Type[0];
						resolvedTypeArgs[i] = wildcard(upper, lower);
					}
				}

				// return supertype parameterized with the resolved type args
				return parameterize(raw(superType), resolvedTypeArgs);
			}
			if (raw(superType).isInterface()) break;
			superType = superTypeOf(superType, raw(superType).getSuperclass());
		}

		// 2)
		List<Type> sharedInterfaces = new ArrayList<>();
		Queue<Type> superInterfaces = new LinkedList<>();
		if (raw(types[0]).isInterface()) superInterfaces.add(types[0]);
		else Collections.addAll(superInterfaces, raw(types[0]).getGenericInterfaces());
		while (!superInterfaces.isEmpty()) {
			Type type = superInterfaces.remove();
			Type pType = superTypeOf(types[0], raw(type));
			if (isAssignable(types, pType) == -1) {
				sharedInterfaces.add(pType);
				continue;
			}
			// if we have a parameterizedtype whose rawtype is assignable to all of
			// types,
			// we just have to resolve each of pType's type variables.
			if (pType instanceof ParameterizedType && //
				isAssignable(types, raw(pType)) == -1)
			{
				ParameterizedType[] castedTypes = new ParameterizedType[types.length];
				castedTypes[0] = (ParameterizedType) pType;
				// generate parameterizedTypes of each type in types w.r.t. supertype
				for (int i = 1; i < castedTypes.length; i++) {
					Type t = superTypeOf(types[i], raw(pType));
					if (!(t instanceof ParameterizedType)) continue;
					castedTypes[i] = (ParameterizedType) t;
				}
				// resolve each of the i type variables of pType using
				// greatestCommonSupertype
				Type[] resolvedTypeArgs = new Type[castedTypes[0]
					.getActualTypeArguments().length];
				for (int i = 0; i < resolvedTypeArgs.length; i++) {
					Type[] typeVarsI = new Type[types.length];
					for (int j = 0; j < typeVarsI.length; j++) {
						typeVarsI[j] = castedTypes[j].getActualTypeArguments()[i];
					}
					// If each of these types implements some recursive interface, e.g.
					// Comparable,
					// the best we can do is return an unbounded wildcard.
					if (Arrays.equals(types, typeVarsI)) resolvedTypeArgs[i] = wildcard();
					else resolvedTypeArgs[i] = superTypeOf(typeVarsI, true);
				}

				// return supertype parameterized with the resolved type args
				return parameterize(raw(pType), resolvedTypeArgs);
			}

			// If this interface is not a supertype of all of types, maybe one of its
			// inherited interfaces is. However, we don't want to keep searching
			// through the interface hierarchy if we have found a type that satisfies
			// all of the types. Thus, we stop adding to the list if we have found at
			// least one satisfying interface. Note that this does not prevent
			// multiple satisfying interfaces at the same depth from being found.
			if (sharedInterfaces.isEmpty()){
				Collections.addAll(superInterfaces, raw(type).getGenericInterfaces());
			}
		}
		if (sharedInterfaces.size() == 1 && !wildcardSingleIface) {
			return sharedInterfaces.get(0);
		}
		else if (!sharedInterfaces.isEmpty()) {
			// TODO: such a wildcard is technically illegal as a result of current
			// Java language specifications. See
			// https://stackoverflow.com/questions/6643241/why-cant-you-have-multiple-interfaces-in-a-bounded-wildcard-generic
			// Consider a wildcard extending a typeVar that extends multiple
			// interfaces?
			return wildcard(sharedInterfaces.toArray(new Type[] {}), new Type[] {});
		}
		return Object.class;
	}

	/**
	 * Obtains the type parameters of {@link Type} {@code src} <b>with respect
	 * to</b> the {@link Class} {@code dest}. When {@code src} has no type
	 * parameters (or is not a subclass of {@code dest}), an empty array is
	 * returned.
	 *
	 * @param src - the {@code Type} whose type parameters will be returned.
	 * @param superclass - the {@code Class} against which we want the type
	 *          parameters of {@code src}
	 * @return an array of {@code Type}s denoting the type
	 */
	public static Type[] typeParamsOf(Type src, Class<?> superclass) {
		// only classes and ParameterizedTypes can have type parameters
		if (!(src instanceof Class || src instanceof ParameterizedType || src instanceof TypeVariable))
			return new Type[0];
		try {
			Type superSrc = superTypeOf(src, superclass);
			if (superSrc instanceof ParameterizedType)
				return ((ParameterizedType) superSrc).getActualTypeArguments();
			return typeParamsOfClass(raw(src), superclass);
		}
		catch (AssertionError e) {
			return new Type[0];
		}
	}

	/**
	 * Finds the type parameters of the most specific super type of the specified
	 * {@code subType} whose erasure is the specified {@code superErasure}. Hence,
	 * will return the type parameters of {@code superErasure} possibly narrowed
	 * down by {@code subType}. If {@code superErasure} is not a super type of
	 * {@code subType}, an empty array will be returned.
	 *
	 * @param subType the type to narrow down type parameters
	 * @param superErasure the erasure of a super type of {@code subType} from
	 *          which to get the parameters
	 * @return type parameters of {@code superErasure} possibly narrowed down by
	 *         {@code subType}, or empty type array if no exists or
	 *         {@code superErasure} is not a super type of subtype
	 */
	private static Type[] typeParamsOfClass(Class<?> subType, Class<?> superErasure) {
		Type pt = parameterize(subType);
		Type superType = superTypeOf(pt, superErasure);
		if (superType instanceof ParameterizedType) {
			return ((ParameterizedType) superType).getActualTypeArguments();
		}
		return new Type[0];
	}

	/**
	 * Discerns whether it would be legal to assign a reference of type
	 * {@code source} to a reference of type {@code target}.
	 *
	 * @param source The type from which assignment is desired.
	 * @param target The type to which assignment is desired.
	 * @return True if the source is assignable to the target.
	 * @throws NullPointerException if {@code target} is null.
	 * @see Class#isAssignableFrom(Class)
	 */
	public static boolean isAssignable(final Type source, final Type target) {
		// HACK: Workaround for possible bug in TypeUtils.isAssignable, which
		// returns false if one wants to assign primitives to their wrappers and
		// the other way around
		if (source instanceof Class && target instanceof Class) {
			final Class<?> boxedSource = Classes.box((Class<?>) source);
			final Class<?> boxedTarget = Classes.box((Class<?>) target);
			return TypeUtils.isAssignable(boxedSource, boxedTarget);
		}
		return TypeUtils.isAssignable(source, target);
	}

	/**
	 * <p>
	 * Checks if the subject type may be implicitly cast to the target type
	 * following the Java generics rules.
	 * </p>
	 *
	 * @param type the subject type to be assigned to the target type
	 * @param toType the target type
	 * @param typeVarAssigns optional map of type variable assignments
	 * @return {@code true} if {@code type} is assignable to {@code toType}.
	 */
	public static boolean isAssignable(final Type type, final Type toType,
		final Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		// Workaround for possible bug in TypeUtils.isAssignable, which returns
		// false if one wants to assign primitives to their wrappers and the other
		// way around
		if (type instanceof Class && toType instanceof Class) {
			return TypeUtils.isAssignable(Classes.box((Class<?>) type),
				Classes.box((Class<?>) toType));
		}
		return TypeUtils.isAssignable(type, toType, typeVarAssigns);
	}

	/**
	 * Checks whether the given object can be cast to the specified type.
	 *
	 * @return true If the destination class is assignable from the source
	 *         object's class, or if the source object is null and destination
	 *         class is non-null.
	 * @see #cast(Object, Class)
	 */
	public static boolean isInstance(final Object obj, final Class<?> dest) {
		if (dest == null) return false;
		return obj == null || dest.isInstance(obj);
	}

	/**
	 * Checks if the given {@link Type} is recursively typed: that is if it's a
	 * {@link ParameterizedType} with a self-referential type parameter.
	 *
	 * @param type Type of interest to interrogate
	 * @return {@code True} if the given type is recursively parameterized,
	 *         {@code False} otherwise
	 */
	public static boolean isRecursive(final Type type) {
		// type = Foo<T extends Foo<T>>
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
			for (Type arg : actualTypeArguments) {
				if (arg instanceof TypeVariable) {
					// arg = T extends Foo<T>
					TypeVariable<?> argVar = (TypeVariable<?>) arg;
					for (Type bound : argVar.getBounds()) {
						// bound is Foo<T>
						if (bound.equals(type)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Helper method to detect if a particular bound, {@code typeBound}, of a
	 * reference type, {@code refType}, is a recursive type parameter: that is, if
	 * {@code typeBound} itself has a type argument equal to {@code refType}.
	 *
	 * @param refType Base type to check
	 * @param typeBound A bound of {@code refType}
	 * @return True if {@code typeBound} is parameterized with a type argument
	 *         that is itself equal to {@code refType}
	 */
	public static boolean isRecursiveBound(final Type refType,
		final Type typeBound)
	{
		if (typeBound instanceof ParameterizedType) {
			for (Type boundArg : ((ParameterizedType) typeBound)
				.getActualTypeArguments())
			{
				if (Objects.equals(refType, boundArg)) return true;
			}
		}
		return false;
	}

	/**
	 * Determines whether or not specified types satisfy the bounds of their
	 * mapped type variables. When a type parameter extends another (such as
	 * {@code <T, S extends T>}), uses another as a type parameter (such as
	 * {@code <T, S extends Comparable>>}), or otherwise depends on another type
	 * variable to be specified, the dependencies must be included in
	 * {@code typeVarAssigns}.
	 *
	 * @param typeVarAssigns specifies the potential types to be assigned to the
	 *          type variables, not {@code null}.
	 * @return whether the types can be assigned to their respective type
	 *         variables.
	 */
	public static boolean varsSatisfied(
		final Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		return TypeUtils.typesSatisfyVariables(typeVarAssigns);
	}

	/**
	 * Casts the given object to the specified type, or null if the types are
	 * incompatible.
	 */
	public static <T> T cast(final Object src, final Class<T> dest) {
		if (!isInstance(src, dest)) return null;
		@SuppressWarnings("unchecked")
		final T result = (T) src;
		return result;
	}

	/**
	 * Converts the given string value to an enumeration constant of the specified
	 * type.
	 *
	 * @param name The value to convert.
	 * @param dest The type of the enumeration constant.
	 * @return The converted enumeration constant.
	 * @throws IllegalArgumentException if the type is not an enumeration type, or
	 *           has no such constant.
	 */
	public static <T> T enumValue(final String name, final Class<T> dest) {
		if (!dest.isEnum()) {
			throw new IllegalArgumentException("Not an enum type: " + name(dest));
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final Enum result = Enum.valueOf((Class) dest, name);
		@SuppressWarnings("unchecked")
		final T typedResult = (T) result;
		return typedResult;
	}

	/**
	 * Creates a new {@link ParameterizedType} of the given class together with
	 * the specified type arguments.
	 *
	 * @param rawType The class of the {@link ParameterizedType}.
	 * @param typeArgs The type arguments to use in parameterizing it.
	 * @return The newly created {@link ParameterizedType}.
	 */
	public static ParameterizedType parameterize(final Class<?> rawType,
		final Type[] typeArgs)
	{
		return parameterize(rawType, rawType.getDeclaringClass(), typeArgs);
	}

	/**
	 * Creates a new {@link ParameterizedType} of the given class together with
	 * the specified type arguments.
	 *
	 * @param rawType The class of the {@link ParameterizedType}.
	 * @param ownerType The owner type of the parameterized class.
	 * @param typeArgs The type arguments to use in parameterizing it.
	 * @return The newly created {@link ParameterizedType}.
	 */
	public static ParameterizedType parameterize(final Class<?> rawType,
		final Type ownerType, final Type[] typeArgs)
	{
		return new TypeUtils.ParameterizedTypeImpl(rawType, ownerType, typeArgs);
	}

	/**
	 * Creates a new {@link WildcardType} with the given upper bound(s).
	 *
	 * @return The newly created {@link WildcardType}.
	 */
	public static WildcardType wildcard(Type... upperBounds) {
		return wildcard(upperBounds, null);
	}

	/**
	 * Creates a new {@link WildcardType} with the given upper and/or lower
	 * bounds.
	 *
	 * @param upperBounds Upper bounds of the wildcard, or null for none.
	 * @param lowerBounds Lower bounds of the wildcard, or null for none.
	 * @return The newly created {@link WildcardType}.
	 */
	public static WildcardType wildcard(final Type[] upperBounds,
		final Type[] lowerBounds)
	{
		return new TypeUtils.WildcardTypeImpl(upperBounds, lowerBounds);
	}

	/**
	 * Learn, recursively, whether any of the type parameters associated with
	 * {@code type} are bound to variables.
	 *
	 * @param type the type to check for type variables
	 * @return boolean
	 */
	public static boolean containsTypeVars(final Type type) {
		return TypeUtils.containsTypeVariables(type);
	}

	/**
	 * Gets the type arguments of a class/interface based on a subtype. For
	 * instance, this method will determine that both of the parameters for the
	 * interface {@link Map} are {@link Object} for the subtype
	 * {@link java.util.Properties Properties} even though the subtype does not
	 * directly implement the {@code Map} interface.
	 * <p>
	 * This method returns {@code null} if {@code type} is not assignable to
	 * {@code toClass}. It returns an empty map if none of the classes or
	 * interfaces in its inheritance hierarchy specify any type arguments.
	 * </p>
	 * <p>
	 * A side effect of this method is that it also retrieves the type arguments
	 * for the classes and interfaces that are part of the hierarchy between
	 * {@code type} and {@code toClass}. So with the above example, this method
	 * will also determine that the type arguments for {@link java.util.Hashtable
	 * Hashtable} are also both {@code Object}. In cases where the interface
	 * specified by {@code toClass} is (indirectly) implemented more than once
	 * (e.g. where {@code toClass} specifies the interface
	 * {@link java.lang.Iterable Iterable} and {@code type} specifies a
	 * parameterized type that implements both {@link java.util.Set Set} and
	 * {@link java.util.Collection Collection}), this method will look at the
	 * inheritance hierarchy of only one of the implementations/subclasses; the
	 * first interface encountered that isn't a subinterface to one of the others
	 * in the {@code type} to {@code toClass} hierarchy.
	 * </p>
	 *
	 * @param type the type from which to determine the type parameters of
	 *          {@code toClass}
	 * @param toClass the class whose type parameters are to be determined based
	 *          on the subtype {@code type}
	 * @return a {@code Map} of the type assignments for the type variables in
	 *         each type in the inheritance hierarchy from {@code type} to
	 *         {@code toClass} inclusive.
	 */
	public static Map<TypeVariable<?>, Type> args(final Type type,
		final Class<?> toClass)
	{
		return TypeUtils.getTypeArguments(type, toClass);
	}

	/**
	 * Tries to determine the type arguments of a class/interface based on a super
	 * parameterized type's type arguments. This method is the inverse of
	 * {@link #args(Type, Class)} which gets a class/interface's type arguments
	 * based on a subtype. It is far more limited in determining the type
	 * arguments for the subject class's type variables in that it can only
	 * determine those parameters that map from the subject {@link Class} object
	 * to the supertype.
	 * <p>
	 * Example: {@link java.util.TreeSet TreeSet} sets its parameter as the
	 * parameter for {@link java.util.NavigableSet NavigableSet}, which in turn
	 * sets the parameter of {@link java.util.SortedSet}, which in turn sets the
	 * parameter of {@link Set}, which in turn sets the parameter of
	 * {@link java.util.Collection}, which in turn sets the parameter of
	 * {@link java.lang.Iterable}. Since {@code TreeSet}'s parameter maps
	 * (indirectly) to {@code Iterable}'s parameter, it will be able to determine
	 * that based on the super type {@code Iterable<? extends
	 * Map<Integer, ? extends Collection<?>>>}, the parameter of {@code TreeSet}
	 * is {@code ? extends Map<Integer, ? extends Collection<?>>}.
	 * </p>
	 *
	 * @param c the class whose type parameters are to be determined, not
	 *          {@code null}
	 * @param superType the super type from which {@code c}'s type arguments are
	 *          to be determined, not {@code null}
	 * @return a {@code Map} of the type assignments that could be determined for
	 *         the type variables in each type in the inheritance hierarchy from
	 *         {@code type} to {@code c} inclusive.
	 */
	public static Map<TypeVariable<?>, Type> args(final Class<?> c,
		final ParameterizedType superType)
	{
		return TypeUtils.determineTypeArguments(c, superType);
	}

	/**
	 * Create a parameterized type instance.
	 *
	 * @param raw the raw class to create a parameterized type instance for
	 * @param typeVarAssigns the mapping used for parameterization
	 * @return {@link ParameterizedType}
	 */
	public static ParameterizedType parameterize(final Class<?> raw,
		final Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		return TypeUtils.parameterize(raw, typeVarAssigns);
	}

	/**
	 * Finds the most specific supertype of {@code type} whose erasure is
	 * {@code searchClass}. In other words, returns a type representing the
	 * class {@code searchClass} plus its exact type parameters in
	 * {@code type}.
	 * <ul>
	 * <li>Returns an instance of {@link ParameterizedType} if
	 * {@code searchClass} is a real class or interface and {@code type} has
	 * parameters for it</li>
	 * <li>Returns an instance of {@link GenericArrayType} if
	 * {@code searchClass} is an array type, and {@code type} has type
	 * parameters for it</li>
	 * <li>Returns an instance of {@link Class} if {@code type} is a raw type,
	 * or has no type parameters for {@code searchClass}</li>
	 * <li>Returns null if {@code searchClass} is not a superclass of type.
	 * </li>
	 * </ul>
	 * <p>
	 * For example, with
	 * {@code class StringList implements List<String>},
	 * {@code superTypeOf(StringList.class, Collection.class)} returns a
	 * {@link ParameterizedType} representing {@code Collection<String>}.
	 * </p>
	 *
	 * @param type
	 * @param searchClass
	 */
	public static Type superTypeOf(final Type type, final Class<?> searchClass) {
		return GenericTypeReflector.getExactSuperType(type, searchClass);
	}

	/**
	 * Map type vars in specified type list to types using the specified map. In
	 * doing so, type vars mapping to other type vars will not be followed but
	 * just replaced.
	 *
	 * @param typesToMap
	 * @param typeVarAssigns
	 */
	public static Type[] unroll(Type[] typesToMap,
		Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		return Arrays.stream(typesToMap)
			.map(type -> unroll(type, typeVarAssigns))
			.toArray(Type[]::new);
	}

	/**
	 * Map type vars in the specified type to a type using the specified map. In
	 * doing so, type vars mapping to other type vars will not be followed but
	 * just replaced.
	 *
	 * @param typeToMap
	 * @param typeVarAssigns
	 */
	public static Type unroll(Type typeToMap,
		Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		return unroll(typeVarAssigns, typeToMap, false);
	}

	/**
	 * Get a type representing {@code type} with variable assignments
	 * "unrolled."
	 *
	 * @param typeArguments as from {@link #args(Type, Class)}
	 * @param type the type to unroll variable assignments for
	 * @param followTypeVars whether a {@link TypeVariable} should be
	 *          recursively followed if it maps to another {@link TypeVariable},
	 *          or if it should be just replaced by the mapping
	 * @return Type
	 */
	public static Type unroll(
		Map<TypeVariable<?>, Type> typeArguments,
		final Type type, boolean followTypeVars
	) {
		return TypeUtils.unrollVariables(typeArguments, type, followTypeVars);
	}

	/**
	 * Tries to parameterize the given raw type with its type parameters. If type
	 * is not raw, it will be returned. Otherwise, a parameterized type containing
	 * the type variables of the specified is returned.
	 *
	 * @param rawType the raw type to parameterize
	 * @return a parameterized type, or the class itself if not a parameterizable class
	 */
	public static Type parameterize(Class<?> rawType) {
		TypeVariable<?>[] typeParams = rawType.getTypeParameters();
		if (typeParams.length == 0) return rawType;
		return parameterize(rawType, typeParams);
	}

	/**
	 * Returns the exact parameter types of the given method in the given type.
	 * This may be different from {@code m.getGenericParameterTypes()} when the
	 * method was declared in a superclass, or {@code type} has a type
	 * parameter that is used in one of the parameters, or {@code type} is a
	 * raw type.
	 *
	 * @param m
	 * @param type
	 */
	public static Type[] paramTypesOf(final Method m, final Type type) {
		return GenericTypeReflector.getExactParameterTypes(m, type);
	}
	// START HERE: fieldType is not named exactFieldType, even though it uses that method.
	// So these exact*Type* methods should be named consistently with fieldType somehow.
	// Also consider how to rename areVarsSatisfied, which is a mouthful.
	// Then, I want to group the Types methods by area of functionality somehow.

	/**
	 * Returns the exact return type of the given method in the given type. This
	 * may be different from {@code m.getGenericReturnType()} when the method
	 * was declared in a superclass, or {@code type} has a type parameter that
	 * is used in the return type, or {@code type} is a raw type.
	 *
	 * @param m
	 * @param type
	 */
	public static Type returnTypeOf(final Method m, final Type type) {
		return GenericTypeReflector.getExactReturnType(m, type);
	}

	// -- Helper methods --

	/**
	 * Discerns whether it would be legal to assign a group of references of types
	 * {@code source} to a reference of type {@code target}.
	 *
	 * @param sources The types from which assignment is desired.
	 * @param target The type to which assignment is desired.
	 * @return the index of the first type not assignable to {@code target}. If
	 *         all types are assignable, returns {@code -1}
	 * @throws NullPointerException if {@code target} is null.
	 * @see Class#isAssignableFrom(Class)
	 */
	private static int isAssignable(final Type[] sources, final Type target) {
		for (int i = 0; i < sources.length; i++) {
			if (!isAssignable(sources[i], target)) return i;
		}
		return -1;
	}

	// -- BEGIN FORK OF APACHE COMMONS LANG 3.4 CODE --

	/*
	 * Licensed to the Apache Software Foundation (ASF) under one or more
	 * contributor license agreements.  See the NOTICE file distributed with
	 * this work for additional information regarding copyright ownership.
	 * The ASF licenses this file to You under the Apache License, Version 2.0
	 * (the "License"); you may not use this file except in compliance with
	 * the License.  You may obtain a copy of the License at
	 *
	 *      http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */

	/**
	 * <p>
	 * Utility methods focusing on type inspection, particularly with regard to
	 * generics.
	 * </p>
	 *
	 * @since 3.0
	 * @version $Id: TypeUtils.java 1606051 2014-06-27 12:22:17Z ggregory $
	 */
	@SuppressWarnings("unused")
	private static class TypeUtils {

		/**
		 * {@link WildcardType} builder.
		 *
		 * @since 3.2
		 */
		private static class WildcardTypeBuilder {

			/**
			 * Constructor
			 */
			private WildcardTypeBuilder() {}

			private Type[] upperBounds;
			private Type[] lowerBounds;

			/**
			 * Specify upper bounds of the wildcard type to build.
			 *
			 * @param bounds to set
			 * @return {@code this}
			 */
			public WildcardTypeBuilder withUpperBounds(final Type... bounds) {
				this.upperBounds = bounds;
				return this;
			}

			/**
			 * Specify lower bounds of the wildcard type to build.
			 *
			 * @param bounds to set
			 * @return {@code this}
			 */
			public WildcardTypeBuilder withLowerBounds(final Type... bounds) {
				this.lowerBounds = bounds;
				return this;
			}

			public WildcardType build() {
				return new WildcardTypeImpl(upperBounds, lowerBounds);
			}
		}

		/**
		 * GenericArrayType implementation class.
		 *
		 * @since 3.2
		 */
		private static final class GenericArrayTypeImpl implements
			GenericArrayType
		{

			private final Type componentType;

			/**
			 * Constructor
			 *
			 * @param componentType of this array type
			 */
			private GenericArrayTypeImpl(final Type componentType) {
				this.componentType = componentType;
			}

			@Override
			public Type getGenericComponentType() {
				return componentType;
			}

			@Override
			public String toString() {
				return TypeUtils.toString(this);
			}

			@Override
			public boolean equals(final Object obj) {
				return obj == this || obj instanceof GenericArrayType && TypeUtils
					.equals(this, (GenericArrayType) obj);
			}

			@Override
			public int hashCode() {
				int result = 67 << 4;
				result |= componentType.hashCode();
				return result;
			}
		}

		/**
		 * ParameterizedType implementation class.
		 *
		 * @since 3.2
		 */
		private static final class ParameterizedTypeImpl implements
			ParameterizedType
		{

			private final Class<?> raw;
			private final Type useOwner;
			private final Type[] typeArguments;

			/**
			 * Constructor
			 *
			 * @param raw type
			 * @param useOwner owner type to use, if any
			 * @param typeArguments formal type arguments
			 */
			private ParameterizedTypeImpl(final Class<?> raw, final Type useOwner,
				final Type[] typeArguments)
			{
				this.raw = raw;
				this.useOwner = useOwner;
				this.typeArguments = typeArguments;
			}

			@Override
			public Type getRawType() {
				return raw;
			}

			@Override
			public Type getOwnerType() {
				return useOwner;
			}

			@Override
			public Type[] getActualTypeArguments() {
				return typeArguments.clone();
			}

			@Override
			public String toString() {
				return TypeUtils.toString(this);
			}

			@Override
			public boolean equals(final Object obj) {
				return obj == this || obj instanceof ParameterizedType && TypeUtils
					.equals(this, (ParameterizedType) obj);
			}

			@Override
			public int hashCode() {
				// NB: Aligned with Guava's ParameterizedTypeImpl's hashCode function.
				return (useOwner == null ? 0 :
					useOwner.hashCode()) ^
					Arrays.asList(typeArguments).hashCode() ^
					raw.hashCode();
			}
		}

		/**
		 * WildcardType implementation class.
		 *
		 * @since 3.2
		 */
		private static final class WildcardTypeImpl implements WildcardType {

			private static final Type[] EMPTY_BOUNDS = new Type[0];

			private final Type[] upperBounds;
			private final Type[] lowerBounds;

			/**
			 * Constructor
			 *
			 * @param upperBound of this type
			 * @param lowerBound of this type
			 */
			private WildcardTypeImpl(final Type upperBound, final Type lowerBound) {
				this(upperBound == null ? null : new Type[] { upperBound },
					lowerBound == null ? null : new Type[] { lowerBound });
			}

			/**
			 * Constructor
			 *
			 * @param upperBounds of this type
			 * @param lowerBounds of this type
			 */
			private WildcardTypeImpl(final Type[] upperBounds,
				final Type[] lowerBounds)
			{
				this.upperBounds = upperBounds == null ? EMPTY_BOUNDS : upperBounds;
				this.lowerBounds = lowerBounds == null ? EMPTY_BOUNDS : lowerBounds;
			}

			@Override
			public Type[] getUpperBounds() {
				return upperBounds.clone();
			}

			@Override
			public Type[] getLowerBounds() {
				return lowerBounds.clone();
			}

			@Override
			public String toString() {
				return TypeUtils.toString(this);
			}

			@Override
			public boolean equals(final Object obj) {
				return obj == this || obj instanceof WildcardType &&
					TypeUtils.equals(this, (WildcardType) obj);
			}

			@Override
			public int hashCode() {
				int result = 73 << 8;
				result |= Arrays.hashCode(upperBounds);
				result <<= 8;
				result |= Arrays.hashCode(lowerBounds);
				return result;
			}
		}

		/**
		 * A wildcard instance matching {@code ?}.
		 *
		 * @since 3.2
		 */
		public static final WildcardType WILDCARD_ALL = //
			wildcardType().withUpperBounds(Object.class).build();

		/**
		 * <p>
		 * Checks if the subject type may be implicitly cast to the target type
		 * following the Java generics rules. If both types are {@link Class}
		 * objects, the method returns the result of
		 * {@link Class#isAssignableFrom(Class)}.
		 * </p>
		 *
		 * @param type the subject type to be assigned to the target type
		 * @param toType the target type
		 * @return {@code true} if {@code type} is assignable to {@code toType}.
		 * @throws NullPointerException if {@code toType} is null.
		 */
		public static boolean isAssignable(final Type type, final Type toType) {
			if (toType == null) {
				throw new NullPointerException("Destination type is null");
			}
			return isAssignable(type, toType, new HashMap<>());
		}

		/**
		 * <p>
		 * Checks if the subject type may be implicitly cast to the target type
		 * following the Java generics rules.
		 * </p>
		 *
		 * @param type the subject type to be assigned to the target type
		 * @param toType the target type
		 * @param typeVarAssigns optional map of type variable assignments
		 * @return {@code true} if {@code type} is assignable to {@code toType}.
		 */
		private static boolean isAssignable(final Type type, final Type toType,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			if (toType == null || toType instanceof Class) {
				return isAssignable(type, (Class<?>) toType);
			}

			if (toType instanceof ParameterizedType) {
				return isAssignable(type, (ParameterizedType) toType, typeVarAssigns);
			}

			if (toType instanceof GenericArrayType) {
				return isAssignable(type, (GenericArrayType) toType, typeVarAssigns);
			}

			if (toType instanceof WildcardType) {
				return isAssignable(type, (WildcardType) toType, typeVarAssigns);
			}

			if (toType instanceof TypeVariable) {
				return isAssignable(type, (TypeVariable<?>) toType, typeVarAssigns);
			}

			if (Any.is(toType)) {
				return isAssignable(type, (Any) toType, typeVarAssigns);
			}

			throw new IllegalStateException("found an unhandled type: " + toType);
		}

		private static boolean isAssignable(final Type type, final Any toType,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			if (type instanceof TypeVariable) {
				// TODO: do we need to do here what we do with the ParameterizedType?
			}
			if (type instanceof ParameterizedType) {
				// check if any of the type parameters are TypeVariables, and if so
				// bind them to a new Any with the bounds of the TypeVariable.
				Type[] typeParameters = ((ParameterizedType) type)
					.getActualTypeArguments();
				for (Type typeParameter : typeParameters) {
					// we only need to bind TypeVariables that are as of yet unbound.
					if (!(typeParameter instanceof TypeVariable<?>)) continue;
					if (typeVarAssigns.containsKey(typeParameter)) continue;
					TypeVariable<?> typeVar = (TypeVariable<?>) typeParameter;
					typeVarAssigns.put(typeVar, new Any(typeVar.getBounds()));

				}
				return true;
			}

			for (Type upperBound : toType.getUpperBounds()) {
				if (!isAssignable(type, upperBound)) return false;
			}

			for (Type lowerBound : toType.getLowerBounds()) {
				if (!isAssignable(lowerBound, type)) return false;
			}

			return true;
		}

		/**
		 * <p>
		 * Checks if the subject type may be implicitly cast to the target class
		 * following the Java generics rules.
		 * </p>
		 *
		 * @param type the subject type to be assigned to the target type
		 * @param toClass the target class
		 * @return {@code true} if {@code type} is assignable to {@code toClass}.
		 */
		private static boolean isAssignable(final Type type,
			final Class<?> toClass)
		{
			if (type == null) {
				// consistency with ClassUtils.isAssignable() behavior
				return toClass == null || !toClass.isPrimitive();
			}

			// only a null type can be assigned to null type which
			// would have cause the previous to return true
			if (toClass == null) {
				return false;
			}

			// all types are assignable to themselves
			if (toClass.equals(type)) {
				return true;
			}

			if (type.equals(Any.class)) {
				return true;
			}

			if (type instanceof Class) {
				// just comparing two classes
				return type.equals(Any.class) ||
					toClass.isAssignableFrom((Class<?>) type);
			}

			if (type instanceof ParameterizedType) {
				// only have to compare the raw type to the class
				return isAssignable(getRawType((ParameterizedType) type), toClass);
			}

			// *
			if (type instanceof TypeVariable) {
				// if any of the bounds are assignable to the class, then the
				// type is assignable to the class.
				for (final Type bound : ((TypeVariable<?>) type).getBounds()) {
					if (isAssignable(bound, toClass)) {
						return true;
					}
				}

				return false;
			}

			// the only classes to which a generic array type can be assigned
			// are class Object and array classes
			if (type instanceof GenericArrayType) {
				return toClass.equals(Object.class) || toClass.isArray() &&
					isAssignable(((GenericArrayType) type).getGenericComponentType(),
						toClass.getComponentType());
			}

			// wildcard types are not assignable to a class (though one would think
			// "? super Object" would be assignable to Object)
			if (type instanceof WildcardType) {
				return false;
			}

			if (Any.is(type)) return true;

			throw new IllegalStateException("found an unhandled type: " + type);
		}

		/**
		 * <p>
		 * Checks if the subject type may be implicitly cast to the target
		 * parameterized type following the Java generics rules.
		 * </p>
		 *
		 * @param type the subject type to be assigned to the target type
		 * @param toParameterizedType the target parameterized type
		 * @param typeVarAssigns a map with type variables
		 * @return {@code true} if {@code type} is assignable to {@code toType}.
		 */
		private static boolean isAssignable(final Type type,
			final ParameterizedType toParameterizedType,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			if (type == null) {
				return true;
			}

			// only a null type can be assigned to null type which
			// would have cause the previous to return true
			if (toParameterizedType == null) {
				return false;
			}

			// all types are assignable to themselves
			if (toParameterizedType.equals(type)) {
				return true;
			}

			// if type is an Any, do some Any resolution
			if (Any.is(type)) {
				for (Type typeParameter : toParameterizedType.getActualTypeArguments()) {
					if (!(typeParameter instanceof TypeVariable<?>)) continue;
					TypeVariable<?> typeVar = (TypeVariable<?>) typeParameter;
					if (typeVarAssigns.containsKey(typeVar)) continue;
					typeVarAssigns.put(typeVar, new Any(typeVar.getBounds()));
				}
				return true;
			}

			// get the target type's raw type
			final Class<?> toClass = getRawType(toParameterizedType);
			// get the subject type's type arguments including owner type arguments
			// and supertype arguments up to and including the target class.
			final Map<TypeVariable<?>, Type> fromTypeVarAssigns =
				getTypeArguments(type, toClass, typeVarAssigns);

			// null means the two types are not compatible
			if (fromTypeVarAssigns == null) {
				if (type instanceof TypeVariable) {
					TypeVariable<?> typeVar = (TypeVariable<?>) type;
					Type[] bounds = typeVar.getBounds();
					if (typeVarAssigns.containsKey(type)) {
						return isAssignable(typeVarAssigns.get(type), toParameterizedType,
							typeVarAssigns);
					}
					if (bounds.length == 1 && Object.class.equals(bounds[0])) {
						// We have an unbound (as bound is Object) type variable with no
						// assignment in the type variable map. Thus we can bind it to
						// our toType
						typeVarAssigns.put((TypeVariable) type, toParameterizedType);
						return true;
					}
				}
				return false;
			}

			// compatible types, but there's no type arguments. this is equivalent
			// to comparing Map< ?, ? > to Map, and raw types are always assignable
			// to parameterized types.
			if (fromTypeVarAssigns.isEmpty()) {
				return true;
			}

			// get the target type's type arguments including owner type arguments
			final Map<TypeVariable<?>, Type> toTypeVarAssigns =
				getTypeArguments(toParameterizedType, toClass, typeVarAssigns);

			// now to check each type argument
			for (final TypeVariable<?> var : toTypeVarAssigns.keySet()) {
				Type toTypeArg = unrollVariableAssignments(var, toTypeVarAssigns);
				Type fromTypeArg = unrollVariableAssignments(var, fromTypeVarAssigns);

				Type toResolved = resolveTypeParameters(toTypeArg, toTypeVarAssigns);
				Type fromResolved = resolveTypeParameters(fromTypeArg,
					fromTypeVarAssigns);

				Type[] toBounds = determineBounds(var, toTypeArg, toResolved,
					toTypeVarAssigns);
				Type[] fromBounds = determineBounds(var, fromTypeArg, fromResolved,
					fromTypeVarAssigns);

				// if either type is bounded we need to recurse
				if (toBounds != null) {
					for (Type to : toBounds) {
						if (fromBounds != null) {
							for (Type from : fromBounds) {
								if (!isAssignable(from, to, typeVarAssigns)) return false;
							}
						}
						else if (!isAssignable(fromResolved == null ? fromTypeArg
							: fromResolved, to, typeVarAssigns)) return false;
					}
					if (toTypeArg == null && toResolved == null && fromResolved != null &&
						typeVarAssigns != null)
					{
						TypeVariable<?> unbounded = (TypeVariable<?>)
							toTypeVarAssigns.get(var);
						typeVarAssigns.put(unbounded, fromResolved);
					}
					continue;
				}
				if (fromBounds != null) {
					for (Type from : fromBounds) {
						if (!isAssignable(from, toResolved == null ? toTypeArg : toResolved,
							typeVarAssigns)) return false;
					}
					continue;
				}

				// if toResolved and toTypeArg are null, it must be linked to an
				// unbounded type variable.
				// We can then bound it to fromResolved (assuming fromResolved actually
				// represents some type i.e. not null) in toTypeVarAssigns and
				// typeVarAssigns.
				// Effectively toResolved = fromResolved.
				if (toTypeArg == null && toResolved == null && typeVarAssigns != null) {
					// bind unbounded to a concrete type
					if (fromResolved != null) {
						TypeVariable<?> unbounded = (TypeVariable<?>)
							toTypeVarAssigns.get(var);
						typeVarAssigns.put(unbounded, fromResolved);
						toResolved = fromResolved;
					}
					// bind unbounded to another type variable
					else {
						typeVarAssigns.put((TypeVariable<?>) toTypeVarAssigns.get(var),
							fromTypeVarAssigns.get(var));
					}
				}

				// parameters must either be absent from the subject type, within
				// the bounds of the wildcard type, or be an exact match to the
				// parameters of the target type.
				if (fromResolved != null && !fromResolved.equals(toResolved)) {
					// check for anys
					if (Any.is(fromResolved) || Any.is(toResolved)) continue;
					if (fromResolved instanceof ParameterizedType &&
						toResolved instanceof ParameterizedType)
					{
						if (raw(fromResolved) != raw(toResolved)) {
							return false;
						}
						Type[] fromTypes = ((ParameterizedType) fromResolved)
							.getActualTypeArguments();
						Type[] toTypes = ((ParameterizedType) toResolved)
							.getActualTypeArguments();
						for (int i = 0; i < fromTypes.length; i++) {
							if (toTypes[i] instanceof TypeVariable<?> &&
								isAssignable(fromTypes[i], toTypes[i], typeVarAssigns))
							{
								typeVarAssigns.put((TypeVariable<?>) toTypes[i], fromTypes[i]);
								continue;
							}
							if (!(Any.is(fromTypes[i]) || Any.is(toTypes[i]))) return false;
						}
						continue;
					}
					return false;
				}
			}
			return true;
		}

		/**
		 * Helper method to try and find the most appropriate type bounds for a
		 * particular {@code var}.
		 *
		 * @param var the type variable to look up
		 * @param typeArg The {@link #unrollVariableAssignments} result for
		 *          {@code var}
		 * @param resolvedType The {@link #resolveTypeParameters} result for
		 *          {@code typeArg}
		 * @param typeVarAssigns the map used for the look up
		 * @return An array of {@code Types} representing the bounds of the given
		 *         {@code var}
		 */
		private static Type[] determineBounds(TypeVariable<?> var, Type typeArg,
			Type resolvedType, Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			Type[] bounds = null;
			if (resolvedType == null && typeArg == null) {
				typeArg = unrollVariableAssignments(var, typeVarAssigns, true);
				if (typeArg != null) {
					// Definitely have a bounded param
					bounds = ((TypeVariable<?>) typeArg).getBounds();
					Set<Type> nonObjects = new HashSet<>(bounds.length);
					for (Type t : bounds) {
						if (!Object.class.equals(t)) {
							nonObjects.add(t);
						}
					}
					if (nonObjects.isEmpty()) bounds = null;
					else if (nonObjects.size() != bounds.length) {
						bounds = nonObjects.toArray(new Type[nonObjects.size()]);
					}
				}
			}
			else {
				WildcardType wt = null;
				if (resolvedType != null && resolvedType instanceof WildcardType) {
					wt = ((WildcardType) resolvedType);
				}
				else if (typeArg != null && typeArg instanceof WildcardType) {
					wt = ((WildcardType) typeArg);
				}
				if (wt != null) {
					bounds = wt.getUpperBounds();
				}
			}

			if (bounds != null) {
				Set<Type> actualTypes = new HashSet<>(bounds.length);
				Set<Type> parentBounds = new HashSet<>(Arrays.asList(bounds));
				for (Type b : bounds) {
					if (b instanceof ParameterizedType) {
						for (Type t : ((ParameterizedType) b).getActualTypeArguments()) {
							if (t instanceof TypeVariable) {
								Type[] subBounds = ((TypeVariable<?>) t).getBounds();
								for (Type s : subBounds) {
									// This stops recursive types from infinite recursion
									if (parentBounds.contains(s)) {
										actualTypes.add(((ParameterizedType) b).getRawType());
									}
									else {
										actualTypes.add(b);
									}
								}
							}
							else {
								actualTypes.add(b);
							}
						}
					}
					else actualTypes.add(b);

					bounds = actualTypes.toArray(new Type[actualTypes.size()]);
				}
			}
			return bounds;
		}

		private static Type resolveTypeParameters(Type type,
			Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			Type toTypeArg;
			if (!(containsTypeVars(type))) return type;
			if (!(type instanceof ParameterizedType)) return type;
			Type[] toParameters = ((ParameterizedType) type).getActualTypeArguments();
			Type[] toParamsResolved = Arrays.stream(toParameters).map(param -> {
				Type resolved = param;
				while (resolved instanceof TypeVariable<?>) {
					if (!typeVarAssigns.keySet().contains(resolved)) break;
					if (typeVarAssigns.get(resolved).equals(resolved)) break;
					resolved = typeVarAssigns.get(resolved);
				}
				return resolved;
			}).toArray(Type[]::new);
			toTypeArg = parameterize(raw(type), toParamsResolved);
			return toTypeArg;
		}

		/**
		 * As {@link #unrollVariableAssignments(TypeVariable, Map, boolean)} with
		 * {@code acceptBounds} defaulting to {@code false}, so that only true
		 * assignments will be unrolled.
		 */
		private static Type unrollVariableAssignments(TypeVariable<?> var,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			return unrollVariableAssignments(var, typeVarAssigns, false);
		}

		/**
		 * Look up {@code var} in {@code typeVarAssigns} <em>transitively</em>, i.e.
		 * keep looking until the value found is <em>not</em> a type variable.
		 *
		 * @param var the type variable to look up
		 * @param typeVarAssigns the map used for the look up
		 * @param acceptBounds If {@code true} and a variable chain does not lead to
		 *          a concrete type assignment, then the bounds of the leaf variable
		 *          will be returned instead of {@code null}.
		 * @return Type or {@code null} if some variable was not in the map
		 * @since 3.2
		 */
		private static Type unrollVariableAssignments(TypeVariable<?> var,
			final Map<TypeVariable<?>, Type> typeVarAssigns, boolean acceptBounds)
		{
			Type result;
			do {
				result = typeVarAssigns.get(var);
				if (result instanceof TypeVariable && !result.equals(var)) {
					var = (TypeVariable<?>) result;
					continue;
				}
				else if (result == null && acceptBounds && var.getBounds().length > 0) {
					// We didn't find an absolute mapping for the given variable, but the
					// variable is bounded so we can return it.
					result = var;
				}
				break;
			}
			while (true);
			return result;
		}

		/**
		 * <p>
		 * Checks if the subject type may be implicitly cast to the target generic
		 * array type following the Java generics rules.
		 * </p>
		 *
		 * @param type the subject type to be assigned to the target type
		 * @param toGenericArrayType the target generic array type
		 * @param typeVarAssigns a map with type variables
		 * @return {@code true} if {@code type} is assignable to
		 *         {@code toGenericArrayType}.
		 */
		private static boolean isAssignable(final Type type,
			final GenericArrayType toGenericArrayType,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			if (type == null || Any.is(type)) {
				return true;
			}

			// only a null type can be assigned to null type which
			// would have cause the previous to return true
			if (toGenericArrayType == null) {
				return false;
			}

			// all types are assignable to themselves
			if (toGenericArrayType.equals(type)) {
				return true;
			}

			final Type toComponentType = toGenericArrayType.getGenericComponentType();

			if (type instanceof Class) {
				final Class<?> cls = (Class<?>) type;

				// compare the component types
				return cls.isArray() && isAssignable(cls.getComponentType(),
					toComponentType, typeVarAssigns);
			}

			if (type instanceof GenericArrayType) {
				// compare the component types
				return isAssignable(((GenericArrayType) type).getGenericComponentType(),
					toComponentType, typeVarAssigns);
			}

			if (type instanceof WildcardType) {
				// so long as one of the upper bounds is assignable, it's good
				for (final Type bound : getImplicitUpperBounds((WildcardType) type)) {
					if (isAssignable(bound, toGenericArrayType)) {
						return true;
					}
				}

				return false;
			}

			if (type instanceof TypeVariable) {
				// probably should remove the following logic and just return false.
				// type variables cannot specify arrays as bounds.
				for (final Type bound : getImplicitBounds((TypeVariable<?>) type)) {
					if (isAssignable(bound, toGenericArrayType)) {
						return true;
					}
				}

				return false;
			}

			if (type instanceof ParameterizedType) {
				// the raw type of a parameterized type is never an array or
				// generic array, otherwise the declaration would look like this:
				// Collection[]< ? extends String > collection;
				return false;
			}

			throw new IllegalStateException("found an unhandled type: " + type);
		}

		/**
		 * <p>
		 * Checks if the subject type may be implicitly cast to the target wildcard
		 * type following the Java generics rules.
		 * </p>
		 *
		 * @param type the subject type to be assigned to the target type
		 * @param toWildcardType the target wildcard type
		 * @param typeVarAssigns a map with type variables
		 * @return {@code true} if {@code type} is assignable to
		 *         {@code toWildcardType}.
		 */
		private static boolean isAssignable(final Type type,
			final WildcardType toWildcardType,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			if (type == null || Any.is(type)) {
				return true;
			}

			// only a null type can be assigned to null type which
			// would have cause the previous to return true
			if (toWildcardType == null) {
				return false;
			}

			// all types are assignable to themselves
			if (toWildcardType.equals(type)) {
				return true;
			}

			final Type[] toUpperBounds = getImplicitUpperBounds(toWildcardType);
			final Type[] toLowerBounds = getImplicitLowerBounds(toWildcardType);

			if (type instanceof WildcardType) {
				final WildcardType wildcardType = (WildcardType) type;
				final Type[] upperBounds = getImplicitUpperBounds(wildcardType);
				final Type[] lowerBounds = getImplicitLowerBounds(wildcardType);

				for (Type toBound : toUpperBounds) {
					// if there are assignments for unresolved type variables,
					// now's the time to substitute them.
					toBound = substituteTypeVariables(toBound, typeVarAssigns);

					// each upper bound of the subject type has to be assignable to
					// each
					// upper bound of the target type
					for (final Type bound : upperBounds) {
						if (!isAssignable(bound, toBound, typeVarAssigns)) {
							return false;
						}
					}
				}

				for (Type toBound : toLowerBounds) {
					// if there are assignments for unresolved type variables,
					// now's the time to substitute them.
					toBound = substituteTypeVariables(toBound, typeVarAssigns);

					// each lower bound of the target type has to be assignable to
					// each
					// lower bound of the subject type
					for (final Type bound : lowerBounds) {
						if (!isAssignable(toBound, bound, typeVarAssigns)) {
							return false;
						}
					}
				}
				return true;
			}

			for (final Type toBound : toUpperBounds) {
				// if there are assignments for unresolved type variables,
				// now's the time to substitute them.
				if (!isAssignable(type, substituteTypeVariables(toBound,
					typeVarAssigns), typeVarAssigns))
				{
					return false;
				}
			}

			for (final Type toBound : toLowerBounds) {
				// if there are assignments for unresolved type variables,
				// now's the time to substitute them.
				if (!isAssignable(substituteTypeVariables(toBound, typeVarAssigns),
					type, typeVarAssigns))
				{
					return false;
				}
			}
			return true;
		}

		/**
		 * <p>
		 * Checks if the subject type may be implicitly cast to the target type
		 * variable following the Java generics rules.
		 * </p>
		 *
		 * @param type the subject type to be assigned to the target type
		 * @param toTypeVariable the target type variable
		 * @param typeVarAssigns a map with type variables
		 * @return {@code true} if {@code type} is assignable to
		 *         {@code toTypeVariable}.
		 */
		private static boolean isAssignable(final Type type,
			final TypeVariable<?> toTypeVariable,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			if (type == null) {
				return true;
			}

			// only a null type can be assigned to null type which
			// would have cause the previous to return true
			if (toTypeVariable == null) {
				return false;
			}

			// all types are assignable to themselves
			if (toTypeVariable.equals(type)) {
				return true;
			}

			// if toTypeVariable is assigned to type in typeVarAssigns, then it is
			// assignable
			if (typeVarAssigns.get(toTypeVariable) == type) {
				return true;
			}

			if (type instanceof TypeVariable) {
				// a type variable is assignable to another type variable, if
				// and only if the former is the latter, extends the latter, or
				// is otherwise a descendant of the latter.
				final Type[] bounds = getImplicitBounds((TypeVariable<?>) type);

				for (final Type bound : bounds) {
					if (isAssignable(bound, toTypeVariable, typeVarAssigns)) {
						return true;
					}
				}
				return false;
			}

			// if the type satisfies every one of the constraints of the
			// type variable, then it can satisfy the type variable.
			if (type instanceof Class || type instanceof ParameterizedType ||
				type instanceof GenericArrayType || type instanceof WildcardType)
			{
				final Type[] toTypeVarBounds = getImplicitBounds(toTypeVariable);

				for (final Type bound : toTypeVarBounds) {
					if (!isAssignable(type, bound, typeVarAssigns)) return false;
				}
				if (!Any.is(type)) {
					typeVarAssigns.put(toTypeVariable, type);
				}

				return true;
			}

			if (Any.is(type)) {
				typeVarAssigns.put(toTypeVariable, new Any(toTypeVariable.getBounds()));
				return true;
			}

			throw new IllegalStateException("found an unhandled type: " + type);
		}

		/**
		 * <p>
		 * Find the mapping for {@code type} in {@code typeVarAssigns}.
		 * </p>
		 *
		 * @param type the type to be replaced
		 * @param typeVarAssigns the map with type variables
		 * @return the replaced type
		 * @throws IllegalArgumentException if the type cannot be substituted
		 */
		private static Type substituteTypeVariables(final Type type,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			if (type instanceof ParameterizedType) {
				return unroll(type, typeVarAssigns);
			}
			if (type instanceof TypeVariable && typeVarAssigns != null) {
				final Type replacementType = typeVarAssigns.get(type);

				if (replacementType == null) {
					throw new IllegalArgumentException(
						"missing assignment type for type variable " + type);
				}
				return replacementType;
			}
			if (type instanceof GenericArrayType && typeVarAssigns != null) {
				final GenericArrayType genArrType = (GenericArrayType) type;
				final Type replacementType = unroll(
					genArrType.getGenericComponentType(), typeVarAssigns);
				return new GenericArrayTypeImpl(replacementType);
			}
			return type;
		}

		/**
		 * <p>
		 * Retrieves all the type arguments for this parameterized type including
		 * owner hierarchy arguments such as {@code Outer<K,V>.Inner<T>.DeepInner
		 * <E>} . The arguments are returned in a {@link Map} specifying the
		 * argument type for each {@link TypeVariable}.
		 * </p>
		 *
		 * @param type specifies the subject parameterized type from which to
		 *          harvest the parameters.
		 * @return a {@code Map} of the type arguments to their respective type
		 *         variables.
		 */
		public static Map<TypeVariable<?>, Type> getTypeArguments(
			final ParameterizedType type)
		{
			return getTypeArguments(type, getRawType(type), null);
		}

		/**
		 * <p>
		 * Gets the type arguments of a class/interface based on a subtype. For
		 * instance, this method will determine that both of the parameters for the
		 * interface {@link Map} are {@link Object} for the subtype
		 * {@link java.util.Properties Properties} even though the subtype does not
		 * directly implement the {@code Map} interface.
		 * </p>
		 * <p>
		 * This method returns {@code null} if {@code type} is not assignable to
		 * {@code toClass}. It returns an empty map if none of the classes or
		 * interfaces in its inheritance hierarchy specify any type arguments.
		 * </p>
		 * <p>
		 * A side effect of this method is that it also retrieves the type arguments
		 * for the classes and interfaces that are part of the hierarchy between
		 * {@code type} and {@code toClass}. So with the above example, this method
		 * will also determine that the type arguments for
		 * {@link java.util.Hashtable Hashtable} are also both {@code Object}. In
		 * cases where the interface specified by {@code toClass} is (indirectly)
		 * implemented more than once (e.g. where {@code toClass} specifies the
		 * interface {@link java.lang.Iterable Iterable} and {@code type} specifies
		 * a parameterized type that implements both {@link java.util.Set Set} and
		 * {@link java.util.Collection Collection}), this method will look at the
		 * inheritance hierarchy of only one of the implementations/subclasses; the
		 * first interface encountered that isn't a subinterface to one of the
		 * others in the {@code type} to {@code toClass} hierarchy.
		 * </p>
		 *
		 * @param type the type from which to determine the type parameters of
		 *          {@code toClass}
		 * @param toClass the class whose type parameters are to be determined based
		 *          on the subtype {@code type}
		 * @return a {@code Map} of the type assignments for the type variables in
		 *         each type in the inheritance hierarchy from {@code type} to
		 *         {@code toClass} inclusive.
		 */
		public static Map<TypeVariable<?>, Type> getTypeArguments(final Type type,
			final Class<?> toClass)
		{
			return getTypeArguments(type, toClass, null);
		}

		/**
		 * <p>
		 * Return a map of the type arguments of @{code type} in the context of
		 * {@code toClass}.
		 * </p>
		 *
		 * @param type the type in question
		 * @param toClass the class
		 * @param subtypeVarAssigns a map with type variables
		 * @return the {@code Map} with type arguments
		 */
		private static Map<TypeVariable<?>, Type> getTypeArguments(final Type type,
			final Class<?> toClass,
			final Map<TypeVariable<?>, Type> subtypeVarAssigns)
		{
			if (type instanceof Class) {
				return getTypeArguments((Class<?>) type, toClass, subtypeVarAssigns);
			}

			if (type instanceof ParameterizedType) {
				return getTypeArguments((ParameterizedType) type, toClass,
					subtypeVarAssigns);
			}

			if (type instanceof GenericArrayType) {
				return getTypeArguments(((GenericArrayType) type)
					.getGenericComponentType(), toClass.isArray() ? toClass
						.getComponentType() : toClass, subtypeVarAssigns);
			}

			// since wildcard types are not assignable to classes, should this just
			// return null?
			if (type instanceof WildcardType) {
				for (final Type bound : getImplicitUpperBounds((WildcardType) type)) {
					// find the first bound that is assignable to the target class
					if (isAssignable(bound, toClass)) {
						return getTypeArguments(bound, toClass, subtypeVarAssigns);
					}
				}

				return null;
			}

			if (type instanceof TypeVariable) {
				for (final Type bound : getImplicitBounds((TypeVariable<?>) type)) {
					// find the first bound that is assignable to the target class
					if (isAssignable(bound, toClass)) {
						return getTypeArguments(bound, toClass, subtypeVarAssigns);
					}
				}

				return null;
			}
			throw new IllegalStateException("found an unhandled type: " + type);
		}

		/**
		 * <p>
		 * Return a map of the type arguments of a parameterized type in the context
		 * of {@code toClass}.
		 * </p>
		 *
		 * @param parameterizedType the parameterized type
		 * @param toClass the class
		 * @param subtypeVarAssigns a map with type variables
		 * @return the {@code Map} with type arguments
		 */
		private static Map<TypeVariable<?>, Type> getTypeArguments(
			final ParameterizedType parameterizedType, final Class<?> toClass,
			final Map<TypeVariable<?>, Type> subtypeVarAssigns)
		{
			final Class<?> cls = getRawType(parameterizedType);

			// make sure they're assignable
			if (!isAssignable(cls, toClass)) {
				return null;
			}

			final Type ownerType = parameterizedType.getOwnerType();
			Map<TypeVariable<?>, Type> typeVarAssigns;

			if (ownerType instanceof ParameterizedType) {
				// get the owner type arguments first
				final ParameterizedType parameterizedOwnerType =
					(ParameterizedType) ownerType;
				typeVarAssigns = getTypeArguments(parameterizedOwnerType,
					getRawType(parameterizedOwnerType), subtypeVarAssigns);
			}
			else {
				// no owner, prep the type variable assignments map
				typeVarAssigns = subtypeVarAssigns == null ? new HashMap<>()
					: new HashMap<>(subtypeVarAssigns);
			}

			// get the subject parameterized type's arguments
			final Type[] typeArgs = parameterizedType.getActualTypeArguments();
			// and get the corresponding type variables from the raw class
			final TypeVariable<?>[] typeParams = cls.getTypeParameters();

			// map the arguments to their respective type variables
			for (int i = 0; i < typeParams.length; i++) {
				final Type typeArg = typeArgs[i];
				typeVarAssigns.put(typeParams[i], typeVarAssigns.containsKey(typeArg)
					? typeVarAssigns.get(typeArg) : typeArg);
			}

			if (toClass.equals(cls)) {
				// target class has been reached. Done.
				return typeVarAssigns;
			}

			// walk the inheritance hierarchy until the target class is reached
			return getTypeArguments(getClosestParentType(cls, toClass), toClass,
				typeVarAssigns);
		}

		/**
		 * <p>
		 * Return a map of the type arguments of a class in the context of @{code
		 * toClass}.
		 * </p>
		 *
		 * @param cls the class in question
		 * @param toClass the context class
		 * @param subtypeVarAssigns a map with type variables
		 * @return the {@code Map} with type arguments
		 */
		private static Map<TypeVariable<?>, Type> getTypeArguments(Class<?> cls,
			final Class<?> toClass,
			final Map<TypeVariable<?>, Type> subtypeVarAssigns)
		{
			// make sure they're assignable
			if (!isAssignable(cls, toClass)) {
				return null;
			}

			// can't work with primitives
			if (cls.isPrimitive()) {
				// both classes are primitives?
				if (toClass.isPrimitive()) {
					// dealing with widening here. No type arguments to be
					// harvested with these two types.
					return new HashMap<>();
				}

				// work with wrapper the wrapper class instead of the primitive
				cls = Classes.box(cls);
			}

			// create a copy of the incoming map, or an empty one if it's null
			final HashMap<TypeVariable<?>, Type> typeVarAssigns =
				subtypeVarAssigns == null ?
					new HashMap<>() : new HashMap<>(subtypeVarAssigns);

			// has target class been reached?
			if (toClass.equals(cls)) {
				return typeVarAssigns;
			}

			// walk the inheritance hierarchy until the target class is reached
			return getTypeArguments(getClosestParentType(cls, toClass), toClass,
				typeVarAssigns);
		}

		/**
		 * <p>
		 * Tries to determine the type arguments of a class/interface based on a
		 * super parameterized type's type arguments. This method is the inverse of
		 * {@link #getTypeArguments(Type, Class)} which gets a class/interface's
		 * type arguments based on a subtype. It is far more limited in determining
		 * the type arguments for the subject class's type variables in that it can
		 * only determine those parameters that map from the subject {@link Class}
		 * object to the supertype.
		 * </p>
		 * <p>
		 * Example: {@link java.util.TreeSet TreeSet} sets its parameter as the
		 * parameter for {@link java.util.NavigableSet NavigableSet}, which in turn
		 * sets the parameter of {@link java.util.SortedSet}, which in turn sets the
		 * parameter of {@link Set}, which in turn sets the parameter of
		 * {@link java.util.Collection}, which in turn sets the parameter of
		 * {@link java.lang.Iterable}. Since {@code TreeSet}'s parameter maps
		 * (indirectly) to {@code Iterable}'s parameter, it will be able to
		 * determine that based on the super type {@code Iterable<? extends
		 * Map<Integer, ? extends Collection<?>>>}, the parameter of {@code TreeSet}
		 * is {@code ? extends Map<Integer, ? extends
		 * Collection<?>>}.
		 * </p>
		 *
		 * @param cls the class whose type parameters are to be determined, not
		 *          {@code null}
		 * @param superType the super type from which {@code cls}'s type arguments
		 *          are to be determined, not {@code null}
		 * @return a {@code Map} of the type assignments that could be determined
		 *         for the type variables in each type in the inheritance hierarchy
		 *         from {@code type} to {@code toClass} inclusive.
		 */
		public static Map<TypeVariable<?>, Type> determineTypeArguments(
			final Class<?> cls, final ParameterizedType superType)
		{
			validateNotNull(cls, "cls is null");
			validateNotNull(superType, "superType is null");

			final Class<?> superClass = getRawType(superType);

			// compatibility check
			if (!isAssignable(cls, superClass)) {
				return null;
			}

			if (cls.equals(superClass)) {
				return getTypeArguments(superType, superClass, null);
			}

			// get the next class in the inheritance hierarchy
			final Type midType = getClosestParentType(cls, superClass);

			// can only be a class or a parameterized type
			if (midType instanceof Class) {
				return determineTypeArguments((Class<?>) midType, superType);
			}

			final ParameterizedType midParameterizedType =
				(ParameterizedType) midType;
			final Class<?> midClass = getRawType(midParameterizedType);
			// get the type variables of the mid class that map to the type
			// arguments of the super class
			final Map<TypeVariable<?>, Type> typeVarAssigns =
				determineTypeArguments(midClass, superType);
			// map the arguments of the mid type to the class type variables
			mapTypeVariablesToArguments(cls, midParameterizedType, typeVarAssigns);

			return typeVarAssigns;
		}

		/**
		 * <p>
		 * Performs a mapping of type variables.
		 * </p>
		 *
		 * @param <T> the generic type of the class in question
		 * @param cls the class in question
		 * @param parameterizedType the parameterized type
		 * @param typeVarAssigns the map to be filled
		 */
		private static <T> void mapTypeVariablesToArguments(final Class<T> cls,
			final ParameterizedType parameterizedType,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			// capture the type variables from the owner type that have assignments
			final Type ownerType = parameterizedType.getOwnerType();

			if (ownerType instanceof ParameterizedType) {
				// recursion to make sure the owner's owner type gets processed
				mapTypeVariablesToArguments(cls, (ParameterizedType) ownerType,
					typeVarAssigns);
			}

			// parameterizedType is a generic interface/class (or it's in the owner
			// hierarchy of said interface/class) implemented/extended by the class
			// cls. Find out which type variables of cls are type arguments of
			// parameterizedType:
			final Type[] typeArgs = parameterizedType.getActualTypeArguments();

			// of the cls's type variables that are arguments of parameterizedType,
			// find out which ones can be determined from the super type's arguments
			final TypeVariable<?>[] typeVars = getRawType(parameterizedType)
				.getTypeParameters();

			// use List view of type parameters of cls so the contains() method can be
			// used:
			final List<TypeVariable<Class<T>>> typeVarList = Arrays.asList(cls
				.getTypeParameters());

			for (int i = 0; i < typeArgs.length; i++) {
				final TypeVariable<?> typeVar = typeVars[i];
				final Type typeArg = typeArgs[i];

				// argument of parameterizedType is a type variable of cls
				if (typeVarList.contains(typeArg)
				// type variable of parameterizedType has an assignment in
				// the super type.
					&& typeVarAssigns.containsKey(typeVar)) {
					// map the assignment to the cls's type variable
					typeVarAssigns.put((TypeVariable<?>) typeArg,
						typeVarAssigns.get(typeVar));
				}
			}
		}

		/**
		 * <p>
		 * Get the closest parent type to the super class specified by
		 * {@code superClass}.
		 * </p>
		 *
		 * @param cls the class in question
		 * @param superClass the super class
		 * @return the closes parent type
		 */
		private static Type getClosestParentType(final Class<?> cls,
			final Class<?> superClass)
		{
			// only look at the interfaces if the super class is also an interface
			if (superClass.isInterface()) {
				// get the generic interfaces of the subject class
				final Type[] interfaceTypes = cls.getGenericInterfaces();
				// will hold the best generic interface match found
				Type genericInterface = null;

				// find the interface closest to the super class
				for (final Type midType : interfaceTypes) {
					Class<?> midClass = null;

					if (midType instanceof ParameterizedType) {
						midClass = getRawType((ParameterizedType) midType);
					}
					else if (midType instanceof Class) {
						midClass = (Class<?>) midType;
					}
					else {
						throw new IllegalStateException("Unexpected generic" +
							" interface type found: " + midType);
					}

					// check if this interface is further up the inheritance chain
					// than the previously found match
					if (isAssignable(midClass, superClass) &&
						isAssignable(genericInterface, (Type) midClass))
					{
						genericInterface = midType;
					}
				}

				// found a match?
				if (genericInterface != null) {
					return genericInterface;
				}
			}

			// none of the interfaces were descendants of the target class, so the
			// super class has to be one, instead
			return cls.getGenericSuperclass();
		}

		/**
		 * <p>
		 * Checks if the given value can be assigned to the target type following
		 * the Java generics rules.
		 * </p>
		 *
		 * @param value the value to be checked
		 * @param type the target type
		 * @return {@code true} if {@code value} is an instance of {@code type}.
		 */
		public static boolean isInstance(final Object value, final Type type) {
			if (type == null) {
				return false;
			}

			return value == null ? !(type instanceof Class) || !((Class<?>) type)
				.isPrimitive() : isAssignable(value.getClass(), type, null);
		}

		/**
		 * <p>
		 * This method strips out the redundant upper bound types in type variable
		 * types and wildcard types (or it would with wildcard types if multiple
		 * upper bounds were allowed).
		 * </p>
		 * <p>
		 * Example, with the variable type declaration:
		 *
		 * <pre>
		 * &lt;K extends java.util.Collection&lt;String&gt; &amp;
		 * java.util.List&lt;String&gt;&gt;
		 * </pre>
		 * <p>
		 * since {@code List} is a subinterface of {@code Collection}, this method
		 * will return the bounds as if the declaration had been:
		 * </p>
		 *
		 * <pre>
		 * &lt;K extends java.util.List&lt;String&gt;&gt;
		 * </pre>
		 *
		 * @param bounds an array of types representing the upper bounds of either
		 *          {@link WildcardType} or {@link TypeVariable}, not {@code null}.
		 * @return an array containing the values from {@code bounds} minus the
		 *         redundant types.
		 */
		public static Type[] normalizeUpperBounds(final Type[] bounds) {
			validateNotNull(bounds, "null value specified for bounds array");
			// don't bother if there's only one (or none) type
			if (bounds.length < 2) {
				return bounds;
			}

			final Set<Type> types = new HashSet<>(bounds.length);

			for (final Type type1 : bounds) {
				boolean subtypeFound = false;

				for (final Type type2 : bounds) {
					if (type1 != type2 && isAssignable(type2, type1, null)) {
						subtypeFound = true;
						break;
					}
				}

				if (!subtypeFound) {
					types.add(type1);
				}
			}

			return types.toArray(new Type[types.size()]);
		}

		/**
		 * <p>
		 * Returns an array containing the sole type of {@link Object} if
		 * {@link TypeVariable#getBounds()} returns an empty array. Otherwise, it
		 * returns the result of {@link TypeVariable#getBounds()} passed into
		 * {@link #normalizeUpperBounds}.
		 * </p>
		 *
		 * @param typeVariable the subject type variable, not {@code null}
		 * @return a non-empty array containing the bounds of the type variable.
		 */
		public static Type[] getImplicitBounds(final TypeVariable<?> typeVariable) {
			validateNotNull(typeVariable, "typeVariable is null");
			final Type[] bounds = typeVariable.getBounds();

			return bounds.length == 0 ? new Type[] { Object.class }
				: normalizeUpperBounds(bounds);
		}

		/**
		 * <p>
		 * Returns an array containing the sole value of {@link Object} if
		 * {@link WildcardType#getUpperBounds()} returns an empty array. Otherwise,
		 * it returns the result of {@link WildcardType#getUpperBounds()} passed
		 * into {@link #normalizeUpperBounds}.
		 * </p>
		 *
		 * @param wildcardType the subject wildcard type, not {@code null}
		 * @return a non-empty array containing the upper bounds of the wildcard
		 *         type.
		 */
		public static Type[] getImplicitUpperBounds(
			final WildcardType wildcardType)
		{
			validateNotNull(wildcardType, "wildcardType is null");
			final Type[] bounds = wildcardType.getUpperBounds();

			return bounds.length == 0 ? new Type[] { Object.class }
				: normalizeUpperBounds(bounds);
		}

		/**
		 * <p>
		 * Returns an array containing a single value of {@code null} if
		 * {@link WildcardType#getLowerBounds()} returns an empty array. Otherwise,
		 * it returns the result of {@link WildcardType#getLowerBounds()}.
		 * </p>
		 *
		 * @param wildcardType the subject wildcard type, not {@code null}
		 * @return a non-empty array containing the lower bounds of the wildcard
		 *         type.
		 */
		public static Type[] getImplicitLowerBounds(
			final WildcardType wildcardType)
		{
			validateNotNull(wildcardType, "wildcardType is null");
			final Type[] bounds = wildcardType.getLowerBounds();

			return bounds.length == 0 ? new Type[] { null } : bounds;
		}

		/**
		 * <p>
		 * Determines whether or not specified types satisfy the bounds of their
		 * mapped type variables. When a type parameter extends another (such as
		 * {@code <T, S extends T>}), uses another as a type parameter (such as
		 * {@code <T, S extends Comparable>>}), or otherwise depends on another type
		 * variable to be specified, the dependencies must be included in
		 * {@code typeVarAssigns}.
		 * </p>
		 *
		 * @param typeVarAssigns specifies the potential types to be assigned to the
		 *          type variables, not {@code null}.
		 * @return whether or not the types can be assigned to their respective type
		 *         variables.
		 */
		public static boolean typesSatisfyVariables(
			final Map<TypeVariable<?>, Type> typeVarAssigns)
		{
			validateNotNull(typeVarAssigns, "typeVarAssigns is null");
			// all types must be assignable to all the bounds of the their mapped
			// type variable.
			for (final Map.Entry<TypeVariable<?>, Type> entry : typeVarAssigns
				.entrySet())
			{
				final TypeVariable<?> typeVar = entry.getKey();
				final Type type = entry.getValue();

				for (final Type bound : getImplicitBounds(typeVar)) {
					if (!isAssignable(type, substituteTypeVariables(bound,
						typeVarAssigns), typeVarAssigns))
					{
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * <p>
		 * Transforms the passed in type to a {@link Class} object. Type-checking
		 * method of convenience.
		 * </p>
		 *
		 * @param parameterizedType the type to be converted
		 * @return the corresponding {@code Class} object
		 * @throws IllegalStateException if the conversion fails
		 */
		private static Class<?> getRawType(
			final ParameterizedType parameterizedType)
		{
			final Type rawType = parameterizedType.getRawType();

			// check if raw type is a Class object
			// not currently necessary, but since the return type is Type instead of
			// Class, there's enough reason to believe that future versions of Java
			// may return other Type implementations. And type-safety checking is
			// rarely a bad idea.
			if (!(rawType instanceof Class)) {
				throw new IllegalStateException("Wait... What!? Type of rawType: " +
					rawType);
			}

			return (Class<?>) rawType;
		}

		/**
		 * <p>
		 * Get the raw type of a Java type, given its context. Primarily for use
		 * with {@link TypeVariable}s and {@link GenericArrayType}s, or when you do
		 * not know the runtime type of {@code type}: if you know you have a
		 * {@link Class} instance, it is already raw; if you know you have a
		 * {@link ParameterizedType}, its raw type is only a method call away.
		 * </p>
		 *
		 * @param type to resolve
		 * @param assigningType type to be resolved against
		 * @return the resolved {@link Class} object or {@code null} if the type
		 *         could not be resolved
		 */
		public static Class<?> getRawType(final Type type,
			final Type assigningType)
		{
			if (type instanceof Class) {
				// it is raw, no problem
				return (Class<?>) type;
			}

			if (type instanceof ParameterizedType) {
				// simple enough to get the raw type of a ParameterizedType
				return getRawType((ParameterizedType) type);
			}

			if (type instanceof TypeVariable) {
				if (assigningType == null) {
					return null;
				}

				// get the entity declaring this type variable
				final Object genericDeclaration = ((TypeVariable<?>) type)
					.getGenericDeclaration();

				// can't get the raw type of a method- or constructor-declared type
				// variable
				if (!(genericDeclaration instanceof Class)) {
					return null;
				}

				// get the type arguments for the declaring class/interface based
				// on the enclosing type
				final Map<TypeVariable<?>, Type> typeVarAssigns =
					getTypeArguments(assigningType, (Class<?>) genericDeclaration);

				// enclosingType has to be a subclass (or subinterface) of the
				// declaring type
				if (typeVarAssigns == null) {
					return null;
				}

				// get the argument assigned to this type variable
				final Type typeArgument = typeVarAssigns.get(type);

				if (typeArgument == null) {
					return null;
				}

				// get the argument for this type variable
				return getRawType(typeArgument, assigningType);
			}

			if (type instanceof GenericArrayType) {
				// get raw component type
				final Class<?> rawComponentType = getRawType(((GenericArrayType) type)
					.getGenericComponentType(), assigningType);

				// return the corresponding array type
				return Classes.array(rawComponentType);
			}

			// (hand-waving) this is not the method you're looking for
			if (type instanceof WildcardType) {
				return null;
			}

			throw new IllegalArgumentException("unknown type: " + type);
		}

		/**
		 * Learn whether the specified type denotes an array type.
		 *
		 * @param type the type to be checked
		 * @return {@code true} if {@code type} is an array class or a
		 *         {@link GenericArrayType}.
		 */
		public static boolean isArrayType(final Type type) {
			return type instanceof GenericArrayType || type instanceof Class &&
				((Class<?>) type).isArray();
		}

		/**
		 * Get the array component type of {@code type}.
		 *
		 * @param type the type to be checked
		 * @return component type or null if type is not an array type
		 */
		public static Type getArrayComponentType(final Type type) {
			if (type instanceof Class) {
				final Class<?> clazz = (Class<?>) type;
				return clazz.isArray() ? clazz.getComponentType() : null;
			}
			if (type instanceof GenericArrayType) {
				return ((GenericArrayType) type).getGenericComponentType();
			}
			return null;
		}

		/**
		 * Get a type representing {@code type} with variable assignments
		 * "unrolled."
		 *
		 * @param typeArguments as from
		 *          {@link TypeUtils#getTypeArguments(Type, Class)}
		 * @param type the type to unroll variable assignments for
		 * @return Type
		 * @since 3.2
		 */
		public static Type unrollVariables(Map<TypeVariable<?>, Type> typeArguments,
			final Type type)
		{
			return unrollVariables(typeArguments, type, true);
		}

		/**
		 * Get a type representing {@code type} with variable assignments
		 * "unrolled."
		 *
		 * @param typeArguments as from
		 *          {@link TypeUtils#getTypeArguments(Type, Class)}
		 * @param type the type to unroll variable assignments for
		 * @param followTypeVars whether a {@link TypeVariable} should be
		 *          recursively followed if it maps to another {@link TypeVariable},
		 *          or if it should be just replaced by the mapping
		 * @return Type
		 * @since 3.2
		 */
		public static Type unrollVariables(Map<TypeVariable<?>, Type> typeArguments,
			final Type type, boolean followTypeVars)
		{
			if (typeArguments == null) {
				typeArguments = Collections.<TypeVariable<?>, Type> emptyMap();
			}
			if (containsTypeVariables(type)) {
				if (type instanceof TypeVariable) {
					if (followTypeVars) {
						return unrollVariables(typeArguments, typeArguments.get(type),
							followTypeVars);
					}
					else {
						return typeArguments.getOrDefault(type, type);
					}
				}
				if (type instanceof ParameterizedType) {
					final ParameterizedType p = (ParameterizedType) type;
					final Map<TypeVariable<?>, Type> parameterizedTypeArguments;
					if (p.getOwnerType() == null) {
						parameterizedTypeArguments = typeArguments;
					}
					else {
						parameterizedTypeArguments = new HashMap<>(typeArguments);
//						parameterizedTypeArguments.putAll(TypeUtils.getTypeArguments(p));
					}
					final Type[] args = p.getActualTypeArguments();
					final Type[] resolved = new Type[args.length];
					for (int i = 0; i < args.length; i++) {
						final Type unrolled = unrollVariables(parameterizedTypeArguments,
							args[i], followTypeVars);
						resolved[i] = unrolled != null ? unrolled : args[i];
					}
					return parameterizeWithOwner(p.getOwnerType(), (Class<?>) p
						.getRawType(), resolved);
				}
				if (type instanceof WildcardType) {
					final WildcardType wild = (WildcardType) type;
					return wildcardType().withUpperBounds(unrollBounds(typeArguments, wild
						.getUpperBounds())).withLowerBounds(unrollBounds(typeArguments, wild
							.getLowerBounds())).build();
				}
				if (type instanceof GenericArrayType) {
					final GenericArrayType genArrType = (GenericArrayType) type;
					final Type componentType = genArrType.getGenericComponentType();
					final Type unrolledComponent = unrollVariables(typeArguments,
						componentType, followTypeVars);
					return array(unrolledComponent);
				}
			}
			return type;
		}

		/**
		 * Local helper method to unroll variables in a type bounds array.
		 *
		 * @param typeArguments assignments {@link Map}
		 * @param bounds in which to expand variables
		 * @return {@code bounds} with any variables reassigned
		 * @since 3.2
		 */
		private static Type[] unrollBounds(
			final Map<TypeVariable<?>, Type> typeArguments, final Type[] bounds)
		{
			final ArrayList<Type> result = new ArrayList<>();
			for (final Type bound : bounds) {
				final Type unrolled = unrollVariables(typeArguments, bound);
				if (unrolled != null) result.add(unrolled);
			}
			return result.toArray(new Type[result.size()]);
		}

		/**
		 * Learn, recursively, whether any of the type parameters associated with
		 * {@code type} are bound to variables.
		 *
		 * @param type the type to check for type variables
		 * @return boolean
		 * @since 3.2
		 */
		public static boolean containsTypeVariables(final Type type) {
			if (type instanceof TypeVariable) {
				return true;
			}
			if (type instanceof Class) {
				return ((Class<?>) type).getTypeParameters().length > 0;
			}
			if (type instanceof ParameterizedType) {
				var pType = (ParameterizedType) type;
				for (final Type arg : pType.getActualTypeArguments()) {
					if (containsTypeVariables(arg)) {
						return true;
					}
				}
				return false;
			}
			if (type instanceof WildcardType) {
				final WildcardType wild = (WildcardType) type;
				return
					containsTypeVariables(TypeUtils.getImplicitLowerBounds(wild)[0]) ||
					containsTypeVariables(TypeUtils.getImplicitUpperBounds(wild)[0]);
			}
			if (type instanceof GenericArrayType) {
				// if this type contains type vars, they will be in the component type
				final GenericArrayType genArrType = (GenericArrayType) type;
				return containsTypeVariables(genArrType.getGenericComponentType());
			}
			return false;
		}

		/**
		 * Create a parameterized type instance.
		 *
		 * @param raw the raw class to create a parameterized type instance for
		 * @param typeArguments the types used for parameterization
		 * @return {@link ParameterizedType}
		 * @since 3.2
		 */
		public static final ParameterizedType parameterize(final Class<?> raw,
			final Type... typeArguments)
		{
			return parameterizeWithOwner(null, raw, typeArguments);
		}

		/**
		 * Create a parameterized type instance.
		 *
		 * @param raw the raw class to create a parameterized type instance for
		 * @param typeArgMappings the mapping used for parameterization
		 * @return {@link ParameterizedType}
		 * @since 3.2
		 */
		public static final ParameterizedType parameterize(final Class<?> raw,
			final Map<TypeVariable<?>, Type> typeArgMappings)
		{
			validateNotNull(raw, "raw class is null");
			validateNotNull(typeArgMappings, "typeArgMappings is null");
			return parameterizeWithOwner(null, raw,
				extractTypeArgumentsFrom(typeArgMappings, raw.getTypeParameters()));
		}

		/**
		 * Create a parameterized type instance.
		 *
		 * @param owner the owning type
		 * @param raw the raw class to create a parameterized type instance for
		 * @param typeArguments the types used for parameterization
		 * @return {@link ParameterizedType}
		 * @since 3.2
		 */
		public static final ParameterizedType parameterizeWithOwner(
			final Type owner, final Class<?> raw, final Type... typeArguments)
		{
			validateNotNull(raw, "raw class is null");
			final Type useOwner;
			if (raw.getEnclosingClass() == null) {
				validateIsTrue(owner == null, "no owner allowed for top-level %s", raw);
				useOwner = null;
			}
			else if (owner == null) {
				useOwner = raw.getEnclosingClass();
			}
			else {
				validateIsTrue(TypeUtils.isAssignable(owner, raw.getEnclosingClass()),
					"%s is invalid owner type for parameterized %s", owner, raw);
				useOwner = owner;
			}
			validateNoNullElements(typeArguments, "null type argument at index %s");
			validateIsTrue(raw.getTypeParameters().length == typeArguments.length,
				"invalid number of type parameters specified: expected %s, got %s", raw
					.getTypeParameters().length, typeArguments.length);

			return new ParameterizedTypeImpl(raw, useOwner, typeArguments);
		}

		/**
		 * Create a parameterized type instance.
		 *
		 * @param owner the owning type
		 * @param raw the raw class to create a parameterized type instance for
		 * @param typeArgMappings the mapping used for parameterization
		 * @return {@link ParameterizedType}
		 * @since 3.2
		 */
		public static final ParameterizedType parameterizeWithOwner(
			final Type owner, final Class<?> raw,
			final Map<TypeVariable<?>, Type> typeArgMappings)
		{
			validateNotNull(raw, "raw class is null");
			validateNotNull(typeArgMappings, "typeArgMappings is null");
			return parameterizeWithOwner(owner, raw, extractTypeArgumentsFrom(
				typeArgMappings, raw.getTypeParameters()));
		}

		/**
		 * Helper method to establish the formal parameters for a parameterized
		 * type.
		 *
		 * @param mappings map containing the assignments
		 * @param variables expected map keys
		 * @return array of map values corresponding to specified keys
		 */
		private static Type[] extractTypeArgumentsFrom(
			final Map<TypeVariable<?>, Type> mappings,
			final TypeVariable<?>[] variables)
		{
			final Type[] result = new Type[variables.length];
			int index = 0;
			for (final TypeVariable<?> var : variables) {
				validateIsTrue(mappings.containsKey(var),
					"missing argument mapping for %s", toString(var));
				result[index++] = mappings.get(var);
			}
			return result;
		}

		/**
		 * Get a {@link WildcardTypeBuilder}.
		 *
		 * @return {@link WildcardTypeBuilder}
		 * @since 3.2
		 */
		public static WildcardTypeBuilder wildcardType() {
			return new WildcardTypeBuilder();
		}

		/**
		 * Create a generic array type instance.
		 *
		 * @param componentType the type of the elements of the array. For example
		 *          the component type of {@code boolean[]} is {@code boolean}
		 * @return {@link GenericArrayType}
		 * @since 3.2
		 */
		public static GenericArrayType genericArrayType(final Type componentType) {
			return new GenericArrayTypeImpl(validateNotNull(componentType,
				"componentType is null"));
		}

		/**
		 * Check equality of types.
		 *
		 * @param t1 the first type
		 * @param t2 the second type
		 * @return boolean
		 * @since 3.2
		 */
		public static boolean equals(final Type t1, final Type t2) {
			if (Objects.equals(t1, t2)) {
				return true;
			}
			if (t1 instanceof ParameterizedType) {
				return equals((ParameterizedType) t1, t2);
			}
			if (t1 instanceof GenericArrayType) {
				return equals((GenericArrayType) t1, t2);
			}
			if (t1 instanceof WildcardType) {
				return equals((WildcardType) t1, t2);
			}
			return false;
		}

		/**
		 * Learn whether {@code t} equals {@code p}.
		 *
		 * @param p LHS
		 * @param t RHS
		 * @return boolean
		 * @since 3.2
		 */
		private static boolean equals(final ParameterizedType p, final Type t) {
			if (t instanceof ParameterizedType) {
				final ParameterizedType other = (ParameterizedType) t;
				if (equals(p.getRawType(), other.getRawType()) && equals(p
					.getOwnerType(), other.getOwnerType()))
				{
					return equals(p.getActualTypeArguments(), other
						.getActualTypeArguments());
				}
			}
			return false;
		}

		/**
		 * Learn whether {@code t} equals {@code a}.
		 *
		 * @param a LHS
		 * @param t RHS
		 * @return boolean
		 * @since 3.2
		 */
		private static boolean equals(final GenericArrayType a, final Type t) {
			return t instanceof GenericArrayType && equals(a
				.getGenericComponentType(), ((GenericArrayType) t)
					.getGenericComponentType());
		}

		/**
		 * Learn whether {@code t} equals {@code w}.
		 *
		 * @param w LHS
		 * @param t RHS
		 * @return boolean
		 * @since 3.2
		 */
		private static boolean equals(final WildcardType w, final Type t) {
			if (t instanceof WildcardType) {
				final WildcardType other = (WildcardType) t;
				return equals(getImplicitLowerBounds(w), getImplicitLowerBounds(
					other)) && equals(getImplicitUpperBounds(w), getImplicitUpperBounds(
						other));
			}
			return false;
		}

		/**
		 * Learn whether {@code t1} equals {@code t2}.
		 *
		 * @param t1 LHS
		 * @param t2 RHS
		 * @return boolean
		 * @since 3.2
		 */
		private static boolean equals(final Type[] t1, final Type[] t2) {
			if (t1.length == t2.length) {
				for (int i = 0; i < t1.length; i++) {
					if (!equals(t1[i], t2[i])) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		/**
		 * Present a given type as a Java-esque String.
		 *
		 * @param type the type to create a String representation for, not
		 *          {@code null}
		 * @return String
		 * @since 3.2
		 */
		public static String toString(final Type type) {
			return toString(type, new HashSet<>());
		}

		private static String toString(final Type type, final Set<Type> done) {
			validateNotNull(type);
			if (type instanceof Class) {
				return classToString((Class<?>) type, done);
			}
			if (Any.is(type)) {
				return type.toString();
			}
			if (type instanceof ParameterizedType) {
				return parameterizedTypeToString((ParameterizedType) type, done);
			}
			if (type instanceof WildcardType) {
				return wildcardTypeToString((WildcardType) type, done);
			}
			if (type instanceof TypeVariable) {
				return typeVariableToString((TypeVariable<?>) type, done);
			}
			if (type instanceof GenericArrayType) {
				return genericArrayTypeToString((GenericArrayType) type);
			}
			if (type instanceof CaptureType) {
				return type.toString();
			}
			throw new IllegalArgumentException("Unknown generic type: " + //
				type.getClass().getName());
		}

		/**
		 * Format a {@link TypeVariable} including its {@link GenericDeclaration}.
		 *
		 * @param var the type variable to create a String representation for, not
		 *          {@code null}
		 * @return String
		 * @since 3.2
		 */
		public static String toLongString(final TypeVariable<?> var) {
			validateNotNull(var, "var is null");
			final StringBuilder buf = new StringBuilder();
			final GenericDeclaration d = ((TypeVariable<?>) var)
				.getGenericDeclaration();
			if (d instanceof Class) {
				Class<?> c = (Class<?>) d;
				while (true) {
					if (c.getEnclosingClass() == null) {
						buf.insert(0, c.getName());
						break;
					}
					buf.insert(0, c.getSimpleName()).insert(0, '.');
					c = c.getEnclosingClass();
				}
			}
			else if (d instanceof Type) {// not possible as of now
				buf.append(toString((Type) d));
			}
			else {
				buf.append(d);
			}
			return buf.append(':').append(typeVariableToString(var, new HashSet<>()))
				.toString();
		}

//		/**
//		 * Wrap the specified {@link Type} in a {@link Typed} wrapper.
//		 *
//		 * @param <T> inferred generic type
//		 * @param type to wrap
//		 * @return Typed&lt;T&gt;
//		 * @since 3.2
//		 */
//		public static <T> Typed<T> wrap(final Type type) {
//			return new Typed<T>() {
//
//				@Override
//				public Type getType() {
//					return type;
//				}
//			};
//		}
//
//		/**
//		 * Wrap the specified {@link Class} in a {@link Typed} wrapper.
//		 *
//		 * @param <T> generic type
//		 * @param type to wrap
//		 * @return Typed&lt;T&gt;
//		 * @since 3.2
//		 */
//		public static <T> Typed<T> wrap(final Class<T> type) {
//			return TypeUtils.<T> wrap((Type) type);
//		}

		/**
		 * Format a {@link Class} as a {@link String}.
		 *
		 * @param c {@code Class} to format
		 * @param done list of already-encountered types
		 * @return String
		 * @since 3.2
		 */
		private static String classToString(final Class<?> c,
			final Set<Type> done)
		{
			final StringBuilder buf = new StringBuilder();

			if (c.getEnclosingClass() != null) {
				buf.append(classToString(c.getEnclosingClass(), done)).append('.')
					.append(c.getSimpleName());
			}
			else {
				buf.append(c.getName());
			}
			if (c.getTypeParameters().length > 0) {
				buf.append('<');
				appendAllTo(buf, ", ", done, c.getTypeParameters());
				buf.append('>');
			}
			return buf.toString();
		}

		/**
		 * Format a {@link TypeVariable} as a {@link String}.
		 *
		 * @param v {@code TypeVariable} to format
		 * @param done list of already-encountered types
		 * @return String
		 * @since 3.2
		 */
		private static String typeVariableToString(final TypeVariable<?> v,
			final Set<Type> done)
		{
			final StringBuilder buf = new StringBuilder(v.getName());
			if (done.contains(v)) return buf.toString();
			done.add(v);
			final Type[] bounds = v.getBounds();
			if (bounds.length > 0 && !(bounds.length == 1 && Object.class.equals(
				bounds[0])))
			{
				buf.append(" extends ");
				appendAllTo(buf, " & ", done, v.getBounds());
			}
			return buf.toString();
		}

		/**
		 * Format a {@link ParameterizedType} as a {@link String}.
		 *
		 * @param p {@code ParameterizedType} to format
		 * @param done list of already-encountered types
		 * @return String
		 * @since 3.2
		 */
		private static String parameterizedTypeToString(final ParameterizedType p,
			final Set<Type> done)
		{
			final StringBuilder buf = new StringBuilder();

			final Type useOwner = p.getOwnerType();
			final Class<?> raw = (Class<?>) p.getRawType();
			final Type[] typeArguments = p.getActualTypeArguments();
			if (useOwner == null) {
				buf.append(raw.getName());
			}
			else {
				if (useOwner instanceof Class) {
					buf.append(((Class<?>) useOwner).getName());
				}
				else {
					buf.append(useOwner.toString());
				}
				buf.append('.').append(raw.getSimpleName());
			}

			appendAllTo(buf.append('<'), ", ", done, typeArguments).append('>');
			return buf.toString();
		}

		/**
		 * Format a {@link WildcardType} as a {@link String}.
		 *
		 * @param w {@code WildcardType} to format
		 * @param done list of already-encountered types
		 * @return String
		 * @since 3.2
		 */
		private static String wildcardTypeToString(final WildcardType w,
			final Set<Type> done)
		{
			final StringBuilder buf = new StringBuilder().append('?');
			if (done.contains(w)) return buf.toString();
			done.add(w);
			final Type[] lowerBounds = w.getLowerBounds();
			final Type[] upperBounds = w.getUpperBounds();
			if (lowerBounds.length > 1 || lowerBounds.length == 1 &&
				lowerBounds[0] != null)
			{
				appendAllTo(buf.append(" super "), " & ", done, lowerBounds);
			}
			else if (upperBounds.length > 1 || upperBounds.length == 1 &&
				!Object.class.equals(upperBounds[0]))
			{
				appendAllTo(buf.append(" extends "), " & ", done, upperBounds);
			}
			return buf.toString();
		}

		/**
		 * Format a {@link GenericArrayType} as a {@link String}.
		 *
		 * @param g {@code GenericArrayType} to format
		 * @return String
		 * @since 3.2
		 */
		private static String genericArrayTypeToString(final GenericArrayType g) {
			return String.format("%s[]", toString(g.getGenericComponentType()));
		}

		/**
		 * Append {@code types} to {@code buf} with separator {@code sep}.
		 *
		 * @param buf destination
		 * @param sep separator
		 * @param done list of already-encountered types
		 * @param types to append
		 * @return {@code buf}
		 * @since 3.2
		 */
		private static StringBuilder appendAllTo(final StringBuilder buf,
			final String sep, final Set<Type> done, final Type... types)
		{
			validateNotEmpty(validateNoNullElements(types));
			if (types.length > 0) {
				buf.append(toString(types[0], done));
				for (int i = 1; i < types.length; i++) {
					buf.append(sep).append(toString(types[i], done));
				}
			}
			return buf;
		}

		private static final String DEFAULT_IS_NULL_EX_MESSAGE =
			"The validated object is null";

		/** Forked from {@code org.apache.commons.lang3.Validate#notNull}. */
		private static <T> T validateNotNull(final T object) {
			return validateNotNull(object, DEFAULT_IS_NULL_EX_MESSAGE);
		}

		/** Forked from {@code org.apache.commons.lang3.Validate#notNull}. */
		private static <T> T validateNotNull(final T object, final String message,
			final Object... values)
		{
			if (object == null) {
				throw new NullPointerException(String.format(message, values));
			}
			return object;
		}

		/** Forked from {@code org.apache.commons.lang3.Validate#isTrue}. */
		private static void validateIsTrue(final boolean expression,
			final String message, final Object... values)
		{
			if (expression == false) {
				throw new IllegalArgumentException(String.format(message, values));
			}
		}

		private static final String DEFAULT_NO_NULL_ELEMENTS_ARRAY_EX_MESSAGE =
			"The validated array contains null element at index: %d";

		/** Forked from {@code org.apache.commons.lang3.Validate#noNullElements}. */
		private static <T> T[] validateNoNullElements(final T[] array) {
			return validateNoNullElements(array,
				DEFAULT_NO_NULL_ELEMENTS_ARRAY_EX_MESSAGE);
		}

		/** Forked from {@code org.apache.commons.lang3.Validate#noNullElements}. */
		private static <T> T[] validateNoNullElements(final T[] array,
			final String message, final Object... values)
		{
			validateNotNull(array);
			for (int i = 0; i < array.length; i++) {
				if (array[i] == null) {
					final Object[] values2 = new Object[values.length + 1];
					System.arraycopy(values, 0, values2, 0, values.length);
					values2[values.length] = Integer.valueOf(i);
					throw new IllegalArgumentException(String.format(message, values2));
				}
			}
			return array;
		}

		private static final String DEFAULT_NOT_EMPTY_ARRAY_EX_MESSAGE =
			"The validated array is empty";

		/** Forked from {@code org.apache.commons.lang3.Validate#notEmpty}. */
		private static <T> T[] validateNotEmpty(final T[] array) {
			return validateNotEmpty(array, DEFAULT_NOT_EMPTY_ARRAY_EX_MESSAGE);
		}

		/** Forked from {@code org.apache.commons.lang3.Validate#notEmpty}. */
		private static <T> T[] validateNotEmpty(final T[] array,
			final String message, final Object... values)
		{
			if (array == null) {
				throw new NullPointerException(String.format(message, values));
			}
			if (array.length == 0) {
				throw new IllegalArgumentException(String.format(message, values));
			}
			return array;
		}
	}

	// -- END FORK OF APACHE COMMONS LANG 3.4 CODE --

	// -- BEGIN FORK OF GENTYREF 1.1.0 CODE --

	/**
	 * Utility class for doing reflection on types.
	 *
	 * @author Wouter Coekaerts <wouter@coekaerts.be>
	 */
	private static class GenericTypeReflector {

		private static final Type UNBOUND_WILDCARD = new TypeUtils.WildcardTypeImpl(
			new Type[] { Object.class }, new Type[] {});

		/**
		 * Returns the erasure of the given type.
		 */
		public static Class<?> erase(final Type type) {
			if (type instanceof Class) {
				return (Class<?>) type;
			}
			else if (type instanceof ParameterizedType) {
				return (Class<?>) ((ParameterizedType) type).getRawType();
			}
			else if (type instanceof TypeVariable) {
				final TypeVariable<?> tv = (TypeVariable<?>) type;
				if (tv.getBounds().length == 0) return Object.class;
				return erase(tv.getBounds()[0]);
			}
			else if (type instanceof GenericArrayType) {
				final GenericArrayType aType = (GenericArrayType) type;
				return Classes.array(erase(aType.getGenericComponentType()));
			}
			else {
				// TODO at least support CaptureType here
				throw new RuntimeException("not supported: " + type.getClass());
			}
		}

		/**
		 * Maps type parameters in a type to their values.
		 *
		 * @param toMapType Type possibly containing type arguments
		 * @param typeAndParams must be either ParameterizedType, or (in case there
		 *          are no type arguments, or it's a raw type) Class
		 * @return toMapType, but with type parameters from typeAndParams replaced.
		 */
		private static Type mapTypeParameters(final Type toMapType,
			final Type typeAndParams)
		{
			if (isMissingTypeParameters(typeAndParams)) {
				return erase(toMapType);
			}
			final VarMap varMap = new VarMap();
			Type handlingTypeAndParams = typeAndParams;
			while (handlingTypeAndParams instanceof ParameterizedType) {
				final ParameterizedType pType =
					(ParameterizedType) handlingTypeAndParams;
				// getRawType should always be Class
				final Class<?> clazz = (Class<?>) pType.getRawType();
				varMap.addAll(clazz.getTypeParameters(), pType
					.getActualTypeArguments());
				handlingTypeAndParams = pType.getOwnerType();
			}
			return varMap.map(toMapType);
		}

		/**
		 * Checks if the given type is a class that is supposed to have type
		 * parameters, but doesn't. In other words, if it's a really raw type.
		 */
		private static boolean isMissingTypeParameters(final Type type) {
			if (type instanceof Class) {
				for (Class<?> clazz = (Class<?>) type; clazz != null; clazz = clazz
					.getEnclosingClass())
				{
					if (clazz.getTypeParameters().length != 0) return true;
				}
				return false;
			}
			else if (type instanceof ParameterizedType) {
				return false;
			}
			else {
				throw new AssertionError("Unexpected type " + type.getClass());
			}
		}

		/**
		 * Returns a type representing the class, with all type parameters the
		 * unbound wildcard ("?"). For example,
		 * <tt>addWildcardParameters(Map.class)</tt> returns a type representing
		 * <tt>Map&lt;?,?&gt;</tt>.
		 * <ul>
		 * <li>If clazz is a class or interface without type parameters, clazz
		 * itself is returned.</li>
		 * <li>If clazz is a class or interface with type parameters, an instance of
		 * ParameterizedType is returned.</li>
		 * <li>if clazz is an array type, an array type is returned with unbound
		 * wildcard parameters added in the the component type.
		 * </ul>
		 */
		public static Type addWildcardParameters(final Class<?> clazz) {
			if (clazz.isArray()) {
				return array(addWildcardParameters(clazz.getComponentType()));
			}
			else if (isMissingTypeParameters(clazz)) {
				final TypeVariable<?>[] vars = clazz.getTypeParameters();
				final Type[] arguments = new Type[vars.length];
				Arrays.fill(arguments, UNBOUND_WILDCARD);
				final Type owner = clazz.getDeclaringClass() == null ? null
					: addWildcardParameters(clazz.getDeclaringClass());
				return parameterize(clazz, owner, arguments);
			}
			else {
				return clazz;
			}
		}

		/**
		 * Finds the most specific supertype of <tt>type</tt> whose erasure is
		 * <tt>searchClass</tt>. In other words, returns a type representing the
		 * class <tt>searchClass</tt> plus its exact type parameters in
		 * <tt>type</tt>.
		 * <ul>
		 * <li>Returns an instance of {@link ParameterizedType} if
		 * <tt>searchClass</tt> is a real class or interface and <tt>type</tt> has
		 * parameters for it</li>
		 * <li>Returns an instance of {@link GenericArrayType} if
		 * <tt>searchClass</tt> is an array type, and <tt>type</tt> has type
		 * parameters for it</li>
		 * <li>Returns an instance of {@link Class} if <tt>type</tt> is a raw type,
		 * or has no type parameters for <tt>searchClass</tt></li>
		 * <li>Returns null if <tt>searchClass</tt> is not a superclass of type.
		 * </li>
		 * </ul>
		 * <p>
		 * For example, with
		 * <tt>class StringList implements List&lt;String&gt;</tt>,
		 * <tt>getExactSuperType(StringList.class, Collection.class)</tt> returns a
		 * {@link ParameterizedType} representing <tt>Collection&lt;String&gt;</tt>.
		 * </p>
		 */
		public static Type getExactSuperType(final Type type,
			final Class<?> searchClass)
		{
			if (type instanceof ParameterizedType || type instanceof Class ||
				type instanceof GenericArrayType)
			{
				final Class<?> clazz = erase(type);

				if (searchClass == clazz) {
					return type;
				}

				if (!searchClass.isAssignableFrom(clazz)) return null;
			}

			for (final Type superType : getExactDirectSuperTypes(type)) {
				final Type result = getExactSuperType(superType, searchClass);
				if (result != null) return result;
			}

			return null;
		}

		/**
		 * Gets the type parameter for a given type that is the value for a given
		 * type variable. For example, with
		 * <tt>class StringList implements List&lt;String&gt;</tt>,
		 * <tt>getTypeParameter(StringList.class, Collection.class.getTypeParameters()[0])</tt>
		 * returns <tt>String</tt>.
		 *
		 * @param type The type to inspect.
		 * @param variable The type variable to find the value for.
		 * @return The type parameter for the given variable. Or null if type is not
		 *         a subtype of the type that declares the variable, or if the
		 *         variable isn't known (because of raw types).
		 */
		public static Type getTypeParameter(final Type type,
			final TypeVariable<? extends Class<?>> variable)
		{
			final Class<?> clazz = variable.getGenericDeclaration();
			final Type superType = getExactSuperType(type, clazz);
			if (superType instanceof ParameterizedType) {
				final int index = Arrays.asList(clazz.getTypeParameters()).indexOf(
					variable);
				return ((ParameterizedType) superType).getActualTypeArguments()[index];
			}
			return null;
		}

		/**
		 * Checks if the capture of subType is a subtype of superType
		 */
		public static boolean isSuperType(final Type superType,
			final Type subType)
		{
			if (superType instanceof ParameterizedType ||
				superType instanceof Class || superType instanceof GenericArrayType)
			{
				final Class<?> superClass = erase(superType);
				final Type mappedSubType = getExactSuperType(capture(subType),
					superClass);
				if (mappedSubType == null) {
					return false;
				}
				else if (superType instanceof Class<?>) {
					return true;
				}
				else if (mappedSubType instanceof Class<?>) {
					// TODO treat supertype by being raw type differently ("supertype, but
					// with warnings")
					return true; // class has no parameters, or it's a raw type
				}
				else if (mappedSubType instanceof GenericArrayType) {
					final Type superComponentType = getArrayComponentType(superType);
					assert superComponentType != null;
					final Type mappedSubComponentType = getArrayComponentType(
						mappedSubType);
					assert mappedSubComponentType != null;
					return isSuperType(superComponentType, mappedSubComponentType);
				}
				else {
					assert mappedSubType instanceof ParameterizedType;
					final ParameterizedType pMappedSubType =
						(ParameterizedType) mappedSubType;
					assert pMappedSubType.getRawType() == superClass;
					final ParameterizedType pSuperType = (ParameterizedType) superType;

					final Type[] superTypeArgs = pSuperType.getActualTypeArguments();
					final Type[] subTypeArgs = pMappedSubType.getActualTypeArguments();
					assert superTypeArgs.length == subTypeArgs.length;
					for (int i = 0; i < superTypeArgs.length; i++) {
						if (!contains(superTypeArgs[i], subTypeArgs[i])) {
							return false;
						}
					}
					// params of the class itself match, so if the owner types are
					// supertypes too, it's a supertype.
					return pSuperType.getOwnerType() == null || isSuperType(pSuperType
						.getOwnerType(), pMappedSubType.getOwnerType());
				}
			}
			else if (superType instanceof CaptureType) {
				if (superType.equals(subType)) return true;
				for (final Type lowerBound : ((CaptureType) superType)
					.getLowerBounds())
				{
					if (isSuperType(lowerBound, subType)) {
						return true;
					}
				}
				return false;
			}
			else if (superType instanceof GenericArrayType) {
				return isArraySupertype(superType, subType);
			}
			else {
				throw new RuntimeException("not implemented: " + superType.getClass());
			}
		}

		private static boolean isArraySupertype(final Type arraySuperType,
			final Type subType)
		{
			final Type superTypeComponent = getArrayComponentType(arraySuperType);
			assert superTypeComponent != null;
			final Type subTypeComponent = getArrayComponentType(subType);
			if (subTypeComponent == null) { // subType is not an array type
				return false;
			}
			return isSuperType(superTypeComponent, subTypeComponent);
		}

		/**
		 * If type is an array type, returns the type of the component of the array.
		 * Otherwise, returns null.
		 */
		public static Type getArrayComponentType(final Type type) {
			if (type instanceof Class) {
				final Class<?> clazz = (Class<?>) type;
				return clazz.getComponentType();
			}
			else if (type instanceof GenericArrayType) {
				final GenericArrayType aType = (GenericArrayType) type;
				return aType.getGenericComponentType();
			}
			else {
				return null;
			}
		}

		private static boolean contains(final Type containingType,
			final Type containedType)
		{
			if (containingType instanceof WildcardType) {
				final WildcardType wContainingType = (WildcardType) containingType;
				for (final Type upperBound : wContainingType.getUpperBounds()) {
					if (!isSuperType(upperBound, containedType)) {
						return false;
					}
				}
				for (final Type lowerBound : wContainingType.getLowerBounds()) {
					if (!isSuperType(containedType, lowerBound)) {
						return false;
					}
				}
				return true;
			}
			return containingType.equals(containedType);
		}

		/**
		 * Returns the direct supertypes of the given type. Resolves type
		 * parameters.
		 */
		private static Type[] getExactDirectSuperTypes(final Type type) {
			if (type instanceof ParameterizedType || type instanceof Class) {
				Class<?> clazz;
				if (type instanceof ParameterizedType) {
					clazz = (Class<?>) ((ParameterizedType) type).getRawType();
				}
				else {
					// TODO primitive types?
					clazz = (Class<?>) type;
					if (clazz.isArray()) return getArrayExactDirectSuperTypes(clazz);
				}

				final Type[] superInterfaces = clazz.getGenericInterfaces();
				final Type superClass = clazz.getGenericSuperclass();
				Type[] result;
				int resultIndex;
				if (superClass == null) {
					result = new Type[superInterfaces.length];
					resultIndex = 0;
				}
				else {
					result = new Type[superInterfaces.length + 1];
					resultIndex = 1;
					// HACK: Case where type is not parameterized but superClass is
					if (superClass instanceof ParameterizedType &&
						!(type instanceof ParameterizedType))
					{
						result[0] = superClass;
					}
					else {
						result[0] = mapTypeParameters(superClass, type);
					}
				}
				for (final Type superInterface : superInterfaces) {
					result[resultIndex++] = mapTypeParameters(superInterface, type);
				}

				return result;
			}
			else if (type instanceof TypeVariable) {
				final TypeVariable<?> tv = (TypeVariable<?>) type;
				return tv.getBounds();
			}
			else if (type instanceof WildcardType) {
				// This should be a rare case: normally this wildcard is already
				// captured.
				// But it does happen if the upper bound of a type variable contains a
				// wildcard
				// TODO shouldn't upper bound of type variable have been captured too?
				// (making this case impossible?)
				return ((WildcardType) type).getUpperBounds();
			}
			else if (type instanceof CaptureType) {
				return ((CaptureType) type).getUpperBounds();
			}
			else if (type instanceof GenericArrayType) {
				return getArrayExactDirectSuperTypes(type);
			}
			else {
				throw new RuntimeException("not implemented type: " + type);
			}
		}

		private static Type[] getArrayExactDirectSuperTypes(final Type arrayType) {
			// see
			// http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#4.10.3
			final Type typeComponent = getArrayComponentType(arrayType);

			Type[] result;
			int resultIndex;
			if (typeComponent instanceof Class && ((Class<?>) typeComponent)
				.isPrimitive())
			{
				resultIndex = 0;
				result = new Type[3];
			}
			else {
				final Type[] componentSupertypes = getExactDirectSuperTypes(
					typeComponent);
				result = new Type[componentSupertypes.length + 3];
				for (resultIndex =
					0; resultIndex < componentSupertypes.length; resultIndex++)
				{
					result[resultIndex] = array(componentSupertypes[resultIndex]);
				}
			}
			result[resultIndex++] = Object.class;
			result[resultIndex++] = Cloneable.class;
			result[resultIndex++] = Serializable.class;
			return result;
		}

		/**
		 * Returns the exact return type of the given method in the given type. This
		 * may be different from <tt>m.getGenericReturnType()</tt> when the method
		 * was declared in a superclass, or <tt>type</tt> has a type parameter that
		 * is used in the return type, or <tt>type</tt> is a raw type.
		 */
		public static Type getExactReturnType(final Method m, final Type type) {
			final Type returnType = m.getGenericReturnType();
			final Type exactDeclaringType = getExactSuperType(capture(type), m
				.getDeclaringClass());
			if (exactDeclaringType == null) {
				// capture(type) is not a subtype of m.getDeclaringClass()
				throw new IllegalArgumentException("The method " + m +
					" is not a member of type " + type);
			}
			return mapTypeParameters(returnType, exactDeclaringType);
		}

		/**
		 * Returns the exact type of the given field in the given type. This may be
		 * different from <tt>f.getGenericType()</tt> when the field was declared in
		 * a superclass, or <tt>type</tt> has a type parameter that is used in the
		 * type of the field, or <tt>type</tt> is a raw type.
		 */
		public static Type getExactFieldType(final Field f, final Type type) {
			final Type returnType = f.getGenericType();
			final Type exactDeclaringType = getExactSuperType(capture(type), f
				.getDeclaringClass());
			if (exactDeclaringType == null) {
				// capture(type) is not a subtype of f.getDeclaringClass()
				throw new IllegalArgumentException("The field " + f +
					" is not a member of type " + type);
			}
			return mapTypeParameters(returnType, exactDeclaringType);
		}

		/**
		 * Returns the exact parameter types of the given method in the given type.
		 * This may be different from <tt>m.getGenericParameterTypes()</tt> when the
		 * method was declared in a superclass, or <tt>type</tt> has a type
		 * parameter that is used in one of the parameters, or <tt>type</tt> is a
		 * raw type.
		 */
		public static Type[] getExactParameterTypes(final Method m,
			final Type type)
		{
			final Type[] parameterTypes = m.getGenericParameterTypes();
			final Type exactDeclaringType = getExactSuperType(capture(type), m
				.getDeclaringClass());
			if (exactDeclaringType == null) {
				// capture(type) is not a subtype of m.getDeclaringClass()
				throw new IllegalArgumentException("The method " + m +
					" is not a member of type " + type);
			}

			final Type[] result = new Type[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				result[i] = mapTypeParameters(parameterTypes[i], exactDeclaringType);
			}
			return result;
		}

		/**
		 * Applies capture conversion to the given type.
		 */
		public static Type capture(final Type type) {
			final VarMap varMap = new VarMap();
			final List<CaptureTypeImpl> toInit = new ArrayList<>();
			if (type instanceof ParameterizedType) {
				final ParameterizedType pType = (ParameterizedType) type;
				final Class<?> clazz = (Class<?>) pType.getRawType();
				final Type[] arguments = pType.getActualTypeArguments();
				final TypeVariable<?>[] vars = clazz.getTypeParameters();
				final Type[] capturedArguments = new Type[arguments.length];
				assert arguments.length == vars.length;
				for (int i = 0; i < arguments.length; i++) {
					Type argument = arguments[i];
					if (argument instanceof WildcardType) {
						final CaptureTypeImpl captured = new CaptureTypeImpl(
							(WildcardType) argument, vars[i]);
						argument = captured;
						toInit.add(captured);
					}
					capturedArguments[i] = argument;
					varMap.add(vars[i], argument);
				}
				for (final CaptureTypeImpl captured : toInit) {
					captured.init(varMap);
				}
				final Type ownerType = pType.getOwnerType() == null ? null : capture(
					pType.getOwnerType());
				return parameterize(clazz, ownerType, capturedArguments);
			}
			return type;
		}

		/**
		 * Returns list of classes and interfaces that are supertypes of the given
		 * type. For example given this class:
		 * <tt>class Foo&lt;A extends Number & Iterable&lt;A&gt;, B extends A&gt;</tt>
		 * <br>
		 * calling this method on type parameters <tt>B</tt>
		 * (<tt>Foo.class.getTypeParameters()[1]</tt>) returns a list containing
		 * <tt>Number</tt> and <tt>Iterable</tt>.
		 * <p>
		 * This is mostly useful if you get a type from one of the other methods in
		 * <tt>GenericTypeReflector</tt>, but you don't want to deal with all the
		 * different sorts of types, and you are only really interested in concrete
		 * classes and interfaces.
		 * </p>
		 *
		 * @return A List of classes, each of them a supertype of the given type. If
		 *         the given type is a class or interface itself, returns a List
		 *         with just the given type. The list contains no duplicates, and is
		 *         ordered in the order the upper bounds are defined on the type.
		 */
		public static List<Class<?>> getUpperBoundClassAndInterfaces(
			final Type type)
		{
			final LinkedHashSet<Class<?>> result = new LinkedHashSet<>();
			buildUpperBoundClassAndInterfaces(type, result);
			return new ArrayList<>(result);
		}

		/**
		 * Helper method for getUpperBoundClassAndInterfaces, adding the result to
		 * the given set.
		 */
		private static void buildUpperBoundClassAndInterfaces(final Type type,
			final Set<Class<?>> result)
		{
			if (type instanceof ParameterizedType || type instanceof Class<?>) {
				result.add(erase(type));
				return;
			}

			for (final Type superType : getExactDirectSuperTypes(type)) {
				buildUpperBoundClassAndInterfaces(superType, result);
			}
		}
	}

	/**
	 * CaptureType represents a wildcard that has gone through capture conversion.
	 * It is a custom subinterface of Type, not part of the java builtin Type
	 * hierarchy.
	 *
	 * @author Wouter Coekaerts <wouter@coekaerts.be>
	 */
	private interface CaptureType extends Type {

		/**
		 * Returns an array of <tt>Type</tt> objects representing the upper bound(s)
		 * of this capture. This includes both the upper bound of a
		 * <tt>? extends</tt> wildcard, and the bounds declared with the type
		 * variable. References to other (or the same) type variables in bounds
		 * coming from the type variable are replaced by their matching capture.
		 */
		Type[] getUpperBounds();

		/**
		 * Returns an array of <tt>Type</tt> objects representing the lower bound(s)
		 * of this type variable. This is the bound of a <tt>? super</tt> wildcard.
		 * This normally contains only one or no types; it is an array for
		 * consistency with {@link WildcardType#getLowerBounds()}.
		 */
		Type[] getLowerBounds();
	}

	private static class CaptureTypeImpl implements CaptureType {

		private final WildcardType wildcard;
		private final TypeVariable<?> variable;
		private final Type[] lowerBounds;
		private Type[] upperBounds;

		/**
		 * Creates an uninitialized CaptureTypeImpl. Before using this type,
		 * {@link #init(VarMap)} must be called.
		 *
		 * @param wildcard The wildcard this is a capture of
		 * @param variable The type variable where the wildcard is a parameter for.
		 */
		public CaptureTypeImpl(final WildcardType wildcard,
			final TypeVariable<?> variable)
		{
			this.wildcard = wildcard;
			this.variable = variable;
			this.lowerBounds = wildcard.getLowerBounds();
		}

		/**
		 * Initialize this CaptureTypeImpl. This is needed for type variable bounds
		 * referring to each other: we need the capture of the argument.
		 */
		void init(final VarMap varMap) {
			final ArrayList<Type> upperBoundsList = new ArrayList<>();
			upperBoundsList.addAll(Arrays.asList(varMap.map(variable.getBounds())));

			final List<Type> wildcardUpperBounds = Arrays.asList(wildcard
				.getUpperBounds());
			if (wildcardUpperBounds.size() > 0 && wildcardUpperBounds.get(
				0) == Object.class)
			{
				// skip the Object bound, we already have a first upper bound from
				// 'variable'
				upperBoundsList.addAll(wildcardUpperBounds.subList(1,
					wildcardUpperBounds.size()));
			}
			else {
				upperBoundsList.addAll(wildcardUpperBounds);
			}
			upperBounds = new Type[upperBoundsList.size()];
			upperBoundsList.toArray(upperBounds);
		}

		@Override
		public Type[] getLowerBounds() {
			return lowerBounds.clone();
		}

		@Override
		public Type[] getUpperBounds() {
			assert upperBounds != null;
			return upperBounds.clone();
		}

		@Override
		public String toString() {
			return "capture of " + wildcard;
		}
	}

	/**
	 * Mapping between type variables and actual parameters.
	 *
	 * @author Wouter Coekaerts <wouter@coekaerts.be>
	 */
	private static class VarMap {

		private final Map<TypeVariable<?>, Type> map = new HashMap<>();

		/**
		 * Creates an empty VarMap
		 */
		VarMap() {}

		void add(final TypeVariable<?> variable, final Type value) {
			map.put(variable, value);
		}

		void addAll(final TypeVariable<?>[] variables, final Type[] values) {
			assert variables.length == values.length;
			for (int i = 0; i < variables.length; i++) {
				map.put(variables[i], values[i]);
			}
		}

		Type map(final Type type) {
			if (type instanceof Class) {
				return type;
			}
			else if (type instanceof TypeVariable) {
				assert map.containsKey(type);
				return map.get(type);
			}
			else if (type instanceof ParameterizedType) {
				final ParameterizedType pType = (ParameterizedType) type;
				return parameterize((Class<?>) pType.getRawType(), pType
					.getOwnerType() == null ? pType.getOwnerType() : map(pType
						.getOwnerType()), map(pType.getActualTypeArguments()));
			}
			else if (type instanceof WildcardType) {
				final WildcardType wType = (WildcardType) type;
				return new TypeUtils.WildcardTypeImpl(map(wType.getUpperBounds()),
					map(wType.getLowerBounds()));
			}
			else if (type instanceof GenericArrayType) {
				return array(map(((GenericArrayType) type).getGenericComponentType()));
			}
			else {
				throw new RuntimeException("not implemented: mapping " + type
					.getClass() + " (" + type + ")");
			}
		}

		Type[] map(final Type[] types) {
			final Type[] result = new Type[types.length];
			for (int i = 0; i < types.length; i++) {
				result[i] = map(types[i]);
			}
			return result;
		}
	}

	// -- END FORK OF GENTYREF 1.1.0 CODE --
}
