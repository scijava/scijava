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

package org.scijava.ops.image.gpl.skeleton;

/**
 * Methods for breaking the cycles (loops) of a skeleton before analysis, as in
 * the Fiji AnalyzeSkeleton plugin. A ring without junctions is always broken
 * by removing one of its voxels.
 *
 * @author Ignacio Arganda-Carreras
 * @author Curtis Rueden
 */
public enum CyclePruning {

		/** Keep all cycles. */
		NONE,

		/** Cut each cycle in the middle of its shortest branch. */
		SHORTEST_BRANCH,

		/**
		 * Cut each cycle at its slab voxel with the lowest average intensity in its
		 * neighborhood. Requires an intensity image.
		 */
		LOWEST_INTENSITY_VOXEL,

		/**
		 * Cut each cycle at the darkest slab voxel of its branch with the lowest
		 * average intensity. Requires an intensity image.
		 */
		LOWEST_INTENSITY_BRANCH
}
