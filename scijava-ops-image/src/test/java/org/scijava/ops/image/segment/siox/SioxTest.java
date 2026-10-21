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

package org.scijava.ops.image.segment.siox;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@code segment.siox}.
 *
 * @author Curtis Rueden
 */
public class SioxTest extends AbstractOpTest {

	private static final int W = 32, H = 24;

	/**
	 * Expected masks, produced by {@code org.siox.SioxSegmentator.segmentate}
	 * of the Fiji Siox_Segmentation project (version 1.0.5) on the image of
	 * {@link #image()} and the trimap of {@link #trimap(float)}, with
	 * smoothness 6 and size factor 0.
	 */
	private static final String[] EXPECTED_6_0 = { //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		".......#######..................", //
		"......#########.................", //
		".....###########................", //
		".....###########................", //
		".....###########................", //
		".....###########................", //
		".....############...............", //
		".....############...............", //
		".....############...............", //
		"......#########.................", //
		".......#######..................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
	};

	/** As {@link #EXPECTED_6_0}, but with smoothness 0 and size factor 4. */
	private static final String[] EXPECTED_0_4 = { //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		".......#######..................", //
		"......#########.......#####.....", //
		".....###########.....#######....", //
		".....###########.....#######....", //
		".....###########.....#######....", //
		".....###########.....#######....", //
		".....###########.....#######....", //
		"......#########.......#####.....", //
		".......#######..................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
	};

	/**
	 * As {@link #EXPECTED_6_0}, but with smoothness 2, size factor 4, and a
	 * known foreground confidence of 0.9 rather than 1.
	 */
	private static final String[] EXPECTED_2_4_UNCERTAIN = { //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		".......#######..................", //
		"......#########.................", //
		".....###########................", //
		".....###########................", //
		".....###########................", //
		".....###########................", //
		".....###########................", //
		"......#########.................", //
		".......#######..................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
		"................................", //
	};

	@Test
	public void testRegressionDefaults() {
		final var mask = siox(image(), trimap(1f));
		assertArrayEquals(EXPECTED_6_0, render(mask));
	}

	@Test
	public void testRegressionExplicit() {
		assertArrayEquals(EXPECTED_6_0, render(siox(image(), trimap(1f), 6, 0.0)));
		// Note: With smoothness 6, the original also drops the smaller object.
		assertArrayEquals(EXPECTED_6_0, render(siox(image(), trimap(1f), 6, 4.0)));
		assertArrayEquals(EXPECTED_0_4, render(siox(image(), trimap(1f), 0, 4.0)));
		assertArrayEquals(EXPECTED_2_4_UNCERTAIN, render(siox(image(), trimap(
			0.9f), 2, 4.0)));
	}

	@Test
	public void testUncertainForegroundWithoutMultipart() {
		// Without samples of confidence 1, size factor 0 keeps no component.
		final var mask = siox(image(), trimap(0.9f), 6, 0.0);
		for (final BitType b : Views.iterable(mask))
			assertTrue(!b.get());
	}

	@Test
	public void testMasks() {
		final var tri = trimap(1f);
		final Img<BitType> fg = ArrayImgs.bits(W, H);
		final Img<BitType> bg = ArrayImgs.bits(W, H);
		final var t = tri.cursor();
		final var f = fg.cursor();
		final var b = bg.cursor();
		while (t.hasNext()) {
			final float v = t.next().get();
			f.next().set(v == 1f);
			b.next().set(v == 0f);
		}
		@SuppressWarnings("unchecked")
		final var mask = (RandomAccessibleInterval<BitType>) ops.op("segment.siox")
			.input(image(), fg, bg).apply();
		assertArrayEquals(EXPECTED_6_0, render(mask));
		@SuppressWarnings("unchecked")
		final var mask2 = (RandomAccessibleInterval<BitType>) ops.op(
			"segment.siox").input(image(), fg, bg, 0, 4.0).apply();
		assertArrayEquals(EXPECTED_0_4, render(mask2));
	}

	@Test
	public void testChannels() {
		final var argb = image();
		final Img<UnsignedByteType> u8 = ArrayImgs.unsignedBytes(W, H, 3);
		final Img<FloatType> f32 = ArrayImgs.floats(W, H, 3);
		final var ra = argb.randomAccess();
		final var ru = u8.randomAccess();
		final var rf = f32.randomAccess();
		for (int y = 0; y < H; y++) {
			for (int x = 0; x < W; x++) {
				final int v = ra.setPositionAndGet(x, y).get();
				final int[] rgb = { ARGBType.red(v), ARGBType.green(v), ARGBType.blue(
					v) };
				for (int c = 0; c < 3; c++) {
					ru.setPositionAndGet(x, y, c).set(rgb[c]);
					rf.setPositionAndGet(x, y, c).set(rgb[c] / 255f);
				}
			}
		}
		assertArrayEquals(EXPECTED_0_4, render(siox(u8, trimap(1f), 0, 4.0)));
		assertArrayEquals(EXPECTED_0_4, render(siox(f32, trimap(1f), 0, 4.0)));
		assertArrayEquals(EXPECTED_6_0, render(siox(u8, trimap(1f))));

		final Img<BitType> fg = ArrayImgs.bits(W, H);
		final Img<BitType> bg = ArrayImgs.bits(W, H);
		final var t = trimap(1f).cursor();
		final var f = fg.cursor();
		final var b = bg.cursor();
		while (t.hasNext()) {
			final float v = t.next().get();
			f.next().set(v == 1f);
			b.next().set(v == 0f);
		}
		@SuppressWarnings("unchecked")
		final var mask = (RandomAccessibleInterval<BitType>) ops.op("segment.siox")
			.input(u8, fg, bg).apply();
		assertArrayEquals(EXPECTED_6_0, render(mask));
	}

	/** A noisy two-color sphere in 3D is extracted almost exactly. */
	@Test
	public void testSphere3D() {
		final int n = 16;
		final Img<ARGBType> img = ArrayImgs.argbs(n, n, n);
		final Img<FloatType> tri = ArrayImgs.floats(n, n, n);
		final boolean[] truth = new boolean[n * n * n];
		final var c = img.localizingCursor();
		final var t = tri.cursor();
		int i = 0;
		while (c.hasNext()) {
			c.fwd();
			t.fwd();
			final long x = c.getLongPosition(0) - 8, y = c.getLongPosition(1) - 8,
				z = c.getLongPosition(2) - 8;
			final long r2 = x * x + y * y + z * z;
			final boolean inside = r2 <= 25;
			truth[i++] = inside;
			final int noise = (int) (next() % 21) - 10;
			c.get().set(inside ? ARGBType.rgba(200 + noise, 60 - noise, 60, 255)
				: ARGBType.rgba(40, 120 + noise, 200 - noise, 255));
			t.get().set(r2 <= 4 ? 1f : r2 >= 64 ? 0f : 0.5f);
		}
		final var mask = siox(img, tri, 0, 0.0);
		int missed = 0, extra = 0, inside = 0;
		i = 0;
		for (final BitType b : Views.flatIterable(mask)) {
			if (truth[i]) inside++;
			if (truth[i] && !b.get()) missed++;
			if (!truth[i] && b.get()) extra++;
			i++;
		}
		// Note: Smoothing and erosion round off the surface, while the final
		// dilation grows the object by about one sample.
		assertTrue(missed < 0.1 * inside, "missed samples: " + missed);
		assertTrue(extra < 0.5 * inside, "extra samples: " + extra);
	}

	@Test
	public void testInvalidInputs() {
		final Img<FloatType> allFg = ArrayImgs.floats(W, H);
		for (final FloatType v : allFg)
			v.set(1f);
		assertThrows(RuntimeException.class, () -> siox(image(), allFg));

		final Img<BitType> fg = ArrayImgs.bits(W, H);
		final Img<BitType> bg = ArrayImgs.bits(W, H);
		fg.firstElement().set(true);
		bg.firstElement().set(true);
		assertThrows(RuntimeException.class, () -> ops.op("segment.siox").input(
			image(), fg, bg).apply());

		assertThrows(RuntimeException.class, () -> siox(image(), ArrayImgs.floats(
			W, H + 1)));
	}

	@Test
	public void testRgbToLab() {
		// Reference white and black of sRGB.
		final float[] white = Siox.rgbToLab(1, 1, 1);
		assertEquals(100, white[0], 1e-2);
		// Note: The rounded matrix coefficients leave a small chroma offset.
		assertEquals(0, white[1], 0.02);
		assertEquals(0, white[2], 0.02);
		assertArrayEquals(new float[] { 0, 0, 0 }, Siox.rgbToLab(0, 0, 0), 1e-4f);
		// Pure sRGB red: L=53.24, a=80.09, b=67.20.
		final float[] red = Siox.rgbToLab(1, 0, 0);
		assertEquals(53.24, red[0], 0.05);
		assertEquals(80.09, red[1], 0.1);
		assertEquals(67.20, red[2], 0.1);
	}

	// -- Helper methods --

	private int seed = 12345;

	private int next() {
		seed = seed * 1103515245 + 12345;
		return (seed >>> 16) & 0x7fff;
	}

	private static int clamp(final int v) {
		return Math.max(0, Math.min(255, v));
	}

	/**
	 * Creates a noisy image of an orange ellipse and a smaller orange disk on
	 * a blue background. Must match the harness that produced the expected
	 * masks.
	 */
	private Img<ARGBType> image() {
		seed = 12345;
		final Img<ARGBType> img = ArrayImgs.argbs(W, H);
		final var ra = img.randomAccess();
		for (int y = 0; y < H; y++) {
			for (int x = 0; x < W; x++) {
				final int nr = next() % 21 - 10, ng = next() % 21 - 10, nb = next() %
					21 - 10;
				final boolean fg = (x - 10) * (x - 10) / 36.0 + (y - 12) * (y - 12) /
					25.0 <= 1 || (x - 24) * (x - 24) + (y - 12) * (y - 12) <= 16;
				final int r, g, b;
				if (fg) {
					r = 220 + nr;
					g = 120 + ng;
					b = 40 + nb;
				}
				else {
					r = 70 + nr;
					g = 90 + ng;
					b = 150 + nb;
				}
				ra.setPositionAndGet(x, y).set(0xff000000 | clamp(r) << 16 | clamp(
					g) << 8 | clamp(b));
			}
		}
		return img;
	}

	/**
	 * Creates a trimap with a two-sample background border and a known
	 * foreground rectangle inside the ellipse.
	 */
	private static Img<FloatType> trimap(final float fgValue) {
		final Img<FloatType> tri = ArrayImgs.floats(W, H);
		final var ra = tri.randomAccess();
		for (int y = 0; y < H; y++) {
			for (int x = 0; x < W; x++) {
				float c = 0.5f;
				if (x < 2 || y < 2 || x >= W - 2 || y >= H - 2) c = 0f;
				else if (x >= 8 && x <= 12 && y >= 10 && y <= 14) c = fgValue;
				ra.setPositionAndGet(x, y).set(c);
			}
		}
		return tri;
	}

	@SuppressWarnings("unchecked")
	private static RandomAccessibleInterval<BitType> siox(final Object image,
		final Object trimap)
	{
		return (RandomAccessibleInterval<BitType>) ops.op("segment.siox").input(
			image, trimap).apply();
	}

	@SuppressWarnings("unchecked")
	private static RandomAccessibleInterval<BitType> siox(final Object image,
		final Object trimap, final Integer smoothness, final Double sizeFactor)
	{
		return (RandomAccessibleInterval<BitType>) ops.op("segment.siox").input(
			image, trimap, smoothness, sizeFactor).apply();
	}

	private static String[] render(final RandomAccessibleInterval<BitType> m) {
		assertEquals(2, m.numDimensions());
		final String[] rows = new String[(int) m.dimension(1)];
		final var ra = m.randomAccess();
		for (int y = 0; y < rows.length; y++) {
			final StringBuilder sb = new StringBuilder();
			for (int x = 0; x < m.dimension(0); x++) {
				sb.append(ra.setPositionAndGet(x, y).get() ? '#' : '.');
			}
			rows[y] = sb.toString();
		}
		return rows;
	}
}
