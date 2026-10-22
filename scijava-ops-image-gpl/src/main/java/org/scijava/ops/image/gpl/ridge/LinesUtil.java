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
 * Constants and helpers shared by the line detector classes, from the
 * ij-ridgedetection {@code LinesUtil} class, plus small mutable holders that
 * stand in for C output parameters.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Thorsten Wagner
 * @author Carsten Steger
 */
final class LinesUtil {

	private LinesUtil() {
		// Prevent instantiation of utility class.
	}

	/** Derivative in row direction. */
	static final int DERIV_R = 1;

	/** Derivative in column direction. */
	static final int DERIV_C = 2;

	/** Second derivative in row direction. */
	static final int DERIV_RR = 3;

	/** Second derivative in row and column direction. */
	static final int DERIV_RC = 4;

	/** Second derivative in column direction. */
	static final int DERIV_CC = 5;

	/** Extract bright lines. */
	static final int MODE_LIGHT = 1;

	/** Extract dark lines. */
	static final int MODE_DARK = 2;

	static final int INITIAL_SIZE = 100;

	static final int REALLOC_FACTOR = 2;

	/** Size for Gaussian mask. */
	static final double MAX_SIZE_MASK_0 = 3.09023230616781;

	/** Size for 1st derivative mask. */
	static final double MAX_SIZE_MASK_1 = 3.46087178201605;

	/** Size for 2nd derivative mask. */
	static final double MAX_SIZE_MASK_2 = 3.82922419517181;

	/** Maximum mask index. */
	static int MASK_SIZE(final double MAX, final double sigma) {
		return (int) Math.ceil(MAX * sigma);
	}

	/**
	 * Translate row and column coordinates of an image into an index into its
	 * one-dimensional array.
	 */
	static int LINCOOR(final int row, final int col, final int width) {
		return row * width + col;
	}

	/** Mirror the row coordinate at the borders of the image. */
	static int BR(final int row, final int height) {
		return ((row) < 0 ? -(row) : (row) >= height ? height - (row) + height - 2
			: (row));
	}

	/** Mirror the column coordinate at the borders of the image. */
	static int BC(final int col, final int width) {
		return ((col) < 0 ? -(col) : (col) >= width ? width - (col) + width - 2
			: (col));
	}

	/** Mutable int, standing in for a C {@code int*} output parameter. */
	static final class MutableInt {

		private int value;

		MutableInt() {}

		MutableInt(final int value) {
			this.value = value;
		}

		int getValue() {
			return value;
		}

		int intValue() {
			return value;
		}

		void setValue(final int value) {
			this.value = value;
		}
	}

	/** Mutable double, standing in for a C {@code double*} output parameter. */
	static final class MutableDouble {

		private double value;

		MutableDouble() {}

		MutableDouble(final double value) {
			this.value = value;
		}

		double getValue() {
			return value;
		}

		double doubleValue() {
			return value;
		}

		void setValue(final double value) {
			this.value = value;
		}
	}
}
