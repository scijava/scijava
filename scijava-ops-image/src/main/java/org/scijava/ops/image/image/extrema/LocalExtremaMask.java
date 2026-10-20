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

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Marks the local maxima (or minima) of an image in a binary mask. See
 * {@link DefaultLocalExtrema} for the definition of a local extremum.
 *
 * @author Curtis Rueden
 * @param <T> image type
 * @param <B> mask type
 * @implNote op names='image.localExtrema'
 */
public class LocalExtremaMask<T extends RealType<T>, B extends BooleanType<B>>
	implements
	Computers.Arity4<RandomAccessibleInterval<T>, Shape, Double, Boolean, RandomAccessibleInterval<B>>
{

	/**
	 * Finds local extrema and marks them in a mask.
	 *
	 * @param input the image in which to search for extrema
	 * @param shape the neighborhood of each pixel; defaults to a 3^n hypercube
	 *          (a {@code RectangleShape} of span 1)
	 * @param threshold minimum value of a maximum (maximum value of a minimum);
	 *          by default, there is no threshold
	 * @param maxima {@code true} (default) to find maxima, {@code false} to find
	 *          minima
	 * @param mask the output mask, of the same dimensions as {@code input}; set
	 *          to {@code true} at local extrema and {@code false} elsewhere
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		@Nullable final Shape shape, @Nullable final Double threshold,
		@Nullable final Boolean maxima, final RandomAccessibleInterval<B> mask)
	{
		if (!Intervals.equalDimensions(input, mask)) {
			throw new IllegalArgumentException(
				"Mask dimensions must match input dimensions");
		}
		final var extrema = LocalExtremaFinder.find(input, shape, threshold,
			maxima);
		for (final var b : Views.flatIterable(mask))
			b.set(false);
		final var n = input.numDimensions();
		final var ra = mask.randomAccess();
		for (final var p : extrema) {
			for (var d = 0; d < n; d++) {
				ra.setPosition(p.getLongPosition(d) - input.min(d) + mask.min(d), d);
			}
			ra.get().set(true);
		}
	}
}
