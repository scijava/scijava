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

package org.scijava.ops.image.transform.focus;

import java.util.Arrays;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Computers;

/**
 * Computes a per-pixel focus measure for every slice of a stack and combines
 * the slices into an extended depth of field image.
 * <p>
 * The focus measure of slice {@code z} is {@code M_z = G_sigma * (Lap(G_1 *
 * I_z))^2}: the local energy of the Laplacian of the slice (after Gaussian
 * smoothing with sigma 1 to suppress pixel noise), averaged with a Gaussian
 * window of the given sigma. Since the Laplacian has zero mean over a
 * neighborhood, this is a local variance of the Laplacian, a standard
 * sharpness measure (Pech-Pacheco et al. 2000).
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
final class FocusStacker<T extends RealType<T>> {

	/** The index of the sharpest slice per pixel, in flat iteration order. */
	final int[] index;

	/** The composite value per pixel, in flat iteration order. */
	final double[] composite;

	FocusStacker(final RandomAccessibleInterval<T> input, final int focusDim,
		final Double sigma, final boolean weighted,
		final BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> createOp,
		final Computers.Arity3<RandomAccessibleInterval<T>, double[], OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<FloatType>> gaussOp,
		final Computers.Arity3<RandomAccessibleInterval<FloatType>, double[], OutOfBoundsFactory<FloatType, RandomAccessibleInterval<FloatType>>, RandomAccessibleInterval<FloatType>> gaussFloatOp)
	{
		final var n = input.numDimensions();
		if (n < 2) {
			throw new IllegalArgumentException(
				"Need at least two dimensions, but got " + n);
		}
		if (focusDim < 0 || focusDim >= n) {
			throw new IllegalArgumentException("Invalid focus dimension: " +
				focusDim);
		}
		final var s = sigma == null ? 2.0 : sigma;
		final var first = Views.hyperSlice(input, focusDim, input.min(focusDim));
		final var size = Intervals.numElements(first);
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Slices are too large: " + size +
				" pixels");
		}
		final var count = (int) size;
		final var noiseSigmas = new double[n - 1];
		Arrays.fill(noiseSigmas, 1);
		final var windowSigmas = new double[n - 1];
		Arrays.fill(windowSigmas, s);

		index = new int[count];
		composite = new double[count];
		final var best = new double[count];
		final var weightSum = weighted ? new double[count] : null;
		final var valueSum = weighted ? new double[count] : null;
		final var smooth = createOp.apply(first, new FloatType());
		final var energy = createOp.apply(first, new FloatType());
		final var measure = createOp.apply(first, new FloatType());
		final var depth = input.dimension(focusDim);
		for (var z = 0; z < depth; z++) {
			final var slice = Views.hyperSlice(input, focusDim, input.min(
				focusDim) + z);
			gaussOp.compute(slice, noiseSigmas, new OutOfBoundsBorderFactory<>(),
				smooth);
			squaredLaplacian(smooth, energy);
			gaussFloatOp.compute(energy, windowSigmas,
				new OutOfBoundsBorderFactory<>(), measure);
			final var mc = Views.flatIterable(measure).cursor();
			final var ic = Views.flatIterable(slice).cursor();
			for (var i = 0; i < count; i++) {
				final var m = mc.next().getRealDouble();
				final var v = ic.next().getRealDouble();
				if (z == 0 || m > best[i]) {
					best[i] = m;
					index[i] = z;
					if (!weighted) composite[i] = v;
				}
				if (weighted) {
					weightSum[i] += m;
					composite[i] += m * v;
					valueSum[i] += v;
				}
			}
		}
		if (weighted) {
			for (var i = 0; i < count; i++) {
				composite[i] = weightSum[i] > 0 ? composite[i] / weightSum[i]
					: valueSum[i] / depth;
			}
		}
	}

	/** Computes the squared discrete Laplacian, with border extension. */
	private static void squaredLaplacian(
		final RandomAccessibleInterval<FloatType> in,
		final RandomAccessibleInterval<FloatType> out)
	{
		final var n = in.numDimensions();
		final var ra = Views.extendBorder(in).randomAccess();
		final var c = Views.flatIterable(out).localizingCursor();
		final var pos = new long[n];
		while (c.hasNext()) {
			c.fwd();
			c.localize(pos);
			final var center = ra.setPositionAndGet(pos).getRealDouble();
			var lap = 0.0;
			for (var d = 0; d < n; d++) {
				pos[d]--;
				lap += ra.setPositionAndGet(pos).getRealDouble() - center;
				pos[d] += 2;
				lap += ra.setPositionAndGet(pos).getRealDouble() - center;
				pos[d]--;
			}
			c.get().setReal(lap * lap);
		}
	}
}
