/*-
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

package org.scijava.ops.image.image.extrema;

import java.util.List;

import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.localextrema.LocalExtrema;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;

/**
 * Finds the local maxima (or minima) of an image, wrapping ImgLib2's
 * {@link LocalExtrema}.
 * <p>
 * A pixel is a local maximum if no pixel in its neighborhood is brighter, and
 * its value is at least {@code threshold} (for minima: no neighbor is darker,
 * and its value is at most {@code threshold}). Extrema are non-strict, so
 * every pixel of a flat plateau that is not adjacent to a brighter pixel is
 * reported. Pixels on the image border are candidates too: the image is
 * extended by border replication. Note that this op finds <em>local</em>
 * extrema; for regional extrema (whole plateaus), use a dedicated regional
 * extrema op.
 * </p>
 * <p>
 * The extremum checks are those of ImgLib2's
 * {@link LocalExtrema.MaximumCheck} and {@link LocalExtrema.MinimumCheck}
 * (BSD-2-Clause, by Tobias Pietzsch).
 * </p>
 *
 * @author Curtis Rueden
 * @param <T> image type
 * @implNote op names='image.localExtrema'
 */
public class DefaultLocalExtrema<T extends RealType<T>> implements
	Functions.Arity4<RandomAccessibleInterval<T>, Shape, Double, Boolean, List<Point>>
{

	/**
	 * Finds local extrema.
	 *
	 * @param input the image in which to search for extrema
	 * @param shape the neighborhood of each pixel; defaults to a 3^n hypercube
	 *          (a {@code RectangleShape} of span 1)
	 * @param threshold minimum value of a maximum (maximum value of a minimum);
	 *          by default, there is no threshold
	 * @param maxima {@code true} (default) to find maxima, {@code false} to find
	 *          minima
	 * @return the positions of the local extrema, sorted in flat iteration
	 *         order (first dimension fastest)
	 */
	@Override
	public List<Point> apply(final RandomAccessibleInterval<T> input,
		@Nullable final Shape shape, @Nullable final Double threshold,
		@Nullable final Boolean maxima)
	{
		return LocalExtremaFinder.find(input, shape, threshold, maxima);
	}
}
