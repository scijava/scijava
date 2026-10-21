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

import java.util.List;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.ops.spi.OpDependency;

/**
 * Isotropic undecimated wavelet transform (&agrave; trous algorithm with the
 * cubic B-spline scaling function, also called the starlet transform).
 * <p>
 * Starting from {@code c_0 = input}, each smooth plane {@code c_j} is
 * computed by convolving {@code c_(j-1)} along every dimension with the 1D
 * filter {@code (1, 4, 6, 4, 1) / 16}, whose taps are spaced
 * {@code 2^(j-1)} pixels apart ("with holes"). The wavelet plane of scale
 * {@code j} is {@code w_j = c_(j-1) - c_j}. Boundaries are handled by mirror
 * reflection (without repeating the border pixel). Dimensions of size 1 are
 * left unfiltered.
 * </p>
 * <p>
 * The result contains the {@code nScales} wavelet planes from fine to coarse,
 * followed by the residual {@code c_nScales}; the sum of all of them
 * reconstructs the input. Wavelet planes of scale 2 and 3 are often used as
 * spot-enhancing filters in single-molecule localization microscopy (Izeddin
 * et al. 2012).
 * </p>
 * <p>
 * Implemented from the description by Starck and Murtagh.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @see "J.-L. Starck, F. Murtagh and A. Bijaoui, Image Processing and Data
 *      Analysis: The Multiscale Approach, Cambridge University Press, 1998"
 * @see <a href="https://doi.org/10.1364/OE.20.002081">I. Izeddin et al.,
 *      "Wavelet analysis for single molecule localization microscopy", Opt.
 *      Express 20(3):2081-2095, 2012</a>
 * @implNote op names='filter.waveletATrous'
 */
public class WaveletATrous<T extends RealType<T>> implements
	BiFunction<RandomAccessibleInterval<T>, Integer, List<RandomAccessibleInterval<FloatType>>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> createOp;

	/**
	 * Decomposes an image into wavelet planes.
	 *
	 * @param input the image to decompose
	 * @param nScales the number of wavelet scales (at least 1)
	 * @return {@code nScales + 1} images of the dimensions of the input: the
	 *         wavelet planes from fine to coarse, followed by the residual
	 *         smooth plane
	 */
	@Override
	public List<RandomAccessibleInterval<FloatType>> apply(
		final RandomAccessibleInterval<T> input, final Integer nScales)
	{
		return Starlet.decompose(input, nScales, () -> createOp.apply(input,
			new FloatType()));
	}
}
