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

package org.scijava.ops.image.image.distancetransform;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.util.MersenneTwisterFast;

/**
 * Tests {@code image.distanceTransform} on inputs whose min is not zero, for
 * the 2D, 3D and n-D implementations, with and without calibration.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DistanceTransformNonZeroMinTest extends AbstractOpTest {

	private static final double EPSILON = 1e-4;

	@Test
	public void test2D() {
		check(new long[] { 30, 28 }, new double[] { 2.54, 1.77 });
	}

	@Test
	public void test3D() {
		check(new long[] { 22, 20, 9 }, new double[] { 1.3, 0.7, 2.1 });
	}

	@Test
	public void testND() {
		check(new long[] { 14, 12, 7, 5 }, new double[] { 1.1, 0.9, 1.7, 2.3 });
	}

	private void check(final long[] dims, final double[] calibration) {
		final var n = dims.length;
		final var img = ArrayImgs.bits(dims);
		final var random = new MersenneTwisterFast(0x12345678);
		// Note: Mostly foreground, so that distances span several pixels.
		img.forEach(b -> b.set(random.nextDouble() < 0.85));

		// A sub-interval of a larger image: the DT must only see the sub-interval.
		final var min = new long[n];
		final var max = new long[n];
		for (var d = 0; d < n; d++) {
			min[d] = 1 + d % 2;
			max[d] = dims[d] - 2;
		}
		checkView(Views.interval(img, min, max), calibration);

		// A translated image: its positions lie entirely outside [0, dims).
		final var offset = new long[n];
		for (var d = 0; d < n; d++)
			offset[d] = d % 2 == 0 ? 100 + d : -50 - d;
		checkView(Views.translate(img, offset), calibration);
	}

	private void checkView(final RandomAccessibleInterval<BitType> view,
		final double[] calibration)
	{
		// Expected: the DT of a zero-min copy of the view.
		final var copy = ArrayImgs.bits(view.dimensionsAsLongArray());
		LoopBuilder.setImages(view, copy).forEachPixel((s, t) -> t.set(s));

		for (final var cal : Arrays.asList(null, calibration)) {
			final var expected = ArrayImgs.floats(view.dimensionsAsLongArray());
			final var actual = Views.translate(ArrayImgs.floats(view
				.dimensionsAsLongArray()), view.minAsLongArray());
			if (cal == null) {
				ops.op("image.distanceTransform").input(copy).output(expected)
					.compute();
				ops.op("image.distanceTransform").input(view).output(actual)
					.compute();
			}
			else {
				ops.op("image.distanceTransform").input(copy, cal).output(expected)
					.compute();
				ops.op("image.distanceTransform").input(view, cal).output(actual)
					.compute();
			}
			assertArrayEquals(view.minAsLongArray(), actual.minAsLongArray());
			final Cursor<FloatType> e = expected.cursor();
			final RandomAccess<FloatType> a = Views.zeroMin(actual).randomAccess();
			while (e.hasNext()) {
				e.fwd();
				a.setPosition(e);
				assertEquals(e.get().get(), a.get().get(), EPSILON, "at " + Arrays
					.toString(e.positionAsLongArray()) + ", calibration " + Arrays
						.toString(cal));
			}
		}
	}
}
