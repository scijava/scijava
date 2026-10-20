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

package org.scijava.ops.image.labeling.edgecorrection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link EdgeCorrection}.
 *
 * @author Curtis Rueden
 */
public class EdgeCorrectionTest extends AbstractOpTest {

	/** Paints an axis-aligned box with the given label into an index image. */
	private static void box(final Img<IntType> indexImg,
		final int label, final long[] min, final long[] max)
	{
		final var c = indexImg.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			var inside = true;
			for (var d = 0; d < min.length; d++) {
				final var p = c.getLongPosition(d);
				if (p < min[d] || p > max[d]) inside = false;
			}
			if (inside) c.get().set(label);
		}
	}

	private static ImgLabeling<Integer, IntType> labeling(
		final Img<IntType> indexImg, final int maxLabel)
	{
		return ImgLabeling.fromImageAndLabels(indexImg, IntStream.rangeClosed(1,
			maxLabel).boxed().collect(Collectors.toList()));
	}

	@Test
	public void testWeights() {
		final var index = ArrayImgs.ints(100, 50);
		// Label 1 is a 10 x 5 box, label 2 touches the edge, and label 3 is a
		// single pixel.
		box(index, 1, new long[] { 10, 10 }, new long[] { 19, 14 });
		box(index, 2, new long[] { 0, 30 }, new long[] { 4, 34 });
		box(index, 3, new long[] { 50, 40 }, new long[] { 50, 40 });
		final var labeling = labeling(index, 3);

		final Map<Integer, Double> weights = ops.op(
			"labeling.edgeCorrectionWeights").input(labeling).outType(
				new Nil<Map<Integer, Double>>()
				{}).apply();
		assertEquals(100.0 * 50 / (90 * 45), weights.get(1), 1e-12);
		assertEquals(0, weights.get(2), 0);
		assertEquals(100.0 * 50 / (99 * 49), weights.get(3), 1e-12);

		final var count = ops.op("labeling.edgeCorrectedCount").input(labeling)
			.outType(Double.class).apply();
		assertEquals(weights.get(1) + weights.get(3), count, 1e-12);
	}

	@Test
	public void testUnbiasedCount3D() {
		// Place cubes of side 6 uniformly at random (by their minimum corner) in
		// an extended region, so that each cube intersecting the 40^3 field of
		// view is (partially) visible. The expected number of cubes whose
		// corner lies inside the field of view is the true density times the
		// field volume; the edge-corrected count of the fully visible cubes
		// estimates it without bias.
		final var random = new Random(1234);
		final int size = 40, side = 6, cubes = 400;
		final var index = ArrayImgs.ints(size, size, size);
		long trueCount = 0;
		int label = 0;
		final var extent = size + 2 * side;
		final var occupied = new boolean[extent][extent][extent];
		for (var i = 0; i < cubes; i++) {
			// The corner lies in [-side + 1, size - 1], so every cube is visible.
			final var min = new long[3];
			for (var d = 0; d < 3; d++)
				min[d] = random.nextInt(size + side - 1) - side + 1;
			// Keep cubes disjoint, so that each label is a whole cube.
			final int ox = (int) min[0] + side, oy = (int) min[1] + side,
					oz = (int) min[2] + side;
			var free = true;
			for (var x = 0; x < side && free; x++)
				for (var y = 0; y < side && free; y++)
					for (var z = 0; z < side && free; z++)
						free = !occupied[ox + x][oy + y][oz + z];
			if (!free) continue;
			for (var x = 0; x < side; x++)
				for (var y = 0; y < side; y++)
					for (var z = 0; z < side; z++)
						occupied[ox + x][oy + y][oz + z] = true;
			label++;
			box(index, label, min, new long[] { min[0] + side - 1, min[1] + side -
				1, min[2] + side - 1 });
			if (min[0] >= 0 && min[1] >= 0 && min[2] >= 0) trueCount++;
		}
		final var labeling = labeling(index, label);

		final var count = ops.op("labeling.edgeCorrectedCount").input(labeling)
			.outType(Double.class).apply();
		// Only interior cubes count, each with weight (40 / 34)^3, about 1.63.
		final var interior = count / Math.pow(40.0 / 34, 3);
		assertEquals(Math.rint(interior), interior, 1e-9);
		// The corrected count is close to the number of cubes "belonging" to the
		// field of view, and much closer than the naive interior count.
		assertTrue(Math.abs(count - trueCount) < 0.25 * trueCount, "count=" +
			count + ", true=" + trueCount);
		assertTrue(Math.abs(count - trueCount) < Math.abs(interior - trueCount),
			"count=" + count + ", interior=" + interior + ", true=" + trueCount);
	}
}
