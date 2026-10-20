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

package org.scijava.ops.image.filter.convolvedBackground;

import java.util.Arrays;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Convolved background subtraction: estimates the background by smoothing the
 * image with a large Gaussian, median or mean filter, and subtracts it.
 * <p>
 * The algorithm is:
 * </p>
 * <ol>
 * <li>{@code bg = smooth(in, radius)}, where {@code smooth} is a Gaussian blur
 * with sigma {@code radius}, or a median or mean filter over a
 * {@link HyperSphereShape} of radius {@code floor(radius)}.</li>
 * <li>{@code bg = max(bg, floor(floor(radius / 10) * 1.5))}, a maximum filter
 * (grayscale dilation) over a {@link HyperSphereShape} that counteracts the
 * shrinking of background features caused by the smoothing; it is skipped
 * when its radius is zero, i.e. for {@code radius < 10}.</li>
 * <li>{@code out = in - bg}, clamped to the range of the output type.</li>
 * </ol>
 * <p>
 * Out-of-bounds values are taken from the image border. The image may have any
 * number of dimensions; all filters are applied in n-D.
 * </p>
 * <p>
 * Translated from the {@code Convoluted_Background_Subtraction} plugin of the
 * BioVoxxel Toolbox by Jan Brocher / BioVoxxel (BSD 3-Clause license,
 * Copyright (C) Jan Brocher / BioVoxxel). The original uses ImageJ's 2D
 * circular rank-filter kernels (which include offsets with
 * {@code d² <= r² + 1}); this op uses n-D hyperspheres ({@code d² <= r²}) and
 * computes the background in floating point.
 * </p>
 *
 * @author Jan Brocher (BioVoxxel)
 * @param <T> input pixel type
 * @param <V> output pixel type
 * @see <a href="https://github.com/biovoxxel/BioVoxxel-Toolbox">BioVoxxel
 *      Toolbox</a>
 * @implNote op names='filter.convolvedBackgroundSubtraction'
 */
public class ConvolvedBackgroundSubtraction<T extends RealType<T>, V extends RealType<V>>
	implements
	Computers.Arity3<RandomAccessibleInterval<T>, Double, String, RandomAccessibleInterval<V>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, FloatType, RandomAccessibleInterval<FloatType>> createOp;

	@OpDependency(name = "filter.gauss")
	private Computers.Arity3<RandomAccessibleInterval<T>, double[], OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<FloatType>> gaussOp;

	@OpDependency(name = "filter.median")
	private Computers.Arity3<RandomAccessibleInterval<T>, Shape, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<FloatType>> medianOp;

	@OpDependency(name = "filter.mean")
	private Computers.Arity3<RandomAccessibleInterval<T>, Shape, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<FloatType>> meanOp;

	@OpDependency(name = "filter.max")
	private Computers.Arity3<RandomAccessibleInterval<FloatType>, Shape, OutOfBoundsFactory<FloatType, RandomAccessibleInterval<FloatType>>, RandomAccessibleInterval<FloatType>> maxOp;

	/**
	 * Subtracts the convolved background from the input.
	 *
	 * @param input the image to correct
	 * @param radius the radius of the smoothing filter (the sigma for the
	 *          Gaussian filter), in pixels; it should be larger than the
	 *          largest foreground object
	 * @param filter the smoothing filter: one of {@code "gaussian"},
	 *          {@code "median"} or {@code "mean"} (default {@code "gaussian"})
	 * @param output the background-subtracted image
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Double radius, @Nullable final String filter,
		final RandomAccessibleInterval<V> output)
	{
		if (radius < 0) {
			throw new IllegalArgumentException("Radius must be non-negative");
		}
		final var background = createOp.apply(input, new FloatType());
		final var method = filter == null ? "gaussian" : filter.toLowerCase();
		final var rankShape = new HyperSphereShape((long) Math.floor(radius));
		switch (method) {
			case "gaussian":
				final var sigmas = new double[input.numDimensions()];
				Arrays.fill(sigmas, radius);
				gaussOp.compute(input, sigmas, new OutOfBoundsBorderFactory<>(),
					background);
				break;
			case "median":
				medianOp.compute(input, rankShape, new OutOfBoundsBorderFactory<>(),
					background);
				break;
			case "mean":
				meanOp.compute(input, rankShape, new OutOfBoundsBorderFactory<>(),
					background);
				break;
			default:
				throw new IllegalArgumentException("Unknown filter: " + filter +
					" (expected gaussian, median or mean)");
		}

		// Counteract the size reduction of background features.
		final var maxRadius = (long) Math.floor(Math.floor(radius / 10) * 1.5);
		final RandomAccessibleInterval<FloatType> bg;
		if (maxRadius > 0) {
			bg = createOp.apply(input, new FloatType());
			maxOp.compute(background, new HyperSphereShape(maxRadius),
				new OutOfBoundsBorderFactory<>(), bg);
		}
		else bg = background;

		LoopBuilder.setImages(input, bg, output).forEachPixel((i, b, o) -> {
			final var v = i.getRealDouble() - b.getRealDouble();
			o.setReal(Math.max(o.getMinValue(), Math.min(o.getMaxValue(), v)));
		});
	}
}
