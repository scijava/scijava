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
package org.scijava.ops.image.segment.hough;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.algorithm.localextrema.SubpixelLocalization;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;

/**
 * Detects circles (or n-dimensional spheres) as peaks in a Hough vote image,
 * as produced by {@code transform.houghCircle}.
 * <p>
 * The last dimension of the vote image indexes the radii, so that index
 * {@code i} corresponds to radius {@code minRadius + i * stepRadius}. The
 * algorithm proceeds as follows:
 * <ol>
 * <li>Find the local maxima of the vote image over its full (n+1)-dimensional
 * 3×3×...×3 neighborhood, discarding those with fewer votes than
 * {@code S(minRadius) / sensitivity}, where {@code S(r)} is the surface area of
 * the n-sphere of radius {@code r} ({@code 2πr} in 2D).</li>
 * <li>Assign each maximum the sensitivity {@code S(r) / votes}, where
 * {@code r} is its radius.</li>
 * <li>Non-maxima suppression: visiting maxima by ascending sensitivity, discard
 * every maximum whose center lies inside an already retained circle.</li>
 * <li>Refine the position and radius of the retained maxima to subpixel
 * accuracy by quadratic fitting, recompute their sensitivity, and discard those
 * whose sensitivity exceeds the given one.</li>
 * </ol>
 * <p>
 * Ported from {@code HoughCircleLocalMaxDetectorOp} of CircleSkinner by
 * Jean-Yves Tinevez
 * (<a href="https://github.com/tinevez/CircleSkinner">CircleSkinner</a>,
 * BSD-2-Clause), generalized to n dimensions. The original sensitivity is
 * {@code 2πr * circleThickness / votes}; this op corresponds to a circle
 * thickness of 1 (divide the sensitivity by the thickness to reproduce the
 * original threshold).
 * <p>
 * The original CircleSkinner license notice follows, with the copyright line
 * as it appears in the CircleSkinner sources.
 * </p>
 *
 * <pre>
 * Copyright (C) 2016 - 2022 My Company, Inc.
 *
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
 * </pre>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Jean-Yves Tinevez
 * @param <T> the type of the vote image
 * @implNote op names='segment.detectHoughCircles'
 */
public class HoughCircleDetector<T extends RealType<T>> implements
	Functions.Arity4<RandomAccessibleInterval<T>, Integer, Integer, Double, List<HoughCircle>>
{

	/**
	 * Detects circles in a Hough vote image.
	 *
	 * @param votes the (n+1)-dimensional vote image, whose last dimension indexes
	 *          the tested radii; NaN votes are ignored
	 * @param minRadius the radius corresponding to the first index of the last
	 *          dimension of the vote image
	 * @param stepRadius the radius increment between successive indices of the
	 *          last dimension of the vote image (defaults to 1)
	 * @param sensitivity the maximal sensitivity of the reported circles; larger
	 *          values detect fainter or more incomplete circles (defaults to 20)
	 * @return the detected circles, sorted by ascending sensitivity (strongest
	 *         first)
	 */
	@Override
	public List<HoughCircle> apply( //
		final RandomAccessibleInterval<T> votes, //
		final Integer minRadius, //
		@Nullable Integer stepRadius, //
		@Nullable Double sensitivity //
	) {
		if (stepRadius == null) stepRadius = 1;
		if (sensitivity == null) sensitivity = 20.;
		final var nDims = votes.numDimensions();
		if (nDims < 2) throw new IllegalArgumentException(
			"Vote image must have at least 2 dimensions: " + nDims);
		if (sensitivity <= 0) throw new IllegalArgumentException(
			"Sensitivity must be positive: " + sensitivity);
		final var n = nDims - 1;
		final var radiusMin = votes.min(n);

		// Find local maxima above the threshold.
		final var threshold = sphereSurface(n, minRadius) / sensitivity;
		final List<Candidate> peaks = new ArrayList<>();
		final var neighborhoods = new RectangleShape(1, true)
			.neighborhoodsRandomAccessible(Views.extendBorder(votes))
			.randomAccess();
		final var cursor = Views.iterable(votes).localizingCursor();
		CANDIDATE: while (cursor.hasNext()) {
			final var value = cursor.next().getRealDouble();
			// Note: NaN votes are never circles.
			if (!(value >= threshold)) continue;
			neighborhoods.setPosition(cursor);
			for (final var neighbor : neighborhoods.get()) {
				if (neighbor.getRealDouble() > value) continue CANDIDATE;
			}
			final var radius = radiusOf(cursor.getDoublePosition(n) - radiusMin,
				minRadius, stepRadius);
			peaks.add(new Candidate(cursor, radius, sphereSurface(n, radius) /
				value));
		}

		// Non-maxima suppression: discard centers inside stronger circles.
		Collections.sort(peaks);
		final List<Candidate> retained = new ArrayList<>();
		NEXT: for (final var tested : peaks) {
			for (final var kept : retained) {
				if (kept.contains(tested)) continue NEXT;
			}
			retained.add(tested);
		}

		// Refine to subpixel accuracy.
		final var spl = new SubpixelLocalization<Candidate, T>(nDims);
		spl.setAllowMaximaTolerance(true);
		spl.setMaxNumMoves(10);
		spl.setNumThreads(1);
		final List<RefinedPeak<Candidate>> refined = spl.process(retained, Views
			.extendBorder(votes), votes);

		final List<HoughCircle> circles = new ArrayList<>(refined.size());
		for (final var peak : refined) {
			final var radius = radiusOf(peak.getDoublePosition(n) - radiusMin,
				minRadius, stepRadius);
			final var ls = sphereSurface(n, radius) / peak.getValue();
			if (ls <= 0 || ls > sensitivity) continue;
			final var center = new RealPoint(n);
			for (var d = 0; d < n; d++)
				center.setPosition(peak.getDoublePosition(d), d);
			circles.add(new HoughCircle(center, radius, ls));
		}
		Collections.sort(circles);
		return circles;
	}

	private static double radiusOf(final double index, final int minRadius,
		final int stepRadius)
	{
		return minRadius + index * stepRadius;
	}

	/**
	 * Computes the surface area of the n-sphere of the given radius, that is,
	 * {@code 2 π^(n/2) r^(n-1) / Γ(n/2)}.
	 */
	private static double sphereSurface(final int n, final double radius) {
		// Note: Γ(n/2) by recurrence from Γ(1) = 1 or Γ(1/2) = √π.
		var gamma = n % 2 == 0 ? 1.0 : Math.sqrt(Math.PI);
		for (var x = n % 2 == 0 ? 1.0 : 0.5; x < n / 2.0; x++)
			gamma *= x;
		return 2 * Math.pow(Math.PI, n / 2.0) * Math.pow(radius, n - 1) / gamma;
	}

	/** A candidate circle on the integer grid of the vote image. */
	private static final class Candidate extends Point implements
		Comparable<Candidate>
	{

		private final double radius;

		private final double sensitivity;

		Candidate(final Localizable pos, final double radius,
			final double sensitivity)
		{
			super(pos);
			this.radius = radius;
			this.sensitivity = sensitivity;
		}

		/** Tests whether the spatial position of another candidate is inside. */
		boolean contains(final Candidate other) {
			var dr2 = 0.0;
			for (var d = 0; d < numDimensions() - 1; d++) {
				final var dx = getDoublePosition(d) - other.getDoublePosition(d);
				dr2 += dx * dx;
			}
			return dr2 <= radius * radius;
		}

		@Override
		public int compareTo(final Candidate o) {
			return Double.compare(sensitivity, o.sensitivity);
		}
	}
}
