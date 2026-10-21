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
 * Fills the holes of a grayscale image, in any number of dimensions.
 * <p>
 * A hole is a regional minimum not connected to the image border; it is
 * filled up to the lowest level at which it can be reached from the border.
 * The result is the reconstruction by erosion, over the input, of a marker
 * equal to the input on the border and to the input maximum elsewhere. On a
 * binary image this is equivalent to the {@code BooleanType} variant of
 * {@code morphology.fillHoles}, which takes precedence for such images.
 * </p>
 * <p>
 * See P. Soille, <em>Morphological Image Analysis</em>, 2nd ed., Springer,
 * 2003, section 6.3.7.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> image type
 * @implNote op names='morphology.fillHoles', priority='-100.'
 */
public class GrayscaleFillHoles<T extends RealType<T>> implements
	Computers.Arity2<RandomAccessibleInterval<T>, Shape, RandomAccessibleInterval<T>>
{

	/**
	 * Fills the holes.
	 *
	 * @param input the image to process
	 * @param connectivity the neighborhood defining which pixels are adjacent,
	 *          such as a {@code DiamondShape} of radius 1 for face connectivity
	 *          (4-connectivity in 2D, 6-connectivity in 3D) or a
	 *          {@code RectangleShape} of span 1 for full connectivity
	 *          (8-connectivity in 2D, 26-connectivity in 3D). Defaults to full
	 *          connectivity.
	 * @param output the filled image, of the same dimensions as {@code input}
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		@Nullable final Shape connectivity,
		final RandomAccessibleInterval<T> output)
	{
		final var grid = new PixelGrid(input, connectivity);
		final var values = grid.values(input);
		final var marker = Reconstructions.borderMarker(values, grid, true);
		Reconstructions.erode(marker, values, grid);
		grid.write(marker, output);
	}
}
