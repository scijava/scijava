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
 * Working representation of a junction between two lines, as in the
 * ij-ridgedetection {@code Junction} class. It is needed to split lines at
 * junction points.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Thorsten Wagner
 * @author Carsten Steger
 */
class Junction implements Comparable<Junction> {

	/** Index of line that is already processed. */
	int cont1;

	/** Index of line that runs into cont1. */
	int cont2;

	/** Index of the junction point in cont1. */
	int pos;

	/** X- (row-, before the coordinate swap) coordinate of the junction. */
	float x;

	/** Y- (column-, before the coordinate swap) coordinate of the junction. */
	float y;

	/** Line that is already processed. */
	Line lineCont1;

	/** Line that runs into lineCont1. */
	Line lineCont2;

	/** True if this junction sits on a start/end of at least one line. */
	boolean isNonTerminal = false;

	@Override
	public int compareTo(final Junction o) {
		return (((this.cont1 - o.cont1) != 0) ? this.cont1 - o.cont1 : this.pos -
			o.pos);
	}

	float getX() {
		return x;
	}

	float getY() {
		return y;
	}

	Line getLine1() {
		return lineCont1;
	}

	Line getLine2() {
		return lineCont2;
	}
}
