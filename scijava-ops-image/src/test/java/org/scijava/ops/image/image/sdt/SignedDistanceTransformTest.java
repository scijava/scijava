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

package org.scijava.ops.image.image.sdt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Nil;

/**
 * Tests {@code image.signedDistanceTransform}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class SignedDistanceTransformTest extends AbstractOpTest {

	private static final Nil<RandomAccessibleInterval<FloatType>> OUT =
		new Nil<>()
		{};

	@Test
	public void testSquare() {
		// A 5 x 5 square at [5, 9]^2 in a 15 x 15 image.
		final var mask = ArrayImgs.bits(15, 15);
		final var c = mask.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final long x = c.getLongPosition(0), y = c.getLongPosition(1);
			c.get().set(x >= 5 && x <= 9 && y >= 5 && y <= 9);
		}
		final RandomAccessibleInterval<FloatType> sdt = ops.op(
			"image.signedDistanceTransform").input(mask).outType(OUT).apply();
		final var ra = sdt.randomAccess();
		assertEquals(0.5, ra.setPositionAndGet(5, 7).get(), 1e-6);
		assertEquals(-0.5, ra.setPositionAndGet(4, 7).get(), 1e-6);
		assertEquals(2.5, ra.setPositionAndGet(7, 7).get(), 1e-6);
		assertEquals(-(Math.sqrt(50) - 0.5), ra.setPositionAndGet(0, 0).get(),
			1e-5);
		// Positive exactly inside the mask.
		final var mc = mask.cursor();
		for (final var t : Views.flatIterable(sdt))
			assertEquals(mc.next().get(), t.get() > 0);

		// Anisotropic calibration stretches the x distances.
		final RandomAccessibleInterval<FloatType> cal = ops.op(
			"image.signedDistanceTransform").input(mask, new double[] { 2, 1 })
			.outType(OUT).apply();
		final var ca = cal.randomAccess();
		assertEquals(-(4 - 0.5), ca.setPositionAndGet(3, 7).get(), 1e-5);
		assertEquals(-(2 - 0.5), ca.setPositionAndGet(7, 3).get(), 1e-5);
	}

	@Test
	public void testUniformMasks() {
		final var empty = ArrayImgs.bits(4, 3);
		final RandomAccessibleInterval<FloatType> e = ops.op(
			"image.signedDistanceTransform").input(empty).outType(OUT).apply();
		for (final var t : Views.iterable(e))
			assertTrue(t.get() < -4);
		final var full = ArrayImgs.bits(4, 3);
		for (final var b : full)
			b.set(true);
		final RandomAccessibleInterval<FloatType> f = ops.op(
			"image.signedDistanceTransform").input(full).outType(OUT).apply();
		for (final var t : Views.iterable(f))
			assertTrue(t.get() > 4);
	}
}
