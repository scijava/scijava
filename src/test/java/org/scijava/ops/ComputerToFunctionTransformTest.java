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
import org.scijava.ops.core.function.Function3;
import org.scijava.ops.core.function.Function4;
import org.scijava.ops.core.function.Function5;
import org.scijava.ops.types.Nil;
import org.scijava.ops.util.Functions;

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
	public void testComputerToFunction() {
		final Function<Byte, AtomicReference<String>> f = Functions.unary(ops, "test.computerToFunctionTestOp", byteNil,
			atomicStringNil);
		final byte in = 11;
		final AtomicReference<String> out = f.apply(in);
		assertOutEquals(argsToString(in), out);
	}

	@Test
	public void testBiComputerToBiFunction() {
		final BiFunction<Byte, Double, AtomicReference<String>> f = Functions.binary(ops,
			"test.biComputerToBiFunctionTestOp", byteNil, doubleNil, atomicStringNil);
		final byte in1 = 111;
		final double in2 = 22.22;
		final AtomicReference<String> out = f.apply(in1, in2);
		assertOutEquals(argsToString(in1, in2), out);
	}

	@Test
	public void testComputer3ToFunction3() {
		final Function3<Byte, Double, Float, AtomicReference<String>> f = Functions.ternary(ops,
			"test.computer3ToFunction3TestOp", byteNil, doubleNil, floatNil, atomicStringNil);
		final byte in1 = 1;
		final double in2 = 2.2;
		final float in3 = 33.3f;
		final AtomicReference<String> out = f.apply(in1, in2, in3);
		assertOutEquals(argsToString(in1, in2, in3), out);
	}

	@Test
	public void testComputer4ToFunction4() {
		final Function4<Byte, Double, Float, Integer, AtomicReference<String>> f = Functions.quaternary(ops,
			"test.computer4ToFunction4TestOp", byteNil, doubleNil, floatNil, integerNil, atomicStringNil);
		final byte in1 = 111;
		final double in2 = .2;
		final float in3 = 3.33f;
		final int in4 = 44444;
		final AtomicReference<String> out = f.apply(in1, in2, in3, in4);
		assertOutEquals(argsToString(in1, in2, in3, in4), out);
	}

	@Test
	public void testComputer5ToFunction5() {
		final Function5<Byte, Double, Float, Integer, Long, AtomicReference<String>> f = Functions.quinary(ops,
			"test.computer5ToFunction5TestOp", byteNil, doubleNil, floatNil, integerNil, longNil, atomicStringNil);
		final byte in1 = 1;
		final double in2 = .222;
		final float in3 = 333.3f;
		final int in4 = 4;
		final long in5 = 55555l;
		final AtomicReference<String> out = f.apply(in1, in2, in3, in4, in5);
		assertOutEquals(argsToString(in1, in2, in3, in4, in5), out);
	}

	private static void assertOutEquals(final String expected, final AtomicReference<String> actual) {
		Assert.assertEquals(expected, actual.get());
	}
}
