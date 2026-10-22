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

package org.scijava.ops.image.filter.helmholtz;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Helmholtz analysis: estimation of the local wavelength and orientation of
 * quasi-periodic patterns (fringes, striations, e.g. of muscle fibers) in 2D
 * images, using the Helmholtz equation {@code ∇²I + k²I = 0}, which holds for
 * a locally harmonic intensity {@code I} with wave number {@code k}.
 * <p>
 * The image is first band-pass filtered to the range of wavelengths of
 * interest, so that it oscillates around zero. Then, at each pixel, the local
 * wave number is {@code k = sqrt(-∇²I / I)} and the wavelength is
 * {@code λ = 2π / k}; pixels where {@code ∇²I / I >= 0} or where {@code λ} is
 * outside {@code [lambdaMin, lambdaMax]} are invalid (NaN). The orientation
 * is that of the intensity gradient, computed with Sobel kernels. The
 * Laplacian is the discrete 5-point Laplacian, which overestimates the
 * wavelength slightly for short wavelengths (by about 2% at 8 pixels and 5% at
 * 6 pixels).
 * </p>
 * <p>
 * Optionally, the raw estimates are smoothed with a validated mean over a disk
 * of the given radius: the mean of the valid values within the disk, which is
 * only accepted (otherwise NaN) if at least 2/3 of the disk pixels are valid
 * and the standard deviation of the valid values is at most {@code lambdaMin}
 * (for the wavelength) or 15 degrees (for the orientation, whose mean is
 * weighted by the gradient magnitude).
 * </p>
 * <p>
 * Translated from the Helmholtz_Analysis plugin for Fiji (<a href=
 * "https://github.com/fiji/Helmholtz_Analysis">Helmholtz_Analysis</a>, formerly
 * named Muscle Tone) by Bob Dougherty, OptiNav, Inc., used under the BSD
 * 3-Clause license. Differences from the plugin: the plugin band-pass filters
 * with ImageJ's FFT "Bandpass Filter" (with intensity autoscaling and
 * saturation) after a Gaussian blur; this op uses the equivalent Gaussian
 * filters in the spatial domain (the FFT filter's Gaussian transfer functions
 * for the structure sizes {@code d} correspond to spatial Gaussians with
 * {@code sigma = d * sqrt(2) / π}), so values differ slightly.
 * </p>
 * <p>
 * The original Helmholtz_Analysis license notice follows.
 * </p>
 *
 * <pre>
 * Copyright (c) 2005, OptiNav, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *   Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *   Neither the name of OptiNav, Inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Bob Dougherty
 */
public final class HelmholtzAnalysis {

	private static final double PHI_TOLERANCE = 15;

	private HelmholtzAnalysis() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Estimates the local wavelength of quasi-periodic patterns in a 2D image,
	 * using the Helmholtz equation.
	 *
	 * @param <T> the input type
	 * @param gaussOp the Gaussian filter op
	 * @param input the 2D input image
	 * @param lambdaMin the minimum wavelength of interest, in pixels
	 * @param lambdaMax the maximum wavelength of interest, in pixels
	 * @param averagingRadius the radius, in pixels, of the disk over which the
	 *          estimates are averaged; defaults to 0 (no averaging)
	 * @return the local wavelength, in pixels, or NaN where it is invalid
	 * @implNote op names='filter.helmholtzWavelength'
	 */
	public static <T extends RealType<T>> RandomAccessibleInterval<FloatType>
		wavelength( //
			@OpDependency(
				name = "filter.gauss") Computers.Arity2<RandomAccessibleInterval<FloatType>, Double, RandomAccessibleInterval<FloatType>> gaussOp, //
			final RandomAccessibleInterval<T> input, //
			final Double lambdaMin, //
			final Double lambdaMax, //
			@Nullable Integer averagingRadius //
	) {
		final var result = analyze(gaussOp, input, lambdaMin, lambdaMax);
		final var r = averagingRadius == null ? 0 : averagingRadius;
		if (r <= 0) return wrap(result.lambda, result.w, result.h);
		final var av = new float[result.lambda.length];
		average(result.w, result.h, r, lambdaMin, result.lambda, null, av);
		return wrap(av, result.w, result.h);
	}

	/**
	 * Estimates the local orientation of quasi-periodic patterns in a 2D image,
	 * using the Helmholtz equation. The orientation is that of the fringes, i.e.
	 * of the lines of locally constant intensity, perpendicular to the
	 * intensity gradient: an angle in degrees within {@code [0, 180)}, measured
	 * from the x axis towards the y axis (clockwise on screen, since y points
	 * down). For example, stripes that run along the y axis have orientation
	 * 90. Averaging uses the linear mean of the angles, as the plugin does, so
	 * averaged orientations near 0 or 180 degrees are typically rejected.
	 *
	 * @param <T> the input type
	 * @param gaussOp the Gaussian filter op
	 * @param input the 2D input image
	 * @param lambdaMin the minimum wavelength of interest, in pixels
	 * @param lambdaMax the maximum wavelength of interest, in pixels
	 * @param averagingRadius the radius, in pixels, of the disk over which the
	 *          estimates are averaged; defaults to 0 (no averaging)
	 * @return the local orientation, in degrees, or NaN where the wavelength
	 *         is invalid
	 * @implNote op names='filter.helmholtzOrientation'
	 */
	public static <T extends RealType<T>> RandomAccessibleInterval<FloatType>
		orientation( //
			@OpDependency(
				name = "filter.gauss") Computers.Arity2<RandomAccessibleInterval<FloatType>, Double, RandomAccessibleInterval<FloatType>> gaussOp, //
			final RandomAccessibleInterval<T> input, //
			final Double lambdaMin, //
			final Double lambdaMax, //
			@Nullable Integer averagingRadius //
	) {
		final var result = analyze(gaussOp, input, lambdaMin, lambdaMax);
		final var r = averagingRadius == null ? 0 : averagingRadius;
		if (r <= 0) return wrap(result.phi, result.w, result.h);
		final var av = new float[result.phi.length];
		average(result.w, result.h, r, PHI_TOLERANCE, result.phi, result.weight,
			av);
		return wrap(av, result.w, result.h);
	}

	// -- Helper methods --

	private static final class Result {

		private int w, h;
		private float[] lambda, phi, weight;
	}

	private static <T extends RealType<T>> Result analyze(
		final Computers.Arity2<RandomAccessibleInterval<FloatType>, Double, RandomAccessibleInterval<FloatType>> gaussOp,
		final RandomAccessibleInterval<T> input, final double lambdaMin,
		final double lambdaMax)
	{
		if (input.numDimensions() != 2) {
			throw new IllegalArgumentException("Only 2D images are supported");
		}
		if (!(lambdaMin > 0 && lambdaMax > lambdaMin)) {
			throw new IllegalArgumentException(
				"Need 0 < lambdaMin < lambdaMax");
		}
		if (input.size() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Image is too large");
		}
		final var w = (int) input.dimension(0);
		final var h = (int) input.dimension(1);

		// Band-pass filter. The plugin blurs with a Gaussian of sigma
		// lambdaMin / 5 (ImageJ "radius" lambdaMin / 2), applies the FFT band-pass
		// filter, then subtracts a Gaussian blur of sigma lambdaMax / 2.5.
		final var f = ArrayImgs.floats(w, h);
		final var ic = Views.flatIterable(input).cursor();
		for (final var t : f)
			t.setReal(ic.next().getRealDouble());
		final var fftScale = Math.sqrt(2) / Math.PI;
		final var sigmaPre = lambdaMin / 5;
		final var sigmaSmall = fftScale * lambdaMin;
		final var sigmaLarge = fftScale * lambdaMax;
		final var sigmaTrend = lambdaMax / 2.5;
		final var low = gauss(gaussOp, f, Math.sqrt(sigmaPre * sigmaPre +
			sigmaSmall * sigmaSmall));
		final var bandPassed = subtract(low, gauss(gaussOp, low, sigmaLarge));
		final var filtered = subtract(bandPassed, gauss(gaussOp, bandPassed,
			sigmaTrend));
		final var data = filtered.update(null).getCurrentStorageArray();

		final var gradX = convolve3x3(data, w, h, new int[] { -1, 0, 1, -2, 0, 2,
			-1, 0, 1 });
		final var gradY = convolve3x3(data, w, h, new int[] { 1, 2, 1, 0, 0, 0, -1,
			-2, -1 });
		final var laplace = convolve3x3(data, w, h, new int[] { 0, 1, 0, 1, -4, 1,
			0, 1, 0 });

		final var result = new Result();
		result.w = w;
		result.h = h;
		result.lambda = new float[w * h];
		result.phi = new float[w * h];
		result.weight = new float[w * h];
		for (var i = 0; i < data.length; i++) {
			final double d = data[i];
			final var k2 = d == 0 ? 0 : laplace[i] / d;
			result.lambda[i] = Float.NaN;
			result.phi[i] = Float.NaN;
			if (k2 >= 0) continue;
			final var wavelength = (float) (2 * Math.PI / Math.sqrt(-k2));
			if (wavelength < lambdaMin || wavelength > lambdaMax) continue;
			result.lambda[i] = wavelength;
			final double gx = gradX[i], gy = gradY[i];
			// Note: atan2 returns ±π for gx = ±0 and gy < 0, which is the same
			// orientation as 0; the angle is reduced to [0, 180) in float precision.
			var phiDeg = (float) Math.toDegrees(Math.atan2(gx, gy));
			if (phiDeg < 0) phiDeg += 180;
			if (phiDeg >= 180) phiDeg -= 180;
			result.phi[i] = phiDeg;
			result.weight[i] = (float) Math.sqrt(gx * gx + gy * gy);
		}
		return result;
	}

	private static ArrayImg<FloatType, FloatArray> gauss(
		final Computers.Arity2<RandomAccessibleInterval<FloatType>, Double, RandomAccessibleInterval<FloatType>> gaussOp,
		final ArrayImg<FloatType, FloatArray> in, final double sigma)
	{
		final var out = ArrayImgs.floats(in.dimensionsAsLongArray());
		gaussOp.compute(in, sigma, out);
		return out;
	}

	private static ArrayImg<FloatType, FloatArray> subtract(
		final ArrayImg<FloatType, FloatArray> a,
		final ArrayImg<FloatType, FloatArray> b)
	{
		final var out = ArrayImgs.floats(a.dimensionsAsLongArray());
		final var pa = a.update(null).getCurrentStorageArray();
		final var pb = b.update(null).getCurrentStorageArray();
		final var po = out.update(null).getCurrentStorageArray();
		for (var i = 0; i < po.length; i++)
			po[i] = pa[i] - pb[i];
		return out;
	}

	/**
	 * Correlates with a 3x3 kernel (row-major, first row at {@code y - 1}),
	 * replicating the edge pixels, as ImageJ's {@code convolve3x3} does.
	 */
	private static float[] convolve3x3(final float[] p, final int w,
		final int h, final int[] kernel)
	{
		final var out = new float[w * h];
		for (var y = 0; y < h; y++) {
			for (var x = 0; x < w; x++) {
				var sum = 0.0;
				var k = 0;
				for (var dy = -1; dy <= 1; dy++) {
					final var yy = Math.min(h - 1, Math.max(0, y + dy));
					for (var dx = -1; dx <= 1; dx++) {
						final var xx = Math.min(w - 1, Math.max(0, x + dx));
						sum += kernel[k++] * p[xx + yy * w];
					}
				}
				out[x + y * w] = (float) sum;
			}
		}
		return out;
	}

	/**
	 * Validated mean over a disk of radius {@code rAv}: the (optionally
	 * weighted) mean of the valid values within the disk, or NaN if fewer than
	 * 2/3 of the disk pixels inside the image are valid or if the standard
	 * deviation of the valid values exceeds {@code sigmaMax}. With weights,
	 * values with zero weight are invalid.
	 */
	private static void average(final int w, final int h, final int rAv,
		final double sigmaMax, final float[] data, final float[] weight,
		final float[] av)
	{
		final var varMax = sigmaMax * sigmaMax;
		final var maskWidth = 2 * rAv + 1;
		final var mask = new boolean[maskWidth * maskWidth];
		var m = 0;
		for (var jm = -rAv; jm <= rAv; jm++) {
			for (var im = -rAv; im <= rAv; im++) {
				mask[m++] = Math.sqrt(im * im + jm * jm) <= rAv;
			}
		}
		final var x = new float[maskWidth * maskWidth];
		final var wt = new float[maskWidth * maskWidth];
		for (var jCent = 0; jCent < h; jCent++) {
			for (var iCent = 0; iCent < w; iCent++) {
				var nRoi = 0;
				var nValid = 0;
				m = 0;
				for (var jm = -rAv; jm <= rAv; jm++) {
					for (var im = -rAv; im <= rAv; im++) {
						if (!mask[m++]) continue;
						final var j = jCent + jm;
						final var i = iCent + im;
						if (j < 0 || j >= h || i < 0 || i >= w) continue;
						final var ind = i + j * w;
						nRoi++;
						final var d = data[ind];
						if (Float.isNaN(d)) continue;
						if (weight != null && !(weight[ind] > 0)) continue;
						x[nValid] = d;
						wt[nValid++] = weight == null ? 1 : weight[ind];
					}
				}
				final var ind = iCent + jCent * w;
				av[ind] = Float.NaN;
				// Note: integer division, as in the original plugin.
				if (nValid == 0 || nValid < 2 * nRoi / 3) continue;
				var sum = 0.0;
				var wSum = 0.0;
				for (var s = 0; s < nValid; s++) {
					sum += wt[s] * x[s];
					wSum += wt[s];
				}
				final var mu = sum / wSum;
				var sumSq = 0.0;
				for (var s = 0; s < nValid; s++) {
					sumSq += wt[s] * (x[s] - mu) * (x[s] - mu);
				}
				if (sumSq / wSum <= varMax) av[ind] = (float) mu;
			}
		}
	}

	private static RandomAccessibleInterval<FloatType> wrap(final float[] data,
		final int w, final int h)
	{
		return ArrayImgs.floats(data, w, h);
	}
}
