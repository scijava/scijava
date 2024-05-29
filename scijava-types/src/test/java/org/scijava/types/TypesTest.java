/*
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

package org.scijava.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.scijava.testutil.ExampleTypes.CircularThing;
import static org.scijava.testutil.ExampleTypes.ComplexThing;
import static org.scijava.testutil.ExampleTypes.IntegerThing;
import static org.scijava.testutil.ExampleTypes.Loop;
import static org.scijava.testutil.ExampleTypes.LoopingThing;
import static org.scijava.testutil.ExampleTypes.NestedThing;
import static org.scijava.testutil.ExampleTypes.NumberThing;
import static org.scijava.testutil.ExampleTypes.RecursiveThing;
import static org.scijava.testutil.ExampleTypes.StrangeThing;
import static org.scijava.testutil.ExampleTypes.StrangerThing;
import static org.scijava.testutil.ExampleTypes.Thing;
import static org.scijava.testutil.ExampleTypes.Words;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.common3.Classes;

/**
 * Tests {@link Types}.
 *
 * @author Curtis Rueden
 * @author Mark Hiner
 * @author Johannes Schindelin
 * @author Gabe Selzer
 */
public class TypesTest {

	/** Tests {@link Types#name}. */
	@Test
	public void testName() {
		@SuppressWarnings("unused")
		class Struct {
			private List<String> list;
		}
		assertEquals("boolean", Types.name(boolean.class));
		assertEquals("java.lang.String", Types.name(String.class));
		assertEquals("java.util.List<java.lang.String>", Types.name(type(Struct.class, "list")));
	}

	/** Tests {@link Types#raw(Type)}. */
	@Test
	public void testRaw() {
		@SuppressWarnings("unused")
		class Struct {

			private int[] intArray;
			private double d;
			private String[][] strings;
			private Void v;
			private List<String> list;
			private HashMap<Integer, Float> map;
		}
		assertSame(int[].class, raw(Struct.class, "intArray"));
		assertSame(double.class, raw(Struct.class, "d"));
		assertSame(String[][].class, raw(Struct.class, "strings"));
		assertSame(Void.class, raw(Struct.class, "v"));
		assertSame(List.class, raw(Struct.class, "list"));
		assertSame(HashMap.class, raw(Struct.class, "map"));
	}

	/** Tests {@link Types#raws}. */
	@Test
	public void testRaws() {
		final Field field = Classes.field(Thing.class, "thing");

		// Object
		assertAllTheSame(Types.raws(Types.typeOf(field, Thing.class)),
			Object.class);

		// N extends Number
		assertAllTheSame(Types.raws(Types.typeOf(field, NumberThing.class)),
			Number.class);

		// Integer
		assertAllTheSame(Types.raws(Types.typeOf(field, IntegerThing.class)),
			Integer.class);

		// Serializable & Cloneable
		assertAllTheSame(Types.raws(Types.typeOf(field, ComplexThing.class)),
			Serializable.class, Cloneable.class);
	}

	/** Tests {@link Types#component(Type)}. */
	@Test
	public void testComponent() {
		@SuppressWarnings("unused")
		class Struct {

			private int[] intArray;
			private double d;
			private String[][] strings;
			private Void v;
			private List<String>[] list;
			private HashMap<Integer, Float> map;
		}
		assertSame(int.class, componentType(Struct.class, "intArray"));
		assertNull(componentType(Struct.class, "d"));
		assertSame(String[].class, componentType(Struct.class, "strings"));
		assertSame(null, componentType(Struct.class, "v"));
		assertSame(List.class, componentType(Struct.class, "list"));
		assertSame(null, componentType(Struct.class, "map"));
	}

	/** Tests {@link Types#typeOf(Field, Class)}. */
	@Test
	public void testTypeOf() {
		final Field field = Classes.field(Thing.class, "thing");

		// T
		final Type tType = Types.typeOf(field, Thing.class);
		assertEquals("T", tType.toString());

		// N extends Number
		final Type nType = Types.typeOf(field, NumberThing.class);
		assertEquals("N", nType.toString());

		// Integer
		final Type iType = Types.typeOf(field, IntegerThing.class);
		assertSame(Integer.class, iType);
	}

	/** Tests {@link Types#typeParamsOf(Class, Class)}. */
	@Test
	public void testTypeParamsOfClass() {
		final Type[] argTypesRaw = Types.typeParamsOf(IntegerThing.class, Thing.class);
		assertEquals(1, argTypesRaw.length);
		assertSame(Integer.class, argTypesRaw[0]);
	}

	/** Tests {@link Types#typeParamsOf(Type, Class)}. */
	@Test
	public <U extends IntegerThing> void testTypeParamsOfType() {
		final Type arg = new Nil<U>() {}.type();
		final Type[] argTypes = Types.typeParamsOf(arg, Thing.class);
		assertNotNull(argTypes);
		assertEquals(1, argTypes.length);
		assertSame(Integer.class, argTypes[0]);
	}

	/** Tests {@link Types#isAssignable(Type, Type)}. */
	@Test
	public void testIsAssignable() {
		// check casting to superclass
		assertTrue(Types.isAssignable(String.class, Object.class));

		// check casting to interface
		assertTrue(Types.isAssignable(ArrayList.class, Collection.class));

		// casting numeric primitives is not supported
		assertFalse(Types.isAssignable(double.class, float.class));
		assertFalse(Types.isAssignable(float.class, double.class));

		// check boxing+widening of primitive numeric type
		assertTrue(Types.isAssignable(int.class, Number.class));

		// casting from null always works
		assertTrue(Types.isAssignable(null, Object.class));
		assertTrue(Types.isAssignable(null, int[].class));
	}

	/** Tests {@link Types#isAssignable(Type, Type)} from null to null. */
	@Test
	public void testIsAssignableNullToNull() {
		Assertions.assertThrows(NullPointerException.class,
			() -> Types.isAssignable(null, null));
	}

	/** Tests {@link Types#isAssignable(Type, Type)} from Class to null. */
	@Test
	public void testIsAssignableClassToNull() {
		Assertions.assertThrows(NullPointerException.class,
			() -> Types.isAssignable(Object.class, null));
	}

	/** Tests {@link Types#isAssignable(Type, Type)} with type variable. */
	@Test
	public <T extends Number> void testIsAssignableT() {
		var t = new Nil<T>() {}.type();
		var listRaw = List.class;
		var listT = new Nil<List<T>>() {}.type();
		var listNumber = new Nil<List<Number>>() {}.type();
		var listInteger = new Nil<List<Integer>>() {}.type();
		var listExtendsNumber = new Nil<List<? extends Number>>() {}.type();
		var listListRaw = new Nil<List<List>>() {}.type();
		var listListInteger = new Nil<List<List<Integer>>>() {}.type();

		assertTrue(Types.isAssignable(t, t));
		assertTrue(Types.isAssignable(listRaw, listRaw));
		assertTrue(Types.isAssignable(listT, listT));
		assertTrue(Types.isAssignable(listNumber, listNumber));
		assertTrue(Types.isAssignable(listInteger, listInteger));
		assertTrue(Types.isAssignable(listExtendsNumber, listExtendsNumber));

		assertTrue(Types.isAssignable(listRaw, listExtendsNumber));
		assertTrue(Types.isAssignable(listT, listExtendsNumber));
		assertTrue(Types.isAssignable(listNumber, listExtendsNumber));
		assertTrue(Types.isAssignable(listInteger, listExtendsNumber));

		assertTrue(Types.isAssignable(listRaw, listT));
		assertTrue(Types.isAssignable(listNumber, listT));
		assertTrue(Types.isAssignable(listInteger, listT));
		assertTrue(Types.isAssignable(listExtendsNumber, listT));
		// List<? extends Number> l = new ArrayList<>();
		// List<Number> l2 = (List<Number>) l;
		assertTrue(Types.isAssignable(listExtendsNumber, listNumber));

		assertTrue(Types.isAssignable(listT, listRaw));
		assertTrue(Types.isAssignable(listNumber, listRaw));
		assertTrue(Types.isAssignable(listInteger, listRaw));
		assertTrue(Types.isAssignable(listExtendsNumber, listRaw));
		assertTrue(Types.isAssignable(listListRaw, listRaw));
		assertTrue(Types.isAssignable(listListInteger, listRaw));

		// Nested Type Variables must be EXACTLY the same to be assignable
		assertFalse(Types.isAssignable(listListInteger, listListRaw));
		assertTrue(Types.isAssignable(listListRaw, listListRaw));
	}

	/**
	 * Tests {@link Types#isAssignable(Type, Type)} with type variables themselves
	 * parameterized with type variables.
	 */
	@Test
	public <N extends Number, S extends String, T extends List<N>> void
		testIsAssignableParameterizedT()
	{
		final Type t = new Nil<T>() {}.type();
		final Type listN = new Nil<List<N>>() {}.type();
		final Type listS = new Nil<List<S>>() {}.type();
		final Type listNumber = new Nil<List<Number>>() {}.type();
		final Type listInteger = new Nil<List<Integer>>() {}.type();
		final Type listExtendsNumber = new Nil<List<? extends Number>>() {}
			.type();
		// T list = (T) new ArrayList<N>();
		assertTrue(Types.isAssignable(listN, t));
		// T list = (T) new ArrayList<Number>();
		assertTrue(Types.isAssignable(listNumber, t));
		// T list = (T) new ArrayList<Integer>();
		assertTrue(Types.isAssignable(listInteger, t));
		// List<? extends Number> l = new ArrayList<Number>();
		// T list = (T) l;
		assertTrue(Types.isAssignable(listExtendsNumber, t));
		// T list = (T) new ArrayList<S>();
		assertFalse(Types.isAssignable(listS, t));
	}

	/** Tests {@link Types#isAssignable(Type, Type)} against {@link Object} */
	@Test
	public <T extends Number> void testIsAssignableObject() {
		final Type iterableT = new Nil<Iterable<T>>() {}.type();
		assertTrue(Types.isAssignable(iterableT, Object.class));
	}

	/** Tests {@link Types#isInstance(Object, Class)}. */
	@Test
	public void testIsInstance() {
		// casting from null always works
		final Object nullObject = null;
		assertTrue(Types.isInstance(nullObject, Object.class));
		assertTrue(Types.isInstance(nullObject, int[].class));

		// casting to null is not allowed
		assertFalse(Types.isInstance(nullObject, null));
		assertFalse(Types.isInstance(new Object(), null));
	}

	private static class RecursiveClass<T extends RecursiveClass<T>> {

	}

	/** Tests {@link Types#isRecursive(Type)} */
	@Test
	public void testIsRecursive() {
		assertFalse(Types.isRecursive(Types.parameterize(ArrayList.class)));
		assertTrue(Types.isRecursive(Types.parameterize(RecursiveClass.class)));
	}

	/** Tests {@link Types#isApplicable(Type[], Type[])} for raw classes. */
	@Test
	public void testIsApplicableRaw() {
		// f(Number, Integer)
		final Type[] dest = { Number.class, Integer.class };

		// f(Double, Integer)
		// [OK] Double -> Number
		final Type[] srcOK = { Double.class, Integer.class };
		assertEquals(-1, Types.isApplicable(srcOK, dest));

		// f(String, Integer)
		// [MISS] String is not assignable to Number
		final Type[] srcMiss = { String.class, Integer.class };
		assertNotEquals(-1, Types.isApplicable(srcMiss, dest));

	}

	/** Tests {@link Types#isApplicable(Type[], Type[])} for single arguments. */
	@Test
	public <T extends Number, U extends BigInteger> void testIsApplicableSingle() {
		// <T extends Number> f(T)
		final Type t = new Nil<T>() {}.type();
		final Type u = new Nil<U>() {}.type();
		final Type[] tDest = { t };

		assertEquals(-1, Types.isApplicable(new Type[] { Double.class }, tDest));
		assertEquals(-1, Types.isApplicable(new Type[] { Number.class }, tDest));
		assertEquals(-1, Types.isApplicable(new Type[] { t }, tDest));
		assertEquals(-1, Types.isApplicable(new Type[] { u }, tDest));
		// String does not extend Number
		assertNotEquals(-1, Types.isApplicable(new Type[] { String.class }, tDest));

		// -SINGLY RECURSIVE CALLS-

		// <T extends Number> f(List<T>)
		final Type listT = new Nil<List<T>>() {}.type();
		final Type[] listTDest = { listT };
		// <U extends BigInteger> f(List<U>)
		final Type listU = new Nil<List<U>>() {}.type();
		final Type[] listUDest = { listU };
		// f(List<Double>)
		final Type listDouble = new Nil<List<Double>>() {}.type();
		final Type[] listDoubleDest = { listDouble };
		// f(List<? super Number>)
		final Type listSuperNumber = new Nil<List<? super Number>>() {}.type();
		final Type[] listSuperNumberDest = { listSuperNumber };
		// f(List<? extends Number>)
		final Type listExtendsNumber = new Nil<List<? extends Number>>() {}
			.type();
		final Type[] listExtendsNumberDest = { listExtendsNumber };

		assertEquals(-1, Types.isApplicable(new Type[] { listT }, listTDest));
		assertEquals(-1, Types.isApplicable(listUDest, listTDest));
		// not all Numbers are BigIntegers.
		assertNotEquals(-1, Types.isApplicable(listTDest, listUDest));
		assertEquals(-1, Types.isApplicable(listTDest, listExtendsNumberDest));
		assertEquals(-1, Types.isApplicable(listUDest, listExtendsNumberDest));
		assertEquals(-1, Types.isApplicable(listTDest, listSuperNumberDest));
		// BigInteger extends Number, not the other way around.
		assertNotEquals(-1, Types.isApplicable(listUDest, listSuperNumberDest));
		assertEquals(-1, Types.isApplicable(listDoubleDest, listExtendsNumberDest));
		// Double extends Number, not the other way around.
		assertNotEquals(-1, Types.isApplicable(listDoubleDest, listSuperNumberDest));

		// -MULTIPLY RECURSIVE CALLS-

		final Type MapListTT = new Nil<Map<List<T>, T>>() {}.type();
		final Type MapListTU = new Nil<Map<List<T>, U>>() {}.type();
		final Type MapListUU = new Nil<Map<List<U>, U>>() {}.type();
		final Type MapListTDouble = new Nil<Map<List<T>, Double>>() {}.type();
		final Type MapListDoubleDouble = new Nil<Map<List<Double>, Double>>() {}
			.type();
		final Type MapListDoubleString = new Nil<Map<List<Double>, String>>() {}
			.type();
		final Type MapListDoubleNumber = new Nil<Map<List<Double>, Number>>() {}
			.type();
		final Type MapListNumberDouble = new Nil<Map<List<Number>, Double>>() {}
			.type();

		// T might not always extend BigInteger(U)
		assertNotEquals(-1, Types.isApplicable(new Type[] { MapListTT }, new Type[] {
			MapListTU }));
		// T might not always be the same as U
		assertNotEquals(-1, Types.isApplicable(new Type[] { MapListTU }, new Type[] {
			MapListTT }));
		assertEquals(-1, Types.isApplicable(new Type[] { MapListUU }, new Type[] {
			MapListTT }));
		// T might not always extend BigInteger(U)
		assertNotEquals(-1, Types.isApplicable(new Type[] { MapListTT }, new Type[] {
			MapListUU }));
		// T might not always be Double
		assertNotEquals(-1, Types.isApplicable(new Type[] { MapListTDouble },
			new Type[] { MapListTT }));
		// T does not extend String.
		assertNotEquals(-1, Types.isApplicable(new Type[] { MapListDoubleString },
			new Type[] { MapListTT }));
		assertEquals(-1, Types.isApplicable(new Type[] { MapListDoubleDouble },
			new Type[] { MapListTT }));
		// T is already fixed to Double (in a parameterized Map), cannot accommodate
		// Number.
		assertNotEquals(-1, Types.isApplicable(new Type[] { MapListNumberDouble },
			new Type[] { MapListTT }));
		// T is already fixed to Double (in a parameterized List) , cannot
		// accommodate Number
		assertNotEquals(-1, Types.isApplicable(new Type[] { MapListDoubleNumber },
			new Type[] { MapListTT }));
	}

	@Test
	public <T extends Number, U extends String, V extends BigInteger> void
		testIsApplicableGenericArrays()
	{
		// generic arrays
		final Type arrayT = new Nil<T[]>() {}.type();
		final Type arrayU = new Nil<U[]>() {}.type();
		final Type arrayV = new Nil<V[]>() {}.type();
		final Type arrayDouble = new Nil<Double[]>() {}.type();

		assertEquals(-1, Types.isApplicable(new Type[] { arrayDouble }, new Type[] {
			arrayT }));
		// Double does not extend String
		assertNotEquals(-1, Types.isApplicable(new Type[] { arrayDouble }, new Type[] {
			arrayU }));
		assertEquals(-1, Types.isApplicable(new Type[] { arrayT }, new Type[] {
			arrayT }));
		assertEquals(-1, Types.isApplicable(new Type[] { arrayV }, new Type[] {
			arrayT }));
		// Number does not extend BigInteger
		assertNotEquals(-1, Types.isApplicable(new Type[] { arrayT }, new Type[] {
			arrayV }));

		// generic multi-dimensional arrays
		final Type arrayT2D = new Nil<T[][]>() {}.type();
		final Type arrayV2D = new Nil<V[][]>() {}.type();
		final Type arrayDouble2D = new Nil<Double[][]>() {}.type();

		assertEquals(-1, Types.isApplicable(new Type[] { arrayDouble2D }, new Type[] {
			arrayT2D }));
		assertEquals(-1, Types.isApplicable(new Type[] { arrayV2D }, new Type[] {
			arrayT2D }));
		// A 2D array does not satisfy a 1D array
		assertNotEquals(-1, Types.isApplicable(new Type[] { arrayT2D }, new Type[] {
			arrayT }));
		// A 1D array does not satisfy a 2D array
		assertNotEquals(-1, Types.isApplicable(new Type[] { arrayT }, new Type[] {
			arrayT2D }));

		// generic parameterized type arrays
		final Type arrayListT = new Nil<List<T>[]>() {}.type();
		final Type arrayListDouble = new Nil<List<Double>[]>() {}.type();
		final Type arrayListString = new Nil<List<String>[]>() {}.type();

		assertEquals(-1, Types.isApplicable(new Type[] { arrayListDouble }, new Type[] {
			arrayListT }));
		// String does not extend Number
		assertNotEquals(-1, Types.isApplicable(new Type[] { arrayListString },
			new Type[] { arrayListT }));
		// Number does not extend BigInteger
		assertNotEquals(-1, Types.isApplicable(new Type[] { arrayListT }, new Type[] {
			arrayU }));

	}

	@Test
	public <S, T extends Thing<S>, U extends IntegerThing, V extends RecursiveThing<V>, W extends RecursiveThing<W> & Loop, X extends Thing<S> & Loop>
		void testIsApplicableTypeVariables()
	{
		final Type t = new Nil<T>() {}.type();
		final Type u = new Nil<U>() {}.type();
		final Type thingInt = new Nil<Thing<Integer>>() {}.type();
		final Type numberThingInt = new Nil<NumberThing<Integer>>() {}.type();
		final Type numberThingDouble = new Nil<NumberThing<Double>>() {}.type();
		final Type strangeThingDouble = new Nil<StrangeThing<Double>>() {}.type();
		final Type strangerThingString = new Nil<StrangerThing<String>>() {}.type();
		final Type integerThing = new Nil<IntegerThing>() {}.type();

		assertEquals(-1, Types.isApplicable(
			new Type[] { thingInt, thingInt, numberThingInt, integerThing },
			new Type[] { t, t, t, t }
		));
		assertEquals(-1, Types.isApplicable(
			new Type[] { thingInt, numberThingInt, strangerThingString },
			new Type[] { t, t, t }
		));
		assertEquals(-1, Types.isApplicable(
			new Type[] { thingInt, numberThingInt, integerThing },
			new Type[] { t, t, t }
		));
		assertEquals(-1, Types.isApplicable(
			new Type[] { numberThingInt, strangeThingDouble },
			new Type[] { t, t }
		));
		// S cannot accommodate a Double since S is already locked to Integer from
		// the first argument.
		assertNotEquals(-1, Types.isApplicable(
			new Type[] { thingInt, numberThingInt, numberThingDouble },
			new Type[] { t, t, t }
		));
		assertEquals(-1, Types.isApplicable(new Type[] { u }, new Type[] { t }));

		// recursive Type Variables
		final Type circularThing = new Nil<CircularThing>() {}.type();
		final Type loopingThing = new Nil<LoopingThing>() {}.type();
		final Type recursiveThingCircular =
			new Nil<RecursiveThing<CircularThing>>()
			{}.type();
		final Type v = new Nil<V>() {}.type();
		final Type w = new Nil<W>() {}.type();
		final Type x = new Nil<X>() {}.type();

		assertEquals(-1, Types.isApplicable(new Type[] { circularThing, circularThing,
			loopingThing }, new Type[] { t, t, t }));
		// V cannot accommodate LoopingThing since V is already locked to
		// CircularThing
		assertNotEquals(-1, Types.isApplicable(new Type[] { circularThing,
			circularThing, loopingThing }, new Type[] { v, v, v }));
		// V cannot accommodate RecursiveThing since V is already locked to
		// CircularThing (V has to extend RecursiveThing<itself>, not
		// RecursiveThing<not itself>).
		assertNotEquals(-1, Types.isApplicable(new Type[] { circularThing,
			circularThing, recursiveThingCircular }, new Type[] { v, v, v }));
		// V cannot accommodate RecursiveThing<CircularThing> since V must extend
		// RecursiveThing<V> (it cannot extend RecursiveThing<not V>)
		assertNotEquals(-1, Types.isApplicable(new Type[] { recursiveThingCircular,
			recursiveThingCircular, recursiveThingCircular }, new Type[] { v, v, v }));
		assertEquals(-1, Types.isApplicable(new Type[] { recursiveThingCircular,
			recursiveThingCircular, recursiveThingCircular }, new Type[] { t, t, t }));
		assertEquals(Types.isApplicable(new Type[] { circularThing, circularThing,
			circularThing }, new Type[] { w, w, w }), -1);
		// W cannot accommodate LoopingThing since W is already
		// fixed to CircularThing
		assertNotEquals(-1, Types.isApplicable(new Type[] { circularThing, loopingThing,
			circularThing }, new Type[] { w, w, w }));
		assertEquals(-1, Types.isApplicable(new Type[] { circularThing, loopingThing,
			circularThing }, new Type[] { x, x, x }));

	}

	/**
	 * Tests {@link Types#isApplicable(Type[], Type[])} when the same type
	 * parameter appears across multiple destination types.
	 */
	@Test
	public <T> void testIsApplicableMatchingT() {
		// <T> f(List<T>, List<T>)
		final Type[] params = { //
			new Nil<List<T>>()
			{}.type(), //
			new Nil<List<T>>()
			{}.type(), //
		};

		// f(List<Integer>, List<Integer>)
		// [OK] T -> Integer
		final Type[] argsOK = { //
			new Nil<List<Integer>>()
			{}.type(), //
			new Nil<List<Integer>>()
			{}.type() };
		assertEquals(-1, Types.isApplicable(argsOK, params));

		// f(List<String>, List<Number>)
		// [MISS] T cannot be both String and Number
		final Type[] argsMiss = { //
			new Nil<List<Double>>()
			{}.type(), //
			new Nil<List<Number>>()
			{}.type() //
		};
		assertNotEquals(-1, Types.isApplicable(argsMiss, params));
	}

	@Test
	public <N, C> void testIsApplicableWildcards() {
		var n = new Nil<List<N>>() {};
		var c = new Nil<List<C>>() {};
		var nWildcard = new Nil<List<? extends Number>>() {};

		Type[] params = { n.type() };
		Type[] argsOk = { nWildcard.type() };
		assertEquals(-1, Types.isApplicable(argsOk, params));

		params = new Type[] { n.type(), c.type() };
		argsOk = new Type[] { nWildcard.type(), nWildcard.type() };
		assertEquals(-1, Types.isApplicable(argsOk, params));

		params = new Type[] { n.type(), n.type() };
		Type[] argsNotOk = { nWildcard.type(), nWildcard.type() };
		assertNotEquals(-1, Types.isApplicable(argsNotOk, params));
	}

	@Test
	public <N> void testIsApplicableWildcardsInParameterizedType() {
		var n = new Nil<N>() {};
		var ln = new Nil<List<N>>() {};
		var lw = new Nil<List<? extends Number>>() {};

		Type[] params = { n.type(), ln.type() };
		Type[] argsNotOk = { Integer.class, lw.type() };
		assertNotEquals(-1, Types.isApplicable(argsNotOk, params));

		params = new Type[] { ln.type(), n.type() };
		argsNotOk = new Type[] { lw.type(), Integer.class };
		assertNotEquals(-1, Types.isApplicable(argsNotOk, params));
	}

	@Test
	public <N extends Number, C extends List<String>> void
		testIsApplicableBoundedWildcards()
	{
		var n = new Nil<List<N>>() {};
		var c = new Nil<List<C>>() {};
		var nNumberWildcard = new Nil<List<? extends Number>>() {};
		var nListWildcard = new Nil<List<? extends List<String>>>() {};

		Type[] params = { n.type() };
		Type[] argsOk = { nNumberWildcard.type() };
		assertEquals(-1, Types.isApplicable(argsOk, params));

		params = new Type[] { n.type(), c.type() };
		argsOk = new Type[] { nNumberWildcard.type(), nListWildcard.type() };
		assertEquals(-1, Types.isApplicable(argsOk, params));

		params = new Type[] { n.type(), c.type() };
		Type[] argsNotOk = { nNumberWildcard.type(), nNumberWildcard.type() };
		assertNotEquals(-1, Types.isApplicable(argsNotOk, params));

		params = new Type[] { n.type(), n.type() };
		argsNotOk = new Type[] { nNumberWildcard.type(), nNumberWildcard.type() };
		assertNotEquals(-1, Types.isApplicable(argsNotOk, params));
	}

	/**
	 * Tests {@link Types#isApplicable(Type[], Type[])} when the given type is
	 * indirectly parameterized by implementing an parameterized interface.
	 */
	@Test
	public <I1, I2> void testIsApplicableIndirectTypeVariables() {

		abstract class NestedThingImplOK1 implements NestedThing<Double, Double> {}

		final Type[] param = new Type[] { new Nil<Function<I1, I2>>() {}
			.type() };
		Type[] argOK = new Type[] { NestedThingImplOK1.class };
		assertEquals(-1, Types.isApplicable(argOK, param));
	}

	/**
	 * Tests {@link Types#isApplicable(Type[], Type[])} when unbounded type
	 * variables are expected but the given ones are nested and bounded.
	 */
	@Test
	public <I1, I2> void testIsApplicableUnboundedTypeVariables() {

		abstract class NestedThingImplOK1 implements
			Function<Iterable<Double>, Consumer<Double>>
		{}

		abstract class NestedThingImplOK2 implements
			Function<Iterable<Double>, Consumer<Integer>>
		{}

		abstract class NestedThingImplOK3 implements
			Function<Double, Consumer<Integer>>
		{}

		final Type[] param = new Type[] { new Nil<Function<I1, I2>>() {}
			.type() };
		Type[] argOK = new Type[] { NestedThingImplOK1.class };
		assertEquals(-1, Types.isApplicable(argOK, param));

		argOK = new Type[] { NestedThingImplOK2.class };
		assertEquals(-1, Types.isApplicable(argOK, param));

		argOK = new Type[] { NestedThingImplOK3.class };
		assertEquals(-1, Types.isApplicable(argOK, param));
	}

	/** Tests {@link Types#cast(Object, Class)}. */
	@Test
	public void testCast() {
		// check casting to superclass
		final String string = "Hello";
		final Object stringToObject = Types.cast(string, Object.class);
		assertSame(string, stringToObject);

		// check casting to interface
		final ArrayList<?> arrayList = new ArrayList<>();
		final Collection<?> arrayListToCollection = //
			Types.cast(arrayList, Collection.class);
		assertSame(arrayList, arrayListToCollection);

		// casting numeric primitives is not supported
		final Float doubleToFloat = Types.cast(5.1, float.class);
		assertNull(doubleToFloat);
		final Double floatToDouble = Types.cast(5.1f, double.class);
		assertNull(floatToDouble);

		// boxing works though
		final Number intToNumber = Types.cast(5, Number.class);
		assertSame(Integer.class, intToNumber.getClass());
		assertEquals(5, intToNumber.intValue());
	}

	/** Tests {@link Types#enumValue(String, Class)}. */
	@Test
	public void testEnumValue() {
		final Words foo = Types.enumValue("FOO", Words.class);
		assertSame(Words.FOO, foo);
		final Words bar = Types.enumValue("BAR", Words.class);
		assertSame(Words.BAR, bar);
		final Words fubar = Types.enumValue("FUBAR", Words.class);
		assertSame(Words.FUBAR, fubar);
	}

	/** Tests {@link Types#enumValue(String, Class)} for invalid value. */
	@Test
	public void testEnumValueNoConstant() {
		Assertions.assertThrows(IllegalArgumentException.class,
			() -> Types.enumValue("NONE", Words.class));
	}

	/** Tests {@link Types#enumValue(String, Class)} for non-enum class. */
	@Test
	public void testEnumValueNonEnum() {
		Assertions.assertThrows(IllegalArgumentException.class,
			() -> Types.enumValue("HOOYAH", String.class));
	}

	// -- Helper methods --

	/** Convenience method to get the {@link Type} of a field. */
	private Type type(final Class<?> c, final String fieldName) {
		return Classes.field(c, fieldName).getGenericType();
	}

	/** Convenience method to call {@link Types#raw} on a field. */
	private Class<?> raw(final Class<?> c, final String fieldName) {
		return Types.raw(type(c, fieldName));
	}

	/** Convenience method to call {@link Types#component} on a field. */
	private Class<?> componentType(final Class<?> c, final String fieldName) {
		return Types.raw(Types.component(type(c, fieldName)));
	}

	private void assertAllTheSame(final List<?> actual, final Object... expected) {
		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			assertSame(expected[i], actual.get(i));
		}
	}
}
