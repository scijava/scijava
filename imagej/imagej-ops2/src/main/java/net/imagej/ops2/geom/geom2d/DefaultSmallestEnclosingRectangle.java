/*
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

package net.imagej.ops2.geom.geom2d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.imagej.ops2.geom.GeomUtils;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.DefaultWritablePolygon2D;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ops.Op;
import org.scijava.ops.OpDependency;
import org.scijava.plugin.Plugin;

/**
 * Generic implementation of {@code geom.smallestBoundingBox}.
 * 
 * @author Daniel Seebacher (University of Konstanz)
 */
@Plugin(type = Op.class, name = "geom.smallestEnclosingBoundingBox", label = "Geometric (2D): Smallest Enclosing Rectangle")
public class DefaultSmallestEnclosingRectangle implements Function<Polygon2D, Polygon2D> {

	@OpDependency(name = "geom.convexHull")
	private Function<Polygon2D, Polygon2D> convexHullFunc;
	@OpDependency(name = "geom.centroid")
	private Function<Polygon2D, RealLocalizable> centroidFunc;
	@OpDependency(name = "geom.size")
	private Function<Polygon2D, DoubleType> areaFunc;
	@OpDependency(name = "geom.boundingBox")
	private Function<Polygon2D, Polygon2D> boundingBoxFunc;

	/**
	 * Rotates the given Polygon2D consisting of a list of RealPoints by the given
	 * angle about the given center.
	 *
	 * @param inPoly
	 *            A Polygon2D consisting of a list of RealPoint RealPoints
	 * @param angle
	 *            the rotation angle
	 * @param center
	 *            the rotation center
	 * @return a rotated polygon
	 */
	private Polygon2D rotate(final Polygon2D inPoly, final double angle, final RealLocalizable center) {

		List<RealLocalizable> out = new ArrayList<>();

		for (RealLocalizable RealPoint : GeomUtils.vertices(inPoly)) {

			// double angleInRadians = Math.toRadians(angleInDegrees);
			double cosTheta = Math.cos(angle);
			double sinTheta = Math.sin(angle);

			double x = cosTheta * (RealPoint.getDoublePosition(0) - center.getDoublePosition(0))
					- sinTheta * (RealPoint.getDoublePosition(1) - center.getDoublePosition(1))
					+ center.getDoublePosition(0);

			double y = sinTheta * (RealPoint.getDoublePosition(0) - center.getDoublePosition(0))
					+ cosTheta * (RealPoint.getDoublePosition(1) - center.getDoublePosition(1))
					+ center.getDoublePosition(1);

			out.add(new RealPoint(x, y));
		}

		return new DefaultWritablePolygon2D(out);
	}

	/**
	 * TODO
	 *
	 * @param input
	 * @param smallestEnclosingBoundingBox
	 */
	@Override
	public Polygon2D apply(final Polygon2D input) {
		// ensure validity of inputs
		if (input == null)
			throw new IllegalArgumentException("Input cannot be null!");

		Polygon2D ch = convexHullFunc.apply(input);
		RealLocalizable cog = centroidFunc.apply(ch);

		Polygon2D minBounds = input;
		double minArea = Double.POSITIVE_INFINITY;
		// for each edge (i.e. line from P(i-1) to P(i)
		for (int i = 1; i < ch.numVertices() - 1; i++) {
			final double angle = Math.atan2(ch.vertex(i).getDoublePosition(1) - ch.vertex(i - 1).getDoublePosition(1),
					ch.vertex(i).getDoublePosition(0) - ch.vertex(i - 1).getDoublePosition(0));

			// rotate the polygon in such a manner that the line has an angle of 0
			final Polygon2D rotatedPoly = rotate(ch, -angle, cog);

			// get the bounds
			final Polygon2D bounds = boundingBoxFunc.apply(rotatedPoly);

			// calculate the area of the bounds
			final double area = areaFunc.apply(bounds).get();

			// if the area of the bounds is smaller, rotate it to match the
			// original polygon and save it.
			if (area < minArea) {
				minArea = area;
				minBounds = rotate(bounds, angle, cog);
			}
		}

		// edge (n-1) to 0
		final double angle = Math.atan2(
				ch.vertex(0).getDoublePosition(1) - ch.vertex(ch.numVertices() - 1).getDoublePosition(1),
				ch.vertex(0).getDoublePosition(0) - ch.vertex(ch.numVertices() - 1).getDoublePosition(0));

		// rotate the polygon in such a manner that the line has an angle of 0
		final Polygon2D rotatedPoly = rotate(ch, -angle, cog);

		// get the bounds
		final Polygon2D bounds = boundingBoxFunc.apply(rotatedPoly);

		// calculate the area of the bounds
		final double area = areaFunc.apply(bounds).get();

		// if the area of the bounds is smaller, rotate it to match the
		// original polygon and save it.
		if (area < minArea) {
			minArea = area;
			minBounds = rotate(bounds, angle, cog);
		}

		return minBounds;
	}

}
