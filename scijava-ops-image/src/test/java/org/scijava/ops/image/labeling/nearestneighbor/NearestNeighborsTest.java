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

package org.scijava.ops.image.labeling.nearestneighbor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link NearestNeighbors}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class NearestNeighborsTest extends AbstractOpTest {

	private static final Nil<Map<Integer, Double>> DISTANCES = new Nil<>() {};
	private static final Nil<Map<String, Double>> STATS = new Nil<>() {};

	private static ImgLabeling<Integer, IntType> labeling(
		final Img<IntType> indexImg, final int maxLabel)
	{
		return ImgLabeling.fromImageAndLabels(indexImg, IntStream.rangeClosed(1,
			maxLabel).boxed().collect(Collectors.toList()));
	}

	/** Creates one single-pixel label per grid node, with the given spacing. */
	private static ImgLabeling<Integer, IntType> grid(final int spacing,
		final int count)
	{
		final var index = ArrayImgs.ints(spacing * count, spacing * count);
		final var ra = index.randomAccess();
		var label = 0;
		for (var y = 0; y < count; y++)
			for (var x = 0; x < count; x++)
				ra.setPositionAndGet(x * spacing + spacing / 2, y * spacing +
					spacing / 2).set(++label);
		return labeling(index, label);
	}

	/** Places single-pixel labels at random positions. */
	private static ImgLabeling<Integer, IntType> randomPoints(
		final Random random, final int size, final int count, final int cluster)
	{
		final var index = ArrayImgs.ints(size, size);
		final var ra = index.randomAccess();
		var label = 0;
		while (label < count) {
			final long x, y;
			if (cluster > 0) {
				// Points cluster in a cluster x cluster square at the center.
				x = (size - cluster) / 2 + random.nextInt(cluster);
				y = (size - cluster) / 2 + random.nextInt(cluster);
			}
			else {
				x = random.nextInt(size);
				y = random.nextInt(size);
			}
			final var t = ra.setPositionAndGet(x, y);
			if (t.get() == 0) t.set(++label);
		}
		return labeling(index, label);
	}

	@Test
	public void testDistances() {
		final var index = ArrayImgs.ints(30, 20);
		final var ra = index.randomAccess();
		// Label 1 is a 3 x 3 square centered at (5, 5).
		for (var x = 4; x <= 6; x++)
			for (var y = 4; y <= 6; y++)
				ra.setPositionAndGet(x, y).set(1);
		ra.setPositionAndGet(8, 9).set(2);
		ra.setPositionAndGet(25, 9).set(3);
		final var distances = ops.op("labeling.nearestNeighborDistances").input(
			labeling(index, 3)).outType(DISTANCES).apply();
		assertEquals(5, distances.get(1), 1e-12);
		assertEquals(5, distances.get(2), 1e-12);
		assertEquals(17, distances.get(3), 1e-12);

		// With a calibration, centroids are compared in calibrated units.
		final var calibrated = ops.op("labeling.nearestNeighborDistances").input(
			labeling(index, 3), new double[] { 2, 0.5 }).outType(DISTANCES).apply();
		assertEquals(Math.sqrt(40), calibrated.get(1), 1e-12);
		assertEquals(Math.sqrt(40), calibrated.get(2), 1e-12);
		assertEquals(34, calibrated.get(3), 1e-12);
	}

	@Test
	public void testRegularGrid() {
		// On a square grid with spacing a, every distance is a and the density
		// is 1 / a^2, so the expected random distance is a / 2 and R = 2.
		final var stats = ops.op("labeling.clarkEvans").input(grid(10, 10))
			.outType(STATS).apply();
		assertEquals(100, stats.get("n"), 0);
		assertEquals(10, stats.get("observedMeanDistance"), 1e-12);
		assertEquals(5, stats.get("expectedMeanDistance"), 1e-12);
		assertEquals(2, stats.get("R"), 1e-12);
		assertEquals(0.26136 * 10 / Math.sqrt(100), stats.get("standardError"),
			1e-5);
		assertTrue(stats.get("z") > 10);
		assertTrue(stats.get("p") < 1e-10);
	}

	@Test
	public void testExplicitVolume() {
		// Quadrupling the area doubles the expected distance and halves R.
		final var stats = ops.op("labeling.clarkEvans").input(grid(10, 10),
			40000.0).outType(STATS).apply();
		assertEquals(10, stats.get("expectedMeanDistance"), 1e-12);
		assertEquals(1, stats.get("R"), 1e-12);
		assertEquals(0, stats.get("z"), 1e-12);
		assertEquals(1, stats.get("p"), 1e-12);
	}

	@Test
	public void testCalibration() {
		// Doubling the spacing doubles all distances, and the default volume is
		// calibrated too, so R is unchanged.
		final var stats = ops.op("labeling.clarkEvans").input(grid(10, 10), null,
			new double[] { 2, 2 }).outType(STATS).apply();
		assertEquals(20, stats.get("observedMeanDistance"), 1e-12);
		assertEquals(10, stats.get("expectedMeanDistance"), 1e-12);
		assertEquals(2, stats.get("R"), 1e-12);
	}

	@Test
	public void testRandomAndClustered() {
		final var random = new Random(0xC1A4C);
		final var randomStats = ops.op("labeling.clarkEvans").input(randomPoints(
			random, 500, 500, 0)).outType(STATS).apply();
		assertEquals(1, randomStats.get("R"), 0.1);
		final var clusteredStats = ops.op("labeling.clarkEvans").input(
			randomPoints(random, 500, 500, 100)).outType(STATS).apply();
		assertTrue(clusteredStats.get("R") < 0.3);
		assertTrue(clusteredStats.get("p") < 1e-10);
	}

	@Test
	public void test3DExpectedDistance() {
		// In 3D, the expected distance for density lambda is
		// Gamma(4/3) / (4 pi lambda / 3)^(1/3) ~= 0.55396 lambda^(-1/3).
		final var index = ArrayImgs.ints(20, 20, 20);
		index.randomAccess().setPositionAndGet(2, 2, 2).set(1);
		index.randomAccess().setPositionAndGet(12, 2, 2).set(2);
		final var stats = ops.op("labeling.clarkEvans").input(labeling(index, 2))
			.outType(STATS).apply();
		final var lambda = 2.0 / 8000;
		assertEquals(0.55396 * Math.pow(lambda, -1.0 / 3), stats.get(
			"expectedMeanDistance"), 1e-3);
		assertEquals(10, stats.get("observedMeanDistance"), 1e-12);
	}
}
