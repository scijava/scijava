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

package net.imagej.ops.math;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.types.Nil;
import org.scijava.ops.util.Computers;

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
		final LongType out = (LongType) ops.run("math.abs", in, in.createVariable());
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testArccos() {
		final FloatType in = new FloatType(0.5f);
		final DoubleType out = new DoubleType();
		ops.run("math.arccos", in, out);
		assertEquals(out.get(), Math.acos(0.5), 0.0);
	}

	@Test
	public void testArccosh() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.arccosh", in, out);
		final double delta = Math.sqrt(1234567890.0 * 1234567890.0 - 1);
		assertEquals(out.get(), Math.log(1234567890 + delta), 0.0);
	}

	@Test
	public void testArccot() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.arccot", in, out);
		assertEquals(out.get(), Math.atan(1.0 / 1234567890), 0.0);
	}

	@Test
	public void testArccoth() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.arccoth", in, out);
		final double result = 0.5 * Math.log(1234567891.0 / 1234567889.0);
		assertEquals(out.get(), result, 0.0);
	}

	@Test
	public void testArccsch() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.arccsch", in, out);
		final double delta = Math.sqrt(1 + 1 / (1234567890.0 * 1234567890.0));
		assertEquals(out.get(), Math.log(1 / 1234567890.0 + delta), 0.0);
	}

	@Test
	public void testArcsech() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.arcsech", in, out);
		final double numer = 1 + Math.sqrt(1 - 1234567890.0 * 1234567890.0);
		assertEquals(out.get(), Math.log(numer / 1234567890.0), 0.0);
	}

	@Test
	public void testArcsin() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.arcsin", in, out);
		assertEquals(out.get(), Math.asin(1234567890), 0.0);
	}

	@Test
	public void testArcsinh() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.arcsinh", in, out);
		final double delta = Math.sqrt(1234567890.0 * 1234567890.0 + 1);
		assertEquals(out.get(), Math.log(1234567890 + delta), 0.0);
	}

	@Test
	public void testArctan() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.arctan", in, out);
		assertEquals(out.get(), Math.atan(1234567890), 0.0);
	}

	@Test
	public void testArctanh() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.arctanh", in, out);
		assertEquals(out.get(), 0.5 * Math.log(1234567891.0 / -1234567889.0), 0.0);
	}

	@Test
	public void testCeil() {
		final LongType in = new LongType(LARGE_NUM);
		final LongType out = (LongType) ops.run("math.ceil", in, in.createVariable());
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testCos() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.cos", in, out);
		assertEquals(out.get(), Math.cos(1234567890), 0.0);
	}

	@Test
	public void testCosh() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.cosh", in, out);
		assertEquals(out.get(), Math.cosh(1234567890), 0.0);
	}

	@Test
	public void testCot() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.cot", in, out);
		assertEquals(out.get(), 1 / Math.tan(1234567890), 0.0);
	}

	@Test
	public void testCoth() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.coth", in, out);
		assertEquals(out.get(), 1 / Math.tanh(1234567890), 0.0);
	}

	@Test
	public void testCsc() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.csc", in, out);
		assertEquals(out.get(), 1 / Math.sin(1234567890), 0.0);
	}

	@Test
	public void testCsch() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.csch", in, out);
		assertEquals(out.get(), 1 / Math.sinh(1234567890), 0.0);
	}

	@Test
	public void testCubeRoot() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.cubeRoot", in, out);
		assertEquals(out.get(), Math.cbrt(1234567890), 0.0);
	}

	@Test
	public void testExp() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.exp", in, out);
		assertEquals(out.get(), Math.exp(1234567890), 0.0);
	}

	@Test
	public void testExpMinusOne() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.expMinusOne", in, out);
		assertEquals(out.get(), Math.exp(1234567890) - 1, 0.0);
	}

	@Test
	public void testFloor() {
		final LongType in = new LongType(LARGE_NUM);
		final LongType out = (LongType) ops.run("math.floor", in, in.createVariable());
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testInvert() {
		final LongType in = new LongType(LARGE_NUM);
		final LongType out = (LongType) ops.run("math.invert", in, 9007199254740992.0, 9007199254740994.0,
				in.createVariable());
		assertEquals(out.get(), LARGE_NUM + 1);
	}

	@Test
	public void testLog() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.log", in, out);
		assertEquals(out.get(), Math.log(1234567890), 0.0);
	}

	@Test
	public void testLog10() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.log10", in, out);
		assertEquals(out.get(), Math.log10(1234567890), 0.0);
	}

	@Test
	public void testLog2() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.log2", in, out);
		assertEquals(out.get(), Math.log(1234567890) / Math.log(2), 0.0);
	}

	@Test
	public void testLogOnePlusX() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.logOnePlusX", in, out);
		assertEquals(out.get(), Math.log1p(1234567890), 0.0);
	}

	@Test
	public void testMax() {
		final LongType in = new LongType(LARGE_NUM);
		final LongType out = (LongType) ops.run("math.max", in, LARGE_NUM + 1.0, in.createVariable());
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testMin() {
		final LongType in = new LongType(LARGE_NUM);
		final LongType out = (LongType) ops.run("math.min", in, LARGE_NUM - 1.0, in.createVariable());
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testNearestInt() {
		final LongType in = new LongType(LARGE_NUM);
		final LongType out = (LongType) ops.run("math.nearestInt", in, in.createVariable());
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testNegate() {
		final LongType in = new LongType(-LARGE_NUM);
		final LongType out = (LongType) ops.run("math.negate", in, in.createVariable());
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testPower() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.power", in, 1.5, out);
		assertEquals(out.get(), Math.pow(1234567890, 1.5), 0.0);
	}

	@Test
	public void testReciprocal() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.reciprocal", in, 0.0, out);
		assertEquals(out.get(), 1.0 / 1234567890, 0.0);
	}

	@Test
	public void testRound() {
		final LongType in = new LongType(LARGE_NUM);
		final LongType out = (LongType) ops.run("math.round", in, in.createVariable());
		assertEquals(out.get(), LARGE_NUM - 1);
	}

	@Test
	public void testSec() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.sec", in, out);
		assertEquals(out.get(), 1 / Math.cos(1234567890), 0.0);
	}

	@Test
	public void testSech() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.sech", in, out);
		assertEquals(out.get(), 1 / Math.cosh(1234567890), 0.0);
	}

	@Test
	public void testSignum() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.signum", in, out);
		assertEquals(out.get(), 1.0, 0.0);
	}

	@Test
	public void testSin() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.sin", in, out);
		assertEquals(out.get(), Math.sin(1234567890), 0.0);
	}

	@Test
	public void testSinc() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.sinc", in, out);
		assertEquals(out.get(), Math.sin(1234567890) / 1234567890, 0.0);
	}

	@Test
	public void testSincPi() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.sincPi", in, out);
		final double PI = Math.PI;
		assertEquals(out.get(), Math.sin(PI * 1234567890) / (PI * 1234567890), 0.0);
	}

	@Test
	public void testSinh() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.sinh", in, out);
		assertEquals(out.get(), Math.sinh(1234567890), 0.0);
	}

	@Test
	public void testSqr() {
		final LongType in = new LongType(94906267L);
		final LongType out = (LongType) ops.run("math.sqr", in, in.createVariable());
		// NB: for any odd number greater than LARGE_NUM - 1, its double
		// representation is not exact.
		assertEquals(out.get(), 94906267L * 94906267L - 1);
	}

	@Test
	public void testSqrt() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.sqrt", in, out);
		assertEquals(out.get(), Math.sqrt(1234567890), 0.0);
	}

	@Test
	public void testStep() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.step", in, out);
		assertEquals(out.get(), 1.0, 0.0);
	}

	@Test
	public void testTan() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.tan", in, out);
		assertEquals(out.get(), Math.tan(1234567890), 0.0);
	}

	@Test
	public void testTanh() {
		final LongType in = new LongType(1234567890);
		final DoubleType out = new DoubleType();
		ops.run("math.tanh", in, out);
		assertEquals(out.get(), Math.tanh(1234567890), 0.0);
	}

	@Test
	public void testUlp() {
		final LongType in = new LongType(LARGE_NUM);
		final DoubleType out = new DoubleType();
		ops.run("math.ulp", in, out);
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

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testArccscIllegalArgument() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("arccsc(x) : x out of range");
		assertArccsc(0, 0);
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
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("arcsec(x) : x out of range");
		assertArcsec(0, 0);
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
		final DoubleType out = (DoubleType) ops.run("math.arccsc", in, in.createVariable());
		assertEquals(o, out.get(), 1e-15);
	}

	private void assertArcsec(final double i, final double o) {
		final DoubleType in = new DoubleType(i);
		final DoubleType out = (DoubleType) ops.run("math.arcsec", in, in.createVariable());
		assertEquals(o, out.get(), 1e-15);
	}

	private void assertRandomGaussian(final double i, final double o, final long seed) {
		final DoubleType in = new DoubleType(i);
		final DoubleType out = (DoubleType) ops.run("math.randomGaussian", in, seed, in.createVariable());
		assertEquals(o, out.get(), 0);
	}

	private void assertRandomGaussian(final double i, final double o, final double i2, final double o2) {
		final DoubleType in = new DoubleType(i);
		final DoubleType out = new DoubleType();
		final long seed = 0xcafebabe12345678L;
		final Random rng = new Random(seed);
		final BiComputer<DoubleType, Random, DoubleType> op = Computers.binary(ops, "math.randomGaussian",
				new Nil<DoubleType>() {}, new Nil<Random>() {}, new Nil<DoubleType>() {});
		op.compute(in, rng, out);
		assertEquals(o, out.get(), 0);
		in.set(i2);
		op.compute(in, rng, out);
		assertEquals(o2, out.get(), 0);
	}

	private void assertRandomUniform(final double i, final double o, final long seed) {
		final DoubleType in = new DoubleType(i);
		final DoubleType out = (DoubleType) ops.run("math.randomUniform", in, seed, in.createVariable());
		assertEquals(o, out.get(), 0);
	}

	private void assertRandomUniform(final double i, final double o, final double i2, final double o2) {
		final DoubleType in = new DoubleType(i);
		final DoubleType out = new DoubleType();
		final long seed = 0xcafebabe12345678L;
		final Random rng = new Random(seed);
		final BiComputer<DoubleType, Random, DoubleType> op = Computers.binary(ops, "math.randomUniform",
				new Nil<DoubleType>() {}, new Nil<Random>() {}, new Nil<DoubleType>() {});
		op.compute(in, rng, out);
		assertEquals(o, out.get(), 0);
		in.set(i2);
		op.compute(in, rng, out);
		assertEquals(o2, out.get(), 0);
	}
}
