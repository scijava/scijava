/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.image.labeling.nearestneighbor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import net.imglib2.KDTree;
import net.imglib2.RealLocalizable;
import net.imglib2.neighborsearch.KNearestNeighborSearchOnKDTree;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.Intervals;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.special.Gamma;
import org.scijava.ops.spi.Nullable;

/**
 * Ops analyzing the spatial distribution of labeled objects via the distances
 * between their centroids and those of their nearest neighbors.
 * <p>
 * Inspired by the {@code Particle_Distribution2D} and
 * {@code Distribution_Analysis} plugins of the BioVoxxel Toolbox by Jan
 * Brocher / BioVoxxel (BSD 3-Clause license, Copyright (C) Jan Brocher /
 * BioVoxxel). The statistics here follow Clark and Evans (1954) directly,
 * generalized to n dimensions via the nearest-neighbor distance distribution
 * of a homogeneous Poisson process, instead of the plugins' tabulated F/t
 * tests.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Jan Brocher (BioVoxxel)
 * @see <a href="https://github.com/biovoxxel/BioVoxxel-Toolbox">BioVoxxel
 *      Toolbox</a>
 */
public final class NearestNeighbors {

	private NearestNeighbors() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes, for each label, the Euclidean distance (in pixels) from its
	 * centroid to the nearest centroid of any other label.
	 *
	 * @param labeling the labeled objects
	 * @return a map from each label to its nearest-neighbor distance (positive
	 *         infinity if there is only one label)
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.nearestNeighborDistances', type=Function
	 */
	public static <L, I extends IntegerType<I>> Map<L, Double> distances(
		final ImgLabeling<L, I> labeling)
	{
		final var labels = new ArrayList<L>();
		final var centroids = new ArrayList<RealLocalizable>();
		for (final var region : new LabelRegions<>(labeling)) {
			if (region.inside().size() == 0) continue;
			labels.add(region.getLabel());
			centroids.add(region.getCenterOfMass());
		}
		final var result = new LinkedHashMap<L, Double>();
		if (labels.size() < 2) {
			for (final var label : labels)
				result.put(label, Double.POSITIVE_INFINITY);
			return result;
		}
		final var tree = new KDTree<>(labels, centroids);
		// The nearest point is the query point itself; take the second nearest.
		final var search = new KNearestNeighborSearchOnKDTree<>(tree, 2);
		for (var i = 0; i < labels.size(); i++) {
			search.search(centroids.get(i));
			result.put(labels.get(i), search.getDistance(1));
		}
		return result;
	}

	/**
	 * Computes the Clark&ndash;Evans aggregation index of the labeled objects'
	 * centroids, with its significance test against complete spatial
	 * randomness.
	 * <p>
	 * The index is {@code R = r_obs / r_exp}, the ratio of the observed mean
	 * nearest-neighbor distance to the one expected for a homogeneous Poisson
	 * process of the same density {@code λ = n / V}. In {@code d} dimensions,
	 * with {@code c_d} the volume of the unit ball, the expected distance is
	 * {@code Γ(1 + 1/d) (λ c_d)^(-1/d)} and its variance is
	 * {@code (Γ(1 + 2/d) - Γ(1 + 1/d)²) (λ c_d)^(-2/d)}; in 2D these reduce to
	 * Clark and Evans' {@code 0.5 / sqrt(λ)} and standard error
	 * {@code 0.26136 / sqrt(n λ)}. {@code R < 1} indicates clustering,
	 * {@code R ≈ 1} randomness and {@code R > 1} regularity (at most 2.1491 in
	 * 2D, for a hexagonal lattice). No edge correction is applied, which biases
	 * {@code R} slightly upwards for objects near the border.
	 * </p>
	 * <p>
	 * Reference: P. J. Clark and F. C. Evans, "Distance to nearest neighbor as a
	 * measure of spatial relationships in populations", <i>Ecology</i> 35(4):
	 * 445&ndash;453, 1954.
	 * </p>
	 *
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @param labeling the labeled objects (at least two)
	 * @param volume the area or volume (in pixels) of the region the objects
	 *          were sampled from (default: the size of the interval of the
	 *          labeling)
	 * @return a map with the entries {@code "n"} (number of objects),
	 *         {@code "observedMeanDistance"}, {@code "expectedMeanDistance"},
	 *         {@code "R"}, {@code "standardError"}, {@code "z"} (the z-score of
	 *         the observed mean distance) and {@code "p"} (the two-sided p-value
	 *         of the z-score)
	 * @implNote op names='labeling.clarkEvans', type=Function
	 */
	public static <L, I extends IntegerType<I>> Map<String, Double> clarkEvans(
		final ImgLabeling<L, I> labeling, @Nullable Double volume)
	{
		if (volume == null) volume = (double) Intervals.numElements(labeling);
		final var distances = distances(labeling).values();
		final var n = distances.size();
		if (n < 2) {
			throw new IllegalArgumentException(
				"At least two objects are required, but there are " + n);
		}
		double sum = 0;
		for (final var distance : distances)
			sum += distance;
		final var observed = sum / n;

		final var d = labeling.numDimensions();
		final var unitBall = Math.pow(Math.PI, d / 2.0) / Gamma.gamma(d / 2.0 +
			1);
		final var scale = Math.pow(n / volume * unitBall, -1.0 / d);
		final var g1 = Gamma.gamma(1 + 1.0 / d);
		final var g2 = Gamma.gamma(1 + 2.0 / d);
		final var expected = g1 * scale;
		final var standardError = Math.sqrt((g2 - g1 * g1) / n) * scale;
		final var z = (observed - expected) / standardError;
		final var p = 2 * new NormalDistribution(null, 0, 1).cumulativeProbability(
			-Math.abs(z));

		final var result = new LinkedHashMap<String, Double>();
		result.put("n", (double) n);
		result.put("observedMeanDistance", observed);
		result.put("expectedMeanDistance", expected);
		result.put("R", observed / expected);
		result.put("standardError", standardError);
		result.put("z", z);
		result.put("p", p);
		return result;
	}
}
