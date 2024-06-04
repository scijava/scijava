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

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.MeshStats;
import net.imglib2.mesh.Triangle;
import net.imglib2.mesh.alg.MarchingCubesRealType;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.image.AbstractFeatureTest;
import org.scijava.types.Nil;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

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
			mesh).outType(Double.class).apply());
	}

	@Test
	public void centroid()
	{
		// Computed with MATLAB.
		final double[] expected = { 5.812621359223301, 5.777346278317152, 4.818770226537216 };
		final RealPoint centroid = ops.op("geom.centroid") //
				.input(mesh) //
				.outType(RealPoint.class) //
				.apply();
		Assertions.assertArrayEquals(expected, centroid.positionAsDoubleArray(), EPSILON);
	}

	@Test
	public void testCompactness() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.572416357359835, ops.op("geom.compactness").input(mesh)
			.outType(Double.class).apply(), EPSILON, "geom.compactness");
	}

	@Test
	public void testConvexHull3D() {
		// NB: convexHull3D is tested in {@link QuickHull3DTest}.
	}

	@Test
	public void testConvexityMesh() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.983930494866521, ops.op("geom.convexity").input(mesh)
			.outType(Double.class).apply(), EPSILON, "geom.convexity");
	}

	@Test
	public void testMainElongation() {
		assertEquals(0.25159610503921337, (ops.op("geom.mainElongation").input(mesh)
			.outType(Double.class).apply()), EPSILON,
			"geom.mainElongation");
	}

	@Test
	public void testMarchingCubes() {
		final Mesh result = (Mesh) ops.op("geom.marchingCubes").input(ROI).apply();
		assertEquals(mesh.triangles().size(), result.triangles().size());
		final Iterator<Triangle> expectedFacets = mesh.triangles().iterator();
		final Iterator<Triangle> actualFacets = result.triangles().iterator();
		while (expectedFacets.hasNext() && actualFacets.hasNext()) {
			assertTriangleEquality(expectedFacets.next(), actualFacets.next(), EPSILON);
		}
		assertTrue(!expectedFacets.hasNext() && !actualFacets.hasNext());
	}

	@Test
	public void testMedianElongation() {
		assertEquals(0.1288247536, ops.op("geom.medianElongation").input(
			mesh).outType(Double.class).apply(), EPSILON,
			"geom.medianElongation");
	}

	@Test
	public void testSizeConvexHullMesh() {
		// ground truth computed with matlab
		assertEquals(304.5, ops.op("geom.sizeConvexHull").input(mesh).outType(
			Double.class).apply(), EPSILON, "geom.sizeConvexHull");
	}

	@Test
	public void testSizeMesh() {
		// ground truth computed with matlab
		assertEquals(257.5, ops.op("geom.size").input(mesh).outType(
			Double.class).apply(), EPSILON, "geom.size");
	}

	@Test
	public void testSolidityMesh() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.845648604269294, ops.op("geom.solidity").input(mesh).outType(
			Double.class).apply(), EPSILON, "geom.solidity");
	}

	@Test
	public void testSpareness() {
		// formula verified
		assertEquals(0.9838757743034947, ops.op("geom.spareness").input(mesh)
			.outType(Double.class).apply(), EPSILON, "geom.spareness");
	}

	@Test
	public void testSphericity() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.830304411183464, ops.op("geom.sphericity").input(mesh)
			.outType(Double.class).apply(), EPSILON, "geom.sphericity");
	}

	@Test
	public void testSurfaceArea() {
		// ground truth computed with matlab
		assertEquals(235.7390893402464, ops.op("geom.boundarySize").input(mesh)
			.outType(Double.class).apply(), EPSILON, "geom.boundarySize");
	}

	@Test
	public void testSurfaceAreaConvexHull() {
		// ground truth computed with matlab
		assertEquals(231.9508788339317, ops.op("geom.boundarySizeConvexHull").input(
			mesh).outType(Double.class).apply(), EPSILON,
			"geom.boundarySizeConvexHull");
	}

	@Test
	public void testVerticesCountConvexHullMesh() {
		// verified with matlab
		assertEquals(57, ops.op("geom.verticesCountConvexHull").input(mesh).outType(
			Double.class).apply(), EPSILON, "geom.verticesCountConvexHull");
	}

	@Test
	public void testVerticesCountMesh() {
		// verified with matlab
		assertEquals(184, ops.op("geom.verticesCount").input(mesh).outType(
			Double.class).apply(), EPSILON, "geom.verticesCount");

	}

	@Test
	public void testVoxelization3D() {
		ops.op("geom.voxelization").input(mesh, 10, 10, 10).outType(
			new Nil<RandomAccessibleInterval<BitType>>()
			{

			}).apply();
		// https://github.com/imagej/imagej-ops/issues/422
	}

	private static void assertTriangleEquality( //
		final Triangle exp, //
		final Triangle act, //
		final double eps //
	) {
		// Determine whether triangles are inverted versions of each other
		boolean inverted = //
				Math.abs(exp.nx() + act.nx()) <= eps && //
				Math.abs(exp.ny() + act.ny()) <= eps && //
				Math.abs(exp.nz() + act.nz()) <= eps;

		// Assert normal equality
		assertEquals(exp.nx() * (inverted ? -1 : 1), act.nx(), eps);
		assertEquals(exp.ny() * (inverted ? -1 : 1), act.ny(), eps);
		assertEquals(exp.nz() * (inverted ? -1 : 1), act.nz(), eps);

		// Assert vertices equality
		long[] expIndices = new long[]{exp.vertex0(), exp.vertex1(), exp.vertex2()};
		long[] actIndices = new long[]{act.vertex0(), act.vertex1(), act.vertex2()};

		// find starting index
		Mesh eM = exp.mesh();
		Mesh aM = act.mesh();
		int actIdx = 0;
		for(; actIdx < 3; actIdx++) {
			if (equal(eM, aM, expIndices[0], actIndices[actIdx], eps)) {
				for(int i = 1; i < 3; i++) {
					actIdx = (actIdx + (inverted ? 2 : 1)) % 3;
					assertTrue(equal(eM, aM, expIndices[i], actIndices[actIdx], eps));
				}
				return;
			}
		}
		Assertions.fail();
	}

	/**
	 * Helper method comparing the equality of vertices from two different {@link Mesh}es
	 *
	 * @param exp one {@link Mesh}
	 * @param act another {@link Mesh}
	 * @param expIdx the index of some vertex in the first {@code Mesh}
	 * @param actIdx the index of another vertex in the second {@code Mesh}
	 * @param eps the threshold for numerical error
	 * @return {@code true} iff the vertices are equal between {@code Mesh}es. Equality is defined as within {@code eps} in each dimension.
	 */
	private static boolean equal(final Mesh exp, final Mesh act, final long expIdx, final long actIdx, final double eps) {
		return exp.vertices().nx(expIdx) - act.vertices().nx(actIdx) <= eps &&
			exp.vertices().ny(expIdx) - act.vertices().ny(actIdx) <= eps &&
			exp.vertices().nz(expIdx) - act.vertices().nz(actIdx) <= eps;
	}
}
