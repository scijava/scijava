/*-
 * #%L
 * GPL-licensed image processing operations for SciJava Ops, translated from copyleft ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.gpl.skeleton;

import java.util.function.Function;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;

/**
 * Classifies the elements of a skeleton by their number of foreground
 * neighbors in the 3<sup>n</sup>-1 neighborhood: elements with fewer than two
 * neighbors are end points ({@value #END_POINT}), elements with exactly two
 * are slab (branch) elements ({@value #SLAB}) and elements with more than two
 * are junctions ({@value #JUNCTION}); background is 0. Elements outside the
 * image are background.
 * <p>
 * This is the n-dimensional generalization of the voxel classification step
 * of the Fiji <a href="https://github.com/fiji/AnalyzeSkeleton">AnalyzeSkeleton</a>
 * plugin by Ignacio Arganda-Carreras (GPL v3+), which uses the tag values 30
 * (end point), 127 (slab) and 70 (junction) instead.
 * </p>
 * <p>
 * Reference: I. Arganda-Carreras, R. Fernández-González, A. Muñoz-Barrutia and
 * C. Ortiz-de-Solórzano, "3D reconstruction of histological sections:
 * Application to mammary gland tissue", Microscopy Research and Technique
 * 73(11):1019-1029, 2010.
 * </p>
 *
 * @author Ignacio Arganda-Carreras
 * @author Curtis Rueden
 * @param <B> type of the binary input
 * @implNote op names='morphology.tagSkeleton'
 */
public class TagSkeleton<B extends BooleanType<B>> implements
	Function<RandomAccessibleInterval<B>, RandomAccessibleInterval<UnsignedByteType>>
{

	/** Tag value of end point elements. */
	public static final int END_POINT = 1;

	/** Tag value of slab (branch) elements. */
	public static final int SLAB = 2;

	/** Tag value of junction elements. */
	public static final int JUNCTION = 3;

	/**
	 * Tags the elements of a skeleton.
	 *
	 * @param input the skeleton; {@code true} elements are foreground
	 * @return the tagged image, with the same interval as the input
	 */
	@Override
	public RandomAccessibleInterval<UnsignedByteType> apply(
		final RandomAccessibleInterval<B> input)
	{
		final RandomAccessibleInterval<UnsignedByteType> out = Views.translate(
			ArrayImgs.unsignedBytes(input.dimensionsAsLongArray()), input
				.minAsLongArray());
		final B background = input.getType().createVariable();
		background.set(false);
		final RandomAccess<Neighborhood<B>> na = new RectangleShape(1, true)
			.neighborhoodsRandomAccessibleSafe(Views.extendValue(input, background))
			.randomAccess();
		final Cursor<B> c = Views.flatIterable(input).localizingCursor();
		final RandomAccess<UnsignedByteType> ra = out.randomAccess();
		while (c.hasNext()) {
			c.fwd();
			if (!c.get().get()) continue;
			na.setPosition(c);
			int count = 0;
			for (final B b : na.get())
				if (b.get()) count++;
			ra.setPosition(c);
			ra.get().set(count < 2 ? END_POINT : count > 2 ? JUNCTION : SLAB);
		}
		return out;
	}
}
