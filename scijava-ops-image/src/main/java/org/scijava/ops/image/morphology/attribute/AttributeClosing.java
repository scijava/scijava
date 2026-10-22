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

package org.scijava.ops.image.morphology.attribute;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.image.morphology.attribute.AttributeFilters.Attribute;
import org.scijava.ops.spi.Nullable;

/**
 * Computes a grayscale attribute closing of an image, in any number of
 * dimensions, with the attribute given by name.
 * <p>
 * Each connected component of each lower level set of the image is preserved
 * if its attribute is at least the given threshold; the others are raised to
 * the level of the smallest preserved component containing them. The
 * supported attributes are {@code area} (see {@code morphology.areaClosing}),
 * {@code boxDiagonal} (see {@code morphology.boxDiagonalClosing}) and
 * {@code grayVolume} (see {@code morphology.grayVolumeClosing}). The name
 * {@code volume} is rejected as ambiguous: the literature uses it for the
 * grayscale volume, while MorphoLibJ uses it for the number of voxels of a
 * 3D component, which is {@code area} here.
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
 * <p>
 * NaN input values are not supported and cause an
 * {@link IllegalArgumentException}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> image type
 * @implNote op names='morphology.attributeClosing'
 */
public class AttributeClosing<T extends RealType<T>> implements
	Computers.Arity4<RandomAccessibleInterval<T>, String, Double, Shape, RandomAccessibleInterval<T>>
{

	/**
	 * Computes the attribute closing.
	 *
	 * @param input the image to filter
	 * @param attribute the name of the attribute: area, boxDiagonal or
	 *          grayVolume (case insensitive)
	 * @param threshold the minimal attribute value of the preserved components
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
		final String attribute, final Double threshold,
		@Nullable final Shape connectivity,
		final RandomAccessibleInterval<T> output)
	{
		AttributeFilters.filter(input, Attribute.of(attribute), threshold,
			connectivity, true, output);
	}
}
