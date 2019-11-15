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

import static org.scijava.ops.TestUtils.argsToString;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.ops.function.Functions;
import org.scijava.ops.types.Nil;

/**
 * @author Marcel Wiedenmann
 */
public class InplaceToFunctionTransformTest extends AbstractTestEnvironment {

	private static final Nil<Byte> byteNil = new Nil<Byte>() {};

	private static final Nil<Double> doubleNil = new Nil<Double>() {};

	private static final Nil<Float> floatNil = new Nil<Float>() {};

	private static final Nil<Integer> integerNil = new Nil<Integer>() {};

	private static final Nil<AtomicReference<String>> atomicStringNil = new Nil<AtomicReference<String>>() {};

	private static final String hello = "hello";

	@Test
	public void testInplace1ToFunction1() {
		final Function<AtomicReference<String>, AtomicReference<String>> f = Functions.match(ops,
			"test.inplace1ToFunction1TestOp", atomicStringNil, atomicStringNil);
		final AtomicReference<String> io = new AtomicReference<>(hello);
		final AtomicReference<String> out = f.apply(io);
		assert io == out;
		assertOutEquals(hello + " inplace", out);
	}

	@Test
	public void testInplace2_1ToFunction2() {
		final BiFunction<AtomicReference<String>, Byte, AtomicReference<String>> f = Functions.match(ops,
			"test.inplace2_1ToFunction2TestOp", atomicStringNil, byteNil, atomicStringNil);
		final AtomicReference<String> io = new AtomicReference<>(hello);
		final byte in2 = 22;
		final AtomicReference<String> out = f.apply(io, in2);
		assert io == out;
		assertOutEquals(argsToString(hello, in2), out);
	}

	@Test
	public void testInplace2_2ToFunction2() {
		final BiFunction<Byte, AtomicReference<String>, AtomicReference<String>> f = Functions.match(ops,
			"test.inplace2_2ToFunction2TestOp", byteNil, atomicStringNil, atomicStringNil);
		final byte in1 = 11;
		final AtomicReference<String> io = new AtomicReference<>(hello);
		final AtomicReference<String> out = f.apply(in1, io);
		assert io == out;
		assertOutEquals(argsToString(in1, hello), out);
	}

	@Test
	public void testInplace3_1ToFunction3() {
		final Functions.Arity3<AtomicReference<String>, Byte, Double, AtomicReference<String>> f = Functions.match(ops,
			"test.inplace3_1ToFunction3TestOp", atomicStringNil, byteNil, doubleNil, atomicStringNil);
		final AtomicReference<String> io = new AtomicReference<>(hello);
		final byte in2 = 22;
		final double in3 = 3.33;
		final AtomicReference<String> out = f.apply(io, in2, in3);
		assert io == out;
		assertOutEquals(argsToString(hello, in2, in3), out);
	}

	@Test
	public void testInplace3_2ToFunction3() {
		final Functions.Arity3<Byte, AtomicReference<String>, Double, AtomicReference<String>> f = Functions.match(ops,
			"test.inplace3_2ToFunction3TestOp", byteNil, atomicStringNil, doubleNil, atomicStringNil);
		final byte in1 = 111;
		final AtomicReference<String> io = new AtomicReference<>(hello);
		final double in3 = 3.33;
		final AtomicReference<String> out = f.apply(in1, io, in3);
		assert io == out;
		assertOutEquals(argsToString(in1, hello, in3), out);
	}

	private static void assertOutEquals(final String expected, final AtomicReference<String> actual) {
		Assert.assertEquals(expected, actual.get());
	}
}
