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

package org.scijava.ops.image.geom.geom2d;

import java.util.List;
import java.util.function.Function;

import org.scijava.ops.image.geom.GeomUtils;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.scijava.ops.spi.OpDependency;

/**
 * Minimum Feret of a polygon.
 *
 * @author Tim-Oliver Buchholz, University of Konstanz
 * @implNote op names='geom.minimumFeret'
 */
public class DefaultMinimumFeret implements
	Function<Polygon2D, Pair<RealLocalizable, RealLocalizable>>
{

	@OpDependency(name = "geom.convexHull")
	private Function<Polygon2D, Polygon2D> function;

	/**
	 * TODO
	 *
	 * @param input
	 * @return the minimum feret of {@code input}
	 */
	@Override
	public Pair<RealLocalizable, RealLocalizable> apply(Polygon2D input) {
		final List<? extends RealLocalizable> points = GeomUtils.vertices(function
			.apply(input));

        var distance = Double.POSITIVE_INFINITY;
        var p0 = points.get(0);
        var p1 = points.get(0);
		double tmpDist = 0;
        var tmpP0 = p0;
        var tmpP1 = p1;

		for (var i = 0; i < points.size() - 2; i++) {
			final var lineStart = points.get(i);
			final var lineEnd = points.get(i + 1);
			tmpDist = 0;
			final var l = new Line(new Vector2D(lineStart.getDoublePosition(0),
				lineStart.getDoublePosition(1)), new Vector2D(lineEnd.getDoublePosition(
					0), lineEnd.getDoublePosition(1)), 10e-12);

			for (var j = 0; j < points.size(); j++) {
				if (j != i && j != i + 1) {
					final var ttmpP0 = points.get(j);

					final var tmp = l.distance(new Vector2D(ttmpP0.getDoublePosition(
						0), ttmpP0.getDoublePosition(1)));

					if (tmp > tmpDist) {
						tmpDist = tmp;
						final var vp = (Vector2D) l.project(new Vector2D(ttmpP0
							.getDoublePosition(0), ttmpP0.getDoublePosition(1)));
						tmpP0 = new RealPoint(vp.getX(), vp.getY());
						tmpP1 = ttmpP0;
					}
				}
			}
			if (tmpDist < distance) {
				distance = tmpDist;
				p0 = tmpP0;
				p1 = tmpP1;
			}
		}

		final var lineStart = points.get(points.size() - 1);
		final var lineEnd = points.get(0);
		final var l = new Line(new Vector2D(lineStart.getDoublePosition(0),
			lineStart.getDoublePosition(1)), new Vector2D(lineEnd.getDoublePosition(
				0), lineEnd.getDoublePosition(1)), 10e-12);
		tmpDist = 0;
		for (var j = 0; j < points.size(); j++) {
			if (j != points.size() - 1 && j != 0 + 1) {
				final var ttmpP0 = points.get(j);

				final var tmp = l.distance(new Vector2D(ttmpP0.getDoublePosition(0),
					ttmpP0.getDoublePosition(1)));

				if (tmp > tmpDist) {
					tmpDist = tmp;
					final var vp = (Vector2D) l.project(new Vector2D(ttmpP0
						.getDoublePosition(0), ttmpP0.getDoublePosition(1)));
					tmpP0 = new RealPoint(vp.getX(), vp.getY());
					tmpP1 = ttmpP0;
				}
			}
		}
		if (tmpDist < distance) {
			distance = tmpDist;
			p0 = tmpP0;
			p1 = tmpP1;
		}

		return new ValuePair<>(p0, p1);
	}
}
