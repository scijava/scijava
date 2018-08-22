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

package org.scijava.ops;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.ops.base.OpService;
import org.scijava.ops.impl.math.Add.MathAddDoublesFunction;
import org.scijava.ops.impl.math.Add.MathAddOp;
import org.scijava.ops.impl.math.Power.MathPowerOp;
import org.scijava.ops.impl.math.Sqrt.MathSqrtOp;
import org.scijava.struct.StructInstance;
import org.scijava.types.Nil;

public class OpsTest {

	private Context context;
	private OpService ops;

	@Before
	public void setUp() {
		context = new Context(OpService.class);
		ops = context.service(OpService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
		context = null;
		ops = null;
	}

	@Test
	public void testSecondaryInputs() {
		Class<Double> c = Double.class;
		StructInstance<Function<Double, Double>> powerConstantFunctionStructInstance = ops.findStructInstance( //
				new Nil<Function<Double, Double>>() {
				}, //
				new Type[] { MathPowerOp.class }, //
				new Type[] { c, c }, //
				new Type[] { c } //
		);
		powerConstantFunctionStructInstance.member("exponent").set(3.0);
		Function<Double, Double> power3 = powerConstantFunctionStructInstance.object();
		assert power3.apply(2.0).equals(8.0);
		
		BiFunction<Double, Double, Double> powerFunction = ops.findOp( //
				new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Type[] { MathPowerOp.class }, //
				new Type[] { c, c }, //
				new Type[] { c } //
		);
		assert powerFunction.apply(2.0, 3.0).equals(8.0);
		
		Class<double[]> cArray = double[].class;
		StructInstance<Computer<double[], double[]>> powerConstantComputerStructInstance = ops.findStructInstance( //
				new Nil<Computer<double[], double[]>>() {
				}, //
				new Type[] { MathPowerOp.class }, //
				new Type[] { cArray, cArray, c }, //
				new Type[] { cArray } //
		);
		powerConstantComputerStructInstance.member("exponent").set(3.0);
		Computer<double[], double[]> power3Arrays = powerConstantComputerStructInstance.object();
		double[] result = new double[3];
		power3Arrays.compute(new double[] {1.0, 2.0, 3.0}, result);
		assert arrayEquals(result, 1.0, 8.0, 27.0);
	}

	@Test
	public void testUnaryOps() {
		Class<Double> c = Double.class;
		Function<Double, Double> sqrtFunction = ops.findOp( //
				new Nil<Function<Double, Double>>() {
				}, //
				new Type[] { MathSqrtOp.class }, //
				new Type[] { c }, //
				new Type[] { c } //
		);
		// execute the function
		double answer = sqrtFunction.apply(16.0);
		assert 4.0 == answer;

		Class<double[]> cArray = double[].class;
		Computer<double[], double[]> sqrtComputer = ops.findOp( //
				new Nil<Computer<double[], double[]>>() {
				}, //
				new Type[] { MathSqrtOp.class }, //
				new Type[] { cArray, cArray }, //
				new Type[] { cArray } //
		);
		// execute the function
		double[] result = new double[2];
		sqrtComputer.compute(new double[] { 16.0, 81.0 }, result);
		assert arrayEquals(result, 4.0, 9.0);
	}

	@Test
	public void testBinaryOps() {
		Class<Double> c = Double.class;
		// look up a function: Double result = math.add(Double v1, Double v2)
		BiFunction<Double, Double, Double> function = ops.findOp( //
				new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Type[] { MathAddOp.class }, //
				new Type[] { c, c }, //
				new Type[] { c } //
		);
		// execute the function
		double answer = function.apply(1.0, 2.0);
		assert 3.0 == answer;

		// look up a specific implementation
		function = ops.findOp( //
				new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Type[] { MathAddDoublesFunction.class }, //
				new Type[] { c, c }, //
				new Type[] { c } //
		);
		answer = function.apply(10.0, 76.0);
		assert 86.0 == answer;

		// look up a computer: math.add(BOTH double[] result, double[] v1,
		// double[] v2)
		Class<double[]> cArray = double[].class;
		final BiComputer<double[], double[], double[]> computer = ops.findOp( //
				new Nil<BiComputer<double[], double[], double[]>>() {
				}, //
				new Type[] { MathAddOp.class }, //
				new Type[] { cArray, cArray, cArray }, //
				new Type[] { cArray } //
		);
		final double[] a1 = { 3, 5, 7 };
		final double[] a2 = { 2, 4, 9 };
		final double[] result = new double[a2.length];
		computer.compute(a1, a2, result);
		assert arrayEquals(result, 5.0, 9.0, 16.0);
	}
	
	private boolean arrayEquals(double[] arr1, Double... arr2) {
		return Arrays.deepEquals(Arrays.stream(arr1).boxed().toArray(Double[]::new), arr2);
	}
}
