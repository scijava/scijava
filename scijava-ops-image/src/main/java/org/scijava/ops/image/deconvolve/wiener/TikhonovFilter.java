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
 * Tikhonov-regularized inverse filter: the minimizer of
 * {@code ||h * x - y||^2 + lambda ||L x||^2}, computed directly in frequency
 * space as {@code X = conj(H) Y / (|H|^2 + lambda |L|^2)}, where {@code L} is
 * a discrete derivative operator of the given order:
 * <ul>
 * <li>order 0: the identity, so this equals {@code deconvolve.wiener} with
 * {@code nsr = lambda};</li>
 * <li>order 1: the forward-difference gradient, penalizing
 * {@code ||grad x||^2};</li>
 * <li>order 2 (default): the discrete Laplacian, also known as the
 * Tikhonov-Miller or constrained least squares filter.</li>
 * </ul>
 * In frequency space, {@code |L|^2 = P^order}, where
 * {@code P = sum_d 4 sin^2(pi k_d / N_d)} is the symbol of the negative
 * discrete Laplacian on the padded image of size {@code N}.
 * <p>
 * Padding conventions are those of {@code deconvolve.wiener}.
 * </p>
 * <p>
 * References:
 * </p>
 * <ul>
 * <li>Tikhonov, A. N., and Arsenin, V. Y. (1977). Solutions of Ill-Posed
 * Problems. Winston.</li>
 * <li>Hunt, B. R. (1973). The application of constrained least squares
 * estimation to image restoration by digital computer. IEEE Transactions on
 * Computers C-22(9), 805-812.</li>
 * <li>Gonzalez, R. C., and Woods, R. E. (2008). Digital Image Processing, 3rd
 * ed., section 5.9. Prentice Hall.</li>
 * </ul>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <I> type of the input image
 * @param <O> type of the output image
 * @param <K> type of the kernel
 * @param <C> complex type used in frequency space
 * @implNote op names='deconvolve.tikhonov'
 */
public class TikhonovFilter<I extends RealType<I> & NativeType<I>, O extends RealType<O> & NativeType<O>, K extends RealType<K> & NativeType<K>, C extends ComplexType<C> & NativeType<C>>
	extends AbstractInverseFilter<I, O, K, C> implements
	Functions.Arity8<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, O, C, Double, Integer, long[], OutOfBoundsFactory<I, RandomAccessibleInterval<I>>, RandomAccessibleInterval<O>>
{

	/** Default regularization parameter. */
	public static final double DEFAULT_LAMBDA = 1e-3;

	/** Default derivative order of the regularization operator. */
	public static final int DEFAULT_ORDER = 2;

	/**
	 * Deconvolves an image with a Tikhonov-regularized inverse filter.
	 *
	 * @param input the image to deconvolve
	 * @param kernel the PSF, normalized to unit sum
	 * @param outType the type of the output image
	 * @param complexType the complex type used in frequency space
	 * @param lambda the regularization parameter, at least 0 (default 1e-3)
	 * @param order the derivative order of the regularization operator: 0 for
	 *          identity, 1 for gradient, or 2 for Laplacian (default 2)
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
		@Nullable Double lambda, //
		@Nullable Integer order, //
		@Nullable final long[] borderSize, //
		@Nullable final OutOfBoundsFactory<I, RandomAccessibleInterval<I>> obfInput //
	) {
		if (lambda == null) lambda = DEFAULT_LAMBDA;
		if (order == null) order = DEFAULT_ORDER;
		if (order < 0 || order > 2) {
			throw new IllegalArgumentException(
				"Regularization order must be 0, 1 or 2: " + order);
		}
		final int o = order;
		return filter(input, kernel, outType, complexType, lambda, //
			(pos, realSize) -> weight(o, pos, realSize), borderSize, obfInput);
	}

	private static double weight(final int order, final long[] pos,
		final long[] realSize)
	{
		if (order == 0) return 1;
		var p = 0.0;
		for (var d = 0; d < pos.length; d++) {
			final var s = Math.sin(Math.PI * pos[d] / realSize[d]);
			p += 4 * s * s;
		}
		return order == 1 ? p : p * p;
	}
}

