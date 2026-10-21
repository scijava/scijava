/* #%L
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

package org.scijava.ops.image.segment.watershed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.scijava.ops.image.segment.watershed.ExtendedMinimaWatershedTest.assertBoundaryDifferences;
import static org.scijava.ops.image.segment.watershed.ExtendedMinimaWatershedTest.assertFewDifferences;
import static org.scijava.ops.image.segment.watershed.ExtendedMinimaWatershedTest.labels;
import static org.scijava.ops.image.segment.watershed.ExtendedMinimaWatershedTest.toArray;

import java.util.Set;

import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;
import org.scijava.util.MersenneTwisterFast;

/**
 * Tests {@link CompactWatershed}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class CompactWatershedTest extends AbstractOpTest {

	private static final Nil<ImgLabeling<Integer, IntType>> LABELING =
		new Nil<>()
		{};

	private static final int W = 30;
	private static final int H = 20;
	private static final int[][] SEEDS = { { 4, 4 }, { 22, 6 }, { 12, 15 } };

	private static ImgLabeling<Integer, IntType> seeds(final long w,
		final long h, final int[][] points, final int... labels)
	{
		final var seeds = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(w, h));
		final var ra = seeds.randomAccess();
		for (var k = 0; k < points.length; k++) {
			ra.setPositionAndGet(points[k]).add(labels[k]);
		}
		return seeds;
	}

	private static Img<DoubleType> random() {
		final var img = ArrayImgs.doubles(W, H);
		final var random = new MersenneTwisterFast(0x1234);
		for (final var t : img)
			t.set(random.nextDouble());
		return img;
	}

	@Test
	public void testLabelsArePreserved() {
		final var out = ops.op("segment.compactWatershed").input(random(), seeds(W,
			H, SEEDS, 7, 42, 3), 0.1).outType(LABELING).apply();
		assertEquals(Set.of(7, 42, 3), labels(out));
		final var ra = out.randomAccess();
		assertEquals(Set.of(42), ra.setPositionAndGet(SEEDS[1]));
	}

	@Test
	public void testWatershedLabel() {
		final var img = random();
		final var markers = seeds(W, H, SEEDS, 7, 42, 3);
		final var all = ArrayImgs.bits(W, H);
		for (final var t : all)
			t.set(true);
		final var lines = ops.op("segment.compactWatershed").input(img, markers,
			0.1).outType(LABELING).apply();
		final var labeled = ops.op("segment.compactWatershed").input(img, markers,
			0.1, null, true, all, 0).outType(LABELING).apply();
		final var ra = lines.randomAccess();
		var lineCount = 0;
		for (final var c = labeled.localizingCursor(); c.hasNext();) {
			final var l = c.next();
			final var expected = ra.setPositionAndGet(c);
			assertEquals(1, l.size());
			if (expected.isEmpty()) {
				assertEquals(Set.of(0), l);
				lineCount++;
			}
			else assertEquals(expected, l);
		}
		assertTrue(lineCount > 0);

		// Without watershed lines, the watershed label is not used.
		final var noLines = ops.op("segment.compactWatershed").input(img, markers,
			0.1, null, false, all, 0).outType(LABELING).apply();
		assertEquals(Set.of(7, 42, 3), labels(noLines));

		// A watershed label which is also a region label is rejected.
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"segment.compactWatershed").input(img, markers, 0.1, null, true, all, 42)
			.outType(LABELING).apply());
	}

	/** Without compactness, the result is the classical seeded watershed. */
	@Test
	public void testZeroCompactnessMatchesSeededWatershed() {
		final var img = random();
		final var markers = seeds(W, H, SEEDS, 1, 2, 3);
		final var expected = ops.op("image.watershed").input(img, markers, true,
			false).outType(LABELING).apply();
		final var actual = ops.op("segment.compactWatershed").input(img, markers,
			0.0, new RectangleShape(1, false), false).outType(LABELING).apply();
		assertBoundaryDifferences(toArray(expected), toArray(actual), W);
	}

	/** With a huge compactness, the result is the Voronoi partition. */
	@Test
	public void testHighCompactnessGivesVoronoi() {
		final var out = ops.op("segment.compactWatershed").input(random(), seeds(W,
			H, SEEDS, 1, 2, 3), 1e6, null, false).outType(LABELING).apply();
		final var actual = toArray(out);
		var checked = 0;
		for (var y = 0; y < H; y++) {
			for (var x = 0; x < W; x++) {
				final var d = new double[SEEDS.length];
				var nearest = 0;
				for (var k = 0; k < SEEDS.length; k++) {
					d[k] = Math.hypot(x - SEEDS[k][0], y - SEEDS[k][1]);
					if (d[k] < d[nearest]) nearest = k;
				}
				var margin = Double.POSITIVE_INFINITY;
				for (var k = 0; k < SEEDS.length; k++)
					if (k != nearest) margin = Math.min(margin, d[k] - d[nearest]);
				// Note: Discrete paths make distances near the bisectors ambiguous.
				if (margin < 2) continue;
				assertEquals(nearest + 1, actual[y * W + x], "(" + x + ", " + y + ")");
				checked++;
			}
		}
		assertTrue(checked > W * H / 2);
	}

	/**
	 * Compactness keeps a region from leaking along a long valley into the
	 * surroundings of another marker.
	 */
	@Test
	public void testCompactnessStopsLeaks() {
		// A flat image with an L-shaped valley from (5, 10) up to row 3 and then
		// along it, passing over the second marker at (25, 10).
		final var img = ArrayImgs.doubles(31, 21);
		final var ra = img.randomAccess();
		for (final var c = img.localizingCursor(); c.hasNext();) {
			c.fwd();
			final var x = c.getIntPosition(0);
			final var y = c.getIntPosition(1);
			final var valley = y == 3 || (x == 5 && y >= 3 && y <= 10);
			ra.setPositionAndGet(c).set(valley ? 0 : 1);
		}
		final var markers = seeds(31, 21, new int[][] { { 5, 10 }, { 25, 10 } },
			1, 2);
		final var classic = ops.op("segment.compactWatershed").input(img, markers,
			0.0, null, false).outType(LABELING).apply();
		assertEquals(Set.of(1), classic.randomAccess().setPositionAndGet(25, 3));
		assertEquals(Set.of(1), classic.randomAccess().setPositionAndGet(25, 4));
		final var compact = ops.op("segment.compactWatershed").input(img, markers,
			1.0, null, false).outType(LABELING).apply();
		assertEquals(Set.of(2), compact.randomAccess().setPositionAndGet(25, 3));
		assertEquals(Set.of(2), compact.randomAccess().setPositionAndGet(25, 4));
		assertEquals(Set.of(1), compact.randomAccess().setPositionAndGet(5, 3));
	}

	/**
	 * Compares with MorphoLibJ 1.6.4, run as a black box
	 * ({@code Watershed.computeWatershed} with compactness 0, full connectivity
	 * and no dams) on the image {@code ExtendedMinimaWatershedTest.F} with
	 * three single-pixel markers. Pixels of equal priority are flooded in a
	 * different order (see {@code ExtendedMinimaWatershedTest}).
	 */
	@Test
	public void testMorphoLibJ() {
		final var img = ArrayImgs.unsignedBytes(20, 16);
		var i = 0;
		for (final var t : img)
			t.set(ExtendedMinimaWatershedTest.F[i++]);
		final var markers = seeds(20, 16, new int[][] { { 4, 4 }, { 14, 5 }, { 9,
			12 } }, 1, 2, 3);
		final var out = ops.op("segment.compactWatershed").input(img, markers, 0.0,
			null, false).outType(LABELING).apply();
		assertFewDifferences(COMPACT_0, toArray(out));
	}

	private static final int[] COMPACT_0 = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2,
		1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2,
		1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2,
		1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2,
	};
}
