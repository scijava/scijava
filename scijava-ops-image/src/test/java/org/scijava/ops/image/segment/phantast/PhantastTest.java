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

package org.scijava.ops.image.segment.phantast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link DefaultPhantast}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class PhantastTest extends AbstractOpTest {

	static final int W = 64, H = 64;

	private static final Nil<RandomAccessibleInterval<BitType>> MASK =
		new Nil<>()
		{};

	/**
	 * Synthetic phase contrast image: textured cells (one of them tiny and
	 * without halo) surrounded by bright halos on a flat noisy background, with
	 * flat holes in the two large cells.
	 */
	public static Img<FloatType> synth() {
		final var p = new float[W * H];
		var seed = 12345;
		final int[][] cells = { { 20, 22, 13 }, { 46, 44, 11 }, { 52, 10, 3 } };
		for (var y = 0; y < H; y++) {
			for (var x = 0; x < W; x++) {
				seed = seed * 1103515245 + 12345;
				final var noise = ((seed >>> 16) & 0x7fff) % 5 - 2;
				double v = 100 + noise;
				for (final var c : cells) {
					final double dx = x - c[0], dy = y - c[1];
					final var r = Math.sqrt(dx * dx + dy * dy);
					if (r <= c[2]) {
						final var hole = c == cells[0] && dx * dx + dy * dy <= 9 ||
							c == cells[1] && dx * dx + dy * dy <= 25;
						if (!hole) v = 100 + 25 * StrictMath.sin(x * 1.3) * StrictMath
							.cos(y * 1.7) + 2 * noise;
					}
					else if (r <= c[2] + 3 && c != cells[2]) {
						v += 60 * (c[2] + 3 - r) / 3.0;
					}
				}
				p[x + W * y] = (float) v;
			}
		}
		return ArrayImgs.floats(p, W, H);
	}

	/**
	 * Compares the mask to the one of the original PHANTAST plugin. The
	 * expected masks were produced by running the steps of
	 * {@code PHANTAST_.run} of PHANTAST-FIJI (local contrast, threshold,
	 * {@code removeSmallObjectsAndFillHoles}, {@code haloCorrection}) on
	 * {@link #synth()} with sigma 1.2 and epsilon 0.03, with ImageJ black
	 * background off, after fixing the off-by-one errors in
	 * {@code haloCorrection} (see {@link DefaultPhantast}). In that
	 * configuration, the plugin fills holes smaller than 100 pixels and removes
	 * objects smaller than 25 pixels.
	 */
	@Test
	public void testPhantastRegression() throws IOException {
		final var noHalo = ops.op("segment.phantast").input(synth(), 1.2, 0.03,
			false, 25L, 100L).outType(MASK).apply();
		assertMask("phantast-nohalo.txt", noHalo);
		final var withHalo = ops.op("segment.phantast").input(synth(), 1.2, 0.03,
			true, 25L, 100L).outType(MASK).apply();
		assertMask("phantast-halo.txt", withHalo);
	}

	/**
	 * With the default sizes, the tiny cell is removed and the large hole is
	 * kept, and halo correction shrinks the cells.
	 */
	@Test
	public void testPhantastDefaults() {
		final var defaults = ops.op("segment.phantast").input(synth(), 1.2, 0.03)
			.outType(MASK).apply();
		final var noHalo = ops.op("segment.phantast").input(synth(), 1.2, 0.03,
			false).outType(MASK).apply();
		final var ra = noHalo.randomAccess();
		// The tiny cell is removed.
		assertTrue(!ra.setPositionAndGet(52, 10).get());
		// The small hole is filled, but the large one is kept.
		assertTrue(ra.setPositionAndGet(20, 22).get());
		assertTrue(!ra.setPositionAndGet(46, 44).get());
		// The halo around the first cell is segmented without halo correction.
		assertTrue(ra.setPositionAndGet(20, 22 + 15).get());

		final var rd = defaults.randomAccess();
		assertTrue(!rd.setPositionAndGet(20, 22 + 15).get());
		assertTrue(rd.setPositionAndGet(20, 22 + 8).get());
		assertTrue(count(defaults) < count(noHalo));
		// Halo correction only removes pixels.
		final var cd = Views.flatIterable(defaults).cursor();
		final var cn = Views.flatIterable(noHalo).cursor();
		while (cd.hasNext()) {
			final var d = cd.next().get();
			final var n = cn.next().get();
			assertTrue(!d || n);
		}
	}

	/** Without halo correction, n-D images are supported. */
	@Test
	public void testPhantast3D() {
		final var flat = ArrayImgs.floats(10, 10, 3);
		for (final var t : flat)
			t.set(5);
		final var mask = ops.op("segment.phantast").input(flat, 1.2, 0.03)
			.outType(MASK).apply();
		assertEquals(3, mask.numDimensions());
		assertEquals(0, count(mask));
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"segment.phantast").input(flat, 1.2, 0.03, true).outType(MASK).apply());
	}

	private static long count(final RandomAccessibleInterval<BitType> mask) {
		long n = 0;
		for (final var b : Views.iterable(mask))
			if (b.get()) n++;
		return n;
	}

	private static void assertMask(final String resource,
		final RandomAccessibleInterval<BitType> mask) throws IOException
	{
		final List<String> expected = new ArrayList<>();
		try (var in = new BufferedReader(new InputStreamReader(PhantastTest.class
			.getResourceAsStream(resource), StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = in.readLine()) != null)
				expected.add(line);
		}
		final List<String> actual = new ArrayList<>();
		final var ra = mask.randomAccess();
		for (var y = 0; y < H; y++) {
			final var sb = new StringBuilder();
			for (var x = 0; x < W; x++) {
				sb.append(ra.setPositionAndGet(x, y).get() ? '#' : '.');
			}
			actual.add(sb.toString());
		}
		assertEquals(String.join("\n", expected), String.join("\n", actual));
	}

	/** NaN would spread through the local contrast filter, so it is rejected. */
	@Test
	public void testNaN() {
		final var img = synth();
		img.firstElement().set(Float.NaN);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"segment.phantast").input(img, 1.2, 0.03).outType(MASK).apply());
	}
}
