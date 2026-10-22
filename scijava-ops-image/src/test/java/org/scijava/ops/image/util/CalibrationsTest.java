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

package org.scijava.ops.image.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link Calibrations} and {@link Neighborhoods}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class CalibrationsTest extends AbstractOpTest {

	@Test
	public void testSpacing() {
		assertArrayEquals(new double[] { 1, 1, 1 }, Calibrations.spacing(null, 3,
			"spacing"));
		final var s = new double[] { 0.5, 2 };
		assertSame(s, Calibrations.spacing(s, 2, "spacing"));
		assertThrows(IllegalArgumentException.class, () -> Calibrations.spacing(
			new double[] { 1 }, 2, "spacing"));
		for (final var bad : new double[] { 0, -1, Double.NaN,
			Double.POSITIVE_INFINITY })
		{
			assertThrows(IllegalArgumentException.class, () -> Calibrations.spacing(
				new double[] { 1, bad }, 2, "spacing"));
		}
	}

	/** The ops sharing the helper reject a non-positive spacing. */
	@Test
	public void testOpsRejectBadSpacing() {
		final var mask = ArrayImgs.bits(5, 4);
		mask.randomAccess().setPositionAndGet(2, 2).set(true);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"image.signedDistanceTransform").input(mask, new double[] { 1, 0 })
			.outType(new Nil<RandomAccessibleInterval<FloatType>>()
			{}).apply());
	}

	@Test
	public void testNeighborOffsets() {
		final var full = Neighborhoods.neighborOffsets(null, 2);
		assertArrayEquals(new int[] { -1, -1 }, full[0]);
		assertArrayEquals(new int[] { 1, 1 }, full[1]);
		assertEquals(8, full.length);
		final var face = Neighborhoods.neighborOffsets(new DiamondShape(1), 3);
		assertEquals(6, face.length);
	}

	@Test
	public void testRasterOffsets() {
		// Dimension 0 varies fastest, as in a flat iteration.
		final var full = Neighborhoods.rasterOffsets(null, 2);
		final int[][] expected = { { -1, -1 }, { 0, -1 }, { 1, -1 }, { -1, 0 }, {
			1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 } };
		assertEquals(expected.length, full.length);
		for (var i = 0; i < expected.length; i++)
			assertArrayEquals(expected[i], full[i]);
		final var face = Neighborhoods.rasterOffsets(new DiamondShape(1), 2);
		final int[][] expectedFace = { { 0, -1 }, { -1, 0 }, { 1, 0 }, { 0, 1 } };
		assertEquals(expectedFace.length, face.length);
		for (var i = 0; i < expectedFace.length; i++)
			assertArrayEquals(expectedFace[i], face[i]);
	}
}
