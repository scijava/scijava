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

import java.util.Collections;
import java.util.List;

import net.imglib2.Localizable;

/**
 * An immutable vertex of a {@link SkeletonGraph}: an end point, a junction
 * (a cluster of adjacent junction voxels), or the starting voxel of a ring.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Ignacio Arganda-Carreras
 */
public final class SkeletonVertex {

	private final List<Localizable> points;

	SkeletonVertex(final List<Localizable> points) {
		this.points = Collections.unmodifiableList(points);
	}

	/**
	 * Gets the voxels of this vertex, in image coordinates.
	 *
	 * @return the voxels; the first one is the representative position used
	 *         for distances
	 */
	public List<Localizable> points() {
		return points;
	}

	@Override
	public String toString() {
		return "SkeletonVertex" + points;
	}
}
