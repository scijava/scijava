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

package org.scijava.ops.image.morphology.reconstruction;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;

/**
 * Marks the extended maxima of an image in a binary mask, in any number of
 * dimensions: the regional maxima of its h-maxima transform (see
 * {@code morphology.hMaxima}).
 * <p>
 * These are the maxima whose dynamic (height above the highest saddle leading
 * to a higher maximum) exceeds {@code h}, each marked together with the part
 * of its peak lying above its value minus {@code h}. They are typical markers
 * for a watershed segmentation of bright objects.
 * </p>
 * <p>
 * See P. Soille, <em>Morphological Image Analysis</em>, 2nd ed., Springer,
 * 2003, section 6.3.5.
 * </p>
 *
 * @author Curtis Rueden
 * @param <T> image type
 * @param <B> mask type
 * @implNote op names='morphology.extendedMaxima'
 */
public class ExtendedMaxima<T extends RealType<T>, B extends BooleanType<B>>
	implements
	Computers.Arity3<RandomAccessibleInterval<T>, Double, Shape, RandomAccessibleInterval<B>>
{

	/**
	 * Finds the extended maxima.
	 *
	 * @param input the image in which to search for maxima
	 * @param h the (non-negative) minimal dynamic of the maxima to keep
	 * @param connectivity the neighborhood defining which pixels are adjacent,
	 *          such as a {@code DiamondShape} of radius 1 for face connectivity
	 *          (4-connectivity in 2D, 6-connectivity in 3D) or a
	 *          {@code RectangleShape} of span 1 for full connectivity
	 *          (8-connectivity in 2D, 26-connectivity in 3D). Defaults to full
	 *          connectivity.
	 * @param output the mask, of the same dimensions as {@code input}; set to
	 *          true on extended maxima and false elsewhere
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input, final Double h,
		@Nullable final Shape connectivity,
		final RandomAccessibleInterval<B> output)
	{
		final var grid = new PixelGrid(input, connectivity);
		final var values = grid.values(input);
		Reconstructions.hMaxima(values, h, Reconstructions.lowest(input
			.getType()), grid);
		grid.write(Reconstructions.regionalMaxima(values, grid), output);
	}
}
