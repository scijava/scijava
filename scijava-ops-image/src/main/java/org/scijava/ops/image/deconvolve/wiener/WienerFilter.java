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

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;

/**
 * Wiener deconvolution with a constant noise-to-signal power ratio (the
 * parametric Wiener filter): {@code X = conj(H) Y / (|H|^2 + K)}, where
 * {@code Y} and {@code H} are the Fourier transforms of the image and PSF and
 * {@code K} is the noise-to-signal ratio. With {@code K = 0} this is the plain
 * inverse filter {@code Y / H}, restricted to frequencies where {@code H} is
 * nonzero.
 * <p>
 * This is also zeroth-order Tikhonov regularization of the inverse filter; see
 * {@code deconvolve.tikhonov} for smoothness (gradient or Laplacian)
 * regularization. The scikit-image {@code wiener} function reduces to this
 * filter when its {@code reg} operator is the identity (its default is a
 * Laplacian, which corresponds to {@code deconvolve.tikhonov}).
 * </p>
 * <p>
 * The image is padded as in {@code deconvolve.richardsonLucy} (circulant
 * model), by default by the kernel size using single-boundary mirroring, and
 * the result is cropped back to the input size. The PSF is expected to be
 * normalized to unit sum, so that {@code K} is relative to the zero-frequency
 * response.
 * </p>
 * <p>
 * References:
 * </p>
 * <ul>
 * <li>Wiener, N. (1949). Extrapolation, Interpolation, and Smoothing of
 * Stationary Time Series. MIT Press.</li>
 * <li>Gonzalez, R. C., and Woods, R. E. (2008). Digital Image Processing, 3rd
 * ed., section 5.8. Prentice Hall.</li>
 * </ul>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <I> type of the input image
 * @param <O> type of the output image
 * @param <K> type of the kernel
 * @param <C> complex type used in frequency space
 * @implNote op names='deconvolve.wiener'
 */
public class WienerFilter<I extends RealType<I> & NativeType<I>, O extends RealType<O> & NativeType<O>, K extends RealType<K> & NativeType<K>, C extends ComplexType<C> & NativeType<C>>
	extends AbstractInverseFilter<I, O, K, C> implements
	Functions.Arity7<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, O, C, Double, long[], OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<O>>
{

	/** Default noise-to-signal power ratio. */
	public static final double DEFAULT_NSR = 1e-3;

	/**
	 * Deconvolves an image with the Wiener filter.
	 *
	 * @param input the image to deconvolve
	 * @param kernel the PSF, normalized to unit sum
	 * @param outType the type of the output image
	 * @param complexType the complex type used in frequency space
	 * @param nsr the noise-to-signal power ratio K, at least 0 (default 1e-3);
	 *          0 yields the plain inverse filter
	 * @param borderSize the padding added to each side in each dimension
	 *          (default: pad by the kernel size)
	 * @param obfInput the out-of-bounds strategy used to pad the input (default:
	 *          single-boundary mirroring)
	 * @return the deconvolved image, with the dimensions of the input
	 */
	@Override
	public RandomAccessibleInterval<O> apply( //
		final RandomAccessibleInterval<I> input, //
		final RandomAccessibleInterval<K> kernel, //
		final O outType, //
		final C complexType, //
		@Nullable Double nsr, //
		@Nullable final long[] borderSize, //
		@Nullable final OutOfBoundsFactory<I, RandomAccessibleInterval<I>> obfInput //
	) {
		if (nsr == null) nsr = DEFAULT_NSR;
		return filter(input, kernel, outType, complexType, nsr, null, borderSize,
			obfInput);
	}
}

