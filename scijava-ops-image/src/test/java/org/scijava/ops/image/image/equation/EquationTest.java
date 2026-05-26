/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2026 SciJava developers.
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

package org.scijava.ops.image.image.equation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@code image.equation}.
 *
 * @author Curtis Rueden
 */
public class EquationTest extends AbstractOpTest {

	private static final double EPS = 1e-12;

	@Test
	public void testConstant() {
		final Img<DoubleType> img = ArrayImgs.doubles(8, 8);
		ops.op("image.equation").input("42").output(img).compute();
		for (final DoubleType px : img)
			assertEquals(42.0, px.get(), EPS);
	}

	@Test
	public void testPositionArray() {
		final Img<DoubleType> img = ArrayImgs.doubles(5, 7);
		ops.op("image.equation").input("p[0] + 10*p[1]").output(img).compute();
		final RandomAccess<DoubleType> ra = img.randomAccess();
		for (long y = 0; y < 7; y++) {
			for (long x = 0; x < 5; x++) {
				ra.setPosition(new long[] { x, y });
				assertEquals(x + 10 * y, ra.get().get(), EPS);
			}
		}
	}

	@Test
	public void testAxisShorthands() {
		final Img<DoubleType> img = ArrayImgs.doubles(4, 4);
		ops.op("image.equation").input("x*y").output(img).compute();
		final RandomAccess<DoubleType> ra = img.randomAccess();
		for (long y = 0; y < 4; y++) {
			for (long x = 0; x < 4; x++) {
				ra.setPosition(new long[] { x, y });
				assertEquals(x * y, ra.get().get(), EPS);
			}
		}
	}

	@Test
	public void testMathFunctions() {
		final Img<DoubleType> img = ArrayImgs.doubles(16, 16);
		ops.op("image.equation").input("cos(0.1*p[0]) + sin(0.1*p[1])").output(img)
			.compute();
		final RandomAccess<DoubleType> ra = img.randomAccess();
		for (long y = 0; y < 16; y++) {
			for (long x = 0; x < 16; x++) {
				ra.setPosition(new long[] { x, y });
				final double expected = Math.cos(0.1 * x) + Math.sin(0.1 * y);
				assertEquals(expected, ra.get().get(), 1e-10);
			}
		}
	}

	@Test
	public void testMultiArgFunction() {
		final Img<DoubleType> img = ArrayImgs.doubles(8, 8);
		ops.op("image.equation").input("atan2(y, x+1)").output(img).compute();
		final RandomAccess<DoubleType> ra = img.randomAccess();
		for (long y = 0; y < 8; y++) {
			for (long x = 0; x < 8; x++) {
				ra.setPosition(new long[] { x, y });
				assertEquals(Math.atan2(y, x + 1), ra.get().get(), 1e-12);
			}
		}
	}

	@Test
	public void testConstants() {
		final Img<DoubleType> img = ArrayImgs.doubles(2, 2);
		ops.op("image.equation").input("pi + e").output(img).compute();
		for (final DoubleType px : img)
			assertEquals(Math.PI + Math.E, px.get(), EPS);
	}

	@Test
	public void test3D() {
		final Img<DoubleType> img = ArrayImgs.doubles(3, 4, 5);
		ops.op("image.equation").input("x + 10*y + 100*z").output(img).compute();
		final RandomAccess<DoubleType> ra = img.randomAccess();
		for (long z = 0; z < 5; z++) {
			for (long y = 0; y < 4; y++) {
				for (long x = 0; x < 3; x++) {
					ra.setPosition(new long[] { x, y, z });
					assertEquals(x + 10 * y + 100 * z, ra.get().get(), EPS);
				}
			}
		}
	}

	@Test
	public void testDeterminism() {
		final Img<DoubleType> a = ArrayImgs.doubles(32, 32);
		final Img<DoubleType> b = ArrayImgs.doubles(32, 32);
		final String expr = "random() + 0.001*cos(x) - 0.001*sin(y)";
		ops.op("image.equation").input(expr).output(a).compute();
		ops.op("image.equation").input(expr).output(b).compute();
		final var ca = a.cursor();
		final var cb = b.cursor();
		while (ca.hasNext()) {
			assertEquals(ca.next().get(), cb.next().get(), 0.0);
		}
	}

	@Test
	public void testRandomInRange() {
		final Img<DoubleType> img = ArrayImgs.doubles(64, 64);
		ops.op("image.equation").input("random()").output(img).compute();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		double sum = 0;
		long count = 0;
		for (final DoubleType px : img) {
			final double v = px.get();
			assertTrue(v >= 0.0 && v < 1.0, "value out of [0,1): " + v);
			if (v < min) min = v;
			if (v > max) max = v;
			sum += v;
			count++;
		}
		// Sanity: not all the same value, and mean roughly near 0.5.
		assertNotEquals(min, max);
		final double mean = sum / count;
		assertTrue(mean > 0.3 && mean < 0.7, "mean = " + mean);
	}

	@Test
	public void testBadExpressionYieldsNaN() {
		final Img<DoubleType> img = ArrayImgs.doubles(4, 4);
		// Unknown identifier — should produce NaN at every pixel rather than
		// killing the whole loop.
		ops.op("image.equation").input("nope(x)").output(img).compute();
		for (final DoubleType px : img)
			assertTrue(Double.isNaN(px.get()));
	}
}
