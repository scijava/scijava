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
import static org.junit.Assert.assertFalse;
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
 * For each arity, we test the following matches and run the following commands 
 * based on the information scenarios.
 * 
 * * Input TYPES are given (IT) 
 * 	1) The output is unspecified (OU): 
 * 		a) match: Function, Inplace
 * 		b) run: none
 * 	2) The output type is given (OT): 
 * 		a) match: Function, Computer
 * 		b) run: none
 *  
 * * Input VALUES are given (IV) (N.B. this case applies for Arity0):
 * 	1) The output is unspecified (OU): 
 * 		a) match: Function, Inplace
 * 		b) run: apply, mutate
 * 	2) The output type is given (OT): 
 * 		a) match: Function, Computer
 * 		b) run: apply
 * 	3) The output value is given (OV): 
 * 		a) match: Computer
 *  	b) run: compute
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

	/** Matches a nullary function in a vacuum. */
	@Test
	public void testArity0_OU_matchF() {
		final Producer<?> op = name("test.addDoubles").input().producer();
		final Object result = op.create();
		assertEquals(0., result);
	}

	/** Runs a nullary function in a vacuum. */
	@Test
	public void testArity0_OU_runF() {
		final Object result = name("test.addDoubles").input().create();
		assertEquals(0., result);
	}

	/** Matches a nullary function with a given output type. */
	@Test
	public void testArity0_OT_matchF() {
		final Double expected = 0.;
		final Producer<Double> op = //
				name("test.addDoubles").input().outType(Double.class).producer();
		final Object result = op.create();
		assertEquals(result, expected);
	}

	/** Matches a nullary computer with a given output type. */
	@Test
	public void testArity0_OT_matchC() {
		final double[] actual = { 1, 2, 3 };
		final double[] expected = { 0, 0, 0 };
		final Computers.Arity0<double[]> op = //
				name("test.addArrays").input().outType(double[].class).computer();
		op.compute(actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a nullary function with a given output type. */
	@Test
	public void testArity0_OT_runF() {
		final Double result = name("test.addDoubles").input().outType(Double.class).create();
		assert (0. == result);
	}

	/** Matches a nullary computer with a given output value. */
	@Test
	public void testArity0_OV_matchC() {
		double[] result = { 1, 2, 3 };
		double[] expected = { 0, 0, 0 };
		final Computers.Arity0<double[]> op = //
				name("test.addArrays").input().output(result).computer();
		op.compute(result);
		assertTrue(Arrays.equals(expected, result));
	}

	/** Runs a nullary computer with a given output value. */
	@Test
	public void testArity0_OV_runC() {
		double[] result = { 1, 2, 3 };
		double[] expected = { 0, 0, 0 };
		name("test.addArrays").input().output(result).compute();
		assertTrue(Arrays.equals(expected, result));
	}

	// -- 1-ary --

	/** Matches a unary function using input types only. */
	@Test
	public void testArity1_IT_OU_matchF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Function<Double, ?> op = //
				name("test.addDoubles").inType(Double.class).function();
		final Object actual = op.apply(input);
		assertEquals(actual, expected);
	}

	/** Matches an inplace with a given output type. */
	@Test
	public void testArity1_IT_OU_matchI() {
		final double[] actual = { 1, 2, 3 };
		final double[] expected = { 1, 2, 3 };
		final Inplaces.Arity1<double[]> op = //
				name("test.mulArrays").inType(double[].class).inplace();
		op.mutate(actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a unary function using input types + output type. */
	@Test
	public void testArity1_IT_OT_matchF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Function<Double, Double> op = //
				name("test.addDoubles").inType(Double.class).outType(Double.class).function();
		final double actual = op.apply(input);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity1_IT_OT_matchC() {
		double[] input = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 1, 2, 3 };
		final Computers.Arity1<double[], double[]> castToInt = //
				name("test.addArrays").inType(double[].class).outType(double[].class).computer();
		castToInt.compute(input, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a unary function using input value only. */
	@Test
	public void testArity1_IV_OU_matchF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Function<Double, ?> op = //
				name("test.addDoubles").input(input).function();
		final Object actual = op.apply(input);
		assertEquals(actual, expected);
	}

	/** Matches a nullary inplace with a given output value. */
	@Test
	public void testArity1_IV_OU_matchI() {
		double[] result = { 1, 2, 3 };
		double[] expected = { 1, 2, 3 };
		final Inplaces.Arity1<double[]> op = //
				name("test.mulArrays").input(result).inplace();
		op.mutate(result);
		assertTrue(Arrays.equals(expected, result));
	}

	/** Runs a unary function using input value only. */
	@Test
	public void testArity1_IV_OU_runF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Object actual = name("test.addDoubles").input(input).apply();
		assertEquals(actual, expected);
	}

	/** Runs a inplace with a given output value. */
	@Test
	public void testArity1_IV_OU_runI() {
		final double[] actual = { 1, 2, 3 };
		final double[] expected = { 1, 2, 3 };
		name("test.mulArrays").input(actual).mutate();
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a unary function using input value + output type. */
	@Test
	public void testArity1_IV_OT_matchF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Function<Double, Double> op = //
				name("test.addDoubles").input(input).outType(Double.class).function();
		final double actual = op.apply(input);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity1_IV_OT_matchC() {
		double[] input = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 1, 2, 3 };
		final Computers.Arity1<double[], double[]> castToInt = //
				name("test.addArrays").input(input).output(actual).computer();
		castToInt.compute(input, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity1_IV_OT_runF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Object actual = name("test.addDoubles").input(input).outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity1_IV_OV_matchC() {
		double[] input = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 1, 2, 3 };
		final Computers.Arity1<double[], double[]> op = //
				name("test.addArrays").input(input).output(actual).computer();
		op.compute(input, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity1_IV_OV_runC() {
		double[] input = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 1, 2, 3 };
		name("test.addArrays").input(input).output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- 2-ary --

	/** Matches a binary function using input types + output type. */
	@Test
	public void testArity2_IT_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double expected = 2.;
		final BiFunction<Double, Double, ?> op = //
				name("test.addDoubles").inType(Double.class, Double.class).function();
		final Object actual = op.apply(input1, input2);
		assertEquals(actual, expected);
	}

	/** Matches a binary inplace1 using input types + output type. */
	@Test
	public void testArity2_IT_OU_matchI1() {
		final double[] actual1 = { 1, 2, 3 };
		final double[] actual2 = { 1, 2, 3 };
		final double[] expected = { 1, 4, 9 };
		final Inplaces.Arity2_1<double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class).inplace1();
		op.mutate(actual1, actual2);
		assertTrue(Arrays.equals(actual1, expected));
		assertFalse(Arrays.equals(actual2, expected));
	}

	/** Matches a binary inplace1 using input types + output type. */
	@Test
	public void testArity2_IT_OU_matchI2() {
		final double[] actual1 = { 1, 2, 3 };
		final double[] actual2 = { 1, 2, 3 };
		final double[] expected = { 1, 4, 9 };
		final Inplaces.Arity2_2<double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class).inplace2();
		op.mutate(actual1, actual2);
		assertTrue(Arrays.equals(actual2, expected));
		assertFalse(Arrays.equals(actual1, expected));
	}

	/** Matches a binary function using input types + output type. */
	@Test
	public void testArity2_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double expected = 2.;
		final BiFunction<Double, Double, Double> op = //
				name("test.addDoubles").inType(Double.class, Double.class).outType(Double.class).function();
		final Double actual = op.apply(input1, input2);
		assertEquals(actual, expected);
	}

	/** Matches a binary computer using input types + output type. */
	@Test
	public void testArity2_IT_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 2, 4, 6 };
		final Computers.Arity2<double[], double[], double[]> op = //
				name("test.addArrays").inType(double[].class, double[].class).outType(double[].class).computer();
		op.compute(input1, input2, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a binary function using input values only. */
	@Test
	public void testArity2_IV_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double expected = 2.;
		final BiFunction<Double, Double, ?> op = //
				name("test.addDoubles").input(input1, input2).function();
		final Object actual = op.apply(input1, input2);
		assertEquals(actual, expected);
	}

	/** Matches a binary inplace1 using input types + output type. */
	@Test
	public void testArity2_IV_OU_matchI1() {
		final double[] actual1 = { 1, 2, 3 };
		final double[] actual2 = { 1, 2, 3 };
		final double[] expected = { 1, 4, 9 };
		final Inplaces.Arity2_1<double[], double[]> op = //
				name("test.mulArrays").input(actual1, actual2).inplace1();
		op.mutate(actual1, actual2);
		assertTrue(Arrays.equals(actual1, expected));
		assertFalse(Arrays.equals(actual2, expected));
	}

	/** Matches a binary inplace1 using input types + output type. */
	@Test
	public void testArity2_IV_OU_matchI2() {
		final double[] actual1 = { 1, 2, 3 };
		final double[] actual2 = { 1, 2, 3 };
		final double[] expected = { 1, 4, 9 };
		final Inplaces.Arity2_2<double[], double[]> op = //
				name("test.mulArrays").input(actual1, actual2).inplace2();
		op.mutate(actual1, actual2);
		assertTrue(Arrays.equals(actual2, expected));
		assertFalse(Arrays.equals(actual1, expected));
	}

	/** Runs a binary function using input values only. */
	@Test
	public void testArity2_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double expected = 2.;
		final Object actual = name("test.addDoubles").input(input1, input2).apply();
		assertEquals(actual, expected);
	}

	/** Matches a binary inplace1 using input types + output type. */
	@Test
	public void testArity2_IV_OU_runI1() {
		final double[] actual1 = { 1, 2, 3 };
		final double[] actual2 = { 1, 2, 3 };
		final double[] expected = { 1, 4, 9 };
		name("test.mulArrays").input(actual1, actual2).mutate1();
		assertTrue(Arrays.equals(actual1, expected));
		assertFalse(Arrays.equals(actual2, expected));
	}

	/** Matches a binary inplace1 using input types + output type. */
	@Test
	public void testArity2_IV_OU_runI2() {
		final double[] actual1 = { 1, 2, 3 };
		final double[] actual2 = { 1, 2, 3 };
		final double[] expected = { 1, 4, 9 };
		name("test.mulArrays").input(actual1, actual2).mutate2();
		assertTrue(Arrays.equals(actual2, expected));
		assertFalse(Arrays.equals(actual1, expected));
	}

	/** Matches a binary function using input value + output type. */
	@Test
	public void testArity2_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double expected = 2.;
		final BiFunction<Double, Double, Double> op = //
				name("test.addDoubles").input(input1, input2).outType(Double.class).function();
		final Double actual = op.apply(input1, input2);
		assertEquals(actual, expected);
	}

	/** Matches a binary computer using input value + output type. */
	@Test
	public void testArity2_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 2, 4, 6 };
		final Computers.Arity2<double[], double[], double[]> op = //
				name("test.addArrays").input(input1, input2).outType(double[].class).computer();
		op.compute(input1, input2, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a binary function using input values + output type. */
	@Test
	public void testArity2_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double expected = 2.;
		final Object actual = name("test.addDoubles").input(input1, input2).outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a binary computer using input value + output value. */
	@Test
	public void testArity2_IV_OV_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 2, 4, 6 };
		final Computers.Arity2<double[], double[], double[]> op = //
				name("test.addArrays").input(input1, input2).output(actual).computer();
		op.compute(input1, input2, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a binary computer using input value + output value. */
	@Test
	public void testArity2_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 2, 4, 6 };
		name("test.addArrays").input(input1, input2).output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- Helper methods --

	private OpBuilder name(String opName) {
		return new OpBuilder(ops, opName);
	}
}
