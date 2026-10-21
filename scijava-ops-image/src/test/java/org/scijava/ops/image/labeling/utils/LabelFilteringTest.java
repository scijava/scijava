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

package org.scijava.ops.image.labeling.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link LabelFiltering}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class LabelFilteringTest extends AbstractOpTest {

	/**
	 * Creates a 10x10 labeling: label 1 is a 2x2 box at (0,0) touching the
	 * minimum borders, label 2 a 3x3 box at (4,4), label 3 a single pixel at
	 * (9,5) touching the maximum x border, and label 4 a 2x1 box at (5,9)
	 * touching the maximum y border.
	 */
	static ImgLabeling<Integer, IntType> boxes() {
		final var labeling = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(10,
			10));
		paint(labeling, 1, 0, 0, 1, 1);
		paint(labeling, 2, 4, 4, 6, 6);
		paint(labeling, 3, 9, 5, 9, 5);
		paint(labeling, 4, 5, 9, 6, 9);
		return labeling;
	}

	static <L> void paint(final ImgLabeling<L, ?> labeling, final L label,
		final long x0, final long y0, final long x1, final long y1)
	{
		final var ra = labeling.randomAccess();
		for (var y = y0; y <= y1; y++)
			for (var x = x0; x <= x1; x++)
				ra.setPositionAndGet(x, y).add(label);
	}

	static <L> Map<L, Long> sizes(final ImgLabeling<L, ?> labeling) {
		final var sizes = new HashMap<L, Long>();
		for (final var region : new LabelRegions<>(labeling)) {
			final var size = region.inside().size();
			if (size > 0) sizes.put(region.getLabel(), size);
		}
		return sizes;
	}

	static <L> Set<L> labels(final ImgLabeling<L, ?> labeling) {
		return sizes(labeling).keySet();
	}

	@SafeVarargs
	static <L> Set<L> set(final L... labels) {
		return new HashSet<>(Arrays.asList(labels));
	}

	private static final Nil<ImgLabeling<Integer, IntType>> LABELING =
		new Nil<>()
		{};

	private ImgLabeling<Integer, IntType> apply(final String name,
		final Object in1)
	{
		return ops.op(name).input(in1).outType(LABELING).apply();
	}

	private ImgLabeling<Integer, IntType> apply(final String name,
		final Object in1, final Object in2)
	{
		return ops.op(name).input(in1, in2).outType(LABELING).apply();
	}

	private ImgLabeling<Integer, IntType> apply(final String name,
		final Object in1, final Object in2, final Object in3)
	{
		return ops.op(name).input(in1, in2, in3).outType(LABELING).apply();
	}

	private ImgLabeling<Integer, IntType> apply(final String name,
		final Object in1, final Object in2, final Object in3, final Object in4)
	{
		return ops.op(name).input(in1, in2, in3, in4).outType(LABELING).apply();
	}

	@Test
	public void testFilterBySize() {
		final var labeling = boxes();
		assertEquals(set(1, 2), labels(apply("labeling.filterBySize", labeling,
			3L)));
		assertEquals(set(1, 3, 4), labels(apply("labeling.filterBySize", labeling,
			0L, 4L)));
		assertEquals(set(1, 4), labels(apply("labeling.filterBySize", labeling, 2L,
			4L)));
		final var all = apply("labeling.filterBySize", labeling);
		assertEquals(sizes(labeling), sizes(all));
		// The input is not modified.
		assertEquals(set(1, 2, 3, 4), labels(labeling));
	}

	@Test
	public void testKeepRemoveLargest() {
		final var labeling = boxes();
		assertEquals(set(2), labels(apply("labeling.keepLargest", labeling)));
		assertEquals(set(1, 3, 4), labels(apply("labeling.removeLargest",
			labeling)));
		// Ties are all kept or removed.
		paint(labeling, 5, 0, 7, 2, 9);
		assertEquals(set(2, 5), labels(apply("labeling.keepLargest", labeling)));
		assertEquals(set(1, 3, 4), labels(apply("labeling.removeLargest",
			labeling)));
	}

	@Test
	public void testKeepRemoveLabels() {
		final var labeling = boxes();
		// Label 5 overlaps label 2 in one pixel.
		paint(labeling, 5, 6, 6, 7, 7);
		final var kept = apply("labeling.keepLabels", labeling, List.of(2, 3, 42));
		assertEquals(Map.of(2, 9L, 3, 1L), sizes(kept));
		final var removed = apply("labeling.removeLabels", labeling, List.of(2));
		assertEquals(Map.of(1, 4L, 3, 1L, 4, 2L, 5, 4L), sizes(removed));
		assertEquals(Set.of(5), removed.randomAccess().setPositionAndGet(6, 6));
	}

	@Test
	public void testFilter() {
		final var labeling = boxes();
		final var even = new Predicate<Integer>() {

			@Override
			public boolean test(final Integer label) {
				return label % 2 == 0;
			}
		};
		assertEquals(set(2, 4), labels(apply("labeling.filter", labeling, even)));

		final Map<Integer, Double> values = Map.of(1, 0.5, 2, 1.5, 3, 2.5);
		assertEquals(set(2, 3), labels(apply("labeling.filter", labeling, values,
			1.0)));
		assertEquals(set(1, 2), labels(apply("labeling.filter", labeling, values,
			Double.NEGATIVE_INFINITY, 2.0)));
		// Label 4 has no value, so it is removed.
		assertEquals(set(1, 2, 3), labels(apply("labeling.filter", labeling,
			values)));
	}

	@Test
	public void testRemoveBorderLabels() {
		final var labeling = boxes();
		assertEquals(set(2), labels(apply("labeling.removeBorderLabels",
			labeling)));
		// Per side: min x, max x, min y, max y.
		assertEquals(set(2, 3, 4), labels(apply("labeling.removeBorderLabels",
			labeling, new boolean[] { true, false, false, false })));
		assertEquals(set(1, 2, 3), labels(apply("labeling.removeBorderLabels",
			labeling, new boolean[] { false, false, false, true })));
		// Per dimension: both x borders.
		assertEquals(set(2, 4), labels(apply("labeling.removeBorderLabels",
			labeling, new boolean[] { true, false })));
	}

	@Test
	public void testRemoveBorderLabels3D() {
		final var labeling = new ImgLabeling<String, UnsignedByteType>(ArrayImgs
			.unsignedBytes(5, 5, 5));
		final var ra = labeling.randomAccess();
		ra.setPositionAndGet(2, 2, 2).add("inner");
		ra.setPositionAndGet(2, 2, 4).add("top");
		ra.setPositionAndGet(2, 2, 3).add("top");
		ra.get().add("inner");
		final ImgLabeling<String, UnsignedByteType> result = ops.op(
			"labeling.removeBorderLabels").input(labeling).outType(
				new Nil<ImgLabeling<String, UnsignedByteType>>()
				{}).apply();
		assertEquals(Map.of("inner", 2L), sizes(result));
		final ImgLabeling<String, UnsignedByteType> xyOnly = ops.op(
			"labeling.removeBorderLabels").input(labeling, new boolean[] { true,
				true, false }).outType(new Nil<ImgLabeling<String, UnsignedByteType>>()
				{}).apply();
		assertEquals(Map.of("inner", 2L, "top", 2L), sizes(xyOnly));
	}

	@Test
	public void testSelectByOverlap() {
		final var labeling = boxes();
		final var mask = ArrayImgs.bits(10, 10);
		final var m = mask.randomAccess();
		// One pixel of label 1 (1/4), three of label 2 (1/3), all of label 3.
		m.setPositionAndGet(1, 1).set(true);
		for (var x = 4; x <= 6; x++)
			m.setPositionAndGet(x, 4).set(true);
		m.setPositionAndGet(9, 5).set(true);
		m.setPositionAndGet(8, 8).set(true);
		assertEquals(set(1, 2, 3), labels(apply("labeling.selectByOverlap",
			labeling, mask)));
		assertEquals(set(2, 3), labels(apply("labeling.selectByOverlap", labeling,
			mask, 0.3)));
		assertEquals(set(3), labels(apply("labeling.selectByOverlap", labeling,
			mask, 1.0)));
	}
}
