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

package org.scijava.ops.image.gpl.mcib3d;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.gpl.AbstractOpTest;

/**
 * Tests {@link SpotsFromSeeds}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class SpotsFromSeedsTest extends AbstractOpTest {

	// -- Synthetic 3D image; must match the harness that produced
	// spots-expected.txt. --

	private static final int W = 32, H = 28, D = 12;
	private static final double[][] BLOBS = { //
		{ 8, 8, 5, 200, 2, 2, 1.5 }, { 20, 9, 6, 150, 1.5, 1.5, 1 }, //
		{ 14, 20, 6, 120, 2.5, 2, 1.5 }, { 22, 20, 5, 90, 1.8, 1.8, 1.2 }, //
		{ 29, 3, 2, 100, 1.5, 1.5, 1 }, { 5, 22, 9, 40, 1, 1, 1 } };
	private static final int[][] SEEDS = { { 8, 8, 5 }, { 10, 8, 5 }, { 20, 9,
		6 }, { 14, 20, 6 }, { 18, 20, 6 }, { 22, 20, 5 }, { 29, 3, 2 }, { 5, 22,
			9 }, { 3, 25, 1 } };

	private static int value(final int x, final int y, final int z) {
		final int n = ((x * 73856093) ^ (y * 19349663) ^ (z * 83492791)) & 1023;
		double v = 20 + 10 * n / 1023.0;
		for (final double[] b : BLOBS) {
			final double dx = (x - b[0]) / b[4], dy = (y - b[1]) / b[5], dz = (z -
				b[2]) / b[6];
			v += b[3] * Math.exp(-(dx * dx + dy * dy + dz * dz) / 2);
		}
		return (int) Math.round(v);
	}

	private static Img<UnsignedShortType> image() {
		final Img<UnsignedShortType> img = ArrayImgs.unsignedShorts(W, H, D);
		final Cursor<UnsignedShortType> c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set(value(c.getIntPosition(0), c.getIntPosition(1), c
				.getIntPosition(2)));
		}
		return img;
	}

	private static List<Point> points(final int[]... coords) {
		final List<Point> points = new ArrayList<>();
		for (final int[] p : coords)
			points.add(new Point(p));
		return points;
	}

	private ImgLabeling<Integer, IntType> spots(
		final RandomAccessibleInterval<?> img, final List<Point> seeds,
		final String method, final Double value, final double[] radii,
		final String growth, final Integer minVolume, final Integer maxVolume)
	{
		@SuppressWarnings("unchecked")
		final ImgLabeling<Integer, IntType> result = ops.op(
			"segment.spotsFromSeeds").input(img, seeds, method, value, radii, growth,
				minVolume, maxVolume).outType(ImgLabeling.class).apply();
		return result;
	}

	/** Encodes a labeling like the harness: value x run length, flat order. */
	private static String rle(final ImgLabeling<Integer, IntType> labeling) {
		final StringBuilder sb = new StringBuilder("labels");
		int prev = -1, run = 0;
		for (final LabelingType<Integer> l : Views.flatIterable(labeling)) {
			final int v = l.isEmpty() ? 0 : l.iterator().next();
			if (v == prev) run++;
			else {
				if (run > 0) sb.append(' ').append(prev).append('x').append(run);
				prev = v;
				run = 1;
			}
		}
		sb.append(' ').append(prev).append('x').append(run);
		return sb.toString();
	}

	private static Map<Integer, Integer> sizes(
		final ImgLabeling<Integer, IntType> labeling)
	{
		final Map<Integer, Integer> sizes = new HashMap<>();
		for (final LabelingType<Integer> l : Views.flatIterable(labeling)) {
			for (final Integer label : l)
				sizes.merge(label, 1, Integer::sum);
		}
		return sizes;
	}

	/**
	 * Compares with mcib3d {@code Segment3DSpots} (mcib3d-core 3.96.31), run on
	 * the same synthetic 16-bit image and seeds by the harness
	 * {@code SpotsHarness.java} (kept with the G4 handoff notes of the
	 * fiji-plugin-ops work), for every growth rule and threshold method. Each
	 * case lists the mcib3d segmentation method (1 classical, 3 maximum, 4
	 * block), local threshold method (1 constant 60, 2 mean with radii 2, 4, 6
	 * and weight 0.5, 3 gaussian with radius 10 and factor 1.285, 4 diff 50),
	 * minimum and maximum (0: none) volume, then the local thresholds and the
	 * run-length encoded label image.
	 */
	@Test
	public void testRegression() throws IOException {
		final Img<UnsignedShortType> img = image();
		// Note: mcib3d processes the seeds in the raster order of its seed image.
		final List<Point> seeds = points(SEEDS);
		seeds.sort((p, q) -> {
			for (int d = 2; d >= 0; d--) {
				final int cmp = Long.compare(p.getLongPosition(d), q.getLongPosition(
					d));
				if (cmp != 0) return cmp;
			}
			return 0;
		});
		int cases = 0;
		try (BufferedReader r = new BufferedReader(new InputStreamReader(
			getClass().getResourceAsStream("spots-expected.txt"),
			StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = r.readLine()) != null) {
				final String[] c = line.split(" ");
				r.readLine(); // thresholds
				final String expected = r.readLine();
				final String growth = c[1].equals("1") ? "classical" : c[1].equals(
					"3") ? "maximum" : "block";
				final String method;
				final Double value;
				final double[] radii;
				switch (c[2]) {
					case "1":
						method = "constant";
						value = 60.0;
						radii = null;
						break;
					case "2":
						method = "mean";
						value = 0.5;
						radii = new double[] { 2, 4, 6 };
						break;
					case "3":
						method = "gaussian";
						value = 1.285;
						radii = new double[] { 10 };
						break;
					default:
						method = "diff";
						value = 50.0;
						radii = null;
				}
				final int maxVolume = Integer.parseInt(c[4]);
				final ImgLabeling<Integer, IntType> result = spots(img, seeds, method,
					value, radii, growth, Integer.parseInt(c[3]), maxVolume == 0 ? null
						: maxVolume);
				assertEquals(expected, rle(result), line);
				cases++;
			}
		}
		assertEquals(15, cases);
	}

	/** Two separated squares give two spots of exactly their size. */
	@Test
	public void testSquares() {
		final Img<FloatType> img = ArrayImgs.floats(20, 12);
		fill(img, 2, 2, 5, 5, 100);
		fill(img, 10, 3, 6, 4, 80);
		final ImgLabeling<Integer, IntType> result = spots(img, points(new int[] {
			4, 4 }, new int[] { 12, 5 }, new int[] { 0, 11 }), "constant", 50.0, null,
			null, null, null);
		final Map<Integer, Integer> sizes = sizes(result);
		assertEquals(Map.of(1, 25, 2, 24), sizes);

		// The minimum volume drops the second square; the labels stay contiguous.
		final ImgLabeling<Integer, IntType> big = spots(img, points(new int[] { 12,
			5 }, new int[] { 4, 4 }), "constant", 50.0, null, null, 25, null);
		assertEquals(Map.of(1, 25), sizes(big));

		// The diff method thresholds at 100 - 30 = 70, keeping both squares
		// apart from the background.
		final ImgLabeling<Integer, IntType> diff = spots(img, points(new int[] { 4,
			4 }), "diff", 30.0, null, null, null, null);
		assertEquals(Map.of(1, 25), sizes(diff));
	}

	/**
	 * Two touching peaks: the classical growth merges them into the spot of the
	 * first seed, the block growth stops at the valley between them.
	 */
	@Test
	public void testTouchingPeaks() {
		final Img<FloatType> img = ArrayImgs.floats(24, 9);
		final Cursor<FloatType> c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final double x = c.getDoublePosition(0), y = c.getDoublePosition(1);
			final double g1 = Math.exp(-((x - 7) * (x - 7) + (y - 4) * (y - 4)) / 8);
			final double g2 = Math.exp(-((x - 15) * (x - 15) + (y - 4) * (y - 4)) /
				8);
			c.get().setReal(100 * Math.max(g1, g2));
		}
		final List<Point> seeds = points(new int[] { 7, 4 }, new int[] { 15, 4 });
		final ImgLabeling<Integer, IntType> classical = spots(img, seeds,
			"constant", 5.0, null, "classical", null, null);
		assertEquals(1, sizes(classical).size());
		for (final String growth : Arrays.asList("block", "maximum")) {
			final ImgLabeling<Integer, IntType> split = spots(img, seeds, "constant",
				5.0, null, growth, null, null);
			final Map<Integer, Integer> sizes = sizes(split);
			assertEquals(2, sizes.size(), growth);
			// Each spot stays on its side of the valley at x = 11.
			for (final Cursor<LabelingType<Integer>> lc = split.localizingCursor(); lc
				.hasNext();)
			{
				final LabelingType<Integer> l = lc.next();
				if (l.contains(1)) assertTrue(lc.getIntPosition(0) <= 11, growth);
				if (l.contains(2)) assertTrue(lc.getIntPosition(0) >= 11, growth);
			}
		}
	}

	/** A maximum volume discards spots growing too large. */
	@Test
	public void testMaxVolume() {
		final Img<FloatType> img = ArrayImgs.floats(20, 12);
		fill(img, 2, 2, 5, 5, 100);
		fill(img, 10, 3, 2, 2, 80);
		final ImgLabeling<Integer, IntType> result = spots(img, points(new int[] {
			4, 4 }, new int[] { 10, 3 }), "constant", 50.0, null, null, null, 10);
		assertEquals(Map.of(1, 4), sizes(result));
	}

	@Test
	public void testInvalidArguments() {
		final Img<FloatType> img = ArrayImgs.floats(5, 5);
		final List<Point> seeds = points(new int[] { 2, 2 });
		assertThrows(IllegalArgumentException.class, () -> spots(img, seeds,
			"constant", null, null, null, null, null));
		assertThrows(IllegalArgumentException.class, () -> spots(img, seeds,
			"otsu", 1.0, null, null, null, null));
		assertThrows(IllegalArgumentException.class, () -> spots(img, points(
			new int[] { 7, 2 }), "constant", 1.0, null, null, null, null));
	}

	private static void fill(final Img<FloatType> img, final int x0,
		final int y0, final int w, final int h, final float v)
	{
		for (int y = y0; y < y0 + h; y++)
			for (int x = x0; x < x0 + w; x++)
				img.getAt(x, y).set(v);
	}
}
