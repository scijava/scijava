/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.topology.eulerCharacteristic;

import static org.scijava.ops.image.topology.eulerCharacteristic.TestHelper.drawCube;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link EulerCharacteristic26N} op
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class EulerCharacteristic26NTest extends AbstractOpTest {

	@Test
	public void testConforms() throws AssertionError {
		final Img<BitType> img = ArrayImgs.bits(3, 3);

		final DoubleType result = new DoubleType();
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ops.op("topology.eulerCharacteristic26N").input(img).output(result)
				.compute();
		});
	}

	@Test
	public void testNeighborhoodEulerIndex() throws Exception {
		final Img<BitType> bits = ArrayImgs.bits(2, 2, 2);
		final RandomAccess<BitType> neighborhood = bits.randomAccess();

		// Create and test all 256 configurations of a 2x2x2 neighborhood
		for (int n = 0; n < 256; n++) {
			setNeighbor(neighborhood, 0, 0, 0, n & 0b00000001);
			setNeighbor(neighborhood, 1, 0, 0, n & 0b00000010);
			setNeighbor(neighborhood, 0, 1, 0, n & 0b00000100);
			setNeighbor(neighborhood, 1, 1, 0, n & 0b00001000);
			setNeighbor(neighborhood, 0, 0, 1, n & 0b00010000);
			setNeighbor(neighborhood, 1, 0, 1, n & 0b00100000);
			setNeighbor(neighborhood, 0, 1, 1, n & 0b01000000);
			setNeighbor(neighborhood, 1, 1, 1, n & 0b10000000);

			final int result = EulerCharacteristic26N.neighborhoodEulerIndex(
				neighborhood, 0, 0, 0);

			assertEquals(n, result, "LUT index is incorrect");
		}
	}

	/**
	 * Test with a single voxel (=solid cube) that floats in the middle of a 3x3x3
	 * space
	 * <p>
	 * Here χ = β_0 - β_1 + β_2 = 1 - 0 + 0 = 1.<br>
	 * The formula χ = vertices - edges + faces for surfaces of polyhedra doesn't
	 * apply because the cuboid is solid.
	 * </p>
	 */
	@Test
	public void testCube() throws Exception {
		final Img<BitType> img = drawCube(1, 1, 1, 1);

		final DoubleType result = new DoubleType();
		ops.op("topology.eulerCharacteristic26N").input(img).output(result)
			.compute();

		assertEquals(1.0, result.get(), 1e-12,
			"Euler characteristic (χ) is incorrect");
	}

	/**
	 * Test with a single voxel (=solid cube) in a 1x1x1 space
	 * <p>
	 * Result differs from {@link #testCube} because voxel touches edges (edge
	 * correction)
	 * </p>
	 */
	@Test
	public void testEdgeCube() throws Exception {
		final Img<BitType> img = drawCube(1, 1, 1, 0);

		final DoubleType result = new DoubleType();
		ops.op("topology.eulerCharacteristic26N").input(img).output(result)
			.compute();

		assertEquals(0.0, result.get(), 1e-12,
			"Euler characteristic (χ) is incorrect");
	}

	/**
	 * Test with a cube that has a cavity inside
	 * <p>
	 * Here χ = β_0 - β_1 + β_2 = 1 - 0 + 1 = 2
	 * </p>
	 */
	@Test
	public void testHollowCube() throws Exception {
		final Img<BitType> img = drawCube(3, 3, 3, 1);
		final RandomAccess<BitType> access = img.randomAccess();

		// Add a cavity
		access.setPosition(new long[] { 2, 2, 2 });
		access.get().setZero();

		final DoubleType result = new DoubleType();
		ops.op("topology.eulerCharacteristic26N").input(img).output(result)
			.compute();

		assertEquals(2.0, result.get(), 1e-12,
			"Euler characteristic (χ) is incorrect");
	}

	/**
	 * Test with a cube that has a "handle"
	 * <p>
	 * Here χ = β_0 - β_1 + β_2 = 1 - 1 + 0 = 0
	 * </p>
	 */
	@Test
	public void testHandleCube() throws Exception {
		final Img<BitType> cube = drawCube(9, 9, 9, 5);
		final RandomAccess<BitType> access = cube.randomAccess();

		// Draw a handle on the front xy-face of the cuboid
		access.setPosition(9, 0);
		access.setPosition(6, 1);
		access.setPosition(4, 2);
		access.get().setOne();
		access.setPosition(3, 2);
		access.get().setOne();
		access.setPosition(7, 1);
		access.get().setOne();
		access.setPosition(8, 1);
		access.get().setOne();
		access.setPosition(4, 2);
		access.get().setOne();

		final DoubleType result = new DoubleType();
		ops.op("topology.eulerCharacteristic26N").input(cube).output(result)
			.compute();

		assertEquals(0.0, result.get(), 1e-12,
			"Euler characteristic (χ) is incorrect");
	}

	// region -- Helper methods --
	private void setNeighbor(RandomAccess<BitType> access, long x, long y, long z,
		long value)
	{
		access.setPosition(x, 0);
		access.setPosition(y, 1);
		access.setPosition(z, 2);
		access.get().setInteger(value);
	}
	// endregion
}
