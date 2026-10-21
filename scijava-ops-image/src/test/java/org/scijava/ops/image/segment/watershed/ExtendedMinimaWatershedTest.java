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

package org.scijava.ops.image.segment.watershed;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link ExtendedMinimaWatershed}.
 *
 * @author Curtis Rueden
 */
public class ExtendedMinimaWatershedTest extends AbstractOpTest {

	private static final Nil<ImgLabeling<Integer, IntType>> LABELING =
		new Nil<>()
		{};

	/** Two cones whose basins meet at a saddle of depth 5 below the rims. */
	private static Img<DoubleType> twoBasins() {
		final var img = ArrayImgs.doubles(21, 11);
		final var ra = img.randomAccess();
		for (var y = 0; y < 11; y++) {
			for (var x = 0; x < 21; x++) {
				final var d1 = Math.hypot(x - 5, y - 5);
				final var d2 = Math.hypot(x - 15, y - 5);
				ra.setPositionAndGet(x, y).set(Math.min(d1, d2));
			}
		}
		return img;
	}

	@Test
	public void testDynamicControlsSplitting() {
		final var img = twoBasins();
		// The saddle between the cones is at height 5 (x = 10, y = 5).
		final var split = ops.op("segment.watershedExtendedMinima").input(img, 1.0)
			.outType(LABELING).apply();
		assertEquals(2, labels(split).size());
		final var merged = ops.op("segment.watershedExtendedMinima").input(img,
			6.0).outType(LABELING).apply();
		assertEquals(1, labels(merged).size());
	}

	@Test
	public void testWatershedLines() {
		final var img = twoBasins();
		final var lines = ops.op("segment.watershedExtendedMinima").input(img, 1.0,
			new DiamondShape(1), true).outType(LABELING).apply();
		final var noLines = ops.op("segment.watershedExtendedMinima").input(img,
			1.0, new DiamondShape(1), false).outType(LABELING).apply();
		final var ra = lines.randomAccess();
		final var raNo = noLines.randomAccess();
		var unlabeled = 0;
		for (var y = 0; y < 11; y++) {
			for (var x = 0; x < 21; x++) {
				assertEquals(1, raNo.setPositionAndGet(x, y).size());
				final var l = ra.setPositionAndGet(x, y);
				if (l.isEmpty()) {
					unlabeled++;
					assertEquals(10, x);
				}
				else {
					assertEquals(x < 10 ? 1 : 2, l.iterator().next());
					assertEquals(l, raNo.get());
				}
			}
		}
		assertEquals(11, unlabeled);
	}

	@Test
	public void testMask() {
		final var img = twoBasins();
		final var mask = ArrayImgs.bits(21, 11);
		for (final var c = Views.interval(mask, new long[] { 0, 0 }, new long[] {
			12, 10 }).cursor(); c.hasNext();)
			c.next().set(true);
		final var out = ops.op("segment.watershedExtendedMinima").input(img, 3.0,
			null, false, mask).outType(LABELING).apply();
		final var ra = out.randomAccess();
		for (var y = 0; y < 11; y++) {
			for (var x = 0; x < 21; x++) {
				final var l = ra.setPositionAndGet(x, y);
				if (x > 12) assertTrue(l.isEmpty());
				else assertEquals(Set.of(1), l);
			}
		}
	}

	/**
	 * Compares with MorphoLibJ on a noisy 8-bit relief with three basins.
	 * <p>
	 * The expected arrays were produced by running MorphoLibJ 1.6.4 as a black
	 * box ({@code MinimaAndMaxima.extendedMinima}, then
	 * {@code BinaryImages.componentsLabeling}, {@code
	 * MinimaAndMaxima.imposeMinima} and {@code Watershed.computeWatershed}) on
	 * the image {@code F}; 0 stands for no label. With watershed lines the
	 * results are identical; without them, MorphoLibJ breaks ties between pixels
	 * of equal priority differently (see {@link #assertFewDifferences}).
	 * </p>
	 */
	@Test
	public void testMorphoLibJ() {
		final var img = ArrayImgs.unsignedBytes(20, 16);
		var i = 0;
		for (final var t : img)
			t.set(F[i++]);
		check(img, 2.0, new DiamondShape(1), true, EMW_H2_C4_LINES);
		check(img, 2.0, new DiamondShape(1), false, EMW_H2_C4);
		check(img, 2.0, new RectangleShape(1, false), true, EMW_H2_C8_LINES);
		check(img, 2.0, new RectangleShape(1, false), false, EMW_H2_C8);
		check(img, 4.0, new DiamondShape(1), true, EMW_H4_C4_LINES);
		check(img, 4.0, new DiamondShape(1), false, EMW_H4_C4);
		check(img, 4.0, new RectangleShape(1, false), true, EMW_H4_C8_LINES);
		check(img, 4.0, new RectangleShape(1, false), false, EMW_H4_C8);
	}

	private void check(final Img<UnsignedByteType> img, final double h,
		final Shape shape, final boolean lines, final int[] expected)
	{
		final var out = ops.op("segment.watershedExtendedMinima").input(img, h,
			shape, lines).outType(LABELING).apply();
		final var actual = toArray(out);
		if (lines) assertArrayEquals(expected, actual);
		else assertFewDifferences(expected, actual);
	}

	/**
	 * Asserts that two label arrays have the same labels and differ on at most
	 * 10% of the pixels.
	 * <p>
	 * This is used for comparisons with MorphoLibJ without watershed lines,
	 * where pixels of equal priority (notably on the plateaus created by minima
	 * imposition) are flooded in a different order, so that whole plateaus may
	 * be assigned to another adjacent region.
	 * </p>
	 */
	static void assertFewDifferences(final int[] expected, final int[] actual) {
		assertEquals(Arrays.stream(expected).boxed().collect(Collectors.toSet()),
			Arrays.stream(actual).boxed().collect(Collectors.toSet()));
		var diff = 0;
		for (var i = 0; i < expected.length; i++)
			if (expected[i] != actual[i]) diff++;
		assertTrue(diff <= expected.length / 10, diff + " differences");
	}

	/**
	 * Asserts that two 2D label arrays differ only on few pixels, each lying
	 * next to a pixel of the expected array having the actual label.
	 */
	static void assertBoundaryDifferences(final int[] expected,
		final int[] actual, final int width)
	{
		final var height = expected.length / width;
		var diff = 0;
		for (var i = 0; i < expected.length; i++) {
			if (expected[i] == actual[i]) continue;
			diff++;
			final var x = i % width;
			final var y = i / width;
			var nearby = false;
			for (var yy = Math.max(0, y - 1); yy <= Math.min(height - 1, y +
				1); yy++)
			{
				for (var xx = Math.max(0, x - 1); xx <= Math.min(width - 1, x +
					1); xx++)
				{
					if (expected[yy * width + xx] == actual[i]) nearby = true;
				}
			}
			assertTrue(nearby, "Pixel (" + x + ", " + y + ") differs");
		}
		assertTrue(diff <= expected.length / 10, diff + " differences");
	}

	static int[] toArray(
		final RandomAccessibleInterval<? extends Set<Integer>> labeling)
	{
		final var result = new int[(int) Views.iterable(labeling).size()];
		var i = 0;
		for (final var l : Views.flatIterable(labeling))
			result[i++] = l.isEmpty() ? 0 : l.iterator().next();
		return result;
	}

	static Set<Integer> labels(final ImgLabeling<Integer, IntType> labeling) {
		final var set = new HashSet<Integer>();
		for (final var l : labeling)
			set.addAll(l);
		return set;
	}

	static final int[] F = {
		12, 11, 10, 9, 9, 8, 9, 11, 13, 14, 15, 14, 13, 12, 11, 11, 11, 12, 13, 15,
		12, 8, 7, 7, 7, 6, 8, 10, 11, 13, 13, 10, 9, 10, 8, 10, 10, 10, 12, 13,
		11, 8, 6, 6, 5, 6, 6, 7, 11, 12, 12, 8, 8, 7, 8, 7, 9, 8, 12, 13,
		10, 6, 6, 5, 3, 3, 6, 6, 9, 10, 9, 8, 7, 4, 6, 5, 8, 7, 10, 12,
		10, 6, 4, 4, 2, 3, 5, 8, 9, 10, 10, 7, 5, 3, 2, 4, 6, 7, 10, 10,
		9, 6, 4, 4, 4, 5, 6, 8, 8, 10, 9, 6, 5, 4, 0, 4, 4, 6, 10, 11,
		9, 8, 8, 6, 5, 4, 7, 7, 9, 11, 10, 8, 6, 3, 2, 3, 4, 7, 10, 12,
		12, 8, 9, 6, 8, 8, 7, 8, 11, 10, 9, 8, 6, 5, 5, 5, 7, 9, 9, 12,
		11, 12, 10, 8, 8, 10, 11, 10, 8, 10, 10, 8, 9, 8, 8, 7, 7, 10, 11, 12,
		14, 12, 12, 11, 12, 10, 8, 7, 8, 7, 7, 9, 8, 9, 9, 8, 9, 11, 12, 15,
		16, 14, 15, 12, 13, 9, 9, 6, 6, 5, 6, 8, 9, 11, 12, 12, 13, 13, 15, 15,
		16, 15, 14, 12, 10, 8, 7, 4, 5, 4, 5, 6, 8, 9, 10, 14, 15, 13, 15, 18,
		20, 17, 15, 13, 11, 9, 7, 4, 4, 1, 2, 4, 7, 10, 11, 13, 16, 17, 17, 17,
		19, 16, 14, 14, 10, 10, 7, 4, 5, 3, 4, 5, 8, 8, 12, 12, 16, 17, 18, 21,
		20, 18, 17, 13, 13, 10, 7, 7, 4, 4, 4, 8, 9, 9, 11, 13, 15, 18, 18, 20,
		21, 18, 15, 13, 14, 12, 9, 8, 6, 8, 6, 9, 9, 11, 14, 13, 16, 17, 20, 23,
	};
	private static final int[] EMW_H2_C4_LINES = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 0, 0, 3, 3, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 0, 3, 3, 3, 3, 3, 0, 0, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 2, 2, 2, 2, 2,
		1, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 2, 2, 2, 2,
		0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 2, 2, 2,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 2, 2,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 2,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0,
	};
	private static final int[] EMW_H2_C4 = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2,
		1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2,
		1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	};
	private static final int[] EMW_H2_C8_LINES = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 0, 0, 3, 3, 3, 3, 3, 0, 0, 0, 2, 2, 2, 2, 2, 2,
		1, 1, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 2, 2, 2, 2,
		1, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 2, 2, 2,
		0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 2, 2,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	};
	private static final int[] EMW_H2_C8 = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2,
		1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2,
		1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2,
		1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2,
	};
	private static final int[] EMW_H4_C4_LINES = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 0, 0, 3, 3, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 0, 3, 3, 3, 3, 3, 0, 0, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 2, 2, 2, 2, 2,
		1, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 2, 2, 2, 2,
		0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 2, 2, 2,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 2, 2,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 2,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0,
	};
	private static final int[] EMW_H4_C4 = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2,
		1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2,
		1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	};
	private static final int[] EMW_H4_C8_LINES = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 0, 0, 3, 3, 3, 3, 3, 0, 0, 0, 2, 2, 2, 2, 2, 2,
		1, 1, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 2, 2, 2, 2,
		1, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 2, 2, 2,
		0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 2, 2,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	};
	private static final int[] EMW_H4_C8 = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2,
		1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2,
		1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2,
		1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2,
	};
}
