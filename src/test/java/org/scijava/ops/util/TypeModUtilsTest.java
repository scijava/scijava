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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.transform.TypeModUtils;
import org.scijava.ops.types.Nil;

public class TypeModUtilsTest {

	@Test
	public <A, B, C> void replaceRaw() {
		Nil<Iterable<A>> n1 = new Nil<Iterable<A>>() {
		};
		Nil<Function<Double, String>> n2 = new Nil<Function<Double, String>>() {
		};
		Nil<A> n3 = new Nil<A>() {
		};
		Nil<B[]> n4 = new Nil<B[]>() {
		};
		Nil<Supplier<? extends Number>> n5 = new Nil<Supplier<? extends Number>>() {
		};
		Nil<Consumer<C>> n6 = new Nil<Consumer<C>>() {
		};

		Type[] types = new Type[] { Double.class, String.class, Iterable.class, String.class };
		assertTrue(TypeModUtils.replaceRawTypes(types, String.class, Double.class));
		assertTrue(Arrays.deepEquals(types, new Type[] { Double.class, Double.class, Iterable.class, Double.class }));
		
		types = new Type[] { Double.class, String.class, Iterable.class, String.class };
		assertTrue(TypeModUtils.replaceRawTypes(types, String.class, Double.class, 1));
		assertTrue(Arrays.deepEquals(types, new Type[] { Double.class, Double.class, Iterable.class, String.class }));
		
		types = new Type[] { Double.class, String.class, Iterable.class, String.class };
		assertTrue(TypeModUtils.replaceRawTypes(types, String.class, Double.class, 1, 3));
		assertTrue(Arrays.deepEquals(types, new Type[] { Double.class, Double.class, Iterable.class, Double.class }));

		types = new Type[] { n1.getType(), n2.getType(), n3.getType(), n4.getType(), n5.getType(), n6.getType() };
		assertTrue(TypeModUtils.replaceRawTypes(types, Function.class, Function.class));
		assertTrue(Arrays.deepEquals(types,
				new Type[] { n1.getType(), n2.getType(), n3.getType(), n4.getType(), n5.getType(), n6.getType() }));

		types = new Type[] { n1.getType(), n2.getType(), n3.getType(), n4.getType(), n5.getType(), n6.getType() };
		assertTrue(TypeModUtils.replaceRawTypes(types, Function.class, Computer.class));
		Nil<Computer<Double, String>> y2 = new Nil<Computer<Double, String>>() {
		};
		assertTrue(Arrays.deepEquals(types,
				new Type[] { n1.getType(), y2.getType(), n3.getType(), n4.getType(), n5.getType(), n6.getType() }));

		types = new Type[] { n1.getType(), n2.getType(), n3.getType(), n4.getType(), n5.getType(), n6.getType() };
		assertTrue(TypeModUtils.replaceRawTypes(types, Supplier.class, Iterable.class));
		Nil<Iterable<? extends Number>> y5 = new Nil<Iterable<? extends Number>>() {
		};
		assertTrue(Arrays.deepEquals(types,
				new Type[] { n1.getType(), n2.getType(), n3.getType(), n4.getType(), y5.getType(), n6.getType() }));

		types = new Type[] { n1.getType(), n2.getType(), n3.getType(), n4.getType(), n5.getType(), n6.getType() };
		assertTrue(TypeModUtils.replaceRawTypes(types, Consumer.class, List.class));
		Nil<List<C>> y6 = new Nil<List<C>>() {
		};
		assertTrue(Arrays.deepEquals(types,
				new Type[] { n1.getType(), n2.getType(), n3.getType(), n4.getType(), n5.getType(), y6.getType() }));

		types = new Type[] { n1.getType(), n2.getType(), n3.getType(), n4.getType(), n5.getType(), n6.getType() };
		assertFalse(TypeModUtils.replaceRawTypes(types, Integer.class, List.class));
		assertTrue(Arrays.deepEquals(types,
				new Type[] { n1.getType(), n2.getType(), n3.getType(), n4.getType(), n5.getType(), n6.getType() }));
	}

	@Test
	public <A, B, C> void unliftParameterized() {
		Nil<Computer<A, B[]>> n1 = new Nil<Computer<A, B[]>>() {
		};
		Nil<Function<Double, String[]>> n2 = new Nil<Function<Double, String[]>>() {
		};
		Nil<Function<C[], Iterable<String>>> n3 = new Nil<Function<C[], Iterable<String>>>() {
		};
		Nil<Computer<C[], Function<String, Integer>>> n4 = new Nil<Computer<C[], Function<String, Integer>>>() {
		};
		
		Nil<Function<Double, String>> y2 = new Nil<Function<Double, String>>() {
		};
		Nil<Function<C, Iterable<String>>> y3 = new Nil<Function<C, Iterable<String>>>() {
		};
		Nil<Function<C[], String>> y31 = new Nil<Function<C[], String>>() {
		};

		Type[] types = new Type[] { n1.getType(), n2.getType(), n3.getType() };
		assertFalse(TypeModUtils.unliftParameterizedTypes(types, Computer.class, Iterable.class));
		assertTrue(Arrays.deepEquals(types, new Type[] { n1.getType(), n2.getType(), n3.getType() }));
		
		types = new Type[] { n1.getType(), n2.getType(), n4.getType() };
		assertFalse(TypeModUtils.unliftParameterizedTypes(types, Computer.class, Function.class));
		assertTrue(Arrays.deepEquals(types, new Type[] { n1.getType(), n2.getType(), n4.getType() }));
		
		types = new Type[] { n1.getType(), n2.getType(), n3.getType() };
		assertTrue(TypeModUtils.unliftParameterizedTypes(types, Function.class, Iterable.class));
		assertTrue(Arrays.deepEquals(types, new Type[] { n1.getType(), n2.getType(), y31.getType() }));
		
		types = new Type[] { n1.getType(), n2.getType(), n3.getType() };
		assertTrue(TypeModUtils.unliftParameterizedTypes(types, Function.class, Array.class));
		assertTrue(Arrays.deepEquals(types, new Type[] { n1.getType(), y2.getType(), y3.getType() }));
		
		types = new Type[] { n1.getType(), n2.getType(), n3.getType() };
		assertTrue(TypeModUtils.unliftParameterizedTypes(types, Function.class, Array.class, 1));
		assertTrue(Arrays.deepEquals(types, new Type[] { n1.getType(), y2.getType(), n3.getType() }));
		
		types = new Type[] { n1.getType(), n2.getType(), n3.getType() };
		assertTrue(TypeModUtils.unliftParameterizedTypes(types, Function.class, Array.class, 0));
		assertTrue(Arrays.deepEquals(types, new Type[] { n1.getType(), n2.getType(), y3.getType() }));
		
		types = new Type[] { n1.getType(), n2.getType(), n3.getType() };
		assertTrue(TypeModUtils.unliftParameterizedTypes(types, Function.class, Array.class, 0, 1));
		assertTrue(Arrays.deepEquals(types, new Type[] { n1.getType(), y2.getType(), y3.getType() }));

	}
}
