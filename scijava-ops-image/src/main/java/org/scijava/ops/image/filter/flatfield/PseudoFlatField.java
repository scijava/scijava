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

package org.scijava.ops.image.filter.flatfield;

import java.util.Arrays;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * Pseudo flat-field correction: corrects uneven illumination using a heavily
 * blurred copy of the image itself as the flat-field estimate.
 * <p>
 * The background {@code G} is estimated by Gaussian-blurring the input with
 * the given sigma(s) (out-of-bounds values are taken from the image border),
 * and each pixel is then scaled by the ratio of the mean background intensity
 * to the local background intensity:
 * </p>
 *
 * <pre>
 * out = in &times; mean(G) / G
 * </pre>
 * <p>
 * Pixels where {@code G == 0} are copied unchanged. Results are clamped to
 * the range of the output type, so an integer output saturates rather than
 * wrapping around; use a floating-point output to keep the full range. The
 * sigma should be
 * considerably larger than the largest foreground object, so that the blurred
 * image contains only the illumination profile.
 * </p>
 * <p>
 * Translated from the {@code Pseudo_flat_field_correction} plugin of the
 * BioVoxxel Toolbox by Jan Brocher / BioVoxxel (BSD 3-Clause license,
 * Copyright (C) Jan Brocher / BioVoxxel). Unlike the original, which blurs
 * single 2D planes and uses integer arithmetic for 8- and 16-bit images, this
 * op blurs in n dimensions and computes in floating point. The RGB (HSB
 * brightness) branch of the original is not ported; apply this op to a
 * brightness channel instead.
 * </p>
 * <p>
 * The original BioVoxxel Toolbox license notice follows.
 * </p>
 *
 * <pre>
 * Copyright (c) 2015, BioVoxxel / Jan Brocher
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of BioVoxxel nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Jan Brocher (BioVoxxel)
 * @param <T> input pixel type
 * @param <V> output pixel type
 * @see <a href="https://github.com/biovoxxel/BioVoxxel-Toolbox">BioVoxxel
 *      Toolbox</a>
 * @implNote op names='filter.pseudoFlatField'
 */
public class PseudoFlatField<T extends RealType<T>, V extends RealType<V>>
	implements
	Computers.Arity2<RandomAccessibleInterval<T>, double[], RandomAccessibleInterval<V>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> createOp;

	@OpDependency(name = "filter.gauss")
	private Computers.Arity3<RandomAccessibleInterval<T>, double[], OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<FloatType>> gaussOp;

	/**
	 * Applies pseudo flat-field correction.
	 *
	 * @param input the image to correct
	 * @param sigmas the standard deviations (one per dimension, in pixels) of
	 *          the Gaussian used to estimate the background
	 * @param output the corrected image
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final double[] sigmas, final RandomAccessibleInterval<V> output)
	{
		if (sigmas.length != input.numDimensions()) {
			throw new IllegalArgumentException("Expected " + input.numDimensions() +
				" sigmas but got " + sigmas.length);
		}
		final var background = createOp.apply(input, new FloatType());
		gaussOp.compute(input, sigmas, new OutOfBoundsBorderFactory<>(),
			background);

		double sum = 0;
		long count = 0;
		for (final var g : Views.flatIterable(background)) {
			sum += g.getRealDouble();
			count++;
		}
		final var meanBackground = sum / count;

		LoopBuilder.setImages(input, background, output).forEachPixel((i, g,
			o) -> {
			final var gv = g.getRealDouble();
			final var v = gv == 0 ? i.getRealDouble() : i.getRealDouble() *
				meanBackground / gv;
			o.setReal(Math.max(o.getMinValue(), Math.min(o.getMaxValue(), v)));
		});
	}
}

/**
 * Convenience version of {@code filter.pseudoFlatField} using the same sigma
 * in every dimension.
 *
 * @param <T> input pixel type
 * @param <V> output pixel type
 * @implNote op names='filter.pseudoFlatField'
 */
class PseudoFlatFieldSingleSigma<T extends RealType<T>, V extends RealType<V>>
	implements
	Computers.Arity2<RandomAccessibleInterval<T>, Double, RandomAccessibleInterval<V>>
{

	@OpDependency(name = "filter.pseudoFlatField")
	private Computers.Arity2<RandomAccessibleInterval<T>, double[], RandomAccessibleInterval<V>> pseudoFlatFieldOp;

	/**
	 * Applies pseudo flat-field correction.
	 *
	 * @param input the image to correct
	 * @param sigma the standard deviation (in pixels, in every dimension) of the
	 *          Gaussian used to estimate the background
	 * @param output the corrected image
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Double sigma, final RandomAccessibleInterval<V> output)
	{
		final var sigmas = new double[input.numDimensions()];
		Arrays.fill(sigmas, sigma);
		pseudoFlatFieldOp.compute(input, sigmas, output);
	}
}
