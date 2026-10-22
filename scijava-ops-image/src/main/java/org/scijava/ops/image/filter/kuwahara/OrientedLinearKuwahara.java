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

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Orientation-adaptive linear Kuwahara filter: the line segments at each
 * pixel follow a given local orientation, such as the output of
 * {@code filter.orientation} (the dominant orientation from the structure
 * tensor).
 * <p>
 * For each pixel, the candidates are the line segments of {@code lineLength}
 * pixels along the local orientation that contain the pixel; the output is
 * the mean of the candidate with the lowest criterion. This is the
 * orientation-adaptive filter of Bakker, van Vliet and Verbeek: it smooths
 * along oriented structures, and the Kuwahara-style selection of the shift
 * keeps edges sharp where the line ends. See {@link LinearKuwahara} and
 * {@link DefaultLinearKuwahara}.
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
 * @param <R> orientation pixel type
 * @param <V> output pixel type
 * @implNote op names='filter.kuwaharaLinear'
 */
public class OrientedLinearKuwahara<T extends RealType<T>, R extends RealType<R>, V extends RealType<V>>
	implements
	Computers.Arity5<RandomAccessibleInterval<T>, RandomAccessibleInterval<R>, Integer, String, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<V>>
{

	/**
	 * Applies the orientation-adaptive linear Kuwahara filter.
	 *
	 * @param input the image to filter (at least 2D)
	 * @param orientation the line orientation at each pixel, in radians
	 *          counterclockwise from the x axis as displayed (y up), as
	 *          computed by filter.orientation; same dimensions as the input
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
		final RandomAccessibleInterval<R> orientation,
		@Nullable final Integer lineLength, @Nullable final String criterion,
		@Nullable final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactory,
		final RandomAccessibleInterval<V> output)
	{
		LinearKuwahara.filter(input, null, orientation, lineLength == null ? 11
			: lineLength, LinearKuwahara.Criterion.byName(criterion),
			outOfBoundsFactory, output);
	}
}
