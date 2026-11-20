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

package org.scijava.ops.image.filter.wavelet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * Utility methods for the isotropic undecimated (&agrave; trous) wavelet
 * transform with the cubic B-spline scaling function, also known as the
 * starlet transform.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @see "J.-L. Starck and F. Murtagh, Astronomical Image and Data Analysis,
 *      2nd ed., Springer, 2006"
 */
final class Starlet {

	/** The 1D cubic B-spline filter: (1, 4, 6, 4, 1) / 16. */
	private static final double[] B3 = { 1 / 16.0, 4 / 16.0, 6 / 16.0,
		4 / 16.0, 1 / 16.0 };

	private Starlet() {
		// Prevent instantiation of utility class.
	}

	/**
	 * Decomposes an image into wavelet planes {@code w_1 ... w_J} and the
	 * residual (smooth) plane {@code c_J}, so that the input equals the sum of
	 * all returned images. Dimensions of size 1 are not filtered.
	 *
	 * @param input the image to decompose
	 * @param nScales the number of wavelet scales {@code J}
	 * @param creator creates a new (zero-min) image with the dimensions of the
	 *          input
	 * @return {@code J + 1} images: the wavelet planes from fine to coarse,
	 *         followed by the residual
	 */
	static <T extends RealType<T>> List<RandomAccessibleInterval<FloatType>>
		decompose(final RandomAccessibleInterval<T> input, final int nScales,
			final Supplier<RandomAccessibleInterval<FloatType>> creator)
	{
		if (nScales < 1) {
			throw new IllegalArgumentException("Need at least one scale: " +
				nScales);
		}
		final var n = input.numDimensions();
		final var planes = new ArrayList<RandomAccessibleInterval<FloatType>>();
		var previous = creator.get();
		LoopBuilder.setImages(Views.zeroMin(input), previous).forEachPixel((i,
			o) -> o.setReal(i.getRealDouble()));
		for (var j = 1; j <= nScales; j++) {
			final var step = 1 << (j - 1);
			final var kernels = new Kernel1D[n];
			for (var d = 0; d < n; d++) {
				kernels[d] = input.dimension(d) > 1 ? Kernel1D.symmetric(halfKernel(
					step)) : Kernel1D.symmetric(1);
			}
			final var smooth = creator.get();
			SeparableKernelConvolution.convolve(kernels, Views.extendMirrorSingle(
				previous), smooth);
			LoopBuilder.setImages(previous, smooth).forEachPixel((w, c) -> w.sub(
				c));
			planes.add(previous);
			previous = smooth;
		}
		planes.add(previous);
		return planes;
	}

	/**
	 * Computes, for each scale, the standard deviation of the wavelet
	 * coefficients of unit-variance white Gaussian noise. These are the norms
	 * of the equivalent filters producing the wavelet planes, which are
	 * separable differences {@code h_(j-1) x ... x h_(j-1) - h_j x ... x h_j}
	 * of the cumulative 1D smoothing filters {@code h_j}.
	 *
	 * @param nScales the number of wavelet scales
	 * @param nDims the number of filtered dimensions
	 * @return the noise standard deviation for scales 1 to {@code nScales}
	 *         (index 0 is scale 1)
	 */
	static double[] noiseLevels(final int nScales, final int nDims) {
		final var result = new double[nScales];
		var h = new double[] { 1 };
		for (var j = 1; j <= nScales; j++) {
			final var next = convolve(h, dilate(1 << (j - 1)));
			// Note: h is centered in next, which is longer by 2^j on each side.
			final var shift = (next.length - h.length) / 2;
			double hh = 0, hn = 0, nn = 0;
			for (var i = 0; i < h.length; i++) {
				hh += h[i] * h[i];
				hn += h[i] * next[i + shift];
			}
			for (final var v : next)
				nn += v * v;
			result[j - 1] = Math.sqrt(Math.pow(hh, nDims) - 2 * Math.pow(hn,
				nDims) + Math.pow(nn, nDims));
			h = next;
		}
		return result;
	}

	/** Returns the half kernel of the B3 filter with holes of the given step. */
	private static double[] halfKernel(final int step) {
		final var half = new double[2 * step + 1];
		half[0] = B3[2];
		half[step] = B3[3];
		half[2 * step] = B3[4];
		return half;
	}

	/** Returns the full B3 filter with holes of the given step. */
	private static double[] dilate(final int step) {
		final var full = new double[4 * step + 1];
		for (var i = 0; i < B3.length; i++)
			full[i * step] = B3[i];
		return full;
	}

	private static double[] convolve(final double[] a, final double[] b) {
		final var result = new double[a.length + b.length - 1];
		for (var i = 0; i < a.length; i++)
			for (var k = 0; k < b.length; k++)
				result[i + k] += a[i] * b[k];
		return result;
	}
}
