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

package org.scijava.ops.image.lgpl.labeling.distance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.lgpl.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link LabelDistances}.
 * <p>
 * Expected values are computed by brute force over all pixel pairs, directly
 * from the definitions in the op documentation.
 * </p>
 *
 * @author Curtis Rueden
 */
public class LabelDistancesTest extends AbstractOpTest {

	private static final Nil<ImgLabeling<Integer, IntType>> LABELING =
		new Nil<>()
		{};

	private static final Nil<RandomAccessibleInterval<DoubleType>> DOUBLES =
		new Nil<>()
		{};

	@Test
	public void testDistanceMapTouchingSquares() {
		final var labeling = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(12,
			7));
		paint(labeling, 1, 1, 1, 5, 5);
		paint(labeling, 2, 6, 1, 10, 5);
		final RandomAccessibleInterval<DoubleType> d = ops.op(
			"labeling.distanceMap").input(labeling).outType(DOUBLES).apply();
		final var ra = d.randomAccess();
		// Touching labels are separated: the shared edge has distance 1.
		assertEquals(1, ra.setPositionAndGet(5, 3).get(), 0);
		assertEquals(1, ra.setPositionAndGet(6, 3).get(), 0);
		assertEquals(3, ra.setPositionAndGet(3, 3).get(), 0);
		assertEquals(2, ra.setPositionAndGet(2, 2).get(), 0);
		assertEquals(0, ra.setPositionAndGet(0, 0).get(), 0);
	}

	@Test
	public void testDistanceMapRandom() {
		for (final var spacing : new double[][] { null, { 1.5, 0.7, 2 } }) {
			final var labeling = randomLabeling(new long[] { 9, 8, 6 }, 4, 0.6, 7);
			final RandomAccessibleInterval<DoubleType> d = ops.op(
				"labeling.distanceMap").input(labeling, spacing).outType(DOUBLES)
				.apply();
			final var ids = ids(labeling);
			final var s = spacing == null ? new double[] { 1, 1, 1 } : spacing;
			final var c = Views.flatIterable(d).cursor();
			for (var i = 0; i < ids.length; i++) {
				final var actual = c.next().get();
				if (ids[i] == 0) {
					assertEquals(0, actual, 0);
					continue;
				}
				var min = Double.POSITIVE_INFINITY;
				for (var j = 0; j < ids.length; j++)
					if (ids[j] != ids[i]) min = Math.min(min, dist2(i, j, labeling, s));
				assertEquals(Math.sqrt(min), actual, 1e-9);
			}
		}
	}

	@Test
	public void testDilateSinglePixel() {
		final var labeling = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(9,
			9));
		paint(labeling, 1, 4, 4, 4, 4);
		final ImgLabeling<Integer, IntType> dilated = ops.op("labeling.dilate")
			.input(labeling, 2.5).outType(LABELING).apply();
		// Digital disk of radius 2.5: x^2 + y^2 <= 6.25.
		assertEquals(Map.of(1, 21L), sizes(dilated));
		final ImgLabeling<Integer, IntType> aniso = ops.op("labeling.dilate")
			.input(labeling, 2.0, new double[] { 1, 2 }).outType(LABELING).apply();
		// With spacing 2 along y, the disk spans 5 pixels in x and 3 in y.
		assertEquals(Map.of(1, 7L), sizes(aniso));
	}

	@Test
	public void testDilateRandom() {
		final var labeling = randomLabeling(new long[] { 15, 13 }, 3, 0.05, 3);
		final var s = new double[] { 1, 1.3 };
		final var r = 2.7;
		final ImgLabeling<Integer, IntType> dilated = ops.op("labeling.dilate")
			.input(labeling, r, s).outType(LABELING).apply();
		final var ids = ids(labeling);
		final var out = ids(dilated);
		for (var i = 0; i < ids.length; i++) {
			if (ids[i] != 0) {
				// Labels are never overwritten.
				assertEquals(ids[i], out[i]);
				continue;
			}
			var min = Double.POSITIVE_INFINITY;
			for (var j = 0; j < ids.length; j++)
				if (ids[j] != 0) min = Math.min(min, dist2(i, j, labeling, s));
			if (min > r * r) assertEquals(0, out[i]);
			else {
				// The pixel gets a label at the minimal distance.
				assertTrue(out[i] != 0);
				var nearest = Double.POSITIVE_INFINITY;
				for (var j = 0; j < ids.length; j++)
					if (ids[j] == out[i]) nearest = Math.min(nearest, dist2(i, j,
						labeling, s));
				assertEquals(min, nearest, 1e-9);
			}
		}
	}

	@Test
	public void testErode() {
		final var labeling = randomLabeling(new long[] { 14, 12 }, 3, 0.8, 11);
		final var s = new double[] { 1, 0.8 };
		final var r = 1.5;
		for (final var backgroundOnly : new boolean[] { false, true }) {
			final ImgLabeling<Integer, IntType> eroded = ops.op("labeling.erode")
				.input(labeling, r, s, backgroundOnly).outType(LABELING).apply();
			final var ids = ids(labeling);
			final var out = ids(eroded);
			for (var i = 0; i < ids.length; i++) {
				var keep = ids[i] != 0;
				for (var j = 0; keep && j < ids.length; j++) {
					final var outside = backgroundOnly ? ids[j] == 0 : ids[j] != ids[i];
					if (outside && dist2(i, j, labeling, s) <= r * r) keep = false;
				}
				assertEquals(keep ? ids[i] : 0, out[i]);
			}
		}
	}

	@Test
	public void testOpenClose() {
		final var labeling = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(16,
			9));
		// A square with a one pixel wide spur.
		paint(labeling, 1, 1, 1, 5, 5);
		paint(labeling, 1, 6, 3, 8, 3);
		// A square separated from it by a gap of one pixel.
		paint(labeling, 2, 10, 1, 14, 5);
		paint(labeling, 3, 1, 7, 1, 7);
		final ImgLabeling<Integer, IntType> opened = ops.op("labeling.open")
			.input(labeling, 1.5).outType(LABELING).apply();
		// The spur and the single pixel are removed, the squares are kept.
		assertEquals(Map.of(1, 25L, 2, 25L), sizes(opened));

		final var gap = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(15, 9));
		paint(gap, 1, 2, 2, 6, 6);
		paint(gap, 2, 8, 2, 12, 6);
		final ImgLabeling<Integer, IntType> closed = ops.op("labeling.close")
			.input(gap, 1.5).outType(LABELING).apply();
		// The gap column is filled, and the outer border stays unlabeled.
		final var ra = closed.randomAccess();
		for (var y = 2; y <= 6; y++)
			assertEquals(1, ra.setPositionAndGet(7, y).size());
		assertTrue(ra.setPositionAndGet(7, 1).isEmpty());
		assertTrue(ra.setPositionAndGet(1, 4).isEmpty());
		assertEquals(Set.of(1), ra.setPositionAndGet(6, 4));
		assertEquals(Set.of(2), ra.setPositionAndGet(8, 4));
		final var size = sizes(closed);
		assertEquals(55, size.get(1) + size.get(2));
	}

	@Test
	public void testInfluenceZones() {
		final var labeling = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(10,
			7));
		paint(labeling, 1, 2, 3, 2, 3);
		paint(labeling, 2, 6, 3, 6, 3);
		final ImgLabeling<Integer, IntType> zones = ops.op(
			"labeling.influenceZones").input(labeling).outType(LABELING).apply();
		final var ra = zones.randomAccess();
		for (var y = 0; y < 7; y++) {
			for (var x = 0; x < 10; x++) {
				final var labels = ra.setPositionAndGet(x, y);
				// The bisector x = 4 is a tie.
				if (x < 4) assertEquals(Set.of(1), labels);
				else if (x > 4) assertEquals(Set.of(2), labels);
				else assertEquals(1, labels.size());
			}
		}
		final var mask = ArrayImgs.bits(10, 7);
		Views.interval(mask, new long[] { 0, 2 }, new long[] { 9, 4 }).forEach(
			BitType::setOne);
		final ImgLabeling<Integer, IntType> masked = ops.op(
			"labeling.influenceZones").input(labeling, null, mask).outType(LABELING)
			.apply();
		final var total = sizes(masked).values().stream().mapToLong(v -> v)
			.sum();
		assertEquals(30, total);
	}

	// -- Helper methods --

	static <L> void paint(final ImgLabeling<L, IntType> labeling, final L label,
		final long x0, final long y0, final long x1, final long y1)
	{
		Views.interval(labeling, new long[] { x0, y0 }, new long[] { x1, y1 })
			.forEach(p -> p.add(label));
	}

	private static ImgLabeling<Integer, IntType> randomLabeling(
		final long[] dims, final int labels, final double density,
		final long seed)
	{
		final var random = new Random(seed);
		final var labeling = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(
			dims));
		for (final var p : Views.flatIterable(labeling)) {
			if (random.nextDouble() < density) p.add(1 + random.nextInt(labels));
		}
		return labeling;
	}

	/** The label of each pixel in raster order, or 0 if unlabeled. */
	private static int[] ids(final ImgLabeling<Integer, IntType> labeling) {
		final var result = new int[(int) labeling.size()];
		var i = 0;
		for (final var p : Views.flatIterable(labeling))
			result[i++] = p.isEmpty() ? 0 : p.iterator().next();
		return result;
	}

	private static double dist2(final int i, final int j,
		final ImgLabeling<?, ?> labeling, final double[] spacing)
	{
		var a = i;
		var b = j;
		var sum = 0.0;
		for (var d = 0; d < labeling.numDimensions(); d++) {
			final var n = (int) labeling.dimension(d);
			final var delta = (a % n - b % n) * spacing[d];
			sum += delta * delta;
			a /= n;
			b /= n;
		}
		return sum;
	}

	private static Map<Integer, Long> sizes(
		final ImgLabeling<Integer, IntType> labeling)
	{
		final var result = new HashMap<Integer, Long>();
		for (final var p : labeling)
			for (final var label : p)
				result.merge(label, 1L, Long::sum);
		return result;
	}
}
