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

package org.scijava.ops.image.gpl.biovoxxel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.gpl.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link ContainedCount}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class ContainedCountTest extends AbstractOpTest {

	/** P = primary, S = secondary, B = both. */
	private static final String[] ART = { //
		"..............................", //
		"..............................", //
		"..PPPPPPPP...PP.....PP...SSSS.", //
		"..PPPPBBPP...PP.....PP...SSSS.", //
		"..PPBPPPPBS..PP.....PP...SSSS.", //
		"..PPPPPBPP..SBBSSSSSBBS.......", //
		"..PPBBPPPP...PP.....PP........", //
		"..PPPPPPPP...PP.....PP........", //
		".............PPPPPPPPP........", //
		".............PPPPPPPPP....P...", //
		"........................PPPPP.", //
		".................S......BBPPP.", //
		".................S.....PPPBPPP", //
		"..PPPPPPPPPPPPPPPPPPP...PPPPP.", //
		"..PPBPPPBBBPPPBBBPPPP...PPPPP.", //
		"..PPPBPPBBBPPPPPPPPPP.....P...", //
		"..PPPPPPPPPPPPPPPPPPP.........", //
		"..............................", //
		"..............................", //
		".............................." };

	/** A pixel of each primary object: rectangle, U shape, disk and bar. */
	private static final int[][] STARTS = { { 2, 2 }, { 13, 2 }, { 26, 9 }, {
		2, 13 } };

	private static Img<BitType> mask(final String chars) {
		final Img<BitType> img = ArrayImgs.bits(ART[0].length(), ART.length);
		final RandomAccess<BitType> ra = img.randomAccess();
		for (int y = 0; y < ART.length; y++)
			for (int x = 0; x < ART[y].length(); x++)
				ra.setPositionAndGet(x, y).set(chars.indexOf(ART[y].charAt(x)) >= 0);
		return img;
	}

	private ImgLabeling<Integer, IntType> primary() {
		return ops.op("labeling.cca").input(mask("PB"),
			StructuringElement.EIGHT_CONNECTED).outType(
				new Nil<ImgLabeling<Integer, IntType>>()
				{}).apply();
	}

	private static Integer labelAt(final ImgLabeling<Integer, IntType> labeling,
		final int[] pos)
	{
		return labeling.randomAccess().setPositionAndGet(pos).iterator().next();
	}

	private Map<Integer, Integer> count(
		final ImgLabeling<Integer, IntType> labeling, final Img<BitType> secondary,
		final Long minSize, final Long maxSize)
	{
		return ops.op("labeling.countContained").input(labeling, secondary,
			minSize, maxSize).outType(new Nil<Map<Integer, Integer>>() {}).apply();
	}

	/**
	 * Compares with the BioVoxxel Speckle_Inspector plugin (compiled from
	 * source), run in macro and batch mode on the same images by the harness
	 * in {@code .claude/fiji-plugin-ops/g2/src/SpeckleHarness.java}, without
	 * edge exclusion; the counts were read from the objects filled in magenta
	 * when the lower and upper secondary counts are equal. The plugin printed
	 * "0 Infinity 5 2 1 3" and "2 4 2 2 1 2" (minimum and maximum secondary
	 * size, then the counts of the four objects).
	 */
	@Test
	public void testRegression() {
		final ImgLabeling<Integer, IntType> labeling = primary();
		final Img<BitType> secondary = mask("SB");
		final int[][] expected = { { 5, 2, 1, 3 }, { 2, 2, 1, 2 } };
		final Long[][] sizes = { { null, null }, { 2L, 4L } };
		for (int s = 0; s < sizes.length; s++) {
			final Map<Integer, Integer> counts = count(labeling, secondary,
				sizes[s][0], sizes[s][1]);
			assertEquals(4, counts.size());
			for (int c = 0; c < STARTS.length; c++) {
				assertEquals(expected[s][c], counts.get(labelAt(labeling, STARTS[c])),
					"object " + c + ", sizes " + s);
			}
		}
	}

	@Test
	public void testFilterByContainedCount() {
		final ImgLabeling<Integer, IntType> labeling = primary();
		final ImgLabeling<Integer, IntType> filtered = ops.op(
			"labeling.filterByContainedCount").input(labeling, mask("SB"), 2, 3,
				null, null).outType(new Nil<ImgLabeling<Integer, IntType>>()
				{}).apply();
		final Set<Integer> kept = new HashSet<>();
		for (final LabelingType<Integer> t : filtered)
			kept.addAll(t);
		// The U shape (2) and the bar (3) are kept; the rectangle (5) and the
		// disk (1) are not.
		assertEquals(Set.of(labelAt(labeling, STARTS[1]), labelAt(labeling,
			STARTS[3])), kept);
	}

	/** In 3D, diagonal neighbors are connected and clipped parts counted. */
	@Test
	public void test3D() {
		final Img<IntType> index = ArrayImgs.ints(8, 8, 8);
		final ImgLabeling<String, IntType> labeling = new ImgLabeling<>(index);
		final RandomAccess<LabelingType<String>> la = labeling.randomAccess();
		final Img<BitType> secondary = ArrayImgs.bits(8, 8, 8);
		final RandomAccess<BitType> sa = secondary.randomAccess();
		for (int z = 1; z <= 5; z++)
			for (int y = 1; y <= 5; y++)
				for (int x = 1; x <= 5; x++)
					la.setPositionAndGet(x, y, z).add("box");
		// Two voxels touching only by a corner: one object.
		sa.setPositionAndGet(2, 2, 2).set(true);
		sa.setPositionAndGet(3, 3, 3).set(true);
		// A single voxel.
		sa.setPositionAndGet(5, 1, 5).set(true);
		// A line crossing the box border: clipped to its inside part.
		for (int x = 4; x <= 7; x++)
			sa.setPositionAndGet(x, 5, 1).set(true);
		// Outside the box.
		sa.setPositionAndGet(7, 7, 7).set(true);
		final Map<String, Integer> counts = ops.op("labeling.countContained")
			.input(labeling, secondary, null, null).outType(
				new Nil<Map<String, Integer>>()
				{}).apply();
		assertEquals(Map.of("box", 3), counts);
		// Only the clipped line (2 voxels) and the pair are at least 2 voxels.
		final Map<String, Integer> big = ops.op("labeling.countContained").input(
			labeling, secondary, 2L, null).outType(new Nil<Map<String, Integer>>()
			{}).apply();
		assertEquals(Map.of("box", 2), big);
	}

	@Test
	public void testIntervalMismatch() {
		final ImgLabeling<Integer, IntType> labeling = primary();
		assertThrows(IllegalArgumentException.class, () -> ContainedCount
			.countContained(labeling, ArrayImgs.bits(5, 5), null, null));
	}
}
