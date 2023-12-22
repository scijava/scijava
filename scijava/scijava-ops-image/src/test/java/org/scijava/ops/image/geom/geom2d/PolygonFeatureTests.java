/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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
package org.scijava.ops.image.geom.geom2d;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.scijava.ops.image.AbstractFeatureTest;
import org.scijava.ops.image.geom.GeomUtils;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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

	@BeforeAll
	public static void setupBefore() {
		ROI = createLabelRegion(getTestImage2D(), 1, 255);
		contour = getPolygon();
	}

	@Test
	public void boundarySizeConvexHull() {
		// ground truth computed with matlab
		assertEquals(272.1520849298494, ops.op("geom.boundarySizeConvexHull")
				.arity1().input(contour).outType(DoubleType.class).apply().get(), EPSILON, "geom.boundarySizeConvexHull");
	}

	@Test
	public void boundingBox() {
		// ground truth verified with matlab
		final List<? extends RealLocalizable> received = GeomUtils
				.vertices((ops.op("geom.boundingBox").arity1().input(contour).outType(Polygon2D.class).apply()));
		final RealPoint[] expected = new RealPoint[] { new RealPoint(1, 6), new RealPoint(1, 109),
				new RealPoint(78, 109), new RealPoint(78, 6) };
		assertEquals(expected.length, received.size(), "Number of polygon points differs.");
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].getDoublePosition(0),
					received.get(i).getDoublePosition(0), EPSILON, "Polygon point " + i + " differs in x-coordinate.");
			assertEquals(expected[i].getDoublePosition(1),
					received.get(i).getDoublePosition(1), EPSILON, "Polygon point " + i + " differs in y-coordinate.");
		}
	}

	@Test
	public void boxivity() {
		// ground truth computed with matlab
		assertEquals(0.6045142846804,
				ops.op("geom.boxivity").arity1().input(contour).outType(DoubleType.class).apply().get(), EPSILON, "geom.boxivity");
	}

	@Test
	public void circularity() {
		// ground truth computed with matlab (according to this formula:
		// circularity = 4pi(area/perimeter^2))
		assertEquals(0.3566312416783,
				ops.op("geom.circularity").arity1().input(contour).outType(DoubleType.class).apply().get(), EPSILON, "geom.circularity");
	}

	// TODO: this test checks LabelRegionToPolygonConverter, which is a Scijava
	// Converter and thus has not been converted to the Scijava-ops framwork. What
	// should we do here?
	@Test
	public void contour() {
		// ground truth computed with matlab
		final Polygon2D test = ops.op("geom.contour").arity2().input(ROI, true).outType(Polygon2D.class).apply();
		final List<? extends RealLocalizable> expected = GeomUtils.vertices(contour);
		final List<? extends RealLocalizable> received = GeomUtils.vertices(test);
		assertEquals(expected.size(), received.size(),
			"Number of polygon points differs.");
		for (int i = 0; i < contour.numVertices(); i++) {
			assertEquals(expected.get(i).getDoublePosition(0), received.get(i)
				.getDoublePosition(0), EPSILON, "Polygon point " + i +
					" differs in x-coordinate.");
			assertEquals(expected.get(i).getDoublePosition(1), received.get(i)
				.getDoublePosition(1), EPSILON, "Polygon point " + i +
					" differs in y-coordinate.");
		}
	}

	@Test
	public void convexHull2D() {
		// ground truth computed with matlab
		final Polygon2D test = ops.op("geom.convexHull").arity1().input(contour).outType(Polygon2D.class).apply();
		final List<? extends RealLocalizable> received = GeomUtils.vertices(test);
		final RealPoint[] expected = new RealPoint[] { new RealPoint(1, 30), new RealPoint(2, 29), new RealPoint(26, 6),
				new RealPoint(31, 6), new RealPoint(42, 9), new RealPoint(49, 22), new RealPoint(72, 65),
				new RealPoint(78, 77), new RealPoint(48, 106), new RealPoint(42, 109), new RealPoint(34, 109),
				new RealPoint(28, 106), new RealPoint(26, 104), new RealPoint(23, 98) };
		assertEquals(expected.length, received.size(), "Number of polygon points differs.");
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].getDoublePosition(0), received.get(i)
				.getDoublePosition(0), EPSILON, "Polygon point " + i +
					" differs in x-coordinate.");
			assertEquals(expected[i].getDoublePosition(1), received.get(i)
				.getDoublePosition(1), EPSILON, "Polygon point " + i +
					" differs in y-coordinate.");
		}
	}

	@Test
	public void convexity() {
		// formula verified and value computed with matlab
		assertEquals(0.7735853919277, ops.op("geom.convexity").arity1().input(contour).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.convexity");
	}

	@Test
	public void eccentricity() {
		// formula is verified, result depends on major- and minor-axis
		// implementation
		assertEquals(0.863668314823,
				ops.op("geom.eccentricity").arity1().input(contour).outType(DoubleType.class).apply().get(),
				EPSILON, "geom.eccentricity");
	}

	@Test
	public void elongation() {
		// formula verified and result computed with matlab
		assertEquals(0.401789429879, ops.op("geom.mainElongation").arity1().input(contour)
			.outType(DoubleType.class).apply().get(), EPSILON, "geom.mainElongation");
	}

	@Test
	public void feretsDiameterForAngle() {
		// ground truth based on minimum ferets diameter and angle
		assertEquals(58.5849810104945, ops.op("geom.feretsDiameter").arity2().input(contour,
			153.434948822922).outType(DoubleType.class).apply().get(), EPSILON,
			"geom.feretsDiameter");
	}

	@Test
	public void majorAxis() {
		// Fitting ellipse is a to polygon adapted version of a pixel-based
		// implementation, which is used in ImageJ1. If a new version of ellipse
		// and fitting ellipse is available in imglib2, this version will be
		// replaced and the numbers will change.
		assertEquals(94.1937028134837, ops.op("geom.majorAxis").arity1().input(contour).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.majorAxis");
	}

	@Test
	public void maximumFeretsAngle() {
		// ground truth computed with matlab
		assertEquals(81.170255332091, ops.op("geom.maximumFeretsAngle").arity1().input(contour)
			.outType(DoubleType.class).apply().get(), EPSILON,
			"geom.maximumFeretsAngle");
	}

	@Test
	public void minimumFeretsDiameter() {
		// ground truth computed with matlab
		assertEquals(58.5849810104945, ops.op("geom.minimumFeretsDiameter").arity1().input(
			contour).outType(DoubleType.class).apply().get(), EPSILON,
			"geom.minimumFeretsDiameter");
	}

	@Test
	public void minimumFeretsAngle() {
		// ground truth computed with matlab
		assertEquals(153.434948822922, ops.op("geom.minimumFeretsAngle").arity1().input(contour)
			.outType(DoubleType.class).apply().get(), EPSILON,
			"geom.minimumFeretAngle");
	}

	@Test
	public void maximumFeretsDiameter() {
		// ground truth computed with matlab
		assertEquals(104.2353107157071, ops.op("geom.maximumFeretsDiameter").arity1().input(
			contour).outType(DoubleType.class).apply().get(), EPSILON,
			"geom.maximumFeretsDiameter");
	}

	@Test
	public void minorAxis() {
		// Fitting ellipse is a to polygon adapted version of a pixel-based
		// implementation, which is used in ImageJ1. If a new version of ellipse
		// and fitting ellipse is available in imglib2, this version will be
		// replaced and the numbers will change.
		assertEquals(47.4793300114545, ops.op("geom.minorAxis").arity1().input(contour).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.minorAxis");
	}

	@Test
	public void perimeterLength() {
		// ground truth computed with matlab
		assertEquals(351.8061325481604, ops.op("geom.boundarySize").arity1().input(contour)
			.outType(DoubleType.class).apply().get(), EPSILON, "geom.boundarySize");
	}

	@Test
	public void roundness() {
		// formula is verified, ground truth is verified with matlab
		assertEquals(0.504060553872, ops.op("geom.roundness").arity1().input(contour).outType(
			DoubleType.class).apply().get(), EPSILON, "roundness");
	}

	@Test
	public void size() {
		// ground truth computed with matlab
		assertEquals(3512.5, ops.op("geom.size").arity1().input(contour).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.size");

	}

	@Test
	public void smallesEnclosingRectangle() {
		// ground truth verified with matlab
		final List<? extends RealLocalizable> received = GeomUtils
				.vertices((ops.op("geom.smallestEnclosingBoundingBox").arity1().input(contour)
						.outType(Polygon2D.class).apply()));
		final RealPoint[] expected = new RealPoint[] { new RealPoint(37.229184188393, -0.006307821699),
				new RealPoint(-14.757779646762, 27.800672834315), new RealPoint(31.725820016821, 114.704793944491),
				new RealPoint(83.712783851976, 86.897813288478) };
		assertEquals(expected.length, received.size(),
			"Number of polygon points differs.");
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].getDoublePosition(0), received.get(i)
				.getDoublePosition(0), EPSILON, "Polygon point " + i +
					" differs in x-coordinate.");
			assertEquals(expected[i].getDoublePosition(1), received.get(i)
				.getDoublePosition(1), EPSILON, "Polygon point " + i +
					" differs in y-coordinate.");
		}
	}

	@Test
	public void sizeConvexHullPolygon() {
		assertEquals(4731, ops.op("geom.sizeConvexHull").arity1().input(contour).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.sizeConvexHull");
	}

	@Test
	public void solidity2D() {
		// formula is verified, ground truth computed with matlab
		assertEquals(0.742443458043, ops.op("geom.solidity").arity1().input(contour).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.solidity");
	}

	@Test
	public void verticesCountConvexHull() {
		// verified with matlab
		assertEquals(14, ops.op("geom.verticesCountConvexHull").arity1().input(contour).outType(
			DoubleType.class).apply().get(), EPSILON, "geom.verticesCountConvexHull");
	}

	@Test
	public void verticesCount() {
		// verified with matlab
		assertEquals(305,
				ops.op("geom.verticesCount").arity1().input(contour).outType(DoubleType.class).apply().get(),
				EPSILON, "geom.verticesCount");
	}

	@Test
	public void centroid() {
		// ground truth computed with matlab
		final RealPoint expected = new RealPoint(38.144483985765, 59.404175563464);
		final RealPoint result = (RealPoint) ops.op("geom.centroid").arity1().input(contour).apply();
		assertEquals(expected.getDoublePosition(0), result.getDoublePosition(0),
			EPSILON, "Centroid X");
		assertEquals(expected.getDoublePosition(1), result.getDoublePosition(1),
			EPSILON, "Centroid Y");
	}
}
