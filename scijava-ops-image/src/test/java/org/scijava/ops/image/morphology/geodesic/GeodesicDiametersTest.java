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

package org.scijava.ops.image.morphology.geodesic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.imglib2.Point;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link GeodesicDiameters}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class GeodesicDiametersTest extends AbstractOpTest {

	private static final Nil<Map<Integer, Double>> DIAMETERS = new Nil<>() {};
	private static final Nil<Map<Integer, List<Point>>> PATHS = new Nil<>() {};

	/** An L shape (1), a U shape (2), a disk (3) and a staircase line (4). */
	private static final String[] SHAPES = { //
		"000000000000000000000000", //
		"001100000000000022000220", //
		"001100003330000022000220", //
		"001100033333000022000220", //
		"001100333333300022000220", //
		"001100333333300022000220", //
		"001100333333300022000220", //
		"001100033333000022000220", //
		"001100003330000022222220", //
		"001100000000000022222220", //
		"001100000000000000000000", //
		"001111111111111000000000", //
		"001111111111111000000000", //
		"000000000000000000000000", //
		"000044400000000000000000", //
		"000000444000000000000000", //
		"000000004440000000000000", //
		"000000000044400000000000", //
		"000000000000400000000000", //
		"000000000000000000000000" };

	static ImgLabeling<Integer, IntType> labeling(final String... rows) {
		final var index = ArrayImgs.ints(rows[0].length(), rows.length);
		final var ra = index.randomAccess();
		var max = 0;
		for (var y = 0; y < rows.length; y++) {
			for (var x = 0; x < rows[y].length(); x++) {
				final var v = rows[y].charAt(x) - '0';
				ra.setPositionAndGet(x, y).set(v);
				max = Math.max(max, v);
			}
		}
		return labeling(index, max);
	}

	static ImgLabeling<Integer, IntType> labeling(final Img<IntType> index,
		final int max)
	{
		return ImgLabeling.fromImageAndLabels(index, IntStream.rangeClosed(1, max)
			.boxed().collect(Collectors.toList()));
	}

	private Map<Integer, Double> diameters(
		final ImgLabeling<Integer, IntType> labeling, final double[] spacing,
		final Integer radius)
	{
		return ops.op("labeling.geodesicDiameter").input(labeling, spacing,
			radius).outType(DIAMETERS).apply();
	}

	/**
	 * Compares with MorphoLibJ 1.6.4 ({@code GeodesicDiameter} with the
	 * {@code QUASI_EUCLIDEAN} chamfer mask, whose weights 1 and √2 equal the
	 * radius 1 neighborhood), run on the same image. MorphoLibJ computes in
	 * single precision.
	 */
	@Test
	public void testRegression() {
		final var d = diameters(labeling(SHAPES), null, 1);
		assertEquals(22.656853103999072, d.get(1), 1e-5);
		assertEquals(19.071066284541065, d.get(2), 1e-5);
		assertEquals(8.242640877131397, d.get(3), 1e-5);
		assertEquals(11.071067238215381, d.get(4), 1e-5);
		// MorphoLibJ with pixel size 0.5 gives half of each value.
		final var c = diameters(labeling(SHAPES), new double[] { 0.5, 0.5 }, 1);
		assertEquals(11.328426551999536, c.get(1), 1e-5);
		assertEquals(4.121320438565698, c.get(3), 1e-5);
	}

	@Test
	public void testAnalytic() {
		final var d = diameters(labeling(SHAPES), null, null);
		// L shape: a path from (2, 1) to (14, 12) must pass the inner corner at
		// (3.5, 10.5), which bounds its length from below; knight moves shorten it
		// compared to the radius 1 neighborhood.
		final var lower = Math.hypot(1.5, 9.5) + Math.hypot(10.5, 1.5) + Math.sqrt(
			2);
		assertTrue(d.get(1) > lower && d.get(1) < 22.656853103999072, //
			() -> "L shape: " + d.get(1));
		// U shape: geodesic diameter far exceeds the Euclidean distance (6) of
		// its extremities.
		assertTrue(d.get(2) > 18);
		// Anisotropic spacing stretches a horizontal bar.
		final var bar = labeling("000000000000", "011111111110", "000000000000");
		assertEquals(9 + Math.sqrt(2), diameters(bar, null, null).get(1), 1e-9);
		assertEquals(18 + Math.sqrt(4 + 9), diameters(bar, new double[] { 2, 3 },
			null).get(1), 1e-9);
	}

	@Test
	public void testSinglePixelAndDisconnected() {
		final var d = diameters(labeling("0000000", "0100202", "0000000"), null,
			null);
		assertEquals(Math.sqrt(2), d.get(1), 1e-12);
		assertEquals(Double.POSITIVE_INFINITY, d.get(2));
	}

	/** Compares with MorphoLibJ 1.6.4 {@code GeodesicDiameter3D}. */
	@Test
	public void test3D() {
		final var index = ArrayImgs.ints(12, 5, 4);
		final var ra = index.randomAccess();
		for (var x = 1; x <= 8; x++)
			ra.setPositionAndGet(x, 2, 1).set(1);
		ra.setPositionAndGet(10, 4, 3).set(2);
		final var d = diameters(labeling(index, 2), null, null);
		assertEquals(8.732050807568877, d.get(1), 1e-9);
		assertEquals(1.7320508075688772, d.get(2), 1e-9);
	}

	@Test
	public void testPaths() {
		final var labeling = labeling(SHAPES);
		final Map<Integer, List<Point>> paths = ops.op(
			"labeling.geodesicDiameterPath").input(labeling, null, 1).outType(PATHS)
			.apply();
		final var diameters = diameters(labeling, null, 1);
		for (final var label : paths.keySet()) {
			final var path = paths.get(label);
			var length = Math.sqrt(2);
			final var ra = labeling.randomAccess();
			for (var i = 0; i < path.size(); i++) {
				final var p = path.get(i);
				assertTrue(ra.setPositionAndGet(p).contains(label));
				if (i == 0) continue;
				final var q = path.get(i - 1);
				final var dx = Math.abs(p.getLongPosition(0) - q.getLongPosition(0));
				final var dy = Math.abs(p.getLongPosition(1) - q.getLongPosition(1));
				assertTrue(dx <= 1 && dy <= 1 && dx + dy > 0);
				length += Math.hypot(dx, dy);
			}
			assertEquals(diameters.get(label), length, 1e-9);
		}
		// The extremities of the L shape, as found by MorphoLibJ.
		final var l = paths.get(1);
		final var ends = List.of(l.get(0), l.get(l.size() - 1));
		assertTrue(ends.contains(new Point(14, 12)));
		assertTrue(ends.contains(new Point(2, 1)));
	}

	/**
	 * A square with a thin tail: the central pixel is the center of the
	 * square, so the first extremity (farthest from it) is the tip of the tail,
	 * and the second one is a corner of the square on the opposite side.
	 */
	@Test
	public void testPathOrder() {
		final var labeling = labeling( //
			"000000000000000000000000", //
			"011111110000000000000000", //
			"011111110000000000000000", //
			"011111110000000000000000", //
			"011111111111111111111110", //
			"011111110000000000000000", //
			"011111110000000000000000", //
			"011111110000000000000000", //
			"000000000000000000000000");
		final Map<Integer, List<Point>> paths = ops.op(
			"labeling.geodesicDiameterPath").input(labeling, null, 1).outType(PATHS)
			.apply();
		final var path = paths.get(1);
		assertEquals(new Point(22, 4), path.get(0));
		final var last = path.get(path.size() - 1);
		assertEquals(1, last.getLongPosition(0));
		assertTrue(last.getLongPosition(1) == 1 || last.getLongPosition(1) == 7,
			last.toString());
	}
}
