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
import org.scijava.function.Inplaces;
import org.scijava.ops.api.OpBuilder;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.math.Add;
import org.scijava.ops.engine.math.Sqrt;
import org.scijava.types.Nil;

public class InplacesTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new Sqrt());
		ops.register(new Add());
	}

	private static Nil<double[]> nilDoubleArray = new Nil<>() {};

	@Test
	public void testUnaryInplaces() {
		Inplaces.Arity1<double[]> inplaceSqrt = OpBuilder.matchInplace(ops,
			"math.sqrt", nilDoubleArray);
		final double[] a1 = { 4, 100, 36 };
		inplaceSqrt.mutate(a1);
		assert arrayEquals(a1, 2.0, 10.0, 6.0);
	}

	@Test
	public void testBinaryInplaces() {
		final Inplaces.Arity2_1<double[], double[]> inplaceAdd = OpBuilder
			.matchInplace2_1(ops, "math.add", nilDoubleArray, nilDoubleArray);
		final double[] a1 = { 3, 5, 7 };
		final double[] a2 = { 2, 4, 9 };
		inplaceAdd.mutate(a1, a2);
		assert arrayEquals(a1, 5.0, 9.0, 16.0);
	}
}
