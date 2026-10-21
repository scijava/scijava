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
 * The h-maxima transform of an image, in any number of dimensions: the
 * reconstruction by dilation of {@code input - h} under {@code input}.
 * <p>
 * Every regional maximum whose dynamic (height above the highest saddle
 * leading to a higher maximum) is at most {@code h} is suppressed, and the
 * remaining maxima are lowered by {@code h}. For integer types,
 * {@code input - h} saturates at the minimum value of the type.
 * </p>
 * <p>
 * See P. Soille, <em>Morphological Image Analysis</em>, 2nd ed., Springer,
 * 2003, section 6.3.4.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> image type
 * @implNote op names='morphology.hMaxima'
 */
public class HMaxima<T extends RealType<T>> implements
	Computers.Arity3<RandomAccessibleInterval<T>, Double, Shape, RandomAccessibleInterval<T>>
{

	/**
	 * Computes the h-maxima transform.
	 *
	 * @param input the image to transform
	 * @param h the (non-negative) height of the maxima to suppress
	 * @param connectivity the neighborhood defining which pixels are adjacent,
	 *          such as a {@code DiamondShape} of radius 1 for face connectivity
	 *          (4-connectivity in 2D, 6-connectivity in 3D) or a
	 *          {@code RectangleShape} of span 1 for full connectivity
	 *          (8-connectivity in 2D, 26-connectivity in 3D). Defaults to full
	 *          connectivity.
	 * @param output the transformed image, of the same dimensions as
	 *          {@code input}
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input, final Double h,
		@Nullable final Shape connectivity,
		final RandomAccessibleInterval<T> output)
	{
		final var grid = new PixelGrid(input, connectivity);
		final var values = grid.values(input);
		Reconstructions.hMaxima(values, h, Reconstructions.lowest(input
			.getType()), grid);
		grid.write(values, output);
	}
}
