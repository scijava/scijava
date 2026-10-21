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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.lgpl.AbstractOpTest;

/**
 * Compares the attribute openings and closings with MorphoLibJ on small 8-bit
 * images.
 * <p>
 * The expected arrays were produced by running MorphoLibJ 1.6.4 as a black box
 * on the same pseudo-random images ({@code F2}: 12x10 and {@code F3}: 6x5x4,
 * values 0 to 9, so with many plateaus and ties): {@code AreaOpeningQueue} and
 * {@code BoxDiagonalOpeningQueue} in 2D, and
 * {@code AttributeFiltering.volumeOpening} (the 3D size opening) in 3D. The
 * closings were obtained by applying the openings to the inverted image
 * ({@code 255 - f}) and inverting the result.
 * </p>
 */
public class AttributeFilterOracleTest extends AbstractOpTest {

	private static final long[] DIMS2 = { 12, 10 };
	private static final long[] DIMS3 = { 6, 5, 4 };

	@Test
	public void testAreaOpeningClosing2D() {
		check("morphology.areaOpening", DIMS2, F2, 3L, face(), AREA_OPEN3_C4);
		check("morphology.areaOpening", DIMS2, F2, 8L, face(), AREA_OPEN8_C4);
		check("morphology.areaOpening", DIMS2, F2, 20L, face(), AREA_OPEN20_C4);
		check("morphology.areaOpening", DIMS2, F2, 3L, full(), AREA_OPEN3_C8);
		check("morphology.areaOpening", DIMS2, F2, 8L, full(), AREA_OPEN8_C8);
		check("morphology.areaOpening", DIMS2, F2, 20L, full(), AREA_OPEN20_C8);
		check("morphology.areaClosing", DIMS2, F2, 3L, face(), AREA_CLOSE3_C4);
		check("morphology.areaClosing", DIMS2, F2, 8L, face(), AREA_CLOSE8_C4);
		check("morphology.areaClosing", DIMS2, F2, 20L, face(), AREA_CLOSE20_C4);
		check("morphology.areaClosing", DIMS2, F2, 3L, full(), AREA_CLOSE3_C8);
		check("morphology.areaClosing", DIMS2, F2, 8L, full(), AREA_CLOSE8_C8);
		check("morphology.areaClosing", DIMS2, F2, 20L, full(), AREA_CLOSE20_C8);
	}

	@Test
	public void testBoxDiagonalOpeningClosing2D() {
		check("morphology.boxDiagonalOpening", DIMS2, F2, 3.0, face(),
			BOX_OPEN3_C4);
		check("morphology.boxDiagonalOpening", DIMS2, F2, 5.0, face(),
			BOX_OPEN5_C4);
		check("morphology.boxDiagonalOpening", DIMS2, F2, 3.0, full(),
			BOX_OPEN3_C8);
		check("morphology.boxDiagonalOpening", DIMS2, F2, 5.0, full(),
			BOX_OPEN5_C8);
		check("morphology.boxDiagonalClosing", DIMS2, F2, 3.0, face(),
			BOX_CLOSE3_C4);
		check("morphology.boxDiagonalClosing", DIMS2, F2, 5.0, face(),
			BOX_CLOSE5_C4);
		check("morphology.boxDiagonalClosing", DIMS2, F2, 3.0, full(),
			BOX_CLOSE3_C8);
		check("morphology.boxDiagonalClosing", DIMS2, F2, 5.0, full(),
			BOX_CLOSE5_C8);
	}

	@Test
	public void testAreaOpeningClosing3D() {
		check("morphology.areaOpening", DIMS3, F3, 4L, face(), AREA3_OPEN4_C6);
		check("morphology.areaOpening", DIMS3, F3, 15L, face(), AREA3_OPEN15_C6);
		check("morphology.areaOpening", DIMS3, F3, 4L, full(), AREA3_OPEN4_C26);
		check("morphology.areaOpening", DIMS3, F3, 15L, full(), AREA3_OPEN15_C26);
		check("morphology.areaClosing", DIMS3, F3, 4L, face(), AREA3_CLOSE4_C6);
		check("morphology.areaClosing", DIMS3, F3, 15L, face(), AREA3_CLOSE15_C6);
		check("morphology.areaClosing", DIMS3, F3, 4L, full(), AREA3_CLOSE4_C26);
		check("morphology.areaClosing", DIMS3, F3, 15L, full(),
			AREA3_CLOSE15_C26);
	}

	private static Shape face() {
		return new DiamondShape(1);
	}

	private static Shape full() {
		return new RectangleShape(1, false);
	}

	private void check(final String name, final long[] dims, final int[] input,
		final Object threshold, final Shape connectivity, final int[] expected)
	{
		final var out = image(dims, new int[input.length]);
		ops.op(name).input(image(dims, input), threshold, connectivity).output(
			out).compute();
		assertArrayEquals(expected, values(out), name + " " + threshold);
	}

	private static Img<UnsignedByteType> image(final long[] dims,
		final int[] values)
	{
		final var bytes = new byte[values.length];
		for (var i = 0; i < values.length; i++)
			bytes[i] = (byte) values[i];
		return ArrayImgs.unsignedBytes(bytes, dims);
	}

	private static int[] values(
		final RandomAccessibleInterval<? extends RealType<?>> image)
	{
		final var result = new int[(int) Views.iterable(image).size()];
		var i = 0;
		for (final var t : Views.flatIterable(image))
			result[i++] = (int) t.getRealDouble();
		return result;
	}

	// -- Expected values (MorphoLibJ 1.6.4) --

	private static final int[] F2 = {
		0, 3, 7, 1, 2, 1, 9, 0, 4, 1, 7, 5,
		9, 6, 0, 4, 9, 3, 7, 5, 3, 1, 5, 8,
		3, 3, 6, 8, 7, 2, 0, 7, 8, 3, 1, 5,
		5, 9, 0, 5, 6, 1, 4, 5, 0, 1, 8, 3,
		0, 4, 4, 4, 2, 7, 9, 8, 1, 3, 2, 2,
		1, 6, 0, 7, 2, 5, 3, 4, 1, 3, 7, 2,
		8, 9, 0, 5, 7, 7, 2, 2, 7, 1, 3, 9,
		0, 5, 9, 1, 2, 8, 0, 5, 9, 8, 2, 2,
		5, 5, 6, 7, 8, 5, 6, 3, 8, 9, 6, 3,
		7, 4, 2, 1, 2, 3, 8, 2, 5, 1, 5, 9,
	};

	private static final int[] AREA_OPEN3_C4 = {
		0, 3, 3, 1, 2, 1, 5, 0, 3, 1, 5, 5,
		3, 3, 0, 4, 7, 3, 5, 5, 3, 1, 5, 5,
		3, 3, 6, 7, 7, 2, 0, 5, 5, 3, 1, 5,
		4, 4, 0, 5, 6, 1, 4, 5, 0, 1, 3, 3,
		0, 4, 4, 4, 2, 7, 7, 7, 1, 3, 2, 2,
		1, 6, 0, 5, 2, 5, 3, 4, 1, 3, 3, 2,
		6, 6, 0, 5, 7, 7, 2, 2, 7, 1, 3, 3,
		0, 5, 6, 1, 2, 7, 0, 5, 8, 8, 2, 2,
		5, 5, 6, 6, 6, 5, 5, 3, 8, 8, 6, 3,
		5, 4, 2, 1, 2, 3, 5, 2, 5, 1, 5, 5,
	};

	private static final int[] AREA_OPEN8_C4 = {
		0, 3, 3, 1, 2, 1, 5, 0, 3, 1, 2, 2,
		3, 3, 0, 4, 4, 3, 5, 5, 3, 1, 2, 2,
		3, 3, 4, 4, 4, 2, 0, 5, 5, 3, 1, 2,
		4, 4, 0, 4, 4, 1, 4, 5, 0, 1, 2, 2,
		0, 4, 4, 4, 2, 5, 5, 5, 1, 2, 2, 2,
		1, 5, 0, 5, 2, 5, 3, 4, 1, 2, 2, 2,
		5, 5, 0, 5, 5, 5, 2, 2, 5, 1, 2, 2,
		0, 5, 5, 1, 2, 5, 0, 5, 5, 5, 2, 2,
		5, 5, 5, 5, 5, 5, 5, 3, 5, 5, 5, 3,
		5, 4, 2, 1, 2, 3, 5, 2, 5, 1, 5, 5,
	};

	private static final int[] AREA_OPEN20_C4 = {
		0, 3, 3, 1, 2, 1, 5, 0, 3, 1, 2, 2,
		3, 3, 0, 4, 4, 3, 5, 5, 3, 1, 2, 2,
		3, 3, 4, 4, 4, 2, 0, 5, 5, 3, 1, 2,
		4, 4, 0, 4, 4, 1, 4, 5, 0, 1, 2, 2,
		0, 4, 4, 4, 2, 5, 5, 5, 1, 2, 2, 2,
		1, 5, 0, 5, 2, 5, 3, 4, 1, 2, 2, 2,
		5, 5, 0, 5, 5, 5, 2, 2, 3, 1, 2, 2,
		0, 5, 5, 1, 2, 5, 0, 3, 3, 3, 2, 2,
		5, 5, 5, 5, 5, 5, 5, 3, 3, 3, 3, 3,
		5, 4, 2, 1, 2, 3, 5, 2, 3, 1, 3, 3,
	};

	private static final int[] BOX_OPEN3_C4 = {
		0, 3, 3, 1, 2, 1, 5, 0, 3, 1, 3, 3,
		3, 3, 0, 4, 4, 3, 5, 5, 3, 1, 3, 3,
		3, 3, 4, 4, 4, 2, 0, 5, 5, 3, 1, 3,
		4, 4, 0, 4, 4, 1, 4, 5, 0, 1, 3, 3,
		0, 4, 4, 4, 2, 5, 5, 5, 1, 2, 2, 2,
		1, 5, 0, 5, 2, 5, 3, 4, 1, 2, 2, 2,
		5, 5, 0, 5, 5, 5, 2, 2, 5, 1, 2, 2,
		0, 5, 5, 1, 2, 5, 0, 5, 5, 5, 2, 2,
		5, 5, 5, 5, 5, 5, 5, 3, 5, 5, 5, 3,
		5, 4, 2, 1, 2, 3, 5, 2, 5, 1, 5, 5,
	};

	private static final int[] BOX_OPEN5_C4 = {
		0, 3, 3, 1, 2, 1, 5, 0, 3, 1, 2, 2,
		3, 3, 0, 4, 4, 3, 5, 5, 3, 1, 2, 2,
		3, 3, 4, 4, 4, 2, 0, 5, 5, 3, 1, 2,
		4, 4, 0, 4, 4, 1, 4, 5, 0, 1, 2, 2,
		0, 4, 4, 4, 2, 5, 5, 5, 1, 2, 2, 2,
		1, 5, 0, 5, 2, 5, 3, 4, 1, 2, 2, 2,
		5, 5, 0, 5, 5, 5, 2, 2, 5, 1, 2, 2,
		0, 5, 5, 1, 2, 5, 0, 5, 5, 5, 2, 2,
		5, 5, 5, 5, 5, 5, 5, 3, 5, 5, 5, 3,
		5, 4, 2, 1, 2, 3, 5, 2, 5, 1, 5, 5,
	};

	private static final int[] AREA_OPEN3_C8 = {
		0, 3, 6, 1, 2, 1, 7, 0, 4, 1, 5, 5,
		6, 6, 0, 4, 7, 3, 7, 5, 3, 1, 5, 5,
		3, 3, 6, 7, 7, 2, 0, 7, 7, 3, 1, 5,
		5, 6, 0, 5, 6, 1, 4, 5, 0, 1, 5, 3,
		0, 4, 4, 4, 2, 7, 7, 7, 1, 3, 2, 2,
		1, 6, 0, 7, 2, 5, 3, 4, 1, 3, 3, 2,
		8, 8, 0, 5, 7, 7, 2, 2, 7, 1, 3, 3,
		0, 5, 8, 1, 2, 7, 0, 5, 8, 8, 2, 2,
		5, 5, 6, 7, 7, 5, 6, 3, 8, 8, 6, 3,
		5, 4, 2, 1, 2, 3, 6, 2, 5, 1, 5, 6,
	};

	private static final int[] AREA_OPEN8_C8 = {
		0, 3, 6, 1, 2, 1, 5, 0, 4, 1, 3, 3,
		6, 6, 0, 4, 6, 3, 5, 5, 3, 1, 3, 3,
		3, 3, 6, 6, 6, 2, 0, 5, 5, 3, 1, 3,
		5, 6, 0, 5, 6, 1, 4, 5, 0, 1, 3, 3,
		0, 4, 4, 4, 2, 6, 6, 6, 1, 3, 2, 2,
		1, 6, 0, 7, 2, 5, 3, 4, 1, 3, 3, 2,
		7, 7, 0, 5, 7, 7, 2, 2, 5, 1, 3, 3,
		0, 5, 7, 1, 2, 7, 0, 5, 5, 5, 2, 2,
		5, 5, 6, 7, 7, 5, 6, 3, 5, 5, 5, 3,
		5, 4, 2, 1, 2, 3, 6, 2, 5, 1, 5, 5,
	};

	private static final int[] AREA_OPEN20_C8 = {
		0, 3, 5, 1, 2, 1, 5, 0, 4, 1, 3, 3,
		5, 5, 0, 4, 5, 3, 5, 5, 3, 1, 3, 3,
		3, 3, 5, 5, 5, 2, 0, 5, 5, 3, 1, 3,
		5, 5, 0, 5, 5, 1, 4, 5, 0, 1, 3, 3,
		0, 4, 4, 4, 2, 5, 5, 5, 1, 3, 2, 2,
		1, 5, 0, 5, 2, 5, 3, 4, 1, 3, 3, 2,
		5, 5, 0, 5, 5, 5, 2, 2, 5, 1, 3, 3,
		0, 5, 5, 1, 2, 5, 0, 5, 5, 5, 2, 2,
		5, 5, 5, 5, 5, 5, 5, 3, 5, 5, 5, 3,
		5, 4, 2, 1, 2, 3, 5, 2, 5, 1, 5, 5,
	};

	private static final int[] BOX_OPEN3_C8 = {
		0, 3, 6, 1, 2, 1, 5, 0, 4, 1, 5, 5,
		6, 6, 0, 4, 6, 3, 5, 5, 3, 1, 5, 5,
		3, 3, 6, 6, 6, 2, 0, 5, 5, 3, 1, 5,
		5, 6, 0, 5, 6, 1, 4, 5, 0, 1, 5, 3,
		0, 4, 4, 4, 2, 6, 6, 6, 1, 3, 2, 2,
		1, 6, 0, 7, 2, 5, 3, 4, 1, 3, 3, 2,
		7, 7, 0, 5, 7, 7, 2, 2, 6, 1, 3, 3,
		0, 5, 7, 1, 2, 7, 0, 5, 6, 6, 2, 2,
		5, 5, 6, 7, 7, 5, 6, 3, 6, 6, 6, 3,
		5, 4, 2, 1, 2, 3, 6, 2, 5, 1, 5, 6,
	};

	private static final int[] BOX_OPEN5_C8 = {
		0, 3, 6, 1, 2, 1, 5, 0, 4, 1, 3, 3,
		6, 6, 0, 4, 6, 3, 5, 5, 3, 1, 3, 3,
		3, 3, 6, 6, 6, 2, 0, 5, 5, 3, 1, 3,
		5, 6, 0, 5, 6, 1, 4, 5, 0, 1, 3, 3,
		0, 4, 4, 4, 2, 6, 6, 6, 1, 3, 2, 2,
		1, 6, 0, 7, 2, 5, 3, 4, 1, 3, 3, 2,
		7, 7, 0, 5, 7, 7, 2, 2, 5, 1, 3, 3,
		0, 5, 7, 1, 2, 7, 0, 5, 5, 5, 2, 2,
		5, 5, 6, 7, 7, 5, 6, 3, 5, 5, 5, 3,
		5, 4, 2, 1, 2, 3, 6, 2, 5, 1, 5, 5,
	};

	private static final int[] AREA_CLOSE3_C4 = {
		6, 6, 7, 2, 2, 2, 9, 4, 4, 3, 7, 7,
		9, 6, 4, 4, 9, 3, 7, 5, 3, 3, 5, 8,
		5, 5, 6, 8, 7, 2, 2, 7, 8, 3, 3, 5,
		5, 9, 4, 5, 6, 2, 4, 5, 1, 1, 8, 3,
		4, 4, 4, 4, 4, 7, 9, 8, 1, 3, 2, 2,
		4, 6, 4, 7, 4, 5, 3, 4, 1, 3, 7, 2,
		8, 9, 4, 5, 7, 7, 2, 2, 7, 3, 3, 9,
		5, 5, 9, 5, 5, 8, 2, 5, 9, 8, 3, 3,
		5, 5, 6, 7, 8, 5, 6, 5, 8, 9, 6, 3,
		7, 4, 2, 2, 2, 3, 8, 5, 5, 5, 5, 9,
	};

	private static final int[] AREA_CLOSE8_C4 = {
		6, 6, 7, 4, 4, 4, 9, 4, 4, 3, 7, 7,
		9, 6, 4, 4, 9, 4, 7, 5, 3, 3, 5, 8,
		5, 5, 6, 8, 7, 4, 4, 7, 8, 3, 3, 5,
		5, 9, 4, 5, 6, 4, 4, 5, 3, 3, 8, 3,
		4, 4, 4, 4, 4, 7, 9, 8, 3, 3, 3, 3,
		4, 6, 4, 7, 4, 5, 4, 4, 3, 3, 7, 3,
		8, 9, 4, 5, 7, 7, 4, 4, 7, 3, 3, 9,
		5, 5, 9, 5, 5, 8, 4, 5, 9, 8, 3, 3,
		5, 5, 6, 7, 8, 5, 6, 5, 8, 9, 6, 3,
		7, 5, 5, 5, 5, 5, 8, 5, 5, 5, 5, 9,
	};

	private static final int[] AREA_CLOSE20_C4 = {
		6, 6, 7, 5, 5, 5, 9, 4, 4, 3, 7, 7,
		9, 6, 5, 5, 9, 5, 7, 5, 3, 3, 5, 8,
		5, 5, 6, 8, 7, 5, 5, 7, 8, 3, 3, 5,
		5, 9, 5, 5, 6, 5, 5, 5, 3, 3, 8, 3,
		5, 5, 5, 5, 5, 7, 9, 8, 3, 3, 3, 3,
		5, 6, 5, 7, 5, 5, 4, 4, 3, 3, 7, 3,
		8, 9, 5, 5, 7, 7, 4, 4, 7, 3, 3, 9,
		6, 6, 9, 5, 5, 8, 4, 5, 9, 8, 3, 3,
		6, 6, 6, 7, 8, 6, 6, 5, 8, 9, 6, 3,
		7, 6, 6, 6, 6, 6, 8, 5, 5, 5, 5, 9,
	};

	private static final int[] BOX_CLOSE3_C4 = {
		6, 6, 7, 3, 3, 3, 9, 4, 4, 3, 7, 7,
		9, 6, 4, 4, 9, 3, 7, 5, 3, 3, 5, 8,
		5, 5, 6, 8, 7, 3, 3, 7, 8, 3, 3, 5,
		5, 9, 4, 5, 6, 3, 4, 5, 3, 3, 8, 3,
		4, 4, 4, 4, 4, 7, 9, 8, 3, 3, 3, 3,
		4, 6, 4, 7, 4, 5, 4, 4, 3, 3, 7, 3,
		8, 9, 4, 5, 7, 7, 4, 4, 7, 3, 3, 9,
		5, 5, 9, 5, 5, 8, 4, 5, 9, 8, 3, 3,
		5, 5, 6, 7, 8, 5, 6, 5, 8, 9, 6, 3,
		7, 4, 3, 3, 3, 3, 8, 5, 5, 5, 5, 9,
	};

	private static final int[] BOX_CLOSE5_C4 = {
		6, 6, 7, 4, 4, 4, 9, 4, 4, 3, 7, 7,
		9, 6, 4, 4, 9, 4, 7, 5, 3, 3, 5, 8,
		5, 5, 6, 8, 7, 4, 4, 7, 8, 3, 3, 5,
		5, 9, 4, 5, 6, 4, 4, 5, 3, 3, 8, 3,
		4, 4, 4, 4, 4, 7, 9, 8, 3, 3, 3, 3,
		4, 6, 4, 7, 4, 5, 4, 4, 3, 3, 7, 3,
		8, 9, 4, 5, 7, 7, 4, 4, 7, 3, 3, 9,
		5, 5, 9, 5, 5, 8, 4, 5, 9, 8, 3, 3,
		5, 5, 6, 7, 8, 5, 6, 5, 8, 9, 6, 3,
		7, 5, 5, 5, 5, 5, 8, 5, 5, 5, 5, 9,
	};

	private static final int[] AREA_CLOSE3_C8 = {
		3, 3, 7, 2, 2, 2, 9, 3, 4, 1, 7, 5,
		9, 6, 2, 4, 9, 3, 7, 5, 3, 1, 5, 8,
		3, 3, 6, 8, 7, 2, 2, 7, 8, 3, 1, 5,
		5, 9, 3, 5, 6, 2, 4, 5, 1, 1, 8, 3,
		4, 4, 4, 4, 2, 7, 9, 8, 1, 3, 2, 2,
		4, 6, 1, 7, 2, 5, 3, 4, 1, 3, 7, 2,
		8, 9, 1, 5, 7, 7, 2, 2, 7, 1, 3, 9,
		5, 5, 9, 1, 2, 8, 2, 5, 9, 8, 2, 2,
		5, 5, 6, 7, 8, 5, 6, 3, 8, 9, 6, 3,
		7, 4, 2, 2, 2, 3, 8, 3, 5, 5, 5, 9,
	};

	private static final int[] AREA_CLOSE8_C8 = {
		3, 3, 7, 3, 3, 3, 9, 3, 4, 1, 7, 5,
		9, 6, 3, 4, 9, 3, 7, 5, 3, 1, 5, 8,
		3, 3, 6, 8, 7, 3, 3, 7, 8, 3, 1, 5,
		5, 9, 3, 5, 6, 3, 4, 5, 1, 1, 8, 3,
		4, 4, 4, 4, 3, 7, 9, 8, 1, 3, 2, 2,
		4, 6, 4, 7, 3, 5, 3, 4, 1, 3, 7, 2,
		8, 9, 4, 5, 7, 7, 2, 2, 7, 1, 3, 9,
		5, 5, 9, 4, 4, 8, 2, 5, 9, 8, 2, 2,
		5, 5, 6, 7, 8, 5, 6, 3, 8, 9, 6, 3,
		7, 5, 5, 5, 5, 5, 8, 3, 5, 5, 5, 9,
	};

	private static final int[] AREA_CLOSE20_C8 = {
		4, 4, 7, 4, 4, 4, 9, 3, 4, 3, 7, 5,
		9, 6, 4, 4, 9, 4, 7, 5, 3, 3, 5, 8,
		4, 4, 6, 8, 7, 4, 4, 7, 8, 3, 3, 5,
		5, 9, 4, 5, 6, 4, 4, 5, 3, 3, 8, 3,
		4, 4, 4, 4, 4, 7, 9, 8, 3, 3, 3, 3,
		4, 6, 4, 7, 4, 5, 3, 4, 3, 3, 7, 3,
		8, 9, 4, 5, 7, 7, 3, 3, 7, 3, 3, 9,
		5, 5, 9, 4, 4, 8, 3, 5, 9, 8, 3, 3,
		5, 5, 6, 7, 8, 5, 6, 3, 8, 9, 6, 3,
		7, 5, 5, 5, 5, 5, 8, 3, 5, 5, 5, 9,
	};

	private static final int[] BOX_CLOSE3_C8 = {
		3, 3, 7, 2, 2, 2, 9, 3, 4, 1, 7, 5,
		9, 6, 2, 4, 9, 3, 7, 5, 3, 1, 5, 8,
		3, 3, 6, 8, 7, 2, 2, 7, 8, 3, 1, 5,
		5, 9, 3, 5, 6, 2, 4, 5, 1, 1, 8, 3,
		4, 4, 4, 4, 2, 7, 9, 8, 1, 3, 2, 2,
		4, 6, 4, 7, 2, 5, 3, 4, 1, 3, 7, 2,
		8, 9, 4, 5, 7, 7, 2, 2, 7, 1, 3, 9,
		5, 5, 9, 4, 4, 8, 2, 5, 9, 8, 2, 2,
		5, 5, 6, 7, 8, 5, 6, 3, 8, 9, 6, 3,
		7, 4, 3, 3, 3, 3, 8, 3, 5, 5, 5, 9,
	};

	private static final int[] BOX_CLOSE5_C8 = {
		3, 3, 7, 3, 3, 3, 9, 3, 4, 1, 7, 5,
		9, 6, 3, 4, 9, 3, 7, 5, 3, 1, 5, 8,
		3, 3, 6, 8, 7, 3, 3, 7, 8, 3, 1, 5,
		5, 9, 3, 5, 6, 3, 4, 5, 1, 1, 8, 3,
		4, 4, 4, 4, 3, 7, 9, 8, 1, 3, 2, 2,
		4, 6, 4, 7, 3, 5, 3, 4, 1, 3, 7, 2,
		8, 9, 4, 5, 7, 7, 2, 2, 7, 1, 3, 9,
		5, 5, 9, 4, 4, 8, 2, 5, 9, 8, 2, 2,
		5, 5, 6, 7, 8, 5, 6, 3, 8, 9, 6, 3,
		7, 5, 5, 5, 5, 5, 8, 3, 5, 5, 5, 9,
	};

	private static final int[] F3 = {
		0, 0, 3, 9, 9, 9,
		5, 6, 2, 3, 2, 8,
		4, 4, 4, 6, 4, 5,
		6, 2, 5, 9, 2, 4,
		9, 9, 5, 9, 0, 0,
		5, 1, 0, 1, 0, 4,
		8, 7, 4, 5, 7, 4,
		6, 5, 2, 9, 8, 0,
		7, 1, 9, 9, 1, 9,
		9, 6, 1, 6, 3, 3,
		9, 7, 4, 5, 5, 6,
		7, 4, 9, 3, 5, 9,
		7, 2, 5, 5, 6, 0,
		5, 2, 9, 1, 2, 9,
		7, 5, 2, 9, 8, 1,
		0, 0, 0, 6, 9, 8,
		8, 6, 6, 7, 5, 2,
		9, 8, 7, 8, 9, 1,
		1, 3, 5, 9, 5, 3,
		2, 2, 2, 6, 9, 6,
	};

	private static final int[] AREA3_OPEN4_C6 = {
		0, 0, 3, 8, 8, 8,
		5, 6, 2, 3, 2, 8,
		4, 4, 4, 6, 4, 5,
		6, 2, 5, 9, 2, 4,
		7, 7, 5, 9, 0, 0,
		5, 1, 0, 1, 0, 4,
		7, 7, 4, 5, 7, 4,
		6, 5, 2, 9, 8, 0,
		7, 1, 9, 9, 1, 4,
		7, 6, 1, 6, 3, 3,
		7, 7, 4, 5, 5, 6,
		7, 4, 6, 3, 5, 6,
		7, 2, 5, 5, 6, 0,
		5, 2, 9, 1, 2, 4,
		7, 5, 2, 6, 6, 1,
		0, 0, 0, 6, 6, 6,
		7, 6, 6, 7, 5, 2,
		7, 7, 7, 7, 7, 1,
		1, 3, 5, 7, 5, 3,
		2, 2, 2, 6, 6, 6,
	};

	private static final int[] AREA3_OPEN15_C6 = {
		0, 0, 3, 4, 4, 4,
		5, 6, 2, 3, 2, 4,
		4, 4, 4, 6, 4, 4,
		6, 2, 5, 6, 2, 4,
		6, 6, 5, 6, 0, 0,
		5, 1, 0, 1, 0, 4,
		6, 6, 4, 5, 6, 4,
		6, 5, 2, 6, 6, 0,
		6, 1, 6, 6, 1, 4,
		6, 6, 1, 6, 3, 3,
		6, 6, 4, 5, 5, 6,
		6, 4, 6, 3, 5, 6,
		6, 2, 5, 5, 6, 0,
		5, 2, 6, 1, 2, 4,
		6, 5, 2, 6, 6, 1,
		0, 0, 0, 6, 6, 6,
		6, 6, 6, 6, 5, 2,
		6, 6, 6, 6, 6, 1,
		1, 3, 5, 6, 5, 3,
		2, 2, 2, 6, 6, 6,
	};

	private static final int[] AREA3_OPEN4_C26 = {
		0, 0, 3, 8, 8, 8,
		5, 6, 2, 3, 2, 8,
		4, 4, 4, 6, 4, 5,
		6, 2, 5, 9, 2, 4,
		9, 9, 5, 9, 0, 0,
		5, 1, 0, 1, 0, 4,
		8, 7, 4, 5, 7, 4,
		6, 5, 2, 9, 8, 0,
		7, 1, 9, 9, 1, 9,
		9, 6, 1, 6, 3, 3,
		8, 7, 4, 5, 5, 6,
		7, 4, 9, 3, 5, 9,
		7, 2, 5, 5, 6, 0,
		5, 2, 9, 1, 2, 9,
		7, 5, 2, 9, 8, 1,
		0, 0, 0, 6, 9, 8,
		8, 6, 6, 7, 5, 2,
		8, 8, 7, 8, 9, 1,
		1, 3, 5, 9, 5, 3,
		2, 2, 2, 6, 9, 6,
	};

	private static final int[] AREA3_OPEN15_C26 = {
		0, 0, 3, 8, 8, 8,
		5, 6, 2, 3, 2, 8,
		4, 4, 4, 6, 4, 5,
		6, 2, 5, 9, 2, 4,
		9, 9, 5, 9, 0, 0,
		5, 1, 0, 1, 0, 4,
		8, 7, 4, 5, 7, 4,
		6, 5, 2, 9, 8, 0,
		7, 1, 9, 9, 1, 9,
		9, 6, 1, 6, 3, 3,
		8, 7, 4, 5, 5, 6,
		7, 4, 9, 3, 5, 9,
		7, 2, 5, 5, 6, 0,
		5, 2, 9, 1, 2, 9,
		7, 5, 2, 9, 8, 1,
		0, 0, 0, 6, 9, 8,
		8, 6, 6, 7, 5, 2,
		8, 8, 7, 8, 9, 1,
		1, 3, 5, 9, 5, 3,
		2, 2, 2, 6, 9, 6,
	};

	private static final int[] AREA3_CLOSE4_C6 = {
		1, 1, 3, 9, 9, 9,
		5, 6, 3, 3, 3, 8,
		4, 4, 4, 6, 4, 5,
		6, 2, 5, 9, 2, 4,
		9, 9, 5, 9, 2, 2,
		5, 1, 1, 1, 1, 4,
		8, 7, 4, 5, 7, 4,
		6, 5, 4, 9, 8, 2,
		7, 2, 9, 9, 2, 9,
		9, 6, 2, 6, 3, 3,
		9, 7, 4, 5, 5, 6,
		7, 4, 9, 5, 5, 9,
		7, 2, 5, 5, 6, 2,
		5, 2, 9, 2, 2, 9,
		7, 5, 2, 9, 8, 3,
		4, 4, 4, 6, 9, 8,
		8, 6, 6, 7, 5, 2,
		9, 8, 7, 8, 9, 2,
		2, 3, 5, 9, 5, 3,
		2, 2, 2, 6, 9, 6,
	};

	private static final int[] AREA3_CLOSE15_C6 = {
		4, 4, 4, 9, 9, 9,
		5, 6, 4, 4, 4, 8,
		4, 4, 4, 6, 4, 5,
		6, 4, 5, 9, 4, 4,
		9, 9, 5, 9, 4, 4,
		5, 4, 4, 4, 4, 4,
		8, 7, 4, 5, 7, 4,
		6, 5, 4, 9, 8, 4,
		7, 4, 9, 9, 4, 9,
		9, 6, 4, 6, 4, 4,
		9, 7, 4, 5, 5, 6,
		7, 4, 9, 5, 5, 9,
		7, 4, 5, 5, 6, 4,
		5, 4, 9, 4, 4, 9,
		7, 5, 4, 9, 8, 4,
		4, 4, 4, 6, 9, 8,
		8, 6, 6, 7, 5, 4,
		9, 8, 7, 8, 9, 4,
		4, 4, 5, 9, 5, 4,
		4, 4, 4, 6, 9, 6,
	};

	private static final int[] AREA3_CLOSE4_C26 = {
		1, 1, 3, 9, 9, 9,
		5, 6, 2, 3, 2, 8,
		4, 4, 4, 6, 4, 5,
		6, 2, 5, 9, 2, 4,
		9, 9, 5, 9, 1, 1,
		5, 1, 1, 1, 1, 4,
		8, 7, 4, 5, 7, 4,
		6, 5, 2, 9, 8, 1,
		7, 1, 9, 9, 1, 9,
		9, 6, 1, 6, 3, 3,
		9, 7, 4, 5, 5, 6,
		7, 4, 9, 3, 5, 9,
		7, 2, 5, 5, 6, 1,
		5, 2, 9, 1, 2, 9,
		7, 5, 2, 9, 8, 1,
		3, 3, 3, 6, 9, 8,
		8, 6, 6, 7, 5, 2,
		9, 8, 7, 8, 9, 1,
		2, 3, 5, 9, 5, 3,
		2, 2, 2, 6, 9, 6,
	};

	private static final int[] AREA3_CLOSE15_C26 = {
		2, 2, 3, 9, 9, 9,
		5, 6, 2, 3, 2, 8,
		4, 4, 4, 6, 4, 5,
		6, 2, 5, 9, 2, 4,
		9, 9, 5, 9, 2, 2,
		5, 2, 2, 2, 2, 4,
		8, 7, 4, 5, 7, 4,
		6, 5, 2, 9, 8, 2,
		7, 2, 9, 9, 2, 9,
		9, 6, 2, 6, 3, 3,
		9, 7, 4, 5, 5, 6,
		7, 4, 9, 3, 5, 9,
		7, 2, 5, 5, 6, 2,
		5, 2, 9, 2, 2, 9,
		7, 5, 2, 9, 8, 2,
		3, 3, 3, 6, 9, 8,
		8, 6, 6, 7, 5, 2,
		9, 8, 7, 8, 9, 2,
		2, 3, 5, 9, 5, 3,
		2, 2, 2, 6, 9, 6,
	};
}
