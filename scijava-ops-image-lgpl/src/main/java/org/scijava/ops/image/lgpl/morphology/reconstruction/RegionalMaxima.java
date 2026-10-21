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
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Marks the regional maxima of an image in a binary mask, in any number of
 * dimensions.
 * <p>
 * A regional maximum is a connected plateau of pixels of constant value, none
 * of whose neighbors has a higher value. Unlike {@code image.localExtrema},
 * which tests each pixel against its neighborhood, whole plateaus are
 * considered: a plateau adjacent to any higher pixel is not a maximum, and
 * every pixel of a maximal plateau is marked. Pixels outside the image are
 * ignored.
 * </p>
 * <p>
 * See P. Soille, <em>Morphological Image Analysis</em>, 2nd ed., Springer,
 * 2003, section 6.3.
 * </p>
 *
 * @author Curtis Rueden
 * @param <T> image type
 * @param <B> mask type
 * @implNote op names='morphology.regionalMaxima'
 */
public class RegionalMaxima<T extends RealType<T>, B extends BooleanType<B>>
	implements
	Computers.Arity2<RandomAccessibleInterval<T>, Shape, RandomAccessibleInterval<B>>
{

	/**
	 * Finds the regional maxima.
	 *
	 * @param input the image in which to search for maxima
	 * @param connectivity the neighborhood defining which pixels are adjacent,
	 *          such as a {@code DiamondShape} of radius 1 for face connectivity
	 *          (4-connectivity in 2D, 6-connectivity in 3D) or a
	 *          {@code RectangleShape} of span 1 for full connectivity
	 *          (8-connectivity in 2D, 26-connectivity in 3D). Defaults to full
	 *          connectivity.
	 * @param output the mask, of the same dimensions as {@code input}; set to
	 *          true on regional maxima and false elsewhere
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		@Nullable final Shape connectivity,
		final RandomAccessibleInterval<B> output)
	{
		final var grid = new PixelGrid(input, connectivity);
		grid.write(Reconstructions.regionalMaxima(grid.values(input), grid),
			output);
	}
}
