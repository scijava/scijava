/*-
 * #%L
 * LGPL-licensed image processing operations for SciJava Ops, translated from LGPL ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.lgpl.segment.watershed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.scijava.ops.image.lgpl.segment.watershed.ExtendedMinimaWatershedTest.labels;

import java.util.Set;

import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.lgpl.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link DistanceTransformWatershed}.
 *
 * @author Curtis Rueden
 */
public class DistanceTransformWatershedTest extends AbstractOpTest {

	private static final Nil<ImgLabeling<Integer, IntType>> LABELING =
		new Nil<>()
		{};

	/**
	 * Two disks of radius 8 whose centers are 12 pixels apart: the distance
	 * transform is about 8 at their centers and about 5.3 at the neck.
	 */
	private static Img<BitType> twoDisks() {
		final var img = ArrayImgs.bits(40, 22);
		final var ra = img.randomAccess();
		for (var y = 0; y < 22; y++) {
			for (var x = 0; x < 40; x++) {
				final var in = Math.hypot(x - 14, y - 11) <= 8 || Math.hypot(x - 26,
					y - 11) <= 8;
				ra.setPositionAndGet(x, y).set(in);
			}
		}
		return img;
	}

	@Test
	public void testSplitsTouchingDisks() {
		final var img = twoDisks();
		final var out = ops.op("segment.distanceTransformWatershed").input(img)
			.outType(LABELING).apply();
		assertEquals(2, labels(out).size());
		final var ra = out.randomAccess();
		final var raIn = img.randomAccess();
		var lines = 0;
		for (var y = 0; y < 22; y++) {
			for (var x = 0; x < 40; x++) {
				final var l = ra.setPositionAndGet(x, y);
				if (!raIn.setPositionAndGet(x, y).get()) {
					assertTrue(l.isEmpty());
					continue;
				}
				if (l.isEmpty()) {
					lines++;
					assertEquals(20, x, "line pixel at (" + x + ", " + y + ")");
				}
				else assertEquals(x < 20 ? 1 : 2, l.iterator().next());
			}
		}
		assertTrue(lines > 0);
	}

	@Test
	public void testWatershedLabel() {
		final var img = twoDisks();
		final var lines = ops.op("segment.distanceTransformWatershed").input(img)
			.outType(LABELING).apply();
		final var labeled = ops.op("segment.distanceTransformWatershed").input(img,
			1.0, null, true, null, -1).outType(LABELING).apply();
		final var ra = lines.randomAccess();
		final var raIn = img.randomAccess();
		var lineCount = 0;
		for (final var c = labeled.localizingCursor(); c.hasNext();) {
			final var l = c.next();
			final var expected = ra.setPositionAndGet(c);
			if (!raIn.setPositionAndGet(c).get()) {
				// Note: The background is not part of the watershed lines.
				assertTrue(l.isEmpty());
			}
			else if (expected.isEmpty()) {
				assertEquals(Set.of(-1), l);
				lineCount++;
			}
			else assertEquals(expected, l);
		}
		assertTrue(lineCount > 0);

		// Without watershed lines, the watershed label is not used.
		final var noLines = ops.op("segment.distanceTransformWatershed").input(img,
			1.0, null, false, null, -1).outType(LABELING).apply();
		assertEquals(Set.of(1, 2), labels(noLines));
	}

	@Test
	public void testDynamicControlsSplitting() {
		final var img = twoDisks();
		final var split = ops.op("segment.distanceTransformWatershed").input(img,
			2.0, new DiamondShape(1), false).outType(LABELING).apply();
		assertEquals(2, labels(split).size());
		// Without lines, every foreground pixel is labeled.
		final var ra = split.randomAccess();
		for (final var c = img.localizingCursor(); c.hasNext();) {
			final var fg = c.next().get();
			assertEquals(fg ? 1 : 0, ra.setPositionAndGet(c).size());
		}
		final var merged = ops.op("segment.distanceTransformWatershed").input(img,
			4.0).outType(LABELING).apply();
		assertEquals(1, labels(merged).size());
	}

	@Test
	public void testCalibration() {
		final var img = twoDisks();
		// Unit spacing gives the same result as no calibration.
		final var plain = ops.op("segment.distanceTransformWatershed").input(img,
			1.0, null, true).outType(LABELING).apply();
		final var unit = ops.op("segment.distanceTransformWatershed").input(img,
			1.0, null, true, new double[] { 1, 1 }).outType(LABELING).apply();
		final var ra = unit.randomAccess();
		for (final var c = plain.localizingCursor(); c.hasNext();) {
			assertEquals(c.next(), ra.setPositionAndGet(c));
		}
		// Doubling the spacing doubles the distances, and thus the neck depth.
		final var scaled = ops.op("segment.distanceTransformWatershed").input(img,
			4.0, null, true, new double[] { 2, 2 }).outType(LABELING).apply();
		assertEquals(2, labels(scaled).size());
	}

	@Test
	public void test3D() {
		final var img = ArrayImgs.bits(30, 16, 16);
		final var ra = img.randomAccess();
		for (final var c = img.localizingCursor(); c.hasNext();) {
			c.fwd();
			final double x = c.getIntPosition(0), y = c.getIntPosition(1), z = c
				.getIntPosition(2);
			final var r1 = Math.sqrt(sq(x - 10) + sq(y - 8) + sq(z - 8));
			final var r2 = Math.sqrt(sq(x - 19) + sq(y - 8) + sq(z - 8));
			ra.setPositionAndGet(c).set(r1 <= 6 || r2 <= 6);
		}
		final var out = ops.op("segment.distanceTransformWatershed").input(img)
			.outType(LABELING).apply();
		assertEquals(2, labels(out).size());
	}

	private static double sq(final double v) {
		return v * v;
	}
}
