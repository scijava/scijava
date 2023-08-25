/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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
package net.imagej.ops2.geom.geom3d;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import net.imagej.mesh2.Mesh;
import net.imagej.mesh2.Triangle;
import net.imagej.ops2.AbstractFeatureTest;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpRetrievalException;
import org.scijava.ops.engine.DependencyMatchingException;
import org.scijava.types.Nil;

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
	public void boxivityMesh() {
		try {
			ops.op("geom.boxivity").arity1().input(mesh).outType(DoubleType.class).apply();
		} catch (OpRetrievalException e) {
			// DefaultSmallestOrientedBoundingBox is not implemented.
		}
	}

	@Test
	public void compactness() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.572416357359835,
				ops.op("geom.compactness").arity1().input(mesh).outType(DoubleType.class).apply().get(), EPSILON, "geom.compactness");
	}

	@Test
	public void convexHull3D() {
		/**
		 * convexHull3D is tested in {@link QuickHull3DTest}.
		 */
	}

	@Test
	public void convexityMesh() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.983930494866521,
				ops.op("geom.convexity").arity1().input(mesh).outType(DoubleType.class).apply().get(), EPSILON, "geom.convexity");
	}

	@Test
	public void mainElongation() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.2079585956045953,
				(ops.op("geom.mainElongation").arity1().input(mesh).outType(DoubleType.class).apply()).get(),
				EPSILON, "geom.mainElongation");
	}

	@Test
	public void marchingCubes() {
		final Mesh result = (Mesh) ops.op("geom.marchingCubes").arity1().input(ROI).apply();
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
	public void medianElongation() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.30059118825775455, ops.op("geom.medianElongation").arity1().input(mesh)
			.outType(DoubleType.class).apply().get(), EPSILON,
			"geom.medianElongation");
	}

	@Test
	public void sizeConvexHullMesh() {
		// ground truth computed with matlab
		assertEquals(304.5, ops.op("geom.sizeConvexHull").arity1().input(mesh).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.sizeConvexHull");
	}

	@Test
	public void sizeMesh() {
		// ground truth computed with matlab
		assertEquals(257.5, ops.op("geom.size").arity1().input(mesh).outType(DoubleType.class)
			.apply().get(), EPSILON, "geom.size");
	}

	@Test
	public void solidityMesh() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.845648604269294, ops.op("geom.solidity").arity1().input(mesh).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.solidity");
	}

	@Test
	public void spareness() {
		// formula verified
		assertEquals(0.7884710437076516, ops.op("geom.spareness").arity1().input(mesh).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.spareness");
	}

	@Test
	public void sphericity() {
		// formula verified and ground truth computed with matlab
		assertEquals(0.830304411183464, ops.op("geom.sphericity").arity1().input(mesh).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.sphericity");
	}

	@Test
	public void surfaceArea() {
		// ground truth computed with matlab
		assertEquals(235.7390893402464, ops.op("geom.boundarySize").arity1().input(mesh).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.boundarySize");
	}

	@Test
	public void surfaceAreaConvexHull() {
		// ground truth computed with matlab
		assertEquals(231.9508788339317, ops.op("geom.boundarySizeConvexHull").arity1().input(
			mesh).outType(DoubleType.class).apply().get(), EPSILON,
			"geom.boundarySizeConvexHull");
	}

	@Test
	public void verticesCountConvexHullMesh() {
		// verified with matlab
		assertEquals(57, ops.op("geom.verticesCountConvexHull").arity1().input(mesh).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.verticesCountConvexHull");
	}

	@Test
	public void verticesCountMesh() {
		// verified with matlab
		assertEquals(184, ops.op("geom.verticesCount").arity1().input(mesh).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.verticesCount");

	}

	@Test
	public void voxelization3D() {
			ops.op("geom.voxelization").arity4().input(mesh, 10, 10, 10)
					.outType(new Nil<RandomAccessibleInterval<BitType>>() {

					}).apply();
		// https://github.com/imagej/imagej-ops/issues/422
	}
}
