/*-
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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
package net.imagej.ops.geom;

import static org.junit.Assert.assertEquals;

import java.util.List;

import net.imagej.ops.features.AbstractFeatureTest;
import net.imagej.ops.geom.geom2d.LabelRegionToPolygonConverter;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for polygon features.
 * 
 * A polygon is often extracted from a 2D label region.
 * 
 * @author Tim-Oliver Buchholz, University of Konstanz
 *
 */
public class PolygonFeatureTests extends AbstractFeatureTest {

	private static final double EPSILON = 10e-12;
	private static LabelRegion<String> ROI;
	private static Polygon2D contour;

	@BeforeClass
	public static void setupBefore() {
		ROI = createLabelRegion(getTestImage2D(), 1, 255);
		contour = getPolygon();
	}

	@Test
	public void boundarySizeConvexHull() {
		// ground truth computed with matlab
		assertEquals("geom.boundarySizeConvexHull", 272.1520849298494,
				((DoubleType) ops.run("geom.boundarySizeConvexHull", contour)).get(), EPSILON);
	}

	@Test
	public void boundingBox() {
		// ground truth verified with matlab
		final List<? extends RealLocalizable> received = GeomUtils.vertices(
			((Polygon2D) ops.run("geom.boundingBox", contour)));
		final RealPoint[] expected = new RealPoint[] { new RealPoint(1, 6), new RealPoint(1, 109),
				new RealPoint(78, 109), new RealPoint(78, 6) };
		assertEquals("Number of polygon points differs.", expected.length, received.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals("Polygon point " + i + " differs in x-coordinate.", expected[i].getDoublePosition(0),
					received.get(i).getDoublePosition(0), EPSILON);
			assertEquals("Polygon point " + i + " differs in y-coordinate.", expected[i].getDoublePosition(1),
					received.get(i).getDoublePosition(1), EPSILON);
		}
	}

	@Test
	public void boxivity() {
		// ground truth computed with matlab
		assertEquals("geom.boxivity", 0.6045142846804,
				((DoubleType) ops.run("geom.boxivity", contour)).get(), EPSILON);
	}

	@Test
	public void circularity() {
		// ground truth computed with matlab (according to this formula:
		// circularity = 4pi(area/perimeter^2))
		assertEquals("geom.circularity", 0.3566312416783,
				((DoubleType) ops.run("geom.circularity", contour)).get(), EPSILON);
	}

	// TODO: this test checks LabelRegionToPolygonConverter, which is a Scijava
	// Converter and thus has not been converted to the Scijava-ops framwork. What
	// should we do here?
	@Test
	public void contour() {
		// ground truth computed with matlab
		final Polygon2D test = (Polygon2D) ops.run("geom.contour", ROI, true);
		final List<? extends RealLocalizable> expected = GeomUtils.vertices(contour);
		final List<? extends RealLocalizable> received = GeomUtils.vertices(test);
		assertEquals("Number of polygon points differs.", expected.size(), received.size());
		for (int i = 0; i < contour.numVertices(); i++) {
			assertEquals("Polygon point " + i + " differs in x-coordinate.", expected.get(i).getDoublePosition(0),
					received.get(i).getDoublePosition(0), EPSILON);
			assertEquals("Polygon point " + i + " differs in y-coordinate.", expected.get(i).getDoublePosition(1),
					received.get(i).getDoublePosition(1), EPSILON);
		}
	}

	@Test
	public void convexHull2D() {
		// ground truth computed with matlab
		final Polygon2D test = (Polygon2D) ops.run("geom.convexHull", contour);
		final List<? extends RealLocalizable> received = GeomUtils.vertices(test);
		final RealPoint[] expected = new RealPoint[] { new RealPoint(1, 30), new RealPoint(2, 29), new RealPoint(26, 6),
				new RealPoint(31, 6), new RealPoint(42, 9), new RealPoint(49, 22), new RealPoint(72, 65),
				new RealPoint(78, 77), new RealPoint(48, 106), new RealPoint(42, 109), new RealPoint(34, 109),
				new RealPoint(28, 106), new RealPoint(26, 104), new RealPoint(23, 98) };
		assertEquals("Number of polygon points differs.", expected.length, received.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals("Polygon point " + i + " differs in x-coordinate.", expected[i].getDoublePosition(0),
					received.get(i).getDoublePosition(0), EPSILON);
			assertEquals("Polygon point " + i + " differs in y-coordinate.", expected[i].getDoublePosition(1),
					received.get(i).getDoublePosition(1), EPSILON);
		}
	}

	@Test
	public void convexity() {
		// formula verified and value computed with matlab
		assertEquals("geom.convexity", 0.7735853919277,
				((DoubleType) ops.run("geom.convexity", contour)).get(), EPSILON);
	}

	@Test
	public void eccentricity() {
		// formula is verified, result depends on major- and minor-axis
		// implementation
		assertEquals("geom.eccentricity", 0.863668314823,
				((DoubleType) ops.run("geom.eccentricity", contour)).get(), EPSILON);
	}

	@Test
	public void elongation() {
		// formula verified and result computed with matlab
		assertEquals("geom.mainElongation", 0.401789429879,
				((DoubleType) ops.run("geom.mainElongation", contour)).get(), EPSILON);
	}

	@Test
	public void feretsDiameterForAngle() {
		// ground truth based on minimum ferets diameter and angle
		assertEquals("geom.feretsDiameter", 58.5849810104945,
				((DoubleType) ops.run("geom.feretsDiameter", contour, 153.434948822922)).get(), EPSILON);
	}

	@Test
	public void majorAxis() {
		// Fitting ellipse is a to polygon adapted version of a pixel-based
		// implementation, which is used in ImageJ1. If a new version of ellipse
		// and fitting ellipse is available in imglib2, this version will be
		// replaced and the numbers will change.
		assertEquals("geom.majorAxis", 94.1937028134837,
				((DoubleType) ops.run("geom.majorAxis", contour)).get(), EPSILON);
	}

	@Test
	public void maximumFeretsAngle() {
		// ground truth computed with matlab
		assertEquals("geom.maximumFeretsAngle", 81.170255332091,
				((DoubleType) ops.run("geom.maximumFeretsAngle", contour)).get(), EPSILON);
	}

	@Test
	public void minimumFeretsDiameter() {
		// ground truth computed with matlab
		assertEquals("geom.minimumFeretsDiameter",  58.5849810104945,
				((DoubleType) ops.run("geom.minimumFeretsDiameter", contour)).get(), EPSILON);
	}

	@Test
	public void minimumFeretsAngle() {
		// ground truth computed with matlab
		assertEquals("geom.minimumFeretAngle", 153.434948822922,
				((DoubleType) ops.run("geom.minimumFeretsAngle", contour)).get(), EPSILON);
	}

	@Test
	public void maximumFeretsDiameter() {
		// ground truth computed with matlab
		assertEquals("geom.maximumFeretsDiameter", 104.2353107157071,
				((DoubleType) ops.run("geom.maximumFeretsDiameter", contour)).get(), EPSILON);
	}

	@Test
	public void minorAxis() {
		// Fitting ellipse is a to polygon adapted version of a pixel-based
		// implementation, which is used in ImageJ1. If a new version of ellipse
		// and fitting ellipse is available in imglib2, this version will be
		// replaced and the numbers will change.
		assertEquals("geom.minorAxis", 47.4793300114545,
				((DoubleType) ops.run("geom.minorAxis", contour)).get(), EPSILON);
	}

	@Test
	public void perimeterLength() {
		// ground truth computed with matlab
		assertEquals("geom.boundarySize", 351.8061325481604,
				((DoubleType) ops.run("geom.boundarySize", contour)).get(), EPSILON);
	}

	@Test
	public void roundness() {
		// formula is verified, ground truth is verified with matlab
		assertEquals("roundness", 0.504060553872,
				((DoubleType) ops.run("geom.roundness", contour)).get(), EPSILON);
	}

	@Test
	public void size() {
		// ground truth computed with matlab
		assertEquals("geom.size", 3512.5, ((DoubleType) ops.run("geom.size", contour)).get(),
				EPSILON);

	}

	@Test
	public void smallesEnclosingRectangle() {
		// ground truth verified with matlab
		final List<? extends RealLocalizable> received = GeomUtils.vertices(
			((Polygon2D) ops.run("geom.smallestEnclosingBoundingBox",
				contour)));
		final RealPoint[] expected = new RealPoint[] { new RealPoint(37.229184188393, -0.006307821699),
				new RealPoint(-14.757779646762, 27.800672834315), new RealPoint(31.725820016821, 114.704793944491),
				new RealPoint(83.712783851976, 86.897813288478) };
		assertEquals("Number of polygon points differs.", expected.length, received.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals("Polygon point " + i + " differs in x-coordinate.", expected[i].getDoublePosition(0),
					received.get(i).getDoublePosition(0), EPSILON);
			assertEquals("Polygon point " + i + " differs in y-coordinate.", expected[i].getDoublePosition(1),
					received.get(i).getDoublePosition(1), EPSILON);
		}
	}

	@Test
	public void sizeConvexHullPolygon() {
		assertEquals("geom.sizeConvexHull", 4731,
				((DoubleType) ops.run("geom.sizeConvexHull", contour)).get(), EPSILON);
	}

	@Test
	public void solidity2D() {
		// formula is verified, ground truth computed with matlab
		assertEquals("geom.solidity", 0.742443458043,
				((DoubleType) ops.run("geom.solidity", contour)).get(), EPSILON);
	}

	@Test
	public void verticesCountConvexHull() {
		// verified with matlab
		assertEquals("geom.verticesCountConvexHull", 14,
				((DoubleType) ops.run("geom.verticesCountConvexHull", contour)).get(), EPSILON);
	}

	@Test
	public void verticesCount() {
		// verified with matlab
		assertEquals("geom.verticesCount", 305,
				((DoubleType) ops.run("geom.verticesCount", contour)).get(), EPSILON);
	}

	// TODO: Fails due to isAssignable being unable to confirm that a LabelRegion is
	// a RandomAccessibleInterval<BoolType>
	@Test
	public void labelRegionToPolygonConverter() {
		// ground truth computed with matlab
		final LabelRegionToPolygonConverter c = new LabelRegionToPolygonConverter();
		c.setContext(ops.context());
		final Polygon2D test = c.convert(ROI, Polygon2D.class);
		final List<? extends RealLocalizable> expected = GeomUtils.vertices(contour);
		final List<? extends RealLocalizable> received = GeomUtils.vertices(test);
		assertEquals("Number of polygon points differs.", expected.size(), received.size());
		for (int i = 0; i < contour.numVertices(); i++) {
			assertEquals("Polygon point " + i + " differs in x-coordinate.", expected.get(i).getDoublePosition(0),
					received.get(i).getDoublePosition(0), EPSILON);
			assertEquals("Polygon point " + i + " differs in y-coordinate.", expected.get(i).getDoublePosition(1),
					received.get(i).getDoublePosition(1), EPSILON);
		}
	}

	@Test
	public void centroid() {
		// ground truth computed with matlab
		final RealPoint expected = new RealPoint(38.144483985765, 59.404175563464);
		final RealPoint result = (RealPoint) ops.run("geom.centroid", contour);
		assertEquals("Centroid X", expected.getDoublePosition(0), result.getDoublePosition(0), EPSILON);
		assertEquals("Centroid Y", expected.getDoublePosition(1), result.getDoublePosition(1), EPSILON);
	}
}
