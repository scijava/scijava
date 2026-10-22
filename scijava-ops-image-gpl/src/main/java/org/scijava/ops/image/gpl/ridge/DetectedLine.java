/*-
 * #%L
 * GPL-licensed image processing operations for SciJava Ops, translated from copyleft ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.gpl.ridge;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.DefaultWritablePolyline;

/**
 * A line detected by {@code segment.detectLines}: a polyline through the
 * sub-pixel positions of its line points, with per-point attributes.
 * <p>
 * The attributes are indexed by vertex, in the order of the vertices at
 * detection; they are not updated if vertices are added or removed later.
 * </p>
 * <p>
 * The {@link #angle(int) angle} of a line point is the direction of the line
 * normal, measured from the Y axis towards the X axis: the unit normal is
 * {@code (sin(angle), cos(angle))} in (X, Y) coordinates. The normal points to
 * the right of the line as it is traversed from its first to its last vertex;
 * the {@link #widthRight(int) right width} is measured along the normal, the
 * {@link #widthLeft(int) left width} against it.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Thorsten Wagner
 */
public class DetectedLine extends DefaultWritablePolyline {

	private final ContourClass contourClass;
	private final double[] angle;
	private final double[] response;
	private final double[] widthLeft;
	private final double[] widthRight;
	private final double[] asymmetry;
	private final double[] intensity;

	DetectedLine(final Line line, final boolean width, final boolean corrected) {
		super(vertices(line));
		final int n = line.num;
		contourClass = line.getContourClass();
		angle = toDouble(line.angle, n);
		response = toDouble(line.response, n);
		widthLeft = width ? toDouble(line.width_l, n) : null;
		widthRight = width ? toDouble(line.width_r, n) : null;
		asymmetry = corrected ? toDouble(line.asymmetry, n) : null;
		intensity = corrected ? toDouble(line.intensity, n) : null;
	}

	/** @return the classification of the line by its junctions */
	public ContourClass contourClass() {
		return contourClass;
	}

	/** @return the number of line points at detection */
	public int size() {
		return angle.length;
	}

	/**
	 * @param i the index of the line point
	 * @return the angle of the line normal at the point, in radians
	 */
	public double angle(final int i) {
		return angle[i];
	}

	/**
	 * @param i the index of the line point
	 * @return the response of the line detector at the point: the magnitude of
	 *         the second directional derivative of the smoothed image across the
	 *         line
	 */
	public double response(final int i) {
		return response[i];
	}

	/** @return whether the line width was estimated */
	public boolean hasWidth() {
		return widthLeft != null;
	}

	/**
	 * @param i the index of the line point
	 * @return the width of the line to the left of the point, or NaN if the
	 *         width was not estimated
	 */
	public double widthLeft(final int i) {
		return widthLeft == null ? Double.NaN : widthLeft[i];
	}

	/**
	 * @param i the index of the line point
	 * @return the width of the line to the right of the point, or NaN if the
	 *         width was not estimated
	 */
	public double widthRight(final int i) {
		return widthRight == null ? Double.NaN : widthRight[i];
	}

	/** @return whether the width and position bias was corrected */
	public boolean hasCorrection() {
		return asymmetry != null;
	}

	/**
	 * @param i the index of the line point
	 * @return the asymmetry of the line profile at the point (the ratio of the
	 *         background contrast between both sides; positive when the
	 *         background is brighter on the right for bright lines), or NaN if
	 *         the bias was not corrected
	 */
	public double asymmetry(final int i) {
		return asymmetry == null ? Double.NaN : asymmetry[i];
	}

	/**
	 * @param i the index of the line point
	 * @return the estimated contrast of the line at the point (negative for
	 *         dark lines), or NaN if the bias was not corrected
	 */
	public double intensity(final int i) {
		return intensity == null ? Double.NaN : intensity[i];
	}

	/** @return the length of the polyline */
	public double length() {
		double length = 0;
		for (int i = 1; i < numVertices(); i++) {
			length += Math.hypot(vertex(i).getDoublePosition(0) - vertex(i - 1)
				.getDoublePosition(0), vertex(i).getDoublePosition(1) - vertex(i - 1)
					.getDoublePosition(1));
		}
		return length;
	}

	private static List<RealPoint> vertices(final Line line) {
		final List<RealPoint> points = new ArrayList<>(line.num);
		for (int i = 0; i < line.num; i++)
			points.add(new RealPoint(line.col[i], line.row[i]));
		return points;
	}

	private static double[] toDouble(final float[] values, final int n) {
		if (values == null) return null;
		final double[] result = new double[n];
		for (int i = 0; i < n; i++)
			result[i] = values[i];
		return result;
	}
}
