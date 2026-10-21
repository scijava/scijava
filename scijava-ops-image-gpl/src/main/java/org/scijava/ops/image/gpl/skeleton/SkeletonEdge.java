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
 * An immutable edge (branch) of a {@link SkeletonGraph}, connecting two
 * vertices (the same vertex for a loop) through a path of slab voxels. The
 * properties correspond to the columns of the "Branch information" table of
 * the Fiji AnalyzeSkeleton plugin.
 *
 * @author Ignacio Arganda-Carreras
 * @author Curtis Rueden
 */
public final class SkeletonEdge {

	private final SkeletonVertex v1;
	private final SkeletonVertex v2;
	private final List<Localizable> slabs;
	private final double length;
	private final double runningAverageLength;
	private final double euclideanDistance;
	private final double averageIntensity;
	private final double averageIntensityInnerThird;

	SkeletonEdge(final SkeletonVertex v1, final SkeletonVertex v2,
		final List<Localizable> slabs, final double length,
		final double runningAverageLength, final double euclideanDistance,
		final double averageIntensity, final double averageIntensityInnerThird)
	{
		this.v1 = v1;
		this.v2 = v2;
		this.slabs = Collections.unmodifiableList(slabs);
		this.length = length;
		this.runningAverageLength = runningAverageLength;
		this.euclideanDistance = euclideanDistance;
		this.averageIntensity = averageIntensity;
		this.averageIntensityInnerThird = averageIntensityInnerThird;
	}

	/** @return the first vertex */
	public SkeletonVertex v1() {
		return v1;
	}

	/** @return the second vertex */
	public SkeletonVertex v2() {
		return v2;
	}

	/**
	 * Gets the vertex at the other end of this edge.
	 *
	 * @param v one of the vertices of this edge
	 * @return the opposite vertex, or {@code null} if {@code v} is not a vertex
	 *         of this edge
	 */
	public SkeletonVertex opposite(final SkeletonVertex v) {
		if (v == v1) return v2;
		if (v == v2) return v1;
		return null;
	}

	/** @return the slab voxels of this edge, in image coordinates */
	public List<Localizable> slabs() {
		return slabs;
	}

	/**
	 * @return the calibrated length of this branch, measured along its voxels
	 *         from the first point of one vertex to the first point of the
	 *         other
	 */
	public double length() {
		return length;
	}

	/**
	 * @return the calibrated branch length smoothed with a running average
	 *         over up to five voxels, which reduces the overestimation of
	 *         diagonal staircase paths
	 */
	public double runningAverageLength() {
		return runningAverageLength;
	}

	/**
	 * @return the calibrated straight-line distance between the first points
	 *         of the two vertices
	 */
	public double euclideanDistance() {
		return euclideanDistance;
	}

	/**
	 * @return the average intensity image value along the branch, or NaN when
	 *         no intensity image was given
	 */
	public double averageIntensity() {
		return averageIntensity;
	}

	/**
	 * @return the average intensity image value along the inner third of the
	 *         branch, or NaN when no intensity image was given
	 */
	public double averageIntensityInnerThird() {
		return averageIntensityInnerThird;
	}

	@Override
	public String toString() {
		return "SkeletonEdge[" + v1.points().get(0) + " - " + v2.points().get(0) +
			", length=" + length + "]";
	}
}
