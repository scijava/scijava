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
import org.scijava.ops.function.Functions;
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

	/** Matches a 1-arity function using input types only. */
	@Test
	public void testArity1_IT_OU_matchF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Function<Double, ?> op = //
				name("test.addDoubles").inType(Double.class).function();
		final Object actual = op.apply(input);
		assertEquals(actual, expected);
	}

	/** Matches a 1-arity inplace1 with a given output type. */
	@Test
	public void testArity1_IT_OU_matchI1() {
		final double[] input = { 1, 2, 3 };
		final double[] expected = { 1.0, 2.0, 3.0 };
		final Inplaces.Arity1<double[]> op = //
				name("test.mulArrays").inType(double[].class).inplace();
		op.mutate(input);
		assertTrue(Arrays.equals(input, expected));
	}

	/** Matches a 1-arity function using input types + output type. */
	@Test
	public void testArity1_IT_OT_matchF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Function<Double, Double> op = //
				name("test.addDoubles").inType(Double.class).outType(Double.class).function();
		final double actual = op.apply(input);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 1-arity computer using input types + output type. */
	@Test
	public void testArity1_IT_OT_matchC() {
		final double[] input = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 1, 2, 3 };
		final Computers.Arity1<double[], double[]> op = //
				name("test.addArrays").inType(double[].class).outType(double[].class).computer();
		op.compute(input, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 1-arity function using input value only. */
	@Test
	public void testArity1_IV_OU_matchF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Function<Double, ?> op = //
				name("test.addDoubles").input(input).function();
		final Object actual = op.apply(input);
		assertEquals(actual, expected);
	}

	/** Matches a 1-arity inplace1 with a given output value. */
	@Test
	public void testArity1_IV_OU_matchI1() {
		final double[] input = { 1, 2, 3 };
		double[] expected = { 1.0, 2.0, 3.0 };
		final Inplaces.Arity1<double[]> op = //
				name("test.mulArrays").input(input).inplace();
		op.mutate(input);
		assertTrue(Arrays.equals(expected, input));
	}

	/** Runs a 1-arity function using input value only. */
	@Test
	public void testArity1_IV_OU_runF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Object actual = name("test.addDoubles").input(input).apply();
		assertEquals(actual, expected);
	}

	/** Runs a 1-arity inplace1 with a given output value. */
	@Test
	public void testArity1_IV_OU_runI1() {
		final double[] input = { 1, 2, 3 };
		final double[] expected = { 1.0, 2.0, 3.0 };
		name("test.mulArrays").input(input).mutate();
		assertTrue(Arrays.equals(input, expected));
	}

	/** Matches a 1-arity function using input value + output type. */
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
		final Computers.Arity1<double[], double[]> op = //
				name("test.addArrays").input(input).output(actual).computer();
		op.compute(input, actual);
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

	/** Matches a 2-arity function using input types only. */
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

	/** Matches a 2-arity inplace1 with a given output type. */
	@Test
	public void testArity2_IT_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4.0, 9.0 };
		final Inplaces.Arity2_1<double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class).inplace1();
		op.mutate(input1, input2);
		assertTrue(Arrays.equals(input1, expected));
	}
	/** Matches a 2-arity inplace2 with a given output type. */
	@Test
	public void testArity2_IT_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4.0, 9.0 };
		final Inplaces.Arity2_2<double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class).inplace2();
		op.mutate(input1, input2);
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Matches a 2-arity function using input types + output type. */
	@Test
	public void testArity2_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double expected = 2.;
		final BiFunction<Double, Double, Double> op = //
				name("test.addDoubles").inType(Double.class, Double.class).outType(Double.class).function();
		final double actual = op.apply(input1, input2);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 2-arity computer using input types + output type. */
	@Test
	public void testArity2_IT_OT_matchC() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 2, 4, 6 };
		final Computers.Arity2<double[], double[], double[]> op = //
				name("test.addArrays").inType(double[].class, double[].class).outType(double[].class).computer();
		op.compute(input1, input2, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 2-arity function using input value only. */
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

	/** Matches a 2-arity inplace1 with a given output value. */
	@Test
	public void testArity2_IV_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		double[] expected = { 1.0, 4.0, 9.0 };
		final Inplaces.Arity2_1<double[], double[]> op = //
				name("test.mulArrays").input(input1, input2).inplace1();
		op.mutate(input1, input2);
		assertTrue(Arrays.equals(expected, input1));
	}
	/** Matches a 2-arity inplace2 with a given output value. */
	@Test
	public void testArity2_IV_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		double[] expected = { 1.0, 4.0, 9.0 };
		final Inplaces.Arity2_2<double[], double[]> op = //
				name("test.mulArrays").input(input1, input2).inplace2();
		op.mutate(input1, input2);
		assertTrue(Arrays.equals(expected, input2));
	}

	/** Runs a 2-arity function using input value only. */
	@Test
	public void testArity2_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double expected = 2.;
		final Object actual = name("test.addDoubles").input(input1, input2).apply();
		assertEquals(actual, expected);
	}

	/** Runs a 2-arity inplace1 with a given output value. */
	@Test
	public void testArity2_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4.0, 9.0 };
		name("test.mulArrays").input(input1, input2).mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}
	/** Runs a 2-arity inplace2 with a given output value. */
	@Test
	public void testArity2_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4.0, 9.0 };
		name("test.mulArrays").input(input1, input2).mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Matches a 2-arity function using input value + output type. */
	@Test
	public void testArity2_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double expected = 2.;
		final BiFunction<Double, Double, Double> op = //
				name("test.addDoubles").input(input1, input2).outType(Double.class).function();
		final double actual = op.apply(input1, input2);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity2_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 2, 4, 6 };
		final Computers.Arity2<double[], double[], double[]> op = //
				name("test.addArrays").input(input1, input2).output(actual).computer();
		op.compute(input1, input2, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity2_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double expected = 2.;
		final Object actual = name("test.addDoubles").input(input1, input2).outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
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

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity2_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 2, 4, 6 };
		name("test.addArrays").input(input1, input2).output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- 3-ary --

	/** Matches a 3-arity function using input types only. */
	@Test
	public void testArity3_IT_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double expected = 3.;
		final Functions.Arity3<Double, Double, Double, ?> op = //
				name("test.addDoubles").inType(Double.class, Double.class, Double.class).function();
		final Object actual = op.apply(input1, input2, input3);
		assertEquals(actual, expected);
	}

	/** Matches a 3-arity inplace1 with a given output type. */
	@Test
	public void testArity3_IT_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8.0, 27.0 };
		final Inplaces.Arity3_1<double[], double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class, double[].class).inplace1();
		op.mutate(input1, input2, input3);
		assertTrue(Arrays.equals(input1, expected));
	}
	/** Matches a 3-arity inplace2 with a given output type. */
	@Test
	public void testArity3_IT_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8.0, 27.0 };
		final Inplaces.Arity3_2<double[], double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class, double[].class).inplace2();
		op.mutate(input1, input2, input3);
		assertTrue(Arrays.equals(input2, expected));
	}
	/** Matches a 3-arity inplace3 with a given output type. */
	@Test
	public void testArity3_IT_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8.0, 27.0 };
		final Inplaces.Arity3_3<double[], double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class, double[].class).inplace3();
		op.mutate(input1, input2, input3);
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Matches a 3-arity function using input types + output type. */
	@Test
	public void testArity3_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double expected = 3.;
		final Functions.Arity3<Double, Double, Double, Double> op = //
				name("test.addDoubles").inType(Double.class, Double.class, Double.class).outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 3-arity computer using input types + output type. */
	@Test
	public void testArity3_IT_OT_matchC() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 3, 6, 9 };
		final Computers.Arity3<double[], double[], double[], double[]> op = //
				name("test.addArrays").inType(double[].class, double[].class, double[].class).outType(double[].class).computer();
		op.compute(input1, input2, input3, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 3-arity function using input value only. */
	@Test
	public void testArity3_IV_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double expected = 3.;
		final Functions.Arity3<Double, Double, Double, ?> op = //
				name("test.addDoubles").input(input1, input2, input3).function();
		final Object actual = op.apply(input1, input2, input3);
		assertEquals(actual, expected);
	}

	/** Matches a 3-arity inplace1 with a given output value. */
	@Test
	public void testArity3_IV_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		double[] expected = { 1.0, 8.0, 27.0 };
		final Inplaces.Arity3_1<double[], double[], double[]> op = //
				name("test.mulArrays").input(input1, input2, input3).inplace1();
		op.mutate(input1, input2, input3);
		assertTrue(Arrays.equals(expected, input1));
	}
	/** Matches a 3-arity inplace2 with a given output value. */
	@Test
	public void testArity3_IV_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		double[] expected = { 1.0, 8.0, 27.0 };
		final Inplaces.Arity3_2<double[], double[], double[]> op = //
				name("test.mulArrays").input(input1, input2, input3).inplace2();
		op.mutate(input1, input2, input3);
		assertTrue(Arrays.equals(expected, input2));
	}
	/** Matches a 3-arity inplace3 with a given output value. */
	@Test
	public void testArity3_IV_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		double[] expected = { 1.0, 8.0, 27.0 };
		final Inplaces.Arity3_3<double[], double[], double[]> op = //
				name("test.mulArrays").input(input1, input2, input3).inplace3();
		op.mutate(input1, input2, input3);
		assertTrue(Arrays.equals(expected, input3));
	}

	/** Runs a 3-arity function using input value only. */
	@Test
	public void testArity3_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double expected = 3.;
		final Object actual = name("test.addDoubles").input(input1, input2, input3).apply();
		assertEquals(actual, expected);
	}

	/** Runs a 3-arity inplace1 with a given output value. */
	@Test
	public void testArity3_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8.0, 27.0 };
		name("test.mulArrays").input(input1, input2, input3).mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}
	/** Runs a 3-arity inplace2 with a given output value. */
	@Test
	public void testArity3_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8.0, 27.0 };
		name("test.mulArrays").input(input1, input2, input3).mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}
	/** Runs a 3-arity inplace3 with a given output value. */
	@Test
	public void testArity3_IV_OU_runI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8.0, 27.0 };
		name("test.mulArrays").input(input1, input2, input3).mutate3();
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Matches a 3-arity function using input value + output type. */
	@Test
	public void testArity3_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double expected = 3.;
		final Functions.Arity3<Double, Double, Double, Double> op = //
				name("test.addDoubles").input(input1, input2, input3).outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity3_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 3, 6, 9 };
		final Computers.Arity3<double[], double[], double[], double[]> op = //
				name("test.addArrays").input(input1, input2, input3).output(actual).computer();
		op.compute(input1, input2, input3, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity3_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double expected = 3.;
		final Object actual = name("test.addDoubles").input(input1, input2, input3).outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity3_IV_OV_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 3, 6, 9 };
		final Computers.Arity3<double[], double[], double[], double[]> op = //
				name("test.addArrays").input(input1, input2, input3).output(actual).computer();
		op.compute(input1, input2, input3, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity3_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 3, 6, 9 };
		name("test.addArrays").input(input1, input2, input3).output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- 4-ary --

	/** Matches a 4-arity function using input types only. */
	@Test
	public void testArity4_IT_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double expected = 4.;
		final Functions.Arity4<Double, Double, Double, Double, ?> op = //
				name("test.addDoubles").inType(Double.class, Double.class, Double.class, Double.class).function();
		final Object actual = op.apply(input1, input2, input3, input4);
		assertEquals(actual, expected);
	}

	/** Matches a 4-arity inplace1 with a given output type. */
	@Test
	public void testArity4_IT_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16.0, 81.0 };
		final Inplaces.Arity4_1<double[], double[], double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class, double[].class, double[].class).inplace1();
		op.mutate(input1, input2, input3, input4);
		assertTrue(Arrays.equals(input1, expected));
	}
	/** Matches a 4-arity inplace2 with a given output type. */
	@Test
	public void testArity4_IT_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16.0, 81.0 };
		final Inplaces.Arity4_2<double[], double[], double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class, double[].class, double[].class).inplace2();
		op.mutate(input1, input2, input3, input4);
		assertTrue(Arrays.equals(input2, expected));
	}
	/** Matches a 4-arity inplace3 with a given output type. */
	@Test
	public void testArity4_IT_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16.0, 81.0 };
		final Inplaces.Arity4_3<double[], double[], double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class, double[].class, double[].class).inplace3();
		op.mutate(input1, input2, input3, input4);
		assertTrue(Arrays.equals(input3, expected));
	}
	/** Matches a 4-arity inplace4 with a given output type. */
	@Test
	public void testArity4_IT_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16.0, 81.0 };
		final Inplaces.Arity4_4<double[], double[], double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class, double[].class, double[].class).inplace4();
		op.mutate(input1, input2, input3, input4);
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Matches a 4-arity function using input types + output type. */
	@Test
	public void testArity4_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double expected = 4.;
		final Functions.Arity4<Double, Double, Double, Double, Double> op = //
				name("test.addDoubles").inType(Double.class, Double.class, Double.class, Double.class).outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 4-arity computer using input types + output type. */
	@Test
	public void testArity4_IT_OT_matchC() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 4, 8, 12 };
		final Computers.Arity4<double[], double[], double[], double[], double[]> op = //
				name("test.addArrays").inType(double[].class, double[].class, double[].class, double[].class).outType(double[].class).computer();
		op.compute(input1, input2, input3, input4, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 4-arity function using input value only. */
	@Test
	public void testArity4_IV_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double expected = 4.;
		final Functions.Arity4<Double, Double, Double, Double, ?> op = //
				name("test.addDoubles").input(input1, input2, input3, input4).function();
		final Object actual = op.apply(input1, input2, input3, input4);
		assertEquals(actual, expected);
	}

	/** Matches a 4-arity inplace1 with a given output value. */
	@Test
	public void testArity4_IV_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		double[] expected = { 1.0, 16.0, 81.0 };
		final Inplaces.Arity4_1<double[], double[], double[], double[]> op = //
				name("test.mulArrays").input(input1, input2, input3, input4).inplace1();
		op.mutate(input1, input2, input3, input4);
		assertTrue(Arrays.equals(expected, input1));
	}
	/** Matches a 4-arity inplace2 with a given output value. */
	@Test
	public void testArity4_IV_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		double[] expected = { 1.0, 16.0, 81.0 };
		final Inplaces.Arity4_2<double[], double[], double[], double[]> op = //
				name("test.mulArrays").input(input1, input2, input3, input4).inplace2();
		op.mutate(input1, input2, input3, input4);
		assertTrue(Arrays.equals(expected, input2));
	}
	/** Matches a 4-arity inplace3 with a given output value. */
	@Test
	public void testArity4_IV_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		double[] expected = { 1.0, 16.0, 81.0 };
		final Inplaces.Arity4_3<double[], double[], double[], double[]> op = //
				name("test.mulArrays").input(input1, input2, input3, input4).inplace3();
		op.mutate(input1, input2, input3, input4);
		assertTrue(Arrays.equals(expected, input3));
	}
	/** Matches a 4-arity inplace4 with a given output value. */
	@Test
	public void testArity4_IV_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		double[] expected = { 1.0, 16.0, 81.0 };
		final Inplaces.Arity4_4<double[], double[], double[], double[]> op = //
				name("test.mulArrays").input(input1, input2, input3, input4).inplace4();
		op.mutate(input1, input2, input3, input4);
		assertTrue(Arrays.equals(expected, input4));
	}

	/** Runs a 4-arity function using input value only. */
	@Test
	public void testArity4_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double expected = 4.;
		final Object actual = name("test.addDoubles").input(input1, input2, input3, input4).apply();
		assertEquals(actual, expected);
	}

	/** Runs a 4-arity inplace1 with a given output value. */
	@Test
	public void testArity4_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16.0, 81.0 };
		name("test.mulArrays").input(input1, input2, input3, input4).mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}
	/** Runs a 4-arity inplace2 with a given output value. */
	@Test
	public void testArity4_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16.0, 81.0 };
		name("test.mulArrays").input(input1, input2, input3, input4).mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}
	/** Runs a 4-arity inplace3 with a given output value. */
	@Test
	public void testArity4_IV_OU_runI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16.0, 81.0 };
		name("test.mulArrays").input(input1, input2, input3, input4).mutate3();
		assertTrue(Arrays.equals(input3, expected));
	}
	/** Runs a 4-arity inplace4 with a given output value. */
	@Test
	public void testArity4_IV_OU_runI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16.0, 81.0 };
		name("test.mulArrays").input(input1, input2, input3, input4).mutate4();
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Matches a 4-arity function using input value + output type. */
	@Test
	public void testArity4_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double expected = 4.;
		final Functions.Arity4<Double, Double, Double, Double, Double> op = //
				name("test.addDoubles").input(input1, input2, input3, input4).outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity4_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 4, 8, 12 };
		final Computers.Arity4<double[], double[], double[], double[], double[]> op = //
				name("test.addArrays").input(input1, input2, input3, input4).output(actual).computer();
		op.compute(input1, input2, input3, input4, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity4_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double expected = 4.;
		final Object actual = name("test.addDoubles").input(input1, input2, input3, input4).outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity4_IV_OV_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 4, 8, 12 };
		final Computers.Arity4<double[], double[], double[], double[], double[]> op = //
				name("test.addArrays").input(input1, input2, input3, input4).output(actual).computer();
		op.compute(input1, input2, input3, input4, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity4_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 4, 8, 12 };
		name("test.addArrays").input(input1, input2, input3, input4).output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- 5-ary --

	/** Matches a 5-arity function using input types only. */
	@Test
	public void testArity5_IT_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double expected = 5.;
		final Functions.Arity5<Double, Double, Double, Double, Double, ?> op = //
				name("test.addDoubles").inType(Double.class, Double.class, Double.class, Double.class, Double.class).function();
		final Object actual = op.apply(input1, input2, input3, input4, input5);
		assertEquals(actual, expected);
	}

	/** Matches a 5-arity inplace1 with a given output type. */
	@Test
	public void testArity5_IT_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32.0, 243.0 };
		final Inplaces.Arity5_1<double[], double[], double[], double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class, double[].class, double[].class, double[].class).inplace1();
		op.mutate(input1, input2, input3, input4, input5);
		assertTrue(Arrays.equals(input1, expected));
	}
	/** Matches a 5-arity inplace2 with a given output type. */
	@Test
	public void testArity5_IT_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32.0, 243.0 };
		final Inplaces.Arity5_2<double[], double[], double[], double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class, double[].class, double[].class, double[].class).inplace2();
		op.mutate(input1, input2, input3, input4, input5);
		assertTrue(Arrays.equals(input2, expected));
	}
	/** Matches a 5-arity inplace3 with a given output type. */
	@Test
	public void testArity5_IT_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32.0, 243.0 };
		final Inplaces.Arity5_3<double[], double[], double[], double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class, double[].class, double[].class, double[].class).inplace3();
		op.mutate(input1, input2, input3, input4, input5);
		assertTrue(Arrays.equals(input3, expected));
	}
	/** Matches a 5-arity inplace4 with a given output type. */
	@Test
	public void testArity5_IT_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32.0, 243.0 };
		final Inplaces.Arity5_4<double[], double[], double[], double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class, double[].class, double[].class, double[].class).inplace4();
		op.mutate(input1, input2, input3, input4, input5);
		assertTrue(Arrays.equals(input4, expected));
	}
	/** Matches a 5-arity inplace5 with a given output type. */
	@Test
	public void testArity5_IT_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32.0, 243.0 };
		final Inplaces.Arity5_5<double[], double[], double[], double[], double[]> op = //
				name("test.mulArrays").inType(double[].class, double[].class, double[].class, double[].class, double[].class).inplace5();
		op.mutate(input1, input2, input3, input4, input5);
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Matches a 5-arity function using input types + output type. */
	@Test
	public void testArity5_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double expected = 5.;
		final Functions.Arity5<Double, Double, Double, Double, Double, Double> op = //
				name("test.addDoubles").inType(Double.class, Double.class, Double.class, Double.class, Double.class).outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 5-arity computer using input types + output type. */
	@Test
	public void testArity5_IT_OT_matchC() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 5, 10, 15 };
		final Computers.Arity5<double[], double[], double[], double[], double[], double[]> op = //
				name("test.addArrays").inType(double[].class, double[].class, double[].class, double[].class, double[].class).outType(double[].class).computer();
		op.compute(input1, input2, input3, input4, input5, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 5-arity function using input value only. */
	@Test
	public void testArity5_IV_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double expected = 5.;
		final Functions.Arity5<Double, Double, Double, Double, Double, ?> op = //
				name("test.addDoubles").input(input1, input2, input3, input4, input5).function();
		final Object actual = op.apply(input1, input2, input3, input4, input5);
		assertEquals(actual, expected);
	}

	/** Matches a 5-arity inplace1 with a given output value. */
	@Test
	public void testArity5_IV_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		double[] expected = { 1.0, 32.0, 243.0 };
		final Inplaces.Arity5_1<double[], double[], double[], double[], double[]> op = //
				name("test.mulArrays").input(input1, input2, input3, input4, input5).inplace1();
		op.mutate(input1, input2, input3, input4, input5);
		assertTrue(Arrays.equals(expected, input1));
	}
	/** Matches a 5-arity inplace2 with a given output value. */
	@Test
	public void testArity5_IV_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		double[] expected = { 1.0, 32.0, 243.0 };
		final Inplaces.Arity5_2<double[], double[], double[], double[], double[]> op = //
				name("test.mulArrays").input(input1, input2, input3, input4, input5).inplace2();
		op.mutate(input1, input2, input3, input4, input5);
		assertTrue(Arrays.equals(expected, input2));
	}
	/** Matches a 5-arity inplace3 with a given output value. */
	@Test
	public void testArity5_IV_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		double[] expected = { 1.0, 32.0, 243.0 };
		final Inplaces.Arity5_3<double[], double[], double[], double[], double[]> op = //
				name("test.mulArrays").input(input1, input2, input3, input4, input5).inplace3();
		op.mutate(input1, input2, input3, input4, input5);
		assertTrue(Arrays.equals(expected, input3));
	}
	/** Matches a 5-arity inplace4 with a given output value. */
	@Test
	public void testArity5_IV_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		double[] expected = { 1.0, 32.0, 243.0 };
		final Inplaces.Arity5_4<double[], double[], double[], double[], double[]> op = //
				name("test.mulArrays").input(input1, input2, input3, input4, input5).inplace4();
		op.mutate(input1, input2, input3, input4, input5);
		assertTrue(Arrays.equals(expected, input4));
	}
	/** Matches a 5-arity inplace5 with a given output value. */
	@Test
	public void testArity5_IV_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		double[] expected = { 1.0, 32.0, 243.0 };
		final Inplaces.Arity5_5<double[], double[], double[], double[], double[]> op = //
				name("test.mulArrays").input(input1, input2, input3, input4, input5).inplace5();
		op.mutate(input1, input2, input3, input4, input5);
		assertTrue(Arrays.equals(expected, input5));
	}

	/** Runs a 5-arity function using input value only. */
	@Test
	public void testArity5_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double expected = 5.;
		final Object actual = name("test.addDoubles").input(input1, input2, input3, input4, input5).apply();
		assertEquals(actual, expected);
	}

	/** Runs a 5-arity inplace1 with a given output value. */
	@Test
	public void testArity5_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32.0, 243.0 };
		name("test.mulArrays").input(input1, input2, input3, input4, input5).mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}
	/** Runs a 5-arity inplace2 with a given output value. */
	@Test
	public void testArity5_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32.0, 243.0 };
		name("test.mulArrays").input(input1, input2, input3, input4, input5).mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}
	/** Runs a 5-arity inplace3 with a given output value. */
	@Test
	public void testArity5_IV_OU_runI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32.0, 243.0 };
		name("test.mulArrays").input(input1, input2, input3, input4, input5).mutate3();
		assertTrue(Arrays.equals(input3, expected));
	}
	/** Runs a 5-arity inplace4 with a given output value. */
	@Test
	public void testArity5_IV_OU_runI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32.0, 243.0 };
		name("test.mulArrays").input(input1, input2, input3, input4, input5).mutate4();
		assertTrue(Arrays.equals(input4, expected));
	}
	/** Runs a 5-arity inplace5 with a given output value. */
	@Test
	public void testArity5_IV_OU_runI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32.0, 243.0 };
		name("test.mulArrays").input(input1, input2, input3, input4, input5).mutate5();
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Matches a 5-arity function using input value + output type. */
	@Test
	public void testArity5_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double expected = 5.;
		final Functions.Arity5<Double, Double, Double, Double, Double, Double> op = //
				name("test.addDoubles").input(input1, input2, input3, input4, input5).outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity5_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 5, 10, 15 };
		final Computers.Arity5<double[], double[], double[], double[], double[], double[]> op = //
				name("test.addArrays").input(input1, input2, input3, input4, input5).output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity5_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double expected = 5.;
		final Object actual = name("test.addDoubles").input(input1, input2, input3, input4, input5).outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity5_IV_OV_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 5, 10, 15 };
		final Computers.Arity5<double[], double[], double[], double[], double[], double[]> op = //
				name("test.addArrays").input(input1, input2, input3, input4, input5).output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity5_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 5, 10, 15 };
		name("test.addArrays").input(input1, input2, input3, input4, input5).output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- Helper methods --

	private OpBuilder name(String opName) {
		return new OpBuilder(ops, opName);
	}
}
