/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2018 SciJava developers.
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

package org.scijava.ops.util;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;
import org.scijava.ops.matcher.MatchingUtils;
import org.scijava.ops.types.Nil;
import org.scijava.util.Types;

public class MatchingUtilsTest {

	private void assertAll(Class<?> from, boolean condition, Nil<?>... tos) {
		for (Nil<?> to : tos) {
			assertAll(from, condition, to.getType());
		}
	}

	private void assertAll(Class<?> from, boolean condition, Type... tos) {
		for (Type to : tos) {
			if (to instanceof ParameterizedType) {
				assertTrue(MatchingUtils.checkGenericAssignability(from, (ParameterizedType) to) == condition);
			} else {
				assertTrue(Types.isAssignable(from, to) == condition);
			}
		}
	}

	@Test
	public void genericAssignabilitySingleVar() {
		abstract class Single<I> implements Supplier<I> {
		}
		Nil<Supplier<Double>> y1 = new Nil<Supplier<Double>>() {
		};
		Nil<Supplier<Number>> y2 = new Nil<Supplier<Number>>() {
		};
		Nil<Double> n1 = new Nil<Double>() {
		};

		assertAll(Single.class, true, y1, y2);
		assertAll(Single.class, false, n1);
	}

	@Test
	public void genericAssignabilitySingleVarBounded() {
		abstract class SingleBounded<I extends Number> implements Supplier<I> {
		}

		Nil<Supplier<Double>> y1 = new Nil<Supplier<Double>>() {
		};
		Nil<Double> n1 = new Nil<Double>() {
		};
		Nil<String> n2 = new Nil<String>() {
		};

		assertAll(SingleBounded.class, true, y1);
		assertAll(SingleBounded.class, false, n1, n2);
	}

	@Test
	public void genericAssignabilitySingleVarBoundedUsedNested() {
		abstract class SingleVarBoundedUsedNested<I extends Number> implements Supplier<List<I>> {
		}

		Nil<Double> n1 = new Nil<Double>() {
		};
		Nil<String> n2 = new Nil<String>() {
		};
		Nil<Supplier<Double>> n3 = new Nil<Supplier<Double>>() {
		};
		Nil<Supplier<List<String>>> n4 = new Nil<Supplier<List<String>>>() {
		};
		Nil<Supplier<List<Double>>> y1 = new Nil<Supplier<List<Double>>>() {
		};

		assertAll(SingleVarBoundedUsedNested.class, true, y1);
		assertAll(SingleVarBoundedUsedNested.class, false, n1, n2, n3, n4);
	}

	@Test
	public void genericAssignabilitySingleVarBoundedUsedNestedAndOther() {
		abstract class SingleVarBoundedUsedNestedAndOther<I extends Number> implements Function<List<I>, Double> {
		}
		abstract class SingleVarBoundedUsedNestedAndOtherNested<I extends Number>
				implements Function<List<I>, List<Double>> {
		}
		abstract class SingleVarBoundedUsedNestedAndOtherNestedWildcard<I extends Number>
				implements Function<List<I>, List<? extends Number>> {
		}

		Nil<Function<List<Double>, Double>> y1 = new Nil<Function<List<Double>, Double>>() {
		};
		Nil<Function<Double, Double>> n1 = new Nil<Function<Double, Double>>() {
		};
		Nil<Function<List<String>, Double>> n2 = new Nil<Function<List<String>, Double>>() {
		};
		Nil<Function<List<Double>, String>> n3 = new Nil<Function<List<Double>, String>>() {
		};

		assertAll(SingleVarBoundedUsedNestedAndOther.class, true, y1);
		assertAll(SingleVarBoundedUsedNestedAndOther.class, false, n1, n2, n3);

		Nil<Function<List<Double>, List<Double>>> y2 = new Nil<Function<List<Double>, List<Double>>>() {
		};
		Nil<Function<Double, List<Double>>> n4 = new Nil<Function<Double, List<Double>>>() {
		};
		Nil<Function<List<String>, List<Double>>> n5 = new Nil<Function<List<String>, List<Double>>>() {
		};
		Nil<Function<List<Double>, List<String>>> n6 = new Nil<Function<List<Double>, List<String>>>() {
		};

		assertAll(SingleVarBoundedUsedNestedAndOtherNested.class, true, y2);
		assertAll(SingleVarBoundedUsedNestedAndOtherNested.class, false, n4, n5, n6);

		Nil<Function<Double, List<Double>>> n7 = new Nil<Function<Double, List<Double>>>() {
		};
		Nil<Function<List<String>, List<Double>>> n8 = new Nil<Function<List<String>, List<Double>>>() {
		};
		Nil<Function<List<Double>, List<String>>> n9 = new Nil<Function<List<Double>, List<String>>>() {
		};
		Nil<Function<List<Double>, List<Double>>> n10 = new Nil<Function<List<Double>, List<Double>>>() {
		};
		Nil<Function<List<Double>, List<Integer>>> n11 = new Nil<Function<List<Double>, List<Integer>>>() {
		};
		Nil<Function<List<Double>, List<? extends Number>>> y3 = new Nil<Function<List<Double>, List<? extends Number>>>() {
		};

		assertAll(SingleVarBoundedUsedNestedAndOtherNestedWildcard.class, false, n7, n8, n9, n10, n11);
		assertAll(SingleVarBoundedUsedNestedAndOtherNestedWildcard.class, true, y3);
	}

	@Test
	public void genericAssignabilitySingleVarNestedBoundNestedAndOther() {
		abstract class SingleVarBoundedNestedAndOther<I extends Iterable<String>> implements Function<I, Double> {
		}
		abstract class SingleVarBoundedNestedWildcardAndOther<I extends Iterable<? extends Number>>
				implements Function<I, List<Double>> {
		}

		Nil<Function<List<String>, Double>> y1 = new Nil<Function<List<String>, Double>>() {
		};
		Nil<Function<Iterable<String>, Double>> y2 = new Nil<Function<Iterable<String>, Double>>() {
		};
		Nil<Function<Double, Double>> n1 = new Nil<Function<Double, Double>>() {
		};
		Nil<Function<List<String>, String>> n2 = new Nil<Function<List<String>, String>>() {
		};
		Nil<Function<Iterable<Double>, Double>> n3 = new Nil<Function<Iterable<Double>, Double>>() {
		};

		assertAll(SingleVarBoundedNestedAndOther.class, true, y1, y2);
		assertAll(SingleVarBoundedNestedAndOther.class, false, n1, n2, n3);

		Nil<Function<List<Integer>, List<Double>>> y3 = new Nil<Function<List<Integer>, List<Double>>>() {
		};
		Nil<Function<List<Double>, List<Double>>> y4 = new Nil<Function<List<Double>, List<Double>>>() {
		};
		Nil<Function<Iterable<Double>, List<Double>>> y5 = new Nil<Function<Iterable<Double>, List<Double>>>() {
		};
		Nil<Function<List<Integer>, Double>> n4 = new Nil<Function<List<Integer>, Double>>() {
		};
		Nil<Function<List<Double>, List<Integer>>> n5 = new Nil<Function<List<Double>, List<Integer>>>() {
		};
		Nil<Function<Iterable<Double>, Iterable<Double>>> n6 = new Nil<Function<Iterable<Double>, Iterable<Double>>>() {
		};
		Nil<Function<Integer, List<Double>>> n7 = new Nil<Function<Integer, List<Double>>>() {
		};
		Nil<Function<List<String>, List<Double>>> n8 = new Nil<Function<List<String>, List<Double>>>() {
		};

		assertAll(SingleVarBoundedNestedWildcardAndOther.class, true, y3, y4, y5);
		assertAll(SingleVarBoundedNestedWildcardAndOther.class, false, n4, n5, n6, n7, n8);
	}

	@Test
	public void genericAssignabilitySingleVarMultipleOccurence() {
		abstract class SingleVarBoundedNestedMultipleOccurence<I extends Iterable<String>> implements Function<I, I> {
		}

		abstract class SingleVarBoundedNestedWildcardMultipleOccurence<I extends Iterable<? extends Number>>
				implements Function<I, I> {
		}

		abstract class SingleVarBoundedNestedWildcardMultipleOccurenceUsedNested<I extends Iterable<? extends Number>>
				implements Function<I, List<I>> {
		}

		Nil<Function<List<String>, List<String>>> y1 = new Nil<Function<List<String>, List<String>>>() {
		};
		Nil<Function<Iterable<String>, Iterable<String>>> y2 = new Nil<Function<Iterable<String>, Iterable<String>>>() {
		};
		Nil<Function<List<String>, List<Integer>>> n1 = new Nil<Function<List<String>, List<Integer>>>() {
		};
		Nil<Function<List<String>, Double>> n2 = new Nil<Function<List<String>, Double>>() {
		};

		assertAll(SingleVarBoundedNestedMultipleOccurence.class, true, y1, y2);
		assertAll(SingleVarBoundedNestedMultipleOccurence.class, false, n1, n2);

		Nil<Function<List<Double>, List<Double>>> y3 = new Nil<Function<List<Double>, List<Double>>>() {
		};
		Nil<Function<Iterable<Double>, Iterable<Double>>> y4 = new Nil<Function<Iterable<Double>, Iterable<Double>>>() {
		};
		Nil<Function<Iterable<Double>, Iterable<Integer>>> n3 = new Nil<Function<Iterable<Double>, Iterable<Integer>>>() {
		};
		Nil<Function<List<String>, Integer>> n4 = new Nil<Function<List<String>, Integer>>() {
		};

		assertAll(SingleVarBoundedNestedWildcardMultipleOccurence.class, true, y3, y4);
		assertAll(SingleVarBoundedNestedWildcardMultipleOccurence.class, false, n3, n4);

		Nil<Function<List<Double>, Iterable<List<Double>>>> n5 = new Nil<Function<List<Double>, Iterable<List<Double>>>>() {
		};
		Nil<Function<Iterable<Double>, List<Iterable<Double>>>> y5 = new Nil<Function<Iterable<Double>, List<Iterable<Double>>>>() {
		};

		assertAll(SingleVarBoundedNestedWildcardMultipleOccurenceUsedNested.class, true, y5);
		assertAll(SingleVarBoundedNestedWildcardMultipleOccurenceUsedNested.class, false, n5);
		
		abstract class SingleVarMultipleOccurenceUsedNested<I> implements Function<I, List<I>> {}
		
		Nil<Function<Integer, List<Number>>> n6 = new Nil<Function<Integer,List<Number>>>() {
		};
		assertAll(SingleVarMultipleOccurenceUsedNested.class, false, n6);
	}

	@Test
	public void genericAssignabilityDoubleVar() {
		abstract class DoubleVar<I, B> implements Function<I, B> {
		}

		abstract class DoubleVarBounded<I extends List<String>, B extends Number> implements Function<I, B> {
		}

		Nil<Function<List<String>, List<String>>> y1 = new Nil<Function<List<String>, List<String>>>() {
		};
		Nil<Function<Iterable<String>, Iterable<String>>> y2 = new Nil<Function<Iterable<String>, Iterable<String>>>() {
		};
		Nil<Function<List<String>, List<Integer>>> y3 = new Nil<Function<List<String>, List<Integer>>>() {
		};
		Nil<Function<List<String>, Double>> y4 = new Nil<Function<List<String>, Double>>() {
		};

		assertAll(DoubleVar.class, true, y1, y2, y3, y4);

		Nil<Function<List<String>, Double>> y5 = new Nil<Function<List<String>, Double>>() {
		};
		Nil<Function<List<String>, Float>> y6 = new Nil<Function<List<String>, Float>>() {
		};
		Nil<Function<Iterable<String>, Double>> n1 = new Nil<Function<Iterable<String>, Double>>() {
		};
		Nil<Function<List<Double>, Integer>> n2 = new Nil<Function<List<Double>, Integer>>() {
		};
		Nil<Function<List<String>, String>> n3 = new Nil<Function<List<String>, String>>() {
		};

		assertAll(DoubleVarBounded.class, true, y5, y6);
		assertAll(DoubleVarBounded.class, false, n1, n2, n3);
	}

	@Test
	public void genericAssignabilityDoubleVarDepending() {
		abstract class BExtendsI<I extends Iterable<? extends Number>, B extends I> implements Function<I, B> {
		}

		abstract class IBoundedByN<N extends Number, I extends Iterable<N>> implements BiFunction<I, I, N> {
		}

		Nil<Function<List<Integer>, List<Integer>>> y1 = new Nil<Function<List<Integer>, List<Integer>>>() {
		};
		Nil<Function<Iterable<Integer>, List<Integer>>> y2 = new Nil<Function<Iterable<Integer>, List<Integer>>>() {
		};
		Nil<Function<Iterable<Integer>, Iterable<Integer>>> y3 = new Nil<Function<Iterable<Integer>, Iterable<Integer>>>() {
		};
		Nil<Function<List<String>, List<Integer>>> n1 = new Nil<Function<List<String>, List<Integer>>>() {
		};
		Nil<Function<List<String>, Double>> n2 = new Nil<Function<List<String>, Double>>() {
		};
		Nil<Function<List<Integer>, List<Double>>> n3 = new Nil<Function<List<Integer>, List<Double>>>() {
		};
		Nil<Function<Integer, List<Integer>>> n4 = new Nil<Function<Integer, List<Integer>>>() {
		};
		Nil<Function<Iterable<Integer>, Integer>> n5 = new Nil<Function<Iterable<Integer>, Integer>>() {
		};
		
		assertAll(BExtendsI.class, true, y1, y2, y3);
		assertAll(BExtendsI.class, false, n1, n2, n3, n4, n5);

		Nil<BiFunction<List<Integer>, List<Integer>, Integer>> y4 = new Nil<BiFunction<List<Integer>, List<Integer>, Integer>>() {
		};
		Nil<BiFunction<Iterable<Integer>, Iterable<Integer>, Integer>> y5 = new Nil<BiFunction<Iterable<Integer>, Iterable<Integer>, Integer>>() {
		};
		Nil<BiFunction<List<Integer>, List<Integer>, Double>> n6 = new Nil<BiFunction<List<Integer>, List<Integer>, Double>>() {
		};
		Nil<BiFunction<Iterable<Double>, Iterable<Integer>, Integer>> n7 = new Nil<BiFunction<Iterable<Double>, Iterable<Integer>, Integer>>() {
		};
		Nil<BiFunction<Iterable<Integer>, List<Integer>, Integer>> n8 = new Nil<BiFunction<Iterable<Integer>, List<Integer>, Integer>>() {
		};
		Nil<BiFunction<Iterable<String>, List<String>, String>> n9 = new Nil<BiFunction<Iterable<String>, List<String>, String>>() {
		};

		assertAll(IBoundedByN.class, true, y4, y5);
		assertAll(IBoundedByN.class, false, n6, n7, n8, n9);
	}

	@Test
	public void genericAssignabilityDoubleVarDependingImplicitelyBounded() {
		abstract class IBoundedByNImplicitely<N extends Number, I extends Iterable<N>>
				implements BiFunction<I, I, List<String>> {
		}

		Nil<BiFunction<Iterable<Double>, Iterable<Double>, List<String>>> y1 = new Nil<BiFunction<Iterable<Double>, Iterable<Double>, List<String>>>() {
		};
		
		assertAll(IBoundedByNImplicitely.class, true, y1);
	}

	@Test
	public void genericAssignabilityDoubleVarBoundedAndWildcard() {
		abstract class DoubleVarBoundedAndWildcard<M extends Number, I extends Iterable<? extends Number>>
				implements BiFunction<I, I, Iterable<M>> {
		}

		Nil<BiFunction<Iterable<Double>, Double, Iterable<Double>>> n1 = new Nil<BiFunction<Iterable<Double>, Double, Iterable<Double>>>() {
		};
		Nil<BiFunction<Double, Iterable<Double>, Iterable<Double>>> n2 = new Nil<BiFunction<Double, Iterable<Double>, Iterable<Double>>>() {
		};
		Nil<BiFunction<List<Float>, List<Number>, Iterable<Double>>> n3 = new Nil<BiFunction<List<Float>, List<Number>, Iterable<Double>>>() {
		};
		Nil<BiFunction<Iterable<Double>, List<Double>, Iterable<Double>>> n4 = new Nil<BiFunction<Iterable<Double>, List<Double>, Iterable<Double>>>() {
		};
		Nil<BiFunction<List<Double>, List<Double>, List<Double>>> n5 = new Nil<BiFunction<List<Double>, List<Double>, List<Double>>>() {
		};
		Nil<BiFunction<List<Integer>, List<Double>, Iterable<Double>>> n6 = new Nil<BiFunction<List<Integer>, List<Double>, Iterable<Double>>>() {
		};
		Nil<BiFunction<List<Double>, List<Double>, Iterable<Double>>> y1 = new Nil<BiFunction<List<Double>, List<Double>, Iterable<Double>>>() {
		};
		Nil<BiFunction<Iterable<Double>, Iterable<Double>, Iterable<Double>>> y2 = new Nil<BiFunction<Iterable<Double>, Iterable<Double>, Iterable<Double>>>() {
		};

		assertAll(DoubleVarBoundedAndWildcard.class, true, y1, y2);
		assertAll(DoubleVarBoundedAndWildcard.class, false, n1, n2, n3, n4, n5, n6);
	}

	@Test
	public void genericAssignabilityWildcards() {
		abstract class Wildcards implements Function<List<? extends Number>, List<? extends Number>> {
		}

		Nil<Function<List<? extends Number>, List<? extends Number>>> y1 = new Nil<Function<List<? extends Number>, List<? extends Number>>>() {
		};
		Nil<Function<Iterable<Integer>, Iterable<Double>>> n1 = new Nil<Function<Iterable<Integer>, Iterable<Double>>>() {
		};
		Nil<Function<List<Double>, List<Integer>>> n2 = new Nil<Function<List<Double>, List<Integer>>>() {
		};
		Nil<Function<List<Double>, List<Double>>> n3 = new Nil<Function<List<Double>, List<Double>>>() {
		};
		Nil<Function<Iterable<Double>, Iterable<Double>>> n4 = new Nil<Function<Iterable<Double>, Iterable<Double>>>() {
		};

		assertAll(Wildcards.class, true, y1);
		assertAll(Wildcards.class, false, n1, n2, n3, n4);
	}

	@Test(expected = NullPointerException.class)
	public void testIsAssignableNullToNull() {
		MatchingUtils.checkGenericAssignability(null, null);
	}

	@Test(expected = NullPointerException.class)
	public void testIsAssignableClassToNull() {
		MatchingUtils.checkGenericAssignability(Object.class, null);
	}

	@Test
	public <T extends Number> void testIsAssignableT() {
		final Type t = new Nil<T>() {
		}.getType();
		final Type listT = new Nil<List<T>>() {
		}.getType();
		final Type listNumber = new Nil<List<Number>>() {
		}.getType();
		final Type listInteger = new Nil<List<Integer>>() {
		}.getType();
		final Type listExtendsNumber = new Nil<List<? extends Number>>() {
		}.getType();

		assertAll(List.class, true, listT, listNumber, listInteger);
		assertAll(List.class, false, listExtendsNumber, t);
	}
	
	/**
	 * {@link MatchingUtils#checkGenericOutputsAssignability(Type[], Type[], HashMap)} not yet fully
	 * implemented. If this is done, all the tests below should not fail.
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
//		assertTrue(-1 == Types.isApplicable(new Type[]{Integer.class}, new Type[]{n.getType()}, typeBounds));
//		Type[] toOuts = new Type[]{lWildNum.getType()};
//		Type[] fromOuts = new Type[]{ln.getType()};
//		assertTrue(-1 == MatchingUtils.checkGenericOutputsAssignability(fromOuts, toOuts, typeBounds));
//		
//		toOuts = new Type[]{lNum.getType()};
//		assertTrue(-1 == MatchingUtils.checkGenericOutputsAssignability(fromOuts, toOuts, typeBounds));
//		
//		toOuts = new Type[]{lwild.getType()};
//		assertTrue(-1 == MatchingUtils.checkGenericOutputsAssignability(fromOuts, toOuts, typeBounds));
//		
//		typeBounds = new HashMap<>();
//		assertTrue(-1 == Types.isApplicable(new Type[]{String.class}, new Type[]{n.getType()}, typeBounds));
//		toOuts = new Type[]{lWildNum.getType()};
//		fromOuts = new Type[]{ln.getType()};
//		assertFalse(-1 == MatchingUtils.checkGenericOutputsAssignability(fromOuts, toOuts, typeBounds));
	}
}
