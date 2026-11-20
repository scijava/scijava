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

package org.scijava.ops.image.filter.retinex;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.function.Computers;

/**
 * Multi-scale retinex (MSR) with optional colour restoration (MSRCR), shared
 * by the {@code filter.retinex} ops.
 * <p>
 * For each channel {@code I}, the multi-scale retinex is
 * {@code R = Σ_n w_n (log I - log(G_σn * I))}, with equal weights
 * {@code w_n = 1/N} and Gaussian surrounds {@code G_σn} applied to the
 * original channel at each scale. With colour restoration, each channel is
 * then multiplied by {@code C_i = β (log(α I_i) - log Σ_j I_j)}, with
 * {@code α = 125} and {@code β = 46}. Finally, the result is mapped linearly
 * from {@code [μ - d·s, μ + d·s]} to {@code [0, 1]} and clipped, where
 * {@code μ} and {@code s} are the mean and standard deviation of {@code R}
 * over the whole image (all channels) and {@code d} is the dynamic parameter;
 * with {@code d = 0} the raw retinex values are returned instead.
 * </p>
 * <p>
 * Intensities are clamped below at 1 for integer types, and at
 * 10<sup>-6</sup> times the image maximum otherwise, so that their logarithm
 * is finite.
 * </p>
 * <p>
 * Implemented from D. J. Jobson, Z. Rahman and G. A. Woodell, "A multiscale
 * retinex for bridging the gap between color images and the human observation
 * of scenes", IEEE Trans. Image Process. 6(7):965-976, 1997.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class Retinex {

	/** Default surround scales (Jobson et al. 1997). */
	public static final double[] DEFAULT_SIGMAS = { 15, 80, 250 };

	/** Default dynamic of the final contrast stretch. */
	public static final double DEFAULT_DYNAMIC = 1.2;

	private static final double ALPHA = 125;
	private static final double BETA = 46;

	private Retinex() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Computes the (multi-scale) retinex of one or more channels.
	 *
	 * @param channels the channels of the image, each with the same dimensions
	 * @param sigmas the Gaussian surround scales, in pixels (null for the
	 *          defaults)
	 * @param colorRestoration whether to apply colour restoration across the
	 *          channels
	 * @param dynamic the dynamic of the final contrast stretch (null for the
	 *          default; 0 to return raw values)
	 * @param gaussOp the Gaussian filter
	 * @param outputs the output channels
	 */
	static <T extends RealType<T>> void compute(
		final List<RandomAccessibleInterval<T>> channels, double[] sigmas,
		final boolean colorRestoration, Double dynamic,
		final Computers.Arity2<RandomAccessibleInterval<DoubleType>, Double, RandomAccessibleInterval<DoubleType>> gaussOp,
		final List<RandomAccessibleInterval<FloatType>> outputs)
	{
		if (sigmas == null) sigmas = DEFAULT_SIGMAS;
		if (sigmas.length == 0) {
			throw new IllegalArgumentException("At least one scale is required");
		}
		for (final var s : sigmas) {
			if (!(s > 0)) {
				throw new IllegalArgumentException("Scales must be positive: " + s);
			}
		}
		final double d = dynamic == null ? DEFAULT_DYNAMIC : dynamic;
		if (!(d >= 0)) {
			throw new IllegalArgumentException("Dynamic must be non-negative: " +
				d);
		}

		final var floor = floor(channels);
		final var dims = channels.get(0).dimensionsAsLongArray();
		final var weight = 1.0 / sigmas.length;

		// Floored intensities of each channel.
		final List<Img<DoubleType>> intensities = new ArrayList<>();
		for (final var c : channels) {
			final var img = ArrayImgs.doubles(dims);
			LoopBuilder.setImages(c, img).forEachPixel((i, o) -> o.set(Math.max(i
				.getRealDouble(), floor)));
			intensities.add(img);
		}

		final List<Img<DoubleType>> retinex = new ArrayList<>();
		final var blurred = ArrayImgs.doubles(dims);
		for (final var img : intensities) {
			final var r = ArrayImgs.doubles(dims);
			for (final var sigma : sigmas) {
				gaussOp.compute(img, sigma, blurred);
				LoopBuilder.setImages(img, blurred, r).forEachPixel((i, b, o) -> o
					.set(o.get() + weight * (Math.log(i.get()) - Math.log(Math.max(b
						.get(), floor)))));
			}
			retinex.add(r);
		}

		if (colorRestoration) {
			final var sum = ArrayImgs.doubles(dims);
			for (final var img : intensities)
				LoopBuilder.setImages(img, sum).forEachPixel((i, s) -> s.set(s.get() +
					i.get()));
			for (var c = 0; c < intensities.size(); c++) {
				LoopBuilder.setImages(intensities.get(c), sum, retinex.get(c))
					.forEachPixel((i, s, r) -> r.set(r.get() * BETA * (Math.log(ALPHA *
						i.get()) - Math.log(s.get()))));
			}
		}

		var lo = 0.0;
		var scale = 1.0;
		if (d > 0) {
			double sum = 0, sumSq = 0;
			long n = 0;
			for (final var r : retinex) {
				for (final var t : r) {
					final var v = t.get();
					sum += v;
					sumSq += v * v;
					n++;
				}
			}
			final var mean = sum / n;
			final var std = Math.sqrt(Math.max(0, sumSq / n - mean * mean));
			lo = mean - d * std;
			scale = std > 0 ? 1 / (2 * d * std) : 0;
		}
		final var stretch = d > 0;
		final var min = lo;
		final var factor = scale;
		for (var c = 0; c < retinex.size(); c++) {
			LoopBuilder.setImages(retinex.get(c), outputs.get(c)).forEachPixel((r,
				o) -> {
				if (!stretch) {
					o.setReal(r.get());
					return;
				}
				final var v = factor == 0 ? 0.5 : (r.get() - min) * factor;
				o.setReal(Math.min(1, Math.max(0, v)));
			});
		}
	}

	private static <T extends RealType<T>> double floor(
		final List<RandomAccessibleInterval<T>> channels)
	{
		final var type = Util.getTypeFromInterval(channels.get(0));
		if (type instanceof IntegerType) return 1;
		var max = 0.0;
		for (final var c : channels)
			for (final var t : Views.iterable(c))
				max = Math.max(max, t.getRealDouble());
		return max > 0 ? 1e-6 * max : 1e-6;
	}

	/**
	 * Splits an image into its channels along a dimension.
	 *
	 * @param img the image
	 * @param channelDim the channel dimension
	 * @return one hyperslice per channel
	 */
	static <T> List<RandomAccessibleInterval<T>> channels(
		final RandomAccessibleInterval<T> img, final int channelDim)
	{
		if (channelDim < 0 || channelDim >= img.numDimensions()) {
			throw new IllegalArgumentException("No such channel dimension: " +
				channelDim);
		}
		final List<RandomAccessibleInterval<T>> list = new ArrayList<>();
		for (var c = img.min(channelDim); c <= img.max(channelDim); c++)
			list.add(Views.hyperSlice(img, channelDim, c));
		return list;
	}
}
