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

package org.scijava.ops.engine.matcher;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.ops.engine.matcher.impl.MatchingUtils;
import org.scijava.types.Nil;
import org.scijava.types.Types;
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
				assertTrue(GenericAssignability.checkGenericAssignability(from,
					(ParameterizedType) to, false) == condition);
			}
			else {
				assertTrue(Types.isAssignable(from, to,
					new HashMap<TypeVariable<?>, Type>()) == condition);
			}
		}
	}

	@Test
	public <E, N extends Number> void genericAssignabilityVarToVar() {
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
	public void genericAssignabilitySingleVar() {
		abstract class Single<I> implements Supplier<I> {}
		Nil<Supplier<Double>> y1 = new Nil<>() {};
		Nil<Supplier<Number>> y2 = new Nil<>() {};
		Nil<Double> n1 = new Nil<>() {};

		assertAll(Single.class, true, y1, y2);
		assertAll(Single.class, false, n1);
	}

	@Test
	public void genericAssignabilitySingleVarBounded() {
		abstract class SingleBounded<I extends Number> implements Supplier<I> {}

		Nil<Supplier<Double>> y1 = new Nil<>() {};
		Nil<Double> n1 = new Nil<>() {};
		Nil<String> n2 = new Nil<>() {};

		assertAll(SingleBounded.class, true, y1);
		assertAll(SingleBounded.class, false, n1, n2);
	}

	@Test
	public void genericAssignabilitySingleVarBoundedUsedNested() {
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
	public void genericAssignabilitySingleVarBoundedUsedNestedAndOther() {
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
		assertAll(SingleVarBoundedUsedNestedAndOtherNested.class, false, n4, n5,
			n6);

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
	public void genericAssignabilitySingleVarNestedBoundNestedAndOther() {
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
	public void genericAssignabilitySingleVarMultipleOccurrence() {
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

		assertAll(SingleVarBoundedNestedWildcardMultipleOccurrence.class, true, y3,
			y4);
		assertAll(SingleVarBoundedNestedWildcardMultipleOccurrence.class, false, n3,
			n4);

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
	public void genericAssignabilityDoubleVar() {
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
	public void genericAssignabilityDoubleVarDepending() {
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
		Nil<BiFunction<Iterable<Integer>, Iterable<Integer>, Integer>> y5 =
			new Nil<>()
			{};
		Nil<BiFunction<List<Integer>, List<Integer>, Double>> n6 = new Nil<>() {};
		Nil<BiFunction<Iterable<Double>, Iterable<Integer>, Integer>> n7 =
			new Nil<>()
			{};
		Nil<BiFunction<Iterable<Integer>, List<Integer>, Integer>> n8 =
			new Nil<>()
			{};
		Nil<BiFunction<Iterable<String>, List<String>, String>> n9 = new Nil<>() {};

		assertAll(IBoundedByN.class, true, y4, y5);
		assertAll(IBoundedByN.class, false, n6, n7, n8, n9);
	}

	@Test
	public void genericAssignabilityDoubleVarDependingImplicitlyBounded() {
		abstract class IBoundedByNImplicitly<N extends Number, I extends Iterable<N>>
			implements BiFunction<I, I, List<String>>
		{}

		Nil<BiFunction<Iterable<Double>, Iterable<Double>, List<String>>> y1 =
			new Nil<>()
			{};

		assertAll(IBoundedByNImplicitly.class, true, y1);
	}

	@Test
	public void genericAssignabilityDoubleVarBoundedAndWildcard() {
		abstract class DoubleVarBoundedAndWildcard<M extends Number, I extends Iterable<? extends Number>>
			implements BiFunction<I, I, Iterable<M>>
		{}

		Nil<BiFunction<Iterable<Double>, Double, Iterable<Double>>> n1 =
			new Nil<>()
			{};
		Nil<BiFunction<Double, Iterable<Double>, Iterable<Double>>> n2 =
			new Nil<>()
			{};
		Nil<BiFunction<List<Float>, List<Number>, Iterable<Double>>> n3 =
			new Nil<>()
			{};
		Nil<BiFunction<Iterable<Double>, List<Double>, Iterable<Double>>> n4 =
			new Nil<>()
			{};
		Nil<BiFunction<List<Double>, List<Double>, List<Double>>> n5 =
			new Nil<>()
			{};
		Nil<BiFunction<List<Integer>, List<Double>, Iterable<Double>>> n6 =
			new Nil<>()
			{};
		Nil<BiFunction<List<Double>, List<Double>, Iterable<Double>>> y1 =
			new Nil<>()
			{};
		Nil<BiFunction<Iterable<Double>, Iterable<Double>, Iterable<Double>>> y2 =
			new Nil<>()
			{};

		assertAll(DoubleVarBoundedAndWildcard.class, true, y1, y2);
		assertAll(DoubleVarBoundedAndWildcard.class, false, n1, n2, n3, n4, n5, n6);
	}

	@Test
	public void genericAssignabilityWildcards() {
		abstract class Wildcards implements
			Function<List<? extends Number>, List<? extends Number>>
		{}

		Nil<Function<List<? extends Number>, List<? extends Number>>> y1 =
			new Nil<>()
			{};
		Nil<Function<Iterable<Integer>, Iterable<Double>>> n1 = new Nil<>() {};
		Nil<Function<List<Double>, List<Integer>>> n2 = new Nil<>() {};
		Nil<Function<List<Double>, List<Double>>> n3 = new Nil<>() {};
		Nil<Function<Iterable<Double>, Iterable<Double>>> n4 = new Nil<>() {};

		assertAll(Wildcards.class, true, y1);
		assertAll(Wildcards.class, false, n1, n2, n3, n4);
	}

	@Test
	public void genericAssignabilityWildcardExtendingTypeVar() {
		abstract class StrangeConsumer<T extends Number> implements
			BiConsumer<List<? extends T>, T>
		{}

		Nil<BiConsumer<List<? extends Number>, Number>> y1 = new Nil<>() {};
		Nil<BiConsumer<List<? extends Integer>, Integer>> y2 = new Nil<>() {};
		Nil<BiConsumer<List<? extends Number>, ? extends Number>> y3 =
			new Nil<>()
			{};

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
				// TODO Auto-generated method stub
				return null;
			}
		}

		class Bar implements Function<O[], Double> {

			@Override
			public Double apply(O[] t) {
				// TODO Auto-generated method stub
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
		boolean success = GenericAssignability.checkGenericAssignability(listT
			.type(), (ParameterizedType) listWildcard.type(), false);
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

		assertAll(List.class, true, listT, listNumber, listInteger,
			listExtendsNumber);
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

	class Thing<T> {}

	class StrangeThing<N extends Number, T> extends Thing<T> {}

	/**
	 * {@link MatchingUtils#checkGenericOutputsAssignability(Type[], Type[], HashMap)}
	 * not yet fully implemented. If this is done, all the tests below should not
	 * fail.
	 */
	@Test
	public <N> void testOutputAssignability() {
//		Nil<N> n = new Nil<N>() {};
//		Nil<List<N>> ln = new Nil<List<N>>() {};
//		Nil<List<? extends Number>> lWildNum = new Nil<List<? extends Number>>() {};
//		Nil<List<Number>> lNum = new Nil<List<Number>>() {};
//		Nil<List<?>> lwild = new Nil<List<?>>() {};
//
//		HashMap<TypeVariable<?>, TypeVarInfo> typeBounds = new HashMap<>();
//		assertTrue(-1 == Types.isApplicable(new Type[]{Integer.class}, new Type[]{n.type()}, typeBounds));
//		Type[] toOuts = new Type[]{lWildNum.type()};
//		Type[] fromOuts = new Type[]{ln.type()};
//		assertTrue(-1 == MatchingUtils.checkGenericOutputsAssignability(fromOuts, toOuts, typeBounds));
//
//		toOuts = new Type[]{lNum.type()};
//		assertTrue(-1 == MatchingUtils.checkGenericOutputsAssignability(fromOuts, toOuts, typeBounds));
//
//		toOuts = new Type[]{lwild.type()};
//		assertTrue(-1 == MatchingUtils.checkGenericOutputsAssignability(fromOuts, toOuts, typeBounds));
//
//		typeBounds = new HashMap<>();
//		assertTrue(-1 == Types.isApplicable(new Type[]{String.class}, new Type[]{n.type()}, typeBounds));
//		toOuts = new Type[]{lWildNum.type()};
//		fromOuts = new Type[]{ln.type()};
//		assertFalse(-1 == MatchingUtils.checkGenericOutputsAssignability(fromOuts, toOuts, typeBounds));
	}
}
