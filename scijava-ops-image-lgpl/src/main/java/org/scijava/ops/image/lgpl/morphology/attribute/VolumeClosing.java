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
 * Computes the grayscale volume closing of an image, in any number of
 * dimensions: removes the dark structures of small volume, that is with a
 * small product of size and contrast.
 * <p>
 * Each connected component of each lower level set of the image is preserved
 * if its volume is at least {@code minVolume}; the others are raised to the
 * level of the smallest preserved component containing them. The volume of a
 * component is the sum, over its pixels, of their height below the level at
 * which the component merges with a larger one, that is the minimum value of
 * the image on the pixels adjacent to the component. The component covering
 * the whole image is always preserved.
 * </p>
 * <p>
 * This is a grayscale volume, not a 3D area: for a 3D size closing, use
 * {@code morphology.areaClosing}. Since the volume of a component decreases
 * when its sub-components are removed, this filter is not idempotent.
 * </p>
 * <p>
 * Implemented by pruning the min-tree of the image (Salembier, Oliveras and
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
 *
 * @author Curtis Rueden
 * @param <T> image type
 * @implNote op names='morphology.volumeClosing'
 */
public class VolumeClosing<T extends RealType<T>> implements
	Computers.Arity3<RandomAccessibleInterval<T>, Double, Shape, RandomAccessibleInterval<T>>
{

	/**
	 * Computes the volume closing.
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
		AttributeFilters.filter(input, Attribute.VOLUME, minVolume,
			connectivity, true, output);
	}
}
