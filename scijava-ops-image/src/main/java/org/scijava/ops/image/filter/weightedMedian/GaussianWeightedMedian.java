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

package org.scijava.ops.image.filter.weightedMedian;

import java.util.Arrays;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Gaussian-weighted median filter: a {@code filter.weightedMedian} whose
 * weights are {@code exp(-d² / (2σ²))} over a hypersphere of the given radius
 * around each pixel, where {@code d} is the distance to the center pixel.
 * Compared to a plain median filter, pixels closer to the center have more
 * influence, which preserves fine structures and edges better at the same
 * radius.
 * <p>
 * Inspired by the {@code Gaussian_Weighted_Median} plugin of the BioVoxxel
 * Toolbox by Jan Brocher / BioVoxxel. That plugin carries a GPL-3 license
 * header, so this op was implemented independently from the general
 * definition of a Gaussian-weighted median, not from its source code. The
 * kernel construction (real-valued weights, user-defined sigma and radius,
 * n-D) therefore differs from the plugin.
 * </p>
 *
 * @author Curtis Rueden
 * @param <T> input pixel type
 * @param <V> output pixel type
 * @see <a href="https://github.com/biovoxxel/BioVoxxel-Toolbox">BioVoxxel
 *      Toolbox</a>
 * @implNote op names='filter.gaussianWeightedMedian'
 */
public class GaussianWeightedMedian<T extends RealType<T>, V extends RealType<V>>
	implements
	Computers.Arity4<RandomAccessibleInterval<T>, Double, Integer, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<V>>
{

	@OpDependency(name = "filter.weightedMedian")
	private Computers.Arity3<RandomAccessibleInterval<T>, RandomAccessibleInterval<DoubleType>, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<V>> weightedMedianOp;

	/**
	 * Applies the Gaussian-weighted median filter.
	 *
	 * @param input the image to filter
	 * @param sigma the standard deviation of the Gaussian weights, in pixels
	 * @param radius the radius of the hyperspherical neighborhood, in pixels
	 *          (default: {@code ceil(2σ)})
	 * @param outOfBounds how to extend the input beyond its bounds (default:
	 *          repeat the border values)
	 * @param output the filtered image
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Double sigma, @Nullable Integer radius,
		@Nullable final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBounds,
		final RandomAccessibleInterval<V> output)
	{
		if (sigma <= 0) {
			throw new IllegalArgumentException("Sigma must be positive");
		}
		if (radius == null) radius = (int) Math.ceil(2 * sigma);
		if (radius < 0) {
			throw new IllegalArgumentException("Radius must be non-negative");
		}
		final var kernel = kernel(input.numDimensions(), sigma, radius);
		weightedMedianOp.compute(input, kernel, outOfBounds, output);
	}

	/**
	 * Creates a hyperspherical kernel of Gaussian weights.
	 *
	 * @param numDimensions the dimensionality of the kernel
	 * @param sigma the standard deviation of the Gaussian
	 * @param radius the radius of the hypersphere
	 * @return the kernel, with size {@code 2 * radius + 1} in every dimension
	 */
	static ArrayImg<DoubleType, DoubleArray> kernel(final int numDimensions,
		final double sigma, final int radius)
	{
		final var dims = new long[numDimensions];
		Arrays.fill(dims, 2L * radius + 1);
		final var kernel = ArrayImgs.doubles(dims);
		final var c = kernel.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			double distSq = 0;
			for (var d = 0; d < numDimensions; d++) {
				final double offset = c.getLongPosition(d) - radius;
				distSq += offset * offset;
			}
			if (distSq <= (double) radius * radius) {
				c.get().set(Math.exp(-distSq / (2 * sigma * sigma)));
			}
		}
		return kernel;
	}
}
