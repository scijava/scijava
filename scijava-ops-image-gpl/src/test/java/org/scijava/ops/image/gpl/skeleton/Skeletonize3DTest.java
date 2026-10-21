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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.gpl.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link Skeletonize3D}.
 * <p>
 * Expected skeletons were produced by running the original Fiji Skeletonize3D
 * plugin (source of version 2.1.2-SNAPSHOT, with ImageJ 1.54p) on the same
 * synthetic images, from a throwaway harness.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class Skeletonize3DTest extends AbstractOpTest {

	/** A cross of two 25x5x5 bars, centered at (15, 15, 5). */
	static Img<BitType> bars() {
		final Img<BitType> img = ArrayImgs.bits(32, 32, 12);
		final Cursor<BitType> c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final int x = c.getIntPosition(0), y = c.getIntPosition(1), z = c
				.getIntPosition(2);
			c.get().set(z >= 3 && z < 8 && ((x >= 3 && x < 28 && y >= 13 &&
				y < 18) || (y >= 3 && y < 28 && x >= 13 && x < 18)));
		}
		return img;
	}

	/** A disk of radius 9 with a rectangular tail. */
	static Img<BitType> blob2D() {
		final Img<BitType> img = ArrayImgs.bits(30, 30);
		final Cursor<BitType> c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final int x = c.getIntPosition(0), y = c.getIntPosition(1);
			final boolean disk = (x - 12) * (x - 12) + (y - 12) * (y - 12) <= 81;
			final boolean tail = x >= 18 && x < 28 && y >= 20 && y < 25;
			c.get().set(disk || tail);
		}
		return img;
	}

	private RandomAccessibleInterval<BitType> skeletonize(
		final RandomAccessibleInterval<BitType> in)
	{
		return ops.op("morphology.skeletonize3D").input(in).outType(
			new Nil<RandomAccessibleInterval<BitType>>()
			{}).apply();
	}

	private static Set<String> foreground(
		final RandomAccessibleInterval<BitType> img)
	{
		final Set<String> set = new TreeSet<>();
		final Cursor<BitType> c = Views.flatIterable(img).localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			if (c.get().get()) set.add(Arrays.toString(c.positionAsLongArray()));
		}
		return set;
	}

	@Test
	public void testBars3D() {
		// The skeleton of the cross is its center line, shortened by two voxels
		// at each end; the original plugin gives the same result.
		final Set<String> expected = new TreeSet<>();
		for (int i = 5; i <= 25; i++) {
			expected.add(Arrays.toString(new long[] { i, 15, 5 }));
			expected.add(Arrays.toString(new long[] { 15, i, 5 }));
		}
		final RandomAccessibleInterval<BitType> out = skeletonize(bars());
		assertArrayEquals(new long[] { 32, 32, 12 }, out.dimensionsAsLongArray());
		assertEquals(expected, foreground(out));
	}

	@Test
	public void testBlob2D() {
		final int[][] expectedPoints = { { 12, 12 }, { 12, 13 }, { 12, 14 }, { 13,
			15 }, { 14, 16 }, { 15, 17 }, { 16, 18 }, { 17, 19 }, { 18, 20 }, { 19,
				21 }, { 20, 22 }, { 21, 22 }, { 22, 22 }, { 23, 22 }, { 24, 22 }, { 25,
					22 }, { 26, 22 } };
		final Set<String> expected = new TreeSet<>();
		for (final int[] p : expectedPoints)
			expected.add(Arrays.toString(new long[] { p[0], p[1] }));
		final RandomAccessibleInterval<BitType> out = skeletonize(blob2D());
		assertEquals(2, out.numDimensions());
		assertEquals(expected, foreground(out));
	}

	@Test
	public void testIdempotentAndTranslated() {
		final RandomAccessibleInterval<BitType> translated = Views.translate(
			blob2D(), 100, -50);
		final RandomAccessibleInterval<BitType> once = skeletonize(translated);
		assertArrayEquals(new long[] { 100, -50 }, once.minAsLongArray());
		final RandomAccessibleInterval<BitType> twice = skeletonize(once);
		assertEquals(17, foreground(once).size());
		assertEquals(foreground(once), foreground(twice));
		assertEquals(foreground(Views.translate(skeletonize(blob2D()), 100, -50)),
			foreground(once));
	}

	@Test
	public void testSolidBallShrinksToPoint() {
		final Img<BitType> img = ArrayImgs.bits(11, 11, 11);
		final Cursor<BitType> c = img.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			double r2 = 0;
			for (int d = 0; d < 3; d++)
				r2 += Math.pow(c.getIntPosition(d) - 5, 2);
			c.get().set(r2 <= 16);
		}
		// Note: The ball thins down to (at most) two voxels next to its center.
		final RandomAccessibleInterval<BitType> out = skeletonize(img);
		final Set<String> skeleton = foreground(out);
		assertTrue(skeleton.size() >= 1 && skeleton.size() <= 2, skeleton
			.toString());
		final Cursor<BitType> sc = Views.flatIterable(out).localizingCursor();
		while (sc.hasNext()) {
			if (!sc.next().get()) continue;
			for (int d = 0; d < 3; d++)
				assertTrue(Math.abs(sc.getIntPosition(d) - 5) <= 1);
		}
	}

	@Test
	public void testRejectsOtherDimensionalities() {
		assertThrows(RuntimeException.class, () -> skeletonize(ArrayImgs.bits(4)));
		assertThrows(RuntimeException.class, () -> skeletonize(ArrayImgs.bits(3, 3,
			3, 3)));
	}
}
