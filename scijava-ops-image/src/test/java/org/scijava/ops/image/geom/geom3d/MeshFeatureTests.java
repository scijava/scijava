/*-
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

package org.scijava.ops.image.geom.geom3d;

import java.util.Iterator;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Triangle;
import org.scijava.ops.image.AbstractFeatureTest;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.types.Nil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MeshFeatureTests extends AbstractFeatureTest {

	private static final double EPSILON = 10e-12;
	private static LabelRegion<String> ROI;
	private static Mesh mesh;

	@BeforeAll
	public static void setupBefore() {
		ROI = createLabelRegion(getTestImage3D(), 1, 255);
		mesh = getMesh();
	}

	@Test
	public void testBoxivityMesh() {
		// DefaultSmallestOrientedBoundingBox is not implemented.
		assertThrows(OpMatchingException.class, () -> ops.op("geom.boxivity").input(
			mesh).outType(DoubleType.class).apply());
	}

	@Test
	public void testCompactness() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.572416357359835, ops.op("geom.compactness").input(mesh)
			.outType(DoubleType.class).apply().get(), EPSILON, "geom.compactness");
	}

	@Test
	public void testConvexHull3D() {
		// NB: convexHull3D is tested in {@link QuickHull3DTest}.
	}

	@Test
	public void testConvexityMesh() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.983930494866521, ops.op("geom.convexity").input(mesh)
			.outType(DoubleType.class).apply().get(), EPSILON, "geom.convexity");
	}

	@Test
	public void testMainElongation() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.2079585956045953, (ops.op("geom.mainElongation").input(mesh)
			.outType(DoubleType.class).apply()).get(), EPSILON,
			"geom.mainElongation");
	}

	@Test
	public void testMarchingCubes() {
		final Mesh result = (Mesh) ops.op("geom.marchingCubes").input(ROI).apply();
		assertEquals(mesh.triangles().size(), result.triangles().size());
		final Iterator<Triangle> expectedFacets = mesh.triangles().iterator();
		final Iterator<Triangle> actualFacets = result.triangles().iterator();
		while (expectedFacets.hasNext() && actualFacets.hasNext()) {
			final Triangle expected = expectedFacets.next();
			final Triangle actual = actualFacets.next();
			assertEquals(expected.v0x(), actual.v0x(), EPSILON);
			assertEquals(expected.v0y(), actual.v0y(), EPSILON);
			assertEquals(expected.v0z(), actual.v0z(), EPSILON);
			assertEquals(expected.v1x(), actual.v1x(), EPSILON);
			assertEquals(expected.v1y(), actual.v1y(), EPSILON);
			assertEquals(expected.v1z(), actual.v1z(), EPSILON);
			assertEquals(expected.v2x(), actual.v2x(), EPSILON);
			assertEquals(expected.v2y(), actual.v2y(), EPSILON);
			assertEquals(expected.v2z(), actual.v2z(), EPSILON);
		}
		assertTrue(!expectedFacets.hasNext() && !actualFacets.hasNext());
	}

	@Test
	public void testMedianElongation() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.30059118825775455, ops.op("geom.medianElongation").input(
			mesh).outType(DoubleType.class).apply().get(), EPSILON,
			"geom.medianElongation");
	}

	@Test
	public void testSizeConvexHullMesh() {
		// ground truth computed with matlab
		assertEquals(304.5, ops.op("geom.sizeConvexHull").input(mesh).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.sizeConvexHull");
	}

	@Test
	public void testSizeMesh() {
		// ground truth computed with matlab
		assertEquals(257.5, ops.op("geom.size").input(mesh).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.size");
	}

	@Test
	public void testSolidityMesh() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.845648604269294, ops.op("geom.solidity").input(mesh).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.solidity");
	}

	@Test
	public void testSpareness() {
		// formula verified
		assertEquals(0.7884710437076516, ops.op("geom.spareness").input(mesh)
			.outType(DoubleType.class).apply().get(), EPSILON, "geom.spareness");
	}

	@Test
	public void testSphericity() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.830304411183464, ops.op("geom.sphericity").input(mesh)
			.outType(DoubleType.class).apply().get(), EPSILON, "geom.sphericity");
	}

	@Test
	public void testSurfaceArea() {
		// ground truth computed with matlab
		assertEquals(235.7390893402464, ops.op("geom.boundarySize").input(mesh)
			.outType(DoubleType.class).apply().get(), EPSILON, "geom.boundarySize");
	}

	@Test
	public void testSurfaceAreaConvexHull() {
		// ground truth computed with matlab
		assertEquals(231.9508788339317, ops.op("geom.boundarySizeConvexHull").input(
			mesh).outType(DoubleType.class).apply().get(), EPSILON,
			"geom.boundarySizeConvexHull");
	}

	@Test
	public void testVerticesCountConvexHullMesh() {
		// verified with matlab
		assertEquals(57, ops.op("geom.verticesCountConvexHull").input(mesh).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.verticesCountConvexHull");
	}

	@Test
	public void testVerticesCountMesh() {
		// verified with matlab
		assertEquals(184, ops.op("geom.verticesCount").input(mesh).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.verticesCount");

	}

	@Test
	public void testVoxelization3D() {
		ops.op("geom.voxelization").input(mesh, 10, 10, 10).outType(
			new Nil<RandomAccessibleInterval<BitType>>()
			{

			}).apply();
		// https://github.com/imagej/imagej-ops/issues/422
	}
}
