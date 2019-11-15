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
public class ComputerToFunctionTransformTest extends AbstractTestEnvironment {

	private static final Nil<Byte> byteNil = new Nil<Byte>() {};

	private static final Nil<Double> doubleNil = new Nil<Double>() {};

	private static final Nil<Float> floatNil = new Nil<Float>() {};

	private static final Nil<Integer> integerNil = new Nil<Integer>() {};

	private static final Nil<Long> longNil = new Nil<Long>() {};

	private static final Nil<AtomicReference<String>> atomicStringNil = new Nil<AtomicReference<String>>() {};

	@Test
	public void testComputer1ToFunction1() {
		final Function<Byte, AtomicReference<String>> f = Functions.match(ops, "test.computer1ToFunction1TestOp", byteNil,
			atomicStringNil);
		final byte in = 11;
		final AtomicReference<String> out = f.apply(in);
		assertOutEquals(argsToString(in), out);
	}

	@Test
	public void testComputer2ToFunction2() {
		final BiFunction<Byte, Double, AtomicReference<String>> f = Functions.match(ops,
			"test.computer2ToFunction2TestOp", byteNil, doubleNil, atomicStringNil);
		final byte in1 = 111;
		final double in2 = 22.22;
		final AtomicReference<String> out = f.apply(in1, in2);
		assertOutEquals(argsToString(in1, in2), out);
	}

	@Test
	public void testComputer3ToFunction3() {
		final Functions.Arity3<Byte, Double, Float, AtomicReference<String>> f = Functions.match(ops,
			"test.computer3ToFunction3TestOp", byteNil, doubleNil, floatNil, atomicStringNil);
		final byte in1 = 1;
		final double in2 = 2.2;
		final float in3 = 33.3f;
		final AtomicReference<String> out = f.apply(in1, in2, in3);
		assertOutEquals(argsToString(in1, in2, in3), out);
	}

	private static void assertOutEquals(final String expected, final AtomicReference<String> actual) {
		Assert.assertEquals(expected, actual.get());
	}
}
