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

package org.scijava.ops.core.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Inplaces;
import org.scijava.ops.function.Producer;

/**
 * Tests {@link OpBuilder}.
 * 
 * @author Curtis Rueden
 */
public class OpBuilderTest extends AbstractTestEnvironment {

	final double[] halves = new double[10];
	{
		for (int i = 0; i < halves.length; i++)
			halves[i] = i / 2.;
	}

	// -- 0-ary --

	/** Runs a nullary function in a vacuum. */
	@Test
	public void testArity0_OU_run() {
		final Object result = name("test.helloWorld").input().create();
		assertEquals("Hello, world!", result);
	}

	/** Runs a nullary function with a given output type. */
	@Test
	public void testArity0_OT_run() {
		final String result = name("test.helloWorld").input().outType(String.class).create();
		assertEquals("Hello, world!", result);
	}

	/** Runs a nullary computer with a given output value. */
	@Test
	public void testArity0_OV_runC() {
		final double[] result = new double[halves.length];
		Arrays.fill(result, 12345);
		name("math.zero").input().output(result).compute();
		assertTrue(Arrays.stream(result).allMatch(v -> v == 0));
	}

	/** Runs a inplace with a given output value. */
	@Test
	public void testArity0_OV_runI() {
		final double[] actual = {1, 4, 9};
		final double[] expected = {1, 2, 3};
		name("math.sqrt").input().output(actual).mutate();
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a nullary function in a vacuum. */
	@Test
	public void testArity0_OU_matchF() {
		final Producer<?> op = name("test.helloWorld").input().producer();
		final Object result = op.create();
		assertEquals("Hello, world!", result);
	}
	
	/** Matches a nullary computer or function with a given output type. */
	@Test
	public void testArity0_OT_matchF() {
		final Producer<String> op = //
			name("test.helloWorld").input().outType(String.class).producer();
		final String result = op.create();
		assertEquals("Hello, world!", result);
	}
	
	/** Matches a nullary computer or function with a given output type. */
	@Test
	public void testArity0_OT_matchC() {
		final double[] result = new double[halves.length];
		Arrays.fill(result, 12345);
		final Computers.Arity0<double[]> op = //
			name("math.zero").input().outType(double[].class).computer();
		op.compute(result);
		assertTrue(Arrays.stream(result).allMatch(v -> v == 0));
	}

	/** Matches a nullary computer or function with a given output type. */
	@Test
	public void testArity0_OT_matchI() {
		final double[] actual = {1, 4, 9};
		final double[] expected = {1, 2, 3};
		final Inplaces.Arity1<double[]> op = //
			name("math.sqrt").input().outType(double[].class).inplace();
		op.mutate(actual);
		assertTrue(Arrays.equals(actual, expected));
	}


	/** Matches a nullary computer with a given output value. */
	@Test
	public void testArity0_OV_match() {
		final double[] result = new double[10];
		Arrays.fill(result, 12345);
		final Computers.Arity0<double[]> op = //
			name("math.zero").input().output(result).computer();
		op.compute(result);
		assertTrue(Arrays.stream(result).allMatch(v -> v == 0));
	}

	// -- 1-ary --

	/** Runs a unary function using input value only. */
	@Test
	public void testArity1_IV_OU_run() {
		final Object result = name("math.sqrt").input(4.).apply();
		assertEquals(2., result);
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity1_IV_OT_run() {
		final double result = name("math.sqrt").input(9.).outType(double.class).apply();
		assertEquals(3., result, 0.);
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity1_IV_OV_run() {
		final int[] result = new int[halves.length];
		name("test.castToInt").input(halves).output(result).compute();
		for (int i = 0; i < result.length; i++)
			assertEquals((int) halves[i], result[i]);
	}

	/** Matches a unary function using input types only. */
	@Test
	public void testArity1_IT_OU_match() {
		final Function<Double, ?> op = //
			name("math.sqrt").inType(Double.class).function();
		final Object result = op.apply(16.);
		assertEquals(4., result);
	}

	/** Matches a unary computer using input types + output type. */
	public void testArity1_IT_OT_matchC() {
		final Computers.Arity1<double[], int[]> castToInt = //
			name("test.castToInt").inType(double[].class).outType(int[].class).computer();
		final int[] result = new int[halves.length];
		castToInt.compute(halves, result);
		for (int i = 0; i < result.length; i++)
			assertEquals((int) halves[i], result[i]);
	}

	/** Matches a unary function using input types + output type. */
	@Test
	public void testArity1_IT_OT_matchF() {
		final Function<Double, Double> op = //
			name("math.sqrt").inType(Double.class).outType(Double.class).function();
		final double result = op.apply(25.);
		assertEquals(5., result, 0.);
	}

	/** Matches a unary function using input value only. */
	@Test
	public void testArity1_IV_OU_match() {
		final Function<Double, ?> op = //
			name("math.sqrt").input(36.).function();
		final Object result = op.apply(49.);
		assertEquals(7., result);
	}

	/** Matches a unary function using input value + output type. */
	@Test
	public void testArity1_IV_OT_match() {
		final Function<Double, Double> op = //
			name("math.sqrt").input(64.).outType(Double.class).function();
		final double result = op.apply(81.);
		assertEquals(9., result, 0.);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity1_IV_OV_match() {
		final int[] result = new int[halves.length];
		final Computers.Arity1<double[], int[]> op = //
			name("test.castToInt").input(halves).output(result).computer();
		op.compute(halves, result);
		for (int i = 0; i < result.length; i++)
			assertEquals((int) halves[i], result[i]);
	}

	// -- 2-ary --

	/** Runs a binary function using input values only. */
	@Test
	public void testArity2_IV_OU_run() {
		final Object result = name("math.add").input(1., 2.).apply();
		assertEquals(3., result);
	}

	/** Runs a binary function using input values + output type. */
	@Test
	public void testArity2_IV_OT_run() {
		final double result = name("math.add").input(3., 4.).outType(double.class).apply();
		assertEquals(7., result, 0.);
	}

	/** Runs a binary computer using input values + output value. */
	@Test
	public void testArity2_IV_OV_run() {
		final String[] s = {"quick", "brown", "fox"};
		final Object[] o = {"er", "est", "es"};
		final String[] result = new String[s.length];
		name("test.concat").input(s, o).output(result).compute();
		for (int i = 0; i < result.length; i++)
			assertEquals(s[i] + o[i], result[i]);
	}

	/** Matches a binary function using input types only. */
	@Test
	public void testArity2_IT_OU_match() {
		final BiFunction<Double, Double, ?> op = //
			name("math.add").inType(Double.class, Double.class).function();
		final Object result = op.apply(5., 6.);
		assertEquals(11., result);
	}

	/** Matches a binary computer using input types + output type. */
	@Test
	public void testArity2_IT_OT_matchC() {
		final Computers.Arity2<String[], Object[], String[]> op = //
			name("test.concat").inType(String[].class, Object[].class).outType(String[].class).computer();
		final String[] s = {"whirl", "p"};
		final Object[] o = {"ed", "eas"};
		final String[] result = new String[s.length];
		op.compute(s, o, result);
		for (int i = 0; i < result.length; i++)
			assertEquals(s[i] + o[i], result[i]);
	}

	/** Matches a binary function using input types + output type. */
	@Test
	public void testArity2_IT_OT_matchF() {
		final BiFunction<Double, Double, Double> op = //
			name("math.add").inType(Double.class, Double.class).outType(Double.class).function();
		final Object result = op.apply(7., 8.);
		assertEquals(15., result);
	}

	/** Matches a binary function using input values only. */
	@Test
	public void testArity2_IV_OU_match() {
		final BiFunction<Double, Double, ?> op = //
			name("math.add").input(9., 10.).function();
		final Object result = op.apply(11., 12.);
		assertEquals(23., result);
	}

	/** Matches a binary function using input values + output type. */
	@Test
	public void testArity2_IV_OT_match() {
		final BiFunction<Double, Double, Double> op = //
			name("math.add").input(13., 14.).outType(Double.class).function();
		final double result = op.apply(15., 16.);
		assertEquals(31., result, 0.);
	}

	/** Matches a binary computer using input value + output value. */
	@Test
	public void testArity2_IV_OV_match() {
		final String[] s = {"ups", "and", "downs"};
		final Object[] o = {"ide", "rogynous", "tream"};
		final String[] result = new String[s.length];
		Computers.Arity2<String[], Object[], String[]> op = name("test.concat").input(s, o).output(result).computer();
		op.compute(s, o, result);
		for (int i = 0; i < result.length; i++)
			assertEquals(s[i] + o[i], result[i]);
	}

	// -- Helper methods --

	private OpBuilder name(String opName) {
		return new OpBuilder(ops, opName);
	}
}
