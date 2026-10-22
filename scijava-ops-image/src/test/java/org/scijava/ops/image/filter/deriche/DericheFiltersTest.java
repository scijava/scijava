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

package org.scijava.ops.image.filter.deriche;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link DericheFilters}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DericheFiltersTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<DoubleType>> DOUBLES =
		new Nil<>()
		{};

	/**
	 * Responses of the 3D ImageJ Suite (mcib3d-core 3.96.31)
	 * {@code CannyDeriche1D} to a 20-pixel step (0 then 100) and ramp (0 to
	 * 19), for alpha 0.5 and 1, run as a black box. Its derivative filter is
	 * {@code -n exp(-alpha (|n| + 1))}, i.e. not normalized; scaled by
	 * {@code (1 - a)^3 / (2 a^2 (1 + a))} with {@code a = exp(-alpha)}, it must
	 * equal the normalized Deriche derivative.
	 */
	private static final double[][] ORACLE = { //
		{ 11.987579093015258, 18.051731392192178, 26.938928622786023,
			39.75987584489995, 57.878305898291046, 82.77184008222301,
			115.60583953178252, 156.2064245027663, 200.8324565324523,
			237.62040064959655, 237.62040064959655, 200.8324565324523,
			156.20642450276634, 115.60583953178252, 82.771840082223, 57.878305898291,
			39.75987584489991, 26.938928622786, 18.051731392192167,
			11.98757909301523 }, //
		{ 9.697907221765716, 12.07174107563967, 14.0763481554042,
			15.632596948202394, 16.779584397386582, 17.593199912656637,
			18.150136904905306, 18.514035131067857, 18.73168261469354,
			18.83319185206995, 18.83319185206996, 18.731682614693554,
			18.51403513106787, 18.15013690490532, 17.593199912656647,
			16.779584397386586, 15.6325969482024, 14.0763481554042,
			12.07174107563967, 9.697907221765718 }, //
		{ 0.027959383104863515, 0.06881931989109988, 0.1675471631604435,
			0.4023710026922018, 0.9495001820249115, 2.1888762703580906,
			4.884055069992278, 10.378746736612532, 20.33616041018532,
			33.869688733846594, 33.869688733846594, 20.33616041018532,
			10.378746736612532, 4.884055069992277, 2.1888762703580897,
			0.9495001820249083, 0.40237100269219933, 0.1675471631604486,
			0.06881931989110292, 0.027959383104860524 }, //
		{ 0.7329242454169593, 1.0716210689045322, 1.2749825083051567,
			1.3787695520603973, 1.4276090167594004, 1.4494950054344404,
			1.4589829505936622, 1.462988794086209, 1.4646192752305538,
			1.4651948916063184, 1.465194891606318, 1.4646192752305536,
			1.4629887940862087, 1.458982950593662, 1.4494950054344402,
			1.4276090167594016, 1.3787695520603993, 1.2749825083051574,
			1.071621068904533, 0.7329242454169603 } };

	private RandomAccessibleInterval<DoubleType> derivative(
		final RandomAccessibleInterval<?> img, final double alpha, final int d,
		final double[] calibration)
	{
		return ops.op("filter.derichePartialDerivative").input(img, alpha, d,
			calibration).outType(DOUBLES).apply();
	}

	@Test
	public void testOracle1D() {
		final double[] alphas = { 0.5, 0.5, 1, 1 };
		for (int c = 0; c < 4; c++) {
			final Img<DoubleType> line = ArrayImgs.doubles(20);
			final boolean step = c % 2 == 0;
			for (int i = 0; i < 20; i++)
				line.getAt(i).set(step ? (i < 10 ? 0 : 100) : i);
			final double a = Math.exp(-alphas[c]);
			final double scale = Math.pow(1 - a, 3) / (2 * a * a * (1 + a));
			final RandomAccessibleInterval<DoubleType> result = derivative(line,
				alphas[c], 0, null);
			for (int i = 0; i < 20; i++) {
				final double expected = ORACLE[c][i] * scale;
				assertEquals(expected, result.getAt(i).get(), 1e-9 * Math.abs(
					expected), "case " + c + " at " + i);
			}
		}
	}

	/** A linear ramp has a constant derivative in physical units. */
	@Test
	public void testRampAndCalibration() {
		final Img<FloatType> img = ArrayImgs.floats(40, 30);
		final Cursor<FloatType> c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().setReal(3 * c.getDoublePosition(0) + 7);
		}
		final RandomAccessibleInterval<DoubleType> dx = derivative(img, 2.0, 0,
			null);
		final RandomAccessibleInterval<DoubleType> dy = derivative(img, 2.0, 1,
			null);
		final RandomAccessibleInterval<DoubleType> dxCal = derivative(img, 1.0, 0,
			new double[] { 2, 1 });
		for (int y = 0; y < 30; y++) {
			for (int x = 10; x < 30; x++) {
				assertEquals(3, dx.getAt(x, y).get(), 1e-6);
				assertEquals(1.5, dxCal.getAt(x, y).get(), 1e-6);
			}
			for (int x = 0; x < 40; x++)
				assertEquals(0, dy.getAt(x, y).get(), 1e-9);
		}
	}

	/**
	 * The edges of a 3D box are one voxel thick, on its bright side, and the
	 * gradient magnitude peaks there.
	 */
	@Test
	public void testBoxEdges3D() {
		final Img<FloatType> img = ArrayImgs.floats(20, 18, 16);
		final Cursor<FloatType> c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			boolean inside = true;
			for (int d = 0; d < 3; d++) {
				final int p = c.getIntPosition(d);
				inside &= p >= 5 && p <= 12;
			}
			c.get().setReal(inside ? 100 : 10);
		}
		final RandomAccessibleInterval<DoubleType> magnitude = ops.op(
			"filter.cannyDeriche").input(img, 1.0).outType(DOUBLES).apply();
		// Along the middle line x, the magnitude peaks at the two faces.
		final RandomAccess<DoubleType> ma = magnitude.randomAccess();
		double best = 0;
		int bestX = -1;
		for (int x = 0; x < 10; x++) {
			ma.setPosition(new int[] { x, 9, 8 });
			if (ma.get().get() > best) {
				best = ma.get().get();
				bestX = x;
			}
		}
		assertTrue(bestX == 4 || bestX == 5, "peak at " + bestX);

		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<BitType> edges =
			(RandomAccessibleInterval<BitType>) ops.op("segment.cannyDericheEdges")
				.input(img, 1.0, 5.0, 10.0).apply();
		// The face between x = 4 and x = 5 gives a one-voxel-thick edge.
		for (int y = 7; y <= 10; y++) {
			for (int z = 7; z <= 10; z++) {
				assertTrue(edges.getAt(4, y, z).get() ^ edges.getAt(5, y, z).get(),
					"face at " + y + "," + z);
				assertTrue(!edges.getAt(3, y, z).get());
				assertTrue(!edges.getAt(6, y, z).get());
				assertTrue(!edges.getAt(8, y, z).get());
			}
		}
		// Far from the box, nothing.
		for (final BitType b : Views.interval(edges, new long[] { 16, 0, 0 },
			new long[] { 19, 17, 15 }))
			assertTrue(!b.get());
	}

	@Test
	public void testInvalidArguments() {
		final Img<FloatType> img = ArrayImgs.floats(5, 5);
		assertThrows(IllegalArgumentException.class, () -> derivative(img, 0, 0,
			null));
		assertThrows(IllegalArgumentException.class, () -> derivative(img, 1, 2,
			null));
		assertThrows(IllegalArgumentException.class, () -> derivative(img, 1, 0,
			new double[] { 1 }));
	}
}
