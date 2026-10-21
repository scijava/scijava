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

package org.scijava.ops.image.lgpl.morphology.attribute;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.image.lgpl.morphology.attribute.AttributeFilters.Attribute;
import org.scijava.ops.spi.Nullable;

/**
 * Computes the grayscale bounding box diagonal closing of an image, in any
 * number of dimensions: removes the dark structures whose bounding box is
 * small, without altering the shape of the others.
 * <p>
 * Each connected component of each lower level set of the image is preserved
 * if the diagonal of its bounding box is at least {@code minDiagonal}; the
 * others are raised to the level of the smallest preserved component
 * containing them. The diagonal is the Euclidean distance between the
 * centers of two opposite corner pixels of the box: a single pixel has a
 * diagonal of 0, two adjacent pixels a diagonal of 1, and a square of 2x2
 * pixels a diagonal of the square root of 2. Unlike the area, this attribute
 * preserves thin elongated structures such as lines and fibres.
 * </p>
 * <p>
 * Implemented by pruning the min-tree of the image (Salembier, Oliveras and
 * Garrido, 1998), built with the union-find algorithm of Najman and Couprie
 * (2006) as formulated by Berger et al. (2007), with the direct filtering
 * rule. Any {@code RealType} and any number of dimensions are supported.
 * </p>
 * <p>
 * See E. J. Breen and R. Jones, "Attribute openings, thinnings, and
 * granulometries", <em>Computer Vision and Image Understanding</em>
 * 64(3):377-389, 1996.
 * </p>
 *
 * @author Curtis Rueden
 * @param <T> image type
 * @implNote op names='morphology.boxDiagonalClosing'
 */
public class BoxDiagonalClosing<T extends RealType<T>> implements
	Computers.Arity3<RandomAccessibleInterval<T>, Double, Shape, RandomAccessibleInterval<T>>
{

	/**
	 * Computes the bounding box diagonal closing.
	 *
	 * @param input the image to filter
	 * @param minDiagonal the minimal bounding box diagonal of the preserved components, in pixels
	 * @param connectivity the neighborhood defining which pixels are adjacent,
	 *          such as a {@code DiamondShape} of radius 1 for face connectivity
	 *          (4-connectivity in 2D, 6-connectivity in 3D) or a
	 *          {@code RectangleShape} of span 1 for full connectivity
	 *          (8-connectivity in 2D, 26-connectivity in 3D). Defaults to full
	 *          connectivity.
	 * @param output the filtered image, of the same dimensions as {@code input}
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Double minDiagonal, @Nullable final Shape connectivity,
		final RandomAccessibleInterval<T> output)
	{
		AttributeFilters.filter(input, Attribute.BOX_DIAGONAL, minDiagonal,
			connectivity, true, output);
	}
}
