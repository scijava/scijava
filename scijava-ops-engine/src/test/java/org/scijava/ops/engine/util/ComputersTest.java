/*
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.OpBuilder;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.math.Add;
import org.scijava.ops.engine.math.Sqrt;
import org.scijava.types.Nil;

public class ComputersTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new Sqrt());
		ops.register(new Add());
	}

	private static Nil<double[]> nilDoubleArray = new Nil<>() {};

	@Test
	public void testUnaryComputers() {
		Computers.Arity1<double[], double[]> sqrtComputer = OpBuilder.matchComputer(
			ops, "math.sqrt", nilDoubleArray, nilDoubleArray);

		double[] result = new double[3];
		sqrtComputer.compute(new double[] { 4.0, 100.0, 25.0 }, result);
		arrayEquals(result, 2d, 1d, 5d);
	}

	@Test
	public void testBinaryComputers() {
		Computers.Arity2<double[], double[], double[]> addComputer = //
			OpBuilder.matchComputer(ops, "math.add", nilDoubleArray, nilDoubleArray,
				nilDoubleArray);

		double[] result = new double[3];
		addComputer.compute(new double[] { 4.0, 100.0, 25.0 }, new double[] { 4d,
			10d, 1.5 }, result);
		arrayEquals(result, 8d, 110d, 26.5);
	}
}
