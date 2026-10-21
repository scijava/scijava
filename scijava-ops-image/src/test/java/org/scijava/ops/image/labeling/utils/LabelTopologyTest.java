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

package org.scijava.ops.image.labeling.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.scijava.ops.image.labeling.utils.LabelFilteringTest.paint;
import static org.scijava.ops.image.labeling.utils.LabelFilteringTest.set;

import java.util.Map;
import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
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
