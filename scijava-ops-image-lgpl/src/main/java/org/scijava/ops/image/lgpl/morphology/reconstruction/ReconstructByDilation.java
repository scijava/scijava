/*-
 * #%L
 * LGPL-licensed image processing operations for SciJava Ops, translated from LGPL ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.lgpl.morphology.reconstruction;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Grayscale morphological reconstruction by dilation of a marker image under a
 * mask image, in any number of dimensions.
 * <p>
 * The marker is repeatedly dilated (with the given connectivity) and clipped to
 * the mask until stability. Each pixel of the result is the largest value
 * {@code v} such that the pixel is connected to a marker pixel of value at
 * least {@code v} by a path along which the mask is at least {@code v}. In
 * particular, each regional maximum of the mask which is not marked is
 * flattened down to the level at which it meets the marker.
 * </p>
 * <p>
 * Implemented with the hybrid algorithm of L. Vincent, "Morphological grayscale
 * reconstruction in image analysis: applications and efficient algorithms",
 * IEEE Transactions on Image Processing 2(2):176-201, 1993. See also P. Soille,
 * <em>Morphological Image Analysis</em>, 2nd ed., Springer, 2003, section 6.2.
 * </p>
 *
 * @author Curtis Rueden
 * @param <T> image type
 * @implNote op names='morphology.reconstructByDilation'
 */
public class ReconstructByDilation<T extends RealType<T>> implements
	Computers.Arity3<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, Shape, RandomAccessibleInterval<T>>
{

	/**
	 * Computes the reconstruction by dilation.
	 *
	 * @param marker the marker image; values above the mask are clipped to it
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
		Reconstructions.dilate(values, grid.values(mask), grid);
		grid.write(values, output);
	}
}
