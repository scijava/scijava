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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.lgpl.AbstractOpTest;

/**
 * Compares the ops of this package with MorphoLibJ on small 8-bit images.
 * <p>
 * The expected arrays were produced by running MorphoLibJ 1.6.4
 * ({@code inra.ijpb.morphology.Reconstruction}, {@code Reconstruction3D},
 * {@code MinimaAndMaxima} and {@code MinimaAndMaxima3D}) as a black box on the
 * same pseudo-random images ({@code F2}: 9x7 and {@code F3}: 5x4x3, values 0
 * to 9, so with many plateaus and ties). Binary results are 0 or 255.
 * Extended extrema use {@code h = 2}. MorphoLibJ {@code killBorders} and
 * {@code fillHoles} have no connectivity parameter and use face connectivity.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class ReconstructionOracleTest extends AbstractOpTest {

	private static final long[] DIMS2 = { 9, 7 };
	private static final long[] DIMS3 = { 5, 4, 3 };

	@Test
	public void testReconstruction2D() {
		checkReconstruction(DIMS2, F2, SEEDS2, SEEDS_HIGH2, face(), REC_DIL2_C4,
			REC_ERO2_C4);
		checkReconstruction(DIMS2, F2, SEEDS2, SEEDS_HIGH2, full(), REC_DIL2_C8,
			REC_ERO2_C8);
	}

	@Test
	public void testReconstruction3D() {
		checkReconstruction(DIMS3, F3, SEEDS3, SEEDS_HIGH3, face(), REC_DIL3_C6,
			REC_ERO3_C6);
		checkReconstruction(DIMS3, F3, SEEDS3, SEEDS_HIGH3, full(), REC_DIL3_C26,
			REC_ERO3_C26);
	}

	@Test
	public void testRegionalExtrema() {
		checkBinary("morphology.regionalMaxima", DIMS2, F2, null, face(),
			RMAX2_C4);
		checkBinary("morphology.regionalMaxima", DIMS2, F2, null, full(),
			RMAX2_C8);
		checkBinary("morphology.regionalMinima", DIMS2, F2, null, face(),
			RMIN2_C4);
		checkBinary("morphology.regionalMinima", DIMS2, F2, null, full(),
			RMIN2_C8);
		checkBinary("morphology.regionalMaxima", DIMS3, F3, null, face(),
			RMAX3_C6);
		checkBinary("morphology.regionalMaxima", DIMS3, F3, null, full(),
			RMAX3_C26);
		checkBinary("morphology.regionalMinima", DIMS3, F3, null, face(),
			RMIN3_C6);
		checkBinary("morphology.regionalMinima", DIMS3, F3, null, full(),
			RMIN3_C26);
	}

	@Test
	public void testExtendedExtrema() {
		checkBinary("morphology.extendedMaxima", DIMS2, F2, 2.0, face(), EMAX2_C4);
		checkBinary("morphology.extendedMaxima", DIMS2, F2, 2.0, full(), EMAX2_C8);
		checkBinary("morphology.extendedMinima", DIMS2, F2, 2.0, face(), EMIN2_C4);
		checkBinary("morphology.extendedMinima", DIMS2, F2, 2.0, full(), EMIN2_C8);
		checkBinary("morphology.extendedMaxima", DIMS3, F3, 2.0, face(), EMAX3_C6);
		checkBinary("morphology.extendedMaxima", DIMS3, F3, 2.0, full(),
			EMAX3_C26);
		checkBinary("morphology.extendedMinima", DIMS3, F3, 2.0, face(), EMIN3_C6);
		checkBinary("morphology.extendedMinima", DIMS3, F3, 2.0, full(),
			EMIN3_C26);
	}

	@Test
	public void testImposeExtrema() {
		checkImpose(DIMS2, F2, MARKERS2, face(), IMIN2_C4);
		checkImpose(DIMS2, F2, MARKERS2, full(), IMIN2_C8);
		checkImpose(DIMS3, F3, MARKERS3, face(), IMIN3_C6);
		checkImpose(DIMS3, F3, MARKERS3, full(), IMIN3_C26);
	}

	@Test
	public void testKillBordersAndFillHoles() {
		checkGray("morphology.killBorders", DIMS2, F2, face(), KILL2);
		checkGray("morphology.fillHoles", DIMS2, F2, face(), FILL2);
		checkGray("morphology.killBorders", DIMS3, F3, face(), KILL3);
		checkGray("morphology.fillHoles", DIMS3, F3, face(), FILL3);
	}

	private static Shape face() {
		return new DiamondShape(1);
	}

	private static Shape full() {
		return new RectangleShape(1, false);
	}

	private void checkReconstruction(final long[] dims, final int[] mask,
		final int[] seeds, final int[] seedsHigh, final Shape connectivity,
		final int[] expectedDilation, final int[] expectedErosion)
	{
		final var out = image(dims, new int[mask.length]);
		ops.op("morphology.reconstructByDilation").input(image(dims, seeds), image(
			dims, mask), connectivity).output(out).compute();
		assertArrayEquals(expectedDilation, values(out));
		ops.op("morphology.reconstructByErosion").input(image(dims, seedsHigh),
			image(dims, mask), connectivity).output(out).compute();
		assertArrayEquals(expectedErosion, values(out));
	}

	private void checkBinary(final String name, final long[] dims,
		final int[] input, final Double h, final Shape connectivity,
		final int[] expected)
	{
		final var out = ArrayImgs.bits(dims);
		if (h == null) {
			ops.op(name).input(image(dims, input), connectivity).output(out)
				.compute();
		}
		else {
			ops.op(name).input(image(dims, input), h, connectivity).output(out)
				.compute();
		}
		assertArrayEquals(expected, values(out), name);
	}

	private void checkImpose(final long[] dims, final int[] input,
		final int[] markers, final Shape connectivity, final int[] expectedMinima)
	{
		final var marks = ArrayImgs.bits(dims);
		var i = 0;
		for (final var b : Views.flatIterable(marks))
			b.set(markers[i++] != 0);
		final var out = image(dims, new int[input.length]);
		ops.op("morphology.imposeMinima").input(image(dims, input), marks,
			connectivity).output(out).compute();
		assertArrayEquals(expectedMinima, values(out));

		// Note: the imposeMaxima result of MorphoLibJ 1.6.4 on these images has
		// regional maxima outside of the markers, so imposeMaxima is checked
		// against its duality with imposeMinima instead.
		final var inverted = new int[input.length];
		for (var j = 0; j < input.length; j++)
			inverted[j] = 255 - input[j];
		ops.op("morphology.imposeMinima").input(image(dims, inverted), marks,
			connectivity).output(out).compute();
		final var expectedMaxima = values(out);
		for (var j = 0; j < input.length; j++)
			expectedMaxima[j] = 255 - expectedMaxima[j];
		ops.op("morphology.imposeMaxima").input(image(dims, input), marks,
			connectivity).output(out).compute();
		assertArrayEquals(expectedMaxima, values(out));
	}

	private void checkGray(final String name, final long[] dims,
		final int[] input, final Shape connectivity, final int[] expected)
	{
		final var out = image(dims, new int[input.length]);
		ops.op(name).input(image(dims, input), connectivity).output(out).compute();
		assertArrayEquals(expected, values(out), name);
	}

	private static Img<UnsignedByteType> image(final long[] dims,
		final int[] values)
	{
		final var bytes = new byte[values.length];
		for (var i = 0; i < values.length; i++)
			bytes[i] = (byte) values[i];
		return ArrayImgs.unsignedBytes(bytes, dims);
	}

	private static int[] values(final RandomAccessibleInterval<?> image) {
		final var result = new int[(int) Views.iterable(image).size()];
		var i = 0;
		for (final var t : Views.flatIterable(image)) {
			if (t instanceof BooleanType) result[i++] = ((BooleanType<?>) t).get()
				? 255 : 0;
			else result[i++] = (int) ((RealType<?>) t).getRealDouble();
		}
		return result;
	}

	// -- Expected values (generated by MorphoLibJ 1.6.4) --

	private static final int[] F2 = {
		4, 3, 2, 1, 0, 0, 3, 4, 8, 3, 9, 7, 9, 0, 5, 8, 3, 7, 6, 5,
		0, 6, 1, 7, 6, 1, 7, 6, 7, 3, 4, 1, 7, 2, 4, 3, 2, 7, 4, 8,
		7, 5, 6, 0, 3, 4, 6, 9, 5, 0, 2, 2, 4, 8, 9, 5, 4, 8, 6, 4,
		3, 9, 1,
	};
	private static final int[] SEEDS2 = {
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0,
	};
	private static final int[] SEEDS_HIGH2 = {
		9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 8, 9, 9, 9, 9,
		9, 9, 9, 9, 9, 9, 9, 9, 9, 3, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
		9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
		9, 9, 9,
	};
	private static final int[] MARKERS2 = {
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0,
	};
	private static final int[] REC_DIL2_C4 = {
		3, 3, 2, 1, 0, 0, 3, 3, 3, 3, 5, 5, 5, 0, 5, 8, 3, 3, 5, 5,
		0, 5, 1, 6, 6, 1, 3, 5, 5, 3, 4, 1, 6, 2, 3, 3, 2, 5, 4, 5,
		5, 5, 5, 0, 3, 4, 5, 5, 5, 0, 2, 2, 3, 3, 5, 5, 4, 5, 5, 4,
		3, 3, 1,
	};
	private static final int[] REC_ERO2_C4 = {
		4, 4, 4, 4, 4, 4, 4, 4, 8, 4, 9, 7, 9, 4, 5, 8, 4, 7, 6, 5,
		3, 6, 4, 7, 6, 4, 7, 6, 7, 3, 4, 4, 7, 4, 4, 4, 6, 7, 4, 8,
		7, 5, 6, 4, 4, 6, 6, 9, 5, 4, 4, 4, 4, 8, 9, 6, 6, 8, 6, 4,
		4, 9, 8,
	};
	private static final int[] RMAX2_C4 = {
		255, 0, 0, 0, 0, 0, 0, 0, 255, 0, 255, 0, 255, 0, 0, 255, 0, 0, 0, 0,
		0, 0, 0, 255, 0, 0, 0, 0, 255, 0, 0, 0, 255, 0, 255, 0, 0, 255, 0, 255,
		0, 0, 255, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 255, 255, 0, 0, 255, 0, 0,
		0, 255, 0,
	};
	private static final int[] RMIN2_C4 = {
		0, 0, 0, 0, 255, 255, 0, 0, 0, 255, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0,
		255, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 255, 0, 0, 0,
		0, 0, 0, 255, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0,
		0, 0, 255,
	};
	private static final int[] EMAX2_C4 = {
		0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 255, 255, 255, 0, 0, 255, 0, 255, 0, 0,
		0, 0, 0, 255, 255, 0, 255, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 255,
		255, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 255, 255, 0, 0, 255, 255, 0,
		0, 255, 0,
	};
	private static final int[] EMIN2_C4 = {
		0, 0, 255, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 255, 0, 0, 0,
		255, 0, 255, 0, 0, 255, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 255, 0, 0, 0,
		0, 0, 0, 255, 0, 255, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 255,
	};
	private static final int[] IMIN2_C4 = {
		5, 4, 4, 4, 4, 4, 4, 5, 9, 5, 10, 8, 10, 4, 6, 0, 4, 8, 7, 6,
		1, 7, 4, 8, 7, 4, 8, 7, 8, 0, 5, 4, 8, 5, 5, 5, 7, 8, 5, 9,
		8, 6, 7, 5, 5, 7, 7, 10, 6, 5, 5, 5, 5, 9, 10, 7, 7, 9, 7, 5,
		5, 10, 9,
	};
	private static final int[] REC_DIL2_C8 = {
		4, 3, 2, 1, 0, 0, 3, 4, 4, 3, 6, 6, 6, 0, 5, 8, 3, 4, 6, 5,
		0, 6, 1, 7, 6, 1, 4, 6, 7, 3, 4, 1, 7, 2, 4, 3, 2, 7, 4, 7,
		7, 5, 6, 0, 3, 4, 6, 7, 5, 0, 2, 2, 4, 4, 6, 5, 4, 7, 6, 4,
		3, 4, 1,
	};
	private static final int[] REC_ERO2_C8 = {
		4, 4, 4, 4, 4, 4, 4, 4, 8, 4, 9, 7, 9, 4, 5, 8, 4, 7, 6, 5,
		3, 6, 4, 7, 6, 4, 7, 6, 7, 3, 4, 4, 7, 4, 4, 4, 5, 7, 4, 8,
		7, 5, 6, 4, 4, 5, 6, 9, 5, 4, 4, 4, 4, 8, 9, 5, 5, 8, 6, 4,
		4, 9, 4,
	};
	private static final int[] RMAX2_C8 = {
		0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 255, 0, 255, 0, 0, 255, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0,
		0, 255, 0,
	};
	private static final int[] RMIN2_C8 = {
		0, 0, 0, 0, 255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0,
		255, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0,
		0, 0, 0, 255, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0,
		0, 0, 255,
	};
	private static final int[] EMAX2_C8 = {
		0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 255, 255, 255, 0, 0, 255, 0, 255, 0, 0,
		0, 0, 0, 255, 0, 0, 255, 0, 255, 0, 0, 0, 255, 0, 0, 0, 0, 255, 0, 255,
		255, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 255, 255, 0, 0, 255, 0, 0,
		0, 255, 0,
	};
	private static final int[] EMIN2_C8 = {
		0, 0, 255, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0,
		255, 0, 255, 0, 0, 255, 0, 0, 0, 0, 0, 255, 0, 255, 0, 0, 255, 0, 0, 0,
		0, 0, 0, 255, 0, 255, 0, 0, 0, 255, 255, 255, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 255,
	};
	private static final int[] IMIN2_C8 = {
		5, 4, 3, 2, 1, 1, 4, 5, 9, 4, 10, 8, 10, 1, 6, 0, 4, 8, 7, 6,
		1, 7, 2, 8, 7, 2, 8, 7, 8, 0, 5, 2, 8, 3, 5, 4, 6, 8, 5, 9,
		8, 6, 7, 3, 4, 6, 7, 10, 6, 3, 3, 3, 5, 9, 10, 6, 6, 9, 7, 5,
		4, 10, 5,
	};
	private static final int[] KILL2 = {
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 2, 4, 0, 0, 3, 0, 0, 0, 0,
		0, 1, 0, 2, 1, 0, 0, 0, 1, 0, 0, 0, 2, 0, 1, 0, 0, 1, 0, 3,
		2, 0, 1, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0,
	};
	private static final int[] FILL2 = {
		4, 3, 2, 1, 0, 0, 3, 4, 8, 3, 9, 7, 9, 0, 5, 8, 4, 7, 6, 5,
		4, 6, 1, 7, 6, 4, 7, 6, 7, 4, 4, 1, 7, 4, 4, 3, 2, 7, 4, 8,
		7, 5, 6, 3, 3, 4, 6, 9, 5, 3, 3, 3, 4, 8, 9, 5, 4, 8, 6, 4,
		3, 9, 1,
	};
	private static final int[] F3 = {
		9, 0, 4, 2, 7, 8, 2, 3, 4, 4, 2, 9, 7, 1, 0, 0, 0, 6, 0, 5,
		9, 8, 2, 1, 0, 2, 0, 6, 4, 7, 4, 5, 6, 2, 0, 3, 3, 4, 9, 2,
		0, 8, 1, 4, 8, 8, 8, 6, 4, 7, 8, 5, 8, 8, 0, 9, 9, 4, 9, 2,
	};
	private static final int[] SEEDS3 = {
		9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	};
	private static final int[] SEEDS_HIGH3 = {
		9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
		9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 6, 9, 9, 9, 9, 9, 9, 9,
		9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
	};
	private static final int[] MARKERS3 = {
		255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	};
	private static final int[] REC_DIL3_C6 = {
		9, 0, 3, 2, 4, 8, 2, 3, 4, 4, 2, 6, 6, 1, 0, 0, 0, 6, 0, 2,
		9, 8, 2, 1, 0, 2, 0, 6, 4, 4, 4, 5, 6, 2, 0, 3, 3, 4, 6, 2,
		0, 8, 1, 4, 4, 8, 8, 6, 4, 4, 8, 5, 6, 6, 0, 8, 8, 4, 6, 2,
	};
	private static final int[] REC_ERO3_C6 = {
		9, 6, 6, 6, 7, 8, 6, 6, 6, 6, 6, 9, 7, 6, 6, 6, 6, 6, 6, 6,
		9, 8, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 9, 6,
		8, 8, 6, 6, 8, 8, 8, 6, 6, 7, 8, 6, 8, 8, 6, 9, 9, 6, 9, 6,
	};
	private static final int[] RMAX3_C6 = {
		255, 0, 255, 0, 255, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 255,
		255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0,
		0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 255, 0, 255, 0,
	};
	private static final int[] RMIN3_C6 = {
		0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 255, 0,
		0, 0, 0, 0, 255, 0, 255, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0,
		255, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0,
	};
	private static final int[] EMAX3_C6 = {
		255, 0, 0, 0, 255, 255, 0, 0, 0, 0, 0, 255, 255, 0, 0, 0, 0, 0, 0, 255,
		255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0,
		0, 255, 0, 0, 255, 255, 255, 0, 0, 255, 255, 0, 255, 255, 0, 255, 255, 0, 255, 0,
	};
	private static final int[] EMIN3_C6 = {
		0, 255, 0, 255, 0, 0, 255, 0, 0, 0, 255, 0, 0, 255, 255, 255, 255, 0, 255, 0,
		0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 0, 255, 255, 0, 0, 0, 0, 255,
		255, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 255,
	};
	private static final int[] IMIN3_C6 = {
		0, 1, 5, 5, 8, 9, 3, 4, 5, 5, 5, 10, 8, 3, 3, 5, 5, 7, 3, 6,
		10, 9, 5, 5, 5, 3, 3, 7, 5, 8, 5, 6, 0, 3, 3, 5, 5, 5, 10, 3,
		9, 9, 5, 5, 9, 9, 9, 7, 5, 8, 9, 6, 9, 9, 3, 10, 10, 5, 10, 3,
	};
	private static final int[] REC_DIL3_C26 = {
		9, 0, 4, 2, 7, 8, 2, 3, 4, 4, 2, 8, 7, 1, 0, 0, 0, 6, 0, 5,
		9, 8, 2, 1, 0, 2, 0, 6, 4, 7, 4, 5, 6, 2, 0, 3, 3, 4, 8, 2,
		0, 8, 1, 4, 7, 8, 8, 6, 4, 7, 8, 5, 8, 8, 0, 8, 8, 4, 8, 2,
	};
	private static final int[] REC_ERO3_C26 = {
		9, 6, 6, 6, 7, 8, 6, 6, 6, 6, 6, 9, 7, 6, 6, 6, 6, 6, 6, 6,
		9, 8, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 9, 6,
		6, 8, 6, 6, 8, 8, 8, 6, 6, 7, 8, 6, 8, 8, 6, 9, 9, 6, 9, 6,
	};
	private static final int[] RMAX3_C26 = {
		255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0,
		255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0,
		0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 255, 0, 255, 0,
	};
	private static final int[] RMIN3_C26 = {
		0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 255, 0,
		0, 0, 0, 0, 255, 0, 255, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0,
		255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0,
	};
	private static final int[] EMAX3_C26 = {
		255, 0, 0, 0, 255, 255, 0, 0, 0, 0, 0, 255, 255, 0, 0, 0, 0, 0, 0, 0,
		255, 255, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0,
		0, 255, 0, 0, 255, 255, 255, 0, 0, 255, 255, 0, 255, 255, 0, 255, 255, 0, 255, 0,
	};
	private static final int[] EMIN3_C26 = {
		0, 255, 0, 255, 0, 0, 255, 0, 0, 0, 255, 0, 0, 255, 255, 255, 255, 0, 255, 0,
		0, 0, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 0, 255, 255, 0, 0, 0, 0, 255,
		255, 0, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0, 255,
	};
	private static final int[] IMIN3_C26 = {
		0, 1, 5, 3, 8, 9, 3, 4, 5, 5, 3, 10, 8, 2, 1, 1, 1, 7, 1, 6,
		10, 9, 3, 2, 2, 3, 1, 7, 5, 8, 5, 6, 0, 3, 1, 4, 4, 5, 10, 3,
		1, 9, 2, 5, 9, 9, 9, 7, 5, 8, 9, 6, 9, 9, 1, 10, 10, 5, 10, 3,
	};
	private static final int[] KILL3 = {
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	};
	private static final int[] FILL3 = {
		9, 0, 4, 2, 7, 8, 2, 3, 4, 4, 2, 9, 7, 1, 0, 0, 0, 6, 0, 5,
		9, 8, 2, 1, 0, 2, 2, 6, 4, 7, 4, 5, 6, 2, 0, 3, 3, 4, 9, 2,
		0, 8, 1, 4, 8, 8, 8, 6, 4, 7, 8, 5, 8, 8, 0, 9, 9, 4, 9, 2,
	};
}
