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

package org.scijava.ops.image.segment.regionGrowing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@link SeededRegionGrowing}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class SeededRegionGrowingTest extends AbstractOpTest {

	private static final Nil<ImgLabeling<Integer, IntType>> LABELS =
		new Nil<>()
		{};

	private static final Nil<ImgLabeling<String, IntType>> STRING_LABELS =
		new Nil<>()
		{};

	/** Two noisy regions separated by a step are recovered from two seeds. */
	@Test
	public void testNoisyStep() {
		final var rnd = new Random(1);
		final long w = 30, h = 20;
		final var in = ArrayImgs.doubles(w, h);
		final var c = in.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set((c.getLongPosition(0) < 12 ? 50 : 150) + 10 * rnd
				.nextGaussian());
		}
		for (final var se : new StructuringElement[] {
			StructuringElement.FOUR_CONNECTED, StructuringElement.EIGHT_CONNECTED })
		{
			final var seeds = new ImgLabeling<String, IntType>(ArrayImgs.ints(w, h));
			final var ra = seeds.randomAccess();
			ra.setPositionAndGet(2, 10).add("dark");
			ra.setPositionAndGet(25, 3).add("bright");
			final ImgLabeling<String, IntType> out = ops.op(
				"segment.seededRegionGrowing").input(in, seeds, se)
				.outType(STRING_LABELS).apply();
			final var oc = Views.flatIterable(out).localizingCursor();
			while (oc.hasNext()) {
				final var set = oc.next();
				assertEquals(1, set.size());
				assertEquals(oc.getLongPosition(0) < 12 ? "dark" : "bright", set
					.iterator().next(), se.toString());
			}
		}
	}

	/**
	 * In 3D, a bright sphere seeded at its center and a background seed give
	 * the sphere; the seeds keep their labels.
	 */
	@Test
	public void testSphere3D() {
		final var in = ArrayImgs.unsignedBytes(15, 15, 15);
		final var c = in.localizingCursor();
		var inside = 0;
		while (c.hasNext()) {
			c.fwd();
			var r2 = 0.0;
			for (var d = 0; d < 3; d++)
				r2 += Math.pow(c.getDoublePosition(d) - 7, 2);
			if (r2 <= 16) {
				c.get().set(200);
				inside++;
			}
			else c.get().set(30);
		}
		final var seeds = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(15, 15,
			15));
		final var ra = seeds.randomAccess();
		ra.setPositionAndGet(7, 7, 7).add(1);
		ra.setPositionAndGet(0, 0, 0).add(2);
		ra.setPositionAndGet(14, 0, 14).add(2);
		final ImgLabeling<Integer, IntType> out = ops.op(
			"segment.seededRegionGrowing").input(in, seeds)
			.outType(LABELS).apply();
		var count = 0;
		final var oc = Views.flatIterable(out).cursor();
		final var ic = Views.flatIterable(in).cursor();
		while (oc.hasNext()) {
			final var label = oc.next().iterator().next();
			final var v = ic.next().get();
			assertEquals(v == 200 ? 1 : 2, label);
			if (label == 1) count++;
		}
		assertEquals(inside, count);
	}

	@Test
	public void testRegionMeansGuideGrowth() {
		// A step between two seeds: the boundary falls at the step.
		final var in = ArrayImgs.doubles(21, 1);
		final var c = in.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			c.get().set(c.getDoublePosition(0) < 7 ? 0 : 100);
		}
		final var seeds = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(21, 1));
		final var ra = seeds.randomAccess();
		ra.setPositionAndGet(0, 0).add(1);
		ra.setPositionAndGet(20, 0).add(2);
		final ImgLabeling<Integer, IntType> out = ops.op(
			"segment.seededRegionGrowing").input(in, seeds)
			.outType(LABELS).apply();
		final var oc = Views.flatIterable(out).localizingCursor();
		while (oc.hasNext()) {
			final var set = oc.next();
			assertTrue(!set.isEmpty());
			assertEquals(oc.getLongPosition(0) < 7 ? 1 : 2, set.iterator().next());
		}
	}

	/**
	 * Images containing NaN (which has no order) and seeds with several labels
	 * at one pixel (whose "first" label is not well-defined) are rejected.
	 */
	@Test
	public void testInvalidInputs() {
		final var in = ArrayImgs.doubles(5, 1);
		final var seeds = new ImgLabeling<Integer, IntType>(ArrayImgs.ints(5, 1));
		final var ra = seeds.randomAccess();
		ra.setPositionAndGet(0, 0).add(1);
		ra.setPositionAndGet(4, 0).add(2);
		in.firstElement().set(Double.NaN);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"segment.seededRegionGrowing").input(in, seeds).outType(LABELS)
			.apply());
		in.firstElement().set(0);
		ra.setPositionAndGet(4, 0).add(3);
		assertThrows(IllegalArgumentException.class, () -> ops.op(
			"segment.seededRegionGrowing").input(in, seeds).outType(LABELS)
			.apply());
	}
}
