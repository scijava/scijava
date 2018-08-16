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
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.struct.ItemIO;
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
	public void testOps() {
		final Nil<Double> nilDouble = new Nil<Double>() {};
		final Type[] inTypes = { nilDouble.getType(), nilDouble.getType() };
		final Type[] outTypes = { nilDouble.getType() };

		// look up a function: Double result = math.add(Double v1, Double v2)
		final BinaryFunctionOp<Double, Double, Double> function = find( //
			new Nil<BinaryFunctionOp<Double, Double, Double>>() {}, //
			Arrays.asList(MathAddOp.class), //
			inTypes, //
			outTypes //
		);
		// execute the function
		final double answer = function.apply(1.0, 2.0);
		System.out.println("Function answer = " + answer);

		// look up a computer: math.add(BOTH double[] result, double[] v1, double[]
		// v2)
		final BinaryComputerOp<double[], double[], double[]> computer = find( //
			new Nil<BinaryComputerOp<double[], double[], double[]>>() {}, //
			Arrays.asList(MathAddOp.class), //
			inTypes, //
			outTypes //
		);
		final double[] a1 = { 3, 5, 7 };
		final double[] a2 = { 2, 4, 9 };
		final double[] result = new double[a2.length];
		computer.compute(a1, a2, result);
		System.out.println("Computer result = " + Arrays.toString(result));
	}

	private <T> T find(final Nil<T> opType, final List<Type> opAdditionalTypes,
		final Type[] inTypes, final Type[] outTypes)
	{
		// FIXME - createTypes does not support multiple additional types,
		// or multiple output types. We will need to generalize this.
		final OpRef ref = OpRef.createTypes(opType.getType(), //
			opAdditionalTypes.get(0), outTypes[0], (Object[]) inTypes);
		@SuppressWarnings("unchecked")
		final T op = (T) ops.op(ref);
		return op;
	}

	private interface MathAddOp extends Op {}

	@Plugin(type = MathAddOp.class)
	@Parameter(key = "number1")
	@Parameter(key = "number2")
	@Parameter(key = "result", type = ItemIO.OUTPUT)
	public static class MathAddDoubles implements MathAddOp, BinaryFunctionOp<Double, Double, Double> {

		@Override
		public Double apply(Double t, Double u) {
			return t + u;
		}
	}

	@Plugin(type = MathAddOp.class)
	@Parameter(key = "integer1")
	@Parameter(key = "integer2")
	@Parameter(key = "resultInteger", type = ItemIO.OUTPUT)
	public static class MathAddBigIntegers implements MathAddOp, BinaryFunctionOp<BigInteger, BigInteger, BigInteger> {

		@Override
		public BigInteger apply(BigInteger t, BigInteger u) {
			return t.add(u);
		}
	}

	@Plugin(type = MathAddOp.class)
	@Parameter(key = "array1")
	@Parameter(key = "array2")
	@Parameter(key = "resultArray", type = ItemIO.BOTH)
	public static class MathAddDoubleArrays implements MathAddOp, BinaryComputerOp<double[], double[], double[]> {

		@Override
		public void compute(double[] in1, double[] in2, double[] out) {
			for (int i = 0; i < out.length; i++) {
				out[i] = in1[i] + in2[i];
			}
		}
	}
}
