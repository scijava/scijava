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

package org.scijava.ops.image.segment.levelset;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.function.Predicate;

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
 * Tests {@code segment.levelSet} and {@code image.levelSet}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class LevelSetTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<BitType>> MASK =
		new Nil<>()
		{};

	private static final Nil<RandomAccessibleInterval<FloatType>> PHI =
		new Nil<>()
		{};

	/** Binary mask of the pixels whose position satisfies the predicate. */
	private static Img<BitType> mask(final Predicate<long[]> inside,
		final long... dims)
	{
		final var img = ArrayImgs.bits(dims);
		final var c = img.localizingCursor();
		final var p = new long[dims.length];
		while (c.hasNext()) {
			c.fwd();
			c.localize(p);
			c.get().set(inside.test(p));
		}
		return img;
	}

	/** Image with value 1 on the mask, 0 elsewhere, plus Gaussian noise. */
	private static Img<DoubleType> noisy(final Img<BitType> truth,
		final double noise, final long seed)
	{
		final var img = ArrayImgs.doubles(truth.dimensionsAsLongArray());
		final var random = new Random(seed);
		final var tc = truth.cursor();
		for (final var t : img)
			t.set((tc.next().get() ? 1 : 0) + noise * random.nextGaussian());
		return img;
	}

	private static double dice(final RandomAccessibleInterval<BitType> a,
		final RandomAccessibleInterval<BitType> b)
	{
		long both = 0, sum = 0;
		final var bc = Views.flatIterable(b).cursor();
		for (final var x : Views.flatIterable(a)) {
			final var y = bc.next().get();
			if (x.get() && y) both++;
			if (x.get()) sum++;
			if (y) sum++;
		}
		return 2.0 * both / sum;
	}

	static String ascii(final RandomAccessibleInterval<BitType> m) {
		final var sb = new StringBuilder("\n");
		for (long y = 0; y < m.dimension(1); y++) {
			for (long x = 0; x < m.dimension(0); x++)
				sb.append(m.getAt(x, y).get() ? '#' : '.');
			sb.append('\n');
		}
		return sb.toString();
	}

	private static double r2(final long[] p, final double... center) {
		var s = 0.0;
		for (var d = 0; d < p.length; d++)
			s += (p[d] - center[d]) * (p[d] - center[d]);
		return s;
	}

	@Test
	public void testChanVeseNoisyDisk() {
		final var truth = mask(p -> r2(p, 32, 32) <= 15 * 15, 64, 64);
		// Signal-to-noise ratio of 2: thresholding at 0.5 misclassifies 31 % of
		// the pixels.
		final var image = noisy(truth, 0.5, 1);
		// A poor initialization: a small square straddling the disk boundary.
		final var init = mask(p -> p[0] >= 40 && p[0] < 52 && p[1] >= 20 &&
			p[1] < 32, 64, 64);
		final RandomAccessibleInterval<BitType> result = ops.op(
			"segment.levelSet").input(image, init).outType(MASK).apply();
		final var dice = dice(result, truth);
		assertTrue(dice > 0.95, "Dice " + dice + ascii(result));
	}

	@Test
	public void testChanVese3DBlob() {
		final long[] dims = { 32, 30, 24 };
		final var truth = mask(p -> Math.pow((p[0] - 15) / 10.0, 2) + Math.pow(
			(p[1] - 14) / 7.0, 2) + Math.pow((p[2] - 12) / 5.0, 2) <= 1, dims);
		final var image = noisy(truth, 0.4, 2);
		final var init = mask(p -> Math.abs(p[0] - 22) <= 2 && Math.abs(p[1] -
			14) <= 2 && Math.abs(p[2] - 12) <= 2, dims);
		final RandomAccessibleInterval<BitType> result = ops.op(
			"segment.levelSet").input(image, init).outType(MASK).apply();
		final var dice = dice(result, truth);
		assertTrue(dice > 0.9, "Dice " + dice);
	}

	@Test
	public void testGeodesicStepEdge() {
		// A bright square [15, 44]^2 with mild noise.
		final var truth = mask(p -> p[0] >= 15 && p[0] <= 44 && p[1] >= 15 &&
			p[1] <= 44, 60, 60);
		final var image = noisy(truth, 0.05, 3);
		// Note: The edge lies halfway between pixels 14 and 15 (and 44 and 45),
		// and the contour settles within one pixel of it: the balloon force can
		// push it slightly past the minimum of the edge stopping function.

		// Expansion from a small disk inside the square.
		final var small = mask(p -> r2(p, 28, 31) <= 16, 60, 60);
		final RandomAccessibleInterval<BitType> grown = ops.op(
			"segment.levelSet").input(image, small, "geodesic").outType(MASK)
			.apply();
		final var dGrown = dice(grown, truth);
		assertTrue(dGrown > 0.93, "Dice " + dGrown + ascii(grown));
		assertWithinOnePixel(grown);

		// Shrinking from a large rectangle outside the square.
		final var large = mask(p -> p[0] >= 4 && p[0] <= 56 && p[1] >= 6 &&
			p[1] <= 54, 60, 60);
		final RandomAccessibleInterval<BitType> shrunk = ops.op(
			"segment.levelSet").input(image, large, "geodesic", 1.0, -1.0).outType(
				MASK).apply();
		final var dShrunk = dice(shrunk, truth);
		assertTrue(dShrunk > 0.93, "Dice " + dShrunk + ascii(shrunk));
		assertWithinOnePixel(shrunk);
	}

	/**
	 * Asserts that the boundary of a mask lies within one pixel of the edge of
	 * the square [15, 44]^2.
	 */
	private static void assertWithinOnePixel(
		final RandomAccessibleInterval<BitType> result)
	{
		final var dilated = mask(p -> p[0] >= 14 && p[0] <= 45 && p[1] >= 14 &&
			p[1] <= 45, 60, 60);
		final var eroded = mask(p -> p[0] >= 16 && p[0] <= 43 && p[1] >= 16 &&
			p[1] <= 43, 60, 60);
		final var rc = Views.flatIterable(result).cursor();
		final var ec = eroded.cursor();
		for (final var d : dilated) {
			final var r = rc.next().get();
			final var e = ec.next().get();
			assertTrue(!r || d.get(), ascii(result));
			assertTrue(r || !e, ascii(result));
		}
	}

	@Test
	public void testLevelSetFunctionAndDeterminism() {
		final var truth = mask(p -> r2(p, 16, 14) <= 64, 32, 30);
		final var image = noisy(truth, 0.3, 4);
		final var init = mask(p -> r2(p, 12, 12) <= 9, 32, 30);
		final RandomAccessibleInterval<FloatType> phi = ops.op("image.levelSet")
			.input(image, init).outType(PHI).apply();
		final RandomAccessibleInterval<FloatType> again = ops.op(
			"image.levelSet").input(image, init).outType(PHI).apply();
		final RandomAccessibleInterval<BitType> result = ops.op(
			"segment.levelSet").input(image, init).outType(MASK).apply();
		final var ac = Views.flatIterable(again).cursor();
		final var rc = Views.flatIterable(result).cursor();
		for (final var t : Views.flatIterable(phi)) {
			assertEquals(t.get(), ac.next().get());
			assertEquals(t.get() > 0, rc.next().get());
		}
		// The level-set function is a signed distance: about 8 (the radius) at
		// the center, up to the roughness of the noisy contour.
		assertEquals(8, phi.getAt(16, 14).get(), 1.5);

		// An offset interval is respected.
		final RandomAccessibleInterval<BitType> shifted = ops.op(
			"segment.levelSet").input(Views.translate(image, 5, -3), Views
				.translate(init, 5, -3)).outType(MASK).apply();
		assertArrayEquals(new long[] { 5, -3 }, shifted.minAsLongArray());
		assertEquals(1.0, dice(Views.zeroMin(shifted), result));

		// Without iterations, the initial mask is returned.
		final RandomAccessibleInterval<BitType> none = ops.op("segment.levelSet")
			.input(image, init, null, null, null, null, null, 0).outType(MASK)
			.apply();
		assertEquals(1.0, dice(none, init));

		assertThrows(RuntimeException.class, () -> ops.op("segment.levelSet")
			.input(image, init, "snake").outType(MASK).apply());
	}
}
