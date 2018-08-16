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

import org.junit.Test;
import org.scijava.types.Nil;

public class OpsTest {

	@Test
	public void testOps() {
		final String opName = "math.add";

		final Nil<Double> nilDouble = new Nil<Double>() {};
		final Type[] inTypes = { nilDouble.getType(), nilDouble.getType() };
		final Type[] outTypes = { nilDouble.getType() };

		// look up a function: Double result = math.add(Double v1, Double v2)
		final BinaryFunctionOp<Double, Double, Double> function = find( //
			opName, //
			new Nil<BinaryFunctionOp<Double, Double, Double>>()
			{}, //
			inTypes, //
			outTypes //
		);
		// execute the function
		final double answer = function.apply(1.0, 2.0);
		System.out.println("Function answer = " + answer);

		// look up a computer: math.add(BOTH double[] result, double[] v1, double[]
		// v2)
		final BinaryComputerOp<double[], double[], double[]> computer = find( //
			opName, //
			new Nil<BinaryComputerOp<double[], double[], double[]>>()
			{}, //
			inTypes, //
			outTypes //
		);
		final double[] a1 = { 3, 5, 7 };
		final double[] a2 = { 2, 4, 9 };
		final double[] result = new double[a2.length];
		computer.compute(a1, a2, result);
		System.out.println("Computer result = " + Arrays.toString(result));
	}

	private <T> T find(final String opName, final Nil<T> opType,
		final Type[] inTypes, final Type[] outTypes)
	{
		// TODO
		return null;
	}
}
