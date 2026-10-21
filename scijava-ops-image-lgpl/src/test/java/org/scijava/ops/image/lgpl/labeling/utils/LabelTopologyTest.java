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

package org.scijava.ops.image.lgpl.labeling.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.scijava.ops.image.lgpl.labeling.utils.LabelFilteringTest.paint;
import static org.scijava.ops.image.lgpl.labeling.utils.LabelFilteringTest.set;

import java.util.Map;
import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.lgpl.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link LabelTopology}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class LabelTopologyTest extends AbstractOpTest {

	private static final Nil<Map<Integer, Set<Integer>>> GRAPH = new Nil<>() {};

	private static final Nil<RandomAccessibleInterval<BitType>> MASK =
		new Nil<>()
		{};

	/** A 6x6 labeling of a 3x3 grid of 2x2 blocks, labeled 0 to 8. */
	private static ImgLabeling<Integer, IntType> grid() {
		final var labeling = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(6,
			6));
		for (var j = 0; j < 3; j++)
			for (var i = 0; i < 3; i++)
				paint(labeling, 3 * j + i, 2 * i, 2 * j, 2 * i + 1, 2 * j + 1);
		return labeling;
	}

	@Test
	public void testBoundaries() {
		final var labeling = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(7,
			7));
		paint(labeling, 1, 1, 1, 5, 5);
		final RandomAccessibleInterval<BitType> boundaries = ops.op(
			"labeling.boundaries").input(labeling).outType(MASK).apply();
		// The perimeter of the 5x5 square.
		assertEquals(16, count(boundaries));
		final var b = boundaries.randomAccess();
		assertEquals(true, b.setPositionAndGet(1, 3).get());
		assertEquals(false, b.setPositionAndGet(2, 3).get());
		assertEquals(false, b.setPositionAndGet(0, 3).get());

		// Touching regions: both sides of the shared edge are boundaries.
		final var g = grid();
		final RandomAccessibleInterval<BitType> all = ops.op("labeling.boundaries")
			.input(g, StructuringElement.FOUR_CONNECTED).outType(MASK).apply();
		// All pixels except the corners of the field of view.
		assertEquals(32, count(all));

		// Pixel (0, 0) differs from a neighbor only diagonally.
		final var corner = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(2, 2));
		paint(corner, 1, 0, 0, 1, 0);
		paint(corner, 1, 0, 1, 0, 1);
		paint(corner, 2, 1, 1, 1, 1);
		final RandomAccessibleInterval<BitType> face = ops.op("labeling.boundaries")
			.input(corner, StructuringElement.FOUR_CONNECTED).outType(MASK).apply();
		assertEquals(3, count(face));
		final RandomAccessibleInterval<BitType> full = ops.op("labeling.boundaries")
			.input(corner, StructuringElement.EIGHT_CONNECTED).outType(MASK).apply();
		assertEquals(4, count(full));
	}

	@Test
	public void testAdjacency() {
		final var g = grid();
		final Map<Integer, Set<Integer>> four = ops.op("labeling.adjacency").input(
			g).outType(GRAPH).apply();
		assertEquals(9, four.size());
		assertEquals(set(1, 3, 5, 7), four.get(4));
		assertEquals(set(1, 3), four.get(0));
		assertEquals(set(0, 2, 4), four.get(1));
		final Map<Integer, Set<Integer>> eight = ops.op("labeling.adjacency").input(
			g, StructuringElement.EIGHT_CONNECTED).outType(GRAPH).apply();
		assertEquals(set(0, 1, 2, 3, 5, 6, 7, 8), eight.get(4));
		assertEquals(set(1, 3, 4), eight.get(0));
		for (final var entry : eight.entrySet())
			for (final var neighbor : entry.getValue())
				assertEquals(true, eight.get(neighbor).contains(entry.getKey()));
	}

	@Test
	public void testAdjacencyOverlapAndIsolation() {
		final var labeling = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(8,
			3));
		paint(labeling, 1, 0, 0, 2, 2);
		paint(labeling, 2, 2, 2, 3, 2);
		paint(labeling, 3, 6, 0, 7, 0);
		final Map<Integer, Set<Integer>> graph = ops.op("labeling.adjacency")
			.input(labeling).outType(GRAPH).apply();
		assertEquals(Map.of(1, set(2), 2, set(1), 3, set()), graph);
	}

	private static long count(final RandomAccessibleInterval<BitType> mask) {
		var count = 0L;
		for (final var b : mask)
			if (b.get()) count++;
		return count;
	}
}
