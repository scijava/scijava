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

package org.scijava.ops.image.filter.kuwahara;

import java.util.Locale;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * The linear Kuwahara filter, shared by the {@code filter.kuwaharaLinear} ops.
 * <p>
 * The sub-windows of the classic Kuwahara filter are replaced by straight line
 * segments of {@code lineLength} pixels in the plane of dimensions 0 and 1. For
 * each pixel and each candidate orientation, the segments of that orientation
 * which contain the pixel are those whose centres lie on the line through the
 * pixel, at most {@code (lineLength - 1) / 2} pixels away. The output is the
 * mean of the candidate segment that minimizes the selection criterion. As in
 * a digital (Bresenham) line, consecutive samples of a segment are one pixel
 * apart along its major axis, and values between pixels along the minor axis
 * are linearly interpolated; offsets along a line are measured in the same
 * steps.
 * </p>
 * <p>
 * Implemented from the description of generalized, orientation-adaptive
 * Kuwahara filtering by P. Bakker, L. J. van Vliet and P. W. Verbeek, "Edge
 * preserving orientation adaptive filtering", Proc. IEEE CVPR 1999, pp.
 * 535-540.
 * </p>
 * <p>
 * Results are clamped to the range of the output type, so an integer output
 * saturates rather than wrapping around.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class LinearKuwahara {

	private LinearKuwahara() {
		// NB: Prevent instantiation of utility class.
	}

	/** Criteria for choosing among candidate line segments. */
	public enum Criterion {

			/** The variance of the segment. */
			VARIANCE("variance"),

			/**
			 * The variance divided by the mean, suited to Poisson (shot) noise in
			 * non-negative images.
			 */
			VARIANCE_OVER_MEAN("varianceovermean", "poisson"),

			/**
			 * The variance divided by the squared mean (the squared coefficient of
			 * variation), suited to multiplicative noise in non-negative images.
			 */
			VARIANCE_OVER_MEAN_SQUARED("varianceovermeansquared", "cv",
				"coefficientofvariation");

		/** Smallest denominator, to avoid dividing by zero. */
		private static final double TINY = 1e-12;

		private final String[] names;

		Criterion(final String... names) {
			this.names = names;
		}

		/**
		 * Looks up a criterion by name, ignoring case, spaces, dashes and
		 * underscores.
		 *
		 * @param name {@code variance}, {@code varianceOverMean} (or
		 *          {@code poisson}) or {@code varianceOverMeanSquared} (or
		 *          {@code cv}); null for {@code variance}
		 * @return the criterion
		 */
		public static Criterion byName(final String name) {
			if (name == null) return VARIANCE;
			final var key = name.toLowerCase(Locale.ROOT).replaceAll("[\\s_\\-]",
				"");
			for (final var c : values()) {
				for (final var n : c.names) {
					if (n.equals(key)) return c;
				}
			}
			throw new IllegalArgumentException("Unknown criterion: " + name);
		}

		double evaluate(final double mean, final double variance) {
			switch (this) {
				case VARIANCE:
					return variance;
				case VARIANCE_OVER_MEAN:
					return variance / Math.max(mean, TINY);
				default:
					return variance / Math.max(mean * mean, TINY);
			}
		}
	}

	/**
	 * Applies the linear Kuwahara filter.
	 *
	 * @param input the image to filter (at least 2D)
	 * @param angles the candidate line orientations shared by all pixels, in
	 *          radians counterclockwise from the x axis as displayed (y up); or
	 *          null to use {@code orientation}
	 * @param orientation the line orientation of each pixel, in the same
	 *          convention, with the same dimensions as the input (used only if
	 *          {@code angles} is null)
	 * @param lineLength the (odd) length of the line segments, in pixels
	 * @param criterion the selection criterion
	 * @param outOfBoundsFactory the strategy for values beyond the image border
	 *          (null for mirroring)
	 * @param output the filtered image
	 */
	static <T extends RealType<T>, R extends RealType<R>, V extends RealType<V>>
		void filter(final RandomAccessibleInterval<T> input, final double[] angles,
			final RandomAccessibleInterval<R> orientation, final int lineLength,
			final Criterion criterion,
		OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final RandomAccessibleInterval<V> output)
	{
		if (input.numDimensions() < 2) {
			throw new IllegalArgumentException("Input must be at least 2D");
		}
		if (lineLength < 3 || lineLength % 2 == 0) {
			throw new IllegalArgumentException(
				"Line length must be odd and at least 3: " + lineLength);
		}
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output must have the same dimensions");
		}
		if (angles == null && !Intervals.equalDimensions(input, orientation)) {
			throw new IllegalArgumentException(
				"Input and orientation must have the same dimensions");
		}
		if (outOfBoundsFactory == null) outOfBoundsFactory =
			new OutOfBoundsMirrorFactory<>(OutOfBoundsMirrorFactory.Boundary.SINGLE);

		final var n = input.numDimensions();
		final var r = (lineLength - 1) / 2;
		// Samples at offsets -2r..2r along the line cover every segment of
		// length 2r + 1 that contains the pixel.
		final var nSamples = 4 * r + 1;
		final var samples = new double[nSamples];
		final RealRandomAccess<T> interp = Views.interpolate(Views.extend(input,
			outOfBoundsFactory), new NLinearInterpolatorFactory<T>())
			.realRandomAccess();

		final var inMin = input.minAsLongArray();
		final var outMin = output.minAsLongArray();
		final var pos = new double[n];
		final var cursor = Views.flatIterable(output).localizingCursor();
		final var orientationCursor = angles == null ? Views.flatIterable(
			orientation).cursor() : null;
		final var theta = angles == null ? new double[1] : angles;
		while (cursor.hasNext()) {
			cursor.fwd();
			if (orientationCursor != null) {
				theta[0] = orientationCursor.next().getRealDouble();
			}
			var bestCrit = Double.POSITIVE_INFINITY;
			var bestMean = 0.0;
			for (final var angle : theta) {
				// Note: Image y increases downwards, so it is opposite to the
				// displayed y axis of the angle convention.
				final var cos = Math.cos(angle);
				final var sin = Math.sin(angle);
				// Note: Unit steps along the major axis, as in a digital line, keep
				// diagonal samples on pixel centres.
				final var major = Math.max(Math.abs(cos), Math.abs(sin));
				final var ux = cos / major;
				final var uy = -sin / major;
				for (var d = 0; d < n; d++)
					pos[d] = cursor.getLongPosition(d) - outMin[d] + inMin[d];
				final var x0 = pos[0];
				final var y0 = pos[1];
				for (var i = 0; i < nSamples; i++) {
					final var t = i - 2 * r;
					pos[0] = x0 + t * ux;
					pos[1] = y0 + t * uy;
					interp.setPosition(pos);
					samples[i] = interp.get().getRealDouble();
				}
				// Sliding sums over the windows [s, s + 2r].
				var sum = 0.0;
				var sumSq = 0.0;
				for (var i = 0; i < lineLength; i++) {
					sum += samples[i];
					sumSq += samples[i] * samples[i];
				}
				for (var s = 0; s <= 2 * r; s++) {
					if (s > 0) {
						final var out = samples[s - 1];
						final var in = samples[s + 2 * r];
						sum += in - out;
						sumSq += in * in - out * out;
					}
					final var mean = sum / lineLength;
					final var variance = Math.max(0, sumSq / lineLength - mean * mean);
					final var crit = criterion.evaluate(mean, variance);
					if (crit < bestCrit) {
						bestCrit = crit;
						bestMean = mean;
					}
				}
			}
			final var o = cursor.get();
			o.setReal(Math.max(o.getMinValue(), Math.min(o.getMaxValue(),
				bestMean)));
		}
	}
}
