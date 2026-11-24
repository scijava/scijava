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

package org.scijava.ops.image.morphology.geodesic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link GeodesicDistanceMaps}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class GeodesicDistanceMapsTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<DoubleType>> MAP =
		new Nil<>()
		{};

	private static double at(final RandomAccessibleInterval<DoubleType> map,
		final long... pos)
	{
		return map.getAt(pos).get();
	}

	private RandomAccessibleInterval<DoubleType> map(final Object marker,
		final Object mask, final double[] spacing, final Integer radius)
	{
		return ops.op("morphology.geodesicDistanceMap").input(marker, mask,
			spacing, radius).outType(MAP).apply();
	}

	/** Without obstacles, the radius 1 map is the octagonal chamfer distance. */
	@Test
	public void testOpenSquare() {
		final var mask = ArrayImgs.bits(9, 7);
		mask.forEach(BitType::setOne);
		final var marker = ArrayImgs.bits(9, 7);
		marker.getAt(0, 0).setOne();
		final var map = map(marker, mask, null, 1);
		for (var y = 0; y < 7; y++) {
			for (var x = 0; x < 9; x++) {
				final var lo = Math.min(x, y);
				final var hi = Math.max(x, y);
				assertEquals(hi - lo + Math.sqrt(2) * lo, at(map, x, y), 1e-9);
			}
		}
		// With radius 2, knight moves give the exact Euclidean distance along
		// their direction.
		final var map2 = map(marker, mask, null, null);
		assertEquals(3 * Math.sqrt(5), at(map2, 6, 3), 1e-9);
		assertEquals(Math.sqrt(2) * 6, at(map2, 6, 6), 1e-9);
	}

	/** Paths go around a wall; pixels outside the mask are NaN. */
	@Test
	public void testWall() {
		final var mask = ArrayImgs.bits(7, 5);
		mask.forEach(BitType::setOne);
		for (var y = 1; y < 5; y++)
			mask.getAt(3, y).setZero();
		final var marker = ArrayImgs.bits(7, 5);
		marker.getAt(0, 4).setOne();
		final var map = map(marker, mask, null, 1);
		assertTrue(Double.isNaN(at(map, 3, 2)));
		// Through the gap at (3, 0): 1 step + 3 diagonals, then 3 steps + 1
		// diagonal.
		assertEquals(4 + 4 * Math.sqrt(2), at(map, 4, 4), 1e-9);
		// Knight moves cannot jump over the wall, so the path still passes
		// through the gap.
		final var map2 = map(marker, mask, null, 2);
		assertTrue(at(map2, 4, 4) > Math.hypot(3, 3.5) + Math.hypot(1, 3.5));
		assertTrue(at(map2, 4, 4) < at(map, 4, 4));
	}

	/** Distances from the center of a disk approximate Euclidean distances. */
	@Test
	public void testIsotropy() {
		final var size = 61;
		final var c = size / 2;
		final var mask = ArrayImgs.bits(size, size);
		mask.forEach(BitType::setOne);
		final var marker = ArrayImgs.bits(size, size);
		marker.getAt(c, c).setOne();
		final var map1 = map(marker, mask, null, 1);
		final var map2 = map(marker, mask, null, 2);
		double max1 = 0, max2 = 0;
		for (var y = 0; y < size; y++) {
			for (var x = 0; x < size; x++) {
				final var e = Math.hypot(x - c, y - c);
				if (e < 10) continue;
				max1 = Math.max(max1, at(map1, x, y) / e - 1);
				max2 = Math.max(max2, at(map2, x, y) / e - 1);
				assertTrue(at(map1, x, y) >= e - 1e-9);
				assertTrue(at(map2, x, y) >= e - 1e-9);
			}
		}
		assertTrue(max1 < 0.083 && max1 > 0.07, "radius 1: " + max1);
		assertTrue(max2 < 0.028, "radius 2: " + max2);
	}

	@Test
	public void testSpacing3D() {
		final var mask = ArrayImgs.bits(4, 3, 5);
		mask.forEach(BitType::setOne);
		final var marker = ArrayImgs.bits(4, 3, 5);
		marker.getAt(0, 0, 0).setOne();
		final var map = map(marker, mask, new double[] { 1, 1, 2.5 }, 1);
		assertEquals(10, at(map, 0, 0, 4), 1e-9);
		assertEquals(Math.sqrt(1 + 1 + 6.25), at(map, 1, 1, 1), 1e-9);
		assertEquals(3, at(map, 3, 0, 0), 1e-9);
	}

	/** Paths never cross from one label into a touching one. */
	@Test
	public void testLabeling() {
		final var labeling = GeodesicDiametersTest.labeling( //
			"1111222", //
			"1111222", //
			"0000222");
		final var marker = ArrayImgs.bits(7, 3);
		marker.getAt(0, 0).setOne();
		final var map = map(marker, labeling, null, 1);
		assertEquals(3, at(map, 3, 0), 1e-9);
		assertEquals(Double.POSITIVE_INFINITY, at(map, 4, 0));
		assertTrue(Double.isNaN(at(map, 0, 2)));
		marker.getAt(6, 2).setOne();
		final var map2 = map(marker, labeling, null, 1);
		assertEquals(2, at(map2, 6, 0), 1e-9);
		assertEquals(3, at(map2, 3, 0), 1e-9);
	}
}
