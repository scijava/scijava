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

package org.scijava.ops.image.threshold.rats;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link Rats} and {@link RatsSurface}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class RatsTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<BitType>> BITS =
		new Nil<>()
		{};

	private static final Nil<RandomAccessibleInterval<FloatType>> FLOATS =
		new Nil<>()
		{};

	/** A step edge gives the mid-level as threshold everywhere. */
	@Test
	public void testStepEdge() {
		final var in = ArrayImgs.unsignedBytes(64, 40);
		final var c = in.localizingCursor();
		while (c.hasNext())
			c.next().set(c.getIntPosition(0) < 32 ? 10 : 90);
		final var surface = ops.op("threshold.ratsSurface").input(in, 1.0, 3.0,
			16).outType(FLOATS).apply();
		assertArrayEquals(new long[] { 64, 40 }, surface.dimensionsAsLongArray());
		for (final var t : surface)
			assertEquals(50, t.get(), 1e-5);
		final var out = ops.op("threshold.rats").input(in, 1.0, 3.0, 16).outType(
			BITS).apply();
		final var oc = out.localizingCursor();
		while (oc.hasNext()) {
			final var b = oc.next().get();
			assertEquals(oc.getIntPosition(0) >= 32, b);
		}
	}

	@Test
	public void testStepEdge3D() {
		final var in = ArrayImgs.floats(12, 10, 20);
		final var c = in.localizingCursor();
		while (c.hasNext())
			c.next().set(c.getIntPosition(2) < 7 ? 100 : 40);
		final var out = ops.op("threshold.rats").input(in, 0.5, 3.0, 4).outType(
			BITS).apply();
		final var oc = out.localizingCursor();
		while (oc.hasNext()) {
			final var b = oc.next().get();
			assertEquals(oc.getIntPosition(2) < 7, b);
		}
	}

	@Test
	public void testFlatImage() {
		final var in = ArrayImgs.floats(20, 10, 3);
		in.forEach(t -> t.set(5));
		final var surface = ops.op("threshold.ratsSurface").input(in).outType(
			FLOATS).apply();
		for (final var t : surface)
			assertEquals(5, t.get(), 0);
		final var out = ops.op("threshold.rats").input(in).outType(BITS).apply();
		for (final var b : out)
			assertFalse(b.get());
	}

	/**
	 * Bright disks on a strong illumination ramp, with noise: no global
	 * threshold separates them (the background on the right is brighter than
	 * the disks on the left), but RATS does.
	 */
	@Test
	public void testUnevenIllumination() {
		final int w = 128, h = 64;
		final var rnd = new Random(17);
		final var in = ArrayImgs.floats(w, h);
		final var truth = new boolean[w * h];
		final int[][] disks = { { 16, 16 }, { 48, 40 }, { 80, 20 }, { 112, 44 },
			{ 20, 48 }, { 100, 12 } };
		final var c = in.localizingCursor();
		var i = 0;
		while (c.hasNext()) {
			c.fwd();
			final int x = c.getIntPosition(0), y = c.getIntPosition(1);
			var inside = false;
			for (final var d : disks)
				inside |= (x - d[0]) * (x - d[0]) + (y - d[1]) * (y - d[1]) <= 36;
			truth[i++] = inside;
			c.get().set((float) (20 + 0.6 * x + (inside ? 60 : 0) + 3 * rnd
				.nextGaussian()));
		}
		// Sanity check: the darkest disk is darker than the brightest
		// background.
		assertTrue(20 + 0.6 * 10 + 60 < 20 + 0.6 * 127);

		for (final Double sigma : new Double[] { 3.0, null }) {
			final var out = ops.op("threshold.rats").input(in, sigma, 3.0, 16)
				.outType(BITS).apply();
			var errors = 0;
			i = 0;
			for (final var b : out)
				if (b.get() != truth[i++]) errors++;
			assertTrue(errors < 0.02 * w * h, "sigma=" + sigma + ": " + errors +
				" errors");
		}
	}

	/**
	 * A quantized, low-noise 8-bit image: a background of 10 with about 10% of
	 * the pixels at 11, and a bright disk. More than half of the horizontal
	 * differences are 0, so the median absolute deviation of the differences is
	 * 0. Before the noise estimate was floored at the quantization noise, sigma
	 * was 0 and every isolated 11 became foreground speckle.
	 */
	@Test
	public void testQuantizedLowNoise() {
		final int w = 64, h = 64;
		final var rnd = new Random(5);
		final var in = ArrayImgs.unsignedBytes(w, h);
		final var truth = new boolean[w * h];
		final var c = in.localizingCursor();
		var i = 0;
		while (c.hasNext()) {
			c.fwd();
			final int x = c.getIntPosition(0) - 32, y = c.getIntPosition(1) - 32;
			final var inside = x * x + y * y <= 100;
			truth[i++] = inside;
			c.get().set(inside ? 100 : rnd.nextDouble() < 0.1 ? 11 : 10);
		}
		final var out = ops.op("threshold.rats").input(in, null, 3.0, 16)
			.outType(BITS).apply();
		var errors = 0;
		i = 0;
		for (final var b : out)
			if (b.get() != truth[i++]) errors++;
		assertEquals(0, errors);
	}

	/** Inputs with a non-zero min give results at the same coordinates. */
	@Test
	public void testTranslatedInput() {
		final var in = ArrayImgs.unsignedBytes(64, 40);
		final var c = in.localizingCursor();
		while (c.hasNext())
			c.next().set(c.getIntPosition(0) < 20 ? 10 : 90);
		final var translated = Views.translate(in, 7, -3);
		final var surface = ops.op("threshold.ratsSurface").input(translated, 1.0,
			3.0, 16).outType(FLOATS).apply();
		assertArrayEquals(new long[] { 7, -3 }, surface.minAsLongArray());
		assertArrayEquals(new long[] { 70, 36 }, surface.maxAsLongArray());
		final var out = ops.op("threshold.rats").input(translated, 1.0, 3.0, 16)
			.outType(BITS).apply();
		assertArrayEquals(new long[] { 7, -3 }, out.minAsLongArray());
		final var oc = out.localizingCursor();
		final var ra = translated.randomAccess();
		while (oc.hasNext()) {
			final var b = oc.next().get();
			assertEquals(ra.setPositionAndGet(oc).get() == 90, b);
		}
	}

	/** NaN pixels are ignored and do not spoil the other pixels. */
	@Test
	public void testNaN() {
		final var rnd = new Random(9);
		final var in = ArrayImgs.floats(64, 40);
		final var c = in.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set(rnd.nextDouble() < 0.02 ? Float.NaN : c.getIntPosition(
				0) < 32 ? 10 : 90);
		}
		final var out = ops.op("threshold.rats").input(in).outType(BITS)
			.apply();
		final var oc = out.localizingCursor();
		final var ic = in.cursor();
		while (oc.hasNext()) {
			final var b = oc.next().get();
			final var v = ic.next().get();
			assertEquals(!Float.isNaN(v) && oc.getIntPosition(0) >= 32, b);
		}
	}
}
