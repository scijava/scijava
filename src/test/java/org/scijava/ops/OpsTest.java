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

import com.google.common.collect.Streams;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.NullaryComputer;
import org.scijava.ops.core.inplace.BiInplace1;
import org.scijava.ops.core.inplace.Inplace;
import org.scijava.types.Nil;

public class OpsTest extends AbstractTestEnvironment {

	private static Nil<Double> nilDouble = new Nil<Double>() {
	};

	private static Nil<double[]> nilDoubleArray = new Nil<double[]>() {
	};

	@Test
	public void unaryFunction() {
		// Look up a function type safe
		Function<Double, Double> sqrtFunction = ops().findOp( //
				"math.sqrt", new Nil<Function<Double, Double>>() {
				}, //
				new Nil[] { nilDouble }, //
				nilDouble//
		);
		double answer = sqrtFunction.apply(16.0);
		assert 4.0 == answer;
	}

	@Test
	public void binaryFunction() {
		// look up a function: Double result = math.add(Double v1, Double v2)
		BiFunction<Double, Double, Double> addFunction = ops().findOp( //
				"math.add", new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Nil[] { nilDouble, nilDouble }, //
				nilDouble//
		);
		assert 3.0 == addFunction.apply(1.0, 2.0);
	}

	@Test
	public void nullaryComputer() {
		NullaryComputer<double[]> sqrtComputer = ops().findOp( //
				"math.zero", new Nil<NullaryComputer<double[]>>() {
				}, //
				new Nil[] { nilDoubleArray }, //
				nilDoubleArray//
		);
		double[] result = new double[] { 1.2323, 13231.1232, 37373773};
		sqrtComputer.compute(result);
		assert arrayEquals(result, 0.0, 0.0, 0.0);
	}
	
	@Test
	public void unaryComputer() {
		Computer<double[], double[]> sqrtComputer = ops().findOp( //
				"math.sqrt", new Nil<Computer<double[], double[]>>() {
				}, //
				new Nil[] { nilDoubleArray, nilDoubleArray }, //
				nilDoubleArray//
		);
		double[] result = new double[2];
		sqrtComputer.compute(new double[] { 16.0, 81.0 }, result);
		assert arrayEquals(result, 4.0, 9.0);
	}

	@Test
	public void binaryComputer() {
		BiComputer<double[], double[], double[]> computer = ops().findOp( //
				"math.add", new Nil<BiComputer<double[], double[], double[]>>() {
				}, //
				new Nil[] { nilDoubleArray, nilDoubleArray, nilDoubleArray }, //
				nilDoubleArray//
		);
		double[] a1 = { 3, 5, 7 };
		double[] a2 = { 2, 4, 9 };
		double[] result = new double[a2.length];
		computer.compute(a1, a2, result);
		assert arrayEquals(result, 5.0, 9.0, 16.0);
	}

	@Test
	public void unaryInplace() {
		Inplace<double[]> inplaceSqrt = ops().findOp( //
				"math.sqrt", new Nil<Inplace<double[]>>() {
				}, //
				new Nil[] { nilDoubleArray }, //
				nilDoubleArray//
		);
		double[] a1 = { 4, 100, 36 };
		inplaceSqrt.mutate(a1);
		assert arrayEquals(a1, 2.0, 10.0, 6.0);
	}

	@Test
	public void binaryInplace() {
		BiInplace1<double[], double[]> inplaceAdd = ops().findOp( //
				"math.add", new Nil<BiInplace1<double[], double[]>>() {
				}, //
				new Nil[] { nilDoubleArray, nilDoubleArray }, //
				nilDoubleArray//
		);
		double[] a1 = { 3, 5, 7 };
		double[] a2 = { 2, 4, 9 };
		inplaceAdd.mutate(a1, a2);
		assert arrayEquals(a1, 5.0, 9.0, 16.0);
	}

	@Test
	public void testSecondaryInputs() {
		Function<Double, Double> power3 = ops().findOp( //
				"math.pow", new Nil<Function<Double, Double>>() {
				}, //
				new Nil[] { nilDouble, nilDouble }, //
				nilDouble, //
				3.0//
		);
		assert power3.apply(2.0).equals(8.0);

		BiFunction<Double, Double, Double> powerFunction = ops().findOp( //
				"math.pow", new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Nil[] { nilDouble, nilDouble }, //
				nilDouble//
		);
		assert powerFunction.apply(2.0, 3.0).equals(8.0);

		Computer<double[], double[]> power3Arrays = ops().findOp( //
				"math.pow", new Nil<Computer<double[], double[]>>() {
				}, //
				new Nil[] { nilDoubleArray, nilDoubleArray, nilDouble }, //
				nilDoubleArray, //
				3.0//
		);
		double[] result = new double[3];
		power3Arrays.compute(new double[] { 1.0, 2.0, 3.0 }, result);
		assert arrayEquals(result, 1.0, 8.0, 27.0);
	}

	@Test
	public void genericFunction() {
		Nil<Iterable<Double>> nilIterableDouble = new Nil<Iterable<Double>>() {};

		// Generic typed BiFunction matches, however the given input types do not
		Exception error = null;
		try {
			@SuppressWarnings("unused")
			BiFunction<Iterable<Double>, Iterable<Double>, Iterable<Double>> addDoubleIters = ops().findOp( //
					"math.add", //
					new Nil<BiFunction<Iterable<Double>, Iterable<Double>, Iterable<Double>>>() {
					}, //
					new Nil[] { new Nil<Double>() {}, nilIterableDouble }, //
					nilIterableDouble//
					);
		} catch (Exception e) {
			error = e;
		}
		assert error != null;
		error = null;
		
		// Generic typed BiFunction does not matches
		try {
			@SuppressWarnings("unused")
			BiFunction<Double, Iterable<Double>, Iterable<Double>> addDoubleIters = ops().findOp( //
					"math.add", //
					new Nil<BiFunction<Double, Iterable<Double>, Iterable<Double>>>() {
					}, //
					new Nil[] { nilIterableDouble, nilIterableDouble }, //
					nilIterableDouble//
					);
		} catch (Exception e) {
			error = e;
		}
		assert error != null;
		error = null;
		
		// Output does not match
		try {
			@SuppressWarnings("unused")
			BiFunction<Iterable<Double>, Iterable<Double>, Iterable<Double>> addDoubleIters = ops().findOp( //
					"math.add", //
					new Nil<BiFunction<Iterable<Double>, Iterable<Double>, Iterable<Double>>>() {
					}, //
					new Nil[] { nilIterableDouble, nilIterableDouble }, //
					new Nil<Double>() {}//
					);
		} catch (Exception e) {
			error = e;
		}
		assert error != null;
		error = null;

		// We have a generic function which adds two iterables of numbers and gives an iterable of double
		BiFunction<Iterable<Double>, Iterable<Double>, Iterable<Double>> addDoubleIters = ops().findOp( //
				"math.add", //
				new Nil<BiFunction<Iterable<Double>, Iterable<Double>, Iterable<Double>>>() {
				}, //
				new Nil[] { nilIterableDouble, nilIterableDouble }, //
				nilIterableDouble//
				);
		
		Iterable<Double> res = addDoubleIters.apply(Arrays.asList(1d, 2d, 3d, 4d), Arrays.asList(1.5, 1.6, 2.3, 2.0));
		arrayEquals(Streams.stream(res).mapToDouble(d -> d).toArray(), 2.5, 3.6, 5.3, 6.0);
	}
}
