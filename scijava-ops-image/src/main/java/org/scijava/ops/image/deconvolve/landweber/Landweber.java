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

package org.scijava.ops.image.deconvolve.landweber;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;
import org.scijava.progress.Progress;

/**
 * Landweber deconvolution: gradient descent on the least squares misfit
 * {@code ||h * x - y||^2 / 2}, iterating
 * {@code x <- x + tau h^T (y - h * x)}, where {@code h^T} is correlation with
 * the PSF. The iteration converges for step sizes {@code 0 < tau < 2 / max|H|^2}
 * (that is, {@code tau < 2} for a PSF normalized to unit sum), and the
 * iteration count acts as the regularization parameter. With the
 * nonnegativity constraint, each iterate is projected onto {@code x >= 0}
 * (projected Landweber).
 * <p>
 * The image is padded as in {@code deconvolve.richardsonLucy}. In the default
 * circulant mode, the image is padded (by default with single-boundary
 * mirroring) and the iteration starts from the padded image. In non-circulant
 * mode, the object is estimated on the padded domain while the residual is
 * only evaluated inside the measured window, which is the least squares
 * analogue of the boundary handling of Bertero and Boccacci used by
 * {@code deconvolve.richardsonLucy}; the iteration then starts from a
 * constant image equal to the mean of the input.
 * </p>
 * <p>
 * References:
 * </p>
 * <ul>
 * <li>Landweber, L. (1951). An iteration formula for Fredholm integral
 * equations of the first kind. American Journal of Mathematics 73(3),
 * 615-624.</li>
 * <li>Eicke, B. (1992). Iteration methods for convexly constrained ill-posed
 * problems in Hilbert space. Numerical Functional Analysis and Optimization
 * 13(5-6), 413-429.</li>
 * <li>Bertero, M., and Boccacci, P. (1998). Introduction to Inverse Problems
 * in Imaging. IOP Publishing.</li>
 * <li>Bertero, M., and Boccacci, P. (2005). A simple method for the reduction
 * of boundary effects in the Richardson-Lucy approach to image deconvolution.
 * Astronomy and Astrophysics 437, 369-374.</li>
 * </ul>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <I> type of the input image
 * @param <O> type of the output image
 * @param <K> type of the kernel
 * @param <C> complex type used in frequency space
 * @implNote op names='deconvolve.landweber'
 */
public class Landweber<I extends RealType<I> & NativeType<I>, O extends RealType<O> & NativeType<O>, K extends RealType<K> & NativeType<K>, C extends ComplexType<C> & NativeType<C>>
	implements
	Functions.Arity10<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, O, C, Integer, Double, Boolean, Boolean, long[], OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<O>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, O, Img<O>> createImg;

	@OpDependency(name = "filter.padInputFFTMethods")
	private Functions.Arity4<RandomAccessibleInterval<I>, Dimensions, Boolean, OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<I>> padOp;

	@OpDependency(name = "filter.padShiftKernelFFTMethods")
	private BiFunction<RandomAccessibleInterval<K>, Dimensions, RandomAccessibleInterval<K>> padKernelOp;

	@OpDependency(name = "filter.createFFTOutput")
	private Functions.Arity3<Dimensions, C, Boolean, RandomAccessibleInterval<C>> createFFTOp;

	@OpDependency(name = "filter.fft")
	private Computers.Arity1<RandomAccessibleInterval<K>, RandomAccessibleInterval<C>> fftKernelOp;

	@OpDependency(name = "filter.linearFilter")
	private Computers.Arity7<RandomAccessibleInterval<O>, RandomAccessibleInterval<K>, Boolean, Boolean, Computers.Arity2<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<O>> linearFilter;

	@OpDependency(name = "stats.mean")
	private Computers.Arity1<Iterable<I>, DoubleType> meanOp;

	@OpDependency(name = "copy.rai")
	private Computers.Arity1<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>> copyInputOp;

	@OpDependency(name = "copy.rai")
	private Computers.Arity1<RandomAccessibleInterval<O>, RandomAccessibleInterval<O>> copyOp;

	/**
	 * Deconvolves an image with the (projected) Landweber iteration.
	 *
	 * @param input the image to deconvolve
	 * @param kernel the PSF
	 * @param outType the type of the output image
	 * @param complexType the complex type used in frequency space
	 * @param maxIterations number of iterations to perform
	 * @param stepSize the step size tau, in (0, 2 / max|H|^2) for convergence
	 *          (default 1 / max|H|^2, which is 1 for a PSF normalized to unit
	 *          sum)
	 * @param nonNegative whether to project each iterate onto nonnegative values
	 *          (default true)
	 * @param nonCirculant whether to use non-circulant edge handling (default
	 *          false)
	 * @param borderSize the padding added to each side in each dimension
	 *          (default: pad by the kernel size)
	 * @param obfInput the out-of-bounds strategy used to pad the input (default:
	 *          single-boundary mirroring when circulant, zero when
	 *          non-circulant)
	 * @return the deconvolved image, with the dimensions of the input
	 */
	@Override
	public RandomAccessibleInterval<O> apply( //
		final RandomAccessibleInterval<I> input, //
		final RandomAccessibleInterval<K> kernel, //
		final O outType, //
		final C complexType, //
		final Integer maxIterations, //
		@Nullable Double stepSize, //
		@Nullable Boolean nonNegative, //
		@Nullable Boolean nonCirculant, //
		@Nullable final long[] borderSize, //
		@Nullable OutOfBoundsFactory<I, RandomAccessibleInterval<I>> obfInput //
	) {
		if (maxIterations < 0) {
			throw new IllegalArgumentException(
				"Iteration count must be nonnegative: " + maxIterations);
		}
		if (stepSize != null && !(stepSize > 0)) {
			throw new IllegalArgumentException("Step size must be positive: " +
				stepSize);
		}
		if (nonNegative == null) nonNegative = true;
		if (nonCirculant == null) nonCirculant = false;
		if (obfInput == null) {
			obfInput = nonCirculant ? //
				new OutOfBoundsConstantValueFactory<>(Util.getTypeFromInterval(input)
					.createVariable()) : //
				new OutOfBoundsMirrorFactory<>(OutOfBoundsMirrorFactory.Boundary.SINGLE);
		}

		// Pad the input and kernel to a fast FFT size.
		final var n = input.numDimensions();
		final var paddedSize = new long[n];
		for (var d = 0; d < n; d++) {
			paddedSize[d] = borderSize == null ? //
				input.dimension(d) + kernel.dimension(d) - 1 : //
				Math.max(kernel.dimension(d), input.dimension(d)) + 2 * borderSize[d];
		}
		final var paddedInput = padOp.apply(input, new FinalDimensions(
			paddedSize), true, obfInput);
		final var paddedKernel = padKernelOp.apply(kernel, new FinalDimensions(
			paddedSize));
		final var paddedDims = new FinalDimensions(paddedInput
			.dimensionsAsLongArray());

		// Location of the measured window within the zero-min padded domain.
		final var winMin = new long[n];
		final var winMax = new long[n];
		for (var d = 0; d < n; d++) {
			winMin[d] = input.min(d) - paddedInput.min(d);
			winMax[d] = winMin[d] + input.dimension(d) - 1;
		}
		final var window = new FinalInterval(winMin, winMax);

		// Transform the kernel once, and find the Lipschitz constant max|H|^2.
		final var fftBuffer = createFFTOp.apply(paddedDims, complexType, true);
		final var fftKernel = createFFTOp.apply(paddedDims, complexType, true);
		fftKernelOp.compute(paddedKernel, fftKernel);
		var maxPower = 0.0;
		for (final var h : Views.iterable(fftKernel)) {
			maxPower = Math.max(maxPower, h.getPowerDouble());
		}
		if (!(maxPower > 0)) {
			throw new IllegalArgumentException("Kernel has no nonzero frequencies");
		}
		final double tau = stepSize == null ? 1 / maxPower : stepSize;

		// Measured data on the padded domain, and the first estimate.
		final var y = createImg.apply(paddedDims, outType);
		copyInputOp.compute(Views.zeroMin(paddedInput), y);
		final var x = createImg.apply(paddedDims, outType);
		if (nonCirculant) {
			final var mean = new DoubleType();
			meanOp.compute(Views.iterable(input), mean);
			final var m = mean.getRealDouble();
			for (final var v : x)
				v.setReal(m);
		}
		else copyOp.compute(y, x);
		if (nonNegative) clampNonNegative(x);

		final Computers.Arity2<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>> convolve =
			(fx, fh, out) -> multiply(fx, fh, out, false);
		final Computers.Arity2<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>> correlate =
			(fx, fh, out) -> multiply(fx, fh, out, true);

		final var residual = createImg.apply(paddedDims, outType);
		final var reblurred = nonCirculant ? //
			createImg.apply(paddedDims, outType) : residual;
		final boolean project = nonNegative;

		Progress.defineTotal(maxIterations);
		for (var i = 0; i < maxIterations; i++) {
			// residual = y - h * x, restricted to the window if non-circulant
			linearFilter.compute(x, null, true, false, convolve, fftBuffer,
				fftKernel, reblurred);
			if (nonCirculant) {
				for (final var v : residual)
					v.setZero();
				LoopBuilder.setImages(Views.interval(residual, window), Views.interval(
					y, window), Views.interval(reblurred, window)).forEachPixel((r, yv,
						hx) -> r.setReal(yv.getRealDouble() - hx.getRealDouble()));
			}
			else {
				LoopBuilder.setImages(residual, y).forEachPixel((r, yv) -> r.setReal(
					yv.getRealDouble() - r.getRealDouble()));
			}

			// x = x + tau h^T residual, optionally projected onto x >= 0
			linearFilter.compute(residual, null, true, false, correlate, fftBuffer,
				fftKernel, residual);
			LoopBuilder.setImages(x, residual).forEachPixel((xv, g) -> {
				final var v = xv.getRealDouble() + tau * g.getRealDouble();
				xv.setReal(project && v < 0 ? 0 : v);
			});
			Progress.update();
		}

		// Crop the measured window.
		final var output = createImg.apply(input, outType);
		copyOp.compute(Views.zeroMin(Views.interval(x, window)), output);
		return output;
	}

	private void clampNonNegative(final Img<O> img) {
		for (final var v : img) {
			if (v.getRealDouble() < 0) v.setZero();
		}
	}

	/** Computes {@code out = X H} or {@code out = X conj(H)}. */
	private void multiply(final RandomAccessibleInterval<C> fftX,
		final RandomAccessibleInterval<C> fftH,
		final RandomAccessibleInterval<C> out, final boolean conjugate)
	{
		LoopBuilder.setImages(fftX, fftH, out).forEachPixel((xc, hc, o) -> {
			final var xr = xc.getRealDouble();
			final var xi = xc.getImaginaryDouble();
			final var hr = hc.getRealDouble();
			final var hi = conjugate ? -hc.getImaginaryDouble() : hc
				.getImaginaryDouble();
			o.setReal(xr * hr - xi * hi);
			o.setImaginary(xr * hi + xi * hr);
		});
	}
}

