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

package org.scijava.ops.image.filter.localContrast;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * Local contrast filter of PHANTAST: the local coefficient of variation of an
 * image, i.e. the Gaussian-weighted local standard deviation divided by the
 * Gaussian-weighted local mean,
 * {@code C = sqrt(G * I² - (G * I)²) / (G * I)}, where {@code G} is a
 * Gaussian kernel with standard deviation {@code sigma}. Pixels whose local
 * mean is not positive are set to 0.
 * <p>
 * In phase contrast microscopy, cells appear as textured regions with high
 * local contrast against a smooth background; thresholding this image is the
 * core of the PHANTAST segmentation (see {@code segment.phantast}).
 * </p>
 * <p>
 * Note: this is unrelated to {@code threshold.localContrast}, which is a local
 * thresholding method that classifies each pixel by whether it is closer to
 * the minimum or the maximum of its neighborhood. This op is a continuous,
 * scale-invariant measure of local intensity variability that works on images
 * of any dimensionality.
 * </p>
 * <p>
 * Translated from {@code PHANTAST_.getLocalContrastImage} of the PHANTAST
 * plugin for Fiji (<a href=
 * "https://github.com/nicjac/PHANTAST-FIJI">PHANTAST-FIJI</a>), Copyright (c)
 * 2013-2014, Nicolas Jaccard, Department of Biochemical Engineering and CoMPLEX,
 * University College London, used under the BSD 3-Clause license. Reference:
 * N. Jaccard, L. D. Griffin, A. Keser, R. J. Macown, A. Super, F. S. Veraitch
 * and N. Szita, "Automated method for the rapid and precise estimation of
 * adherent cell culture characteristics from phase contrast microscopy
 * images", Biotechnology and Bioengineering 111(3):504-517, 2014.
 * </p>
 * <p>
 * The original PHANTAST license notice follows.
 * </p>
 *
 * <pre>
 * Copyright (c) 2013, Nicolas Jaccard
 * Department of Biochemical Engineering, UCL
 * Centre for Mathematics and Physics in the Life Sciences and Experimental
 * Biology, UCL
 * The British Heart Foundation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution
 *   * Neither the names of the University College London or British Heart
 *     Foundation nor the names of its contributors may be used to endorse
 *     or promote products derived from this software without specific
 *     prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Nicolas Jaccard
 * @param <T> the input type
 * @implNote op names='filter.localContrast'
 */
public class DefaultLocalContrast<T extends RealType<T>> implements
	BiFunction<RandomAccessibleInterval<T>, Double, RandomAccessibleInterval<FloatType>>
{

	@OpDependency(name = "filter.gauss")
	private Computers.Arity2<RandomAccessibleInterval<FloatType>, Double, RandomAccessibleInterval<FloatType>> gaussOp;

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> createOp;

	/**
	 * Computes the local contrast image.
	 *
	 * @param input the input image
	 * @param sigma the standard deviation, in pixels, of the Gaussian window
	 *          over which the local mean and standard deviation are computed
	 *          (PHANTAST default: 1.2)
	 * @return the local contrast (coefficient of variation) image
	 */
	@Override
	public RandomAccessibleInterval<FloatType> apply(
		final RandomAccessibleInterval<T> input, final Double sigma)
	{
		if (sigma == null || sigma <= 0) {
			throw new IllegalArgumentException("sigma must be positive");
		}
		final var zeroMin = Views.zeroMin(input);
		final var squared = Converters.convert(zeroMin,
			(Converter<T, FloatType>) (in, out) -> {
				final var v = in.getRealDouble();
				out.setReal(v * v);
			}, new FloatType());
		final var floatInput = Converters.convert(zeroMin,
			(Converter<T, FloatType>) (in, out) -> out.setReal(in.getRealDouble()),
			new FloatType());

		final var mean = createOp.apply(zeroMin, new FloatType());
		gaussOp.compute(floatInput, sigma, mean);
		final var output = createOp.apply(zeroMin, new FloatType());
		gaussOp.compute(squared, sigma, output);

		LoopBuilder.setImages(mean, output).forEachPixel((m, out) -> {
			final var mu = m.getRealDouble();
			if (mu > 0) {
				final var variance = Math.max(0, out.getRealDouble() - mu * mu);
				out.setReal(Math.sqrt(variance) / mu);
			}
			else out.setZero();
		});
		return output;
	}
}
