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

package org.scijava.ops.image.color.deconvolve;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Separates the stains of a brightfield RGB image by colour deconvolution
 * (Ruifrok and Johnston, 2001).
 * <p>
 * Each RGB value is converted to optical density {@code OD = -log10(I / I0)}
 * and multiplied by the inverse of the stain matrix, whose rows are the
 * unit-length OD vectors of the stains. The result is the amount of each stain
 * at each pixel, in OD units: a pixel with stain amounts {@code c} has
 * {@code OD = c · M}. When only two stain vectors are given, the third is
 * their normalized cross product. Zero intensities are clamped to one grey
 * level for integer images, and to a transmittance of 10<sup>-6</sup>
 * otherwise.
 * </p>
 * <p>
 * Implemented from the paper; see {@link StainVectors} for published stain
 * vectors.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @see <a href="https://pubmed.ncbi.nlm.nih.gov/11531144/">A. C. Ruifrok and
 *      D. A. Johnston, "Quantification of histochemical staining by color
 *      deconvolution", Anal. Quant. Cytol. Histol. 23(4):291-299, 2001</a>
 * @implNote op names='color.deconvolveStains'
 */
public class DeconvolveStains<T extends RealType<T>> implements
	Functions.Arity4<RandomAccessibleInterval<T>, double[][], Integer, Double, RandomAccessibleInterval<DoubleType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> createOp;

	/**
	 * Deconvolves the stains of an RGB image.
	 *
	 * @param input the RGB image, with the red, green and blue channels along
	 *          one dimension
	 * @param stains the OD vectors of two or three stains, one row of red, green
	 *          and blue absorbances per stain (need not be normalized)
	 * @param channelDim the dimension holding the RGB channels (default: the last
	 *          dimension of size 3)
	 * @param i0 the incident light intensity, i.e. the intensity of unstained
	 *          background (default: the maximum value of the pixel type for
	 *          integer types, otherwise the maximum intensity of the image)
	 * @return the stain amounts in OD units, with the same dimensions as the
	 *         input; index k along the channel dimension holds stain k
	 */
	@Override
	public RandomAccessibleInterval<DoubleType> apply(
		final RandomAccessibleInterval<T> input, final double[][] stains,
		@Nullable final Integer channelDim, @Nullable final Double i0)
	{
		final int cd = channelDim == null ? ColourDeconvolution.defaultChannelDim(
			input) : channelDim;
		final double intensity = i0 == null ? ColourDeconvolution.defaultI0(input)
			: i0;
		final var output = createOp.apply(input, new DoubleType());
		ColourDeconvolution.deconvolve(input, stains, cd, intensity, output);
		return output;
	}
}
