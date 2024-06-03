/*-
 * #%L
 * SciJava library for generic type reasoning.
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

package org.scijava.types.infer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.common3.Any;
import org.scijava.types.Nil;

public class InferTypeVariablesTest {

	class Bar {}

	static class FooThing extends RecursiveThing<FooThing> {}

	static class BazThing extends RecursiveThing<BazThing> {}

	static abstract class RecursiveThing<T extends RecursiveThing<T>> {}

	static abstract class RecursiveSubThing<U> extends
		RecursiveThing<RecursiveSubThing<U>>
	{}

	class StrangeThing<N extends Number, T> extends Thing<T> {}

	class Thing<T> {}

	class TypedBar<E> extends Bar {

		E type;
	}

	@Test
	public <T, U extends Comparable<Double>> void testInferFromTypeVar()
		throws TypeInferenceException
	{
		final Type compT = new Nil<Comparable<T>>() {}.type();
		final Type u = new Nil<U>() {}.type();

		final Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(compT, u, typeAssigns);

		// We expect T=Double
		final Type t = new Nil<T>() {}.type();
		final Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		TypeVariable<?> typeVarT = (TypeVariable<?>) t;
		expected.put(typeVarT, new TypeMapping(typeVarT, Double.class, false));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void testInferFromWildcardExtendingClass()
		throws TypeInferenceException
	{
		final Nil<List<? extends T>> listT = new Nil<>() {};
		final Nil<List<? extends Double>> listWildcard = new Nil<>() {};

		final Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(listT.type(), listWildcard
			.type(), typeAssigns);

		// We expect T= (? extends Double)
		final Type t = new Nil<T>() {}.type();
		final Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		TypeVariable<?> typeVarT = (TypeVariable<?>) t;
		Type mappedType = ((ParameterizedType) listWildcard.type())
			.getActualTypeArguments()[0];
		WildcardType mappedWildcard = (WildcardType) mappedType;
		expected.put(typeVarT, new WildcardTypeMapping(typeVarT, mappedWildcard,
			true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void
		testInferFromWildcardExtendingParameterizedType()
			throws TypeInferenceException
	{
		final Nil<List<? extends Comparable<T>>> listT = new Nil<>() {};
		final Nil<List<? extends Comparable<Double>>> listWildcard = new Nil<>() {};

		final Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(listT.type(), listWildcard
			.type(), typeAssigns);

		// We expect T=Double
		final Type t = new Nil<T>() {}.type();
		final Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		TypeVariable<?> typeVarT = (TypeVariable<?>) t;
		expected.put(typeVarT, new TypeMapping(typeVarT, Double.class, false));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void
		testInferGenericArrayTypeFromExtendingWildcardType()
			throws TypeInferenceException
	{
		final Type type = new Nil<List<T[]>>() {}.type();
		final Type inferFrom = new Nil<List<? extends Double[]>>() {}.type();

		// Example - If we try to infer U[] from (? extends Double[]), the U is NOT
		// malleable, because we can then "refine" U to Number.
		class Foo<U> implements Function<U[], U> {

			@Override
			public U apply(U[] ts) {
				return null;
			}
		}
		// ERRORS
//		Function<? extends Double[], Number> f = new Foo<>();

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect T=Double
		TypeVariable<?> typeVarT = (TypeVariable<?>) new Nil<T>() {}.type();
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		expected.put(typeVarT, new TypeMapping(typeVarT, Double.class, false));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void
		testInferGenericArrayTypeFromSuperWildcardType()
			throws TypeInferenceException
	{
		// Example - If we try to infer U[] from (? super Double[]), the U is
		// malleable, because we can then "refine" U to Number.
		class Foo<U> implements Function<U[], U> {

			@Override
			public U apply(U[] ts) {
				return null;
			}
		}
		Function<? super Double[], Number> f = new Foo<>();

		final Type type = new Nil<List<T[]>>() {}.type();
		final Type inferFrom = new Nil<List<? super Double[]>>() {}.type();

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect T=Double
		TypeVariable<?> typeVarT = (TypeVariable<?>) new Nil<T>() {}.type();
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		expected.put(typeVarT, new TypeMapping(typeVarT, Double.class, true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <O extends Number> void testInferOToAny()
		throws TypeInferenceException
	{
		final Type iterableO = new Nil<Iterable<O>>() {}.type();
		final Type object = Object.class;

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(iterableO, object, typeAssigns);

		// We expect O = Any
		TypeVariable<?> typeVarO = (TypeVariable<?>) new Nil<O>() {}.type();
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		expected.put(typeVarO, new TypeMapping(typeVarO, Any.class, true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <O extends Number> void testInferOToAnyWithClass()
		throws TypeInferenceException
	{
		final Type type = new Nil<TypedBar<O>>() {}.type();
		final Type inferFrom = Bar.class;

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect O = Any
		TypeVariable<?> typeVarO = (TypeVariable<?>) new Nil<O>() {}.type();
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		expected.put(typeVarO, new TypeMapping(typeVarO, Any.class, true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <O extends Number> void testInferOToAnyWithInterface()
		throws TypeInferenceException
	{
		final Type type = new Nil<ArrayList<O>>() {}.type();
		final Type inferFrom = Cloneable.class;

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect O = Any
		TypeVariable<?> typeVarO = (TypeVariable<?>) new Nil<O>() {}.type();
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		expected.put(typeVarO, new TypeMapping(typeVarO, Any.class, true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <O extends Number> void testInferOToAnyWithRawType()
		throws TypeInferenceException
	{
		final Type type = new Nil<TypedBar<O>>() {}.type();
		final Type inferFrom = TypedBar.class;

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect O = Any
		TypeVariable<?> typeVarO = (TypeVariable<?>) new Nil<O>() {}.type();
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		expected.put(typeVarO, new TypeMapping(typeVarO, Any.class, true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <O extends RecursiveThing<O>> void testInferRecursiveTypeVar() {
		final Type type = new Nil<O>() {}.type();
		final Type inferFrom = FooThing.class;

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect O = FooThing
		TypeVariable<?> typeVarO = (TypeVariable<?>) new Nil<O>() {}.type();
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		expected.put(typeVarO, new TypeMapping(typeVarO, FooThing.class, false));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void testInferSuperWildcard()
		throws TypeInferenceException
	{
		final Nil<List<? super T>> listT = new Nil<>() {};
		final Nil<List<? super Number>> listWildcard = new Nil<>() {};

		final Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(listT.type(), listWildcard
			.type(), typeAssigns);

		// We expect T=Number
		final Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		TypeVariable<?> typeVarT = (TypeVariable<?>) new Nil<T>() {}.type();
		expected.put(typeVarT, new TypeMapping(typeVarT, Number.class, true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <O extends Number, I extends O> void
		testInferTypeVarExtendingTypeVar()
	{
		final Type type = new Nil<I>() {}.type();
		final Type inferFrom = Double.class;

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect I= Double, O = Double
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		TypeVariable<?> typeVarI = (TypeVariable<?>) new Nil<I>() {}.type();
		expected.put(typeVarI, new TypeMapping(typeVarI, Double.class, false));
		TypeVariable<?> typeVarO = (TypeVariable<?>) new Nil<O>() {}.type();
		expected.put(typeVarO, new TypeMapping(typeVarO, Double.class, true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void testInferTypeVarInconsistentMapping()
		throws TypeInferenceException
	{

		final Type t = new Nil<T>() {}.type();

		final Type[] tArr = { t, t };
		final Type[] badInferFrom = { Integer.class, Double.class };

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		Assertions.assertThrows(TypeInferenceException.class,
			() -> GenericAssignability.inferTypeVariablesWithTypeMappings(tArr,
				badInferFrom, typeAssigns));
	}

	@Test
	public <T extends Number> void testInferWildcardAndClass()
		throws TypeInferenceException
	{

		// Example - the below should throw an exception because this inference is
		// not allowed.
		class Foo<N extends Number> implements Function<List<? super N>, N> {

			@Override
			public N apply(List<? super N> objects) {
				return null;
			}
		}
//		Function<List<? super Number>, Double> f = new Foo<Double>();
		final Nil<List<? super T>> listT = new Nil<>() {};
		final Nil<T> t = new Nil<>() {};
		final Nil<List<? super Number>> listWildcard = new Nil<>() {};

		Type[] types = new Type[] { listT.type(), t.type() };
		Type[] inferFroms = new Type[] { listWildcard.type(), Double.class };

		final Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		Assertions.assertThrows(TypeInferenceException.class,
			() -> GenericAssignability.inferTypeVariablesWithTypeMappings(types,
				inferFroms, typeAssigns));
	}

	@Test
	public <I, O> void testSupertypeTypeInference()
		throws TypeInferenceException
	{
		final Type t = new Nil<Function<Thing<I>, List<O>>>() {}.type();
		final Type[] tArgs = ((ParameterizedType) t).getActualTypeArguments();
		final Type dest =
			new Nil<Function<StrangeThing<Double, String>, List<Double>>>()
			{}.type();
		final Type[] destArgs = ((ParameterizedType) dest).getActualTypeArguments();

		final Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariablesWithTypeMappings(tArgs, destArgs,
			typeAssigns);

		// We expect I=String, O=Double
		final Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		TypeVariable<?> typeVarI = (TypeVariable<?>) ((ParameterizedType) tArgs[0])
			.getActualTypeArguments()[0];
		expected.put(typeVarI, new TypeMapping(typeVarI, String.class, false));
		TypeVariable<?> typeVarO = (TypeVariable<?>) ((ParameterizedType) tArgs[1])
			.getActualTypeArguments()[0];
		expected.put(typeVarO, new TypeMapping(typeVarO, Double.class, false));

		Assertions.assertEquals(typeAssigns, expected);
	}

	@Test
	public <T> void testWildcardTypeInference() throws TypeInferenceException {
		final Type t = new Nil<T>() {}.type();
		final Type listWild = new Nil<List<? extends T>>() {}.type();
		final Type integer = new Nil<Integer>() {}.type();
		final Type listDouble = new Nil<List<Double>>() {}.type();

		final Type[] types = { listWild, t };
		final Type[] inferFroms = { listDouble, integer };

		final Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		Assertions.assertThrows(TypeInferenceException.class,
			() -> GenericAssignability.inferTypeVariablesWithTypeMappings(types,
				inferFroms, typeAssigns));
	}

	// NB RecursiveSubThing<L> extends RecursiveThing<RecursiveSubThing<L>>
	@Test
	public <T extends RecursiveThing<T>, L extends RecursiveThing<L>> void
		inferImgLabelingFromRAI()
	{
		// This is a confusing test. Key insight
		// RecursiveSubThing has *two* type variables:
		// one *any* param, declared by RST
		// one recursive param, declared by superclass RecursiveThing
		// The "L" in this test is the "any" param, even though it itself is also
		// recursive in this case.
		var paramType = new Nil<RecursiveSubThing<L>>() {}.type();
		var argType = new Nil<T>() {}.type();

		Map<TypeVariable<?>, Type> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(new java.lang.reflect.Type[] {
			paramType }, new java.lang.reflect.Type[] { argType }, typeAssigns);
		TypeVariable<?> typeVar = (TypeVariable<?>) new Nil<L>() {}.type();
		final Map<TypeVariable<?>, Type> expected = new HashMap<>();
		expected.put(typeVar, Any.class);
		Assertions.assertEquals(expected, typeAssigns);
	}
}
