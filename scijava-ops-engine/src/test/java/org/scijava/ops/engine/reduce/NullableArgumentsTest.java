/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.reduce;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Container;
import org.scijava.function.Functions;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.describe.BaseDescriptors;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;

import java.util.Arrays;

public class NullableArgumentsTest extends AbstractTestEnvironment //
	implements OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new BaseDescriptors<>());
		ops.register(new NullableArgumentsTest());
		ops.register(new TestOpNullableArg());
	}

	@Test
	public void testClassWithTwoNullables() {
		Double sum = ops.op("test.nullableAdd").arity3().input(2.0, 5.0, 7.0)
			.outType(Double.class).apply();
		Double expected = 14.0;
		Assertions.assertEquals(expected, sum);
	}

	@Test
	public void testClassWithOneNullable() {
		Double sum = ops.op("test.nullableAdd").arity2().input(2.0, 5.0).outType(
			Double.class).apply();
		Double expected = 7.0;
		Assertions.assertEquals(expected, sum);
	}

	@Test
	public void testClassWithoutNullables() {
		Double sum = ops.op("test.nullableAdd").arity1().input(2.0).outType(
			Double.class).apply();
		Double expected = 2.0;
		Assertions.assertEquals(expected, sum);
	}

	@OpField(names = "test.nullableMultiply")
	public final Computers.Arity3<Double[], Double[], Double[], Double[]> nullableField =
		new Computers.Arity3<>()
		{

			@Override
			public void compute(Double[] in1, @Nullable Double[] in2,
				@Nullable Double[] in3, Double[] out)
		{
				if (in2 == null) {
					in2 = new Double[in1.length];
					Arrays.fill(in2, 1.);
				}
				if (in3 == null) {
					in3 = new Double[in1.length];
					Arrays.fill(in3, 1.);
				}
				for (int i = 0; i < in1.length; i++) {
					out[i] = in1[i] * in2[i] * in3[i];
				}
			}
		};

	@Test
	public void testFieldWithTwoNullables() {
		Double[] d1 = { 2.0 };
		Double[] d2 = { 5.0 };
		Double[] d3 = { 7.0 };
		Double[] o = { 50.0 };
		ops.op("test.nullableMultiply").arity3().input(d1, d2, d3).output(o)
			.compute();
		Double expected = 70.0;
		Assertions.assertEquals(expected, o[0]);
	}

	@Test
	public void testFieldWithOneNullable() {
		Double[] d1 = { 2.0 };
		Double[] d2 = { 5.0 };
		Double[] o = { 50.0 };
		ops.op("test.nullableMultiply").arity2().input(d1, d2).output(o).compute();
		Double expected = 10.0;
		Assertions.assertEquals(expected, o[0]);
	}

	@Test
	public void testFieldWithoutNullables() {
		Double[] d1 = { 2.0 };
		Double[] o = { 50.0 };
		ops.op("test.nullableMultiply").arity1().input(d1).output(o).compute();
		Double expected = 2.0;
		Assertions.assertEquals(expected, o[0]);
	}

	@OpMethod(names = "test.nullableConcatenate", type = Functions.Arity3.class)
	public static String nullableMethod(String in1, @Nullable String in2,
		@Nullable String in3)
	{
		if (in2 == null) in2 = "";
		if (in3 == null) in3 = "";
		return in1.concat(in2).concat(in3);
	}

	@Test
	public void testMethodWithTwoNullables() {
		String out = ops.op("test.nullableConcatenate").arity3().input("a", "b",
			"c").outType(String.class).apply();
		String expected = "abc";
		Assertions.assertEquals(expected, out);
	}

	@Test
	public void testMethodWithOneNullable() {
		String out = ops.op("test.nullableConcatenate").arity2().input("a", "b")
			.outType(String.class).apply();
		String expected = "ab";
		Assertions.assertEquals(expected, out);
	}

	@Test
	public void testMethodWithoutNullables() {
		String out = ops.op("test.nullableConcatenate").arity1().input("a").outType(
			String.class).apply();
		String expected = "a";
		Assertions.assertEquals(expected, out);
	}

	private static final String PERMUTED_NAME = "test.nullableOr";

	@OpMethod(names = PERMUTED_NAME, type = Computers.Arity3_3.class)
	public static void nullablePermutedComputer( //
		int[] in1, //
		@Nullable int[] in2, //
		@Container int[] out, //
		@Nullable int[] in3 //
	) {
		if (in2 == null) in2 = new int[in1.length];
		if (in3 == null) in3 = new int[in1.length];
		for (int i = 0; i < out.length; i++) {
			out[i] = in1[i] | in2[i] | in3[i];
		}
	}

	@Test
	public void testPermutedMethodWithTwoNullables() {
		int[] out = new int[1];
		ops.op(PERMUTED_NAME).arity3().input( //
			new int[] { 1 }, //
			new int[] { 2 }, //
			new int[] { 4 } //
		).output(out).compute();
		Assertions.assertEquals(7, out[0]);
	}

	@Test
	public void testPermutedMethodWithOneNullable() {
		int[] out = new int[1];
		ops.op(PERMUTED_NAME).arity2().input( //
			new int[] { 1 }, //
			new int[] { 2 } //
		).output(out).compute();
		Assertions.assertEquals(3, out[0]);
	}

	@Test
	public void testPermutedMethodWithoutNullables() {
		int[] out = new int[1];
		ops.op(PERMUTED_NAME).arity1().input( //
			new int[] { 1 } //
		).output(out).compute();
		Assertions.assertEquals(1, out[0]);
	}

	@Test
	public void testNullableHelp() {
		// Add in a couple Ops needed for conversion
		var expected = PERMUTED_NAME +
			":\n\t- (number[], number[] = null, @CONTAINER number[], number[] = null) -> None";
		Assertions.assertEquals(ops.op("test.nullableOr").help(), expected);
	}
}
