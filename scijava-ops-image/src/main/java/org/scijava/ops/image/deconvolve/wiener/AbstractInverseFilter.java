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

package org.scijava.ops.image.deconvolve.wiener;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.OpDependency;

/**
 * Base class for direct (non-iterative) regularized inverse filters, which
 * compute the estimate {@code X = conj(H) Y / (|H|^2 + lambda W)} in frequency
 * space, where {@code Y} and {@code H} are the Fourier transforms of the
 * padded image and PSF, and {@code W} is a nonnegative per-frequency
 * regularization weight.
 * <p>
 * Padding, kernel shifting and the FFTs reuse the same ops as
 * {@code deconvolve.richardsonLucy} and {@code filter.convolve}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <I> type of the input image
 * @param <O> type of the output image
 * @param <K> type of the kernel
 * @param <C> complex type used in frequency space
 */
public abstract class AbstractInverseFilter<I extends RealType<I> & NativeType<I>, O extends RealType<O> & NativeType<O>, K extends RealType<K> & NativeType<K>, C extends ComplexType<C> & NativeType<C>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, O, RandomAccessibleInterval<O>> outputCreator;

	@OpDependency(name = "filter.padInputFFTMethods")
	private Functions.Arity4<RandomAccessibleInterval<I>, Dimensions, Boolean, OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<I>> padOp;

	@OpDependency(name = "filter.padShiftKernelFFTMethods")
	private BiFunction<RandomAccessibleInterval<K>, Dimensions, RandomAccessibleInterval<K>> padKernelOp;

	@OpDependency(name = "filter.createFFTOutput")
	private Functions.Arity3<Dimensions, C, Boolean, RandomAccessibleInterval<C>> createOp;

	@OpDependency(name = "filter.linearFilter")
	private Computers.Arity7<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, Boolean, Boolean, Computers.Arity2<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<O>> linearFilter;

	/** Regularization weight {@code W} as a function of frequency. */
	@FunctionalInterface
	protected interface Weight {

		/**
		 * Gets the regularization weight at one frequency.
		 *
		 * @param pos position of the frequency sample in the (real-to-complex)
		 *          FFT array
		 * @param realSize size of the padded image in real space
		 * @return the regularization weight
		 */
		double at(long[] pos, long[] realSize);
	}

	/**
	 * Pads the input and kernel, then applies the inverse filter.
	 *
	 * @param input the image to deconvolve
	 * @param kernel the PSF
	 * @param outType the type of the output image
	 * @param complexType the complex type used in frequency space
	 * @param lambda the regularization parameter
	 * @param weight the regularization weight, or null for a constant 1
	 * @param borderSize the border size in each dimension, or null to pad by
	 *          the kernel size
	 * @param obfInput the out-of-bounds strategy for the input, or null for
	 *          single-boundary mirroring
	 * @return the deconvolved image, with the same dimensions as the input
	 */
	protected RandomAccessibleInterval<O> filter(
		final RandomAccessibleInterval<I> input,
		final RandomAccessibleInterval<K> kernel, final O outType,
		final C complexType, final double lambda, final Weight weight,
		final long[] borderSize, OutOfBoundsFactory<I, RandomAccessibleInterval<I>> obfInput)
	{
		if (!(lambda >= 0)) {
			throw new IllegalArgumentException(
				"Regularization parameter must be nonnegative: " + lambda);
		}
		if (obfInput == null) {
			obfInput = new OutOfBoundsMirrorFactory<>(
				OutOfBoundsMirrorFactory.Boundary.SINGLE);
		}

		final var paddedSize = paddedSize(input, kernel, borderSize);
		final var paddedInput = padOp.apply(input, new FinalDimensions(
			paddedSize), true, obfInput);
		final var paddedKernel = padKernelOp.apply(kernel, new FinalDimensions(
			paddedSize));

		final var fftSize = new FinalDimensions(paddedSize);
		final var fftInput = createOp.apply(fftSize, complexType, true);
		final var fftKernel = createOp.apply(fftSize, complexType, true);

		final var realSize = paddedInput.dimensionsAsLongArray();
		final Computers.Arity2<RandomAccessibleInterval<C>, RandomAccessibleInterval<C>, RandomAccessibleInterval<C>> frequencyOp =
			(fftY, fftH, out) -> invert(fftY, fftH, out, lambda, weight, realSize);

		// Note: filter.ifft crops the centered region of the padded result, which
		// is where filter.padInputFFTMethods placed the input.
		final var output = outputCreator.apply(input, outType);
		linearFilter.compute(paddedInput, paddedKernel, true, true, frequencyOp,
			fftInput, fftKernel, output);
		return output;
	}

	private void invert(final RandomAccessibleInterval<C> fftY,
		final RandomAccessibleInterval<C> fftH,
		final RandomAccessibleInterval<C> out, final double lambda,
		final Weight weight, final long[] realSize)
	{
		final var pos = new long[fftY.numDimensions()];
		final var cy = Views.flatIterable(fftY).localizingCursor();
		final var ch = Views.flatIterable(fftH).cursor();
		final var co = Views.flatIterable(out).cursor();
		while (cy.hasNext()) {
			final var y = cy.next();
			final var h = ch.next();
			final var o = co.next();
			cy.localize(pos);
			final var hr = h.getRealDouble();
			final var hi = h.getImaginaryDouble();
			final var yr = y.getRealDouble();
			final var yi = y.getImaginaryDouble();
			final var denom = hr * hr + hi * hi + lambda * //
				(weight == null ? 1 : weight.at(pos, realSize));
			if (denom > 0) {
				// conj(H) * Y / denom
				o.setReal((hr * yr + hi * yi) / denom);
				o.setImaginary((hr * yi - hi * yr) / denom);
			}
			else {
				o.setReal(0);
				o.setImaginary(0);
			}
		}
	}

	/**
	 * Computes the padded size the same way as {@code deconvolve.richardsonLucy}
	 * and {@code filter.convolve}.
	 */
	static long[] paddedSize(final Dimensions input, final Dimensions kernel,
		final long[] borderSize)
	{
		final var paddedSize = new long[input.numDimensions()];
		for (var d = 0; d < paddedSize.length; d++) {
			paddedSize[d] = borderSize == null ? //
				input.dimension(d) + kernel.dimension(d) - 1 : //
				Math.max(kernel.dimension(d), input.dimension(d)) + 2 * borderSize[d];
		}
		return paddedSize;
	}
}
