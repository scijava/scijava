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

package org.scijava.ops.image.gpl.ridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.gpl.AbstractOpTest;

/**
 * Tests {@link DefaultDetectLines} and {@link EstimateLineParameters}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class DetectLinesTest extends AbstractOpTest {

	private static Img<FloatType> image(final int kind) {
		return ArrayImgs.floats(RidgeTestImages.image(kind), RidgeTestImages.W,
			RidgeTestImages.H);
	}

	private static DetectedLines detect(final RandomAccessibleInterval<FloatType> img,
		final RidgeTestImages.Case c)
	{
		return ops.op("segment.detectLines").input(img, c.sigma, c.low, c.high,
			c.dark, c.width, c.correct, c.extend, c.minLength, c.maxLength,
			c.slope ? OverlapResolution.SLOPE : OverlapResolution.NONE).outType(
				DetectedLines.class).apply();
	}

	private static DetectedLines detect(final RidgeTestImages.Case c) {
		return detect(image(c.image), c);
	}

	private static double first(final DetectedLine l, final int d) {
		return Math.min(l.vertex(0).getDoublePosition(d), l.vertex(l.numVertices() -
			1).getDoublePosition(d));
	}

	/** Must match the line order used by the harness for overlap resolution. */
	private static final Comparator<DetectedLine> LINE_ORDER = Comparator
		.<DetectedLine> comparingInt(DetectedLine::numVertices)
		.thenComparingDouble(l -> first(l, 0)).thenComparingDouble(l -> first(l,
			1));

	/**
	 * Compares the op with the original plugin on all test cases.
	 * <p>
	 * The expected values in {@code ridge-expected.txt} were produced by running
	 * the {@code LineDetector} of ij-ridgedetection 1.4.2-SNAPSHOT (compiled from
	 * source, with ImageJ 1.54p) on the images of {@link RidgeTestImages} with
	 * the parameters of {@link RidgeTestImages#CASES}, resetting the line ID
	 * counter before each case. With overlap resolution, whose output order
	 * depends on identity hash codes in the plugin, lines are sorted by number
	 * of points and then by start coordinates on both sides, and merged lines
	 * may be reversed.
	 * </p>
	 */
	@Test
	public void testRegression() throws IOException {
		final List<String> expected = readExpected();
		int k = 0;
		for (int c = 0; c < RidgeTestImages.CASES.length; c++) {
			final RidgeTestImages.Case cs = RidgeTestImages.CASES[c];
			final DetectedLines result = detect(cs);
			final List<DetectedLine> lines = new ArrayList<>(result.lines());
			if (cs.slope) lines.sort(LINE_ORDER);
			final String[] header = expected.get(k++).split(" ");
			assertEquals("case " + c, header[0] + " " + header[1]);
			assertEquals(Integer.parseInt(header[2]), lines.size(), "case " + c);
			assertEquals(Integer.parseInt(header[3]), result.junctions().size(),
				"case " + c);
			for (final DetectedLine l : lines) {
				final String[] lh = expected.get(k++).split(" ");
				assertEquals(ContourClass.values()[Integer.parseInt(lh[1])], l
					.contourClass(), "case " + c);
				assertEquals(Integer.parseInt(lh[2]), l.numVertices(), "case " + c);
				final List<double[]> points = new ArrayList<>();
				for (int i = 0; i < l.numVertices(); i++)
					points.add(parse(expected.get(k++)));
				// Note: with overlap resolution, the direction of a merged line
				// depends on the identity hash order of the plugin.
				final boolean reversed = cs.slope && Math.abs(points.get(0)[0] - l
					.vertex(0).getDoublePosition(0)) + Math.abs(points.get(0)[1] - l
						.vertex(0).getDoublePosition(1)) > 1e-3;
				for (int i = 0; i < l.numVertices(); i++) {
					final double[] e = points.get(reversed ? l.numVertices() - 1 - i
						: i);
					final String msg = "case " + c + " point " + i;
					assertEquals(e[0], l.vertex(i).getDoublePosition(0), 1e-4, msg);
					assertEquals(e[1], l.vertex(i).getDoublePosition(1), 1e-4, msg);
					assertEquals(e[2], l.angle(i), 1e-4, msg);
					assertEquals(e[3], l.response(i), 1e-3, msg);
					if (cs.width || cs.correct) {
						assertEquals(e[4], l.widthLeft(i), 1e-4, msg);
						assertEquals(e[5], l.widthRight(i), 1e-4, msg);
					}
					else assertFalse(l.hasWidth());
					if (cs.correct) {
						assertEquals(e[6], l.asymmetry(i), 1e-4, msg);
						assertEquals(e[7], l.intensity(i), 1e-2, msg);
					}
					else assertFalse(l.hasCorrection());
				}
			}
			for (final LineJunction j : result.junctions()) {
				final String[] e = expected.get(k++).split(" ");
				final String msg = "case " + c + " junction";
				assertEquals("junction", e[0]);
				assertEquals(Double.parseDouble(e[1]), j.x(), 1e-4, msg);
				assertEquals(Double.parseDouble(e[2]), j.y(), 1e-4, msg);
				final int e1 = Integer.parseInt(e[3]), e2 = Integer.parseInt(e[4]);
				final int a1 = lines.indexOf(j.line1()), a2 = lines.indexOf(j.line2());
				if (cs.slope) {
					// Note: the order of the two lines of a junction between merged
					// lines (and so its position) depends on the plugin hash order.
					assertEquals(Math.min(e1, e2), Math.min(a1, a2), msg);
					assertEquals(Math.max(e1, e2), Math.max(a1, a2), msg);
				}
				else {
					assertEquals(e1, a1, msg);
					assertEquals(e2, a2, msg);
					assertEquals(Integer.parseInt(e[5]), j.position(), msg);
				}
				assertEquals(Boolean.parseBoolean(e[6]), j.isNonTerminal(), msg);
			}
		}
		assertEquals(expected.size(), k);
	}

	/** A Gaussian line profile is located at its center. */
	@Test
	public void testGaussianLinePosition() {
		final DetectedLines result = detect(RidgeTestImages.CASES[0]);
		final DetectedLine main = longest(result.lines());
		assertTrue(main.length() > 60, "length " + main.length());
		final double t = 2 * Math.PI / 40;
		for (int i = 0; i < main.numVertices(); i++) {
			final double x = main.vertex(i).getDoublePosition(0);
			final double y = main.vertex(i).getDoublePosition(1);
			if (x < 4 || x > RidgeTestImages.W - 5) continue;
			assertEquals(24 + 6 * Math.sin(t * x), y, 0.25, "x = " + x);
			assertTrue(main.response(i) > 8);
		}
	}

	/**
	 * Dark lines are detected as the bright lines of the inverted image, with
	 * the opposite contrast.
	 */
	@Test
	public void testDarkLineInvariance() {
		final RidgeTestImages.Case c = RidgeTestImages.CASES[2];
		final Img<FloatType> dark = image(c.image);
		final Img<FloatType> bright = dark.copy();
		bright.forEach(v -> v.set(255 - v.get()));
		final DetectedLines d = detect(dark, c);
		final DetectedLines b = ops.op("segment.detectLines").input(bright, c.sigma,
			c.low, c.high, false, true, true).outType(DetectedLines.class).apply();
		assertEquals(d.lines().size(), b.lines().size());
		for (int n = 0; n < d.lines().size(); n++) {
			final DetectedLine ld = d.lines().get(n), lb = b.lines().get(n);
			assertEquals(ld.numVertices(), lb.numVertices());
			for (int i = 0; i < ld.numVertices(); i++) {
				assertEquals(ld.vertex(i).getDoublePosition(0), lb.vertex(i)
					.getDoublePosition(0), 1e-3);
				assertEquals(ld.vertex(i).getDoublePosition(1), lb.vertex(i)
					.getDoublePosition(1), 1e-3);
				assertEquals(ld.widthLeft(i), lb.widthLeft(i), 1e-3);
				assertEquals(ld.intensity(i), -lb.intensity(i), 1e-2);
			}
		}
	}

	/** The width and position of a dark bar of width 5 at 30 degrees. */
	@Test
	public void testDarkBarWidth() {
		final DetectedLines result = detect(RidgeTestImages.CASES[2]);
		final DetectedLine line = longest(result.lines());
		assertTrue(line.length() > 50, "length " + line.length());
		final double a = Math.toRadians(30);
		for (int i = 2; i < line.numVertices() - 2; i++) {
			final double x = line.vertex(i).getDoublePosition(0);
			final double y = line.vertex(i).getDoublePosition(1);
			assertEquals(0, -(x - 32) * Math.sin(a) + (y - 24) * Math.cos(a), 0.15);
			// The width search line is mirrored at the image borders.
			if (x < 6 || y < 6 || x > RidgeTestImages.W - 7 || y > RidgeTestImages.H -
				7) continue;
			assertEquals(5, line.widthLeft(i) + line.widthRight(i), 0.5);
			assertEquals(-140, line.intensity(i), 10);
		}
	}

	/**
	 * A bar of width 5 between backgrounds of different brightness: the
	 * uncorrected position and width are biased, the corrected ones are not
	 * (Steger 1998), and the asymmetry is recovered.
	 */
	@Test
	public void testAsymmetricBiasCorrection() {
		final DetectedLine biased = longest(detect(RidgeTestImages.CASES[3])
			.lines());
		final DetectedLine corrected = longest(detect(RidgeTestImages.CASES[4])
			.lines());
		final double biasedX = meanX(biased), correctedX = meanX(corrected);
		assertTrue(Math.abs(biasedX - 32) > 0.3, "biased x " + biasedX);
		assertEquals(32, correctedX, 0.1);
		final double asymmetry = (110.0 - 40) / (200 - 40);
		for (int i = 2; i < corrected.numVertices() - 2; i++) {
			assertEquals(5, corrected.widthLeft(i) + corrected.widthRight(i), 0.4);
			assertEquals(asymmetry, corrected.asymmetry(i), 0.05);
			assertEquals(160, corrected.intensity(i), 12);
		}
		// The bias makes the uncorrected widths asymmetric.
		final int mid = biased.numVertices() / 2;
		assertTrue(Math.abs(biased.widthLeft(mid) - biased.widthRight(mid)) > 0.4);
	}

	/** Crossing lines are split at the crossing, or merged straight across. */
	@Test
	public void testCrossingLines() {
		final DetectedLines split = detect(RidgeTestImages.CASES[5]);
		assertEquals(4, split.lines().size());
		assertFalse(split.junctions().isEmpty());
		for (final LineJunction j : split.junctions()) {
			assertEquals(32, j.x(), 0.5);
			assertEquals(24, j.y(), 0.5);
		}
		for (final DetectedLine l : split.lines()) {
			assertTrue(l.contourClass() == ContourClass.START_JUNCTION || l
				.contourClass() == ContourClass.END_JUNCTION);
		}

		final DetectedLines merged = detect(RidgeTestImages.CASES[8]);
		assertEquals(2, merged.lines().size());
		assertEquals(1, merged.junctions().size());
		assertTrue(merged.junctions().get(0).isNonTerminal());
		final double[] angles = { 20, 110 };
		for (final DetectedLine l : merged.lines()) {
			// Each merged line is straight through the crossing.
			final double dx = l.vertex(l.numVertices() - 1).getDoublePosition(0) - l
				.vertex(0).getDoublePosition(0);
			final double dy = l.vertex(l.numVertices() - 1).getDoublePosition(1) - l
				.vertex(0).getDoublePosition(1);
			final double angle = Math.toDegrees(Math.atan2(dy, dx));
			final double a = ((angle % 180) + 180) % 180;
			final double err = Math.min(Math.abs(a - angles[0]), Math.abs(a -
				angles[1]));
			assertTrue(err < 3, "angle " + a);
			assertEquals(l.length(), Math.hypot(dx, dy), 0.05 * l.length());
		}
	}

	/** Lines are pruned by length. */
	@Test
	public void testLengthPruning() {
		final DetectedLines all = detect(RidgeTestImages.CASES[9]);
		final DetectedLines pruned = detect(RidgeTestImages.CASES[10]);
		assertTrue(pruned.lines().size() < all.lines().size());
		for (final DetectedLine l : pruned.lines()) {
			assertTrue(l.length() >= 10 && l.length() <= 40, "length " + l.length());
		}
		boolean ring = false;
		for (final DetectedLine l : all.lines())
			ring |= l.length() > 60;
		assertTrue(ring);
	}

	/** Lines of a translated image are translated. */
	@Test
	public void testTranslatedInput() {
		final RidgeTestImages.Case c = RidgeTestImages.CASES[5];
		final DetectedLines ref = detect(c);
		final DetectedLines moved = detect(Views.translate(image(c.image), 10,
			-5), c);
		assertEquals(ref.lines().size(), moved.lines().size());
		for (int n = 0; n < ref.lines().size(); n++) {
			final DetectedLine a = ref.lines().get(n), b = moved.lines().get(n);
			for (int i = 0; i < a.numVertices(); i++) {
				assertEquals(a.vertex(i).getDoublePosition(0) + 10, b.vertex(i)
					.getDoublePosition(0), 1e-4);
				assertEquals(a.vertex(i).getDoublePosition(1) - 5, b.vertex(i)
					.getDoublePosition(1), 1e-4);
			}
		}
		final LineJunction j = moved.junctions().get(0);
		assertEquals(ref.junctions().get(0).x() + 10, j.x(), 1e-4);
		assertEquals(ref.junctions().get(0).y() - 5, j.y(), 1e-4);
	}

	@Test
	public void testInvalidInput() {
		final Img<FloatType> img3d = ArrayImgs.floats(16, 16, 3);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"segment.detectLines").input(img3d, 1.5, 3.0, 8.0).outType(
				DetectedLines.class).apply());
		final Img<FloatType> img = image(RidgeTestImages.GAUSS);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"segment.detectLines").input(img, 0.3, 3.0, 8.0).outType(
				DetectedLines.class).apply());
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"segment.detectLines").input(img, 20.0, 3.0, 8.0).outType(
				DetectedLines.class).apply());
	}

	/** The plugin dialog defaults are consistent with its estimation. */
	@Test
	public void testEstimateLineParameters() {
		final double[] p = ops.op("segment.estimateLineParameters").input(3.5,
			87.0, 230.0).outType(double[].class).apply();
		// Plugin defaults: line width 3.5, contrasts 87 and 230 give sigma 1.51,
		// lower threshold 3.06 and upper threshold 7.99.
		assertEquals(1.51, p[0], 0.005);
		assertEquals(3.06, p[1], 0.005);
		assertEquals(7.99, p[2], 0.005);
		final double[] d = ops.op("segment.estimateLineParameters").input(3.5,
			25.0, 168.0, true).outType(double[].class).apply();
		// For dark lines, the contrasts are relative to 255: 255 - 168 = 87.
		assertEquals(p[0], d[0], 1e-12);
		assertEquals(p[1], d[1], 1e-12);
		assertEquals(p[2], d[2], 1e-12);
	}

	private static DetectedLine longest(final List<DetectedLine> lines) {
		DetectedLine best = null;
		for (final DetectedLine l : lines)
			if (best == null || l.length() > best.length()) best = l;
		return best;
	}

	private static double meanX(final DetectedLine l) {
		double sum = 0;
		int n = 0;
		for (int i = 2; i < l.numVertices() - 2; i++) {
			sum += l.vertex(i).getDoublePosition(0);
			n++;
		}
		return sum / n;
	}

	private static double[] parse(final String line) {
		final String[] t = line.split(" ");
		final double[] v = new double[t.length];
		for (int i = 0; i < t.length; i++)
			v[i] = Double.parseDouble(t[i]);
		return v;
	}

	private static List<String> readExpected() throws IOException {
		final List<String> lines = new ArrayList<>();
		try (final BufferedReader r = new BufferedReader(new InputStreamReader(
			DetectLinesTest.class.getResourceAsStream("ridge-expected.txt"),
			StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = r.readLine()) != null)
				if (!line.isEmpty()) lines.add(line);
		}
		return lines;
	}
}
