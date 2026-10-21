/*
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

package org.scijava.ops.image.morphology.attribute;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.image.morphology.attribute.AttributeFilters.Attribute;
import org.scijava.ops.spi.Nullable;

/**
 * Computes the grayscale area closing of an image, in any number of dimensions:
 * removes the dark structures made of fewer than a given number of pixels,
 * without altering the shape of the others.
 * <p>
 * Each connected component of each lower level set of the image is preserved
 * if it has at least {@code minArea} pixels; the others are raised to the
 * level of the smallest preserved component containing them. Equivalently,
 * the value of a pixel is the minimum, over all connected sets of at least
 * {@code minArea} pixels containing it, of the maximum of the image on that
 * set. On a binary image, this removes the background components of fewer than
 * {@code minArea} pixels (that is, it fills the small holes).
 * </p>
 * <p>
 * Implemented by pruning the min-tree of the image (Salembier, Oliveras and
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
 * @implNote op names='morphology.areaClosing'
 */
public class AreaClosing<T extends RealType<T>> implements
	Computers.Arity3<RandomAccessibleInterval<T>, Long, Shape, RandomAccessibleInterval<T>>
{

	/**
	 * Computes the area closing.
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
			connectivity, true, output);
	}
}
