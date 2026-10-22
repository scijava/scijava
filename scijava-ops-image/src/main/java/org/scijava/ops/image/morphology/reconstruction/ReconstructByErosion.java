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
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Grayscale morphological reconstruction by erosion of a marker image over a
 * mask image, in any number of dimensions.
 * <p>
 * The marker is repeatedly eroded (with the given connectivity) and clipped
 * from below by the mask until stability. This is the dual of
 * {@code morphology.reconstructByDilation}: each regional minimum of the mask
 * which is not marked is filled up to the level at which it meets the marker.
 * </p>
 * <p>
 * Implemented with the hybrid algorithm of L. Vincent, "Morphological grayscale
 * reconstruction in image analysis: applications and efficient algorithms",
 * IEEE Transactions on Image Processing 2(2):176-201, 1993. See also P. Soille,
 * <em>Morphological Image Analysis</em>, 2nd ed., Springer, 2003, section 6.2.
 * </p>
 * <p>
 * NaN input values are not supported and cause an
 * {@link IllegalArgumentException}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> image type
 * @implNote op names='morphology.reconstructByErosion'
 */
public class ReconstructByErosion<T extends RealType<T>> implements
	Computers.Arity3<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, Shape, RandomAccessibleInterval<T>>
{

	/**
	 * Computes the reconstruction by erosion.
	 *
	 * @param marker the marker image; values below the mask are raised to it
	 * @param mask the mask image, of the same dimensions as the marker
	 * @param connectivity the neighborhood defining which pixels are adjacent,
	 *          such as a {@code DiamondShape} of radius 1 for face connectivity
	 *          (4-connectivity in 2D, 6-connectivity in 3D) or a
	 *          {@code RectangleShape} of span 1 for full connectivity
	 *          (8-connectivity in 2D, 26-connectivity in 3D). Defaults to full
	 *          connectivity.
	 * @param output the reconstruction, of the same dimensions as the marker
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> marker,
		final RandomAccessibleInterval<T> mask, @Nullable final Shape connectivity,
		final RandomAccessibleInterval<T> output)
	{
		final var grid = new PixelGrid(marker, connectivity);
		final var values = grid.values(marker);
		Reconstructions.erode(values, grid.values(mask), grid);
		grid.write(values, output);
	}
}
