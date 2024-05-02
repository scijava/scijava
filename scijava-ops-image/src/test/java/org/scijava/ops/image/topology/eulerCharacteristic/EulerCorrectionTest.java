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
import org.scijava.ops.image.topology.eulerCharacteristic.EulerCorrection.Traverser;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link EulerCorrection}
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class EulerCorrectionTest extends AbstractOpTest {

	@Test
	public void testConforms() throws Exception {
		final Img<BitType> img = ArrayImgs.bits(3, 3);

		final DoubleType result = new DoubleType();
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			ops.op("topology.eulerCorrection").input(img).output(result).compute();
		});
	}

	@Test
	public void testCube() throws Exception {
		final Img<BitType> cube = drawCube(3, 3, 3, 1);
		final Traverser<BitType> traverser = new Traverser<>(cube);

		final int vertices = EulerCorrection.stackCorners(traverser);
		assertEquals(0, vertices, "Number of stack vertices is incorrect");

		final long edges = EulerCorrection.stackEdges(traverser);
		assertEquals(0, edges, "Number stack edge voxels is incorrect");

		final int faces = EulerCorrection.stackFaces(traverser);
		assertEquals(0, faces, "Number stack face voxels is incorrect");

		final long voxelEdgeIntersections = EulerCorrection.voxelEdgeIntersections(
			traverser);
		assertEquals(0, voxelEdgeIntersections,
			"Number intersections is incorrect");

		final long voxelFaceIntersections = EulerCorrection.voxelFaceIntersections(
			traverser);
		assertEquals(0, voxelFaceIntersections,
			"Number intersections is incorrect");

		final long voxelEdgeFaceIntersections = EulerCorrection
			.voxelEdgeFaceIntersections(traverser);
		assertEquals(0, voxelEdgeFaceIntersections,
			"Number intersections is incorrect");

		DoubleType result = new DoubleType();
		ops.op("topology.eulerCorrection").input(cube).output(result).compute();

		assertEquals(0, result.get(), 1e-12, "Euler correction is incorrect");
	}

	@Test
	public void testEdgeCube() throws Exception {
		final int edges = 12;
		final int cubeSize = 3;
		final int edgeSize = cubeSize - 2;
		final Img<BitType> cube = drawCube(cubeSize, cubeSize, cubeSize, 0);
		final Traverser<BitType> traverser = new Traverser<>(cube);

		final int vertices = EulerCorrection.stackCorners(traverser);
		assertEquals(8, vertices, "Number of stack vertices is incorrect");

		final long stackEdges = EulerCorrection.stackEdges(traverser);
		assertEquals(edges * edgeSize, stackEdges,
			"Number stack edge voxels is incorrect");

		final int faces = EulerCorrection.stackFaces(traverser);
		assertEquals(6 * edgeSize * edgeSize, faces,
			"Number stack face voxels is incorrect");

		final long voxelEdgeIntersections = EulerCorrection.voxelEdgeIntersections(
			traverser);
		// you can fit n - 1 2x1 edges on edges whose size is n
		final long expectedVEIntersections = edges * (cubeSize - 1);
		assertEquals(expectedVEIntersections, voxelEdgeIntersections,
			"Number intersections is incorrect");

		final long xyVFIntersections = (cubeSize + 1) * (cubeSize + 1);
		final long yzVFIntersections = (cubeSize - 1) * (cubeSize + 1);
		final long xzVFIntersections = (cubeSize - 1) * (cubeSize - 1);
		final long expectedVFIntersections = xyVFIntersections * 2 +
			yzVFIntersections * 2 + xzVFIntersections * 2;
		final long voxelFaceIntersections = EulerCorrection.voxelFaceIntersections(
			traverser);
		assertEquals(expectedVFIntersections, voxelFaceIntersections,
			"Number intersections is incorrect");

		final long voxelEdgeFaceIntersections = EulerCorrection
			.voxelEdgeFaceIntersections(traverser);
		assertEquals(108, voxelEdgeFaceIntersections,
			"Number intersections is incorrect");

		DoubleType result = new DoubleType();
		ops.op("topology.eulerCorrection").input(cube).output(result).compute();
		assertEquals(1, result.get(), 1e-12, "Euler contribution is incorrect");
	}
}
