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

package org.scijava.ops.image.segment.fastmarching;

import java.util.Arrays;
import java.util.List;

import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.ops.spi.Nullable;

/**
 * Fast marching ops: arrival times of a front that starts at seed pixels and
 * advances outward with a pixel-dependent speed.
 * <p>
 * The arrival time {@code T} solves the Eikonal equation
 * {@code |grad T| F = 1} with {@code T = 0} at the seeds, where {@code F} is
 * the speed image (or {@code 1 / cost} for a cost image). It is computed with
 * the fast marching method of J. A. Sethian ("A fast marching level set method
 * for monotonically advancing fronts", <i>PNAS</i> 93(4): 1591&ndash;1595,
 * 1996), in any number of dimensions and with anisotropic pixel spacing, using
 * first-order or second-order upwind differences. With unit speed, the arrival
 * time approximates the Euclidean distance to the seeds; in general it is the
 * minimal travel time {@code integral ds / F} along any path, so fronts refract
 * at interfaces between media of different speeds.
 * </p>
 * <p>
 * {@code morphology.geodesicDistanceMap} also measures path lengths from seeds,
 * but differs in two ways. First, it computes exact shortest paths in a graph
 * whose edges join a pixel to a fixed set of neighbors (Dijkstra), so its
 * distances are polygonal (chamfer) approximations of the Euclidean ones, with
 * a direction-dependent error that does not shrink with the distance; fast
 * marching instead solves a discretization of the continuous Eikonal equation,
 * whose relative error does decrease with the distance. Second, it is limited
 * to a binary domain (unit speed inside the mask, impassable outside), whereas
 * fast marching accepts any non-negative speed field. A binary mask can be
 * used as a speed image here to get a continuous-metric geodesic distance.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class FastMarchingOps {

	private FastMarchingOps() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes the arrival time map of a front starting at the pixels of a
	 * seed mask.
	 *
	 * @param seeds the seed pixels, where the arrival time is 0; must have the
	 *          dimensions of the speed image
	 * @param speed the speed of the front at each pixel, in calibrated units per
	 *          unit of time; pixels whose speed is zero, negative or NaN are
	 *          impassable
	 * @param calibration the pixel spacing along each dimension (default: 1 in
	 *          every dimension)
	 * @param stopTime the arrival time after which propagation stops; later
	 *          pixels are left at positive infinity (default: no limit)
	 * @param secondOrder whether to use second-order upwind differences where
	 *          enough accepted pixels are available, for better accuracy
	 *          (default: true)
	 * @param asCost if true, the speed image is instead interpreted as a cost
	 *          image (time per unit length), and the speed is its reciprocal
	 *          (default: false)
	 * @return the arrival times, with the interval of the speed image;
	 *         unreachable pixels are positive infinity
	 * @param <B> seed mask type
	 * @param <T> speed image type
	 * @implNote op names='image.fastMarching', type=Function
	 */
	public static <B extends BooleanType<B>, T extends RealType<T>>
		RandomAccessibleInterval<DoubleType> timesFromMask(
			final RandomAccessibleInterval<B> seeds,
			final RandomAccessibleInterval<T> speed,
			@Nullable final double[] calibration, @Nullable final Double stopTime,
			@Nullable final Boolean secondOrder, @Nullable final Boolean asCost)
	{
		return times(speed, maskSeeds(seeds, speed), calibration, stopTime,
			secondOrder, asCost);
	}

	/**
	 * Computes the arrival time map of a front starting at seed points.
	 *
	 * @param seeds the seed points, in the coordinates of the speed image, where
	 *          the arrival time is 0
	 * @param speed the speed of the front at each pixel, in calibrated units per
	 *          unit of time; pixels whose speed is zero, negative or NaN are
	 *          impassable
	 * @param calibration the pixel spacing along each dimension (default: 1 in
	 *          every dimension)
	 * @param stopTime the arrival time after which propagation stops; later
	 *          pixels are left at positive infinity (default: no limit)
	 * @param secondOrder whether to use second-order upwind differences where
	 *          enough accepted pixels are available, for better accuracy
	 *          (default: true)
	 * @param asCost if true, the speed image is instead interpreted as a cost
	 *          image (time per unit length), and the speed is its reciprocal
	 *          (default: false)
	 * @return the arrival times, with the interval of the speed image;
	 *         unreachable pixels are positive infinity
	 * @param <T> speed image type
	 * @implNote op names='image.fastMarching', type=Function
	 */
	public static <T extends RealType<T>> RandomAccessibleInterval<DoubleType>
		timesFromPoints(final List<? extends Localizable> seeds,
			final RandomAccessibleInterval<T> speed,
			@Nullable final double[] calibration, @Nullable final Double stopTime,
			@Nullable final Boolean secondOrder, @Nullable final Boolean asCost)
	{
		return times(speed, pointSeeds(seeds, speed), calibration, stopTime,
			secondOrder, asCost);
	}

	/**
	 * Segments the region reached by a front starting at the pixels of a seed
	 * mask within a given time: the pixels whose fast marching arrival time
	 * (see {@code image.fastMarching}) is at most {@code threshold}.
	 *
	 * @param seeds the seed pixels, where the arrival time is 0; must have the
	 *          dimensions of the speed image
	 * @param speed the speed of the front at each pixel, in calibrated units per
	 *          unit of time; pixels whose speed is zero, negative or NaN are
	 *          impassable
	 * @param threshold the maximal arrival time of the segmented pixels
	 * @param calibration the pixel spacing along each dimension (default: 1 in
	 *          every dimension)
	 * @param secondOrder whether to use second-order upwind differences where
	 *          enough accepted pixels are available (default: true)
	 * @param asCost if true, the speed image is instead interpreted as a cost
	 *          image (time per unit length), and the speed is its reciprocal
	 *          (default: false)
	 * @return the segmentation mask, with the interval of the speed image
	 * @param <B> seed mask type
	 * @param <T> speed image type
	 * @implNote op names='segment.fastMarching', type=Function
	 */
	public static <B extends BooleanType<B>, T extends RealType<T>>
		RandomAccessibleInterval<BitType> maskFromMask(
			final RandomAccessibleInterval<B> seeds,
			final RandomAccessibleInterval<T> speed, final Double threshold,
			@Nullable final double[] calibration,
			@Nullable final Boolean secondOrder, @Nullable final Boolean asCost)
	{
		return mask(speed, maskSeeds(seeds, speed), threshold, calibration,
			secondOrder, asCost);
	}

	/**
	 * Segments the region reached by a front starting at seed points within a
	 * given time: the pixels whose fast marching arrival time (see
	 * {@code image.fastMarching}) is at most {@code threshold}.
	 *
	 * @param seeds the seed points, in the coordinates of the speed image, where
	 *          the arrival time is 0
	 * @param speed the speed of the front at each pixel, in calibrated units per
	 *          unit of time; pixels whose speed is zero, negative or NaN are
	 *          impassable
	 * @param threshold the maximal arrival time of the segmented pixels
	 * @param calibration the pixel spacing along each dimension (default: 1 in
	 *          every dimension)
	 * @param secondOrder whether to use second-order upwind differences where
	 *          enough accepted pixels are available (default: true)
	 * @param asCost if true, the speed image is instead interpreted as a cost
	 *          image (time per unit length), and the speed is its reciprocal
	 *          (default: false)
	 * @return the segmentation mask, with the interval of the speed image
	 * @param <T> speed image type
	 * @implNote op names='segment.fastMarching', type=Function
	 */
	public static <T extends RealType<T>> RandomAccessibleInterval<BitType>
		maskFromPoints(final List<? extends Localizable> seeds,
			final RandomAccessibleInterval<T> speed, final Double threshold,
			@Nullable final double[] calibration,
			@Nullable final Boolean secondOrder, @Nullable final Boolean asCost)
	{
		return mask(speed, pointSeeds(seeds, speed), threshold, calibration,
			secondOrder, asCost);
	}

	// -- Helper methods --

	private static <T extends RealType<T>> RandomAccessibleInterval<BitType>
		mask(final RandomAccessibleInterval<T> speed, final int[] seeds,
			final Double threshold, final double[] calibration,
			final Boolean secondOrder, final Boolean asCost)
	{
		if (threshold == null || Double.isNaN(threshold)) {
			throw new IllegalArgumentException("A time threshold is required");
		}
		final var times = march(speed, seeds, calibration, threshold, secondOrder,
			asCost);
		final var bits = ArrayImgs.bits(speed.dimensionsAsLongArray());
		var i = 0;
		for (final var b : bits)
			b.set(times[i++] <= threshold);
		return Views.translate(bits, speed.minAsLongArray());
	}

	private static <T extends RealType<T>> RandomAccessibleInterval<DoubleType>
		times(final RandomAccessibleInterval<T> speed, final int[] seeds,
			final double[] calibration, final Double stopTime,
			final Boolean secondOrder, final Boolean asCost)
	{
		final var times = march(speed, seeds, calibration, stopTime == null
			? Double.POSITIVE_INFINITY : stopTime, secondOrder, asCost);
		return Views.translate(ArrayImgs.doubles(times, speed
			.dimensionsAsLongArray()), speed.minAsLongArray());
	}

	private static <T extends RealType<T>> double[] march(
		final RandomAccessibleInterval<T> speed, final int[] seeds,
		final double[] calibration, final double stopTime,
		final Boolean secondOrder, final Boolean asCost)
	{
		final var n = speed.numDimensions();
		final double[] cal;
		if (calibration == null) {
			cal = new double[n];
			Arrays.fill(cal, 1);
		}
		else cal = calibration;
		final var cost = asCost != null && asCost;
		final var f = new double[size(speed)];
		var i = 0;
		for (final var t : Views.flatIterable(speed)) {
			final var v = t.getRealDouble();
			f[i++] = cost ? 1 / v : v;
		}
		final var solver = new FastMarching(speed.dimensionsAsLongArray(), cal, f,
			secondOrder == null || secondOrder);
		return solver.propagate(seeds, stopTime);
	}

	private static <B extends BooleanType<B>> int[] maskSeeds(
		final RandomAccessibleInterval<B> seeds,
		final RandomAccessibleInterval<?> speed)
	{
		if (!Arrays.equals(seeds.dimensionsAsLongArray(), speed
			.dimensionsAsLongArray()))
		{
			throw new IllegalArgumentException(
				"Seed and speed image dimensions differ: " + Arrays.toString(seeds
					.dimensionsAsLongArray()) + " vs " + Arrays.toString(speed
						.dimensionsAsLongArray()));
		}
		final var indices = new int[size(seeds)];
		var count = 0;
		var i = 0;
		for (final var b : Views.flatIterable(seeds)) {
			if (b.get()) indices[count++] = i;
			i++;
		}
		return Arrays.copyOf(indices, count);
	}

	private static int[] pointSeeds(final List<? extends Localizable> seeds,
		final RandomAccessibleInterval<?> speed)
	{
		final var n = speed.numDimensions();
		final var indices = new int[seeds.size()];
		var s = 0;
		for (final var p : seeds) {
			if (p.numDimensions() != n) {
				throw new IllegalArgumentException("Seed point has " + p
					.numDimensions() + " dimensions but the image has " + n);
			}
			var index = 0L;
			var stride = 1L;
			for (var d = 0; d < n; d++) {
				final var c = p.getLongPosition(d) - speed.min(d);
				if (c < 0 || c >= speed.dimension(d)) {
					throw new IllegalArgumentException(
						"Seed point outside the image: " + Arrays.toString(p
							.positionAsLongArray()));
				}
				index += c * stride;
				stride *= speed.dimension(d);
			}
			indices[s++] = (int) index;
		}
		return indices;
	}

	private static int size(final RandomAccessibleInterval<?> image) {
		final var size = Intervals.numElements(image);
		if (size > Integer.MAX_VALUE - 8) {
			throw new IllegalArgumentException("Image too large: " + size +
				" pixels");
		}
		return (int) size;
	}
}
