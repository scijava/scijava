/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package org.scijava.ops.transform;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.scijava.ops.types.Any;
import org.scijava.util.Types;

/**
 * Utility class to do modification on {@link Type}s.
 * 
 * @author David Kolb
 */
public final class TypeModUtils {

	private TypeModUtils() {
		// NB: prevent instantiation of utility class.
	}

	// -- Utility methods --
	

	/**
	 * Attempts to perform "unlifting" of the specified types. See {@link #unliftParameterizedType(Type, Class, Class, Integer...)}.
	 * The passed arry of types will be mutated inplace. Returns a boolean indicating if the list changed. 
	 * 
	 * @param types
	 * @param searchRawType
	 * @param unliftRawArgType
	 * @param argIndices
	 * @return
	 */
	public static boolean unliftParameterizedTypes(Type[] types, Class<?> searchRawType, Class<?> unliftRawArgType, Integer... argIndices) {
		return mutateTypes(types, t -> unliftParameterizedType(t, searchRawType, unliftRawArgType, argIndices));
	}
	
	/**
	 * Attempts to "unlift" the type arguments of the specified parameterized type if:
	 * <ul>
	 * <li>The type is a {@link ParameterizedType}</li>
	 * <li>The types raw type equals the search raw type</li>
	 * <li>The raw type of the type argument equals the specified unlift raw argument type</li>
	 * </ul>
	 * Otherwise, null will be returned indicating that "unlifting" is not possible.
	 * The list of integers specifies which type arguments should be considered. If non are given,
	 * all arguments will be processed. If arrays (reified or generic) should be "unlifted", 
	 * {@link Array}.class can be passed as the unlift raw arg type. Also see
	 * {@link #unliftType(Type, Class)}.</br></br>
	 * E.g.
	 * <pre>
	 * The type:
	 *    Function&lt;Iterable&lt;Double&gt;, String[]&gt;
	 *    
	 * With: searchRawType = Function.class and unliftRawArgType = Iterable.class
	 * will result in:
	 *    Function&lt;Double, String[]&gt;
	 *    
	 * With: searchRawType = Function.class and unliftRawArgType = Array.class
	 * will result in:
	 *    Function&lt;Iterable&lt;Double&gt;, String&gt;
	 *    
	 * </pre>
	 * 
	 * 
	 * @param type the type to "unlift"
	 * @param searchRawType the raw type to check for
	 * @param unliftRawArgType the raw type of type arguments that should be unlifted
	 * @param argIndices indices of type arguments to process
	 * @return "unlifted" type or null if not possible
	 */
	public static Type unliftParameterizedType(Type type, Class<?> searchRawType, Class<?> unliftRawArgType, Integer... argIndices) {
		if (type instanceof ParameterizedType) {
			ParameterizedType casted = (ParameterizedType) type;
			
			if (Types.raw(type).equals(searchRawType)) {
				Type[] typeArgs = casted.getActualTypeArguments();
				boolean hit = unliftTypes(typeArgs, unliftRawArgType, argIndices);
				// Types can't be parameterized with primitives in java.
				// Hence, if the unlifting of type args would result in
				// primitives, we would create an invalid type
				boolean containsPrimitive = containsPrimitive(typeArgs);
				if(hit && !containsPrimitive) {
					return Types.parameterize(searchRawType, typeArgs);
				}
			}
		} 
		return null;
	}
	
	/**
	 * Attempts to perform "unlifting" of the specified types. See {@link #unliftType(Type, Class)}.
	 * The passed arry of types will be mutated inplace. Returns a boolean indicating if the list changed.
	 * The list of integers specifies which types in the passed array should be considered. If non are given,
	 * all types will be processed.
	 * 
	 * @param types
	 * @param unliftRawType
	 * @param argIndices
	 * @return
	 */
	public static boolean unliftTypes(Type[] types, Class<?> unliftRawType, Integer... indices) {
		return mutateTypes(types, t -> unliftType(t, unliftRawType), indices);
	}
	
	/**
	 * Attempts to "unlift" the specified type directly if its raw type matches the specified unlift raw type.
	 * "Unlifiting" will be performed in two cases:
	 * <ol>
	 * <li>The specified type is an array type (either reified or generic, pass {@link Array}.class as unlift raw type). 
	 * Then the component type will be returned.</li>
	 * <li>The specified type is a parameterized type. Then the first type argument will be returned if the parameterized
	 * types raw type equals the unlift raw type and it has exactly one type argument.</li>
	 * </ol>
	 * Otherwise null will be returned indicating that "unlifting" is not possible.</br></br>
	 * E.g.
	 * <pre>
	 * The type:
	 *    Iterable&lt;Double&gt;
	 *    
	 * With: unliftRawType = Iterable.class
	 * will result in:
	 *    Double
	 *    
	 * The type:
	 *    Double[]
	 *    
	 * With: unliftRawType = Array.class
	 * will result in:
	 *    Double
	 * </pre>
	 * 
	 * @param type the type to "unlift"
	 * @param unliftRawType the raw type to check for
	 * @return "unlifted" type or null if not possible
	 */
	public static Type unliftType(Type type, Class<?> unliftRawType) {
		if (unliftRawType.equals(Array.class)) {
			return Types.component(type);
		} else if (type instanceof ParameterizedType) {
			ParameterizedType casted = (ParameterizedType) type;
			
			if (Types.raw(casted).equals(unliftRawType)) {
				Type[] typeArgs = casted.getActualTypeArguments();
				if (typeArgs.length == 1) {
					return typeArgs[0];
				}
			}
		}
		return null;
	}


	/**
	 * Attempts to perform raw type replacement of the specified types. See {@link #replaceRawType(Type, Class, Class)}.
	 * The passed array of types will be mutated inplace. Returns a boolean indicating if the list changed. 
	 * 
	 * @param types
	 * @param searchClass
	 * @param replacement
	 * @param indices
	 * @return
	 */
	public static boolean replaceRawTypes(Type[] types, Class<?> searchClass, Class<?> replacement, Integer... indices) {
		return mutateTypes(types, t -> replaceRawType(t, searchClass, replacement), indices);
	}

	/**
	 * Attempts to exchange the raw type of the specified type with the specified replacement if the original 
	 * raw type equals the search class. This method does not check if the returned type is actually permitted
	 * (i.e. if the passed type is a parameterized type, the replacement may have a different amount of type
	 * arguments). If type is a class, the replacement will be returned if the class equals the search class.
	 * If type is a parameterized type, its raw type will be exchanged.
	 * 
	 * @param type the type whose raw type should be replaced
	 * @param searchClass the raw type to check for
	 * @param replacement the replacement type
	 * @return
	 */
	public static Type replaceRawType(Type type, Class<?> searchClass, Class<?> replacement) {
		if (type instanceof ParameterizedType) {
			ParameterizedType casted = (ParameterizedType) type;
			Class<?> rawType = Types.raw(type);
			
			if (rawType.equals(searchClass)) {
				return Types.parameterize(replacement, casted.getActualTypeArguments());
			}
		} else if (type instanceof Class) {
			if (((Class<?>) type).equals(searchClass)) {
				return replacement;
			}
		}
		return null;
	}
	
	/**
	 * Insert the specified type at the specified index into the specified array.
	 * Has the same behavior as {@link List#add(int, Object)}.
	 * 
	 * @param types
	 * @param type
	 * @param index
	 * @return
	 */
	public static Type[] insert(Type[] types, Type type, int index) {
		List<Type> out = new ArrayList<>();
		out.addAll(Arrays.asList(types));
		if(index < out.size()) out.add(index, type);
		else out.add(type);
		return out.toArray(new Type[out.size()]);
	}
	
	/**
	 * Remove the specified index from the specified array.
	 * Has the same behavior as {@link List#remove(int)}.
	 * 
	 * @param types
	 * @param index
	 * @return
	 */
	public static Type[] remove(Type[] types, int index) {
		List<Type> out = new ArrayList<>();
		out.addAll(Arrays.asList(types));
		if(index < types.length) out.remove(index);
		return out.toArray(new Type[out.size()]);
	}
	
	private static boolean mutateTypes(Type[] types, Function<Type, Type> typeFunc, Integer... indices) {
		List<Integer> is = Arrays.asList(indices);
		
		boolean hit = false;
		for (int i = 0; i < types.length; i++) {
			if (!is.contains(i) && !is.isEmpty()) {
				continue;
			}
			Type replaced = typeFunc.apply(types[i]);
			
			if (replaced != null) {
				types[i] = replaced;
				hit = true;
			}
		}
		return hit;
	}
	
	private static boolean containsPrimitive(Type[] types) {
		return Arrays.stream(types).filter(t -> !(t instanceof Any)).anyMatch(t -> Types.raw(t).isPrimitive());
	}
}
