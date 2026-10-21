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
 * Removes the structures touching the image border, in any number of
 * dimensions, for grayscale and binary ({@code BitType}) images alike.
 * <p>
 * The result is {@code input - R}, where {@code R} is the reconstruction by
 * dilation, under the input, of a marker equal to the input on the border and
 * to the input minimum elsewhere. For binary images, this removes the
 * foreground components touching the border; for grayscale images, each
 * pixel is lowered by the highest level at which it is connected to the
 * border.
 * </p>
 * <p>
 * See P. Soille, <em>Morphological Image Analysis</em>, 2nd ed., Springer,
 * 2003, section 6.3.8.
 * </p>
 *
 * @author Curtis Rueden
 * @param <T> image type
 * @implNote op names='morphology.killBorders'
 */
public class KillBorders<T extends RealType<T>> implements
	Computers.Arity2<RandomAccessibleInterval<T>, Shape, RandomAccessibleInterval<T>>
{

	/**
	 * Removes the border structures.
	 *
	 * @param input the image to process
	 * @param connectivity the neighborhood defining which pixels are adjacent,
	 *          such as a {@code DiamondShape} of radius 1 for face connectivity
	 *          (4-connectivity in 2D, 6-connectivity in 3D) or a
	 *          {@code RectangleShape} of span 1 for full connectivity
	 *          (8-connectivity in 2D, 26-connectivity in 3D). Defaults to full
	 *          connectivity.
	 * @param output the processed image, of the same dimensions as
	 *          {@code input}
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		@Nullable final Shape connectivity,
		final RandomAccessibleInterval<T> output)
	{
		final var grid = new PixelGrid(input, connectivity);
		final var values = grid.values(input);
		final var marker = Reconstructions.borderMarker(values, grid, false);
		Reconstructions.dilate(marker, values, grid);
		for (var i = 0; i < grid.size; i++)
			values[i] -= marker[i];
		grid.write(values, output);
	}
}
