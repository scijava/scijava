/*
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

package org.scijava.ops.engine.matcher.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.scijava.testutil.ExampleTypes.CircularThing;
import static org.scijava.testutil.ExampleTypes.IntegerThing;
import static org.scijava.testutil.ExampleTypes.Loop;
import static org.scijava.testutil.ExampleTypes.LoopingThing;
import static org.scijava.testutil.ExampleTypes.NestedThing;
import static org.scijava.testutil.ExampleTypes.NumberThing;
import static org.scijava.testutil.ExampleTypes.RecursiveThing;
import static org.scijava.testutil.ExampleTypes.StrangeThing;
import static org.scijava.testutil.ExampleTypes.StrangerThing;
import static org.scijava.testutil.ExampleTypes.Thing;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigInteger;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;
import org.scijava.common3.Types;
import org.scijava.types.infer.GenericAssignability;

public class MatchingUtilsTest {

	private void assertAll(Class<?> from, boolean condition, Nil<?>... tos) {
		for (Nil<?> to : tos) {
			assertAll(from, condition, to.type());
		}
	}

	private void assertAll(Class<?> from, boolean condition, Type... tos) {
		for (Type to : tos) {
			if (to instanceof ParameterizedType) {
				assertEquals(condition,
					GenericAssignability.checkGenericAssignability(from,
						(ParameterizedType) to, false));
			}
			else {
				assertEquals(condition,
					Types.isAssignable(from, to, new HashMap<>()));
			}
		}
	}

	@Test
	public <E, N extends Number> void testGenericAssignabilityVarToVar() {
		abstract class Single<I> implements Supplier<I> {}
		abstract class SingleBounded<I extends Number> implements Supplier<I> {}
		Nil<Supplier<E>> y1 = new Nil<>() {};
		Nil<Supplier<N>> y2 = new Nil<>() {};
		Nil<Double> n1 = new Nil<>() {};

		assertAll(Single.class, true, y1, y2);
		assertAll(Single.class, false, n1);

		assertAll(SingleBounded.class, true, y2);
		assertAll(SingleBounded.class, false, y1, n1);
	}

	@Test
	public void testGenericAssignabilitySingleVar() {
		abstract class Single<I> implements Supplier<I> {}
		Nil<Supplier<Double>> y1 = new Nil<>() {};
		Nil<Supplier<Number>> y2 = new Nil<>() {};
		Nil<Double> n1 = new Nil<>() {};

		assertAll(Single.class, true, y1, y2);
		assertAll(Single.class, false, n1);
	}

	@Test
	public void testGenericAssignabilitySingleVarBounded() {
		abstract class SingleBounded<I extends Number> implements Supplier<I> {}

		Nil<Supplier<Double>> y1 = new Nil<>() {};
		Nil<Double> n1 = new Nil<>() {};
		Nil<String> n2 = new Nil<>() {};

		assertAll(SingleBounded.class, true, y1);
		assertAll(SingleBounded.class, false, n1, n2);
	}

	@Test
	public void testGenericAssignabilitySingleVarBoundedUsedNested() {
		abstract class SingleVarBoundedUsedNested<I extends Number> implements
			Supplier<List<I>>
		{}

		Nil<Double> n1 = new Nil<>() {};
		Nil<String> n2 = new Nil<>() {};
		Nil<Supplier<Double>> n3 = new Nil<>() {};
		Nil<Supplier<List<String>>> n4 = new Nil<>() {};
		Nil<Supplier<List<Double>>> y1 = new Nil<>() {};

		assertAll(SingleVarBoundedUsedNested.class, true, y1);
		assertAll(SingleVarBoundedUsedNested.class, false, n1, n2, n3, n4);
	}

	@Test
	public void testGenericAssignabilitySingleVarBoundedUsedNestedAndOther() {
		abstract class SingleVarBoundedUsedNestedAndOther<I extends Number>
			implements Function<List<I>, Double>
		{}
		abstract class SingleVarBoundedUsedNestedAndOtherNested<I extends Number>
			implements Function<List<I>, List<Double>>
		{}
		abstract class SingleVarBoundedUsedNestedAndOtherNestedWildcard<I extends Number>
			implements Function<List<I>, List<? extends Number>>
		{}

		Nil<Function<List<Double>, Double>> y1 = new Nil<>() {};
		Nil<Function<Double, Double>> n1 = new Nil<>() {};
		Nil<Function<List<String>, Double>> n2 = new Nil<>() {};
		Nil<Function<List<Double>, String>> n3 = new Nil<>() {};

		assertAll(SingleVarBoundedUsedNestedAndOther.class, true, y1);
		assertAll(SingleVarBoundedUsedNestedAndOther.class, false, n1, n2, n3);

		Nil<Function<List<Double>, List<Double>>> y2 = new Nil<>() {};
		Nil<Function<Double, List<Double>>> n4 = new Nil<>() {};
		Nil<Function<List<String>, List<Double>>> n5 = new Nil<>() {};
		Nil<Function<List<Double>, List<String>>> n6 = new Nil<>() {};

		assertAll(SingleVarBoundedUsedNestedAndOtherNested.class, true, y2);
		assertAll(SingleVarBoundedUsedNestedAndOtherNested.class, false, n4, n5, n6);

		Nil<Function<Double, List<Double>>> n7 = new Nil<>() {};
		Nil<Function<List<String>, List<Double>>> n8 = new Nil<>() {};
		Nil<Function<List<Double>, List<String>>> n9 = new Nil<>() {};
		Nil<Function<List<Double>, List<Double>>> n10 = new Nil<>() {};
		Nil<Function<List<Double>, List<Integer>>> n11 = new Nil<>() {};
		Nil<Function<List<Double>, List<? extends Number>>> y3 = new Nil<>() {};

		assertAll(SingleVarBoundedUsedNestedAndOtherNestedWildcard.class, false, n7,
			n8, n9, n10, n11);
		assertAll(SingleVarBoundedUsedNestedAndOtherNestedWildcard.class, true, y3);
	}

	@Test
	public void testGenericAssignabilitySingleVarNestedBoundNestedAndOther() {
		abstract class SingleVarBoundedNestedAndOther<I extends Iterable<String>>
			implements Function<I, Double>
		{}
		abstract class SingleVarBoundedNestedWildcardAndOther<I extends Iterable<? extends Number>>
			implements Function<I, List<Double>>
		{}

		Nil<Function<List<String>, Double>> y1 = new Nil<>() {};
		Nil<Function<Iterable<String>, Double>> y2 = new Nil<>() {};
		Nil<Function<Double, Double>> n1 = new Nil<>() {};
		Nil<Function<List<String>, String>> n2 = new Nil<>() {};
		Nil<Function<Iterable<Double>, Double>> n3 = new Nil<>() {};

		assertAll(SingleVarBoundedNestedAndOther.class, true, y1, y2);
		assertAll(SingleVarBoundedNestedAndOther.class, false, n1, n2, n3);

		Nil<Function<List<Integer>, List<Double>>> y3 = new Nil<>() {};
		Nil<Function<List<Double>, List<Double>>> y4 = new Nil<>() {};
		Nil<Function<Iterable<Double>, List<Double>>> y5 = new Nil<>() {};
		Nil<Function<List<Integer>, Double>> n4 = new Nil<>() {};
		Nil<Function<List<Double>, List<Integer>>> n5 = new Nil<>() {};
		Nil<Function<Iterable<Double>, Iterable<Double>>> n6 = new Nil<>() {};
		Nil<Function<Integer, List<Double>>> n7 = new Nil<>() {};
		Nil<Function<List<String>, List<Double>>> n8 = new Nil<>() {};

		assertAll(SingleVarBoundedNestedWildcardAndOther.class, true, y3, y4, y5);
		assertAll(SingleVarBoundedNestedWildcardAndOther.class, false, n4, n5, n6,
			n7, n8);
	}

	@Test
	public void testGenericAssignabilitySingleVarMultipleOccurrence() {
		abstract class SingleVarBoundedNestedMultipleOccurrence<I extends Iterable<String>>
			implements Function<I, I>
		{}

		abstract class SingleVarBoundedNestedWildcardMultipleOccurrence<I extends Iterable<? extends Number>>
			implements Function<I, I>
		{}

		abstract class SingleVarBoundedNestedWildcardMultipleOccurrenceUsedNested<I extends Iterable<? extends Number>>
			implements Function<I, List<I>>
		{}

		Nil<Function<List<String>, List<String>>> y1 = new Nil<>() {};
		Nil<Function<Iterable<String>, Iterable<String>>> y2 = new Nil<>() {};
		Nil<Function<List<String>, List<Integer>>> n1 = new Nil<>() {};
		Nil<Function<List<String>, Double>> n2 = new Nil<>() {};

		assertAll(SingleVarBoundedNestedMultipleOccurrence.class, true, y1, y2);
		assertAll(SingleVarBoundedNestedMultipleOccurrence.class, false, n1, n2);

		Nil<Function<List<Double>, List<Double>>> y3 = new Nil<>() {};
		Nil<Function<Iterable<Double>, Iterable<Double>>> y4 = new Nil<>() {};
		Nil<Function<Iterable<Double>, Iterable<Integer>>> n3 = new Nil<>() {};
		Nil<Function<List<String>, Integer>> n4 = new Nil<>() {};

		assertAll(SingleVarBoundedNestedWildcardMultipleOccurrence.class, true, y3, y4);
		assertAll(SingleVarBoundedNestedWildcardMultipleOccurrence.class, false, n3, n4);

		Nil<Function<List<Double>, Iterable<List<Double>>>> n5 = new Nil<>() {};
		Nil<Function<Iterable<Double>, List<Iterable<Double>>>> y5 = new Nil<>() {};

		assertAll(SingleVarBoundedNestedWildcardMultipleOccurrenceUsedNested.class,
			true, y5);
		assertAll(SingleVarBoundedNestedWildcardMultipleOccurrenceUsedNested.class,
			false, n5);

		abstract class SingleVarMultipleOccurrenceUsedNested<I> implements
			Function<I, List<I>>
		{}

		Nil<Function<Integer, List<Number>>> n6 = new Nil<>() {};
		assertAll(SingleVarMultipleOccurrenceUsedNested.class, false, n6);
	}

	@Test
	public void testGenericAssignabilityDoubleVar() {
		abstract class DoubleVar<I, B> implements Function<I, B> {}

		abstract class DoubleVarBounded<I extends List<String>, B extends Number>
			implements Function<I, B>
		{}

		Nil<Function<List<String>, List<String>>> y1 = new Nil<>() {};
		Nil<Function<Iterable<String>, Iterable<String>>> y2 = new Nil<>() {};
		Nil<Function<List<String>, List<Integer>>> y3 = new Nil<>() {};
		Nil<Function<List<String>, Double>> y4 = new Nil<>() {};

		assertAll(DoubleVar.class, true, y1, y2, y3, y4);

		Nil<Function<List<String>, Double>> y5 = new Nil<>() {};
		Nil<Function<List<String>, Float>> y6 = new Nil<>() {};
		Nil<Function<Iterable<String>, Double>> n1 = new Nil<>() {};
		Nil<Function<List<Double>, Integer>> n2 = new Nil<>() {};
		Nil<Function<List<String>, String>> n3 = new Nil<>() {};

		assertAll(DoubleVarBounded.class, true, y5, y6);
		assertAll(DoubleVarBounded.class, false, n1, n2, n3);
	}

	@Test
	public void testGenericAssignabilityDoubleVarDepending() {
		abstract class BExtendsI<I extends Iterable<? extends Number>, B extends I>
			implements Function<I, B>
		{}

		abstract class IBoundedByN<N extends Number, I extends Iterable<N>>
			implements BiFunction<I, I, N>
		{}

		Nil<Function<List<Integer>, List<Integer>>> y1 = new Nil<>() {};
		Nil<Function<Iterable<Integer>, List<Integer>>> y2 = new Nil<>() {};
		Nil<Function<Iterable<Integer>, Iterable<Integer>>> y3 = new Nil<>() {};
		Nil<Function<List<String>, List<Integer>>> n1 = new Nil<>() {};
		Nil<Function<List<String>, Double>> n2 = new Nil<>() {};
		Nil<Function<List<Integer>, List<Double>>> n3 = new Nil<>() {};
		Nil<Function<Integer, List<Integer>>> n4 = new Nil<>() {};
		Nil<Function<Iterable<Integer>, Integer>> n5 = new Nil<>() {};

		assertAll(BExtendsI.class, true, y1, y2, y3);
		assertAll(BExtendsI.class, false, n1, n2, n3, n4, n5);

		Nil<BiFunction<List<Integer>, List<Integer>, Integer>> y4 = new Nil<>() {};
		Nil<BiFunction<Iterable<Integer>, Iterable<Integer>, Integer>> y5 = new Nil<>() {};
		Nil<BiFunction<List<Integer>, List<Integer>, Double>> n6 = new Nil<>() {};
		Nil<BiFunction<Iterable<Double>, Iterable<Integer>, Integer>> n7 = new Nil<>() {};
		Nil<BiFunction<Iterable<Integer>, List<Integer>, Integer>> n8 = new Nil<>() {};
		Nil<BiFunction<Iterable<String>, List<String>, String>> n9 = new Nil<>() {};

		assertAll(IBoundedByN.class, true, y4, y5);
		assertAll(IBoundedByN.class, false, n6, n7, n8, n9);
	}

	@Test
	public void testGenericAssignabilityDoubleVarDependingImplicitlyBounded() {
		abstract class IBoundedByNImplicitly<N extends Number, I extends Iterable<N>>
			implements BiFunction<I, I, List<String>>
		{}

		Nil<BiFunction<Iterable<Double>, Iterable<Double>, List<String>>> y1 = new Nil<>() {};

		assertAll(IBoundedByNImplicitly.class, true, y1);
	}

	@Test
	public void testGenericAssignabilityDoubleVarBoundedAndWildcard() {
		abstract class DoubleVarBoundedAndWildcard<M extends Number, I extends Iterable<? extends Number>>
			implements BiFunction<I, I, Iterable<M>>
		{}

		Nil<BiFunction<Iterable<Double>, Double, Iterable<Double>>> n1 = new Nil<>() {};
		Nil<BiFunction<Double, Iterable<Double>, Iterable<Double>>> n2 = new Nil<>() {};
		Nil<BiFunction<List<Float>, List<Number>, Iterable<Double>>> n3 = new Nil<>() {};
		Nil<BiFunction<Iterable<Double>, List<Double>, Iterable<Double>>> n4 = new Nil<>() {};
		Nil<BiFunction<List<Double>, List<Double>, List<Double>>> n5 = new Nil<>() {};
		Nil<BiFunction<List<Integer>, List<Double>, Iterable<Double>>> n6 = new Nil<>() {};
		Nil<BiFunction<List<Double>, List<Double>, Iterable<Double>>> y1 = new Nil<>() {};
		Nil<BiFunction<Iterable<Double>, Iterable<Double>, Iterable<Double>>> y2 = new Nil<>() {};

		assertAll(DoubleVarBoundedAndWildcard.class, true, y1, y2);
		assertAll(DoubleVarBoundedAndWildcard.class, false, n1, n2, n3, n4, n5, n6);
	}

	@Test
	public void testGenericAssignabilityWildcards() {
		abstract class Wildcards implements
			Function<List<? extends Number>, List<? extends Number>>
		{}

		Nil<Function<List<? extends Number>, List<? extends Number>>> y1 = new Nil<>() {};
		Nil<Function<Iterable<Integer>, Iterable<Double>>> n1 = new Nil<>() {};
		Nil<Function<List<Double>, List<Integer>>> n2 = new Nil<>() {};
		Nil<Function<List<Double>, List<Double>>> n3 = new Nil<>() {};
		Nil<Function<Iterable<Double>, Iterable<Double>>> n4 = new Nil<>() {};

		assertAll(Wildcards.class, true, y1);
		assertAll(Wildcards.class, false, n1, n2, n3, n4);
	}

	@Test
	public void testGenericAssignabilityWildcardExtendingTypeVar() {
		abstract class StrangeConsumer<T extends Number> implements
			BiConsumer<List<? extends T>, T>
		{}

		Nil<BiConsumer<List<? extends Number>, Number>> y1 = new Nil<>() {};
		Nil<BiConsumer<List<? extends Integer>, Integer>> y2 = new Nil<>() {};
		Nil<BiConsumer<List<? extends Number>, ? extends Number>> y3 = new Nil<>() {};

		Nil<BiConsumer<List<? extends Integer>, Double>> n1 = new Nil<>() {};

		assertAll(StrangeConsumer.class, true, y1, y2, y3);
		assertAll(StrangeConsumer.class, false, n1);
	}

	/**
	 * Suppose we have a
	 *
	 * <pre>{@code
	 * class Foo<I extends Number> implements Function<I[], Double>
	 * }</pre>
	 *
	 * It is legal to write
	 *
	 * <pre>{@code
	 *
	 * Function<Double[], Double[]> fooFunc = new Foo<>();
	 * }</pre>
	 *
	 * If we instead have a
	 *
	 * <pre>{@code
	 * class Bar implements Function<O[], Double>
	 * }</pre>
	 *
	 * where {@code O extends Number}, is <strong>not</strong> legal to write
	 *
	 * <pre>{@code
	 *
	 * Function<Double[], Double[]> barFunc = new Bar<>();
	 * }</pre>
	 *
	 * @param <O>
	 */
	@Test
	public <O extends Number> void testGenericArrayFunction() {
		class Foo<I extends Number> implements Function<I[], Double> {

			@Override
			public Double apply(I[] t) {
				return null;
			}
		}

		class Bar implements Function<O[], Double> {

			@Override
			public Double apply(O[] t) {
				return null;
			}
		}
		Nil<Function<Double[], Double>> doubleFunction = new Nil<>() {};
		assertAll(Foo.class, true, doubleFunction);
		assertAll(Bar.class, false, doubleFunction);
	}

	@Test
	public void testGenericArrayToWildcardWithinParameterizedType() {
		abstract class Foo<T extends Number> implements List<T[]> {}
		final Nil<List<? extends Double[]>> upperType = new Nil<>() {};
		final Nil<List<? super Double[]>> lowerType = new Nil<>() {};

		// Since it is legal to write
		// List<? extends Double[]> list = new Foo<>() {...};
		// assertAll must return true
		assertAll(Foo.class, true, upperType, lowerType);
	}

	@Test
	public <T extends Number> void testSuperWildcardToSuperWildcard() {
		final Nil<List<? super T>> listT = new Nil<>() {};
		final Nil<List<? super Number>> listWildcard = new Nil<>() {};

		// unfortunately we cannot use assertAll since it is impossible to create a
		// Class implementing List<? super T>
		boolean success = GenericAssignability.checkGenericAssignability(
			listT.type(), (ParameterizedType) listWildcard.type(), false);
		Assertions.assertTrue(success);
	}

	@Test
	public void testNonReifiableFunction() {
		Function<Double[], Double[]> fooFunc = (in) -> in;
		final Nil<Function<Double[], Double[]>> doubleFunc = new Nil<>() {};
		final Nil<Function<Integer[], Integer[]>> integerFunc = new Nil<>() {};

		boolean successDouble = GenericAssignability.checkGenericAssignability(
			fooFunc.getClass(), (ParameterizedType) doubleFunc.type(), false);
		Assertions.assertTrue(successDouble);
		boolean successInteger = GenericAssignability.checkGenericAssignability(
			fooFunc.getClass(), (ParameterizedType) integerFunc.type(), false);
		Assertions.assertTrue(successInteger);
	}

	@Test
	public void testIsAssignableNullToNull() {
		assertThrows(NullPointerException.class, () -> GenericAssignability
			.checkGenericAssignability(null, null, false));
	}

	@Test
	public void testIsAssignableClassToNull() {
		assertThrows(NullPointerException.class, () -> GenericAssignability
			.checkGenericAssignability(Object.class, null, false));
	}

	@Test
	public <T extends Number> void testIsAssignableT() {
		final Nil<T> t = new Nil<>() {};
		final Nil<List<T>> listT = new Nil<>() {};
		final Nil<List<Number>> listNumber = new Nil<>() {};
		final Nil<List<Integer>> listInteger = new Nil<>() {};
		final Nil<List<? extends Number>> listExtendsNumber = new Nil<>() {};

		assertAll(List.class, true, listT, listNumber, listInteger, listExtendsNumber);
		assertAll(List.class, false, t);
	}

	@Test
	public <T extends Number> void testIsAssignableOutputToObject() {
		final Type fooSource = new Nil<Function<T, List<T>>>() {}.type();
		final Type fooFunc = new Nil<Function<Double, Object>>() {}.type();

		Assertions.assertFalse(GenericAssignability.checkGenericAssignability(
			fooSource, (ParameterizedType) fooFunc, false));
		Assertions.assertTrue(GenericAssignability.checkGenericAssignability(
			fooSource, (ParameterizedType) fooFunc, true));
	}

	/**
	 * Tests {@link MatchingUtils#checkGenericOutputsAssignability(Type[], Type[], HashMap)}.
	 */
	@Test
	public <N> void testOutputAssignability() {
		Nil<N> n = new Nil<N>() {};
		Nil<List<N>> ln = new Nil<List<N>>() {};
		Nil<List<? extends Number>> lWildNum = new Nil<List<? extends Number>>() {};
		Nil<List<Number>> lNum = new Nil<List<Number>>() {};
		Nil<List<?>> lwild = new Nil<List<?>>() {};

		HashMap<TypeVariable<?>, MatchingUtils.TypeVarInfo> typeBounds = new HashMap<>();
		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { Integer.class },
			new Type[] { n.type() },
			typeBounds
		));
		Type[] toOuts = new Type[] { lWildNum.type() };
		Type[] fromOuts = new Type[] { ln.type() };
		assertEquals(-1, MatchingUtils.checkGenericOutputsAssignability(fromOuts, toOuts, typeBounds));

		toOuts = new Type[] { lNum.type() };
		assertEquals(-1, MatchingUtils.checkGenericOutputsAssignability(fromOuts, toOuts, typeBounds));

		toOuts = new Type[] { lwild.type() };
		assertEquals(-1, MatchingUtils.checkGenericOutputsAssignability(fromOuts, toOuts, typeBounds));

		// TODO: Investigate how to finish implementing
		//  checkGenericOutputAssignability properly,
		//  such that the following test can pass.
//
//		typeBounds = new HashMap<>();
//		assertEquals(-1, MatchingUtils.isApplicable(
//			new Type[] { String.class },
//			new Type[] { n.type() },
//			typeBounds
//		));
//		toOuts = new Type[] { lWildNum.type() };
//		fromOuts = new Type[] { ln.type() };
//		assertNotEquals(-1, MatchingUtils.checkGenericOutputsAssignability(
//			fromOuts, toOuts, typeBounds));
	}

	/** Tests {@link MatchingUtils#isApplicable(Type[], Type[])} for raw classes. */
	@Test
	public void testIsApplicableRaw() {
		// f(Number, Integer)
		final Type[] dest = { Number.class, Integer.class };

		// f(Double, Integer)
		// [OK] Double -> Number
		final Type[] srcOK = { Double.class, Integer.class };
		assertEquals(-1, MatchingUtils.isApplicable(srcOK, dest));

		// f(String, Integer)
		// [MISS] String is not assignable to Number
		final Type[] srcMiss = { String.class, Integer.class };
		assertNotEquals(-1, MatchingUtils.isApplicable(srcMiss, dest));

	}

	/** Tests {@link MatchingUtils#isApplicable(Type[], Type[])} for single arguments. */
	@Test
	public <T extends Number, U extends BigInteger> void testIsApplicableSingle() {
		// <T extends Number> f(T)
		final Type t = new Nil<T>() {}.type();
		final Type u = new Nil<U>() {}.type();
		final Type[] tDest = { t };

		assertEquals(-1, MatchingUtils.isApplicable(new Type[] { Double.class }, tDest));
		assertEquals(-1, MatchingUtils.isApplicable(new Type[] { Number.class }, tDest));
		assertEquals(-1, MatchingUtils.isApplicable(new Type[] { t }, tDest));
		assertEquals(-1, MatchingUtils.isApplicable(new Type[] { u }, tDest));
		// String does not extend Number
		assertNotEquals(-1, MatchingUtils.isApplicable(new Type[] { String.class }, tDest));

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
		final Type listExtendsNumber = new Nil<List<? extends Number>>() {}.type();
		final Type[] listExtendsNumberDest = { listExtendsNumber };

		assertEquals(-1, MatchingUtils.isApplicable(new Type[] { listT }, listTDest));
		assertEquals(-1, MatchingUtils.isApplicable(listUDest, listTDest));
		// not all Numbers are BigIntegers.
		assertNotEquals(-1, MatchingUtils.isApplicable(listTDest, listUDest));
		assertEquals(-1, MatchingUtils.isApplicable(listTDest, listExtendsNumberDest));
		assertEquals(-1, MatchingUtils.isApplicable(listUDest, listExtendsNumberDest));
		assertEquals(-1, MatchingUtils.isApplicable(listTDest, listSuperNumberDest));
		// BigInteger extends Number, not the other way around.
		assertNotEquals(-1, MatchingUtils.isApplicable(listUDest, listSuperNumberDest));
		assertEquals(-1, MatchingUtils.isApplicable(listDoubleDest, listExtendsNumberDest));
		// Double extends Number, not the other way around.
		assertNotEquals(-1, MatchingUtils.isApplicable(listDoubleDest, listSuperNumberDest));

		// -MULTIPLY RECURSIVE CALLS-

		final Type MapListTT = new Nil<Map<List<T>, T>>() {}.type();
		final Type MapListTU = new Nil<Map<List<T>, U>>() {}.type();
		final Type MapListUU = new Nil<Map<List<U>, U>>() {}.type();
		final Type MapListTDouble = new Nil<Map<List<T>, Double>>() {}.type();
		final Type MapListDoubleDouble = new Nil<Map<List<Double>, Double>>() {}.type();
		final Type MapListDoubleString = new Nil<Map<List<Double>, String>>() {}.type();
		final Type MapListDoubleNumber = new Nil<Map<List<Double>, Number>>() {}.type();
		final Type MapListNumberDouble = new Nil<Map<List<Number>, Double>>() {}.type();

		// T might not always extend BigInteger(U)
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { MapListTT },
			new Type[] { MapListTU }
		));
		// T might not always be the same as U
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { MapListTU },
			new Type[] { MapListTT }
		));
		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { MapListUU },
			new Type[] { MapListTT }
		));
		// T might not always extend BigInteger(U)
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { MapListTT },
			new Type[] { MapListUU }
		));
		// T might not always be Double
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { MapListTDouble },
			new Type[] { MapListTT }
		));
		// T does not extend String.
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { MapListDoubleString },
			new Type[] { MapListTT }
		));
		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { MapListDoubleDouble },
			new Type[] { MapListTT }
		));
		// T is already fixed to Double (in a parameterized Map), cannot accommodate Number.
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { MapListNumberDouble },
			new Type[] { MapListTT }
		));
		// T is already fixed to Double (in a parameterized List) , cannot
		// accommodate Number
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { MapListDoubleNumber },
			new Type[] { MapListTT }
		));
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

		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { arrayDouble },
			new Type[] { arrayT }
		));
		// Double does not extend String
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { arrayDouble },
			new Type[] { arrayU }
		));
		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { arrayT },
			new Type[] { arrayT }
		));
		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { arrayV },
			new Type[] { arrayT }
		));
		// Number does not extend BigInteger
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { arrayT },
			new Type[] { arrayV }
		));

		// generic multi-dimensional arrays
		final Type arrayT2D = new Nil<T[][]>() {}.type();
		final Type arrayV2D = new Nil<V[][]>() {}.type();
		final Type arrayDouble2D = new Nil<Double[][]>() {}.type();

		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { arrayDouble2D },
			new Type[] { arrayT2D }
		));
		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { arrayV2D },
			new Type[] { arrayT2D }
		));
		// A 2D array does not satisfy a 1D array
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { arrayT2D },
			new Type[] { arrayT }
		));
		// A 1D array does not satisfy a 2D array
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { arrayT },
			new Type[] { arrayT2D }
		));

		// generic parameterized type arrays
		final Type arrayListT = new Nil<List<T>[]>() {}.type();
		final Type arrayListDouble = new Nil<List<Double>[]>() {}.type();
		final Type arrayListString = new Nil<List<String>[]>() {}.type();

		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { arrayListDouble },
			new Type[] { arrayListT }
		));
		// String does not extend Number
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { arrayListString },
			new Type[] { arrayListT }
		));
		// Number does not extend BigInteger
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { arrayListT },
			new Type[] { arrayU }
		));
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

		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { thingInt, thingInt, numberThingInt, integerThing },
			new Type[] { t, t, t, t }
		));
		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { thingInt, numberThingInt, strangerThingString },
			new Type[] { t, t, t }
		));
		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { thingInt, numberThingInt, integerThing },
			new Type[] { t, t, t }
		));
		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { numberThingInt, strangeThingDouble },
			new Type[] { t, t }
		));
		// S cannot accommodate a Double since S is already locked to Integer from
		// the first argument.
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { thingInt, numberThingInt, numberThingDouble },
			new Type[] { t, t, t }
		));
		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { u },
			new Type[] { t }
		));

		// recursive Type Variables
		final Type circularThing = new Nil<CircularThing>() {}.type();
		final Type loopingThing = new Nil<LoopingThing>() {}.type();
		final Type recursiveThingCircular = new Nil<RecursiveThing<CircularThing>>() {}.type();
		final Type v = new Nil<V>() {}.type();
		final Type w = new Nil<W>() {}.type();
		final Type x = new Nil<X>() {}.type();

		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { circularThing, circularThing, loopingThing },
			new Type[] { t, t, t }
		));
		// V cannot accommodate LoopingThing since V is already locked to
		// CircularThing
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { circularThing, circularThing, loopingThing },
			new Type[] { v, v, v }
		));
		// V cannot accommodate RecursiveThing since V is already locked to
		// CircularThing (V has to extend RecursiveThing<itself>, not
		// RecursiveThing<not itself>).
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { circularThing, circularThing, recursiveThingCircular },
			new Type[] { v, v, v }
		));
		// V cannot accommodate RecursiveThing<CircularThing> since V must extend
		// RecursiveThing<V> (it cannot extend RecursiveThing<not V>)
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { recursiveThingCircular, recursiveThingCircular, recursiveThingCircular },
			new Type[] { v, v, v }
		));
		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { recursiveThingCircular, recursiveThingCircular, recursiveThingCircular },
			new Type[] { t, t, t }
		));
		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { circularThing, circularThing, circularThing },
			new Type[] { w, w, w }
		));
		// W cannot accommodate LoopingThing since W is already
		// fixed to CircularThing
		assertNotEquals(-1, MatchingUtils.isApplicable(
			new Type[] { circularThing, loopingThing, circularThing },
			new Type[] { w, w, w }
		));
		assertEquals(-1, MatchingUtils.isApplicable(
			new Type[] { circularThing, loopingThing, circularThing },
			new Type[] { x, x, x }
		));
	}

	/**
	 * Tests {@link MatchingUtils#isApplicable(Type[], Type[])} when the same type
	 * parameter appears across multiple destination types.
	 */
	@Test
	public <T> void testIsApplicableMatchingT() {
		// <T> f(List<T>, List<T>)
		final Type[] params = { //
			new Nil<List<T>>() {}.type(), //
			new Nil<List<T>>() {}.type(), //
		};

		// f(List<Integer>, List<Integer>)
		// [OK] T -> Integer
		final Type[] argsOK = { //
			new Nil<List<Integer>>() {}.type(), //
			new Nil<List<Integer>>() {}.type()
		};
		assertEquals(-1, MatchingUtils.isApplicable(argsOK, params));

		// f(List<String>, List<Number>)
		// [MISS] T cannot be both String and Number
		final Type[] argsMiss = { //
			new Nil<List<Double>>() {}.type(), //
			new Nil<List<Number>>() {}.type() //
		};
		assertNotEquals(-1, MatchingUtils.isApplicable(argsMiss, params));
	}

	@Test
	public <N, C> void testIsApplicableWildcards() {
		var n = new Nil<List<N>>() {};
		var c = new Nil<List<C>>() {};
		var nWildcard = new Nil<List<? extends Number>>() {};

		Type[] params = { n.type() };
		Type[] argsOk = { nWildcard.type() };
		assertEquals(-1, MatchingUtils.isApplicable(argsOk, params));

		params = new Type[] { n.type(), c.type() };
		argsOk = new Type[] { nWildcard.type(), nWildcard.type() };
		assertEquals(-1, MatchingUtils.isApplicable(argsOk, params));

		params = new Type[] { n.type(), n.type() };
		Type[] argsNotOk = { nWildcard.type(), nWildcard.type() };
		assertNotEquals(-1, MatchingUtils.isApplicable(argsNotOk, params));
	}

	@Test
	public <N> void testIsApplicableWildcardsInParameterizedType() {
		var n = new Nil<N>() {};
		var ln = new Nil<List<N>>() {};
		var lw = new Nil<List<? extends Number>>() {};

		Type[] params = { n.type(), ln.type() };
		Type[] argsNotOk = { Integer.class, lw.type() };
		assertNotEquals(-1, MatchingUtils.isApplicable(argsNotOk, params));

		params = new Type[] { ln.type(), n.type() };
		argsNotOk = new Type[] { lw.type(), Integer.class };
		assertNotEquals(-1, MatchingUtils.isApplicable(argsNotOk, params));
	}

	@Test
	public <N extends Number, C extends List<String>> void testIsApplicableBoundedWildcards() {
		var n = new Nil<List<N>>() {};
		var c = new Nil<List<C>>() {};
		var nNumberWildcard = new Nil<List<? extends Number>>() {};
		var nListWildcard = new Nil<List<? extends List<String>>>() {};

		Type[] params = { n.type() };
		Type[] argsOk = { nNumberWildcard.type() };
		assertEquals(-1, MatchingUtils.isApplicable(argsOk, params));

		params = new Type[] { n.type(), c.type() };
		argsOk = new Type[] { nNumberWildcard.type(), nListWildcard.type() };
		assertEquals(-1, MatchingUtils.isApplicable(argsOk, params));

		params = new Type[] { n.type(), c.type() };
		Type[] argsNotOk = { nNumberWildcard.type(), nNumberWildcard.type() };
		assertNotEquals(-1, MatchingUtils.isApplicable(argsNotOk, params));

		params = new Type[] { n.type(), n.type() };
		argsNotOk = new Type[] { nNumberWildcard.type(), nNumberWildcard.type() };
		assertNotEquals(-1, MatchingUtils.isApplicable(argsNotOk, params));
	}

	/**
	 * Tests {@link MatchingUtils#isApplicable(Type[], Type[])} when the given type is
	 * indirectly parameterized by implementing an parameterized interface.
	 */
	@Test
	public <I1, I2> void testIsApplicableIndirectTypeVariables() {

		abstract class NestedThingImplOK1 implements NestedThing<Double, Double> {}

		final Type[] param = new Type[] { new Nil<Function<I1, I2>>() {}.type() };
		Type[] argOK = new Type[] { NestedThingImplOK1.class };
		assertEquals(-1, MatchingUtils.isApplicable(argOK, param));
	}

	/**
	 * Tests {@link MatchingUtils#isApplicable(Type[], Type[])} when unbounded type
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

		final Type[] param = new Type[] { new Nil<Function<I1, I2>>() {}.type() };
		Type[] argOK = new Type[] { NestedThingImplOK1.class };
		assertEquals(-1, MatchingUtils.isApplicable(argOK, param));

		argOK = new Type[] { NestedThingImplOK2.class };
		assertEquals(-1, MatchingUtils.isApplicable(argOK, param));

		argOK = new Type[] { NestedThingImplOK3.class };
		assertEquals(-1, MatchingUtils.isApplicable(argOK, param));
	}

}
