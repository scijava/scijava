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

package org.scijava.ops.image.labeling.measure;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.IntegerType;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.scijava.ops.spi.Nullable;

/**
 * The equivalent ellipsoid of each labeled region: the solid ellipsoid with
 * the same centroid and second-order central moments as the region, in any
 * number of dimensions.
 * <p>
 * Each pixel counts as a solid box of the size of the pixel spacing, so that
 * the covariance matrix of the region is that of its pixel centers plus
 * {@code s_d² / 12} on the diagonal (the variance of a uniform distribution
 * over one pixel). A solid ellipsoid in {@code n} dimensions with semi-axis
 * length {@code a} has variance {@code a² / (n + 2)} along that axis, so the
 * radii are {@code sqrt((n + 2) λ_k)} for the eigenvalues {@code λ_k} of the
 * covariance matrix, along the corresponding eigenvectors (see e.g. R. J.
 * Prokop and A. P. Reeves, "A survey of moment-based techniques for
 * unoccluded object representation and recognition", <i>CVGIP: Graphical
 * Models and Image Processing</i> 54(5): 438&ndash;460, 1992). In 2D this is
 * the familiar moment-based equivalent ellipse.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class EquivalentEllipsoids {

	private EquivalentEllipsoids() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes the equivalent ellipsoid of each label. Centers are in
	 * calibrated coordinates (the pixel position times the spacing, measured
	 * between pixel centers), radii in the units of the spacing, in decreasing
	 * order. Each axis direction is a unit vector whose first non-zero
	 * component is positive; in 2D, the major axis direction is thus
	 * {@code (cos θ, sin θ)} with the angle {@code θ} in (-90°, 90°].
	 *
	 * @param labeling the labeled objects
	 * @param spacing the pixel spacing along each dimension (default 1)
	 * @return a map from each label to its equivalent ellipsoid
	 * @param <L> label type
	 * @param <I> index type of the labeling
	 * @implNote op names='labeling.equivalentEllipsoid', type=Function
	 */
	public static <L, I extends IntegerType<I>> Map<L, EquivalentEllipsoid>
		equivalentEllipsoids(final ImgLabeling<L, I> labeling,
			@Nullable double[] spacing)
	{
		final var n = labeling.numDimensions();
		final var s = spacing == null ? InscribedBalls.ones(n) : spacing;
		if (s.length != n) {
			throw new IllegalArgumentException("Expected " + n +
				" spacing values but got " + s.length);
		}
		final var result = new LinkedHashMap<L, EquivalentEllipsoid>();
		final var pos = new double[n];
		for (final var region : new LabelRegions<>(labeling)) {
			final var count = region.inside().size();
			if (count == 0) continue;
			final var center = new double[n];
			var cursor = region.inside().localizingCursor();
			while (cursor.hasNext()) {
				cursor.fwd();
				for (var d = 0; d < n; d++)
					center[d] += cursor.getDoublePosition(d) * s[d];
			}
			for (var d = 0; d < n; d++)
				center[d] /= count;
			final var cov = new double[n][n];
			cursor = region.inside().localizingCursor();
			while (cursor.hasNext()) {
				cursor.fwd();
				for (var d = 0; d < n; d++)
					pos[d] = cursor.getDoublePosition(d) * s[d] - center[d];
				for (var i = 0; i < n; i++)
					for (var j = 0; j <= i; j++)
						cov[i][j] += pos[i] * pos[j];
			}
			for (var i = 0; i < n; i++) {
				for (var j = 0; j <= i; j++) {
					cov[i][j] /= count;
					cov[j][i] = cov[i][j];
				}
				cov[i][i] += s[i] * s[i] / 12;
			}
			result.put(region.getLabel(), ellipsoid(center, cov));
		}
		return result;
	}

	private static EquivalentEllipsoid ellipsoid(final double[] center,
		final double[][] cov)
	{
		final var n = center.length;
		final var eigen = new EigenDecomposition(MatrixUtils.createRealMatrix(
			cov));
		final var values = eigen.getRealEigenvalues();
		final var order = new Integer[n];
		for (var k = 0; k < n; k++)
			order[k] = k;
		Arrays.sort(order, (a, b) -> Double.compare(values[b], values[a]));
		final var radii = new double[n];
		final var axes = new double[n][];
		for (var k = 0; k < n; k++) {
			radii[k] = Math.sqrt((n + 2) * Math.max(0, values[order[k]]));
			final var v = eigen.getEigenvector(order[k]).unitVector().toArray();
			var first = 0;
			while (first < n - 1 && Math.abs(v[first]) < 1e-12)
				first++;
			if (v[first] < 0) for (var d = 0; d < n; d++)
				v[d] = -v[d];
			axes[k] = v;
		}
		return new EquivalentEllipsoid(center, radii, axes);
	}
}
