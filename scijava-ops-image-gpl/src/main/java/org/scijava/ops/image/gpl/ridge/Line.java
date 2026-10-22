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

/**
 * Working representation of an extracted line (contour), as in the
 * ij-ridgedetection {@code Line} class. Row coordinates are Y and column
 * coordinates are X.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Thorsten Wagner
 * @author Carsten Steger
 */
class Line {

	/** Number of points. */
	int num;

	/** Row coordinates of the line points. */
	float[] row;

	/** Column coordinates of the line points. */
	float[] col;

	/** Angle of the normal (measured from the row axis). */
	float[] angle;

	/** Response of the line point (second derivative). */
	float[] response;

	/** Width to the left of the line. */
	float[] width_l;

	/** Width to the right of the line. */
	float[] width_r;

	/** Asymmetry of the line point. */
	float[] asymmetry;

	/** Intensity of the line point. */
	float[] intensity;

	private ContourClass cont_class;

	private final int id;

	/**
	 * Creates a line with the next ID of the given ID source.
	 *
	 * @param ids the per-detection ID counter
	 */
	Line(final Lines ids) {
		id = ids.nextId();
	}

	ContourClass getContourClass() {
		return cont_class;
	}

	void setContourClass(final ContourClass cont_class) {
		this.cont_class = cont_class;
	}

	float[] getXCoordinates() {
		return col;
	}

	float[] getYCoordinates() {
		return row;
	}

	int getNumber() {
		return num;
	}

	int getID() {
		return id;
	}

	int getStartOrdEndPosition(final float x, final float y) {
		final double distStart = Math.sqrt(Math.pow(col[0] - x, 2) + Math.pow(
			row[0] - y, 2));
		final double distEnd = Math.sqrt(Math.pow(col[(this.num - 1)] - x, 2) +
			Math.pow(row[(this.num - 1)] - y, 2));
		return (distStart < distEnd ? 0 : this.num - 1);
	}

	double estimateLength() {
		double length = 0;
		for (int i = 1; i < num; i++) {
			length += Math.sqrt(Math.pow(col[i] - col[i - 1], 2) + Math.pow(row[i] -
				row[i - 1], 2));
		}
		return length;
	}
}
