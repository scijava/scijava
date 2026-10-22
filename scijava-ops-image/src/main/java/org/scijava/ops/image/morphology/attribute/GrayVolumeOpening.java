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
 * Computes the grayscale volume opening of an image, in any number of
 * dimensions: removes the bright structures of small volume, that is with a
 * small product of size and contrast.
 * <p>
 * Each connected component of each upper level set of the image is preserved
 * if its volume is at least {@code minVolume}; the others are lowered to the
 * level of the smallest preserved component containing them. The volume of a
 * component is the sum, over its pixels, of their height above the level at
 * which the component merges with a larger one, that is the maximum value of
 * the image on the pixels adjacent to the component. The component covering
 * the whole image is always preserved.
 * </p>
 * <p>
 * The literature calls this attribute the <em>volume</em> (Vachier, 1995;
 * Meyer, 1998), since it is the volume under (or above) the image surface
 * restricted to the component. This op is named {@code grayVolumeOpening}
 * rather than {@code volumeOpening} because MorphoLibJ uses the latter name
 * for a different filter: its 3D "volume opening" is a size opening that
 * counts the voxels of each component, which here is
 * {@code morphology.areaOpening} applied to a 3D image. Since the grayscale
 * volume of a component decreases when its sub-components are removed,
 * this filter is not idempotent.
 * </p>
 * <p>
 * Implemented by pruning the max-tree of the image (Salembier, Oliveras and
 * Garrido, 1998), built with the union-find algorithm of Najman and Couprie
 * (2006) as formulated by Berger et al. (2007), with the direct filtering
 * rule. Any {@code RealType} and any number of dimensions are supported.
 * </p>
 * <p>
 * See C. Vachier, "Extraction de caracteristiques, segmentation d'image et
 * morphologie mathematique", PhD thesis, Ecole des Mines de Paris, 1995; and
 * F. Meyer, "From connected operators to levelings", in <em>Mathematical
 * Morphology and its Applications to Image and Signal Processing</em>, Kluwer,
 * 1998, pp. 191-198.
 * </p>
 * <p>
 * NaN input values are not supported and cause an
 * {@link IllegalArgumentException}. Images are limited to 2<sup>31</sup>
 * pixels.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> image type
 * @implNote op names='morphology.grayVolumeOpening'
 */
public class GrayVolumeOpening<T extends RealType<T>> implements
	Computers.Arity3<RandomAccessibleInterval<T>, Double, Shape, RandomAccessibleInterval<T>>
{

	/**
	 * Computes the grayscale volume opening.
	 *
	 * @param input the image to filter
	 * @param minVolume the minimal volume of the preserved components, in pixels times intensity units
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
		final Double minVolume, @Nullable final Shape connectivity,
		final RandomAccessibleInterval<T> output)
	{
		AttributeFilters.filter(input, Attribute.GRAY_VOLUME, minVolume,
			connectivity, false, output);
	}
}
