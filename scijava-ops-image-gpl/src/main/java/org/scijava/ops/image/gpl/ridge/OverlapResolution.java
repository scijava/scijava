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
 * How the line detector resolves lines that overlap (cross) each other.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Mark Hiner
 */
public enum OverlapResolution {

		/** Lines are split at each junction, and not merged again. */
		NONE,

		/**
		 * Line fragments meeting at junctions are merged so that the merged lines
		 * are as straight as possible, assuming that a straight crossing is more
		 * likely than two touching turns.
		 */
		SLOPE
}
