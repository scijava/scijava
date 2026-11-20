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
 * Builds the graphs of the skeletons (trees) of a 2D or 3D skeleton image.
 * <p>
 * Skeleton voxels are classified by their number of 26-connected neighbors
 * into end points, slabs and junctions (see {@code morphology.tagSkeleton}).
 * Each connected skeleton becomes a {@link SkeletonGraph}, whose vertices are
 * end points and junctions (adjacent junction voxels are merged into one
 * vertex), and whose edges are the branches between them, with their slab
 * voxels and calibrated lengths. Optionally, end branches are pruned and
 * cycles are broken first, after which the skeleton is analyzed again.
 * </p>
 * <p>
 * This is a translation of the Fiji
 * <a href="https://github.com/fiji/AnalyzeSkeleton">AnalyzeSkeleton</a> plugin
 * by Ignacio Arganda-Carreras (GPL v3+), without its display parts. Results
 * match the plugin, including its visit order of neighbor voxels (which
 * determines how branches around junction clusters are traced), its ring
 * lengths (which omit the closing step) and its maximum branch length (which
 * ignores one-voxel branches next to a junction). Deviations:
 * </p>
 * <ul>
 * <li>Branch intensities are measured in the given intensity image, whereas
 * the plugin averages the skeleton image values.</li>
 * <li>When end pruning removes the root vertex of a tree, another vertex
 * becomes the root, so that cycle pruning still finds the cycles of that tree
 * (the plugin then silently prunes none).</li>
 * <li>An end point whose only branch loops back to itself is pruned without
 * error (the plugin fails).</li>
 * </ul>
 * <p>
 * Reference: I. Arganda-Carreras, R. Fernández-González, A. Muñoz-Barrutia and
 * C. Ortiz-de-Solórzano, "3D reconstruction of histological sections:
 * Application to mammary gland tissue", Microscopy Research and Technique
 * 73(11):1019-1029, 2010.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Ignacio Arganda-Carreras
 * @param <B> type of the skeleton image
 * @param <T> type of the intensity image
 * @implNote op names='topology.skeletonGraph'
 */
public class DefaultSkeletonGraph<B extends BooleanType<B>, T extends RealType<T>>
	implements
	Functions.Arity5<RandomAccessibleInterval<B>, double[], CyclePruning, Double, RandomAccessibleInterval<T>, List<SkeletonGraph>>
{

	/**
	 * Builds the skeleton graphs.
	 *
	 * @param skeleton the 2D or 3D skeleton image, e.g. the output of
	 *          {@code morphology.skeletonize}
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
	 * @return one graph per skeleton (tree)
	 */
	@Override
	public List<SkeletonGraph> apply( //
		final RandomAccessibleInterval<B> skeleton, //
		@Nullable final double[] calibration, //
		@Nullable final CyclePruning cyclePruning, //
		@Nullable final Double pruneEnds, //
		@Nullable final RandomAccessibleInterval<T> intensity //
	) {
		return SkeletonAnalyzer.analyze(skeleton, calibration, cyclePruning,
			pruneEnds, intensity, false).graphs();
	}
}
