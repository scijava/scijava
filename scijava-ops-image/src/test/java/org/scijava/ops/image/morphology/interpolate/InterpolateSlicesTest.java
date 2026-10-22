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

package org.scijava.ops.image.morphology.interpolate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@code morphology.interpolateSlices}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class InterpolateSlicesTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<BitType>> OUT =
		new Nil<>()
		{};

	private static ArrayImg<BitType, LongArray> sphere(final int size,
		final double radius)
	{
		final var img = ArrayImgs.bits(size, size, size);
		final var c = img.localizingCursor();
		final double m = (size - 1) / 2.0;
		while (c.hasNext()) {
			c.fwd();
			var r2 = 0.0;
			for (var d = 0; d < 3; d++)
				r2 += Math.pow(c.getDoublePosition(d) - m, 2);
			c.get().set(r2 <= radius * radius);
		}
		return img;
	}

	@Test
	public void testSphere() {
		final var size = 41;
		final var truth = sphere(size, 18);
		// Keep every fourth slice along z.
		final var sparse = ArrayImgs.bits(size, size, size);
		final var sc = sparse.localizingCursor();
		final var tr = truth.randomAccess();
		while (sc.hasNext()) {
			sc.fwd();
			if (sc.getLongPosition(2) % 4 == 0) sc.get().set(tr.setPositionAndGet(sc)
				.get());
		}
		final RandomAccessibleInterval<BitType> out = ops.op(
			"morphology.interpolateSlices").input(sparse).outType(OUT).apply();

		long both = 0, a = 0, b = 0;
		final var oc = Views.flatIterable(out).cursor();
		for (final var t : truth) {
			final var o = oc.next().get();
			if (t.get()) a++;
			if (o) b++;
			if (o && t.get()) both++;
		}
		final var dice = 2.0 * both / (a + b);
		assertTrue(dice > 0.95, "Dice " + dice);

		// Annotated slices are unchanged.
		final var or = out.randomAccess();
		final var sr = sparse.randomAccess();
		for (var x = 0; x < size; x++)
			for (var y = 0; y < size; y++)
				assertEquals(sr.setPositionAndGet(x, y, 20).get(), or
					.setPositionAndGet(x, y, 20).get());
	}

	@Test
	public void testMorphBetweenSquares() {
		// A 2 x 2 square on slice 0 and a 6 x 6 square on slice 4 (both centred,
		// interpolated along dimension 0); the middle slice gets a 4 x 4 square.
		final var mask = ArrayImgs.bits(5, 10, 10);
		final var ra = mask.randomAccess();
		for (var y = 0; y < 10; y++) {
			for (var x = 0; x < 10; x++) {
				ra.setPositionAndGet(0, x, y).set(x >= 4 && x <= 5 && y >= 4 &&
					y <= 5);
				ra.setPositionAndGet(4, x, y).set(x >= 2 && x <= 7 && y >= 2 &&
					y <= 7);
			}
		}
		final RandomAccessibleInterval<BitType> out = ops.op(
			"morphology.interpolateSlices").input(mask, 0).outType(OUT).apply();
		final var oa = out.randomAccess();
		for (var y = 0; y < 10; y++) {
			for (var x = 0; x < 10; x++) {
				final var inside = x >= 3 && x <= 6 && y >= 3 && y <= 6;
				assertEquals(inside, oa.setPositionAndGet(2, x, y).get(), "at " + x +
					", " + y);
			}
		}
	}

	@Test
	public void testSingleAnnotatedSlice() {
		final var mask = ArrayImgs.bits(6, 6, 4);
		mask.randomAccess().setPositionAndGet(2, 3, 1).set(true);
		final RandomAccessibleInterval<BitType> out = ops.op(
			"morphology.interpolateSlices").input(mask).outType(OUT).apply();
		final var mc = mask.cursor();
		for (final var t : Views.flatIterable(out))
			assertEquals(mc.next().get(), t.get());
	}

	@Test
	public void testNonZeroMin() {
		// Squares of growing size on every third slice along z, in a larger
		// image with foreground outside of the cropped region.
		final var big = ArrayImgs.bits(18, 16, 13);
		final var c = big.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final long x = c.getLongPosition(0), y = c.getLongPosition(1), z = c
				.getLongPosition(2);
			final var r = 1 + z / 3;
			final var inCrop = x >= 2 && x <= 15 && y >= 3 && y <= 14 && z >= 1;
			if (!inCrop) c.get().set((x + y) % 2 == 0);
			else if ((z - 1) % 3 == 0) c.get().set(Math.abs(x - 8) <= r && Math.abs(
				y - 9) <= r);
		}
		final var crop = Views.interval(big, new long[] { 2, 3, 1 }, new long[] {
			15, 14, 12 });
		checkNonZeroMin(crop, null);
		checkNonZeroMin(crop, 2);
		checkNonZeroMin(Views.translate(crop, 30, -20, 7), null);
	}

	private void checkNonZeroMin(final RandomAccessibleInterval<BitType> view,
		final Integer dim)
	{
		final var copy = ArrayImgs.bits(view.dimensionsAsLongArray());
		LoopBuilder.setImages(view, copy).forEachPixel((s, t) -> t.set(s));
		final RandomAccessibleInterval<BitType> expected = ops.op(
			"morphology.interpolateSlices").input(copy, dim).outType(OUT).apply();
		final RandomAccessibleInterval<BitType> actual = ops.op(
			"morphology.interpolateSlices").input(view, dim).outType(OUT).apply();
		assertArrayEquals(view.minAsLongArray(), actual.minAsLongArray());
		assertArrayEquals(view.maxAsLongArray(), actual.maxAsLongArray());
		final var e = Views.flatIterable(expected).cursor();
		var filled = 0;
		for (final var a : Views.flatIterable(actual)) {
			final var ev = e.next().get();
			assertEquals(ev, a.get());
			if (ev) filled++;
		}
		// Sanity check: the interpolation filled more than the annotated slices.
		var annotated = 0;
		for (final var b : copy)
			if (b.get()) annotated++;
		assertTrue(filled > annotated, filled + " <= " + annotated);
	}
}
