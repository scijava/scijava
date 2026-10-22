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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.scijava.ops.image.labeling.utils.LabelFilteringTest.paint;
import static org.scijava.ops.image.labeling.utils.LabelFilteringTest.sizes;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link LabelEditing}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class LabelEditingTest extends AbstractOpTest {

	private static final Nil<ImgLabeling<Integer, IntType>> LABELING =
		new Nil<>()
		{};

	@Test
	public void testRelabel() {
		final var labeling = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(6,
			2));
		paint(labeling, 9, 0, 0, 0, 0);
		paint(labeling, 5, 1, 0, 2, 0);
		paint(labeling, 2, 3, 0, 5, 1);
		paint(labeling, 9, 5, 1, 5, 1);
		final ImgLabeling<Integer, IntType> relabeled = ops.op("labeling.relabel")
			.input(labeling).outType(LABELING).apply();
		// Numeric labels are renumbered in their natural order.
		assertEquals(Map.of(1, 6L, 2, 2L, 3, 2L), sizes(relabeled));
		assertEquals(Set.of(1, 3), relabeled.randomAccess().setPositionAndGet(5,
			1));
		final ImgLabeling<Integer, IntType> fromZero = ops.op("labeling.relabel")
			.input(labeling, 0).outType(LABELING).apply();
		assertEquals(Map.of(0, 6L, 1, 2L, 2, 2L), sizes(fromZero));
	}

	@Test
	public void testRelabelRasterOrder() {
		// Labels without a natural ordering are numbered in raster order.
		final var labeling = new ImgLabeling<Object, IntType>(ArrayImgs.ints(3,
			2));
		final var a = new Object();
		final var b = new Object();
		paint(labeling, b, 1, 0, 1, 0);
		paint(labeling, a, 0, 1, 2, 1);
		final ImgLabeling<Integer, IntType> relabeled = ops.op("labeling.relabel")
			.input(labeling).outType(LABELING).apply();
		assertEquals(Map.of(1, 1L, 2, 3L), sizes(relabeled));
		assertEquals(Set.of(1), relabeled.randomAccess().setPositionAndGet(1, 0));
	}

	@Test
	public void testMergeLabels() {
		final var labeling = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(5,
			5));
		paint(labeling, 3, 0, 0, 1, 1);
		paint(labeling, 4, 3, 3, 4, 4);
		paint(labeling, 7, 1, 1, 2, 2);
		paint(labeling, 8, 0, 4, 0, 4);
		final ImgLabeling<Integer, IntType> merged = ops.op("labeling.mergeLabels")
			.input(labeling, List.of(7, 4, 3)).outType(LABELING).apply();
		// Label 3 overlaps label 7 at (1, 1): the union has 4 + 4 + 4 - 1 pixels.
		assertEquals(Map.of(3, 11L, 8, 1L), sizes(merged));
		final ImgLabeling<Integer, IntType> target = ops.op("labeling.mergeLabels")
			.input(labeling, List.of(4, 8), 42).outType(LABELING).apply();
		assertEquals(Map.of(3, 4L, 7, 4L, 42, 5L), sizes(target));
	}

	@Test
	public void testFillHoles() {
		final var labeling = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(12,
			7));
		// A ring with a 3x3 hole.
		paint(labeling, 1, 0, 0, 4, 4);
		removeBox(labeling, 1, 1, 3, 3);
		// Two half rings of different labels around a 1x3 hole.
		paint(labeling, 2, 6, 0, 7, 4);
		paint(labeling, 3, 8, 0, 8, 4);
		removeBox(labeling, 7, 1, 7, 3);
		// A C shape open to the border.
		paint(labeling, 4, 9, 0, 11, 4);
		removeBox(labeling, 10, 1, 11, 3);

		final ImgLabeling<Integer, IntType> filled = ops.op("labeling.fillHoles")
			.input(labeling).outType(LABELING).apply();
		assertEquals(Map.of(1, 25L, 2, 7L, 3, 5L, 4, 9L), sizes(filled));
		assertEquals(Set.of(1), filled.randomAccess().setPositionAndGet(2, 2));
		assertTrue(filled.randomAccess().setPositionAndGet(7, 2).isEmpty());
	}

	@Test
	public void testFillHolesConnectivity() {
		// A diamond outline encloses a hole only for 4-connected background.
		final var labeling = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(5,
			5));
		final var ra = labeling.randomAccess();
		for (final var p : new int[][] { { 2, 0 }, { 1, 1 }, { 3, 1 }, { 0, 2 }, {
			4, 2 }, { 1, 3 }, { 3, 3 }, { 2, 4 } })
		{
			ra.setPositionAndGet(p[0], p[1]).add(1);
		}
		final ImgLabeling<Integer, IntType> four = ops.op("labeling.fillHoles")
			.input(labeling, new DiamondShape(1)).outType(LABELING)
			.apply();
		assertEquals(Map.of(1, 13L), sizes(four));
		final ImgLabeling<Integer, IntType> eight = ops.op("labeling.fillHoles")
			.input(labeling, new RectangleShape(1, false)).outType(LABELING)
			.apply();
		assertEquals(Map.of(1, 8L), sizes(eight));
		// The default is full connectivity, so the outline is not filled.
		final ImgLabeling<Integer, IntType> defaults = ops.op("labeling.fillHoles")
			.input(labeling).outType(LABELING).apply();
		assertEquals(Map.of(1, 8L), sizes(defaults));
	}

	@Test
	public void testFillHoles3D() {
		final var labeling = new ImgLabeling<String, IntType>(ArrayImgs.ints(5, 5,
			5));
		final var ra = labeling.randomAccess();
		for (var z = 0; z < 5; z++)
			for (var y = 0; y < 5; y++)
				for (var x = 0; x < 5; x++) {
					final var shell = x == 0 || x == 4 || y == 0 || y == 4 || z == 0 ||
						z == 4;
					if (shell) ra.setPositionAndGet(x, y, z).add("cube");
				}
		final ImgLabeling<String, IntType> filled = ops.op("labeling.fillHoles")
			.input(labeling).outType(new Nil<ImgLabeling<String, IntType>>()
			{}).apply();
		assertEquals(Map.of("cube", 125L), sizes(filled));
	}

	@Test
	public void testCropLabel() {
		final var labeling = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(6,
			6));
		paint(labeling, 1, 1, 2, 3, 3);
		paint(labeling, 2, 3, 3, 5, 5);
		final RandomAccessibleInterval<BitType> crop = ops.op("labeling.cropLabel")
			.input(labeling, 1).outType(new Nil<RandomAccessibleInterval<BitType>>()
			{}).apply();
		assertArrayEquals(new long[] { 1, 2 }, crop.minAsLongArray());
		assertArrayEquals(new long[] { 3, 3 }, crop.maxAsLongArray());
		for (final var b : crop)
			assertTrue(b.get());

		final RandomAccessibleInterval<BitType> padded = ops.op(
			"labeling.cropLabel").input(labeling, 2, 1L).outType(
				new Nil<RandomAccessibleInterval<BitType>>()
				{}).apply();
		assertArrayEquals(new long[] { 2, 2 }, padded.minAsLongArray());
		assertArrayEquals(new long[] { 6, 6 }, padded.maxAsLongArray());
		var count = 0;
		for (final var b : padded)
			if (b.get()) count++;
		assertEquals(9, count);
		final var p = padded.randomAccess();
		assertTrue(p.setPositionAndGet(3, 3).get());
		assertTrue(!p.setPositionAndGet(2, 2).get());
		assertTrue(!p.setPositionAndGet(6, 6).get());
	}

	private static void removeBox(final ImgLabeling<Integer, IntType> labeling,
		final long x0, final long y0, final long x1, final long y1)
	{
		final var ra = labeling.randomAccess();
		for (var y = y0; y <= y1; y++)
			for (var x = x0; x <= x1; x++)
				ra.setPositionAndGet(x, y).clear();
	}
}
