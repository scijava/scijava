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
 * Computes the grayscale area opening of an image, in any number of dimensions:
 * removes the bright structures made of fewer than a given number of pixels,
 * without altering the shape of the others.
 * <p>
 * Each connected component of each upper level set of the image is preserved
 * if it has at least {@code minArea} pixels; the others are lowered to the
 * level of the smallest preserved component containing them. Equivalently,
 * the value of a pixel is the maximum, over all connected sets of at least
 * {@code minArea} pixels containing it, of the minimum of the image on that
 * set. On a binary image, this removes the foreground components of fewer than
 * {@code minArea} pixels.
 * </p>
 * <p>
 * Implemented by pruning the max-tree of the image (Salembier, Oliveras and
 * Garrido, 1998), built with the union-find algorithm of Najman and Couprie
 * (2006) as formulated by Berger et al. (2007), with the direct filtering
 * rule. Any {@code RealType} and any number of dimensions are supported.
 * </p>
 * <p>
 * See L. Vincent, "Morphological area openings and closings for grey-scale
 * images", in <em>Shape in Picture</em>, NATO ASI Series F 126, Springer,
 * 1994, pp. 197-208; and E. J. Breen and R. Jones, "Attribute openings,
 * thinnings, and granulometries", <em>Computer Vision and Image
 * Understanding</em> 64(3):377-389, 1996.
 * </p>
 *
 * @author Curtis Rueden
 * @param <T> image type
 * @implNote op names='morphology.areaOpening'
 */
public class AreaOpening<T extends RealType<T>> implements
	Computers.Arity3<RandomAccessibleInterval<T>, Long, Shape, RandomAccessibleInterval<T>>
{

	/**
	 * Computes the area opening.
	 *
	 * @param input the image to filter
	 * @param minArea the minimal number of pixels of the preserved components
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
		final Long minArea, @Nullable final Shape connectivity,
		final RandomAccessibleInterval<T> output)
	{
		AttributeFilters.filter(input, Attribute.AREA, minArea,
			connectivity, false, output);
	}
}
