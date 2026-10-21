/*-
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

package org.scijava.ops.image.filter.tensor;

import java.util.Arrays;

import net.imglib2.FinalDimensions;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gauss3.SeparableSymmetricConvolution;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.algorithm.linalg.eigen.TensorEigenValues;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.concurrent.Parallelization;

/**
 * Shared machinery for the ops in this package: Gaussian smoothing, Hessian
 * matrix, structure tensor and sorted tensor eigenvalues, all computed in
 * double precision using imglib2-algorithm.
 * <p>
 * Symmetric tensors are stored as images with one extra (last) dimension of
 * size {@code n(n+1)/2}, holding the upper triangle in row-major order
 * {@code [t11, t12, ..., t1n, t22, t23, ..., tnn]}. This is the layout used by
 * {@code HessianMatrix} of imglib2-algorithm and expected by {@link TensorEigenValues}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
final class TensorUtils {

	private TensorUtils() {
		// Prevent instantiation of static utility class.
	}

	/** Number of independent entries of a symmetric {@code n x n} tensor. */
	static int tensorSize(final int n) {
		return n * (n + 1) / 2;
	}

	/**
	 * Converts a physical-unit scale into per-dimension pixel-unit sigmas.
	 *
	 * @param n number of dimensions
	 * @param sigma scale in physical units (must be non-negative)
	 * @param calibration pixel spacing per dimension, or null for unit spacing
	 * @return {@code sigma / calibration[d]} for each dimension {@code d}
	 */
	static double[] pixelSigmas(final int n, final double sigma,
		final double[] calibration)
	{
		if (sigma < 0 || Double.isNaN(sigma)) {
			throw new IllegalArgumentException("Sigma must be non-negative: " +
				sigma);
		}
		final var spacing = spacing(n, calibration);
		final var sigmas = new double[n];
		for (var d = 0; d < n; d++)
			sigmas[d] = sigma / spacing[d];
		return sigmas;
	}

	/**
	 * Gets the pixel spacing per dimension, defaulting to 1 where the given
	 * calibration is missing.
	 */
	static double[] spacing(final int n, final double[] calibration) {
		final var spacing = new double[n];
		for (var d = 0; d < n; d++) {
			spacing[d] = calibration != null && d < calibration.length
				? calibration[d] : 1;
			if (!(spacing[d] > 0)) {
				throw new IllegalArgumentException(
					"Calibration must be positive: " + Arrays.toString(calibration));
			}
		}
		return spacing;
	}

	/** Ensures the input has the expected dimensionality. */
	static void checkDimensions(final RandomAccessibleInterval<?> input,
		final int n, final String opName)
	{
		if (input.numDimensions() != n) {
			throw new IllegalArgumentException(opName + " requires a " + n +
				"D image, but the input is " + input.numDimensions() + "D");
		}
	}

	/** Creates a double-precision image with the given dimensions. */
	static Img<DoubleType> createImg(final long... dims) {
		return Util.getSuitableImgFactory(new FinalDimensions(dims),
			new DoubleType()).create(dims);
	}

	/** Creates an n+1 dimensional image with {@code c} channels appended. */
	static Img<DoubleType> createImg(final RandomAccessibleInterval<?> input,
		final long c)
	{
		final var n = input.numDimensions();
		final var dims = Arrays.copyOf(input.dimensionsAsLongArray(), n + 1);
		dims[n] = c;
		return createImg(dims);
	}

	/**
	 * Smooths the source with a separable Gaussian. A sigma of zero along a
	 * dimension means no smoothing along that dimension.
	 *
	 * @param source extended source
	 * @param sigmas per-dimension sigmas in pixel units
	 * @param target the output interval
	 */
	static <T extends RealType<T>> void gauss(final RandomAccessible<T> source,
		final double[] sigmas, final RandomAccessibleInterval<DoubleType> target)
	{
		final var halfkernels = new double[sigmas.length][];
		for (var d = 0; d < sigmas.length; d++) {
			halfkernels[d] = sigmas[d] > 0 //
				? Gauss3.halfkernels(new double[] { sigmas[d] })[0] //
				: new double[] { 1 };
		}
		try {
			SeparableSymmetricConvolution.convolve(halfkernels, source, target,
				Parallelization.getExecutorService());
		}
		catch (final IncompatibleTypeException exc) {
			throw new IllegalStateException(exc);
		}
	}

	/**
	 * Smooths the input with a Gaussian of the given per-dimension pixel
	 * sigmas, mirroring at the image border.
	 */
	static <T extends RealType<T>> Img<DoubleType> smooth(
		final RandomAccessibleInterval<T> input, final double[] sigmas)
	{
		final var zeroMin = Views.zeroMin(input);
		final var smoothed = createImg(zeroMin.dimensionsAsLongArray());
		gauss(Views.extendMirrorSingle(zeroMin), sigmas, smoothed);
		return smoothed;
	}

	/**
	 * Computes the Hessian matrix at scale {@code sigma}: second-order finite
	 * differences of the Gaussian-smoothed input, expressed in physical units.
	 *
	 * @param input n-dimensional input
	 * @param sigma Gaussian scale in physical units (0 for no smoothing)
	 * @param calibration pixel spacing per dimension, or null
	 * @param normalize whether to scale-normalize the result by
	 *          {@code sigma^2}
	 * @return n+1 dimensional tensor image
	 */
	static <T extends RealType<T>> Img<DoubleType> hessian(
		final RandomAccessibleInterval<T> input, final double sigma,
		final double[] calibration, final boolean normalize)
	{
		final var n = input.numDimensions();
		final var spacing = spacing(n, calibration);
		final var smoothed = smooth(input, pixelSigmas(n, sigma, calibration));
		final var hessian = createImg(smoothed, tensorSize(n));
		// Note: HessianMatrix of imglib2-algorithm differentiates the central
		// difference gradient again, i.e. it uses a stencil of step 2 for the
		// diagonal entries. The compact [1, -2, 1] stencil used here is about
		// four times more accurate for smooth structures at small scales.
		final var extended = Views.extendBorder(smoothed);
		for (int i = 0, k = 0; i < n; i++) {
			for (var j = i; j < n; j++, k++) {
				final var out = Views.hyperSlice(hessian, n, k);
				if (i == j) {
					LoopBuilder.setImages(shifted(extended, smoothed, i, -1, i, 0),
						shifted(extended, smoothed, i, 0, i, 0), shifted(extended, smoothed,
							i, 1, i, 0), out).multiThreaded().forEachPixel((b, c, f, h) -> h
								.setReal(f.get() - 2 * c.get() + b.get()));
				}
				else {
					LoopBuilder.setImages(shifted(extended, smoothed, i, 1, j, 1),
						shifted(extended, smoothed, i, 1, j, -1), shifted(extended,
							smoothed, i, -1, j, 1), shifted(extended, smoothed, i, -1, j, -1),
						out).multiThreaded().forEachPixel((pp, pm, mp, mm, h) -> h.setReal(
							0.25 * (pp.get() - pm.get() - mp.get() + mm.get())));
				}
			}
		}
		// Convert from pixel units into physical units, and scale-normalize.
		final var s2 = normalize ? sigma * sigma : 1;
		for (int i = 0, k = 0; i < n; i++) {
			for (var j = i; j < n; j++, k++) {
				final var factor = s2 / (spacing[i] * spacing[j]);
				if (factor != 1) scale(Views.hyperSlice(hessian, n, k), factor);
			}
		}
		return hessian;
	}

	/**
	 * Computes the structure tensor: the Gaussian-weighted (integration scale)
	 * average of the outer product of the gradient, where the gradient is
	 * computed by central differences of the input smoothed at the gradient
	 * scale.
	 *
	 * @param input n-dimensional input
	 * @param sigmaGradient Gaussian scale for gradient computation in physical
	 *          units (0 for no pre-smoothing)
	 * @param sigmaIntegration Gaussian scale for the averaging of the gradient
	 *          products, in physical units
	 * @param calibration pixel spacing per dimension, or null
	 * @return n+1 dimensional tensor image
	 */
	static <T extends RealType<T>> Img<DoubleType> structureTensor(
		final RandomAccessibleInterval<T> input, final double sigmaGradient,
		final double sigmaIntegration, final double[] calibration)
	{
		final var n = input.numDimensions();
		final var spacing = spacing(n, calibration);
		final var integrationSigmas = pixelSigmas(n, sigmaIntegration,
			calibration);
		final var smoothed = smooth(input, pixelSigmas(n, sigmaGradient,
			calibration));

		// Compute the gradient, in physical units.
		final var gradient = createImg(smoothed, n);
		final var extended = Views.extendBorder(smoothed);
		for (var d = 0; d < n; d++) {
			final var slice = Views.hyperSlice(gradient, n, d);
			PartialDerivative.gradientCentralDifference(extended, slice, d);
			if (spacing[d] != 1) scale(slice, 1 / spacing[d]);
		}

		// Compute the outer products of the gradient.
		final var products = createImg(smoothed, tensorSize(n));
		LoopBuilder.setImages(Views.collapse(gradient), Views.collapse(products))
			.multiThreaded().forEachPixel((g, p) -> {
				for (int i = 0, k = 0; i < n; i++) {
					final var gi = g.get(i).getRealDouble();
					for (var j = i; j < n; j++, k++)
						p.get(k).setReal(gi * g.get(j).getRealDouble());
				}
			});

		// Integrate by smoothing each product image.
		final var tensor = createImg(smoothed, tensorSize(n));
		for (var k = 0; k < tensorSize(n); k++) {
			gauss(Views.extendMirrorSingle(Views.hyperSlice(products, n, k)),
				integrationSigmas, Views.hyperSlice(tensor, n, k));
		}
		return tensor;
	}

	/**
	 * Computes the eigenvalues of each symmetric tensor and sorts them.
	 *
	 * @param tensor n+1 dimensional tensor image
	 * @param byMagnitude if true, sort ascending by absolute value
	 *          ({@code |λ1| <= |λ2| <= ...}); otherwise, sort descending by
	 *          value ({@code λ1 >= λ2 >= ...})
	 * @return n+1 dimensional image with n eigenvalues along the last
	 *         dimension
	 */
	static Img<DoubleType> eigenvalues(final RandomAccessibleInterval<DoubleType> tensor,
		final boolean byMagnitude)
	{
		final var n = tensor.numDimensions() - 1;
		final var evs = createImg(Views.hyperSlice(tensor, n, 0), n);
		TensorEigenValues.calculateEigenValuesSymmetric(tensor, evs, nTasks(),
			Parallelization.getExecutorService());
		LoopBuilder.setImages(Views.collapse(evs)).multiThreaded().forEachChunk(
			chunk -> {
				final var values = new double[n];
				chunk.forEachPixel(c -> {
					for (var i = 0; i < n; i++)
						values[i] = c.get(i).getRealDouble();
					sort(values, byMagnitude);
					for (var i = 0; i < n; i++)
						c.get(i).setReal(values[i]);
				});
				return null;
			});
		return evs;
	}

	/**
	 * Sorts the values in place: ascending by absolute value if
	 * {@code byMagnitude}, otherwise descending by value.
	 */
	static void sort(final double[] values, final boolean byMagnitude) {
		// Use insertion sort, since the arrays are tiny (one entry per dimension).
		for (var i = 1; i < values.length; i++) {
			final var v = values[i];
			var j = i - 1;
			while (j >= 0 && (byMagnitude ? Math.abs(values[j]) > Math.abs(v)
				: values[j] < v))
			{
				values[j + 1] = values[j];
				j--;
			}
			values[j + 1] = v;
		}
	}

	private static void scale(final RandomAccessibleInterval<DoubleType> image,
		final double factor)
	{
		LoopBuilder.setImages(image).multiThreaded().forEachPixel(t -> t.mul(
			factor));
	}

	/**
	 * Gets the values of {@code source} over {@code interval}, shifted by
	 * {@code o1} along dimension {@code d1} and by {@code o2} along {@code d2}.
	 */
	private static RandomAccessibleInterval<DoubleType> shifted(
		final RandomAccessible<DoubleType> source, final Interval interval,
		final int d1, final long o1, final int d2, final long o2)
	{
		final var offset = new long[interval.numDimensions()];
		offset[d1] += o1;
		offset[d2] += o2;
		return Views.interval(source, Intervals.translate(interval, offset));
	}

	private static int nTasks() {
		return Runtime.getRuntime().availableProcessors();
	}
}
