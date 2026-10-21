/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.color.deltae;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@code color.deltaE}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DeltaETest extends AbstractOpTest {

	/**
	 * The CIEDE2000 test data of Sharma, Wu and Dalal (2005), Table 1: L1, a1,
	 * b1, L2, a2, b2 and the expected colour difference.
	 */
	private static final double[][] SHARMA = { //
		{ 50.0000, 2.6772, -79.7751, 50.0000, 0.0000, -82.7485, 2.0425 }, //
		{ 50.0000, 3.1571, -77.2803, 50.0000, 0.0000, -82.7485, 2.8615 }, //
		{ 50.0000, 2.8361, -74.0200, 50.0000, 0.0000, -82.7485, 3.4412 }, //
		{ 50.0000, -1.3802, -84.2814, 50.0000, 0.0000, -82.7485, 1.0000 }, //
		{ 50.0000, -1.1848, -84.8006, 50.0000, 0.0000, -82.7485, 1.0000 }, //
		{ 50.0000, -0.9009, -85.5211, 50.0000, 0.0000, -82.7485, 1.0000 }, //
		{ 50.0000, 0.0000, 0.0000, 50.0000, -1.0000, 2.0000, 2.3669 }, //
		{ 50.0000, -1.0000, 2.0000, 50.0000, 0.0000, 0.0000, 2.3669 }, //
		{ 50.0000, 2.4900, -0.0010, 50.0000, -2.4900, 0.0009, 7.1792 }, //
		{ 50.0000, 2.4900, -0.0010, 50.0000, -2.4900, 0.0010, 7.1792 }, //
		{ 50.0000, 2.4900, -0.0010, 50.0000, -2.4900, 0.0011, 7.2195 }, //
		{ 50.0000, 2.4900, -0.0010, 50.0000, -2.4900, 0.0012, 7.2195 }, //
		{ 50.0000, -0.0010, 2.4900, 50.0000, 0.0009, -2.4900, 4.8045 }, //
		{ 50.0000, -0.0010, 2.4900, 50.0000, 0.0010, -2.4900, 4.8045 }, //
		{ 50.0000, -0.0010, 2.4900, 50.0000, 0.0011, -2.4900, 4.7461 }, //
		{ 50.0000, 2.5000, 0.0000, 50.0000, 0.0000, -2.5000, 4.3065 }, //
		{ 50.0000, 2.5000, 0.0000, 73.0000, 25.0000, -18.0000, 27.1492 }, //
		{ 50.0000, 2.5000, 0.0000, 61.0000, -5.0000, 29.0000, 22.8977 }, //
		{ 50.0000, 2.5000, 0.0000, 56.0000, -27.0000, -3.0000, 31.9030 }, //
		{ 50.0000, 2.5000, 0.0000, 58.0000, 24.0000, 15.0000, 19.4535 }, //
		{ 50.0000, 2.5000, 0.0000, 50.0000, 3.1736, 0.5854, 1.0000 }, //
		{ 50.0000, 2.5000, 0.0000, 50.0000, 3.2972, 0.0000, 1.0000 }, //
		{ 50.0000, 2.5000, 0.0000, 50.0000, 1.8634, 0.5757, 1.0000 }, //
		{ 50.0000, 2.5000, 0.0000, 50.0000, 3.2592, 0.3350, 1.0000 }, //
		{ 60.2574, -34.0099, 36.2677, 60.4626, -34.1751, 39.4387, 1.2644 }, //
		{ 63.0109, -31.0961, -5.8663, 62.8187, -29.7946, -4.0864, 1.2630 }, //
		{ 61.2901, 3.7196, -5.3901, 61.4292, 2.2480, -4.9620, 1.8731 }, //
		{ 35.0831, -44.1164, 3.7933, 35.0232, -40.0716, 1.5901, 1.8645 }, //
		{ 22.7233, 20.0904, -46.6940, 23.0331, 14.9730, -42.5619, 2.0373 }, //
		{ 36.4612, 47.8580, 18.3852, 36.2715, 50.5065, 21.2231, 1.4146 }, //
		{ 90.8027, -2.0831, 1.4410, 91.1528, -1.6435, 0.0447, 1.4441 }, //
		{ 90.9257, -0.5406, -0.9208, 88.6381, -0.8985, -0.7239, 1.5381 }, //
		{ 6.7747, -0.2908, -2.4247, 5.8714, -0.0985, -2.2286, 0.6377 }, //
		{ 2.0776, 0.0795, -1.1350, 0.9033, -0.0636, -0.5514, 0.9082 } };

	private double deltaE(final double[] lab1, final double[] lab2,
		final String formula)
	{
		return ops.op("color.deltaE").input(lab1, lab2, formula).outType(
			Double.class).apply();
	}

	@Test
	public void testSharmaCIEDE2000() {
		for (final var row : SHARMA) {
			final double[] p = { row[0], row[1], row[2] };
			final double[] q = { row[3], row[4], row[5] };
			assertEquals(row[6], deltaE(p, q, "CIEDE2000"), 5e-5);
			// CIEDE2000 is symmetric.
			assertEquals(row[6], deltaE(q, p, "CIEDE2000"), 5e-5);
		}
	}

	@Test
	public void testCIE76AndCIE94() {
		final double[] p = { 50, 2.5, 0 };
		final double[] q = { 73, 25, -18 };
		final double dl = 23, da = 22.5, db = -18;
		assertEquals(Math.sqrt(dl * dl + da * da + db * db), deltaE(p, q,
			"CIE76"), 1e-12);

		// CIE94 by hand: C1 = 2.5, C2 = hypot(25, 18).
		final var c1 = 2.5;
		final var c2 = Math.hypot(25, 18);
		final var dc = c1 - c2;
		final var dh2 = da * da + db * db - dc * dc;
		final var sc = 1 + 0.045 * c1;
		final var sh = 1 + 0.015 * c1;
		final var expected = Math.sqrt(dl * dl + dc * dc / (sc * sc) + dh2 / (sh *
			sh));
		assertEquals(expected, deltaE(p, q, "CIE94"), 1e-12);

		final var sct = 1 + 0.048 * c1;
		final var sht = 1 + 0.014 * c1;
		final var textiles = Math.sqrt(dl * dl / 4 + dc * dc / (sct * sct) + dh2 /
			(sht * sht));
		assertEquals(textiles, deltaE(p, q, "CIE94 textiles"), 1e-12);

		// Pure lightness differences around L = 50 are equal under all formulas.
		final double[] g1 = { 49.5, 0, 0 };
		final double[] g2 = { 50.5, 0, 0 };
		assertEquals(1, deltaE(g1, g2, "CIE76"), 1e-12);
		assertEquals(1, deltaE(g1, g2, "CIE94"), 1e-12);
		assertEquals(1, deltaE(g1, g2, "CIEDE2000"), 1e-12);
	}

	@Test
	public void testImage() {
		// Lab images with the channel along dimension 0, one Sharma pair per
		// pixel.
		final var n = SHARMA.length;
		final var lab1 = ArrayImgs.doubles(3, n);
		final var lab2 = ArrayImgs.doubles(3, n);
		final var a1 = lab1.randomAccess();
		final var a2 = lab2.randomAccess();
		for (var i = 0; i < n; i++) {
			for (var c = 0; c < 3; c++) {
				a1.setPositionAndGet(c, i).set(SHARMA[i][c]);
				a2.setPositionAndGet(c, i).set(SHARMA[i][c + 3]);
			}
		}
		final RandomAccessibleInterval<DoubleType> out = ops.op("color.deltaE")
			.input(lab1, lab2).outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();
		assertArrayEquals(new long[] { n }, out.dimensionsAsLongArray());
		final var oa = out.randomAccess();
		for (var i = 0; i < n; i++)
			assertEquals(SHARMA[i][6], oa.setPositionAndGet(i).get(), 5e-5);
	}
}
