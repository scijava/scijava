/* #%L
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

package org.scijava.ops.image.morphology.reconstruction;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Imposes minima on an image, in any number of dimensions: modifies the image
 * so that its only regional minima are the given markers.
 * <p>
 * Marker pixels are set to the lowest value of the type, and every other pixel
 * is raised to the lowest level at which it can be reached from a marker,
 * which fills every basin not containing a marker. This is the reconstruction
 * by erosion of the marker image over {@code min(input + e, marker image)},
 * where the marker image is lowest on markers and highest elsewhere. For
 * integer types, {@code e} is 1 (so that the markers are strictly lower than
 * all other pixels, at the price of increasing the other pixels by 1, with
 * saturation at the maximum value), and the lowest value is the minimum value
 * of the type; for floating point types, {@code e} is 0 and the lowest value
 * is negative infinity.
 * </p>
 * <p>
 * See P. Soille, <em>Morphological Image Analysis</em>, 2nd ed., Springer,
 * 2003, section 6.3.6.
 * </p>
 * <p>
 * NaN input values are not supported and cause an
 * {@link IllegalArgumentException}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> image type
 * @param <B> marker type
 * @implNote op names='morphology.imposeMinima'
 */
public class ImposeMinima<T extends RealType<T>, B extends BooleanType<B>>
	implements
	Computers.Arity3<RandomAccessibleInterval<T>, RandomAccessibleInterval<B>, Shape, RandomAccessibleInterval<T>>
{

	/**
	 * Imposes the minima.
	 *
	 * @param input the image to modify
	 * @param markers the binary marker image, of the same dimensions as
	 *          {@code input}, true where minima are to be imposed
	 * @param connectivity the neighborhood defining which pixels are adjacent,
	 *          such as a {@code DiamondShape} of radius 1 for face connectivity
	 *          (4-connectivity in 2D, 6-connectivity in 3D) or a
	 *          {@code RectangleShape} of span 1 for full connectivity
	 *          (8-connectivity in 2D, 26-connectivity in 3D). Defaults to full
	 *          connectivity.
	 * @param output the modified image, of the same dimensions as
	 *          {@code input}
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final RandomAccessibleInterval<B> markers,
		@Nullable final Shape connectivity,
		final RandomAccessibleInterval<T> output)
	{
		final var grid = new PixelGrid(input, connectivity);
		final var type = input.getType();
		final var lowest = Reconstructions.lowest(type);
		final var highest = Reconstructions.highest(type);
		final var e = Reconstructions.increment(type);
		final var mask = grid.values(input);
		final var marked = grid.flags(markers);
		final var values = new double[grid.size];
		for (var i = 0; i < grid.size; i++) {
			values[i] = marked[i] ? lowest : highest;
			mask[i] = marked[i] ? lowest : Math.min(mask[i] + e, highest);
		}
		Reconstructions.erode(values, mask, grid);
		grid.write(values, output);
	}
}
