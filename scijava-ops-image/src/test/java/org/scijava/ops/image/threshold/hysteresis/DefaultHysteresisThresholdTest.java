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

package org.scijava.ops.image.threshold.hysteresis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link DefaultHysteresisThreshold}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DefaultHysteresisThresholdTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<BitType>> BITS =
		new Nil<>()
		{};

	@Test
	public void test1D() {
		final var in = ArrayImgs.unsignedBytes(new byte[] { //
			0, 5, 5, 10, 5, 0, 5, 6, 5, 0, 12, 0, 4, 5 }, 14);
		final var out = ops.op("threshold.hysteresis").input(in, 5.0, 10.0)
			.outType(BITS).apply();
		assertBits(out, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0);
	}

	@Test
	public void testConnectivity() {
		// A diagonal chain of weak pixels from a strong pixel.
		final var in = ArrayImgs.unsignedBytes(new byte[] { //
			20, 0, 0, //
			0, 8, 0, //
			0, 0, 8 }, 3, 3);
		final var eight = ops.op("threshold.hysteresis").input(in, 5.0, 10.0)
			.outType(BITS).apply();
		assertBits(eight, 1, 0, 0, 0, 1, 0, 0, 0, 1);
		final var four = ops.op("threshold.hysteresis").input(in, 5.0, 10.0,
			new DiamondShape(1)).outType(BITS).apply();
		assertBits(four, 1, 0, 0, 0, 0, 0, 0, 0, 0);
	}

	@Test
	public void testEqualThresholdsIsPlainThreshold() {
		final var in = randomImage(new Random(3), 9, 7, 5);
		final var out = ops.op("threshold.hysteresis").input(in, 100.0, 100.0)
			.outType(BITS).apply();
		final var c = in.cursor();
		final var oc = out.cursor();
		while (c.hasNext())
			assertEquals(c.next().get() >= 100, oc.next().get());
	}

	/** Compares against a naive fixed-point iteration on random 3D images. */
	@Test
	public void testRandomAgainstReference() {
		final var rnd = new Random(42);
		for (final Shape shape : new Shape[] { null, new DiamondShape(1) }) {
			final var in = randomImage(rnd, 12, 10, 6);
			final var out = ops.op("threshold.hysteresis").input(in, 230.0, 250.0,
				shape).outType(BITS).apply();
			final var expected = reference(in, 230, 250, shape != null);
			final var oc = out.cursor();
			var i = 0;
			var fg = 0;
			var weakOnly = 0;
			final var ic = in.cursor();
			while (oc.hasNext()) {
				final var b = oc.next().get();
				final var v = ic.next().get();
				assertEquals(expected[i++], b);
				if (b) fg++;
				if (!b && v >= 230) weakOnly++;
			}
			// Make sure the test is not trivial.
			assertTrue(fg > 10, "fg=" + fg);
			assertTrue(weakOnly > 10, "weakOnly=" + weakOnly);
		}
	}

	@Test
	public void testLowAboveHighFails() {
		final var in = ArrayImgs.unsignedBytes(3);
		assertThrows(RuntimeException.class, () -> ops.op("threshold.hysteresis")
			.input(in, 10.0, 5.0).outType(BITS).apply());
	}

	private static ArrayImg<UnsignedByteType, ByteArray> randomImage(
		final Random rnd, final long... dims)
	{
		final var img = ArrayImgs.unsignedBytes(dims);
		img.forEach(t -> t.set(rnd.nextInt(256)));
		return img;
	}

	private static boolean[] reference(
		final ArrayImg<UnsignedByteType, ByteArray> in, final int low,
		final int high, final boolean faces)
	{
		final long[] dims = in.dimensionsAsLongArray();
		final int w = (int) dims[0], h = (int) dims[1], dd = (int) dims[2];
		final var v = new int[w * h * dd];
		var i = 0;
		for (final var t : in)
			v[i++] = t.get();
		final var on = new boolean[v.length];
		for (i = 0; i < v.length; i++)
			on[i] = v[i] >= high;
		var changed = true;
		while (changed) {
			changed = false;
			for (var z = 0; z < dd; z++)
				for (var y = 0; y < h; y++)
					for (var x = 0; x < w; x++) {
						final var idx = x + w * (y + h * z);
						if (on[idx] || v[idx] < low) continue;
						for (var dz = -1; dz <= 1 && !on[idx]; dz++)
							for (var dy = -1; dy <= 1 && !on[idx]; dy++)
								for (var dx = -1; dx <= 1 && !on[idx]; dx++) {
									if (faces && Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1)
										continue;
									final int xx = x + dx, yy = y + dy, zz = z + dz;
									if (xx < 0 || yy < 0 || zz < 0 || xx >= w || yy >= h ||
										zz >= dd) continue;
									if (on[xx + w * (yy + h * zz)]) {
										on[idx] = true;
										changed = true;
									}
								}
					}
		}
		return on;
	}

	private static void assertBits(final RandomAccessibleInterval<BitType> out,
		final int... expected)
	{
		var i = 0;
		for (final var b : out)
			assertEquals(expected[i++] == 1, b.get(), "index " + (i - 1));
	}
}
