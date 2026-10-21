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

package org.scijava.ops.image.labeling.overlap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link OverlapMeasures}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class OverlapMeasuresTest extends AbstractOpTest {

	private static final Nil<Double> TOTAL = new Nil<>() {};
	private static final Nil<Map<Integer, Double>> BY_LABEL = new Nil<>() {};

	private static final String[] SOURCE = { //
		"1112200", //
		"1112200", //
		"1100233", //
		"0003333", //
		"4400333" };
	private static final String[] TARGET = { //
		"1111220", //
		"1102220", //
		"1102200", //
		"0033330", //
		"0000333" };

	private static ImgLabeling<Integer, IntType> labeling(final String... rows) {
		final var index = ArrayImgs.ints(rows[0].length(), rows.length);
		final var ra = index.randomAccess();
		for (var y = 0; y < rows.length; y++)
			for (var x = 0; x < rows[y].length(); x++)
				ra.setPositionAndGet(x, y).set(rows[y].charAt(x) - '0');
		return ImgLabeling.fromImageAndLabels(index, IntStream.rangeClosed(1, 9)
			.boxed().collect(Collectors.toList()));
	}

	private double total(final String name, final String[] s,
		final String[] t)
	{
		return ops.op("labeling.overlap." + name).input(labeling(s), labeling(t))
			.outType(TOTAL).apply();
	}

	private Map<Integer, Double> byLabel(final String name) {
		return ops.op("labeling.overlap." + name + "ByLabel").input(labeling(
			SOURCE), labeling(TARGET)).outType(BY_LABEL).apply();
	}

	/**
	 * Compares with the label overlap measures of MorphoLibJ 1.6.4
	 * ({@code LabelImages.getJaccardIndex} etc.), run on the same images.
	 * Label 4 occurs only in the source; for it, MorphoLibJ reports 0 rather
	 * than the undefined (NaN) target overlap and false negative error, and 0
	 * rather than 2 for the volume similarity, so it is checked separately.
	 */
	@Test
	public void testRegression() {
		assertEquals(0.7727272727272727, total("targetOverlap", SOURCE, TARGET),
			1e-12);
		assertEquals(0.5862068965517241, total("jaccard", SOURCE, TARGET), 1e-12);
		assertEquals(0.7391304347826086, total("dice", SOURCE, TARGET), 1e-12);
		assertEquals(0.08695652173913043, total("volumeSimilarity", SOURCE,
			TARGET), 1e-12);
		assertEquals(0.2272727272727273, total("falseNegativeError", SOURCE,
			TARGET), 1e-12);
		assertEquals(0.2916666666666667, total("falsePositiveError", SOURCE,
			TARGET), 1e-12);

		assertByLabel(byLabel("targetOverlap"), 0.875, 0.5714285714285714,
			0.8571428571428571, Double.NaN);
		assertByLabel(byLabel("jaccard"), 0.7777777777777778, 0.5, 0.6, 0);
		assertByLabel(byLabel("dice"), 0.875, 0.6666666666666666, 0.75, 0);
		assertByLabel(byLabel("volumeSimilarity"), 0, -0.3333333333333333, 0.25,
			2);
		assertByLabel(byLabel("falseNegativeError"), 0.125, 0.4285714285714286,
			0.1428571428571429, Double.NaN);
		assertByLabel(byLabel("falsePositiveError"), 0.125, 0.2,
			0.3333333333333333, 1);
	}

	private static void assertByLabel(final Map<Integer, Double> actual,
		final double... expected)
	{
		assertEquals(expected.length, actual.size());
		for (var i = 0; i < expected.length; i++)
			assertEquals(expected[i], actual.get(i + 1), 1e-12);
	}

	@Test
	public void testIdenticalAndDisjoint() {
		final String[] a = { "1100", "1122" };
		final String[] b = { "0011", "2200" };
		assertEquals(1, total("jaccard", a, a));
		assertEquals(1, total("dice", a, a));
		assertEquals(0, total("volumeSimilarity", a, a));
		assertEquals(0, total("falseNegativeError", a, a));
		assertEquals(0, total("falsePositiveError", a, a));
		assertEquals(0, total("jaccard", a, b));
		assertEquals(0, total("dice", a, b));
		assertEquals(0, total("targetOverlap", a, b));
		assertEquals(1, total("falseNegativeError", a, b));
		assertEquals(1, total("falsePositiveError", a, b));
	}
}
