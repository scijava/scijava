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

package org.scijava.ops.image.segment.levelset;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Computers;

/**
 * Shared implementation of {@code image.levelSetFunction} and
 * {@code segment.levelSet}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
final class LevelSets {

	static final String CHAN_VESE = "chanVese";
	static final String GEODESIC = "geodesic";

	private LevelSets() {
		// Prevent instantiation of static utility class.
	}

	/** Evolves the level-set function and returns it in flat order. */
	static <T extends RealType<T>, B extends BooleanType<B>> double[] evolve(
		final RandomAccessibleInterval<T> image,
		final RandomAccessibleInterval<B> init, final String method,
		final Double curvature, final Double propagation, final Double advection,
		final Double sigma, final Integer maxIterations, final Double tolerance,
		final double[] calibration,
		final BiFunction<RandomAccessibleInterval<BitType>, double[], RandomAccessibleInterval<FloatType>> sdtOp,
		final Computers.Arity2<RandomAccessibleInterval<DoubleType>, double[], RandomAccessibleInterval<DoubleType>> gaussOp)
	{
		final var dims = image.dimensionsAsLongArray();
		final var n = dims.length;
		if (!Arrays.equals(dims, init.dimensionsAsLongArray())) {
			throw new IllegalArgumentException(
				"Image and initial mask dimensions differ: " + Arrays.toString(dims) +
					" vs " + Arrays.toString(init.dimensionsAsLongArray()));
		}
		final var size = Intervals.numElements(image);
		if (size > Integer.MAX_VALUE - 8) {
			throw new IllegalArgumentException("Image too large: " + size +
				" pixels");
		}
		final double[] cal;
		if (calibration == null) {
			cal = new double[n];
			Arrays.fill(cal, 1);
		}
		else if (calibration.length != n) {
			throw new IllegalArgumentException("Expected " + n +
				" calibration values but got " + calibration.length);
		}
		else cal = calibration;
		final var geodesic = isGeodesic(method);
		final var iterations = maxIterations == null ? 500 : maxIterations;
		final var tol = tolerance == null ? 0.005 : tolerance;
		if (iterations < 0 || tol < 0) {
			throw new IllegalArgumentException(
				"Iterations and tolerance must be non-negative");
		}

		// Initialization with the signed distance of the initial mask.
		final var mask = ArrayImgs.bits(dims);
		final var mc = mask.cursor();
		for (final var b : Views.flatIterable(init))
			mc.next().set(b.get());
		final var phi = flat(sdtOp.apply(mask, cal), (int) size);
		final UnaryOperator<double[]> sdt = p -> {
			var i = 0;
			for (final var b : mask)
				b.set(p[i++] > 0);
			return flat(sdtOp.apply(mask, cal), p.length);
		};

		final var u = normalized(image, (int) size, !geodesic);
		final var evolution = new LevelSetEvolution(dims, cal);
		if (geodesic) {
			final var s = sigma == null ? 1 : sigma;
			final var g = edgeMap(u, dims, cal, s, gaussOp);
			evolution.geodesic(phi, g, curvature == null ? 1 : curvature,
				propagation == null ? 1 : propagation, advection == null ? 1
					: advection, iterations, tol, sdt);
		}
		else {
			evolution.chanVese(phi, u, curvature == null ? 1 : curvature,
				propagation == null ? 0 : propagation, iterations, tol, sdt);
		}
		evolution.reinitialize(phi, sdt);
		return phi;
	}

	private static boolean isGeodesic(final String method) {
		if (method == null) return false;
		switch (method.toLowerCase(Locale.ROOT)) {
			case "chanvese":
			case "chan-vese":
			case "cv":
				return false;
			case "geodesic":
			case "gac":
				return true;
			default:
				throw new IllegalArgumentException("Unknown level-set method: " +
					method + " (expected " + CHAN_VESE + " or " + GEODESIC + ")");
		}
	}

	/**
	 * The edge stopping function {@code 1 / (1 + (|grad u_sigma| / lambda)^2)},
	 * with {@code lambda} a tenth of the largest gradient magnitude.
	 */
	private static double[] edgeMap(final double[] u, final long[] dims,
		final double[] cal, final double sigma,
		final Computers.Arity2<RandomAccessibleInterval<DoubleType>, double[], RandomAccessibleInterval<DoubleType>> gaussOp)
	{
		final var n = dims.length;
		final double[] smooth;
		if (sigma > 0) {
			final var sigmas = new double[n];
			for (var d = 0; d < n; d++)
				sigmas[d] = sigma / cal[d];
			final var out = ArrayImgs.doubles(dims);
			gaussOp.compute(ArrayImgs.doubles(u, dims), sigmas, out);
			smooth = out.update(null).getCurrentStorageArray();
		}
		else smooth = u;
		final var strides = new long[n];
		var stride = 1L;
		for (var d = 0; d < n; d++) {
			strides[d] = stride;
			stride *= dims[d];
		}
		final var mag = new double[u.length];
		final var pos = new long[n];
		var max = 0.0;
		for (var i = 0; i < u.length; i++) {
			var s = 0.0;
			for (var d = 0; d < n; d++) {
				final var lo = pos[d] > 0 ? i - strides[d] : i;
				final var hi = pos[d] < dims[d] - 1 ? i + strides[d] : i;
				final var steps = (hi - lo) / strides[d];
				if (steps == 0) continue;
				final var v = (smooth[(int) hi] - smooth[(int) lo]) / (steps * cal[d]);
				s += v * v;
			}
			mag[i] = Math.sqrt(s);
			max = Math.max(max, mag[i]);
			for (var d = 0; d < n; d++) {
				if (++pos[d] < dims[d]) break;
				pos[d] = 0;
			}
		}
		final var lambda = 0.1 * max;
		for (var i = 0; i < mag.length; i++) {
			final var r = lambda > 0 ? mag[i] / lambda : 0;
			mag[i] = 1 / (1 + r * r);
		}
		return mag;
	}

	/**
	 * Rescales the image to zero mean and unit variance (for Chan-Vese), or to
	 * the range [0, 1].
	 */
	private static <T extends RealType<T>> double[] normalized(
		final RandomAccessibleInterval<T> image, final int size,
		final boolean standardize)
	{
		final var u = new double[size];
		var min = Double.POSITIVE_INFINITY;
		var max = Double.NEGATIVE_INFINITY;
		var i = 0;
		for (final var t : Views.flatIterable(image)) {
			final var v = t.getRealDouble();
			u[i++] = v;
			min = Math.min(min, v);
			max = Math.max(max, v);
		}
		if (standardize) {
			var mean = 0.0;
			for (final var v : u)
				mean += v;
			mean /= size;
			var variance = 0.0;
			for (final var v : u)
				variance += (v - mean) * (v - mean);
			final var std = Math.sqrt(variance / size);
			for (i = 0; i < size; i++)
				u[i] = std > 0 ? (u[i] - mean) / std : 0;
			return u;
		}
		final var range = max - min;
		for (i = 0; i < size; i++)
			u[i] = range > 0 ? (u[i] - min) / range : 0;
		return u;
	}

	private static double[] flat(final RandomAccessibleInterval<FloatType> img,
		final int size)
	{
		final var out = new double[size];
		var i = 0;
		for (final var t : Views.flatIterable(img))
			out[i++] = t.getRealDouble();
		return out;
	}
}
