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

package org.scijava.ops.image.filter.kuwahara;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Linear Kuwahara filter: edge- and line-preserving smoothing with line
 * segments searched over evenly spaced orientations.
 * <p>
 * For each pixel, every line segment of {@code lineLength} pixels that
 * contains the pixel, at each of the orientations {@code kπ/nAngles}, is a
 * candidate; the output is the mean of the candidate with the lowest
 * criterion (by default, the lowest variance). Thin oriented structures such
 * as fibres and membranes are smoothed along their length without being
 * blurred across. Lines lie in the plane of dimensions 0 and 1; further
 * dimensions are filtered plane by plane. See {@link LinearKuwahara} for
 * details, and the {@code filter.kuwaharaLinear} variant taking an orientation
 * image (e.g. from {@code filter.orientation}) for the orientation-adaptive
 * filter.
 * </p>
 * <p>
 * Implemented from P. Bakker, L. J. van Vliet and P. W. Verbeek, "Edge
 * preserving orientation adaptive filtering", Proc. IEEE CVPR 1999, pp.
 * 535-540.
 * </p>
 * <p>
 * Results are clamped to the range of the output type, so an integer output
 * saturates rather than wrapping around.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> input pixel type
 * @param <V> output pixel type
 * @implNote op names='filter.kuwaharaLinear'
 */
public class DefaultLinearKuwahara<T extends RealType<T>, V extends RealType<V>>
	implements
	Computers.Arity5<RandomAccessibleInterval<T>, Integer, Integer, String, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<V>>
{

	/**
	 * Applies the linear Kuwahara filter.
	 *
	 * @param input the image to filter (at least 2D)
	 * @param nAngles the number of evenly spaced line orientations (default: 30)
	 * @param lineLength the (odd) length of the line segments, in pixels
	 *          (default: 11)
	 * @param criterion the criterion minimized by the chosen segment: variance,
	 *          varianceOverMean (for Poisson noise) or varianceOverMeanSquared
	 *          (for multiplicative noise) (default: variance)
	 * @param outOfBoundsFactory the strategy for values beyond the image border
	 *          (default: mirror, without repeating the border pixel)
	 * @param output the filtered image
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		@Nullable final Integer nAngles, @Nullable final Integer lineLength,
		@Nullable final String criterion,
		@Nullable final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final RandomAccessibleInterval<V> output)
	{
		final int n = nAngles == null ? 30 : nAngles;
		if (n < 1) {
			throw new IllegalArgumentException("Number of angles must be positive: " +
				n);
		}
		final var angles = new double[n];
		for (var k = 0; k < n; k++)
			angles[k] = Math.PI * k / n;
		LinearKuwahara.<T, DoubleType, V> filter(input, angles, null,
			lineLength == null ? 11
			: lineLength, LinearKuwahara.Criterion.byName(criterion),
			outOfBoundsFactory, output);
	}
}
