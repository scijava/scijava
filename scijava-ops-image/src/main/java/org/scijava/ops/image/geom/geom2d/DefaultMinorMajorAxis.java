/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import org.scijava.ops.image.geom.GeomUtils;
import net.imglib2.RealLocalizable;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

/**
 * Generic implementation of {@code geom.secondMultiVariate}.
 *
 * @author Daniel Seebacher (University of Konstanz)
 * @implNote op names='geom.secondMoment'
 */
public class DefaultMinorMajorAxis implements
	Function<Polygon2D, Pair<DoubleType, DoubleType>>
{

	/**
	 * Code taken from ImageJ ({@code EllipseFitter#getEllipseParam()}) and
	 * adapted to work with a {@link Polygon2D}.
	 *
	 * @param input
	 * @param points vertices of polygon in counter clockwise order.
	 * @return the minor and major axis
	 */
	private double[] getMinorMajorAxis(final Polygon2D input,
		final List<RealLocalizable> points)
	{
        var moments = getMoments(input, points);

        var m00 = moments[0];
        var u20 = moments[1];
        var u11 = moments[2];
        var u02 = moments[3];

        var m4 = 4.0 * Math.abs(u02 * u20 - u11 * u11);
		if (m4 < 0.000001) {
			m4 = 0.000001;
		}

        var a11 = u02 / m4;
        var a12 = u11 / m4;
        var a22 = u20 / m4;

        var tmp = a11 - a22;
		if (tmp == 0.0) {
			tmp = 0.000001;
		}

        var ta = 0.5 * Math.atan(2.0 * a12 / tmp);
		if (ta < 0.0) {
			ta += Math.PI / 2d;
		}
		if (a12 > 0.0) {
			ta += Math.PI / 2d;
		}

		else if (a12 == 0.0) {
			if (a22 > a11) {
				ta = 0.0;
				tmp = a22;
				a22 = a11;
				a11 = tmp;
			}
			else if (a11 != a22) {
				ta = Math.PI / 2d;
			}
		}

		tmp = Math.sin(ta);
		if (tmp == 0.0) tmp = 0.000001;
        var z = a12 * Math.cos(ta) / tmp;
        var major = Math.sqrt(1.0 / Math.abs(a22 + z));
        var minor = Math.sqrt(1.0 / Math.abs(a11 - z));
		// equalize areas
        var scale = Math.sqrt(m00 / (Math.PI * major * minor));

		major = major * scale * 2.0;
		minor = minor * scale * 2.0;
        var angle = 180.0 * ta / Math.PI;

		if (angle == 180.0) angle = 0.0;
		if (major < minor) {
			tmp = major;
			major = minor;
			minor = tmp;
		}

		return new double[] { minor, major };
	}

	/**
	 * Calculates moments for {@link Polygon2D}
	 *
	 * @param points vertices of polygon in counter clockwise order.
	 * @return moments m00, n20, n11 and n02
	 * @see "On  Calculation of Arbitrary Moments of Polygon2Ds, Carsten Steger, October 1996"
	 */
	private double[] getMoments(final Polygon2D input,
		final List<RealLocalizable> points)
	{

		// calculate normalized moment
		double m00 = 0;
		double m01 = 0;
		double m02 = 0;
		double m10 = 0;
		double m11 = 0;
		double m20 = 0;

		for (var i = 1; i < points.size(); i++) {
            var a = getX(input, i - 1) * getY(input, i) - getX(input, i) * getY(
				input, i - 1);

			m00 += a;
			m10 += a * (getX(input, i - 1) + getX(input, i));
			m01 += a * (getY(input, i - 1) + getY(input, i));

			m20 += a * (Math.pow(getX(input, i - 1), 2) + getX(input, i - 1) * getX(
				input, i) + Math.pow(getX(input, i), 2));
			m11 += a * (2 * getX(input, i - 1) * getY(input, i - 1) + getX(input, i -
				1) * getY(input, i) + getX(input, i) * getY(input, i - 1) + 2 * getX(
					input, i) * getY(input, i));
			m02 += a * (Math.pow(getY(input, i - 1), 2) + getY(input, i - 1) * getY(
				input, i) + Math.pow(getY(input, i), 2));
		}

		m00 /= 2d;
		m01 /= 6 * m00;
		m02 /= 12d * m00;
		m10 /= 6d * m00;
		m11 /= 24d * m00;
		m20 /= 12d * m00;

		// calculate central moments
        var n20 = m20 - Math.pow(m10, 2);
        var n11 = m11 - m10 * m01;
        var n02 = m02 - Math.pow(m01, 2);

		return new double[] { m00, n20, n11, n02 };
	}

	private double getY(final Polygon2D input, final int index) {
        var i = index;
		if (i == input.numVertices()) i = 0;
		return input.vertex(i).getDoublePosition(1);
	}

	private double getX(final Polygon2D input, final int index) {
        var i = index;
		if (i == input.numVertices()) i = 0;
		return input.vertex(i).getDoublePosition(0);
	}

	/**
	 * TODO
	 *
	 * @param input the input {@link Polygon2D}
	 * @return the major and minor axes, within a {@link Pair}
	 */
	@Override
	public Pair<DoubleType, DoubleType> apply(final Polygon2D input) {

		List<RealLocalizable> points = new ArrayList<>(GeomUtils.vertices(input));

		// Sort RealLocalizables of P by x-coordinate (in case of a tie,
		// sort by
		// y-coordinate). Sorting is counter clockwise.
		Collections.sort(points, new Comparator<RealLocalizable>() {

			@Override
			public int compare(final RealLocalizable o1, final RealLocalizable o2) {
				final var o1x = new Double(o1.getDoublePosition(0));
				final var o2x = new Double(o2.getDoublePosition(0));
				final var result = o2x.compareTo(o1x);
				if (result == 0) {
					return new Double(o2.getDoublePosition(1)).compareTo(new Double(o1
						.getDoublePosition(1)));
				}
				return result;
			}
		});
		points.add(points.get(0));

		// calculate minor and major axis
        var minorMajorAxis = getMinorMajorAxis(input, points);
		return new ValuePair<>(new DoubleType(minorMajorAxis[0]), new DoubleType(
			minorMajorAxis[1]));
	}
}
