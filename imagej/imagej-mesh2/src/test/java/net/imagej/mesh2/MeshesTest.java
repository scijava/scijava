/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2016 - 2022 ImageJ2 developers.
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
package net.imagej.mesh2;

import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.RandomAccessibleIntervalCursor;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Test;
import org.scijava.collections.LongArray;
import org.scijava.testutil.ArrayIO;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MeshesTest {

	private static final double EPSILON = 10e-12;

	private static final Point p1 = new Point(0, 0, 0);
	private static final Point p2 = new Point(1, 0, 0);
	private static final Point p3 = new Point(1, 1, 0);
	private static final Point p4 = new Point(1, 1, 1);

	@Test
	public void testRemoveDuplicateVertices() {

		Mesh mesh = createMeshWithNoise();

		Mesh res = Meshes.removeDuplicateVertices(mesh, 2);
		assertEquals(4, res.vertices().size());

		assertEquals(p1.getDoublePosition(0), res.vertices().x(0), EPSILON);
		assertEquals(p1.getDoublePosition(1), res.vertices().y(0), EPSILON);
		assertEquals(p1.getDoublePosition(2), res.vertices().z(0), EPSILON);

		assertEquals(p2.getDoublePosition(0), res.vertices().x(1), EPSILON);
		assertEquals(p2.getDoublePosition(1), res.vertices().y(1), EPSILON);
		assertEquals(p2.getDoublePosition(2), res.vertices().z(1), EPSILON);

		assertEquals(p3.getDoublePosition(0), res.vertices().x(2), EPSILON);
		assertEquals(p3.getDoublePosition(1), res.vertices().y(2), EPSILON);
		assertEquals(p3.getDoublePosition(2), res.vertices().z(2), EPSILON);

		assertEquals(p4.getDoublePosition(0), res.vertices().x(3), EPSILON);
		assertEquals(p4.getDoublePosition(1), res.vertices().y(3), EPSILON);
		assertEquals(p4.getDoublePosition(2), res.vertices().z(3), EPSILON);

		res = Meshes.removeDuplicateVertices(mesh, 3);
		assertEquals(6, res.vertices().size());
	}

	@Test
	public void testMarchingCubesBooleanType() {
		LabelRegion<String> ROI = createLabelRegion(getTestImage3D(), 1, 255);
		Mesh mesh = getMesh();
		final Mesh result = Meshes.marchingCubes(ROI);
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
	public void testMarchingCubesRealType() {
		LabelRegion<String> ROI = createLabelRegion(getTestImage3D(), 1, 255);
		Mesh mesh = getMesh();
		final Mesh result = Meshes.marchingCubes(ROI, 1.0);
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

	private static Mesh createMeshWithNoise() {
		Mesh mesh = new NaiveDoubleMesh();

		// Make mesh with two triangles sharing two points with each other.
		// The points are a bit off in the third decimal digit.
		mesh.vertices().add(p1.getDoublePosition(0) + 0.001, p1.getDoublePosition(1) - 0.001, p1.getDoublePosition(2) - 0.004);
		mesh.vertices().add(p2.getDoublePosition(0) + 0.004, p2.getDoublePosition(1) - 0.000, p2.getDoublePosition(2) + 0.002);
		mesh.vertices().add(p3.getDoublePosition(0) - 0.002, p3.getDoublePosition(1) + 0.003, p3.getDoublePosition(2) + 0.001);
		mesh.triangles().add(0, 1, 2);
		mesh.vertices().add(p2.getDoublePosition(0) + 0.001, p2.getDoublePosition(1) - 0.001, p2.getDoublePosition(2) - 0.004);
		mesh.vertices().add(p4.getDoublePosition(0) + 0.004, p4.getDoublePosition(1) - 0.000, p4.getDoublePosition(2) + 0.002);
		mesh.vertices().add(p3.getDoublePosition(0) + 0.002, p3.getDoublePosition(1) + 0.003, p3.getDoublePosition(2) + 0.001);
		mesh.triangles().add(3, 4, 5);
		return mesh;
	}

	private static Img<FloatType> getTestImage3D() {
		try (final InputStream is = MeshesTest.class.getResourceAsStream("/3d_geometric_features_testlabel.bin")) {
			return ArrayImgs.floats(ArrayIO.floats(is), 13, 12, 10);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Mesh getMesh() {
		final Mesh m = new NaiveDoubleMesh();
		// To prevent duplicates, map each (x, y, z) triple to its own index.
		final Map<Vector3D, Long> indexMap = new HashMap<>();
		final LongArray indices = new LongArray();
		final URL url = MeshesTest.class.getResource("/3d_geometric_features_mesh.txt");
		try (Stream<String> lines = Files.lines(Paths.get(url.toURI()))) {
			lines.forEach(l -> {
				String[] coord = l.split(" ");
				final double x = Double.parseDouble(coord[0]);
				final double y = Double.parseDouble(coord[1]);
				final double z = Double.parseDouble(coord[2]);
				final Vector3D vertex = new Vector3D(x, y, z);
				final long vIndex = indexMap.computeIfAbsent(vertex, //
						v -> m.vertices().add(x, y, z));
				indices.add(vIndex);
			});
		} catch (IOException | URISyntaxException exc) {
			exc.printStackTrace();
		}
		for (int i = 0; i < indices.size(); i += 3) {
			final long v0 = indices.get(i);
			final long v1 = indices.get(i + 1);
			final long v2 = indices.get(i + 2);
			m.triangles().add(v0, v1, v2);
		}
		return m;
	}

	protected static <T extends RealType<T>> LabelRegion<String> createLabelRegion(
			final RandomAccessibleInterval<T> interval, final float min, final float max, long... dims)
	{
		if (dims == null || dims.length == 0) {
			dims = new long[interval.numDimensions()];
			interval.dimensions(dims);
		}
		final ImgLabeling<String, IntType> labeling =
				new ImgLabeling<>(ArrayImgs.ints(dims));

		final RandomAccess<LabelingType<String>> ra = labeling.randomAccess();
		final RandomAccessibleIntervalCursor<T> c = new RandomAccessibleIntervalCursor<>(interval);
		final long[] pos = new long[labeling.numDimensions()];
		while (c.hasNext()) {
			final T item = c.next();
			final float value = item.getRealFloat();
			if (value >= min && value <= max) {
				c.localize(pos);
				ra.setPosition(pos);
				ra.get().add("1");
			}
		}
		final LabelRegions<String> labelRegions = new LabelRegions<>(labeling);

		return labelRegions.getLabelRegion("1");

	}

}
