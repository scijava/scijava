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
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.ops.math.Add.MathAddDoublesFunction;
import org.scijava.ops.math.Add.MathAddOp;
import org.scijava.ops.math.Power.MathPowerOp;
import org.scijava.ops.math.Sqrt.MathSqrtOp;
import org.scijava.struct.StructInstance;
import org.scijava.types.Nil;

public class OpsTest extends AbstractTestEnvironment {

	@Test
	@SuppressWarnings("unchecked")
	public void unaryFunction() {
		// Look up a function type safe
		Class<Double> c = Double.class;
		Function<Double, Double> sqrtFunction = ops().findOp( //
				MathSqrtOp.class, new Nil<Function<Double, Double>>() {
				}, //
				new Type[] { c }, //
				c//
		);
		double answer = sqrtFunction.apply(16.0);
		assert 4.0 == answer;

		// Look up a function unsafe
		MathSqrtOp sqrtOp = ops().findOp(MathSqrtOp.class, null, new Type[] { c }, c);
		sqrtFunction = (Function<Double, Double>) sqrtOp;
		assert 4.0 == sqrtFunction.apply(16.0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void binaryFunction() {
		Class<Double> c = Double.class;
		// look up a function: Double result = math.add(Double v1, Double v2)
		BiFunction<Double, Double, Double> addFunction = ops().findOp( //
				MathAddOp.class, new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Type[] { c, c }, //
				c//
		);
		assert 3.0 == addFunction.apply(1.0, 2.0);

		// Look up a function unsafe
		MathAddOp addOp = ops().findOp(MathAddOp.class, null, new Type[] { c, c }, c);
		addFunction = (BiFunction<Double, Double, Double>) addOp;
		assert 86.0 == addFunction.apply(10.0, 76.0);

		// look up a specific implementation
		addFunction = ops().findOp( //
				MathAddDoublesFunction.class, new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Type[] { c, c }, //
				c//
		);
		assert 86.0 == addFunction.apply(10.0, 76.0);

		// Look up a function unsafe
		MathAddDoublesFunction addDoublesOp = ops().findOp(MathAddDoublesFunction.class, null, new Type[] { c, c }, c);
		addFunction = (BiFunction<Double, Double, Double>) addDoublesOp;
		assert 86.0 == addFunction.apply(10.0, 76.0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void unaryComputer() {
		Class<double[]> cArray = double[].class;
		Computer<double[], double[]> sqrtComputer = ops().findOp( //
				MathSqrtOp.class, new Nil<Computer<double[], double[]>>() {
				}, //
				new Type[] { cArray, cArray }, //
				cArray//
		);
		double[] result = new double[2];
		sqrtComputer.compute(new double[] { 16.0, 81.0 }, result);
		assert arrayEquals(result, 4.0, 9.0);

		MathSqrtOp sqrtOp = ops().findOp(MathSqrtOp.class, null, new Type[] { cArray, cArray }, cArray);
		sqrtComputer = (Computer<double[], double[]>) sqrtOp;
		result = new double[2];
		sqrtComputer.compute(new double[] { 16.0, 81.0 }, result);
		assert arrayEquals(result, 4.0, 9.0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void binaryComputer() {
		Class<double[]> cArray = double[].class;
		BiComputer<double[], double[], double[]> computer = ops().findOp( //
				MathAddOp.class, new Nil<BiComputer<double[], double[], double[]>>() {
				}, //
				new Type[] { cArray, cArray, cArray }, //
				cArray//
		);
		double[] a1 = { 3, 5, 7 };
		double[] a2 = { 2, 4, 9 };
		double[] result = new double[a2.length];
		computer.compute(a1, a2, result);
		assert arrayEquals(result, 5.0, 9.0, 16.0);

		MathAddOp addOp = ops().findOp(MathAddOp.class, null, new Type[] { cArray, cArray, cArray }, //
				cArray);
		computer = (BiComputer<double[], double[], double[]>) addOp;
		a1 = new double[] { 3, 5, 7 };
		a2 = new double[] { 2, 4, 9 };
		result = new double[a2.length];
		computer.compute(a1, a2, result);
		assert arrayEquals(result, 5.0, 9.0, 16.0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void unaryInplace() {
		Class<double[]> cArray = double[].class;
		Inplace<double[]> inplaceSqrt = ops().findOp( //
				MathSqrtOp.class, new Nil<Inplace<double[]>>() {
				}, //
				new Type[] { cArray }, //
				cArray//
		);
		double[] a1 = { 4, 100, 36 };
		inplaceSqrt.mutate(a1);
		assert arrayEquals(a1, 2.0, 10.0, 6.0);

		MathSqrtOp sqrtOp = ops().findOp(MathSqrtOp.class, null, new Type[] { cArray }, //
				cArray);
		inplaceSqrt = (Inplace<double[]>) sqrtOp;
		a1 = new double[] { 4, 100, 36 };
		inplaceSqrt.mutate(a1);
		assert arrayEquals(a1, 2.0, 10.0, 6.0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void binaryInplace() {
		Class<double[]> cArray = double[].class;
		BiInplace1<double[], double[]> inplaceAdd = ops().findOp( //
				MathAddOp.class, new Nil<BiInplace1<double[], double[]>>() {
				}, //
				new Type[] { cArray, cArray }, //
				cArray//
		);
		double[] a1 = { 3, 5, 7 };
		double[] a2 = { 2, 4, 9 };
		inplaceAdd.mutate(a1, a2);
		assert arrayEquals(a1, 5.0, 9.0, 16.0);
		
		MathAddOp addOp = ops().findOp(MathAddOp.class, null, new Type[] { cArray, cArray }, //
				cArray);
		try {
			// will fail as 'addOp' will be matched to a function
			inplaceAdd = (BiInplace1<double[], double[]>) addOp;
		} catch (ClassCastException e) {
			// expected as the add double arrays function has a higher priority than the inplace
		}
	}

	@Test
	public void testSecondaryInputs() {
		Class<Double> c = Double.class;
		StructInstance<Function<Double, Double>> powerConstantFunctionStructInstance = ops().findOpInstance( //
				MathPowerOp.class, new Nil<Function<Double, Double>>() {
				}, //
				new Type[] { c, c }, //
				c, //
				3.0//
		);
		Function<Double, Double> power3 = powerConstantFunctionStructInstance.object();
		assert power3.apply(2.0).equals(8.0);

		BiFunction<Double, Double, Double> powerFunction = ops().findOp( //
				MathPowerOp.class, new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Type[] { c, c }, //
				c//
		);
		assert powerFunction.apply(2.0, 3.0).equals(8.0);

		Class<double[]> cArray = double[].class;
		StructInstance<Computer<double[], double[]>> powerConstantComputerStructInstance = ops().findOpInstance( //
				MathPowerOp.class, new Nil<Computer<double[], double[]>>() {
				}, //
				new Type[] { cArray, cArray, c }, //
				cArray, //
				3.0//
		);
		Computer<double[], double[]> power3Arrays = powerConstantComputerStructInstance.object();
		double[] result = new double[3];
		power3Arrays.compute(new double[] { 1.0, 2.0, 3.0 }, result);
		assert arrayEquals(result, 1.0, 8.0, 27.0);
	}
}
