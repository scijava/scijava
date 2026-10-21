/*-
 * #%L
 * GPL-licensed image processing operations for SciJava Ops, translated from copyleft ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.gpl.biovoxxel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.gpl.AbstractOpTest;

/**
 * Tests {@link LimitedMeanThreshold}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class LimitedMeanThresholdTest extends AbstractOpTest {

	private static final int W = 32, H = 24;

	/**
	 * Synthetic test images; must match the harness that produced
	 * {@code limited-mean-expected.txt}.
	 */
	private static int value(final int kind, final int x, final int y) {
		final int n = ((x * 73856093) ^ (y * 19349663)) & 1023;
		final double r = n / 1023.0;
		final boolean blob = (x - 10) * (x - 10) + (y - 12) * (y - 12) < 36 ||
			(x - 24) * (x - 24) + (y - 8) * (y - 8) < 16;
		switch (kind) {
			case 0: // dark background with noise, bright blobs
				return blob ? (int) (150 + 80 * r) : (int) (20 + 30 * r * r);
			case 1: // bright background, dark blobs
				return blob ? (int) (30 + 70 * r) : (int) (200 + 40 * r);
			case 2: // background mostly exactly zero, graded blobs
				return blob ? (int) (60 + 190 * r) : (r < 0.8 ? 0 : (int) (40 * r));
			case 3: // ramp plus noise
				return Math.min(255, (int) (x * 6 + 60 * r));
			default: // saturated foreground
				return blob ? 255 : (int) (90 + 60 * r);
		}
	}

	private Img<BitType> threshold(final RandomAccessibleInterval<?> img,
		final boolean differential, final boolean force)
	{
		final Img<BitType> out = ArrayImgs.bits(img.dimensionsAsLongArray());
		ops.op("threshold.limitedMean").input(img, differential, force).output(out)
			.compute();
		return out;
	}

	private static String bits(final Img<BitType> img) {
		final StringBuilder sb = new StringBuilder();
		for (final BitType b : Views.flatIterable(img))
			sb.append(b.get() ? '1' : '0');
		return sb.toString();
	}

	/**
	 * Compares with the BioVoxxel Limited_Mean plugin (compiled from source),
	 * run in macro mode on the same synthetic 8-bit images by the harness in
	 * {@code .claude/fiji-plugin-ops/g2/src/LimHarness.java}; each line holds
	 * the image kind, the differential and force options, and the plugin
	 * result (1 for 255).
	 */
	@Test
	public void testRegression() throws IOException {
		int cases = 0;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(
			getClass().getResourceAsStream("limited-mean-expected.txt"),
			StandardCharsets.UTF_8)))
		{
			for (String line; (line = in.readLine()) != null;) {
				final String[] f = line.split(" ");
				final int kind = Integer.parseInt(f[0]);
				final Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(W, H);
				final Cursor<UnsignedByteType> c = img.localizingCursor();
				while (c.hasNext()) {
					c.fwd();
					c.get().set(value(kind, c.getIntPosition(0), c.getIntPosition(1)));
				}
				assertEquals(f[3], bits(threshold(img, Boolean.parseBoolean(f[1]),
					Boolean.parseBoolean(f[2]))), line.substring(0, 16));
				cases++;
			}
		}
		assertEquals(20, cases);
	}

	private static <T extends RealType<T>> Img<T> bimodal(final Img<T> img,
		final double background, final double low, final double high)
	{
		final Cursor<T> c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final int x = c.getIntPosition(0);
			final int y = c.getIntPosition(1);
			c.get().setReal(y < 2 ? (x % 2 == 0 ? low : high) : background);
		}
		return img;
	}

	/**
	 * With a dominant background, MoLiM removes it and thresholds at the mean
	 * of the remaining (low and high) values, keeping only the high ones.
	 */
	@Test
	public void testDarkBackground() {
		final Img<UnsignedByteType> img = bimodal(ArrayImgs.unsignedBytes(10, 10),
			10, 100, 200);
		final Img<BitType> out = threshold(img, false, false);
		final Cursor<BitType> c = out.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final boolean high = c.getIntPosition(1) < 2 && c.getIntPosition(0) %
				2 == 1;
			assertEquals(high, c.get().get());
		}
		// The float image gives the same partition.
		final Img<FloatType> fimg = bimodal(ArrayImgs.floats(10, 10), 10, 100,
			200);
		assertEquals(bits(out), bits(threshold(fimg, false, false)));
		// Default options (differential) give the same result here.
		final Img<BitType> def = ArrayImgs.bits(10, 10);
		ops.op("threshold.limitedMean").input(img, null, null).output(def)
			.compute();
		assertEquals(bits(out), bits(def));
	}

	/** A bright background is reversed: the darkest pixels are foreground. */
	@Test
	public void testBrightBackground() {
		final Img<UnsignedByteType> img = bimodal(ArrayImgs.unsignedBytes(10, 10),
			245, 155, 55);
		final Img<BitType> out = threshold(img, false, false);
		final Cursor<BitType> c = out.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final boolean dark = c.getIntPosition(1) < 2 && c.getIntPosition(0) %
				2 == 1;
			assertEquals(dark, c.get().get());
		}
		// Forcing keeps the histogram orientation, so the limitation removes
		// every bin up to the bright mode, the limited mean is undefined, and the
		// plugin then thresholds at 0: every pixel is foreground.
		for (final BitType b : threshold(img, false, true))
			assertEquals(true, b.get());
	}
}
