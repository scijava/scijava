/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops2.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.scijava.function.Computers;
import org.scijava.ops.util.ComputerUtils;
import org.scijava.types.Nil;

/**
 * Tests {@link UnaryRealTypeMath}.
 *
 * @author Leon Yang
 * @author Alison Walter
 * @author Curtis Rueden
 */
public class UnaryRealTypeMathTest extends AbstractOpTest {

	// NB: long number LARGE_NUM is rounded to double 9007199254740992.0.
	final static private long LARGE_NUM = 9007199254740993L;

	@Test
	public void testAbs() {
		final LongType in = new LongType(-LARGE_NUM);
		final LongType out = in.createVariable();
		ops.op("math.abs").input(in).output(out).compute();
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testArccos() {
		final FloatType in = new FloatType(0.5f);
		final DoubleType out = new DoubleType();
		ops.op("math.arccos").input(in).output(out).compute();
		assertEquals(out.get(), Math.acos(0.5), 0.0);
	}

	@Test
	public void testArccosh() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.arccosh").input(in).output(out).compute();
		final double delta = Math.sqrt(1234567890.0 * 1234567890.0 - 1);
		assertEquals(out.get(), Math.log(1234567890 + delta), 0.0);
	}

	@Test
	public void testArccot() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.arccot").input(in).output(out).compute();
		assertEquals(out.get(), Math.atan(1.0 / 1234567890), 0.0);
	}

	@Test
	public void testArccoth() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.arccoth").input(in).output(out).compute();
		final double result = 0.5 * Math.log(1234567891.0 / 1234567889.0);
		assertEquals(out.get(), result, 0.0);
	}

	@Test
	public void testArccsch() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.arccsch").input(in).output(out).compute();
		final double delta = Math.sqrt(1 + 1 / (1234567890.0 * 1234567890.0));
		assertEquals(out.get(), Math.log(1 / 1234567890.0 + delta), 0.0);
	}

	@Test
	public void testArcsech() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.arcsech").input(in).output(out).compute();
		final double numer = 1 + Math.sqrt(1 - 1234567890.0 * 1234567890.0);
		assertEquals(out.get(), Math.log(numer / 1234567890.0), 0.0);
	}

	@Test
	public void testArcsin() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.arcsin").input(in).output(out).compute();
		assertEquals(out.get(), Math.asin(1234567890), 0.0);
	}

	@Test
	public void testArcsinh() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.arcsinh").input(in).output(out).compute();
		final double delta = Math.sqrt(1234567890.0 * 1234567890.0 + 1);
		assertEquals(out.get(), Math.log(1234567890 + delta), 0.0);
	}

	@Test
	public void testArctan() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.arctan").input(in).output(out).compute();
		assertEquals(out.get(), Math.atan(1234567890), 0.0);
	}

	@Test
	public void testArctanh() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.arctanh").input(in).output(out).compute();
		assertEquals(out.get(), 0.5 * Math.log(1234567891.0 / -1234567889.0), 0.0);
	}

	@Test
	public void testCeil() {
		final LongType in = new LongType(LARGE_NUM);
		final LongType out = in.createVariable();
		ops.op("math.ceil").input(in).output(out).compute();
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testCos() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.cos").input(in).output(out).compute();
		assertEquals(out.get(), Math.cos(1234567890), 0.0);
	}

	@Test
	public void testCosh() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.cosh").input(in).output(out).compute();
		assertEquals(out.get(), Math.cosh(1234567890), 0.0);
	}

	@Test
	public void testCot() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.cot").input(in).output(out).compute();
		assertEquals(out.get(), 1 / Math.tan(1234567890), 0.0);
	}

	@Test
	public void testCoth() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.coth").input(in).output(out).compute();
		assertEquals(out.get(), 1 / Math.tanh(1234567890), 0.0);
	}

	@Test
	public void testCsc() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.csc").input(in).output(out).compute();
		assertEquals(out.get(), 1 / Math.sin(1234567890), 0.0);
	}

	@Test
	public void testCsch() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.csch").input(in).output(out).compute();
		assertEquals(out.get(), 1 / Math.sinh(1234567890), 0.0);
	}

	@Test
	public void testCubeRoot() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.cubeRoot").input(in).output(out).compute();
		assertEquals(out.get(), Math.cbrt(1234567890), 0.0);
	}

	@Test
	public void testExp() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.exp").input(in).output(out).compute();
		assertEquals(out.get(), Math.exp(1234567890), 0.0);
	}

	@Test
	public void testExpMinusOne() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.expMinusOne").input(in).output(out).compute();
		assertEquals(out.get(), Math.exp(1234567890) - 1, 0.0);
	}

	@Test
	public void testFloor() {
		final LongType in = new LongType(LARGE_NUM);
		final LongType out = in.createVariable();
		ops.op("math.floor").input(in).output(out).compute();
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testInvert() {
		final LongType in = new LongType(LARGE_NUM);
		final LongType out = in.createVariable();
		ops.op("math.invert").input(in, 9007199254740992.0, 9007199254740994.0).output(out).compute();
		assertEquals(out.get(), LARGE_NUM + 1);
	}

	@Test
	public void testLog() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.log").input(in).output(out).compute();
		assertEquals(out.get(), Math.log(1234567890), 0.0);
	}

	@Test
	public void testLog10() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.log10").input(in).output(out).compute();
		assertEquals(out.get(), Math.log10(1234567890), 0.0);
	}

	@Test
	public void testLog2() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.log2").input(in).output(out).compute();
		assertEquals(out.get(), Math.log(1234567890) / Math.log(2), 0.0);
	}

	@Test
	public void testLogOnePlusX() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.logOnePlusX").input(in).output(out).compute();
		assertEquals(out.get(), Math.log1p(1234567890), 0.0);
	}

	@Test
	public void testMax() {
		final LongType in = new LongType(LARGE_NUM);
		final LongType out = in.createVariable();
		ops.op("math.max").input(in, LARGE_NUM + 1.0).output(out).compute();
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testMin() {
		final LongType in = new LongType(LARGE_NUM);
		final LongType out = in.createVariable();
		ops.op("math.min").input(in, LARGE_NUM - 1.0).output(out).compute();
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testNearestInt() {
		final LongType in = new LongType(LARGE_NUM);
		final LongType out = in.createVariable();
		ops.op("math.nearestInt").input(in).output(out).compute();
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testNegate() {
		final LongType in = new LongType(-LARGE_NUM);
		final LongType out = in.createVariable();
		ops.op("math.negate").input(in).output(out).compute();
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testPower() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.power").input(in, 1.5).output(out).compute();
		assertEquals(out.get(), Math.pow(1234567890, 1.5), 0.0);
	}

	@Test
	public void testReciprocal() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.reciprocal").input(in, 0.0).output(out).compute();
		assertEquals(out.get(), 1.0 / 1234567890, 0.0);
	}

	@Test
	public void testRound() {
		final LongType in = new LongType(LARGE_NUM);
		final LongType out = in.createVariable();
		ops.op("math.round").input(in).output(out).compute();
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testSec() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.sec").input(in).output(out).compute();
		assertEquals(out.get(), 1 / Math.cos(1234567890), 0.0);
	}

	@Test
	public void testSech() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.sech").input(in).output(out).compute();
		assertEquals(out.get(), 1 / Math.cosh(1234567890), 0.0);
	}

	@Test
	public void testSignum() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.signum").input(in).output(out).compute();
		assertEquals(out.get(), 1.0, 0.0);
	}

	@Test
	public void testSin() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.sin").input(in).output(out).compute();
		assertEquals(out.get(), Math.sin(1234567890), 0.0);
	}

	@Test
	public void testSinc() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.sinc").input(in).output(out).compute();
		assertEquals(out.get(), Math.sin(1234567890) / 1234567890, 0.0);
	}

	@Test
	public void testSincPi() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.sincPi").input(in).output(out).compute();
		final double PI = Math.PI;
		assertEquals(out.get(), Math.sin(PI * 1234567890) / (PI * 1234567890), 0.0);
	}

	@Test
	public void testSinh() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.sinh").input(in).output(out).compute();
		assertEquals(out.get(), Math.sinh(1234567890), 0.0);
	}

	@Test
	public void testSqr() {
		final LongType in = new LongType(94906267L);
		final LongType out = in.createVariable();
		ops.op("math.sqr").input(in).output(out).compute();
		// NB: for any odd number greater than LARGE_NUM - 1, its double
		// representation is not exact.
		assertEquals(out.get(), 94906267L * 94906267L - 1);
	}

	@Test
	public void testSqrt() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.sqrt").input(in).output(out).compute();
		assertEquals(out.get(), Math.sqrt(1234567890), 0.0);
	}

	@Test
	public void testStep() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.step").input(in).output(out).compute();
		assertEquals(out.get(), 1.0, 0.0);
	}

	@Test
	public void testTan() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.tan").input(in).output(out).compute();
		assertEquals(out.get(), Math.tan(1234567890), 0.0);
	}

	@Test
	public void testTanh() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.op("math.tanh").input(in).output(out).compute();
		assertEquals(out.get(), Math.tanh(1234567890), 0.0);
	}

	@Test
	public void testUlp() {
		final LongType in = new LongType(LARGE_NUM);
		final DoubleType out = new DoubleType();
		ops.op("math.ulp").input(in).output(out).compute();
		assertEquals(out.get(), 2.0, 0.0);
	}

	// -- complex tests --

	@Test
	public void testArccsc() {
		assertArccsc(-1, -Math.PI / 2);
		assertArccsc(1, Math.PI / 2);
		assertArccsc(2, Math.PI / 6);
		assertArccsc(-2, -Math.PI / 6);
		assertArccsc(2 * Math.sqrt(3) / 3, Math.PI / 3);
		assertArccsc(-(2 * Math.sqrt(3)) / 3, -Math.PI / 3);
	}

	@Test
	public void testArccscIllegalArgument() {
		IllegalArgumentException e = Assertions.assertThrows(
			IllegalArgumentException.class, () -> {
				assertArccsc(0, 0);
			});
		Assertions.assertTrue(e.getMessage().equalsIgnoreCase(
			"arccsc(x) : x out of range"));
	}

	@Test
	public void testArcsec() {
		assertArcsec(-1, Math.PI);
		assertArcsec(1, 0);
		assertArcsec(Math.sqrt(2), Math.PI / 4);
		assertArcsec(-Math.sqrt(2), 3 * Math.PI / 4);
		assertArcsec(2, Math.PI / 3);
		assertArcsec(-2, 2 * Math.PI / 3);
	}

	@Test
	public void testArcsecIllegalArgument() {
		IllegalArgumentException e = Assertions.assertThrows(
			IllegalArgumentException.class, () -> {
				assertArcsec(0, 0);
			});
		Assertions.assertTrue(e.getMessage().equalsIgnoreCase(
			"arcsec(x) : x out of range"));
	}

	@Test
	public void testRandomGaussian() {
		final long seed = 0xabcdef1234567890L;
		assertRandomGaussian(23, 16.53373419964066, seed);
		assertRandomGaussian(27, -15.542815799078497, 0xfeeddeadbeefbeefL);
		assertRandomGaussian(123, -49.838353142718006, 124, 181.75101003563117);
	}

	@Test
	public void testRandomUniform() {
		final long seed = 0xabcdef1234567890L;
		assertRandomUniform(23, 14.278690684728433, seed);
		assertRandomUniform(27, 5.940945158572171, 0xfeeddeadbeefbeefL);
		assertRandomUniform(123, 52.3081016051914, 124, 95.52110798318904);
	}

	// -- Helper methods --

	private void assertArccsc(final double i, final double o) {
		final DoubleType in = new DoubleType(i);
		final DoubleType out = in.createVariable();
		ops.op("math.arccsc").input(in).output(out).compute();
		assertEquals(o, out.get(), 1e-15);
	}

	private void assertArcsec(final double i, final double o) {
		final DoubleType in = new DoubleType(i);
		final DoubleType out = in.createVariable();
		ops.op("math.arcsec").input(in).output(out).compute();
		assertEquals(o, out.get(), 1e-15);
	}

	private void assertRandomGaussian(final double i, final double o, final long seed) {
		final DoubleType in = new DoubleType(i);
		final DoubleType out = in.createVariable();
		ops.op("math.randomGaussian").input(in, seed).output(out).compute();
		assertEquals(o, out.get(), 0);
	}

	private void assertRandomGaussian(final double i, final double o, final double i2, final double o2) {
		final DoubleType in = new DoubleType(i);
		final DoubleType out = new DoubleType();
		final long seed = 0xcafebabe12345678L;
		final Random rng = new Random(seed);
		final Computers.Arity2<DoubleType, Random, DoubleType> op = ComputerUtils.match(ops.env(), "math.randomGaussian",
				new Nil<DoubleType>() {}, new Nil<Random>() {}, new Nil<DoubleType>() {});
		op.compute(in, rng, out);
		assertEquals(o, out.get(), 0);
		in.set(i2);
		op.compute(in, rng, out);
		assertEquals(o2, out.get(), 0);
	}

	private void assertRandomUniform(final double i, final double o, final long seed) {
		final DoubleType in = new DoubleType(i);
		final DoubleType out = in.createVariable();
		ops.op("math.randomUniform").input(in, seed).output(out).compute();
		assertEquals(o, out.get(), 0);
	}

	private void assertRandomUniform(final double i, final double o, final double i2, final double o2) {
		final DoubleType in = new DoubleType(i);
		final DoubleType out = new DoubleType();
		final long seed = 0xcafebabe12345678L;
		final Random rng = new Random(seed);
		final Computers.Arity2<DoubleType, Random, DoubleType> op = ComputerUtils.match(ops.env(), "math.randomUniform",
				new Nil<DoubleType>() {}, new Nil<Random>() {}, new Nil<DoubleType>() {});
		op.compute(in, rng, out);
		assertEquals(o, out.get(), 0);
		in.set(i2);
		op.compute(in, rng, out);
		assertEquals(o2, out.get(), 0);
	}
}
