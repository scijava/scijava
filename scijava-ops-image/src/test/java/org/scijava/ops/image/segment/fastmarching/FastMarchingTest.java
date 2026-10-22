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

package org.scijava.ops.image.segment.fastmarching;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@code image.fastMarching} and {@code segment.fastMarching}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class FastMarchingTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<DoubleType>> TIMES =
		new Nil<>()
		{};

	private static final Nil<RandomAccessibleInterval<BitType>> MASK =
		new Nil<>()
		{};

	private static double[] values(final RandomAccessibleInterval<DoubleType> img) {
		final var out = new double[(int) Views.iterable(img).size()];
		var i = 0;
		for (final var t : Views.flatIterable(img))
			out[i++] = t.get();
		return out;
	}

	/**
	 * Maximal absolute and relative error with respect to the Euclidean
	 * distance from a point seed at the center of a constant-speed image.
	 */
	private double[] euclideanErrors(final long[] dims, final double[] cal,
		final boolean secondOrder, final double minRadius)
	{
		final var speed = ArrayImgs.doubles(dims);
		speed.forEach(t -> t.set(1));
		final var center = new long[dims.length];
		for (var d = 0; d < dims.length; d++)
			center[d] = dims[d] / 2;
		final RandomAccessibleInterval<DoubleType> times = ops.op(
			"image.fastMarching").input(List.of(new Point(center)), speed, cal,
				null, secondOrder).outType(TIMES).apply();
		var maxAbs = 0.0;
		var maxRel = 0.0;
		final var c = Views.iterable(times).localizingCursor();
		while (c.hasNext()) {
			final var t = c.next().get();
			var r2 = 0.0;
			for (var d = 0; d < dims.length; d++)
				r2 += Math.pow((c.getLongPosition(d) - center[d]) * cal[d], 2);
			final var r = Math.sqrt(r2);
			if (r < minRadius) continue;
			final var err = Math.abs(t - r);
			maxAbs = Math.max(maxAbs, err);
			if (r > 0) maxRel = Math.max(maxRel, err / r);
			else assertEquals(0, t);
		}
		return new double[] { maxAbs, maxRel };
	}

	@Test
	public void testConstantSpeed2D() {
		// Note: For a point source, the first-order scheme overestimates the
		// distance off the axes, with an error growing like log(r) (already 0.29
		// at the diagonal neighbor of the seed); the second-order scheme is
		// markedly more accurate. The bounds below are about 10 % above the
		// observed errors (1.15 and 0.33 at radii up to 42).
		final var dims = new long[] { 61, 61 };
		final var cal = new double[] { 1, 1 };
		final var first = euclideanErrors(dims, cal, false, 0);
		final var second = euclideanErrors(dims, cal, true, 0);
		assertTrue(first[0] < 1.25, "first-order error " + first[0]);
		assertTrue(second[0] < 0.36, "second-order error " + second[0]);
		// The relative error decreases with the distance: beyond radius 10, it
		// is at most 7 % (first order) and 2.6 % (second order).
		assertTrue(first[1] > 0.2 && euclideanErrors(dims, cal, false, 10)[1] <
			0.075);
		assertTrue(euclideanErrors(dims, cal, true, 10)[1] < 0.03);
	}

	@Test
	public void testConstantSpeed3DAnisotropic() {
		final var e = euclideanErrors(new long[] { 21, 25, 11 }, new double[] {
			1, 0.8, 2 }, true, 0);
		assertTrue(e[0] < 0.9, "error " + e[0]);
	}

	/**
	 * Fast medium (speed 2) for {@code x < 20}, slow medium (speed 1) beyond,
	 * with a point seed in the fast medium: the arrival time in the slow medium
	 * is that of the refracted path obeying the law of Snell, found here by
	 * minimizing the travel time over the crossing point on the interface.
	 */
	@Test
	public void testTwoSpeedMedium() {
		final var speed = ArrayImgs.doubles(40, 30);
		final var c = speed.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set(c.getLongPosition(0) < 20 ? 2 : 1);
		}
		final double sx = 8, sy = 5, x0 = 19.5;
		final RandomAccessibleInterval<DoubleType> times = ops.op(
			"image.fastMarching").input(List.of(new Point(8, 5)), speed).outType(
				TIMES).apply();
		final var tc = Views.iterable(times).localizingCursor();
		var maxErr = 0.0;
		while (tc.hasNext()) {
			final var t = tc.next().get();
			final double px = tc.getLongPosition(0), py = tc.getLongPosition(1);
			final double expected;
			if (px < x0) expected = Math.hypot(px - sx, py - sy) / 2;
			else {
				double lo = Math.min(sy, py), hi = Math.max(sy, py);
				for (var k = 0; k < 200; k++) {
					final var m1 = lo + (hi - lo) / 3;
					final var m2 = hi - (hi - lo) / 3;
					if (snell(sx, sy, x0, m1, px, py) < snell(sx, sy, x0, m2, px, py))
						hi = m2;
					else lo = m1;
				}
				expected = snell(sx, sy, x0, lo, px, py);
			}
			maxErr = Math.max(maxErr, Math.abs(t - expected));
		}
		// Note: The interface is only resolved to within a pixel, which is
		// where most of the error comes from.
		assertTrue(maxErr < 0.6, "error " + maxErr);

		// The refracted path is faster than the straight one.
		final var ra = times.randomAccess();
		final var straight = Math.hypot(x0 - sx, (39 - sy) * (x0 - sx) / (39 -
			sx)) / 2 + Math.hypot(39 - x0, (39 - sy) * (39 - x0) / (39 - sx));
		assertTrue(ra.setPositionAndGet(39, 29).get() < straight - 1);
	}

	private static double snell(final double sx, final double sy,
		final double x0, final double y, final double px, final double py)
	{
		return Math.hypot(x0 - sx, y - sy) / 2 + Math.hypot(px - x0, py - y);
	}

	@Test
	public void testSeedMaskBarrierAndCost() {
		// A wall at x = 10 with a gap at y in [0, 2].
		final var speed = ArrayImgs.doubles(20, 20);
		final var c = speed.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set(c.getLongPosition(0) == 10 && c.getLongPosition(1) > 2 ? 0
				: 1);
		}
		final var seeds = ArrayImgs.bits(20, 20);
		final var sa = seeds.randomAccess();
		sa.setPositionAndGet(2, 15).set(true);
		sa.setPositionAndGet(3, 15).set(true);
		final RandomAccessibleInterval<DoubleType> times = ops.op(
			"image.fastMarching").input(seeds, speed).outType(TIMES).apply();
		final var ra = times.randomAccess();
		assertEquals(0, ra.setPositionAndGet(2, 15).get());
		assertEquals(Double.POSITIVE_INFINITY, ra.setPositionAndGet(10, 15)
			.get());
		// Going around the wall is longer than the straight distance.
		assertTrue(ra.setPositionAndGet(17, 15).get() > 14 + 8);

		// A cost image (with infinite cost on the wall) gives the same result.
		final var cost = ArrayImgs.doubles(20, 20);
		final var cc = cost.cursor();
		for (final var s : speed)
			cc.next().set(1 / s.get());
		final RandomAccessibleInterval<DoubleType> fromCost = ops.op(
			"image.fastMarching").input(seeds, cost, null, null, null, true)
			.outType(TIMES).apply();
		assertArrayEquals(values(times), values(fromCost));

		// Points and the equivalent mask give the same result.
		final RandomAccessibleInterval<DoubleType> fromPoints = ops.op(
			"image.fastMarching").input(List.of(new Point(2, 15), new Point(3, 15)),
				speed).outType(TIMES).apply();
		assertArrayEquals(values(times), values(fromPoints));

		// Determinism.
		final RandomAccessibleInterval<DoubleType> again = ops.op(
			"image.fastMarching").input(seeds, speed).outType(TIMES).apply();
		assertArrayEquals(values(times), values(again));
	}

	@Test
	public void testStopTimeAndSegmentation() {
		final var speed = ArrayImgs.doubles(31, 31);
		speed.forEach(t -> t.set(1));
		final var seed = List.of(new Point(15, 15));
		final RandomAccessibleInterval<DoubleType> full = ops.op(
			"image.fastMarching").input(seed, speed).outType(TIMES).apply();
		final RandomAccessibleInterval<DoubleType> stopped = ops.op(
			"image.fastMarching").input(seed, speed, null, 6.0).outType(TIMES)
			.apply();
		final RandomAccessibleInterval<BitType> mask = ops.op(
			"segment.fastMarching").input(seed, speed, 6.0).outType(MASK).apply();
		final var fc = Views.flatIterable(full).cursor();
		final var mc = Views.flatIterable(mask).cursor();
		var count = 0;
		for (final var s : Views.flatIterable(stopped)) {
			final var f = fc.next().get();
			final var inside = mc.next().get();
			assertEquals(f <= 6, inside);
			assertEquals(inside ? f : Double.POSITIVE_INFINITY, s.get());
			if (inside) count++;
		}
		// Roughly a disk of radius 6.
		assertEquals(Math.PI * 36, count, 12);

		// An offset interval is respected.
		final var shifted = Views.translate(speed, 100, -50);
		final RandomAccessibleInterval<DoubleType> st = ops.op(
			"image.fastMarching").input(List.of(new Point(115, -35)), shifted)
			.outType(TIMES).apply();
		assertEquals(100, st.min(0));
		assertArrayEquals(values(full), values(st));
		assertThrows(RuntimeException.class, () -> ops.op("image.fastMarching")
			.input(List.of(new Point(0, 0)), shifted).outType(TIMES).apply());
	}
}
