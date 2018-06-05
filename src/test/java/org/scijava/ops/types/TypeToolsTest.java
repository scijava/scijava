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

package org.scijava.ops.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Tests {@link TypeTools}.
 *
 * @author Gabe Selzer
 */
public class TypeToolsTest {

	/** Tests {@link TypeTools#satisfies(Type[], Type[])} for raw classes. */
	@Test
	public void testSatisfiesRaw() {
		// f(Number, Integer)
		final Type[] dest = { Number.class, Integer.class };

		// f(Double, Integer)
		// [OK] Double -> Number
		final Type[] srcOK = { Double.class, Integer.class };
		assertTrue(TypeTools.satisfies(srcOK, dest));

		// f(String, Integer)
		// [MISS] String is not assignable to Number
		final Type[] srcMiss = { String.class, Integer.class };
		assertFalse(TypeTools.satisfies(srcMiss, dest));

	}

	/** Tests {@link TypeTools#satisfies(Type[], Type[])} for single arguments. */
	@Test
	public <T extends Number, U extends BigInteger> void testSatisfiesSingle() {
		// <T extends Number> f(T)
		final Type t = new Nil<T>() {}.getType();
		final Type u = new Nil<U>() {}.getType();
		final Type[] tDest = { t };

		assertTrue(TypeTools.satisfies(new Type[] { Double.class }, tDest));
		assertTrue(TypeTools.satisfies(new Type[] { Number.class }, tDest));
		assertTrue(TypeTools.satisfies(new Type[] { t }, tDest));
		assertTrue(TypeTools.satisfies(new Type[] { u }, tDest));
		// String does not extend Number
		assertFalse(TypeTools.satisfies(new Type[] { String.class }, tDest));

		// -SINGLY RECURSIVE CALLS-

		// <T extends Number> f(List<T>)
		final Type listT = new Nil<List<T>>() {}.getType();
		final Type[] listTDest = { listT };
		// <U extends BigInteger> f(List<U>)
		final Type listU = new Nil<List<U>>() {}.getType();
		final Type[] listUDest = { listU };
		// f(List<Double>)
		final Type listDouble = new Nil<List<Double>>() {}.getType();
		final Type[] listDoubleDest = { listDouble };
		// f(List<? super Number>)
		final Type listSuperNumber = new Nil<List<? super Number>>() {}.getType();
		final Type[] listSuperNumberDest = { listSuperNumber };
		// f(List<? extends Number>)
		final Type listExtendsNumber = new Nil<List<? extends Number>>() {}
			.getType();
		final Type[] listExtendsNumberDest = { listExtendsNumber };

		assertTrue(TypeTools.satisfies(new Type[] { listT }, listTDest));
		assertTrue(TypeTools.satisfies(listUDest, listTDest));
		// not all Numbers are BigIntegers.
		assertFalse(TypeTools.satisfies(listTDest, listUDest));
		assertTrue(TypeTools.satisfies(listTDest, listExtendsNumberDest));
		assertTrue(TypeTools.satisfies(listUDest, listExtendsNumberDest));
		assertTrue(TypeTools.satisfies(listTDest, listSuperNumberDest));
		// BigInteger extends Number, not the other way around.
		assertFalse(TypeTools.satisfies(listUDest, listSuperNumberDest));
		assertTrue(TypeTools.satisfies(listDoubleDest, listExtendsNumberDest));
		// Double extends Number, not the other way around.
		assertFalse(TypeTools.satisfies(listDoubleDest, listSuperNumberDest));

		// -MULTIPLY RECURSIVE CALLS-

		final Type MapListTT = new Nil<Map<List<T>, T>>() {}.getType();
		final Type MapListTU = new Nil<Map<List<T>, U>>() {}.getType();
		final Type MapListUU = new Nil<Map<List<U>, U>>() {}.getType();
		final Type MapListTDouble = new Nil<Map<List<T>, Double>>() {}.getType();
		final Type MapListDoubleDouble = new Nil<Map<List<Double>, Double>>() {}
			.getType();
		final Type MapListDoubleString = new Nil<Map<List<Double>, String>>() {}
			.getType();
		final Type MapListDoubleNumber = new Nil<Map<List<Double>, Number>>() {}
			.getType();
		final Type MapListNumberDouble = new Nil<Map<List<Number>, Double>>() {}
			.getType();

		// T might not always extend BigInteger(U)
		assertFalse(TypeTools.satisfies(new Type[] { MapListTT }, new Type[] {
			MapListTU }));
		// T might not always be the same as U
		assertFalse(TypeTools.satisfies(new Type[] { MapListTU }, new Type[] {
			MapListTT }));
		assertTrue(TypeTools.satisfies(new Type[] { MapListUU }, new Type[] {
			MapListTT }));
		// T might not always extend BigInteger(U)
		assertFalse(TypeTools.satisfies(new Type[] { MapListTT }, new Type[] {
			MapListUU }));
		// T might not always be Double
		assertFalse(TypeTools.satisfies(new Type[] { MapListTDouble }, new Type[] {
			MapListTT }));
		// T does not extend String.
		assertFalse(TypeTools.satisfies(new Type[] { MapListDoubleString },
			new Type[] { MapListTT }));
		assertTrue(TypeTools.satisfies(new Type[] { MapListDoubleDouble },
			new Type[] { MapListTT }));
		// T is already fixed to Double (in a parameterized Map), cannot accomodate
		// Nubmer.
		assertFalse(TypeTools.satisfies(new Type[] { MapListNumberDouble },
			new Type[] { MapListTT }));
		// T is already fixed to Double (in a parameterized List) , cannot
		// accomodate Number
		assertFalse(TypeTools.satisfies(new Type[] { MapListDoubleNumber },
			new Type[] { MapListTT }));
	}

	@Test
	public <T extends Number, U extends String, V extends BigInteger> void
		testSatisfiesGenericArrays()
	{
		// generic arrays
		final Type arrayT = new Nil<T[]>() {}.getType();
		final Type arrayU = new Nil<U[]>() {}.getType();
		final Type arrayV = new Nil<V[]>() {}.getType();
		final Type arrayDouble = new Nil<Double[]>() {}.getType();

		assertTrue(TypeTools.satisfies(new Type[] { arrayDouble }, new Type[] {
			arrayT }));
		// Double does not extend String
		assertFalse(TypeTools.satisfies(new Type[] { arrayDouble }, new Type[] {
			arrayU }));
		assertTrue(TypeTools.satisfies(new Type[] { arrayT }, new Type[] {
			arrayT }));
		assertTrue(TypeTools.satisfies(new Type[] { arrayV }, new Type[] {
			arrayT }));
		// Number does not extend BigInteger
		assertFalse(TypeTools.satisfies(new Type[] { arrayT }, new Type[] {
			arrayV }));

		// generic multi-dimensional arrays
		final Type arrayT2D = new Nil<T[][]>() {}.getType();
		final Type arrayV2D = new Nil<V[][]>() {}.getType();
		final Type arrayDouble2D = new Nil<Double[][]>() {}.getType();

		assertTrue(TypeTools.satisfies(new Type[] { arrayDouble2D }, new Type[] {
			arrayT2D }));
		assertTrue(TypeTools.satisfies(new Type[] { arrayV2D }, new Type[] {
			arrayT2D }));
		// A 2D array does not satisfy a 1D array
		assertFalse(TypeTools.satisfies(new Type[] { arrayT2D }, new Type[] {
			arrayT }));
		// A 1D array does not satisfy a 2D array
		assertFalse(TypeTools.satisfies(new Type[] { arrayT }, new Type[] {
			arrayT2D }));

		// generic parameterized type arrays
		final Type arrayListT = new Nil<List<T>[]>() {}.getType();
		final Type arrayListDouble = new Nil<List<Double>[]>() {}.getType();
		final Type arrayListString = new Nil<List<String>[]>() {}.getType();

		assertTrue(TypeTools.satisfies(new Type[] { arrayListDouble }, new Type[] {
			arrayListT }));
		// String does not extend Number
		assertFalse(TypeTools.satisfies(new Type[] { arrayListString }, new Type[] {
			arrayListT }));
		// Number does not extend BigInteger
		assertFalse(TypeTools.satisfies(new Type[] { arrayListT }, new Type[] {
			arrayU }));
	}

	@Test
	public <S, T extends Thing<S>, U extends IntegerThing, V extends RecursiveThing<V>, W extends RecursiveThing<W> & Loop, X extends Thing<S> & Loop>
		void testSatisfiesTypeVariables()
	{
		final Type t = new Nil<T>() {}.getType();
		final Type u = new Nil<U>() {}.getType();
		final Type thingInt = new Nil<Thing<Integer>>() {}.getType();
		final Type numberThingInt = new Nil<NumberThing<Integer>>() {}.getType();
		final Type numberThingDouble = new Nil<NumberThing<Double>>() {}.getType();
		final Type strangeThingDouble = new Nil<StrangeThing<Double>>() {}
			.getType();
		final Type strangerThingString = new Nil<StrangerThing<String>>() {}
			.getType();
		final Type integerThing = new Nil<IntegerThing>() {}.getType();

		assertTrue(TypeTools.satisfies(new Type[] { thingInt, thingInt,
			numberThingInt, integerThing }, new Type[] { t, t, t, t }));
		assertTrue(TypeTools.satisfies(new Type[] { thingInt, numberThingInt,
			strangerThingString }, new Type[] { t, t, t }));
		assertTrue(TypeTools.satisfies(new Type[] { thingInt, numberThingInt,
			integerThing }, new Type[] { t, t, t }));
		assertTrue(TypeTools.satisfies(new Type[] { numberThingInt,
			strangeThingDouble }, new Type[] { t, t }));
		// S cannot accommodate a Double since S is already locked to Integer from
		// the first argument.
		assertFalse(TypeTools.satisfies(new Type[] { thingInt, numberThingInt,
			numberThingDouble }, new Type[] { t, t, t }));
		assertTrue(TypeTools.satisfies(new Type[] { u }, new Type[] { t }));

		// recursive Type Variables
		final Type circularThing = new Nil<CircularThing>() {}.getType();
		final Type loopingThing = new Nil<LoopingThing>() {}.getType();
		final Type recursiveThingCircular =
			new Nil<RecursiveThing<CircularThing>>()
			{}.getType();
		final Type v = new Nil<V>() {}.getType();
		final Type w = new Nil<W>() {}.getType();
		final Type x = new Nil<X>() {}.getType();

		assertTrue(TypeTools.satisfies(new Type[] { circularThing, circularThing,
			loopingThing }, new Type[] { t, t, t }));
		// V cannot accommodate LoopingThing since V is already locked to
		// CircularThing
		assertFalse(TypeTools.satisfies(new Type[] { circularThing, circularThing,
			loopingThing }, new Type[] { v, v, v }));
		// V cannot accommodate RecursiveThing since V is already locked to
		// CircularThing (V has to extend RecursiveThing<itself>, not
		// RecursiveThing<not itself>).
		assertFalse(TypeTools.satisfies(new Type[] { circularThing, circularThing,
			recursiveThingCircular }, new Type[] { v, v, v }));
		// V cannot accommodate RecursiveThing<CircularThing> since V must extend
		// RecursiveThing<V> (it cannot extend RecursiveThing<not V>)
		assertFalse(TypeTools.satisfies(new Type[] { recursiveThingCircular,
			recursiveThingCircular, recursiveThingCircular }, new Type[] { v, v,
				v }));
		assertTrue(TypeTools.satisfies(new Type[] { recursiveThingCircular,
			recursiveThingCircular, recursiveThingCircular }, new Type[] { t, t,
				t }));
		assertTrue(TypeTools.satisfies(new Type[] { circularThing, circularThing,
			circularThing }, new Type[] { w, w, w }));
		// W cannot accommodate LoopingThing since W is already
		// fixed to CircularThing
		assertFalse(TypeTools.satisfies(new Type[] { circularThing, loopingThing,
			circularThing }, new Type[] { w, w, w }));
		assertTrue(TypeTools.satisfies(new Type[] { circularThing, loopingThing,
			circularThing }, new Type[] { x, x, x }));
	}

	/**
	 * Tests {@link TypeTools#satisfies(Type[], Type[])} when the same type
	 * parameter appears across multiple destination types.
	 */
	@Test
	public <T> void testSatisfiesMatchingT() {
		// <T> f(List<T>, List<T>)
		final Type[] params = { //
			new Nil<List<T>>()
			{}.getType(), //
			new Nil<List<T>>()
			{}.getType(), //
		};

		// f(List<Integer>, List<Integer>)
		// [OK] T -> Integer
		final Type[] argsOK = { //
			new Nil<List<Integer>>()
			{}.getType(), //
			new Nil<List<Integer>>()
			{}.getType() };
		assertTrue(TypeTools.satisfies(argsOK, params));

		// f(List<String>, List<Number>)
		// [MISS] T cannot be both String and Number
		final Type[] argsMiss = { //
			new Nil<List<Double>>()
			{}.getType(), //
			new Nil<List<Number>>()
			{}.getType() //
		};
		assertFalse(TypeTools.satisfies(argsMiss, params));
	}

	// -- Helper classes --

	private static class Thing<T> {

		@SuppressWarnings("unused")
		private T thing;
	}

	private static class NumberThing<N extends Number> extends Thing<N> {
		// NB: No implementation needed.
	}

	private static class IntegerThing extends NumberThing<Integer> {
		// NB: No implementation needed.
	}

	private static class StrangeThing<S extends Number> extends Thing<Integer> {
		// NB: No implementation needed.
	}

	private static class StrangerThing<R extends String> extends
		StrangeThing<Double>
	{
		// NB: No implementation needed.
	}

	private static class RecursiveThing<T extends RecursiveThing<T>> extends
		Thing<Integer>
	{
		// NB: No implementation needed.
	}

	private static interface Loop {
		// NB: No implementation needed.
	}

	private static class CircularThing extends RecursiveThing<CircularThing>
		implements Loop
	{
		// NB: No implementation needed.
	}

	private static class LoopingThing extends RecursiveThing<LoopingThing>
		implements Loop
	{
		// NB: No implementation needed.
	}
}
