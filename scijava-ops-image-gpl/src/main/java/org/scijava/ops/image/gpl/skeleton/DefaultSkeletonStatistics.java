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

import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;

/**
 * Measures the skeletons (trees) of a 2D or 3D skeleton image: for each tree,
 * the number of branches, junctions, end point, junction and slab voxels,
 * triple and quadruple points, the average and maximum branch length, and
 * optionally the longest shortest path between two of its vertices (computed
 * with the Floyd-Warshall algorithm). The result has one
 * {@link SkeletonStatistics} row per tree, like the Results table of the Fiji
 * AnalyzeSkeleton plugin; each row also holds the tree graph, whose edges
 * provide the per-branch information.
 * <p>
 * This is a translation of the Fiji
 * <a href="https://github.com/fiji/AnalyzeSkeleton">AnalyzeSkeleton</a> plugin
 * by Ignacio Arganda-Carreras (GPL v3+), with the longest shortest path code
 * by Huub Hovens, without its display parts. See {@code topology.skeletonGraph}
 * for the graph construction, the pruning options and the deviations from the
 * plugin.
 * </p>
 * <p>
 * Reference: I. Arganda-Carreras, R. Fernández-González, A. Muñoz-Barrutia and
 * C. Ortiz-de-Solórzano, "3D reconstruction of histological sections:
 * Application to mammary gland tissue", Microscopy Research and Technique
 * 73(11):1019-1029, 2010.
 * </p>
 *
 * @author Ignacio Arganda-Carreras
 * @author Huub Hovens
 * @author Curtis Rueden
 * @param <B> type of the skeleton image
 * @param <T> type of the intensity image
 * @implNote op names='topology.skeletonStatistics'
 */
public class DefaultSkeletonStatistics<B extends BooleanType<B>, T extends RealType<T>>
	implements
	Functions.Arity6<RandomAccessibleInterval<B>, double[], CyclePruning, Double, RandomAccessibleInterval<T>, Boolean, List<SkeletonStatistics>>
{

	/**
	 * Measures the skeletons.
	 *
	 * @param skeleton the 2D or 3D skeleton image, e.g. the output of
	 *          {@code morphology.skeletonize3D}
	 * @param calibration the element spacing along each dimension (defaults to 1
	 *          for every dimension); lengths are reported in these units
	 * @param cyclePruning how to break the cycles of the skeleton (defaults to
	 *          {@link CyclePruning#NONE})
	 * @param pruneEnds the maximum calibrated length of the end branches to
	 *          remove (by default no end branches are removed). With
	 *          {@code Double.POSITIVE_INFINITY}, all branches ending in an end
	 *          point are removed, like the Prune ends option of the plugin.
	 * @param intensity an intensity image with the same dimensions as the
	 *          skeleton, used for the intensity-based cycle pruning modes and
	 *          for the branch intensities (by default branch intensities are
	 *          NaN)
	 * @param longestShortestPath whether to compute the longest shortest path
	 *          of each tree (default false, in which case its length is NaN)
	 * @return the statistics of each skeleton (tree)
	 */
	@Override
	public List<SkeletonStatistics> apply( //
		final RandomAccessibleInterval<B> skeleton, //
		@Nullable final double[] calibration, //
		@Nullable final CyclePruning cyclePruning, //
		@Nullable final Double pruneEnds, //
		@Nullable final RandomAccessibleInterval<T> intensity, //
		@Nullable final Boolean longestShortestPath //
	) {
		return SkeletonAnalyzer.analyze(skeleton, calibration, cyclePruning,
			pruneEnds, intensity, Boolean.TRUE.equals(longestShortestPath))
			.statistics();
	}
}
