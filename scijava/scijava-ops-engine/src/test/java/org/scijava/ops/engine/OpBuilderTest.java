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

package org.scijava.ops.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.function.Producer;
import org.scijava.ops.api.OpBuilder;

/**
 * Tests {@link OpBuilder}. For each arity, we test the following matches and
 * run the following commands based on the information scenarios.
 * <p>
 * Input TYPES are given (IT):
 * <p>
 * <ol>
 * <li>The output is unspecified (OU):</li>
 * <ol type="a">
 * <li>match: Function, Inplace</li>
 * <li>run: none</li>
 * </ol>
 * <li>The output type is given (OT):</li>
 * <ol type="a">
 * <li>match: Function, Computer</li>
 * <li>run: none</li>
 * </ol>
 * </ol>
 * Input VALUES are given (IV) (N.B. this case applies for Arity0):
 * <p>
 * <ol>
 * <li>The output is unspecified (OU):</li>
 * <ol type="a">
 * <li>match: Function, Inplace</li>
 * <li>run: apply, mutate</li>
 * </ol>
 * <li>The output type is given (OT):</li>
 * <ol type="a">
 * <li>match: Function, Computer</li>
 * <li>run: apply</li>
 * </ol>
 * <li>The output value is given (OV):</li>
 * <ol type="a">
 * <li>match: Computer</li>
 * <li>run: compute</li>
 * </ol>
 * </ol>
 * 
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public class OpBuilderTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void AddNeededOps() {
		 ops.register(new OpBuilderTestOps());
	}

	final double[] halves = new double[10];
	{
		for (int i = 0; i < halves.length; i++)
			halves[i] = i / 2.;
	}

	// -- 0-ary --

	/** Matches a nullary function in a vacuum. */
	@Test
	public void testArity0_OU_matchF() {
		final Producer<?> op = name("test.addDoubles").arity0().producer();
		final Object result = op.create();
		assertEquals(0., result);
	}

	/** Runs a nullary function in a vacuum. */
	@Test
	public void testArity0_OU_runF() {
		final Object result = name("test.addDoubles").arity0().create();
		assertEquals(0., result);
	}

	/** Matches a nullary function with a given output type. */
	@Test
	public void testArity0_OT_matchF() {
		final Double expected = 0.;
		final Producer<Double> op = //
			name("test.addDoubles").arity0().outType(Double.class).producer();
		final Object result = op.create();
		assertEquals(result, expected);
	}

	/** Matches a nullary computer with a given output type. */
	@Test
	public void testArity0_OT_matchC() {
		final double[] actual = { 1, 2, 3 };
		final double[] expected = { 0, 0, 0 };
		final Computers.Arity0<double[]> op = //
			name("test.addArrays").arity0().outType(double[].class).computer();
		op.compute(actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a nullary function with a given output type. */
	@Test
	public void testArity0_OT_runF() {
		final Double result = name("test.addDoubles").arity0().outType(Double.class)
			.create();
		assert (0. == result);
	}

	/** Matches a nullary computer with a given output value. */
	@Test
	public void testArity0_OV_matchC() {
		double[] result = { 1, 2, 3 };
		double[] expected = { 0, 0, 0 };
		final Computers.Arity0<double[]> op = //
			name("test.addArrays").arity0().output(result).computer();
		op.compute(result);
		assertTrue(Arrays.equals(expected, result));
	}

	/** Runs a nullary computer with a given output value. */
	@Test
	public void testArity0_OV_runC() {
		double[] result = { 1, 2, 3 };
		double[] expected = { 0, 0, 0 };
		name("test.addArrays").arity0().output(result).compute();
		assertTrue(Arrays.equals(expected, result));
	}

	// -- 1-ary --

	/** Matches a 1-arity function using input types only. */
	@Test
	public void testArity1_IT_OU_matchF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Function<Double, ?> op = //
			name("test.addDoubles") //
				.arity1() //
				.inType(Double.class) //
				.function();
		final Object actual = op.apply(input);
		assertEquals(actual, expected);
	}

	/** Matches a 1-arity inplace1 with a given output type. */
	@Test
	public void testArity1_IT_OU_matchI1() {
		final double[] input = { 1, 2, 3 };
		final double[] expected = { 1.0, 2.0, 3.0 };
		final Inplaces.Arity1<double[]> op = //
			name("test.mulArrays1_1") //
				.arity1() //
				.inType(double[].class) //
				.inplace();
		op.mutate(input);
		assertTrue(Arrays.equals(input, expected));
	}

	/** Matches a 1-arity function using input types + output type. */
	@Test
	public void testArity1_IT_OT_matchF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Function<Double, Double> op = //
			name("test.addDoubles").arity1().inType(Double.class) //
				.outType(Double.class).function();
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
			name("test.addArrays") //
				.arity1() //
				.inType(double[].class) //
				.outType(double[].class).computer();
		op.compute(input, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 1-arity function using input value only. */
	@Test
	public void testArity1_IV_OU_matchF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Function<Double, ?> op = //
			name("test.addDoubles") //
				.arity1() //
				.input(input) //
				.function();
		final Object actual = op.apply(input);
		assertEquals(actual, expected);
	}

	/** Matches a 1-arity inplace1 with a given output value. */
	@Test
	public void testArity1_IV_OU_matchI1() {
		final double[] input = { 1, 2, 3 };
		double[] expected = { 1.0, 2.0, 3.0 };
		final Inplaces.Arity1<double[]> op = //
			name("test.mulArrays1_1").arity1().input(input) //
				.inplace();
		op.mutate(input);
		assertTrue(Arrays.equals(expected, input));
	}

	/** Runs a 1-arity function using input value only. */
	@Test
	public void testArity1_IV_OU_runF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Object actual = name("test.addDoubles") //
			.arity1() //
			.input(input) //
			.apply();
		assertEquals(actual, expected);
	}

	/** Runs a 1-arity inplace1 with a given output value. */
	@Test
	public void testArity1_IV_OU_runI1() {
		final double[] input = { 1, 2, 3 };
		final double[] expected = { 1.0, 2.0, 3.0 };
		name("test.mulArrays1_1") //
			.arity1() //
			.input(input) //
			.mutate();
		assertTrue(Arrays.equals(input, expected));
	}

	/** Matches a 1-arity function using input value + output type. */
	@Test
	public void testArity1_IV_OT_matchF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Function<Double, Double> op = //
			name("test.addDoubles").arity1().input(input) //
				.outType(Double.class).function();
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
			name("test.addArrays") //
				.arity1() //
				.input(input) //
				.output(actual).computer();
		op.compute(input, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity1_IV_OT_runF() {
		final Double input = 1.;
		final Double expected = 1.;
		final Object actual = name("test.addDoubles") //
			.arity1() //
			.input(input) //
			.outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity1_IV_OV_matchC() {
		double[] input = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 1, 2, 3 };
		final Computers.Arity1<double[], double[]> op = //
			name("test.addArrays") //
				.arity1() //
				.input(input) //
				.output(actual).computer();
		op.compute(input, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity1_IV_OV_runC() {
		double[] input = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 1, 2, 3 };
		name("test.addArrays") //
			.arity1() //
			.input(input) //
			.output(actual).compute();
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
			name("test.addDoubles") //
				.arity2() //
				.inType(Double.class, Double.class) //
				.function();
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
			name("test.mulArrays2_1") //
				.arity2() //
				.inType(double[].class, double[].class) //
				.inplace1();
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
			name("test.mulArrays2_2") //
				.arity2() //
				.inType(double[].class, double[].class) //
				.inplace2();
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
			name("test.addDoubles").arity2().inType(Double.class, Double.class) //
				.outType(Double.class).function();
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
			name("test.addArrays") //
				.arity2() //
				.inType(double[].class, double[].class) //
				.outType(double[].class).computer();
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
			name("test.addDoubles") //
				.arity2() //
				.input(input1, input2) //
				.function();
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
			name("test.mulArrays2_1").arity2().input(input1, input2) //
				.inplace1();
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
			name("test.mulArrays2_2").arity2().input(input1, input2) //
				.inplace2();
		op.mutate(input1, input2);
		assertTrue(Arrays.equals(expected, input2));
	}

	/** Runs a 2-arity function using input value only. */
	@Test
	public void testArity2_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double expected = 2.;
		final Object actual = name("test.addDoubles") //
			.arity2() //
			.input(input1, input2) //
			.apply();
		assertEquals(actual, expected);
	}

	/** Runs a 2-arity inplace1 with a given output value. */
	@Test
	public void testArity2_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4.0, 9.0 };
		name("test.mulArrays2_1") //
			.arity2() //
			.input(input1, input2) //
			.mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Runs a 2-arity inplace2 with a given output value. */
	@Test
	public void testArity2_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4.0, 9.0 };
		name("test.mulArrays2_2") //
			.arity2() //
			.input(input1, input2) //
			.mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Matches a 2-arity function using input value + output type. */
	@Test
	public void testArity2_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double expected = 2.;
		final BiFunction<Double, Double, Double> op = //
			name("test.addDoubles").arity2().input(input1, input2) //
				.outType(Double.class).function();
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
			name("test.addArrays") //
				.arity2() //
				.input(input1, input2) //
				.output(actual).computer();
		op.compute(input1, input2, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity2_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double expected = 2.;
		final Object actual = name("test.addDoubles") //
			.arity2() //
			.input(input1, input2) //
			.outType(Double.class).apply();
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
			name("test.addArrays") //
				.arity2() //
				.input(input1, input2) //
				.output(actual).computer();
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
		name("test.addArrays") //
			.arity2() //
			.input(input1, input2) //
			.output(actual).compute();
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
			name("test.addDoubles") //
				.arity3() //
				.inType(Double.class, Double.class, Double.class) //
				.function();
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
			name("test.mulArrays3_1") //
				.arity3() //
				.inType(double[].class, double[].class, double[].class) //
				.inplace1();
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
			name("test.mulArrays3_2") //
				.arity3() //
				.inType(double[].class, double[].class, double[].class) //
				.inplace2();
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
			name("test.mulArrays3_3") //
				.arity3() //
				.inType(double[].class, double[].class, double[].class) //
				.inplace3();
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
			name("test.addDoubles").arity3().inType(Double.class, Double.class, Double.class) //
				.outType(Double.class).function();
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
			name("test.addArrays") //
				.arity3() //
				.inType(double[].class, double[].class, double[].class) //
				.outType(double[].class).computer();
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
			name("test.addDoubles") //
				.arity3() //
				.input(input1, input2, input3) //
				.function();
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
			name("test.mulArrays3_1").arity3().input(input1, input2, input3) //
				.inplace1();
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
			name("test.mulArrays3_2").arity3().input(input1, input2, input3) //
				.inplace2();
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
			name("test.mulArrays3_3").arity3().input(input1, input2, input3) //
				.inplace3();
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
		final Object actual = name("test.addDoubles") //
			.arity3() //
			.input(input1, input2, input3) //
			.apply();
		assertEquals(actual, expected);
	}

	/** Runs a 3-arity inplace1 with a given output value. */
	@Test
	public void testArity3_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8.0, 27.0 };
		name("test.mulArrays3_1") //
			.arity3() //
			.input(input1, input2, input3) //
			.mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Runs a 3-arity inplace2 with a given output value. */
	@Test
	public void testArity3_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8.0, 27.0 };
		name("test.mulArrays3_2") //
			.arity3() //
			.input(input1, input2, input3) //
			.mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Runs a 3-arity inplace3 with a given output value. */
	@Test
	public void testArity3_IV_OU_runI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8.0, 27.0 };
		name("test.mulArrays3_3") //
			.arity3() //
			.input(input1, input2, input3) //
			.mutate3();
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
			name("test.addDoubles").arity3().input(input1, input2, input3) //
				.outType(Double.class).function();
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
			name("test.addArrays") //
				.arity3() //
				.input(input1, input2, input3) //
				.output(actual).computer();
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
		final Object actual = name("test.addDoubles") //
			.arity3() //
			.input(input1, input2, input3) //
			.outType(Double.class).apply();
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
			name("test.addArrays") //
				.arity3() //
				.input(input1, input2, input3) //
				.output(actual).computer();
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
		name("test.addArrays") //
			.arity3() //
			.input(input1, input2, input3) //
			.output(actual).compute();
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
			name("test.addDoubles") //
				.arity4() //
				.inType(Double.class, Double.class, Double.class, Double.class) //
				.function();
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
			name("test.mulArrays4_1") //
				.arity4() //
				.inType(double[].class, double[].class, double[].class, double[].class) //
				.inplace1();
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
			name("test.mulArrays4_2") //
				.arity4() //
				.inType(double[].class, double[].class, double[].class, double[].class) //
				.inplace2();
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
			name("test.mulArrays4_3") //
				.arity4() //
				.inType(double[].class, double[].class, double[].class, double[].class) //
				.inplace3();
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
			name("test.mulArrays4_4") //
				.arity4() //
				.inType(double[].class, double[].class, double[].class, double[].class) //
				.inplace4();
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
			name("test.addDoubles").arity4().inType(Double.class, Double.class, Double.class, Double.class) //
				.outType(Double.class).function();
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
			name("test.addArrays") //
				.arity4() //
				.inType(double[].class, double[].class, double[].class, double[].class) //
				.outType(double[].class).computer();
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
			name("test.addDoubles") //
				.arity4() //
				.input(input1, input2, input3, input4) //
				.function();
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
			name("test.mulArrays4_1").arity4().input(input1, input2, input3, input4) //
				.inplace1();
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
			name("test.mulArrays4_2").arity4().input(input1, input2, input3, input4) //
				.inplace2();
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
			name("test.mulArrays4_3").arity4().input(input1, input2, input3, input4) //
				.inplace3();
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
			name("test.mulArrays4_4").arity4().input(input1, input2, input3, input4) //
				.inplace4();
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
		final Object actual = name("test.addDoubles") //
			.arity4() //
			.input(input1, input2, input3, input4) //
			.apply();
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
		name("test.mulArrays4_1") //
			.arity4() //
			.input(input1, input2, input3, input4) //
			.mutate1();
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
		name("test.mulArrays4_2") //
			.arity4() //
			.input(input1, input2, input3, input4) //
			.mutate2();
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
		name("test.mulArrays4_3") //
			.arity4() //
			.input(input1, input2, input3, input4) //
			.mutate3();
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
		name("test.mulArrays4_4") //
			.arity4() //
			.input(input1, input2, input3, input4) //
			.mutate4();
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
			name("test.addDoubles").arity4().input(input1, input2, input3, input4) //
				.outType(Double.class).function();
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
			name("test.addArrays") //
				.arity4() //
				.input(input1, input2, input3, input4) //
				.output(actual).computer();
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
		final Object actual = name("test.addDoubles") //
			.arity4() //
			.input(input1, input2, input3, input4) //
			.outType(Double.class).apply();
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
			name("test.addArrays") //
				.arity4() //
				.input(input1, input2, input3, input4) //
				.output(actual).computer();
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
		name("test.addArrays") //
			.arity4() //
			.input(input1, input2, input3, input4) //
			.output(actual).compute();
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
			name("test.addDoubles") //
				.arity5() //
				.inType(Double.class, Double.class, Double.class, Double.class, Double.class) //
				.function();
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
			name("test.mulArrays5_1") //
				.arity5() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace1();
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
			name("test.mulArrays5_2") //
				.arity5() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace2();
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
			name("test.mulArrays5_3") //
				.arity5() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace3();
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
			name("test.mulArrays5_4") //
				.arity5() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace4();
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
			name("test.mulArrays5_5") //
				.arity5() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace5();
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
			name("test.addDoubles").arity5().inType(Double.class, Double.class, Double.class, Double.class, Double.class) //
				.outType(Double.class).function();
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
			name("test.addArrays") //
				.arity5() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class) //
				.outType(double[].class).computer();
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
			name("test.addDoubles") //
				.arity5() //
				.input(input1, input2, input3, input4, input5) //
				.function();
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
			name("test.mulArrays5_1").arity5().input(input1, input2, input3, input4, input5) //
				.inplace1();
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
			name("test.mulArrays5_2").arity5().input(input1, input2, input3, input4, input5) //
				.inplace2();
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
			name("test.mulArrays5_3").arity5().input(input1, input2, input3, input4, input5) //
				.inplace3();
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
			name("test.mulArrays5_4").arity5().input(input1, input2, input3, input4, input5) //
				.inplace4();
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
			name("test.mulArrays5_5").arity5().input(input1, input2, input3, input4, input5) //
				.inplace5();
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
		final Object actual = name("test.addDoubles") //
			.arity5() //
			.input(input1, input2, input3, input4, input5) //
			.apply();
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
		name("test.mulArrays5_1") //
			.arity5() //
			.input(input1, input2, input3, input4, input5) //
			.mutate1();
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
		name("test.mulArrays5_2") //
			.arity5() //
			.input(input1, input2, input3, input4, input5) //
			.mutate2();
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
		name("test.mulArrays5_3") //
			.arity5() //
			.input(input1, input2, input3, input4, input5) //
			.mutate3();
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
		name("test.mulArrays5_4") //
			.arity5() //
			.input(input1, input2, input3, input4, input5) //
			.mutate4();
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
		name("test.mulArrays5_5") //
			.arity5() //
			.input(input1, input2, input3, input4, input5) //
			.mutate5();
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
			name("test.addDoubles").arity5().input(input1, input2, input3, input4, input5) //
				.outType(Double.class).function();
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
			name("test.addArrays") //
				.arity5() //
				.input(input1, input2, input3, input4, input5) //
				.output(actual).computer();
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
		final Object actual = name("test.addDoubles") //
			.arity5() //
			.input(input1, input2, input3, input4, input5) //
			.outType(Double.class).apply();
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
			name("test.addArrays") //
				.arity5() //
				.input(input1, input2, input3, input4, input5) //
				.output(actual).computer();
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
		name("test.addArrays") //
			.arity5() //
			.input(input1, input2, input3, input4, input5) //
			.output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- 6-ary --

	/** Matches a 6-arity function using input types only. */
	@Test
	public void testArity6_IT_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double expected = 6.;
		final Functions.Arity6<Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity6() //
				.inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6);
		assertEquals(actual, expected);
	}

	/** Matches a 6-arity inplace1 with a given output type. */
	@Test
	public void testArity6_IT_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] expected = { 1.0, 64.0, 729.0 };
		final Inplaces.Arity6_1<double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays6_1") //
				.arity6() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6);
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Matches a 6-arity inplace2 with a given output type. */
	@Test
	public void testArity6_IT_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] expected = { 1.0, 64.0, 729.0 };
		final Inplaces.Arity6_2<double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays6_2") //
				.arity6() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6);
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Matches a 6-arity inplace3 with a given output type. */
	@Test
	public void testArity6_IT_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] expected = { 1.0, 64.0, 729.0 };
		final Inplaces.Arity6_3<double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays6_3") //
				.arity6() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6);
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Matches a 6-arity inplace4 with a given output type. */
	@Test
	public void testArity6_IT_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] expected = { 1.0, 64.0, 729.0 };
		final Inplaces.Arity6_4<double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays6_4") //
				.arity6() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6);
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Matches a 6-arity inplace5 with a given output type. */
	@Test
	public void testArity6_IT_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] expected = { 1.0, 64.0, 729.0 };
		final Inplaces.Arity6_5<double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays6_5") //
				.arity6() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6);
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Matches a 6-arity inplace6 with a given output type. */
	@Test
	public void testArity6_IT_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] expected = { 1.0, 64.0, 729.0 };
		final Inplaces.Arity6_6<double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays6_6") //
				.arity6() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6);
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Matches a 6-arity function using input types + output type. */
	@Test
	public void testArity6_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double expected = 6.;
		final Functions.Arity6<Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity6().inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 6-arity computer using input types + output type. */
	@Test
	public void testArity6_IT_OT_matchC() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 6, 12, 18 };
		final Computers.Arity6<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity6() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.outType(double[].class).computer();
		op.compute(input1, input2, input3, input4, input5, input6, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 6-arity function using input value only. */
	@Test
	public void testArity6_IV_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double expected = 6.;
		final Functions.Arity6<Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity6() //
				.input(input1, input2, input3, input4, input5, input6) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6);
		assertEquals(actual, expected);
	}

	/** Matches a 6-arity inplace1 with a given output value. */
	@Test
	public void testArity6_IV_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		double[] expected = { 1.0, 64.0, 729.0 };
		final Inplaces.Arity6_1<double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays6_1").arity6().input(input1, input2, input3, input4, input5, input6) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6);
		assertTrue(Arrays.equals(expected, input1));
	}

	/** Matches a 6-arity inplace2 with a given output value. */
	@Test
	public void testArity6_IV_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		double[] expected = { 1.0, 64.0, 729.0 };
		final Inplaces.Arity6_2<double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays6_2").arity6().input(input1, input2, input3, input4, input5, input6) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6);
		assertTrue(Arrays.equals(expected, input2));
	}

	/** Matches a 6-arity inplace3 with a given output value. */
	@Test
	public void testArity6_IV_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		double[] expected = { 1.0, 64.0, 729.0 };
		final Inplaces.Arity6_3<double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays6_3").arity6().input(input1, input2, input3, input4, input5, input6) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6);
		assertTrue(Arrays.equals(expected, input3));
	}

	/** Matches a 6-arity inplace4 with a given output value. */
	@Test
	public void testArity6_IV_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		double[] expected = { 1.0, 64.0, 729.0 };
		final Inplaces.Arity6_4<double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays6_4").arity6().input(input1, input2, input3, input4, input5, input6) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6);
		assertTrue(Arrays.equals(expected, input4));
	}

	/** Matches a 6-arity inplace5 with a given output value. */
	@Test
	public void testArity6_IV_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		double[] expected = { 1.0, 64.0, 729.0 };
		final Inplaces.Arity6_5<double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays6_5").arity6().input(input1, input2, input3, input4, input5, input6) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6);
		assertTrue(Arrays.equals(expected, input5));
	}

	/** Matches a 6-arity inplace6 with a given output value. */
	@Test
	public void testArity6_IV_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		double[] expected = { 1.0, 64.0, 729.0 };
		final Inplaces.Arity6_6<double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays6_6").arity6().input(input1, input2, input3, input4, input5, input6) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6);
		assertTrue(Arrays.equals(expected, input6));
	}

	/** Runs a 6-arity function using input value only. */
	@Test
	public void testArity6_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double expected = 6.;
		final Object actual = name("test.addDoubles") //
			.arity6() //
			.input(input1, input2, input3, input4, input5, input6) //
			.apply();
		assertEquals(actual, expected);
	}

	/** Runs a 6-arity inplace1 with a given output value. */
	@Test
	public void testArity6_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] expected = { 1.0, 64.0, 729.0 };
		name("test.mulArrays6_1") //
			.arity6() //
			.input(input1, input2, input3, input4, input5, input6) //
			.mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Runs a 6-arity inplace2 with a given output value. */
	@Test
	public void testArity6_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] expected = { 1.0, 64.0, 729.0 };
		name("test.mulArrays6_2") //
			.arity6() //
			.input(input1, input2, input3, input4, input5, input6) //
			.mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Runs a 6-arity inplace3 with a given output value. */
	@Test
	public void testArity6_IV_OU_runI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] expected = { 1.0, 64.0, 729.0 };
		name("test.mulArrays6_3") //
			.arity6() //
			.input(input1, input2, input3, input4, input5, input6) //
			.mutate3();
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Runs a 6-arity inplace4 with a given output value. */
	@Test
	public void testArity6_IV_OU_runI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] expected = { 1.0, 64.0, 729.0 };
		name("test.mulArrays6_4") //
			.arity6() //
			.input(input1, input2, input3, input4, input5, input6) //
			.mutate4();
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Runs a 6-arity inplace5 with a given output value. */
	@Test
	public void testArity6_IV_OU_runI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] expected = { 1.0, 64.0, 729.0 };
		name("test.mulArrays6_5") //
			.arity6() //
			.input(input1, input2, input3, input4, input5, input6) //
			.mutate5();
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Runs a 6-arity inplace6 with a given output value. */
	@Test
	public void testArity6_IV_OU_runI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] expected = { 1.0, 64.0, 729.0 };
		name("test.mulArrays6_6") //
			.arity6() //
			.input(input1, input2, input3, input4, input5, input6) //
			.mutate6();
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Matches a 6-arity function using input value + output type. */
	@Test
	public void testArity6_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double expected = 6.;
		final Functions.Arity6<Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity6().input(input1, input2, input3, input4, input5, input6) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity6_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 6, 12, 18 };
		final Computers.Arity6<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity6() //
				.input(input1, input2, input3, input4, input5, input6) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity6_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double expected = 6.;
		final Object actual = name("test.addDoubles") //
			.arity6() //
			.input(input1, input2, input3, input4, input5, input6) //
			.outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity6_IV_OV_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 6, 12, 18 };
		final Computers.Arity6<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity6() //
				.input(input1, input2, input3, input4, input5, input6) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity6_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 6, 12, 18 };
		name("test.addArrays") //
			.arity6() //
			.input(input1, input2, input3, input4, input5, input6) //
			.output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- 7-ary --

	/** Matches a 7-arity function using input types only. */
	@Test
	public void testArity7_IT_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double expected = 7.;
		final Functions.Arity7<Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity7() //
				.inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7);
		assertEquals(actual, expected);
	}

	/** Matches a 7-arity inplace1 with a given output type. */
	@Test
	public void testArity7_IT_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] expected = { 1.0, 128.0, 2187.0 };
		final Inplaces.Arity7_1<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays7_1") //
				.arity7() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7);
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Matches a 7-arity inplace2 with a given output type. */
	@Test
	public void testArity7_IT_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] expected = { 1.0, 128.0, 2187.0 };
		final Inplaces.Arity7_2<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays7_2") //
				.arity7() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7);
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Matches a 7-arity inplace3 with a given output type. */
	@Test
	public void testArity7_IT_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] expected = { 1.0, 128.0, 2187.0 };
		final Inplaces.Arity7_3<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays7_3") //
				.arity7() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7);
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Matches a 7-arity inplace4 with a given output type. */
	@Test
	public void testArity7_IT_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] expected = { 1.0, 128.0, 2187.0 };
		final Inplaces.Arity7_4<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays7_4") //
				.arity7() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7);
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Matches a 7-arity inplace5 with a given output type. */
	@Test
	public void testArity7_IT_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] expected = { 1.0, 128.0, 2187.0 };
		final Inplaces.Arity7_5<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays7_5") //
				.arity7() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7);
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Matches a 7-arity inplace6 with a given output type. */
	@Test
	public void testArity7_IT_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] expected = { 1.0, 128.0, 2187.0 };
		final Inplaces.Arity7_6<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays7_6") //
				.arity7() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7);
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Matches a 7-arity inplace7 with a given output type. */
	@Test
	public void testArity7_IT_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] expected = { 1.0, 128.0, 2187.0 };
		final Inplaces.Arity7_7<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays7_7") //
				.arity7() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7);
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Matches a 7-arity function using input types + output type. */
	@Test
	public void testArity7_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double expected = 7.;
		final Functions.Arity7<Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity7().inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 7-arity computer using input types + output type. */
	@Test
	public void testArity7_IT_OT_matchC() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 7, 14, 21 };
		final Computers.Arity7<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity7() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.outType(double[].class).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 7-arity function using input value only. */
	@Test
	public void testArity7_IV_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double expected = 7.;
		final Functions.Arity7<Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity7() //
				.input(input1, input2, input3, input4, input5, input6, input7) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7);
		assertEquals(actual, expected);
	}

	/** Matches a 7-arity inplace1 with a given output value. */
	@Test
	public void testArity7_IV_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		double[] expected = { 1.0, 128.0, 2187.0 };
		final Inplaces.Arity7_1<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays7_1").arity7().input(input1, input2, input3, input4, input5, input6, input7) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7);
		assertTrue(Arrays.equals(expected, input1));
	}

	/** Matches a 7-arity inplace2 with a given output value. */
	@Test
	public void testArity7_IV_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		double[] expected = { 1.0, 128.0, 2187.0 };
		final Inplaces.Arity7_2<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays7_2").arity7().input(input1, input2, input3, input4, input5, input6, input7) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7);
		assertTrue(Arrays.equals(expected, input2));
	}

	/** Matches a 7-arity inplace3 with a given output value. */
	@Test
	public void testArity7_IV_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		double[] expected = { 1.0, 128.0, 2187.0 };
		final Inplaces.Arity7_3<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays7_3").arity7().input(input1, input2, input3, input4, input5, input6, input7) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7);
		assertTrue(Arrays.equals(expected, input3));
	}

	/** Matches a 7-arity inplace4 with a given output value. */
	@Test
	public void testArity7_IV_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		double[] expected = { 1.0, 128.0, 2187.0 };
		final Inplaces.Arity7_4<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays7_4").arity7().input(input1, input2, input3, input4, input5, input6, input7) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7);
		assertTrue(Arrays.equals(expected, input4));
	}

	/** Matches a 7-arity inplace5 with a given output value. */
	@Test
	public void testArity7_IV_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		double[] expected = { 1.0, 128.0, 2187.0 };
		final Inplaces.Arity7_5<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays7_5").arity7().input(input1, input2, input3, input4, input5, input6, input7) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7);
		assertTrue(Arrays.equals(expected, input5));
	}

	/** Matches a 7-arity inplace6 with a given output value. */
	@Test
	public void testArity7_IV_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		double[] expected = { 1.0, 128.0, 2187.0 };
		final Inplaces.Arity7_6<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays7_6").arity7().input(input1, input2, input3, input4, input5, input6, input7) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7);
		assertTrue(Arrays.equals(expected, input6));
	}

	/** Matches a 7-arity inplace7 with a given output value. */
	@Test
	public void testArity7_IV_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		double[] expected = { 1.0, 128.0, 2187.0 };
		final Inplaces.Arity7_7<double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays7_7").arity7().input(input1, input2, input3, input4, input5, input6, input7) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7);
		assertTrue(Arrays.equals(expected, input7));
	}

	/** Runs a 7-arity function using input value only. */
	@Test
	public void testArity7_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double expected = 7.;
		final Object actual = name("test.addDoubles") //
			.arity7() //
			.input(input1, input2, input3, input4, input5, input6, input7) //
			.apply();
		assertEquals(actual, expected);
	}

	/** Runs a 7-arity inplace1 with a given output value. */
	@Test
	public void testArity7_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] expected = { 1.0, 128.0, 2187.0 };
		name("test.mulArrays7_1") //
			.arity7() //
			.input(input1, input2, input3, input4, input5, input6, input7) //
			.mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Runs a 7-arity inplace2 with a given output value. */
	@Test
	public void testArity7_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] expected = { 1.0, 128.0, 2187.0 };
		name("test.mulArrays7_2") //
			.arity7() //
			.input(input1, input2, input3, input4, input5, input6, input7) //
			.mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Runs a 7-arity inplace3 with a given output value. */
	@Test
	public void testArity7_IV_OU_runI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] expected = { 1.0, 128.0, 2187.0 };
		name("test.mulArrays7_3") //
			.arity7() //
			.input(input1, input2, input3, input4, input5, input6, input7) //
			.mutate3();
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Runs a 7-arity inplace4 with a given output value. */
	@Test
	public void testArity7_IV_OU_runI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] expected = { 1.0, 128.0, 2187.0 };
		name("test.mulArrays7_4") //
			.arity7() //
			.input(input1, input2, input3, input4, input5, input6, input7) //
			.mutate4();
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Runs a 7-arity inplace5 with a given output value. */
	@Test
	public void testArity7_IV_OU_runI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] expected = { 1.0, 128.0, 2187.0 };
		name("test.mulArrays7_5") //
			.arity7() //
			.input(input1, input2, input3, input4, input5, input6, input7) //
			.mutate5();
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Runs a 7-arity inplace6 with a given output value. */
	@Test
	public void testArity7_IV_OU_runI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] expected = { 1.0, 128.0, 2187.0 };
		name("test.mulArrays7_6") //
			.arity7() //
			.input(input1, input2, input3, input4, input5, input6, input7) //
			.mutate6();
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Runs a 7-arity inplace7 with a given output value. */
	@Test
	public void testArity7_IV_OU_runI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] expected = { 1.0, 128.0, 2187.0 };
		name("test.mulArrays7_7") //
			.arity7() //
			.input(input1, input2, input3, input4, input5, input6, input7) //
			.mutate7();
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Matches a 7-arity function using input value + output type. */
	@Test
	public void testArity7_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double expected = 7.;
		final Functions.Arity7<Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity7().input(input1, input2, input3, input4, input5, input6, input7) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity7_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 7, 14, 21 };
		final Computers.Arity7<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity7() //
				.input(input1, input2, input3, input4, input5, input6, input7) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity7_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double expected = 7.;
		final Object actual = name("test.addDoubles") //
			.arity7() //
			.input(input1, input2, input3, input4, input5, input6, input7) //
			.outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity7_IV_OV_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 7, 14, 21 };
		final Computers.Arity7<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity7() //
				.input(input1, input2, input3, input4, input5, input6, input7) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity7_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 7, 14, 21 };
		name("test.addArrays") //
			.arity7() //
			.input(input1, input2, input3, input4, input5, input6, input7) //
			.output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- 8-ary --

	/** Matches a 8-arity function using input types only. */
	@Test
	public void testArity8_IT_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double expected = 8.;
		final Functions.Arity8<Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity8() //
				.inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8);
		assertEquals(actual, expected);
	}

	/** Matches a 8-arity inplace1 with a given output type. */
	@Test
	public void testArity8_IT_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_1<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_1") //
				.arity8() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Matches a 8-arity inplace2 with a given output type. */
	@Test
	public void testArity8_IT_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_2<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_2") //
				.arity8() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Matches a 8-arity inplace3 with a given output type. */
	@Test
	public void testArity8_IT_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_3<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_3") //
				.arity8() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Matches a 8-arity inplace4 with a given output type. */
	@Test
	public void testArity8_IT_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_4<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_4") //
				.arity8() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Matches a 8-arity inplace5 with a given output type. */
	@Test
	public void testArity8_IT_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_5<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_5") //
				.arity8() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Matches a 8-arity inplace6 with a given output type. */
	@Test
	public void testArity8_IT_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_6<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_6") //
				.arity8() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Matches a 8-arity inplace7 with a given output type. */
	@Test
	public void testArity8_IT_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_7<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_7") //
				.arity8() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Matches a 8-arity inplace8 with a given output type. */
	@Test
	public void testArity8_IT_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_8<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_8") //
				.arity8() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Matches a 8-arity function using input types + output type. */
	@Test
	public void testArity8_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double expected = 8.;
		final Functions.Arity8<Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity8().inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 8-arity computer using input types + output type. */
	@Test
	public void testArity8_IT_OT_matchC() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 8, 16, 24 };
		final Computers.Arity8<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity8() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.outType(double[].class).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 8-arity function using input value only. */
	@Test
	public void testArity8_IV_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double expected = 8.;
		final Functions.Arity8<Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity8() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8);
		assertEquals(actual, expected);
	}

	/** Matches a 8-arity inplace1 with a given output value. */
	@Test
	public void testArity8_IV_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_1<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_1").arity8().input(input1, input2, input3, input4, input5, input6, input7, input8) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(expected, input1));
	}

	/** Matches a 8-arity inplace2 with a given output value. */
	@Test
	public void testArity8_IV_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_2<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_2").arity8().input(input1, input2, input3, input4, input5, input6, input7, input8) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(expected, input2));
	}

	/** Matches a 8-arity inplace3 with a given output value. */
	@Test
	public void testArity8_IV_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_3<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_3").arity8().input(input1, input2, input3, input4, input5, input6, input7, input8) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(expected, input3));
	}

	/** Matches a 8-arity inplace4 with a given output value. */
	@Test
	public void testArity8_IV_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_4<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_4").arity8().input(input1, input2, input3, input4, input5, input6, input7, input8) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(expected, input4));
	}

	/** Matches a 8-arity inplace5 with a given output value. */
	@Test
	public void testArity8_IV_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_5<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_5").arity8().input(input1, input2, input3, input4, input5, input6, input7, input8) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(expected, input5));
	}

	/** Matches a 8-arity inplace6 with a given output value. */
	@Test
	public void testArity8_IV_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_6<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_6").arity8().input(input1, input2, input3, input4, input5, input6, input7, input8) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(expected, input6));
	}

	/** Matches a 8-arity inplace7 with a given output value. */
	@Test
	public void testArity8_IV_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_7<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_7").arity8().input(input1, input2, input3, input4, input5, input6, input7, input8) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(expected, input7));
	}

	/** Matches a 8-arity inplace8 with a given output value. */
	@Test
	public void testArity8_IV_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		double[] expected = { 1.0, 256.0, 6561.0 };
		final Inplaces.Arity8_8<double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays8_8").arity8().input(input1, input2, input3, input4, input5, input6, input7, input8) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8);
		assertTrue(Arrays.equals(expected, input8));
	}

	/** Runs a 8-arity function using input value only. */
	@Test
	public void testArity8_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double expected = 8.;
		final Object actual = name("test.addDoubles") //
			.arity8() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8) //
			.apply();
		assertEquals(actual, expected);
	}

	/** Runs a 8-arity inplace1 with a given output value. */
	@Test
	public void testArity8_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		name("test.mulArrays8_1") //
			.arity8() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8) //
			.mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Runs a 8-arity inplace2 with a given output value. */
	@Test
	public void testArity8_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		name("test.mulArrays8_2") //
			.arity8() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8) //
			.mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Runs a 8-arity inplace3 with a given output value. */
	@Test
	public void testArity8_IV_OU_runI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		name("test.mulArrays8_3") //
			.arity8() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8) //
			.mutate3();
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Runs a 8-arity inplace4 with a given output value. */
	@Test
	public void testArity8_IV_OU_runI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		name("test.mulArrays8_4") //
			.arity8() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8) //
			.mutate4();
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Runs a 8-arity inplace5 with a given output value. */
	@Test
	public void testArity8_IV_OU_runI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		name("test.mulArrays8_5") //
			.arity8() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8) //
			.mutate5();
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Runs a 8-arity inplace6 with a given output value. */
	@Test
	public void testArity8_IV_OU_runI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		name("test.mulArrays8_6") //
			.arity8() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8) //
			.mutate6();
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Runs a 8-arity inplace7 with a given output value. */
	@Test
	public void testArity8_IV_OU_runI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		name("test.mulArrays8_7") //
			.arity8() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8) //
			.mutate7();
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Runs a 8-arity inplace8 with a given output value. */
	@Test
	public void testArity8_IV_OU_runI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] expected = { 1.0, 256.0, 6561.0 };
		name("test.mulArrays8_8") //
			.arity8() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8) //
			.mutate8();
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Matches a 8-arity function using input value + output type. */
	@Test
	public void testArity8_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double expected = 8.;
		final Functions.Arity8<Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity8().input(input1, input2, input3, input4, input5, input6, input7, input8) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity8_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 8, 16, 24 };
		final Computers.Arity8<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity8() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity8_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double expected = 8.;
		final Object actual = name("test.addDoubles") //
			.arity8() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8) //
			.outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity8_IV_OV_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 8, 16, 24 };
		final Computers.Arity8<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity8() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity8_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 8, 16, 24 };
		name("test.addArrays") //
			.arity8() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8) //
			.output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- 9-ary --

	/** Matches a 9-arity function using input types only. */
	@Test
	public void testArity9_IT_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double expected = 9.;
		final Functions.Arity9<Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity9() //
				.inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertEquals(actual, expected);
	}

	/** Matches a 9-arity inplace1 with a given output type. */
	@Test
	public void testArity9_IT_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_1<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_1") //
				.arity9() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Matches a 9-arity inplace2 with a given output type. */
	@Test
	public void testArity9_IT_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_2<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_2") //
				.arity9() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Matches a 9-arity inplace3 with a given output type. */
	@Test
	public void testArity9_IT_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_3<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_3") //
				.arity9() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Matches a 9-arity inplace4 with a given output type. */
	@Test
	public void testArity9_IT_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_4<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_4") //
				.arity9() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Matches a 9-arity inplace5 with a given output type. */
	@Test
	public void testArity9_IT_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_5<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_5") //
				.arity9() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Matches a 9-arity inplace6 with a given output type. */
	@Test
	public void testArity9_IT_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_6<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_6") //
				.arity9() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Matches a 9-arity inplace7 with a given output type. */
	@Test
	public void testArity9_IT_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_7<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_7") //
				.arity9() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Matches a 9-arity inplace8 with a given output type. */
	@Test
	public void testArity9_IT_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_8<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_8") //
				.arity9() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Matches a 9-arity inplace9 with a given output type. */
	@Test
	public void testArity9_IT_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_9<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_9") //
				.arity9() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Matches a 9-arity function using input types + output type. */
	@Test
	public void testArity9_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double expected = 9.;
		final Functions.Arity9<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity9().inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 9-arity computer using input types + output type. */
	@Test
	public void testArity9_IT_OT_matchC() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 9, 18, 27 };
		final Computers.Arity9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity9() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.outType(double[].class).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 9-arity function using input value only. */
	@Test
	public void testArity9_IV_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double expected = 9.;
		final Functions.Arity9<Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity9() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertEquals(actual, expected);
	}

	/** Matches a 9-arity inplace1 with a given output value. */
	@Test
	public void testArity9_IV_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_1<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_1").arity9().input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(expected, input1));
	}

	/** Matches a 9-arity inplace2 with a given output value. */
	@Test
	public void testArity9_IV_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_2<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_2").arity9().input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(expected, input2));
	}

	/** Matches a 9-arity inplace3 with a given output value. */
	@Test
	public void testArity9_IV_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_3<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_3").arity9().input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(expected, input3));
	}

	/** Matches a 9-arity inplace4 with a given output value. */
	@Test
	public void testArity9_IV_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_4<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_4").arity9().input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(expected, input4));
	}

	/** Matches a 9-arity inplace5 with a given output value. */
	@Test
	public void testArity9_IV_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_5<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_5").arity9().input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(expected, input5));
	}

	/** Matches a 9-arity inplace6 with a given output value. */
	@Test
	public void testArity9_IV_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_6<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_6").arity9().input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(expected, input6));
	}

	/** Matches a 9-arity inplace7 with a given output value. */
	@Test
	public void testArity9_IV_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_7<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_7").arity9().input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(expected, input7));
	}

	/** Matches a 9-arity inplace8 with a given output value. */
	@Test
	public void testArity9_IV_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_8<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_8").arity9().input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(expected, input8));
	}

	/** Matches a 9-arity inplace9 with a given output value. */
	@Test
	public void testArity9_IV_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		double[] expected = { 1.0, 512.0, 19683.0 };
		final Inplaces.Arity9_9<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays9_9").arity9().input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertTrue(Arrays.equals(expected, input9));
	}

	/** Runs a 9-arity function using input value only. */
	@Test
	public void testArity9_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double expected = 9.;
		final Object actual = name("test.addDoubles") //
			.arity9() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
			.apply();
		assertEquals(actual, expected);
	}

	/** Runs a 9-arity inplace1 with a given output value. */
	@Test
	public void testArity9_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		name("test.mulArrays9_1") //
			.arity9() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
			.mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Runs a 9-arity inplace2 with a given output value. */
	@Test
	public void testArity9_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		name("test.mulArrays9_2") //
			.arity9() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
			.mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Runs a 9-arity inplace3 with a given output value. */
	@Test
	public void testArity9_IV_OU_runI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		name("test.mulArrays9_3") //
			.arity9() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
			.mutate3();
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Runs a 9-arity inplace4 with a given output value. */
	@Test
	public void testArity9_IV_OU_runI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		name("test.mulArrays9_4") //
			.arity9() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
			.mutate4();
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Runs a 9-arity inplace5 with a given output value. */
	@Test
	public void testArity9_IV_OU_runI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		name("test.mulArrays9_5") //
			.arity9() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
			.mutate5();
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Runs a 9-arity inplace6 with a given output value. */
	@Test
	public void testArity9_IV_OU_runI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		name("test.mulArrays9_6") //
			.arity9() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
			.mutate6();
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Runs a 9-arity inplace7 with a given output value. */
	@Test
	public void testArity9_IV_OU_runI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		name("test.mulArrays9_7") //
			.arity9() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
			.mutate7();
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Runs a 9-arity inplace8 with a given output value. */
	@Test
	public void testArity9_IV_OU_runI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		name("test.mulArrays9_8") //
			.arity9() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
			.mutate8();
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Runs a 9-arity inplace9 with a given output value. */
	@Test
	public void testArity9_IV_OU_runI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] expected = { 1.0, 512.0, 19683.0 };
		name("test.mulArrays9_9") //
			.arity9() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
			.mutate9();
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Matches a 9-arity function using input value + output type. */
	@Test
	public void testArity9_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double expected = 9.;
		final Functions.Arity9<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity9().input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity9_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 9, 18, 27 };
		final Computers.Arity9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity9() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity9_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double expected = 9.;
		final Object actual = name("test.addDoubles") //
			.arity9() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
			.outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity9_IV_OV_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 9, 18, 27 };
		final Computers.Arity9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity9() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity9_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 9, 18, 27 };
		name("test.addArrays") //
			.arity9() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9) //
			.output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- 10-ary --

	/** Matches a 10-arity function using input types only. */
	@Test
	public void testArity10_IT_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double expected = 10.;
		final Functions.Arity10<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity10() //
				.inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertEquals(actual, expected);
	}

	/** Matches a 10-arity inplace1 with a given output type. */
	@Test
	public void testArity10_IT_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_1") //
				.arity10() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Matches a 10-arity inplace2 with a given output type. */
	@Test
	public void testArity10_IT_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_2") //
				.arity10() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Matches a 10-arity inplace3 with a given output type. */
	@Test
	public void testArity10_IT_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_3") //
				.arity10() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Matches a 10-arity inplace4 with a given output type. */
	@Test
	public void testArity10_IT_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_4") //
				.arity10() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Matches a 10-arity inplace5 with a given output type. */
	@Test
	public void testArity10_IT_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_5") //
				.arity10() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Matches a 10-arity inplace6 with a given output type. */
	@Test
	public void testArity10_IT_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_6") //
				.arity10() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Matches a 10-arity inplace7 with a given output type. */
	@Test
	public void testArity10_IT_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_7") //
				.arity10() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Matches a 10-arity inplace8 with a given output type. */
	@Test
	public void testArity10_IT_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_8") //
				.arity10() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Matches a 10-arity inplace9 with a given output type. */
	@Test
	public void testArity10_IT_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_9") //
				.arity10() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Matches a 10-arity inplace10 with a given output type. */
	@Test
	public void testArity10_IT_OU_matchI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_10") //
				.arity10() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace10();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(input10, expected));
	}

	/** Matches a 10-arity function using input types + output type. */
	@Test
	public void testArity10_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double expected = 10.;
		final Functions.Arity10<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity10().inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 10-arity computer using input types + output type. */
	@Test
	public void testArity10_IT_OT_matchC() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 10, 20, 30 };
		final Computers.Arity10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity10() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.outType(double[].class).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 10-arity function using input value only. */
	@Test
	public void testArity10_IV_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double expected = 10.;
		final Functions.Arity10<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity10() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertEquals(actual, expected);
	}

	/** Matches a 10-arity inplace1 with a given output value. */
	@Test
	public void testArity10_IV_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_1").arity10().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(expected, input1));
	}

	/** Matches a 10-arity inplace2 with a given output value. */
	@Test
	public void testArity10_IV_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_2").arity10().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(expected, input2));
	}

	/** Matches a 10-arity inplace3 with a given output value. */
	@Test
	public void testArity10_IV_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_3").arity10().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(expected, input3));
	}

	/** Matches a 10-arity inplace4 with a given output value. */
	@Test
	public void testArity10_IV_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_4").arity10().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(expected, input4));
	}

	/** Matches a 10-arity inplace5 with a given output value. */
	@Test
	public void testArity10_IV_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_5").arity10().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(expected, input5));
	}

	/** Matches a 10-arity inplace6 with a given output value. */
	@Test
	public void testArity10_IV_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_6").arity10().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(expected, input6));
	}

	/** Matches a 10-arity inplace7 with a given output value. */
	@Test
	public void testArity10_IV_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_7").arity10().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(expected, input7));
	}

	/** Matches a 10-arity inplace8 with a given output value. */
	@Test
	public void testArity10_IV_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_8").arity10().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(expected, input8));
	}

	/** Matches a 10-arity inplace9 with a given output value. */
	@Test
	public void testArity10_IV_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_9").arity10().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(expected, input9));
	}

	/** Matches a 10-arity inplace10 with a given output value. */
	@Test
	public void testArity10_IV_OU_matchI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		double[] expected = { 1.0, 1024.0, 59049.0 };
		final Inplaces.Arity10_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays10_10").arity10().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
				.inplace10();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertTrue(Arrays.equals(expected, input10));
	}

	/** Runs a 10-arity function using input value only. */
	@Test
	public void testArity10_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double expected = 10.;
		final Object actual = name("test.addDoubles") //
			.arity10() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
			.apply();
		assertEquals(actual, expected);
	}

	/** Runs a 10-arity inplace1 with a given output value. */
	@Test
	public void testArity10_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		name("test.mulArrays10_1") //
			.arity10() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
			.mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Runs a 10-arity inplace2 with a given output value. */
	@Test
	public void testArity10_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		name("test.mulArrays10_2") //
			.arity10() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
			.mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Runs a 10-arity inplace3 with a given output value. */
	@Test
	public void testArity10_IV_OU_runI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		name("test.mulArrays10_3") //
			.arity10() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
			.mutate3();
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Runs a 10-arity inplace4 with a given output value. */
	@Test
	public void testArity10_IV_OU_runI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		name("test.mulArrays10_4") //
			.arity10() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
			.mutate4();
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Runs a 10-arity inplace5 with a given output value. */
	@Test
	public void testArity10_IV_OU_runI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		name("test.mulArrays10_5") //
			.arity10() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
			.mutate5();
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Runs a 10-arity inplace6 with a given output value. */
	@Test
	public void testArity10_IV_OU_runI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		name("test.mulArrays10_6") //
			.arity10() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
			.mutate6();
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Runs a 10-arity inplace7 with a given output value. */
	@Test
	public void testArity10_IV_OU_runI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		name("test.mulArrays10_7") //
			.arity10() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
			.mutate7();
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Runs a 10-arity inplace8 with a given output value. */
	@Test
	public void testArity10_IV_OU_runI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		name("test.mulArrays10_8") //
			.arity10() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
			.mutate8();
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Runs a 10-arity inplace9 with a given output value. */
	@Test
	public void testArity10_IV_OU_runI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		name("test.mulArrays10_9") //
			.arity10() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
			.mutate9();
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Runs a 10-arity inplace10 with a given output value. */
	@Test
	public void testArity10_IV_OU_runI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] expected = { 1.0, 1024.0, 59049.0 };
		name("test.mulArrays10_10") //
			.arity10() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
			.mutate10();
		assertTrue(Arrays.equals(input10, expected));
	}

	/** Matches a 10-arity function using input value + output type. */
	@Test
	public void testArity10_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double expected = 10.;
		final Functions.Arity10<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity10().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity10_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 10, 20, 30 };
		final Computers.Arity10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity10() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity10_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double expected = 10.;
		final Object actual = name("test.addDoubles") //
			.arity10() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
			.outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity10_IV_OV_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 10, 20, 30 };
		final Computers.Arity10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity10() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity10_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 10, 20, 30 };
		name("test.addArrays") //
			.arity10() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) //
			.output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- 11-ary --

	/** Matches a 11-arity function using input types only. */
	@Test
	public void testArity11_IT_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double expected = 11.;
		final Functions.Arity11<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity11() //
				.inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertEquals(actual, expected);
	}

	/** Matches a 11-arity inplace1 with a given output type. */
	@Test
	public void testArity11_IT_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_1") //
				.arity11() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Matches a 11-arity inplace2 with a given output type. */
	@Test
	public void testArity11_IT_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_2") //
				.arity11() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Matches a 11-arity inplace3 with a given output type. */
	@Test
	public void testArity11_IT_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_3") //
				.arity11() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Matches a 11-arity inplace4 with a given output type. */
	@Test
	public void testArity11_IT_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_4") //
				.arity11() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Matches a 11-arity inplace5 with a given output type. */
	@Test
	public void testArity11_IT_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_5") //
				.arity11() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Matches a 11-arity inplace6 with a given output type. */
	@Test
	public void testArity11_IT_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_6") //
				.arity11() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Matches a 11-arity inplace7 with a given output type. */
	@Test
	public void testArity11_IT_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_7") //
				.arity11() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Matches a 11-arity inplace8 with a given output type. */
	@Test
	public void testArity11_IT_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_8") //
				.arity11() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Matches a 11-arity inplace9 with a given output type. */
	@Test
	public void testArity11_IT_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_9") //
				.arity11() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Matches a 11-arity inplace10 with a given output type. */
	@Test
	public void testArity11_IT_OU_matchI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_10") //
				.arity11() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace10();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(input10, expected));
	}

	/** Matches a 11-arity inplace11 with a given output type. */
	@Test
	public void testArity11_IT_OU_matchI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_11") //
				.arity11() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace11();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(input11, expected));
	}

	/** Matches a 11-arity function using input types + output type. */
	@Test
	public void testArity11_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double expected = 11.;
		final Functions.Arity11<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity11().inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 11-arity computer using input types + output type. */
	@Test
	public void testArity11_IT_OT_matchC() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 11, 22, 33 };
		final Computers.Arity11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity11() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.outType(double[].class).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 11-arity function using input value only. */
	@Test
	public void testArity11_IV_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double expected = 11.;
		final Functions.Arity11<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity11() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertEquals(actual, expected);
	}

	/** Matches a 11-arity inplace1 with a given output value. */
	@Test
	public void testArity11_IV_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_1").arity11().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(expected, input1));
	}

	/** Matches a 11-arity inplace2 with a given output value. */
	@Test
	public void testArity11_IV_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_2").arity11().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(expected, input2));
	}

	/** Matches a 11-arity inplace3 with a given output value. */
	@Test
	public void testArity11_IV_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_3").arity11().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(expected, input3));
	}

	/** Matches a 11-arity inplace4 with a given output value. */
	@Test
	public void testArity11_IV_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_4").arity11().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(expected, input4));
	}

	/** Matches a 11-arity inplace5 with a given output value. */
	@Test
	public void testArity11_IV_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_5").arity11().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(expected, input5));
	}

	/** Matches a 11-arity inplace6 with a given output value. */
	@Test
	public void testArity11_IV_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_6").arity11().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(expected, input6));
	}

	/** Matches a 11-arity inplace7 with a given output value. */
	@Test
	public void testArity11_IV_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_7").arity11().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(expected, input7));
	}

	/** Matches a 11-arity inplace8 with a given output value. */
	@Test
	public void testArity11_IV_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_8").arity11().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(expected, input8));
	}

	/** Matches a 11-arity inplace9 with a given output value. */
	@Test
	public void testArity11_IV_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_9").arity11().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(expected, input9));
	}

	/** Matches a 11-arity inplace10 with a given output value. */
	@Test
	public void testArity11_IV_OU_matchI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_10").arity11().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
				.inplace10();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(expected, input10));
	}

	/** Matches a 11-arity inplace11 with a given output value. */
	@Test
	public void testArity11_IV_OU_matchI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		double[] expected = { 1.0, 2048.0, 177147.0 };
		final Inplaces.Arity11_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays11_11").arity11().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
				.inplace11();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertTrue(Arrays.equals(expected, input11));
	}

	/** Runs a 11-arity function using input value only. */
	@Test
	public void testArity11_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double expected = 11.;
		final Object actual = name("test.addDoubles") //
			.arity11() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
			.apply();
		assertEquals(actual, expected);
	}

	/** Runs a 11-arity inplace1 with a given output value. */
	@Test
	public void testArity11_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		name("test.mulArrays11_1") //
			.arity11() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
			.mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Runs a 11-arity inplace2 with a given output value. */
	@Test
	public void testArity11_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		name("test.mulArrays11_2") //
			.arity11() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
			.mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Runs a 11-arity inplace3 with a given output value. */
	@Test
	public void testArity11_IV_OU_runI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		name("test.mulArrays11_3") //
			.arity11() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
			.mutate3();
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Runs a 11-arity inplace4 with a given output value. */
	@Test
	public void testArity11_IV_OU_runI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		name("test.mulArrays11_4") //
			.arity11() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
			.mutate4();
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Runs a 11-arity inplace5 with a given output value. */
	@Test
	public void testArity11_IV_OU_runI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		name("test.mulArrays11_5") //
			.arity11() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
			.mutate5();
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Runs a 11-arity inplace6 with a given output value. */
	@Test
	public void testArity11_IV_OU_runI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		name("test.mulArrays11_6") //
			.arity11() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
			.mutate6();
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Runs a 11-arity inplace7 with a given output value. */
	@Test
	public void testArity11_IV_OU_runI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		name("test.mulArrays11_7") //
			.arity11() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
			.mutate7();
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Runs a 11-arity inplace8 with a given output value. */
	@Test
	public void testArity11_IV_OU_runI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		name("test.mulArrays11_8") //
			.arity11() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
			.mutate8();
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Runs a 11-arity inplace9 with a given output value. */
	@Test
	public void testArity11_IV_OU_runI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		name("test.mulArrays11_9") //
			.arity11() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
			.mutate9();
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Runs a 11-arity inplace10 with a given output value. */
	@Test
	public void testArity11_IV_OU_runI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		name("test.mulArrays11_10") //
			.arity11() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
			.mutate10();
		assertTrue(Arrays.equals(input10, expected));
	}

	/** Runs a 11-arity inplace11 with a given output value. */
	@Test
	public void testArity11_IV_OU_runI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] expected = { 1.0, 2048.0, 177147.0 };
		name("test.mulArrays11_11") //
			.arity11() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
			.mutate11();
		assertTrue(Arrays.equals(input11, expected));
	}

	/** Matches a 11-arity function using input value + output type. */
	@Test
	public void testArity11_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double expected = 11.;
		final Functions.Arity11<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity11().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity11_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 11, 22, 33 };
		final Computers.Arity11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity11() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity11_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double expected = 11.;
		final Object actual = name("test.addDoubles") //
			.arity11() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
			.outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity11_IV_OV_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 11, 22, 33 };
		final Computers.Arity11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity11() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity11_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 11, 22, 33 };
		name("test.addArrays") //
			.arity11() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) //
			.output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- 12-ary --

	/** Matches a 12-arity function using input types only. */
	@Test
	public void testArity12_IT_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double expected = 12.;
		final Functions.Arity12<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity12() //
				.inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertEquals(actual, expected);
	}

	/** Matches a 12-arity inplace1 with a given output type. */
	@Test
	public void testArity12_IT_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_1") //
				.arity12() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Matches a 12-arity inplace2 with a given output type. */
	@Test
	public void testArity12_IT_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_2") //
				.arity12() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Matches a 12-arity inplace3 with a given output type. */
	@Test
	public void testArity12_IT_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_3") //
				.arity12() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Matches a 12-arity inplace4 with a given output type. */
	@Test
	public void testArity12_IT_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_4") //
				.arity12() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Matches a 12-arity inplace5 with a given output type. */
	@Test
	public void testArity12_IT_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_5") //
				.arity12() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Matches a 12-arity inplace6 with a given output type. */
	@Test
	public void testArity12_IT_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_6") //
				.arity12() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Matches a 12-arity inplace7 with a given output type. */
	@Test
	public void testArity12_IT_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_7") //
				.arity12() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Matches a 12-arity inplace8 with a given output type. */
	@Test
	public void testArity12_IT_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_8") //
				.arity12() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Matches a 12-arity inplace9 with a given output type. */
	@Test
	public void testArity12_IT_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_9") //
				.arity12() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Matches a 12-arity inplace10 with a given output type. */
	@Test
	public void testArity12_IT_OU_matchI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_10") //
				.arity12() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace10();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(input10, expected));
	}

	/** Matches a 12-arity inplace11 with a given output type. */
	@Test
	public void testArity12_IT_OU_matchI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_11") //
				.arity12() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace11();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(input11, expected));
	}

	/** Matches a 12-arity inplace12 with a given output type. */
	@Test
	public void testArity12_IT_OU_matchI12() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_12") //
				.arity12() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace12();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(input12, expected));
	}

	/** Matches a 12-arity function using input types + output type. */
	@Test
	public void testArity12_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double expected = 12.;
		final Functions.Arity12<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity12().inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 12-arity computer using input types + output type. */
	@Test
	public void testArity12_IT_OT_matchC() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 12, 24, 36 };
		final Computers.Arity12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity12() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.outType(double[].class).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 12-arity function using input value only. */
	@Test
	public void testArity12_IV_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double expected = 12.;
		final Functions.Arity12<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity12() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertEquals(actual, expected);
	}

	/** Matches a 12-arity inplace1 with a given output value. */
	@Test
	public void testArity12_IV_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_1").arity12().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(expected, input1));
	}

	/** Matches a 12-arity inplace2 with a given output value. */
	@Test
	public void testArity12_IV_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_2").arity12().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(expected, input2));
	}

	/** Matches a 12-arity inplace3 with a given output value. */
	@Test
	public void testArity12_IV_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_3").arity12().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(expected, input3));
	}

	/** Matches a 12-arity inplace4 with a given output value. */
	@Test
	public void testArity12_IV_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_4").arity12().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(expected, input4));
	}

	/** Matches a 12-arity inplace5 with a given output value. */
	@Test
	public void testArity12_IV_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_5").arity12().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(expected, input5));
	}

	/** Matches a 12-arity inplace6 with a given output value. */
	@Test
	public void testArity12_IV_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_6").arity12().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(expected, input6));
	}

	/** Matches a 12-arity inplace7 with a given output value. */
	@Test
	public void testArity12_IV_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_7").arity12().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(expected, input7));
	}

	/** Matches a 12-arity inplace8 with a given output value. */
	@Test
	public void testArity12_IV_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_8").arity12().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(expected, input8));
	}

	/** Matches a 12-arity inplace9 with a given output value. */
	@Test
	public void testArity12_IV_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_9").arity12().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(expected, input9));
	}

	/** Matches a 12-arity inplace10 with a given output value. */
	@Test
	public void testArity12_IV_OU_matchI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_10").arity12().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.inplace10();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(expected, input10));
	}

	/** Matches a 12-arity inplace11 with a given output value. */
	@Test
	public void testArity12_IV_OU_matchI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_11").arity12().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.inplace11();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(expected, input11));
	}

	/** Matches a 12-arity inplace12 with a given output value. */
	@Test
	public void testArity12_IV_OU_matchI12() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		double[] expected = { 1.0, 4096.0, 531441.0 };
		final Inplaces.Arity12_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays12_12").arity12().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.inplace12();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertTrue(Arrays.equals(expected, input12));
	}

	/** Runs a 12-arity function using input value only. */
	@Test
	public void testArity12_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double expected = 12.;
		final Object actual = name("test.addDoubles") //
			.arity12() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
			.apply();
		assertEquals(actual, expected);
	}

	/** Runs a 12-arity inplace1 with a given output value. */
	@Test
	public void testArity12_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		name("test.mulArrays12_1") //
			.arity12() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
			.mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Runs a 12-arity inplace2 with a given output value. */
	@Test
	public void testArity12_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		name("test.mulArrays12_2") //
			.arity12() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
			.mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Runs a 12-arity inplace3 with a given output value. */
	@Test
	public void testArity12_IV_OU_runI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		name("test.mulArrays12_3") //
			.arity12() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
			.mutate3();
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Runs a 12-arity inplace4 with a given output value. */
	@Test
	public void testArity12_IV_OU_runI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		name("test.mulArrays12_4") //
			.arity12() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
			.mutate4();
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Runs a 12-arity inplace5 with a given output value. */
	@Test
	public void testArity12_IV_OU_runI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		name("test.mulArrays12_5") //
			.arity12() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
			.mutate5();
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Runs a 12-arity inplace6 with a given output value. */
	@Test
	public void testArity12_IV_OU_runI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		name("test.mulArrays12_6") //
			.arity12() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
			.mutate6();
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Runs a 12-arity inplace7 with a given output value. */
	@Test
	public void testArity12_IV_OU_runI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		name("test.mulArrays12_7") //
			.arity12() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
			.mutate7();
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Runs a 12-arity inplace8 with a given output value. */
	@Test
	public void testArity12_IV_OU_runI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		name("test.mulArrays12_8") //
			.arity12() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
			.mutate8();
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Runs a 12-arity inplace9 with a given output value. */
	@Test
	public void testArity12_IV_OU_runI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		name("test.mulArrays12_9") //
			.arity12() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
			.mutate9();
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Runs a 12-arity inplace10 with a given output value. */
	@Test
	public void testArity12_IV_OU_runI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		name("test.mulArrays12_10") //
			.arity12() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
			.mutate10();
		assertTrue(Arrays.equals(input10, expected));
	}

	/** Runs a 12-arity inplace11 with a given output value. */
	@Test
	public void testArity12_IV_OU_runI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		name("test.mulArrays12_11") //
			.arity12() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
			.mutate11();
		assertTrue(Arrays.equals(input11, expected));
	}

	/** Runs a 12-arity inplace12 with a given output value. */
	@Test
	public void testArity12_IV_OU_runI12() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] expected = { 1.0, 4096.0, 531441.0 };
		name("test.mulArrays12_12") //
			.arity12() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
			.mutate12();
		assertTrue(Arrays.equals(input12, expected));
	}

	/** Matches a 12-arity function using input value + output type. */
	@Test
	public void testArity12_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double expected = 12.;
		final Functions.Arity12<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity12().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity12_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] input12 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 12, 24, 36 };
		final Computers.Arity12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity12() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity12_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double expected = 12.;
		final Object actual = name("test.addDoubles") //
			.arity12() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
			.outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity12_IV_OV_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] input12 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 12, 24, 36 };
		final Computers.Arity12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity12() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity12_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] input12 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 12, 24, 36 };
		name("test.addArrays") //
			.arity12() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) //
			.output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- 13-ary --

	/** Matches a 13-arity function using input types only. */
	@Test
	public void testArity13_IT_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double expected = 13.;
		final Functions.Arity13<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity13() //
				.inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertEquals(actual, expected);
	}

	/** Matches a 13-arity inplace1 with a given output type. */
	@Test
	public void testArity13_IT_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_1") //
				.arity13() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Matches a 13-arity inplace2 with a given output type. */
	@Test
	public void testArity13_IT_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_2") //
				.arity13() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Matches a 13-arity inplace3 with a given output type. */
	@Test
	public void testArity13_IT_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_3") //
				.arity13() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Matches a 13-arity inplace4 with a given output type. */
	@Test
	public void testArity13_IT_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_4") //
				.arity13() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Matches a 13-arity inplace5 with a given output type. */
	@Test
	public void testArity13_IT_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_5") //
				.arity13() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Matches a 13-arity inplace6 with a given output type. */
	@Test
	public void testArity13_IT_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_6") //
				.arity13() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Matches a 13-arity inplace7 with a given output type. */
	@Test
	public void testArity13_IT_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_7") //
				.arity13() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Matches a 13-arity inplace8 with a given output type. */
	@Test
	public void testArity13_IT_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_8") //
				.arity13() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Matches a 13-arity inplace9 with a given output type. */
	@Test
	public void testArity13_IT_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_9") //
				.arity13() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Matches a 13-arity inplace10 with a given output type. */
	@Test
	public void testArity13_IT_OU_matchI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_10") //
				.arity13() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace10();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(input10, expected));
	}

	/** Matches a 13-arity inplace11 with a given output type. */
	@Test
	public void testArity13_IT_OU_matchI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_11") //
				.arity13() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace11();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(input11, expected));
	}

	/** Matches a 13-arity inplace12 with a given output type. */
	@Test
	public void testArity13_IT_OU_matchI12() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_12") //
				.arity13() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace12();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(input12, expected));
	}

	/** Matches a 13-arity inplace13 with a given output type. */
	@Test
	public void testArity13_IT_OU_matchI13() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_13") //
				.arity13() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace13();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(input13, expected));
	}

	/** Matches a 13-arity function using input types + output type. */
	@Test
	public void testArity13_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double expected = 13.;
		final Functions.Arity13<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity13().inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 13-arity computer using input types + output type. */
	@Test
	public void testArity13_IT_OT_matchC() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 13, 26, 39 };
		final Computers.Arity13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity13() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.outType(double[].class).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 13-arity function using input value only. */
	@Test
	public void testArity13_IV_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double expected = 13.;
		final Functions.Arity13<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity13() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertEquals(actual, expected);
	}

	/** Matches a 13-arity inplace1 with a given output value. */
	@Test
	public void testArity13_IV_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_1").arity13().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(expected, input1));
	}

	/** Matches a 13-arity inplace2 with a given output value. */
	@Test
	public void testArity13_IV_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_2").arity13().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(expected, input2));
	}

	/** Matches a 13-arity inplace3 with a given output value. */
	@Test
	public void testArity13_IV_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_3").arity13().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(expected, input3));
	}

	/** Matches a 13-arity inplace4 with a given output value. */
	@Test
	public void testArity13_IV_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_4").arity13().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(expected, input4));
	}

	/** Matches a 13-arity inplace5 with a given output value. */
	@Test
	public void testArity13_IV_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_5").arity13().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(expected, input5));
	}

	/** Matches a 13-arity inplace6 with a given output value. */
	@Test
	public void testArity13_IV_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_6").arity13().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(expected, input6));
	}

	/** Matches a 13-arity inplace7 with a given output value. */
	@Test
	public void testArity13_IV_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_7").arity13().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(expected, input7));
	}

	/** Matches a 13-arity inplace8 with a given output value. */
	@Test
	public void testArity13_IV_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_8").arity13().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(expected, input8));
	}

	/** Matches a 13-arity inplace9 with a given output value. */
	@Test
	public void testArity13_IV_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_9").arity13().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(expected, input9));
	}

	/** Matches a 13-arity inplace10 with a given output value. */
	@Test
	public void testArity13_IV_OU_matchI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_10").arity13().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.inplace10();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(expected, input10));
	}

	/** Matches a 13-arity inplace11 with a given output value. */
	@Test
	public void testArity13_IV_OU_matchI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_11").arity13().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.inplace11();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(expected, input11));
	}

	/** Matches a 13-arity inplace12 with a given output value. */
	@Test
	public void testArity13_IV_OU_matchI12() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_12").arity13().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.inplace12();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(expected, input12));
	}

	/** Matches a 13-arity inplace13 with a given output value. */
	@Test
	public void testArity13_IV_OU_matchI13() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		double[] expected = { 1.0, 8192.0, 1594323.0 };
		final Inplaces.Arity13_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays13_13").arity13().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.inplace13();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertTrue(Arrays.equals(expected, input13));
	}

	/** Runs a 13-arity function using input value only. */
	@Test
	public void testArity13_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double expected = 13.;
		final Object actual = name("test.addDoubles") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.apply();
		assertEquals(actual, expected);
	}

	/** Runs a 13-arity inplace1 with a given output value. */
	@Test
	public void testArity13_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		name("test.mulArrays13_1") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Runs a 13-arity inplace2 with a given output value. */
	@Test
	public void testArity13_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		name("test.mulArrays13_2") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Runs a 13-arity inplace3 with a given output value. */
	@Test
	public void testArity13_IV_OU_runI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		name("test.mulArrays13_3") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.mutate3();
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Runs a 13-arity inplace4 with a given output value. */
	@Test
	public void testArity13_IV_OU_runI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		name("test.mulArrays13_4") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.mutate4();
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Runs a 13-arity inplace5 with a given output value. */
	@Test
	public void testArity13_IV_OU_runI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		name("test.mulArrays13_5") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.mutate5();
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Runs a 13-arity inplace6 with a given output value. */
	@Test
	public void testArity13_IV_OU_runI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		name("test.mulArrays13_6") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.mutate6();
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Runs a 13-arity inplace7 with a given output value. */
	@Test
	public void testArity13_IV_OU_runI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		name("test.mulArrays13_7") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.mutate7();
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Runs a 13-arity inplace8 with a given output value. */
	@Test
	public void testArity13_IV_OU_runI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		name("test.mulArrays13_8") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.mutate8();
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Runs a 13-arity inplace9 with a given output value. */
	@Test
	public void testArity13_IV_OU_runI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		name("test.mulArrays13_9") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.mutate9();
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Runs a 13-arity inplace10 with a given output value. */
	@Test
	public void testArity13_IV_OU_runI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		name("test.mulArrays13_10") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.mutate10();
		assertTrue(Arrays.equals(input10, expected));
	}

	/** Runs a 13-arity inplace11 with a given output value. */
	@Test
	public void testArity13_IV_OU_runI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		name("test.mulArrays13_11") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.mutate11();
		assertTrue(Arrays.equals(input11, expected));
	}

	/** Runs a 13-arity inplace12 with a given output value. */
	@Test
	public void testArity13_IV_OU_runI12() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		name("test.mulArrays13_12") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.mutate12();
		assertTrue(Arrays.equals(input12, expected));
	}

	/** Runs a 13-arity inplace13 with a given output value. */
	@Test
	public void testArity13_IV_OU_runI13() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] expected = { 1.0, 8192.0, 1594323.0 };
		name("test.mulArrays13_13") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.mutate13();
		assertTrue(Arrays.equals(input13, expected));
	}

	/** Matches a 13-arity function using input value + output type. */
	@Test
	public void testArity13_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double expected = 13.;
		final Functions.Arity13<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity13().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity13_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] input12 = { 1, 2, 3 };
		double[] input13 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 13, 26, 39 };
		final Computers.Arity13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity13() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity13_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double expected = 13.;
		final Object actual = name("test.addDoubles") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity13_IV_OV_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] input12 = { 1, 2, 3 };
		double[] input13 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 13, 26, 39 };
		final Computers.Arity13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity13() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity13_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] input12 = { 1, 2, 3 };
		double[] input13 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 13, 26, 39 };
		name("test.addArrays") //
			.arity13() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) //
			.output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- 14-ary --

	/** Matches a 14-arity function using input types only. */
	@Test
	public void testArity14_IT_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double expected = 14.;
		final Functions.Arity14<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity14() //
				.inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertEquals(actual, expected);
	}

	/** Matches a 14-arity inplace1 with a given output type. */
	@Test
	public void testArity14_IT_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_1") //
				.arity14() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Matches a 14-arity inplace2 with a given output type. */
	@Test
	public void testArity14_IT_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_2") //
				.arity14() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Matches a 14-arity inplace3 with a given output type. */
	@Test
	public void testArity14_IT_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_3") //
				.arity14() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Matches a 14-arity inplace4 with a given output type. */
	@Test
	public void testArity14_IT_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_4") //
				.arity14() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Matches a 14-arity inplace5 with a given output type. */
	@Test
	public void testArity14_IT_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_5") //
				.arity14() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Matches a 14-arity inplace6 with a given output type. */
	@Test
	public void testArity14_IT_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_6") //
				.arity14() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Matches a 14-arity inplace7 with a given output type. */
	@Test
	public void testArity14_IT_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_7") //
				.arity14() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Matches a 14-arity inplace8 with a given output type. */
	@Test
	public void testArity14_IT_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_8") //
				.arity14() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Matches a 14-arity inplace9 with a given output type. */
	@Test
	public void testArity14_IT_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_9") //
				.arity14() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Matches a 14-arity inplace10 with a given output type. */
	@Test
	public void testArity14_IT_OU_matchI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_10") //
				.arity14() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace10();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(input10, expected));
	}

	/** Matches a 14-arity inplace11 with a given output type. */
	@Test
	public void testArity14_IT_OU_matchI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_11") //
				.arity14() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace11();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(input11, expected));
	}

	/** Matches a 14-arity inplace12 with a given output type. */
	@Test
	public void testArity14_IT_OU_matchI12() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_12") //
				.arity14() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace12();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(input12, expected));
	}

	/** Matches a 14-arity inplace13 with a given output type. */
	@Test
	public void testArity14_IT_OU_matchI13() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_13") //
				.arity14() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace13();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(input13, expected));
	}

	/** Matches a 14-arity inplace14 with a given output type. */
	@Test
	public void testArity14_IT_OU_matchI14() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_14") //
				.arity14() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace14();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(input14, expected));
	}

	/** Matches a 14-arity function using input types + output type. */
	@Test
	public void testArity14_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double expected = 14.;
		final Functions.Arity14<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity14().inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 14-arity computer using input types + output type. */
	@Test
	public void testArity14_IT_OT_matchC() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 14, 28, 42 };
		final Computers.Arity14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity14() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.outType(double[].class).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 14-arity function using input value only. */
	@Test
	public void testArity14_IV_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double expected = 14.;
		final Functions.Arity14<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity14() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertEquals(actual, expected);
	}

	/** Matches a 14-arity inplace1 with a given output value. */
	@Test
	public void testArity14_IV_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_1").arity14().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(expected, input1));
	}

	/** Matches a 14-arity inplace2 with a given output value. */
	@Test
	public void testArity14_IV_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_2").arity14().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(expected, input2));
	}

	/** Matches a 14-arity inplace3 with a given output value. */
	@Test
	public void testArity14_IV_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_3").arity14().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(expected, input3));
	}

	/** Matches a 14-arity inplace4 with a given output value. */
	@Test
	public void testArity14_IV_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_4").arity14().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(expected, input4));
	}

	/** Matches a 14-arity inplace5 with a given output value. */
	@Test
	public void testArity14_IV_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_5").arity14().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(expected, input5));
	}

	/** Matches a 14-arity inplace6 with a given output value. */
	@Test
	public void testArity14_IV_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_6").arity14().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(expected, input6));
	}

	/** Matches a 14-arity inplace7 with a given output value. */
	@Test
	public void testArity14_IV_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_7").arity14().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(expected, input7));
	}

	/** Matches a 14-arity inplace8 with a given output value. */
	@Test
	public void testArity14_IV_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_8").arity14().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(expected, input8));
	}

	/** Matches a 14-arity inplace9 with a given output value. */
	@Test
	public void testArity14_IV_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_9").arity14().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(expected, input9));
	}

	/** Matches a 14-arity inplace10 with a given output value. */
	@Test
	public void testArity14_IV_OU_matchI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_10").arity14().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.inplace10();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(expected, input10));
	}

	/** Matches a 14-arity inplace11 with a given output value. */
	@Test
	public void testArity14_IV_OU_matchI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_11").arity14().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.inplace11();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(expected, input11));
	}

	/** Matches a 14-arity inplace12 with a given output value. */
	@Test
	public void testArity14_IV_OU_matchI12() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_12").arity14().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.inplace12();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(expected, input12));
	}

	/** Matches a 14-arity inplace13 with a given output value. */
	@Test
	public void testArity14_IV_OU_matchI13() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_13").arity14().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.inplace13();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(expected, input13));
	}

	/** Matches a 14-arity inplace14 with a given output value. */
	@Test
	public void testArity14_IV_OU_matchI14() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		double[] expected = { 1.0, 16384.0, 4782969.0 };
		final Inplaces.Arity14_14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays14_14").arity14().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.inplace14();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertTrue(Arrays.equals(expected, input14));
	}

	/** Runs a 14-arity function using input value only. */
	@Test
	public void testArity14_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double expected = 14.;
		final Object actual = name("test.addDoubles") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.apply();
		assertEquals(actual, expected);
	}

	/** Runs a 14-arity inplace1 with a given output value. */
	@Test
	public void testArity14_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		name("test.mulArrays14_1") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Runs a 14-arity inplace2 with a given output value. */
	@Test
	public void testArity14_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		name("test.mulArrays14_2") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Runs a 14-arity inplace3 with a given output value. */
	@Test
	public void testArity14_IV_OU_runI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		name("test.mulArrays14_3") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.mutate3();
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Runs a 14-arity inplace4 with a given output value. */
	@Test
	public void testArity14_IV_OU_runI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		name("test.mulArrays14_4") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.mutate4();
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Runs a 14-arity inplace5 with a given output value. */
	@Test
	public void testArity14_IV_OU_runI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		name("test.mulArrays14_5") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.mutate5();
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Runs a 14-arity inplace6 with a given output value. */
	@Test
	public void testArity14_IV_OU_runI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		name("test.mulArrays14_6") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.mutate6();
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Runs a 14-arity inplace7 with a given output value. */
	@Test
	public void testArity14_IV_OU_runI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		name("test.mulArrays14_7") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.mutate7();
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Runs a 14-arity inplace8 with a given output value. */
	@Test
	public void testArity14_IV_OU_runI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		name("test.mulArrays14_8") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.mutate8();
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Runs a 14-arity inplace9 with a given output value. */
	@Test
	public void testArity14_IV_OU_runI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		name("test.mulArrays14_9") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.mutate9();
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Runs a 14-arity inplace10 with a given output value. */
	@Test
	public void testArity14_IV_OU_runI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		name("test.mulArrays14_10") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.mutate10();
		assertTrue(Arrays.equals(input10, expected));
	}

	/** Runs a 14-arity inplace11 with a given output value. */
	@Test
	public void testArity14_IV_OU_runI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		name("test.mulArrays14_11") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.mutate11();
		assertTrue(Arrays.equals(input11, expected));
	}

	/** Runs a 14-arity inplace12 with a given output value. */
	@Test
	public void testArity14_IV_OU_runI12() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		name("test.mulArrays14_12") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.mutate12();
		assertTrue(Arrays.equals(input12, expected));
	}

	/** Runs a 14-arity inplace13 with a given output value. */
	@Test
	public void testArity14_IV_OU_runI13() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		name("test.mulArrays14_13") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.mutate13();
		assertTrue(Arrays.equals(input13, expected));
	}

	/** Runs a 14-arity inplace14 with a given output value. */
	@Test
	public void testArity14_IV_OU_runI14() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] expected = { 1.0, 16384.0, 4782969.0 };
		name("test.mulArrays14_14") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.mutate14();
		assertTrue(Arrays.equals(input14, expected));
	}

	/** Matches a 14-arity function using input value + output type. */
	@Test
	public void testArity14_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double expected = 14.;
		final Functions.Arity14<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity14().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity14_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] input12 = { 1, 2, 3 };
		double[] input13 = { 1, 2, 3 };
		double[] input14 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 14, 28, 42 };
		final Computers.Arity14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity14() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity14_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double expected = 14.;
		final Object actual = name("test.addDoubles") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity14_IV_OV_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] input12 = { 1, 2, 3 };
		double[] input13 = { 1, 2, 3 };
		double[] input14 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 14, 28, 42 };
		final Computers.Arity14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity14() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity14_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] input12 = { 1, 2, 3 };
		double[] input13 = { 1, 2, 3 };
		double[] input14 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 14, 28, 42 };
		name("test.addArrays") //
			.arity14() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) //
			.output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- 15-ary --

	/** Matches a 15-arity function using input types only. */
	@Test
	public void testArity15_IT_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double input15 = 1.;
		final Double expected = 15.;
		final Functions.Arity15<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity15() //
				.inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertEquals(actual, expected);
	}

	/** Matches a 15-arity inplace1 with a given output type. */
	@Test
	public void testArity15_IT_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_1") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Matches a 15-arity inplace2 with a given output type. */
	@Test
	public void testArity15_IT_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_2") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Matches a 15-arity inplace3 with a given output type. */
	@Test
	public void testArity15_IT_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_3") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Matches a 15-arity inplace4 with a given output type. */
	@Test
	public void testArity15_IT_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_4") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Matches a 15-arity inplace5 with a given output type. */
	@Test
	public void testArity15_IT_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_5") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Matches a 15-arity inplace6 with a given output type. */
	@Test
	public void testArity15_IT_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_6") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Matches a 15-arity inplace7 with a given output type. */
	@Test
	public void testArity15_IT_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_7") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Matches a 15-arity inplace8 with a given output type. */
	@Test
	public void testArity15_IT_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_8") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Matches a 15-arity inplace9 with a given output type. */
	@Test
	public void testArity15_IT_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_9") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Matches a 15-arity inplace10 with a given output type. */
	@Test
	public void testArity15_IT_OU_matchI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_10") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace10();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(input10, expected));
	}

	/** Matches a 15-arity inplace11 with a given output type. */
	@Test
	public void testArity15_IT_OU_matchI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_11") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace11();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(input11, expected));
	}

	/** Matches a 15-arity inplace12 with a given output type. */
	@Test
	public void testArity15_IT_OU_matchI12() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_12") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace12();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(input12, expected));
	}

	/** Matches a 15-arity inplace13 with a given output type. */
	@Test
	public void testArity15_IT_OU_matchI13() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_13") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace13();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(input13, expected));
	}

	/** Matches a 15-arity inplace14 with a given output type. */
	@Test
	public void testArity15_IT_OU_matchI14() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_14") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace14();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(input14, expected));
	}

	/** Matches a 15-arity inplace15 with a given output type. */
	@Test
	public void testArity15_IT_OU_matchI15() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_15<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_15") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace15();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(input15, expected));
	}

	/** Matches a 15-arity function using input types + output type. */
	@Test
	public void testArity15_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double input15 = 1.;
		final Double expected = 15.;
		final Functions.Arity15<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity15().inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 15-arity computer using input types + output type. */
	@Test
	public void testArity15_IT_OT_matchC() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 15, 30, 45 };
		final Computers.Arity15<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity15() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.outType(double[].class).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 15-arity function using input value only. */
	@Test
	public void testArity15_IV_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double input15 = 1.;
		final Double expected = 15.;
		final Functions.Arity15<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity15() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertEquals(actual, expected);
	}

	/** Matches a 15-arity inplace1 with a given output value. */
	@Test
	public void testArity15_IV_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_1").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(expected, input1));
	}

	/** Matches a 15-arity inplace2 with a given output value. */
	@Test
	public void testArity15_IV_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_2").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(expected, input2));
	}

	/** Matches a 15-arity inplace3 with a given output value. */
	@Test
	public void testArity15_IV_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_3").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(expected, input3));
	}

	/** Matches a 15-arity inplace4 with a given output value. */
	@Test
	public void testArity15_IV_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_4").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(expected, input4));
	}

	/** Matches a 15-arity inplace5 with a given output value. */
	@Test
	public void testArity15_IV_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_5").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(expected, input5));
	}

	/** Matches a 15-arity inplace6 with a given output value. */
	@Test
	public void testArity15_IV_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_6").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(expected, input6));
	}

	/** Matches a 15-arity inplace7 with a given output value. */
	@Test
	public void testArity15_IV_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_7").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(expected, input7));
	}

	/** Matches a 15-arity inplace8 with a given output value. */
	@Test
	public void testArity15_IV_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_8").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(expected, input8));
	}

	/** Matches a 15-arity inplace9 with a given output value. */
	@Test
	public void testArity15_IV_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_9").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(expected, input9));
	}

	/** Matches a 15-arity inplace10 with a given output value. */
	@Test
	public void testArity15_IV_OU_matchI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_10").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.inplace10();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(expected, input10));
	}

	/** Matches a 15-arity inplace11 with a given output value. */
	@Test
	public void testArity15_IV_OU_matchI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_11").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.inplace11();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(expected, input11));
	}

	/** Matches a 15-arity inplace12 with a given output value. */
	@Test
	public void testArity15_IV_OU_matchI12() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_12").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.inplace12();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(expected, input12));
	}

	/** Matches a 15-arity inplace13 with a given output value. */
	@Test
	public void testArity15_IV_OU_matchI13() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_13").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.inplace13();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(expected, input13));
	}

	/** Matches a 15-arity inplace14 with a given output value. */
	@Test
	public void testArity15_IV_OU_matchI14() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_14").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.inplace14();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(expected, input14));
	}

	/** Matches a 15-arity inplace15 with a given output value. */
	@Test
	public void testArity15_IV_OU_matchI15() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		final Inplaces.Arity15_15<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays15_15").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.inplace15();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertTrue(Arrays.equals(expected, input15));
	}

	/** Runs a 15-arity function using input value only. */
	@Test
	public void testArity15_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double input15 = 1.;
		final Double expected = 15.;
		final Object actual = name("test.addDoubles") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.apply();
		assertEquals(actual, expected);
	}

	/** Runs a 15-arity inplace1 with a given output value. */
	@Test
	public void testArity15_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		name("test.mulArrays15_1") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Runs a 15-arity inplace2 with a given output value. */
	@Test
	public void testArity15_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		name("test.mulArrays15_2") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Runs a 15-arity inplace3 with a given output value. */
	@Test
	public void testArity15_IV_OU_runI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		name("test.mulArrays15_3") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.mutate3();
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Runs a 15-arity inplace4 with a given output value. */
	@Test
	public void testArity15_IV_OU_runI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		name("test.mulArrays15_4") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.mutate4();
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Runs a 15-arity inplace5 with a given output value. */
	@Test
	public void testArity15_IV_OU_runI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		name("test.mulArrays15_5") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.mutate5();
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Runs a 15-arity inplace6 with a given output value. */
	@Test
	public void testArity15_IV_OU_runI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		name("test.mulArrays15_6") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.mutate6();
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Runs a 15-arity inplace7 with a given output value. */
	@Test
	public void testArity15_IV_OU_runI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		name("test.mulArrays15_7") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.mutate7();
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Runs a 15-arity inplace8 with a given output value. */
	@Test
	public void testArity15_IV_OU_runI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		name("test.mulArrays15_8") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.mutate8();
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Runs a 15-arity inplace9 with a given output value. */
	@Test
	public void testArity15_IV_OU_runI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		name("test.mulArrays15_9") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.mutate9();
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Runs a 15-arity inplace10 with a given output value. */
	@Test
	public void testArity15_IV_OU_runI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		name("test.mulArrays15_10") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.mutate10();
		assertTrue(Arrays.equals(input10, expected));
	}

	/** Runs a 15-arity inplace11 with a given output value. */
	@Test
	public void testArity15_IV_OU_runI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		name("test.mulArrays15_11") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.mutate11();
		assertTrue(Arrays.equals(input11, expected));
	}

	/** Runs a 15-arity inplace12 with a given output value. */
	@Test
	public void testArity15_IV_OU_runI12() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		name("test.mulArrays15_12") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.mutate12();
		assertTrue(Arrays.equals(input12, expected));
	}

	/** Runs a 15-arity inplace13 with a given output value. */
	@Test
	public void testArity15_IV_OU_runI13() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		name("test.mulArrays15_13") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.mutate13();
		assertTrue(Arrays.equals(input13, expected));
	}

	/** Runs a 15-arity inplace14 with a given output value. */
	@Test
	public void testArity15_IV_OU_runI14() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		name("test.mulArrays15_14") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.mutate14();
		assertTrue(Arrays.equals(input14, expected));
	}

	/** Runs a 15-arity inplace15 with a given output value. */
	@Test
	public void testArity15_IV_OU_runI15() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] expected = { 1.0, 32768.0, 1.4348907E7 };
		name("test.mulArrays15_15") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.mutate15();
		assertTrue(Arrays.equals(input15, expected));
	}

	/** Matches a 15-arity function using input value + output type. */
	@Test
	public void testArity15_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double input15 = 1.;
		final Double expected = 15.;
		final Functions.Arity15<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity15().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity15_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] input12 = { 1, 2, 3 };
		double[] input13 = { 1, 2, 3 };
		double[] input14 = { 1, 2, 3 };
		double[] input15 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 15, 30, 45 };
		final Computers.Arity15<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity15() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity15_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double input15 = 1.;
		final Double expected = 15.;
		final Object actual = name("test.addDoubles") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity15_IV_OV_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] input12 = { 1, 2, 3 };
		double[] input13 = { 1, 2, 3 };
		double[] input14 = { 1, 2, 3 };
		double[] input15 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 15, 30, 45 };
		final Computers.Arity15<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity15() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity15_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] input12 = { 1, 2, 3 };
		double[] input13 = { 1, 2, 3 };
		double[] input14 = { 1, 2, 3 };
		double[] input15 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 15, 30, 45 };
		name("test.addArrays") //
			.arity15() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) //
			.output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- 16-ary --

	/** Matches a 16-arity function using input types only. */
	@Test
	public void testArity16_IT_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double input15 = 1.;
		final Double input16 = 1.;
		final Double expected = 16.;
		final Functions.Arity16<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity16() //
				.inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertEquals(actual, expected);
	}

	/** Matches a 16-arity inplace1 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_1") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Matches a 16-arity inplace2 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_2") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Matches a 16-arity inplace3 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_3") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Matches a 16-arity inplace4 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_4") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Matches a 16-arity inplace5 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_5") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Matches a 16-arity inplace6 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_6") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Matches a 16-arity inplace7 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_7") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Matches a 16-arity inplace8 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_8") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Matches a 16-arity inplace9 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_9") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Matches a 16-arity inplace10 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_10") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace10();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input10, expected));
	}

	/** Matches a 16-arity inplace11 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_11") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace11();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input11, expected));
	}

	/** Matches a 16-arity inplace12 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI12() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_12") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace12();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input12, expected));
	}

	/** Matches a 16-arity inplace13 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI13() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_13") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace13();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input13, expected));
	}

	/** Matches a 16-arity inplace14 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI14() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_14") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace14();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input14, expected));
	}

	/** Matches a 16-arity inplace15 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI15() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_15<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_15") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace15();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input15, expected));
	}

	/** Matches a 16-arity inplace16 with a given output type. */
	@Test
	public void testArity16_IT_OU_matchI16() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_16<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_16") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.inplace16();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(input16, expected));
	}

	/** Matches a 16-arity function using input types + output type. */
	@Test
	public void testArity16_IT_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double input15 = 1.;
		final Double input16 = 1.;
		final Double expected = 16.;
		final Functions.Arity16<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity16().inType(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a 16-arity computer using input types + output type. */
	@Test
	public void testArity16_IT_OT_matchC() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 16, 32, 48 };
		final Computers.Arity16<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity16() //
				.inType(double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class, double[].class) //
				.outType(double[].class).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a 16-arity function using input value only. */
	@Test
	public void testArity16_IV_OU_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double input15 = 1.;
		final Double input16 = 1.;
		final Double expected = 16.;
		final Functions.Arity16<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, ?> op = //
			name("test.addDoubles") //
				.arity16() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.function();
		final Object actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertEquals(actual, expected);
	}

	/** Matches a 16-arity inplace1 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_1").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace1();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input1));
	}

	/** Matches a 16-arity inplace2 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_2").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace2();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input2));
	}

	/** Matches a 16-arity inplace3 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_3").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace3();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input3));
	}

	/** Matches a 16-arity inplace4 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_4").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace4();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input4));
	}

	/** Matches a 16-arity inplace5 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_5").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace5();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input5));
	}

	/** Matches a 16-arity inplace6 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_6").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace6();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input6));
	}

	/** Matches a 16-arity inplace7 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_7").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace7();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input7));
	}

	/** Matches a 16-arity inplace8 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_8").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace8();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input8));
	}

	/** Matches a 16-arity inplace9 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_9").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace9();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input9));
	}

	/** Matches a 16-arity inplace10 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_10").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace10();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input10));
	}

	/** Matches a 16-arity inplace11 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_11").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace11();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input11));
	}

	/** Matches a 16-arity inplace12 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI12() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_12").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace12();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input12));
	}

	/** Matches a 16-arity inplace13 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI13() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_13").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace13();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input13));
	}

	/** Matches a 16-arity inplace14 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI14() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_14").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace14();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input14));
	}

	/** Matches a 16-arity inplace15 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI15() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_15<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_15").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace15();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input15));
	}

	/** Matches a 16-arity inplace16 with a given output value. */
	@Test
	public void testArity16_IV_OU_matchI16() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		final Inplaces.Arity16_16<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.mulArrays16_16").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.inplace16();
		op.mutate(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertTrue(Arrays.equals(expected, input16));
	}

	/** Runs a 16-arity function using input value only. */
	@Test
	public void testArity16_IV_OU_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double input15 = 1.;
		final Double input16 = 1.;
		final Double expected = 16.;
		final Object actual = name("test.addDoubles") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.apply();
		assertEquals(actual, expected);
	}

	/** Runs a 16-arity inplace1 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI1() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_1") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate1();
		assertTrue(Arrays.equals(input1, expected));
	}

	/** Runs a 16-arity inplace2 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI2() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_2") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate2();
		assertTrue(Arrays.equals(input2, expected));
	}

	/** Runs a 16-arity inplace3 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI3() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_3") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate3();
		assertTrue(Arrays.equals(input3, expected));
	}

	/** Runs a 16-arity inplace4 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI4() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_4") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate4();
		assertTrue(Arrays.equals(input4, expected));
	}

	/** Runs a 16-arity inplace5 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI5() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_5") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate5();
		assertTrue(Arrays.equals(input5, expected));
	}

	/** Runs a 16-arity inplace6 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI6() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_6") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate6();
		assertTrue(Arrays.equals(input6, expected));
	}

	/** Runs a 16-arity inplace7 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI7() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_7") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate7();
		assertTrue(Arrays.equals(input7, expected));
	}

	/** Runs a 16-arity inplace8 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI8() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_8") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate8();
		assertTrue(Arrays.equals(input8, expected));
	}

	/** Runs a 16-arity inplace9 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI9() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_9") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate9();
		assertTrue(Arrays.equals(input9, expected));
	}

	/** Runs a 16-arity inplace10 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI10() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_10") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate10();
		assertTrue(Arrays.equals(input10, expected));
	}

	/** Runs a 16-arity inplace11 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI11() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_11") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate11();
		assertTrue(Arrays.equals(input11, expected));
	}

	/** Runs a 16-arity inplace12 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI12() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_12") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate12();
		assertTrue(Arrays.equals(input12, expected));
	}

	/** Runs a 16-arity inplace13 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI13() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_13") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate13();
		assertTrue(Arrays.equals(input13, expected));
	}

	/** Runs a 16-arity inplace14 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI14() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_14") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate14();
		assertTrue(Arrays.equals(input14, expected));
	}

	/** Runs a 16-arity inplace15 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI15() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_15") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate15();
		assertTrue(Arrays.equals(input15, expected));
	}

	/** Runs a 16-arity inplace16 with a given output value. */
	@Test
	public void testArity16_IV_OU_runI16() {
		final double[] input1 = { 1, 2, 3 };
		final double[] input2 = { 1, 2, 3 };
		final double[] input3 = { 1, 2, 3 };
		final double[] input4 = { 1, 2, 3 };
		final double[] input5 = { 1, 2, 3 };
		final double[] input6 = { 1, 2, 3 };
		final double[] input7 = { 1, 2, 3 };
		final double[] input8 = { 1, 2, 3 };
		final double[] input9 = { 1, 2, 3 };
		final double[] input10 = { 1, 2, 3 };
		final double[] input11 = { 1, 2, 3 };
		final double[] input12 = { 1, 2, 3 };
		final double[] input13 = { 1, 2, 3 };
		final double[] input14 = { 1, 2, 3 };
		final double[] input15 = { 1, 2, 3 };
		final double[] input16 = { 1, 2, 3 };
		final double[] expected = { 1.0, 65536.0, 4.3046721E7 };
		name("test.mulArrays16_16") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.mutate16();
		assertTrue(Arrays.equals(input16, expected));
	}

	/** Matches a 16-arity function using input value + output type. */
	@Test
	public void testArity16_IV_OT_matchF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double input15 = 1.;
		final Double input16 = 1.;
		final Double expected = 16.;
		final Functions.Arity16<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = //
			name("test.addDoubles").arity16().input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.outType(Double.class).function();
		final double actual = op.apply(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity16_IV_OT_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] input12 = { 1, 2, 3 };
		double[] input13 = { 1, 2, 3 };
		double[] input14 = { 1, 2, 3 };
		double[] input15 = { 1, 2, 3 };
		double[] input16 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 16, 32, 48 };
		final Computers.Arity16<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity16() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity16_IV_OT_runF() {
		final Double input1 = 1.;
		final Double input2 = 1.;
		final Double input3 = 1.;
		final Double input4 = 1.;
		final Double input5 = 1.;
		final Double input6 = 1.;
		final Double input7 = 1.;
		final Double input8 = 1.;
		final Double input9 = 1.;
		final Double input10 = 1.;
		final Double input11 = 1.;
		final Double input12 = 1.;
		final Double input13 = 1.;
		final Double input14 = 1.;
		final Double input15 = 1.;
		final Double input16 = 1.;
		final Double expected = 16.;
		final Object actual = name("test.addDoubles") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity16_IV_OV_matchC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] input12 = { 1, 2, 3 };
		double[] input13 = { 1, 2, 3 };
		double[] input14 = { 1, 2, 3 };
		double[] input15 = { 1, 2, 3 };
		double[] input16 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 16, 32, 48 };
		final Computers.Arity16<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = //
			name("test.addArrays") //
				.arity16() //
				.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
				.output(actual).computer();
		op.compute(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity16_IV_OV_runC() {
		double[] input1 = { 1, 2, 3 };
		double[] input2 = { 1, 2, 3 };
		double[] input3 = { 1, 2, 3 };
		double[] input4 = { 1, 2, 3 };
		double[] input5 = { 1, 2, 3 };
		double[] input6 = { 1, 2, 3 };
		double[] input7 = { 1, 2, 3 };
		double[] input8 = { 1, 2, 3 };
		double[] input9 = { 1, 2, 3 };
		double[] input10 = { 1, 2, 3 };
		double[] input11 = { 1, 2, 3 };
		double[] input12 = { 1, 2, 3 };
		double[] input13 = { 1, 2, 3 };
		double[] input14 = { 1, 2, 3 };
		double[] input15 = { 1, 2, 3 };
		double[] input16 = { 1, 2, 3 };
		double[] actual = { 0, 0, 0 };
		double[] expected = { 16, 32, 48 };
		name("test.addArrays") //
			.arity16() //
			.input(input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) //
			.output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}

	// -- Helper methods --

	private OpBuilder name(String opName) {
		return ops.op(opName);
	}
}
