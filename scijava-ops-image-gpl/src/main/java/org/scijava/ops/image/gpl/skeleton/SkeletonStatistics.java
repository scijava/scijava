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
 * Immutable statistics of one skeleton (tree): one row of the "Results" table
 * of the Fiji AnalyzeSkeleton plugin.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Ignacio Arganda-Carreras
 */
public final class SkeletonStatistics {

	private final SkeletonGraph graph;
	private final int branches;
	private final int junctions;
	private final int endPointVoxels;
	private final int junctionVoxels;
	private final int slabVoxels;
	private final double averageBranchLength;
	private final int triplePoints;
	private final int quadruplePoints;
	private final double maximumBranchLength;
	private final double longestShortestPath;
	private final List<Localizable> longestShortestPathPoints;

	SkeletonStatistics(final SkeletonGraph graph, final int branches,
		final int junctions, final int endPointVoxels, final int junctionVoxels,
		final int slabVoxels, final double averageBranchLength,
		final int triplePoints, final int quadruplePoints,
		final double maximumBranchLength, final double longestShortestPath,
		final List<Localizable> longestShortestPathPoints)
	{
		this.graph = graph;
		this.branches = branches;
		this.junctions = junctions;
		this.endPointVoxels = endPointVoxels;
		this.junctionVoxels = junctionVoxels;
		this.slabVoxels = slabVoxels;
		this.averageBranchLength = averageBranchLength;
		this.triplePoints = triplePoints;
		this.quadruplePoints = quadruplePoints;
		this.maximumBranchLength = maximumBranchLength;
		this.longestShortestPath = longestShortestPath;
		this.longestShortestPathPoints = Collections.unmodifiableList(
			longestShortestPathPoints);
	}

	/** @return the graph of this skeleton */
	public SkeletonGraph graph() {
		return graph;
	}

	/** @return the number of branches */
	public int branches() {
		return branches;
	}

	/** @return the number of junctions (clusters of adjacent junction voxels) */
	public int junctions() {
		return junctions;
	}

	/** @return the number of end point voxels */
	public int endPointVoxels() {
		return endPointVoxels;
	}

	/** @return the number of junction voxels */
	public int junctionVoxels() {
		return junctionVoxels;
	}

	/** @return the number of slab voxels */
	public int slabVoxels() {
		return slabVoxels;
	}

	/** @return the average calibrated branch length (0 without branches) */
	public double averageBranchLength() {
		return averageBranchLength;
	}

	/** @return the number of junctions with exactly three branches */
	public int triplePoints() {
		return triplePoints;
	}

	/** @return the number of junctions with exactly four branches */
	public int quadruplePoints() {
		return quadruplePoints;
	}

	/** @return the maximum calibrated branch length */
	public double maximumBranchLength() {
		return maximumBranchLength;
	}

	/**
	 * @return the calibrated length of the longest shortest path between two
	 *         vertices of the graph, or NaN if it was not computed
	 */
	public double longestShortestPath() {
		return longestShortestPath;
	}

	/**
	 * @return the voxels of the longest shortest path, in image coordinates
	 *         (empty if it was not computed); the first one is its start
	 */
	public List<Localizable> longestShortestPathPoints() {
		return longestShortestPathPoints;
	}
}
