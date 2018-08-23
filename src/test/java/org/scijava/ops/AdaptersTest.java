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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.ops.base.OpService;
import org.scijava.ops.impl.math.Add.MathAddOp;
import org.scijava.types.Nil;

public class AdaptersTest {

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
	public void testComputerAsFunction() {
		Class<double[]> cArray = double[].class;
		final BiComputer<double[], double[], double[]> computer = ops.findOp( //
				new Nil<BiComputer<double[], double[], double[]>>() {
				}, //
				new Type[] { MathAddOp.class }, //
				new Type[] { cArray, cArray, cArray }, //
				new Type[] { cArray } //
		);

		BiFunction<double[], double[], double[]> computerAsFunction = Adapters.asBiFunction(computer, (arr1, arr2) -> {
			return new double[arr1.length];
		});

		final double[] a1 = { 3, 5, 7 };
		final double[] a2 = { 2, 4, 9 };
		double[] result = computerAsFunction.apply(a1, a2);
		assert OpsTest.arrayEquals(result, 5.0, 9.0, 16.0);
	}

	@Test
	public void testFunctionAsComputer() {
		Class<double[]> c = double[].class;
		// look up a function: Double result = math.add(Double v1, Double v2)
		BiFunction<double[], double[], double[]> function = ops.findOp( //
				new Nil<BiFunction<double[], double[], double[]>>() {
				}, //
				new Type[] { MathAddOp.class }, //
				new Type[] { c, c }, //
				new Type[] { c } //
		);

		BiComputer<double[], double[], double[]> functionAsComputer = Adapters.asBiComputer(function, (from, to) -> {
			for (int i = 0; i < from.length; i++) {
				to[i] = from[i];
			}
		});

		final double[] a1 = { 3, 5, 7 };
		final double[] a2 = { 2, 4, 9 };
		final double[] result = new double[a2.length];
		functionAsComputer.compute(a1, a2, result);
		assert OpsTest.arrayEquals(result, 5.0, 9.0, 16.0);
	}
}